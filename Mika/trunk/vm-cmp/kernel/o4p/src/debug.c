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

#include <pthread.h>

void _o4p_abort(char *file, int line, int type, char *message, int rc){
  char strerror_buf[64];

  switch(type) {
  case O4P_ABORT_BAD_STATUS:
    _wabort(file, line, ABORT_WONKA, "%s returned '%s'", message, x_status2char(rc));
    break;

  case O4P_ABORT_PTHREAD_RETCODE:
     if (strerror_r(rc, strerror_buf, 64) == 0) {
      _wabort(file, line, ABORT_WONKA, "%s returned '%s'", message, strerror_buf);
    }
    break;

  case O4P_ABORT_OVERFLOW:
    _wabort(file, line, ABORT_WONKA, "%s", message);
    break;

  default:
    _wabort(file, line, ABORT_WONKA, "o4p_abort called with unknown type %d and message '%s'", type, message);
  }
}

#ifdef DEBUG

static pthread_mutex_t loempa_mutex = PTHREAD_MUTEX_INITIALIZER;

#define BSIZE 1024

ssize_t write(int fd, const void *buf, size_t count);

void _loempa(const char *function, const int line, const int level, const char *fmt, ...) {

  va_list ap;
  char buffer[BSIZE];
  x_size i;

  pthread_mutex_lock(&loempa_mutex);

  i = x_snprintf(buffer, BSIZE, "OS %35s %4d: ", function, line);
  va_start(ap, fmt);
  i += x_vsnprintf(buffer + i, BSIZE - i, fmt, ap);
  va_end(ap);

  write(STDOUT_FILENO, buffer, i);
  pthread_mutex_unlock(&loempa_mutex);

}

#endif

