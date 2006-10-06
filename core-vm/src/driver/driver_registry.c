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
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
**************************************************************************/


#include "hashtable.h"
#include "driver_virtual.h"

static w_hashtable driver_registry_hashtable;

w_void startDriverRegistry(w_void) {
  driver_registry_hashtable = ht_create((w_ubyte *)"hashtable:driver-registry", 51, cstring_hash, cstring_equal, 0, 0);
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
