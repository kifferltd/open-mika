/*****************************************************************************
* Copyright (c) 2005 by Chris Gray, trading as /k/ Embedded Java Solutions. *
* All rights reserved.  The contents of this file may not be copied or      *
* distributed in any form without express written consent of the author.    *
*****************************************************************************/

#include <stdio.h>
#include <math.h>

#include "core-classes.h"
#include "math-classes.h"
#include "exception.h"

typedef  union {
  unsigned short W[4];
  w_double wd;
  double d;
  w_float wf;
  float f;
} math_constant;


int Math_shrink_String(char* chars,  int len) {
  while (chars[len] == 0) {
    len--;  
  }
  while (chars[len] == '0')  {
     chars[len--] = 0;
  }
  if(chars[len] == '.') {
    chars[++len] = '0';
  }

  return len + 1;
}

int Math_truncate_String(char* chars) {
  int len = strlen(chars);
  int k;
  int carry = 0;
  char *point = strstr(chars, ".");

  /*
   * Exit without doing anything if no decimal point is present.
   */ 
  if (!point) {
    return len;
  }

  k = len - 1;
  if (chars[k] == '0') {
    while (chars[k] == '0') {
      len = k--;
    }
    goto postlude;
  }

  if (chars[k] == '.') {
    k = k - 1;
  }
  carry = chars[len - 1] > '5' || chars[len - 1] == '5' && (chars[k] & 1);

  len = len - 1;
  k = len;
  while (carry) {
    if (k == 0) {
      memmove(chars + 1, chars, len++);
      chars[0] = '1';

      goto postlude;
    }
    if (chars[k - 1] == '.') {
      --k;
    }

    chars[k - 1] += carry;
    if (chars[k - 1] > '9') {
      chars[k - 1] = '0';
      if (k-- == len) {
        --len;
      }
    }
    else {
      carry = 0;
    }

  }

postlude:
  if (chars[len - 1] == '.') {
    chars[len++] = '0';
  }
  chars[len] = 0;

  return len;
}


w_instance MathHelper_static_doubleToString(JNIEnv *env, w_instance myClazz, w_double value) {
  char chars[64];
  char trial[64];
  int l;
  math_constant mc;
  double d0, d1;
  double diff0, diff1;
  char* pattern = "%.17f";

#ifdef ARM
  mc.wd = value<<32 | value>>32;
#else
  mc.wd = value;
#endif
  d0 = mc.d;
 
  l = snprintf(chars, 64, pattern, d0);
  if (l == -1 || l >= 64) {
     w_thread thread = JNIEnv2w_thread(env); 
     throwException(thread, clazzIllegalArgumentException, NULL);
     return NULL;
  }

  l = Math_shrink_String(chars, l);

  strncpy(trial, chars, l + 1);
  sscanf(chars, "%lf", &d1);
  diff0 = fabsl(d0 - d1);
  if (diff0 < d0 * 0.75e-15) diff0 = d0 * 0.75e-15;
  diff1 = diff0;
  while (TRUE) {
    int l0 = Math_truncate_String(trial);
    if (l0 >= l) {
      break;
    }

    l = l0;
    sscanf(trial, "%lf", &d1);
    if (fabs(d0 - d1) > diff0) {
      break;
    }
    strncpy(chars, trial, l + 1);
  }

  return (*env)->NewStringUTF(env, chars);
}

w_instance MathHelper_static_floatToString(JNIEnv *env, w_instance myClazz, w_float value) {
  char chars[64];
  char trial[64];
  int l;
  math_constant mc;
  float f0, f1;
  float diff0, diff1;
  char* pattern = "%.9f";

  mc.wf = value;
  f0 = mc.f;
  l = snprintf(chars, 64, pattern, f0);
  if (l == -1 || l >= 64) {
     w_thread thread = JNIEnv2w_thread(env); 
     throwException(thread, clazzIllegalArgumentException, NULL);
     return NULL;
  }

  l = Math_shrink_String(chars, l);

  strncpy(trial, chars, l + 1);
  sscanf(chars, "%f", &f1);
  diff0 = fabsf(f0 - f1);
  if (diff0 < f0 * 0.7e-7f) diff0 = f0 * 0.7e-7f;
  diff1 = diff0;
  while (TRUE) {
    int l0 = Math_truncate_String(trial);
    if (l0 == l) {
      break;
    }

    l = l0;
    sscanf(trial, "%f", &f1);
    if (fabsf(f0 - f1) > diff0) {
      break;
    }
    strncpy(chars, trial, l + 1);
  }

  return (*env)->NewStringUTF(env, chars);
}

void init_math(void) {
  collectMathFixups();
  loadMathClasses();
}
