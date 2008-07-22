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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Various utility functions for working with String objects.
 * 
 * @author kristian
 */
public class Strings {
	private static final Log log = LogFactory.getLog(Strings.class);

	/** @return false if any of the strings are null or have zero trimmed length */
	public static boolean allHaveContent(String... strings) {
		for (String s : strings)
			if (!hasContent(s)) return false;
		return true;
	}
	
	/** @return false if the string is null or has zero trimmed length */
	public static boolean hasContent(String s) {
		return s != null && s.trim().length() != 0;
	}

	/** Parses the input string as an integer. Returns null if the string is null, empty or does not contain a parsable integer. */ 
	public static Integer parseIntOrNull(String s) {
		if (!hasContent(s)) return null;
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}

	/** Throws AssertionError if any of the strings are null or have zero length */
	public static void assertAllHaveContent(String... strings) {
		if (!allHaveContent(strings)) throw new AssertionError("String is null or blank: " + Arrays.asList(strings));
	}

	/** Does exactly what you think */
	public static String rot13(String s) {
		StringBuilder sb = new StringBuilder(s);
		for (int i=0; i<sb.length(); i++) {
			char c = sb.charAt(i);
	
			if ((c>='a' && c<='m') || (c>='A' && c<='M')) {
				c += 13;
			} else if ((c>='n' && c<='z') || (c>='N' && c<='Z')) {
				c -= 13;
			}
	
			sb.setCharAt(i, c);
		}
		return sb.toString();
	}

	/** Return the given string with any white space on the right side removed */
	public static String trimRight(String s) {
		int k = s.length();
		while (k > 0 && Character.isWhitespace(s.charAt(k-1))) k--;
		return s.substring(0, k);
	}
	/** Return the given string with any white space on the left side removed */
	public static String trimLeft(String s) {
		int k = 0;
		while (k < s.length() && Character.isWhitespace(s.charAt(k))) k++;
		return s.substring(k, s.length());
	}

	/**
	 * @return a new array including only those strings which are non-empty
	 * after trim(). If trim is true then the array returned will include the
	 * trimmed version of the strings.
	 */
	public static List<String> removeEmptyAfterTrim(boolean trim, List<String> strings) {
		List<String> l = Empty.list();
		for (String s : strings) {
			if (s != null) {
				String t = s.trim();
				if (t.length() > 0) l.add(trim ? t : s);
			}
		}
		return l;
	}

	/** Forwards to {@link #removeEmptyAfterTrim(boolean, Collection)} */
	public static String[] removeEmptyAfterTrim(boolean trim, String... s) {
		return removeEmptyAfterTrim(trim, Arrays.asList(s)).toArray(new String[0]);
	}
	
	private static final Pattern COLLAPSE_WHITESPACE_PATTERN = Pattern.compile("\\s+");

	/** Replace any sequence of whitespace with a single space and trim */
	public static String collapseWhitespace(String s) {
		return COLLAPSE_WHITESPACE_PATTERN.matcher(s).replaceAll(" ").trim();
	}
	
	private static final Pattern SINGLE_WORDS_PATTERN = Pattern.compile("\\W+");
	
	/** Replace all non-word characters with a space and trim */
	public static String singleWords(String s) {
		return SINGLE_WORDS_PATTERN.matcher(s).replaceAll(" ").trim();
	}
	
	/**
	 * Compare two strings for equality, after running collapseWhitespace
	 * @return whether the two strings have the same black content
	 */
	public static boolean equalsAfterWhitespaceCollapse(String a, String b) {
		if (a == null && b == null) return true;
		if (a == null || b == null) return false;
		return collapseWhitespace(a).equals(collapseWhitespace(b));
	}

	/** Forwards to {@link #removeEmptyAfterTrim(boolean, List)} */
	public static String[] trimRemoveEmpty(String... s) {
		return removeEmptyAfterTrim(true, s);
	}

