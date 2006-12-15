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
** $Id: CharArrayWriter.java,v 1.2 2006/10/04 14:24:14 cvsroot Exp $
*/

package java.io;

public class CharArrayWriter extends Writer {

  protected char[] buf;
  protected int count=0;
  private static final int defaultsize = 32;

  public CharArrayWriter() {
  	buf = new char[defaultsize];
  }
  
  public CharArrayWriter(int size) {
  	if (size < 0) throw new IllegalArgumentException();
  	buf = new char[size];
  }
  
  public void close() {
  }
  
  public void flush() {
  }
  
  public void reset() {
    count =0;
  }
  
  public int size() {
    return count;
  }
  
  public char[] toCharArray() {
    synchronized (lock) {
	    int c = count;
	    char [] ca = new char[c];
    	System.arraycopy(buf, 0, ca, 0, c);
    	return ca;
    }
  }
  
  public String toString() {
    synchronized (lock) {
     	return new String(buf,0,count);
    }
  }

  public void write(int oneChar) {
    synchronized (lock) {
      int c = count;
    	if (c == buf.length) resize(2*c+1,c);
      	buf[count++]=(char) oneChar;
    }
  }

  public void write(char[] buf, int offset, int count){
    if (offset < 0 || count < 0 || offset > buf.length - count) throw new ArrayIndexOutOfBoundsException();
    synchronized (lock) {
      int c = this.count;
    	if (c+count > this.buf.length) resize(count+c,c);
    	System.arraycopy(buf, offset, this.buf, c, count);
    	this.count += count;
    }
  }

  public void write(String str, int offset, int count) {
    if (offset < 0 || count < 0 || offset > str.length() - count) throw new StringIndexOutOfBoundsException();
    synchronized (lock) {
      int c = this.count;
    	if (c+count > this.buf.length) resize(count+c,c);
    	str.getChars(offset, offset+count, buf, c);
    	this.count += count;
    }
  }

  public void writeTo(Writer out) throws IOException {
    synchronized (lock) {
      	out.write(buf,0,count);
    }
  }
/**
* do not use resize when the object is not locked !!!
*/
  private void resize(int newsize, int count) {
    char[] temp = new char[newsize];
    System.arraycopy(buf, 0, temp, 0, count);

    buf = temp;

  }
}
