###########################################################################
# Copyright (c) 2001 by Punch Telematix. All rights reserved.             #
#                                                                         #
# Redistribution and use in source and binary forms, with or without      #
# modification, are permitted provided that the following conditions      #
# are met:                                                                #
# 1. Redistributions of source code must retain the above copyright       #
#    notice, this list of conditions and the following disclaimer.        #
# 2. Redistributions in binary form must reproduce the above copyright    #
#    notice, this list of conditions and the following disclaimer in the  #
#    documentation and/or other materials provided with the distribution. #
# 3. Neither the name of Punch Telematix nor the names of                 #
#    other contributors may be used to endorse or promote products        #
#    derived from this software without specific prior written permission.#
#                                                                         #
# THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          #
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    #
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    #
# IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       #
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            #
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    #
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         #
# BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   #
# WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    #
# OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  #
# IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           #
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

# First output the #ifdef stuff and the function prototypes

BEGIN { 
  print "#ifndef _CORE_CLASSES_H"
  print "#define _CORE_CLASSES_H"
  print " "
  print "/*"
  print "** This file is automatically generated. Do NOT edit this file."
  print "*/"
  print " "
  print "#include \"repository.h\""
  print "#include \"wonka.h\""
  print "#include \"jni.h\""
  print " "
  print "void loadCoreClasses(void);"
  print "void startHandlers(void);"
  print "void collectCoreFixups(void);"
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
}

/^[ \t]/ {

  if($1=="") next

  if($2=="+") { 
    field=$1
    gsub("\\$", "_dollar_", field)
    offset[thisclazz,field] = "F_"thisclazz"_"field
  }
  else { 
    descriptor=$2

    if(substr(descriptor,1,1)!="(") {
      field=$1
      gsub("\\$", "_dollar_", field)
      offset[thisclazz,field] = "F_"thisclazz"_"field
      next
    }

    method=$1
    gsub("\\$", "_dollar_", method)
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
      paramlist = paramlist ", " type
    }

    result = substr(descriptor,endparen+1)

    parse(result)
    printf "%-10s %s(JNIEnv*, w_instance%s);\n", type, functionname, paramlist
  } 
}


END {
  print " "
  print "/*"
  print "** For each clazz we declare a clazz structure."
  print "** For each field or method of a clazz we declare an integer"
  print "** which will be initialized to hold its slot number."
  print "*/"
  print " "
  for(c = 1; c in clazz; ++c) {
    printf "extern w_clazz clazz%s;\n",clazz[c]
    for(cf in offset) {
      split(cf,a,SUBSEP)
      if(a[1]==clazz[c]) printf "extern w_int %s;\n",offset[cf]
    }
    print " "
  }

  print "#endif /* _CORE_CLASSES_H */"
}
