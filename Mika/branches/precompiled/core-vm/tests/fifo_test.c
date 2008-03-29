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

/*
** $Id: fifo_test.c,v 1.2 2004/11/18 23:51:52 cvs Exp $
*/

#include <stdio.h>
#include <string.h>
#include "tests.h"
#include "oswald.h"
#include "wonka.h"
#include "oswald.h"
#include "fifo.h"

#define FFT_STACK_SIZE ((1024 * 1) + MARGIN)

w_void fifo_test1(w_void *t) {
  w_int   i, k = 0;
  w_fifo  fifo;
  w_int   result;

  /*
  ** Initialize some Wonka structures.
  */

  wonka_init();

  fifo = allocFifo(127);

  while(1) {
    oempa("[fifo] Pass %d\n", k++);

    /*
    ** Fill the fifo
    */
    
    for(i=0; i<20000; i++) {
      putFifo((void *)i, fifo);
    }

    /*
    ** Clear the fifo
    */
      
    for(i=0; i<20000; i++) {

      result = (int)getFifo(fifo);
      if(result != i) {
        oempa("[fifo] Retrieved value != added value (%d != %d)\n", result, i);
        exit(0);
      }
    }
    
    x_thread_sleep(1);
  }

}

static x_thread ft1_thread;

x_ubyte * fifo_test(x_ubyte * memory) {
  x_status status;

  oempa("Starting fifo tests\n");
 
  ft1_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(ft1_thread, fifo_test1, ft1_thread, x_alloc_static_mem(memory, FFT_STACK_SIZE), FFT_STACK_SIZE, 4, TF_START);

  if (status != xs_success) {
    oempa("Could not start fifo test thread... Status is '%s'\n", x_status2char(status));
    exit(0);
  }
  
  return memory;
  
}

