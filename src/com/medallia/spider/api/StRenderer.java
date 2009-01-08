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
package com.medallia.spider.api;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateWriter;
import org.antlr.stringtemplate.language.ASTExpr;
import org.apache.commons.lang.StringEscapeUtils;

import com.medallia.spider.MethodInvoker;
import com.medallia.spider.MethodInvoker.LifecycleHandlerSet;
import com.medallia.spider.api.StRenderable.Input;
import com.medallia.spider.api.StRenderable.Output;
import com.medallia.spider.api.StRenderable.PostAction;
import com.medallia.spider.api.StRenderable.StTemplatePostAction;
import com.medallia.spider.api.StRenderable.V;
import com.medallia.spider.sttools.StTool;
import com.medallia.tiny.CollUtils;
import com.medallia.tiny.Empty;
import com.medallia.tiny.ObjectProvider;
import com.medallia.tiny.string.ExplodingStringTemplateErrorListener;
import com.medallia.tiny.string.HtmlString;
import com.medallia.tiny.string.JsString;
import com.medallia.tiny.string.StringTemplateBuilder.SimpleAttributeRenderer;

/**
 * Class that handles the rendering of an instance of {@link StRenderable}, which
 * is given in the constructor. The {@link #actionAndRender(ObjectProvider, Map)}
 * method is called to do the rendering.
 * <p>
 * 
 * The returned PostAction should be handled by the caller. One exception is
 * {@link StTemplatePostAction} which is replaced with a {@link StRenderPostAction}
 * by rendering the template. The caller can check whether the returned object
 * is an instance of that interface and if so retrieve the rendered content.
 *
 */
public abstract class StRenderer {
	private final StRenderable renderable;
	private final StringTemplateGroup stGroup;
	
	/** Forwards to {@link #StRenderer(StRenderable, boolean)} with true as the second argument */
	public StRenderer(StRenderable renderable) {
		this(renderable, true);
	}
	/**
	 * @param renderable the object to render
	 * @param debugMode true if debug mode is on; the template source is then re-read from disk every time
	 */
	public StRenderer(StRenderable renderable, boolean debugMode) {
		this.renderable = renderable;
		this.stGroup = createStGroup(renderable, debugMode);
	}
	
	/** @return the default target */
	protected PostAction defaultPostAction() {
		return stRenderPostAction(getTemplateNameFromClass(renderable.getClassForTemplateName()));
	}

	/** PostAction that holds the result of the rendering of the template source */
	public interface StRenderPostAction extends PostAction {
		/** @return the rendered content */
		String getStContent();
	}
	
	/** Call {@link #render(String)} and wrap the return value in a {@link StRenderPostAction} */
	protected StRenderPostAction stRenderPostAction(String templateName) {
		final String stContent = render(templateName);
		return new StRenderPostAction() {
			public String getStContent() {
				return stContent;
			}
		};
	}
	
	/**
	 * Call the action method of the {@link StRenderable}, render the template if applicable and return the result.
	 * 
	 * @param injector dependency injector with the objects available for injection
	 * @param inputParams the request parameters
	 * @return result of the action and render
	 * @throws MissingAttributesException if the template referenced any attributes not set by the action method
	 */
	public PostAction actionAndRender(ObjectProvider injector, LifecycleHandlerSet hs, Map<String, String[]> inputParams) throws MissingAttributesException {
		PostAction pa = invokeAction(injector, hs, inputParams);
		return pa == null ? defaultPostAction() : render(pa);
	}
	
	private PostAction render(PostAction pa) {
		if (pa instanceof StTemplatePostAction) {
			return stRenderPostAction(((StTemplatePostAction)pa).templateName());
		} else {
			return pa;
		}
	}

	/** map from the class to the action method of that class; stored for performance reasons */
	private static final ConcurrentMap<Class<?>, Method> ACTION_METHOD_MAP = Empty.concurrentMap();
	
	/** @return the action method of the given class; throws AssertionError if no such method exists */
	private static Method findActionMethod(Class<?> clazz) {
		Method am = ACTION_METHOD_MAP.get(clazz);
		if (am != null)
			return am;
		
		for (Method m : CollUtils.concat(Arrays.asList(clazz.getMethods()), Arrays.asList(clazz.getDeclaredMethods()))) {
			if (m.getName().equals("action")) {
				int modifiers = m.getModifiers();
				if (Modifier.isPrivate(modifiers) || Modifier.isStatic(modifiers)) continue;
				m.setAccessible(true);
				ACTION_METHOD_MAP.put(clazz, m);
				return m;
			}
		}
		throw new AssertionError("No action method found in " + clazz);
	}
	
