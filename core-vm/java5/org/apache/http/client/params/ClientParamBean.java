/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpclient/trunk/module-client/src/main/java/org/apache/http/client/params/ClientParamBean.java $
 * $Revision: 659595 $
 * $Date: 2008-05-23 09:47:14 -0700 (Fri, 23 May 2008) $
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

package org.apache.http.client.params;

import java.util.Collection;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.conn.ClientConnectionManagerFactory;
import org.apache.http.params.HttpAbstractParamBean;
import org.apache.http.params.HttpParams;

public class ClientParamBean extends HttpAbstractParamBean {

    public ClientParamBean (final HttpParams params) {
        super(params);
    }

    public void setConnectionManagerFactoryClassName (final String factory) {
        params.setParameter(ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME, factory);
    }
    
    public void setConnectionManagerFactory(ClientConnectionManagerFactory factory) {
        params.setParameter(ClientPNames.CONNECTION_MANAGER_FACTORY, factory);
    }

    public void setHandleRedirects (final boolean handle) {
        params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, handle);
    }

    public void setRejectRelativeRedirect (final boolean reject) {
        params.setBooleanParameter(ClientPNames.REJECT_RELATIVE_REDIRECT, reject);
    }

    public void setMaxRedirects (final int maxRedirects) {
        params.setIntParameter(ClientPNames.MAX_REDIRECTS, maxRedirects);
    }

    public void setAllowCircularRedirects (final boolean allow) {
        params.setBooleanParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, allow);
    }

    public void setHandleAuthentication (final boolean handle) {
        params.setBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, handle);
    }

    public void setCookiePolicy (final String policy) {
        params.setParameter(ClientPNames.COOKIE_POLICY, policy);
    }

    public void setVirtualHost (final HttpHost host) {
        params.setParameter(ClientPNames.VIRTUAL_HOST, host);
    }

    public void setDefaultHeaders (final Collection <Header> headers) {
        params.setParameter(ClientPNames.DEFAULT_HEADERS, headers);
    }

    public void setDefaultHost (final HttpHost host) {
        params.setParameter(ClientPNames.DEFAULT_HOST, host);
    }
    
}
