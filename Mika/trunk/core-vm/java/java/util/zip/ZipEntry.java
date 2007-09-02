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
 * $Id: ZipEntry.java,v 1.3 2006/06/10 15:03:11 cvs Exp $
 */
package java.util.zip;

import java.io.IOException;

public class ZipEntry implements ZipConstants, Cloneable {

  public static final int DEFLATED=8;
  public static final int STORED=0;	

  String comment;
  String  name;
  int method=-1;
  long time=-1;
  long crc=-1;
  long size=-1; //uncompressed size
  long compressedSize=-1;
  byte[] extra;//extra data...
  byte[] data;

  /**
  ** these fields is only used by ZipFile !
  */
  long pointer;   // position of Header in ZipFile
  ZipFile zipFile;// allows a zipFile to check if the Entry was created on its behalf
  long initPointer;

  ZipEntry(String name, long index, ZipFile zf) {
    this.name = name;
    initPointer = index;
    zipFile = zf;
  }

  public ZipEntry(ZipEntry ze) {
    this.name = ze.name;
    this.comment = ze.comment;
    this.method = ze.method;
    this.time = ze.time;
    this.crc = ze.crc;
    this.size = ze.size;
    this.compressedSize = ze.compressedSize;
    this.extra = ze.extra;
    this.pointer = ze.pointer;
    this.zipFile = ze.zipFile;
    this.initPointer = ze.initPointer;
  }
  
  public ZipEntry(String zname) {
  	if (zname.length() > 65535) {
      throw new IllegalArgumentException();
  	}
  	name = zname;
  }

  public String getComment() {
   	return comment;
  }

  public long getCompressedSize(){
    if (initPointer != 0) {
      init();
    }
   	return compressedSize;
  }

  public long getCrc(){
    if (initPointer != 0) {
      init();
    }
   	return crc;
  }

  public byte[] getExtra() {
    if (initPointer != 0) {
      init();
    }
   	return extra;
  }

  public int getMethod(){
    if (initPointer != 0) {
      init();
    }
   	return method;
  }

   public String getName() {
    return this.name;
  }

  public long getSize() {
    if (initPointer != 0) {
      init();
    }
  	return size;
  }

  public long getTime() {
    if (initPointer != 0) {
      init();
    }
   	return time;
  }

  public boolean isDirectory() {
  	return name.endsWith("/");
  }

  public void setComment(String cmnt) {
 	if(cmnt != null) {
 		if (cmnt.length() > 65535){
  			throw new IllegalArgumentException();   	
  	 	}
  	}
  	comment = cmnt;
  }

  public void setCrc(long CRC) {
   	crc = CRC;
  }

  public void setExtra(byte[] xtra) {
   	if (xtra != null) {
   	 	if (xtra.length > 65535){
   	 	 	throw new IllegalArgumentException();
   	 	}
   	}
   	extra = xtra;
  }

  public void setMethod(int m) {
   	if (m != STORED && m != DEFLATED) {
   	 	throw new IllegalArgumentException();
   	}
   	method = m;   	
  }

  public void setSize(long sz) {
   	if (sz < 0 ) {
   	 	throw new IllegalArgumentException();
   	}
   	size = sz;
  }

  public void setTime(long t) {
   	time = t;
  }

  public void setCompressedSize(long cpsize){
   	compressedSize = cpsize;
  }

  public Object clone(){
    try {
      ZipEntry ze = (ZipEntry)super.clone();
      ze.extra = (byte[]) extra.clone();
      return ze;
    }
    catch(CloneNotSupportedException cnse){
      return null;
    }

  }

  private void init(){
      try {
        zipFile.createEntry(this);
      }
      catch(IOException ioe){}
        initPointer = 0;
  }
}

