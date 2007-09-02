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
 * $Id: ZipFile.java,v 1.4 2006/06/09 09:43:34 cvs Exp $
 */

package java.util.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;

/**
 ** Class for manipulating zip files.
 ** IMPORTANT: if you add any set...() methods to this class, be sure to
 ** override them in com.acunia.wonka.net.jar.ImmutableJarFile!
 */
public class ZipFile implements ZipConstants {

  public static final int OPEN_READ = 1;
  public static final int OPEN_DELETE = 4;

  private final static byte CDS0 = endCenDirS[0];
  private final static byte CDS1 = endCenDirS[1];
  private final static byte CDS2 = endCenDirS[2];
  private final static byte CDS3 = endCenDirS[3];

  private final static byte FHS0 = cenFileHeaderS[0];
  private final static byte FHS1 = cenFileHeaderS[1];
  private final static byte FHS2 = cenFileHeaderS[2];
  private final static byte FHS3 = cenFileHeaderS[3];

  private RandomAccessFile raf;
  private String name;

  private int nrEntries;
  String[] strings;
  long[] longs;
  int capacity;

//  static {
    //only to avoid bootstrapping problems ...
//    new java.util.jar.JarEntry("");
//  }

  public ZipFile (String name) throws IOException {
  	this.name = name;
  	this.raf = new RandomAccessFile(name, "r");
  	getEntries();
  	if (strings == null){
 	   	throw new ZipException("Bad or No ZipFile ");
  	}
  }

  public ZipFile (File file) throws ZipException, IOException {
  	this.name = file.getPath();
  	this.raf = new RandomAccessFile(file, "r");
  	getEntries();
  	if (strings == null){
 	   	throw new ZipException("Bad or No ZipFile ");
  	}
  }

  public ZipFile (File file, int mode) throws ZipException, IOException {
     this(file);
     if(mode == OPEN_DELETE){
       throw new IllegalArgumentException("OPEN_DELELTE: Unsupported feature");
     }
  }

  public void close() throws IOException {
   	nrEntries = 0;
   	capacity = 1;
   	strings = new String[1];
   	longs = null;
  }

  public Enumeration entries(){
  	return new Enum();
  }

  protected void finalize() throws IOException {
    close();
  }

  public ZipEntry getEntry(String zname) {
    int   hash = zname.hashCode() % capacity;
    while(true) {
      if(hash < 0){
        hash += capacity;
      }
      if(strings[hash] == null) {
        return null;
      }
      else if(zname.equals(strings[hash])) {
        break;
      }
      hash--;
    }
	  try {
    	return new ZipEntry(zname, longs[hash], this);
	  }
 	  catch(Exception e){}
	  return null;
  }

  public String getName(){
   	return name;
  }

  public InputStream getInputStream(ZipEntry ze) throws IOException{
    //check if entry belongs to this zipFile!
    if(ze.zipFile != this){
      throw new IOException("entry " + ze + " doesn't not belong to file " + this);
    }
    if(ze.initPointer != 0) {
      createEntry(ze);
    }
    return new ZipByteArrayInputStream(checkHeader(ze));
  }

  /**
  ** since jdk 1.2 ...
  */
  public int size() {
   	return nrEntries;
  }	

  /**
  ** called from constructor ... (no synchronization needed on raf)!
  */
  private void getEntries() throws IOException {
   	//our file system uses buffers of at least 1024 bytes
   	long pos = raf.length();
        wonka.vm.Etc.woempa(7, "raf.length() = " + pos);
   	int size = (1024 > pos ? (int)pos : 1024);
   	byte [] bytes = new byte[size];
   	while (pos > 0) {
   		pos -= size;
        wonka.vm.Etc.woempa(7, "seeking to offset " + pos + " and reading " + size + " bytes");
   		raf.seek(pos);
   		raf.readFully(bytes,0,size);
   		size = locateEndCD(bytes, size);
        wonka.vm.Etc.woempa(7, "read " + size + " bytes");
   		if (size == -1) {
   		 	break;
   		}
   		if (size > 0) {
   		 	System.arraycopy(bytes,0,bytes,bytes.length-size,size);
   		}
     	size = (1024 > pos ? (int)pos : 1024)-size;
   	}
  }

