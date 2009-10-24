/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Adapted CG 20090811 from Apache Harmony 'enhanced' revision 793553.
 * Removed dependency on com.ibm.icu.text.DateFormat. */

package java.text;

import java.io.InvalidObjectException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;

/**
 * An abstract class for date/time formatting subclasses which formats and
 * parses dates or time in a language-independent manner. The date/time
 * formatting subclass, such as {@link SimpleDateFormat}, allows for formatting
 * (i.e., date -> text), parsing (text -> date), and normalization. The date is
 * represented as a {@code Date} object or as the milliseconds since January 1,
 * 1970, 00:00:00 GMT.
 * <p>
 * DateFormat provides many class methods for obtaining default date/time
 * formatters based on the default or a given locale and a number of formatting
 * styles. The formatting styles include FULL, LONG, MEDIUM, and SHORT. More
 * details and examples for using these styles are provided in the method
 * descriptions.
 * <p>
 * {@code DateFormat} helps you to format and parse dates for any locale. Your
 * code can be completely independent of the locale conventions for months, days
 * of the week, or even the calendar format: lunar vs. solar.
 * <p>
 * To format a date for the current Locale, use one of the static factory
 * methods:
 * <blockquote>
 *
 * <pre>
 * myString = DateFormat.getDateInstance().format(myDate);
 * </pre>
 *
 * </blockquote>
 * <p>
 * If you are formatting multiple dates, it is more efficient to get the format
 * and use it multiple times so that the system doesn't have to fetch the
 * information about the local language and country conventions multiple times.
 * <blockquote>
 *
 * <pre>
 * DateFormat df = DateFormat.getDateInstance();
 * for (int i = 0; i &lt; a.length; ++i) {
 *     output.println(df.format(myDate[i]) + &quot;; &quot;);
 * }
 * </pre>
 *
 * </blockquote>
 * <p>
 * To format a number for a different locale, specify it in the call to
 * {@code getDateInstance}:
 * <blockquote>
 *
 * <pre>
 * DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);
 * </pre>
 *
 * </blockquote>
 * <p>
 * {@code DateFormat} can also be used to parse strings:
 * <blockquote>
 *
 * <pre>
 * myDate = df.parse(myString);
 * </pre>
 *
 * </blockquote>
 * <p>
 * Use {@code getDateInstance} to get the normal date format for a country.
 * Other static factory methods are available: Use {@code getTimeInstance} to
 * get the time format for a country. Use {@code getDateTimeInstance} to get the
 * date and time format. You can pass in different options to these factory
 * methods to control the length of the result; from SHORT to MEDIUM to LONG to
 * FULL. The exact result depends on the locale, but generally:
 * <ul>
 * <li>SHORT is completely numeric, such as 12.13.52 or 3:30pm
 * <li>MEDIUM is longer, such as Jan 12, 1952
 * <li>LONG is longer, such as January 12, 1952 or 3:30:32pm
 * <li>FULL is pretty completely specified, such as Tuesday, April 12, 1952 AD
 * or 3:30:42pm PST.
 * </ul>
 * <p>
 * If needed, the time zone can be set on the format. For even greater control
 * over the formatting or parsing, try casting the {@code DateFormat} you get
 * from the factory methods to a {@code SimpleDateFormat}. This will work for
 * the majority of countries; just remember to put it in a try block in case you
 * encounter an unusual one.
 * <p>
 * There are versions of the parse and format methods which use
 * {@code ParsePosition} and {@code FieldPosition} to allow you to
 * <ul>
 * <li>progressively parse through pieces of a string;
 * <li>align any particular field.
 * </ul>
 * <h4>Synchronization</h4>
 * <p>
 * Date formats are not synchronized. It is recommended to create separate
 * format instances for each thread. If multiple threads access a format
 * concurrently, it must be synchronized externally.
 *
 * @see NumberFormat
 * @see SimpleDateFormat
 * @see Calendar
 * @see TimeZone
 */
public abstract class DateFormat extends Format {

  private static final long serialVersionUID = 7218322306649953788L;

  /**
   * The calendar that this {@code DateFormat} uses to format a number
   * representing a date.
   */
  protected Calendar calendar;

