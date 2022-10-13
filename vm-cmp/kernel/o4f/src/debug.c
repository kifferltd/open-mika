/**************************************************************************
* Copyright (c) 2020, 2021 by KIFFER Ltd. All rights reserved.            *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#include "oswald.h"
#include "iot_uart.h"
#include "im4000uart.h"

static IotUARTHandle_t uart_handle = NULL;
static SemaphoreHandle_t uart_mutex;

#define ABORT_BUFSIZE 256
#define LOEMPA_BUFSIZE 1024
static char loempa_buf[LOEMPA_BUFSIZE];

static void initDebugUart(void) {
  w_int lUartInstance = IM4000_COM3;
  w_int iResult = IOT_UART_INVALID_VALUE;
  const size_t uNumBytesToRead = 10u;
  w_ubyte puRxBuffer[16] = { 0 };
  w_ubyte puTxBuffer[ sizeof( puRxBuffer ) + 2 ] = { 0 };
  IotUARTConfig_t xUARTConfig;
  w_int iNumBytesRead = 0;

  uart_mutex = xSemaphoreCreateMutex();
  configASSERT( NULL != uart_mutex );

  uart_handle = iot_uart_open( lUartInstance );
  configASSERT( NULL != uart_handle );

   /* Set UART configuration */
    xUARTConfig.ulBaudrate = 115200u;
    xUARTConfig.ucWordlength = 8u;
    xUARTConfig.xParity = eUartParityNone;
    xUARTConfig.xStopbits = eUartStopBitsOne;
    xUARTConfig.ucFlowControl = 1u; /* Enable flow control. */
    iResult = iot_uart_ioctl(uart_handle, eUartSetConfig, &xUARTConfig);
    configASSERT( IOT_UART_SUCCESS == iResult );
}

static bool ensureUartIsInitialised() {
  if (xTaskGetSchedulerState() == taskSCHEDULER_NOT_STARTED) {
    return false;
  }
  if (!uart_handle) {
    printf("\nInitialising UART\n");
    initDebugUart();
  }
  return true;
}

w_int x_debug_read(const void *buf, size_t count) {
  w_int bytes_read = -1;
  if (ensureUartIsInitialised()) {
    if (xTaskGetSchedulerState() == taskSCHEDULER_RUNNING) { 
      xSemaphoreTake(uart_mutex, portMAX_DELAY);
    }

    w_int rc = iot_uart_read_sync(uart_handle, buf, count);
    if (IOT_UART_SUCCESS == rc ) {
      rc = iot_uart_ioctl(uart_handle, eGetRxNoOfbytes, &bytes_read);
    }
    if (IOT_UART_SUCCESS != rc ) {
      printf("iot_uart_ioctl(eGetRxNoOfbytes) returned %d\r\n", rc);
      bytes_read = -rc;
    }

    if (xTaskGetSchedulerState() == taskSCHEDULER_RUNNING) { 
      xSemaphoreGive(uart_mutex);
    }
  }

  return bytes_read;
}

void x_debug_write(const void *buf, size_t count) {
  if (ensureUartIsInitialised()) {
    if (xTaskGetSchedulerState() == taskSCHEDULER_RUNNING) { 
      xSemaphoreTake(uart_mutex, portMAX_DELAY);
    }
    iot_uart_write_sync(uart_handle, buf, count);
    if (xTaskGetSchedulerState() == taskSCHEDULER_RUNNING) { 
      xSemaphoreGive(uart_mutex);
    }
  }
}

void _o4f_abort(char *file, int line, int type, char *message, x_status rc){
  char strerror_buf[ABORT_BUFSIZE];

  x_thread kthread;
  x_status status = xs_success;
  w_thread thread = NULL;
  va_list ap;

  vTaskSuspendAll();

  w_int hdrlen = x_snprintf(strerror_buf, ABORT_BUFSIZE, "%s:%d ", file, line);
  w_int totlen = hdrlen;

  switch(type) {
  case O4F_ABORT_BAD_STATUS:
    totlen += x_snprintf(strerror_buf + totlen, ABORT_BUFSIZE - totlen, "\r\nOSWALD BAD STATUS: %s %4d: %s returned '%s'", file, line, message, x_status2char(rc));
    break;

  case O4F_ABORT_OVERFLOW:
    totlen += x_snprintf(strerror_buf + totlen, ABORT_BUFSIZE - totlen, "\r\nOSWALD OVERFLOW: %s %4d: %s", file, line, message);
    break;

  case O4F_ABORT_MEMCHUNK:
    totlen += x_snprintf(strerror_buf + totlen, ABORT_BUFSIZE - totlen, "\r\nOSWALD MEMCHUNK: %s %4d: %s", file, line, message);
    break;

  case O4F_ABORT_MONITOR:
    if (rc <= xs_unknown) {
      totlen += x_snprintf(strerror_buf + totlen, ABORT_BUFSIZE - totlen, "\r\nOSWALD MONITOR: %s %4d: %s returned '%s'", file, line, message, x_status2char(rc));
    }
    else {
      totlen += x_snprintf(strerror_buf + totlen, ABORT_BUFSIZE - totlen, "\r\nOSWALD MONITOR: %s %4d: %s: %p", file, line, message, rc);
    }
    break;

  default:
    totlen += x_snprintf(strerror_buf + totlen, ABORT_BUFSIZE - totlen, "\r\nOSWALD ABORT called with unknown type %d and message '%s'", type, message);
  }
  x_debug_write(strerror_buf, totlen);
  vTaskDelay(1000);

  exit(1);
}

#ifdef DEBUG

void _loempa(const char *file, const char *function, const int line, const int level, const char *fmt, ...) {

  va_list ap;

  if (o4fe->status == O4F_ENV_STATUS_NORMAL) {
    vTaskSuspendAll();
  }

  w_int hdrlen = x_snprintf(loempa_buf, LOEMPA_BUFSIZE, "OSWALD %s %s.%d: ", file, function, line);
  w_int totlen = hdrlen;
  va_start(ap, fmt);
  totlen += x_vsnprintf(loempa_buf + totlen, LOEMPA_BUFSIZE - totlen, fmt , ap);
  va_end(ap);
  x_debug_write(loempa_buf, totlen);

  if (o4fe->status == O4F_ENV_STATUS_NORMAL) {
    xTaskResumeAll();
  }

}

#endif

