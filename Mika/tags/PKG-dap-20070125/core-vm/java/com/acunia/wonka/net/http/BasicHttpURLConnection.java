/**************************************************************************
* Copyright (c) 2001 by Acunia N.V. All rights reserved.                  *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2005 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/


package com.acunia.wonka.net.http;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.jar.Attributes;
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

  /**
   ** If set to <code>true</code>, cache the proxy user name and password
   ** until we detect that system property <code>http.proxyHost</code>
   ** has changed.
   */
  private final static boolean CACHE_PROXY_AUTH = true;

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
   ** Default value for instanceFollowRedirects.
   */
  private static boolean defaultFollowRedirects = true;

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
  private HttpOutputStream out;
    
  
  /**
   ** The request headers, as a Map from key to value.
   */
  private Map requestHeaders;

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
   ** The value of the <code>Content-Length</code> request header,
   ** or -1 if none has been set. Package-protected, so HttpInputStream
   ** can see it.
   */
  int requestContentLength;

  /**
   ** True iff parseResponse() has been called.
   */
  private boolean responseParsed;

  /**
   ** Prepare the connection (we only connect later, "lazily").
   ** The <var>requestHeaders</var> are pre-loaded with:
   ** <ul>
   ** <li><code>accept-encoding=gzip</code>
   ** <li><code>host=<own hostname>[:port]</code>
   ** <li><code>user-agent=Wonka-HTTP</code>
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
    requestHeaders.put(new Attributes.Name("user-agent"),"Wonka-HTTP");
    requestContentLength = -1;
    instanceFollowRedirects = defaultFollowRedirects;
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
  public synchronized void connect() throws IOException {
    if(!connected){
      responseHeaders = new Hashtable();
      keys = new ArrayList();
      int port = url.getPort();
      resolveHost();
      
      if (usingProxy()) {
  	socket = new Socket(proxyHost, getProxyPort());
      }
      else {
        socket = new Socket(url.getHost(), port == -1 ? 80 : port);
      }
	  
      socket.setSoTimeout(timeout);
      in = new BufferedInputStream(socket.getInputStream(),4096);
      connected = true;
      //send request ...
      if("PUT".equals(method) || "POST".equals(method)){
        OutputStream out = socket.getOutputStream();
        out.write(getRequestLine().getBytes());
        sendPartialHeaders(out);
        doOutput = true;
      }
      else if("GET".equals(method)){
        requestGET();
      }
      else if("HEAD".equals(method)){
        requestGET();
      }
      else if("DELETE".equals(method)){
        requestGET();
      }
      else if("OPTIONS".equals(method)){
        requestGET();
      }
      else if("TRACE".equals(method)){
        requestGET();
      }
      else {
        throw new IOException("invalid method '"+method+"'");
      }
    }
  }

  /**
   ** Disconnect from the host, by closing the socket.
   */
  public synchronized void disconnect(){
    if(socket != null){
      try {
        socket.close();
      }
      catch(IOException ioe){}
      connected = false;
      socket = null;
    }
    connected = false;
  }

  /**
   ** Get the response header field named <var>name</var>.
   */
  public String getHeaderField(String name){
    try {
      parseResponse();
    }
    catch(IOException ioe){
      ioe.printStackTrace();
      return null;
    }
    return internal_getResponseProperty(name);
  }

  /**
   ** Get the HTTP response code. Implies connecting and parsing the HTTP
   ** response headers.
   */
  public int getResponseCode() {
    try {
      parseResponse();
    }
    catch(IOException ioe){
      ioe.printStackTrace();
      return -1;
    }
    return responseCode;
  }

  /**
   ** Get the HTTP response message. Implies connecting and parsing the HTTP
   ** response headers.
   */
  public String getResponseMessage() {
    try {
      parseResponse();
    }
    catch(IOException ioe){
      ioe.printStackTrace();
      return null;
    }
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
    /* [CG 20050926] Don't think we need this, parseResponse() does it for us.
    if (!connected) {
      connect();
    }
    if (out != null) {
      try {
        out.flush_internal();
      }
      catch (IOException ioe) {
        // Ignore - probably stream was already closed
      }
    }
    */
    parseResponse();

    return in;
  }

  /**
   ** Get the OutputStream to which the request content should be written.
   ** If necessary, <code>connect()</code> first.
   */
  public synchronized OutputStream getOutputStream() throws IOException {
    if (!connected) {
      connect();
    }
    if(!doOutput){
      throw new IOException("output is disabled");
    }
    if (out == null) {
      out = new HttpOutputStream(socket.getOutputStream(),this);
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
   ** Check that the request property indicated by <var>key</var> has the
   ** value <code>value</code>.
   */
//  private boolean internal_checkRequestProperty (String key, String value) {
//    return ((String) requestHeaders.get(new Attributes.Name(key))).equalsIgnoreCase(value);  
//  }

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

    return requestLine.toString();
  }

  /**
   ** Marshal the request headers into a String.
   ** If a <code>connection</code> request header is present, it is ignored.
   ** We always add two headers:
   ** <li>
   ** <li><code>Connection: close</code>
   ** <li><code>Date: </code><i>current date and time</i>
   ** </ul>
   ** If a proxy is being used and proxyUser is non-empty, we also add 
   ** a proxy authentication header.
   */
  private String getRequestHeaders() throws UnknownHostException {
    addProxyAuthenticationHeader();	
    StringBuffer request = new StringBuffer(1024);
    request.append("Connection: close\r\n");
    request.append("Date: ");
    dateFormatter.format(new Date(), request, new java.text.FieldPosition(0));
    request.append("\r\n");
    Iterator it = requestHeaders.entrySet().iterator();
    boolean skip;
    while(it.hasNext()){
      Map.Entry entry = (Map.Entry)it.next();
      Object key = entry.getKey();
      try {
        skip = ((String)key).equalsIgnoreCase("connection");
      }
      catch (ClassCastException cce) {
        skip = false;
      }
      if (!skip) {
        request.append(key);
        request.append(": ");
        request.append(entry.getValue());
        request.append("\r\n");
      }
    }
	
    request.append("\r\n");

    return request.toString();
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
    request.append("Date: ");
    dateFormatter.format(new Date(), request, new java.text.FieldPosition(0));
    request.append("\r\n");
    Iterator it = requestHeaders.entrySet().iterator();
    while(it.hasNext()){
      Map.Entry entry = (Map.Entry)it.next();
      request.append(normaliseName(entry.getKey().toString()));
      request.append(": ");
      request.append(entry.getValue());
      request.append("\r\n");
    }
	
    int length = request.length();
    char[] chars = new char[length];
    request.getChars(0,length,chars,0);

    out.write(decoder.cToB(chars,0,length));
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
    int space2 = -1;

    if (space1 >= 0) {
      space2 = challenge.indexOf(' ', space1 + 1);
      if (space2 < 0) {
        space2 = challenge.length();
      }
    }
    if (space2 >= 0) {
      scheme = challenge.substring(space1 + 1, space2);
      String rest = challenge.substring(space2 + 1);
      //challenge = challenge.substring(0, space2);
      int realmstart = rest.indexOf("realm=\"");
      int realmend = (realmstart < 0) ? -1 : rest.indexOf("\"", realmstart + 7);
      if (realmend >= 0) {
        prompt += " for realm " + rest.substring(realmstart + 6, realmend + 1);
      }
    }

    PasswordAuthentication passwordAuth = Authenticator.requestPasswordAuthentication(addr, port, "HTTP", prompt, scheme);

    if (passwordAuth!=null) {		
      proxyUser = passwordAuth.getUserName();
      proxyPassword = new String(passwordAuth.getPassword());	

      return true;
    }

    return false;
  }

  /**
   ** Perform a GET-style request, i.e. one with no body. We send all the 
   ** headers except Content-Length.
   */
  private void requestGET() throws IOException {
    OutputStream out = socket.getOutputStream();
    requestHeaders.remove(new Attributes.Name("content-length"));
    out.write(getRequestLine().getBytes());
    out.write(getRequestHeaders().getBytes());
    doOutput = false;
    //parseResponse();
  }

  /**
   ** Parse an HTTP response into a response code, a response message (if any),
   ** and a set of response headers.
   */
  synchronized void parseResponse() throws IOException {
    if (responseParsed) {

        return;

    }

    if (!connected) {
      connect();
    }

    if (out != null) {
      try {
        out.flush_internal();
      }
      catch (IOException ioe) {
        // Ignore - probably stream was already closed
      }
    }

    try {
      if(probeStatusLine()){
        String line = readLine(false);
        int space1 = line.indexOf(' ');
        if (space1 >= 0) {
          //String version = line.substring(0,space1);
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
        }
      }

    }
    catch(RuntimeException rt){
      rt.printStackTrace();
      throw new IOException();
    }

    if(internal_checkResponseProperty("Transfer-encoding", "chunked")){
      in = new ChunkedInputStream(in);
    }
	
    if(responseCode>=100 && responseCode<200) {
        // Continue recognized. Parse next header
        parseResponse();
    }
    else if (((responseCode==HTTP_MOVED_PERM) || (responseCode==HTTP_MOVED_TEMP) || (responseCode==HTTP_SEE_OTHER)) && instanceFollowRedirects) {
		
		String location = internal_getResponseProperty("location").trim();
		if(location==null) {
			throw new IOException("HTTP redirect (" + responseCode + ") has no 'Location' header.");
		}
		
		this.url = new URL(location);

		disconnect();
		//connect();
                parseResponse();
    }
    else if(responseCode==HTTP_UNAUTHORIZED) {
      String challenge = internal_getResponseProperty("www-authenticate").trim();
      if (getAuthorisation(challenge, url.toString(), hostAddr, url.getPort())) {

	disconnect();
	//connect();
        parseResponse();
      }
      else {
        responseParsed = true;
        throw new IOException(url + " requires authorisation, but we have no credentials");
      }
    }
    else if(responseCode == HTTP_PROXY_AUTH && usingProxy()) {
      String challenge = internal_getResponseProperty("proxy-authenticate").trim();
      if (getAuthorisation(challenge, "Proxy", getProxyAddr(), getProxyPort())) {

	disconnect();
	//connect();
        parseResponse();
      }
      else {
        responseParsed = true;
        throw new IOException("Proxy server requested authentication, but we have no credentials");
      }
    }
    else if((responseCode<200) || (responseCode>=400)) {
      responseParsed = true;
      throw new IOException("Server returned HTTP response code " + responseCode + " for " + method + " to " + url);
    }		
    else {
      responseParsed = true;
    }
  }

  /**
  ** Check if the first bytes begin with "HTTP/", otherwise we are dealing
  ** with an "HTTP/0.9" response. Needs <var>in</var> to support <code>mark()/reset()</code>.
  ** @return	true if this is a HTTP/1.0 or HTTP/1.1 response, false otherwise.
  */
  private boolean probeStatusLine() throws IOException {
    byte[] bytes = new byte[8];
    in.mark(8);
    int len = in.read(bytes);
    in.reset();
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
    int ch = in.read();
    while(ch != -1){
      boolean stop = false;
      if(ch == '\r'){
        stop = true;
        in.mark(1);
        ch = in.read();
      }
      if(ch == '\n'){
        if(buf.length() == 0){
          break;
        }
        stop = true;
        in.mark(1);
        ch = in.read();
      }
      if(stop){
        if(continued && (ch == ' ' || ch  == '\t')){
          continue;
        }
        else {
          in.reset();
          break;
        }
      }
      buf.append((char)ch);
      ch = in.read();
    }

    return buf.toString();
  }
}