  /**
   * The number format used to format a number.
   */
  protected NumberFormat numberFormat;

  /**
   * The format style constant defining the default format style. The default
   * is MEDIUM.
   */
    public final static int DEFAULT = 2;

  /**
   * The format style constant defining the full style.
   */
  public final static int FULL = 0;

  /**
   * The format style constant defining the long style.
   */
  public final static int LONG = 1;

  /**
   * The format style constant defining the medium style.
   */
  public final static int MEDIUM = 2;

  /**
   * The format style constant defining the short style.
   */
  public final static int SHORT = 3;

  /**
   * The {@code FieldPosition} selector for 'G' field alignment, corresponds
   * to the {@link Calendar#ERA} field.
   */
  public final static int ERA_FIELD = 0;

  /**
   * The {@code FieldPosition} selector for 'y' field alignment, corresponds
   * to the {@link Calendar#YEAR} field.
   */
  public final static int YEAR_FIELD = 1;

  /**
   * The {@code FieldPosition} selector for 'M' field alignment, corresponds
   * to the {@link Calendar#MONTH} field.
   */
  public final static int MONTH_FIELD = 2;

  /**
   * The {@code FieldPosition} selector for 'd' field alignment, corresponds
   * to the {@link Calendar#DATE} field.
   */
  public final static int DATE_FIELD = 3;

  /**
   * The {@code FieldPosition} selector for 'k' field alignment, corresponds
   * to the {@link Calendar#HOUR_OF_DAY} field. {@code HOUR_OF_DAY1_FIELD} is
   * used for the one-based 24-hour clock. For example, 23:59 + 01:00 results
   * in 24:59.
   */
  public final static int HOUR_OF_DAY1_FIELD = 4;

  /**
   * The {@code FieldPosition} selector for 'H' field alignment, corresponds
   * to the {@link Calendar#HOUR_OF_DAY} field. {@code HOUR_OF_DAY0_FIELD} is
   * used for the zero-based 24-hour clock. For example, 23:59 + 01:00 results
   * in 00:59.
   */
  public final static int HOUR_OF_DAY0_FIELD = 5;

  /**
   * FieldPosition selector for 'm' field alignment, corresponds to the
   * {@link Calendar#MINUTE} field.
   */
  public final static int MINUTE_FIELD = 6;

  /**
   * FieldPosition selector for 's' field alignment, corresponds to the
   * {@link Calendar#SECOND} field.
   */
  public final static int SECOND_FIELD = 7;

  /**
   * FieldPosition selector for 'S' field alignment, corresponds to the
   * {@link Calendar#MILLISECOND} field.
   */
    public final static int MILLISECOND_FIELD = 8;

  /**
   * FieldPosition selector for 'E' field alignment, corresponds to the
   * {@link Calendar#DAY_OF_WEEK} field.
   */
  public final static int DAY_OF_WEEK_FIELD = 9;

  /**
   * FieldPosition selector for 'D' field alignment, corresponds to the
   * {@link Calendar#DAY_OF_YEAR} field.
   */
  public final static int DAY_OF_YEAR_FIELD = 10;

  /**
   * FieldPosition selector for 'F' field alignment, corresponds to the
   * {@link Calendar#DAY_OF_WEEK_IN_MONTH} field.
   */
  public final static int DAY_OF_WEEK_IN_MONTH_FIELD = 11;

  /**
   * FieldPosition selector for 'w' field alignment, corresponds to the
   * {@link Calendar#WEEK_OF_YEAR} field.
   */
  public final static int WEEK_OF_YEAR_FIELD = 12;

  /**
   * FieldPosition selector for 'W' field alignment, corresponds to the
   * {@link Calendar#WEEK_OF_MONTH} field.
   */
  public final static int WEEK_OF_MONTH_FIELD = 13;

  /**
   * FieldPosition selector for 'a' field alignment, corresponds to the
   * {@link Calendar#AM_PM} field.
   */
  public final static int AM_PM_FIELD = 14;

  /**
   * FieldPosition selector for 'h' field alignment, corresponding to the
   * {@link Calendar#HOUR} field. {@code HOUR1_FIELD} is used for the
   * one-based 12-hour clock. For example, 11:30 PM + 1 hour results in 12:30
   * AM.
   */
  public final static int HOUR1_FIELD = 15;

