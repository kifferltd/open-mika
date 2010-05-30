/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2010 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package java.util.zip;

import java.io.InputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

/**
**	Basic InputStream for reading zip formatted streams.
** GENERAL NOTE:
**	we use in.read(byte[] , int, int) to read bytes.  In some occasions we
**	might not get all the bytes we want (not yet available in the inputstream in)
**      there might be some bytes left in buf.  Whenever we need bytes we ask them from
**	getBytes([BII) --> this method will take the remaining bytes from buf or else
**	it will get them directly from in ...
*/
public class ZipInputStream extends InflaterInputStream implements ZipConstants {
         
  private boolean closed;
  private CRC32 crc = new CRC32();
  private LinkedList entries = new LinkedList();
          
  private ZipEntry currentEntry;
  private boolean entryOpen;
  private boolean preset;
  private boolean inflating;
  private boolean inCentDir;
  private boolean allowDataDescriptor;
  private long dataCount;
  private int used;
  private boolean zipFileStream;
  private ZipStreamInfo zipStreamInfo;
          
  public ZipInputStream(InputStream in) {
    super(in, new Inflater(true));
    if (in instanceof ZipByteArrayInputStream) {
      setupZipByteArrayStream(); 
    }    
    else if (in.markSupported()) {
      try {
        setupZipMarkableStream(); 
      }
      catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }    
  }
	
  protected ZipEntry createZipEntry(String zname) {
		return new ZipEntry(zname);
	}
	
	public int available() throws IOException{
	 	return (closed ? 0 : 1);
	}
	public void close() throws IOException {
		if (!closed) {
			if (entryOpen) {
				closeEntry();
			}
			super.close();
			closed=true;
			currentEntry=null;
		}
	}

/**
** skip will only skip bytes within the current Entry ...
** call getNextEntry() to skip more data
**
*/	
	public long skip(long n) throws IOException {
	 	return super.skip(n);
	}

	public ZipEntry getNextEntry() throws IOException { 
	 	if (zipFileStream){
     return nextZipStreamEntry();
    }
    closeEntry();
    readLFHeader();
    if(inflating && entryOpen){
      //if there is no entryOpen there is no need to preset the buf
     	presetBuf();
    }	
	 	return currentEntry;
	}
		
  public void closeEntry() throws IOException {
    if (!entryOpen) {
      return;
    }
    if (zipFileStream) {
      closeZipStreamEntry();
      return;
    }
    // we discard all remaining data by calling read ...
    byte [] data = new byte [1024];
    while(/*dataCount > 0 &&*/ read(data,0,1024) != -1) {
    }
    used = len - inf.getRemaining();
    //System.out.println("closeEntry(): after draining, used = " + used + " (len = " + len + ") inf remaining "+inf.getRemaining());
    String corruption_type = null;
    if (preset) {
      if (inflating) {
        if (currentEntry.getCrc() != crc.getValue()) {
          corruption_type = "bad crc" + " (was " + crc.getValue() + ", expected " + currentEntry.getCrc() + ")";
	}
	else if (currentEntry.getSize() != inf.getTotalOut()) {
          corruption_type = "bad decompressed length";
	}
	else if (currentEntry.getCompressedSize() != (inf.getTotalIn() + skipTillNextHeader() - inf.getRemaining())) {
          corruption_type = "bad compressed length";
        } 
        if (corruption_type != null) {
          throw new ZipException("data was corrupted: " + corruption_type);
	}
      }
      else {
        if (currentEntry.getCrc() != crc.getValue()) {
          corruption_type = "bad crc";
        }
        else if (dataCount != 0) {
          corruption_type = "bad length";
        }
        if (corruption_type != null) {
          throw new ZipException("data was corrupted: " + corruption_type);
	}
      }
    }
    else {
      skipTillNextHeader();
      if (getBytes(data,0,16) < 16) {
        throw new ZipException("data was corrupted - EOF during header");
      }
      if (data[0] != dataDescS[0] || data[1] != dataDescS[1] || data[2] != dataDescS[2] || data[3] != dataDescS[3] || crc.getValue() != ZipFile.bytesToLong(data, 4)) {
        throw new ZipException("corrupt dataDescriptor");
      }
      currentEntry.setCrc(crc.getValue());
      currentEntry.setCompressedSize(ZipFile.bytesToLong(data, 8));
      currentEntry.setSize(ZipFile.bytesToLong(data, 12));
    }
    entryOpen=false;
  }
	
/**
** read will only read bytes in the current Entry ...
** call getNextEntry to read more bytes ...
**
*/	
	public int read(byte[] buff, int offset, int len) throws IOException {
    if (zipFileStream) {
      return zipStreamRead(buff, offset, len);
    }
    int read=-1;
	  if(entryOpen) {
	  	if (inflating) {
		   	read = super.read(buff, offset, len);
	     	if (inf.needsDictionary()) {
	        throw new ZipException("needs dictionary");
	     	}
	      dataCount -= read;
		  }	
	    else {
	     	if (dataCount > 0) {
	        len = (len > dataCount ? (int)dataCount : len);
	        read = getBytes(buff, offset, len);
	        dataCount -= read;
	      }	          	
	    }
	    if (read != -1) {
	      crc.update(buff, offset, read);
	    }
	  }
	  return read;
	}
	
