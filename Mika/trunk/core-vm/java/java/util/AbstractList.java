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
** $Id: AbstractList.java,v 1.3 2006/03/29 09:27:14 cvs Exp $
*/

package java.util;

public abstract class AbstractList extends AbstractCollection implements List {

  protected transient int modCount = 0;

  protected AbstractList() {
  }

  public abstract Object get(int index);

  public Object set(int index, Object element) 
    throws UnsupportedOperationException, ClassCastException, IllegalArgumentException
   {
    throw new UnsupportedOperationException("method set from AbtstractList, called by "+this.getClass());
  }

  public Object remove(int index) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("method remove from AbtstractList, called by "+this.getClass());
  }

  public void add(int index, Object Element) 
    throws UnsupportedOperationException, ClassCastException, IllegalArgumentException
   {
    throw new UnsupportedOperationException("method add from AbtstractList, called by "+this.getClass());
  }
  public boolean add(Object o) throws UnsupportedOperationException,
      ClassCastException, IllegalArgumentException {
    add(size(),o);
    return true;
  }

  public boolean addAll(int index, Collection c) {
    boolean changedList = false;
    Iterator it = c.iterator();

    while (it.hasNext()) {
      add(index++,it.next());
      changedList = true;
    }
    return changedList; 
  }

  public int indexOf(Object o) throws UnsupportedOperationException {
    Iterator it;
    int i;

    it = iterator();
    i = 0;
    if (o==null) {
      while (it.hasNext()) {

        if (it.next()==null) return i;

        ++i;
      }
    }
    else {
      while (it.hasNext()) {

        if (o.equals(it.next())) return i;

        ++i;
      }
    }

    return -1;
  }

  public int hashCode() {
   	int hash=1;
   	Object o;
   	Iterator it = iterator();
   	while (it.hasNext()) {
   		o = it.next();
   		hash = hash * 31 + (o==null ? 0 : o.hashCode());
   	}
   	return hash;	
  }

  public boolean equals(Object o) {
  	if (!(o instanceof List)) return false;
  	List l = (List) o;
  	if ( l.size() != size()) return false;
  	//save time if different size !
  	int size = size();
  	for (int i=0 ; i < size ; i++) {
  	 	if (!(get(i) == null ? l.get(i)==null : get(i).equals(l.get(i))))
  	 		return false;
  	}
   	return true;
  }

  public int lastIndexOf(Object o) throws UnsupportedOperationException {
    ListIterator lit;
    int i;

    i = size();
    lit = listIterator(i);
    if (o==null) {
      while (lit.hasPrevious()) {
        --i;
        if (lit.previous()==null) return i;
       }
    }
    else {
      while (lit.hasPrevious()) {
        --i;
        if (o.equals(lit.previous())) return i;
      }
    }
    return -1;
  }

  public void clear() {
   	removeRange(0, size());
  }

  protected void removeRange(int frix, int toix) {
  	if (frix < 0) throw new IndexOutOfBoundsException("starting index < 0");
  	try {
  	    while ( frix < toix ) {
  		remove(frix);
  		toix--;
  	    }
  	}
  	catch(IndexOutOfBoundsException ioobe) {
  		throw new NoSuchElementException("reached the end of the list --> stopped removing");
  	}
  }

  private class _Iterator implements Iterator {
    private int i, m,status;

    public _Iterator() {
      i = 0;
      m = modCount;
      status=0;
    }

    public boolean hasNext() {
      return i<size();

    }

    public Object next() throws UnsupportedOperationException {
      if (modCount!=m) throw new ConcurrentModificationException();
      if (i>=size()) throw new NoSuchElementException("Sorry no more Elements left");
      status=1;
      Object o = get(i++);
      if (modCount!=m) throw new ConcurrentModificationException("list was co-modified");
//should we check again if the list is changed or not --> might be waste of time
//and throw a ConcurrentModificationException !!!
// we do the checks to be correct and care about performance later ...
      return o;

    }

    public void remove() throws ConcurrentModificationException, IllegalStateException {
      if (modCount!=m) throw new ConcurrentModificationException("in iterator "+this);
      if (status != 1)
        throw new IllegalStateException("remove() must be called after next()");
      AbstractList.this.remove(i-1);
      status = 0;
      m++;
      i--;
      if (modCount!=m) throw new ConcurrentModificationException("the list is co-modified ! while removing");
    }

  }

  public Iterator iterator() {
    return new _Iterator();
  }

  public ListIterator listIterator() throws UnsupportedOperationException {
//    throw new UnsupportedOperationException();
  	return listIterator(0);
  }

  public ListIterator listIterator(int index) throws UnsupportedOperationException {
//    throw new UnsupportedOperationException();
   	return new _ListIterator(index);
  }

  private class _ListIterator implements ListIterator {

    private int current;
    private int status = 1;//nothing
    private int m = modCount;

    _ListIterator() {
      	this(0);
    }
    _ListIterator(int first) {
      current = first;
    }

    public boolean hasNext() {
      return (size() > current);
    }

    public Object next() {
      if (modCount!=m) throw new ConcurrentModificationException();
      if (size() <= current) {
        throw new NoSuchElementException("No next element");
      }
      Object answer = get(current);
      current++;
      status = -1;
      return answer;
    }

    public boolean hasPrevious() {
      return (current > 0);
    }

    public Object previous() {
      if (modCount!=m) throw new ConcurrentModificationException();
      if (current <= 0) {
        throw new NoSuchElementException("No previous element");
      }
      current--;
      status = 0;
      return get(current);
    }

    public int nextIndex() {
      return current;
    }

    public int previousIndex() {
      return current -1;
    }

    public void remove() {
      	if (modCount!=m) throw new ConcurrentModificationException();
    	if ( status == 1 ) throw new IllegalStateException("remove must be called after next or previous");
      AbstractList.this.remove(current+status);
    	if ( status == -1 ) current--;
    	status = 1;
    	m++;
    }

    public void set(Object o) {
        if (modCount!=m) throw new ConcurrentModificationException();
    	if ( status == 1 ) throw new IllegalStateException("set must be called after next or previous");
      AbstractList.this.set(current+status,o);
    	m++;
    }

    public void add(Object o) {
        if (modCount!=m) throw new ConcurrentModificationException();
        AbstractList.this.add(current++,o);
        status = 1;
    	m++;
    }

  }

  public List subList(int fromIndex, int toIndex) throws UnsupportedOperationException {
    if (fromIndex < 0 || toIndex > size()) throw new IndexOutOfBoundsException();
    if (toIndex < fromIndex) throw new IllegalArgumentException();
    	return new SubList(fromIndex , toIndex, modCount,this);
  }


  private class SubList extends AbstractList {
  	private int start;
    private int length;
    private AbstractList backList;
  	
  	public SubList(int from, int to, int mod, AbstractList backing) {
      start = from;
      length = to - from;
      this.modCount=mod;
      backList=backing;
   	}
   	
   	public int size() {
   	 	if (this.modCount != backList.modCount) throw new ConcurrentModificationException();
   	 	return length;
   	} 	
   	public Object get(int pos) {
   	 	if ( pos < 0 || pos >= length )  throw new IndexOutOfBoundsException("in SubList from AbstractList method get");
   	 	if (this.modCount != backList.modCount) throw new ConcurrentModificationException();
   	 	return backList.get(start+pos);
   	}
   	public Object set(int pos, Object o) {
   	 	if ( pos < 0 || pos >= length )  throw new IndexOutOfBoundsException("in SubList from AbstractList method set");
   	 	if (this.modCount != backList.modCount) throw new ConcurrentModificationException();
   	 	return backList.set(start+pos,o);
   	}
	public void add(int pos, Object o) {   	
   	 	if ( pos < 0 || pos > length )  throw new IndexOutOfBoundsException("in SubList from AbstractList method add");
   	 	if (this.modCount++ != backList.modCount) throw new ConcurrentModificationException();
   	 	length++;
   	 	backList.add(start+pos, o);
   	}
   	public boolean addAll(int pos, Collection c) {
                if (c.isEmpty()) return false;
    		Iterator it = c.iterator();
                while (it.hasNext()) {
      			add(pos++,it.next());
    		}
    		return true;
   	}
   	public ListIterator listIterator(int idx) {
   	 	if (this.modCount != backList.modCount) throw new ConcurrentModificationException();
   	 	return new SubListListIterator(idx,start,length);
   	}
   	
   	public Iterator iterator() {
   	 	if (this.modCount != backList.modCount) throw new ConcurrentModificationException();
   		return new SubListListIterator(0,start,length);
   	}
   	public Object remove(int pos) {
   	 	if ( pos < 0 || pos >= length )  throw new IndexOutOfBoundsException("in SubList from AbstractList method remove");
   	 	if (this.modCount++ != backList.modCount) throw new ConcurrentModificationException();
   	 	length--;
   	 	return backList.remove(start+pos);
   	}
   	protected void removeRange(int f, int t) {
  		if (f < 0) throw new IndexOutOfBoundsException("starting index < 0");
  		try {
  	    		while ( f < t ) {
  			remove(f);
  			t--;
  	    		}
  		}
  		catch(IndexOutOfBoundsException ioobe) {
  			throw new NoSuchElementException("reached the end of the list --> stopped removing");
  		}	
   	}  	
  }

  private class SubListListIterator implements ListIterator {
    private int current;
    private int status = 0;
    private int m = modCount;
    private int size;
    private int start;
    SubListListIterator(int start,int length) {
      	this(0,start,length);
    }
    SubListListIterator(int first, int startidx, int length) {
      current = first;
      start = startidx;
      size=length;
    }

    public boolean hasNext() {
      return (size > current);
    }

    public Object next() {
      if (modCount!=m) throw new ConcurrentModificationException();
      if (size <= current) {
        throw new NoSuchElementException("No next element");
      }
      Object answer = get(current);
      current++;
      status = -1;
      return answer;
    }

    public boolean hasPrevious() {
      return (current  > 0);
    }

    public Object previous() {
      if (modCount!=m) throw new ConcurrentModificationException();
      if (current <= 0) {
        throw new NoSuchElementException("No previous element");
      }
      current--;
      status = +1;
      return get(current);
    }

    public int nextIndex() {
      return current;
    }

    public int previousIndex() {
      return (current - 1);
    }

    public void remove() {
      	if (modCount!=m) throw new ConcurrentModificationException();
    	if ( status == 0 ) throw new IllegalStateException("remove must be called after next or previous");
      AbstractList.this.remove(start+current+status);
    	if ( status == -1 ) current--;
    	status = 0;
    	size--;
    	m++;
    }

    public void set(Object o) {
      if (modCount!=m) throw new ConcurrentModificationException();
    	if ( status == 0 ) throw new IllegalStateException("set must be called after next or previous");
      AbstractList.this.set(start+current+status,o);
    	m++;
    }

    public void add(Object o) {
        if (modCount!=m) throw new ConcurrentModificationException();
      	if (status == 1) current++;
        AbstractList.this.add(start+current,o);
        status = 0;
    	m++;
    	size++;
    }

  }



}

