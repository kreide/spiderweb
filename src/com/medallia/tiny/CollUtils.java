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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


/**
 * Various util functions for working with collections.
 */
public class CollUtils {

	/** @return If the given array reference is null, return an empty list, otherwise return Arrays.asList */
	public static <T> List<T> asListOrEmpty(T... a) {
		if (a == null) return Collections.emptyList();
		return Arrays.asList(a);
	}
	
	/** Put a lowercase version of k in the map if it is non-null */
	public static <V> void putLowerNotNull(Map<String, V> m, String k, V v) {
		if (k != null) m.put(k.toLowerCase(), v);
	}

	/** @return the first element of the given collection, or null if the collection is empty or null */
	public static <X> X firstOrNull(Iterable<X> l) {
		if (l != null && l.iterator().hasNext())
			return l.iterator().next();
		return null;
	}
	/** @return the first element */
	public static <X> X first(Iterable<X> l) {
		return l.iterator().next();
	}
	/** @return the sublist of the given list which does not include the first element */
	public static <X> List<X> tail(List<X> l) {
		return l.subList(1, l.size());
	}
	/** @return an iterable that will skip the first element of the given iterable */
	public static <X> Iterable<X> tail(Iterable<X> l) {
		Iterator<X> it = l.iterator();
		it.next();
		return iterable(it);
	}
	/** @return the last element from the given list */
	public static <X> X last(List<X> l) {
		return l.get(l.size()-1);
	}
	/** @return the last element from the given collection */
	public static <X> X last(Collection<X> l) {
		if (l.isEmpty()) throw new IllegalArgumentException("Collection cannot be empty");
		X x = null;
		for (X obj : l) x = obj;
		return x;
	}

	/** @return true if the given index points to the last element of the list */
	public static boolean isLastIdx(List<?> l, int idx) {
		return l.size() == (idx + 1);
	}

	/** Reverse the given list and return a reference to it */
	public static <X> List<X> reverse(List<X> l) {
		Collections.reverse(l);
		return l;
	}
	/** Reverse the given collection and return a copy of it */
	public static <X> List<X> reversedCopy(Collection<X> l) {
		return CollUtils.reverse(Empty.list(l));
	}
	
	/** Add obj to the collection for key, creating a new list if necessary */
	public static <K, X> void addToMapList(Map<K, List<X>> m, K key, X obj) {
		List<X> l = m.get(key);
		if (l == null) m.put(key, l = Empty.list());
		l.add(obj);
	}

	/** Convert the given primitive array to a list */
	public static List<Integer> toList(int[] ia) {
		List<Integer> l = Empty.list();
		for (int i : ia) l.add(i);
		return l;
	}	
	
	/** Sort by 'a', breaking ties with 'b' */
	public static <X> Comparator<X> chainedComparator(
			final Comparator<? super X> a,
			final Comparator<? super X> b) 
	{
		return new Comparator<X>() {
			public int compare(X o1, X o2) {
				int i = a.compare(o1, o2);
				return i != 0 ? i : b.compare(o1,o2);
			}			
		};
	}
	
	/** @return true if the intersection between the two arguments is non-empty */
	public static <X> boolean intersectionNonEmpty(Collection<? extends X> s1, Set<? extends X> s2) {
		for (X x : s1) {
			if (s2.contains(x)) return true;
		}
		return false;
	}
	/** Optimized version of intersectionNonEmpty for two sets - iterates over the smallest set
	 * @return true if the intersection between the two sets is non-empty */
	public static <X> boolean intersectionNonEmpty(Set<X> s1, Set<X> s2) {
		// iterate over the smallest set; first argument is iterated over
		if (s1.size() > s2.size()) {
			Set<X> t = s1;
			s1 = s2;
			s2 = t;
		}
		return intersectionNonEmpty( (Collection<X>) s1, s2);
	}

	/** Split the given collection into smaller collections, each no larger than the given max size */
	public static <X> Collection<Collection<X>> split(Collection<X> c, int max) {
		if (c == null || c.size() <= max) return Collections.singletonList(c);
		List<X> l = c instanceof List ? (List<X>) c : Empty.list(c);
		List<Collection<X>> ll = Empty.list();
		for (int k = 0; k < c.size(); k += max) {
			ll.add(l.subList(k, Math.min(c.size(), k + max)));
		}
		return ll;
	}
	
