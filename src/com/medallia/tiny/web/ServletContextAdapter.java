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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.medallia.tiny.Implement;


/** empty implementation of ServletContext */
public class ServletContextAdapter implements ServletContext {

	@Implement public Object getAttribute(String arg0) {
		return null;
	}

	@Implement public Enumeration getAttributeNames() {
		return null;
	}

	@Implement public ServletContext getContext(String arg0) {
		return null;
	}

	@Implement public String getInitParameter(String arg0) {
		return null;
	}

	@Implement public Enumeration getInitParameterNames() {
		return null;
	}

	@Implement public int getMajorVersion() {
		return 0;
	}

	@Implement public String getMimeType(String arg0) {
		return null;
	}

	@Implement public int getMinorVersion() {
		return 0;
	}

	@Implement public RequestDispatcher getNamedDispatcher(String arg0) {
		return null;
	}

	@Implement public String getRealPath(String arg0) {
		return null;
	}

	@Implement public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	@Implement public URL getResource(String arg0) throws MalformedURLException {
		return null;
	}

	@Implement public InputStream getResourceAsStream(String arg0) {
		return null;
	}

	@Implement public Set getResourcePaths(String arg0) {
		return null;
	}

	@Implement public String getServerInfo() {
		return null;
	}

	@Implement public Servlet getServlet(String arg0) throws ServletException {
		return null;
	}

	@Implement public String getServletContextName() {
		return null;
	}

	@Implement public Enumeration getServletNames() {
		return null;
	}

	@Implement public Enumeration getServlets() {
		return null;
	}

	@Implement public void log(String arg0) {
	}

	@Implement public void log(Exception arg0, String arg1) {
	}

	@Implement public void log(String arg0, Throwable arg1) {
	}

	@Implement public void removeAttribute(String arg0) {
	}

	@Implement public void setAttribute(String arg0, Object arg1) {
	}

}
