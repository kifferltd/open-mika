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
*                                                                         *
* Modifications Copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions, Antwerp, Belgium.  All rights reserved.                      *
*                                                                         *
**************************************************************************/

/*
** $Id: ZipFile.c,v 1.6 2006/10/04 14:24:17 cvsroot Exp $
*/

#include <string.h>

#include "clazz.h"
#include "dates.h"
#include "arrays.h"
#include "exception.h"
#include "heap.h"
#include "core-classes.h"
#include "new_deflate_internals.h"
#include "zipfile.h"


w_long ZipFile_bytesToLong(JNIEnv *env, w_instance Class, w_instance byteArray, w_int offset){
  w_long result=0;
  w_int length;
  w_ubyte* bytes;
  w_word w;

  if(!byteArray){
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  }
  else {
    length = (w_size)instance2Array_length(byteArray);

    if(offset < 0 || offset > length - 4){
      throwException(JNIEnv2w_thread(env), clazzArrayIndexOutOfBoundsException, NULL);
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

w_long ZipFile_getDate(JNIEnv *env, w_instance Class, w_instance byteArray, w_int offset){
  w_long result = 0;
  w_int length;
  w_ubyte* bytes;
  w_word w;
  w_Date date;

  if(!byteArray){
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  }
  else {
    length = (w_size)instance2Array_length(byteArray);

    if(offset < 0 || offset > length - 4){
      throwException(JNIEnv2w_thread(env), clazzArrayIndexOutOfBoundsException, NULL);
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
    wprintf("Failed to open a unzip device\n");
    return NULL;
  }

  // check if OK !!!
  if((s = deviceBSWrite(inflate_device, c_data, (signed)c_size, &lread, x_millis2ticks(5000)) != wds_success)) {
    wprintf("Failed to write data to unzip device %i\n",s);
  }

  u_data = allocClearedMem_with_retries(u_size, 5);	

  if(u_data == NULL){
    wprintf("Failed to allocate %d bytes for u_data\n", u_size);
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
    wprintf("  -UNZIP ERROR !!\n");
    releaseMem(u_data);
    deviceBSClose(inflate_device);
    return NULL;
  }

  if (s == wds_data_exhausted) {
    wprintf("  -AIAI, no full buffer received, we did not calculate the decompressed size correctly, or device errors\n");
  }
  else {
    if (offs != (signed)u_size) {
      wprintf("  -AIAI, there is still data left on the device, we did not calculate the decompressed size correctly\n");
      releaseMem(u_data);
      deviceBSClose(inflate_device);
      return NULL;
    }
  }
  deviceBSClose(inflate_device);

  if (d_crc != (-1 ^ update_ISO3309_CRC(-1, u_data, u_size))) {
    wprintf("crc failed\n");
    releaseMem(u_data);
    return NULL;
  }
  return u_data;
}
*/


w_instance ZipFile_quickInflate(JNIEnv *env, w_instance theClass, w_instance cData, w_int u_size, w_int crc){
  w_ubyte * u_data;
  w_ubyte * c_data;
  w_size c_size;
  w_instance uData = NULL;
  char * errmsg = NULL;
  w_thread thread = JNIEnv2w_thread(env);

  if(! cData){
    throwException(thread, clazzNullPointerException, NULL);
  }
  else {
    c_data = (w_ubyte*)instance2Array_byte(cData);
    c_size = (w_size)instance2Array_length(cData);

    uData = allocArrayInstance_1d(thread, atype2clazz[P_byte], u_size);

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


