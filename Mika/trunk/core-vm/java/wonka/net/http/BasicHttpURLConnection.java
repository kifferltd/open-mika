/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2005, 2008, 2009, 2011 by Chris Gray, /k/ Embedded  *
* Java Solutions. All rights reserved.                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package wonka.net.http;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.jar.Attributes;
import java.util.zip.GZIPInputStream;
import wonka.decoders.Latin1Decoder;
import wonka.encoder.Base64Encoder;

/**
 ** A simple implementation of HttpURLConnection.
 ** This implementation does not support connection re-use.
 ** Requests will be sent using transfer-encoding: chunked iff the 
 ** content-length request header has not been set at the time of connecting.
 ** HTTP proxies are supported, but the only proxy-authentication method
 ** supported is Basic Authentication.
 **
 ** System property wonka.net.http.timeout can be used to set the timeout
 ** applied to all socket read() operations. The default is 60000 milliseconds,
 ** and a value of 0 means that no timeout will be applied.
 */
public class BasicHttpURLConnection extends HttpURLConnection {
    private static final String POST = "POST";

    private static final String GET = "GET";

    private static final String PUT = "PUT";

    private static final String HEAD = "HEAD";

    private static final String DELETE = "DELETE";

    private static final String OPTIONS = "OPTIONS";

    private static final String TRACE = "TRACE";

  /**
   ** If set to <code>true</code>, cache the proxy user name and password
   ** until we detect that system property <code>http.proxyHost</code>
   ** has changed.
   */
  private final static boolean CACHE_PROXY_AUTH = true;

  private static final int MAX_RECONNECTS = 20;

  /**
   ** This is the date format we always use when sending a request, 
   **and the first one we try when parsing the date in a response.
   */
  private static SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'");

  /**
   ** First fall-back when parsing the date in a response.
   */
  private static SimpleDateFormat rfc850Parser = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss 'GMT'");

  /**
   ** Second fall-back when parsing the date in a response (and the last for now).
   */
  private static SimpleDateFormat asctimeParser = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

  /**
   ** Timeout (in milliseconds) to be applied to all socket read()s.
   ** Determined by system property wonka.net.http.timeout, default = 60000.
   */
  private static int timeout;

  /**
   ** A decoder for ISO 8859-1, used for converting chars to bytes.
   */
  private static Latin1Decoder decoder = new Latin1Decoder();

  /**
   ** The value of the <code>http.proxyHost</code> system property.
   ** Empty string (<code>""</code>) if property is not defined.
   */
  private static String proxyHost;

  /**
   ** The proxy host, resolved as an InetAddress. Only set when 
   ** <code>getProxyAddr()</code> has been called.
   */
  private static InetAddress proxyAddr;

  /**
   ** The value of the <code>http.proxyUser</code> system property.
   ** Empty string (<code>""</code>) if property is not defined.
   */
  private static String proxyUser;

  /**
   ** The value of the <code>http.proxyPassword</code> system property.
   ** Empty string (<code>""</code>) if property is not defined.
   */
  private static String proxyPassword;

  /**
   ** Mapping of protection spaces (represented as InetAddress ":" realm)
   ** onto credentials (represented as base64-encoded userid:password).
   */
  private static HashMap basicCredentials = new HashMap();

  /**
   ** Default value for instanceFollowRedirects.
   */
  private static boolean defaultFollowRedirects = true;

  /**
   ** Package access so other classes can also see it.
   */
  static boolean verbose = (System.getProperty("mika.verbose", "").indexOf("http") >= 0);

  /**
   ** Implement verbosity
   */
  static void debug(String s) {
    if (verbose) {
      System.err.println(s);
    }
  }

  private boolean requestSent;

  /**
   ** Set up the static variables <code>proxyHost</code>, <code>proxyUser</code>,
   ** and <code>proxyPassword</code>. We also set <code>proxyAddr</code> to null,
   ** so that if this value is needed the proxy address must be resolved again.
   */
  private static void setProxyFields() {
    proxyHost = System.getProperty("http.proxyHost", "").trim();
    proxyAddr = null;
    proxyUser = System.getProperty("http.proxyUser", "").trim();
    proxyPassword = System.getProperty("http.proxyPassword", "").trim();
  }