  /**
   * The {@code FieldPosition} selector for 'z' field alignment, corresponds
   * to the {@link Calendar#ZONE_OFFSET} and {@link Calendar#DST_OFFSET}
   * fields.
   */
  public final static int HOUR0_FIELD = 16;

  /**
   * The {@code FieldPosition} selector for 'z' field alignment, corresponds
   * to the {@link Calendar#ZONE_OFFSET} and {@link Calendar#DST_OFFSET}
   * fields.
   */
  public final static int TIMEZONE_FIELD = 17;

  /**
   * Constructs a new instance of {@code DateFormat}.
   */
  protected DateFormat() {
  }

  /**
   * Returns a new instance of {@code DateFormat} with the same properties.
   * 
   * @return a shallow copy of this {@code DateFormat}.
   * 
   * @see java.lang.Cloneable
   */
  public Object clone() {
    DateFormat clone = (DateFormat) super.clone();
    clone.calendar = (Calendar) calendar.clone();
    clone.numberFormat = (NumberFormat) numberFormat.clone();
    return clone;
  }

  /**
   * Compares this date format with the specified object and indicates if they
   * are equal.
   * 
   * @param object
   *            the object to compare with this date format.
   * @return {@code true} if {@code object} is a {@code DateFormat} object and
   *         it has the same properties as this date format; {@code false}
   *         otherwise.
   * @see #hashCode
   */
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof DateFormat)) {
      return false;
    }
    DateFormat dateFormat = (DateFormat) object;
    return numberFormat.equals(dateFormat.numberFormat)
            && calendar.getTimeZone().equals(dateFormat.calendar.getTimeZone())
            && calendar.getFirstDayOfWeek() == dateFormat.calendar.getFirstDayOfWeek()
            && calendar.getMinimalDaysInFirstWeek() == dateFormat.calendar.getMinimalDaysInFirstWeek()
            && calendar.isLenient() == dateFormat.calendar.isLenient();
  }

  /**
   * Formats the specified object as a string using the pattern of this date
   * format and appends the string to the specified string buffer.
   * <p>
   * If the {@code field} member of {@code field} contains a value specifying
   * a format field, then its {@code beginIndex} and {@code endIndex} members
   * will be updated with the position of the first occurrence of this field
   * in the formatted text.
   *
   * @param object
   *            the source object to format, must be a {@code Date} or a
   *            {@code Number}. If {@code object} is a number then a date is
   *            constructed using the {@code longValue()} of the number.
   * @param buffer
   *            the target string buffer to append the formatted date/time to.
   * @param field
   *            on input: an optional alignment field; on output: the offsets
   *            of the alignment field in the formatted text.
   * @return the string buffer.
   * @throws IllegalArgumentException
   *            if {@code object} is neither a {@code Date} nor a
   *            {@code Number} instance.
   */
  public final StringBuffer format(Object object, StringBuffer buffer,
            FieldPosition field) {
    if (object instanceof Date) {
      return format((Date) object, buffer, field);
    }
    if (object instanceof Number) {
      return format(new Date(((Number) object).longValue()), buffer, field);
    }
    throw new IllegalArgumentException();
  }

  /**
   * Formats the specified date using the rules of this date format.
   * 
   * @param date
   *            the date to format.
   * @return the formatted string.
   */
  public final String format(Date date) {
    return format(date, new StringBuffer(64), new FieldPosition(0)).toString();
  }

  /**
   * Formats the specified date as a string using the pattern of this date
   * format and appends the string to the specified string buffer.
   * <p>
   * If the {@code field} member of {@code field} contains a value specifying
   * a format field, then its {@code beginIndex} and {@code endIndex} members
   * will be updated with the position of the first occurrence of this field
   * in the formatted text.
   *
   * @param date
   *            the date to format.
   * @param buffer
   *            the target string buffer to append the formatted date/time to.
   * @param field
   *            on input: an optional alignment field; on output: the offsets
   *            of the alignment field in the formatted text.
   * @return the string buffer.
   */
  public abstract StringBuffer format(Date date, StringBuffer buffer, FieldPosition field);

  /**
   * Gets the list of installed locales which support {@code DateFormat}.
   * 
   * @return an array of locales.
   */
  // TODO: should be driven by mika.dateformats file?
  public static Locale[] getAvailableLocales() {
    return Locale.getAvailableLocales();
  }

  /**
   * Returns the calendar used by this {@code DateFormat}.
   * 
   * @return the calendar used by this date format.
   */
  public Calendar getCalendar() {
      return calendar;
  }

  /**
   * Returns a {@code DateFormat} instance for formatting and parsing dates in
   * the DEFAULT style for the default locale.
   * 
   * @return the {@code DateFormat} instance for the default style and locale.
   */
  public final static DateFormat getDateInstance() {
    return getDateInstance(DEFAULT, Locale.getDefault());
  }

  /**
   * Returns a {@code DateFormat} instance for formatting and parsing dates in
   * the specified style for the default locale.
   * 
   * @param style
   *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT.
   * @return the {@code DateFormat} instance for {@code style} and the default
   *         locale.
   * @throws IllegalArgumentException
   *             if {@code style} is not one of SHORT, MEDIUM, LONG, FULL, or
   *             DEFAULT.
   */
    public final static DateFormat getDateInstance(int style) {
        checkDateStyle(style);
        return getDateInstance(style, Locale.getDefault());
    }

  /**
   * Returns a {@code DateFormat} instance for formatting and parsing dates in
   * the specified style for the specified locale.
   * 
   * @param style
   *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT.
   * @param locale
   *            the locale.
   * @throws IllegalArgumentException
   *             if {@code style} is not one of SHORT, MEDIUM, LONG, FULL, or
   *             DEFAULT.
   * @return the {@code DateFormat} instance for {@code style} and
   *         {@code locale}.
   */
  public final static DateFormat getDateInstance(int style, Locale locale) {
    checkDateStyle(style);
    return new SimpleDateFormat(DATESTYLES[style], locale);
  }

  // TODO: get these from mika.dateformats
  private static final String[] DATESTYLES = {"EEEE, d MMMM yyyy", "dd MMMM yyyy", "dd-MMM-yyyy", "dd/MM/yy"};
  private static final String[] TIMESTYLES = {"HH:mm:ss 'o''clock' z", "HH:mm:ss z", "HH:mm:ss", "HH:mm"};

  /**
   * Returns a {@code DateFormat} instance for formatting and parsing dates
   * and time values in the DEFAULT style for the default locale.
   * 
   * @return the {@code DateFormat} instance for the default style and locale.
   */
  public final static DateFormat getDateTimeInstance() {
    return getDateTimeInstance(DEFAULT, DEFAULT, Locale.getDefault());
  }

  /**
   * Returns a {@code DateFormat} instance for formatting and parsing of both
   * dates and time values in the manner appropriate for the default locale.
   * 
   * @param dateStyle
   *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT.
   * @param timeStyle
   *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT.
   * @return the {@code DateFormat} instance for {@code dateStyle},
   *         {@code timeStyle} and the default locale.
   * @throws IllegalArgumentException
   *             if {@code dateStyle} or {@code timeStyle} is not one of
   *             SHORT, MEDIUM, LONG, FULL, or DEFAULT.
   */
  public final static DateFormat getDateTimeInstance(int dateStyle, int timeStyle) {
      checkTimeStyle(timeStyle);
      checkDateStyle(dateStyle);
      return getDateTimeInstance(dateStyle, timeStyle, Locale.getDefault());
  }

  /**
   * Returns a {@code DateFormat} instance for formatting and parsing dates
   * and time values in the specified styles for the specified locale.
   * The Mika implementation simply concatenates the date and time.
   * 
   * @param dateStyle
   *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT.
   * @param timeStyle
   *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT.
   * @param locale
   *            the locale.
   * @return the {@code DateFormat} instance for {@code dateStyle},
   *         {@code timeStyle} and {@code locale}.
   * @throws IllegalArgumentException
   *             if {@code dateStyle} or {@code timeStyle} is not one of
   *             SHORT, MEDIUM, LONG, FULL, or DEFAULT.
   */
  public final static DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale locale) {
    checkTimeStyle(timeStyle);
    checkDateStyle(dateStyle);
    String pattern = DATESTYLES[dateStyle]+' '+TIMESTYLES[timeStyle];
    return new SimpleDateFormat(pattern, locale);
  }

  /**
   * Returns a {@code DateFormat} instance for formatting and parsing dates
   * and times in the SHORT style for the default locale.
   * 
   * @return the {@code DateFormat} instance for the SHORT style and default
   *         locale.
   */
  public final static DateFormat getInstance(){
    return getDateTimeInstance(SHORT,SHORT,Locale.getDefault());
  }

  /**
   * Returns the {@code NumberFormat} used by this {@code DateFormat}.
   * 
   * @return the {@code NumberFormat} used by this date format.
   */
  public NumberFormat getNumberFormat() {
    return numberFormat;
  }

  /**
   * Returns a {@code DateFormat} instance for formatting and parsing time
   * values in the DEFAULT style for the default locale.
   * 
   * @return the {@code DateFormat} instance for the default style and locale.
   */
  public final static DateFormat getTimeInstance(){
    return getTimeInstance(DEFAULT, Locale.getDefault());
  }

  /**
   * Returns a {@code DateFormat} instance for formatting and parsing time
   * values in the specified style for the default locale.
   * 
   * @param style
   *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT.
   * @return the {@code DateFormat} instance for {@code style} and the default
   *         locale.
   * @throws IllegalArgumentException
   *             if {@code style} is not one of SHORT, MEDIUM, LONG, FULL, or
   *             DEFAULT.
   */
  public final static DateFormat getTimeInstance(int style){
    return getTimeInstance(style, Locale.getDefault());
  }

  /**
   * Returns a {@code DateFormat} instance for formatting and parsing time
   * values in the specified style for the specified locale.
   * 
   * @param style
   *            one of SHORT, MEDIUM, LONG, FULL, or DEFAULT.
   * @param locale
   *            the locale.
   * @throws IllegalArgumentException
   *             if {@code style} is not one of SHORT, MEDIUM, LONG, FULL, or
   *             DEFAULT.
   * @return the {@code DateFormat} instance for {@code style} and
   *         {@code locale}.
   */
  public final static DateFormat getTimeInstance(int style, Locale loc){
    checkTimeStyle(style);
    return new SimpleDateFormat(TIMESTYLES[style], loc);
  }

  /**
   * Returns the time zone of this date format's calendar.
   * 
   * @return the time zone of the calendar used by this date format.
   */
  public TimeZone getTimeZone(){
    return calendar.getTimeZone();
  }

  public int hashCode(){
     return calendar.getFirstDayOfWeek()
          + calendar.getMinimalDaysInFirstWeek()
          + calendar.getTimeZone().hashCode()
          + (calendar.isLenient() ? 1231 : 1237)
          + numberFormat.hashCode();
  }


  /**
   * Indicates whether the calendar used by this date format is lenient.
   * 
   * @return {@code true} if the calendar is lenient; {@code false} otherwise.
   */
  public boolean isLenient(){
    return calendar.isLenient();
  }

    /**
   * Parses a date from the specified string using the rules of this date
   * format.
   * 
   * @param string
   *            the string to parse.
   * @return the {@code Date} resulting from the parsing.
   * @throws ParseException
   *         if an error occurs during parsing.
   */
  public Date parse(String string) throws ParseException {
    ParsePosition position = new ParsePosition(0);
    Date date = parse(string, position);
    if (date == null){
      throw new ParseException("parsing failed!", position.getErrorIndex());
    }
    return date;
  }

  /**
   * Parses a date from the specified string starting at the index specified
   * by {@code position}. If the string is successfully parsed then the index
   * of the {@code ParsePosition} is updated to the index following the parsed
   * text. On error, the index is unchanged and the error index of {@code
   * ParsePosition} is set to the index where the error occurred.
   * <p>
   * By default, parsing is lenient: If the input is not in the form used by
   * this object's format method but can still be parsed as a date, then the
   * parse succeeds. Clients may insist on strict adherence to the format by
   * calling {@code setLenient(false)}.
   *
   * @param string
   *            the string to parse.
   * @param position
   *            input/output parameter, specifies the start index in {@code
   *            string} from where to start parsing. If parsing is successful,
   *            it is updated with the index following the parsed text; on
   *            error, the index is unchanged and the error index is set to
   *            the index where the error occurred.
   * @return the date resulting from the parse, or {@code null} if there is an
   *         error.
   */
  public abstract Date parse (String date, ParsePosition pp);

  /**
   * Parses a date from the specified string starting at the index specified
   * by {@code position}. If the string is successfully parsed then the index
   * of the {@code ParsePosition} is updated to the index following the parsed
   * text. On error, the index is unchanged and the error index of
   * {@code ParsePosition} is set to the index where the error occurred.
   * <p>
   * By default, parsing is lenient: If the input is not in the form used by
   * this object's format method but can still be parsed as a date, then the
   * parse succeeds. Clients may insist on strict adherence to the format by
   * calling {@code setLenient(false)}.
   *
   * @param string
   *            the string to parse.
   * @param position
   *            input/output parameter, specifies the start index in
   *            {@code string} from where to start parsing. If parsing is
   *            successful, it is updated with the index following the parsed
   *            text; on error, the index is unchanged and the error index
   *            is set to the index where the error occurred.
   * @return the date resulting from the parsing, or {@code null} if there is
   *         an error.
   */
  public Object parseObject(String string, ParsePosition position) {
      return parse(string, position);
  }

  /**
   * Sets the calendar used by this date format.
   * 
   * @param cal
   *            the new calendar.
   */
  public void setCalendar(Calendar cal) {
    calendar = cal;
  }

  /**
   * Specifies whether or not date/time parsing shall be lenient. With lenient
   * parsing, the parser may use heuristics to interpret inputs that do not
   * precisely match this object's format. With strict parsing, inputs must
   * match this object's format.
   * 
   * @param value
   *            {@code true} to set the calendar to be lenient, {@code false}
   *            otherwise.
   */
  public void setLenient(boolean value) {
      calendar.setLenient(value);
  }

  /**
   * Sets the {@code NumberFormat} used by this date format.
   * 
   * @param format
   *            the new number format.
   */
  public void setNumberFormat(NumberFormat format) {
    numberFormat = format;
  }

  /**
   * Sets the time zone of the calendar used by this date format.
   * 
   * @param timezone
   *            the new time zone.
   */
  public void setTimeZone(TimeZone timezone) {
    calendar.setTimeZone(timezone);
  }

  /**
   * The instances of this inner class are used as attribute keys and values
   * in {@code AttributedCharacterIterator} that the
   * {@link SimpleDateFormat#formatToCharacterIterator(Object)} method returns.
   * <p>
   * There is no public constructor in this class, the only instances are the
   * constants defined here.
   */
  public static class Field extends Format.Field {

    private static final long serialVersionUID = 7441350119349544720L;

    private static Hashtable table = new Hashtable();

    /**
     * Marks the era part of a date.
     */
    public final static Field ERA = new Field("era", Calendar.ERA);

    /**
     * Marks the year part of a date.
     */
    public final static Field YEAR = new Field("year", Calendar.YEAR);

    /**
     * Marks the month part of a date.
     */
    public final static Field MONTH = new Field("month", Calendar.MONTH);

    /**
     * Marks the hour of the day part of a date (0-11).
     */
    public final static Field HOUR_OF_DAY0 = new Field("hour of day", Calendar.HOUR_OF_DAY);

    /**
     * Marks the hour of the day part of a date (1-12).
     */
    public final static Field HOUR_OF_DAY1 = new Field("hour of day 1", -1);

    /**
     * Marks the minute part of a time.
     */
        public final static Field MINUTE = new Field("minute", Calendar.MINUTE);

    /**
     * Marks the second part of a time.
     */
    public final static Field SECOND = new Field("second", Calendar.SECOND);

    /**
     * Marks the millisecond part of a time.
     */
    public final static Field MILLISECOND = new Field("millisecond", Calendar.MILLISECOND);

    /**
     * Marks the day of the week part of a date.
     */
    public final static Field DAY_OF_WEEK = new Field("day of week", Calendar.DAY_OF_WEEK);

    /**
     * Marks the day of the month part of a date.
     */
    public final static Field DAY_OF_MONTH = new Field("day of month", Calendar.DAY_OF_MONTH);

    /**
     * Marks the day of the year part of a date.
     */
    public final static Field DAY_OF_YEAR = new Field("day of year", Calendar.DAY_OF_YEAR);

    /**
     * Marks the day of the week in the month part of a date.
     */
    public final static Field DAY_OF_WEEK_IN_MONTH = new Field("day of week in month", Calendar.DAY_OF_WEEK_IN_MONTH); 
    /**
     * Marks the week of the year part of a date.
     */
    public final static Field WEEK_OF_YEAR = new Field("week of year", Calendar.WEEK_OF_YEAR);

    /**
     * Marks the week of the month part of a date.
     */
    public final static Field WEEK_OF_MONTH = new Field("week of month", Calendar.WEEK_OF_MONTH);

    /**
     * Marks the time indicator part of a date.
     */
    public final static Field AM_PM = new Field("am pm", Calendar.AM_PM);

    /**
      * Marks the hour part of a date (0-11).
      */
    public final static Field HOUR0 = new Field("hour", Calendar.HOUR);

    /**
     * Marks the hour part of a date (1-12).
     */
    public final static Field HOUR1 = new Field("hour 1", -1);

    /**
     * Marks the time zone part of a date.
     */
    public final static Field TIME_ZONE = new Field("time zone", -1);

    /**
     * The calendar field that this field represents.
     */
    private int calendarField = -1;

    /**
     * Constructs a new instance of {@code DateFormat.Field} with the given
     * fieldName and calendar field.
     *
     * @param fieldName
     *            the field name.
     * @param calendarField
     *            the calendar field type of the field.
     */
    protected Field(String fieldName, int calendarField) {
      super(fieldName);
      this.calendarField = calendarField;
      if (calendarField != -1
              && table.get(new Integer(calendarField)) == null) {
          table.put(new Integer(calendarField), this);
      }
    }

    /**
     * Returns the Calendar field that this field represents.
     * 
     * @return the calendar field.
     */
    public int getCalendarField() {
      return calendarField;
    }

    /**
     * Returns the {@code DateFormat.Field} instance for the given calendar
     * field.
     * 
     * @param calendarField
     *            a calendar field constant.
     * @return the {@code DateFormat.Field} corresponding to
     *         {@code calendarField}.
     * @throws IllegalArgumentException
     *             if {@code calendarField} is negative or greater than the
     *             field count of {@code Calendar}.
     */
    public static Field ofCalendarField(int calendarField) {
      if (calendarField < 0 || calendarField >= Calendar.FIELD_COUNT) {
        throw new IllegalArgumentException();
      }

      return (Field)table.get(new Integer(calendarField));
    }

    /**
     * Resolves instances that are deserialized to the constant
     * {@code DateFormat.Field} values.
     *
     * @return the resolved field object.
     * @throws InvalidObjectException
     *             if an error occurs while resolving the field object.
     */
    protected Object readResolve() throws InvalidObjectException {
      if (this.getClass() != Field.class) {
                // text.0C=cannot resolve subclasses
        throw new InvalidObjectException("cannot resolve subclasses");
      }

      if (calendarField != -1) {
        try {
          Field result = ofCalendarField(calendarField);
                               
          if (result != null && this.getName().equals(result.getName())) {
            return result;
          }
        } catch (IllegalArgumentException e) {
          throw new InvalidObjectException("unknown attribute");
        }
      } else {
        if (this.equals(TIME_ZONE)) {
          return TIME_ZONE;
        }
        if (this.equals(HOUR1)) {
          return HOUR1;
        }
        if (this.equals(HOUR_OF_DAY1)) {
          return HOUR_OF_DAY1;
        }
      }
      throw new InvalidObjectException("unknown attribute");
    }
  }

  private static void checkDateStyle(int style) {
    if (!(style == SHORT || style == MEDIUM || style == LONG || style == FULL || style == DEFAULT)) {
      throw new IllegalArgumentException("illegal date style: " + style);
    }
  }

  private static void checkTimeStyle(int style) {
    if (!(style == SHORT || style == MEDIUM || style == LONG || style == FULL || style == DEFAULT)) {
      throw new IllegalArgumentException("illegal time style: " + style);
    }
  }

}
