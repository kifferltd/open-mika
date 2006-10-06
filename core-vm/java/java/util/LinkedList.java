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
** $Id: LinkedList.java,v 1.3 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.util;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;

public class LinkedList extends AbstractSequentialList implements List, Cloneable, Serializable {

// All iterators of this class are fail-fast --> if you override a method of this class
  private static final long serialVersionUID = 876323262645176354L;

  transient int size;
  private transient Link first;
  private transient Link stop;

  public LinkedList() {
    setup();
  }

  public LinkedList(Collection c) {
  	setup();
  	addAll(c);
  }

  public boolean add(Object e) {
    modCount++;
    Link l = new Link(stop.before, stop, e);
    l.before.after = l;
    stop.before = l;
    size++;
    return true;
  }
  
  public void addLast(Object e){
   	add(e);
  }

  public void addFirst(Object e) {
    modCount++;
    Link l = new Link(first, first.after, e);
    l.after.before = l;
    first.after = l;
    size++;
  }

  public void clear () {
  	modCount++;
   	size = 0;
   	first.after=stop;
   	stop.before=first;
  }

  public Object getFirst() {
   	if (size==0) {
   	 	throw new NoSuchElementException();
   	}
   	return first.after.data;
  }

  public Object getLast() {
   	if (size==0) {
   	 	throw new NoSuchElementException();
   	}
   	return stop.before.data;
  }

  public Object removeFirst() {
    if (size==0) {
      throw new NoSuchElementException();
    }
    Link l = first.after;
    Object o = l.data;
    modCount++;
    first.after = l.after;
    l.after.before=first;
    size--;
    return o;
  }

  public Object removeLast() {
    if (size==0) {
      throw new NoSuchElementException();
    }
    Link last = stop.before;
    Object o = last.data;
    modCount++;
    last.before.after = stop;
    stop.before = last.before;
    size--;
    return o;
  }

  public boolean addAll(Collection c){
    int sz = c.size();
    if (sz == 0 ) {
      return false;
    }
    Iterator it = c.iterator();
    modCount++;
    size += sz;
    Link l = stop.before;
    while (it.hasNext()) {
      l.after = new Link(l , stop, it.next());
      l = l.after;
    }
    stop.before = l;
    return true;
  }

/**
** we have this method so we can optimize the search procedures ...
** if position < size() / 2 then we start browsing with first else we use last
** to start with ...
*/
  Link findLink(int position) {
    Link l=null;
    //System.out.println("calling findLink() -->"+position+", size is "+size+" last ="+last);
    if (position < size/2+1) {
      // lookup using first Link as start ...

      l = first.after;
      while (position-- > 0) {
        l = l.after;
      }
    }
    else  {
      // lookup using last Link as start ...
      l = stop;
      while (position++ < size) {
        //System.out.println("calling findLink() -->"+position+", size is "+size+", l ="+l+" -->"+l.before);
        l = l.before;
      }
    }
    //if (l != null) System.out.println("returning findLink() -->"+l+", before "+l.before+", after "+l.after+", data "+l.data+", pos "+position);
    return l;
  }

  public boolean addAll(int idx, Collection c){
    // it is possible this method is called on an empty LinkedList
    // we don't do checks to prevent accessing first and last (which
    // are null at that point) since that case is delegated to addAll(Collection c)
    if (idx < 0 || idx > size) {
      throw new IndexOutOfBoundsException();
    }
    int sz = c.size(); //this will trigger the NullPointerException !
    if (sz == 0 ) {
      return false;
    }
    Iterator it = c.iterator();
    modCount++;
    Link next=stop;
    if (idx != size) {
      next = findLink(idx);	
    }
    Link l = next.before;
    size += sz;
    while (it.hasNext()) {
      l.after = new Link(l , next, it.next());
      l = l.after;
    }
    next.before = l;
    return true;
  }

  public void add(int idx, Object e){
    if (idx < 0 || idx > size) {
      throw new IndexOutOfBoundsException();
    }
    Link l = stop;
    if (idx < size) {
      l = findLink(idx);
    }
    modCount++;
    size++;
    l = new Link (l.before, l, e);
    l.before.after = l;
    l.after.before = l;
  }

  public Object remove(int idx){
    if (idx < 0 || idx >= size) {
      throw new IndexOutOfBoundsException();
    }
    Link l = findLink(idx);
    modCount++;
    size--;
    l.before.after = l.after;
    l.after.before = l.before;
    return l.data;
  }

  public Object set(int idx, Object e){
    if (idx < 0 || idx >= size) {
      throw new IndexOutOfBoundsException();
    }
    Link l = findLink(idx);
    Object d = l.data;
    l.data = e;
    return d;
  }

  public boolean contains(Object o) {
    Link l = first.after;
    if (o==null) {
      for (int i=0; i < size; i++){
        if (l.data == null) {
          return true;
        }
        l = l.after;
      }  	  			  			  			  			  			  			  	
    }
    else {
      for (int i=0; i < size; i++){
        if (o.equals(l.data)) {
          return true;
        }
        l = l.after;
      }
    }
    return false;
  }

  public int indexOf(Object o) {
    Link l = first.after;
    if (o==null) {
      for (int i=0; i < size; i++){
        if (l.data == null) {
          return i;
        }
        l = l.after;
      }  	  			  			  			  			  			  			  	
    }
    else {
      for (int i=0; i < size; i++){
        if (o.equals(l.data)) {
          return i;
        }
        l = l.after;
      }
    }
    return -1;
  }

