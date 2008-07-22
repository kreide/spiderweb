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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.AutoIndentWriter;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.medallia.spider.Task.CustomPostAction;
import com.medallia.spider.api.StRenderable;
import com.medallia.spider.api.StRenderer;
import com.medallia.spider.api.StRenderable.PostAction;
import com.medallia.spider.api.StRenderer.StRenderPostAction;
import com.medallia.spider.sttools.CachedTool;
import com.medallia.spider.sttools.StTool;
import com.medallia.spider.test.RenderTaskTestCase;
import com.medallia.tiny.Clock;
import com.medallia.tiny.Empty;
import com.medallia.tiny.MimeType;
import com.medallia.tiny.ObjectProvider;
import com.medallia.tiny.Strings;
import com.medallia.tiny.string.ExplodingStringTemplateErrorListener;
import com.medallia.tiny.string.HtmlString;
import com.medallia.tiny.web.HttpHeaders;

/**
 * Spider is a framework for creating web applications. Its major design goals are to:
 * <ul>
 * 
 * <li> Make it trivially easy to write good test cases
 * <li> Reduce boilerplate code to a minimum
 * <li> Avoid static state through dependency injection
 * <li> Have strict M-V-C separation
 * <li> Prefer convention over configuration
 * </ul>
 * 
 * Different techniques are used to realize each of these goals and are documented below. Here is an overview
 * of the steps required to get started with spider:
 * <ul>
 * 
 * <li> Create a subclass of {@link SpiderServlet}. This class should be referenced in the web.xml file. For
 *      a project called "Foo" this class will typically be located in a package called "foo.web", although
 *      this is not a requirement.
 *   
 * <li> Create a class that inherits {@link RenderTask}. See doc on {@link IRenderTask}. By convention
 *      this class should be located in a package called "st" relative to where the servlet class is, e.g.
 *      "foo.web.st". The task class name must end with "Task", e.g. "FooBarTask".
 *     
 * <li> Create a .st file which holds the StringTemplate source. See doc on {@link StRenderable}. This
 *      file should be a resource file located in the same package as the task class and its name should
 *      be the same as that of the class excluding the "Task" prefix, and unlike the class name the
 *      first character should be lower case. E.g. if the task class name is "FooBarTask" the .st file
 *      should be named "fooBar.st".
 * </ul>
 *   
 * Additionally a test case should be created for each subclass of {@link RenderTask}; see doc
 * on {@link spider.test.RenderTaskTestCase}. By convention the test classes should be placed in
 * a package called "st.test" relative to where the servlet class is, and the name of the test class
 * for each task should be the same as that of the task plus the postfix "Test". E.g. "FooBarTaskTest"
 * in package "foo.web.st.test".
 * <p>
 * 
 * <b> Unit testing <br>
 * ============ </b>
 * <p>
 * 
 * One of the most important feature of a framework is to facilitate testing; Spider includes
 * a testing framework that makes it trivial to write comprehensive test cases. See
 * {@link RenderTaskTestCase} for documentation and examples.
 * <p>
 * 
 * <b> Request parameter parsing <br>
 * ========================= </b>
 * <p>
 * 
 * The HTTP protocol is text based, which means that any web application needs to parse request parameters
 * into proper data types; this includes integers, enums and any custom data types defined by each
 * application. This code tends to either be duplicated or at the very least need a method call for each
 * request parameter to convert it into the right data type. Spider handles this via a proxy interface
 * which is dependency injected: see {@link spider.api.StRenderable.Input}. Custom parsers can be
 * registered by overriding @{link {@link #registerInputArgParser(StRenderer)}}.
 * <p>
 * 
 * <b> Dependency injection <br>
 * ==================== </b>
 * <p>
 * 
 * A web application often has several services and / or background tasks that are configured and instantiated
 * when the servlet starts, typically in the {@link #init(ServletConfig)}} method. Since each module of
 * the application typically needs to use a different set of these services a way to get references to
 * the objects is needed. Often this is done by putting the references into static variables or having an
 * object that has references to all the services and pass this object to all modules of the app. Both
 * approaches make it difficult to determine which services a given module needs.
 * <p>
 * 
 * Spider solves this problem by dependency injecting the needed services; the dependencies are thus
 * documented simply as a list of arguments. See {@link StRenderable} for more docs. The objects available
 * for injection can be registered by overriding {@link #registerObjects(ObjectProvider)}.
 * <p>
 *  
 * <b> M-V-C separation <br>
 * ================ </b>
 * <p>
 * 
 * M-V-C is the preferred approach for an application that presents a UI, however, it turns out that, for
 * a number of reasons, it is hard to keep this separation in practice. Spider attempts to solve this
 * problem by using StringTemplate as its templating language. StringTemplate was developed specifically
 * to make a templating language with enough expressive power to make it useful, but no more. The author
 * of StringTemplate wrote a paper going into detail on the motivation for StringTemplate:
 * <p>
 * 
 *   http://www.cs.usfca.edu/~parrt/papers/mvc.templates.pdf
 *   <p>
 *   
 * Spider goes a step further requiring all attributes used in the template be listed using TypeTags;
 * these tags serve both a documentation for the template as well as a type safe way to set attributes.
 * See {@link StRenderable} for more docs.
 * <p>
 * 
 * <b> Convention over configuration <br>
 * ============================= </b>
 * <p>
 * 
 * Paradoxically having no choice can often be liberating; if there is only one way to do something
 * the focus can simply be on actually getting it done.
 * <p>
 * 
 * Configuration for a web application tends to be very repetitive since most teams, if they are well
 * organized, will adopt conventions to avoid having to check configuration files all the time. The
 * configuration is thus copied and pasted each time a new module is added. In addition to being extra
 * work this duplication also increases the maintenance burden.
 * <p>
 * 
 * Spider solves this by having no mandatory configuration (beyond the minimum required for a Java Servlet).
 * A URI maps to a name of a class and the URI is valid if that class exists. Various parameters
 * can be changed, however, but this is typically done by method overriding instead of external configuration
 * files (which cannot be automatically refactored and are not checked at compile time).
 *
 */
