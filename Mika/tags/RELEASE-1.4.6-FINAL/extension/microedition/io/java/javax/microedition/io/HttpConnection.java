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

package javax.microedition.io;

import java.io.IOException;

public interface HttpConnection extends ContentConnection {

  public final static String GET = "GET";
  public final static String HEAD = "HEAD";
  public final static String POST = "POST";

  public final static int HTTP_ACCEPTED = 202;
  public final static int HTTP_BAD_GATEWAY = 502;
  public final static int HTTP_BAD_METHOD = 405;
  public final static int HTTP_BAD_REQUEST = 400;
  public final static int HTTP_CLIENT_TIMEOUT = 408;
  public final static int HTTP_CONFLICT = 409;
  public final static int HTTP_CREATED = 201;
  public final static int HTTP_ENTITY_TOO_LARGE = 413;
  public final static int HTTP_EXPECT_FAILED = 417;
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
  public final static int HTTP_TEMP_REDIRECT = 307;
  public final static int HTTP_UNAUTHORIZED = 401;
  public final static int HTTP_UNAVAILABLE = 503;
  public final static int HTTP_UNSUPPORTED_RANGE = 416;
  public final static int HTTP_UNSUPPORTED_TYPE = 415;
  public final static int HTTP_USE_PROXY = 305;
  public final static int HTTP_VERSION = 505;

  public long getDate() throws IOException;
  public long getExpiration() throws IOException;
  public String getFile();
  public String getHeaderField(int idx) throws IOException;
  public String getHeaderField(String name) throws IOException;
  public long getHeaderFieldDate(String field, long def) throws IOException;
  public int getHeaderFieldInt(String field, int def) throws IOException;
  public String getHeaderFieldKey(int field) throws IOException;
  public String getHost();
  public long getLastModified() throws IOException;
  public int getPort();
  public String getProtocol();
  public String getQuery();
  public String getRef();
  public String getRequestMethod();
  public String getRequestProperty(String key);
  public int getResponseCode() throws IOException;
  public String getResponseMessage() throws IOException;
  public String getURL();
  public void setRequestMethod(String method) throws IOException;
  public void setRequestProperty(String key, String value) throws IOException;

}

