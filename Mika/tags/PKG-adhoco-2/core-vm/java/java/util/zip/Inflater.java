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
   	 	throw new ArrayIndexOutOfBoundsException();
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
