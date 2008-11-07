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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Various methods related to encoding of data and checksums
 */
public class Encoding {

	/** 
	 * @return the canonical hex-encoded MD5 hash of the given string.
	 */
	public static String md5(byte [] data) {
		if (data == null) return null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			return hexEncode(md5.digest(data));
		} catch ( NoSuchAlgorithmException ex ) {
			throw new AssertionError(ex);
		}
	}
	/** 
	 * @return the canonical hex-encoded MD5 hash of the given string.
	 */
	public static String md5(String s) {
		if (s == null) return null;
		try {
			return md5(s.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
	
	/**
	 * http://javapractices.com/Topic56.cjp
	 *
	 * Canonical hex encoding of a byte array; lowercase, no spaces.
	 */
	public static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
	/** @return a hex encoded string of the given bytes */
	public static String hexEncode(byte[] aInput) {
		StringBuilder result = new StringBuilder();
		for ( int idx = 0; idx < aInput.length; ++idx) {
			byte b = aInput[idx];
			result.append( HEX_DIGITS[ (b & 0xf0) >> 4 ] );
			result.append( HEX_DIGITS[ b & 0x0f] );
		}
		return result.toString();
	}
	/** @return a decoded (2-character) hex-encoded byte array, with 0 for non-bytes. */
	public static byte[] hexDecode(String hex) {
		int n = hex.length();
		byte[] a = new byte[n/2];
		n--;   // ignore single trailing character
		for (int i = 0; i < n; i += 2)
			try {
				a[i/2] = (byte)Integer.parseInt(hex.substring(i, i+2), 16);
			}
		catch (NumberFormatException e) { }   // ignore
		return a;
	}
	
	public static final String CHARSET_UTF8_NAME = "utf-8";
	public static final Charset CHARSET_UTF8 = Charset.forName(CHARSET_UTF8_NAME);
	
	/*
	 * This charset is most likely used if UTF-8 fails
	 * 
	 * Use something like this code to read strings via JDBC if the driver does not know what is
	 * going on, or the strings were inserted using the wrong encoding:
	 * new String(rs.getBytes(1),"ISO-8859-1")
	 */
	public static final String CHARSET_ISO_8859_NAME = "ISO-8859-1";
	public static final Charset CHARSET_ISO_8859 = Charset.forName(CHARSET_ISO_8859_NAME);
	
	/**
	 * Encode the given int as 4 bytes in the given byte array, or, if the array
	 * is null, create a new of length 4. The idx argument specifies where in the
	 * array the first byte should be placed.
	 * @return an encoding of k
	 * */
	public static byte[] toByteArray(byte[] b, int idx, int k) {
		if (b == null) b = new byte[4]; // wtf?
		for (int i = idx; i < idx + 4; i++) {
			int n = (k << 24) >> 24;
			b[i] = (byte)n;
			k >>= 8;
		}
		return b;
	}
	/**
	 * @return b as an int
	 */
	public static int fromByteArrayToInt(byte[] b, int idx) {
		int k = 0;
		for (int i = idx+3; i >= idx; i--) {
			k <<= 8;
			k |= b[i] & 0xff;
		}
		return k;
	}
	
	/**
	 * Determine if a String is representable in a given character encoding
	 * @return true iff <code>charset</code> can represent <code>text</code>
	 */
	public static boolean isEncodable(String text, Charset charset) {
		return charset.newEncoder().canEncode(text);
	}
	
	/** @return the UTF bytes of the given string */
	public static byte[] getUTF8Bytes(String s) {
		try {
			return s.getBytes(CHARSET_UTF8_NAME);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF8 doesn't exist", e);
		}
	}
	/** @return the String of the given UTF bytes */
	public static String fromUTF8Bytes(byte[] b) {
		try {
			return new String(b, CHARSET_UTF8_NAME);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF8 doesn't exist", e);
		}
	}
}
