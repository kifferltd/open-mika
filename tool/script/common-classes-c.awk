
BEGIN {
  if(JNI=="true") {
  }
  else {
  } 

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
}

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
    prinf "// found a 'd' in " id ", incrementing k"
    k += 1
    c = index(substr(id,c+1,np-c),"d")
  }
  c = index(substr(id,1,np),"j")
  while (c) {
    prinf "// found a 'j' in " id ", incrementing k"
    k += 1
    c = index(substr(id,c+1,np-c),"j")
  }
  print "// total of " k " 64-bit params in " id
  return k
}

function id2iplist(id) {
  nparams = length(id)-2
  depth = length(id)-1
  depth += count64bitparams(id)

  cursor = 1
  plist = sprintf("thread, (w_instance) top[-%d].c", depth--)
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
        plist = sprintf("%s, (%s)top[-%d].c",plist,id2type[letter],depth--)
        break
      case "d":
      case "j":
# take two words from stack
        plist = sprintf("%s, slots2%s(top[-%d],top[-%d])",plist,id2type[letter],depth, depth-1)
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
        plist = sprintf("%s, (%s)top[-%d].c",plist,id2type[letter],depth--)
        break
      case "d":
      case "j":
# take two words from stack
        plist = sprintf("%s, slots2%s(top[-%d],top[-%d])",plist,id2type[letter],depth, depth-1)
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

# skip lines beginning with '#'

/^#/{next}

# No leading whitespace -> clazz declaration
# two or more fields -> field or method

/^[a-zA-Z]/ {
  fqn=$1
  slash=index(fqn,"/")
  lastslash=0
  while(slash) { lastslash+=slash; rest=substr(fqn,lastslash+1); slash=index(rest,"/") }
  thisclazz=substr(fqn,lastslash+1)
  gsub("\\$", "_dollar_", thisclazz)
  clazz[fqn]=thisclazz
  path[fqn]=substr(fqn,1,lastslash)

  l=length(thisclazz)
  if(l>4&&(l-index(thisclazz,"Error")==4)) likelyerrors=thisclazz" "likelyerrors
  if(l>8&&(l-index(thisclazz,"Exception")==8)) likelyexceptions=thisclazz" "likelyexceptions
}

/^[ \t]/ {

# If third field is "+" then second is a "wotsit" field.
# If third field begins with "(" then second was a method name
# and third is its descriptor; if present, the fourth is the native 
# function entry point.
# If the third field is neither "+" nor begins with "(" then the
# second was a field.

  thismember = $1
  gsub("\\$", "_dollar_", thismember)
  if($2=="+") {
    fictitiousfields[thisclazz] = thismember" "fictitiousfields[thisclazz]
    wotsits[thisclazz] = thismember" "wotsits[thisclazz]
  }
  else if(substr($2,1,1)!="(") {
    realfields[thisclazz,$1] = "F_"thisclazz"_"thismember
  }
  else {
    method=thismember
    descr=$2
    if($3) methods[thisclazz,method,descr] = $3
    id=descr2id(descr)
    idmap[descr] = id
    if(!protos[id]) {
      static_plists[id]=id2splist(id)
      instance_plists[id]=id2iplist(id)
      rtypes[id]=id2rtype(id)
      protos[id]=id2proto(id)
    }
  }
}

END {

  print " "
  for (c in clazz) {
    printf "w_clazz clazz%s;\n",clazz[c]
  }
  print  " "

  print "/*"
  print "** slot numbers of Java fields"
  print "*/"
  print " "

  for(cf in realfields) {
    printf "w_int %s;\n",realfields[cf]
  }
  print " "

  print "/*"
  print "** slot numbers of \"fictitious\" fields (wotsits and their ilk)"
  print "*/"
  print " "
  for(c in fictitiousfields) {
    split(fictitiousfields[c], a);
    for (i in a) {
      printf "w_int F_%s_%s;\n",c,a[i]
    }
  }
  print " "

  printf "/*\n** Wotsit functions for %s-classes\n*/\n\n", module
  for(c in clazz) {
    thisclazz = clazz[c];
    mcount = 0;
    for (m in methods) {
      split(m, a, SUBSEP)
      if(a[1] == thisclazz) {
        if (!mcount) {
          printf "JNINativeMethod %s_methods[] = {\n", thisclazz
        }
        printf "  {\"%s\", \"%s\", %s},\n", a[2], a[3], methods[m]
        ++mcount;
      }
    }
    if (mcount) {
      printf("};\n\n");
    }
    split(wotsits[thisclazz], ff);
    have_fixup1 = 0;
    have_fixup2 = 0;
    for (cf in realfields) {
      split(cf, a, SUBSEP)
      if(a[1] == thisclazz) {
        if (!have_fixup1) {
          have_fixup1 = 1;
          printf "static void fixup1_%s(w_clazz clazz) {\n\n", thisclazz
        }
        printf "  %s = findFieldOffset(clazz, \"%s\");\n",realfields[cf],a[2]
      }        
    }
    j = 0;
    for(i in ff) {
      j++;
    }
    if (j) {
      if (!have_fixup1) {
        have_fixup1 = 1;
        printf "static void fixup1_%s(w_clazz clazz) {\n\n", thisclazz
      }
      for(i in ff) {
        printf "  woempa(1, \"clazz '%s' wotsit field '%s' at index %%d.\\n\", clazz->instanceSize - clazz->numReferenceFields);\n", thisclazz, ff[i]
        printf "#ifdef PACK_BYTE_FIELDS\n"
        printf "  F_%s_%s = FIELD_SIZE_32_BITS + (clazz->instanceSize - clazz->numReferenceFields);\n", thisclazz, ff[i]
        printf "  if (clazz->nextByteSlot == (clazz->instanceSize * 4)) {\n"
        printf "    clazz->instanceSize += 4;\n"
        printf "  }\n"
        printf "#else\n"
        printf "  F_%s_%s = clazz->instanceSize - clazz->numReferenceFields;\n", thisclazz, ff[i]
        printf "#endif\n"
        printf "  clazz->instanceSize += 1;\n"
      }
    }
    if (have_fixup1) {
      clazz_fixup1[c] = "fixup1_" thisclazz;
      printf "}\n\n"
    }
    if (mcount) {
      printf "static void fixup2_%s(w_clazz clazz) {\n\n", thisclazz
      printf "  registerNatives(clazz, %s_methods, %d);\n", thisclazz, mcount
      clazz_fixup2[c] = "fixup2_" thisclazz;
      printf "}\n\n"
    }
  }

  printf "static void prepareNativeFrame(w_frame frame, w_thread thread, w_frame caller, w_method method) {\n"
  print "  frame->flags = FRAME_NATIVE;\n  frame->label = \"frame\";\n  frame->previous = caller;\n  frame->thread = thread; frame->method = method;\n"
  print "  frame->jstack_top = frame->jstack_base;\n  frame->jstack_base[0].s = stack_notrace;\n "
  printf "  frame->auxstack_base = caller->auxstack_top;\n  frame->auxstack_top = caller->auxstack_top;\n\n"
  printf "#ifdef TRACE_CLASSLOADERS\n"
  printf "  {\n    w_instance loader = isSet(method->flags, ACC_STATIC)\n        ? method->spec.declaring_clazz->loader\n        : instance2clazz((w_instance)frame->jstack_base[- method->exec.arg_i].c)->loader;\n"
  printf "    if (loader && !getBooleanField(loader, F_ClassLoader_systemDefined)) {\n      frame->udcl = loader;\n    }\n"
  printf "    else {\n      frame->udcl = caller->udcl;\n    }\n  }\n"
  printf "#endif\n"
  printf "}\n\n"

  printf "static w_double slots2w_double(w_Slot s1, w_Slot s2) {\n"
  printf "  union{w_double d; w_word w[2];} two_words;\n"
  printf "  two_words.w[0] = s1.c;\n"
  printf "  two_words.w[1] = s2.c;\n"
  printf "  return two_words.d;\n"
  printf "}\n\n"

  printf "static w_long slots2w_long(w_Slot s1, w_Slot s2) {\n"
  printf "  union{w_long j; w_word w[2];} two_words;\n"
  printf "  two_words.w[0] = s1.c;\n"
  printf "  two_words.w[1] = s2.c;\n"
  printf "  return two_words.j;\n"
  printf "}\n\n"

  printf "/* dispatchers */\n"
  printf ""

  for (id in protos) {
    printf "void native_dispatcher_%s(w_frame caller, w_method method);\n\n", id
    printf "void native_dispatcher_%s(w_frame caller, w_method method) {\n", id
    printf "  volatile w_thread thread = caller->thread;\n"
    printf "  w_Frame theFrame;\n"
    printf "  w_frame frame = &theFrame;\n"
    printf "  w_int idx = - method->exec.arg_i;\n"
    printf "  w_instance target = isSet(method->flags, ACC_STATIC) ? clazz2Class(method->spec.declaring_clazz) : (w_instance) caller->jstack_top[idx].c;\n"
    printf "  x_monitor m = isSet(method->flags, ACC_SYNCHRONIZED) ? getMonitor(target) : NULL;\n"
    nonvoid = rtypes[id] != "void"
    reference = rtypes[id] == "w_instance"
    twoslots = rtypes[id] == "w_long" || rtypes[id] == "w_double"
    if (nonvoid) printf "  %s result;\n\n",id2rtype(id)
    printf "  woempa(7, \"Calling %%M\\n\", method);\n"
    printf "  frame->jstack_base = caller->jstack_top;\n"
    printf "  prepareNativeFrame(frame, thread, caller, method);\n\n"
    printf "  threadMustBeSafe(thread);\n\n"
    printf "  if (m) {\n"
    printf "    x_monitor_eternal(m);\n"
    printf "  }\n\n"
    printf "  thread->top = frame;\n\n"
# this looks wrong to me
#    printf "  frame->jstack_top[0].c = 0;\n"
#    printf "  frame->jstack_top[0].s = stack_%strace;\n", reference ? "" : "no"
#    printf "  frame->jstack_top += 1;\n"
    printf "  w_slot top = caller->jstack_top;\n"

# TODO can we not unify the static and instance paths here?
    printf "  if (isSet(method->flags, ACC_STATIC)) {\n"
    printf "    typedef %s (sfun_%s) (w_thread, w_instance%s);\n",rtypes[id],id,protos[id]
    printf "    sfun_%s *sf%s = (sfun_%s*)method->exec.function.%s_fun;\n    ",id,id,id,nonvoid ? (reference ? "ref" : twoslots ? "long" : "word") : "void"
    if (nonvoid) printf "result ="
    printf "sf%s(%s);\n",id,static_plists[id]
    printf "  }\n"
    printf "  else {\n"
    printf "    typedef %s (ifun_%s) (w_thread, w_instance%s);\n",rtypes[id],id,protos[id]
    printf "    ifun_%s *if%s = (ifun_%s*)method->exec.function.%s_fun;\n    ",id,id,id,nonvoid ? (reference ? "ref" : twoslots ? "long" : "word") : "void"
    if (nonvoid) printf "result ="
    printf "if%s(%s);\n",id,instance_plists[id]
    printf "  }\n"
    
    printf "  if (m) {\n"
    printf "    x_monitor_exit(m);\n"
    printf "  }\n\n";
    if (nonvoid) {
      printf "  if (thread->exception) {\n"
# this looks wrong to me
#      printf "    woempa(7, \"%%m threw %%e, ignoring return value\\n\", method, thread->exception);\n"
#      printf "    caller->jstack_top[idx].s = stack_notrace;\n"
#      printf "    caller->jstack_top += idx + 1;\n"
      printf "    thread->top = caller;\n"
      printf "  }\n"
      printf "  else {\n"
      if (reference) printf "    enterUnsafeRegion(thread);\n"
      if (twoslots) {
        printf "    woempa(7, \"%%m result = %%16x\\n\", method, result);\n"
        printf "    union{%s l; w_word w[2];} two_words;\n", id2rtype(id)
        ptintf "    two_words.l = result;\n"
        printf "    caller->jstack_top[idx].c = two_words.w[0];\n"
        printf "    caller->jstack_top[idx].s = stack_notrace;\n"
        printf "    caller->jstack_top += 1;\n"
        printf "    caller->jstack_top[idx + 1].c = two_words.w[1];\n"
        printf "    caller->jstack_top[idx + 1].s = stack_notrace;\n"
        printf "    caller->jstack_top += idx + 2;\n"
      }
      else {
        printf "    woempa(7, \"%%m result = %%08x\\n\", method, result);\n"
        printf "    caller->jstack_top[idx].c = (w_word)result;\n"
        printf "    caller->jstack_top[idx].s = stack_%strace;\n", reference ? "" : "no"
        printf "    caller->jstack_top += idx + 1;\n"
      }
      if (reference) printf "    if (result) {\n      setFlag(instance2flags(result), O_BLACK);\n    }\n"
      printf "    thread->top = caller;\n"
      printf "    enterSafeRegion(thread);\n"
      printf "  }\n"
    }
    else {
      printf "  enterUnsafeRegion(thread);\n"
      printf "  caller->jstack_top += idx;\n"
      printf "  thread->top = caller;\n"
      printf "  enterSafeRegion(thread);\n"
    }
    printf "}\n\n"

  }


  printf "static struct {\n"
  printf "  const char *descr;\n"
  printf "  w_callfun dispatcher;\n"
  printf "} %s_native_dispatchers[] = {\n", Module
  for (descr in idmap) {
    printf "  { \"%s\", native_dispatcher_%s", descr, idmap[descr]
    printf " },\n"
  }
  printf "  { NULL, NULL, NULL }\n};\n\n"

  printf "void collect%sDispatchers(w_hashtable hashtable, w_hashtable instance_hashtable) {\n", Module
  printf "  w_string descr_string;\n\n"
  printf "  for (int i = 0; %s_native_dispatchers[i].descr; ++i) {\n", Module
  printf "    descr_string = cstring2String(%s_native_dispatchers[i].descr, strlen(%s_native_dispatchers[i].descr));\n", Module, Module
  printf "    woempa(1, \"adding  dispatchers for descriptor %%w\\n\", descr_string);\n"
  printf "    ht_write_no_lock(hashtable, (w_word)descr_string, (w_word)%s_native_dispatchers[i].dispatcher);\n", Module
  printf "    woempa(1, \"added (%%w, 0x%%08x) to descriptors hashtable, now holds %%d items\\n\", descr_string, (w_word)%s_native_dispatchers[i].dispatcher, hashtable->occupancy);\n", Module
  printf "  }\n"
  printf "}\n\n"

  print "static struct {"
  print "  w_clazz *clazzptr;"
  print "  const char *classname;"
  print "  w_fixup fixup1;"
  print "  w_fixup fixup2;"
  print "} classmap[] = {"
  ccount = 0
  for(c in clazz) {
    basename = clazz[c]
    thisclazz = basename
    gsub("_dollar_", "$", basename)
    fullname = sprintf("%s%s", path[c], basename)
    fu1 = clazz_fixup1[c];
    if (!fu1) fu1 = "NULL"
    fu2 = clazz_fixup2[c];
    if (!fu2) fu2 = "NULL"
    printf "  {&clazz%s, \"%s\", %s, %s},\n", thisclazz, fullname, fu1, fu2
    ++ccount
  }
  print "};"
  print ""

  printf "void collect%sFixups() {\n", Module
  print  "  const char *cname;"
  print  "  w_string slashed, dotified;"
  print  "  int i;"
  printf "  for (i = 0; i < %d; ++i) {\n", ccount
  print  "    w_fixup fu = classmap[i].fixup1;"
  print  "    if (fu) {"
  print  "      cname = classmap[i].classname;"
  print  "      slashed = cstring2String(cname, strlen(cname));"
  print  "      dotified = slashes2dots(slashed);"
  print  "      deregisterString(slashed);"
  print  "      ht_write_no_lock(fixup1_hashtable,(w_word)dotified,(w_word)fu);"
  print  "    }"
  print  "  }"
  printf "  for (i = 0; i < %d; ++i) {\n", ccount
  print  "    w_fixup fu = classmap[i].fixup2;"
  print  "    if (fu) {"
  print  "      cname = classmap[i].classname;"
  print  "      slashed = cstring2String(cname, strlen(cname));"
  print  "      dotified = slashes2dots(slashed);"
  print  "      deregisterString(slashed);"
  print  "      ht_write_no_lock(fixup2_hashtable,(w_word)dotified,(w_word)fu);"
  print  "    }"
  print  "  }"
  print  "}"
  print  ""

  printf "w_clazz loadOne%sClass(const char *name) {\n", Module
  print  "  w_string dotified;"
  print  "  w_clazz clazz;"
  print  "  dotified = slashes2dots(cstring2String(name, strlen(name)));"
  print  "  clazz = seekClazzByName(dotified, NULL);"
  print  "  if (clazz == NULL) {"
  print  "    clazz = loadBootstrapClass(dotified);"
  print  "    if (clazz == NULL) {"
  printf "      woempa(9,\"Unable to find WNI class %%s:\\n\",name);\n"
  print  "    }"
  print  "  }"
  print  "  deregisterString(dotified);"
  print  "  return clazz;"
  print  "}"
  print  ""
  printf "void load%sClasses() {\n", Module
  print  "  int i;"
  printf "  for (i = 0; i < %d; ++i) {\n", ccount
  printf "    *classmap[i].clazzptr = loadOne%sClass(classmap[i].classname);\n", Module
  print  "  }"
  print  "}"

}
