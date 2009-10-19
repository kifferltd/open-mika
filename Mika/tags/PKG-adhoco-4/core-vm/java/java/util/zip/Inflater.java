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
 * $Id: Inflater.java,v 1.3 2006/10/04 14:24:15 cvsroot Exp $
 */
package java.util.zip;

/**
**  	this class is the base class of inflating done in java.util.zip.
**  	this class is used by InflaterInputStream, GZIPInputStream, ZipInputStream,
**	ZipFile, JarFile and JarInputStream ...
**
**	this class is mapped directly onto the ZLIB library and should be used very carefully.
** 	Only give input (setInput) if needsInput returns true. Start Inflating (inflate) until
**	new Input is required or the inflater is finished.
**
**  	If you use the inflater with noHeader = true then the inflater will insert a fake header.
**	You can pass the inflater the deflated data (deflated using the noHeader=true options).
** 	It will inflate the data correctly, however it will expect 4 trailing bytes wich represent the
**	Adler32 checksum.  finished will not return true unless it could verify the checksum.  If you pass an
**	extra 4 bytes as input it the inflater will return finished = true and say getRemaining() is 4. The Inflater-
** 	InputStream is build in that way that no bytes will get lost.  Since Zip, JAR and GZIP provide trailing data,
**	we can be sure that the inflater will finish it job ...
**
**  	NOTE: for some reason some deflaters generate more bytes than needed by this inflater. So the inflater might say
**	it was finished but not have used all provided bytes.  The ZipInputStream just skip such bytes till it reaches it next
**	header and verify if everything is ok.  But this will lead to Format exceptions in the GZIPInputStream ...
**	Altough it might not use all bytes it still generates all bytes passed to the deflating algorithm ...
*/
public class Inflater {
	
  private Adler32 adler = new Adler32();	

	private boolean finished;
  private boolean needsInput=true;
	private boolean needsDict;
	private boolean noHeader;
	private boolean skip;
        	
	private int totalIn;
	private int totalOut;
	private int remain;
	
	private byte[] header;
  private int dictAdler;
		
	public Inflater() {
		this(false);
	}
	public Inflater(boolean noHeader) {		
		create();
		this.noHeader = noHeader;
		skip = !noHeader;
		if (skip){
		  header = new byte[6];
		}
	}

//CALLS TO OWN PUBLIC INTERFACE		
	public int inflate(byte [] buf) throws DataFormatException {
	 	return inflate(buf, 0, buf.length);
	}
 	public void setInput(byte[] buf) {
 	 	setInput(buf, 0, buf.length);
 	}
 	public void setDictionary(byte [] buf){
 		setDictionary(buf , 0, buf.length);
 	}
		
//CALLS WHO RETURN MEMBER VARIABLES	
 	public synchronized int getAdler(){
 	  return (dictAdler == 0 ? (int) adler.getValue() : dictAdler);
 	}
	
 	public synchronized boolean needsDictionary(){
 	 	return needsDict;
 	}
	
	public synchronized boolean finished() {
	 	return finished;
	}	

 	public synchronized boolean needsInput(){
 	 	return needsInput;
 	}
	 	
 	public synchronized int getTotalIn(){
 	  return totalIn;
 	}
 	
 	public synchronized int getTotalOut(){
 	  return totalOut;
 	}

//REAL WORKERS ... 	  	 	
	public synchronized int inflate(byte [] buf, int off, int len) throws DataFormatException {
		if(finished){
		  return 0;
		}
	  len = _inflate(buf, off, len);
	  adler.update(buf,off,len);
	  totalOut += len;
	  return len;
	}


/**
**  if a Header is expected we check if dictionary is needed
**  |4 bits cinfo|4 bits cm|2 bits Flevel|1 bit FDICT|5bits checksum|
**  cinfo = compression info if CM=8 then cinfo = log2(LZ77 window size) - 8
**         else cinfo is not defined (0)
**  cm = compression method ...
**  FLevel = compression level --> pure info, not needed to decompress
**  FDict = set to 0 (if 1 then extra 4 bytes added to header ...)
**  checksum = these five bits are set the 2 bytes are divisable by 31
*/
 	public synchronized void setInput(byte[] buf, int off, int len) {
   	if (off < 0 || len < 0 || buf.length - len < off) {
   	 	throw new ArrayIndexOutOfBoundsException("offset = " + off + ", length = " + len + ", buffer length = " + buf.length);
   	}
   	if (len != 0) { 	        	
   	  if(skip){
   	    /**
   	    ** this case can only occur if we have a ZLIB header !!!
   	    ** this means we need at least 2 bytes as header and 4 bytes as trailer so we can ask for more input
   	    ** until we at least recieved 6 bytes ...
   	    **
   	    ** first fill up the header array (we have to make sure nothing strange
   	    ** happens if we set the input byte per byte. (don't do that though)
   	    */
   	    int put = (6 > len ? len : 6) - totalIn;
   	    System.arraycopy(buf,off, header, totalIn, put);
   	    totalIn += len;
   	    if (totalIn >= 6){
   	      /**
   	      ** header is full ...
   	      */
       		//System.out.println("DEBUG - header is full");	
   	      if (!((header[1] & 0x020) == 1)){
         		//System.out.println("DEBUG - header is full - no dictionary needed");	
   	        _setInput(header,2,4);
   	        remain = 6;
   	      }
   	      else {
         		//System.out.println("DEBUG - header is full - dictionary needed");	
   	        dictAdler = (int)ZipFile.bytesToLong(header,2);
   	        remain = 10;
   	      }
 	        _setInput(buf, off+put, len-put);
   	      skip = false;
   	      header = null;
   	    }
          	
   	  }
   	  else {
     		//System.out.println("DEBUG - passing input straight to native");	  		
     		totalIn += len;
     		_setInput(buf,off,len);
   		}
   		needsInput=false;
   	} 	
 	}
 	
 	public synchronized void reset(){
 		needsInput = true;
 		finished = false;
 		needsDict = false;
    totalIn = 0;
	  totalOut = 0; 	
	  dictAdler = 0;	
 		adler.reset();
 		nativeReset();
 	}
 	
protected native void finalize();

//native stuff ... 	 	
	private native void nativeReset();
  private native void create();
	private native int _inflate(byte[] buf, int off, int len) throws DataFormatException;
 	private native void _setInput(byte[] buf, int off, int len);
	 	
	public native synchronized void end();
 	public native synchronized void setDictionary(byte [] buf, int off, int len);
 	public native synchronized int getRemaining();

}
