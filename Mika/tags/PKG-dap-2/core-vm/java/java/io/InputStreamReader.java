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
** $Id: InputStreamReader.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

import wonka.decoders.Decoder;

/**
 ** InputStreamReader turns an underlying byte stream into a stream of Unicode characters.
 **
 ** <p>This implementation directly supports the six encodings which are
 ** mandatory for all Java implementations:
 ** <dl>
 ** <dt>US-ASCII
 ** <dd>Each byte ?bbbbbbb is translated to a character 000000000bbbbbbb.
 ** <dt>ISO-8859-1 (Latin-1)
 ** <dd>Each byte bbbbbbbb is translated to a character 00000000bbbbbbbb.
 ** <dt>UTF-8
 ** <dd>A single byte 0bbbbbbb is translated to a character 000000000bbbbbbb.
 ** A byte pair 110xxxxx 10yyyyyy is translated to a character 00000xxxxxyyyyyy.
 ** A byte triple 1110xxxx 10yyyyyy 10zzzzzz is translated to a character xxxxyyyyyyzzzzzz.
 ** <p>Any byte that does not fit into this scheme will result in a UTFDataFormatException.
 ** <dt>UTF-16
 ** <dd>The first two bytes must be a Byte Ordering Mark, else an exception is thrown.
 ** If the first two bytes are FE FF, then the remainder of the stream is treated
 ** as being big-endian (see below): if the first two bytes are FF FE then it is
 ** treated as little-endian.  Any other values for the first two bytes result in
 ** a UTFDataFormatException.  The BOM itself is not passed on.
 ** <dt>UTF-16BE
 ** <dd>Each pair of bytes xxxxxxxx yyyyyyyy is translated to a character xxxxxxxxyyyyyyyy.
 ** <dt>UTF-16LE
 ** <dd>Each pair of bytes xxxxxxxx yyyyyyyy is translated to a character yyyyyyyyxxxxxxxx.

 ** </dl>
 **
 ** <p>By supporting these formats directly we avoid some bootstrapping issues.
 ** However, if support is required for other encodings such as SJIS then we
 ** should come up with a scheme using reflection to delegate to a subclass.
 **
 ** <p>The implementation of read([BII) for UTF encodings is not efficient:
 ** a better implementation would take advantage of the fact that the reader
 ** is allowed to read more bytes than are strictly necessary, e.g. by reading
 ** the worst-case number of bytes. FIXME. 
 */
public class InputStreamReader extends Reader {

  private static final String default_encoding = GetSystemProperty.FILE_ENCODING;

  private InputStream in;
  private Decoder decoder;

  public InputStreamReader(InputStream bytestream) {
    super(bytestream);
    decoder = Decoder.getDefault(default_encoding);
    in = bytestream;
  }

  public InputStreamReader(InputStream bytestream, String enc) throws UnsupportedEncodingException {
    super(bytestream);
    decoder = Decoder.get(enc);
    in = bytestream;
  }

  /**
   ** Close the stream, freeing any temporary buffers we may be holding.
   */
  public void close() throws IOException {
    synchronized(lock){
      if(in != null){
        in.close();
        in = null;
      }
    }
  }

  /**
   ** Return the encoding being used, in its canonical form.
   */
  public String getEncoding(){
    return decoder.getEncoding();
  }

  public int read() throws IOException {
    synchronized(lock){
      if(in == null){
        throw new IOException();
      }
      return decoder.getChar(in);
    }
  }

  /**
   ** Read a number of characters from the input stream.  Returns -1 if a read()
   ** on the underlying input stream returned -1 before even one complete character
   ** had been read.  In the case of UTF-8 we may read more bytes than we really
   ** need to (we assume the worst case of 3 bytes per char), which is permitted
   ** by the API.  Any extra bytes will be kept for a subsequent read.
   */
  public int read(char[] buf, int off, int len) throws IOException{
    synchronized(lock){
      if(in == null){
        throw new IOException();
      }
      return decoder.cFromStream(in, buf,off,len);
    }
  }

  /**
   ** Returns true iff there are yet-to-be-processed characters in the
   ** underlying bytestream.
   */
  public boolean ready() throws IOException{
    synchronized(lock){
      if(in == null){
        throw new IOException();
      }
      return in.available() > 0;
    }
  }
}
