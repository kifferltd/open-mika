#include "oswald.h"

#include <pthread.h>

void _o4p_abort(char *file, int line, int type, char *message, int rc){
  char strerror_buf[64];

  switch(type) {
  case O4P_ABORT_BAD_STATUS:
    _wabort(file, line, ABORT_WONKA, "%s returned '%s'", message, x_status2char(rc));
  case O4P_ABORT_PTHREAD_RETCODE:
     if (strerror_r(rc, strerror_buf, 64) == 0) {
      _wabort(file, line, ABORT_WONKA, "%s returned '%s'", message, strerror_buf);
    }
    else {
      _wabort(file, line, ABORT_WONKA, "%s returned unknown error code %d", message, rc);
    }
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

