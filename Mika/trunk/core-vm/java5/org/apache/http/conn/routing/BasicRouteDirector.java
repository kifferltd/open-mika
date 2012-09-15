/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpclient/trunk/module-client/src/main/java/org/apache/http/conn/routing/BasicRouteDirector.java $
 * $Revision: 620255 $
 * $Date: 2008-02-10 02:23:55 -0800 (Sun, 10 Feb 2008) $
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

package org.apache.http.conn.routing;



/**
 * Basic implementation of an {@link HttpRouteDirector HttpRouteDirector}.
 * This implementation is stateless and therefore thread-safe.
 * 
 * @author <a href="mailto:rolandw at apache.org">Roland Weber</a>
 *
 *
 * <!-- empty lines to avoid svn diff problems -->
 * @version $Revision: 620255 $
 *
 * @since 4.0
 */
public class BasicRouteDirector implements HttpRouteDirector {

    // public default constructor


    /**
     * Provides the next step.
     *
     * @param plan      the planned route
     * @param fact      the currently established route, or
     *                  <code>null</code> if nothing is established
     *
     * @return  one of the constants defined in this class, indicating
     *          either the next step to perform, or success, or failure.
     *          0 is for success, a negative value for failure.
     */
    public int nextStep(RouteInfo plan, RouteInfo fact) {
        if (plan == null) {
            throw new IllegalArgumentException
                ("Planned route may not be null.");
        }

        int step = UNREACHABLE;

        if ((fact == null) || (fact.getHopCount() < 1))
            step = firstStep(plan);
        else if (plan.getHopCount() > 1)
            step = proxiedStep(plan, fact);
        else
            step = directStep(plan, fact);

        return step;

    } // nextStep


    /**
     * Determines the first step to establish a route.
     *
     * @param plan      the planned route
     *
     * @return  the first step
     */
    protected int firstStep(RouteInfo plan) {

        return (plan.getHopCount() > 1) ?
            CONNECT_PROXY : CONNECT_TARGET;
    }


    /**
     * Determines the next step to establish a direct connection.
     *
     * @param plan      the planned route
     * @param fact      the currently established route
     *
     * @return  one of the constants defined in this class, indicating
     *          either the next step to perform, or success, or failure
     */
    protected int directStep(RouteInfo plan, RouteInfo fact) {

        if (fact.getHopCount() > 1)
            return UNREACHABLE;
        if (!plan.getTargetHost().equals(fact.getTargetHost()))
            return UNREACHABLE;
        // If the security is too low, we could now suggest to layer
        // a secure protocol on the direct connection. Layering on direct
        // connections has not been supported in HttpClient 3.x, we don't
        // consider it here until there is a real-life use case for it.

        // Should we tolerate if security is better than planned?
        // (plan.isSecure() && !fact.isSecure())
        if (plan.isSecure() != fact.isSecure())
            return UNREACHABLE;

        // Local address has to match only if the plan specifies one.
        if ((plan.getLocalAddress() != null) &&
            !plan.getLocalAddress().equals(fact.getLocalAddress())
            )
            return UNREACHABLE;

        return COMPLETE;
    }


    /**
     * Determines the next step to establish a connection via proxy.
     *
     * @param plan      the planned route
     * @param fact      the currently established route
     *
     * @return  one of the constants defined in this class, indicating
     *          either the next step to perform, or success, or failure
     */
    protected int proxiedStep(RouteInfo plan, RouteInfo fact) {

        if (fact.getHopCount() <= 1)
            return UNREACHABLE;
        if (!plan.getTargetHost().equals(fact.getTargetHost()))
            return UNREACHABLE;
        final int phc = plan.getHopCount();
        final int fhc = fact.getHopCount();
        if (phc < fhc)
            return UNREACHABLE;

        for (int i=0; i<fhc-1; i++) {
            if (!plan.getHopTarget(i).equals(fact.getHopTarget(i)))
                return UNREACHABLE;
        }
        // now we know that the target matches and proxies so far are the same
        if (phc > fhc)
            return TUNNEL_PROXY; // need to extend the proxy chain
            
        // proxy chain and target are the same, check tunnelling and layering
        if ((fact.isTunnelled() && !plan.isTunnelled()) ||
            (fact.isLayered()   && !plan.isLayered()))
            return UNREACHABLE;

        if (plan.isTunnelled() && !fact.isTunnelled())
            return TUNNEL_TARGET;
        if (plan.isLayered() && !fact.isLayered())
            return LAYER_PROTOCOL;

        // tunnel and layering are the same, remains to check the security
        // Should we tolerate if security is better than planned?
        // (plan.isSecure() && !fact.isSecure())
        if (plan.isSecure() != fact.isSecure())
            return UNREACHABLE;

        return COMPLETE;
    }


} // class BasicRouteDirector
