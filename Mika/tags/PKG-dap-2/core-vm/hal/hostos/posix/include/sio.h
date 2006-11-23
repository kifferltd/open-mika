#ifndef _SIO_H
#define _SIO_H

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

#define SIO0_DEFAULT_BITRATE B115200
#define SIO1_DEFAULT_BITRATE B115200
#define SIO2_DEFAULT_BITRATE B115200
#define SIO3_DEFAULT_BITRATE B115200

void sio_set_path(int n, char *s);

#endif /* SIO_H */

