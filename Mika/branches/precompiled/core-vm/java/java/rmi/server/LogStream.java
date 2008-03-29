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

package java.rmi.server;

import java.io.*;

public class LogStream extends PrintStream {

  public final static int BRIEF = 10;
  public final static int SILENT = 0;
  public final static int VERBOSE = 20;

  private LogStream() {
    super(null);
  }
                            
  public static PrintStream getDefaultStream() {
    System.out.println("[LogStream.getDefaultStream] Not implemented...");
    return null;
  }
  
  public OutputStream getOutputStream() {
    System.out.println("[LogStream.getOutputStream] Not implemented...");
    return null;
  }
  
  public static LogStream log(String name) {
    System.out.println("[LogStream.log] Not implemented...");
    return null;
  }
  
  public static int parseLevel(String s) {
    System.out.println("[LogStream.parseLevel] Not implemented...");
    return 0;
  }
  
  public static void setDefaultStream(PrintStream newDefault) {
    System.out.println("[LogStream.setDefaultStream] Not implemented...");
  }
  
  public void setOutputStream(OutputStream out) {
    System.out.println("[LogStream.setOutputStream] Not implemented...");
  }
  
  public String toString() {
    System.out.println("[LogStream.toString] Not implemented...");
    return null;
  }
  
  public void write(byte[] b, int off, int len) {
    System.out.println("[LogStream.write] Not implemented...");
  }
  
  public void write(int b) {
    System.out.println("[LogStream.write] Not implemented...");
  }

}

