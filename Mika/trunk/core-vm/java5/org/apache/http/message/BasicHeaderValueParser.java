/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpcore/trunk/module-main/src/main/java/org/apache/http/message/BasicHeaderValueParser.java $
 * $Revision: 595670 $
 * $Date: 2007-11-16 06:15:01 -0800 (Fri, 16 Nov 2007) $
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.message;


import java.util.List;
import java.util.ArrayList;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;



/**
 * Basic implementation for parsing header values into elements.
 * Instances of this class are stateless and thread-safe.
 * Derived classes are expected to maintain these properties.
 *
 * @author <a href="mailto:bcholmes@interlog.com">B.C. Holmes</a>
 * @author <a href="mailto:jericho@thinkfree.com">Park, Sung-Gu</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg at ural.com">Oleg Kalnichevski</a>
 * @author and others
 *
 *
 * <!-- empty lines above to avoid 'svn diff' context problems -->
 * @version $Revision: 595670 $
 *
 * @since 4.0
 */
public class BasicHeaderValueParser implements HeaderValueParser {

    /**
     * A default instance of this class, for use as default or fallback.
     * Note that {@link BasicHeaderValueParser} is not a singleton, there
     * can be many instances of the class itself and of derived classes.
     * The instance here provides non-customized, default behavior.
     */
    public final static
        BasicHeaderValueParser DEFAULT = new BasicHeaderValueParser();

    private final static char PARAM_DELIMITER                = ';';
    private final static char ELEM_DELIMITER                 = ',';
    private final static char[] ALL_DELIMITERS               = new char[] {
                                                                PARAM_DELIMITER, 
                                                                ELEM_DELIMITER
                                                                };  
    
    // public default constructor


    /**
     * Parses elements with the given parser.
     *
     * @param value     the header value to parse
     * @param parser    the parser to use, or <code>null</code> for default
     *
     * @return  array holding the header elements, never <code>null</code>
     */
    public final static
        HeaderElement[] parseElements(final String value,
                                      HeaderValueParser parser)
        throws ParseException {

        if (value == null) {
            throw new IllegalArgumentException
                ("Value to parse may not be null");
        }

        if (parser == null)
            parser = BasicHeaderValueParser.DEFAULT;

        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        ParserCursor cursor = new ParserCursor(0, value.length());
        return parser.parseElements(buffer, cursor);
    }


