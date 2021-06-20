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

#include <string.h>

#include "clazz.h"
#include "comms.h"
#include "core-classes.h"
#include "uart-classes.h"
#include "exception.h"
#include "fifo.h"
#include "hashtable.h"
#include "heap.h"
#include "methods.h"
#include "wstrings.h"
#include "mika_threads.h"
#include "driver_byteserial.h"
#include "sio.h"

/**@name File UARTDriver.c
   @author Chris Gray
   @version $Revision: 1.5 $
*/

/*
 The list of names.  THIS IS NOT THREAD SAFE!
*/
static w_fifo namelist;

/*
 If the given device is a byte serial device, add its name to namelist.
*/
static void scanport(w_device device) {
  w_string nameString = cstring2String(device->name, strlen(device->name));
  woempa(7,"Adding %w to namelist\n", nameString);
  putFifo(nameString,namelist);
}

/*
 Return the name of the first byte serial device.
 Must be called before any call to UARTDriver_static_nextUARTname.
*/
w_instance
UARTDriver_static_firstUARTname (JNIEnv *env, w_instance classUARTDriver
) {
  w_string name;

  namelist = allocFifo(14);
  everyDeviceByDriverFamily(scanport, "sio");
  
  name = getFifo(namelist);
  if (name) {
    woempa(7,"Returning %w\n",name);

    return newStringInstance(name);

  }
  else {
    woempa(7,"Did not find any byte serial devices\n");
    releaseFifo(namelist);

    return NULL;
  }
}

/*
 Return the name of the next byte serial device.
 Must be called after UARTDriver_static_nextUARTname, and must not be
 again after it has returned NULL.
*/
w_instance
UARTDriver_static_nextUARTname (JNIEnv *env, w_instance classUARTDriver
) {
  w_string name;
  name = (w_string)getFifo(namelist);
  if (name) {
    woempa(7,"Returning %w\n",name);

    return newStringInstance(name);

  }
  else {
    woempa(7,"Exhausted all byte serial devices\n");
    releaseFifo(namelist);

    return NULL;
  }
}

/*
  Register a serial device with a given individual name, family name, and number
*/
void
UARTDriver_static_registerSerialDevice0(JNIEnv *env, w_instance theClass, w_instance nameString, w_instance familyString, w_int number) {
  w_string name = String2string(nameString);
  w_string family = String2string(familyString);
  char * device_name_chars = string2UTF8(name, NULL);
  char * family_name_chars = string2UTF8(family, NULL);
  woempa(7, "registerDevice(%s, %s, %d, %d)\n", device_name_chars + 2, family_name_chars + 2, number, wdt_byte_serial);
  registerDevice(device_name_chars + 2, family_name_chars + 2, number, wdt_byte_serial);
  releaseMem(device_name_chars);
  releaseMem(family_name_chars);
}

/*
  Attach a physical device (e.g. /dev/ttyS0) as the given family and number.
  Family must be "sio".
*/
void
UARTDriver_static_attachSerialDevice0(JNIEnv *env, w_instance theClass, w_instance familyString, w_int number, w_instance pathString) {
  w_string family = String2string(familyString);
  w_string path = String2string(pathString);
  char * path_chars = string2UTF8(path, NULL);
  char * family_name_chars = string2UTF8(family, NULL);
  if (strncmp(family_name_chars + 2, "sio", 3) == 0) {
    woempa(7, "sio_set_path(%d, %s)\n", number, path_chars + 2);
    sio_set_path(number, path_chars + 2);
  }
  else {
    woempa(9, "unknown device family %s\n", family_name_chars + 2);
  }
  releaseMem(path_chars);
  releaseMem(family_name_chars);
}

/*
 * Set up the links between the Java and native (WNI) parts of the comms classes.
 */
void init_comm(void) {
  collectUartFixups();
  loadUartClasses();
}