	/** @return the array with each string trimmed if it is non-null */
	public static String[] trim(String... s) {
		for (int i=0; i < s.length; i++)
			if (s[i] != null) s[i] = s[i].trim();
		return s;
	}
	
	/** @return the array with each string trimmed if it is non-null */
	public static List<String> trim(List<String> l) {
		return Arrays.asList(trim(l.toArray(new String[0])));
	}
	
	/** Forwards to {@link #removeEmptyAfterTrim(boolean, List)} with 'true' for the first argument */
	public static List<String> trimRemoveEmpty(List<String> l) {
		return removeEmptyAfterTrim(true, l);
	}

	/** @return the string trimmed if it is non-null, otherwise null */
	public static String trimmed(String s) {
		return trim(s)[0];
	}

	/** Extract String from obj; if it is a String[], take the first element, otherwise toString */
	public static String extract(Object obj) {
		if (obj==null) return null;
		if (obj instanceof String[]) {
			String[] ss = (String[])obj;
			if (ss.length>1) {
				log.warn("more than one entry in request parameter: " + Arrays.toString(ss), new Throwable());
			}
			if (ss.length==0) {
				log.warn("no entries in request parameter", new Throwable());
				return null;
			}
			return ss[0];
		}
		return obj.toString();
	}
	
	/** Function for properly capitializing english names.
	 *
	 * Original code found here: http://freejava.info/capitalize-english-names/
	 * Permission was given (by email to Kristian Eide on 2006-06-29 from the author
	 * Dimiter Petrov <hinotori2772@gmail.com> to use the code as we see fit.
	 */
	public static String capitalizeAndTrimEnglishNames(String txt) {
		if (txt == null) return "";
		String lcName = txt.toLowerCase().trim();
		if (lcName.indexOf(' ') == -1)
			return capitalizeSingleEnglishName(lcName);
		StringBuilder res = new StringBuilder(lcName.length());
		String [] names = lcName.split(" ");
		for (int i = 0; i < names.length; i++) {
			if (i > 0)
				res.append(" ");
			res.append(capitalizeSingleEnglishName(names[i]));
		}
		return res.toString();
	}
	private static String capitalizeSingleEnglishName(String lcName) {
		if (lcName == null || lcName.length() == 0)
			return "";
		String exFound = enCapExceptions.get(lcName);
		if (exFound != null)
			return exFound;
		StringBuilder res = new StringBuilder(lcName.length());
		int i;
		int n = lcName.length();
		if (lcName.startsWith("d'")) {
			res.append("d'");
			i = 2;
		} else if (lcName.startsWith("mc")) {
			res.append("Mc");
			if (n > 2)
				res.append(Character.toUpperCase(lcName.charAt(2)));
			i = 3;
		} else if (lcName.startsWith("mac")) {
			res.append("Mac");
			if (n > 3)
				res.append(Character.toUpperCase(lcName.charAt(3)));
			i = 4;
		} else {
			res.append(Character.toUpperCase(lcName.charAt(0)));
			i = 1;
		}
		for ( ; i < n ; i++) {
			if (lcName.charAt(i) == ' ' && (i > 0) && (lcName.charAt(i-1) != ' '))
				res.append(' ');
			if (i == 0)
				res.append(Character.toUpperCase(lcName.charAt(i)));
			else {
				switch (lcName.charAt(i-1)) {
				case '-':
				case '.':
				case ' ':
					res.append(Character.toUpperCase(lcName.charAt(i)));
					break;
				case '\'':
					if (i == (n-1))
						res.append(lcName.charAt(i));
					else
						res.append(Character.toUpperCase(lcName.charAt(i)));
					break;
				default:
					res.append(lcName.charAt(i));
				break;
				}
			}
		}
		return res.toString();
	}
	private static final Map<String, String> enCapExceptions = Empty.hashMap();
	static {
		enCapExceptions.put("macintosh", "Macintosh");
		enCapExceptions.put("von", "von");
		enCapExceptions.put("van", "van");
		enCapExceptions.put("de", "de");
		enCapExceptions.put("la", "la");
		enCapExceptions.put("da", "da");
		enCapExceptions.put("di", "di");
	}
	/**
	 * Splits the given string into a list of tokens separated by sep.
	 * Returns an empty list if str is null.
	 */
	public static List<String> split(String str, String sep) {
		return split(str, sep, false);
	}
	
