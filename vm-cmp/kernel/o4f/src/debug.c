/**************************************************************************
* Copyright (c) 2020, 2021, 2022 by KIFFER Ltd. All rights reserved.      *
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
#include "im4000uart.h"

#define ABORT_BUFSIZE 256
#define LOEMPA_BUFSIZE 1024
static char loempa_buf[LOEMPA_BUFSIZE];
static im4000_com_t debug_uart = 0xdeadbeef;

static void initDebugUart(void) {
    debug_uart = x_uart_init_by_name("COM3");
}

static bool ensureDebugUartIsInitialised() {
  if (xTaskGetSchedulerState() == taskSCHEDULER_NOT_STARTED) {
    return false;
  }
  if (debug_uart == 0xdeadbeef) {
    initDebugUart();
  }
  return true;
}

w_int x_debug_read(const void *buf, size_t count) {
  w_int bytes_read = -1;
  if (ensureDebugUartIsInitialised()) {
    bytes_read = x_uart_read(debug_uart, buf, count);
  }

  return bytes_read;
}

void x_debug_write(const void *buf, size_t count) {
  if (ensureDebugUartIsInitialised()) {
    x_uart_write(debug_uart, buf, count);
  }
}

void _o4f_abort(char *file, int line, int type, char *message, x_status rc) {
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

