#ifndef _DEVICE_H
#define  _DEVICE_H

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

#include "wonka.h"

typedef struct w_Driver *w_driver;
typedef struct w_Device *w_device;

typedef enum {
  wdt_byte_serial       = 1101,           /* a streaming device */
  wdt_block_random      = 1201,           /* a block oriented device */
  wdt_packet            = 1301,           /* a network device */
  wdt_frame_buffer      = 1401            /* a memory mapping device */
} w_device_type;

typedef enum {
  wdo_removed           = 2001,           /* removed succesfully */
  wdo_aliases_left      = 2002,           /* alias removed, but still present */
  wdo_in_use            = 2003,           /* device/driver is still in use (opened) so not removed */
  wdo_not_found         = 2004            /* not found, so not removed */
} w_device_obituary;

/*
** A w_Device is a struct discribing a known device to the system. Device 
** structures are created by the registry when you register the device. Devices
** can be registered and deregistered at any time, if an appropriate driver is
** available, it is assigned.
**
** example :
**   registerDevice("cua0", "cua", 0, wdt_byte_serial);
**
** Devices are grouped into families. Each family has one driver, and every
** family member is identified by a familyMember number. This is much like the
** major and minor number in UNIX.
**
** Every device has a usage count. Devices can not be removed if it is > 0. The
** usage counter must be maintained by the driver.
*/
typedef struct w_Device {
  w_device_type type;                   /* the type of this device */
  char *name;                             /* original name of this device, no aliases */
  char *driverFamily;                     /* the family of drivers that can serve this device (MAJOR_NR) */
  w_int familyMember;                     /* an ID that is used by the driver to identify the devices in a family (MINOR_NR) */
  w_driver driver;                      /* the driver which is assigned to serve this device */
  w_void *control;                        /* a device specific control block used by the driver */
  w_device nextFamily, prevFamily;      /* a doubly linked list of all devices in a family, constructed when a driver is assigned */
  w_int usage;                            /* nr of times this device is opened. Device can not be removed if usage > 0. Must be set by the driver. */
  w_int nrAliases;                        /* nr of times this device is in the hashtable */
} w_Device;

/*
** These functions are used to start and stop the device registry. They are 
** called by the kernel at startup and shutdown to construct and destruct the
** registries hashtable
*/
w_void startDeviceRegistry(w_void);
w_void stopDeviceRegistry(w_void);

/*
** registerDevice : Give a unique name, a driverFamily (used to identify a 
**      driver for this device), a familyMember number (used by the driver to
**      identify the devices it serves) and a device type. If the name is 
**      unique the device structure is created and a suitable driver is looked
**      for. It returns the device pointer.
**      If the name is already registered, NULL is returned.
**
** registerExistingDevice : This function is used to insert a statically created
**      device structure in the hashtable (ex. the initial console which is used
**      for debug messages). If the name defined in the device is not present in
**      hashtable, the device is added, and if there isn't already a driver 
**      assigned, we look for one in the driver registry. The device pointer is
**      returned. If the name was already present in the hashtable, it returns
**      NULL. An ExistingDevice need already been in use, so the device 
**      structure need to be constucted, the device need to be initialised and 
**      the driver need to be set. The linked list of  devices in the family
**      is constructed, so does not have to be set. If the ExistingDevice is 
**      allocated correctly using allocMem, deregisterDevice may be called. If
**      it is defined statically or allocated in another way, you MAY NOT call
**      deregisterDevice because this will try to free the devices memory.
**
** aliasDevice : Tries to create an alias entry in the hashtable to the device
**      with the given name. If the name is not found it returns NULL, else it
**      returns the device pointer.
**      The number of aliases is counted, so when you remove a device, it will
**      only remove the entry in the hashtable if there are other aliases left.
**      This way the device is actually removed when the last alias is removed.
**
** deregisterDevice : If a device is in use, it is not removed (the usage 
**      counter must be properly managed by the driver), and the function
**      returns wdo_in_use. If it is not in use and there are no more aliases,
**      the device is deregistered from the driver (thereby calling termDevice
**      of the driver which cleans up the device), freed, and removed from the 
**      hashtable. If it has aliases, the given name is just removed from the 
**      hashtable.
*/
w_device registerDevice(const char *name, const char *driverFamily, w_int familyMember, w_device_type type);
w_device registerExistingDevice(w_device device);
w_device aliasDevice(const char *name, const char *alias);
w_device_obituary deregisterDevice(const char *name);

/*
** Find a device by its unique name, returns its pointer. If the device is not 
** found, returns NULL.
*/
w_device getDeviceByName(const char *name);

/*
** These functions iterate over the registry, calling the given function on
** each entry if you call everyDevice, on each entry of a certain type if
** everyDeviceByType is called, or on each entry of a certain family if
** everyDeviceByDriverFamily is called.
*/
w_void everyDevice(w_void(*fun)(w_device device));
w_void everyDeviceByType(w_void(*fun)(w_device device), w_device_type type);
w_void everyDeviceByDriverFamily(w_void(*fun)(w_device device), const char *driverFamily);

/*
** These functions are used internally, and are defined in the driver registry code
*/
w_void searchDriverForDevice(w_device device);
w_void deregisterDeviceFromDriver(w_device device);

#endif
