#!/bin/sh

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

