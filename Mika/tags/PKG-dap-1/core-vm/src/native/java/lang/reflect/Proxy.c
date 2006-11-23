/**************************************************************************
*                                                                         *
* Copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
*                                                                         *
**************************************************************************/

#include "clazz.h"
#include "core-classes.h"

w_boolean Proxy_static_isProxyClass(JNIEnv *env, w_instance classProxy, w_instance theClass) {
  w_clazz clazz = Class2clazz(theClass);
  return !! isSet(clazz->flags, CLAZZ_IS_PROXY);
}

