/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpclient/trunk/module-client/src/main/java/org/apache/http/auth/MalformedChallengeException.java $
 * $Revision: 505684 $
 * $Date: 2007-02-10 04:40:02 -0800 (Sat, 10 Feb 2007) $
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

package org.apache.http.auth;

import org.apache.http.ProtocolException;

/**
 * Signals that authentication challenge is in some way invalid or 
 * illegal in the given context
 *
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 4.0
 */
public class MalformedChallengeException extends ProtocolException {

    private static final long serialVersionUID = 814586927989932284L;

    /**
     * Creates a new MalformedChallengeException with a <tt>null</tt> detail message. 
     */
    public MalformedChallengeException() {
        super();
    }

    /**
     * Creates a new MalformedChallengeException with the specified message.
     * 
     * @param message the exception detail message
     */
    public MalformedChallengeException(String message) {
        super(message);
    }

    /**
     * Creates a new MalformedChallengeException with the specified detail message and cause.
     * 
     * @param message the exception detail message
     * @param cause the <tt>Throwable</tt> that caused this exception, or <tt>null</tt>
     * if the cause is unavailable, unknown, or not a <tt>Throwable</tt>
     */
    public MalformedChallengeException(String message, Throwable cause) {
        super(message, cause);
    }
}
