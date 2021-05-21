/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpcore/trunk/module-main/src/main/java/org/apache/http/message/BasicHeaderValueFormatter.java $
 * $Revision: 574185 $
 * $Date: 2007-09-10 02:19:47 -0700 (Mon, 10 Sep 2007) $
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

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.util.CharArrayBuffer;


/**
 * Basic implementation for formatting header value elements.
 * Instances of this class are stateless and thread-safe.
 * Derived classes are expected to maintain these properties.
 * 
 * @author <a href="mailto:oleg at ural.com">Oleg Kalnichevski</a>
 * @author and others
 *
 *
 * <!-- empty lines above to avoid 'svn diff' context problems -->
 * @version $Revision: 574185 $
 *
 * @since 4.0
 */
public class BasicHeaderValueFormatter implements HeaderValueFormatter {

    /**
     * A default instance of this class, for use as default or fallback.
     * Note that {@link BasicHeaderValueFormatter} is not a singleton, there
     * can be many instances of the class itself and of derived classes.
     * The instance here provides non-customized, default behavior.
     */
    public final static
        BasicHeaderValueFormatter DEFAULT = new BasicHeaderValueFormatter();


    /**
     * Special characters that can be used as separators in HTTP parameters.
     * These special characters MUST be in a quoted string to be used within
     * a parameter value .
     */
    public final static String SEPARATORS = " ;,:@()<>\\\"/[]?={}\t";


    /**
     * Unsafe special characters that must be escaped using the backslash
     * character
     */
    public final static String UNSAFE_CHARS = "\"\\";



    // public default constructor



    /**
     * Formats an array of header elements.
     *
     * @param elems     the header elements to format
     * @param quote     <code>true</code> to always format with quoted values,
     *                  <code>false</code> to use quotes only when necessary
     * @param formatter         the formatter to use, or <code>null</code>
     *                          for the {@link #DEFAULT default}
     *
     * @return  the formatted header elements
     */
    public final static
        String formatElements(final HeaderElement[] elems,
                              final boolean quote,
                              HeaderValueFormatter formatter) {
        if (formatter == null)
            formatter = BasicHeaderValueFormatter.DEFAULT;
        return formatter.formatElements(null, elems, quote).toString();
    }


    // non-javadoc, see interface HeaderValueFormatter
    public CharArrayBuffer formatElements(CharArrayBuffer buffer,
                                          final HeaderElement[] elems,
                                          final boolean quote) {
        if (elems == null) {
            throw new IllegalArgumentException
                ("Header element array must not be null.");
        }

        int len = estimateElementsLen(elems);
        if (buffer == null) {
            buffer = new CharArrayBuffer(len);
        } else {
            buffer.ensureCapacity(len);
        }

        for (int i=0; i<elems.length; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            formatHeaderElement(buffer, elems[i], quote);
        }

        return buffer;
    }


    /**
     * Estimates the length of formatted header elements.
     *
     * @param elems     the header elements to format, or <code>null</code>
     *
     * @return  a length estimate, in number of characters
     */
    protected int estimateElementsLen(final HeaderElement[] elems) {
        if ((elems == null) || (elems.length < 1))
            return 0;

        int result = (elems.length-1) * 2; // elements separated by ", "
        for (int i=0; i<elems.length; i++) {
            result += estimateHeaderElementLen(elems[i]);
        }

        return result;
    }



    /**
     * Formats a header element.
     *
     * @param elem      the header element to format
     * @param quote     <code>true</code> to always format with quoted values,
     *                  <code>false</code> to use quotes only when necessary
     * @param formatter         the formatter to use, or <code>null</code>
     *                          for the {@link #DEFAULT default}
     *
     * @return  the formatted header element
     */
    public final static
        String formatHeaderElement(final HeaderElement elem,
                                   boolean quote,
                                   HeaderValueFormatter formatter) {
        if (formatter == null)
            formatter = BasicHeaderValueFormatter.DEFAULT;
        return formatter.formatHeaderElement(null, elem, quote).toString();
    }


    // non-javadoc, see interface HeaderValueFormatter
    public CharArrayBuffer formatHeaderElement(CharArrayBuffer buffer,
                                               final HeaderElement elem,
                                               final boolean quote) {
        if (elem == null) {
            throw new IllegalArgumentException
                ("Header element must not be null.");
        }

        int len = estimateHeaderElementLen(elem);
        if (buffer == null) {
            buffer = new CharArrayBuffer(len);
        } else {
            buffer.ensureCapacity(len);
        }

        buffer.append(elem.getName());
        final String value = elem.getValue();
        if (value != null) {
            buffer.append('=');
            doFormatValue(buffer, value, quote);
        }

        final int parcnt = elem.getParameterCount();
        if (parcnt > 0) {
            for (int i=0; i<parcnt; i++) {
                buffer.append("; ");
                formatNameValuePair(buffer, elem.getParameter(i), quote);
            }
        }

        return buffer;
    }


    /**
     * Estimates the length of a formatted header element.
     *
     * @param elem      the header element to format, or <code>null</code>
     *
     * @return  a length estimate, in number of characters
     */
    protected int estimateHeaderElementLen(final HeaderElement elem) {
        if (elem == null)
            return 0;

        int result = elem.getName().length(); // name
        final String value = elem.getValue();
        if (value != null) {
            // assume quotes, but no escaped characters
            result += 3 + value.length(); // ="value"
        }

        final int parcnt = elem.getParameterCount();
        if (parcnt > 0) {
            for (int i=0; i<parcnt; i++) {
                result += 2 +                   // ; <param>
                    estimateNameValuePairLen(elem.getParameter(i));
            }
        }

        return result;
    }




