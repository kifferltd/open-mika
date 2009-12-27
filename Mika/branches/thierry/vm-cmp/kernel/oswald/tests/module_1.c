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

static int d = 0;
int e = 0;
int parameter = 100;

extern int c;

extern int get_int(int a);

static int bss_space[10];

//#define AFTER
#define BEFORE

#ifdef BEFORE
int get_int(int a) {

  return a + e + 512;
  
}
#endif

extern int defined_in_2;

extern int is_an_exported_function(int a);

int do_add(int a, int b) {

  int result;
  
  printf("1 doing the adding\n");

  result = d + e;
  printf("2 result is now %d\n", result);

  result += c;
  printf("3 result is now %d\n", result);

  result += a;
  printf("4 result is now %d\n", result);

  result += b;
  printf("5 result is now %d\n", result);

  result += get_int(256);
  printf("6 result is now %d\n", result);

  result = is_an_exported_function(result);
  printf("7 result is now %d\n", result);

  printf("Joepie de poepie\n");

  e = result;
    
  return result + defined_in_2;
  
}

#ifdef AFTER
int get_int(int a) {

  return a + e + 512;
  
}
#endif

void init_module(void) {
  printf("initializing module, parameter = %d\n", parameter);
}

void clean_module(void) {
  printf("cleaning module, parameter = %d\n", parameter);
}

/*
** JNI stuff
*/

void Java_Peer_my_destroy__AAB_I_String(void * env, void * AA_bytes, int I, void * String) {
}

void Java_Peer_1underscore_my_1destroy__Ljava_lang_String_2 (void *a, int b, int c) {
}

void Java_Peer_1underscore_my_1destroy___3_3_3BILjava_lang_String_2 (void * a, int b, int c, int d) {
}

void Java_Peer_destroy__Ljava_lang_String_2 (void *a, int b, int c) {
}

void Java_Peer_destroy___3_3_3BILjava_lang_String_2 (void * a, int b, int c, int d) {
}
