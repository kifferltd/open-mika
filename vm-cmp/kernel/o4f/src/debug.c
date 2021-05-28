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

#define ABORT_BUFSIZE 256

void _o4f_abort(char *file, int line, int type, char *message, x_status rc){
  char strerror_buf[ABORT_BUFSIZE];

  x_thread kthread;
  x_status status = xs_success;
  w_thread thread = NULL;
  va_list ap;
  switch(type) {
  case O4F_ABORT_BAD_STATUS:
    x_snprintf(strerror_buf, ABORT_BUFSIZE, "\nOSWALD BAD STATUS: %s %4d: %s returned '%s'", file, line, message, x_status2char(rc));
    break;

  case O4F_ABORT_OVERFLOW:
    x_snprintf(strerror_buf, ABORT_BUFSIZE, "\nOSWALD OVERFLOW: %s %4d: %s", file, line, message);
    break;

  case O4F_ABORT_MONITOR:
    x_snprintf(strerror_buf, ABORT_BUFSIZE, "\nOSWALD MONITOR: %s %4d: %s", file, line, message);
    break;

  default:
    x_snprintf(strerror_buf, ABORT_BUFSIZE, "\nOSWALD ABORT called with unknown type %d and message '%s'", type, message);
  }

  exit(1);
}

#ifdef DEBUG

#define BSIZE 1024

ssize_t write(int fd, const void *buf, size_t count);

void _loempa(const char *file, const char *function, const int line, const int level, const char *fmt, ...) {

  va_list ap;

  if (o4fe->status == O4F_ENV_STATUS_NORMAL) {
    vTaskSuspendAll();
  }

  printf("OSWALD %s %s.%d: ", file, function, line);
  va_start(ap, fmt);
  vprintf(fmt, ap);
  va_end(ap);

  if (o4fe->status == O4F_ENV_STATUS_NORMAL) {
    xTaskResumeAll();
  }

}

#endif