  /**
   ** Returns the address of the proxy as a <code>InetAddress</code>.
   ** <code>proxyHost</code> will be resolved if not already done. 
   */
  private static synchronized InetAddress getProxyAddr() throws UnknownHostException {
    if (proxyAddr == null) {
      proxyAddr = InetAddress.getByName(proxyHost);
    }

    return proxyAddr;
  }

  /**
   ** Set up the proxy fields, the date formatters and the socket timeout.
   */
  static {
    //setProxyFields();
    try {
      TimeZone tz = TimeZone.getTimeZone("GMT");
      dateFormatter.setTimeZone(tz);
      rfc850Parser.setTimeZone(tz);
      asctimeParser.setTimeZone(tz);
    }
    catch(RuntimeException rt){}
    timeout = Integer.getInteger("wonka.net.http.timeout", 60).intValue() * 1000;
  }

  /**
   ** The socket over which the request is sent and the response received.
   */
  private Socket socket;

  /**
   ** The InputStream associated with <var>socket</var>.
   */
  private InputStream in;

  /**
   ** The stream used to write to <var>socket</var>.
   */
  private OutputStream out;
    
  
  /**
   ** The request headers, as a Map from key to value.
   */
  private Map requestHeaders;

  /**
   ** The status line (e.g. "HTTP 200 OK") received from the server.
   */
  private String responseLine;

  /**
   ** The response headers, as a Map from key to value.
   ** Only valid after <code>connect()</code>.
   */
  private Map responseHeaders;

  /**
   ** A list of valid keys into <var>responseHeaders</var>.
   ** Only valid after <code>connect()</code>.
   */
  private ArrayList keys;

  /**
   ** The URL host, resolved as an InetAddress. Call <code>resolveHost()</code>
   ** to perform the resolution.
   */
  InetAddress hostAddr;

  /**
   ** The realm extracted from a WWW-Authenticate challenge, or null if no challenge has been received.
   */
  private String realm;

  /**
   ** The value of the <code>Content-length</code> request header,
   ** or -1 if none has been set. Package-protected, so HttpInputStream
   ** can see it.
   */
  int requestContentLength;

  /**
   ** True iff parseResponse() has been called.
   */
  private boolean responseParsed;

  /**
   ** Number of reconnects (redirections or authorisation attempts) so far.
   */
  private int reconnectCount;

  /**
   ** Prepare the connection (we only connect later, "lazily").
   ** The <var>requestHeaders</var> are pre-loaded with:
   ** <ul>
   ** <li><code>accept-encoding=gzip</code>
   ** <li><code>host=<own hostname>[:port]</code>
   ** <li><code>user-agent=Mika-HTTP</code>
   ** </ul>
   */
  public BasicHttpURLConnection(URL url) {
    super(url);
    String newProxyHost = System.getProperty("http.proxyHost", "").trim();
    if (!CACHE_PROXY_AUTH || !newProxyHost.equalsIgnoreCase(proxyHost)) {
      setProxyFields();
    }
    requestHeaders = new Attributes();
    requestHeaders.put(new Attributes.Name("accept-encoding"),"gzip");
    requestHeaders.put(new Attributes.Name("host"),url.getHost()+(url.getPort()== -1 ? "" : ":"+String.valueOf(url.getPort())));
    requestHeaders.put(new Attributes.Name("user-agent"),"Mika-HTTP");
    requestContentLength = -1;
    instanceFollowRedirects = defaultFollowRedirects;
  }

  /*
  ** Get the attribute name of the n'th header field. Note that if n == 0
  ** null will be returned, because we associate 0 with the HTTP status line.
  */
  public String getHeaderFieldKey(int n) {
    // ensure that response has been read
    try {
      getInputStream();
    }
    catch (IOException ioe) {}
    if (responseHeaders == null) {
      return null;
    }

    if (keys == null || n <= 0 || n > keys.size()) {
      return null;
    }

    return (String)keys.get(n - 1);
  }

  /*
  ** Get the attribute value of the n'th header field. Note that if n == 0
  ** the HTTP status line will be returned.
  */
  public String getHeaderField(int n) {
    // ensure that response has been read
    try {
      getInputStream();
    }
    catch (IOException ioe) {
    }
    if (responseHeaders == null) {
      return null;
    }

    if (keys == null || n < 0 || n > keys.size()) {
      return null;
    }

    if (n == 0) {
      return responseLine;
    }

    return (String)responseHeaders.get(keys.get(n - 1));
  }

