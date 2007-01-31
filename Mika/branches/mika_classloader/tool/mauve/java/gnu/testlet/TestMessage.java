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


package gnu.testlet;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestMessage implements Testlet
{
	public void test(TestHarness h)
	{
	h.debug("DEBUG is ON",true);
	h.checkPoint("Method NAME");
	h.setclass("ClassNAME");
	h.check(false,"Boodschap 1");
	h.check("abc", "fgr");
	//h.check(1,2);
	h.check(1.0,2.0);	
	h.check(2,2,"Boodschap 2");	

	//h.check(h.getClass(), this);
	h.check(this,this,"no message");
	h.check(h.getClass(),this,"no message");


//	System.out.println("TestHarness.fail(String) -> wijzigt hetcheckpoint\ndoet:\n\tTestHarness.checkPoint(String);\n\tTestHarness.check(false);");
	h.fail("newName");
	h.check(false,"boodschap3 ");
	h.check(true,"boodschap3");
	}
}	