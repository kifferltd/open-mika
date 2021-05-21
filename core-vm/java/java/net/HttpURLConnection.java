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

/*
* Adapted 20110414 from the Apache Harmony "enhanced" source revision 929253 by Chris Gray.
*/

package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;

/**
 * This abstract subclass of {@code URLConnection} defines methods for managing
 * HTTP connection according to the description given by RFC 2068.
 * 
 * @see ContentHandler
 * @see URL
 * @see URLConnection
 * @see URLStreamHandler
 */
public abstract class HttpURLConnection extends URLConnection {

    private String methodTokens[] = { "GET", "DELETE", "HEAD", "OPTIONS",
            "POST", "PUT", "TRACE" };

   /**
    * The HTTP request method of this {@code HttpURLConnection}. The default
    * value is {@code "GET"}.
    */
    protected String method = "GET"; //$NON-NLS-1$

    /**
     * The status code of the response obtained from the HTTP request. The
     * default value is {@code -1}.
     * <p>
     * <li>1xx: Informational</li>
     * <li>2xx: Success</li>
     * <li>3xx: Relocation/Redirection</li>
     * <li>4xx: Client Error</li>
     * <li>5xx: Server Error</li>
     */
    protected int responseCode = -1;

    /**
     * The HTTP response message which corresponds to the response code.
     */
    protected String responseMessage;

    /**
     * Flag to define whether the protocol will automatically follow redirects
     * or not. The default value is {@code true}.
     */
    protected boolean instanceFollowRedirects = followRedirects;

    private static boolean followRedirects = true;

    /**
     * If the HTTP chunked encoding is enabled this parameter defines the
     * chunk-length. Default value is {@code -1} that means the chunked encoding
     * mode is disabled.
     */
    //protected int chunkLength = -1;

    /**
     * If using HTTP fixed-length streaming mode this parameter defines the
     * fixed length of content. Default value is {@code -1} that means the
     * fixed-length streaming mode is disabled.
     */
    //protected int fixedContentLength = -1;

    //private final static int DEFAULT_CHUNK_LENGTH = 1024;

    // 2XX: generally "OK"
    // 3XX: relocation/redirect
    // 4XX: client error
    // 5XX: server error
    public final static int HTTP_ACCEPTED = 202;
    public final static int HTTP_BAD_GATEWAY = 502;
    public final static int HTTP_BAD_METHOD = 405;
    public final static int HTTP_BAD_REQUEST = 400;
    public final static int HTTP_CLIENT_TIMEOUT = 408;
    public final static int HTTP_CONFLICT = 409;
    public final static int HTTP_CREATED = 201;
    public final static int HTTP_ENTITY_TOO_LARGE = 413;
    public final static int HTTP_FORBIDDEN = 403;
    public final static int HTTP_GATEWAY_TIMEOUT = 504;
    public final static int HTTP_GONE = 410;
    public final static int HTTP_INTERNAL_ERROR = 500;
    public final static int HTTP_LENGTH_REQUIRED = 411;
    public final static int HTTP_MOVED_PERM = 301;
    public final static int HTTP_MOVED_TEMP = 302;
    public final static int HTTP_MULT_CHOICE = 300;
    public final static int HTTP_NO_CONTENT = 204;
    public final static int HTTP_NOT_ACCEPTABLE = 406;
    public final static int HTTP_NOT_AUTHORITATIVE = 203;
    public final static int HTTP_NOT_FOUND = 404;
    public final static int HTTP_NOT_IMPLEMENTED = 501;
    public final static int HTTP_NOT_MODIFIED = 304;
    public final static int HTTP_OK = 200;
    public final static int HTTP_PARTIAL = 206;
    public final static int HTTP_PAYMENT_REQUIRED = 402;
    public final static int HTTP_PRECON_FAILED = 412;
    public final static int HTTP_PROXY_AUTH = 407;
    public final static int HTTP_REQ_TOO_LONG = 414;
    public final static int HTTP_RESET = 205;
    public final static int HTTP_SEE_OTHER = 303;
    public final static int HTTP_SERVER_ERROR = 500;
    public final static int HTTP_USE_PROXY = 305;
    public final static int HTTP_UNAUTHORIZED = 401;
    public final static int HTTP_UNSUPPORTED_TYPE = 415;
    public final static int HTTP_UNAVAILABLE = 503;
    public final static int HTTP_VERSION = 505;

    /**
     * Constructs a new {@code HttpURLConnection} instance pointing to the
     * resource specified by the {@code url}.
     * 
     * @param url
     *            the URL of this connection.
     * @see URL
     * @see URLConnection
     */
    protected HttpURLConnection(URL url) {
        super(url);
    }

    /**
     * Closes the connection to the HTTP server.
     * 
     * @see URLConnection#connect()
     * @see URLConnection#connected
     */
    public abstract void disconnect();

    /**
     * Returns an input stream from the server in the case of an error such as
     * the requested file has not been found on the remote server. This stream
     * can be used to read the data the server will send back.
     * 
     * @return the error input stream returned by the server.
     */
    public java.io.InputStream getErrorStream() {
        return null;
    }

