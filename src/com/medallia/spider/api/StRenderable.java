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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.medallia.spider.Task;

/**
 * Interface implemented by classes that can be rendered by a {@link StRenderer}. In
 * addition to implementing this interface a .st file with the actual StringTemplate
 * source must (with some exceptions documented below) also be created and placed in
 * the same package as the class.
 * <p>
 * 
 * The entry point called by {@link StRenderer} is a method with the following signature:
 * <p>
 * 
 *   PostAction action(...)
 *   <p>
 * 
 * This method is not defined in this interface since dependency injection is used on its
 * arguments. In addition to any objects registered with the {@link StRenderer} instance
 * an object implementing the interface annotated with {@link StRenderable.Input} is available.
 * This object is actually a proxy that handles request parameter parsing; the methods on
 * the interface should have the same name as the request parameter, while the return type
 * should be the type the request parameter should be parsed into. For example:
 * <p>
 * 
 * <pre>
 *   @Input interface Params {
 *     Command cmd();
 *     String foo();
 *     int bar()
 *   }
 * </pre>
 *   
 * The action method is declared thus:
 * <p>
 * 
 *   void action(Params p)
 *   <p>
 *   
 * When a method is called on Params the request parameter with the same name as the method
 * is parsed into the given type. By default enums, ints and booleans are supported; custom
 * types can be registered via {@link StRenderer#registerArgParser(Class, spider.api.StRenderer.InputArgParser)}.
 * <p>
 * 
 * Dynamic forms are also supported; see {@link DynamicInput}. This object is also dependency injected.
 * <p>
 * 
 * The return type of the action method can be either void or a PostAction. If void the default
 * PostAction will be assumed, which is to render a StringTemplate; see {@link StRenderer#defaultPostAction()}.
 * <p>
 * 
 * The attributes available in the StringTemplate are defined in an interface annotated with
 * {@link StRenderable.Output}. For example:
 * 
 * <pre>
 *   @Output interface Values {
 *     V<Foo> FOO = v();
 *     V<List<Bar>> BARS = v();
 *   }
 * </pre>
 * 
 * The {@link #getAttr(spider.api.StRenderable.V)} method is called with each member of this interface. Usually
 * an abstract base class, e.g. {@link Task}, is inherited which implements this method; clients need only
 * call {@link Task#attr()}. The name of the members are in all caps to satisfy the Java naming convention, but
 * the attribute is converted to all lowercase in the template. 
 * <p>
 * 
 * Note that the StringTemplate rendering will fail if an attribute is referenced without being set.
 * 
 */
public interface StRenderable {
	
	/** input (request) variables */
	@Retention(RetentionPolicy.RUNTIME)
	@interface Input {
		/** This input may occur zero or more times; the values will be returned as an array */
		@Retention(RetentionPolicy.RUNTIME)
		@interface MultiValued {}
		
		/** This input takes a single string, and splits it on the separator using string.split() */		
		@Retention(RetentionPolicy.RUNTIME)
		@interface List {
			String separator();
		}
	}
	
	/** output (in StringTemplate) variables */
	@Retention(RetentionPolicy.RUNTIME)
	@interface Output {
		
	}
	
	/** Object used to dynamically retrieve input variables; this is needed for dynamically
	 * generated forms where the number of input elements is not known at compile time.
	 */
	interface DynamicInput {
		/**
		 * Dynamic version of the methods declared in {@link Input}.
		 * 
		 * @param name name of the input variable, which is the same as the method name would have been
		 * @param type type of the object the value is to be parsed into; the same as the return type of the method
		 * @return the parsed value
		 */
		<X> X getInput(String name, Class<X> type);
	}
	
	/** TypeTag Interface to be used in the Output interface
	 * @param <X> type of the tag
	 */
	public final class V<X> {
		/** @return a TypeTag object; see class doc */
		public static <X> V<X> v() {
			return new V<X>();
		}
	}
	
	/** @return the Object for the given TypeTag */
	<X> X getAttr(V<X> tag);
	
	/** @return true if the given TypeTag is set */
	boolean hasAttr(V<?> tag);
	
	/** @return the class which is in the same package as the .st file; this method is typically
	 * overidden by tasks that extend another file, but want to use the same .st file.
	 */
	Class<?> getClassForTemplateName();

	/** interface returned by the action method. This interface is only used
	 * to have a common return type for the action method and shold not be
	 * used except for by subinterfaces.
	 */
	public interface PostAction { }

	/** PostAction that gives the name of the .st file to render */
	public interface StTemplatePostAction extends PostAction {
		/** @return the name of the .st file (without the .st part) to render */
		String templateName();
	}

}
