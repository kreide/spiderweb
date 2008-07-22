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
package com.medallia.tiny;


/**
 * Predicate that can be used for accepting or rejecting an
 * object.
 * 
 * @see EPredicate for more doc.
 * @param <E> Type of element 
 */
public interface Predicate<E> extends EPredicate<E, RuntimeException> {

	/** @return true if the object is "good", "accepted", "approved" etc */
	boolean accept(E e);

	/** pre-defined predicates */
	public static class Predicates {
		public static final Predicate ACCEPT_ALL = new Predicate() {
			public boolean accept(Object e) { return true; }
		};
		/** @return a predicate that accepts all objects */
		@SuppressWarnings("unchecked")
		public static <X> Predicate<X> acceptAll() { return ACCEPT_ALL; }
		
		/** @return a predicate that is the inverse of the given predicate */
		public static <X> Predicate<X> not(final Predicate<X> p) {
			return new Predicate<X>() {
				public boolean accept(X e) {
					return !p.accept(e);
				}
			};
		}
		
		public static final Predicate NOT_NULL = new Predicate() {
			public boolean accept(Object e) {
				return e != null;
			}
		};
		/** @return a predicate that accepts all non null objects */
		@SuppressWarnings("unchecked")
		public static <X> Predicate<X> notNull() { return NOT_NULL; }
	}
}
