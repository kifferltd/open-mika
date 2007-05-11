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
package java.awt.datatransfer;
import java.util.*;

public final class SystemFlavorMap implements FlavorMap{
  private static com.acunia.wonka.rudolph.DefaultFlavorMap defaultfm = new com.acunia.wonka.rudolph.DefaultFlavorMap();
  private static SystemFlavorMap sfm = new SystemFlavorMap();

  private SystemFlavorMap(){
    ;
  }

  public static FlavorMap getDefaultFlavorMap(){
    return sfm;
  }

  public Map getNativesForFlavors(DataFlavor[] flavors){
    return defaultfm.getNativesForFlavors(flavors);
  }

  public Map getFlavorsForNatives(String[] natives){
    return defaultfm.getFlavorsForNatives(natives);
  }

  public static String encodeJavaMIMEType(String mimeType){
    return "JAVA_DATAFLAVOR:"+mimeType;
  }

  public static String encodeDataFlavor(DataFlavor df){
    return "JAVA_DATAFLAVOR:"+df.getMimeType();
  }

  public static boolean isJavaMIMEType(String atom){
    return(atom.indexOf("JAVA_DATAFLAVOR:")>=0);
  }

  public static String decodeJavaMIMEType(String atom){
    if(isJavaMIMEType(atom)) return atom.substring(atom.indexOf("JAVA_DATAFLAVOR:")+16);
    else return null;
  }

  public static DataFlavor decodeDataFlavor(String atom) throws ClassNotFoundException{
    String mimeType = decodeJavaMIMEType(atom);
    if(mimeType!=null) return new DataFlavor(mimeType,"");
    else return null;
  }

}