	/** Wrap the given iterator in an iterable */
	public static <X> Iterable<X> iterable(final Iterator<X> it) {
		return new Iterable<X>() {
			@Implement public Iterator<X> iterator() {
				return it;
			}
		};
	}

	/** Put elements from the iterator into lists, with maxPerLists elements in each list */
	public static <X> Iterable<List<X>> toIterableLists(final Iterator<X> it, final int maxPerList) {
		return iterable(new Iterator<List<X>>() {
			private boolean atEnd = false;
			@Implement public boolean hasNext() {
				atEnd = atEnd || !it.hasNext();
				return !atEnd;
			}
			@Implement public List<X> next() {
				List<X> l = Empty.list();
				int k = 0;
				do {
					l.add(it.next());
					if (++k >= maxPerList) break;
					if (!hasNext()) break;
				} while (true);
				return l;
			}
			public void remove() { throw new UnsupportedOperationException(); }
		});
	}

	/** @return a list of the elements in the given iterable */
	public static <X> List<X> toList(Iterable<X> iterable) {
		List<X> l = Empty.list();
		for (X x : iterable) {
			l.add(x);
		}
		return l;
	}

	/**
	 * Creates a map from the entries in another map whose keys are in <code>keys</code>.
	 * Iterating over the resulting map gives the elements in the order of <code>keys</code>.
	 * If any element of <code>keys</code> is not in the input map, there will be no such entry in the returned map.
	 * @return a "submap" of <code>m</code> where all the keys are taken from <code>keys</code>.
	 */
	public static <X, Y> Map<X, Y> retainOnly(Map<X, Y> m, X ... keys) {
		Map<X, Y> r = Empty.linkedHashMap();
		for (X key : keys) {
			if (m.containsKey(key))
				r.put(key, m.get(key));
		}
		return r;
	}

	/** toString up to maxElements of the given collection */ 
	public static String toString(Collection<?> c, int maxElements) {
		return c.isEmpty() ? "[empty]" : String.valueOf(CollUtils.first(toIterableLists(c.iterator(), maxElements)));
	}

	
	/** Allocate an array */
	@SuppressWarnings("unchecked") // though safe
	public static <T> T[] newArray(Class<T> type, int n) {
		return (T[])java.lang.reflect.Array.newInstance(type, n);
	}

	/** add those elements which are not null to the given collection and return it */
	public static <X> Collection<X> addNonNulls(Collection<X> c, X... xl) {
		for (X x : xl) {
			if (x != null) c.add(x);
		}
		return c;
	}

	/** return a list with those elements that are not null */
	public static <X> List<X> listWithNonNulls(X... c) {
		return (List<X>) addNonNulls(Empty.<X>list(), c);
	}
	
	/** object that returns a subset of a given collection; also allows for passing through
	 * an Exception in a type-safe manner */
	public interface ECollectionFilter<X, Z extends Exception> {
		/** @return a subset of a given collection */
		Collection<X> filtered(Collection<X> c) throws Z;
	}
	
	/** object that returns a subset of a given collection */
	public interface CollectionFilter<X> extends ECollectionFilter<X, RuntimeException> {
		/** @return a subset of a given collection */
		Collection<X> filtered(Collection<X> c);
	}

	/** @return an object that can filter collections based on the given predicate */
	public static final <X> CollectionFilter<X> collectionFilter(Predicate<? super X> pred) {
		final ECollectionFilter<X, RuntimeException> cf = collectionFilter0(pred);
		return new CollectionFilter<X>() {
			@Implement public Collection<X> filtered(Collection<X> c) {
				return cf.filtered(c);
			}
		};
	}
	/** @return an object that can filter collections based on the given predicate */
	public static <X, Z extends Exception> ECollectionFilter<X, Z> collectionFilter(EPredicate<? super X, Z> pred) {
		return collectionFilter0(pred);
	}
	/** @return an object that can filter collections based on the given predicate */
	private static <X, Z extends Exception> ECollectionFilter<X, Z> collectionFilter0(final EPredicate<? super X, Z> pred) {
		return new ECollectionFilter<X, Z>() {
			@Implement public Collection<X> filtered(Collection<X> c) throws Z {
				if (pred == null) return c;
				
				List<X> a = Empty.list();
				for (X x : c) if (pred.accept(x)) a.add(x);
				return a;
			}
		};
	}
	
