/**************************************************************************
* Copyright (c) 2021 by KIFFER Ltd. All rights reserved.                  *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#include <string.h>

#include "clazz.h"
#include "exception.h"
#include "interpreter.h"
#include "loading.h"
#include "methods.h"
#include "descriptor.h"
#include "wstrings.h"
#include "mika_threads.h"

/*
** Convert a pointer into the method code to a PC.
*/
w_int code2pc(w_method method, w_code opcodeptr) {

  if (isSet(method->flags, ACC_NATIVE)) {
    return -1;
  }

  return opcodeptr - method->exec.code;

}

/*
** From a byte code pointer of a method, give back the source line.
*/

w_int code2line(w_method method, w_code opcodeptr) {
  w_methodDebugInfo debug_info = method->exec.debug_info;
  w_size pc = opcodeptr - method->exec.code;
  w_size this_line = 0;
  w_size this_pc;
  w_int i;

  if (isSet(method->flags, ACC_NATIVE)) {
    return -1;
  }

  if (!debug_info) {
    return -2;
  }

  woempa(1,"Method %p has code %p: codeptr %p => pc %d\n",method,method->exec.code,opcodeptr,pc);
  for (i = 0; i < debug_info->numLineNums; i++) {
    this_line = debug_info->lineNums[i].line_number;
    this_pc = debug_info->lineNums[i].start_pc;
    woempa(1,"  line %d is pc %d\n",this_line,this_pc);
    if (this_pc <= pc) {
      if ((i + 1) < debug_info->numLineNums) {
        w_size next_pc = debug_info->lineNums[i + 1].start_pc;
        if (next_pc > pc) {
          woempa(1,"  line %d is pc %d, so use previous\n",debug_info->lineNums[i + 1].line_number,next_pc);
          break;
        }
      }
      else {
        woempa(1,"  line %d is last line, so use it\n",this_line);
        break;
      }
    }
   }

  return this_line;
  
}

w_int createMethodSpecUsingDescriptor(w_clazz declaring_clazz, w_string name, w_string desc_string, w_MethodSpec **specptr) {
  w_MethodSpec *spec = allocMem(sizeof(w_MethodSpec));
  w_clazz  item_clazz;
  w_size   i;
  w_size   nargs = 0;
  w_clazz *args = NULL;
  w_size   rparen;

  if (!spec) {
    wabort(ABORT_WONKA, "Unable to allocate space for spec\n");
  }
  woempa(1, "Method name is '%w', descriptor '%w', declaring class is %k, spec at %p\n", name, desc_string, declaring_clazz, spec);
  spec->declaring_clazz = declaring_clazz;
  spec->name = name;
  if (string_char(desc_string, 0) != '(') {
    woempa(9, "Missing '(' in %w\n", desc_string);
    i = string_length(desc_string);
    rparen = 1;
  }
  else {
    i = 1;
    rparen = string_length(desc_string);
  }

  while(--rparen && string_char(desc_string, rparen) != ')');

  if (!rparen) {
    woempa(9, "Missing ')' in %w\n", desc_string);
    releaseMem(spec);

    return  CLASS_LOADING_FAILED;

  }

  if (rparen > 1) {
    args = allocMem(256 * sizeof(w_clazz));
    if (!args) {
      woempa(9, "No memory to allocate arg_types array!\n");
    releaseMem(spec);

      return CLASS_LOADING_FAILED;

    }

    while (i < rparen && nargs < 256) {
      item_clazz = parseDescriptor(desc_string, &i, rparen, declaring_clazz->loader);
      if (item_clazz) {
        woempa(1, "Arg[%d] has type %k\n", nargs, item_clazz);
        args[nargs] = item_clazz;
        if (mustBeLoaded(&args[nargs]) == CLASS_LOADING_FAILED) {
          woempa(9, "Failed to load element %d of %w\n", i, desc_string);
          releaseMem(args);
          releaseMem(spec);

          return CLASS_LOADING_FAILED;
        }
        ++nargs;
      }
      else {
        woempa(9, "Failed to parse element %d of %w\n", i, desc_string);
        releaseMem(args);
        releaseMem(spec);

        return CLASS_LOADING_FAILED;

      }
    }

    spec->arg_types = reallocMem(args, (nargs + 1) * sizeof(w_clazz));
    if (!spec->arg_types) {
      wabort(ABORT_WONKA, "Unable to allocate space for spec->arg_types\n");
    }
    spec->arg_types[nargs] = NULL;
  }
  else {
    spec->arg_types = NULL;
  }

  i = rparen + 1;
  item_clazz = parseDescriptor(desc_string, &i, string_length(desc_string), declaring_clazz->loader);
  if (item_clazz) {
    woempa(1, "Return type %k\n", item_clazz);
    spec->return_type = item_clazz;
    if (mustBeLoaded(&spec->return_type) == CLASS_LOADING_FAILED) {
      woempa(9, "Failed to load return type of %w\n", desc_string);
      if (spec->arg_types) {
        releaseMem(spec->arg_types);
      }
      releaseMem(spec);

      return CLASS_LOADING_FAILED;
    }
  }
  else {
    woempa(9, "Failed to parse element %d of %w\n", i, desc_string);
    if (spec->arg_types) {
      releaseMem(spec->arg_types);
    }
    releaseMem(spec);

    return CLASS_LOADING_FAILED;

  }

  *specptr = spec;

  return CLASS_LOADING_SUCCEEDED;
}

