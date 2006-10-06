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
** $Id: Inflater.c,v 1.7 2006/10/04 14:24:17 cvsroot Exp $
*/

#include <string.h>

#include "clazz.h"
#include "arrays.h"
#include "exception.h"
#include "heap.h"
#include "core-classes.h"
#include "deflate_driver.h"

void Inflater_create(JNIEnv *env, w_instance thisInflater) {
  woempa(6,"DEBUG - creating new inflater\n");
  {
    w_device unzipper = deviceBSOpen("unzip_", wdp_none);
    while(!unzipper) {
      woempa(9, "Inflater_create: no device available, retrying\n");
      ++*gc_kicks_pointer;
      x_monitor_enter(gc_monitor, x_eternal);
      x_monitor_notify_all(gc_monitor);
      x_monitor_exit(gc_monitor);
      x_thread_sleep(8);
      unzipper = deviceBSOpen("unzip_", wdp_none);
    }
    woempa(6,"DEBUG - created new inflater (%p)\n",unzipper);
    deviceBSSet(unzipper, wdi_set_no_auto, 1, 0);
    setWotsitField(thisInflater, F_Inflater_wotsit, unzipper);
    woempa(6,"DEBUG - create finished - new inflater (%p)\n",unzipper);
  }
}

void Inflater_reset(JNIEnv *env, w_instance thisInflater) {
  w_device zstream = getWotsitField(thisInflater, F_Inflater_wotsit);
  woempa(6,"DEBUG - resetting inflater %j\n", thisInflater);
  if (zstream) {
    woempa(6, "reseting inflater\n");
    deviceBSSet(zstream, wdi_reset, 0, x_millis2ticks(50));
  }
}

void Inflater_finalize(JNIEnv *env, w_instance thisInflater) {
  Inflater_end(env, thisInflater);
}

void Inflater_end(JNIEnv *env, w_instance thisInflater) {
  woempa(6,"DEBUG - ending inflater %j\n", thisInflater);
  {
    w_device zstream = getWotsitField(thisInflater, F_Inflater_wotsit);
    if (zstream) {
      w_word reply;

      deviceBSQuery(zstream, wdi_get_processed_size, &reply, 0);
      setIntegerField(thisInflater, F_Inflater_remain, getIntegerField(thisInflater, F_Inflater_totalIn) - (w_int)reply - getIntegerField(thisInflater, F_Inflater_remain));
      clearWotsitField(thisInflater, F_Inflater_wotsit);
      deviceBSClose(zstream);
    }
  }
}

void Inflater_setDictionary(JNIEnv *env, w_instance thisInflater, w_instance byteArray, w_int off, w_int len) {
  w_thread thread = JNIEnv2w_thread(env);

  if (!byteArray) {
    throwException(thread, clazzNullPointerException, NULL);
  }
  else {
    if (off < 0 || len < 0 || off > instance2Array_length(byteArray) - len) {
      throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    }
    else {
      w_device zstream = getWotsitField(thisInflater, F_Inflater_wotsit);
      w_sbyte * data = instance2Array_byte(byteArray) + off;

      if (zstream) {
        w_int l;
        w_driver_status status;
        while ((status = deviceBSSet(zstream, wdi_send_dictionary, 1, x_millis2ticks(50))) == wds_no_instance);
        if (status != wds_success) {
          wprintf("internal error occured while setting dictionary for %p\n",thisInflater);
        }
        //the write will push the whole block in one go in the queue ...
        status = deviceBSWrite(zstream, (w_ubyte*)data, len, &l, x_eternal);
        if (status == wds_success) {
          setBooleanField(thisInflater, F_Inflater_needsDict, WONKA_FALSE);
          setIntegerField(thisInflater, F_Inflater_dictAdler, 0);
        }
        else {
          wprintf("internal error occured while setting dictionary for %p\n",thisInflater);
        }
      }
    }
  }
}

void Inflater_setInput(JNIEnv *env, w_instance thisInflater, w_instance byteArray, w_int off, w_int len) {
  w_device zstream = getWotsitField(thisInflater, F_Inflater_wotsit);
  w_sbyte * data = instance2Array_byte(byteArray) + off;
  w_driver_status status;
  w_int l;

  woempa(6, "DEBUG setting input for %p \n",thisInflater);
  status = deviceBSWrite(zstream, (w_ubyte*)data, len, &l, x_eternal);
  if (status != wds_success) {
    wprintf("internal error occured while setting input for %p (data is lost)\n",thisInflater);
  }
}

#define UNZIP_READ_TIMEOUT 2

w_int Inflater_inflate(JNIEnv *env, w_instance thisInflater, w_instance byteArray, w_int off, w_int len) {
  w_thread thread = JNIEnv2w_thread(env);
  w_int ret=0;

  if (!getWotsitField(thisInflater, F_Inflater_wotsit)) {
    return -1;
  }
  if (!byteArray) {
    throwException(thread, clazzNullPointerException, NULL);
  }
  else if (off < 0 || len < 0 || off > instance2Array_length(byteArray) - len) {
    throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
  }
  else {
    if (len == 0){
      return 0;
    }
    else{
      w_device zstream = getWotsitField(thisInflater, F_Inflater_wotsit);
      w_sbyte * data = instance2Array_byte(byteArray);
      w_driver_status status;
      w_int l;

      woempa(6, "DEBUG - inflating bytes\n");
      data += off;
      while((status = deviceBSRead(zstream, (w_ubyte*)data, len, &l, UNZIP_READ_TIMEOUT)) == wds_no_instance){
        w_word reply;

        deviceBSQuery(zstream, wdi_get_need_more_input, &reply, 0);
        ret += l;
        if(reply){
          woempa(6, "DEBUG - inflater needs more input\n");
          setBooleanField(thisInflater, F_Inflater_needsInput, WONKA_TRUE);
          return ret;
        }
        data += l;
        len -= l;
      }
      if (status == wds_data_exhausted) {
        woempa(6, "DEBUG - inflater inflated all bytes (EOF)\n");
        setBooleanField(thisInflater, F_Inflater_finished, WONKA_TRUE);
      }
      else if (status == wds_internal_error) {
        wprintf("DEBUG - inflater had an internal error\n");
        throwException(thread, clazzDataFormatException, NULL);
      }
      ret += l;
    }
  }
  return ret;  
}

w_int Inflater_getRemaining(JNIEnv *env, w_instance thisInflater) {
  w_device zstream = getWotsitField(thisInflater, F_Inflater_wotsit);
  if (zstream) {
    w_word reply;
    w_int ret;
    deviceBSQuery(zstream, wdi_get_processed_size, &reply, 0);
    ret = getIntegerField(thisInflater, F_Inflater_totalIn) - (w_int)reply - getIntegerField(thisInflater, F_Inflater_remain);
    woempa(6, "getRemaining returns %i (bytes processed %i, in = %i, rem = %i)\n",ret, reply,
      getIntegerField(thisInflater, F_Inflater_totalIn), getIntegerField(thisInflater, F_Inflater_remain));
   return ret;
  }
  else {
    woempa(6, "getRemaining returns %i\n",getIntegerField(thisInflater, F_Inflater_remain));
    return getIntegerField(thisInflater, F_Inflater_remain);
  }
}

