/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpcore/trunk/module-main/src/main/java/org/apache/http/protocol/SyncBasicHttpContext.java $
 * $Revision: 613298 $
 * $Date: 2008-01-18 14:09:22 -0800 (Fri, 18 Jan 2008) $
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

package org.apache.http.protocol;

/**
 * Thread-safe extension of the {@link BasicHttpContext}.
 *
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 *
 * @version $Revision: 613298 $
 * 
 * @since 4.0
 */
public class SyncBasicHttpContext extends BasicHttpContext {
    
    public SyncBasicHttpContext(final HttpContext parentContext) {
        super(parentContext);
    }
    
    public synchronized Object getAttribute(final String id) {
        return super.getAttribute(id);
    }

    public synchronized void setAttribute(final String id, final Object obj) {
        super.setAttribute(id, obj);
    }
    
    public synchronized Object removeAttribute(final String id) {
        return super.removeAttribute(id);
    }

}
