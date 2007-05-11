/*
** $Id: random.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <oswald.h>

/*
** ================ Random Number Generator from BSD =======================
**
** Copyright (c) 1983 Regents of the University of California.
** All rights reserved.
**
** Redistribution and use in source and binary forms are permitted
** provided that: (1) source distributions retain this entire copyright
** notice and comment, and (2) distributions including binaries display
** the following acknowledgement:  ``This product includes software
** developed by the University of California, Berkeley and its contributors''
** in the documentation or other materials provided with the distribution
** and in all advertising materials mentioning features or use of this
** software. Neither the name of the University nor the names of its
** contributors may be used to endorse or promote products derived
** from this software without specific prior written permission.
** THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
** IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
** WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
*/

static x_word randtbl[32] = { 0L,
  0x9a319039L, 0x32d9c024L, 0x9b663182L, 0x5da1f342L, 
  0xde3b81e0L, 0xdf0a6fb5L, 0xf103bc02L, 0x48f340fbL, 
  0x7449e56bL, 0xbeb1dbb0L, 0xab5c5918L, 0x946554fdL, 
  0x8c2e680fL, 0xeb3d799fL, 0xb11ee0b7L, 0x2d436b86L, 
  0xda672e2aL, 0x1588ca88L, 0xe369735dL, 0x904f35f7L, 
  0xd7158fd6L, 0x6fa6f051L, 0x616e6b96L, 0xac94efdcL, 
  0x36413f93L, 0xc622c298L, 0xf5a42ab8L, 0x8a88d77bL, 
  0xf5ad9d0eL, 0x8999220bL, 0x27fb47b9L
};

static x_word *state = &randtbl[1];
static x_word *end_ptr = &randtbl[32];

/*
** Each thread has two state variables for so that we don't need to use a mutex
** or something to get a good random number. Therefore we implemented a random
** number generator setup function that is called from within the thread creation 
** function.
*/

void x_init_random(x_thread thread) {

  thread->fptr = &randtbl[4];
  thread->rptr = &randtbl[1];

}

/*
** This returns a good 31 bit random number on a thread by thread basis.
*/

x_word x_random(void) {

  x_word i;
	
  *thread_current->fptr += *thread_current->rptr;
  i = (*thread_current->fptr >> 1) & 0x7fffffffUL;  /* chucking least random bit */
  if (++thread_current->fptr >= end_ptr) {
    thread_current->fptr = state;
    ++thread_current->rptr;
  }
  else {
    if (++thread_current->rptr >= end_ptr) {
      thread_current->rptr = state;
    }
  }

  return i;

}

