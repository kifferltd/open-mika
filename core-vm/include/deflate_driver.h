#ifndef _STREAM_UNZIP_H
#define _STREAM_UNZIP_H
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

#include "driver_byteserial.h"

extern w_Driver_ByteSerial deflate_driver;

typedef enum {
  wdi_end_of_input           = 3001,      /* Send this if end of data is reached while compressing */
  wdi_reset                  = 3002,      /* Send this to reset the device (flush all queues and completely reinitialise) */
  wdi_send_dictionary        = 3003,      /* Send this to set a dictionary. Can only be done with a cleanly reset device. */
                                          /* After this call you need to write the dictionary to the device WITH ONE SINGLE WRITE */
  wdi_set_compression_level  = 3004,      /* Set the given compression level */
  wdi_set_no_auto            = 3005,      /* If this is set to TRUE, no automatic restart will happen after a file is */
                                          /* finished (use on a cleanely reset device) */
  wdi_get_need_more_input    = 3006,      /* Returns TRUE if the device is waiting for data */
  wdi_get_processed_size     = 3007       /* Get the original size (before (de)compression). This is can be read after u */
                                          /* receive a wds_data_exhausted (end-of-file), and only if u are in no_auto mode. */
} w_driver_ioctl_unzip;

#endif
