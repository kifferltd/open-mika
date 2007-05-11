#ifndef _TESTS_H
#define _TESTS_H

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

/*
** $Id: tests.h,v 1.1.1.1 2004/07/12 14:07:48 cvs Exp $
*/

#include "wonka.h"

#define BASE_STACK_SIZE (1024 * 5) // Minimum is 3K, tried 2K and it segfaults on Linux x86

/*
** The margin we want when allocating stacks.
*/

#define MARGIN (1024 * 1)

#ifndef RUNTIME_CHECKS
#define RUNTIME_CHECKS
#endif

#include <oswald.h>

x_ubyte * hashtable_test(x_ubyte * memory);
x_ubyte * fifo_test(x_ubyte * memory);
x_ubyte * ts_mem_test(x_ubyte * memory);

w_void wonka_init(w_void);

void * x_mem_get(x_size size);

void _oempa(const char *function, const int line, const char *fmt, ...);
#define oempa(format, a...) _oempa(__FUNCTION__, __LINE__, format, ##a)

#endif /* _TESTS_H */

