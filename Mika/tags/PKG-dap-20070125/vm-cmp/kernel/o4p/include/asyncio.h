#ifndef _ASYNCIO_H
#define _ASYNCIO_H

/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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

#include "oswald.h"

/*
 * Replace by macros, because gcc 4.x is reluctant to inline stuff
static inline x_int x_async_register(x_int fd) {
  return 0;
}

static inline void x_async_unregister(x_int fd) {
}

static inline x_int x_async_block(x_int fd, x_int timeout) {
  x_thread_sleep(x_usecs2ticks(50000));
  return 0;
}

static inline void x_async_setup(void) {
}
*/

#define x_async_register(fd) 0

#define x_async_unregister(fd)

#define x_async_block(fb,t) x_thread_sleep(x_usecs2ticks(t))

#define x_async_setup()

#endif /* _ASYNCIO_H */

