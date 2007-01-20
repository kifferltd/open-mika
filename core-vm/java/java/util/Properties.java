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


/*
** $Id: Properties.java,v 1.4 2006/04/18 11:35:28 cvs Exp $
**
*/
package java.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

public class Properties extends Hashtable {

  private static final String keyValueSeparators = "= :\t\r\n\f";
  private static final String commentChars = "#!";
  private static final long serialVersionUID = 4112578634029874840L;

  protected Properties defaults;

  public Properties() {
    this(null);
  }

  public Properties(Properties deflts) {
    this.defaults = deflts;
  }

  public String getProperty(String key) {
    String val = (String) get(key);
    if (val == null && defaults != null){
      val = defaults.getProperty(key);
    }
    return val;
  }

  public String getProperty(String key, String defaultValue){
    String val = getProperty(key);
    return (val == null) ? defaultValue : val;
  }

  public Object setProperty(String key, String value){
    return put(key, value);
  }

  /**
   * Copy all key/value pairs in a new hashtable merged with the
   * default values if any.
   */
  private Hashtable flatten() {
    Hashtable h;

    if (defaults==null){
      // Look out - this has to work with NativeProperties, which doesn't
      // implement many methods of Hashtable. Change at your peril!
      h = new Hashtable();
      Object k;
      Object v;
      Enumeration e = keys();

      while (e.hasMoreElements()) {
        k = e.nextElement();
        v = get(k);
        h.put(k, v);
      }
    }
    else {
      h = defaults.flatten();
      h.putAll(this);
    }
    return h;
  }

  public Enumeration propertyNames() {
    return this.flatten().keys();
  }


