/*****************************************************************************
* Copyright (c) 2005 by Chris Gray, trading as /k/ Embedded Java Solutions. *
* All rights reserved.  The contents of this file may not be copied or      *
* distributed in any form without express written consent of the author.    *
*****************************************************************************/

#include <stdio.h>

#include "core-classes.h"
#include "math-classes.h"
#include "exception.h"

typedef  union {
  unsigned short W[4];
  w_double value;
  double d;
} math_constant;


void   Math_shrink_String(char* chars,  int retval) {
  if(chars[retval] == 0) {
    retval--;  
  }
  while (chars[retval] == '0')  {
     chars[retval--] = 0;
  }
  if(chars[retval] == '.') {
    chars[retval+1] = '0';
  }
}


w_instance MathHelper_static_doubleToString(JNIEnv *env, w_instance myClazz, w_double value) {
  char chars[64];
  int retval;
  math_constant mc;
  double d ;
  char* pattern = "%.10f";

#ifdef ARM
  mc.value = value<<32 | value>>32;
#else
  mc.value = value;
#endif
  d = mc.d;
 
  retval = snprintf(chars, 64, pattern,d);
  if (retval == -1 || retval >= 64) {
     w_thread thread = JNIEnv2w_thread(env); 
     throwException(thread, clazzIllegalArgumentException, NULL);
     printf("retval = %d when formatting D %g with '%s'\n",retval,d,pattern);
     return NULL;
  }

  Math_shrink_String(chars, retval);
  return (*env)->NewStringUTF(env, chars);
}

w_instance MathHelper_static_floatToString(JNIEnv *env, w_instance myClazz, w_float value) {
  char chars[64];
  int retval;
  float f =  *((float*)&value);
  char* pattern = "%.7f";

  retval = snprintf(chars, 64, pattern,f);

  if (retval == -1 || retval >= 64) {
     w_thread thread = JNIEnv2w_thread(env); 
     throwException(thread, clazzIllegalArgumentException, NULL);
     printf("retval = %d when formatting F %g with '%s'\n",retval,f,pattern);
     return NULL;
  }

  Math_shrink_String(chars, retval);
  
  return (*env)->NewStringUTF(env, chars);
}
void init_math(void) {
  collectMathFixups();
  loadMathClasses();
}
