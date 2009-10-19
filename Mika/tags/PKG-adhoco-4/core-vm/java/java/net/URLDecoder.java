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
