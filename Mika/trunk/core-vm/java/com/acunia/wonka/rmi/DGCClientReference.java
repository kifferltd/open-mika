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

package com.acunia.wonka.rmi;

import java.lang.ref.*;
import java.rmi.server.ObjID;

/**
** this class is designed to be used as a key in Hashtable.
*/
final class DGCClientReference extends WeakReference {

  ObjID id;

  private int hashcode;

  DGCClientReference(Object o, ReferenceQueue queue, ObjID id){
    super(o,queue);
    hashcode = o.hashCode();
    this.id = id;
  }

  public boolean equals(Object o){
    return this == o || this.get() == o;
  }

  public int hashCode(){
    return hashcode;
  }
}
