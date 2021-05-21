/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpcore/trunk/module-main/src/main/java/org/apache/http/HttpResponseFactory.java $
 * $Revision: 573864 $
 * $Date: 2007-09-08 08:53:25 -0700 (Sat, 08 Sep 2007) $
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

package org.apache.http;

import org.apache.http.protocol.HttpContext;


/**
 * A factory for {@link HttpResponse HttpResponse} objects.
 *
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 *
 * @version $Revision: 573864 $
 * 
 * @since 4.0
 */
public interface HttpResponseFactory {

    /**
     * Creates a new response from status line elements.
     *
     * @param ver       the protocol version
     * @param status    the status code
     * @param context   the context from which to determine the locale
     *                  for looking up a reason phrase to the status code, or
     *                  <code>null</code> to use the default locale
     *
     * @return  the new response with an initialized status line
     */    
    HttpResponse newHttpResponse(ProtocolVersion ver, int status,
                                 HttpContext context);
    
    /**
     * Creates a new response from a status line.
     *
     * @param statusline the status line
     * @param context    the context from which to determine the locale
     *                   for looking up a reason phrase if the status code
     *                   is updated, or
     *                   <code>null</code> to use the default locale
     *
     * @return  the new response with the argument status line
     */    
    HttpResponse newHttpResponse(StatusLine statusline,
                                 HttpContext context);
    
}
