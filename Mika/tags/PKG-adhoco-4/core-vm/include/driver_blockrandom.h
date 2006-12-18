#ifndef _DRIVER_BLOCKRANDOM_H
#define  _DRIVER_BLOCKRANDOM_H

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
