/**************************************************************************
* Copyright (c) 2001, 2003 by Punch Telematix. All rights reserved.       *
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

#ifndef _COMMS_H
#define  _COMMS_H

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

#define IOFLAG_REAL_FLAGS (IOFLAG_DTR|IOFLAG_RTS|IOFLAG_DSR|IOFLAG_CTS|IOFLAG_CD|IOFLAG_RI)

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

