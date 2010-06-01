/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#include "hashtable.h"
#include "driver_virtual.h"

static w_hashtable driver_registry_hashtable;

w_void startDriverRegistry(w_void) {
  driver_registry_hashtable = ht_create((char*)"hashtable:driver-registry", 51, cstring_hash, cstring_equal, 0, 0);
  woempa(1, "Created driver registry hashtable at %p\n", driver_registry_hashtable);
}

w_void stopDriverRegistry(w_void) {
  woempa(1, "Destroying driver registry hashtable.\n");
  ht_destroy(driver_registry_hashtable);
}

w_device _prev;
w_device *_change;
w_int _nr;
w_driver _driver;

w_void regdev(w_device device) {
  *_change = device;
  
  device->prevFamily = _prev;

  // if driver is already set, then we have an ExistingDevice, which (normally) is already in use, so 
  // does not have to be initialised 
  if(device->driver == NULL) {
    device->driver = _driver;

    // initialise device (create control block)
    device->driver->initDevice(device);
  }
  _change = &device->nextFamily;
  
  _prev = device;
  
  _nr += 1;
}

w_int registerDriver(w_driver driver) {
  w_driver d;
  
  d = (w_driver)ht_read(driver_registry_hashtable, (w_word)driver->driverFamily);
  if (d) {
    woempa(9, "OeiOei, already have a driver in this family called %s (family: %s)\n", d->name, d->driverFamily);
    woempa(9, "Driver NOT added, first remove old driver, and then try again\n");
    return -1;
  }
  else {
    // First put the driver in the hashtable, then call the init function.
    // The disk_driver adds device from its init function and we had some problems when these steps were reversed 
    woempa(7, "Adding driver %s (family: %s) to driver registry\n", driver->name, driver->driverFamily);
    ht_write(driver_registry_hashtable, (w_word)driver->driverFamily, (w_word)driver);
    
    // register to devices (control blocks must be created by the driver itself in the 
    // initDevice routine which is called by init ) 
    // 
    _nr = 0;
    _prev = NULL;
    _driver = driver;
    _change = &driver->registeredTo;
    everyDeviceByDriverFamily(regdev, driver->driverFamily);
    *_change = NULL;

    return _nr;
  } 
}

w_device_obituary deregisterDriver(const char *driverFamily) {
  w_device t;
  w_driver driver = (w_driver)ht_erase(driver_registry_hashtable, (w_word)driverFamily);
  
  if (driver) {
    if (driver->usage > 0) {
      woempa(7, "Could not remove driver %s (family: %s) from driver registry because it is still in use\n", driver->name, driver->driverFamily);
      return wdo_in_use;
    }
    else {
      // we assume that the usage of the driver is the sum of the usages of the devices
      // so if driver is not in use, devices are not in use
      woempa(7, "Removed driver %s (family: %s) from driver registry\n", driver->name, driver->driverFamily);

      // deregister with devices (control block must already have been cleaned by the 
      // termDevice routine of the driver which is called in term)

      while (driver->registeredTo != NULL) {
        // terminate device (cleanup control block)
        driver->termDevice(driver->registeredTo);

        // this is normally not necessary, but just make sure
        driver->registeredTo->usage = 0;
        
        driver->registeredTo->prevFamily = NULL;
        driver->registeredTo->driver = NULL;
        driver->registeredTo->control = NULL;
        t = driver->registeredTo->nextFamily;
        driver->registeredTo->nextFamily = NULL;
        driver->registeredTo = t;
      }
      return wdo_removed;
    }
  }
  else {
    woempa(9, "No driver found in family `%s'\n", driverFamily);
    return wdo_not_found;
  }
}

w_driver getDriverByFamily(const char *driverFamily) {
  w_driver driver;

  woempa(1, "Looking for driver in family `%s'\n", driverFamily);
  driver = (w_driver)ht_read(driver_registry_hashtable, (w_word)driverFamily);
  if (driver) {
    woempa(1, "Found %s (family: %s)\n", driver->name, driver->driverFamily);
  }
  else {
    woempa(1, "Did not find a driver in family `%s'\n", driverFamily);
  }
  return driver;
}

w_void everyDriver(w_void(*fun)(w_driver driver)) {
  w_driver driver;
  w_fifo driver_fifo;

  driver_fifo = ht_list_values(driver_registry_hashtable);
  while ((driver = getFifo(driver_fifo))) {
    woempa(1, "Retrieved %s (family: %s) from fifo\n", driver->name, driver->driverFamily);
    fun(driver);
  }
  releaseFifo(driver_fifo);
}

w_void searchDriverForDevice(w_device device) {
  w_driver driver;
  
  driver = getDriverByFamily(device->driverFamily);
  if (driver) {
    ht_lock(driver_registry_hashtable);
    // driver found, now register to driver
    device->driver = driver;
    device->nextFamily = driver->registeredTo;
    device->prevFamily = NULL;
    if (driver->registeredTo != NULL) driver->registeredTo->prevFamily = device;
    driver->registeredTo = device;
    ht_unlock(driver_registry_hashtable);
    
    // initialise device (create control block)
    driver->initDevice(device);
  }
}
 
w_void deregisterDeviceFromDriver(w_device device) {
  w_driver driver;
  
  driver = getDriverByFamily(device->driverFamily);
  if (driver) {
    ht_lock(driver_registry_hashtable);
    // driver found, deregister from driver
    if (device->prevFamily != NULL) {
      device->prevFamily->nextFamily = device->nextFamily;
    }
    else {
      driver->registeredTo = device->nextFamily;
    }
    if (device->nextFamily != NULL) {
      device->nextFamily->prevFamily = device->prevFamily;
    }

    device->driver = NULL;
    device->nextFamily = NULL;
    device->prevFamily = NULL;
    ht_unlock(driver_registry_hashtable);
    
    // terminate device (cleanup control block)
    driver->termDevice(device);
  }
}
