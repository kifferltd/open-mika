/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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
