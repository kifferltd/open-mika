/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpcore/trunk/module-main/src/main/java/org/apache/http/protocol/HttpRequestInterceptorList.java $
 * $Revision: 554903 $
 * $Date: 2007-07-10 03:54:17 -0700 (Tue, 10 Jul 2007) $
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

import java.util.List;

import org.apache.http.HttpRequestInterceptor;

/**
 * Provides access to an ordered list of request interceptors.
 * Lists are expected to be built upfront and used read-only afterwards
 * for {@link HttpProcessor processing}.
 *
 * @author <a href="mailto:rolandw at apache.org">Roland Weber</a>
 *
 * @version $Revision: 554903 $
 * 
 * @since 4.0
 */
public interface HttpRequestInterceptorList {

    /**
     * Appends a request interceptor to this list.
     *
     * @param itcp      the request interceptor to add
     */
    void addRequestInterceptor(HttpRequestInterceptor itcp)
        ;


    /**
     * Inserts a request interceptor at the specified index.
     *
     * @param itcp      the request interceptor to add
     * @param index     the index to insert the interceptor at
     */
    void addRequestInterceptor(HttpRequestInterceptor itcp, int index);
    
    
    /**
     * Obtains the current size of this list.
     *
     * @return  the number of request interceptors in this list
     */
    int getRequestInterceptorCount()
        ;


    /**
     * Obtains a request interceptor from this list.
     *
     * @param index     the index of the interceptor to obtain,
     *                  0 for first
     *
     * @return  the interceptor at the given index, or
     *          <code>null</code> if the index is out of range
     */
    HttpRequestInterceptor getRequestInterceptor(int index)
        ;


    /**
     * Removes all request interceptors from this list.
     */
    void clearRequestInterceptors()
        ;


    /**
     * Removes all request interceptor of the specified class
     *
     * @param clazz  the class of the instances to be removed.
     */
    void removeRequestInterceptorByClass(Class clazz);
    
    
    /**
     * Sets the request interceptors in this list.
     * This list will be cleared and re-initialized to contain
     * all request interceptors from the argument list.
     * If the argument list includes elements that are not request
     * interceptors, the behavior is implementation dependent.
     *
     * @param itcps     the list of request interceptors
     */
    void setInterceptors(List itcps)
        ;


} // interface HttpRequestInterceptorList

