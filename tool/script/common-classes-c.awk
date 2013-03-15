
# skip lines beginning with '#'

/^\#/{next}

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
  print  "  char *cname;"
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

  printf "w_clazz loadOne%sClass(char *name) {\n", Module
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
