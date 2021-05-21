/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpclient/trunk/module-client/src/main/java/org/apache/http/conn/params/ConnConnectionPNames.java $
 * $Revision: 576068 $
 * $Date: 2007-09-16 03:25:01 -0700 (Sun, 16 Sep 2007) $
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.conn.params;


/**
 * Parameter names for connections in HttpConn.
 *
 * @version $Revision: 576068 $
 * 
 * @since 4.0
 */
public interface ConnConnectionPNames {


    /**
     * Defines the maximum number of ignorable lines before we expect
     * a HTTP response's status line.
     * <p>
     * With HTTP/1.1 persistent connections, the problem arises that
     * broken scripts could return a wrong Content-Length
     * (there are more bytes sent than specified).
     * Unfortunately, in some cases, this cannot be detected after the
     * bad response, but only before the next one.
     * So HttpClient must be able to skip those surplus lines this way.
     * </p>
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * 0 disallows all garbage/empty lines before the status line.
     * Use {@link java.lang.Integer#MAX_VALUE} for unlimited
     * (default in lenient mode).
     * </p>
     */
    public static final String MAX_STATUS_LINE_GARBAGE = "http.connection.max-status-line-garbage";


}
