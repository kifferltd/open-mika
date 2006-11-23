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
** $Id: GuardedObject.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.security;

public class GuardedObject implements java.io.Serializable{

  private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        if (guard != null){
                guard.checkGuard(object);
        }
        oos.defaultWriteObject();
  }

  private Guard guard;
  private Object object;

  public GuardedObject(Object object, Guard guard) {
        this.object = object;
        this.guard = guard;
  }

  public Object getObject() throws SecurityException {
        if (guard != null){
                guard.checkGuard(object);
        }
        return object;
  }

}
