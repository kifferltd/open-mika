#ifndef _monitor_test_
#define _monitor_test_

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
** $Id: monitor_test.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

void monitor_test(void*);

int ERRORs;

void prog1(void*);
void prog2(void*);
void prog3(void*);
void prog4(void*);

x_monitor monitor1;
x_monitor monitor2;
x_monitor monitor3;

int counter;

void* mstack[10];	// the pointers to the mem of our stacks of our threads
void* mstruct[10];	// the pointers to the mem of our structs for our threads
void create_mem(void);
void* m_thread; 
void* m_stack; 

#endif // _monitor_test_