  /***
   ** private methods used for code with regular files.
   **/ 
  
	private void readLFHeader() throws IOException {
    byte [] header = new byte [22];
    int l = 0;
    while (true) {
      l = getBytes(header,l,22-l);
	    if (l == -1) {
	 	    // we reached the end of the stream ...
	 	    currentEntry=null;
	      break;
	    }
	    else {
	    	// we read some bytes and we expect this to be a header ...
	 	    if (header[0] != 80 || header[1] != 75 ){
	 	 	    throw new ZipException("corrupt header found (not 80:75)");
	 	    }
	 	    if (header[2] == 3 && header[3] == 4) {
 	    		parseLocalFileHeader(header);
	 	      break;
	 	    }
	 	    else if (header[2] == 1 && header[3] == 2) {
	 	    	parseFileHeader(header);
	 	    	l = 0;
	 		  }
	 		  else if (header[2] == 5 && header[3] == 6) {
	 	      parseEndCD(header);
	 	      l = 0;
	      }
	 		  else if (allowDataDescriptor && header[2] == 7 && header[3] == 8) {
	        //lets hope we don't come here to often ...
	        allowDataDescriptor = false;
	 		    for(int i=4 ; i <16 ; i++){
	 		      if(header[i] != 0){
    	 	 	    throw new ZipException("header data is wrong"); 		
	 		      }	 		
	 		    }
	 		    l = 6;
	 		    System.arraycopy(header,16,header,0,6);
	      }
	 			else {
			 	  throw new ZipException("corrupt header found");			 		
	 	    }
	    }
	  }	
	}
		
/**
** when we locate a FileHeader, it means we found the central directory ...
** the FileHeaders should have been preceeded by a LocalFileHeader.  	
** So we have a reference in our LinkedList to this Header. If we don't
** find one or data is inconsistant then we throw a ZipException.
*/
  private void parseFileHeader(byte[] header) throws IOException {
  	// we have 22 bytes --> 4 are already checked  Signature
  	// version made by and version needed --> skip ... (4 bytes)
  	// general purpose bytes (2) skip
  	byte [] buff = new byte [46];
  	System.arraycopy(header,0,buff,0,22);
  	// we read some more bytes since the header has 46 bytes ...
  	if ( getBytes(buff, 22, 24) < 24 ) {
  	 	throw new ZipException("bytes missing --> corrupt header");
  	}
  	long time = ZipFile.getDate(buff , 12);
    long crc32 = ZipFile.bytesToLong(buff, 16);
    long compSize = ZipFile.bytesToLong(buff, 20);
    long size = ZipFile.bytesToLong(buff, 24);
  	int fnlen = (0x0ff&(char)buff[28]) + (0x0ff &(char)buff[29])*256;
  	byte [] fname = new byte[fnlen];
  	getBytes(fname,0, fnlen);
  	String name = new String(fname, 0, fnlen);
  	ListIterator li = entries.listIterator(0);
  	ZipEntry ze=null;
  	while (li.hasNext()) {
  		ze = (ZipEntry) li.next();
  		if (name.equals(ze.getName())) {
  		 	break;
  		}			
  	}
  	if (ze == null) {
  	 	throw new ZipException("corrupt header");	
  	}	
  	if ( time != ze.getTime() || crc32 != ze.getCrc() || size != ze.getSize() || compSize != ze.getCompressedSize()) {
  	 	throw new ZipException("corrupt header");
  	}
  	fnlen = (0x0ff&(char)buff[30]) + (0x0ff &(char)buff[31])*256;
  	fname = new byte[fnlen];
  	if (fnlen == 0) {
  		ze.setExtra(null);	 	
  	}
  	else {
  		getBytes(fname,0, fnlen);
  		ze.setExtra(fname);	 	
  	}
  	fnlen = (0x0ff&(char)buff[32]) + (0x0ff &(char)buff[33])*256;
  	fname = new byte[fnlen];
  	if (fnlen == 0) {
  		ze.setComment(null);	 	
  	}
  	else {
  		getBytes(fname,0, fnlen);
  		ze.setComment(new String(fname, 0, fnlen));	 	
  	}
  	inCentDir=true;
  	// the rest of the bytes are not verified	
  }