  /**
   ** Resolve URL host as an <code>InetAddress</code> 
   ** and store it in <code>hostAddr</code>.
   */
  private void resolveHost() throws UnknownHostException {
    String hostname = url.getHost();
    hostAddr = InetAddress.getByName(hostname);
  }

  /**
   ** Get the port on the HTTP proxy to which requests should be sent.
   ** If there is no HTTP proxy (system property <code>http.proxyPort</code>
   ** is undefined or empty), returns 80.
   */
  protected int getProxyPort() throws IllegalArgumentException
  {
    String proxyPortStr = System.getProperty("http.proxyPort");

    if((proxyPortStr==null) || (proxyPortStr.trim().equals(""))) {
      return 80;
    }
    else {
      try {
        return Integer.parseInt(proxyPortStr);
      }
      catch(NumberFormatException ex) {
    	throw new IllegalArgumentException("http.proxyPort is not a number");
       }
    }
  }
  
  /**
   ** Connect to the remote host, send the request, and get the response.
   */
  public void connect() throws IOException {
      if (connected) {
        return;
      }

      keys = new ArrayList();
      int port = url.getPort();
      if (port < 0) {
        port = url.getDefaultPort();
      }
      resolveHost();
      
      if (usingProxy()) {
        int proxyport = getProxyPort();
        debug("HTTP: connecting to proxy " +  proxyHost + ":" + proxyport);
  	socket = new Socket(proxyHost, proxyport);
      }
      else {
        String host = url.getHost();
        debug("HTTP: connecting to " +  host + ":" + port);
        socket = new Socket(host, port);
      }
	  
      socket.setSoTimeout(timeout);
      in = new BufferedInputStream(socket.getInputStream(),4096);
      out = socket.getOutputStream();
      connected = true;
    }

    protected void doRequest() throws IOException {
      if (requestSent) {
        // Clean up and get out
        OutputStream local_out = out;
        if ((local_out != null) && !responseParsed) {
          debug("HTTP: flushing " +  local_out);
          try {
            ((HttpOutputStream)local_out).flush_internal();
          }
          catch (ClassCastException ioe) {
            // Ignore - we were writing directly to the socket
          }
          catch (IOException ioe) {
            // Ignore - probably stream was already closed
          }
        }
        return;
      }

      if(PUT.equals(method) || POST.equals(method)){
        out.write(getRequestLine().getBytes());
        sendPartialHeaders(out);
        doOutput = true;
        requestSent = true;
        return;
      }
      else if(GET.equals(method)) {
        requestGET();
      }
      else if(HEAD.equals(method)) {
        requestGET();
      }
      else if(DELETE.equals(method)) {
        requestGET();
      }
      else if(OPTIONS.equals(method)) {
        requestGET();
      }
      else if(TRACE.equals(method)) {
        requestGET();
      }
      else {
        throw new IOException("invalid method '"+method+"'");
      }

      requestSent = true;

      parseResponse();

      while (true) {
        if (responseCode == HTTP_UNAUTHORIZED) {
          String challenge = internal_getResponseProperty("www-authenticate");
          if (challenge == null) {
            throw new ProtocolException("HTTP_UNAUTHORIZED response contained no www-authenticate header");
          }
          else if (getAuthorisation(challenge.trim(), url.toString(), hostAddr, url.getPort())) {
            debug("HTTP: retrying with authorization");

            if (!reconnect()) {
              break;
            }
          }
          else {
            throw new IOException(url + " requires authorisation, but we have no credentials");
          }
        }
        else if (responseCode == HTTP_PROXY_AUTH && usingProxy()) {
          String challenge = internal_getResponseProperty("proxy-authenticate");
          if (challenge == null) {
            throw new ProtocolException("HTTP_PROXY_AUTH response contained no proxy-authenticate header");
          }
          else if (getAuthorisation(challenge.trim(), "Proxy", getProxyAddr(), getProxyPort())) {
            debug("HTTP: retrying with proxy authorization");

            if (!reconnect()) {
              break;
            }
          }
          else {
            throw new IOException("Proxy server requested authentication, but we have no credentials");
          }
        }
        else break;
      }
  }

