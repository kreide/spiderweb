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
package com.medallia.spider;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.medallia.spider.api.StRenderable;
import com.medallia.tiny.CollUtils;
import com.medallia.tiny.Empty;
import com.medallia.tiny.Encoding;
import com.medallia.tiny.Implement;

/**
 * Abstract implementation of {@link ITask} that all tasks will normally
 * inherit. Has several convenience methods for common actions.
 */
public abstract class Task implements ITask {
	
	/** @return a PostAction pointing at the given template, which will be used for rendering instead of the default */
	public static StTemplatePostAction template(final String templateName) {
		return new StTemplatePostAction() {
			public String templateName() { return templateName; }
		};
	}

	/** @return a PostAction which sends the given string, as UTF-8; no template is used */
	public static PostAction rawStringUtf8(final String str) {
		return new BinaryDataPostAction() {
			@Override protected String getContentType() { return "text/plain"; }
			@Override protected void writeTo(OutputStream out) throws IOException {
				out.write(Encoding.getUTF8Bytes(str));
			}
		};
	}

	/** @return a PostAction which redirects to the HTTP referer, i.e. the action the client came from */
	public static PostAction redirectToReferer() {
		return new CustomPostAction() {
			public void respond(HttpServletRequest req, HttpServletResponse res) throws IOException {
				res.sendRedirect(req.getHeader("Referer"));
			}
		};
	}
	
	/** @return a PostAction which redirects to given task without any query parameters */
	public static PostAction redirectToTask(final Class<? extends IRenderTask> ct) {
		return redirectToTask(ct, null);
	}
	/** @return a PostAction which redirects to given task with the given query string */
	public static PostAction redirectToTask(final Class<? extends IRenderTask> ct, final String qs) {
		return new CustomPostAction() {
			public void respond(HttpServletRequest req, HttpServletResponse res) throws IOException {
				URL url = new URL(req.getRequestURL().toString());
				String path = url.getPath();
				String uri = path.substring(0, path.lastIndexOf('/') + 1) + uriNameForTask(ct);
				if (qs != null)
					uri += "?" + qs;
				url = new URL(url.getProtocol(), url.getHost(), url.getPort(), uri);
				res.sendRedirect(url.toExternalForm());
			}
		};
	}
	
	/** @return the URI name that maps to the given class, e.g. 'foo' for FooTask */
	public static String uriNameForTask(Class<? extends IRenderTask> ct) {
		String sn = ct.getSimpleName();
		sn = sn.substring(0, sn.length()-4);
		return sn.substring(0, 1).toLowerCase() + sn.substring(1);
	}

	/** Forwards to {@link V#v()} for convenience; see {@link StRenderable} for doc. */
	public static <X> V<X> v() {
		return V.v();
	}
	
	@Implement public Class<?> getClassForTemplateName() {
		return getClass();
	}
	
	private final Map<V<?>, Object> attrs = Empty.hashMap();
	
	/** set an attribute available in the StringTemplate
	 * 
	 * @param <X> type of the object to set
	 * @param v the typetag
	 * @param obj the object to set
	 * @return the given object
	 */
	protected <X> X attr(V<X> v, X obj) {
		attrs.put(v, obj);
		return obj;
	}

	@Implement public <X> X getAttr(V<X> tag) {
		@SuppressWarnings("unchecked")
		X x = (X) attrs.get(tag);
		return x;
	}
	
	@Implement public boolean hasAttr(V<?> tag) {
		return attrs.containsKey(tag);
	}
	
	/** PostAction that does some custom processing */
	public interface CustomPostAction extends PostAction {
		/** do custom processing */
		void respond(HttpServletRequest req, HttpServletResponse res) throws IOException;
	}
	
	/** PostAction that sends binary data, e.g. a dynamic image, to the client */
	public abstract static class BinaryDataPostAction implements CustomPostAction {
		protected abstract String getContentType();
		
		/** write data to the given OutputStream */
		protected abstract void writeTo(OutputStream out) throws IOException;
		
		@Implement public void respond(HttpServletRequest req, HttpServletResponse res) throws IOException {
			res.setContentType(getContentType());
			writeTo(res.getOutputStream());
		}
	}
	
	@Implement public Collection<EmbeddedRenderTask> dependsOn() {
		return with();
	}
	
	/** convenience method for returning embedded task instances */
	public Collection<EmbeddedRenderTask> with(EmbeddedRenderTask... tasks) {
		return Empty.list(Arrays.asList(tasks));
	}
	/** convenience method for returning embedded task instances */
	public Collection<EmbeddedRenderTask> with(EmbeddedRenderTask[] ta, EmbeddedRenderTask... tasks) {
		return CollUtils.concat(Arrays.asList(tasks), Arrays.asList(ta));
	}
	
}