  /**
  ** called from within constructor ... (no synchronization needed on raf)!
  */
  private int locateEndCD(byte[] bytes, int size) throws IOException {
    //we start looking for  endCenDirS
    for (int p=size  ; --p > -1 ; ) {
      if (bytes[p] == CDS3) {
      //this might be the last byte of the signature!
        if (p >=3) {
          if (bytes[p-1] == CDS2 && bytes[p-2] == CDS1 && bytes[p-3] == CDS0) {
            //we have found a signature ...
            if(readEntries(bytes, size, p+1)){
              return -1;
            }
          }
        }
        else {
          return p;         		
        } 	
      }
    }
    return 0;
  }

  /**
  ** called from constructor ... (no synchronization needed on raf)!
  */
  private boolean readEntries(byte [] bytes, int size, int pos) throws IOException {
   	int nrE = size - pos;
   	nrE = (16 > nrE ? nrE : 16);
	  byte [] b = new byte [16];
   	System.arraycopy(bytes, pos, b,0, nrE);
   	if (nrE < 16){
   		raf.readFully(b,nrE,16-nrE);
   	}
   	//b should contain 16 bytes of the possible header
   	nrE = (0x0ff&(char)b[6]) + (0x0ff &(char)b[7])*256;
   	long stP = bytesToLong(b, 12);
  	size = (int)bytesToLong(b, 8);
   	if (nrE < 0 || (stP+size) > raf.length() || size < 0) {
   		// we encountered a fony header
  		return false;
   	}
   	raf.seek(stP);
   	b = new byte[size];
   	raf.readFully(b);
   	pos=0;
   	
   	//creating internal hastable structure.
   	int capacity = ((int)(nrE/0.74f)) + 1;
   	this.capacity = capacity;
    strings = new String[capacity];
    longs = new long[capacity];
    //end of creation.
       	
   	for (int i=0; i < nrE ; i++) {
      if (pos+46 > size) {
   		 	throw new ZipException("too few bytes for header");//bad Header ...   			
      }
     	if (FHS0 != b[pos] || FHS1 != b[pos+1] || FHS2 != b[pos+2] || FHS3 != b[pos+3]){
          throw new ZipException("bad header in central file directory");//bad Header ...
   		}
   		long pointer = stP + pos;
   		// we have found a correct header ...
   		// lets skip useless bytes
   		pos+=28;
   		int len = (0x0ff&(char)b[pos]) + (0x0ff &(char)b[pos+1])*256;
   		pos+=18;
   		if (pos+len > size) {
   		 	throw new ZipException("not enough bytes in header");
   		}

   		String name = new String(b,pos,len);
   		pos-=36;
   		int hlp = (0x0ff&(char)b[pos]) + (0x0ff &(char)b[pos+1])*256;
   		if (hlp != 0 && hlp != 8) {
   		 	throw new ZipException("unknown store/zip method "+hlp);
   		}
      pos += 20;

   		hlp = (0x0ff&(char)b[pos]) + (0x0ff &(char)b[pos+1])*256;
   		pos+=2;
   		int com = (0x0ff&(char)b[pos]) + (0x0ff &(char)b[pos+1])*256;
   		if(hlp < 0 || com < 0 || pos+14+len+hlp+com > size) {
   		 	throw new ZipException("invalid header data");
   		}
   		pos+=10;
      //inlined put method.
      int hashcode = name.hashCode() % capacity;
      while(true){
        if(hashcode < 0){
          hashcode += capacity;
        }
        if(strings[hashcode] == null) {
          strings[hashcode] = name;
          longs[hashcode] = pointer;
          break;
        }
        else if(name.equals(strings[hashcode])) {
          throw new IOException("duplicate entry found "+name);
        }
        hashcode--;
      }
   		//end put method.
   		
   		pos+= 4+len;
   		if (hlp > 0) {
   		 	pos += hlp;
   		}
   		if (com > 0) {
   		 	pos += com;
   		}
   	}
   	nrEntries = nrE;
   	return true;
  }

