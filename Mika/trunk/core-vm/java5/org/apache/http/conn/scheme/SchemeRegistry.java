/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpclient/trunk/module-client/src/main/java/org/apache/http/conn/scheme/SchemeRegistry.java $
 * $Revision: 648356 $
 * $Date: 2008-04-15 10:57:53 -0700 (Tue, 15 Apr 2008) $
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
package org.apache.http.conn.scheme;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;

/**
 * A set of supported protocol {@link Scheme schemes}.
 * Schemes are identified by lowercase names.
 *
 * @author <a href="mailto:rolandw at apache.org">Roland Weber</a>
 *
 *
 * <!-- empty lines to avoid svn diff problems -->
 * @version   $Revision: 648356 $ $Date: 2008-04-15 10:57:53 -0700 (Tue, 15 Apr 2008) $
 *
 * @since 4.0
 */
public final class SchemeRegistry {

    /** The available schemes in this registry. */
    private final Map<String,Scheme> registeredSchemes;


    /**
     * Creates a new, empty scheme registry.
     */
    public SchemeRegistry() {
        super();
        registeredSchemes = new LinkedHashMap<String,Scheme>();
    }


    /**
     * Obtains a scheme by name.
     *
     * @param name      the name of the scheme to look up (in lowercase)
     *
     * @return  the scheme, never <code>null</code>
     *
     * @throws IllegalStateException
     *          if the scheme with the given name is not registered
     */
    public synchronized final Scheme getScheme(String name) {
        Scheme found = get(name);
        if (found == null) {
            throw new IllegalStateException
                ("Scheme '"+name+"' not registered.");
        }
        return found;
    }


    /**
     * Obtains the scheme for a host.
     * Convenience method for <code>getScheme(host.getSchemeName())</pre>
     *
     * @param host      the host for which to obtain the scheme
     *
     * @return  the scheme for the given host, never <code>null</code>
     *
     * @throws IllegalStateException
     *          if a scheme with the respective name is not registered
     */
    public synchronized final Scheme getScheme(HttpHost host) {
        if (host == null) {
            throw new IllegalArgumentException("Host must not be null.");
        }
        return getScheme(host.getSchemeName());
    }


    /**
     * Obtains a scheme by name, if registered.
     *
     * @param name      the name of the scheme to look up (in lowercase)
     *
     * @return  the scheme, or
     *          <code>null</code> if there is none by this name
     */
    public synchronized final Scheme get(String name) {
        if (name == null)
            throw new IllegalArgumentException("Name must not be null.");

        // leave it to the caller to use the correct name - all lowercase
        //name = name.toLowerCase();
        Scheme found = registeredSchemes.get(name);
        return found;
    }


    /**
     * Registers a scheme.
     * The scheme can later be retrieved by its name
     * using {@link #getScheme(String) getScheme} or {@link #get get}.
     *
     * @param sch       the scheme to register
     *
     * @return  the scheme previously registered with that name, or
     *          <code>null</code> if none was registered
     */
    public synchronized final Scheme register(Scheme sch) {
        if (sch == null)
            throw new IllegalArgumentException("Scheme must not be null.");

        Scheme old = registeredSchemes.put(sch.getName(), sch);
        return old;
    }


    /**
     * Unregisters a scheme.
     *
     * @param name      the name of the scheme to unregister (in lowercase)
     *
     * @return  the unregistered scheme, or
     *          <code>null</code> if there was none
     */
    public synchronized final Scheme unregister(String name) {
        if (name == null)
            throw new IllegalArgumentException("Name must not be null.");

        // leave it to the caller to use the correct name - all lowercase
        //name = name.toLowerCase();
        Scheme gone = registeredSchemes.remove(name);
        return gone;
    }


    /**
     * Obtains the names of the registered schemes in their default order.
     *
     * @return  List containing registered scheme names.
     */
    public synchronized final List<String> getSchemeNames() {
        return new ArrayList<String>(registeredSchemes.keySet());
    }

    /**
     * Populates the internal collection of registered {@link Scheme protocol schemes} 
     * with the content of the map passed as a parameter.
     * 
     * @param map protocol schemes
     */
    public synchronized void setItems(final Map<String, Scheme> map) {
        if (map == null) {
            return;
        }
        registeredSchemes.clear();
        registeredSchemes.putAll(map);
    }

} // class SchemeRegistry

