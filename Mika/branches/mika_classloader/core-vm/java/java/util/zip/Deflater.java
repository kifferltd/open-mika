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
//System.out.println("Deflater: created with default algorithm");
	}
	public Deflater(int lvl) {
	        this(lvl, false);
	}
	public Deflater(int lvl, boolean noHeader) {
 		level = (9 < lvl || lvl < 0) ? DEFAULT_COMPRESSION : lvl;
//System.out.println("Deflater: created with compression level " + lvl + (noHeader ? " without " : " with ") + "header");
level = 0; // TESTING !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
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
//System.out.println("Deflater: setting compression level to "+ lvl);
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
//System.out.println("Deflater: setInput(" + buf + ", " + off + ", " + len + ")");
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
//System.out.println("Deflater: deflate(" + buf + ", " + off + ", " + len + ")");
 	  int tot = 0;
 	  if(len == 0){
//System.out.println("Deflater: deflate() returns 0");
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
    //System.out.println("deflated "+rd+" bytes");
 	
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
   	    //System.out.println("writing trailing adler "+rd);
        System.arraycopy(headerDict, finished, buf, off, rd);
        tot+=rd; 	   	
      }
 	  }
 	  totalOut += tot;
    //System.out.println("deflate returning "+tot+" bytes");
//System.out.println("Deflater: deflate() returns " + tot);
 	  return tot;
 	}
 	
 	public synchronized void reset(){
//System.out.println("Deflater: reset");
 	  totalIn=0;
	  totalOut=0;
	  dictadler=0;
	  nativeReset();
 	}
 	public synchronized void setDictionary(byte [] buf, int off, int len){
    //System.out.println("Deflater: setDictionary(" + buf + ", " + off + ", " + len + ")");
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
