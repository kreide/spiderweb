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

import java.io.File;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.medallia.spider.IRenderTask;
import com.medallia.spider.SpiderServlet;
import com.medallia.spider.Task;
import com.medallia.tiny.web.JettyWebRunner;
import com.medallia.tiny.web.ServletContextAdapter;


/**
 * Abstract TestCase class for testing {@link IRenderTask} implementations.
 * <p>
 * 
 * Each web application should create a common subclass of this class used
 * by all its test cases. That subclass should implement
 * <p>
 * 
 *   {@link StRenderTestCase#getServletMock(Class)}
 *   <p>
 *   
 * Typically this method will simply call
 * <p>
 * 
 *   {@link #makeServletMock(Class)}
 *   <p>
 *   
 * with a class that subclasses the servlet class and instantiates mock classes
 * for all the services available for dependency injection.
 */
public abstract class RenderTaskTestCase extends StRenderTestCase<IRenderTask> {

	@Override protected String uriForTask(Class<? extends IRenderTask> ct) {
		return "/" + Task.uriNameForTask(ct);
	}

	/** @return a ServletMock that forwards request to an instance of the given class */
	protected ServletMock makeServletMock(Class<? extends SpiderServlet> servletClass) throws Exception {
		final SpiderServlet servlet = servletClass.newInstance();
		servlet.init(new ServletConfig() {
			public String getServletName() { return null; }
			public String getInitParameter(String arg0) { return null; }
			public Enumeration getInitParameterNames() { return null; }
			public ServletContext getServletContext() {
				return new ServletContextAdapter() {
					@Override public String getRealPath(String relativePath) {
						return new File(JettyWebRunner.findWebRoot(), relativePath).getAbsolutePath();
					}
				};
			}
		});
		return new ServletMock() {
			public void service(HttpServletRequest req, HttpServletResponse res) throws Exception {
				servlet.service(req, res);
			}
			public void destroy() {
				servlet.destroy();
			}
		};
	}
	
}
