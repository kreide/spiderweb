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
package com.medallia.spider.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import com.medallia.spider.api.StRenderable;
import com.medallia.tiny.Encoding;
import com.medallia.tiny.Strings;
import com.medallia.tiny.test.TestCaseWithFixtures;


/**
 * Abstract TestCase class for test cases that do "black box" testing of the
 * modules. The test class should call {@link #action()} to simulate a web
 * request; {@link #action(Map)} can be used to send in request parameters.
 * <p>
 * 
 * Normally the class name of the test class is used to map to the correct
 * module, but the the {@link #action(Class)} can be used instead.
 * <p>
 * 
 * Normally each web application creates a common base class for all its
 * test cases that takes care of setting up the test environment, e.g.
 * create mock objects for the services that are dependency injected.
 *
 * @param <X> type of the {@link StRenderable} used by the test case
 */
public abstract class StRenderTestCase<X extends StRenderable> extends TestCaseWithFixtures {
	
	/** result of the actionAndRender method - can be either a redirect or the content of the StringTemplate render operation */
	public interface StRenderResult {
		/** @return true if the result is a redirect */
		boolean isRedirect();
		/** @return the redirect */
		String getRedirect();
		/** @return the content of the StringTemplate render operation */
		String getStContent();
		/** @return the binary content if the task produced any */
		byte[] getBinaryContent();
	}
	
	/** @return result of Action - no request parameters */
	protected StRenderResult action() throws Exception {
		return action(Collections.<String, String>emptyMap());
	}
	/** @return result of Action - request parameters passed in the given map */
	protected StRenderResult action(final Map<String, String> params) throws Exception {
		return action(getStRenderableClass(), params);
	}
	/** @return result of Action on the given class - no request parameters */
	protected StRenderResult action(Class<? extends X> renderableClass) throws Exception {
		return action(renderableClass, Collections.<String, String>emptyMap());
	}
	
	/** @return result of Action on the given instance; request parameters passed in the given map */
	protected StRenderResult action(final Class<? extends X> renderableClass, final Map<String, String> params) throws Exception {
		HttpServletRequest request = new HttpServletRequestWrapper(nullProxyForInterface(HttpServletRequest.class)) {
			@Override public String getMethod() { return "GET"; }
			@Override public String getRequestURI() {
				return uriForTask(renderableClass);
			}
			@Override public String getContextPath() { return ""; }
			@Override public Map getParameterMap() { return params; }
			@Override public Cookie[] getCookies() { return new Cookie[0]; }
			@Override public HttpSession getSession() { return nullProxyForInterface(HttpSession.class); }
			@Override public HttpSession getSession(boolean create) { return getSession(); }
			@Override public Object getAttribute(String name) { return null; }
			@Override public Enumeration getAttributeNames() { return Collections.enumeration(Collections.emptySet()); }
			@Override public String getHeader(String name) {
				if ("Referer".equals(name))
					return "http://" + renderableClass.getName() + "-test";
				return super.getHeader(name);
			}
		};
		
		final ByteArrayOutputStream w = new ByteArrayOutputStream();
		final String[] redirect = new String[1];
		
		HttpServletResponse response = new HttpServletResponseWrapper(nullProxyForInterface(HttpServletResponse.class)) {
			@Override public void sendRedirect(String location) throws IOException {
				redirect[0] = location;
			}
			@Override public ServletOutputStream getOutputStream() throws IOException {
				return new ServletOutputStream() {
					@Override public void write(int b) throws IOException {
						w.write(b);
					}
				};
			}
			@Override public PrintWriter getWriter() throws IOException {
				return new PrintWriter(w);
			}
			@Override public String encodeURL(String s) { return s; }
			@Override public String getCharacterEncoding() { return "utf8"; }
			@Override public boolean isCommitted() { return false; }
		};
		
		servletMock.service(request, response);
		
		return new StRenderResult() {
			public boolean isRedirect() { return getRedirect() != null; }
			public String getRedirect() { return redirect[0]; }
			public String getStContent() {
				return Encoding.fromUTF8Bytes(getBinaryContent());
			}
			public byte[] getBinaryContent() {
				return w.toByteArray();
			}
		};
	}
	
	/** @return a {@link Proxy} implementation of the given interface where all methods return null */
	public static <X> X nullProxyForInterface(Class<X> x) {
		return x.cast(Proxy.newProxyInstance(x.getClassLoader(), new Class<?>[] { x }, new InvocationHandler() {
			public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
				return null;
			}
		}));
	}


	/** Mock class for the Servlet */
	public interface ServletMock {
		/** called with mock request and response objects */
		void service(HttpServletRequest req, HttpServletResponse res) throws Exception;
		/** release any allocated resources */
		void destroy();
	}
	
	private ServletMock servletMock;
	
	@Override protected void safeUp() throws Exception {
		this.servletMock = getServletMock();
	}
	@Override protected void safeDown() throws Exception {
		this.servletMock.destroy();
		this.servletMock = null;
	}
	
	/** @return ServletMock that will be used in the test */
	protected abstract ServletMock getServletMock() throws Exception;
	
	/** @return the URI that maps to the given class */
	protected abstract String uriForTask(Class<? extends X> ct);
	
	/** @return the StAction class to be tested; defaults to the enclosing class. */
	@SuppressWarnings("unchecked")
	protected Class<? extends X> getStRenderableClass() {
		Class<? extends StRenderTestCase> testClass = getClass();
		Class<?> stClass = testClass.getEnclosingClass();
		if (stClass == null) {
			String s = testClass.getSimpleName();
			if (s.endsWith("Test")) {
				s = s.substring(0, s.length() - 4);
				List<String> packages = Arrays.asList(testClass.getPackage().getName().split("\\."));
				for (int i = packages.size(); i > 0; i--) {
					try {
						stClass = Class.forName(Strings.join(".", packages.subList(0, i)) + "." + s);
						break;
					} catch (ClassNotFoundException e) {
						// try again
					}
				}
			}
		}
				
		if (stClass == null) {
			throw new RuntimeException("Please override this method");
		}
		return (Class<? extends X>) stClass;
	}
	
	/** special string that can be prepended to each content string to signal that
	 * the rest of the string should not be present.
	 */
	protected static final String NOT = "!!!";
	
	/** assert that the RenderResult has the given content */
	protected void assertHasContent(StRenderResult rr, String... contents) {
		assertHas(rr.getStContent(), contents);
	}
	
	/** assert that the RenderResult is of type redirect and which has the given redirect */
	protected void assertRedirectTo(StRenderResult rr, String... redirect) {
		assertHas(rr.getRedirect(), redirect);
	}
	
	/** assert that the string has the given content */
	private void assertHas(String str, String... contents) {
		for (String content : contents) {
			if (content != null)
				if (content.startsWith(NOT))
					assertFalse("Found " + content + " in " + str, str.contains(content.substring(NOT.length())));
				else
					assertTrue("Did not find " + content + " in: " +str, str.contains(content));
		}
	}

}