void releaseMethodSpec(w_MethodSpec *spec) {
  if (spec->arg_types) {
    w_int i;

    if (spec->arg_types) {
      for (i = 0; spec->arg_types[i]; ++i) {
        if (getClazzState(spec->arg_types[i]) == CLAZZ_STATE_UNLOADED) {
          deregisterUnloadedClazz(spec->arg_types[i]);
        }
      }
      releaseMem(spec->arg_types);
    }
  }
  if (spec->return_type && getClazzState(spec->return_type) == CLAZZ_STATE_UNLOADED) {
     deregisterUnloadedClazz(spec->return_type);
  }
}

/*
** See whether a method matches a w_MethodSpec.
** - If the name is different, the match fails.
** - If the w_MethodSpec has return_type != NULL, and the return_type of
**   the method does not agree, the match fails.
** - If the method has a non-empty argument list and the spec has an
**   empty argument list, or vice versa, the match fails.
** - Otherwise the argument lists are compared: if they are both the same
**   length and the arg_type's resolve pairwise to the same class, the 
**   match succeeds.
**
** Note that this function may indirectly call mustBeLoaded() on parameter
** and return types. It is the caller's responsibility to ensure thread 
** safety.
*/
w_boolean methodMatchesSpec(w_method method, w_MethodSpec *spec) {
  w_size  j;

  if (!method->spec.name || !method->spec.return_type) {
    wabort(ABORT_WONKA, "Method has %s == NULL!\n", method->spec.name ? "return_type" : "name");
  }

  woempa(1, "  method name is %w, spec name is %w\n", method->spec.name, spec->name);

  if (method->spec.name != spec->name) {
    woempa(1, "Sorry.\n");

    return WONKA_FALSE;

  }

  woempa(1, "  method return type is %K, spec return type is %K\n", method->spec.return_type, spec->return_type);
  if (spec->return_type && !sameClassReference(&method->spec.return_type, &spec->return_type)) {
    woempa(1, "Sorry.\n");

    return WONKA_FALSE;

  }

  woempa(1, "  method arg list is %p, spec arg list is %p (must be both null or both non-null)\n", method->spec.arg_types, spec->arg_types);
  if (!!method->spec.arg_types != !!spec->arg_types) {
    woempa(1, "Sorry.\n");

    return WONKA_FALSE;

  }

  if (!method->spec.arg_types && !spec->arg_types) {
    woempa(1, "  method and spec arg lists both empty -> bingo\n");

    return WONKA_TRUE;

  }

   for (j = 0; method->spec.arg_types[j] && spec->arg_types[j]; ++j) {
    woempa(1, "  Arg[%d]: type is %K, looking for %K\n", j, method->spec.arg_types[j], spec->arg_types[j]);
    if (!sameClassReference(&method->spec.arg_types[j], &spec->arg_types[j])) {
      woempa(1, "Sorry, arg[%d] does not match: method has %K, spec has %K.\n", j, method->spec.arg_types[j], spec->arg_types[j]);

      return WONKA_FALSE;

    }
  }

  if (!method->spec.arg_types[j] && !spec->arg_types[j]) {
    woempa(1, "Bingo!\n");

    return WONKA_TRUE;

  }

  return WONKA_FALSE;
}

