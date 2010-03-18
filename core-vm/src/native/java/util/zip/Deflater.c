/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: Deflater.c,v 1.4 2006/10/04 14:24:17 cvsroot Exp $
*/

#include <string.h>

#include "clazz.h"
#include "arrays.h"
#include "exception.h"
#include "heap.h"
#include "core-classes.h"
#include "deflate_driver.h"

void Deflater_updateLvl(JNIEnv *env, w_instance thisDeflater) {
  w_int lvl = getIntegerField(thisDeflater, F_Deflater_level);
  w_device zstream = getWotsitField(thisDeflater, F_Deflater_wotsit);
  woempa(7, "zstream = %p\n", zstream);
  if (lvl < 0 ) {
     lvl = 8;
  }
  if(zstream){
    deviceBSSet(zstream, wdi_set_compression_level , (w_word)lvl , x_millis2ticks(50));
  }
}

void Deflater_create(JNIEnv *env, w_instance thisDeflater) {
  w_device zstream = deviceBSOpen("zip_", wdp_none);
  woempa(7, "zstream = %p\n", zstream);
  setWotsitField(thisDeflater, F_Deflater_wotsit, zstream);
  Deflater_updateLvl(env, thisDeflater);
}

void Deflater_reset(JNIEnv *env, w_instance thisDeflater) {
  w_device zstream = getWotsitField(thisDeflater, F_Deflater_wotsit);
  woempa(7, "zstream = %p\n", zstream);
  if (zstream) {
    deviceBSSet(zstream, wdi_reset, 0, x_millis2ticks(50));
  }
}

void Deflater_finalize(JNIEnv *env, w_instance thisDeflater) {
  w_device zstream = getWotsitField(thisDeflater, F_Deflater_wotsit);
  woempa(7, "zstream = %p\n", zstream);
  if (zstream) {
    clearWotsitField(thisDeflater, F_Deflater_wotsit);
    deviceBSClose(zstream);
  }
}

void Deflater_end(JNIEnv *env, w_instance thisDeflater) {
  w_device zstream = getWotsitField(thisDeflater, F_Deflater_wotsit);
  woempa(7, "zstream = %p\n", zstream);
  if (zstream) {
    clearWotsitField(thisDeflater, F_Deflater_wotsit);
    deviceBSClose(zstream);
  }
}

void Deflater_setDictionary(JNIEnv *env, w_instance thisDeflater, w_instance byteArray, w_int off, w_int len) {
  w_thread thread = JNIEnv2w_thread(env);

  if (!byteArray) {
    throwException(thread, clazzNullPointerException, NULL);
  }
  else {
    if (off < 0 || len < 0 || off > instance2Array_length(byteArray) - len) {
      throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    }
    else {
      w_device zstream = getWotsitField(thisDeflater, F_Deflater_wotsit);
      woempa(7, "zstream = %p\n", zstream);
      if (zstream){
        w_sbyte * data = instance2Array_byte(byteArray);
        w_driver_status status;
        w_size l;

        data += off;
        while ((status = deviceBSSet(zstream, wdi_send_dictionary, 1, x_millis2ticks(50))) == wds_no_instance);
        if (status != wds_success) {
          woempa(9, "internal error occured while setting dictionary for %p\n",thisDeflater);
        }
        //the write will push the whole block in one go in the queue ...
        while ((status = deviceBSWrite(zstream, data, len, &l, x_millis2ticks(50))) == wds_no_instance);
        if (status != wds_success) {
          woempa(9, "internal error occured while setting dictionary for %p\n",thisDeflater);
        }
      }
    }
  }
}

void Deflater_setInput(JNIEnv *env, w_instance thisDeflater, w_instance byteArray, w_int off, w_int len) {
  w_device zstream = getWotsitField(thisDeflater, F_Deflater_wotsit);
  w_sbyte * data = instance2Array_byte(byteArray) + off;
  w_driver_status status;
  w_size l;

  woempa(7, "zstream = %p\n", zstream);
  woempa(7, "DEBUG setting input for %p \n",thisDeflater);
  while ((status = deviceBSWrite(zstream, data, len, &l, x_millis2ticks(500))) == wds_no_instance);
  woempa(7, "DEBUG status = %d\n",status);
  if (status != wds_success) {
    woempa(9, "internal error occured while setting input for %p (data is lost)\n",thisDeflater);
  }
}


w_int Deflater_deflate(JNIEnv *env, w_instance thisDeflater, w_instance byteArray, w_int off, w_int len) {
  w_thread thread = JNIEnv2w_thread(env);
  w_int ret=0;

  if (!getWotsitField(thisDeflater, F_Deflater_wotsit)) {
    return 0;
  }
  if (!byteArray) {
    throwException(thread, clazzNullPointerException, NULL);
  }
  else {
    if (off < 0 || len < 0 || off > instance2Array_length(byteArray) - len) {
      throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    }
    else {
      w_device zstream = getWotsitField(thisDeflater, F_Deflater_wotsit);
      w_sbyte * data = instance2Array_byte(byteArray);
      w_driver_status status;
      w_size l;

      woempa(7, "zstream = %p data = %p len = %d\n", zstream, data, len);
      if(len == 0) {
        return ret;
      }
      data += off;

      while((status = deviceBSRead(zstream, data, len, &l, x_millis2ticks(500))) == wds_no_instance){
        w_word reply;

        while((status = deviceBSQuery(zstream, wdi_get_need_more_input, &reply, 1)) == wds_no_instance) {
          // x_thread_sleep(1);
        }
        if(status != wds_success){
          w_printf("ERROR OCCURED in deflate - query status...\n");
        }
        ret += l;
        if(reply){
          woempa(7, "DEBUG - deflater needs more input\n");
          setBooleanField(thisDeflater, F_Deflater_needsInput, WONKA_TRUE);
          return ret;
        }
        data += l;
        len -= l;
      }
      if (status == wds_data_exhausted) {
        woempa(7, "DEBUG - deflater inflated all bytes (EOF)\n");
        setIntegerField(thisDeflater, F_Deflater_finished, 2);
      }
      else if (status == wds_internal_error) {
        w_printf("deflater had an internal error\n");
      }
      ret += l;
    }
  }                    
  return ret;  
}

void Deflater_finish(JNIEnv *env, w_instance thisDeflater) {
  w_device zstream = getWotsitField(thisDeflater, F_Deflater_wotsit);
  w_driver_status status;
  woempa(7, "zstream = %p\n", zstream);
  if (zstream) {
    while ((status = deviceBSSet(zstream, wdi_end_of_input, 0, x_millis2ticks(50))) == wds_no_instance);
    woempa(7, "DEBUG status = %d\n",status);
    setIntegerField(thisDeflater, F_Deflater_finished, 1);
  }
  woempa(7, "END zstream = %p\n", zstream);
}
