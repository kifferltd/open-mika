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
** $Id: AbstractCollection.java,v 1.2 2006/02/27 11:46:39 cvs Exp $
*/

package java.util;

public abstract class AbstractCollection implements Collection {

  protected AbstractCollection() {
  }

  public abstract Iterator iterator();

  public abstract int size();

  public boolean isEmpty() {
    return (size() == 0);
  }

  public boolean contains(Object o) throws UnsupportedOperationException {
    Iterator it = iterator();
    if (o==null) {
      while (it.hasNext()) {
        if (it.next()==null) return true;
      }
    }
    else {
      while (it.hasNext()) {
        if (o.equals(it.next())) return true;
      }
    }

    return false;
  }

  public Object[] toArray() {

    Iterator it = iterator();
    Object[] newArray = new Object[size()];
    int counter = 0;

    while (it.hasNext()) {
      newArray[counter++]=it.next();
    }
    return newArray;
  }

  public Object[] toArray(Object[] a) throws NullPointerException, ArrayStoreException {
    if (a.length < size()) {
      Class ctype = a.getClass().getComponentType();
      a = (Object[]) java.lang.reflect.Array.newInstance(ctype,size());
    }
    Object[] b = toArray();
    System.arraycopy(b, 0, a, 0, size());
    if (a.length > size()) {
     	a[size()] = null;
    }
    return a;
  }

  public boolean add(Object o) throws UnsupportedOperationException, NullPointerException, ClassCastException, IllegalArgumentException {
//System.out.println("JV: I need to add something to "+this.getClass().getName());
    throw new UnsupportedOperationException();
  }

  public boolean remove(Object o) throws UnsupportedOperationException {
    Iterator i = iterator();
    Object ito;
    while (i.hasNext()) {
    	ito = i.next();
    	if ((o==null ? ito == null : o.equals(ito))) {
//System.out.println("DEBUG trying remove, o = "+o+" class"+o.getClass()+", ito = "+ito+" from "+ito.getClass());    		
    		i.remove();
    		return true;
    	}
    }
    return false;
  }

  public boolean containsAll(Collection c) {
    Iterator it=c.iterator();
    while (it.hasNext()) {
      if ( !contains(it.next()) )
        return false;
    }
    return true;
  }

  public boolean addAll(Collection c) throws UnsupportedOperationException {
    Iterator it=c.iterator();
    boolean modified=false;
    while (it.hasNext()) {
      if (add(it.next()) )
        modified = true;
    }
    return modified;
  }

  public boolean removeAll(Collection c) throws UnsupportedOperationException{

    Iterator it=c.iterator();
    boolean modified=false;
    Object o;
    while (it.hasNext()) {
    	o = it.next();
     	while  (remove(o)){
           modified = true;
      	}
    }
    return modified;

  }

  public void clear() {
    Iterator it = iterator();
    while (it.hasNext()) {
      it.next();
      it.remove();
    }
  }


  public boolean retainAll(Collection c) throws UnsupportedOperationException {
	Iterator it = iterator();
    	boolean modified=false;
	Object o;
    	
	while (it.hasNext()) {
        	o = it.next();
        	if (! c.contains(o)) {
        	 	it.remove();
        	        modified=true;
        	} 	
	
	}
    	return modified;
  }


  public String toString() {

    String newString = "[";
    Iterator it = iterator();

    while (it.hasNext()) {
      if (newString.length()>1)
        newString = newString + ", ";
      newString = newString + String.valueOf(it.next());
    }

    newString = newString + "]";

    return newString;
  }
}
