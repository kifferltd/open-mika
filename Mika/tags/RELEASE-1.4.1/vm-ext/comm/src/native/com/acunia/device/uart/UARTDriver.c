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
**************************************************************************/

/*
** $Id: UARTDriver.c,v 1.5 2006/06/01 13:22:19 cvs Exp $
*/

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
#include "threads.h"
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

  namelist = allocFifo(15);
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


