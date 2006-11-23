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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

#include <string.h>

/* 
** $Id: varia.c,v 1.2 2005/06/14 09:46:04 cvs Exp $ 
*/

#include "threads.h"    // currentWonkaThread
#include "wstrings.h"   // w_string

#include "awt-classes.h"
#include "rudolph.h"
#include "platform.h"
#include "varia.h"

#include <stdio.h>     // printf, scanf, ...

void w_string2c_string(w_string string, char *c_string) {
  /*
  ** Convert array of w_chars to a C-style string:
  */

  if (string_length(string) > 0) {
    char *dummy = (char *)c_string;
    int i = 0;
        
    for (i = 0; i < (w_int)string_length(string); ++i) {
       (*dummy) = string_char(string, i);
       dummy++;
    }
    (*dummy) = '\0';
  }
  else {
    *c_string = '\0';
  }
}

void pixel_dump(r_pixel pixel) {
  const int size = (pixels2bytes(1)*8 + 1);
  char buffer[size];
  w_word mask = (unsigned)(1 << (size - 2));
  w_int i = 0;
  
  memset(buffer, 0x00, (size_t)size);
  while (mask) {
    if (pixel & mask) {
      buffer[i++] = '1';
    }
    else {
      buffer[i++] = '0';
    }
    mask >>= 1;
  }

  printf(" bits of pixel '%x' are '%s'\n", pixel, buffer);  
}
