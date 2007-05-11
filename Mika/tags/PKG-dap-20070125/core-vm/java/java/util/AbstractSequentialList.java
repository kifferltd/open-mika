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

/*
** $Id: AbstractSequentialList.java,v 1.2 2005/09/10 18:09:01 cvs Exp $
*/

package java.util;

public abstract class AbstractSequentialList extends AbstractList {

// All iterators of this class are fail-fast --> if you override a method of this class
//

  protected AbstractSequentialList() {
  }

  public abstract ListIterator listIterator(int index);

  public Object get(int index) {
  	if (index >= size() || index < 0) {
  		throw new IndexOutOfBoundsException();
  	}
  	ListIterator lit = listIterator(index);
  	//System.out.println("get in abstractSeq "+index);
  	//System.out.println("returning from get in abstractSeq "+index);
  	return lit.next();
  }

  public Object set(int index, Object element)
    throws UnsupportedOperationException, ClassCastException, IllegalArgumentException
   {
  	if (index >= size() || index < 0) {
  		throw new IndexOutOfBoundsException();
  	}
  	ListIterator lit = listIterator(index);
  	Object o = lit.next();
  	lit.set(element);
  	return o;
  }

  public Object remove(int index) throws UnsupportedOperationException {
  	if (index >= size() || index < 0) {
  		throw new IndexOutOfBoundsException();
  	}
  	ListIterator lit = listIterator(index);
  	Object o = lit.next();
  	lit.remove();
  	return o;
  }

  public void add(int index, Object element)
    throws UnsupportedOperationException, ClassCastException, IllegalArgumentException   {
  	if (index > size() || index < 0) {
  		throw new IndexOutOfBoundsException();
  	}
  	ListIterator lit = listIterator(index);
  	lit.add(element);
  }

  public boolean addAll(Collection c) {
   	return addAll(size(),c);
  }

  public boolean addAll(int index, Collection c) {
  	if (index > size() || index < 0) {
  		throw new IndexOutOfBoundsException();
  	}
  	if (c.size() == 0) {
  		return false;
  	}
  	Iterator it = c.iterator();
  	ListIterator lit = listIterator(index);
  	while (it.hasNext()) {
  		lit.add(it.next());
  	}
  	return true;
  }

  public Iterator iterator() {
    return listIterator(0);
  }
}

