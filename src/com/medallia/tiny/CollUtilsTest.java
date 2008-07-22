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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;


/** Tests for the CollUtils functions */
public class CollUtilsTest extends TestCase {
	/** test the split() method */
	public void testSplit() {
		assertEquals(1, CollUtils.split(Arrays.asList(new Integer[] { 1, 2, 3 }), 3).size());
		List<Integer> list = Arrays.asList(new Integer[] { 1, 2, 3, 4 });
		Collection<Collection<Integer>> c = CollUtils.split(list, 3);
		assertEquals(2, c.size());
		Iterator<Collection<Integer>> it = c.iterator();
		assertEquals(3, it.next().size());
		assertEquals(1, it.next().size());
		CollUtils.split(Empty.hashSet(list), 3);
	}
	/** test the toIterableLists() method */
	public void testToIterableLists() {
		final int[] hasNextCount = { 0 }, nextCount = { 0 };
		final Iterator<Integer> myIt = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5 }).iterator();
		Iterator<List<Integer>> it = CollUtils.toIterableLists(new Iterator<Integer>() {
			@Implement public boolean hasNext() {
				hasNextCount[0]++;
				return myIt.hasNext();
			}
			@Implement public Integer next() {
				nextCount[0]++;
				return myIt.next();
			}
			@Implement public void remove() { myIt.remove(); }
		}, 3).iterator();
		assertTrue(it.hasNext());
		assertEquals(3, it.next().size());
		assertTrue(it.hasNext());
		assertEquals(2, it.next().size());
		assertFalse(it.hasNext());
		
		assertEquals(6, hasNextCount[0]);
		assertEquals(5, nextCount[0]);
	}
	
	private <X> void testIntersection(Collection<X> expected, Collection<X> ... collections) {
		List<Set<X>> lists = Empty.list();
		for (Collection<X> item : collections) {
			lists.add(Empty.hashSet(item));
		}

		assertEquals(Empty.hashSet(expected), CollUtils.intersection(lists));
		assertEquals(Empty.hashSet(expected), CollUtils.intersectionInplace(lists));
	}
	
	@SuppressWarnings("unchecked")
	public void testIntersection() {
		testIntersection(Arrays.asList(1,2,3), Arrays.asList(1, 2, 3));
		testIntersection(Arrays.asList(1,2,3,4,5),
			Arrays.asList(1, 2, 3, 4, 5),
			Arrays.asList(1, 2, 3, 4, 5),
			Arrays.asList(1, 2, 3, 4, 5)
		);
		testIntersection(Collections.<Integer>emptyList(),
			Arrays.asList(1, 2, 3, 4, 5),
			Arrays.asList(1, 2, 3),
			Arrays.asList(4, 5)
		);
		testIntersection(Collections.<Integer>emptyList());
		
		testIntersection(Collections.<Integer>emptyList(),
			Arrays.asList(1, 2, 3, 4, 5),
			Collections.<Integer>emptyList()
		);
	}
	

	public void testSortedCopy() {
		List<Integer> l = Arrays.asList(new Integer[] { 52, 5, 16 });
		List<Integer> sorted = CollUtils.sortedCopy(l);
		assertNotSame(l, sorted);
		Collections.sort(l);
		assertEquals(l, sorted);

		sorted = CollUtils.sortedCopy(l, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return o2.compareTo(o1);
			}
		});
		Collections.reverse(l);
		assertEquals(l, sorted);
	}
}