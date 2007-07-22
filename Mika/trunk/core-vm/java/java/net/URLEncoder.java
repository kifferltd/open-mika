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

public class URLEncoder {

  private URLEncoder() {}
  
  private static final String allowed = "-_.*";
  private static final String hex="0123456789abcdef";
	
	public static String encode(String str){
		int len = str.length();
		// we take a guess that we will encounter a maximum of 10 chars to be replaced by '%xx'
		// if more of such cases occur, the StringBuffer wil need to grow ...
		StringBuffer buf = new StringBuffer(len+10);
		char c;
		for (int i = 0 ; i < len ; i++) {
		     c = str.charAt(i);
		     if (Character.isLetterOrDigit(c) || allowed.indexOf(c) != -1) {
		 	buf.append(c);
		     }
		     else {
		 	if (c == ' ') {
		 	 	buf.append('+');
		 	}		 	
		 	else {
		 	 	buf.append('%');
		 	 	buf.append(hex.charAt((c&0xff)/16));	
		 	 	buf.append(hex.charAt(c%16));	
		 	}
		     }
		}
		return buf.toString();		
	}	

  public static String encode(String str, String enc) throws UnsupportedEncodingException {
    int len = str.length();
    // we take a guess that we will encounter a maximum of 10 chars to be replaced by '%xx'
    // if more of such cases occur, the StringBuffer wil need to grow ...
    StringBuffer buf = new StringBuffer(len + 10);
    for (int i = 0; i < len; i++) {
      char c = str.charAt(i);
      if (Character.isLetterOrDigit(c) || allowed.indexOf(c) != -1) {
        buf.append(c);
      } else {
        if (c == ' ') {
          buf.append('+');
        } else {
          byte[] bytes = new String(new char[]{c}).getBytes(enc);
          for (int j=0; j < bytes.length ; j++) {         
            buf.append('%');
            int b = bytes[j];
            buf.append(hex.charAt((b & 0xff) / 16));
            buf.append(hex.charAt(b % 16));
          }
        }
      }
    }
    return buf.toString();
  } 
}
