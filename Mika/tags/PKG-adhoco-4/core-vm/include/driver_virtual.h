#ifndef _DRIVER_VIRTUAL_H
#define  _DRIVER_VIRTUAL_H

/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
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

#include "device.h"

/*
** A driver is a struct consisting of driver common fields. Every driver has a
** name and a driver family. When a driver is registered, a static struct is 
** constructed where all fields are set correctly, and the registerDriver 
** function is called.
**
** example:
**   w_Driver_ByteSerial stupid_driver = {
**     "stupid-driver",
**     "sd",
**     0,
**     NULL,
**     stupid_initDevice,
**     stupid_termDevice
**   };
**
**   ...
**
**   registerDriver((w_driver)&stupid_driver);
**
** If a driver is placed in a module, the only thing is that 'registerDriver' 
** must be called in init_module() and 'deregisterDriver' in cleanup_module();
**
** The usage counter must be maintained by the driver. A driver in use can not
** be removed.
**
** There a two types of drivers: the genuine drivers which server genuine 
** devices (real hardware connected to the computer), and soft drivers which
** are driver-interface to software devices which are normally threads serving
** driver requests.
*/
typedef struct w_Driver {
  char *name;                             /* the name of this driver, must not be unique, but is supposed to be */
  char *driverFamily;                     /* this is the unique name of the family of devices we can serve (MAJOR_NR) */
  w_int usage;                            /* nr of times driver is in use, so this is the sum of all usages of the devices in this family. Driver can not be removed if usage > 0. */
  w_device registeredTo;                /* this is the head of the doubly linked list of devices in this family */
  w_void (*initDevice)(w_device);       /* this function must initialise a device and its control block, it is called by registerDriver */
  w_void (*termDevice)(w_device);       /* terminate the device and clean up its control block, it is called by deregisterDriver */
} w_Driver;

/*
** These functions are used to start and stop the driver registry. They are 
** called by the kernel at startup and shutdown to construct and destruct the
** registries hashtable
*/
w_void startDriverRegistry(w_void);
w_void stopDriverRegistry(w_void);

/*
** registerDriver : Register a given driver (with all field set correct) with
**      the driver registry. Then the driver will be registered with all the
**      present devices in his family. A doubly linked list of the family is
**      created. Then the initDevice function is called on every device.
**      Return the number of registered devices, or -1 if its family is aleady
**      being served by a driver.
**
** deregisterDriver : If the driver is not in use, it calls the termDevice 
**      function on every device in the family, and then cleans the linked list.
**      It then returns wdo_removed.
**      If the driver is in use, it returns wdo_in_use. If the driver is not 
**      found it returns wdo_not_found.
*/
w_int registerDriver(w_driver driver);
w_device_obituary deregisterDriver(const char *driverFamily);

/*
** getDriverByFamily : Find a driver by its unique familyname, returns its 
**      pointer.If the device is not found, returns NULL.
**
** everyDriver : This function iterates over the registry, calling the given
**      function on each entry.
*/
w_driver getDriverByFamily(const char *driverFamily);
w_void everyDriver(w_void(*fun)(w_driver driver));

#endif
