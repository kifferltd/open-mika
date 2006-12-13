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
