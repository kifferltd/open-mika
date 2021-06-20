/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
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

#include "core-classes.h"
#include "uart-classes.h"
#include "clazz.h"
#include "comms.h"
#include "exception.h"
#include "hashtable.h"
#include "loading.h"
#include "wstrings.h"
#include "mika_threads.h"
#include "arrays.h"
#include "ts-mem.h"
#include "driver_byteserial.h"

/* File UARTControlStream.c
*/
/* Constants which define serial port event types
**
** These have to be manually kept in line with javax/comm/SerialPortEvent
**   = bummer!
** TODO: devise a way to do this automagically
*/

#define DATA_AVAILABLE 1
#define OUTPUT_BUFFER_EMPTY 2
#define CTS   3
#define DSR   4
#define RI    5
#define CD    6
#define OE    7
#define PE    8
#define FE    9
#define BI   10

/* Helper functions for object constructors
*/
/* createFromString
** Initialise an instance of UARTControlStream using the named UART.
** Called from the constructor \textsf{UARTControlStream(String)}.
*/
void
UARTControlStream_createFromString
( JNIEnv *env, w_instance thisUARTControlStream,
  w_instance nameString
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_string   nameStr;
  w_device commport;
  char      *name;

  if (!nameString) {
    throwNullPointerException(thread);
  }
  else {

    nameStr = String2string(nameString);
    name = string2UTF8(nameStr, NULL);
 
    commport = getDeviceByName(name + 2);
    if (commport->type != wdt_byte_serial) {
      woempa(9,"Device %s is of wrong type! (%d s/b %d)\n", name + 2, commport->type, wdt_byte_serial);
      commport = NULL;
    }
    if (!commport) {
      throwException(thread,clazzIllegalArgumentException,NULL);
    }
    else if (deviceBSOpen(name + 2, wdp_write | wdp_read) == NULL) {
      throwIOException(thread);
    }
    else {
      setWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit, commport);
    }
    releaseMem(name);
  }
}

/* getbaudrate
** Get the baud rate for this UART by querying the driver.
*/
w_int
UARTControlStream_getbaudrate
( JNIEnv *env, w_instance thisUARTControlStream
) {
  w_thread thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;

  if(commport == NULL || deviceBSQuery(commport, wdi_get_bitrate, &reply, x_eternal) != wds_success) {
    throwIOException(thread);
  }
  
  return (w_int)reply;
}

/* setbaudrate
** Set the bitrate of the UART to the given value.    
** Uses the \textsf{set} function of the underlying driver.
** The \textsf{query} function is then used to enable the user to check
** whether the operation was successful.
*/
w_int
UARTControlStream_setbaudrate
( JNIEnv *env, w_instance thisUARTControlStream,
  w_int     newrate
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply = 0;

  if(commport && deviceBSSet(commport, wdi_set_bitrate, (w_word)newrate, x_eternal) == wds_success) {
    if(deviceBSQuery(commport, wdi_get_bitrate, &reply, x_eternal) == wds_success) {
      return (w_int)reply;
    }
  }

  throwIOException(thread);
  return 0;
}

/* getdatabits
** Get the number of data bits used on this UART by querying the driver.
*/
w_int
UARTControlStream_getdatabits
( JNIEnv *env, w_instance thisUARTControlStream
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;


  if(commport == NULL || deviceBSQuery(commport, wdi_get_databits, &reply, x_eternal) != wds_success) {
    throwIOException(thread);
  }

  return (w_int)reply;
}

/* setdatabits
** Set the number of data bits used on the UART to the given value.    
** Uses the \textsf{set} function of the underlying driver.
** The \textsf{query} function is then used to enable the user to check
** whether the operation was successful.
*/
w_int
UARTControlStream_setdatabits
( JNIEnv *env, w_instance thisUARTControlStream,
  w_int     newbits
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply = 0;

  if(commport && deviceBSSet(commport, wdi_set_databits, (w_word)newbits, x_eternal) == wds_success) {
    if(deviceBSQuery(commport, wdi_get_databits, &reply, x_eternal) == wds_success) {
      return (w_int)reply;
    }
  }
  
  throwIOException(thread);
  return 0;
}

/* getstopbits
** Get the number of stop bits used on this UART by querying the driver.
*/
w_int
UARTControlStream_getstopbits
( JNIEnv *env, w_instance thisUARTControlStream
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;

  if(commport == NULL || deviceBSQuery(commport, wdi_get_stopbits, &reply, x_eternal) != wds_success) {
    throwIOException(thread);
  }

  return (w_int)reply;
}

/* setstopbits
** Set the number of stop bits used on the UART to the given value.    
** Uses the \textsf{set} function of the underlying driver.
** The \textsf{query} function is then used to enable the user to check
** whether the operation was successful.
*/
w_int
UARTControlStream_setstopbits
( JNIEnv *env, w_instance thisUARTControlStream,
  w_int     newbits
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;

  if(commport && deviceBSSet(commport, wdi_set_stopbits, (w_word)newbits, x_eternal) == wds_success) {
    if(deviceBSQuery(commport, wdi_get_stopbits, &reply, x_eternal) == wds_success) {
      return (w_int)reply;
    }
  }
  
  throwIOException(thread);
  return 0;
}