  public int lastIndexOf(Object o){
    Link l = stop.before;
    if (o==null) {
      for (int i=size-1; i >= 0; i--){
        if (l.data == null) {
          return i;
        }
        l = l.before;
      }  	  			  			  			  			  			  			  	
    }
    else {
      for (int i=size-1; i >= 0; i--){
        if (o.equals(l.data)) {
          return i;
        }
        l = l.before;
      }
    }
    return -1;
  }

  public boolean remove(Object e) {
    Link l = first.after;
    if (e==null) {
      for (int i=0; i < size; i++){
        if (l.data == null) {
          l.before.after = l.after;
          l.after.before = l.before;
          size--;
          return true;
        }
        l = l.after;
      }  	  			  			  			  			  			  			  	
    }
    else {
      for (int i=0; i < size; i++){
        if (e.equals(l.data)) {
          l.before.after = l.after;
          l.after.before = l.before;
          size--;
          return true;
        }
        l = l.after;
      }
    }
    return false;
  }
  
  public Object[] toArray(){
    Object [] array = new Object[size];
    Link l = first.after;
    for (int i=0 ; i < size; i++) {
      array[i] = l.data;
      l = l.after;
    }
    return array;
  }

  public Object[] toArray(Object[] arr){
    Object [] array = arr;
    if ( array.length < size ) array = (Object[])Array.newInstance(arr.getClass().getComponentType(),size);
    if ( array.length > size ) array[size] = null ;
    Link l = first.after;
    for (int i=0; i < size ; i++) {
      array[i] = l.data;
      l = l.after;
    }
    return  array;
  }

  public Object clone() {
    LinkedList ll = null;
    try {
      ll = (LinkedList) super.clone();
    }
    catch(CloneNotSupportedException cnse) {}
    ll.size = 0;
    ll.setup();
    ll.addAll(this);
    return ll;
  }

  public int size() {
    return size;
  }

  public ListIterator listIterator(int i) {
    //System.out.println("calling ListIterator in LinkedList");
    if ( i < 0 || i >size ) {
      throw new IndexOutOfBoundsException();
    }
    return new LListIterator(i);
  }

  private class LListIterator implements ListIterator {

    private int index;
    private int status = 0;
    private int m = modCount;
    private Link link;

    public LListIterator(int idx) {
      index = idx;
      link = findLink(idx);
    }

    public Object next() {
      if (modCount!=m) throw new ConcurrentModificationException();
      if (size <= index) {
        throw new NoSuchElementException("No next element");
      }
      Object answer = link.data;
      index++;
      link = link.after;
      status = -1;
      return answer;     		
    }

    public Object previous() {
      if (modCount!=m) throw new ConcurrentModificationException();
      if (index <= 0) {
        throw new NoSuchElementException("No previous element");
      }
      //System.out.println("calling previous() -->"+link.data);
      index--;
      status = +1;
      link = link.before;
      Object answer = link.data;
      //System.out.println("calling previous() "+answer+"--> at "+(index+1)+" and size "+size);
      return answer;

    }

    public boolean hasNext() {
      return index < size;
    }

    public boolean hasPrevious() {
      return index > 0;
    }

    public int nextIndex() {
      return index;
    }

    public int previousIndex() {
      return index-1;
    }

    public void add(Object e) {
      if (modCount!=m) {	
        throw new ConcurrentModificationException();
      }
      status = 0;
      m++;
      Link l = new Link (link.before, link, e);
      link.before = l;
      l.before.after = l;          	
      modCount++;
      size++;
      index++;       	
      if (modCount!=m) {	
        throw new ConcurrentModificationException("warning: the original LinkedList might be corrupted");
      }
    }

    public void remove(){
      if (modCount!=m) {
        throw new ConcurrentModificationException();
      }
      if ( status == 0 ) throw new IllegalStateException("remove must be called after next or previous");
      if ( status == -1 ) {//remove after a next: shift cursor one back
        index--;
        link = link.before;	
      }
      status = 0;
      m++;
      modCount++;
      size--;
      Link l = link.after;
      l.before = link.before;
      link.before.after = l;
      link = l;
      if (modCount!=m) {
        throw new ConcurrentModificationException("warning: the original LinkedList might be corrupted");
      }
    }

    public void set(Object e) {
      if (modCount!=m) {
        throw new ConcurrentModificationException();
      }
      if ( status == 0 ) throw new IllegalStateException("set must be called after next or previous");
      m++;
      modCount++;
      if (status == -1) {
        link.before.data = e;
      }
      else {
        link.data = e;  	
      }
      if (modCount!=m) {	
        throw new ConcurrentModificationException();
      }
    }
  }

  private class Link {
    public Link before;
    public Link after;
    public Object data;

    public Link (Link f, Link a , Object d) {
      before = f;
      after = a;
      data = d;   	
    }
    public Link(Object d) {
      before = null;
      after = null;
      data = d;   	
    }
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    setup();
    int s = in.readInt();
    for(int i = 0 ; i < s ; i++){
      System.out.println("LinkedList.readObject()");
      add(in.readObject());
    }
  }

  private void setup() {
    stop = new Link( null, null, "BUG: the LinkedList got screwed up! --> last element is bad");
    first = new Link(null, stop, "BUG: the LinkedList got screwed up! --> first element is bad");
    stop.after=stop;
    stop.before=first;
    first.before=first;
    first.after=stop;    
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    int s = size;
    out.writeInt(s);
    Link l = first.after;
    for(int i = 0 ; i < s ; i++){
      out.writeObject(l.data);
      l = l.after;
    }
  }
}

