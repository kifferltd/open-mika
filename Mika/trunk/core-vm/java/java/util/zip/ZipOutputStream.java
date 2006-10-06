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
 * $Id: ZipOutputStream.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
 */
package java.util.zip;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;

/**
** the format of a Zip stream looks like:
** [local file Header + file data + data_descriptor] ...
** [file header] ... end of central dir record.
**
** for each local file (ZipEntry we push we write a file header at the end of the stream)
** ...
*/
public class ZipOutputStream extends DeflaterOutputStream implements ZipConstants {
		
	public static final int DEFLATED = 8;
	public static final int STORED = 0;
	
	private boolean finished=false;
	
	private int level = DEFLATED;
	private int method = DEFLATED;
	
	/**
	** will be use to store de central dir data
	*/
        private ByteArrayOutputStream centralDirRecord = new ByteArrayOutputStream();	
	private String comment=null;
	private long byteCount=0;
	private int NrOfEntries=0;

        /**
        ** we use a fixed time for each entry and keep the bytes ...
        */
	private byte [] timeBytes;		
	private boolean entryOpen=false;
        private byte [] currentFileHeader=null;
        private ZipEntry currentEntry=null;
        private boolean preset;
        private boolean deflating;
        private long dataCount;

        private LinkedList names = new LinkedList();        		 	
	private CRC32 crc = new CRC32();
	
	public ZipOutputStream(OutputStream out) {
	 	super(out, new Deflater(8, true));
	 	timeBytes = new byte[4];
	 	longToDosTime(System.currentTimeMillis(),timeBytes,0);
	}
	

	public void close() throws IOException {
		finish();
		out.close();
		
	}
	
	public void finish() throws IOException {
		if (!finished){
	 		if(entryOpen) {
	 			closeEntry();
		 	}
		//write central dir record
			//write file headers
			out.write(centralDirRecord.toByteArray());
			byte [] endCD = (byte[])EndOfCDRecord.clone();
	                endCD[8] = (byte)NrOfEntries;
	                endCD[9] = ((byte)(NrOfEntries>>>8));
	                endCD[10] =  endCD[8];
			endCD[11] =  endCD[9];			       	
			putBytes(centralDirRecord.size(), 12, endCD);
			putBytes((int)byteCount, 16, endCD);			
			if (comment != null) {
			       	int l = comment.length();
			       	endCD[20] = (byte)l;
			       	endCD[21] = ((byte)(l>>>8));		
				out.write(endCD,0,22);	
				out.write(comment.getBytes());
			}
			else {
				out.write(endCD,0,22);	
			
			}				
			finished = true;
		}
	}
	