	/**
	 * Splits the given string into a list of tokens separated by sep.
	 * @param trim If true, tokens are trimmed.
	 * @return The list of tokens or an empty list if str is null.
	 */
	public static List<String> split(String str, String sep, boolean trim) {
		List<String> l = Empty.list();
		if (str != null) {
			StringTokenizer st = new StringTokenizer(str, sep);
			while (st.hasMoreTokens())
				l.add(trim ? st.nextToken().trim() : st.nextToken());
		}
		return l;
	}
	
	/** split the given string on the given regex and return all non-empty elements */
	public static List<String> splitNoEmpty(String s, String regex) {
		List<String> l = Empty.list();
		for (String k : s.split(regex)) {
			if (hasContent(k)) l.add(k);
		}
		return l;
	}

	/**
	 * Splits the given string into a list of integers separated by sep.
	 * 'null' strings are not allowed and will throw a NumberFormatException.
	 */
	public static List<Integer> splitToInt(String str, String sep) {
		return splitToInt(str, sep, false);
	}

	/**
	 * Splits the given string into a list of integers separated by sep.
	 * If allowNull is true, the string 'null' is parsed as a null Integer object.
	 * If allowNull is false, the string 'null' is not allowed and throws a NumberFormatException.
	 */
	public static List<Integer> splitToInt(String str, String sep, boolean allowNull) {
		List<String> sl = split(str, sep);
		List<Integer> l = Empty.list();
		for (String s : sl) {
			String st = s.trim();
			if (allowNull && st.equals("null")) l.add(null);
			else l.add(Integer.valueOf(st));
		}
		return l;
	}

	/**
	 * Splits the given string into a list of doubles separated by sep.
	 */
	public static List<Double> splitToDouble(String str, String sep) {
		List<String> sl = split(str, sep);
		List<Double> l = Empty.list();
		for (String s : sl) l.add(Double.valueOf(s.trim()));
		return l;
	}
	
	/**
	 * Join strings by separator, folding nulls and empty strings.
	 */
	public static String join(String sep, String... ting) {
		return join(sep, true, ting);
	}
	/**
	 * Join strings by separator, folding nulls (and optionally empty strings)
	 */
	public static String join(String sep, boolean foldEmpty, String... ting) {
		if (ting.length == 0) return "";
		if (ting.length == 1) return ting[0];
		StringBuilder sb = Empty.sb();
		String mysep = "";
		for (String s : ting) {
			if (s == null || (foldEmpty && s.length() == 0)) continue;
			sb.append(mysep);
			sb.append(s);
			mysep = sep;
		}
		return sb.toString();
	}
	
	/** Collection version of join */
	public static String join(String sep, Collection<String> c) {
		return join(sep, c.toArray(new String[0]));
	}
	
	/**
	 * Join the toString of each object element into a single string, with
	 * each element separated by the given sep (which can be empty).
	 */
	public static String joinObjects(String sep, Collection<? extends Object> l) {
		return sepList(sep, l, -1);
	}
	
	/** Collection version of join */
	public static String join(String sep, boolean foldEmpty, Collection<String> c) {
		return join(sep, foldEmpty, c.toArray(new String[0]));
	}

