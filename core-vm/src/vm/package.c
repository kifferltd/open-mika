/**************************************************************************
* Parts Copyright (C) 2008 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#include "clazz.h"
#include "loading.h"
#include "package.h"
#include "wstrings.h"

w_package createPackage(w_string name, w_instance loader) {
  w_package p = allocClearedMem(sizeof(w_Package));
  p->name = registerString(name);
  p->label = "package";
  p->loader = loader;

  return p;
}

void destroyPackage(w_package p) {
  deregisterString(p->name);
  releaseMem(p);
}

/**
 ** Get the w_Package for a class, creating it if it does not already exist.
 */
w_package getPackageForClazz(w_clazz clazz, w_instance loader) {
  w_string class_name = clazz->dotified;
  w_instance effective_loader = loader ? loader : systemClassLoader;
  w_hashtable ht = loader2packages(effective_loader);
  w_string package_name;
  w_int i;
  w_int j;
  w_package p;

  for (i = 0; string_char(class_name, i) == '['; ++i); 
  for (j = string_length(class_name) - 1; string_char(class_name, j) != '.'; --j); 
  package_name = j > i ? w_substring(class_name, i, j - i) : registerString(string_empty);
  ht_lock(ht);
  p = (w_package)ht_read_no_lock(ht, (w_word)package_name);
  if (!p) {
    p = createPackage(package_name, effective_loader);
    ht_write_no_lock(ht, (w_word)package_name, (w_word)p);
  }
  clazz->package = p;
  ht_unlock(ht);
  deregisterString(package_name);

  return p;
}

static void package_iterator(w_word key, w_word value) {
  w_string name = (w_string) key;
  w_package package = (w_package)value;
  if (!package->loader) {
    package->loader = systemClassLoader;
  }
}

/*
** Fix system_package_hashtable so that every package which currently
** has its loader set to null points to systemClassLoader instead.
*/
void patchPackages() {
  ht_every(system_package_hashtable, package_iterator);
}
 
