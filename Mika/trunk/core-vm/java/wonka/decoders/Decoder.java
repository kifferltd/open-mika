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
** $Id: Decoder.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package wonka.decoders;

import java.io.*;

public abstract class Decoder {

  private static final String[] names;
  private static final Object[] decoders;
  public static final Decoder DEFAULT;

  static {
    decoders = new Object[37];
    names = new String[37];

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

    put("UTF-16BE", "UTF16BeDecoder");

    put("UTF-16LE", "UTF16LeDecoder");

    Decoder d = new Latin1Decoder();
    DEFAULT = d;
    put("ISO8859_1", d);
    put("8859_1", d);
    put("ISO8859-1", d);
    put("ISO-8859-1", d);
    put("ISO-8859-15", d);
    put("ISO_8859-1", d);
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
    int hash = coding.hashCode() % 37;
    String[] k = names;
    String k2;

    do {
      if(hash < 0) hash += 37;

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
    int hash = key.hashCode() % 37;
    String[] k = names;
    String k2;

    do {
      if(hash < 0) hash += 37;
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
