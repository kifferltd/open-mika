#ifndef _UART_H
#define _UART_H

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

