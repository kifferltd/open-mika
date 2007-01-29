#ifndef _DISPATCHER_H_
#define _DISPATCHER_H_

/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
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
*                                                                         *
* Modifications copyright (c) 2006 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: dispatcher.h,v 1.6 2006/10/04 14:24:14 cvsroot Exp $
*/

void initialize_native_dispatcher(w_frame caller, w_method method);

void native_instance_synchronized_reference(w_frame caller, w_method method);
void native_instance_synchronized_32bits(w_frame caller, w_method method);
void native_instance_synchronized_64bits(w_frame caller, w_method method);
void native_instance_synchronized_void(w_frame caller, w_method method);

void native_instance_unsynchronized_reference(w_frame caller, w_method method);
void native_instance_unsynchronized_32bits(w_frame caller, w_method method);
void native_instance_unsynchronized_64bits(w_frame caller, w_method method);
void native_instance_unsynchronized_void(w_frame caller, w_method method);

void native_static_synchronized_reference(w_frame caller, w_method method);
void native_static_synchronized_32bits(w_frame caller, w_method method);
void native_static_synchronized_64bits(w_frame caller, w_method method);
void native_static_synchronized_void(w_frame caller, w_method method);

void native_static_unsynchronized_reference(w_frame caller, w_method method);
void native_static_unsynchronized_32bits(w_frame caller, w_method method);
void native_static_unsynchronized_64bits(w_frame caller, w_method method);
void native_static_unsynchronized_void(w_frame caller, w_method method);

typedef void (*w_callfun)(w_frame frame, w_method method);

void initialize_dispatcher(w_frame caller, w_method method);

extern w_callfun dispatchers[];

/*
** If USE_SPECIAL_CASE_DISPATCHERS is defined, special cases such as
** trivial methods and getters or setters will use special dispatchers.
*/
#define USE_SPECIAL_CASE_DISPATCHERS

/*
** If BACKPATCH_SPECIAL_CASES is defined, special-case dispatchers will
** patch their call sites whenever they are called with invokenonvirtual 
** or invokestatic. Not enabled by default, as it seems to offer little
** gain in return for the increased VM code size and complexity.
** Only operative if USE_SPECIAL_CASE_DISPATCHERS is defined.
*/
//#define BACKPATCH_SPECIAL_CASES

#endif /* _DISPATCHER_H_ */