  private void parseLocalFileHeader(byte[] header) throws IOException {
	// we have 22 bytes --> 4 are already checked  Signature
	// version needed --> ... (2 bytes)
	// general purpose bytes (2)
	if(inCentDir) {
	 	throw new ZipException("stream is corrupted");
	}
	int mode = (0x0ff&(char)header[4]) + (0x0ff &(char)header[5])*256;
	if (mode > 20) {
	 	throw new ZipException("higher zip version needed");
	}
	mode = (0x08&(0x0ff&(char)header[6]));
	// we read some more bytes since the header has 30 bytes ...
	if ( getBytes(header, 0, 8) < 8 ) {
	 	throw new ZipException("bytes missing --> corrupt header");
	}
	int fnlen = (0x0ff&(char)header[4]) + (0x0ff &(char)header[5])*256;
	byte [] fname = new byte[fnlen];
	getBytes(fname,0, fnlen);
	String name = new String(fname, 0, fnlen);
	ListIterator li = entries.listIterator(0);
	ZipEntry ze;
	while (li.hasNext()) {
		ze = (ZipEntry) li.next();
		if (name.equals(ze.getName())) {
		 	throw new ZipException("file name already exist");
		}		
	}	
	ze = createZipEntry(name);
	if (mode != 8) {
		preset = true;
    ze.setCrc(ZipFile.bytesToLong(header, 14));
    ze.setCompressedSize(ZipFile.bytesToLong(header, 18));
	  ze.setSize(ZipFile.bytesToLong(header, 0));
	}
	else {
	 	preset = false;
	}
	mode = (0x0ff&(char)header[8]) + (0x0ff &(char)header[9])*256;	
  if (mode != 8 && mode != 0) {
	 	throw new ZipException("stream is corrupted");
  }
  if (mode == 8) {
    inflating=true;
  }
  else {
   	if (!preset) {
	   	throw new ZipException("stream is corrupted");         	
    }
    inflating=false;
    dataCount = ze.getSize();
  }
	inf.reset();
  ze.setMethod(mode);
  ze.setTime(ZipFile.getDate(header , 10));
	fnlen = (0x0ff&(char)header[6]) + (0x0ff &(char)header[7])*256;
	fname = new byte[fnlen];
	if (fnlen == 0) {
		ze.setExtra(null);	 	
	}
	else {
		getBytes(fname,0, fnlen);
		ze.setExtra(fname);	 	
	}
	entries.addLast(ze);
	currentEntry=ze;
  crc.reset();
  if(preset && ze.getCompressedSize() == 0){
    entryOpen=false;	
    allowDataDescriptor = true;
  }
	else {
    entryOpen=true;	
	}
	// the rest of the bytes are not verified	
	
  }
  private void parseEndCD(byte[] header) throws IOException {
	  if (((0x0ff&(char)header[10]) + (0x0ff &(char)header[11])*256) != entries.size()) {
	 	  throw new ZipException("corrupt header");
	  }
    int commentSize = ((0x0ff&(char)header[20]) + (0x0ff &(char)header[21])*256);
	  getBytes(new byte[commentSize],0,commentSize);
	  entries.clear();
	  inCentDir = false;	                                                                         	
  }
   	
  /**
  ** while reading through the stream some bytes are read and put into buf.
  ** if we need bytes we should check if there are unread bytes in buf
  ** getBytes will read bytes from buf or in (if necessary)
  */
  private int getBytes(byte [] target, int offset, int length) throws IOException {
	int rd = 0;
	while (length > 0){		
		int get = len - used;
		//System.out.println("DEBUG: getBytes get ="+get+", len = "+len+", used = "+used);
		get = (get > length ? length : get);
		if (get > 0) {
		 	System.arraycopy(buf, used, target, offset, get);
		 	used +=get;
			//System.out.println("getBytes(" + target + ", " + offset + ", " + length + "): used now = " + used);
		}
		else {
		 	get = in.read(target, offset, length);
		 	if (get == -1) {
		 		if (rd == 0){
		 		 	rd--;
		 		}
		 	 	break;	
		 	}		 	
		}
	 	length -= get;
	 	rd +=get;
	 	offset+=get;
	}
	return rd;
  }

