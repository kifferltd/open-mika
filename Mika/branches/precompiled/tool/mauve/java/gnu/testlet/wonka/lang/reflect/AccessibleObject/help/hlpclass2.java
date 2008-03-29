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


package  gnu.testlet.wonka.lang.reflect.AccessibleObject.help;

public class hlpclass2 {
   	
	private int ipr = 4;
	public int ipu = 1;
	protected int ipt = 2;
	int idef = 3;
	protected static int ispt = 5;

	public void publicMethod(){}
	protected void protectedMethod(){}
	protected static void protectedStaticMethod(){}
	void defaultMethod(){}
	private void privateMethod(){}
	
	public hlpclass2() {}
	protected hlpclass2(int i) {}
	hlpclass2(float f) {}
	private hlpclass2(Object o) {}

}