public abstract class SpiderServlet extends HttpServlet {
	private static Log log;
	
	private final StringTemplateGroup pageStGroup;
	
	/** constructor that creates the initial state */
	public SpiderServlet() {
		pageStGroup = new StringTemplateGroup("mygroup") {
			@Override public String getFileNameFromTemplateName(String name) {
				return super.getFileNameFromTemplateName(findPathForTemplate(name));
			}
			public StringTemplate getEmbeddedInstanceOf(StringTemplate enclosingInstance, String name) throws IllegalArgumentException {
				final StTool t = getStTool(name);
				if (t != null) return new StringTemplate() {
					public int write(StringTemplateWriter out) throws IOException {
						String s = t.render(this).toString();
						out.write(s);
						return s.length();
					}
				};
				return super.getEmbeddedInstanceOf(enclosingInstance, name);
			}
		};
		pageStGroup.setErrorListener(ExplodingStringTemplateErrorListener.LISTENER);
		StRenderer.registerWebRenderers(pageStGroup);
	}

	/**
	 * @return the URI which a request is redirected to if it does not contain
	 * a valid task name, e.g. 'foo' (assuming there is a FooTask).
	 */
	protected abstract String getDefaultURI();

	/** Register the objects that should be available for dependency injection. The
	 * {@link ObjectProvider#register(Object)} method is typically used.
	 */
	protected void registerObjects(ObjectProvider injector) { }

	/** Register any custom request parameter parsers. The method
	 * {@link StRenderer#registerArgParser(Class, spider.api.StRenderer.InputArgParser)
	 * should be used for this.
	 */
	protected void registerInputArgParser(StRenderer renderer) { }
	
	private boolean debugMode = true;

