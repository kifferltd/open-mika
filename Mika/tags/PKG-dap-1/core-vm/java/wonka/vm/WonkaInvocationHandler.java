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


/*
** $Id: WonkaInvocationHandler.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package wonka.vm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

public final class WonkaInvocationHandler implements InvocationHandler {

  protected WonkaInvocationHandler() {}

  public Object invoke(Object proxy, Method method, Object[] args)
    throws Throwable
  {
    Object result;

  // TODO: suppose proxy is not a proxy, method is not an interface method
  //       or not from one of its interfaces ... what should we do?
    try {
      result = method.invoke(proxy, args);
    }
    catch (Error e) {
      throw e;
    }
    catch (RuntimeException re) {
      throw re;
    }
    catch (Throwable t) {
      Class[] fwows = method.getExceptionTypes();

      for (int i = 0; i < fwows.length; ++i) {
        if (fwows[i].isInstance(t)) {
          throw t;
        }
      }

      throw new UndeclaredThrowableException(t);
    }

    return result;
  }

}

