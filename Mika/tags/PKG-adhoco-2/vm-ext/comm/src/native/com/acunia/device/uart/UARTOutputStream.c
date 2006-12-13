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
** $Id: UARTOutputStream.c,v 1.5 2006/10/04 14:24:21 cvsroot Exp $
*/

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
    throwException(thread,clazzNullPointerException,NULL);
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
        throwException(thread,clazzIOException,NULL);
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
    throwException(thread,clazzIOException,NULL);
    return;
  }

  if (deviceBSWrite(s, &b, 1, &dummy, x_eternal) != wds_success) {
    throwException(thread,clazzIOException,NULL);
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
    throwException(thread,clazzIOException,"stream is closed %x!",getWotsitField(thisOutputStream, F_UARTOutputStream_open));
    return;
  }

  if (length<0 || offset<0 || offset > instance2Array_length(ByteArray) - length) {
    throwException(thread,clazzArrayIndexOutOfBoundsException,NULL);
    return;
  }

  do {
    if (deviceBSWrite(s, dest, length, &dummy, x_eternal) != wds_success) {
      throwException(thread,clazzIOException,"failed to write !");
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

