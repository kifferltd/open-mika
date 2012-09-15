/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpclient/trunk/module-client/src/main/java/org/apache/http/client/NonRepeatableRequestException.java $
 * $Revision: 664326 $
 * $Date: 2008-06-07 04:48:27 -0700 (Sat, 07 Jun 2008) $
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

package org.apache.http.client;

import org.apache.http.ProtocolException;

/**
 * Signals failure to retry the request due to non-repeatable request 
 * entity.
 * 
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 4.0
 */
public class NonRepeatableRequestException extends ProtocolException {

    private static final long serialVersionUID = 82685265288806048L;

    /**
     * Creates a new NonRepeatableEntityException with a <tt>null</tt> detail message. 
     */
    public NonRepeatableRequestException() {
        super();
    }

    /**
     * Creates a new NonRepeatableEntityException with the specified detail message.
     * 
     * @param message The exception detail message
     */
    public NonRepeatableRequestException(String message) {
        super(message);
    }

}