  /**
   ** Disconnect from the host, by closing the socket.
   */
  public void disconnect(){
    debug("HTTP: disconnecting " +  this);
    // [CG 20090226] Close input stream
    InputStream local_in = in;
    in = null;
    if (local_in != null) {
      debug("HTTP: closing " +  local_in);
      try {
        local_in.close();
      }
      catch(IOException ioe){}
    }

    // [CG 20090702] Close output stream
    OutputStream local_out = out;
    out = null;
    if (local_out != null) {
      debug("HTTP: closing " +  local_out);
      try {
        local_out.close();
      }
      catch(IOException ioe){}
    }

    Socket local_socket = socket;
    socket = null;
    if(local_socket != null){
      debug("HTTP: disconnecting " +  local_socket);
      try {
        local_socket.close();
      }
      catch(IOException ioe){}
    }
    connected = false;
  }

  /**
   ** Get the response header field named <var>name</var>.
   */
  public String getHeaderField(String name){
    // ensure that response has been read
    try {
      getInputStream();
    }
    catch (IOException ioe) {}
    if (responseHeaders == null) {
      return null;
    }

    return internal_getResponseProperty(name);
  }

  /**
   ** Get the HTTP response code. Implies connecting and parsing the HTTP
   ** response headers.
   */
  public int getResponseCode() throws IOException {
    connect();
    doRequest();
    parseResponse();

    return responseCode;
  }

  /**
   ** Get the HTTP response message. Implies connecting and parsing the HTTP
   ** response headers.
   */
  public String getResponseMessage() throws IOException {
    connect();
    doRequest();
    parseResponse();

    return responseMessage;
  }

  /**
   ** Parse the response header field named <var>name</var> and return the
   ** result as milliseconds since the start of the epoch. If no such header
   ** found, return <var>def</def>.
   */
  public long getHeaderFieldDate(String name, long def){
    String field = getHeaderField(name);
    if(field != null){
      try {
        return dateFormatter.parse(field).getTime();
      }
      catch(ParseException pe1){
        try {
          return rfc850Parser.parse(field).getTime();
        }
        catch(ParseException pe2){
          try {
            return asctimeParser.parse(field).getTime();
          }
          catch(ParseException pe3){}
        }
      }
    }
    return def;
  }

  /**
   ** Get the InputStream on which the response is received.
   ** If necessary, <code>connect()</code> first.
   */
  public InputStream getInputStream() throws IOException {
    connect();
    doRequest();
    OutputStream local_out = out;
    out = null;
    if (local_out != null && local_out instanceof HttpOutputStream) {
      debug("HTTP: closing " +  local_out);
      try {
        local_out.close();
      }
      catch(IOException ioe){}
    }

    parseResponse();
    checkConnection();

    return in;
  }

  /**
   ** Get the OutputStream to which the request content should be written.
   ** If necessary, <code>connect()</code> first.
   */
  public OutputStream getOutputStream() throws IOException {

    if(!doOutput){
      throw new IOException("output is disabled");
    }

    if (requestSent) {
      throw new ProtocolException("request already sent");
    }

    if (out != null) {
      return out;
    }

    if (method.equals(GET)) {
      method = POST;
    }
    else if (!(PUT.equals(method) || POST.equals(method))) {
      throw new ProtocolException("can only call getOutputStream() on PUT or POST");
    }

    connect();
    doRequest();

      if (requestContentLength < 0) {
        out = new HttpOutputStream(socket.getOutputStream());
      }
      else {
        out = socket.getOutputStream();
        out.write(13);
        out.write(10);
      }

    return out;
  }

  /**
   ** Get the value of the request header named <var>name</var>.
   */
  public String getRequestProperty(String key){
    return internal_getRequestProperty(key);
  }

  /**
   ** Version for internal use within this class.
   */
  private String internal_getRequestProperty (String key) {
    return (String) requestHeaders.get(new Attributes.Name(key));  
  }

  /**
   ** Get the value of the response header named <var>name</var>.
   */
  private String internal_getResponseProperty (String key) {
    return (String) responseHeaders.get(normaliseName(key));  
  }

