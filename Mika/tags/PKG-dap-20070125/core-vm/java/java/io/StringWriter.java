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
** $Id: StringWriter.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

public class StringWriter extends Writer {

  private StringBuffer buf;

  public  StringWriter() {
  	this(16);
  }
  
  public StringWriter(int initialSize) {
  	super();
  	buf = new StringBuffer(initialSize); 	
  }

/**
**	This method does nothing ...
**      required implementation of abstract method
*/
  public void close() throws IOException {}

/**
**	This method does nothing ...
**      required implementation of abstract method
*/
  public void flush(){}

  public StringBuffer getBuffer(){
   	return buf;
  }

  public String toString() {
   	return buf.toString();
  }

  public void write(int oneChar) {
   	buf.append((char)oneChar);
  }
  
  public void write(char[] buffer, int o, int c) {
   	buf.append(buffer, o, c);
  }
  
  public void write(String str) {
   	buf.append(str);
  }
  
  public void write(String str, int offset, int count) {
    if (count < 0) {
      throw new ArrayIndexOutOfBoundsException();
    }
    buf.append(str.substring(offset,count+offset));
  }

}
