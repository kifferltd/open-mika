#!/bin/sh

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
# 3. Neither the name of Punch Telematix nor the names of other           #
#    contributors may be used to endorse or promote products derived      #
#    from this software without specific prior written permission.        #
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

CURPATH=`pwd`

IFSBAK=$IFS

for NAME in $*; do

  FULLPATH=$CURPATH/$NAME

  echo "Converting $FULLPATH"

  IFS='
'
  
  rm -rf /tmp/pngifier
  mkdir /tmp/pngifier
  cd /tmp/pngifier

  jar xf $FULLPATH

  echo "  Converting gif's..."

  for x in $(find . -name \*.gif); do
    convert $x $(echo $x | sed -e 's/\.gif$/.png/')
    rm -f $x
  done

  echo "  Converting jpg's..."

  for x in $(find . -name \*.jpg); do
    convert "$x" "$(echo $x | sed -e 's/\.jpg$/.png/')"
    rm -f "$x"
  done

  echo "  Converting classes..."

  for x in $(find . -name \*.class); do
    cat $x | sed -e 's/\.gif/.png/g' > '$x 2'
    cat '$x 2' | sed -e 's/\.jpg/.png/g' > $x
    rm -f '$x 2';
  done
  
  rm -f $FULLPATH

  jar cfm $FULLPATH META-INF/MANIFEST.MF *
  
  cd ..

  IFS=$IFSBAK

done

