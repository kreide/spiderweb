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
package com.medallia.spider.sttools;

import org.antlr.stringtemplate.StringTemplate;

/**
 * Virtual sub-template that allows callbacks to Java code from StringTemplate.
 * 
 * Normally this is not a good thing, but in some cases it is still the lesser evil.
 */
public interface StTool {
	
	/** @return the rendered sub-template */
	Object render(StringTemplate st);

}
