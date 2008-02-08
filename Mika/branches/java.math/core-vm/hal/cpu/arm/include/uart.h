#ifndef _UART_H
#define _UART_H

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

#include "oswald.h"
#include "wonka.h"

/**@name General \texttt{\#define}s
*/
//@{

/**@name UART bitrate scaler
** To set the UART to bitrate <n> bits/sec, set its prescaler to 230400/n - 1.
*/
///
#define BITRATE_DIVIDEND 230400

/**@name Calculate rx buffer size
** Rule-of-thumb for calculating receive buffer size: n/8 bytes (covers about
** 1200 msec of latency for continuous asynch data).
*/
///
#define RX_BUF_SIZE_DIVISOR 8

/**@name UART snooze time
** Number of milliseconds to wait for a change in the modem signals
** or for new data to arrive
*/
///
#define UART_SIGNAL_SNOOZE (w_size)25 

/**@name I/O flags IOFLAG_AVAIL, IOFLAG_EMPTY
** Internally we use these flags for DATA_AVAILABLE and OUTPUT_EMPTY
*/
#define IOFLAG_AVAIL  0x8000
#define IOFLAG_EMPTY  0x0080 // N.B. not yet implemented!
//@}

/**@name Reference to the VHDL-defined UART structure
*/
//@{
///
typedef struct UART * w_uartRegs;
//@}

/**@name Structure used to describe a communications port
*/
typedef struct w_Uart {
//@{
/**@name UART clock speed (bits/sec)
*/
//@{
///
  w_int      bitrate;
//@}
/**@name Receive buffer
** Receive buffer: 
** \begin{itemize}
** \item[]size, start, point where next char will be read/written;
** \item[]number of bytes currently used; and 
** \item[]the value of rxBytesInBuf at which  DATA_AVAILABLE will be signalled.
** \end{itemize}
*/
//@{
///
  w_int      rxBufSize;
///
  w_ubyte    *rxBuffer;
///
  w_ubyte    *rxReadPtr;
///
  w_ubyte    *rxWritePtr;
///
  w_int      rxBytesInBuf;
///
  w_int      rxThreshold;
//@}
/**@name Semaphores
** Counting semaphores used to synchronize producers and consumers of
** received bytes, sent bytes, and i/o events.
*/
//@{
///
  x_Sem     rxSem;    // count = bytes in buffer
///
  x_Sem     txSem;    // count = bytes buffered in producer
///
  x_Sem     evtSem;   // count = outstanding events
//@}
/**@name Valid flags
** Which flags (DTR, RTS, DSR, CTS, CD, RI) are supported by this UART
*/
//@{
///
  w_short    validflags;
//@}
/**@name Event reporting
** Values of the IOFLAGS for this uart:
**   oldflags = already reported via ioevt
**   newflags = latest available values
**   thisflag: only one bit is set, indicating which bit of newflags
**             caused ioevt to return control to its caller last time.
**   thisvalue: either zero or equal to thisflag, depending on whether
**             the value of the flag identified by thisflag was 0 or 1.
*/
//@{
///
  w_short    oldflags;
///
  w_short    newflags;
///
  w_short    thisflag;
///
  w_short    thisvalue;
///
//@}

/**@name Misc. flags
** Other flags used internally for congestion control etc.
*/
//@{
///
  w_word     miscflags;
//@}
/**@name IRQ number and Interrupt Handler function
** The number of the IRQ which we hook, the name of the function
** which handles the IRQ, and its entry point address.
*/
//@{
///
  unsigned int irqNumber;
///
  char *irqHandlerName;
///
  void (*irqHandler)(void);
//@}

/**@name Hardware registers
** Pointer to the hardware registers of the UART
*/
///
  w_uartRegs uartRegs;
} w_Uart;

/**@name w_uart
** We also define a type w_uart, a pointer to a w_Uart.
*/
///
typedef struct w_Uart *w_uart;
//@}

/*
** Miscellaneous flags
*/
#define UART_OPEN_FOR_READ   1
#define UART_OPEN_FOR_WRITE  2
// Rx congestion:
#define UART_RX_THROTTLE     4  // apply ctsrts flow control if available
#define UART_RX_DISCARD      8  // discard incoming data
// HW flow control:
#define UART_CTSRTS_NONE     0
#define UART_CTSRTS_IN       16
#define UART_CTSRTS_OUT      32

#define UART_WAIT_FOR_IOEVT 256

#define SYS_STREAM_UART0 4
#define SYS_STREAM_UART1 5
#define SYS_STREAM_UART2 6
#define SYS_STREAM_UART3 7
#define SYS_STREAM_UART4 8
#define SYS_STREAM_CONSOLE 9

#define UART00_DEFAULT_BITRATE 115200
#define UART01_DEFAULT_BITRATE 9600
#define UART02_DEFAULT_BITRATE 115200
#define UART03_DEFAULT_BITRATE 9600
#define UART04_DEFAULT_BITRATE 9600

extern void startUarts(void);
extern void startSpecials(void);
extern void startConsole(void);

extern w_Uart uart00;
extern w_Uart uart01;
extern w_Uart uart02;
extern w_Uart uart03;
extern w_Uart uart04;

#endif

