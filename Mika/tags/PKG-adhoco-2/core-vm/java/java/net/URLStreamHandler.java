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

public abstract class URLStreamHandler {

  protected boolean equals(URL url1, URL url2){
    return url1.getProtocol().equals(url2.getProtocol())
        && url1.getPort() == url2.getPort()
        && url1.getHost().equals(url2.getHost())
        && url1.getFile().equals(url2.getFile())
        && (url1.getRef() == null ? url2.getRef() == null : url1.getRef().equals(url2.getRef()));
  }

/**
** the default implementation returns -1. should be overriden by subclasses ...
*/
  protected int getDefaultPort(){
    return -1;
  }

  protected int hashCode(URL url){
    int hashCode = url.getFile().hashCode() ^ url.getHost().hashCode() ^ url.getProtocol().hashCode();
    hashCode = hashCode ^ url.getPort();
    String ref = url.getRef();
    if(ref != null){
      hashCode ^= ref.hashCode();
    }
    return hashCode;
  }

  protected InetAddress getHostAddress(URL url){
    try {
      return InetAddress.getByName(url.getHost());
    }
    catch(UnknownHostException uhe){
      return null;
    }
  }

  protected boolean hostsEqual(URL url1, URL url2) {
    String host1 = url1.getHost();
    String host2 = url2.getHost();
    try {
      return  InetAddress.getByName(host1).equals(InetAddress.getByName(host2));
    } catch (UnknownHostException e) {
      return host1.equals(host2);
    }
  }

  protected abstract URLConnection openConnection(URL url) throws IOException ;
  
/**
**  The default implementation of parseURL parses an "http-like" URL:
**  <ul><li>If the first two characters are `//', then everything up to
**          the next `/' is treated as `host & port'.
**      <li>If present, `host and port' consist of either host:port or
**          just `host', where `port' is a number and `host' is a string.
**      <li>Everything after the last `?' is a query string.
**      <li>The rest is `the path'.
**  </ul>
**
**  @param start points to the character after the colon (':') indicating the 
**  end of the protocol
**  @param end   points to the character after the end of the URL spec, or to the '#' character if one was present.
**  @status implemented
**  @remark uses package protected method to update the url ...
*/  
  protected void parseURL(URL url, String spec, int start, int end) {
    int here = start;
    int there = end;

    try {
      if (end > start + 2 && spec.regionMatches(start, "//", 0, 2)) {
        int third_slash = spec.indexOf("/", start + 2);
        if (third_slash < 0) {
          third_slash = end;
        }
        String host_and_port = spec.substring(start+2, third_slash);
        int colon = host_and_port.lastIndexOf(':');
        if (colon <= host_and_port.indexOf(']')) {
          url.setHost(host_and_port);
          url.setPort(getDefaultPort());
        }
        else {
          url.setHost(host_and_port.substring(0, colon));
          url.setPort(Integer.parseInt(host_and_port.substring(colon + 1)));
        }

        here = third_slash;
      }

      int query = spec.lastIndexOf("?");
      if (query >= here) {
        url.setQuery(spec.substring(query + 1, end));
        there = query;
      }

      url.setFile(spec.substring(here, there));
    }
    catch(Exception e){ /* all exceptions cause a silent return */ 
    }
  }

  protected boolean sameFile(URL url1, URL url2){
     return url1.getProtocol().equals(url2.getProtocol())
         && url1.getPort() == url2.getPort()
         && url1.getHost().equals(url2.getHost())
         && url1.getFile().equals(url2.getFile());
  }

  protected void setURL(URL url, String protocol, String host, int port, String file, String ref) {
     url.set(protocol, host, port, file, ref);
  }

  protected  void setURL(URL url, String protocol, String host, int port, String authority,
                         String userInfo, String path, String query, String ref){

     url.set(protocol, host, port, authority, userInfo, path, query, ref);
  }

 /**
  ** Reverse the parsing of a URL, i.e. recreate the string from which it was
  ** created.  Again, the default implementation assumes a "http-like" syntax..
  */
  protected String toExternalForm(URL url) {
    StringBuffer buf = new StringBuffer(url.getProtocol());
    buf.append("://");
    buf.append(url.getHost());
    if (url.getPort() != -1 && url.getPort() != getDefaultPort()) {
      buf.append(':');
      buf.append(url.getPort());
    }
    //the slash is already in the file ...
    //buf.append('/');
    buf.append(url.getFile());

    // -> This is done by getFile()
    //String query = url.getQuery();
    //if (query != null) {
    //  buf.append('?');
    //  buf.append(query);
    //}

    String ref = url.getRef();
    if (ref != null) {
      buf.append('#');
      buf.append(ref);
    }
    return buf.toString();
  }
}