	/** @return the given collection with only those elements accepted by the given predicate
	 * 
	 * Implementation note: takes a Set instead of a Collection since this method should not
	 * be used with a List whose remove() runs in linear time. Instead use
	 * {@link #filteredCopy(Collection, Predicate)}.
	 */
	public static <X, Y extends Set<? extends X>> Y filtered(Y elements, Predicate<? super X> pred) {
		for (Iterator<? extends X> it = elements.iterator(); it.hasNext();)
			if (!pred.accept(it.next()))
				it.remove();
		return elements;
	}
	
	/** @return a copy of the given collection with only those elements accepted by the given predicate */
	public static <X> List<X> filteredCopy(Iterable<? extends X> fields, Predicate<? super X> pred) {
		return filteredCopy0(fields, pred);
	}
	
	/** @return a copy of the given collection with only those elements accepted by the given predicate */
	public static <X, Z extends Exception> List<X> filteredCopy(Iterable<? extends X> fields, EPredicate<? super X, Z> pred) throws Z {
		return filteredCopy0(fields, pred);
	}
	/** @return a copy of the given collection with only those elements accepted by the given predicate */
	private static <X, Z extends Exception> List<X> filteredCopy0(Iterable<? extends X> fields, EPredicate<? super X, Z> pred) throws Z {
		List<X> a = Empty.list();
		for (X x : fields) if (pred.accept(x)) a.add(x);
		return a;
	}

	/** add all the elements from the given iterable to the collection */
	public static <X> void addAll(Collection<X> l, Iterable<X> it) {
		for (X x : it)
			l.add(x);
	}
	
	/**
	 * Returns the only element in the collection.
	 * @throws AssertionError if the collection does not contain exactly one element
	 */
	public static <X> X getOnlyElement(Collection<X> c) {
		int size = c.size();
		if (size != 1) throw new AssertionError("Expected size 1, was: " + size + " in " + c);
		return first(c);
	}
	
	/**
	 * @return the only element in the collection, or null if empty
	 * @throws RuntimeException if the collection contains more than one element
	 */
	public static <X> X getOneOrNull(Collection<X> c) {
		int size = c.size();
		if (size > 1) throw new RuntimeException("Expected size 0 or 1, was: " + size + " in " + c);
		return firstOrNull(c);
	}
	
	/**
	 * @return list, after shuffling
	 */
	public static List<Integer> shuffled(List<Integer> list, Random random) {
		Collections.shuffle(list, random);
		return list;
	}

	/**
	 * @return intersection between all the sets
	 * 
	 * The intersection is performed "inplace" using one of the sets.
	 * The list of sets will also (possibly) be reordered.
	 */
	public static <X> Set<X> intersectionInplace(List<Set<X>> sets) {
		if (sets.isEmpty()) return Collections.emptySet();
		if (sets.size() == 1) return sets.get(0);

		// sort collections so we can find the smallest and iterate from biggest to smallest
		Collections.sort(sets, sortBySizeReversed);

		// start with the last and smallest set
		Set<X> set = sets.remove(sets.size() - 1);

		// the idea is to try to make the intersection as small as possible
		// early in the iteration by starting with the biggest data set.
		// It's then most likely to remove the most items.
		return retainAllAll(set, sets);
	}

	/**
	 * @return intersection between all the sets
	 * 
	 * This is a non-destructive version of intersectionInplace
	 * and will create a new hashSet to hold the final result
	 */
	public static <X> Set<X> intersection(Collection<Set<X>> sets) {
		if (sets.isEmpty()) return Empty.hashSet();
		if (sets.size() == 1) return Empty.hashSet(CollUtils.getOnlyElement(sets));
		
		// see comments in intersectionInplace for why we are doing this
		List<Set<X>> data = sortedCopy(sets, sortBySizeReversed);
		Set<X> intersection = Empty.hashSet(data.remove(data.size() - 1));
		
		return retainAllAll(intersection, data);
	}

	private static <X> Set<X> retainAllAll(Set<X> set, Collection<Set<X>> sets) {
		// it's important that we're retaining with sets, because it will perform something like:
		// for item in sets:
		//   set = [i for i in set if i in item]
		for (Set<X> item : sets) {
			set.retainAll(item);
		}
		return set;
	}
	
