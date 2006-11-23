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

package gnu.testlet.wonka.lang.reflect.Member;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.lang.reflect.Member;

/**
* this file contains testcode for the java.lang.reflect.Member class <br>
* <br>
* Needs tests on: <br>
* -   <br>
*
*/
public class SMMemberTest implements Testlet {
	
	protected TestHarness th;

  public void test (TestHarness harness){
    th = harness;
    th.setclass("java.lang.reflect.Member");
    th.checkPoint("member=PUBLIC type=int");
    th.check(Member.PUBLIC , 0 , "test field PUBLIC");
    th.checkPoint("member=DECLARED type=int");
    th.check(Member.DECLARED , 1 , "test field PUBLIC");	
  } 	
}