void _registerNativeMethod(w_clazz clazz, w_fun_dec fp, const char * utf8name, const char * utf8sig) {
  w_method method = NULL;
  w_string name_string;
  w_string desc_string;
  w_MethodSpec *spec;
  w_size   i;

  name_string = utf2String(utf8name, strlen(utf8name));
  desc_string = utf2String(utf8sig, strlen(utf8sig));
  if (!name_string || !desc_string) {
    wabort(ABORT_WONKA, "Unable to convert name and desc to w_string\n");
  }
  if (createMethodSpecUsingDescriptor(clazz, name_string, desc_string, &spec) == CLASS_LOADING_FAILED) {
    w_thread thread = currentWonkaThread;
    if (thread && clazzLinkageError) {
      woempa(9, "%t %k %k %w %w\n", thread, clazzLinkageError, clazz, name_string, desc_string);
      throwException(thread, clazzLinkageError, "Uh oh: failed to build method spec using declaring_clazz %k, name %w, desc %w.\n",clazz, name_string, desc_string);
    }
    else {
      wabort(ABORT_WONKA,"Uh oh: failed to build method spec using declaring_clazz %k, name %w, desc %w.\n",clazz, name_string, desc_string);
    }

    return;

  }

  woempa(1, "Seek %w%w in %K\n", name_string, desc_string, clazz);
  for (i = 0; i < clazz->numDeclaredMethods; ++i) {
    w_method m = &clazz->own_methods[i];

    woempa(1, "Candidate: %m\n", m);
    if (m->spec.name == name_string && m->spec.desc == desc_string && methodMatchesSpec(m, spec)) {
      method = m;
      break;
    }
  }

  if (!method) {
    wabort(ABORT_WONKA,"Uh oh: class %k doesn't have a %w%w method.\n",clazz, name_string, desc_string);
  }

  releaseMethodSpec(spec);
  releaseMem(spec);
  deregisterString(name_string);
  deregisterString(desc_string);

  /*
  ** This is not really true but we know better than the compiler in this case...
  */
  method->exec.function.word_fun = (w_word_fun)fp;
}

void registerNatives(w_clazz clazz, const JNINativeMethod *methods, w_int mcount) {
  w_int i;
  w_thread thread = currentWonkaThread;

  for (i = 0; i < mcount; i++) {
    registerNativeMethod(clazz, methods[i].fnPtr, methods[i].name, methods[i].signature);
    if (exceptionThrown(thread)) {
      break;
    }
  }
}

char * print_method_short(char * buffer, int * remain, void * data, int w, int p, unsigned int f) {

  w_method method = data;
  w_int    nbytes;
  char    *temp;
  w_int    i;

  if (*remain < 1) {

    return buffer;

  }

  temp = buffer;

  if (method == NULL) {
    strncpy(temp, (char *)"<NULL>", *remain);
    if (*remain < 6) {
      temp += *remain;
      *remain = 0;
    }
    else {
      temp += 6;
      *remain -= 6;
    }

    return temp;

  }

  nbytes = x_snprintf(temp, *remain, "%w", method->spec.name);
  *remain -= nbytes;
  temp += nbytes;

  if (method->spec.return_type) {
    nbytes = x_snprintf(temp, *remain, "(");
    *remain -= nbytes;
    temp += nbytes;
    if (method->spec.arg_types) {
      for (i = 0; method->spec.arg_types[i]; ++i) {
        nbytes = x_snprintf(temp, *remain, "%y", method->spec.arg_types[i]);
        *remain -= nbytes;
        temp += nbytes;
      }
    }
    nbytes = x_snprintf(temp, *remain, ")%y", method->spec.return_type);
  }
  else if (method->spec.desc) {
    nbytes = x_snprintf(temp, *remain, "%w", method->spec.desc);
  }
  else {
    nbytes = x_snprintf(temp, *remain, "(?unknown?)?unknown?");
  }
  *remain -= nbytes;

  return temp + nbytes;
}

