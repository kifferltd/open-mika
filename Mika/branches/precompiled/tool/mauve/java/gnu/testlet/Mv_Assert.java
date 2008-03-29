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

public class Mv_Assert implements Testlet {
	
	protected Mv_Assert() {
	  fName ="JUnit test in Mauve";
	}
	
	public static void assertTrue(String str, boolean check) {
		th.check(check,str);
	}
	
	public static void assertEqual(String str, double val, double exp){
	  th.check(val, exp, str);
	}
	
	public static void assertTrue(boolean check) {
	  th.check(check);
	}
	
	public static void assertApproximatelyEqual(double exp, double val, double d) {
	    double cmp = exp - val;
	    if (cmp < 0.0){
	      cmp = -cmp;
	    }
	    th.check(cmp < d);
	}
	
	public static void assertEqual(long exp, long val) {
	    th.check(exp, val);
	}
	
	public static void assertEqual(Object exp, Object val) {
	    th.check(exp, val);
	}
	
	public static void assertApproximatelyEqual(String str, double exp, double val, double d) {
	    th.check((Math.abs(exp-val) < d),str);
	}
	
	public static void assertEqual(String str, long exp, long val) {
	    th.check(val, exp, str);
	}
	
	public static void assertEqual(String str, Object exp, Object val) {
		th.check(val,exp,str);
	}
	
	public static void assertNotNull(Object object) {
		th.check(object != null);
	}
	
	public static void assertNotNull(String str, Object object) {
		th.check(object != null, str);
	}
	
	public static void assertNull(Object object) {
		th.check(object , null);
	}
	
	public static void assertNull(String str, Object object) {
	  th.check(object,null, str);
	}
	
	public static void assertSame(Object exp, Object val) {
	  th.check(exp, val);
	}
	public static void assertSame(String str, Object exp, Object val) {
		th.check(exp,val,str);
	}
	
	public static void fail() {
		th.fail("no message");
	}
	
	public static void fail(String str) {
		th.fail(str);
	}
		
// adding code
	protected void setUp() throws Exception {}
	
	private final String fName;

	public Mv_Assert(String name){
		fName = name;
	}
	
 	protected void tearDown() {}
	protected void runTest() throws Exception {}
	protected static TestHarness th;
	
	public void test(TestHarness testharness)	{
		th =testharness;
		try 	{
			setUp();
			runTest();
		 	}
		catch (Throwable t) {
			th.fail("caught	uncaught Exception got "+t);
			t.printStackTrace();
		}
		finally {tearDown();}
	}
}
