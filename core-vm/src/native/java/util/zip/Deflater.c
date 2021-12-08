/**************************************************************************
* Copyright (c) 2010, 2021 by KIFFER Ltd. All rights reserved.            *
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

void Deflater_updateLvl(w_thread thread, w_instance thisDeflater) {
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

void Deflater_create(w_thread thread, w_instance thisDeflater) {
  w_device zstream = deviceBSOpen("zip_", wdp_none);
  woempa(7, "zstream = %p\n", zstream);
  setWotsitField(thisDeflater, F_Deflater_wotsit, zstream);
  Deflater_updateLvl(thread, thisDeflater);
}

void Deflater_reset(w_thread thread, w_instance thisDeflater) {
  w_device zstream = getWotsitField(thisDeflater, F_Deflater_wotsit);
  woempa(7, "zstream = %p\n", zstream);
  if (zstream) {
    deviceBSSet(zstream, wdi_reset, 0, x_millis2ticks(50));
  }
}

void Deflater_finalize(w_thread thread, w_instance thisDeflater) {
  w_device zstream = getWotsitField(thisDeflater, F_Deflater_wotsit);
  woempa(7, "zstream = %p\n", zstream);
  if (zstream) {
    clearWotsitField(thisDeflater, F_Deflater_wotsit);
    deviceBSClose(zstream);
  }
}

void Deflater_end(w_thread thread, w_instance thisDeflater) {
  w_device zstream = getWotsitField(thisDeflater, F_Deflater_wotsit);
  woempa(7, "zstream = %p\n", zstream);
  if (zstream) {
    clearWotsitField(thisDeflater, F_Deflater_wotsit);
    deviceBSClose(zstream);
  }
}

void Deflater_setDictionary(w_thread thread, w_instance thisDeflater, w_instance byteArray, w_int off, w_int len) {

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

void Deflater_setInput(w_thread thread, w_instance thisDeflater, w_instance byteArray, w_int off, w_int len) {
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


w_int Deflater_deflate(w_thread thread, w_instance thisDeflater, w_instance byteArray, w_int off, w_int len) {
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
          throwException(thread, clazzZipException, "ERROR OCCURED in deflate - query status...");
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
        throwException(thread, clazzZipException, "deflater had an internal error");
      }
      ret += l;
    }
  }                    
  return ret;  
}

void Deflater_finish(w_thread thread, w_instance thisDeflater) {
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