char * print_method_long(char * buffer, int * remain, void * data, int w, int p, unsigned int f) {

  w_method method = data;
  w_int    nbytes;
  char    *temp;
  w_int    i;

  if (*remain < 1) {

    return buffer;

  }

  temp = buffer;

  if (method == NULL) {
    strncpy(temp, (char *)"<NULL>", *remain);
    if (*remain < 6) {
      temp += *remain;
      *remain = 0;
    }
    else {
      temp += 6;
      *remain -= 6;
    }

    return temp;

  }
  
  if (isSet(method->flags, ACC_PUBLIC)) {
    nbytes = x_snprintf(temp, *remain, "public ");
    *remain -= nbytes;
    temp += nbytes;
  }
  else if (isSet(method->flags, ACC_PROTECTED)) {
    nbytes = x_snprintf(temp, *remain, "protected ");
    *remain -= nbytes;
    temp += nbytes;
  }
  else if (isSet(method->flags, ACC_PRIVATE)) {
    nbytes = x_snprintf(temp, *remain, "private ");
    *remain -= nbytes;
    temp += nbytes;
  }

  if (isSet(method->flags, ACC_ABSTRACT)) {
    nbytes = x_snprintf(temp, *remain, "abstract ");
    *remain -= nbytes;
    temp += nbytes;
  }

  if (isSet(method->flags, ACC_STATIC)) {
    nbytes = x_snprintf(temp, *remain, "static ");
    *remain -= nbytes;
    temp += nbytes;
  }

  if (isSet(method->flags, ACC_FINAL)) {
    nbytes = x_snprintf(temp, *remain, "final ");
    *remain -= nbytes;
    temp += nbytes;
  }

  if (isSet(method->flags, ACC_SYNCHRONIZED)) {
    nbytes = x_snprintf(temp, *remain, "synchronized ");
    *remain -= nbytes;
    temp += nbytes;
  }

  if (isSet(method->flags, ACC_NATIVE)) {
    nbytes = x_snprintf(temp, *remain, "native ");
    *remain -= nbytes;
    temp += nbytes;
  }

  nbytes = x_snprintf(temp, *remain, "%w", method->spec.name);
  *remain -= nbytes;
  temp += nbytes;

  if (method->spec.return_type) {
    nbytes = x_snprintf(temp, *remain, "(");
    *remain -= nbytes;
    temp += nbytes;
    if (method->spec.arg_types) {
      for (i = 0; method->spec.arg_types[i]; ++i) {
        nbytes = x_snprintf(temp, *remain, "%y", method->spec.arg_types[i]);
        *remain -= nbytes;
        temp += nbytes;
      }
    }
    nbytes = x_snprintf(temp, *remain, ")%y", method->spec.return_type);
  }
  else if (method->spec.desc) {
    nbytes = x_snprintf(temp, *remain, "%w", method->spec.desc);
  }
  else {
    nbytes = x_snprintf(temp, *remain, "(?)?");
  }
  *remain -= nbytes;
  temp += nbytes;

  nbytes = x_snprintf(temp, *remain, " of %K", method->spec.declaring_clazz);
  *remain -= nbytes;

  return temp + nbytes;
}

#if defined (DEBUG)

void methodTableDump(w_clazz clazz) {

  w_size i;
  w_method method;

  if (!clazz->vmlt) {
    woempa(9, "=== %K has no vmlt, nothing to dump ===", clazz);

    return;

  }

  woempa(9,"=== Method Table Dump for %k ===\n",clazz);
  woempa(9,"  %d virtual methods, of which %d inheritable.\n", clazz->numInheritableMethods,clazz->numInheritableMethods);
  i=0;
  woempa(9,"Inheritable methods:\n");
  while (i < clazz->numInheritableMethods) {
    method = clazz->vmlt[i]; 
    woempa(9,"slot %3d %m : from class %k\n",method->slot, method, method->spec.declaring_clazz);
    ++i;
  }
  woempa(9,"Non-inheritable methods:\n");
  while (i < clazz->numInheritableMethods) {
    method = clazz->vmlt[i]; 
    woempa(9,"slot %3d %m : from class %k\n",method->slot, method, method->spec.declaring_clazz);
//    woempa(9,"slot %3d %w\n",method->slot, NM(method));
//    woempa(9,"     from class \"%k\"\n", method->spec.declaring_clazz);
    ++i;
  }
  woempa(9,"=== End Method Table Dump for %k ===\n",clazz);

}

#endif /* DEBUG */

