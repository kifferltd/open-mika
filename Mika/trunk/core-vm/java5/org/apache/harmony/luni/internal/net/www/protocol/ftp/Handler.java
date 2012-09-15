/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.luni.internal.net.www.protocol.ftp;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.harmony.luni.util.Msg;

public class Handler extends URLStreamHandler {
    /**
     * Open a URLConnection on the given URL.
     */
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new FtpURLConnection(u);
    }

    /**
     * Returns a connection, which is established via the <code>proxy</code>,
     * to the FTP server specified by this <code>URL</code>. If
     * <code>proxy</code> is DIRECT type, the connection is made in normal
     * way.
     * 
     * @param u
     *            the URL which the connection is pointing to
     * @param proxy
     *            the proxy which is used to make the connection
     * @return a connection to the resource pointed by this url.
     * 
     * @throws IOException
     *             if this handler fails to establish a connection.
     * @throws IllegalArgumentException
     *             if any argument is null or the type of proxy is wrong.
     * @throws UnsupportedOperationException
     *             if the protocol handler doesn't support this method.
     */
    @Override
    protected URLConnection openConnection(URL u, Proxy proxy)
            throws IOException {
        if (null == u || null == proxy) {
            throw new IllegalArgumentException(Msg.getString("K034b")); //$NON-NLS-1$
        }
        return new FtpURLConnection(u, proxy);
    }

    /**
     * Return the default port.
     */
    @Override
    protected int getDefaultPort() {
        return 21;
    }
}
