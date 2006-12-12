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
 * Description: A helper for visual tests.
 * provides a static method to invoke a protected method of class java.awt.Component
 * by reflection.
 *
 * Author: J. Vandeneede
 * Created: 2001/01/08
 */

package gnu.testlet.wonka.awt.Component;
import java.lang.reflect.Method;
import java.awt.Component;

public class DispatcherHelper {

  public static boolean setDispatcher()
    {
    try
      {
      Class Comp = Class.forName("java.awt.Component");
      Class[] Args = new Class[1];
      Args[0] = Integer.TYPE;
      Method awtStart = Comp.getDeclaredMethod("awt", Args);

      Object[] obj = new Object[1];
      obj[0] = new Integer(1);
      awtStart.invoke(null,obj);
      return true;
      }
    catch (Exception e)
      {return false;}
    }
  }

