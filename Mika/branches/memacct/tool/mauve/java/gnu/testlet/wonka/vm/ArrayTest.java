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



package gnu.testlet.wonka.vm;

import java.lang.reflect.Array;
import gnu.testlet.*;

public class ArrayTest implements Testlet {

  protected TestHarness th;	

  public void test(TestHarness harness) {

    th = harness;
    th.setclass("java.lang.reflect.Array");
    /*
    ** Some simple creation tests...
    */
    th.checkPoint("Some simple creation tests...");

    int[] d0 = {0};
    int[] a0 = (int[])Array.newInstance(int.class, d0);
    int[] b0 = (int[])Array.newInstance(int.class, 0);
    int[] d1 = {10, 10};
    String[][] a1 = (String[][])Array.newInstance(String.class, d1);
    int[] d2 = {10};
    int[][] a2 = (int[][])Array.newInstance(int[].class, d2);
    int[][] b2 = (int[][])Array.newInstance(int[].class, 10);
    int[] d3 = {10, 10};
    int[][][] a3 = (int[][][])Array.newInstance(int[].class, d3);

    /*
    ** Now try to fill some odd sized (8 bits) array and read it back...
    */

/*    
    int[] d4 = {2, 3, 4};
    byte[][][] a4 = (byte[][][])Array.newInstance(byte.class, d4);
*/

    byte[][][] a4 = new byte[2][3][4];
    byte data = 0;

    for (int i = 0; i < a4.length; i++) {
      for (int j = 0; j < a4[i].length; j++) {
        for (int k = 0; k < a4[i][j].length; k++) {
          a4[i][j][k] = data++;
        }
      }
    }
    
    data = 0;
    for (int i = 0; i < a4.length; i++) {
      for (int j = 0; j < a4[i].length; j++) {
        for (int k = 0; k < a4[i][j].length; k++) {
          th.check(a4[i][j][k] == data++ ,"a4[" + i + "][" + j + "][" + k + "] = " + a4[i][j][k] + " in stead of " + (data - 1));
        }
      }
    }


  }

  public ArrayTest() {
  }

}
