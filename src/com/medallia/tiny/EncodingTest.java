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

import java.util.List;

import junit.framework.TestCase;


public class EncodingTest extends TestCase {
	public static final String ENGLISH = "For he on honeydew hath fed, And drunk the milk of Paradise";
	public static final String EUROPEAN = "Fõr hé ôn hØnæîdÊw hàth fëd, Ånd drûnk thé mìlk Öf Pârãdise.";
	public static final String SPECIALCHARS = "'-.,;:_*¨^~`?=)(/&%¤#\"!§|@£${[]}+´\\1234567890<>";
	public static final String RUSSIAN = "осуществляет ";
	public static final String FOREIGN_MIX = "je suis grand et vert. 広告掲載 만장 이상 보유. 광고용, 출판용";
	public static final String EVIL_STRING_NOLF = "quote\"ba[=ng]!eq=t\\'ick'back`><&amp;☃ $etc ";
	public static final String EVIL_STRING = EVIL_STRING_NOLF + " lf\n cr\015..";

	List<String> allStrings = Empty.<String>buildList()
		.add(ENGLISH)
		.add(EUROPEAN)
		.add(SPECIALCHARS)
		.add(RUSSIAN)
		.add(FOREIGN_MIX).get();
	
	private void run(String text, boolean passISO8859) {
		assertTrue(Encoding.isEncodable(text, Encoding.CHARSET_UTF8)); //UTF8 takes everything
		assertEquals(passISO8859, Encoding.isEncodable(text, Encoding.CHARSET_ISO_8859));
	}
	public void testSnowman() {
		run(EncodingTest.EVIL_STRING, false);
	}
	public void testNice() {
		run(ENGLISH, true);
		run(EUROPEAN, true);
		run(SPECIALCHARS, true);
	}
	public void testComplicated() {
		run(RUSSIAN, false);
		run(FOREIGN_MIX, false);
	}

	public void testHex() {
		for (String testString : allStrings) {
			String encoded = Encoding.hexEncode(Encoding.getUTF8Bytes(testString));
			String decoded = Encoding.fromUTF8Bytes(Encoding.hexDecode(encoded));
			assertEquals("failed on string '" + testString + "'", testString, decoded);
		}
	}
	
}
