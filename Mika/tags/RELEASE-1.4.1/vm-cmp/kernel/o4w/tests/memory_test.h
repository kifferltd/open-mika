#ifndef _MEMORY_TEST_
#define _MEMORY_TEST_

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
** $Id: memory_test.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

void memory_test(void*);

x_thread alloc_thread;
x_thread calloc_thread;
x_thread realloc_thread;
x_thread tag_thread;

void alloc_prog(void*);
void calloc_prog(void*);
void realloc_prog(void*);
void tag_prog(void*);

#endif //_MEMORY_TEST_
