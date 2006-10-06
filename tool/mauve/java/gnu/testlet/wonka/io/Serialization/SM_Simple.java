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

public class SM_Simple implements Serializable {
  private boolean b1 = true;
  private boolean b2 = false;

  int  anInt = 666;
  int  someOtherInt = 10;
  long aLong = 7;

  float  aFloat  = (float)16.66;
  double aDouble = 3.14159265;

  public boolean equals(Object object) {
    SM_Simple other = (SM_Simple) object;

    boolean equalBoolean = ((this.b1 == other.b1) && (this.b2 == other.b2));
    boolean equalInt = ((this.anInt == other.anInt) && (this.someOtherInt == other.someOtherInt));
    boolean equalLong = (this.aLong == other.aLong);
    boolean equalFloat = ((other.aFloat-0.001 < this.aFloat) && (this.aFloat < other.aFloat+0.001));
    boolean equalDouble = ((other.aDouble-0.001 < this.aDouble) && (this.aDouble < other.aDouble+0.001));

    System.out.println("equal boolean? " + equalBoolean);
    System.out.println("equal int? " + equalInt);
    System.out.println("equal long? " + equalLong);
    System.out.println("equal float? " + equalFloat);
    System.out.println("equal double? " + equalDouble);

    System.out.println();
    boolean result2 = equalBoolean && equalInt && equalLong && equalFloat && equalDouble;
    boolean result  =  this.b1 == other.b1 && this.b2 == other.b2
                      && this.anInt == other.anInt && this.someOtherInt == other.someOtherInt
                      && this.aLong == other.aLong
                      && (other.aFloat-0.001 < this.aFloat) && (this.aFloat < other.aFloat+0.001)
                      && (other.aDouble-0.001 < this.aDouble) && (this.aDouble < other.aDouble+0.001);

    System.out.println("EQUAL:" + result);
    return result;
  }
}