  private void presetBuf(){
    len -= used;
    System.arraycopy(buf, used, buf, 0, len);
	  inf.setInput(buf,0,len);
  }

  private int skipTillNextHeader() throws IOException{
   	int skipped=0;
   	byte [] head = new byte[1];
   	getBytes(head,0,1);
	  //if (l < 1) System.out.println("getBytes(" + head + ", 0, 1) returned " + l);
   	while (head[0] != 80) {
   	 	skipped++;
	   	getBytes(head,0,1);   	 	
	    //if (l < 1) System.out.println("getBytes(" + head + ", 0, 1) returned " + l);
   	}
   	used--;
	  //System.out.println("skipTillNextHeader(): used now = " + used);
   	buf[used] = head[0];   	
    //System.out.println("skipTillNextHeader(): skipped = " +skipped);
   	return skipped;
  }

  /***
   ** Functions used for shortcutting ZipFile stream. 
   **/
  
  private void setupZipByteArrayStream() {
    byte[] bytes = ((ZipByteArrayInputStream)in).getBytes();
    int size = bytes.length;
    byte CDS3 = endCenDirS[3];
    for (int p=size  ; --p > -1 ; ) {
      if (bytes[p] == CDS3) {
      //this might be the last byte of the signature!
        if (p >=3) {
          if (bytes[p-1] == endCenDirS[2] && bytes[p-2] == endCenDirS[1] && bytes[p-3] == endCenDirS[0]) {
            //we have found a signature ...
            if(readEntries(bytes, size, p+1)){
              this.zipFileStream = true;
              return;
            }
          }
        }
      }
    }
    //System.out.println("ZipInputStream.setupZipByteArrayStream(): No valid zipfile found");
  }
  
  private void setupZipMarkableStream() throws IOException {
    in.mark(Integer.MAX_VALUE);
    LinkedList buffers = new LinkedList();
    int bufsiz = 100000;
    int totlen = 0;
    byte CDS3 = endCenDirS[3];
    while (true) {
      try {
        byte[] buf = new byte[bufsiz];
        int l = in.read(buf);
        if (l < 0) {
          break;
        }
        if (l < bufsiz) {
          byte[] newbuf = new byte[l];
          System.arraycopy(buf, 0, newbuf, 0, l);
          buf = newbuf;
        }
        buffers.add(buf);
        totlen += l;
      }
      catch (IOException ioe) {
        break;
      }
    }
    int nbufs = buffers.size();
    int count = 0;
    buf = new byte[totlen];
    while (count < totlen) {
      byte[] nextbuf = (byte[])buffers.removeFirst();
      System.arraycopy(nextbuf, 0, buf, count, nextbuf.length);
      count += nextbuf.length;
    }
    int p = buf.length - 1;
    while (--p >= 0) {
      if (p >= 3 && buf[p] == CDS3) {
      //this might be the last byte of the signature!
        if (buf[p-1] == endCenDirS[2] && buf[p-2] == endCenDirS[1] && buf[p-3] == endCenDirS[0]) {
          //we have found a signature ...
          if(readEntries(buf, buf.length, p+1)){
            zipFileStream = true;
            break;
          }
        }
      }
    }

    in.reset();
    in.mark(0);
  }
  
  private boolean readEntries(byte [] bytes, int size, int pos) {
    int nrE = size - pos;
    nrE = (16 > nrE ? nrE : 16);
    byte [] b = new byte [16];
    System.arraycopy(bytes, pos, b,0, nrE);
    //b should contain 16 bytes of the possible header
    nrE = (0x0ff&(char)b[6]) + (0x0ff &(char)b[7])*256;
    int stP = (int) ZipFile.bytesToLong(b, 12);
    int length = (int) ZipFile.bytesToLong(b, 8);
    if (nrE < 0 || (stP+length) > size || length < 0) {
      //we encountered a fony header
      return false;
    }
    
    zipStreamInfo = new ZipStreamInfo(nrE, stP, bytes);
    return true;
  }  
    
