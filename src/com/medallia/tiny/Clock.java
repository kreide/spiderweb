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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * A collection of utility functions for working with Date and Calendar objects.
 * 
 */
public class Clock {
	public static final long MILLISECONDS_PER_HOUR = 1000L * 60 * 60;
	public static final long MILLISECONDS_PER_DAY = MILLISECONDS_PER_HOUR * 24;
	
	private static long offset = 0;

	public static Date getTime() {
		return new Date(timeMillis());
	}

	public static Date now() { return getTime(); }

	/** Returns the date for the given number of milliseconds in the future */
	public static Date nowPlusMillis(long millis) {
		return new Date(Clock.timeMillis() + millis);
	}


	public static long timeMillis() {
		return System.currentTimeMillis() + offset;
	}

	public static void setTime(Date d) {
		offset = d.getTime() - System.currentTimeMillis();
	}
	
	/** @return the time offset methods of this class are currently using (for testing purposes) */
	public static long getOffset() {
		return offset;
	}
	
	/** set the time offset methods of this class are currently using (for testing purposes) */
	public static void setOffset(long d) {
		offset = d;
	}

	public static void setOffsetDays(double d) {
		offset = (long)(MILLISECONDS_PER_HOUR*24.0*d);
	}

	public static Date addDays(Date rootDate, int i) {
		return new Date(rootDate.getTime() + MILLISECONDS_PER_HOUR * 24 * i);
	}

	private static final Locale LOCALE_US = Locale.US;
	public static Calendar getCalendarNow() {
		return getCalendar(Clock.getTime());
	}
	
	/** Store a ThreadLocal version of the Calendar object since it is 
	 * slow to create.
	 */
	private static final ThreadLocal<Calendar> CALENDAR_TL = new ThreadLocal<Calendar>() {
		@Override protected Calendar initialValue() {
			return new GregorianCalendar(LOCALE_US);
		}
	};
	
	/**
	 * @return a Calendar, one unique object per thread, for the US
	 * where all the fields are blank.
	 */
	public static Calendar getThreadLocalCalendarClear() {
		Calendar c = CALENDAR_TL.get();
		c.clear();
		return c;
	}
	
	public static Calendar getCalendar(Date d) {
		return getCalendar(d, LOCALE_US);
	}
	public static Calendar getCalendar(Date d, Locale l) {
		Calendar c = new GregorianCalendar(l);
		c.setTime(d);
		return c;
	}

	private static final Date epoch = new Date(0);
	private static final long DAYS_SINCE_EPOCH_FACTOR = MILLISECONDS_PER_HOUR * 24;
	public static Date epoch() { return epoch; }

	/** Returns approx. number of days since the epoch; does not take into account leap years or other factors, but
	 * it is consistent with fromDaysSinceEpoch. The rollover is midnight in the local time zone */
	public static int getDaysSinceEpoch() {
		return getDaysSinceEpoch(getTime());
	}
	public static int getDaysSinceEpoch(Date d) {
		long t = d.getTime();
		// TODO: where and how is this used? is it always correct to use server time zone?
		t += TimeZone.getDefault().getOffset(t);
		t /= DAYS_SINCE_EPOCH_FACTOR;
		return (int) t;
	}

	/** Return the approx. date given the number of days since the epoch. Time is set to midnight. */
	public static Date fromDaysSinceEpoch(int daysSinceEpoch) {
		long t = daysSinceEpoch * DAYS_SINCE_EPOCH_FACTOR;
		t -= TimeZone.getDefault().getOffset(t);
		return new Date(t);
	}

	/** Return a Date that is set to the first weekday of the given date in the given locale */
	public static Date firstWeekdayOf(Date d, Locale l) {
		Calendar c = getCalendar(d, l);
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
		return c.getTime();
	}

	public static class ClockTest extends TestCase {
		public void testDaysSinceEpoch() throws Exception {
			DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			int k = getDaysSinceEpoch(dateTimeFormat.parse("2006-10-19 23:59:59"));
			assertEquals(dateTimeFormat.parse("2006-10-19 00:00:00"), fromDaysSinceEpoch(k));

			k = getDaysSinceEpoch(dateTimeFormat.parse("2006-01-19 00:00:00"));
			assertEquals(dateTimeFormat.parse("2006-01-19 00:00:00"), fromDaysSinceEpoch(k));
		}
	}

	/** Set the hour, minute, seconds and milliseconds to 0, unless endOfDay is true in which case it will be 23:59:59
	 *  @return reference to the given calendar */
	public static Calendar setTimeOfDay(Calendar c, boolean endOfDay) {
		if (endOfDay) {
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			c.set(Calendar.MILLISECOND, 999);
		} else {
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
		}
		return c;
	}
	
	/** Returns the date that comes first on our timeline. Note that null comes after all Date objects */
	public static Date firstDateWhereNullIsLatest(Date a, Date b) {
		if (a == null) return b;
		if (b == null) return a;
		return (a.before(b) ? a : b);
	}

}
