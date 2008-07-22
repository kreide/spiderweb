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

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/** Similar to Override, but for interfaces. Java 1.5 does not allow
 * this, thus we use this instead until we can move to Java 1.6.
 */
// @Retention(RetentionPolicy.RUNTIME)  needed if we want to enforce them
@Target(ElementType.METHOD)
public @interface Implement {
	/** Optionally the interface the method comes from (purely as documentation). */
	Class value() default Implement.class;
	
	// null is not allowed as the default value, so we use this class
}
