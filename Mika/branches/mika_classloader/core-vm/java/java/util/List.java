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
** $Id: List.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.util;

public interface List extends Collection {

  public int size();
  public boolean isEmpty();
  public boolean contains(Object o);
  public Iterator iterator();

  public Object[] toArray();
  public Object[] toArray(Object[] a) throws ArrayStoreException;

  public boolean add(Object o) throws ClassCastException, IllegalArgumentException, UnsupportedOperationException;
  public boolean remove(Object o);

  public boolean containsAll (Collection c) throws ConcurrentModificationException;
  public boolean addAll (Collection c) throws ClassCastException, IllegalArgumentException, IndexOutOfBoundsException,
  	NullPointerException, UnsupportedOperationException, ConcurrentModificationException;
  public boolean addAll (int index, Collection c) throws ClassCastException, IllegalArgumentException, IndexOutOfBoundsException,
  	NullPointerException, UnsupportedOperationException, ConcurrentModificationException;
  public boolean removeAll (Collection c) throws UnsupportedOperationException, ConcurrentModificationException;
  public boolean retainAll (Collection c) throws UnsupportedOperationException, ConcurrentModificationException;

  public void clear() throws UnsupportedOperationException;
  public boolean equals(Object o);
  public int hashCode();      

  public Object get(int index) throws IndexOutOfBoundsException;
  public Object set(int index, Object element) throws ClassCastException, IllegalArgumentException,
  	IndexOutOfBoundsException, UnsupportedOperationException;
  public void add(int index, Object element)throws ClassCastException, IllegalArgumentException, IndexOutOfBoundsException,
  	UnsupportedOperationException;
  public Object remove(int index)throws IndexOutOfBoundsException, UnsupportedOperationException;

  public int indexOf(Object o);
  public int lastIndexOf(Object o);

  public ListIterator listIterator();
  public ListIterator listIterator(int index) throws IndexOutOfBoundsException;

  public List subList(int fromIndex, int toIndex) throws IndexOutOfBoundsException;
}
