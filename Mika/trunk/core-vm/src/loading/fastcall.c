/**************************************************************************
* Copyright  (c) 2006, 2010 by Chris Gray, /k/ Embedded Java Solutions.
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of other contributors
*    may be used to endorse or promote products derived from this software
*    without specific prior written permission.
* 
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
* INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
* FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL /K/
* EMBEDDED SOLUTIONS OR OTHER CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
* INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
* OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
* 
**************************************************************************/

#include "clazz.h"
#include "constant.h"
#include "exception.h"
#include "loading.h"
#include "wonka.h"
#include "strings.h"
#include "methods.h"
#include "fastcall.h"

w_fastclass static_calls[FAST_STATIC_CLASSES];
w_fastclass virtual_calls[FAST_VIRTUAL_CLASSES];
w_fastclass special_calls[FAST_SPECIAL_CLASSES];

w_string clazz_name_Character;
w_string clazz_name_Hashtable;
w_string clazz_name_Math;

void fastcall_check_class(w_fastclass fclass, w_string method_name, 
 w_string method_sig, unsigned char * bytecodes) {
  int i;

  woempa(5,"looking for %w.%w%w\n",fclass->class_name,method_name,method_sig);
  for (i=0 ; fclass->calls[i] != NULL; i++) {
    if((method_name == fclass->calls[i]->method_name)
      && (fclass->calls[i]->method_sig == method_sig)){

     woempa(1,"Using  %w.%w%w fastcalls\n",fclass->class_name,
           fclass->calls[i]->method_name,fclass->calls[i]->method_sig);

      //A bit of a hack to guarantee that some classes get initialized...
      if(fclass->class_name ==  clazz_name_Character) {
        mustBeInitialized(clazzCharacter);
      }
#ifdef NATIVE_MATH
      else if(fclass->class_name ==  clazz_name_Math) {
        mustBeInitialized(clazzMath);
      }
#endif
      bytecodes[-1] = 0xd3;
      bytecodes[0]  = fclass->calls[i]->index >> 8;
      bytecodes[1]  = fclass->calls[i]->index & 0xff;
      return;
    }
  }
}

void fastcall_check_invoke(w_clazz clazz, unsigned char * bytecodes, w_fastclass* classes, w_int size) {
  w_int idx = (bytecodes[0] << 8) | bytecodes[1];
  w_string clazz_name = NULL;
  w_string method_name = NULL;
  w_string method_sig = NULL;

  if(getMemberConstantStrings(clazz, idx, &clazz_name, &method_name, &method_sig)) {

    for(idx=0 ; idx < size ; idx++) {
      if(classes[idx]->class_name == clazz_name) {
        fastcall_check_class(classes[idx], method_name, method_sig, bytecodes);
        break;
      }
    }
  }
  if(clazz_name) {
    deregisterString(clazz_name);
  }
  if(method_name) {
    deregisterString(method_name);
  }
  if(method_sig) {
    deregisterString(method_sig);
  }
}

void fastcall_check_invoke_static(w_clazz clazz, unsigned char * bytecodes) {
#ifdef USE_FAST_CALLS
  fastcall_check_invoke(clazz,bytecodes,static_calls, FAST_STATIC_CLASSES);
#endif
}

void fastcall_check_invoke_virtual(w_clazz clazz, unsigned char * bytecodes) {
#ifdef USE_FAST_CALLS
  fastcall_check_invoke(clazz,bytecodes,virtual_calls, FAST_VIRTUAL_CLASSES);
#endif
}

void fastcall_check_invoke_special(w_clazz clazz, unsigned char * bytecodes) {
#ifdef USE_FAST_CALLS
  fastcall_check_invoke(clazz,bytecodes,special_calls, FAST_SPECIAL_CLASSES);
#endif
}

