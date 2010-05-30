#ifndef _DRIVER_BLOCKRANDOM_H
#define  _DRIVER_BLOCKRANDOM_H

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

#include "oswald.h"
#include "driver_virtual.h"
#include "driver_byteserial.h"

typedef struct w_Driver_BlockRandom {
  char *name;
  char *driverFamily;
  w_int usage;
  w_device registeredTo;
  w_void (*initDevice)(w_device);
  w_void (*termDevice)(w_device);
  
  w_device (*open)(w_device device);
  w_void (*close)(w_device device);
  
  w_driver_status (*read)(w_device device, w_ubyte *bytes, w_int sector, w_int size, x_sleep timeout);
  w_driver_status (*write)(w_device device, w_ubyte *bytes, w_int sector, w_int size, x_sleep timeout);
  w_driver_status (*set)(w_device device, w_driver_ioctl command, w_word param, x_sleep timeout);
  w_driver_status (*query)(w_device device, w_driver_ioctl query, w_word *reply, x_sleep timeout);
} w_Driver_BlockRandom;
typedef w_Driver_BlockRandom *w_driver_blockrandom;

/*
** These are helper functions. They are just wrappers for the driver calls so
** that the driver user does not have to cast and dereference any pointers.
**
** example :
**
** w_device dev;
** w_ubyte buf[10]; 
** int l;
**   
** dev = deviceOpen("hda0", wdp_read);
** if(dev != NULL) {
**   deviceRead(dev, buf, 1, 10, 100);
**   ...
**   deviceClose(dev);
** }
**
*/
static inline w_device deviceBROpen(const char *deviceName, w_driver_perm mode) {
  w_device dev = getDeviceByName(deviceName);
  w_driver_blockrandom driver;

  if (dev) {
    if (dev->type == wdt_block_random) {
      if (dev->driver) {
        driver = (w_driver_blockrandom)dev->driver;
        return driver->open(dev);
      }
      else {
        woempa(9, "Device %s does not have a driver assigned.\n", deviceName);
        return NULL;
      }  
    }
    else {
      woempa(9, "Device %s is not a block-random device, could not open.\n", deviceName);
      return NULL;
    }   
  }
  else {
    woempa(9, "Device %s does not exist.\n", deviceName);
    return NULL;
  }
}
  
static inline w_void deviceBRClose(w_device dev) {
  w_driver_blockrandom driver;
  driver = (w_driver_blockrandom)dev->driver;
  driver->close(dev);
}
  
static inline w_driver_status deviceBRRead(w_device device, w_ubyte *bytes, w_int sector, w_int size, x_sleep timeout) {
  w_driver_blockrandom driver;
  driver = (w_driver_blockrandom)device->driver;
  return driver->read(device, bytes, sector, size, timeout);
}
  
static inline w_driver_status deviceBRWrite(w_device device, w_ubyte *bytes, w_int sector, w_int size, x_sleep timeout) {
  w_driver_blockrandom driver;
  driver = (w_driver_blockrandom)device->driver;
  return driver->write(device, bytes, sector, size, timeout);
}
  
static inline w_driver_status deviceBRSet(w_device dev, w_driver_ioctl command, w_word param, x_sleep timeout) {
  w_driver_blockrandom driver;
  driver = (w_driver_blockrandom)dev->driver;
  return driver->set(dev, command, param, timeout);
}
  
static inline w_driver_status deviceBRQuery(w_device dev, w_driver_ioctl query, w_word *reply, x_sleep timeout) {
  w_driver_blockrandom driver;
  driver = (w_driver_blockrandom)dev->driver;
  return driver->query(dev, query, reply, timeout);
}
  
#endif