  /**
   * Load from an input stream.
   */
  public void load(InputStream in) throws IOException {
    int size = 1024;
    byte[] bytes = new byte[size];
    char[] string = new char[size];
    int stringSize = size;
    int have=0;
    int idx=1;
    boolean eol = false;
    boolean separated= false;
    boolean trim = true;

    do {
      //skip all whitespaces
      int ch;
      do {
        if (idx >= have) {
          have = in.read(bytes, 0, size);
          if (have == -1) {
            return;
          }
          idx = 0;
        }
        ch = bytes[idx];
        if (ch < 0 || ch > 32){
          break;
        }
        idx++;
      } while (true);

      //check for comment line ...
      if (idx >= have) {
        have = in.read(bytes, 0, size);
        if (have == -1) {
          return;
        }
        idx = 0;
      }

      boolean nocomment = commentChars.indexOf(bytes[idx]) == -1;
      //start parsing the key ...
      int chars = 0;
      do {
        if (idx >= have) {
          have = in.read(bytes, 0, size);
          if (have == -1) {
            if (chars > 0 && nocomment) {
              put(new String(string, 0, chars),"");
            }
            return;
          }
          idx = 0;
        }
        ch = bytes[idx++];
        if (chars >= stringSize) {
          string = growByteArray(string);
          stringSize = string.length;
        }
        if (ch == '\\') {
          if (idx >= have) {
            have = in.read(bytes, 0, size);
            if (have == -1) {
              if (chars > 0 && nocomment) {
                string[chars++] = (char) (ch & 0xff);
                put(new String(string, 0, chars),"");
              }
              return;
            }
            idx = 0;
          }
          ch = bytes[idx++];
          switch(ch){
            case 'n':
              string[chars++] = '\n';
              break;
            case 'r':
              string[chars++] = '\r';
              break;
            case 't':
              string[chars++] = '\t';
              break;
            case 'f':
              string[chars++] = '\f';
              break;
            case '\r':
              if (idx >= have) {
                have = in.read(bytes, 0, size);
                if (have == -1) {
                  if (chars > 0 && nocomment) {
                    string[chars++] = (char) (ch & 0xff);
                    put(new String(string, 0, chars),"");
                  }
                  return;
                }
                idx = 0;
              }
              if (bytes[idx] == '\n'){
                idx++;
              }
            case '\n':
              //continuation.	     
              break;
            case 'u':
	      int unicode = 0;
	      for(int k = 0; k < 4; k++){
		if (idx >= have) {
                  have = in.read(bytes, 0, size);
		  if (have == -1) {
		    if (chars > 0 && nocomment) {
		      string[chars++] = (char) (ch & 0xff);
		      put(new String(string, 0, chars),"");
		    }
		    return;
		  }
		  idx = 0;
		}
                unicode = unicode<<4 | fromHex(bytes[idx++]);
	      }  
	      string[chars++] = (char)unicode;
	      break;
	    default:
              string[chars++] = (char)(ch & 0xff);
              break;
          }
        } else if (keyValueSeparators.indexOf(ch) != -1) {
           eol = (ch == '\n' || ch == '\r');
           separated = (ch == '=' || ch == ':');
           break;
        }
        else {
          string[chars++] = (char) (ch & 0xff);
        }
      } while(true);

      String key = new String(string, 0, chars);

      if(eol) {
        if(nocomment) {
          put(key,"");
        }
        eol = false;
        continue;
      }
      //strip all spaces before and/or after the separator
      if (!separated) {
        //no separator found yet
        separated = false;
        //trim all spaces and tabs
        do {
          if (idx >= have) {
            have = in.read(bytes, 0, size);
            if (have == -1) {
              if(nocomment) {
                put(new String(string, 0, chars),"");
              }
              return;
            }
            idx = 0;
          }
          ch = bytes[idx];
          if (ch != ' ' && ch != '\t' && ch != '\f'){
            break;
          }
          idx++;
        } while (true);
        //check if char is separator
        if(ch != ':' && ch != '='){
          trim = false;
        }
        else {
          idx++;
        }
      }
      if (trim) {
        do {
          if (idx >= have) {
            have = in.read(bytes, 0, size);
            if (have == -1) {
              if(nocomment) {
                put(new String(string, 0, chars),"");
              }
              return;
            }
            idx = 0;
          }
          ch = bytes[idx];
          if (ch != ' ' && ch != '\t' && ch != '\f'){
            break;
          }
          idx++;
        } while (true);
      }
      else {
        trim = true;
      }
      //start parsing the value ...
      chars = 0;
      do {
        if (idx >= have) {
          have = in.read(bytes, 0, size);
          if (have == -1) {
            if (nocomment) {
              put(key, new String(string, 0, chars));
            }
            return;
          }
          idx = 0;
        }
        ch = bytes[idx++];
        if (chars >= stringSize) {
          string = growByteArray(string);
          stringSize = string.length;
        }
        if (ch == '\\') {
          if (idx >= have) {
            have = in.read(bytes, 0, size);
            if (have == -1) {
              if (nocomment) {
                string[chars++] = (char) (ch & 0xff);
                put(key, new String(string, 0, chars));
              }
              return;
            }
            idx = 0;
          }
          ch = bytes[idx++];
          switch(ch){
            case 'n':
              string[chars++] = '\n';
              break;
            case 'r':
              string[chars++] = '\r';
              break;
            case 't':
              string[chars++] = '\t';
              break;
            case 'f':
              string[chars++] = '\f';
              break;
            case '\r':
              if (idx >= have) {
                have = in.read(bytes, 0, size);
                if (have == -1) {
                  if (chars > 0 && nocomment) {
                    string[chars++] = (char) (ch & 0xff);
                    put(key, new String(string, 0, chars));
                  }
                  return;
                }
                idx = 0;
              }
              if (bytes[idx] == '\n'){
                idx++;
              }
            case '\n':
              //continuation.             
	      break;
	    case 'u':
	      int unicode = 0;
	      for(int k = 0; k < 4; k++){
                if (idx >= have) {
                  have = in.read(bytes, 0, size);
                  if (have == -1) {
                    if (chars > 0 && nocomment) {
                      string[chars++] = (char) (ch & 0xff);
                      put(key, new String(string, 0, chars));
                    }
                    return;
                  }
                  idx = 0;
                }
                unicode = unicode<<4 | fromHex(bytes[idx++]);
	      } 
	      string[chars++] = (char)unicode;	      
	      break;
            default:
              string[chars++] = (char)(ch & 0xff);
              break;
          }
        }
        else if (ch == '\n') {
          if(nocomment) {
            put(key, new String(string, 0, chars));
          }
          break;
        }
        else if (ch == '\r') {
          if(nocomment) {
            put(key, new String(string, 0, chars));
          }
          if (idx >= have) {
            have = in.read(bytes, 0, size);
            if (have == -1) {
              return;
            }
            idx = 0;
            if (bytes[idx] == '\n'){
              idx++;
            }
          }
          break;
        }
        else {
          string[chars++] = (char) (ch & 0xff);
        }
      } while(true);
    } while (true);
  }

