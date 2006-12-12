#ifndef _QUEUE_TEST_
#define _QUEUE_TEST_

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
** $Id: queue_test.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

void queue_test(void*);

void* queue_stack[2];	// the pointers to the mem of our stacks of our threads
void* queue_struct[2];	// the pointers to the mem of our structs for our threads
void* q_thread;
void* q_stack;

x_queue queue;

int produced;
int stuurtext[5];

void queue_producer(void* arg);
void queue_consumer(void* arg);

#endif //_QUEUE_TEST_
