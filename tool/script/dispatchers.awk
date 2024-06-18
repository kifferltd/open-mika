###########################################################################
# Copyright (c) 2024 by Chris Gray, theGrayZone CommV.                    #
# All rights reserved.                                                    #
#                                                                         #
# Redistribution and use in source and binary forms, with or without      #
# modification, are permitted provided that the following conditions      #
# are met:                                                                #
# 1. Redistributions of source code must retain the above copyright       #
#    notice, this list of conditions and the following disclaimer.        #
# 2. Redistributions in binary form must reproduce the above copyright    #
#    notice, this list of conditions and the following disclaimer in the  #
#    documentation and/or other materials provided with the distribution. #
# 3. Neither the name of Chris Gray nor the names of other contributors   #
#    may be used to endorse or promote products derived from this         #
#    software without specific prior written permission.                  #
#                                                                         #
# THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          #
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    #
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    #
# IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    #
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      #
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       #
# GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           #
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    #
# IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         #
# OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  #
# ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              #
###########################################################################

function descr2id(descr) {
# skip the opening '('
  cursor = 2
  rparen = index(descr,")")
  id = ""
  while(cursor<rparen) {
    letter = substr(descr,cursor,1)
    if(d2id[letter]) {
      id = sprintf("%s%s",id,d2id[letter])
    }
    else {
      print "unexpected letter", letter
    }
    while(letter=="[") letter = substr(descr,++cursor,1)
    if(letter=="L") {
      skip = index(substr(descr,cursor),";")-1
      cursor += skip
    }
    cursor++
  }

  letter=substr(descr,++cursor,1)
  if(d2id[letter]) {
    id = sprintf("%s_%s",id,d2id[letter])
  }
  else {
      print "unexpected letter", letter
  }
  
  return id
}

function count64bitparams(id) {
  k = 0
  np = length(id)-2
  
  c = index(substr(id,1,np),"d")
  while (c) {
    k += 1
    c = index(substr(id,c+1,np-c),"d")
  }
  c = index(substr(id,1,np),"j")
  while (c) {
    k += 1
    c = index(substr(id,c+1,np-c),"j")
  }
  return k
}

function id2plist(id) {
  cursor = 1
  nparams = length(id)-2
  depth = length(id)-2
  depth += count64bitparams(id)

  plist = sprintf("thread, target")
  while(cursor<=nparams){
    letter=substr(id,cursor++,1)
    switch (letter) {
      case "a":
      case "b":
      case "c":
      case "f":
      case "i":
      case "l":
      case "s":
      case "z":
# take 1 word from stack
        plist = sprintf("%s, (%s)GET_SLOT_CONTENTS(top - %d)",plist,id2type[letter],depth--)
        break
      case "d":
      case "j":
# take two words from stack
        plist = sprintf("%s, slots2%s(top-%d)",plist,id2type[letter],depth)
        depth-=2
        break
      default:
        print "unexpected letter", letter
        depth--
    }
  }
  return plist
}

function id2splist(id) {
  cursor=1
  nparams = length(id)-2
  depth = length(id)-2
  depth += count64bitparams(id)

  plist = "thread, target"
  while(cursor<=nparams){
    letter=substr(id,cursor++,1)
    switch (letter) {
      case "a":
      case "b":
      case "c":
      case "f":
      case "i":
      case "l":
      case "s":
      case "z":
# take 1 word from stack
        plist = sprintf("%s, (%s)GET_SLOT_CONTENTS(top - %d)",plist,id2type[letter],depth--)
        break
      case "d":
      case "j":
# take two words from stack
        plist = sprintf("%s, slots2%s(top-%d)",plist,id2type[letter],depth)
        depth-=2
        break
      default:
        print "unexpected letter", letter
        depth--
    }
  }
  return plist
}

function id2proto(id) {
  cursor=1
  uscore=index(id,"_")
  proto = ""
  while(cursor<uscore) {
    letter=substr(id,cursor,1)
    if(id2type[letter]) {
      proto = sprintf("%s, %s",proto,id2type[letter])
    }
    else {
      print "unexpected letter", letter
    }
    cursor++
  }
  return proto
}

function id2rtype(id) {
  letter=substr(id,length(id),1)
  if(id2type[letter]) {
    rtype = id2type[letter]
  }
  else {
    print "unexpected letter", letter
  }

  return rtype
}

