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

/**
* not a testfile --> used by SMFieldTest
*/
package gnu.testlet.wonka.lang.reflect.Modifier;

public class Mhlp
{
	public final int pui = 23;
	public static int puj;
	private long prl ;
	protected static final long prk= 1L;
	public Mhlp (char c){puj = 54; prl = 1234L; }
	
	public  void method1(){};
	private strictfp static final double method2(int i, long j,char[] ca, String s) {return 1.0;}
	protected synchronized Object method3(Object o) throws NullPointerException, Exception {return o;}
	private native void method4(String s, byte b);
	public String toString() { return "string";}
	


	public Mhlp ()throws NullPointerException, IllegalArgumentException, Exception { }		
	private Mhlp (int i)	{ }
	protected Mhlp (long l)	{ }
	public Mhlp (Object o)	{ }

	
}	
  