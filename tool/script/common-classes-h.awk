###########################################################################
# Copyright (c) 2021, 2022 by KIFFER Ltd. All rights reserved.            #
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

function parse(a) {
  startchar = substr(a,1,1)
  if(startchar=="[" || startchar=="L") type = "w_instance"
  else if(startchar=="B") type = "w_sbyte"
  else if(startchar=="C") type = "w_char"
  else if(startchar=="D") type = "w_double"
  else if(startchar=="F") type = "w_float"
  else if(startchar=="I") type = "w_int"
  else if(startchar=="J") type = "w_long"
  else if(startchar=="S") type = "w_short"
  else if(startchar=="Z") type = "w_boolean"
  else if(startchar=="V") type = "void"
  else {
    print > /dev/stderr
    print "  invalid character "startchar"!"  
    exit 1
  }

  while(startchar=="[") {a = substr(a,2); startchar = substr(a,1,1)}

  if(startchar=="L") {
    semicolon = index(a,";");
    if(!semicolon) {
      print > /dev/stderr
      print "  missing semicolon!" 
      exit 1
    }

    a = substr(a,semicolon);
  }

  return substr(a,2)
}

BEGIN {
  printf "void collect%sDispatchers(w_hashtable hashtable);\n", Module
  printf "w_clazz loadOne%sClass(const char *name);\n", Module
  print " "
  print "/*"
  print "** Prototypes for the native methods"
  print "*/"
  print " "
}

# Skip lines starting with "#"

/^#/{next}

# no leading whitespace -> clazz declaration
# else -> field or method declaration


/^[a-zA-Z]/ {
  fqn=$1
  ++clazzcount
  slash=index(fqn,"/")
  lastslash=0
  while(slash) { lastslash+=slash; rest=substr(fqn,lastslash+1); slash=index(rest,"/") }
  thisclazz=substr(fqn,lastslash+1)
  gsub("\\$", "_dollar_", thisclazz)
  clazz[clazzcount]=thisclazz
  path[clazzcount]=substr(fqn,1,lastslash)

  l=length(thisclazz)
  if (l > 4 && (l - index(thisclazz, "Error") == 4)) {
    likelyerrors = thisclazz" "likelyerrors
  }
  if (l > 8 && (l - index(thisclazz, "Exception") == 8)) {
    likelyexceptions=thisclazz" "likelyexceptions
  }

  if (thisclazz == "Throwable") {
    likelyerrors = thisclazz" "likelyerrors
  }
  if (thisclazz == "ThreadDeath") {
    likelyerrors = thisclazz" "likelyerrors
  }

}

/^[ \t]/ {

  if($1=="") next

  if($2=="+") { 
    field=$1
    offset[thisclazz,field] = "F_"thisclazz"_"field
  }
  else { 
    descriptor=$2

    if(substr(descriptor,1,1)!="(") {
      field=$1
      offset[thisclazz,field] = "F_"thisclazz"_"field
      next
    }

    method=$1
    offset[thisclazz,method] = "M_"thisclazz"_"method
    functionname=$3

    if(!functionname) next

    endparen = index(descriptor,")");

    if(endparen==0) {
      print "  descriptor does not contain \")\"!" # > /dev/stderr
      exit 1
    }

    arguments = substr(descriptor,2,endparen-2)

    paramlist = ""

    while(arguments) {
      arguments = parse(arguments)
      if (length(paramlist)) {
        paramlist = paramlist ", " type
      }
      else {
        paramlist = type
      }
    }
#

    result = substr(descriptor,endparen+1)

    parse(result)
    if (length(paramlist) == 0) {
      printf "%-10s %s(w_thread, jobject);\n", type, functionname
    }
    else {
      printf "%-10s %s(w_thread, jobject, %s);\n", type, functionname, paramlist
    }
  } 
}

END {
  print "#include \"dispatcher.h\"" 
  print "#include \"hashtable.h\"" 
  print ""
  print "extern w_hashtable dispatchers_hashtable;"
#  print "extern w_hashtable instance_dispatchers_hashtable;"

  print " "
  print "/*"
  print "** For each clazz we declare:"
  print "**   - a clazz structure;"
  print "**   - a constant Wonka string holding the class name;"
  print "**   - a constant Wonka string holding the class descriptor."
  print "** For each field or method of a clazz we declare an integer"
  print "** which will be initialized to hold its slot number."
  print "*/"
  for(c = 1; c in clazz; ++c) {
    basename = clazz[c]
    print " "
    printf "extern w_clazz clazz%s;\n",basename
    for(cf in offset) {
      split(cf,a,SUBSEP)
      if(a[1]==basename) printf "extern w_int %s;\n",offset[cf]
    }
  }
  print " "

  printf("\n/*\n** Hashtable mapping class names onto fixup_ functions\n*/\n\n");
  print "extern w_hashtable awt_classes_hashtable;"

  print "#endif "
}
