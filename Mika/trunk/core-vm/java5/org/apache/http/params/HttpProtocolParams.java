/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpcore/trunk/module-main/src/main/java/org/apache/http/params/HttpProtocolParams.java $
 * $Revision: 576089 $
 * $Date: 2007-09-16 05:39:56 -0700 (Sun, 16 Sep 2007) $
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

package org.apache.http.params;

import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.protocol.HTTP;

/**
 * This class implements an adaptor around the {@link HttpParams} interface
 * to simplify manipulation of the HTTP protocol specific parameters.
 * <br/>
 * Note that the <i>implements</i> relation to {@link CoreProtocolPNames}
 * is for compatibility with existing application code only. References to
 * the parameter names should use the interface, not this class.
 *
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 * 
 * @version $Revision: 576089 $
 * 
 * @since 4.0
 *
 * @see CoreProtocolPNames
 */
public final class HttpProtocolParams implements CoreProtocolPNames {
    
    /**
     */
    private HttpProtocolParams() {
        super();
    }

    /**
     * Returns the charset to be used for writing HTTP headers.
     * @return The charset
     */
    public static String getHttpElementCharset(final HttpParams params) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        String charset = (String) params.getParameter
            (CoreProtocolPNames.HTTP_ELEMENT_CHARSET);
        if (charset == null) {
            charset = HTTP.DEFAULT_PROTOCOL_CHARSET;
        }
        return charset;
    }
    
    /**
     * Sets the charset to be used for writing HTTP headers.
     * @param charset The charset
     */
    public static void setHttpElementCharset(final HttpParams params, final String charset) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        params.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, charset);
    }

    /**
     * Returns the default charset to be used for writing content body, 
     * when no charset explicitly specified.
     * @return The charset
     */
    public static String getContentCharset(final HttpParams params) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        String charset = (String) params.getParameter
            (CoreProtocolPNames.HTTP_CONTENT_CHARSET);
        if (charset == null) {
            charset = HTTP.DEFAULT_CONTENT_CHARSET;
        }
        return charset;
    }
    
    /**
     * Sets the default charset to be used for writing content body,
     * when no charset explicitly specified.
     * @param charset The charset
     */
    public static void setContentCharset(final HttpParams params, final String charset) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, charset);
    }

    /**
     * Returns {@link ProtocolVersion protocol version} to be used per default. 
     *
     * @return {@link ProtocolVersion protocol version}
     */
    public static ProtocolVersion getVersion(final HttpParams params) { 
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        Object param = params.getParameter
            (CoreProtocolPNames.PROTOCOL_VERSION);
        if (param == null) {
            return HttpVersion.HTTP_1_1;
        }
        return (ProtocolVersion)param;
    }
    
    /**
     * Assigns the {@link ProtocolVersion protocol version} to be used by the 
     * HTTP methods that this collection of parameters applies to. 
     *
     * @param version the {@link ProtocolVersion protocol version}
     */
    public static void setVersion(final HttpParams params, final ProtocolVersion version) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, version);
    }

    public static String getUserAgent(final HttpParams params) { 
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        return (String) params.getParameter(CoreProtocolPNames.USER_AGENT);
    }
    
    public static void setUserAgent(final HttpParams params, final String useragent) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        params.setParameter(CoreProtocolPNames.USER_AGENT, useragent);
    }

    public static boolean useExpectContinue(final HttpParams params) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        return params.getBooleanParameter
            (CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
    }
    
    public static void setUseExpectContinue(final HttpParams params, boolean b) {
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, b);
    }
}