	public void closeEntry() throws IOException {
		if (finished) {
		 	throw new IOException();
		}
		if (deflating) {
			def.finish();
			deflate();
		}
    if (!preset) {
			//Descriptor is needed
			byte [] trailer = new byte [16];
		 	System.arraycopy(dataDescS, 0, trailer, 0, 4); //data descriptor Signature
			int bytes = (int)crc.getValue();
			currentEntry.setCrc(crc.getValue());
			putBytes(bytes, 4, trailer);
			putBytes(bytes, 16, currentFileHeader);
			bytes = def.getTotalOut();
			currentEntry.setCompressedSize(bytes);
			putBytes(bytes, 8, trailer);
			putBytes(bytes, 20, currentFileHeader);
			bytes = def.getTotalIn();	
			currentEntry.setSize(bytes);
			putBytes(bytes, 12, trailer);
			putBytes(bytes, 24, currentFileHeader);
			out.write(trailer, 0, 16);
    }
    else {
    	if (deflating) {
    	 	if (currentEntry.getCrc() != crc.getValue()
    	 	 ||	(int)currentEntry.getSize() != def.getTotalIn()
    	 	 ||	(int)currentEntry.getCompressedSize() != def.getTotalOut()) {
       	 	 	throw new ZipException("problem when deflating " + currentEntry + ": current entry does not match def");
    	 	}        	
    	}
    	else {
    	 	if (currentEntry.getCrc() != crc.getValue()) {
      throw new ZipException("problem when storing " + currentEntry + ": CRC of data is " + currentEntry.getCrc() + ", should be " + crc.getValue());
    }
    if ((int)currentEntry.getSize() != dataCount
    	 	    ||	(int)currentEntry.getCompressedSize() != dataCount) {
    	 	 	throw new ZipException("problem when storing " + currentEntry + ": sizes do not match : getSize() returns " + currentEntry.getSize() + ", getCompressedSize() returns " + currentEntry.getCompressedSize() + ", dataCount is " + dataCount);
    	 	}
    	}
    }	
	 	byte [] extra = currentEntry.getExtra();	 	
	 	if (extra != null) {
	 		int i = extra.length;
		 	currentFileHeader[30] = (byte)i;
	 		currentFileHeader[31] = ((byte)(i>>>8));
	 	}
	        String com = currentEntry.getComment();
	 	if (com != null) {
	 		int i = com.length();
		 	currentFileHeader[32] = (byte)i;
	 		currentFileHeader[33] = ((byte)(i>>>8));
	 	}	 	
	 	centralDirRecord.write(currentFileHeader, 0, 46);
	 	centralDirRecord.write(currentEntry.getName().getBytes());	
	 	if (extra != null) {
                 	centralDirRecord.write(extra);
	 	}
	 	if (com != null) {
	 	    	centralDirRecord.write(com.getBytes());
	 	}	 	
	        byteCount += (preset ? 0 : 16) + currentEntry.getCompressedSize();	
	        entryOpen=false;
	 	def.reset();    // reseting the deflater ... (releasing memory)
	}
	
/**
**  Prepares the stream to write the data belonging to the ZipEntry passed to this method	
**  DO NOT CHANGE THE ZIPENTRY AFTER PASSING IT TO THIS METHOD, INCONSISTANT DATA WILL BE WRITTEN OUT !
*/	
	public void putNextEntry(ZipEntry ze) throws IOException {
		if (finished) {
		 	throw new IOException();
		}
	 	if(entryOpen) {
	 		closeEntry();
	 	}
		String s = ze.getName();
	 	if (names.contains(s)) {
	 	 	throw new ZipException("stream already contains "+s);
	 	}
	 	names.addLast(s);
	 	currentEntry = ze;
                byte [] header = new byte [30];
                currentFileHeader = (byte[])fileHeader.clone();
	 	System.arraycopy(locFileHeaderS, 0, header, 0, 4); //local File Header Signature
	 	header[4] = (byte) 20;  // version needed to extract (byte one): 20 --> 2.0
	 	header[5] = (byte) 0;   // version needed to extract (byte two): Os indication
	 	//general purpose bits (2 bytes)    --> CHsize 8
	 	   	//--> see CRC, un- and compressed size
	 	//compression method (2 bytes)      --> CHsize 10
	 	int i = ze.getMethod();
	 	i = (i == -1 ? method : i);
		header[8] = (byte)i;
		currentFileHeader[10] = (byte)i;
	 	def.setLevel(level);
	 	if (i == DEFLATED) {
	 	 	deflating = true;
	 	}
	 	else {
	 	 	deflating = false;
	 	 	dataCount=0;
	 	}	 	
	 	//date and time fields (2 bytes each) --> MS-Dos format ...  --> CHsize 14
	 	if (ze.getTime() == -1) {
	 	 	System.arraycopy(timeBytes, 0, header, 10, 4);
	 	 	System.arraycopy(timeBytes, 0, currentFileHeader, 12, 4);
	 	}
	 	else {
	 	 	longToDosTime(ze.getTime(), header, 10);
	 	 	System.arraycopy(header,10, currentFileHeader, 12, 4);	 		 	
	 	}
	 	
	 //CRC-32, compressed size and uncompressed size is only set all three are defined in the entry.
	  	//CRC-32 (4 bytes)  --> CHsize 18
	 	//compressed size (4 bytes) --> CHsize 22	 	
	 	//uncompressed size (4 bytes) --> CHsize 26
	 	if(ze.getCrc() == -1l || ze.getSize() == -1l || ze.getCompressedSize()== -1l) {
			if (!deflating) {
			 	throw new ZipException("CRC, size and compressed size should be set when storing");
			}	 	 	
	 	 	preset = false;	
			header[6] = (byte)8;
			currentFileHeader[8] = (byte)8;	 	 		 	
	 	
	 	}
	 	else {
	 		int bytes = (int)ze.getCrc();
	 		putBytes(bytes, 14 , header);
	 		putBytes(bytes, 16 , currentFileHeader);
	 		bytes = (int)ze.getCompressedSize();
	 		putBytes(bytes, 18 , header);
	 		putBytes(bytes, 20 , currentFileHeader);
	 		bytes = (int)ze.getSize();
	 		putBytes(bytes, 22 , header);
	 		putBytes(bytes, 24 , currentFileHeader);
	 		preset = true;
	 	}
	 	//file name length (2 bytes) --> CHsize 28
	 	putBytes((int)byteCount, 42, currentFileHeader);
		i = s.length();
	        byteCount += i + 30;	
		header[26] = (byte)i;
	 	currentFileHeader[28] = (byte)i;
		i = i>>>8;
		header[27] = (byte)i;
	 	currentFileHeader[29] = (byte)i;
	 	//extra field length (2 bytes) --> CHsize 30
	 	byte [] extra = ze.getExtra();
	 	if (extra != null) {
	 		i = extra.length;
	 		byteCount += i;
			header[28] = (byte)i;
			header[29] = ((byte)(i>>>8));
	 	}	
	 	out.write(header, 0, 30);   // writing header
	 	//file name --> (length already specified)
	 	out.write(s.getBytes());	// writing file name
	 	//extra field
	 	if (extra != null) {
                 	out.write(extra,0,i);
	 	}	 	
	 	crc.reset();	// reseting the crc checksum
	 	NrOfEntries++;	
	 	entryOpen=true;		
	}
	
