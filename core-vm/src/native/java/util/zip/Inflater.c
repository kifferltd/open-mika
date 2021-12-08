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
#include "arrays.h"
#include "exception.h"
#include "heap.h"
#include "core-classes.h"
#include "deflate_driver.h"

void Inflater_create(w_thread thread, w_instance thisInflater) {
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

void Inflater_reset(w_thread thread, w_instance thisInflater) {
  w_device zstream = getWotsitField(thisInflater, F_Inflater_wotsit);
  woempa(6,"DEBUG - resetting inflater %j\n", thisInflater);
  if (zstream) {
    woempa(6, "reseting inflater\n");
    deviceBSSet(zstream, wdi_reset, 0, x_millis2ticks(250));
  }
}

void Inflater_finalize(w_thread thread, w_instance thisInflater) {
  Inflater_end(thread, thisInflater);
}

void Inflater_end(w_thread thread, w_instance thisInflater) {
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

void Inflater_setDictionary(w_thread thread, w_instance thisInflater, w_instance byteArray, w_int off, w_int len) {

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
        while ((status = deviceBSSet(zstream, wdi_send_dictionary, 1, x_millis2ticks(250))) == wds_no_instance);
        if (status != wds_success) {
          w_printf("internal error occured while setting dictionary for %p\n",thisInflater);
        }
        //the write will push the whole block in one go in the queue ...
        status = deviceBSWrite(zstream, (w_ubyte*)data, len, &l, x_eternal);
        if (status == wds_success) {
          setBooleanField(thisInflater, F_Inflater_needsDict, WONKA_FALSE);
          setIntegerField(thisInflater, F_Inflater_dictAdler, 0);
        }
        else {
          w_printf("internal error occured while setting dictionary for %p\n",thisInflater);
        }
      }
    }
  }
}

void Inflater_setInput(w_thread thread, w_instance thisInflater, w_instance byteArray, w_int off, w_int len) {
  w_device zstream = getWotsitField(thisInflater, F_Inflater_wotsit);
  w_sbyte * data = instance2Array_byte(byteArray) + off;
  w_driver_status status;
  w_int l;

  woempa(6, "DEBUG setting input for %p \n",thisInflater);
  status = deviceBSWrite(zstream, (w_ubyte*)data, len, &l, x_eternal);
  if (status != wds_success) {
    w_printf("internal error occured while setting input for %p (data is lost)\n",thisInflater);
  }
}

#define UNZIP_READ_TIMEOUT 2

w_int Inflater_inflate(w_thread thread, w_instance thisInflater, w_instance byteArray, w_int off, w_int len) {
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
        w_printf("DEBUG - inflater had an internal error\n");
        throwException(thread, clazzDataFormatException, NULL);
      }
      ret += l;
    }
  }
  return ret;  
}

w_int Inflater_getRemaining(w_thread thread, w_instance thisInflater) {
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

