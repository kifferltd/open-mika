###########################################################################
# Copyright (c) 2021, 2022, 2023, 2024 by KIFFER Ltd. All rights reserved.#
#                                                                         #
# Redistribution and use in source and binary forms, with or without      #
# modification, are permitted provided that the following conditions      #
# are met:                                                                #
# 1. Redistributions of source code must retain the above copyright       #
#    notice, this list of conditions and the following disclaimer.        #
# 2. Redistributions in binary form must reproduce the above copyright    #
#    notice, this list of conditions and the following disclaimer in the  #
#    documentation and/or other materials provided with the distribution. #
# 3. Neither the name of KIFFER Ltd nor the names of other contributors   #
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

# Function to parse a method descriptor.

BEGIN {
  gendir = ENVIRON["gendir"]
  dispdir = gendir "/dispatchers"
  system("mkdir -p " dispdir "/idmap")
  system("mkdir -p " dispdir "/plist")
  system("mkdir -p " dispdir "/rtype")
  system("mkdir -p " dispdir "/proto")

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
# OLD
#    idmap[descr] = id
#    if(!protos[id]) {
#      plists[id]=id2plist(id)
#      rtypes[id]=id2rtype(id)
#      protos[id]=id2proto(id)
#    }
# NEW
    dotted = descr;  gsub(/\//,".",dotted)
    print id > dispdir "/idmap/" dotted 
    print id2plist(id) > dispdir "/plist/" id
    print id2rtype(id) > dispdir "/rtype/" id
    print id2proto(id) > dispdir "/proto/" id
# END
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