	private static final ConcurrentMap<Class<?>, Class<?>> INPUT_ANNOTATION_MAP = Empty.concurrentMap();
	private static final ConcurrentMap<Class<?>, Class<?>> OUTPUT_ANNOTATION_MAP = Empty.concurrentMap();
	
	/** @return the interface declared within the given class which is also annotated with the given annotation */ 
	private static <X extends Annotation> Class<X> findInterfaceWithAnnotation(Map<Class<?>, Class<?>> methodMap, Class<?> clazz, Class<? extends Annotation> annotation) {
		Class<?> annotatedInterface = methodMap.get(clazz);
		if (annotatedInterface == null) {
			for (Class<?> c : CollUtils.concat(Arrays.asList(clazz.getClasses()), Arrays.asList(clazz.getDeclaredClasses()))) {
				Annotation i = c.getAnnotation(annotation);
				if (i != null) {
					annotatedInterface = c;
					methodMap.put(clazz, annotatedInterface);
					break;
				}
			}
		}
		@SuppressWarnings("unchecked")
		Class<X> x = (Class<X>) annotatedInterface;
		return x;
	}
	
	/**
	 * Call the action method of the {@link StRenderable} and return the post action that needs to be rendered
	 * 
	 * @param injector dependency injector with the objects available for injection
	 * @param hs handler for the life cycle of the injected objects
	 * @param inputParams the request parameters
	 * @return result of the action and render
	 */
	public PostAction invokeAction(ObjectProvider injector, LifecycleHandlerSet hs, Map<String, String[]> inputParams) {
		DynamicInputImpl dynamicInput = new DynamicInputImpl(inputParams, inputArgParsers);
		injector = injector.copyWith(dynamicInput).errorOnUnknownType();
		
		Method am = findActionMethod(renderable.getClass());
		Class<Input> inputInterface = findInterfaceWithAnnotation(INPUT_ANNOTATION_MAP, renderable.getClass(), Input.class);
		if (inputInterface != null) {
			injector.register(createInput(inputInterface, dynamicInput));
		}
		
		return (PostAction) new MethodInvoker(injector, hs).invoke(am, renderable);
	}
	
	/** object that can parse a request parameter argument into a proper type */
	public interface InputArgParser<X> {
		/** @return the parsed object */
		X parse(String str);
	}
	
	private final Map<Class<?>, InputArgParser<?>> inputArgParsers = Empty.hashMap();

