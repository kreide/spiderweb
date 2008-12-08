package com.medallia.tiny;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class with convenience methods for rethrowing (and minor handling) of exceptions and errors
 */
public class Rethrow {

	/** add a comment to an exception (without changing its type) */
	public static <X extends Throwable> X comment(X e, String expl) {
		return comment(e,expl,1);
	}

	/** add a comment to an exception (without changing its type) */
	private static <X extends Throwable> X comment(X e, String expl, int ofs) {
		// OOME is a singleton, so don't confuse things
		if (e instanceof OutOfMemoryError) return e;
	
		List<StackTraceElement> l = new ArrayList<StackTraceElement>(Arrays.asList(e.getStackTrace()));
		int pos = l.size() - Thread.currentThread().getStackTrace().length + ofs + 3;
		if (pos < 0 || ofs < 0) pos = 0;
		l.add(pos, new StackTraceElement("", expl + "  ", "note", 0));
		e.setStackTrace(l.toArray(new StackTraceElement[0]));
		return e;
	}

	/**
	 * Rethrow an exception, wrapping it in an RuntimeException if needed. In this way the exception
	 * can go unchecked
	 * @return An Error for convenience. Will always throws an exception so the actual Error is never returned.  
	 */
	public static Error rethrow(Throwable t) {
		if (t instanceof RuntimeException) throw (RuntimeException) t;
		if (t instanceof Error) throw (Error) t;
		throw new RuntimeException("Unexpected checked exception, see "+t, t);
	}

	/**
	 * Throw the target exception if it not null, otherwise throw the original exception
	 * @return An Error for convenience. Will always throws an exception so the actual Error is never returned.  
	 */
	public static Error rethrowTargetException(InvocationTargetException e) {
		Throwable t = e.getTargetException();
		if (t == null) t = e;
		throw uncheckedThrow(t);
	}

	/**
	 * Rethrow the given throwable with the given string added as a comment.
	 * This method is useful in attaching context information without changing
	 * the type of the throwable.
	 * 
	 * @return An Error for convenience. Will always throw an exception so the
	 *         actual Error is never returned.
	 */
	public static Error withComment(Throwable t, String expl) {
		// Special case to extract target exception
		if (t instanceof InvocationTargetException) {
			Throwable target = ((InvocationTargetException)t).getTargetException();
			if (target != null) t = target;
		}
		throw uncheckedThrow(comment(t, expl, 1));
	}

	/**
	 * Rethrow the throwable, such that it will be unchecked
	 * @return An Error for convenience. Will always throws an exception so the actual Error is never returned.  
	 */
	public static Error uncheckedThrow(Throwable o) {
		throw Rethrow.<Error>uncheckedThrow0(o);
	}

	@SuppressWarnings("unchecked")
	private static <X extends Throwable> Error uncheckedThrow0(Throwable o) throws X {
		throw (X)o;
	}

}
