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

import java.io.UnsupportedEncodingException;

public class URLDecoder {

  private static final String hex="0123456789abcdef";
  private static final String HEX="0123456789ABCDEF";

  public static String decode(String str) {
    int len = str.length();
    char c;
    // by specifying len we make sure that the StringBuffer doesn't need to grow
    // if not to many cases of '%xx' occur we will not waste to much memory
    StringBuffer buf = new StringBuffer(len);
    for (int i = 0 ; i < len ;) {
      c = str.charAt(i++);
      if (c == '+') {
        buf.append(' ');
      }
      else {
        if (c == '%') {
          if (i+2 >= len) {
            throw new IllegalArgumentException("bad syntax "+str);
          }
          c = str.charAt(i++);
          int ch = hex.indexOf(c);
          if(ch == -1) {
            ch = HEX.indexOf(c);
          }
          ch *= 16;	 	 	
          c = str.charAt(i++);
          int hlp = hex.indexOf(c);
          if(hlp == -1){
            hlp = HEX.indexOf(c);
          }
          if (ch < 0 || hlp == -1) {
            throw new IllegalArgumentException("bad syntax "+str);
          }
          buf.append((char)(hlp+ch));
        }
        else {
          buf.append(c);
        }
      }
    }
    return buf.toString();
  }

  public static String decode(String str, String enc) throws UnsupportedEncodingException {
    int len = str.length();
    StringBuffer buf = new StringBuffer(len);
    for (int i = 0; i < len;) {
      char c = str.charAt(i++);
      if (c == '+') {
        buf.append(' ');
      } else {
        if (c == '%') {
          int pos = buf.length();
          while (c == '%') {
            if (i + 2 >= len) {
              throw new IllegalArgumentException("bad syntax " + str);
            }
            c = str.charAt(i++);
            int ch = hex.indexOf(c);
            if (ch == -1) {
              ch = HEX.indexOf(c);
            }
            ch *= 16;
            c = str.charAt(i++);
            int hlp = hex.indexOf(c);
            if (hlp == -1) {
              hlp = HEX.indexOf(c);
            }
            if (ch < 0 || hlp == -1) {
              throw new IllegalArgumentException("bad syntax " + str);
            }
            buf.append((char) (hlp + ch));
          }
          if(pos < buf.length()) {
            int l = buf.length();
            byte[] bytes = new byte[l-pos];
            for(int j=0; j < bytes.length ; j++) {
              bytes[j] = (byte) buf.charAt(pos+j);
            }
            buf.setLength(pos);
            buf.append(new String(bytes,enc));
          }
        } else {
          buf.append(c);
        }
      }
    }
    return buf.toString();
  }
}