	public void setComment(String comment){
		if (comment.length() > 0x0ffff) {
		 	throw new IllegalArgumentException();
		}
		this.comment = comment;	
	}
	
	public void setLevel(int lvl) {
	
	}
	public void setMethod(int m) {
	  	if (m != DEFLATED && m != STORED){
	  	 	throw new IllegalArgumentException();
	  	}
	  	method = m;
	}
	
	public void write(byte [] buf, int offset, int len) throws IOException {
		if (finished) {
		 	throw new IOException();
		}
		crc.update(buf, offset, len);
		if (deflating) {
			super.write(buf, offset, len);
		}
		else {
		 	out.write(buf, offset, len);
		 	dataCount += len;		
		}
	}
	/**
	** put the 4 bytes of the int value in the byteArray
	** LSB goes first ...
	*/
	private void putBytes(int bytes, int off, byte [] buf) {
		buf[off++] = (byte)bytes;
		buf[off++] = ((byte)(bytes>>>8));
		buf[off++] = ((byte)(bytes>>>16));
		buf[off] = ((byte)(bytes>>>24));			
	}
	
	private void longToDosTime(long time, byte[] bytes, int offset) {
	 	GregorianCalendar gc = new GregorianCalendar(0,0,0);
	 	gc.setTime(new Date(time));
	 	int value = (gc.get(Calendar.HOUR_OF_DAY))<<11;
	 	value |= (gc.get(Calendar.MINUTE) & 0x3f)<<5;
	 	value |= (gc.get(Calendar.SECOND)/2) & 0x1f;
                bytes[offset] = (byte)value;
                bytes[offset+1] = (byte)(value>>>8);
		value = ((gc.get(Calendar.YEAR)-1980)<<9);
	 	value |= (((gc.get(Calendar.MONTH)+1)& 0xf)<<5);
	 	value |= ((gc.get(Calendar.DATE)) & 0x1f);
                bytes[offset+2] = (byte)value;
                bytes[offset+3] = (byte)(value>>>8);	
	}
}
