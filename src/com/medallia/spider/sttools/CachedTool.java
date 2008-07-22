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

import com.medallia.tiny.Implement;
import com.medallia.tiny.string.HtmlString;


/**
 * Tool that is supposed to add a hash to a link to a static resource
 * to handle browser cache invalidation.
 * <p>
 * 
 * Note: not yet implemented, simply returns the link unmodified.
 *
 */
public class CachedTool implements StTool {

	@Implement public HtmlString render(StringTemplate st) {
		// XXX: Implement
		String r = String.valueOf(st.getAttribute("it"));
		@SuppressWarnings("deprecation")
		HtmlString html = HtmlString.rawUnsafe(r);
		return html;
	}

}
