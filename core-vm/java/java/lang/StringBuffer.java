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
** $Id: StringBuffer.java,v 1.6 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.lang;

public final class StringBuffer implements java.io.Serializable, CharSequence {

  private static final long serialVersionUID = 3388685877147921107L;

  char[] value;
  int count;
  //private boolean shared;

  public StringBuffer() {
    value = new char[16];
    count = 0;
  }

  public StringBuffer(int length) throws NegativeArraySizeException {
    value = new char[length];
    count = 0;
  }

  native private void createFromString(String string);
  
  public StringBuffer(String string) {
    createFromString(string);
  }

  public int length() {
    return count;
  }

  public int capacity() {
    return value.length;
  }

  native public void ensureCapacity(int minimumCapacity);

  public synchronized void setLength(int newLength) throws StringIndexOutOfBoundsException {
    if (newLength < 0) {
      throw new IndexOutOfBoundsException();
    }
    else if (newLength > count) {
      ensureCapacity(newLength);
      for (int i = count; i < newLength; ++i) {
        value[i] = (char)0;
      }
    }
    count = newLength;
  }


  public char charAt(int index) throws IndexOutOfBoundsException {
    if (index >= count) throw new IndexOutOfBoundsException();
    return value[index];
  }

  public void getChars(int srcOffset, int srcEnd, char[] dst, int dstOffset) throws IndexOutOfBoundsException {
    if (srcEnd > count) {
      throw new IndexOutOfBoundsException();
    }
    System.arraycopy(value, srcOffset, dst, dstOffset, srcEnd - srcOffset);
  }

  public void setCharAt(int index, char ch) throws IndexOutOfBoundsException {
    if (index < 0 || index >= count) {
      throw new IndexOutOfBoundsException();
    }
    value[index] = ch;
  }

  public StringBuffer append(Object obj) {
    if (obj == null) {
      return this.append("null");
    }
    return append(obj.toString());
  }

  native public synchronized StringBuffer append(String s);

  native public synchronized StringBuffer append(char c);

  public StringBuffer append(char[] str) throws NullPointerException  {
    return this.append(new String(str));
  }

  public synchronized StringBuffer append(char[] str, int offset, int len) throws NullPointerException, StringIndexOutOfBoundsException {
    return this.append(new String(str,offset,len));
  }

  public StringBuffer append(boolean b) {
    return this.append(String.valueOf(b));
  }

  public StringBuffer append(int i) {
    return this.append(String.valueOf(i));
  }

  public StringBuffer append(long l) {
    return this.append(String.valueOf(l));
  }

  public StringBuffer append(float f) {
    return this.append(String.valueOf(f));
  }

  public StringBuffer append(double d) {
    return this.append(String.valueOf(d));
  }
  
  public synchronized StringBuffer append(StringBuffer buf) {
    int len = buf.count;
    this.ensureCapacity(count + len);
    System.arraycopy(buf.value,0, value,count,len);
    count += len;
    return this;
  }

  public StringBuffer deleteCharAt(int index) throws StringIndexOutOfBoundsException {
    if (index < 0 || index >= count) {
      throw new StringIndexOutOfBoundsException();
    }
    if (index < --count) {
      System.arraycopy(value, index + 1, value, index, count - index);
    }

    return this;
  }

  public synchronized StringBuffer replace(int start, int end, String str) throws StringIndexOutOfBoundsException {

    if (str == null) {
      throw new NullPointerException();
    }

    if (end > count) {
      end = count;
    }

    if (start < 0 || start >= count || start > end) {
      throw new StringIndexOutOfBoundsException();
    }

    int l = count - end;
    char[] temp = null;
    if (l > 0) {
      temp = new char[l];
      getChars(end, count, temp, 0);
    }
    count = start;
    append(str);
    if (l > 0) {
      append(temp, 0, l);
    }

    return this;
  }

  public String substring(int start) {
    return this.substring(start, this.count);
  }

  native synchronized public String substring(int start, int end) throws StringIndexOutOfBoundsException;
  
  public int indexOf(String string) {
    return indexOf(string, 0);
  }
  
  public int indexOf(String string, int index) {    
    return toString().indexOf(string, index);
  }
  
  public int lastIndexOf(String string) {
    return lastIndexOf(string, count);
  }

  public int lastIndexOf(String string, int index) {    
    return toString().lastIndexOf(string, index);
  }

  public synchronized StringBuffer insert(int offset, String str) throws StringIndexOutOfBoundsException {
    if (str == null) {
      str = "null";
    }

    if (offset < 0 || offset > count) {
      throw new StringIndexOutOfBoundsException("offset "+offset+" into StringBuffer of length "+count);
    }

    int l = count - offset;
    char[] temp = null;
    if (l > 0) {
      temp = new char[l];
      getChars(offset, count, temp, 0);
    }
    count = offset;
    append(str);
    if (l > 0) {
      append(temp, 0, l);
    }

    return this;
  }

  public StringBuffer insert(int offset, Object obj) throws StringIndexOutOfBoundsException {
    return this.insert(offset, String.valueOf(obj));
  }

  public StringBuffer insert(int offset, char[] str, int stroffset, int strlen) throws NullPointerException, StringIndexOutOfBoundsException {
    return this.insert(offset, new String(str,stroffset,strlen));
  }

  public StringBuffer insert(int offset, char[] str) throws NullPointerException, StringIndexOutOfBoundsException {
    return this.insert(offset, new String(str));
  }

  public StringBuffer insert(int offset, boolean b) throws StringIndexOutOfBoundsException {
    return this.insert(offset, String.valueOf(b));
  }

  public StringBuffer insert(int offset, char c) throws StringIndexOutOfBoundsException {
    return this.insert(offset, String.valueOf(c));
  }

  public StringBuffer insert(int offset, int i) throws StringIndexOutOfBoundsException {
    return this.insert(offset, String.valueOf(i));
  }

  public StringBuffer insert(int offset, long l) throws StringIndexOutOfBoundsException {
    return this.insert(offset, String.valueOf(l));
  }

  public StringBuffer insert(int offset, float f) throws StringIndexOutOfBoundsException {
    return this.insert(offset, String.valueOf(f));
  }

  public StringBuffer insert(int offset, double d) throws StringIndexOutOfBoundsException {
    return this.insert(offset, String.valueOf(d));
  }

  public synchronized StringBuffer reverse() {
    char temp;
    int i = 0;
    int j = count - 1;
    while (j > i) {
      temp = value[i];
      value[i] = value[j];
      value[j] = temp;
      ++i;
      --j;
    }

    return this;
  }

  public native String toString();

  public synchronized StringBuffer delete(int start, int end) {
    if (start < 0 || start > count || start > end) {
      throw new StringIndexOutOfBoundsException();
    }
    if (end >= count) {
      end = count;
    }
    int l = end - start;
    count -= l;
    if (start < count) {
      System.arraycopy(value, start + l, value, start, count - start);
    }

    return this;
  }

  public CharSequence subSequence(int start, int end) {    
    return this.substring(start, end);
  }
}