  /**
   ** Version for internal use within this class.
   */
  private boolean internal_checkResponseProperty (String key, String value) {
    String rawValue = (String) responseHeaders.get(normaliseName(key));  

    return rawValue != null && rawValue.equalsIgnoreCase(value);  
  }

  /**
   ** Convert a name to "normalised form", in which the first letter is upper
   ** case and all others are lower case. This conversion should always be
   ** performed before using a String as key into <code>responseHeaders</code>.
   */
  private String normaliseName(String s) {
    StringBuffer sb = new StringBuffer(s.toLowerCase());
    sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
    int l = sb.length() - 1;
    for (int i = 1; i < l; ++i) {
      if (sb.charAt(i) == '-') {
        ++i;
        sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
      }
    }

    return sb.toString();
  }

  /**
   ** Set the <code>if-modified-since</code> request header.
   ** Must be called before <code>connect()</code>.
   */
  public void setIfModifiedSince(long time){
    super.setIfModifiedSince(time);
    requestHeaders.put(new Attributes.Name("if-modified-since"), dateFormatter.format(new Date(time)));
  }

  /**
   ** Set the request header named <var>name</var>.
   ** Must be called before <code>connect()</code>.
   */
  public void setRequestProperty(String name, String value){
    String lc = name.toLowerCase();
    if ("content-length".equals(lc)) {
      requestContentLength = Integer.parseInt(value);
    }
    requestHeaders.put(new Attributes.Name(lc), value);
  }

  /**
   ** Returns true iff system property <code>http.proxyHost</code> is defined
   ** and non-empty.
   */
  public boolean usingProxy(){
    return !proxyHost.equals("");
  }

  /**
   ** Generate the HTTP Request-Line. 
   ** When no proxy is used, the Request-Line consists of the following:
   ** <ul>
   ** <li>The request token, e.g. <code>GET</code>.
   ** <li>A space.
   ** <li>The "file" part of the URL, or "/" if this is empty.
   ** (Note: the "file" part includes the query part, if any).
   ** <li>If the URL has a fragment part, "#" followed by the fragment part.
   ** <li>A space.
   ** <li><code>HTTP/1.1</code>
   ** <li>Carriage return, line feed.
   ** </ul>
   ** When a proxy is used the Request-Line consists of the following:
   ** <ul>
   ** <li>The request token, e.g. <code>GET</code>.
   ** <li>A space.
   ** <li>The URL originally requested.
   ** <li>A space.
   ** <li><code>HTTP/1.1</code>
   ** <li>Carriage return, line feed.
   ** </ul>
   */
  private String getRequestLine(){
    StringBuffer requestLine = new StringBuffer(method);

    requestLine.append(' ');
	
    if (usingProxy()) {
      requestLine.append(url.toString());
    }
    else {
      if(url.getFile().equals("")) {
    	requestLine.append('/');
      }
      else {
    	requestLine.append(url.getFile());
      }

      String ref = url.getRef();
      if(ref != null){
        requestLine.append('#');
        requestLine.append(ref);
      }
    }
	
    requestLine.append(" HTTP/1.1\r\n");
    debug("HTTP: request: " + requestLine.substring(0, requestLine.length() - 2));

    return requestLine.toString();
  }

  /**
   ** Dump the request geaders to be sent as a series of debug messages.
   */
  private void dumpRequestHeaders(String headers) {
    try {
      BufferedReader r = new BufferedReader(new StringReader(headers));
      String h = r.readLine();
      while (h != null) {
        debug("HTTP:          " + h);
        h = r.readLine();
      }
    }
    catch (IOException ioe) {}
  }

