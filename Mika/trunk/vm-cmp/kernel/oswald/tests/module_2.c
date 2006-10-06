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
