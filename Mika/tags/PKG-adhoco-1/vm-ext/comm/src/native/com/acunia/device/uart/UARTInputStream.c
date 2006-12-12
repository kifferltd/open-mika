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
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/


/*
** $Id: UARTInputStream.c,v 1.6 2006/10/04 14:24:21 cvsroot Exp $
*/

#include "arrays.h"
#include "clazz.h"
#include "core-classes.h"
#include "uart-classes.h"
#include "comms.h"
#include "exception.h"
#include "hashtable.h"
#include "heap.h"
#include "ts-mem.h"
#include "wstrings.h"
#include "threads.h"
#include "driver_byteserial.h"

/*
** Create an InputStream by name
** native method
** @name createFromString
** Initialise an instance of UARTInputStream using the named UART.
** Called from the constructor \textsf{UARTInputStream(String)}.
*/
void
UARTInputStream_createFromString
(  JNIEnv *env, w_instance thisInputStream,
  w_instance nameString
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_string   nameStr;
  w_device commport;
  
  char    *name;

  if (!nameString) {
    throwException(thread,clazzNullPointerException,NULL);
    return;
  }

  nameStr = String2string(nameString);
  name = string2UTF8(nameStr, NULL);
 
  commport = getDeviceByName(name + 2);
  if (commport->type != wdt_byte_serial) {
    woempa(9,"Device %s is of wrong type! (%d s/b %d)\n", name + 2, commport->type, wdt_byte_serial);
    commport = NULL;
  }
  if (commport) {
    setWotsitField(thisInputStream, F_UARTInputStream_wotsit, commport);
    if (deviceBSOpen(name + 2, wdp_read) == NULL) {
      throwException(thread,clazzIOException,NULL);
    }
  }
  else {
    throwException(thread,clazzIllegalArgumentException,NULL);
  }
  releaseMem(name);
}

/* Read many bytes                 
** native method
** readIntoBuffer([BII)I                    
** Read bytes from the input stream into an array.
*/
w_int
UARTInputStream_readIntoBuffer
(  JNIEnv *env, w_instance thisInputStream,
  w_instance ByteArray, w_int offset, w_int length
) {
  w_thread     thread = JNIEnv2w_thread(env);
  w_device   s = (w_device)getWotsitField(thisInputStream, F_UARTInputStream_wotsit);
  w_sbyte      *byte_array  = instance2Array_byte(ByteArray);
  w_sbyte      *dest  = byte_array + offset;
  w_size       lread;

  if(getWotsitField(thisInputStream, F_UARTInputStream_wotsit) == WONKA_FALSE){
    throwException(thread,clazzIOException,NULL);
    return -1;
  }

  if (length<0 || offset<0 || offset > instance2Array_length(ByteArray) - length) {
    throwException(thread,clazzArrayIndexOutOfBoundsException,NULL);
    return -1;
  }

  
  if (deviceBSRead(s, dest, length, &lread, x_eternal) != wds_success) {
    throwException(thread,clazzIOException,NULL);
  }

  return lread;
}


/* native method
** skip0(L)L                    
** Skip bytes from the input stream.
** We do this by asking the underlying driver to ``read into nowhere''.
*/
w_long
UARTInputStream_skip0
(  JNIEnv *env, w_instance thisInputStream,
  w_long n
) {
  w_thread     thread = JNIEnv2w_thread(env);
  w_device   s = (w_device)getWotsitField(thisInputStream, F_UARTInputStream_wotsit);
  w_long       l = 0;
  w_size       lread;
  w_int        rc = wds_success;
  w_ubyte      *buffer = allocMem((w_size)(l > 32768 ? 32768 : l));

  if(getWotsitField(thisInputStream, F_UARTInputStream_open) == WONKA_FALSE){
    throwException(thread,clazzIOException,NULL);
    return -1;
  }

  for (l=0;l<n-32767 && rc==wds_success;l+=32767) {
    rc = deviceBSRead(s, buffer, 32767, &lread, x_eternal);
  }
  rc = deviceBSRead(s, buffer, (w_int)l, &lread, x_eternal);

  if (rc != wds_success) {
    throwException(thread,clazzIOException,NULL);
  }

  releaseMem(buffer);
  
  return l;
}

/* native method
** @name available()I                    
** Ask the underlying driver how many bytes of input are waiting to be read.
*/
w_int
UARTInputStream_available
(  JNIEnv *env, w_instance thisInputStream
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device s = (w_device)getWotsitField(thisInputStream, F_UARTInputStream_wotsit);
  w_word     reply;

  if(getWotsitField(thisInputStream, F_UARTInputStream_open) == WONKA_FALSE){
    throwException(thread,clazzIOException,"Stream is closed");
    return 0;
  }

  if(deviceBSQuery(s, wdi_get_available, &reply, x_eternal) != wds_success) {
    throwException(thread,clazzIOException,"query failed");
  }

  return reply;

}

/* native method
** close0()V
** Ask the underlying driver to close the device for input.
*/
void
UARTInputStream_close0
(  JNIEnv *env, w_instance thisInputStream
) {
  w_device s = (w_device)getWotsitField(thisInputStream, F_UARTInputStream_wotsit);

  if (s) {
    deviceBSClose(s);
    clearWotsitField(thisInputStream, F_UARTInputStream_wotsit);
  }
}

