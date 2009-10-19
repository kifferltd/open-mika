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
    StringBuffer buffer = new StringBuffer("[");
    Iterator iter = iterator();
    while (iter.hasNext()) {
      Object element = iter.next();
      buffer.append(element == this ? 
          "(this Collection)" : String.valueOf(element)).append(", ");
      
    }
    int length = buffer.length();
    if(length > 1) {
      buffer.setLength(length - 2);
      buffer.append(']');
    }
    return buffer.toString();
  }
}