	/** Array version of sepList */
	public static String sepList(String sep, Object[] arg, int max) {
		return sepList(sep, Arrays.asList(arg), max);
	}
	/** Same as sepList with no wrapping */
	public static String sepList(String sep, Iterable<?> os, int max) {
		return sepList(sep, null, os, max);
	}
	/** Return the concatenation of toString of the objects obtained from the iterable, separated by sep, and if max
	 * is > 0 include no more than that number of objects. If wrap is non-null, prepend and append each object with it
	 */
	public static String sepList(String sep, String wrap, Iterable<?> os, int max) {
		StringBuilder sb = Empty.sb();
		String s = "";
		if (max == 0) max = -1;
		for (Object o : os) {
			sb.append(s); s = sep;
			if (max-- == 0) { sb.append("..."); break; }
			if (wrap != null) sb.append(wrap);
			sb.append(o);
			if (wrap != null) sb.append(wrap);
		}
		return sb.toString();
	}

	/** Return toString() of the given object, or null if the returned value is a
	 * zero length string */
	public static String toStringBlankIsNull(Object o) {
		if (o == null) return null;
		String s = String.valueOf(o).trim();
		return s.length() == 0 ? null : s;
	}

	/** Return toString() of the given object, or a zero length string if either the argument
	 * or the value returned from toString is null */
	public static String toStringNullIsBlank(Object o) {
		if (o==null) return "";
		String s = o.toString();
		if (s==null) return "";
		return s;
	}
	
	/** Return the bytes of toString on the given object in the given charset */
	public static byte[] getBytes(Object o, Charset cs) {
		try {
			return o.toString().getBytes(cs.name());
		} catch (UnsupportedEncodingException ex) {
			throw new AssertionError(ex);
		}
	}

	/** Convert the given bytes to a String using the given charset */
	public static String fromBytes(byte[] bytes, Charset cs) {
		try {
			return new String(bytes, cs.name());
		} catch (UnsupportedEncodingException ex) {
			throw new AssertionError(ex);
		}
	}

	/** Return null if the argument is null, otherwise s.toLowerCase */
	public static String lower(String s) {
		return s == null ? s : s.toLowerCase();
	}
	
