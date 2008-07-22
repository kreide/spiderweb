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
 * object. Allows for passing through an Exception in a
 * type-safe manner.
 * 
 * @see CollUtils for methods that take a Predicate
 * @see Predicate if the accept function does not throw a checked exception
 * @param <E> Type of element 
 * @param <Z> Type of exception thrown by the accept function
 */
public interface EPredicate<E, Z extends Exception> {
	
	/** @return true if the object is "good", "accepted", "approved" etc */
	boolean accept(E e) throws Z;

}
