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
