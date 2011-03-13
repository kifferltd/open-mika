/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2011 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
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

package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;

public abstract class HttpURLConnection extends URLConnection {

  protected String method=GET;
  protected int responseCode=-1;
  protected String responseMessage;
  protected boolean instanceFollowRedirects = followRed;

  private static final String DELETE="DELETE";
  private static final String GET="GET";
  private static final String HEAD="HEAD";
  private static final String OPTIONS="OPTIONS";
  private static final String POST="POST";
  private static final String PUT="PUT";
  private static final String TRACE="TRACE";

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


  private static boolean followRed=true;

  /**
  ** @status implemented
  */
  protected HttpURLConnection(URL url) {
     	super(url);
  }

  // abstract methods ...

  public abstract void disconnect();

  public abstract boolean usingProxy();


  // non abstract methods ...

  public String getRequestMethod() {
   	return method;
  }

  /**
  **
  ** @remark mtoken should be a non-null String (a null String will result in a NullPointerException)
  */
  public void setRequestMethod(String mtoken) throws ProtocolException{
   	if (!mtoken.equals(DELETE) && !mtoken.equals(GET)
   	 	&& !mtoken.equals(HEAD) && !mtoken.equals(OPTIONS)
   	 	&& !mtoken.equals(POST) && !mtoken.equals(PUT)
   	   	&& !mtoken.equals(TRACE)) {
   	 	throw new ProtocolException();	  	
   	}   	
   	method = mtoken;
  }

  /**
  ** @status implemented
  ** @remark returns null --> should be overriden by subclasses
  */
  public InputStream getErrorStream() {
   	return null;
  }

  /**
  ** @status implemented
  */
  public Permission getPermission() throws IOException {   	
    int port = url.getPort();
    if (port < 0) {
      port = 80;
    }

    return new SocketPermission(url.getHost() + ":" + port , "connect,resolve");
  }

  /**
  ** @status implemented
  ** @remark Note that for this method to work, subclasses must override getHeaderField()
  ** 	and set the field responseCode;
  */
  public int getResponseCode() throws IOException {
    connect();
    return responseCode;
  }

  /**
  ** @status implemented
  ** @remark Note that for this method to work, subclasses must override getHeaderField()
  ** 	and set the field responsemessage;
  */
  public String getResponseMessage() throws IOException {
    connect();
    return responseMessage;
  }

  // static methods ...

  public static boolean getFollowRedirects() {
    return followRed;
  }
  public static void setFollowRedirects(boolean folR) {
    followRed = folR;
  }

  public void setInstanceFollowRedirects(boolean folR) {
    	instanceFollowRedirects = folR;
  }

  public boolean getInstanceFollowRedirects(){
    	return instanceFollowRedirects;
  }

}
