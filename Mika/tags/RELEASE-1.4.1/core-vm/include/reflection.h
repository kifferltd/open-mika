#ifndef _REFLECTION_H
#define _REFLECTION_H

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
*                                                                         *
* Modifications copyright (c) 2006 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

#include "jni.h"
#include "wonka.h"

w_boolean widen(w_clazz F_clazz, w_word F_data[], w_clazz T_clazz, void *T_data);
w_instance createWrapperInstance(w_thread thread, w_clazz clazz, w_int *slot);

w_frame invoke(JNIEnv *env, w_method method, w_instance This, w_instance Arguments);
w_clazz getWrappedValue(w_instance value, w_word **data);

void voidProxyMethodCode(JNIEnv *env, w_instance thisProxy, ...);
w_word singleProxyMethodCode(JNIEnv *env, w_instance thisProxy, ...);
w_long doubleProxyMethodCode(JNIEnv *env, w_instance thisProxy, ...);

void wrapException(w_thread thread, w_clazz wrapper_clazz, w_size field_offset);
#endif/* _REFLECTION_H */