/* getflowcontrol
** Get the type of flow control (if any) used on this UART by querying the driver.
*/
w_int
UARTControlStream_getflowcontrol
( JNIEnv *env, w_instance thisUARTControlStream
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;

  if(commport == NULL || deviceBSQuery(commport, wdi_get_flowcontrol, &reply, x_eternal) != wds_success) {
    throwIOException(thread);
  }

  return (w_int)reply;
}

/* setflowcontrol
** Set the type of flow control (if any) used on this UART.    
** Parameter \textsf{newflowcon} is one of the \texttt{FLOWCON_xxx}
** constants defined in \texttt{comms.c}.
** Uses the \textsf{set} function of the underlying driver.
** The \textsf{query} function is then used to enable the user to check
** whether the operation was successful.
*/
w_int
UARTControlStream_setflowcontrol
( JNIEnv *env, w_instance thisUARTControlStream,
  w_int     newflowcon
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;

  if(commport && deviceBSSet(commport, wdi_set_flowcontrol, (w_word)newflowcon, x_eternal) == wds_success) {
    if(deviceBSQuery(commport, wdi_get_flowcontrol, &reply, x_eternal) == wds_success) {
      return (w_int)reply;
    }
  }

  throwIOException(thread);
  return 0;
}

/* getparity
** Get the parity setting for this UART by querying the driver.
*/
w_int
UARTControlStream_getparity
( JNIEnv *env, w_instance thisUARTControlStream
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;

  if(commport == NULL || deviceBSQuery(commport, wdi_get_parity, &reply, x_eternal) != wds_success) {
    throwIOException(thread);
  }
  
  return (w_int)reply;
}

/* setdtr
** Set the type of parity checking (if any) used on this UART.    
** Uses the \textsf{set} function of the underlying driver.
** The \textsf{query} function is then used to enable the user to check
** whether the operation was successful.
*/
w_int
UARTControlStream_setparity
( JNIEnv *env, w_instance thisUARTControlStream,
  w_int     newparity
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;

  if(commport && deviceBSSet(commport, wdi_set_parity, (w_word)newparity, x_eternal) == wds_success) {
    if(deviceBSQuery(commport, wdi_get_parity, &reply, x_eternal) == wds_success) {
      return (w_int)reply;
    }
  }
  
  throwIOException(thread);
  return 0;
}

/* getdsr
** Get the current logical value of the \texttt{DSR} signal.    
** Uses the \textsf{query} function of the underlying driver.
*/
w_boolean
UARTControlStream_getdsr
( JNIEnv *env, w_instance thisUARTControlStream
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;

  if(commport == NULL || deviceBSQuery(commport, wdi_get_signals, &reply, x_eternal) != wds_success) {
    throwIOException(thread);
  }

  return !!(reply&IOFLAG_DSR);
}

/* getcd
** Get the current logical value of the \texttt{CD} signal.    
** Uses the \textsf{query} function of the underlying driver.
*/
w_boolean
UARTControlStream_getcd
( JNIEnv *env, w_instance thisUARTControlStream
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;

  if(commport == NULL || deviceBSQuery(commport, wdi_get_signals, &reply, x_eternal) != wds_success) {
    throwIOException(thread);
  }

  return !!(reply&IOFLAG_CD);
}

/* getcts
** Get the current logical value of the \texttt{CTS} signal.    
** Uses the \textsf{query} function of the underlying driver.
*/
w_boolean
UARTControlStream_getcts
( JNIEnv *env, w_instance thisUARTControlStream
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;

  if(commport == NULL || deviceBSQuery(commport, wdi_get_signals, &reply, x_eternal) != wds_success) {
    throwIOException(thread);
  }

  return !!(reply&IOFLAG_CTS);
}

/* getri
** Get the current logical value of the \texttt{RI} signal.    
** Uses the \textsf{query} function of the underlying driver.
*/
w_boolean
UARTControlStream_getri
( JNIEnv *env, w_instance thisUARTControlStream
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;

  if(commport == NULL || deviceBSQuery(commport, wdi_get_signals, &reply, x_eternal) != wds_success) {
    throwIOException(thread);
  }

  return !!(reply&IOFLAG_RI);
}

/* setdtr
** Set the \texttt{DTR} signal to the given logical value.    
** Uses the \textsf{set} function of the underlying driver.
** The \textsf{query} function is then used to enable the user to check
** whether the operation was successful.
*/
w_boolean
UARTControlStream_setdtr
( JNIEnv *env, w_instance thisUARTControlStream,
  w_boolean newdtr
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;

  if(commport && deviceBSSet(commport, wdi_set_signals, (w_word)((IOFLAG_DTR<<16)|(newdtr?IOFLAG_DTR:0)), x_eternal) == wds_success) {
    if(deviceBSQuery(commport, wdi_get_signals, &reply, x_eternal) == wds_success) {
      return !!(reply&IOFLAG_DTR);
    }
  }
  
  throwIOException(thread);
  return 0;
}

