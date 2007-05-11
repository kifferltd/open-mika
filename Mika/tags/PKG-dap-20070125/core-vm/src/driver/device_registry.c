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
*                                                                         *
**************************************************************************/

#include <string.h>

#include "hashtable.h"
#include "ts-mem.h"
#include "device.h"

static w_hashtable device_registry_hashtable;

w_void startDeviceRegistry(w_void) {
  device_registry_hashtable = ht_create((w_ubyte *)"hashtable:device-registry", 51, cstring_hash, cstring_equal, 0, 0);
  woempa(1, "Created device registry hashtable at %p\n", device_registry_hashtable);
}

w_void stopDeviceRegistry(w_void) {
  woempa(1, "Destroying device registry hashtable.\n");
  ht_destroy(device_registry_hashtable);
}

w_device registerDevice(const char *name, const char *driverFamily, w_int familyMember, w_device_type type) {
  w_device device;

  device = (w_device)ht_read(device_registry_hashtable, (w_word)name);
  if (device) {
    woempa(9, "OeiOei, already have a device called %s (family: %s)\n", device->name, device->driverFamily);
    woempa(9, "Device NOT added\n");
    return NULL;
  }
  else {
    device = allocClearedMem(sizeof(w_Device));
    if (!device) {
      wabort(ABORT_WONKA, "Unable to allocate device\n");
    }
    
    device->type = type;
    device->familyMember = familyMember;
  
    // copy them strings
    device->name = allocMem(strlen(name) + 1);
    if (!device->name) {
      wabort(ABORT_WONKA, "Unable to allocate device->name\n");
    }
    strcpy(device->name, name);
    device->driverFamily = allocMem(strlen(driverFamily) + 1);
    if (!device->driverFamily) {
      wabort(ABORT_WONKA, "Unable to allocate device->driverFamily\n");
    }
    strcpy(device->driverFamily, driverFamily);

    // if driver is already present, we should register with him
    searchDriverForDevice(device);

    woempa(7, "Adding device %s (family: %s) to device registry\n", device->name, device->driverFamily);
    ht_write(device_registry_hashtable, (w_word)device->name, (w_word)device);

    device->nrAliases = 1;

    return device;
  } 
}

w_device registerExistingDevice(w_device device) {
  w_device d;
  char *n;

  d = (w_device)ht_read(device_registry_hashtable, (w_word)device->name);
  if (d) {
    woempa(9, "OeiOei, already have a device called %s (family: %s)\n", device->name, device->driverFamily);
    woempa(9, "Device NOT added\n");
    return NULL;
  }
  else {
    if (device->driver != NULL) {
      searchDriverForDevice(device);
    }

    // if we don't do this, releasing the device will crash !!
    n = allocMem(strlen(device->name) + 1);
    if (!n) {
      wabort(ABORT_WONKA, "Unable to allocate n\n");
    }
    strcpy(n, device->name);
    device->name = n;
    
    n = allocMem(strlen(device->driverFamily) + 1);
    if (!n) {
      wabort(ABORT_WONKA, "Unable to allocate n\n");
    }
    strcpy(n, device->driverFamily);
    device->driverFamily = n;

    woempa(7, "Adding device %s (family: %s) to device registry\n", device->name, device->driverFamily);
    ht_write(device_registry_hashtable, (w_word)device->name, (w_word)device);

    device->nrAliases = 1;

    return device;
  } 
}

w_device aliasDevice(const char *name, const char *alias) {
  w_device device;

  device = (w_device)ht_read(device_registry_hashtable, (w_word)name);
  if (!device) {
    woempa(9, "OeiOei, device %s not found, unable to make alias\n", name);
    return NULL;
  }
  else {
    woempa(7, "Making alias device %s for device %s (family: %s) to device registry\n", name, device->name, device->driverFamily);
    ht_write(device_registry_hashtable, (w_word)device->name, (w_word)device);

    device->nrAliases += 1;

    return device;
  } 
}

w_device_obituary deregisterDevice(const char *name) {
  w_device device = (w_device)ht_erase(device_registry_hashtable, (w_word)name);
  if (device) {
    if (device->usage > 0) {
      woempa(7, "Could NOT remove device %s (family: %s) from device registry because it is still in use\n", device->name, device->driverFamily);
      return wdo_in_use;
    }
    else {
      woempa(7, "Removed device %s (family: %s) from device registry\n", device->name, device->driverFamily);

      device->nrAliases -= 1;
      if (device->nrAliases <= 0) {
        // deregister with driver
        deregisterDeviceFromDriver(device);

        // release strings
        releaseMem(device->name);
        releaseMem(device->driverFamily);

        // free the device
        releaseMem(device);
        return wdo_removed;
      }
      else {
        if (strcmp(device->name, name) == 0) {
          woempa(7, "Removed device named %s (family: %s) from device registry, aliases still present\n", name, device->driverFamily);
        }
        else {
          woempa(7, "Removed an alias named %s (family: %s) from device registry, device still present\n", name, device->driverFamily);
        }
        return wdo_aliases_left;
      }
    }
  }
  else {
    woempa(1, "No device `%s' found in device name hashtable\n", name);
    return wdo_not_found;
  }
}

w_device getDeviceByName(const char *name) {
  w_device device;

  woempa(1, "Looking for device `%s'\n", name);
  device = (w_device)ht_read(device_registry_hashtable, (w_word)name);
  if (device) {
    woempa(1, "Found %s (family: %s)\n", device->name, device->driverFamily);
  }
  else {
    woempa(1,"Did not find a device called `%s'\n", name);
  }

  return device;
}

w_void everyDevice(w_void(*fun)(w_device device)) {
  w_device device;
  w_fifo device_fifo;

  device_fifo = ht_list_values(device_registry_hashtable);
  while ((device = getFifo(device_fifo))) {
    woempa(1, "Retrieved %s (family: %s) from fifo\n", device->name, device->driverFamily);
    fun(device);
  }
  releaseFifo(device_fifo);
}

w_void everyDeviceByType(w_void(*fun)(w_device device), w_device_type type) {
  w_device device;
  w_fifo device_fifo;

  device_fifo = ht_list_values(device_registry_hashtable);
  while ((device = getFifo(device_fifo))) {
    woempa(1, "Retrieved %s (family: %s) from fifo\n", device->name, device->driverFamily);
    if (device->type == type) {
      fun(device);
    }
  }
  releaseFifo(device_fifo);
}

w_void everyDeviceByDriverFamily(w_void(*fun)(w_device device), const char *driverFamily) {
  w_device device;
  w_fifo device_fifo;

  device_fifo = ht_list_values(device_registry_hashtable);
  while ((device = getFifo(device_fifo))) {
    woempa(1, "Retrieved %s (family: %s) from fifo\n", device->name, device->driverFamily);
    if (strcmp(device->driverFamily, driverFamily) == 0) {
      fun(device);
    }
  }
  releaseFifo(device_fifo);
}