  /**
   ** <p>Marshal the request headers into a String.
   ** <p>If the followibng headers are provided by the client they are ignored:
   ** <ul>
   ** <li><code>Connection</code>
   ** <li><code>Accept-encoding</code>
   ** </ul>
   ** <p>We always add three headers:
   ** <ul>
   ** <li><code>Connection: close</code>
   ** <li><code>Accept-encoding: gzip</code>
   ** <li><code>Date: </code><i>current date and time</i>
   ** </ul>
   ** <p>If a proxy is being used and proxyUser is non-empty, we also add 
   ** a proxy authentication header.
   */
  private String getRequestHeaders() throws UnknownHostException {
    addProxyAuthenticationHeader();	
    addBasicAuthenticationHeader();	
    StringBuffer request = new StringBuffer(1024);
    request.append("Connection: close\r\n");
    request.append("Accept-encoding: gzip\r\n");
    request.append("Date: ");
    dateFormatter.format(new Date(), request, new java.text.FieldPosition(0));
    request.append("\r\n");
    Iterator it = requestHeaders.entrySet().iterator();
    boolean skip;
    while(it.hasNext()){
      Map.Entry entry = (Map.Entry)it.next();
      Object key = entry.getKey();
      try {
        skip = ((String)key).equalsIgnoreCase("connection")
            || ((String)key).equalsIgnoreCase("accept-encoding");
      }
      catch (ClassCastException cce) {
        skip = false;
      }
      if (!skip) {
        int here = request.length();
        request.append(key);
        request.append(": ");
        request.append(entry.getValue());
        request.append("\r\n");
        request.setCharAt(here, Character.toUpperCase(request.charAt(here)));
      }
    }
	
    request.append("\r\n");

    if (verbose) {
      dumpRequestHeaders(request.toString());
    }

    return request.toString();
  }

  /**
   ** If we have basic credentials for this protection space, add basic 
   ** authentication headers to the request headers held in <var>buffer</var>.
   */
  private void addBasicAuthenticationHeader() throws UnknownHostException {
    if (realm != null) {
      String credentials = (String)basicCredentials.get(hostAddr + ":" + realm);
      if (credentials != null) {
        requestHeaders.put(new Attributes.Name("authorization"), "Basic " + credentials);
      }
    }
  }

  /**
   ** If a proxy is being used and proxyUser is non-empty, add proxy 
   ** authentication headers to the request headers held in <var>buffer</var>.
   */
  private void addProxyAuthenticationHeader() throws UnknownHostException {
    if (usingProxy() && proxyUser != null && proxyUser.length() > 0) {
      //int port = 
      getProxyPort();

      StringBuffer unencoded = new StringBuffer(proxyUser);
      unencoded.append(':');
      unencoded.append(proxyPassword);
      StringBuffer buffer = new StringBuffer(64);
      buffer.append("Basic ");
      buffer.append(Base64Encoder.encode(unencoded.toString()));
      requestHeaders.put(new Attributes.Name("proxy-authorization"), buffer.toString());
    }	  
  }  

  /**
   ** Send the headers for [the first chunk of] a PUT/POST request.
   ** We always send <code>Connection: close</code>, regardless of
   ** what was specified using setRequestHeader(). We also always add
   ** a <code>Date:<code> header.
   */
  private void sendPartialHeaders(OutputStream out) throws IOException {
    StringBuffer request = new StringBuffer(1024);
    requestHeaders.remove(new Attributes.Name("connection"));
    request.append("Connection: close\r\n"); //connection will be closed after the response
    addProxyAuthenticationHeader();	
    addBasicAuthenticationHeader();	
    request.append("Date: ");
    dateFormatter.format(new Date(), request, new java.text.FieldPosition(0));
    request.append("\r\n");
    Iterator it = requestHeaders.entrySet().iterator();
    while(it.hasNext()){
      Map.Entry entry = (Map.Entry)it.next();
      int here = request.length();
      request.append(normaliseName(entry.getKey().toString()));
      request.append(": ");
      request.append(entry.getValue());
      request.append("\r\n");
      request.setCharAt(here, Character.toUpperCase(request.charAt(here)));
    }
	
    if (verbose) {
      dumpRequestHeaders(request.toString());
    }

    int length = request.length();
    char[] chars = new char[length];
    request.getChars(0,length,chars,0);
    out.write(decoder.cToB(chars,0,length));
  }

  /**
   * Add username:password to the table as an authorisation for addr:realm.
   */
  private void addAuthorisation(InetAddress addr, String realm, String username, char[] password) {
    StringBuffer unencoded = new StringBuffer(username);
    unencoded.append(':');
    unencoded.append(password);
    basicCredentials.put(addr + ":" + realm, Base64Encoder.encode(unencoded.toString()));
  }

  /**
   * Remove username:password from the table as an authorisation for addr:realm.
   */
  private void removeAuthorisation(InetAddress addr, String realm) {
    basicCredentials.remove(addr + ":" + realm);
  }