BEGIN {
  d2id["B"] = "b"
  d2id["C"] = "c"
  d2id["D"] = "d"
  d2id["F"] = "f"
  d2id["I"] = "i"
  d2id["J"] = "j"
  d2id["L"] = "l"
  d2id["S"] = "s"
  d2id["Z"] = "z"
  d2id["["] = "a"
  d2id["V"] = "v"

  id2type["b"] = "w_byte"
  id2type["c"] = "w_char"
  id2type["d"] = "w_double"
  id2type["f"] = "w_float"
  id2type["i"] = "w_int"
  id2type["j"] = "w_long"
  id2type["l"] = "w_instance"
  id2type["s"] = "w_short"
  id2type["z"] = "w_boolean"
  id2type["a"] = "w_instance"
  id2type["v"] = "void"

  gendir = ENVIRON["gendir"]
  dispdir = gendir "/dispatchers"
  lscmd = "ls " dispdir "/idmap"
  while ((lscmd | getline descr) > 0) {
    idfile = dispdir "/idmap/" descr
    printf "// idfile = %s\n", idfile
    getline id < idfile
    close(idfile)

    gsub(/\./,"/",descr)
    printf "// descriptor %s -> id '%s'\n", descr, id
    idmap[descr] = id

    if(!protos[id]) {
      plists[id]=id2plist(id)
      printf "//   id %s -> plist '%s'\n", id, plists[id]
      rtypes[id]=id2rtype(id)
      printf "//   id %s -> rtype '%s'\n", id, rtypes[id]
      protos[id]=id2proto(id)
      printf "//   id %s -> proto '%s'\n", id, protos[id]
    }
  }
  close(lscmd)
  
  print " "
  print "/*"
  print "** This file is generated automatically from core-classes.in"
  print "** Do NOT edit this file."
  print "*/"
  print " "
  print "#include \"wstrings.h\""
  print "#include \"clazz.h\""
  print "#include \"fields.h\""
  print "#include \"hashtable.h\""
  print "#include \"jni.h\""
  print "#include \"locks.h\""
  print "#include \"methods.h\""
  print "#include \"ts-mem.h\""
  print "#include \"mika_threads.h\""
  print "#include \"loading.h\""
  print "#include \"core-classes.h\""
  print " "
  printf "/* code to return a void result */\n"
  printf ""

  printf "static void prepareNativeFrame(w_frame frame, w_thread thread, w_frame caller, w_method method) {\n"
  print "  frame->flags = FRAME_NATIVE;\n  frame->label = \"frame\";\n  frame->previous = caller;\n  frame->thread = thread; frame->method = method;\n"
  print "  frame->jstack_top = frame->jstack_base;\n  SET_SLOT_SCANNING(frame->jstack_base, stack_notrace);\n "
  printf "  frame->auxstack_base = caller->auxstack_top;\n  frame->auxstack_top = caller->auxstack_top;\n\n"
  printf "#ifdef TRACE_CLASSLOADERS\n"
  printf "  {\n    w_instance loader = isSet(method->flags, ACC_STATIC)\n        ? method->spec.declaring_clazz->loader\n        : instance2clazz((w_instance) GET_SLOT_CONTENTS(frame->jstack_base - method->exec.arg_i))->loader;\n"
  printf "    if (loader && !getBooleanField(loader, F_ClassLoader_systemDefined)) {\n      frame->udcl = loader;\n    }\n"
  printf "    else {\n      frame->udcl = caller->udcl;\n    }\n  }\n"
  printf "#endif\n"
  printf "}\n\n"

  printf "static void return_void(w_frame caller, w_int depth);\n\n"
  printf "static void return_void(w_frame caller, w_int depth) {\n"
  printf "  caller->jstack_top += -depth;\n"
  printf "}\n\n"

  printf "/* code to return a reference result */\n"
  printf ""

  printf "static void return_reference(w_frame caller, w_int depth, w_word result);\n\n"
  printf "static void return_reference(w_frame caller, w_int depth, w_word result) {\n"
  printf "  SET_REFERENCE_SLOT(caller->jstack_top - depth, result);\n"
  printf "  caller->jstack_top += -depth + 1;\n"
  printf "}\n\n"

  printf "/* code to return a non-reference, single-slot result */\n"
  printf ""

  printf "static void return_oneslot(w_frame caller, w_int depth, w_word result);\n\n"
  printf "static void return_oneslot(w_frame caller, w_int depth, w_word result) {\n"
  printf "  SET_SCALAR_SLOT(caller->jstack_top - depth, result);\n"
  printf "  caller->jstack_top += -depth + 1;\n"
  printf "}\n\n"

  printf "/* code to return a w_double result */\n"
  printf ""

  printf "static void return_w_long(w_frame caller, w_int depth, w_long result);\n\n"
  printf "static void return_w_long(w_frame caller, w_int depth, w_long result) {\n"
  printf "  w_long2slots(result, caller->jstack_top-depth);\n"
  printf "  caller->jstack_top += -depth + 2;\n"
  printf "}\n\n"

  printf "/* code to return a w_double result */\n"
  printf ""

  printf "static void return_w_double(w_frame caller, w_int depth, w_double result);\n\n"
  printf "static void return_w_double(w_frame caller, w_int depth, w_double result) {\n"
  printf "  w_long2slots(result, caller->jstack_top-depth);\n"
  printf "  caller->jstack_top += -depth + 2;\n"
  printf "}\n\n"

  for (id in protos) {
    printf "void native_dispatcher_%s(w_frame caller, w_method method);\n\n", id
    printf "void native_dispatcher_%s(w_frame caller, w_method method) {\n", id
    printf "  volatile w_thread thread = caller->thread;\n"
    printf "  w_Frame theFrame;\n"
    printf "  w_frame frame = &theFrame;\n"
    printf "  w_int depth = method->exec.arg_i;\n"
    printf "  w_instance target = isSet(method->flags, ACC_STATIC) ? clazz2Class(method->spec.declaring_clazz) : (w_instance) GET_SLOT_CONTENTS(caller->jstack_top - depth);\n"
    printf "  x_monitor m = isSet(method->flags, ACC_SYNCHRONIZED) ? getMonitor(target) : NULL;\n"
    nonvoid = rtypes[id] != "void"
    reference = rtypes[id] == "w_instance"
    twoslots = rtypes[id] == "w_long" || rtypes[id] == "w_double"
    if (nonvoid) printf "  %s result;\n\n",id2rtype(id)
    printf "  woempa(1, \"Calling %%M using native_dispatcher_%s\\n\", method);\n", id
    printf "  frame->jstack_base = caller->jstack_top;\n"
    printf "  prepareNativeFrame(frame, thread, caller, method);\n\n"
    printf "  threadMustBeSafe(thread);\n\n"
    printf "  if (m) {\n"
    printf "    x_monitor_eternal(m);\n"
    printf "  }\n\n"
    printf "  thread->top = frame;\n\n"
    printf "  w_slot top = caller->jstack_top;\n"

    printf "  typedef %s (_fun_%s) (w_thread, w_instance%s);\n",rtypes[id],id,protos[id]
    printf "  _fun_%s *_f%s = (_fun_%s*)method->exec.function.%s_fun;\n  ",id,id,id,nonvoid ? (reference ? "ref" : twoslots ? "long" : "word") : "void"
    if (nonvoid) printf "result = "
    printf "_f%s(%s);\n",id,plists[id]
    
    printf "  if (m) {\n"
    printf "    x_monitor_exit(m);\n"
    printf "  }\n\n";
    if (nonvoid) {
      printf "  if (thread->exception) {\n"
      printf "    thread->top = caller;\n"
      printf "  }\n"
      printf "  else {\n"
      if (reference) printf "    enterUnsafeRegion(thread);\n"
      if (twoslots) {
        printf "    woempa(1, \"%%m result = %%016x\\n\", method, result);\n"
        printf "    return_%s(caller, depth, result);\n", id2rtype(id)
      }
      else {
        printf "    woempa(1, \"%%m result = %%%s\\n\", method, result);\n", nonvoid ? "08x" : "p"
        printf "    return_%s(caller, depth, result);\n", reference ? "reference" : "oneslot"
      }
      if (reference) printf "    if (result) {\n      setFlag(instance2flags(result), O_BLACK);\n    }\n"
      printf "    thread->top = caller;\n"
      if (reference) printf "    enterSafeRegion(thread);\n"
      printf "  }\n"
    }
    else {
      printf "  return_void(caller, depth);\n"
      printf "  thread->top = caller;\n"
    }
    printf "}\n\n"

  }


  printf "static struct {\n"
  printf "  const char *descr;\n"
  printf "  w_callfun dispatcher;\n"
  printf "} native_dispatchers[] = {\n"
  for (descr in idmap) {
    printf "  { \"%s\", native_dispatcher_%s", descr, idmap[descr]
    printf " },\n"
  }
  printf "  { NULL, NULL }\n};\n\n"

  printf "void collectDispatchers(w_hashtable hashtable) {\n"
  printf "  w_string descr_string;\n\n"
  printf "  for (int i = 0; native_dispatchers[i].descr; ++i) {\n"
  printf "    descr_string = ascii2String(native_dispatchers[i].descr, strlen(native_dispatchers[i].descr));\n"
  printf "    woempa(1, \"adding  dispatchers for descriptor %%w\\n\", descr_string);\n"
  printf "    ht_write_no_lock(hashtable, (w_word)descr_string, (w_word)native_dispatchers[i].dispatcher);\n"
  printf "    woempa(1, \"added (%%w, 0x%%08x) to descriptors hashtable, now holds %%d items\\n\", descr_string, (w_word)native_dispatchers[i].dispatcher, hashtable->occupancy);\n"
  printf "  }\n"
  printf "}\n\n"
}
