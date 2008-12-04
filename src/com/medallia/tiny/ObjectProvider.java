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
package	 com.medallia.tiny;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Has the ability to register objects which can later be retrieved based on class or annotation.
 * 
 * Each class can be mapped to a single object. If multiple objects are registered for the same class, then the
 * new values will overwrite old ones for lookups by class ({@link #get(Class)}. Lookups by annotation ({@link #getByAnnotation(Class, Class)})
 * will give you the last object stored for that annotation.
 * 
 * {@link #get(Class)} will try to give you an object matching the type directly. If no such object exist, then an unspecified¨
 * contained object will be returned. I.e. <tt>op.get(Object.class)</tt> can give you any object in the provider, provided that
 * none have been registered for the class <tt>Object.class</tt> directly.
 * 
 * Usually this class will be used for the {@link #makeArgsFor(Method)} method, which will find valid parameters for all
 * arguments to the function, provided such objects are registered. The parameters may have annotations, in which case the lookup
 * will be based on the annotation. If not, we will use lookup by class.
 */
public class ObjectProvider {
	private static final Object NO_ARG = new Object(); // So we can still set the last argument to null
	private static final Log LOG = LogFactory.getLog(ObjectProvider.class);
	private static final Object[] NO_ARUMENTS = new Object[0];
	
	private Object lastArg = NO_ARG;
	protected final Map<Class<?>, Object> map;
	private final Map<Class<?>, Object> annotationMap;
	private boolean errorOnUnknownType;
	/**
	 * Creates a new ObjectProvider with no registered objects
	 */
	public ObjectProvider() { 
		map = Empty.linkedHashMap();
		annotationMap = Empty.linkedHashMap();
	}
	
	/** make a new object provider by copying the given provider */
	protected ObjectProvider(ObjectProvider from) { 
		map = Empty.linkedHashMap(from.map);
		annotationMap = Empty.linkedHashMap(from.annotationMap);
		lastArg = from.lastArg;
	}
	
	/** @return a copy of this object */
	protected ObjectProvider copyObjectProvider() {
		return new ObjectProvider(this);
	}

	/** if called an exception will be thrown if an unknown object is requested instead of passing in null */
	public ObjectProvider errorOnUnknownType() {
		errorOnUnknownType = true;
		return this;
	}

	/**
	 * Ensures that o is of type X and registers it.
	 * Casts a {@link ClassCastException} if o is not of type X
	 * @return this ObjectProvider
	 */
	public <X> ObjectProvider castAndRegister(Class<X> c, Object o) {
		return register(c, c.cast(o));
	}
	/**
	 * Registers o for o.getClass().
	 */
	public ObjectProvider register(Object o) {
		if (o instanceof Proxy) {
			for (Class<?> c : o.getClass().getInterfaces())
				castAndRegister(c, o);
			return this;
		}
		return castAndRegister(o.getClass(), o);
	}

	/**
	 * Registers o for o.getClass() and for the provided annotation
	 */
	public ObjectProvider registerWithAnnotation(Class<? extends Annotation> annotation, Object o) {
		annotationMap.put(annotation, o);
		return register(o);
	}
	
	/**
	 * Get an object by annotation
	 */
	@SuppressWarnings("unchecked")
	public <X> X getByAnnotation(Class<? extends Annotation> annotation, Class<X> c) {
		Object o = annotationMap.get(annotation);
		if (o == null) LOG.warn("No object for annotation " + annotation + " in " + this);
		
//		We do a safe cast if possible here, since we do not know what type of object was used when registering
		return c.isPrimitive() ? (X)o : c.cast(o);
	}
	
	/**
	 * Registers o for c.
	 */
	public <X> ObjectProvider register(Class<X> c, X o) {
		map.put(c, o);
		return this;
	}
	
	/** Object that can produce another object */
	public interface ObjectFactory<X> {
		/** @return the produced object */
		X make();
	}

	/**
	 * Register a factory object; if this ObjectProvider is asked to
	 * provide the type of object the factory produces it will
	 * call {@link ObjectFactory#make()} to obtain the object.
	 */
	public <X> ObjectProvider registerFactory(ObjectFactory<X> of) {
		for (Type t : of.getClass().getGenericInterfaces()) {
			if (t instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) t;
				if (pt.getRawType().equals(ObjectFactory.class)) {
					@SuppressWarnings("unchecked")
					Class<X> x = (Class<X>) pt.getActualTypeArguments()[0];
					registerFactory(x, of);
					break;
				}
			}
		}
		return this;
	}

	/** Same as {@link #registerFactory(ObjectFactory)}, but give the type of the produced
	 * objects explicitly.
	 */
	public <X> ObjectProvider registerFactory(Class<X> c, ObjectFactory<X> of) {
		map.put(c, of);
		return this;
	}
	
	/**
	 * @return an object of type c, or null if none are registered with this ObjectProvider
	 */
	@SuppressWarnings("unchecked")
	public <X> X get(Class<X> c) {
		if (map.containsKey(c)) return (X) toValue(map.get(c)); //c.cast(e.value()) will fail if c.isPrimitive()
		for (Map.Entry<Class<?>, Object> e : map.entrySet()) {
			if (c.isAssignableFrom(e.getKey())) {
				Object v = toValue(e.getValue());
				LOG.debug("Returning "  + v + " with key " + e.getKey() + " for " + c);
				return (X) v;
			}
		}
		String msg = "No object registred for " + c + " in " + this;
		if (errorOnUnknownType) {
			throw new RuntimeException(msg);
		} else {
			LOG.info(msg);
			return null;
		}
	}
	
	private Object toValue(Object obj) {
		if (obj instanceof ObjectFactory) {
			obj = ((ObjectFactory)obj).make();
		}
		return obj;
	}

	/**
	 * @return True if an object of type c is registered with this ObjectProvider, false otherwise
	 */
	public boolean has(Class<?> c) {
		if (map.containsKey(c)) return true; //c.cast(e.value()) will fail if c.isPrimitive()
		for (Class<?> mappedclass : map.keySet()) {
			if (c.isAssignableFrom(mappedclass)) return true;
		}
		return false;
	}
	/**
	 * @return a copy of this, with the extra object o registered
	 */
	public ObjectProvider copyWith(Object o) {
		return copyObjectProvider().register(o);
	}

	/**
	 * @return a copy of this, with the extra object o registered
	 */
	public <X> ObjectProvider copyWith(Class<X> c, X o) {
		return copyObjectProvider().register(c, o);
	}
	@Override public String toString() {
		return "ObjectProvider: " + Arrays.toString(new Object[]{map, annotationMap, lastArg}); 
	}
	
	/**
	 * Returns an ObjectProvivder that will use the provided object as the last object when calling {@link #makeArgsFor(Method)}
	 */
	public ObjectProvider copyWithLast(Object o) {
		ObjectProvider op = copyObjectProvider();
		op.lastArg = o;
		return op;
	}

	/**
	 * Creates a parameter list for invoking the method using object from this ObjectProvider.
	 * Parameters are obtained by calling {@link #get(Class)} on the classes in m.getParameterTypes().
	 * If a lastArg is specified for this ObjectProvider, the last argument will be that object.
	 */
	public Object[] makeArgsFor(Method m) {
		Class<?>[] pt = m.getParameterTypes();
		Annotation[][] a = m.getParameterAnnotations();
		return makeArgsFor(pt, a);
	}
	/** Same as {@link #makeArgsFor(Method)}, but for a {@link Constructor} */
	public Object[] makeArgsFor(Constructor cons) {
		return makeArgsFor(cons.getParameterTypes(), cons.getParameterAnnotations());
	}

	private Object[] makeArgsFor(Class<?>[] pt, Annotation[][] a) {
		if (pt.length == 0) return NO_ARUMENTS;
		Object[] params = new Object[pt.length];
		for (int i = 0; i < pt.length; i++) {
			if (i == pt.length - 1 && lastArg != NO_ARG)
				params[i] = lastArg;
			else {
				if (a[i].length == 1)
					params[i] = getByAnnotation(a[i][0].annotationType(), pt[i]);
				else if (a[i].length == 0)
					params[i] = get(pt[i]);
				else
					throw new IllegalArgumentException("Parameter " + i + " has multiple annotations");
			}
		}
		return params;
	}

}