/**************************************************************************
* Copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
**************************************************************************/

#include "core-classes.h"
#include "fields.h"

w_int Hashtable_firstBusySlot(JNIEnv *env, w_instance thisHashtable, w_int i) {
  w_int cap = getIntegerField(thisHashtable, F_Hashtable_capacity);
  w_instance ki = getReferenceField(thisHashtable, F_Hashtable_keys);
  w_instance *ka = instance2Array_instance(ki);

  if (i >= 0) {
    for(; i < cap; ++i) {
      if(ka[i]) { 

        return i;

      }
    }
  }

  return -1;
}

/*
w_boolean HashtableElementEnum_hasMoreElements(JNIEnv *env, w_instance thisHashtableElementEnum) {
  return Hashtable_firstBusySlot(env, getReferenceField(thisHashtableElementEnum, F_Hashtable_dollar_HashtableElementEnum_this_dollar_0), getIntegerField(thisHashtableElementEnum, F_Hashtable_dollar_HashtableElementEnum_nextSlot)) >= 0;
}

w_boolean HashtableKeyEnum_hasMoreElements(JNIEnv *env, w_instance thisHashtableKeyEnum) {
  return Hashtable_firstBusySlot(env, getReferenceField(thisHashtableKeyEnum, F_Hashtable_dollar_HashtableKeyEnum_this_dollar_0), getIntegerField(thisHashtableKeyEnum, F_Hashtable_dollar_HashtableKeyEnum_nextSlot)) >= 0;
}
*/


