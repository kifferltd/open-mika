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

/* Derived by CG 20090806 from Harmony "enhanced" revision 772552 */

package java.text;

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
/**
 * The base class for all formats.
 * <p>
 * This is an abstract base class which specifies the protocol for classes which
 * convert other objects or values, such as numeric values and dates, and their
 * string representations. In some cases these representations may be localized
 * or contain localized characters or strings. For example, a numeric formatter
 * such as {@code DecimalFormat} may convert a numeric value such as 12345 to
 * the string "$12,345". It may also parse the string back into a numeric value.
 * A date and time formatter like {@code SimpleDateFormat} may represent a
 * specific date, encoded numerically, as a string such as "Wednesday, February
 * 26, 1997 AD".
 * <p>
 * Many of the concrete subclasses of {@code Format} employ the notion of a
 * pattern. A pattern is a string representation of the rules which govern the
 * conversion between values and strings. For example, a {@code DecimalFormat}
 * object may be associated with the pattern "$#,##0.00;($#,##0.00)", which is a
 * common US English format for currency values, yielding strings such as
 * "$1,234.45" for 1234.45, and "($987.65)" for -987.6543. The specific syntax
 * of a pattern is defined by each subclass. Even though many subclasses use
 * patterns, the notion of a pattern is not inherent to {@code Format} classes
 * in general, and is not part of the explicit base class protocol.
 * <p>
 * Two complex formatting classes are worth mentioning: {@code MessageFormat}
 * and {@code ChoiceFormat}. {@code ChoiceFormat} is a subclass of
 * {@code NumberFormat} which allows the user to format different number ranges
 * as strings. For instance, 0 may be represented as "no files", 1 as "one
 * file", and any number greater than 1 as "many files". {@code MessageFormat}
 * is a formatter which utilizes other {@code Format} objects to format a string
 * containing multiple values. For instance, a {@code MessageFormat} object
 * might produce the string "There are no files on the disk MyDisk on February
 * 27, 1997." given the arguments 0, "MyDisk", and the date value of 2/27/97.
 * See the {@link ChoiceFormat} and {@link MessageFormat} descriptions for
 * further information.
 */

public abstract class Format implements Serializable, Cloneable {

  private static final long serialVersionUID = -299282585814624189L;

  /**
   * Constructs a new {@code Format} instance.
   */
    public Format() {
    }

  /**
   * Returns a copy of this {@code Format} instance.
   * 
   * @return a shallow copy of this format.
   * 
   * @see java.lang.Cloneable
   */
  public Object clone() {
    try {
      return super.clone();
    }
    catch(CloneNotSupportedException cnse){}
    return null;
  }
  
  String convertPattern(String template, String fromChars, String toChars, boolean check) {
    if (!check && fromChars.equals(toChars)) {
      return template;
    }
    boolean quote = false;
    StringBuffer output = new StringBuffer();
    int length = template.length();
    for (int i = 0; i < length; i++) {
      int index;
      char next = template.charAt(i);
      if (next == '\'') {
        quote = !quote;
      }
      if (!quote && (index = fromChars.indexOf(next)) != -1) {
        output.append(toChars.charAt(index));
      } else if (check
              && !quote
              && ((next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z'))) {
        throw new IllegalArgumentException("Invalid pattern char '" + String.valueOf(next) + "' in '" +  template + "'");
      } else {
        output.append(next);
      }
    }
    if (quote) {
      // text.04=Unterminated quote
      throw new IllegalArgumentException("Unterminated quote");
    }
    return output.toString();
  }

  /**
   * Formats the specified object using the rules of this format.
   * 
   * @param object
   *            the object to format.
   * @return the formatted string.
   * @throws IllegalArgumentException
   *         if the object cannot be formatted by this format.
   */
  public final String format(Object obj) throws IllegalArgumentException {
    return format(obj, new StringBuffer(), new FieldPosition(0)).toString();
  }
  
  /**
   * Appends the specified object to the specified string buffer using the
   * rules of this format.
   * <p>
   * {@code field} is an input/output parameter. If its {@code field}
   * member contains an enum value specifying a field on input, then its
   * {@code beginIndex} and {@code endIndex} members will be updated with the
   * text offset of the first occurrence of this field in the formatted text.
   *
   * @param object
   *            the object to format.
   * @param buffer
   *            the string buffer where the formatted string is appended to.
   * @param field
   *            on input: an optional alignment field; on output: the offsets
   *            of the alignment field in the formatted text.
   * @return the string buffer.
   * @throws IllegalArgumentException
   *            if the object cannot be formatted by this format.
   */

  public abstract StringBuffer format(Object obj, StringBuffer appendBuf, FieldPosition pos) throws IllegalArgumentException;
  
  /**
   * Formats the specified object using the rules of this format and returns
   * an {@code AttributedCharacterIterator} with the formatted string and no
   * attributes.
   * <p>
   * Subclasses should return an {@code AttributedCharacterIterator} with the
   * appropriate attributes.
   *
   * @param object
   *            the object to format.
   * @return an {@code AttributedCharacterIterator} with the formatted object
   *         and attributes.
   * @throws IllegalArgumentException
   *            if the object cannot be formatted by this format.
   */
    public AttributedCharacterIterator formatToCharacterIterator(Object object) {
        return new AttributedString(format(object)).getIterator();
    }

  /**
   * Parses the specified string using the rules of this format.
   * 
   * @param string
   *            the string to parse.
   * @return the object resulting from the parse.
   * @throws ParseException
   *            if an error occurs during parsing.
   */
  public Object parseObject(String source) throws ParseException {
    ParsePosition pos = new ParsePosition(0);
    Object o = parseObject(source, pos);
    // [CG 20090806] Here Apache Harmony has:
    // if (position.getErrorIndex() != -1 || position.getIndex() == 0) {
    // but I have retained our code as it seems more efficient.
    if (o == null) {
      throw new ParseException(pos.toString(), pos.getErrorIndex());
    }
    return o;
  }
  
  /**
   * Parses the specified string starting at the index specified by
   * {@code position}. If the string is successfully parsed then the index of
   * the {@code ParsePosition} is updated to the index following the parsed
   * text. On error, the index is unchanged and the error index of
   * {@code ParsePosition} is set to the index where the error occurred.
   * 
   * @param string
   *            the string to parse.
   * @param position
   *            input/output parameter, specifies the start index in
   *            {@code string} from where to start parsing. If parsing is
   *            successful, it is updated with the index following the parsed
   *            text; on error, the index is unchanged and the error index is
   *            set to the index where the error occurred.
   * @return the object resulting from the parse or {@code null} if there is
   *         an error.
   */
  public abstract Object parseObject(String source, ParsePosition pos);
  
}
