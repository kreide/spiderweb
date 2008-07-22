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

import org.antlr.stringtemplate.StringTemplateErrorListener;

/**
 * Throws RuntimeException if anything bad happens; StringTemplate by default logs all errors
 * and then proceeds to print your raw template on toString() (!!)
 * 
 * @author kristian
 */
public class ExplodingStringTemplateErrorListener implements StringTemplateErrorListener {
	
	public static final StringTemplateErrorListener LISTENER = new ExplodingStringTemplateErrorListener();
	
	public void error(String msg, Throwable t) {
		throw new RuntimeException(msg, t);
	}
	public void warning(String msg) {
		throw new RuntimeException("Warning: " + msg);
	}
	

}
