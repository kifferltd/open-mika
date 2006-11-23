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
** $Id: CollatorResource.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

import wonka.vm.IntHashtable;

class CollatorResource {

  static IntHashtable orderHash;

  int order;
  char base;
  boolean full;
  char[] decomposition;

  public CollatorResource(int order){
    this.order = order;
  }

  public CollatorResource(int order, char base, boolean can, char[] decom){
    this.order = order;
    this.base = base;
    this.full = can;
    this.decomposition = decom;
  }

  static Object getInstance(int order, boolean full, char[] decom, char base){
    if(decom == null && base == 0){
      Object o = orderHash.get(order);
      if(o == null){
        o = new CollatorResource(order);
        orderHash.put(order, o);
      }
      return o;
    }
    return new CollatorResource(order, base, full, decom);
  }
}
