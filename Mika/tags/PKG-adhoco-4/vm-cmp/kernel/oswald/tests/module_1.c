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
