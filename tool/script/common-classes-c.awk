
BEGIN {
  if(JNI=="true") {
  }
  else {
  } 

  desc2id["B"] = "b"
  desc2id["C"] = "c"
  desc2id["D"] = "d"
  desc2id["F"] = "f"
  desc2id["I"] = "i"
  desc2id["J"] = "j"
  desc2id["L"] = "l"
  desc2id["S"] = "s"
  desc2id["Z"] = "z"
  desc2id["["] = "a"
  desc2id["V"] = "v"

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
    if(desc2id[letter]) {
      id = sprintf("%s%s",id,desc2id[letter])
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
  if(desc2id[letter]) {
    id = sprintf("%s_%s",id,desc2id[letter])
  }
  else {
      print "unexpected letter", letter
  }
  
  return id
}

function id2iplist(id) {
  depth = length(id)-1
  plist = sprintf("thread, (w_instance) top[-%d].c", depth--)
  while(depth){
    letter=substr(id,depth,1)
    switch (letter) {
      case "a":
      case "b":
      case "c":
      case "f":
      case "i":
      case "l":
      case "s":
      case "z":
        plist = sprintf("%s, (%s)top[-%d].c",plist,id2type[letter],depth--)
#    return if3(thread, (w_instance) top[-3].c, top[-2].c, top[-1].c);
        break
      case "d":
# TODO - take two words from stack
        plist = sprintf("%s, (%s)top[-%d].c",plist,id2type[letter],depth--)
        break
      case "j":
# TODO - take two words from stack
        plist = sprintf("%s, (%s)top[-%d].c",plist,id2type[letter],depth--)
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
  depth = length(id)-2
  plist = "thread, theClass"
  while(depth){
    letter=substr(id,depth,1)
    switch (letter) {
      case "a":
      case "b":
      case "c":
      case "f":
      case "i":
      case "l":
      case "s":
      case "z":
        plist = sprintf("%s, (%s)top[-%d].c",plist,id2type[letter],depth--)
        break
      case "d":
# TODO - take two words from stack
        plist = sprintf("%s, (%s)top[-%d].c",plist,"w_double",depth--)
        break
      case "j":
# TODO - take two words from stack
        plist = sprintf("%s, (%s)top[-%d].c",plist,"w_long",depth--)
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
          printf "void fixup1_%s(w_clazz clazz) {\n\n", thisclazz
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
        printf "void fixup1_%s(w_clazz clazz) {\n\n", thisclazz
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
      printf "void fixup2_%s(w_clazz clazz) {\n\n", thisclazz
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
  printf "  {\n    w_instance loader = isSet(method->flags, ACC_STATIC)\n        ? method->spec.declaring_clazz->loader\n        : instance2clazz(frame->jstack_base[- method->exec.arg_i].c)->loader;\n"
  printf "    if (loader && !getBooleanField(loader, F_ClassLoader_systemDefined)) {\n      frame->udcl = loader;\n    }\n"
  printf "    else {\n      frame->udcl = caller->udcl;\n    }\n  }\n"
  printf "#endif\n"
  printf "}\n\n"

  print "/* dispatchers */"
  for (id in protos) {
    printf "void native_static_synchronized_%s(w_frame caller, w_method method) {\n", id
    printf "  w_Frame theFrame;\n  w_frame frame = &theFrame;\n  w_int idx = - method->exec.arg_i;\n"
    printf "  w_instance theClass;\n  x_monitor m = NULL;\n  volatile w_thread thread = caller->thread;\n "
    nonvoid = rtypes[id] != "void"
    reference = rtypes[id] == "w_instance"
    if (nonvoid) printf "  %s result;\n\n",id2rtype(id)
    printf "  woempa(7, \"Calling %%M\\n\", method);\n  frame->jstack_base = caller->jstack_top;\n  prepareNativeFrame(frame, thread, caller, method);\n\n"
    printf "  threadMustBeSafe(thread);\n\n"
    printf "  theClass = clazz2Class(frame->method->spec.declaring_clazz);\n"
    printf "  if (isSet(method->flags, ACC_SYNCHRONIZED)) {\n    m = getMonitor(theClass);\n    x_monitor_eternal(m);\n  }\n\n  thread->top = frame;\n\n"
    printf "  frame->jstack_top[0].c = 0;\n  frame->jstack_top[0].s = stack_%strace;\n  frame->jstack_top += 1;\n", reference ? "" : "no"
    printf "  w_slot top = caller->jstack_top;\n"
    printf "  typedef %s (sfun_%s) (w_thread, w_instance%s);\n",rtypes[id],id,protos[id]
    printf "  sfun_%s *sf%s = (sfun_%s*)method->exec.function.word_fun;\n",id,id,id
    if (nonvoid) printf "  result ="
    printf "  sf%s(%s);\n",id,static_plists[id]
    printf "  if (m) {\n    x_monitor_exit(m);\n  }\n\n";
    if (nonvoid) {
      printf "  if (thread->exception) {\n    woempa(7, \"%%m threw %%e, ignoring return value\\n\", method, thread->exception);\n"
      printf "    caller->jstack_top[idx].s = stack_notrace;\n    caller->jstack_top += idx + 1;\n    thread->top = caller;\n  }\n"
      printf "  else {\n    enterUnsafeRegion(thread);\n"
      printf "    woempa(7, \"%%m result = %%08x\\n\", method, result);\n"
      printf "    caller->jstack_top[idx].c = (w_word)result;\n    caller->jstack_top[idx].s = stack_%strace;\n    caller->jstack_top += idx + 1;\n", reference ? "" : "no"
      if (reference) printf "    if (result) {\n      setFlag(instance2flags(result), O_BLACK);\n    }\n"
      printf "    thread->top = caller;\n    enterSafeRegion(thread);\n  }\n"
    }
    else {
      printf "  enterUnsafeRegion(thread);\n  caller->jstack_top += idx + 1;\n  thread->top = caller;\n    enterSafeRegion(thread);\n"
    }
    printf "}\n\n"
  }

  for (id in protos) {
    printf "void native_instance_%s(w_frame caller, w_method method) {\n", id
    printf "  w_Frame theFrame;\n  w_frame frame = &theFrame;\n  w_int idx = - method->exec.arg_i;\n"
    printf "  w_instance o;\n  x_monitor m = NULL;\n  volatile w_thread thread = caller->thread;\n"
    nonvoid = rtypes[id] != "void"
    reference = rtypes[id] == "w_instance"
    if (nonvoid) printf "  %s result;\n\n",id2rtype(id)
    printf "  woempa(7, \"Calling %%M\\n\", method);\n  frame->jstack_base = caller->jstack_top;\n  prepareNativeFrame(frame, thread, caller, method);\n\n"
    printf "  threadMustBeSafe(thread);\n\n"
    printf "  o = (w_instance) caller->jstack_top[idx].c;\n"
    printf "  if (isSet(method->flags, ACC_SYNCHRONIZED)) {\n    m = getMonitor(o);\n    x_monitor_eternal(m);\n  }\n\n  thread->top = frame;\n\n"
    printf "  frame->jstack_top[0].c = 0;\n  frame->jstack_top[0].s = stack_%strace;\n  frame->jstack_top += 1;\n", reference ? "" : "no"
    printf "  w_slot top = caller->jstack_top;\n"
    printf "  typedef %s (ifun_%s) (w_thread, w_instance%s);\n",rtypes[id],id,protos[id]
    printf "  ifun_%s *if%s = (ifun_%s*)method->exec.function.word_fun;\n",id,id,id
    if (nonvoid) printf "  result ="
    printf "  if%s(%s);\n",id,instance_plists[id]
    printf "  if (m) {\n    x_monitor_exit(m);\n  }\n\n";
    if (nonvoid) {
      printf "  if (thread->exception) {\n    woempa(7, \"%%m threw %%e, ignoring return value\\n\", method, thread->exception);\n"
      printf "    caller->jstack_top[idx].s = stack_notrace;\n    caller->jstack_top += idx + 1;\n    thread->top = caller;\n  }\n"
      printf "  else {\n    enterUnsafeRegion(thread);\n"
      printf "    woempa(7, \"%%m result = %%08x\\n\", method, result);\n"
      printf "    caller->jstack_top[idx].c = (w_word)result;\n    caller->jstack_top[idx].s = stack_%strace;\n    caller->jstack_top += idx + 1;\n", reference ? "" : "no"
      if (reference) printf "    if (result) {\n      setFlag(instance2flags(result), O_BLACK);\n    }\n"
      printf "    thread->top = caller;\n    enterSafeRegion(thread);\n  }\n"
    }
    else {
      printf "  enterUnsafeRegion(thread);\n  caller->jstack_top += idx + 1;\n  thread->top = caller;\n    enterSafeRegion(thread);\n"
    }
    printf "}\n\n"
  }

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
