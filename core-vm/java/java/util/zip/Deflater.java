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


/**
 * $Id: Deflater.java,v 1.3 2006/03/14 15:00:36 cvs Exp $
 */
package java.util.zip;

/**
** 	This class is the core of all deflating done in java.util.zip.
**	It is used by DeflaterOutputStream, ZipOutputStream, GZIPOutputStream
**   and JarOutputStream.
**  Before you use setInput you should always check if the deflater needs input (with needsInput)
**   call deflate untill needsInput returns true (give more input and start over);
**	Once finish is called you should not give new input anymore. just call deflate untill
**   finished returns true ...
*/
public class Deflater {

	private static final byte[] headerNoDict = { 0,0 };
	private final byte[] headerDict = { 0,0x3e, 0, 0, 0 ,0};
	
	
	public static final int BEST_COMPRESSION = 9;
	public static final int BEST_SPEED = 1;
	public static final int DEFAULT_COMPRESSION = -1;
	public static final int DEFAULT_STRATEGY = 0;
	public static final int DEFLATED = 8;
	public static final int FILTERED = 1;
	public static final int HUFFMAN_ONLY = 2;
	public static final int NO_COMPRESSION = 0;
			
  private boolean needsInput=true;
	private int dictadler=0;
	private boolean noHeader;

	private Adler32 adler = new Adler32();
	
	private int level;
	private int strategy;
	private int totalIn;
	private int totalOut;
	private int finished;
	// 0 = not finished
	// 1 = finish is called
	// 2 or more = done deflating ...
  // if a ZLIB needs to be suppiled finished will keep track of how many adler bytes are written already
	
	public Deflater() {
		this(DEFAULT_COMPRESSION , false);
	}
	public Deflater(int lvl) {
	        this(lvl, false);
	}
	public Deflater(int lvl, boolean noHeader) {
 		level = (9 < lvl || lvl < 0) ? DEFAULT_COMPRESSION : lvl;
		create();	
    this.noHeader = noHeader;
	}

//Straight calls to public API		
	public synchronized int getTotalIn(){
	  return totalIn;
	}

	public synchronized int getTotalOut(){
	  return totalOut;
	}
	
 	public synchronized int getAdler(){
 	  return (int)adler.getValue();
 	}
	
	public int deflate(byte [] buf) {
	 	return deflate(buf, 0, buf.length);
	}
	
 	public void setInput(byte[] buf) {
 	 	setInput(buf, 0, buf.length);	
 	}
 	
 	public void setDictionary(byte [] buf){
 		setDictionary(buf , 0, buf.length);
 	}

	public synchronized boolean finished() {
	 	return (finished > 1);
	}	
 	
 	public synchronized boolean needsInput(){
 	 	return (needsInput || (finished > 1));
 	}
 	
 	public synchronized void setLevel(int lvl){
 		if (9 < lvl || lvl < 0) {
 		 	throw new IllegalArgumentException();	
 		}
 		level=lvl;
 		updateLvl();
 	}	
 	
 	public synchronized void setStrategy(int strat){
 		if (DEFAULT_STRATEGY == strat && FILTERED == strat && HUFFMAN_ONLY == strat) {
 		 	throw new IllegalArgumentException();	
 		}
 		strategy=strat;
 	}	

 	public synchronized void setInput(byte[] buf, int off, int len){
 	  adler.update(buf,off,len);
 	  totalIn += len;
 	  _setInput(buf, off, len);
 	  needsInput = false;
 	}
 	
/**
**  if a Header is expected we check if dictionary is was passed
**  |4 bits cinfo|4 bits cm|2 bits Flevel|1 bit FDICT|5bits checksum|
**  cinfo = compression info if CM=8 then cinfo = log2(LZ77 window size) - 8
**         else cinfo is not defined (0)
**  cm = compression method ...
**  FLevel = compression level --> pure info, not needed to decompress
**  FDict = set to 0 (if 1 then extra 4 bytes added to header .int rdint rd..)
**  checksum = these five bits are set the 2 bytes are divisable by 31
*/
 	public synchronized int deflate(byte[] buf, int off, int len){
 	  int tot = 0;
 	  if(len == 0){
 	    return 0;
 	  }
 	  if (totalOut < 6 && !noHeader){
 	    if(dictadler != 0){
 	      tot = ( len > 6 ? 6 : len ) - totalOut;
   	    //System.out.println("writing header (with adler)"+tot);
 	      System.arraycopy(headerDict, totalOut, buf, off, tot);
 	      len -= tot;
 	      off += tot;
 	    }
 	    else if (totalOut < 2){
 	      tot = ( len > 2 ? 2 : len ) - totalOut;
 	      System.arraycopy(headerNoDict, totalOut, buf, off, tot);
   	    //System.out.println("writing header (without adler)"+tot);
 	      totalOut += tot;
 	      len -= tot;
 	      off += tot; 	
 	    }
 	  }
 	  int rd  = _deflate(buf, off, len);
 	
 	  tot += rd;
 	  len -=rd;
 	  if (finished > 0 && 0 < len){
      off += rd;
      rd = _deflate(buf, off, len);
      if(!noHeader){
        if(finished == 2){
          intToBytes((int)adler.getValue(), headerDict, 2);
        }
        rd = (4 > len ? len : 4);
        System.arraycopy(headerDict, finished, buf, off, rd);
        tot+=rd; 	   	
      }
 	  }
 	  totalOut += tot;
 	  return tot;
 	}
 	
 	public synchronized void reset(){
 	  totalIn=0;
	  totalOut=0;
	  dictadler=0;
	  nativeReset();
 	}
 	public synchronized void setDictionary(byte [] buf, int off, int len){
 	  if(finished < 2){
   	  Adler32 ad = new Adler32();
 	    ad.update(buf,off,len);
 	    intToBytes((int)ad.getValue(), headerDict, 2);
 	    _setDictionary(buf,off,len);
 	  }
 	}

protected native void finalize();
 	
//native stuff 	 	
 	private native void create();
 	private native void updateLvl();
 	private native void nativeReset();
 	private native void _setInput(byte[] buf, int off, int len);
 	private native void _setDictionary(byte [] buf, int off, int len);
	private native int _deflate(byte[] buf, int off, int len);	
 	
	public native synchronized void finish();
	public native synchronized void end();

	static void intToBytes(int val, byte[] bytes, int off){
		bytes[off++] = (byte)(val>>>24);
		bytes[off++] = (byte)(val>>>16);
		bytes[off++] = (byte)(val>>>8);
		bytes[off]   = (byte) val;
	}
}
