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
** $Id: PipedReader.java,v 1.3 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;


public class PipedReader extends Reader {


	private static final int pSize = 1024;

/*
**  pipe and pointers are used internally to synchronize datatransfer ...
**
**  pipe is a char[] of size pSize. The written data is stored in this array
**
**  pointers is an int[] of size 2. pointers[0] is de read position, where pointers[1] is the write position.
**  Only the PipedWriter is allowed to change the write pointer(WP). THe read pointer (RP)can only be moved by the reader
**  the pipe is empty if the pointers are equal (same position in pipe) and the buffer is full if RP == WP+1 (this means the
**  effectieve buffer size is pSize -1) ...
**  NOTE: WP points to an empty cell, while RP points to an unread char (unless RP == WP)
**  There is no need to synchronize access to the pipe since the RP pointer is only moved after char are reads
**  so there is no chance of overwritting unread chars	
*/	
	
	private char[] pipe=null;
	private int [] pointers=null;
	
	private boolean closed = false;
	private Thread thread;
  private PipedWriter srcW;
	
	public PipedReader() {
		thread = Thread.currentThread();
	}
	
	public PipedReader(PipedWriter src) throws IOException {
		thread = Thread.currentThread();
	  src.connect(this);
	}
	
	
	public void close() throws IOException{
	 	synchronized (lock) {
		  if (!closed) {
		 	  closed = true;
		 	  pipe = null;
		  }
		}
	}

	public void connect(PipedWriter src) throws IOException {
	 	synchronized (lock) {
	 		src.connect(this);
	 	}
	}
	
	public int read() throws IOException {
 		synchronized (lock) {
			if (closed || pipe == null) {
		 		throw new IOException();	
			}
      while(pointers[0] == pointers[1]) {
       	Thread.yield();
       	if(srcW.closed()){
       	  return -1;
       	}
       	if(!thread.isAlive()) {
       	 	throw new IOException();
       	}
      }
      int i = pipe[pointers[0]];
      pointers[0] = (pointers[0]+1) % pSize;
      return i;
    }	
	}
	
	public int read(char buff[], int offset, int count) throws IOException {
    if (offset < 0 || count < 0 || offset > buff.length - count) throw new ArrayIndexOutOfBoundsException();
    synchronized (lock) {
		  if (closed || pipe == null) {
		 	   throw new IOException();	
		  }
      while(pointers[0] == pointers[1]) {
        Thread.yield();
        if(srcW.closed()){
          if(pointers[0] != pointers[1]){
            break;
          }
          return -1;
        }
        if(!thread.isAlive()) {
          throw new IOException();
        }
      }   		
      int available = (pSize + pointers[1] - pointers[0])%pSize;
      available = (available > count ? count : available);
      internalread(buff, offset, available);
      offset += available;
      count -= available;
      return available;
    }
	}

	private void internalread(char [] buff, int offset, int count) {
		if (count+pointers[0] <= pSize) {
       System.arraycopy(pipe, pointers[0], buff, offset, count);
       pointers[0] += count;
		}
		else {
			int nr = pSize - pointers[0];
      System.arraycopy(pipe, pointers[0], buff, offset, nr);
      pointers[0]=0;
      internalread(buff, offset+nr, count-nr);				 	
		} 		
	}
		
	public boolean ready() throws IOException{
		synchronized (lock) {
			if (closed || pipe == null || !thread.isAlive()) {
		 		throw new IOException();	
			}
		  	return (pointers[1] != pointers[0]);
		}
	
	}
	
	// default acces methods : for communication between PipedReader and Writer ...
	
	void setPipe(char[] pp) {
	 	pipe = pp;
	}
	
	void setPointers(int[] ptrs) throws IOException {
	 	if (closed || pointers != null) {
	 		throw new IOException();
	 	}
	 	pointers = ptrs;
	}
	
	Thread switchThreads(Thread trd, PipedWriter src) {
	 	srcW = src;
	 	Thread t = this.thread;
	 	this.thread = trd;
	 	return t;
	}
	
	boolean closed() {
	 	return closed;
	}
	
}
