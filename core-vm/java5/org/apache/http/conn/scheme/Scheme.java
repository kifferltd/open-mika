/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpclient/trunk/module-client/src/main/java/org/apache/http/conn/scheme/Scheme.java $
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
package org.apache.http.conn.scheme;

import java.util.Locale;

import org.apache.http.util.LangUtils;

/**
 * Encapsulates specifics of a protocol scheme such as "http" or "https".
 * Schemes are identified by lowercase names.
 * Supported schemes are typically collected in a
 * {@link SchemeRegistry SchemeRegistry}.
 *
 * <p>
 * For example, to configure support for "https://" URLs,
 * you could write code like the following:
 * </p>
 * <pre>
 * Scheme https = new Scheme("https", new MySecureSocketFactory(), 443);
 * SchemeRegistry.DEFAULT.register(https);
 * </pre>
 *
 * @author <a href="mailto:rolandw at apache.org">Roland Weber</a>
 * @author Michael Becke 
 * @author Jeff Dever
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 */
public final class Scheme {

    /** The name of this scheme, in lowercase. (e.g. http, https) */
    private final String name;
    
    /** The socket factory for this scheme */
    private final SocketFactory socketFactory;
    
    /** The default port for this scheme */
    private final int defaultPort;
    
    /** Indicates whether this scheme allows for layered connections */
    private final boolean layered;


    /** A string representation, for {@link #toString toString}. */
    private String stringRep;


    /**
     * Creates a new scheme.
     * Whether the created scheme allows for layered connections
     * depends on the class of <code>factory</code>.
     *
     * @param name      the scheme name, for example "http".
     *                  The name will be converted to lowercase.
     * @param factory   the factory for creating sockets for communication
     *                  with this scheme
     * @param port      the default port for this scheme
     */
    public Scheme(final String name,
                  final SocketFactory factory,
                  final int port) {

        if (name == null) {
            throw new IllegalArgumentException
                ("Scheme name may not be null");
        }
        if (factory == null) {
            throw new IllegalArgumentException
                ("Socket factory may not be null");
        }
        if ((port <= 0) || (port > 0xffff)) {
            throw new IllegalArgumentException
                ("Port is invalid: " + port);
        }

        this.name = name.toLowerCase(Locale.ENGLISH);
        this.socketFactory = factory;
        this.defaultPort = port;
        this.layered = (factory instanceof LayeredSocketFactory);
    }


    /**
     * Obtains the default port.
     *
     * @return  the default port for this scheme
     */
    public final int getDefaultPort() {
        return defaultPort;
    }


    /**
     * Obtains the socket factory.
     * If this scheme is {@link #isLayered layered}, the factory implements
     * {@link LayeredSocketFactory LayeredSocketFactory}.
     *
     * @return  the socket factory for this scheme
     */
    public final SocketFactory getSocketFactory() {
        return socketFactory;
    }


    /**
     * Obtains the scheme name.
     *
     * @return  the name of this scheme, in lowercase
     */
    public final String getName() {
        return name;
    }


    /**
     * Indicates whether this scheme allows for layered connections.
     *
     * @return <code>true</code> if layered connections are possible,
     *         <code>false</code> otherwise
     */
    public final boolean isLayered() {
        return layered;
    }


    /**
     * Resolves the correct port for this scheme.
     * Returns the given port if it is valid, the default port otherwise.
     * 
     * @param port      the port to be resolved,
     *                  a negative number to obtain the default port
     * 
     * @return the given port or the defaultPort
     */
    public final int resolvePort(int port) {
        return ((port <= 0) || (port > 0xffff)) ? defaultPort : port;
    }


    /**
     * Return a string representation of this object.
     *
     * @return  a human-readable string description of this scheme
     */
    @Override
    public final String toString() {
        if (stringRep == null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(this.name);
            buffer.append(':');
            buffer.append(Integer.toString(this.defaultPort));
            stringRep = buffer.toString();
        }
        return stringRep;
    }


    /**
     * Compares this scheme to an object.
     *
     * @param obj       the object to compare with
     *
     * @return  <code>true</code> iff the argument is equal to this scheme
     */
    @Override
    public final boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof Scheme)) return false;

        Scheme s = (Scheme) obj;
        return (name.equals(s.name) &&
                defaultPort == s.defaultPort &&
                layered == s.layered &&
                socketFactory.equals(s.socketFactory)
                );
    } // equals


    /**
     * Obtains a hash code for this scheme.
     *
     * @return  the hash code
     */
    @Override
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.defaultPort);
        hash = LangUtils.hashCode(hash, this.name);
        hash = LangUtils.hashCode(hash, this.layered);
        hash = LangUtils.hashCode(hash, this.socketFactory);
        return hash;
    }

} // class Scheme
