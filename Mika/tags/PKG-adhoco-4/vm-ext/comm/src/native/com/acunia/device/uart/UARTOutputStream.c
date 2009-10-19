/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
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

#include "arrays.h"
#include "core-classes.h"
#include "uart-classes.h"
#include "comms.h"
#include "hashtable.h"
#include "ts-mem.h"
#include "wstrings.h"
#include "threads.h"
#include "exception.h"
#include "driver_byteserial.h"

/* Helper function for object constructor
*/
/**native method
** createFromString
** Initialise an instance of UARTOutputStream using the named UART.
** Called from the constructor \textsf{UARTOutputStream(String)}.
*/
void
UARTOutputStream_createFromString
(  JNIEnv *env, w_instance thisOutputStream,
  w_instance nameString
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_string   nameStr;
  w_device commport;
  
  char       *name;

  if (!nameString) {
    throwNullPointerException(thread);
    return;
  }

  nameStr = String2string(nameString);
  name = string2UTF8(nameStr, NULL);
 
  commport = getDeviceByName(name + 2);
  if (commport) {
    if (commport->type != wdt_byte_serial) {
      woempa(9,"Device %s is of wrong type! (%d s/b %d)\n", name + 2, commport->type, wdt_byte_serial);
      commport = NULL;
    }
    else {
      setWotsitField(thisOutputStream, F_UARTOutputStream_wotsit, commport);
      if (deviceBSOpen(name + 2, wdp_write) == NULL) {
        throwIOException(thread);
      }
    }
  }
  else {
    throwException(thread,clazzIllegalArgumentException,NULL);
  }
  releaseMem(name);
}


/* Helper functions for read methods
*/
/**native method
** write(I)V                                 
** Write one byte to the output stream.
** 
*/
void
UARTOutputStream_write
(  JNIEnv *env, w_instance thisOutputStream,
  w_int oneByte
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_sbyte    b = oneByte & 0xff;
  w_size     dummy;
  w_device s = (w_device)getWotsitField(thisOutputStream, F_UARTOutputStream_wotsit);

  if(getWotsitField(thisOutputStream, F_UARTOutputStream_open) == WONKA_FALSE){
    throwIOException(thread);
    return;
  }

  if (deviceBSWrite(s, &b, 1, &dummy, x_eternal) != wds_success) {
    throwIOException(thread);
  }

}

/**native method
** readIntoBuffer([BII)I                    
** Write bytes to the output stream from an array.
*/
void
UARTOutputStream_writeFromBuffer
(  JNIEnv *env, w_instance thisOutputStream,
  w_instance ByteArray, w_int offset, w_int length
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device s = (w_device)getWotsitField(thisOutputStream, F_UARTOutputStream_wotsit);
  w_sbyte    *byte_array = instance2Array_byte(ByteArray);
  w_sbyte    *dest = byte_array + offset;
  w_int     dummy;

  if(getWotsitField(thisOutputStream, F_UARTOutputStream_open) == WONKA_FALSE){
    throwIOException(thread);
    return;
  }

  if (length<0 || offset<0 || offset > instance2Array_length(ByteArray) - length) {
    throwArrayIndexOutOfBoundsException(thread);
    return;
  }

  do {
    if (deviceBSWrite(s, dest, length, &dummy, x_eternal) != wds_success) {
      throwIOException(thread);
      return;
    }
    if (dummy == length) {
      break;
    }
    length -= dummy;
    dest += dummy; 
  } while (1);
}

/**native method
** flush()V                    
** Currently does nothing whatever.
*/
void
UARTOutputStream_flush
(  JNIEnv *env, w_instance thisOutputStream
) {
}

/**native method
** close0()V
** Ask the underlying driver to close the device for output.
*/
void
UARTOutputStream_close0
(  JNIEnv *env, w_instance thisOutputStream
) {
  w_device s = (w_device)getWotsitField(thisOutputStream, F_UARTOutputStream_wotsit);

  if (s) {
    deviceBSClose(s);
    clearWotsitField(thisOutputStream, F_UARTOutputStream_wotsit);
  }
}