    // non-javadoc, see interface HeaderValueParser
    public HeaderElement[] parseElements(final CharArrayBuffer buffer,
                                         final ParserCursor cursor) {

        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        }
        if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }

        List elements = new ArrayList(); 
        while (!cursor.atEnd()) {
            HeaderElement element = parseHeaderElement(buffer, cursor);
            if (!(element.getName().length() == 0 && element.getValue() == null)) {
                elements.add(element);
            }
        }
        return (HeaderElement[])
            elements.toArray(new HeaderElement[elements.size()]);
    }


    /**
     * Parses an element with the given parser.
     *
     * @param value     the header element to parse
     * @param parser    the parser to use, or <code>null</code> for default
     *
     * @return  the parsed header element
     */
    public final static
        HeaderElement parseHeaderElement(final String value,
                                         HeaderValueParser parser)
        throws ParseException {

        if (value == null) {
            throw new IllegalArgumentException
                ("Value to parse may not be null");
        }

        if (parser == null)
            parser = BasicHeaderValueParser.DEFAULT;

        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        ParserCursor cursor = new ParserCursor(0, value.length());
        return parser.parseHeaderElement(buffer, cursor);
    }


    // non-javadoc, see interface HeaderValueParser
    public HeaderElement parseHeaderElement(final CharArrayBuffer buffer,
                                            final ParserCursor cursor) {

        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        }
        if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
        
        NameValuePair nvp = parseNameValuePair(buffer, cursor);
        NameValuePair[] params = null;
        if (!cursor.atEnd()) {
            char ch = buffer.charAt(cursor.getPos() - 1); 
            if (ch != ELEM_DELIMITER) {
                params = parseParameters(buffer, cursor);
            }
        }
        return createHeaderElement(nvp.getName(), nvp.getValue(), params);
    }


    /**
     * Creates a header element.
     * Called from {@link #parseHeaderElement}.
     *
     * @return  a header element representing the argument
     */
    protected HeaderElement createHeaderElement(
            final String name, 
            final String value, 
            final NameValuePair[] params) {
        return new BasicHeaderElement(name, value, params);
    }


    /**
     * Parses parameters with the given parser.
     *
     * @param value     the parameter list to parse
     * @param parser    the parser to use, or <code>null</code> for default
     *
     * @return  array holding the parameters, never <code>null</code>
     */
    public final static
        NameValuePair[] parseParameters(final String value,
                                        HeaderValueParser parser)
        throws ParseException {

        if (value == null) {
            throw new IllegalArgumentException
                ("Value to parse may not be null");
        }

        if (parser == null)
            parser = BasicHeaderValueParser.DEFAULT;

        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        ParserCursor cursor = new ParserCursor(0, value.length());
        return parser.parseParameters(buffer, cursor);
    }



    // non-javadoc, see interface HeaderValueParser
    public NameValuePair[] parseParameters(final CharArrayBuffer buffer,
                                           final ParserCursor cursor) {

        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        }
        if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
        
        int pos = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        
        while (pos < indexTo) {
            char ch = buffer.charAt(pos);
            if (HTTP.isWhitespace(ch)) {
                pos++;
            } else {
                break;
            }
        }
        cursor.updatePos(pos);
        if (cursor.atEnd()) {
            return new NameValuePair[] {};
        }
        
        List params = new ArrayList(); 
        while (!cursor.atEnd()) {
            NameValuePair param = parseNameValuePair(buffer, cursor);
            params.add(param);
            char ch = buffer.charAt(cursor.getPos() - 1);
            if (ch == ELEM_DELIMITER) {
                break;
            }
        }
        
        return (NameValuePair[])
            params.toArray(new NameValuePair[params.size()]);
    }

    /**
     * Parses a name-value-pair with the given parser.
     *
     * @param value     the NVP to parse
     * @param parser    the parser to use, or <code>null</code> for default
     *
     * @return  the parsed name-value pair
     */
    public final static
       NameValuePair parseNameValuePair(final String value,
                                        HeaderValueParser parser)
        throws ParseException {

        if (value == null) {
            throw new IllegalArgumentException
                ("Value to parse may not be null");
        }

        if (parser == null)
            parser = BasicHeaderValueParser.DEFAULT;

        CharArrayBuffer buffer = new CharArrayBuffer(value.length());
        buffer.append(value);
        ParserCursor cursor = new ParserCursor(0, value.length());
        return parser.parseNameValuePair(buffer, cursor);
    }


    // non-javadoc, see interface HeaderValueParser
    public NameValuePair parseNameValuePair(final CharArrayBuffer buffer,
                                            final ParserCursor cursor) {
        return parseNameValuePair(buffer, cursor, ALL_DELIMITERS);
    }
    
    private static boolean isOneOf(final char ch, final char[] chs) {
        if (chs != null) {
            for (int i = 0; i < chs.length; i++) {
                if (ch == chs[i]) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public NameValuePair parseNameValuePair(final CharArrayBuffer buffer,
                                            final ParserCursor cursor,
                                            final char[] delimiters) {

        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        }
        if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }

        boolean terminated = false;
        
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        
        // Find name
        String name = null;
        while (pos < indexTo) {
            char ch = buffer.charAt(pos);
            if (ch == '=') {
                break;
            }
            if (isOneOf(ch, delimiters)) {
                terminated = true;
                break;
            }
            pos++;
        }
        
        if (pos == indexTo) {
            terminated = true;
            name = buffer.substringTrimmed(indexFrom, indexTo);
        } else {
            name = buffer.substringTrimmed(indexFrom, pos);
            pos++;
        }
        
        if (terminated) {
            cursor.updatePos(pos);
            return createNameValuePair(name, null);
        }

        // Find value
        String value = null;
        int i1 = pos;
        
        boolean qouted = false;
        boolean escaped = false;
        while (pos < indexTo) {
            char ch = buffer.charAt(pos);
            if (ch == '"' && !escaped) {
                qouted = !qouted;
            }
            if (!qouted && !escaped && isOneOf(ch, delimiters)) {
                terminated = true;
                break;
            }
            if (escaped) {
                escaped = false;
            } else {
                escaped = qouted && ch == '\\';
            }
            pos++;
        }
        
        int i2 = pos;
        // Trim leading white spaces
        while (i1 < i2 && (HTTP.isWhitespace(buffer.charAt(i1)))) {
            i1++;
        }
        // Trim trailing white spaces
        while ((i2 > i1) && (HTTP.isWhitespace(buffer.charAt(i2 - 1)))) {
            i2--;
        }
        // Strip away quotes if necessary
        if (((i2 - i1) >= 2) 
            && (buffer.charAt(i1) == '"') 
            && (buffer.charAt(i2 - 1) == '"')) {
            i1++;
            i2--;
        }
        value = buffer.substring(i1, i2);
        if (terminated) {
            pos++;
        }
        cursor.updatePos(pos);
        return createNameValuePair(name, value);
    }

    /**
     * Creates a name-value pair.
     * Called from {@link #parseNameValuePair}.
     *
     * @param name      the name
     * @param value     the value, or <code>null</code>
     *
     * @return  a name-value pair representing the arguments
     */
    protected NameValuePair createNameValuePair(final String name, final String value) {
        return new BasicNameValuePair(name, value);
    }

}

