#ifndef _DRIVER_BYTESERIAL_H
#define  _DRIVER_BYTESERIAL_H

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

typedef enum {
  wdp_none              = 0,              /* no permissions */
  wdp_read              = 1,              /* open file with read permissions */
  wdp_write             = 2,              /* write permissions */
  wdp_read_write        = 3               /* read-write permissions */
} w_driver_perm;

typedef enum {
  wds_success           = 100,            /* the operation went ok, don't worry  */
  wds_no_instance       = 101,            /* the operation could not be performed in the given timeframe */
  wds_data_exhausted    = 102,            /* ran out of data to read (end of file and such) */
  wds_space_exhausted   = 103,            /* ran out of space to write to (medium full and such) */
  wds_write_only        = 104,            /* device has been opened write only, not allowed to read */
  wds_read_only         = 105,            /* device has been opened read only, not allowed to write */
  wds_not_open          = 106,            /* the device has not been opened */
  wds_internal_error    = 107,            /* some internal driver error, driver can become unstable after this, better close it */
  wds_not_implemented   = 108,            /* called function is not (yet) implemented */
  wds_no_such_command   = 109,            /* command number of a query/set operation is out of range */
  wds_illegal_value     = 110             /* value of a set operation is out of range */
} w_driver_status;

typedef enum {
  wdi_set_bitrate       = 201,            /* set the bitrate (param: 1 to 115200) */
  wdi_set_databits      = 202,            /* set the number of data bits (param: 1 to 16) */
  wdi_set_stopbits      = 203,            /* set the number of stop bits (param: 1 to 4, in half-bits) */
  wdi_set_flowcontrol   = 204,            /* set flow control mode (param: bitmask, see FLOWCON_) */
  wdi_set_parity        = 205,            /* set parity mode (param: 0=none, 1=odd, 2=even) */
  wdi_set_signals       = 206,            /* set control signals (param: new values in low-order 16 bits, mask of values to change in high-order 16 bits. bit0=DTR, bit1=RTS) */
  wdi_send_break        = 208,            /* send a break "character" (param: 1 to 1000 in msec) */
  wdi_set_rx_bufsize    = 210,            /* )                                      (param: size in bytes) */
  wdi_set_rx_threshold  = 211,            /* )                                      (param: device blocks until given amount of bytes is present for reading, default 0) */
  wdi_set_rx_timeout    = 212,            /* ) only used on buffered devices to set (param: device blocks for a given amount of time, default 0x7fffffff ms) */
  wdi_set_tx_bufsize    = 213,            /* ) buffering parameters                 (param: size in bytes) */
  wdi_set_tx_threshold  = 214,            /* )                                      (param: device blocks until the given amount of space is available in the buffer, default 0) */
  wdi_set_tx_timeout    = 215,            /* )                                      (param: device blocks for a given amount of time, default 0x7fffffff ms) */
  wdi_get_bitrate       = 301,            /* get the bitrate */
  wdi_get_databits      = 302,            /* get the number of start bits */
  wdi_get_stopbits      = 303,            /* get the number of stop bits */
  wdi_get_flowcontrol   = 304,            /* get flow control mode */
  wdi_get_parity        = 305,            /* get parity mode */
  wdi_get_signals       = 306,            /* get control signals (return: bit0=DTR, bit1=RTS, bit8=DSR, bit9=CTS, bit10=RI, bit11=CD) */
  wdi_get_available     = 307,            /* get the number of bytes in the rx buffer */
  wdi_wait_for_event    = 308,            /* wait for an event to happen */
  wdi_get_event_data    = 309             /* get the data that the event produced */
} w_driver_ioctl;

