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

/*
** $Id: Decoder.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package wonka.decoders;

import java.io.*;

public abstract class Decoder {

  static final int UNDEFINED = 0;
  static final int BIGEND    = 1;
  static final int LITTLEEND = 2;
  
  
  private static final int HT_SIZE = 47;
  private static final String[] names;
  private static final Object[] decoders;
  public static final Decoder DEFAULT;

  static {
    decoders = new Object[HT_SIZE];
    names = new String[HT_SIZE];

    String s = "ASCIIDecoder";
    put("ASCII", s);
    put("US-ASCII", s);
    put("ISO646-US", s);

    s = "UTF8Decoder";
    put("UTF8", s);
    put("UTF-8", s);

    s = "UTF16Decoder";
    put("UTF16", s);
    put("UTF-16", s);

    put("UNICODEBIGUNMARKED", "UTF16BeDecoder");
    put("UTF-16BE", "UTF16BeDecoder");

    put("UNICODELITTLEUNMARKED", "UTF16LeDecoder");
    put("UTF-16LE", "UTF16LeDecoder");

    put("UNICODELITTLE","UnicodeLittleDecoder");
    put("UNICODEBIG","UnicodeBigDecoder");
    put("UNICODE","UnicodeDecoder");
    
    Decoder d = new Latin1Decoder();
    DEFAULT = d;
    put("ISO8859_1", d);
    put("8859_1", d);
    put("ISO8859-1", d);
    put("ISO-8859-1", d);
    put("ISO-8859-15", d);
    put("ISO_8859-1", d);    
    put("ISO8859-15", d);
    put("ISO8859_15", d);
    put("ISO_8859-1:1978", d);
    put("ISO_8859-1:1987", d);
    put("ISO-IR-100", d);
    put("LATIN1", d);
    put("L1", d);
    put("CSLSOLATIN1", d);
  }

  public static char[] decode(byte[] bytes, String enc) throws UnsupportedEncodingException {
    return get(enc).bToC(bytes,0,bytes.length);
  }

  public static byte[] encode(char[] chars, String enc) throws UnsupportedEncodingException {
    return get(enc).cToB(chars,0,chars.length);
  }

  public static Decoder getDefault(String key){
    try {
      return get(key);
    }
    catch(UnsupportedEncodingException uee){}
    return DEFAULT;
  }

  public static Decoder get(String key) throws UnsupportedEncodingException {
    String coding = key.toUpperCase();
    int hash = coding.hashCode() % HT_SIZE;
    String[] k = names;
    String k2;

    do {
      if(hash < 0) hash += HT_SIZE;

      k2 = k[hash];

      if(k2 == null) {
        throw new UnsupportedEncodingException(key);

      } else if(coding.equals(k2)) {
        Object o = decoders[hash];
        try {
          return ((Decoder)o).getInstance();
        }
        catch(ClassCastException cce){
          try {
            Decoder d = (Decoder)Class.forName("wonka.decoders."+o).newInstance();
            decoders[hash] = d;
            return d.getInstance();
          }
          catch(Exception e){            
            throw new UnsupportedEncodingException("Unable to initiate encoder for "+key);
          }
        }
      }
      hash--;
    } while(true);
  }

  private static void put(String key, Object newvalue) throws NullPointerException {
    int hash = key.hashCode() % HT_SIZE;
    String[] k = names;
    String k2;

    do {
      if(hash < 0) hash += HT_SIZE;
      k2 = k[hash];

      if(k2 == null) {
        k[hash] = key;
        decoders[hash] = newvalue;
        return;
      }
      hash--;
    } while(true);
  }

  public byte[] cToB(StringBuffer buffer){
    int length = buffer.length();
    char[] chars = new char[length];
    buffer.getChars(0,length, chars,0);
    return cToB(chars,0,length);
  }

  public abstract char[] bToC(byte[] bytes, int off, int len);
  public abstract int cFromStream(InputStream in, char[] chars, int off, int len) throws IOException;
  public abstract byte[] cToB(char[] chars, int off, int len);
  public abstract int getChar(InputStream in) throws IOException;
  public abstract String getEncoding();

  /**
  ** some decoders can be shared between multiple objects.  However others like UTF8Decoder need to be able to
  ** save and store data, making it hard to be shared between multiple objects and threads.
  */
  protected Decoder getInstance(){
    return this;
  }

}
