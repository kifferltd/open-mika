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

#include <stdio.h>

static int d = 0; // Should be no problem
//int e = 0;        // Should complain about 'multiply defined'
extern int e; // Defined in module_1.c

extern int c;

extern int parameter;

extern int get_int(int a); // in module_1

extern int is_an_exported_function(int a); // in kernel

int defined_in_2 = 100;

int second_add(int a, int b) {

  int result;
  
  printf("2 doing the adding\n");

  result = d + e;
  printf("2 result is now %d\n", result);

  result += c;
  printf("2 result is now %d\n", result);

  result += a;
  printf("2 result is now %d\n", result);

  result += b;
  printf("2 result is now %d\n", result);

  result += get_int(256);
  printf("2 result is now %d\n", result);

  result = is_an_exported_function(result);
  printf("2 result is now %d\n", result);
  
  return result;
  
}

void init_module(void) {
  printf("module 2: initializing, parameter = %d\n", parameter);
}

void clean_module(void) {
  printf("module 2: cleaning, parameter = %d\n", parameter);
}
