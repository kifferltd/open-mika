/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2005 by /k/ Embedded Java Solutions.                *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

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