  /**
   ** Get the information required for WWW or Proxy Authorisation, by invoking
   ** the default Authenticator.
   ** <param>challenge
   **   The value of the WWW-Authenticate or Proxy-Authenticate header in the
   **   server's 401 or 407 response.
   ** <param>name
   **   Either "Proxy" for Proxy Authorisation, or the string form of the URL
   **   for which authorisation is required.
   ** <param>addr
   **   The address of the proxy server (proxy authorisation) or origin server
   **   (WWW authorisation).
   ** <param>port
   **   The port used for the proxy server (proxy authorisation) or origin server
   **   (WWW authorisation).
   */
  private boolean getAuthorisation(String challenge, String name, InetAddress addr, int port) {
    String prompt = name + " requires authorization";
    String scheme = "[not set]";
    int space1 = challenge.indexOf(' ');

    if (space1 >= 0) {
      scheme = challenge.substring(0, space1);
      String rest = challenge.substring(space1 + 1);
      int realmstart = rest.indexOf("realm=\"");
      int realmend = (realmstart < 0) ? -1 : rest.indexOf("\"", realmstart + 7);
      if (realmend >= 0) {
        realm = rest.substring(realmstart + 6, realmend + 1);
        prompt += " for realm " + realm;
      }
    }

    PasswordAuthentication passwordAuth = Authenticator.requestPasswordAuthentication(addr, port, "HTTP", prompt, scheme);

    if (passwordAuth!=null) {
      if ("Proxy".equalsIgnoreCase(name)) {
        proxyUser = passwordAuth.getUserName();
        proxyPassword = new String(passwordAuth.getPassword());
      }
      else {
        addAuthorisation(addr, realm, passwordAuth.getUserName(), passwordAuth.getPassword());
      }

      return true;
    }

    return false;
  }

  /**
   ** Perform a GET-style request, i.e. one with no body. We send all the 
   ** headers except Content-Length.
   */
  private void requestGET() throws IOException {
    requestHeaders.remove(new Attributes.Name("content-length"));
    out.write(getRequestLine().getBytes());
    out.write(getRequestHeaders().getBytes());
    doOutput = false;
  }

  /**
   ** Parse an HTTP response into a response code, a response message (if any),
   ** and a set of response headers.
   */
  void parseResponse() throws IOException {
    if (responseParsed) {

        return;

    }

    responseHeaders = new HashMap();
    // TODO: should this code be moved to checkConnection()?
    if (out != null) {
      try {
        ((HttpOutputStream)out).flush_internal();
      }
      catch (ClassCastException ioe) {
        // Ignore - we were writing directly to the socket
      }
      catch (IOException ioe) {
        // Ignore - probably stream was already closed
      }
    }

    try {
      if(probeStatusLine()){
        responseParsed = true;
        String line = readLine(false);
        responseLine = line;
        debug("HTTP: response: " + line);
        int space1 = line.indexOf(' ');
        if (space1 >= 0) {
          String codeString;
          int space2 = line.indexOf(' ',space1 + 1);
          if (space2 >= 0) {
            codeString = line.substring(space1 + 1, space2);
            try {
              responseCode = Integer.parseInt(codeString);
              responseMessage = line.substring(space2 + 1);
            }
            catch (NumberFormatException nfe) {
            }
          }
          else {
          // Technically this is wrong by RFC 2068, but whatever ...
            codeString = line.substring(space1 + 1);
            try {
              responseCode = Integer.parseInt(codeString);
            }
            catch (NumberFormatException nfe) {
            }
          }
        }

        while(!"".equals(line)){
          Object key;

          int colon = line.indexOf(':');
          if(colon != -1) {
            String name = line.substring(0,colon);
            if(name.trim().equals("")){
              key = new Integer(keys.size());
            }
            else {
              key = normaliseName(name);
            }
            responseHeaders.put(key, line.substring(colon+1).trim());
            keys.add(key);
          }
          else {
            key = new Integer(keys.size());
            responseHeaders.put(key, line.trim()  + ")");
            keys.add(key);
          }
          line = readLine(true);
          debug("HTTP:           " + line);
        }
      }

    }
    catch(RuntimeException rt){
      rt.printStackTrace();
      throw new IOException();
    }

    // TODO: should this code be moved to checkConnection()?
    if(internal_checkResponseProperty("Transfer-encoding", "chunked")){
      in = new ChunkedInputStream(in);
      debug("HTTP: switched to chunked input");
    }

    // TODO: should this code be moved to checkConnection()?
    if(internal_checkResponseProperty("Content-encoding", "gzip")){
      in = new GZIPInputStream(in);
      debug("HTTP: gunzipping input");
    }

    if(responseCode>=100 && responseCode<200) {
        // Continue recognized. Parse next header
        responseParsed = false;
        parseResponse();
    }
    else if (((responseCode==HTTP_MOVED_PERM) || (responseCode==HTTP_MOVED_TEMP) || (responseCode==HTTP_SEE_OTHER)) && instanceFollowRedirects) {
      if (!(GET == method || HEAD == method)) {
        if (responseCode == HTTP_SEE_OTHER) {
          method = GET;
        }
        else {
          throw new IOException(responseCode + " redirection response not allowed for " + method);
        }
      }

      String location = internal_getResponseProperty("location").trim();
      debug("HTTP: redirecting to " + location);
      if(location==null) {
        throw new IOException("HTTP redirect (" + responseCode + ") has no 'Location' header.");
      }

      this.url = new URL(location);
      // [CG 20071216] Fix problem reported by K. Pauls when visiting sf.net,
      // I don't see where it says we have to this but it seems reasonable
      requestHeaders.put(new Attributes.Name("host"),url.getHost()+(url.getPort()== -1 ? "" : ":"+String.valueOf(url.getPort())));

      reconnect();
    }
  }

