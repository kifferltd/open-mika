/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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
**          the next `/' is treated as `credentials, host and port'.
**      <li>If present, `userinfo, host and port' consists of either 
**          `userinfo'@`host and port' or just `host and port'.
**      <li>If present, `host and port' consists of either host:port or
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
      StringBuffer spec_sb = new StringBuffer(spec.substring(start + 2, end));
      if (spec_sb.length() > 0 && spec.regionMatches(start, "//", 0, 2)) {
        int third_slash = spec_sb.indexOf("/");
        if (third_slash < 0) {
          third_slash = spec_sb.length();
        }
        url.setAuthority(spec_sb.substring(0, third_slash));
        int ape = spec_sb.indexOf("@");
        if (ape >= 0 && ape < third_slash) {
          String user_info = spec_sb.substring(0, ape);
          url.setUserInfo(user_info);
        }
        String host_and_port = spec_sb.substring(ape + 1, third_slash);
        int colon = host_and_port.lastIndexOf(':');
        if (colon <= host_and_port.indexOf(']')) {
          url.setHost(host_and_port);
        }
        else {
          url.setHost(host_and_port.substring(0, colon));
          url.setPort(Integer.parseInt(host_and_port.substring(colon + 1)));
        }

        here = start + 2 + third_slash;
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
     int port1 = url1.getPort();
     int port2 = url2.getPort();

     if (port1 == -1) {
       port1 = getDefaultPort();
     }
     if (port2 == -1) {
       port2 = getDefaultPort();
     }

     return url1.getProtocol().equals(url2.getProtocol())
         && url1.getHost().equals(url2.getHost())
         && port1 == port2
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
    buf.append(":");
    if (url.getAuthority() != null) {
        buf.append("//");
        buf.append(url.getAuthority());
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
