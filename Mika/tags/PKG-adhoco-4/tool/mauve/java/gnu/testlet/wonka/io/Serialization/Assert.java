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

/**
 * Insert the type's description here.
 * Creation date: (08/01/00 %r)
 * @author: 
 */
public class Assert {
	private static final boolean DEVELOPMENT = true;
	public static void bug (boolean expr, String message)
	{
		if (DEVELOPMENT)
		{
			Bug.when(expr, message);
		}
	}
	public static void fatal (Throwable t, String message)
	{
		System.exit(1);
	}
	public static void fatal (boolean expr, String message)
	{
		System.exit(1);
	}
	public static void problem (Throwable t, String message) {
		throw new Bug (t, message);
	}
	public static void problem (boolean expr, String message) {
		Bug.when (expr, message);
	}
}