    /**
     * Returns the value of {@code followRedirects} which indicates if this
     * connection follows a different URL redirected by the server. It is
     * enabled by default.
     * 
     * @return the value of the flag.
     * @see #setFollowRedirects
     */
    public static boolean getFollowRedirects() {
        return followRedirects;
    }

    /**
     * Returns the permission object (in this case {@code SocketPermission})
     * with the host and the port number as the target name and {@code
     * "resolve, connect"} as the action list. If the port number of this URL
     * instance is lower than {@code 0} the port will be set to {@code 80}.
     * 
     * @return the permission object required for this connection.
     * @throws IOException
     *             if an IO exception occurs during the creation of the
     *             permission object.
     */
    public java.security.Permission getPermission() throws IOException {
        int port = url.getPort();
        if (port < 0) {
            port = 80;
        }
        return new SocketPermission(url.getHost() + ":" + port, //$NON-NLS-1$
                "connect, resolve"); //$NON-NLS-1$
    }

    /**
     * Returns the request method which will be used to make the request to the
     * remote HTTP server. All possible methods of this HTTP implementation is
     * listed in the class definition.
     * 
     * @return the request method string.
     * @see #method
     * @see #setRequestMethod
     */
    public String getRequestMethod() {
        return method;
    }

    /**
     * Returns the response code returned by the remote HTTP server.
     * 
     * @return the response code, -1 if no valid response code.
     * @throws IOException
     *             if there is an IO error during the retrieval.
     * @see #getResponseMessage
     */
    public int getResponseCode() throws IOException {
        // Call getInputStream() first since getHeaderField() doesn't return
        // exceptions
        getInputStream();
        String response = getHeaderField(0);
        if (response == null) {
            return -1;
        }
        response = response.trim();
        int mark = response.indexOf(" ") + 1; //$NON-NLS-1$
        if (mark == 0) {
            return -1;
        }
        int last = mark + 3;
        if (last > response.length()) {
            last = response.length();
        }
        responseCode = Integer.parseInt(response.substring(mark, last));
        if (last + 1 <= response.length()) {
            responseMessage = response.substring(last + 1);
        }
        return responseCode;
    }

    /**
     * Returns the response message returned by the remote HTTP server.
     * 
     * @return the response message. {@code null} if no such response exists.
     * @throws IOException
     *             if there is an error during the retrieval.
     * @see #getResponseCode()
     */
    public String getResponseMessage() throws IOException {
        if (responseMessage != null) {
            return responseMessage;
        }
        getResponseCode();
        return responseMessage;
    }

    /**
     * Sets the flag of whether this connection will follow redirects returned
     * by the remote server. This method can only be called with the permission
     * from the security manager.
     * 
     * @param auto
     *            the value to enable or disable this option.
     * @see SecurityManager#checkSetFactory()
     */
    public static void setFollowRedirects(boolean auto) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        followRedirects = auto;
    }

    /**
     * Sets the request command which will be sent to the remote HTTP server.
     * This method can only be called before the connection is made.
     * 
     * @param method
     *            the string representing the method to be used.
     * @throws ProtocolException
     *             if this is called after connected, or the method is not
     *             supported by this HTTP implementation.
     * @see #getRequestMethod()
     * @see #method
     */
    public void setRequestMethod(String method) throws ProtocolException {
        if (connected) {
            throw new ProtocolException("Connection already established");
        }
        for (int i = 0; i < methodTokens.length; i++) {
            if (methodTokens[i].equals(method)) {
                // if there is a supported method that matches the desired
                // method, then set the current method and return
                this.method = methodTokens[i];
                return;
            }
        }
        // if none matches, then throw ProtocolException
        throw new ProtocolException();
    }

    /**
     * Returns whether this connection uses a proxy server or not.
     * 
     * @return {@code true} if this connection passes a proxy server, false
     *         otherwise.
     */
    public abstract boolean usingProxy();

    /**
     * Returns whether this connection follows redirects.
     * 
     * @return {@code true} if this connection follows redirects, false
     *         otherwise.
     */
    public boolean getInstanceFollowRedirects() {
        return instanceFollowRedirects;
    }

    /**
     * Sets whether this connection follows redirects.
     * 
     * @param followRedirects
     *            {@code true} if this connection will follows redirects, false
     *            otherwise.
     */
    public void setInstanceFollowRedirects(boolean followRedirects) {
        instanceFollowRedirects = followRedirects;
    }

    /**
     * Returns the date value in milliseconds since {@code 01.01.1970, 00:00h}
     * corresponding to the header field {@code field}. The {@code defaultValue}
     * will be returned if no such field can be found in the response header.
     * 
     * @param field
     *            the header field name.
     * @param defaultValue
     *            the default value to use if the specified header field wont be
     *            found.
     * @return the header field represented in milliseconds since January 1,
     *         1970 GMT.
     */
    public long getHeaderFieldDate(String field, long defaultValue) {
        return super.getHeaderFieldDate(field, defaultValue);
    }

}
