/*
 * This file is part of the Spider Web Framework.
 * 
 * The Spider Web Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Spider Web Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Spider Web Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.medallia.tiny.string;

import java.util.HashMap;

import org.antlr.stringtemplate.AttributeRenderer;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.lang.StringEscapeUtils;

import com.medallia.tiny.Empty;
import com.medallia.tiny.Implement;

/**
 * Builder for StringTemplate objects. Also has a few utility functions for working with StringTemplates.
 * 
 * @author kristian
 */
public class StringTemplateBuilder {

	private final HashMap<String, Object> attr = Empty.hashMap();
	private boolean escapeHtml;
	
	private SymbolNotFoundListener symbolNotFoundListener;
	/** listener for when a symbol in a StringTemplate is not found */
	public static interface SymbolNotFoundListener {
		/** called when a symbol is not found */
		void symbolNotFound(String symbol);
	}
	/** set the listener for symbol not found */
	public StringTemplateBuilder setSymbolNotFoundListener(SymbolNotFoundListener l) {
		this.symbolNotFoundListener = l;
		return this;
	}

	private StringTemplateBuilder() {
	}
	

	/** call to turn on HTML escaping */
	public StringTemplateBuilder escapeHtml() {
		escapeHtml = true;
		return this;
	}

	/** Make a new StringTemplateBuilder */
	public static StringTemplateBuilder t() {
		return new StringTemplateBuilder();
	}

	/** set a StringTemplate attribute */
	public StringTemplateBuilder attr(String name, Object obj) {
		attr.put(name, obj);
		return this;
	}

	/** Make a StringTemplate from the given template string */
	public StringTemplate go(String template) {
		StringTemplate st;
		if (symbolNotFoundListener != null) {
			st = new StringTemplate(template, DefaultTemplateLexer.class) {
				@Override public Object get(StringTemplate self, String attribute) {
					Object o = super.get(self, attribute);
					if (self == this && o == null) {
						symbolNotFoundListener.symbolNotFound(attribute);
					}
					return o;
				}			
			};
		} else {
			st = new StringTemplate(template, DefaultTemplateLexer.class);
		}
		st.setAttributes(attr);
		st.setErrorListener(ExplodingStringTemplateErrorListener.LISTENER);
		if (escapeHtml) {
			st.registerRenderer(String.class, new SimpleAttributeRenderer() {
				public String toString(Object o) {
					return StringEscapeUtils.escapeHtml(String.valueOf(o));
				}
			});
		}
		return st;
	}
	
	/** Convenience method for when you do not want to set attributes using the builder */
	public static StringTemplate st(String template) {
		return t().go(template);
	}

	/** Throws RuntimeException if the given string is not a valid StringTemplate */
	public static void verifyValidTemplate(String template) {
		StringTemplate st = st("");
		st.setErrorListener(new StringTemplateErrorListener() {
			public void error(String s, Throwable ex) {
				// dig deep for a reasonable error string
				Throwable last = null;
				for (Throwable t = ex; t != null; t = t.getCause()) 
					last = t;
				throw new RuntimeException(last != null ? last.getMessage() : s, ex);
			}
			public void warning(String s) {
				throw new RuntimeException(s);
			}
		});
		st.setTemplate(template);
	}

	/** Use this for passing the given string through StringTemplate without any attributes set. */
	public static String renderNull(String str) {
		return st(str).toString();
	}
	
	
	/** implementation of AttributeRenderer that forwards the
	 * {@link #toString(Object, String)} call to {@link #toString(Object)}
	 */
	public abstract static class SimpleAttributeRenderer implements AttributeRenderer {
		@Implement public String toString(Object s, String format) {
			return toString(s);
		}
	}

}
