/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package com.acunia.wonka.rudolph;

import java.awt.datatransfer.*;
import java.util.*;

public final class DefaultFlavorMap implements FlavorMap{
  private HashMap mapSysJava = new HashMap(5);
  private HashMap mapJavaSys = new HashMap(5);


  public DefaultFlavorMap(){
    DataFlavor value = DataFlavor.stringFlavor;
    String key = "key";
/* THIS ARE THE THINGS I FOND WHEN I WAS SEARCHING MORE DATA
getNativeForFlavors(null)
native 0 = COMPOUND_TEXT
native 1 = COMPOUND_TEXT
native 2 = COMPOUND_TEXT
native 3 = COMPOUND_TEXT
native 4 = FILE_NAME
native 5 = COMPOUND_TEXT
native 6 = COMPOUND_TEXT
native 7 = COMPOUND_TEXT
native 8 = COMPOUND_TEXT
native 9 = COMPOUND_TEXT
native 10 = COMPOUND_TEXT
native 11 = COMPOUND_TEXT
native 12 = COMPOUND_TEXT
native 13 = COMPOUND_TEXT
native 14 = COMPOUND_TEXT
native 15 = COMPOUND_TEXT
native 16 = COMPOUND_TEXT
native 17 = COMPOUND_TEXT
native 18 = COMPOUND_TEXT
native 19 = COMPOUND_TEXT
native 20 = COMPOUND_TEXT
native 21 = COMPOUND_TEXT
native 22 = COMPOUND_TEXT
native 23 = COMPOUND_TEXT
native 24 = COMPOUND_TEXT
native 25 = PNG

getNativeForFlavor({DataFlavor.stringFlavor})
native 0 = COMPOUND_TEXT

getFlavorsForNatives(null)
flavor 0 = java.awt.datatransfer.DataFlavor[mimetype=application/x-java-file-list;representationclass=java.util.List]
((DataFlavor)flavors[0]).getHumanPresentableName() = application/x-java-file-list
flavor 1 = java.awt.datatransfer.DataFlavor[mimetype=image/x-java-image;representationclass=java.awt.Image]
((DataFlavor)flavors[1]).getHumanPresentableName() = image/x-java-image
flavor 2 = java.awt.datatransfer.DataFlavor[mimetype=application/x-java-serialized-object;representationclass=java.lang.String]
((DataFlavor)flavors[2]).getHumanPresentableName() = Unicode String
flavor 3 = java.awt.datatransfer.DataFlavor[mimetype=application/x-java-serialized-object;representationclass=java.lang.String]
((DataFlavor)flavors[3]).getHumanPresentableName() = Unicode String
flavor 4 = java.awt.datatransfer.DataFlavor[mimetype=application/x-java-serialized-object;representationclass=java.lang.String]
((DataFlavor)flavors[4]).getHumanPresentableName() = Unicode String

getNativeForFlavor({"COMPOUND_TEXT"});
flavor 0 = java.awt.datatransfer.DataFlavor[mimetype=application/x-java-serialized-object;representationclass=java.lang.String]
((DataFlavor)flavors[0]).getHumanPresentableName() = Unicode String
*/
// must be changed, here I will import all implemented keys with their valeus for this system
    mapSysJava.put(key,value);
    mapJavaSys.put(value,key);
  }

  public Map getNativesForFlavors(DataFlavor[] flavors){
    if(flavors == null) return mapJavaSys;
    else{
      HashMap returnedMap = new HashMap(5);
      for(int i=0; i<flavors.length; i++){
        returnedMap.put(flavors[i], mapJavaSys.get(flavors[i]));
      }
      return returnedMap;
    }
  }

  public Map getFlavorsForNatives(String[] natives){
    if(natives == null) return mapSysJava;
    else{
      HashMap returnedMap = new HashMap(5);
      for(int i=0; i<natives.length; i++){
        returnedMap.put(natives[i], mapSysJava.get(natives[i]));
      }
      return returnedMap;
    }
  }

}