w_method interfaceLookup(w_method imethod, w_clazz clazz) {
  w_method result;
  w_size  i;

  threadMustBeSafe(currentWonkaThread);

#ifdef RUNTIME_CHECKS
  if (isNotSet(imethod->flags, METHOD_IS_INTERFACE)) {
    wabort(ABORT_WONKA, "Method %M is not an interface method, must use virtualLookup instead\n", imethod);
  }
#endif

  result = (w_method)ht2k_read(interface_hashtable, (w_word)clazz, (w_word)imethod);

  if (!result) {
    woempa(1, "Searching %K for method %w %w\n", clazz, imethod->spec.name, imethod->spec.desc);

    for (i = 0; i < clazz->numDeclaredMethods; ++i) {
      w_method m = &clazz->own_methods[i];

      if (m->spec.name == imethod->spec.name && m->spec.desc == imethod->spec.desc) {
        woempa(1, "Candidate is %M\n", m);
        result = m;
        break;
      }
    }

    if (!result && clazz->numSuperClasses) {
      for (i = 0; i < clazz->supers[0]->numInheritableMethods; ++i) {
        w_method m = clazz->supers[0]->vmlt[i];

        if (m->spec.name == imethod->spec.name && m->spec.desc == imethod->spec.desc) {
          woempa(1, "Candidate is %M\n", m);
          result = m;
          break;
        }
      }
    }

    if (result) {
      x_monitor monitor = result->spec.declaring_clazz->resolution_monitor;
      x_monitor_eternal(monitor);
      if (mustBeLoaded(&result->spec.return_type) == CLASS_LOADING_FAILED) {
        woempa(9, "Failed to load return type %K of %M\n", result->spec.return_type, result);
        result = NULL;
      }
      else if (mustBeLoaded(&imethod->spec.return_type) == CLASS_LOADING_FAILED) {
        woempa(9, "Failed to load return type %K of %M\n", imethod->spec.return_type, imethod);
        result = NULL;
      }
      else if (result->spec.return_type != imethod->spec.return_type) {
        woempa(9, "Return type %K of %M differs from %K of %M\n", result->spec.return_type, result, imethod->spec.return_type, imethod);
        result = NULL;
      }
      else if (result->spec.arg_types) {
        for (i = 0; result->spec.arg_types[i]; ++i) {
          if (mustBeLoaded(&result->spec.arg_types[i]) == CLASS_LOADING_FAILED) {
            woempa(9, "Failed to load arg[%d] type %K of %M\n", i, result->spec.return_type, result);
            result = NULL;
            break;
          }
          else if (mustBeLoaded(&imethod->spec.arg_types[i]) == CLASS_LOADING_FAILED) {
            woempa(9, "Failed to load arg[%d] type %K of %M\n", i, imethod->spec.return_type, imethod);
            result = NULL;
            break;
          }
          else if (result->spec.arg_types[i] != imethod->spec.arg_types[i]) {
            woempa(9, "Arg[%d] type %K of %M differs from %K of %M\n", i, result->spec.arg_types[i], result, imethod->spec.arg_types[i], imethod);
            result = NULL;
            break;
          }
        }
      }
      x_monitor_exit(monitor);
    }

    if (result) {
      ht2k_write(interface_hashtable, (w_word)clazz, (w_word)imethod, (w_word)result);
    }
  }

  return result;
}

/*
** Convenience function to find a method in its declaring clazz without using JNI
*/

w_method find_method(w_clazz clazz, const char* method_name, const char* method_descriptor) {
  w_MethodSpec *spec;
  w_string method_name_string = utf2String(method_name, strlen(method_name));
  w_string method_desc_string = utf2String(method_descriptor, strlen(method_descriptor));
  if (createMethodSpecUsingDescriptor(clazzThreadGroup, method_name_string, method_desc_string, &spec) == CLASS_LOADING_FAILED) {
    wabort(ABORT_WONKA,"Uh oh: failed to build method spec using declaring_clazz %k, name %w, desc %w.\n",clazzThreadGroup, method_name_string, method_desc_string);
  }

  w_method the_method = NULL;
  for (int i = 0; i < clazz->numDeclaredMethods; ++i) {
    w_method candidate = &clazz->own_methods[i];
    woempa(1, "Checking %M\n", candidate);

    if (candidate->spec.name == method_name_string && candidate->spec.desc == method_desc_string) {
      the_method = candidate;
      woempa(7, "Found %k.%s%s at %p\n", clazz, method_name, method_descriptor);
      break;
    }
  }

  releaseMethodSpec(spec);
  releaseMem(spec);
  deregisterString(method_name_string);
  deregisterString(method_desc_string);

  return the_method;
}