	/** Return a version of the given string with line feeds inserted in an attempt to make
	 * no line wider than the given margin. Note that some lines might still exceed the
	 * margin if there is no space to wrap on (long words are not wrapped) */
	public static String wrapLines(String text, int margin) {
		StringBuilder sb = Empty.sb();
		int lastSpace = -1, curLineLength = 0;
		for (int i=0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\n') {
				curLineLength = 0;
				lastSpace = -1;
			}
			if (curLineLength >= margin && lastSpace >= 0) {
				sb.setCharAt(lastSpace, '\n');
				curLineLength = 0;
				lastSpace = -1;
			} else if (c == ' ') {
				lastSpace = sb.length();
			}
			curLineLength++;
			sb.append(c);
		}
		return sb.toString();
	}

	/** Capitalize the first character of the given string and lowercase the rest; uses the default locale, so beware */
	public static String capitalizeFirstCharacterLowercaseRest(String s) {
		if (!hasContent(s)) return s;
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}
	
	/** Capitalize the first character of the given string; uses the default locale, so beware */
	public static String capitalizeFirstCharacter(String s) {
		if (!hasContent(s)) return s;
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}
	
	/** @return The string in the given collection of strings that best matches our string (case insensitive), or null if the given collection is empty */
	public static String findBestMatch(String mine, Collection<String> others) {
		return findBestMatchOrNull(mine, others, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Returns string in the given collection of strings that best matches our string (case insensitive), or null if none of the strings
	 * matches particularly well. If you need to specify what counts as 'particularly well', please use the
	 * findBestMatchOrNull(mine, others, minSize, maxDistance) method instead.
	 * @param mine The string we want to match against
	 * @param others The collection of string we want to match against our string to find the best match
	 * @return The best matching others-string or null if none matches
	 */
	public static String findBestMatchOrNull(String mine, Collection<String> others) {
		return findBestMatchOrNull(mine, others, 3, Math.max(1, (int) (mine.length() * 0.25)));
	}

	/**
	 * Returns string in the given collection of strings that best matches our string (case insensitive), or null if none of the strings
	 * matches particularly well.
	 * @param mine The string we want to match against
	 * @param others The collection of string we want to match against our string to find the best match
	 * @param minSize The minimum length our string has to be trigger the algorithm. 0 here counts as 
	 * @param maxDistance The maximum distance between the strings before they are considered as not matching
	 * @return The best matching others-string or null if all strings have a distance longer than maxDistance
	 */
	public static String findBestMatchOrNull(String mine, Collection<String> others, int minSize, int maxDistance) {
		String bestMatch = null;
		if (mine.length() < minSize && minSize > 0) return null;
		int bestDistance = mine.length() + 1;
		for (String other : others) {
			if (Math.abs(other.length() - mine.length()) > maxDistance) continue;
			int distance = Strings.editDist(mine, other, false);
			if (distance < bestDistance && distance <= maxDistance) {
				bestDistance = distance;
				bestMatch = other;
			}
		}
		return bestMatch;
	}
	
	/**
	 * Damerau-Levenshtein edit distance
	 * param a misspelled string
	 * param b (sub)string to look for
	 * param substring look for substring or match whole word
	 * return edit distance
	 */
	public static int editDist(String a, String b, boolean substring) {
		return editDist(a.toLowerCase().toCharArray(), b.toLowerCase().toCharArray(), substring);
	}
	/**
	 * Damerau-Levenshtein edit distance
	 * param a misspelled string
	 * param b (sub)string to look for
	 * param substring look for substring or match whole word
	 * return edit distance
	 */
	public static int editDist(char[] a, char[] b, boolean substring) {
		int alen = a.length;
		int blen = b.length;
		int[][] d = new int[alen+1][blen+1];
		if (substring) {
			// free removal at start
			int cnt = 0;
			for (int i=alen-blen+1; i<=alen; ++i) d[i][0] = cnt++;
		} else {
			for (int i=0; i<=alen; ++i) d[i][0] = i;
		}
		for (int j=1; j<=blen; ++j) d[0][j] = j;

		for (int i=1; i<=alen; ++i) {
			for (int j=1; j<=blen; ++j) {
				int swap = 1;
				if (a[i-1]==b[j-1]) swap = 0;
				int val = d[i-1][j]+1;
				if (val > d[i][j-1]+1)
					val = d[i][j-1]+1;
				if (val > d[i-1][j-1]+swap)
					val = d[i-1][j-1]+swap;
				if (i>1 && j>1 && a[i-1]==b[j-2] && a[i-2]==b[j-1] && val>d[i-2][j-2]) {
					val = d[i-2][j-2]+1;
				}
				d[i][j] = val;
			}
		}
		int best = d[alen][blen];
		
		if (substring) {
			// free removal at end
			for (int i=blen-1; i<=alen; ++i) {
				if (d[i][blen]<best)
					best = d[i][blen];
			}
			//print(d);
		}
		
		return best;
	}

	/** Return a max number of chars of the given string for display purposes; some extra chars are
	 * added at the end to explain how much is missing. */
	public static String maxForDisplay(String str, int max) {
		int l = str.length();
		if (l <= max) return str;
		return str.substring(0, max) + " (" + (l-max) + " more chars)";
	}

	/**
	 * Splits the string s on characters c not preceded by an odd number of escape characters '\'.
	 * All escape characters are left intact.
	 * @param s The string to split
	 * @param c The charater to split on
	 * @return A tokenized version of s
	 */
	public static String[] splitUnescaped(String s, char c) {
		List<String> r = Empty.list();
		char [] car = s.toCharArray();
		boolean isEscaped = false;
		int lastp = 0;
		for (int i = 0; i < car.length; i++) {
			if (isEscaped) {
				isEscaped = false;
				continue;
			}
			else if (car[i] == '\\') isEscaped = true;
			else if (car[i] == c) {
				r.add(s.substring(lastp, i));
				lastp = i+1;
			}
		}
		r.add(s.substring(lastp));
		return r.toArray(new String[0]);
	}

	/**
	 * Unescapes all characters escaped by '\' in the string.
	 * No characters are taken to have any special meaning. For instance, an escaped 'n' just become a regular 'n'.
	 * @param s
	 * @return s with escape characters removed.
	 */
	public static String unescape(String s) {
		return s.replaceAll("\\\\(.)", "$1");
	}

	/**
	 * Splits the string s on characters c not preceded by an odd number of escape characters '\'.
	 * All escaped characters are unescaped.
	 * @param s The string to split
	 * @param c The charater to split on
	 * @return A tokenized version of s
	 */
	public static String[] splitAndUnescape(String s, char c) {
		String[] ans = splitUnescaped(s, c);
		for (int i=0; i<ans.length; ++i) {
			ans[i] = unescape(ans[i]);
		}
		return ans;
	}
	
	/** test cases for the Strings methods */
	public static class Test extends TestCase {

		/** test that the {@link Strings#capitalizeFirstCharacter(String)} method works */
		public void testCapitalizeFirst() {
			assertEquals(null, capitalizeFirstCharacter(null));
			assertEquals("", capitalizeFirstCharacter(""));
			assertEquals("A", capitalizeFirstCharacter("a"));
			assertEquals("Aa", capitalizeFirstCharacter("aa"));
			assertEquals("AA", capitalizeFirstCharacter("aA"));
		}
		
		/** test that the {@link Strings#capitalizeFirstCharacterLowercaseRest(String)} method works */
		public void testCapitalizeFirstLowerRest() {
			assertEquals(null, capitalizeFirstCharacterLowercaseRest(null));
			assertEquals("", capitalizeFirstCharacterLowercaseRest(""));
			assertEquals("A", capitalizeFirstCharacterLowercaseRest("a"));
			assertEquals("Aa", capitalizeFirstCharacterLowercaseRest("aa"));
			assertEquals("Aa", capitalizeFirstCharacterLowercaseRest("aA"));
		}
		
		/** test that the {@link Strings#wrapLines(String, int)} method works */
		public void testLineWrap() {
			String[] s = {
				"abcdef\nab cdef", "abcdef\nab\ncdef",
				"ab  d", "ab \nd",
				"abcd", "abcd",
				"abcd\n", "abcd\n",
				"abcdefgh", "abcdefgh",
				"abcd\n", "abcd\n",
				"ab   ", "ab \n ",
				"ab a\nab cd\na cdef\nabcdef", "ab a\nab\ncd\na\ncdef\nabcdef",
			};
			for (int i=0; i < s.length; i += 2) {
				check(wrapLines(s[i], 4), s[i+1]);
			}
		}
		private void check(String expect, String was) {
			String p1 = expect.replace('\n', '$');
			String p2 = was.replace('\n', '$');
			if (!p1.equals(p2)) System.out.println(p1 + "|\n---\n" + p2 + "|\n===");
			assertEquals(p1 + " not " + p2, expect, was);
		}
		
		/** Test the bestMatch feature, both default values and corner cases */
		public void testBestMatch() {
			doTest("a", 0, 1, "b", "b");
			doTest("a", 0, 1, "B", "B");
			doTest("a", null, "B"); // too small for defaults
			doTest("jllkkesrjjls", "jlktesrjgls", "hfdklaj", "jlktesrjgls", "kdplasfre");
			doTest("jkerlwjksdf", null, "hfdklaj", "jlktesrjgls", "kdplasfre"); // too far apart
			doTest("heisann!", "heisannja!", "heisannja!", "fjjflaf");
			doTest("heisann!", 12, 3, null, "heisannja!", "fjjflaf"); // too short string
			doTest("heisann!", 4, 1, null, "heisannja!", "fjjflaf"); // too far apart
		}
		
		private void doTest(String mine, String solution, String... others) {
			assertEquals(solution, findBestMatchOrNull(mine, Arrays.asList(others)));
		}
		private void doTest(String mine, int minSize, int maxDistance, String solution, String... others) {
			assertEquals(solution, findBestMatchOrNull(mine, Arrays.asList(others), minSize, maxDistance));
		}
	}

}
