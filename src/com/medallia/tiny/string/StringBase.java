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
package com.medallia.tiny.string;

import java.io.Serializable;

public abstract class StringBase implements CharSequence, Serializable {
	/** needed by Serializable */
	protected StringBase() { }
	protected StringBase(String s) {
		this.s = s;			
	}
	protected String s;
	@Override public String toString() {
		return s + "[an exploded " + this.getClass() + " (did you remember inScript() or inAttr()?)]";
	}
	public int length() {
		return s.length();
	}

	public char charAt(int arg0) {
		return s.charAt(arg0);
	}
	
	@Override public int hashCode() {
		return s == null ? 0 : s.hashCode();
	}
	@Override public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StringBase other = (StringBase) obj;
		if (s == null) {
			if (other.s != null)
				return false;
		} else if (!s.equals(other.s))
			return false;
		return true;
	}

}