#ifndef _COMMS_H
#define  _COMMS_H

/**************************************************************************
* Copyright (c) 2001, 2003 by Acunia N.V. All rights reserved.            *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: comms.h,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

#include "fifo.h"
#include "wonka.h"

/**
** "Real" flags, supplied by the UART (actually, we fake DSR)
*/
#define IOFLAG_DTR          0x0001
#define IOFLAG_RTS          0x0002
#define IOFLAG_DSR          0x0100
#define IOFLAG_CTS          0x0200
#define IOFLAG_CD           0x0400
#define IOFLAG_RI           0x0800

/**
** Synthetic flags used to turn software-detected conditions into events
*/
#define IOFLAG_FE           0x1000
#define IOFLAG_PE           0x2000
#define IOFLAG_OE           0x4000

/**
** Enumerations for set/query/ioevt interface
*/
#define FLOWCON_NONE           0
#define FLOWCON_CTSRTS_IN      1
#define FLOWCON_CTSRTS_OUT     2

#ifndef WINNT
#define PARITY_NONE            0
#define PARITY_ODD             1
#define PARITY_EVEN            2
#endif

#define IOEVT_DATA_AVAILABLE   1
#define IOEVT_SIGNALS_CHANGED  2 
#define IOEVT_ERROR_DETECTED   3
#define IOEVT_BREAK_DETECTED   4

/**
** Miscellaneous #define's
*/
#define WONKA_EOF 0xffffffffL
#define LF 10
#define CR 13

extern w_hashtable commport_hashtable;

char * reportInterrupts(void);

#endif /* _COMMS_H */

