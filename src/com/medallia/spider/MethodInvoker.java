package com.medallia.spider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.medallia.tiny.CollUtils;
import com.medallia.tiny.Empty;
import com.medallia.tiny.Implement;
import com.medallia.tiny.ObjectProvider;
import com.medallia.tiny.Rethrow;

/**
 * Class that handles invoking a given method or constructor. An instance of
 * {@link ObjectProvider} is used to obtain the arguments for the method.
 *
 */
public class MethodInvoker {

	/** Object that handles the life cycle of another object */
	public interface LifecycleHandler<X> {
		/** Called before x is used */
		void onInit(X x);
		/** Called if an error occurred during the usage of x */
		void onError(X x, Throwable t);
		/** Called if no error occurred during the usage of x */
		void onSuccess(X x);
	}
	
	/** Storage for a set of {@link LifecycleHandler} objects */
	public interface LifecycleHandlerSet {
		/** Register a {@link LifecycleHandler} for objects of the given type */
		<X> void register(Class<X> clazz, LifecycleHandler<X> h);
	}
	private interface LifecycleHandlerSet0 extends LifecycleHandlerSet {
		Map<Class<?>, LifecycleHandler<?>> getHandlers();
	}
	
	/** @return a {@link LifecycleHandlerSet} that should be passed to {@link #MethodInvoker(ObjectProvider, LifecycleHandlerSet)} */
	public static LifecycleHandlerSet getLifecycleHandlerSet() {
		return new LifecycleHandlerSet0() {
			private final Map<Class<?>, LifecycleHandler<?>> m = Empty.hashMap();
			@Implement public <X> void register(Class<X> clazz, LifecycleHandler<X> h) {
				m.put(clazz, h);
			}
			@Implement public Map<Class<?>, LifecycleHandler<?>> getHandlers() {
				return m;
			}
		};
	}
	
	private final ObjectProvider injector;
	private final Map<Class<?>, LifecycleHandler<?>> handlers;
	
	/**
	 * @param injector used to obtain the arguments for the method to be invoked
	 * @param h used on the method arguments
	 */
	public MethodInvoker(ObjectProvider injector, LifecycleHandlerSet h) {
		this.injector = injector;
		this.handlers = ((LifecycleHandlerSet0)h).getHandlers();
	}

	/** @return the object created by invoking the given constructor
	 * Note: any exception thrown by the constructor is thrown unchecked by this method.
	 */
	public <X> X invoke(final Constructor<X> cons) {
		final Object[] consArgs = injector.makeArgsFor(cons);
		return invoke(consArgs, new Callable<X>(){
			@Implement public X call() throws Exception {
				try {
					return cons.newInstance(consArgs);
				} catch (Exception e) {
					throw Rethrow.withComment(e, "While invoking constructor " + cons + " with " + Arrays.toString(consArgs));
				}
			}
		});
	}
	
	/** @return the object returned from invoking the given method on the given object
	 * Note: any exception thrown by the constructor is thrown unchecked by this method.
	 */
	public Object invoke(final Method m, final Object obj) {
		final Object[] args = injector.makeArgsFor(m);
		return invoke(args, new Callable<Object>(){
			@Implement public Object call() throws Exception {
				try {
					return m.invoke(obj, args);
				} catch (Exception e) {
					throw Rethrow.withComment(e, "While invoking method " + m + " with " + Arrays.toString(args));
				}
			}
		});
	}
	
	/** Wrapper for an object and its {@link LifecycleHandler} */
	private interface BoundLifecycleHandler {
		void onInit();
		void onError(Throwable t);
		void onSuccess();
	}
	
	private <X> X invoke(Object[] args, Callable<X> c) {
		List<BoundLifecycleHandler> hl = findLifecycleHandlers(args);
		try {
			return invoke(hl, args, c);
		} catch (Exception e) {
			// Work around Java's type system
			throw Rethrow.uncheckedThrow(e);
		}
	}
	
	private <X> X invoke(List<BoundLifecycleHandler> hl, Object[] args, Callable<X> c) throws Exception {
		if (hl.isEmpty()) {
			return c.call();
		} else {
			BoundLifecycleHandler h = CollUtils.first(hl);
			h.onInit();
			X x;
			try {
				x = invoke(CollUtils.tail(hl), args, c);
			} catch (Throwable t) {
				try {
					h.onError(t);
				} catch (Throwable nested) {
					// ignore these
				}
				throw Rethrow.uncheckedThrow(t);
			}
			h.onSuccess();
			return x;
		}
	}
	
	private List<BoundLifecycleHandler> findLifecycleHandlers(Object[] args) {
		List<BoundLifecycleHandler> l = Empty.list();
		for (Object o : args) {
			LifecycleHandler<?> h = findHandler(o);
			if (h != null)
				l.add(bind(o, h));
		}
		return l;
	}

	private LifecycleHandler<?> findHandler(Object o) {
		Class<?> c = o.getClass();
		for (Map.Entry<Class<?>, LifecycleHandler<?>> me : handlers.entrySet()) {
			if (me.getKey().isAssignableFrom(c))
				return me.getValue();
		}
		return null;
	}

	private <X> BoundLifecycleHandler bind(Object o, final LifecycleHandler<X> h) {
		@SuppressWarnings("unchecked")
		final X x = (X) o;
		return new BoundLifecycleHandler() {
			@Implement public void onInit() { h.onInit(x); }
			@Implement public void onError(Throwable t) { h.onError(x, t); }
			@Implement public void onSuccess() { h.onSuccess(x); }
		};
	}

}
