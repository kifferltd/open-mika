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

import java.util.Arrays;
import java.io.Serializable;

public class SM_ClassArray implements Serializable {
  int[] intArray = null;
  int[] intArray2 = { 1, 2, 5 };
  int[][] intArray3 = { { 1, 2, 5 }, { 11, 12, 15 } };
  byte[] byteArray = { (byte)0xac, (byte)0xed };
  double[] doubleArray = { 1.0, 0.0, -999.0 };
  Object[] objectArray = { new Integer(666), "Hello world."};

  public boolean equals(Object object) {
    SM_ClassArray other = (SM_ClassArray) object;

    // intArray3
    for (int i = 0; i < intArray3.length; i++) {
      if ( ! Arrays.equals(this.intArray3[i], other.intArray3[i]) ) {
        return false;
      }
    }
    return
      Arrays.equals(this.intArray, other.intArray)
      && Arrays.equals(this.intArray2, other.intArray2)
      && Arrays.equals(this.byteArray, other.byteArray)
      && Arrays.equals(this.doubleArray, other.doubleArray)
      && Arrays.equals(this.objectArray, other.objectArray);
  }
}