  private char[] growByteArray(char[] bytes) {
    int l = bytes.length;
    char[] newBytes = new char[l*2];
    System.arraycopy(bytes,0,newBytes,0,l);
    return newBytes;
  }

  public synchronized void save(OutputStream out, String header) {
    try {
      store(out, header);
    }catch(IOException ioe){}
  }

  public synchronized void store(OutputStream out, String header) throws IOException {
    //BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(out));
    byte[] bytes = new byte[1024];
    int count = 0;
    if(header != null) {
      bytes[0] = (byte)'#';
      out.write(bytes,0, addSlashes(header, out, bytes, '\n', 1));
    }
    try {
      bytes[0] = (byte)'#';
      String date = new Date().toString();
      int l = date.length();
      date.getBytes(0, l, bytes, 1);
      bytes[l+1] = (byte)'\n';
      out.write(bytes,0, l+2);
    }
    catch (RuntimeException rt) {}

    Enumeration en = keys();
    try {
      do {
        String key = (String) en.nextElement();
        count = addSlashes(key,out, bytes, '=', count);
        count = addSlashes((String)get(key),out, bytes, '\n', count);
      } while (true);
    }
    catch (RuntimeException rt) {}
    out.write(bytes, 0, count);
  }

  private int addSlashes(String key, OutputStream out, byte[] bytes, int ch, int idx) throws IOException {
    int len = key.length();
    char[] chars = new char[len];
    key.getChars(0,len, chars,0);

    for(int i = 0 ; i < len ; i++){
      int c = chars[i];
      switch(c) {
        case '\t':
          bytes[idx++] = (byte)'\\';
          bytes[idx++] = (byte)'t';
          break;
        case '\r':
          bytes[idx++] = (byte)'\\';
          bytes[idx++] = (byte)'r';
          break;
        case '\n':
          bytes[idx++] = (byte)'\\';
          bytes[idx++] = (byte)'n';
          break;
        case '\f':
          bytes[idx++] = (byte)'\\';
          bytes[idx++] = (byte)'f';
          break;
        case '=':
        case ' ':
        case ':':
        case '\\':
        case '#':
        case '!':
          bytes[idx++] = (byte)'\\';
        default:
          if(c < 0x20 || c > 0x7e) {
            bytes[idx++] = (byte)'\\';
	    bytes[idx++] = (byte)'u';
	    bytes[idx++] = toHex(c>>12); 
	    bytes[idx++] = toHex(c>>8);	  
	    bytes[idx++] = toHex(c>>4);	  
	    bytes[idx++] = toHex(c);	  
	  }
	  else {
	    bytes[idx++] = (byte)c;
	  }
          break;
      }
      if (idx >= 1018) {
        out.write(bytes,0,idx);
        idx = 0;
      }
    }
    bytes[idx++] = (byte)ch;
    return idx;
  }

  private int fromHex(int b) {
    if(b >= '0' && b <= '9') {
      return b - '0'; 
    }
    if(b >= 'a' && b <= 'f') {
      return 10 + b - 'a';
    }    
    if(b >= 'A' && b <= 'F') {
      return 10 + b - 'A';
    }    
    return 0;
  }
  
  private byte toHex(int i) {
    int hex = '0' + (0x0f & i);	  
    if(hex > '9') {
      hex = ('A' - '9' - 1) + hex;    
    }
    return (byte) hex;
  }
  
  public void list(PrintStream out) throws NullPointerException {
    out.println("-- listing properties --");
    Hashtable h = flatten();
    String key;
    String value;
    Enumeration en = h.keys();
    while(en.hasMoreElements()) {
      Object o = en.nextElement();
      key = (String)o;
      value = (String) h.get(key);
      if( value.length() > 40 ){
  	value = value.substring(0,37)+"...";
      }
      out.println(key + "=" + value);
    }
  }

  public void list(PrintWriter out) throws NullPointerException  {
    Hashtable h = flatten();
    String key;
    String value;
    Enumeration en = h.keys();
    while(en.hasMoreElements()) {
      key = (String) en.nextElement();
      value = (String) h.get(key);
      if( value.length() > 40 ){
  	  value = value.substring(0,37)+"...";
      }
      out.println(key + "=" + value);
    }
  }
}
