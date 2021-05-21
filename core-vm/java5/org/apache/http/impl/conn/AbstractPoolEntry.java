/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpclient/trunk/module-client/src/main/java/org/apache/http/impl/conn/AbstractPoolEntry.java $
 * $Revision: 658775 $
 * $Date: 2008-05-21 10:30:45 -0700 (Wed, 21 May 2008) $
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

package org.apache.http.impl.conn;


import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.RouteTracker;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.OperatedClientConnection;



/**
 * A pool entry for use by connection manager implementations.
 * Pool entries work in conjunction with an
 * {@link AbstractClientConnAdapter adapter}.
 * The adapter is handed out to applications that obtain a connection.
 * The pool entry stores the underlying connection and tracks the
 * {@link HttpRoute route} established.
 * The adapter delegates methods for establishing the route to
 * it's pool entry.
 * <br/>
 * If the managed connections is released or revoked, the adapter
 * gets disconnected, but the pool entry still contains the
 * underlying connection and the established route.
 *
 * @author <a href="mailto:rolandw at apache.org">Roland Weber</a>
 * @author <a href="mailto:becke@u.washington.edu">Michael Becke</a>
 *
 *
 * <!-- empty lines to avoid svn diff problems -->
 * @version   $Revision: 658775 $
 *
 * @since 4.0
 */
public abstract class AbstractPoolEntry {

    /** The connection operator. */
    protected final ClientConnectionOperator connOperator;

    /** The underlying connection being pooled or used. */
    protected final OperatedClientConnection connection;

    /** The route for which this entry gets allocated. */
    //@@@ currently accessed from connection manager(s) as attribute
    //@@@ avoid that, derived classes should decide whether update is allowed
    //@@@ SCCM: yes, TSCCM: no
    protected volatile HttpRoute route;
    
    /** Connection state object */
    protected volatile Object state;
    
    /** The tracked route, or <code>null</code> before tracking starts. */
    protected volatile RouteTracker tracker;


    /**
     * Creates a new pool entry.
     *
     * @param connOperator     the Connection Operator for this entry
     * @param route   the planned route for the connection,
     *                or <code>null</code>
     */
    protected AbstractPoolEntry(ClientConnectionOperator connOperator,
                                HttpRoute route) {
        super();
        if (connOperator == null) {
            throw new IllegalArgumentException("Connection operator may not be null");
        }
        this.connOperator = connOperator;
        this.connection = connOperator.createConnection();
        this.route = route;
        this.tracker = null;
    }

    /**
     * Returns the state object associated with this pool entry.
     * 
     * @return The state object
     */
    public Object getState() {
        return state;
    }
    
    /**
     * Assigns a state object to this pool entry.
     * 
     * @param state The state object
     */
    public void setState(final Object state) {
        this.state = state;
    }
    
    /**
     * Opens the underlying connection.
     *
     * @param route         the route along which to open the connection
     * @param context       the context for opening the connection
     * @param params        the parameters for opening the connection
     *
     * @throws IOException  in case of a problem
     */
    public void open(HttpRoute route,
                     HttpContext context, HttpParams params)
        throws IOException {

        if (route == null) {
            throw new IllegalArgumentException
                ("Route must not be null.");
        }
        //@@@ is context allowed to be null? depends on operator?
        if (params == null) {
            throw new IllegalArgumentException
                ("Parameters must not be null.");
        }
        if ((this.tracker != null) && this.tracker.isConnected()) {
            throw new IllegalStateException("Connection already open.");
        }

        // - collect the arguments
        // - call the operator
        // - update the tracking data
        // In this order, we can be sure that only a successful
        // opening of the connection will be tracked.

        //@@@ verify route against planned route?

        this.tracker = new RouteTracker(route);
        final HttpHost proxy  = route.getProxyHost();

        connOperator.openConnection
            (this.connection,
             (proxy != null) ? proxy : route.getTargetHost(),
             route.getLocalAddress(),
             context, params);

        RouteTracker localTracker = tracker; // capture volatile        

        // If this tracker was reset while connecting,
        // fail early.
        if (localTracker == null) {
            throw new IOException("Request aborted");
        }

        if (proxy == null) {
            localTracker.connectTarget(this.connection.isSecure());
        } else {
            localTracker.connectProxy(proxy, this.connection.isSecure());
        }

    } // open


