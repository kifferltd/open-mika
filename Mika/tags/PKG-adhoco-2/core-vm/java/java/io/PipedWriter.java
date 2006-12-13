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
** $Id: PipedWriter.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;


/**
**	See PipedReader for more information
*/
public class PipedWriter extends Writer {

	private static final int pSize = 1024;	

	private boolean closed=false;
	private Thread thread;
	private PipedReader sinkR;
	
	private char[] pipe;
	private int[] pointers;


	public PipedWriter() {
		thread = Thread.currentThread();
	}
	
	public PipedWriter(PipedReader sink) throws IOException {
		thread = Thread.currentThread();
	        connect(sink);
	}
	
	public void close() throws IOException{
    		synchronized (lock) {
		     	if (!closed) {
	          closed = true;
	     	 		pipe = null;
	     	 		pointers = null;	
	     		}
	     	}
	}

	public void connect(PipedReader sink) throws IOException {
    		synchronized (lock) {
			if (closed || pipe != null) {
			 	throw new IOException();
			}
			int[] p = new int[2];
			sink.setPointers(p);
			pointers=p;
			pipe = new char[pSize];
			sink.setPipe(pipe);
			sinkR = sink;
			thread = sink.switchThreads(thread,this);
		}
	}
	
	public void flush() throws IOException {
		if (closed) {
			throw new IOException();
		}
	}
	public void write(int ch)throws IOException {
    		synchronized (lock) {
	        	//System.out.println("thread is alive ="+thread.isAlive());
		        if(closed || !thread.isAlive() || pipe == null) {
				      throw new IOException();
	        	}
    		        while((pSize - 1 - ((pSize + pointers[1] - pointers[0])%pSize))==0) {
    		         	Thread.yield();
    		         	if(!thread.isAlive() || sinkR.closed()) {
    		         	 	throw new IOException();
    		         	}
    		        }
	                pipe[pointers[1]] = (char)ch;
	                pointers[1] = (pointers[1]+1) % pSize;
	        }	
	}
	
	public void write(char buff[], int offset, int count) throws IOException {
    		if (offset < 0 || count < 0 || offset > buff.length - count) throw new ArrayIndexOutOfBoundsException();
    		synchronized (lock) {
                    //System.out.println("thread is alive ="+thread.isAlive());
		    if(closed || !thread.isAlive() || pipe == null) {
			throw new IOException();
	            }
    		    while(count > 0) { //if count > 0 we still have bytes to write ...
    		        int space = pSize - 1 - ((pSize + pointers[1] - pointers[0])%pSize);
                        //System.out.println("WRITING count ="+count+", space ="+space+", W pointer ="+pointers[1]+", R pointer ="+pointers[0] );
    		        while(space==0) {
    		         	Thread.yield();
    		         	if(!thread.isAlive() || sinkR.closed()) {
    		         	 	throw new IOException();
    		         	}
    		         	space = pSize - 1 - ((pSize + pointers[1] - pointers[0])%pSize);
    		        }
                        //System.out.println("WRITING 2 count ="+count+", space ="+space+", W pointer ="+pointers[1]+", R pointer ="+pointers[0] );
    		        space = (space > count ? count : space);
    		        internalwrite(buff, offset, space);
    		        offset+=space;
    		        count-=space;
	            }
	        }
	}	
	
	private void internalwrite(char buff[], int offset, int count) {
		//System.out.println("internal write count ="+count+", W pointer ="+pointers[1]+", R pointer ="+pointers[0] );
		if (count+pointers[1] <= pSize) {
		        System.arraycopy(buff, offset, pipe, pointers[1], count);
		        pointers[1] += count;
		}
		else {
			int nr = pSize - pointers[1];
		        System.arraycopy(buff, offset, pipe, pointers[1], nr);
		        pointers[1]=0;
		        internalwrite(buff, offset+nr, count-nr);				 	
		} 		
	}
	
	// default acces methods : for communication between PipedReader and Writer ...
	
	boolean closed() {
	 	return closed;
	}
	
}
