/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpclient/trunk/module-client/src/main/java/org/apache/http/cookie/CookieSpecRegistry.java $
 * $Revision: 652950 $
 * $Date: 2008-05-02 16:49:48 -0700 (Fri, 02 May 2008) $
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

package org.apache.http.cookie;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.params.HttpParams;

/**
 * Cookie specification registry that can be used to obtain the corresponding
 * cookie specification implementation for a given type of type or version of 
 * cookie. 
 * 
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 *
 * @since 4.0
 */
public final class CookieSpecRegistry {

    private final Map<String,CookieSpecFactory> registeredSpecs;
    
    public CookieSpecRegistry() {
        super();
        this.registeredSpecs = new LinkedHashMap<String,CookieSpecFactory>();
    }
    
    /**
     * Registers a {@link CookieSpecFactory} with the given identifier. 
     * If a specification with the given name already exists it will be overridden.  
     * This nameis the same one used to retrieve the {@link CookieSpecFactory} 
     * from {@link #getCookieSpec(String)}.
     * 
     * @param name the identifier for this specification
     * @param factory the {@link CookieSpecFactory} class to register
     * 
     * @see #getCookieSpec(String)
     */
    public synchronized void register(final String name, final CookieSpecFactory factory) {
         if (name == null) {
             throw new IllegalArgumentException("Name may not be null");
         }
        if (factory == null) {
            throw new IllegalArgumentException("Cookie spec factory may not be null");
        }
        registeredSpecs.put(name.toLowerCase(Locale.ENGLISH), factory);
    }

    /**
     * Unregisters the {@link CookieSpecFactory} with the given ID.
     * 
     * @param id the identifier of the {@link CookieSpec cookie specification} to unregister
     */
    public synchronized void unregister(final String id) {
         if (id == null) {
             throw new IllegalArgumentException("Id may not be null");
         }
         registeredSpecs.remove(id.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Gets the {@link CookieSpec cookie specification} with the given ID.
     * 
     * @param name the {@link CookieSpec cookie specification} identifier
     * @param params the {@link HttpParams HTTP parameters} for the cookie
     *  specification. 
     * 
     * @return {@link CookieSpec cookie specification}
     * 
     * @throws IllegalStateException if a policy with the given name cannot be found
     */
    public synchronized CookieSpec getCookieSpec(final String name, final HttpParams params) 
        throws IllegalStateException {

        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        CookieSpecFactory factory = registeredSpecs.get(name.toLowerCase(Locale.ENGLISH));
        if (factory != null) {
            return factory.newInstance(params);
        } else {
            throw new IllegalStateException("Unsupported cookie spec: " + name);
        }
    } 

    /**
     * Gets the {@link CookieSpec cookie specification} with the given name.
     * 
     * @param name the {@link CookieSpec cookie specification} identifier
     * 
     * @return {@link CookieSpec cookie specification}
     * 
     * @throws IllegalStateException if a policy with the given name cannot be found
     */
    public synchronized CookieSpec getCookieSpec(final String name) 
        throws IllegalStateException {
        return getCookieSpec(name, null);
    } 

    /**
     * Obtains a list containing names of all registered {@link CookieSpec cookie 
     * specs} in their default order.
     * 
     * Note that the DEFAULT policy (if present) is likely to be the same
     * as one of the other policies, but does not have to be.
     * 
     * @return list of registered cookie spec names
     */
    public synchronized List<String> getSpecNames(){
        return new ArrayList<String>(registeredSpecs.keySet()); 
    }
    
    /**
     * Populates the internal collection of registered {@link CookieSpec cookie 
     * specs} with the content of the map passed as a parameter.
     * 
     * @param map cookie specs
     */
    public synchronized void setItems(final Map<String, CookieSpecFactory> map) {
        if (map == null) {
            return;
        }
        registeredSpecs.clear();
        registeredSpecs.putAll(map);
    }

}
