/**************************************************************************
* Copyright (c) 2021 by KIFFER Ltd. All rights reserved.                  *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#include <string.h>

#include "clazz.h"
#include "dates.h"
#include "arrays.h"
#include "exception.h"
#include "heap.h"
#include "core-classes.h"
#include "new_deflate_internals.h"
#include "zipfile.h"


w_long ZipFile_bytesToLong(w_thread thread, w_instance Class, w_instance byteArray, w_int offset){
  w_long result=0;
  w_int length;
  w_ubyte* bytes;
  w_word w;

  if(!byteArray){
    throwException(thread, clazzNullPointerException, NULL);
  }
  else {
    length = (w_size)instance2Array_length(byteArray);

    if(offset < 0 || offset > length - 4){
      throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    }
    else {
      bytes = (w_ubyte*)instance2Array_byte(byteArray);
      w = bytes[offset++];
      w |= (bytes[offset++])<<8;
      w |= (bytes[offset++])<<16;
      w |= (bytes[offset++])<<24;
      result |= w;
    }
  }
  return result;
}

w_long ZipFile_getDate(w_thread thread, w_instance Class, w_instance byteArray, w_int offset){
  w_long result = 0;
  w_int length;
  w_ubyte* bytes;
  w_word w;
  w_Date date;

  if(!byteArray){
    throwException(thread, clazzNullPointerException, NULL);
  }
  else {
    length = (w_size)instance2Array_length(byteArray);

    if(offset < 0 || offset > length - 4){
      throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    }
    else {

      /**
      **	Note on the Time format:
      **		time(first 2 bytes):
      **			bit 0-4    : seconds / 2 	(0-29)
      **			bit 5-10   : minute        	(0-59)
      **			bit 11-15  : hour of 24h clock	(0-23)
      **		date(last 2 bytes) :
      **			bit 0-4    : day of month 	(1-31)
      **			bit 5-8    : month 		(1-12)
      **			bit 9-15   : year - 1980
      */	

      bytes = (w_ubyte*)instance2Array_byte(byteArray);
      w  = bytes[offset++];
      w |= (bytes[offset++])<<8;
      date.second = 2 * (w & 0x1f);
      date.minute = (w>>5) & 0x3f;
      w = (w>>11) * 60 + date.minute;
      w = w * 60 + date.second;
      date.msec = w * 1000;
      w  = bytes[offset++];
      w |= (bytes[offset++])<<8;
      date.day = w & 0x1f;
      date.month = ((w>>5) & 0x0f);
      date.year = (w>>9) + 1980;
      result = date2millis(&date) - MSEC1970;
    }
  }
  return result;
}

/*
w_ubyte* quickInflate(w_ubyte* c_data, w_size c_size, w_size u_size, w_word d_crc){
  w_int lread, offs;
  w_driver_status s;
  w_ubyte* u_data;
  w_device inflate_device;


  if((inflate_device = deviceBSOpen("unzip_", wdp_none)) == 0) {
    w_printf("Failed to open a unzip device\n");
    return NULL;
  }

  // check if OK !!!
  if((s = deviceBSWrite(inflate_device, c_data, (signed)c_size, &lread, x_millis2ticks(5000)) != wds_success)) {
    w_printf("Failed to write data to unzip device %i\n",s);
  }

  u_data = allocClearedMem_with_retries(u_size, 5);	

  if(u_data == NULL){
    w_printf("Failed to allocate %d bytes for u_data\n", u_size);
    deviceBSClose(inflate_device);
    return NULL;
  }

  s = wds_success;
  offs = 0;
  while (s == wds_success && (signed)u_size - offs > 0) {
    s = deviceBSRead(inflate_device, u_data + offs, (signed)(u_size - offs), &lread, x_millis2ticks(1000));
    offs += lread;
  }

  if (s == wds_internal_error) {
    w_printf("  -UNZIP ERROR !!\n");
    releaseMem(u_data);
    deviceBSClose(inflate_device);
    return NULL;
  }

  if (s == wds_data_exhausted) {
    w_printf("  -AIAI, no full buffer received, we did not calculate the decompressed size correctly, or device errors\n");
  }
  else {
    if (offs != (signed)u_size) {
      w_printf("  -AIAI, there is still data left on the device, we did not calculate the decompressed size correctly\n");
      releaseMem(u_data);
      deviceBSClose(inflate_device);
      return NULL;
    }
  }
  deviceBSClose(inflate_device);

  if (d_crc != (-1 ^ update_ISO3309_CRC(-1, u_data, u_size))) {
    w_printf("crc failed\n");
    releaseMem(u_data);
    return NULL;
  }
  return u_data;
}
*/


w_instance ZipFile_quickInflate(w_thread thread, w_instance theClass, w_instance cData, w_int u_size, w_int crc){
  w_ubyte * u_data;
  w_ubyte * c_data;
  w_size c_size;
  w_instance uData = NULL;
  char * errmsg = NULL;

  threadMustBeSafe(thread);
  if(! cData){
    throwException(thread, clazzNullPointerException, NULL);
  }
  else {
    c_data = (w_ubyte*)instance2Array_byte(cData);
    c_size = (w_size)instance2Array_length(cData);

    enterUnsafeRegion(thread);
    uData = allocArrayInstance_1d(thread, atype2clazz[P_byte], u_size);
    enterSafeRegion(thread);

    if(uData){
      u_data = (w_ubyte*)instance2Array_byte(uData);
      if(quickInflate(c_data, c_size, u_data, (w_size) u_size, (w_word) crc, &errmsg, WINF_KEEP_DATA)) {
        w_clazz exception_clazz = isSuperClass(clazzJarFile, Class2clazz(theClass)) ? clazzJarException : clazzZipException;
        throwException(thread, exception_clazz, errmsg ? errmsg : "quickInflate() returned NULL");
      }
    }
  }
  return uData;
}


