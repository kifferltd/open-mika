#ifndef _STREAM_UNZIP_H
#define _STREAM_UNZIP_H
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