/*
** w_Driver_ByteSerial is a type of driver used to server byte-serial devices.
** These devices are of a streaming nature.
**
** Every function must pass a timeout value. This is the maximum amount of 
** ticks that the driver is allowed to use in the function. This means that
** no function, whatsoever, is allowed to block for an unknown time. If for
** example you use the read/write UNIX calls in your driver, you MUST use 
** select to make sure data is available, or else these function may block
** indefenetly causing the whole of oswald to freese (if we do not use 
** native threading methods, which is not advised because they do not allow
** proper thread-fidling to implement priority-inversion-prevention and such).
** Oswald provides a function to convert ticks to usecs so the user can time 
** his operations: x_sleep x_ticks2usec(x_sleep timeout);
** If you want non-blocking function call, use x_no_wait as a timeout value.
**
** open : Function called to open a streaming device for reading/ writing.
**      device and permissions must be passed. On success, the device is 
**      returned, else NULL.
**      A device should be opened only once. If you want to change opening mode
**      of an already open file, close it and reopen with the correct 
**      permissions.
**      A cloning device is a device which returns a unique device structure
**      everytime the device is opened. This cloned device is created by the 
**      device registry. These devices need to be removed from the registry
**      by the close call. Naming convention for cloning devices is dev_ 
**      and for cloned devices dev0, dev1, etc. like normal devices.
**
** close : Function called to close a streaming device.
**
** read : Read one or more byte in a byte array. The requested length is a 
**      maximum length, the driver will never return more bytes, but can return
**      less. The number of read bytes is returned in lread. If no data is 
**      present the read function may wait for timeout ticks until data is 
**      available. If no data comes available in the given timewindow,
**      wds_no_instance is returned.
**
** write : Write one or more bytes from the given byte array to the device. The
**      actual number of written bytes is returned in lwritn. If no space is 
**      available, the driver may block for maximum timeout ticks. If no space
**      comes available in the given timewindow, wds_no_instance is returned.
**
** seek : Scip to the given absolute position in the stream.
**
** set : Function used to set driver specific information like serial port
**      parameters and such. A command and a correct parameter need to be 
**      provided (for standard commands and parameters look above). Not every
**      device allows every given parameter. If the parameter is really out of
**      range, a wds_illegal_value is returned. If the command is unknown, a
**      wds_no_such_command is returned. If the command is known and the
**      parameter is 'reasonable', it is still possible that the hardware does
**      not accept the given value. Therefore it is advised to always check the
**      'set' with a 'query'.
**
** query : Function used to query driver parameters. Much like 'set', but return
**      the requested parameter thru reply.
*/
typedef struct w_Driver_ByteSerial {
  char *name;
  char *driverFamily;
  w_int usage;
  w_device registeredTo;
  w_void (*initDevice)(w_device);
  w_void (*termDevice)(w_device);
  
  w_device (*open)(w_device device, w_driver_perm mode);
  w_void (*close)(w_device device);
  
  w_driver_status (*read)(w_device device, w_ubyte *bytes, w_int length, w_int *lread, x_sleep timeout);
  w_driver_status (*write)(w_device device, w_ubyte *bytes, w_int length, w_int *lwritn, x_sleep timeout);
  w_driver_status (*seek)(w_device device, w_int offset, x_sleep timeout);
  w_driver_status (*set)(w_device device, w_driver_ioctl command, w_word param, x_sleep timeout);
  w_driver_status (*query)(w_device device, w_driver_ioctl query, w_word *reply, x_sleep timeout);
} w_Driver_ByteSerial;
typedef w_Driver_ByteSerial *w_driver_byteserial;

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
** dev = deviceOpen("tty0", wdp_read);
** if(dev != NULL) {
**   deviceRead(dev, buf, 10, &l, 100);
**   ...
**   deviceClose(dev);
** }
**
*/
static inline w_device deviceBSOpen(const char *deviceName, w_driver_perm mode) {
  w_device dev = getDeviceByName(deviceName);
  w_driver_byteserial driver;
  
  if (dev) {
    if (dev->type == wdt_byte_serial) {
      if (dev->driver) {
        driver = (w_driver_byteserial)dev->driver;
        return driver->open(dev, mode);
      } 
      else {
        woempa(9, "Device %s does not have a driver assigned.\n", deviceName);
        return NULL;
      }
    }
    else {
      woempa(9, "Device %s is not a byte-serial device, could not open.\n", deviceName);
      return NULL;    
    }
  } 
  else {
    woempa(9, "Device %s does not exist.\n", deviceName);
    return NULL;
  }
}

static inline w_void deviceBSClose(w_device dev) {
  w_driver_byteserial driver;
  driver = (w_driver_byteserial)dev->driver;
  driver->close(dev);
}

static inline w_driver_status deviceBSRead(w_device dev, w_ubyte *bytes, w_int length, w_int *lread, x_sleep timeout) {
  w_driver_byteserial driver;
  driver = (w_driver_byteserial)dev->driver;
  return driver->read(dev, bytes, length, lread, timeout);
}

static inline w_driver_status deviceBSWrite(w_device dev, w_ubyte *bytes, w_int length, w_int *lwritn, x_sleep timeout) {
  w_driver_byteserial driver;
  driver = (w_driver_byteserial)dev->driver;
  return driver->write(dev, bytes, length, lwritn, timeout);
}

static inline w_driver_status deviceBSSeek(w_device dev, int offset, x_sleep timeout) {
  w_driver_byteserial driver;
  driver = (w_driver_byteserial)dev->driver;
  return driver->seek(dev, offset, timeout);
}

static inline w_driver_status deviceBSSet(w_device dev, w_driver_ioctl command, w_word param, x_sleep timeout) {
  w_driver_byteserial driver;
  driver = (w_driver_byteserial)dev->driver;
  return driver->set(dev, command, param, timeout);
}

static inline w_driver_status deviceBSQuery(w_device dev, w_driver_ioctl query, w_word *reply, x_sleep timeout) {
  w_driver_byteserial driver;
  driver = (w_driver_byteserial)dev->driver;
  return driver->query(dev, query, reply, timeout);
}

#endif
