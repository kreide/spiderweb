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
package com.medallia.tiny.web;

import javax.servlet.http.HttpServletResponse;

/** Http Response Header utilities */
public class HttpHeaders {

	/**
	 * Add necessary headers to make the response be cached as long as possible.
	 * 
	 * Note that this method is intended for URLs which include a version number
	 * or hash of the content they refer to.
	 */
	public static void addCacheForeverHeaders(HttpServletResponse response) {
		assert !response.isCommitted() : "Cannot change committed response";
		response.setHeader("Pragma", "cache");
		response.setHeader("Expires", "Tue, 31 Dec 2030 00:00:00 GMT");
		response.setHeader("Cache-Control", "max-age=946080000, public, cache");		
	}
	
	/**
	 * Add necessary headers to make the response not be cached by the client.
	 */
	public static void addNoCacheHeaders(HttpServletResponse response) {
		assert !response.isCommitted() : "Cannot change committed response";
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Expires", "Mon, 1 Jan 2007 08:00:00 GMT");
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
	}

}