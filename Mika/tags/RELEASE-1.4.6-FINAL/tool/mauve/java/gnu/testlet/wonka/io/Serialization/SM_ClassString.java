/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


package gnu.testlet.wonka.io.Serialization;

import java.io.Serializable;


public class SM_ClassString implements Serializable {
  String aString     = "Hello world.";
  String sameString  = aString;
  String otherString = null;

  public boolean equals (Object object) {
    SM_ClassString other = (SM_ClassString)object;

    System.out.println("SM_ClassString In equals");
    boolean equal_aString = this.aString == null ?
                              other.aString == null :
                              this.aString.equals(other.aString);
    System.out.println("equal_aString: " + equal_aString);

    boolean equal_sameString = this.sameString == null ?
                                 other.sameString == null :
                                 this.sameString.equals(other.sameString);
    System.out.println("equal_sameString: " + equal_sameString);

    boolean equal_otherString = this.otherString == null ?
                                    other.otherString == null :
                                    this.otherString.equals(other.otherString);
    System.out.println("equal_otherString: " + equal_otherString);

    System.out.println("SM_ClassString End equals");

    return equal_aString && equal_sameString && equal_otherString;
  }
}