	private static Comparator<Collection<?>> sortBySizeReversed = new Comparator<Collection<?>>() {
		public int compare(Collection<?> o1, Collection<?> o2) {
			// swap o1 and o2 to reverse ordering
			return ((Integer)o2.size()).compareTo(o1.size());
		}
	};

	/** @return a copy of the given map sorted by the key */
	public static <K extends Comparable<? super K>, V> Map<K, V> sortedByKeyCopy(Map<K, V> map) {
		Map<K, V> m = Empty.linkedHashMap();
		for (K k : CollUtils.sortedCopy(map.keySet())) m.put(k, map.get(k));
		return m;
	}

	/** Check if the first element in the list is Comparable, and if so try to sort, otherwise copy and return.
	 *
	 * All elements in the list must be Comparable with eachother - or not at all. */
	@SuppressWarnings({ "unchecked", "cast" }) // and can never be safe
	public static <X> List<X> sortedIfPossible(Collection<X> c, Comparator comp) {
		if (c.isEmpty()) return Collections.emptyList();
		return CollUtils.sortedCopy( (Collection<? extends Comparable>) c, comp);
	}

	/** Sort the given list and return a reference to it */
	public static <X extends Comparable<? super X>> List<X> sorted(List<X> l) {
		Collections.sort(l);
		return l;
	}

	/** Sort the given list, using the given comparator, and return a reference to it */
	public static <X> List<X> sorted(List<X> l, Comparator<? super X> comp) {
		if (comp != null) Collections.sort(l, comp);
		return l;
	}

	/** @return a sorted list copy of the given collection */
	public static <X extends Comparable<? super X>> List<X> sortedCopy(Collection<? extends X> c) {
		List<X> l = Empty.list(c);
		Collections.sort(l);
		return l;
	}

	/** @return a sorted list copy of the given collection, using the given comparator for ordering */
	public static <X> List<X> sortedCopy(Collection<? extends X> c, Comparator<? super X> comp) {
		List<X> l = Empty.list(c);
		Collections.sort(l, comp);
		return l;
	}

	/** XXX: requires distinct values! */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortedByValueCopy(Map<? extends K, V> map) {
		Map<V, K> im = new IdentityHashMap<V, K>();
		for (Map.Entry<? extends K, V> me : map.entrySet()) im.put(me.getValue(), me.getKey());
		Map<K, V> m = Empty.linkedHashMap();
		for (V v : sortedCopy(map.values())) m.put(im.get(v), v);
		return m;
	}

	/** XXX: requires distinct values! */
	public static <K, V> Map<K, V> sortedByValueCopy(Map<? extends K, V> map, Comparator<? super V> comp) {
		Map<V, K> im = new IdentityHashMap<V, K>();
		for (Map.Entry<? extends K, V> me : map.entrySet()) im.put(me.getValue(), me.getKey());
		Map<K, V> m = Empty.linkedHashMap();
		for (V v : sortedCopy(map.values(), comp)) m.put(im.get(v), v);
		return m;
	}

	/** Check if the first element in the list is Comparable, and if so try to sort, otherwise copy and return.
	 *
	 * All elements in the list must be Comparable with eachother - or not at all. */
	@SuppressWarnings("unchecked") // and can never be safe
	public static <X> List<X> sortedIfPossible(Collection<X> c) {
		if (c.isEmpty()) return Collections.emptyList();
		if (c.iterator().next() instanceof Comparable) return (List<X>) sortedCopy( (Collection<? extends Comparable>) c);
		return new ArrayList<X>(c);
	}

	/** Use the given comparator to create a sorted list with the elements from the collection. */
	@SuppressWarnings("unchecked") // by design
	public static <X> List<X> sortedUnsafe(Collection<X> c, Comparator<?> comp) {
		return sortedCopy(c, (Comparator<X>) comp);
	}

	/** @return the concatenation of the two collections */
	public static <X> List<X> concat(Collection<? extends X> c1, Collection<? extends X> c2) {
		List<X> l = Empty.list();
		l.addAll(c1);
		l.addAll(c2);
		return l;		
	}

	/** @return the union of the two collections */
	public static <X> Set<X> union(Collection<? extends X> c1, Collection<? extends X> c2) {
		Set<X> l = Empty.hashSet();
		l.addAll(c1);
		l.addAll(c2);
		return l;		
	}
	
}
