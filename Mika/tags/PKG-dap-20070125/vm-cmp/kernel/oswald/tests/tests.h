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
** $Id: tests.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#define BASE_STACK_SIZE (1024 * 5) // Minimum is 3K, tried 2K and it segfaults on Linux x86

/*
** The margin we want when allocating stacks.
*/

#define MARGIN (1024 * 10)

#ifndef DEBUG
#define DEBUG
#endif

#ifndef RUNTIME_CHECKS
#define RUNTIME_CHECKS
#endif

#include <oswald.h>

x_ubyte * timer_test(x_ubyte * memory);
x_ubyte * mutex_test(x_ubyte * memory);
x_ubyte * thread_test(x_ubyte * memory);
x_ubyte * block_test(x_ubyte * memory);
x_ubyte * memory_test(x_ubyte * memory);
x_ubyte * sem_test(x_ubyte * memory);
x_ubyte * queue_test(x_ubyte * memory);
x_ubyte * monitor_test(x_ubyte * memory);
x_ubyte * signals_test(x_ubyte * memory);
x_ubyte * map_test(x_ubyte * memory);
x_ubyte * atomic_test(x_ubyte * memory);
x_ubyte * modules_test(x_ubyte * memory);
x_ubyte * exception_test(x_ubyte * memory);
x_ubyte * list_test(x_ubyte * memory);
x_ubyte * stack_test(x_ubyte * memory);
x_ubyte * join_test(x_ubyte * memory);

void * x_mem_get(x_size size);
x_int find_thread_in_pcbs(x_thread thread);

void _oempa(const char *function, const int line, const char *fmt, ...);
#define oempa(format, a...) _oempa(__FUNCTION__, __LINE__, format, ##a)

extern x_size prio_offset;

#endif /* _TESTS_H */
