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


/**
 * $Id: IntArrayList.java,v 1.2 2006/04/18 13:00:29 cvs Exp $
 */

package wonka.vm;

public class IntArrayList {

  private int[] elements;
  private int size;
  private static final int defaultCapacity = 8;

  public IntArrayList() {
    this(defaultCapacity);
  }

  public IntArrayList(int initialCapacity) {
    this.elements = new int[initialCapacity];
  }

  public void ensureCapacity(int minCapacity) {
    if (this.elements.length < minCapacity) {
      int[] oldElements = this.elements;
      int oldlength = this.size;
      minCapacity = (minCapacity > elements.length * 2 ? minCapacity : elements.length * 2);
      this.elements = new int[minCapacity];
      System.arraycopy(oldElements, 0, this.elements, 0, oldlength);
    }
  }

  public int size() {
    return this.size;
  }

  public boolean isEmpty() {
    return (size == 0);
  }

  public boolean contains(int elem) {
  	for (int i=0 ; i < size ; i++) {	
        if (elements[i]==elem) return true;
    }
    return false;
  }

  public int indexOf (int elem) {
  	for (int i=0; i < size ; i++) {	
	     if (elements[i]==elem) return i;
    }
    return -1;
  }

  public int lastIndexOf (int elem) {
    for (int i=size-1; i >= 0 ; i--) {	
	   if (elements[i]==elem) return i;
    }
    return -1;
  }

  public int[] toArray() {
    int[] answer = new int[this.size];
    System.arraycopy(this.elements, 0, answer, 0, this.size);
    return answer;
  }


  public int get(int index) {
    if (index >= size) throw new IndexOutOfBoundsException();
    // if index < 0 interpreter.c will throw the exception
    return this.elements[index];
  }

  public int set (int index, int element) {
    if (index >= size) throw new IndexOutOfBoundsException();
    // if index < 0 interpreter.c will throw the exception
    int answer = this.elements[index];
    this.elements[index] = element;
    return answer;
  }

  public boolean add (int o) {
    ensureCapacity(this.size+1);
    int position = this.size;
    this.elements[position] = o;
    this.size++;
    return true;
  }

  public boolean add (int[] array) {
    ensureCapacity(this.size+array.length);
    System.arraycopy(array, 0, elements, size, array.length);
    size += array.length;
    return true;
  }


  public void add (int index, int element) {
    if ((index <0) || (index > size)) {
      throw new IndexOutOfBoundsException("Asked index "+index+" in array of size "+size);
    }
    int todo = this.size-index;
    ensureCapacity(this.size+1);
    System.arraycopy(this.elements, index, this.elements, index+1, todo);
    this.elements[index] = element;
    this.size++;
  }

  public int remove (int index) {
    int answer;
    if ((index <0) || (index > size)) {
      throw new IndexOutOfBoundsException("Asked index "+index+" in array of size "+size);
    }
    answer = this.elements[index];
    int todo = size-index-1;
    System.arraycopy(this.elements, index+1, this.elements, index, todo);
    size--;
    return answer;
  }

  public void clear() {
    this.size = 0;
  }

  /**
  ** values between size and newSize are NOT cleared and could contains values other then '0'
  */
  public void setSize(int newSize){
    if(newSize < 0){
      throw new ArrayIndexOutOfBoundsException();
    }
    ensureCapacity(newSize);
    this.size = newSize;
  }

/**
**  removeRange doesn't have to be overwritten ...
**  (but is allowed !)
*/
  public void removeRange (int fromIndex, int toIndex) {
    if (fromIndex > toIndex) throw new IndexOutOfBoundsException();
    if(toIndex != size) {
    System.arraycopy(this.elements, toIndex, this.elements, fromIndex, size-toIndex);
    }
    size -= (toIndex - fromIndex);
  }
}
