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
** $Id: ListIterator.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.util;

public interface ListIterator extends Iterator {

  public boolean hasNext();
  public Object next() throws NoSuchElementException,ConcurrentModificationException;

  public boolean hasPrevious();
  public Object previous() throws NoSuchElementException,ConcurrentModificationException;

  public int nextIndex();
  public int previousIndex();

  public void remove() throws UnsupportedOperationException, IllegalStateException,ConcurrentModificationException;

  public void set(Object o) throws UnsupportedOperationException, ClassCastException, ConcurrentModificationException,
  	IllegalArgumentException, IllegalStateException;
  public void add(Object o)
  throws UnsupportedOperationException, ClassCastException, IllegalArgumentException, ConcurrentModificationException;
}