	/** Set the debug mode on or off. In debug mode the .st files are re-read on each
	 * request and error messages and stack traces may be printed on the rendered
	 * page.
	 * 
	 * The default is true.
	 * 
	 * @param b true if debug mode should be turned on, false otherwise
	 */
	protected void setDebugMode(boolean b) {
		debugMode = b;
		pageStGroup.setRefreshInterval(debugMode ? 0 : Integer.MAX_VALUE);
	}
	
	/** sets up the logging; this is done here instead of in the constructor to give subclasses
	 * a chance to configure log4j.
	 */
	@Override
	public void init(ServletConfig cfg) throws ServletException {
		log = LogFactory.getLog(getClass());
		super.init(cfg);
	}

	/** Forwards to {@link #handleRequest(HttpServletRequest, HttpServletResponse)} */
	@Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		handleRequest(req, res);
	}
	/** Forwards to {@link #handleRequest(HttpServletRequest, HttpServletResponse)} */
	@Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		handleRequest(req, res);
	}
	
	/** Handle a request; exceptions are caught here and sent to {@link #handleException(HttpServletRequest, HttpServletResponse, Throwable)} */
	protected void handleRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
		try {
			handleInternal(req, res);
		} catch (Throwable t) {
			handleException(req, res, t);
		}
	}
	
	/** Handle an exception thrown; this should never happen during normal operation of the app and is in all
	 * cases the result of a programming error.
	 * 
	 * In debug mode the error is printed directly on the respond, otherwise the user is redirected to the generic
	 * error page (error.st).
	 */
	protected void handleException(HttpServletRequest req, HttpServletResponse res, Throwable t) throws IOException {
		log.error("For URI: " + req.getRequestURI(), t);
		if (debugMode) {
			printError(res, t);
		} else {
			res.sendRedirect("error");
		}
	}

	/** print the error */
	protected void printError(HttpServletResponse res, Throwable t) throws IOException {
		PrintWriter w = new PrintWriter(getUtf8Writer(res));
		w.println("<pre>");
		t.printStackTrace(w);
		w.println("</pre>");
		w.flush();
	}

	/** an {@link EmbeddedRenderTask} and the {@link PostAction} it returned */
	private static class EmbeddedContent {
		private final EmbeddedRenderTask embeddedTask;
		private final StRenderPostAction postAction;
		public EmbeddedContent(EmbeddedRenderTask embeddedTask, StRenderPostAction postAction) {
			this.embeddedTask = embeddedTask;
			this.postAction = postAction;
		}
	}

	/** Parse the URI and forward the request to the appropriate task */
	protected void handleInternal(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String uri = req.getRequestURI();
		if (uri.length() == 0) {
			res.sendRedirect("/" + getDefaultURI());
			return;
		}
		if (serveStatic(uri, res)) return;
		log.info("Serving URI: " + uri + (debugMode ? " [debug mode]" : ""));
		
		ITask t = findTask(uri);
		if (t == null) {
			log.info("No task found, sending to default URI");
			res.sendRedirect(getDefaultURI());
			return;
		}
		
		@SuppressWarnings("unchecked")
		Map<String, String[]> reqParams = req.getParameterMap();
		
		List<EmbeddedContent> embeddedContent = Empty.list();
		for (EmbeddedRenderTask ert : t.dependsOn())
			renderEmbedded(ert, reqParams, embeddedContent);

		renderFinal(t, req, reqParams, embeddedContent, res);
	}

	/** render the given embedded task (recursively) */
	private void renderEmbedded(EmbeddedRenderTask t, Map<String, String[]> reqParams, List<EmbeddedContent> embeddedContent) {
		for (EmbeddedRenderTask ert : t.dependsOn())
			renderEmbedded(ert, reqParams, embeddedContent);

		PostAction po = render(t, reqParams, null, "embedded/");
		if (po instanceof StRenderPostAction)
			embeddedContent.add(new EmbeddedContent(t, (StRenderPostAction) po));
		else
			throw new RuntimeException("EmbeddedRenderTask returned unsupported PostAction " + po);
	}

	/** map from URI ending to resource path, i.e. package name. */
	private static final Map<String, String> resourceMap = Empty.hashMap();
	static {
		addResourceMapping("images", "gif", "jpg", "png");
		addResourceMapping("css", "css");
	}
	private static void addResourceMapping(String path, String... ending) {
		for (String s : ending) {
			String old = resourceMap.put(s, path);
			if (old != null)
				throw new AssertionError(s + " alreadys maps to " + old);
		}
	}
	
	private final Date boot = Clock.now();
	
	/** serve static resources, e.g. images and css that do not have any dynamic component */
	private boolean serveStatic(String uri, HttpServletResponse res) {
		int k = uri.lastIndexOf('.');
		if (k > 0) {
			int m = uri.lastIndexOf('/');
			if (m >= 0) {
				String ext = uri.substring(k+1);
				String path = resourceMap.get(ext);
				if (path != null) {
					String name = uri.substring(m, uri.length());
					InputStream in = getClass().getResourceAsStream(path + name);
					if (in != null) {
						try {
							String mimeType = MimeType.getMimeTypeForExtension(ext);
							if (mimeType != null) res.setHeader("Content-Type", mimeType);
							res.setDateHeader("Date", boot.getTime());				
							HttpHeaders.addCacheForeverHeaders(res);
							
							copy(in, res.getOutputStream());
						} catch (IOException ex) {
							log.warn("Failed to serve resource " + path + name, ex);
						}
					} else {
						// TODO: Send error image / file here
						log.warn("Resource not found: " + name);
					}
					return true;
				}
			}
		}
		return false;
	}
	
	private static final int COPY_BUFFER_SIZE = 1024 * 4;
	
	/** Copy input to output; neither stream is closed */
	public static int copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[COPY_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
	
	private final String taskPackage = findTaskPackage(getClass());
	
	/** @return the name of the package where task classes are assumed to be */
	private String findTaskPackage(Class<? extends SpiderServlet> clazz) {
		Class<?> p = clazz;
		while (p.getSuperclass() != getServletParent())
			p = p.getSuperclass();
		
		return p.getPackage().getName() + ".st.";
	}

	protected Class<? extends SpiderServlet> getServletParent() {
		return SpiderServlet.class;
	}

	/** @return an instance of the task the given URI maps to, or null if no such class exists */
	private ITask findTask(String uri) {
		String tn = extractTaskName(uri);
		if (tn != null) {
			String cn = taskPackage + tn;
			Class<?> c;
			try {
				c = Class.forName(cn);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("No class " + cn);
			}
			if (c != null && ITask.class.isAssignableFrom(c)) {
				@SuppressWarnings({"unchecked","cast"})
				Constructor<ITask>[] consArr = (Constructor<ITask>[]) c.getConstructors();
				if (consArr.length != 1)
					throw new RuntimeException("Class " + c + " must have exactly one constructor");
				Constructor<ITask> cons = consArr[0];
				ObjectProvider injector = makeObjectProvider();
				Object[] consArgs = injector.makeArgsFor(cons);
				try {
					return cons.newInstance(consArgs);
				} catch (Exception ex) {
					throw new RuntimeException("Could not invoke constructor " + cons + " with " + Arrays.toString(consArgs) + " for " + c, ex);
				}
			}
		}
		return null;
	}
	
	/** @return an instance of ObjectProvider with all the objects that are available for dependency injection */
	private ObjectProvider makeObjectProvider() {
		ObjectProvider injector = new ObjectProvider();
		registerObjects(injector);
		return injector;
	}

	/** @return the task name the given URI maps to */
	private String extractTaskName(String uri) {
		int k = uri.lastIndexOf('/');
		if (k >= 0) {
			String s = Strings.capitalizeFirstCharacter(uri.substring(k+1));
			return s.length() == 0 ? null : s + "Task";
		}
		return null;
	}

	/** map from name of a StTool to an instance of it */
	private static Map<String, StTool> stTools = Empty.hashMap();
	static {
		stTools.put("cached", new CachedTool());
	}

	/** @return the StTool for the given name */
	protected StTool getStTool(String name) {
		return stTools.get(name);
	}

	/** @return the path to the StringTemplate with the given name */
	protected String findPathForTemplate(String name) {
		name = "st/" + name;
		String path = name + ".st";
		Class<?> c = getClass();
		while (c != null) {
			if (c.getResource(path) != null)
				return c.getPackage().getName().replace('.', '/') + "/" + name;
			
			if (c == SpiderServlet.class)
				break;
			
			c = c.getSuperclass();
		}
		throw new RuntimeException("Cannot find template " + name);
	}
	
	/** render the given task and write the output to the response */
	private void renderFinal(ITask t, HttpServletRequest req, Map<String, String[]> reqParams, List<EmbeddedContent> embeddedContent, HttpServletResponse res) throws IOException {
		PostAction po = render(t, reqParams, embeddedContent, "pages/");
		
		if (po instanceof CustomPostAction) {
			((CustomPostAction)po).respond(req, res);
			
		} else if (po instanceof StRenderPostAction) {
			if (!(t instanceof IRenderTask))
				throw new RuntimeException("Task " + t + " must be a RenderTask");
			
			IRenderTask rt = (IRenderTask) t;
			String stContent = ((StRenderPostAction)po).getStContent();
			
			StringTemplate pageSt = pageStGroup.getInstanceOf("page");
			pageSt.setAttribute("pagetitle", rt.getPageTitle());
			pageSt.setAttribute("body", unsafeHtmlString(stContent));

			addEmbedded(embeddedContent, pageSt);
			
			Writer w = getUtf8Writer(res);
			try {
				pageSt.write(new AutoIndentWriter(w));
			} finally {
				w.close();
			}
		}
	}

	/** @return a Writer that writes UTF-8 to the given response */
	protected Writer getUtf8Writer(HttpServletResponse res) throws IOException {
		res.setContentType("text/html; charset=utf-8");
		Writer w = new OutputStreamWriter(res.getOutputStream(), "utf-8");
		return w;
	}

	/** Pattern to extract the part of the class name preceding the "Task" postfix */
	private static final Pattern CLASS_NAME_PREFIX_PATTERN = Pattern.compile(".*\\.(.+)Task.*");

	/** @return the PostAction returned from {@link StRenderer#actionAndRender(ObjectProvider, Map)} on the given task */
	private PostAction render(ITask t, Map<String, String[]> reqParams, final List<EmbeddedContent> embeddedContent, final String relativeTemplatePath) {
		StRenderer renderer = new StRenderer(t, debugMode) {
			@Override protected Pattern getClassNamePrefixPattern() {
				return CLASS_NAME_PREFIX_PATTERN;
			}
			@Override protected StTool getStTool(String name) {
				return SpiderServlet.this.getStTool(name);
			}
			@Override protected String getPageRelativePath() {
				return relativeTemplatePath;
			}
			@Override protected String renderFinal(StringTemplate st) {
				if (embeddedContent != null)
					addEmbedded(embeddedContent, st);
				return super.renderFinal(st);
			}
		};
		registerInputArgParser(renderer);
		
		ObjectProvider injector = makeObjectProvider();

		long nt = System.nanoTime();
		PostAction po = renderer.actionAndRender(injector, reqParams);
		log.info("StRender of " + t.getClass().getSimpleName() + " in " + TimeUnit.MILLISECONDS.convert(System.nanoTime() - nt, TimeUnit.NANOSECONDS) + " ms");
		return po;
	}
	
	private void addEmbedded(List<EmbeddedContent> embeddedContent, StringTemplate st) {
		for (EmbeddedContent ec : embeddedContent) {
			// The variables that went into stContent have already been escaped 
			st.setAttribute(ec.embeddedTask.getStAttribute(), unsafeHtmlString(ec.postAction.getStContent()));
		}
	}

	private HtmlString unsafeHtmlString(String html) {
		@SuppressWarnings("deprecation")
		HtmlString stContent = HtmlString.rawUnsafe(html);
		return stContent;
	}
	
}