  boolean reconnect() throws IOException {
    disconnect();
    responseParsed = false;
    if (++reconnectCount >= MAX_RECONNECTS) {
      return false;
    }

    connect();

    return true;
  }

  /**
   ** Throw an exception if it is not possible to read data from the connection.
   ** 
   */
  void checkConnection() throws IOException {

    if (responseCode == HTTP_OK) {
      return;
    }

    if (++reconnectCount >= MAX_RECONNECTS) {
      throw new ProtocolException("Server redirected too many  times (" + MAX_RECONNECTS + ")");
    }

    if (responseCode == HTTP_NOT_FOUND) {
      throw new FileNotFoundException(url.toString());
    }

    throw new IOException("Server returned HTTP response code " + responseCode + " for " + method + " to " + url);
  }

  /**
  ** Check if the first bytes begin with "HTTP/", otherwise we are dealing
  ** with an "HTTP/0.9" response. Needs <var>in</var> to support <code>mark()/reset()</code>.
  ** @return	true if this is a HTTP/1.0 or HTTP/1.1 response, false otherwise.
  */
  private boolean probeStatusLine() throws IOException {
    connect();
    byte[] bytes = new byte[8];
    // [CG 20090226] Guard against close() in another thread
    InputStream local_in = in;
    local_in.mark(8);
    int len = local_in.read(bytes);
    local_in.reset();
    if(len < 8){
      return false;
    }
    if (! "HTTP/".equals(new String(bytes, 0, 5))) {
      return false;
    }

    return true;
  }

  /**
   ** Read one line of the response headers, allowing for "extension lines"
   ** (lines beginning with a space or tab, which continue the previous
   ** line). Needs to read a few bytes ahead in order to determine whether
   ** the following line is a continuation line, so <var>in</var> needs to
   ** support <code>mark()/reset()</code>.
   */
  private String readLine(boolean continued) throws IOException {
    StringBuffer buf = new StringBuffer(64);
    // [CG 20090226] Guard against close() in another thread
    InputStream local_in = in;
    int ch = local_in.read();
    while(ch != -1){
      boolean stop = false;
      if(ch == '\r'){
        stop = true;
        local_in.mark(1);
        ch = local_in.read();
      }
      if(ch == '\n'){
        if(buf.length() == 0){
          break;
        }
        stop = true;
        local_in.mark(1);
        ch = local_in.read();
      }
      if(stop){
        if(continued && (ch == ' ' || ch  == '\t')){
          continue;
        }
        else {
          local_in.reset();
          break;
        }
      }
      buf.append((char)ch);
      ch = local_in.read();
    }

    return buf.toString();
  }
}