    /**
     * Tracks tunnelling of the connection to the target.
     * The tunnel has to be established outside by sending a CONNECT
     * request to the (last) proxy.
     *
     * @param secure    <code>true</code> if the tunnel should be
     *                  considered secure, <code>false</code> otherwise
     * @param params    the parameters for tunnelling the connection
     *
     * @throws IOException  in case of a problem
     */
    public void tunnelTarget(boolean secure, HttpParams params)
        throws IOException {

        if (params == null) {
            throw new IllegalArgumentException
                ("Parameters must not be null.");
        }

        //@@@ check for proxy in planned route?
        if ((this.tracker == null) || !this.tracker.isConnected()) {
            throw new IllegalStateException("Connection not open.");
        }
        if (this.tracker.isTunnelled()) {
            throw new IllegalStateException
                ("Connection is already tunnelled.");
        }

        // LOG.debug?

        this.connection.update(null, tracker.getTargetHost(),
                               secure, params);
        this.tracker.tunnelTarget(secure);

    } // tunnelTarget


    /**
     * Tracks tunnelling of the connection to a chained proxy.
     * The tunnel has to be established outside by sending a CONNECT
     * request to the previous proxy.
     *
     * @param next      the proxy to which the tunnel was established.
     *  See {@link org.apache.http.conn.ManagedClientConnection#tunnelProxy
     *                                  ManagedClientConnection.tunnelProxy}
     *                  for details.
     * @param secure    <code>true</code> if the tunnel should be
     *                  considered secure, <code>false</code> otherwise
     * @param params    the parameters for tunnelling the connection
     *
     * @throws IOException  in case of a problem
     */
    public void tunnelProxy(HttpHost next, boolean secure, HttpParams params)
        throws IOException {

        if (next == null) {
            throw new IllegalArgumentException
                ("Next proxy must not be null.");
        }
        if (params == null) {
            throw new IllegalArgumentException
                ("Parameters must not be null.");
        }

        //@@@ check for proxy in planned route?
        if ((this.tracker == null) || !this.tracker.isConnected()) {
            throw new IllegalStateException("Connection not open.");
        }

        // LOG.debug?

        this.connection.update(null, next, secure, params);
        this.tracker.tunnelProxy(next, secure);

    } // tunnelProxy


    /**
     * Layers a protocol on top of an established tunnel.
     *
     * @param context   the context for layering
     * @param params    the parameters for layering
     *
     * @throws IOException  in case of a problem
     */
    public void layerProtocol(HttpContext context, HttpParams params)
        throws IOException {

        //@@@ is context allowed to be null? depends on operator?
        if (params == null) {
            throw new IllegalArgumentException
                ("Parameters must not be null.");
        }

        if ((this.tracker == null) || !this.tracker.isConnected()) {
            throw new IllegalStateException("Connection not open.");
        }
        if (!this.tracker.isTunnelled()) {
            //@@@ allow this?
            throw new IllegalStateException
                ("Protocol layering without a tunnel not supported.");
        }
        if (this.tracker.isLayered()) {
            throw new IllegalStateException
                ("Multiple protocol layering not supported.");
        }

        // - collect the arguments
        // - call the operator
        // - update the tracking data
        // In this order, we can be sure that only a successful
        // layering on top of the connection will be tracked.

        final HttpHost target = tracker.getTargetHost();

        connOperator.updateSecureConnection(this.connection, target,
                                             context, params);

        this.tracker.layerProtocol(this.connection.isSecure());

    } // layerProtocol


    /**
     * Shuts down the entry.
     * 
     * If {@link #open(HttpRoute, HttpContext, HttpParams)} is in progress,
     * this will cause that open to possibly throw an {@link IOException}.
     */
    protected void shutdownEntry() { 
        tracker = null;
    }


} // class AbstractPoolEntry

