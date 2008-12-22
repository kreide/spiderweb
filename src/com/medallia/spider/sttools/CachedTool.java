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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.antlr.stringtemplate.StringTemplate;

import com.medallia.spider.StaticResources.StaticResource;
import com.medallia.spider.StaticResources.StaticResourceLookup;
import com.medallia.tiny.Encoding;
import com.medallia.tiny.Implement;


/**
 * Tool that is supposed to add a hash to a link to a static resource
 * to handle browser cache invalidation.
 * <p>
 * 
 * Note: not yet implemented, simply returns the link unmodified.
 *
 */
public class CachedTool implements StTool {
	
	private final StaticResourceLookup srl;
	
	public CachedTool(StaticResourceLookup srl) {
		this.srl = srl;
	}

	@Implement public String render(StringTemplate st) {
		String resourceName = String.valueOf(st.getAttribute("it"));
		StaticResource sr = srl.findStaticResource(resourceName);
		
		if (sr != null) {
			// copy into buffer and calculate md5
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			try {
				sr.copyTo(buffer);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			// create and return link
			String md5 = Encoding.md5(buffer.toByteArray());
			
			return resourceName + "?" + md5;
		}
		
		throw new RuntimeException("Resource not found: " + resourceName);
	}

}
