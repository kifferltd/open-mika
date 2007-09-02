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
