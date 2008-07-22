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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;




/** Formatting utilities
 *  
 * "The Format subclasses are not designed to be thread safe [...] But it's very easy to make NumberFormat 
 * thread-safe in this case, so we went ahead and fixed it anyway." -- java bug #4101500 
 * 
 * note that this only applies to format() - parse() is still dangerous 
 */
public class Format {

	/** DecimalFormat with three decimals accuracy */
	public static final DecimalFormat DF_3 = new DecimalFormat("0.0");
	static {
		DF_3.setMinimumFractionDigits(3);
		DF_3.setMaximumFractionDigits(3);
	}
	
	/** DecimalFormat with two decimals accuracy */
	public static final DecimalFormat DF_2 = new DecimalFormat("0.0");
	static {
		Format.DF_2.setMinimumFractionDigits(2);
		Format.DF_2.setMaximumFractionDigits(2);
	}

	/** DecimalFormat with one decimal accuracy */
	public static final DecimalFormat DF_1 = new DecimalFormat("0.0");
	static {
		Format.DF_1.setMinimumFractionDigits(1);
		Format.DF_1.setMaximumFractionDigits(1);
	}
	
	/** DecimalFormat without decimals */
	public static final DecimalFormat DF_0 = new DecimalFormat("0.0");
	static {
		Format.DF_0.setMaximumFractionDigits(0);
	}
	
	/** DecimalFormat for a percentage with one decimal accuracy */
	public static final DecimalFormat DF_PERCENT = new DecimalFormat("0%");
	static {
		Format.DF_PERCENT.setMinimumFractionDigits(1);
		Format.DF_PERCENT.setMaximumFractionDigits(1);
	}
	
	
	/** @return The decimal format based on the given number of decimals (up to 3 decimal digits ONLY) */
	public static final DecimalFormat getDecimalFormatFrom(int numDecimals) {
		if (numDecimals == 0) return Format.DF_0;
		if (numDecimals == 1) return Format.DF_1;
		if (numDecimals == 2) return Format.DF_2;
		if (numDecimals == 3) return Format.DF_3;
		throw new IllegalArgumentException("The number of decimals should be in the range [0-3]");
	}
	
	
	// non-final for Groovy reasons
	private static String[] siPostfix = new String[] { "", "K", "M", "G", "T" };
	public static final double EPS = 0.0000000001;
	
	/**
	 * Return n in SI format, e.i. postfix with K, M, G etc. If the resulting number
	 * has only one digit it will be returned with one decimal.
	 */
	public static String toSi(long n) {
		return toSiWithBase(n, 1000);
	}
	
	/** @return n in SI format with base 1024, e.g. "2.3MB". Forwards to {@link #toSiWithBase(long, int)} */
	public static String toSiBytes(long n) {
		return toSiWithBase(n, 1024) + "B";
	}
	
	/**
	 * @return n in SI format with the given base (typically 1000 or 1024), i.e. postfix with
	 * K, M, G etc. If the resulting number has only one digit it will be returned with one decimal.
	 */
	private static String toSiWithBase(long n, int siBase) {
		String prefix = ((n < 0) ? "-" : "");
		int i = 0;
		double v = Math.abs(n);
		while (v >= siBase) {
			v /= siBase;
			i++;
		}
		v += EPS;
		if (Math.round(v) >= siBase) {
			v /= siBase;
			i++;
		}
		if (i == 0 || v >= 9.5) return prefix + String.valueOf(Math.round(v)) + siPostfix[i];
		return prefix + DF_1.format(v + EPS) + siPostfix[i];
	}

	/**
	 * Return n in SI format, e.i. postfix with K, M, G etc. The number is intentionally
	 * made strongly ambiguous.
	 */
	public static String toSiObf(long n) {
		return 
			n == 0 ? "0" :
			n < 10 ? "<10" :
			n < 250 ? String.valueOf(10*((n+5)/10)) :
			toSi(n);
	}

	/** @return DateFormat.format, or null if the date argument is null */
	public static String format(DateFormat df, Date d) {
		return d == null ? null : df.format(d);
	}

	/**
	 * Format a time length in a rounded (pretty) form, using d/h/m/s instead of days / hours / minutes / seconds
	 * @param t the time length
	 * @param tu the unit of the given time length
	 * @return the formatted time length
	 */
	public static String formatPrettyButShort(long t, TimeUnit tu) {
		return Format.formatPretty(t, tu).replace(" days", "d").replace(" hours", "h").replace(" minutes", "m").replace(" seconds", "s");
	}

	/**
	 * Format a time length in a pretty form (rounded down)
	 * @param t the time length
	 * @param tu the unit of the given time length
	 * @return the formatted time length
	 */
	public static String formatPretty(long t, TimeUnit tu) {
		long days = TimeUnit.MILLISECONDS.convert(t, tu) / Clock.MILLISECONDS_PER_DAY;
		if (days > 0) t -= tu.convert(days, TimeUnit.MILLISECONDS) * Clock.MILLISECONDS_PER_DAY;
		long secs = TimeUnit.SECONDS.convert(t, tu);
		StringBuilder sb = Empty.sb();
		if (days > 0) sb.append(days + " days, ");
		if (secs >= 3600) sb.append(String.format("%.2f", secs / 3600.0) + " hours");
		else if (secs >= 60) sb.append(String.format("%.1f", secs / 60.0) + " minutes");
		else sb.append(String.format("%d", secs) + " seconds");
		return sb.toString();
	}

}
