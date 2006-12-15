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
*                                                                         *
* Additions copyright (C) 2005 Chris Gray, /k/ Embedded Java Solutions.   *
* Permission is hereby granted to distribute these changes under the      *
* terms of the Wonka Public Licence.                                      *
**************************************************************************/


/**
 * $Id: AbstractSet.java,v 1.2 2005/09/10 18:10:07 cvs Exp $
 */

package java.util;

public abstract class AbstractSet extends AbstractCollection implements Set {

  protected AbstractSet() {
  }

  public boolean equals (Object o) {
    if (!( o instanceof Set)) return false;
    // null is no instance of Set !!!
    if (o == this) return true;
    Set s = (Set)o;
    boolean answer = true;
    answer =(answer && (size() == s.size()));
    answer =(answer && containsAll(s));
    return answer;
  }

  public int hashCode() {
    int h = 0;
    Object o;
    Iterator it = iterator();
    while (it.hasNext()) {
      o = it.next();
      h = h +(o == null ? 0 :o.hashCode());
    }
    return h;
  }

  public boolean removeAll(Collection c) {
    Iterator it;
    Object o;
    boolean result = false;

    if (size() < c.size()) {
      // this set is smaller than c, iterate over this set and remove each 
      // element which is present in c
      it = iterator();
      while (it.hasNext()) {
        o = it.next();
	if (c.contains(o)) {
	  it.remove();
	  result = true;
	}
      }
    }
    else {
      // this set is at least as big as c, iterate over c and remove each 
      // element which is present in this set
      it = c.iterator();
      while (it.hasNext()) {
        o = it.next();
	if (this.contains(o)) {
	  it.remove();
	  result = true;
	}
      }
    }

    return result;
  }

}  
