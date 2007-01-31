#ifndef _MUTEX_TEST_H
#define _MUTEX_TEST_H

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
** $Id: mutex_test.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

int count;
x_mutex mutex;

void mutex_test(void*);
void* mutex_stack[10];	// the pointers to the mem of our stacks of our threads
void* mutex_struct[10];	// the pointers to the mem of our structs for our threads

void* mu_thread;
void* mu_stack;

void create_mutex_mem(void);
void mutex_prog(void* arg);

#endif //_MUTEX_TEST_H
