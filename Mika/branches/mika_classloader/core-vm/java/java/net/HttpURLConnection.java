/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
   	return new SocketPermission(url.getHost()+":"+(url.getPort()== -1 ? 80 : url.getPort()) , "connect");
  }

  /**
  ** @status implemented
  ** @remark Note that for this method to work, subclasses must override getHeaderField()
  ** 	and set the field responseCode;
  */
  public int getResponseCode() throws IOException {
	  if(!connected){
	    connect();
	  }
	  return responseCode;
  }

  /**
  ** @status implemented
  ** @remark Note that for this method to work, subclasses must override getHeaderField()
  ** 	and set the field responsemessage;
  */
  public String getResponseMessage() throws IOException {
	  if(!connected){
	    connect();
	  }
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