    /**
     * Formats a set of parameters.
     *
     * @param nvps      the parameters to format
     * @param quote     <code>true</code> to always format with quoted values,
     *                  <code>false</code> to use quotes only when necessary
     * @param formatter         the formatter to use, or <code>null</code>
     *                          for the {@link #DEFAULT default}
     *
     * @return  the formatted parameters
     */
    public final static
        String formatParameters(final NameValuePair[] nvps,
                                final boolean quote,
                                HeaderValueFormatter formatter) {
        if (formatter == null)
            formatter = BasicHeaderValueFormatter.DEFAULT;
        return formatter.formatParameters(null, nvps, quote).toString();
    }


    // non-javadoc, see interface HeaderValueFormatter
    public CharArrayBuffer formatParameters(CharArrayBuffer buffer,
                                            NameValuePair[] nvps,
                                            boolean quote) {
        if (nvps == null) {
            throw new IllegalArgumentException
                ("Parameters must not be null.");
        }

        int len = estimateParametersLen(nvps);
        if (buffer == null) {
            buffer = new CharArrayBuffer(len);
        } else {
            buffer.ensureCapacity(len);
        }

        for (int i = 0; i < nvps.length; i++) {
            if (i > 0) {
                buffer.append("; ");
            }
            formatNameValuePair(buffer, nvps[i], quote);
        }

        return buffer;
    }


    /**
     * Estimates the length of formatted parameters.
     *
     * @param nvps      the parameters to format, or <code>null</code>
     *
     * @return  a length estimate, in number of characters
     */
    protected int estimateParametersLen(final NameValuePair[] nvps) {
        if ((nvps == null) || (nvps.length < 1))
            return 0;

        int result = (nvps.length-1) * 2; // "; " between the parameters
        for (int i=0; i<nvps.length; i++) {
            result += estimateNameValuePairLen(nvps[i]);
        }

        return result;
    }


    /**
     * Formats a name-value pair.
     *
     * @param nvp       the name-value pair to format
     * @param quote     <code>true</code> to always format with a quoted value,
     *                  <code>false</code> to use quotes only when necessary
     * @param formatter         the formatter to use, or <code>null</code>
     *                          for the {@link #DEFAULT default}
     *
     * @return  the formatted name-value pair
     */
    public final static
        String formatNameValuePair(final NameValuePair nvp,
                                   final boolean quote,
                                   HeaderValueFormatter formatter) {
        if (formatter == null)
            formatter = BasicHeaderValueFormatter.DEFAULT;
        return formatter.formatNameValuePair(null, nvp, quote).toString();
    }


    // non-javadoc, see interface HeaderValueFormatter
    public CharArrayBuffer formatNameValuePair(CharArrayBuffer buffer,
                                               final NameValuePair nvp,
                                               final boolean quote) {
        if (nvp == null) {
            throw new IllegalArgumentException
                ("NameValuePair must not be null.");
        }

        int len = estimateNameValuePairLen(nvp);
        if (buffer == null) {
            buffer = new CharArrayBuffer(len);
        } else {
            buffer.ensureCapacity(len);
        }

        buffer.append(nvp.getName());
        final String value = nvp.getValue();
        if (value != null) {
            buffer.append('=');
            doFormatValue(buffer, value, quote);
        }

        return buffer;
    }


    /**
     * Estimates the length of a formatted name-value pair.
     *
     * @param nvp       the name-value pair to format, or <code>null</code>
     *
     * @return  a length estimate, in number of characters
     */
    protected int estimateNameValuePairLen(final NameValuePair nvp) {
        if (nvp == null)
            return 0;

        int result = nvp.getName().length(); // name
        final String value = nvp.getValue();
        if (value != null) {
            // assume quotes, but no escaped characters
            result += 3 + value.length(); // ="value"
        }
        return result;
    }


    /**
     * Actually formats the value of a name-value pair.
     * This does not include a leading = character.
     * Called from {@link #formatNameValuePair formatNameValuePair}.
     *
     * @param buffer    the buffer to append to, never <code>null</code>
     * @param value     the value to append, never <code>null</code>
     * @param quote     <code>true</code> to always format with quotes,
     *                  <code>false</code> to use quotes only when necessary
     */
    protected void doFormatValue(final CharArrayBuffer buffer,
                                 final String value,
                                 boolean quote) {

        if (!quote) {
            for (int i = 0; (i < value.length()) && !quote; i++) {
                quote = isSeparator(value.charAt(i));
            }
        }

        if (quote) {
            buffer.append('"');
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (isUnsafe(ch)) {
                buffer.append('\\');
            }
            buffer.append(ch);
        }
        if (quote) {
            buffer.append('"');
        }
    }


    /**
     * Checks whether a character is a {@link #SEPARATORS separator}.
     *
     * @param ch        the character to check
     *
     * @return  <code>true</code> if the character is a separator,
     *          <code>false</code> otherwise
     */
    protected boolean isSeparator(char ch) {
        return SEPARATORS.indexOf(ch) >= 0;
    }


    /**
     * Checks whether a character is {@link #UNSAFE_CHARS unsafe}.
     *
     * @param ch        the character to check
     *
     * @return  <code>true</code> if the character is unsafe,
     *          <code>false</code> otherwise
     */
    protected boolean isUnsafe(char ch) {
        return UNSAFE_CHARS.indexOf(ch) >= 0;
    }


} // class BasicHeaderValueFormatter