/* setrts
** Set the \texttt{RTS} signal to the given logical value.    
** Uses the \textsf{set} function of the underlying driver.
** The \textsf{query} function is then used to enable the user to check
** whether the operation was successful.
*/
w_boolean
UARTControlStream_setrts
( JNIEnv *env, w_instance thisUARTControlStream,
  w_boolean newrts
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_word     reply;

  if(commport && deviceBSSet(commport, wdi_set_signals, (w_word)((IOFLAG_RTS<<16)|(newrts?IOFLAG_RTS:0)), x_eternal) == wds_success) {
    if(deviceBSQuery(commport, wdi_get_signals, &reply, x_eternal) == wds_success) {
      return !!(reply&IOFLAG_RTS);
    }
  }

  throwIOException(thread);
  return 0;
}


/* sendBreak
** Send a \texttt{BREAK} signal.
** Uses the \textsf{set} function of the underlying driver.
*/
void
UARTControlStream_sendbreak (JNIEnv *env, w_instance thisUARTControlStream,
  w_int millis
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);

  if(commport == NULL || deviceBSSet(commport, wdi_send_break, (w_word)millis, x_eternal) != wds_success) {
    throwIOException(thread);
  }
}


/* waitforevent
** Call the underlying driver's \textsf{ioevt} function, and analyse the result.
*/
void
UARTControlStream_waitforevent (JNIEnv *env, w_instance thisUARTControlStream,
  w_instance  theSerialPortEvent
) {
  w_thread   thread = JNIEnv2w_thread(env);
  w_device commport = (w_device)getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit);
  w_int      event;
  w_int      param;

  if (commport == NULL || deviceBSQuery(commport, wdi_wait_for_event, &event, x_eternal) != wds_success) {
    throwIOException(thread);
    return;
  }

  if(NULL == getWotsitField(thisUARTControlStream, F_UARTControlStream_wotsit)){
    throwIOException(thread);
  }

  deviceBSQuery(commport, wdi_get_event_data, &param, x_eternal);

  switch (event) {
    case(IOEVT_DATA_AVAILABLE):
      setIntegerField(theSerialPortEvent, F_SerialPortEvent_eventtype,  DATA_AVAILABLE);
      break;

    case(IOEVT_SIGNALS_CHANGED):
      
      if (param & (IOFLAG_DSR<<16)) {
        setIntegerField(theSerialPortEvent, F_SerialPortEvent_eventtype, DSR);
        setBooleanField(theSerialPortEvent, F_SerialPortEvent_oldvalue, !(param & IOFLAG_DSR));
        setBooleanField(theSerialPortEvent, F_SerialPortEvent_newvalue, !!(param & IOFLAG_DSR));
      }
      else if (param & (IOFLAG_CTS<<16)) {
        setIntegerField(theSerialPortEvent, F_SerialPortEvent_eventtype, CTS);
        setBooleanField(theSerialPortEvent, F_SerialPortEvent_oldvalue, !(param & IOFLAG_CTS));
        setBooleanField(theSerialPortEvent, F_SerialPortEvent_newvalue, !!(param & IOFLAG_CTS));
      }
      else if (param & (IOFLAG_RI<<16)) {
        setIntegerField(theSerialPortEvent, F_SerialPortEvent_eventtype, RI);
        setBooleanField(theSerialPortEvent, F_SerialPortEvent_oldvalue, !(param & IOFLAG_RI));
        setBooleanField(theSerialPortEvent, F_SerialPortEvent_newvalue, !!(param & IOFLAG_RI));
      }
      else { //if (param & (IOFLAG_CD<<16)) 
        setIntegerField(theSerialPortEvent, F_SerialPortEvent_eventtype, CD);
        setBooleanField(theSerialPortEvent, F_SerialPortEvent_oldvalue, !(param & IOFLAG_CD));
        setBooleanField(theSerialPortEvent, F_SerialPortEvent_newvalue, !!(param & IOFLAG_CD));
      }
      break;

    case(IOEVT_ERROR_DETECTED):
      if (param & (IOFLAG_OE)) {
        setIntegerField(theSerialPortEvent, F_SerialPortEvent_eventtype, OE);
      }
      else if (param & (IOFLAG_PE)) {
        setIntegerField(theSerialPortEvent, F_SerialPortEvent_eventtype, PE);
      }
      else { // if (param & (IOFLAG_FE)) 
        setIntegerField(theSerialPortEvent, F_SerialPortEvent_eventtype, FE);
      }
      break;

    case(IOEVT_BREAK_DETECTED):
      setIntegerField(theSerialPortEvent, F_SerialPortEvent_eventtype, BI);
      break;

    default:
      throwIOException(thread);
  }
}

/* native method
** close()V
** Ask the underlying driver to close the device for input.
*/
void
UARTControlStream_close(JNIEnv *env, w_instance thisStream) {
  w_device s = (w_device)getWotsitField(thisStream, F_UARTControlStream_wotsit);

  if (s) {
    clearWotsitField(thisStream, F_UARTControlStream_wotsit);
    deviceBSClose(s);
  }
}
