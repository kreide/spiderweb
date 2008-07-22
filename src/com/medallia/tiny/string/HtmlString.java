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

import java.util.Arrays;
import java.util.Date;

import org.antlr.stringtemplate.AttributeRenderer;

import com.medallia.tiny.Strings;
import com.medallia.tiny.string.StringTemplateBuilder.SimpleAttributeRenderer;

/**
 * Ok, *this* is horrible. An attempt to abuse the Java type checker to look for cross-side scripting and stability errors.
 * 
 * The deal is that all objects that aren't htmlStrings will be escaped on output; only compile-time constant strings can be converted to HtmlString.
 */
public class HtmlString extends StringBase implements Htmlable {
	public static final char NBSP = (char)160;
	
	/** String template attribute renderer used for rendering HtmlStrings. */
	public static final AttributeRenderer ST_RENDERER = new SimpleAttributeRenderer() {
		public String toString(Object o) {
			return ((HtmlString)o).asString();
		}
	};
	
	public static HtmlString format(String s, Object... more) {
		assertConstant(s);
		for (int i = more.length; i-- > 0;) {
			more[i] = escapeIfNecessary(more[i]);
		}
		return new HtmlString(String.format(s, more)); 
	}

	public static Object escapeIfNecessary(Object o) {
		if (o==null || o instanceof Number || o instanceof Date) return o;
		if (o instanceof Htmlable) return ((Htmlable) o).getHtml().asString();
		return escape(o.toString());
	}

	/**
	 * Concatenate strings -- those that are not Htmlable will be escaped.
	 * @param cs List of strings
	 * @return
	 */
	public static HtmlString cat(CharSequence... cs) {
		return cat(Arrays.asList(cs));
	}
	/**
	 * Concatenate strings -- those that are not Htmlable will be escaped.
	 * @param cs List of strings
	 * @return
	 */
	public static HtmlString cat(Iterable<CharSequence> cs) {
		StringBuffer sb = new StringBuffer();
		for (CharSequence c : cs) {
			if (c==null) continue;
			sb.append(escapeIfNecessary(c));
		}
		return new HtmlString(sb.toString());
	}

	
	public static HtmlString fromText(String s) {		
		return new HtmlString(escape(s));
	}
	public static HtmlString constant(String s) {
		assertConstant(s);
		return new HtmlString(s);
	}
	
	private static volatile boolean debugMode = false;
	
	/** call to enable debug mode. In this mode expensive checks are made to catch programming
	 * problems, e.g. that {@link #constant(String)} is only called with constant strings.
	 */
	public static void enableDebugMode() {
		debugMode = true;
	}
	
	private static void assertConstant(String s2) {
		if (debugMode) {
			String copy = new String(s2);
			if (copy.intern() != s2) throw new RuntimeException("That string is fraudulent: "+s2);
		}
	}
	
	/** needed by Serializable */
	private HtmlString() { }
	
	private HtmlString(String s) {
		super(s);
	}
	
	public CharSequence subSequence(int arg0, int arg1) {
		return new HtmlString(s.substring(arg0, arg1));
	}
	
	public HtmlString getHtml() {
		return this;
	}
	
	public String asString() {
		return s;
	}
	
	public boolean isEmpty() {
		return !Strings.hasContent(asString());
	}
	
	public static String escape(String s) {
		if (s==null) return null;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 128 || !safeForHtml[c]) {
				if (c=='&') sb.append("&amp;");
				else if (c=='<') sb.append("&lt;");
				else if (c=='>') sb.append("&gt;");
				else {
					sb.append("&#");
					sb.append((int)c);
					sb.append(";");
				}
			} else sb.append(c);
		}
		return sb.toString();
	} 
	static boolean safeForHtml[] = new boolean[128];
	static {
		for (char c = 32; c<126; c++)
			safeForHtml[c] = "<>'\\\"&".indexOf(c)<0;
	}
	@Deprecated
	public static HtmlString rawUnsafe(String s) {
		return new HtmlString(s);
	}

	public static HtmlString spaceIndent(int i, CharSequence cs) {
		char c[] = new char[i];
		Arrays.fill(c, NBSP);
		return HtmlString.cat(new String(c), cs);
	}
}