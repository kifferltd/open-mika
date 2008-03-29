#ifndef _SIO_H
#define _SIO_H

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

#include <termios.h>
#include <poll.h>
#include "oswald.h"
#include "wonka.h"

/*
** Rule-of-thumb for calculating receive buffer size: n/8 bytes (covers about
** 1200 msec of latency for continuous asynch data).
*/
#define RX_BUF_SIZE_DIVISOR 8

// number of milliseconds to wait for change
#define SIO_SIGNAL_SNOOZE (w_long)25 

/*
** Internally we use these flags for DATA_AVAILABLE and OUTPUT_EMPTY
*/
#define IOFLAG_AVAIL  0x8000
#define IOFLAG_EMPTY  0x0080 // N.B. not yet implemented!

typedef struct termios *w_termios;

/*
** Miscellaneous flags
*/
#define SIO_OPEN_FOR_READ   1
#define SIO_OPEN_FOR_WRITE  2
// Rx congestion:
#define SIO_RX_THROTTLE     4  // apply ctsrts flow control if available
#define SIO_RX_DISCARD      8  // discard incoming data
// HW flow control:
#define SIO_CTSRTS_NONE     0
#define SIO_CTSRTS_IN       16
#define SIO_CTSRTS_OUT      32

#define SIO_WAIT_FOR_IOEVT 256

void sio_set_path(int n, char *s);

#endif /* SIO_H */