	private Object createInput(Class<?> x, final DynamicInputImpl dynamicInput) {
		return Proxy.newProxyInstance(x.getClassLoader(), new Class<?>[] { x }, 
			new InvocationHandler() {
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					return dynamicInput.getInput(method.getName(), method.getReturnType(), method);
				}
			}
		);
	}
	
	/** register the given {@link InputArgParser} */
	public <X> void registerArgParser(Class<X> type, InputArgParser<X> parser) {
		inputArgParsers.put(type, parser);
	}

	/** Exception thrown when a referenced StringTemplate attribute is not set */
	public static class MissingAttributesException extends RuntimeException {
		private final List<String> missingAttrs0;

		private MissingAttributesException(List<String> missingAttrs, StringTemplate st) {
			super("Missing attributes: " + missingAttrs + " in:\n" + st.getTemplate());
			this.missingAttrs0 = missingAttrs;
		}

		/** @return list of the referenced attributes that were not set */
		public List<String> getMissingAttributes() {
			return missingAttrs0;
		}
	}
	
	private String render(String tn) throws MissingAttributesException {
		StringTemplate st = getStInstance(tn);
		return render(st);
	}
	
	/** @return Pattern applied on the class name of the class implementing {@link StRenderable}; the
	 * first group of this pattern should match the unique prefix, i.e. the part that maps to the
	 * name of the .st file.
	 */
	protected abstract Pattern getClassNamePrefixPattern();
	
	/** @return the template name based on the name of the given class */
	protected String getTemplateNameFromClass(Class<?> c) {
		String tn = c.getName();
		
		Pattern p = getClassNamePrefixPattern();
		Matcher m = p.matcher(tn);
		if (!m.matches()) throw new AssertionError("Default template name expects class name [" + tn + "] to match regex " + p.pattern());
		
		tn = m.group(1);
		tn = tn.substring(0, 1).toLowerCase() + tn.substring(1);
		return tn;
	}
	
	private List<String> missingAttrs;
	private Set<String> nullAttrs;
	
	/** @return the result of rendering the given StringTemplate in the context set up by this class */
	public String render(StringTemplate st) throws MissingAttributesException {
		missingAttrs = Empty.list();
		nullAttrs = Empty.hashSet();
		
		Class<Output> outputInterface = findInterfaceWithAnnotation(OUTPUT_ANNOTATION_MAP, renderable.getClass(), Output.class);
		if (outputInterface != null) {
			for (Field f : outputInterface.getDeclaredFields()) {
				f.setAccessible(true);
				V<?> tag;
				try {
					tag = (V<?>) f.get(null);
				} catch (Exception e) {
					throw new RuntimeException("For " + f, e);
				}
				String fname = f.getName().toLowerCase();
				Object obj = renderable.getAttr(tag);
				if (obj != null) {
					st.setAttribute(fname, obj);
				} else if (renderable.hasAttr(tag)) {
					nullAttrs.add(fname);
				}
			}
		}

		try {
			String stContent = renderFinal(st);
			if (!missingAttrs.isEmpty()) throw new MissingAttributesException(missingAttrs, st);
			
			return stContent;
		} finally {
			missingAttrs = null;
			nullAttrs = null;
		}
	}

	/** @return a StringTemplate with the template loaded from the given filename */
	protected StringTemplate getStInstance(String tn) {
		return stGroup.getInstanceOf(tn);
	}

	/** @return a StringTemplate using the given template */
	public StringTemplate makeStInstance(String template) {
		StringTemplate st = stGroup.createStringTemplate();
		st.setGroup(stGroup);
		st.setTemplate(template);
		return st;
	}
	
	/** @return the error listener that will be notified if an error occurs during ST rendering */
	protected StringTemplateErrorListener getStErrorListener() {
		return ExplodingStringTemplateErrorListener.LISTENER;
	}
	
	/** actual perform the rendering of the given StringTemplate by calling {@link StringTemplate#toString()} */
	protected String renderFinal(StringTemplate st) {
		return st.toString();
	}
	
	/** @return the relative path to the .st files; by default this is a package called "pages" */
	protected String getPageRelativePath() {
		return "pages/";
	}

	/** @return the path to the .st file of the given name, relative to the package of the given class */
	protected String findPathForTemplate(Class<?> c, String name) {
		name = getPageRelativePath() + name;
		String path = name + ".st";
		while (c != null) {
			if (c.getResource(path) != null)
				return c.getPackage().getName().replace('.', '/') + "/" + name;
			c = c.getSuperclass();
		}
		throw new RuntimeException("Cannot find template " + name);
	}
	
	/** @return the StTool for the given name; by default this method always returns null */
	protected StTool getStTool(String name) {
		return null;
	}
	
	private StringTemplateGroup createStGroup(final StRenderable renderable, boolean debugMode) {
		StringTemplateGroup stGroup = new StringTemplateGroup("mygroup") {
			@Override public String getFileNameFromTemplateName(String name) {
				return super.getFileNameFromTemplateName(findPathForTemplate(renderable.getClassForTemplateName(), name));
			}
			@Override public StringTemplate getEmbeddedInstanceOf(StringTemplate enclosingInstance, String name) throws IllegalArgumentException {
				final StTool t = getStTool(name);
				if (t != null) return withEnclosing(enclosingInstance, new StringTemplate(this, name) {
					@Override public int write(StringTemplateWriter out) throws IOException {
						// Use ASTExpr to render since the code for using AttributeRenderer is there
						return new ASTExpr(null, null, null).writeAttribute(this, t.render(this), out);
					}
				});
				return super.getEmbeddedInstanceOf(enclosingInstance, name);
			}
			private StringTemplate withEnclosing(StringTemplate enclosingInstance, StringTemplate st) {
				st.setEnclosingInstance(enclosingInstance);
				return st;
			}
			@Override public StringTemplate createStringTemplate() {
				return new StringTemplate() {
					@Override public Object get(StringTemplate self, String attribute) {
						Object o = super.get(self, attribute);
						if (self == this && o == null && !nullAttrs.contains(attribute)) {
							missingAttrs.add(attribute);
						}
						return o;
					}
				};
			}
		};
		stGroup.setErrorListener(getStErrorListener());
		
		registerWebRenderers(stGroup);
		
		if (debugMode)
			stGroup.setRefreshInterval(0);
		return stGroup;
	}
	
	/** Register renderers (by calling {@link StringTemplateGroup#registerRenderer(Class, Object)}
	 * useful for rendering web pages. This includes renderes for:
	 * 
	 *   o HtmlString
	 *   o JsString
	 *   
	 * All plain String objects are escaped with {@link StringEscapeUtils#escapeHtml(String)}.
	 * 
	 * @param stGroup the object to register the renderers on
	 */
	public static void registerWebRenderers(StringTemplateGroup stGroup) {
		stGroup.registerRenderer(HtmlString.class, HtmlString.ST_RENDERER);
		stGroup.registerRenderer(JsString.class, JsString.ST_RENDERER);
		stGroup.registerRenderer(String.class, new SimpleAttributeRenderer() {
			public String toString(Object o) {
				return StringEscapeUtils.escapeHtml(String.valueOf(o));
			}
		});
	}

}