w_fastclass createClassTable(w_size size, w_string name) {
  w_size i = 0;
  w_fastclass current = (w_fastclass) allocMem(sizeof(w_FastClass));
  current->class_name = name;
  current->calls = allocMem((size+1)*sizeof(w_fastcall));
  current->calls[size] = NULL;
  for(; i < size ; i++) {
    current->calls[i] = (w_fastcall)allocMem(sizeof(w_FastCall));
  }
  return current;
}

void fastcall_init_tables() {
#ifdef USE_FAST_CALLS
  w_fastclass current;

  w_string no_args_void = cstring2String("()V", 3);
  w_string no_args_int = cstring2String("()I", 3);
  w_string double_double = cstring2String("(D)D", 4);
  w_string clazz_name_String = cstring2String("java/lang/String", 16);
  w_string init = cstring2String("<init>", 6);

  clazz_name_Character = cstring2String("java/lang/Character", 19);
  clazz_name_Hashtable = cstring2String("java/util/Hashtable", 19);
  clazz_name_Math = cstring2String("java/lang/Math", 14);

  current = createClassTable(1,cstring2String("java/lang/System", 16));
  current->calls[0]->index = FAST_SYSTEM_CURRENTTIMEMILLIS;
  current->calls[0]->method_name = cstring2String("currentTimeMillis", 17);
  current->calls[0]->method_sig = cstring2String("()J", 3);
  static_calls[0] = current;

  current = createClassTable(5,clazz_name_Character);
  static_calls[1] = current;
  current->calls[0]->index = FAST_CHARACTER_DIGIT_CHAR_INT;
  current->calls[0]->method_name = cstring2String("digit", 5);
  current->calls[0]->method_sig = cstring2String("(CI)I", 5);
  current->calls[1]->index = FAST_CHARACTER_FORDIGIT_INT_INT;
  current->calls[1]->method_name = cstring2String("forDigit", 8);
  current->calls[1]->method_sig = cstring2String("(II)C", 5);
  current->calls[2]->index = FAST_CHARACTER_ISLETTER;
  current->calls[2]->method_name = cstring2String("isLetter", 8);
  current->calls[2]->method_sig = cstring2String("(C)Z", 4);
  current->calls[3]->index = FAST_CHARACTER_ISWHITESPACE;
  current->calls[3]->method_name = cstring2String("isWhitespace", 12);
  current->calls[3]->method_sig = cstring2String("(C)Z", 4);
  current->calls[4]->index = FAST_CHARACTER_ISDIGIT_CHAR;
  current->calls[4]->method_name = cstring2String("isDigit", 7);
  current->calls[4]->method_sig = cstring2String("(C)Z", 4);

#ifdef NATIVE_MATH
  current = createClassTable(8,clazz_name_Math);
  current->calls[0]->index = FAST_MATH_SQRT;
  current->calls[0]->method_name = cstring2String("sqrt", 4);
  current->calls[0]->method_sig = double_double;
  current->calls[1]->index = FAST_MATH_SIN;
  current->calls[1]->method_name = cstring2String("sin", 3);
  current->calls[1]->method_sig = double_double;
  current->calls[2]->index = FAST_MATH_COS;
  current->calls[2]->method_name = cstring2String("cos", 3);
  current->calls[2]->method_sig = double_double;
  current->calls[3]->index = FAST_MATH_TAN;
  current->calls[3]->method_name = cstring2String("tan", 3);
  current->calls[3]->method_sig = double_double;
  current->calls[4]->index = FAST_MATH_ASIN;
  current->calls[4]->method_name = cstring2String("asin", 4);
  current->calls[4]->method_sig = double_double;
  current->calls[5]->index = FAST_MATH_ATAN;
  current->calls[5]->method_name = cstring2String("atan", 4);
  current->calls[5]->method_sig = double_double;
  current->calls[6]->index = FAST_MATH_LOG;
  current->calls[6]->method_name = cstring2String("log", 3);
  current->calls[6]->method_sig = double_double;
  current->calls[7]->index = FAST_MATH_EXP;
  current->calls[7]->method_name = cstring2String("exp", 3);
  current->calls[7]->method_sig = double_double;
  static_calls[2] = current;
#endif

  current = createClassTable(3,cstring2String("java/lang/StringBuffer", 22));
  virtual_calls[0] = current;
  current->calls[0]->index = FAST_STRINGBUFFER_APPEND_STRING;
  current->calls[0]->method_name = cstring2String("append", 6);
  current->calls[0]->method_sig = cstring2String("(Ljava/lang/String;)Ljava/lang/StringBuffer;", 44);
  current->calls[1]->index = FAST_STRINGBUFFER_APPEND_CHAR;
  current->calls[1]->method_name = cstring2String("append", 6);
  current->calls[1]->method_sig = cstring2String("(C)Ljava/lang/StringBuffer;", 27);
  current->calls[2]->index = FAST_STRINGBUFFER_TOSTRING;
  current->calls[2]->method_name = cstring2String("toString", 8);
  current->calls[2]->method_sig = cstring2String("()Ljava/lang/String;", 20);

  current = createClassTable(8,clazz_name_String);
  virtual_calls[1] = current;
  current->calls[0]->index = FAST_STRING_SUBSTRING;
  current->calls[0]->method_name = cstring2String("substring", 9);
  current->calls[0]->method_sig = cstring2String("(II)Ljava/lang/String;", 22);
  current->calls[1]->index = FAST_STRING_INDEXOF_CHAR;
  current->calls[1]->method_name = cstring2String("indexOf", 7);
  current->calls[1]->method_sig = cstring2String("(II)I", 5);
  current->calls[2]->index = FAST_STRING_CHARAT;
  current->calls[2]->method_name = cstring2String("charAt", 6);
  current->calls[2]->method_sig = cstring2String("(I)C", 4);
  current->calls[3]->index = FAST_STRING_EQUALS;
  current->calls[3]->method_name = cstring2String("equals", 6);
  current->calls[3]->method_sig = cstring2String("(Ljava/lang/Object;)Z", 21);
  current->calls[4]->index = FAST_STRING_HASHCODE;
  current->calls[4]->method_name = cstring2String("hashCode", 8);
  current->calls[4]->method_sig = no_args_int;
  current->calls[5]->index = FAST_STRING_LENGTH;
  current->calls[5]->method_name = cstring2String("length", 6);
  current->calls[5]->method_sig = no_args_int;
  current->calls[6]->index = FAST_STRING_TOSTRING;
  current->calls[6]->method_name = cstring2String("toString", 8);
  current->calls[6]->method_sig = cstring2String("()Ljava/lang/String;", 20);
  current->calls[7]->index = FAST_STRING_STARTSWITH;
  current->calls[7]->method_name = cstring2String("startsWith", 10);
  current->calls[7]->method_sig = cstring2String("(Ljava/lang/String;I)V", 22);

  current = createClassTable(1,clazz_name_Hashtable);
  virtual_calls[2] = current;
  current->calls[0]->index = FAST_HASHTABLE_FIRSTBUSYSLOT;
  current->calls[0]->method_name = cstring2String("firstBusySlot", 13);
  current->calls[0]->method_sig = cstring2String("(I)I", 4);

  current = createClassTable(3,clazz_name_String);
  special_calls[0] = current;
  current->calls[0]->index = FAST_STRING_CREATE_BYTE;
  current->calls[0]->method_name = init;
  current->calls[0]->method_sig = cstring2String("([BIII)V", 8);
  current->calls[1]->index = FAST_STRING_CREATE_CHAR;
  current->calls[1]->method_name = init;
  current->calls[1]->method_sig = cstring2String("([CII)V", 7);
  current->calls[2]->index = FAST_STRING_CREATE_EMPTY;
  current->calls[2]->method_name = init;
  current->calls[2]->method_sig = no_args_void;
#endif
}

