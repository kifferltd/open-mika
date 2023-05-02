#ifndef _TESTS_H
#define _TESTS_H

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

#include "oswald.h"

void timer_test(void);
void mutex_test(void);
void thread_test(void);
void block_test(void);
void memory_test(void);
void sem_test(void);
void queue_test(void);
void monitor_test(void);
void signals_test(void);
void map_test(void);
void atomic_test(void);
void modules_test(void);
void exception_test(void);
void list_test(void);
void stack_test(void);
void join_test(void);

void * x_mem_get(x_size size);
x_int find_thread_in_pcbs(x_thread thread);

void _oempa(const char *function, const int line, const char *fmt, ...);
#define oempa(format, a...) _oempa(__FUNCTION__, __LINE__, format, ##a)

extern x_size prio_offset;

#endif /* _TESTS_H */
