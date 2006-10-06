/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
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