  private byte[] checkHeader(ZipEntry ze) throws IOException {
    if (ze.data != null) {
      return ze.data;
    }
    synchronized(raf){
      raf.seek(ze.pointer);
     	byte [] header = new byte [30];
      raf.readFully(header,0,30);
    	if ( header[0] != locFileHeaderS[0] || header[1] != locFileHeaderS[1]
    	  || header[2] != locFileHeaderS[2] || header[3] != locFileHeaderS[3]) {
    	 	throw new ZipException("corrupt header found");
    	}

    	// we have 30 bytes --> 4 are already checked  Signature
    	// version needed --> ... (2 bytes)
    	// general purpose bytes (2)
    	int mode = (0x0ff&(char)header[4]) + (0x0ff &(char)header[5])*256;
    	if (mode > 20) {
    		throw new ZipException("higher zip version needed");
    	}
    	mode = (0x08 & header[6]);
    	int fnlen = (0x0ff & header[26]) + (0x0ff & header[27])*256;
    	byte [] fname = new byte[fnlen];

    	raf.readFully(fname,0, fnlen);
    	String name = new String(fname, 0, fnlen);
    	if (!ze.name.equals(name)) {
    	   throw new ZipException("entry pointed to wrong data '" + name + "' != '" + ze.name + "'");
    	}
    	fnlen = (0x0ff&(char)header[28]) + (0x0ff &(char)header[29])*256;
    	fname = new byte[fnlen];
    	if (fnlen != 0) {
    	  raf.readFully(fname,0, fnlen);
    	}

    	mode = (0x0ff&(char)header[8]) + (0x0ff &(char)header[9])*256;

      if (mode != 8 && mode != 0) {
  	throw new ZipException("stream is corrupted");
      }

      if (mode == 8) {
      	header = new byte[(int) ze.compressedSize];
      	raf.readFully(header);
        header = quickInflate(header, (int)ze.size,(int)ze.crc);
      }
      else {
         	header = new byte[(int) ze.size];
         	raf.readFully(header);
      }
      ze.data = header;
      return header;
    	// the rest of the bytes are not verified
    }
  }

  void createEntry(ZipEntry ze) throws IOException {
		synchronized(raf){
  		raf.seek(ze.initPointer);
  		int pos = 0;
  		byte[] b = new byte[46];
  		raf.readFully(b);
  		if (FHS0 != b[pos] || FHS1 != b[pos+1] || FHS2 != b[pos+2] || FHS3 != b[pos+3]){

  			throw new ZipException("bad header in central file directory");//bad Header ...
  		}
  		// we have found a correct header ...
  		// lets skip useless bytes
  		pos+=28;
  		int len = (0x0ff&(char)b[pos]) + (0x0ff &(char)b[pos+1])*256;
  		pos+=18;
  		byte[] name = new byte[len];
  		raf.readFully(name);
  		pos-=36;
  		int hlp = (0x0ff&(char)b[pos]) + (0x0ff &(char)b[pos+1])*256;
  		if (hlp != 0 && hlp != 8) {
  		 	throw new ZipException("unknown store/zip method "+hlp);
  		}
  		ze.method = hlp;
  		pos+=2;
  		ze.time = getDate(b,pos);
  		pos+=4;
  		ze.crc = bytesToLong(b,pos);
  		pos+=4;
  		ze.compressedSize = bytesToLong(b,pos);
  		pos+=4;
  		ze.size = bytesToLong(b,pos);
  		pos+=6;
  		hlp = (0x0ff&(char)b[pos]) + (0x0ff &(char)b[pos+1])*256;
  		pos+=2;
  		int com = (0x0ff&(char)b[pos]) + (0x0ff &(char)b[pos+1])*256;
  		if(hlp < 0 || com < 0) {
  		 	throw new ZipException("invalid header data");
  		}
  		pos += 10;
  		ze.pointer = bytesToLong(b,pos);
  		if (hlp > 0) {
    	 	byte [] extra = new byte[hlp];
  		 	raf.readFully(extra);
  		 	ze.extra = extra;
  		}
  		if (com > 0) {
        byte[] comment = new byte[com];
  		 	raf.readFully(comment);
        ze.comment = new String(comment);
  		}
  		ze.initPointer = 0;
    }
  }

  private class Enum implements Enumeration  {

    int index = -1;

    Enum(){
      for(int i=0 ; i < capacity ; i++){
        if(strings[i] != null){
          index = i;
          break;
        }
      }
    }

    public boolean hasMoreElements() {
     	return index != -1;
    }

    public Object nextElement(){
      if(index == -1){
        throw new java.util.NoSuchElementException();
      }
      String ename = strings[index];
      int i=index+1;
      index = -1;
      for( ; i < capacity ; i++){
        if(strings[i] != null){
          index = i;
          break;
        }
      }
      try {
        return getEntry(ename);
      }
      catch(Exception e){
        return null;
      }
    }
  }

  static native byte[] quickInflate(byte[] cData, int uSize, int CRC) throws ZipException;

/**
**	these methods are static (default acces) so ZipInputStream is able call it
*/	
  native static long bytesToLong(byte [] header, int offset);
  native static long getDate(byte [] header, int offset);

}