  private ZipEntry nextZipStreamEntry() throws IOException {
    if (entryOpen) {
      closeZipStreamEntry();
    }
    if (zipStreamInfo.nrEntries <= zipStreamInfo.currentEntry) {
      return null;
    }
    byte[] b = zipStreamInfo.data;
    int size = b.length;
    int pos = zipStreamInfo.entryPointer;
    if (pos + 46 > size) {
      throw new ZipException("too few bytes for header");// bad Header ...
    }
    if (cenFileHeaderS[0] != b[pos] || cenFileHeaderS[1] != b[pos + 1]
        || cenFileHeaderS[2] != b[pos + 2] || cenFileHeaderS[3] != b[pos + 3]) {
      throw new ZipException("bad header in central file directory");
    }
    // we have found a correct header ...
    // lets skip useless bytes
    pos += 28;
    int len = (0x0ff & (char) b[pos]) + (0x0ff & (char) b[pos + 1]) * 256;
    pos += 18;
    if (pos + len > size) {
      throw new ZipException("not enough bytes in header");
    }

    String name = new String(b, pos, len);
    ZipEntry ze = createZipEntry(name);
    pos -= 36;
    int hlp = (0x0ff & (char) b[pos]) + (0x0ff & (char) b[pos + 1]) * 256;
    if (hlp != 0 && hlp != 8) {
      throw new ZipException("unknown store/zip method " + hlp);
    }
    ze.compressionMethod = hlp;
    pos += 2;
    ze.time = ZipFile.getDate(b, pos);
    pos += 4;
    ze.crc = ZipFile.bytesToLong(b, pos);
    pos += 4;
    ze.compressedSize = ZipFile.bytesToLong(b, pos);
    pos += 4;
    ze.size = ZipFile.bytesToLong(b, pos);
    pos += 6;
    hlp = (0x0ff & (char) b[pos]) + (0x0ff & (char) b[pos + 1]) * 256;
    pos += 2;
    int com = (0x0ff & (char) b[pos]) + (0x0ff & (char) b[pos + 1]) * 256;
    if (hlp < 0 || com < 0) {
      throw new ZipException("invalid header data");
    }
    pos += 10;
    int localFHoffs = (int)ZipFile.bytesToLong(b, pos);
    int localFHlen = (0x0ff & (char) b[localFHoffs + 26]) + (0x0ff & (char) b[localFHoffs + 27]) * 256;
    // TODO: this should be the same as in global FH I guess
    int localFHxlen = (0x0ff & (char) b[localFHoffs + 28]) + (0x0ff & (char) b[localFHoffs + 29]) * 256;
    // apparently this can be different to the global FH

    // WAS: ze.pointer = ZipFile.bytesToLong(b, pos) + len + hlp + com + 30;
    ze.pointer = localFHoffs + localFHlen + localFHxlen + 30;
    pos += 4 + len;
    if (hlp > 0) {
      byte[] extra = new byte[hlp];
      System.arraycopy(b, pos, extra, 0, hlp);
      ze.extra = extra;
      pos += hlp;
    }
    if (com > 0) {
      ze.comment = new String(b, pos, com);
      pos += com;
    }
    zipStreamInfo.currentEntry++;
    zipStreamInfo.entryPointer = pos;
    currentEntry = ze;
    entryOpen = false;
    return ze;
  }

  private void closeZipStreamEntry() {
    // TODO Add checks for zipfile consistency.    
    entryOpen = false;
    currentEntry = null;
  }

  private int zipStreamRead(byte[] buff, int offset, int len) throws ZipException {
    if (currentEntry == null) {
      return -1;
    }
    if (!entryOpen) {
      //TODO add integrety checks for zipfile.
      //checkEntry();
      if (currentEntry.compressionMethod != 0) {
        inf.reset();
        inf.setInput(zipStreamInfo.data, (int)currentEntry.pointer
            , (int)currentEntry.compressedSize);
      } else {
        zipStreamInfo.have = (int) currentEntry.size;
      }
      entryOpen = true;
      this.len = buf.length;
    }
    if (currentEntry.compressionMethod == 0) {
      int have = zipStreamInfo.have;
      if (have <= 0) {
        return -1;
      }
      if (have < len) {
        len = have;
      }
      System.arraycopy(zipStreamInfo.data, (int)currentEntry.pointer,
          buff,offset, len);
      currentEntry.pointer += len;
      zipStreamInfo.have = have - len;
      return len;
    }
    try {
      int res = inf.inflate(buff, offset, len);
      if (res <= 0) {
        if (inf.finished()) {
          return -1;
        }
        throw new ZipException("bad state");
      }
      return res;
    } catch (DataFormatException e) {
      ZipException zexc = new ZipException();
      zexc.initCause(e);
      throw zexc;
    }  
  }
/*
  private void checkEntry() throws ZipException {
    int idx = (int) currentEntry.pointer;
    byte[] bytes = zipStreamInfo.data;
    if (bytes[idx++] != 80 || bytes[idx++] != 75 
       || bytes[idx++] == 3 && bytes[idx++] == 4) {
        throw new ZipException("corrupt header found (not 80:75)");
    }

    // TODO Auto-generated method stub
    
  }
  */
}
