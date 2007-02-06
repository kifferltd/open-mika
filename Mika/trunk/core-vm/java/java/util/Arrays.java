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
** $Id: Arrays.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
*/

package java.util;

import java.io.Serializable;

public class Arrays {

  private Arrays() {}
  
  public static List asList(Object[] arr){
   	if (arr == null){
   	 	throw new NullPointerException();
   	}
   	return new AsList(arr);   	
  }

  private static class AsList extends AbstractList 
         implements RandomAccess, Serializable{
    
   	private Object []  back;
   	
   	public AsList(Object [] arr){
   	       	back = arr;
   	}
   	
   	public Object set(int idx, Object e){
   	 	Object old = back[idx];
   	 	back[idx] = e;
   	 	return old;
   	}
   	public int size(){
   	 	return back.length;
   	}
   	public Object get(int idx){
   	 	return back[idx];
   	}
  }


// binarySearch methods ...
  public static int binarySearch(byte[] arr, byte e){
    int size = arr.length;
    if(size == 0) {
      return -1;
    }
    int low = -1;
    int high = size;
    int index = high/2;
    int b;
    do {
      b = arr[index];
      if(b == e) {
        return index;          
      }
      if(b > e) {
        high = index;
      } else {
        low = index;
      }
      int nindex = (high + low) / 2;
      if(low >= nindex || nindex >= high) {
        break;
      }
      index = nindex;
    } while(true);
    return (e > b ? -1 : 0) -1 - index;
  }
  
  public static int binarySearch(char[] arr, char e){
    int size = arr.length;
    if(size == 0) {
      return -1;
    }
    int low = -1;
    int high = size;
    int index = high/2;
    int b;
    do {
      b = arr[index];
      if(b == e) {
        return index;          
      }
      if(b > e) {
        high = index;
      } else {
        low = index;
      }
      int nindex = (high + low) / 2;
      if(low >= nindex || nindex >= high) {
        break;
      }
      index = nindex;
    } while(true);
    return (e > b ? -1 : 0) -1 - index;
  }
    
  
  public static int binarySearch(short[] arr, short e){
    int size = arr.length;
    if(size == 0) {
      return -1;
    }
    int low = -1;
    int high = size;
    int index = high/2;
    int b;
    do {
      b = arr[index];
      if(b == e) {
        return index;          
      }
      if(b > e) {
        high = index;
      } else {
        low = index;
      }
      int nindex = (high + low) / 2;
      if(low >= nindex || nindex >= high) {
        break;
      }
      index = nindex;
    } while(true);
    return (e > b ? -1 : 0) -1 - index;
  }
  
  public static int binarySearch(int[] arr, int e){
    int size = arr.length;
    if(size == 0) {
      return -1;
    }
    int low = -1;
    int high = size;
    int index = high/2;
    int b;
    do {
      b = arr[index];
      if(b == e) {
        return index;          
      }
      if(b > e) {
        high = index;
      } else {
        low = index;
      }
      int nindex = (high + low) / 2;
      if(low >= nindex || nindex >= high) {
        break;
      }
      index = nindex;
    } while(true);
    return (e > b ? -1 : 0) -1 - index;
  }

  public static int binarySearch(long[] arr, long e){
    int size = arr.length;
    if(size == 0) {
      return -1;
    }
    int low = -1;
    int high = size;
    int index = high/2;
    long b;
    do {
      b = arr[index];
      if(b == e) {
        return index;          
      }
      if(b > e) {
        high = index;
      } else {
        low = index;
      }
      int nindex = (high + low) / 2;
      if(low >= nindex || nindex >= high) {
        break;
      }
      index = nindex;
    } while(true);
    return (e > b ? -1 : 0) -1 - index;
  }

  public static int binarySearch(float[] arr, float e){
    int size = arr.length;
    if(size == 0) {
      return -1;
    }
    int low = -1;
    int high = size;
    int index = high/2;
    float b;
    do {
      b = arr[index];
      if(b == e) {
        return index;          
      }
      if(b > e) {
        high = index;
      } else {
        low = index;
      }
      int nindex = (high + low) / 2;
      if(low >= nindex || nindex >= high) {
        break;
      }
      index = nindex;
    } while(true);
    return (e > b ? -1 : 0) -1 - index;
  }

  public static int binarySearch(double[] arr, double e){
    int size = arr.length;
    if(size == 0) {
      return -1;
    }
    int low = -1;
    int high = size;
    int index = high/2;
    double b;
    do {
      b = arr[index];
      if(b == e) {
        return index;          
      }
      if(b > e) {
        high = index;
      } else {
        low = index;
      }
      int nindex = (high + low) / 2;
      if(low >= nindex || nindex >= high) {
        break;
      }
      index = nindex;
    } while(true);
    return (e > b ? -1 : 0) -1 - index;
  }

  public static int binarySearch(Object[] arr, Object e){
		return binarySearch(arr, e, null); 	
  }
  
  public static int binarySearch(Object[] arr, Object e,Comparator comp){
  	if(comp == null) {
     comp = new Collections.StdComparator(); 
    }
    int size = arr.length;
    if(size == 0) {
      return -1;
    }
    int low = -1;
    int high = size;
    int index = high/2;
    int cmp;
    do {
      cmp = comp.compare(arr[index],e);
      if(cmp == 0) {
        return index;          
      }
      if(cmp > 0) {
        high = index;
      } else {
        low = index;
      }
      int nindex = (high + low) / 2;
      if(low >= nindex || nindex >= high) {
        break;
      }
      index = nindex;
    } while(true);
    return (cmp < 0 ? -1 : 0) -1 - index;
  }

// fill methods ...
  public static native void fill(boolean []arr, boolean val);
  
  public static void fill(boolean []arr, int from, int to, boolean val){
   	if (from < 0 || to < 0 ||  from > arr.length || to > arr.length){
   	 	throw new ArrayIndexOutOfBoundsException();
   	}   	
   	if (from > to){
   	 	throw new IllegalArgumentException();
   	}
   	for (int i=from; i < to ; i++){
   	 	arr[i] = val;
   	}
  }
  public static native void fill(byte []arr, byte val);
  
  public static native void fill(byte []arr, int from, int to, byte val);
  
  public static void fill(char []arr, char val){
   	for (int i=0; i < arr.length ; i++){
   	 	arr[i] = val;
   	}
  }
  public static void fill(char []arr, int from, int to, char val){
   	if (from < 0 || to < 0 ||  from > arr.length || to > arr.length){
   	 	throw new ArrayIndexOutOfBoundsException();
   	}   	
   	if (from > to){
   	 	throw new IllegalArgumentException();
   	}
   	for (int i=from; i < to ; i++){
   	 	arr[i] = val;
   	}
  }
  public static void fill(short []arr, short val){
   	for (int i=0; i < arr.length ; i++){
   	 	arr[i] = val;
   	}
  }
  public static void fill(short []arr, int from, int to, short val){
   	if (from < 0 || to < 0 ||  from > arr.length || to > arr.length){
   	 	throw new ArrayIndexOutOfBoundsException();
   	}   	
   	if (from > to){
   	 	throw new IllegalArgumentException();
   	}
   	for (int i=from; i < to ; i++){
   	 	arr[i] = val;
   	}
  }
  public static void fill(int []arr, int val){
   	for (int i=0; i < arr.length ; i++){
   	 	arr[i] = val;
   	}
  }
  public static void fill(int []arr, int from, int to, int val){
   	if (from < 0 || to < 0 ||  from > arr.length || to > arr.length){
   	 	throw new ArrayIndexOutOfBoundsException();
   	}   	
   	if (from > to){
   	 	throw new IllegalArgumentException();
   	}
   	for (int i=from; i < to ; i++){
   	 	arr[i] = val;
   	}
  }
  public static void fill(long []arr, long val){
   	for (int i=0; i < arr.length ; i++){
   	 	arr[i] = val;
   	}
  }
  public static void fill(long []arr, int from, int to, long val){
   	if (from < 0 || to < 0 ||  from > arr.length || to > arr.length){
   	 	throw new ArrayIndexOutOfBoundsException();
   	}   	
   	if (from > to){
   	 	throw new IllegalArgumentException();
   	}
   	for (int i=from; i < to ; i++){
   	 	arr[i] = val;
   	}
  }
  public static void fill(float []arr, float val){
   	for (int i=0; i < arr.length ; i++){
   	 	arr[i] = val;
   	}
  }
  public static void fill(float []arr, int from, int to, float val){
   	if (from < 0 || to < 0 ||  from > arr.length || to > arr.length){
   	 	throw new ArrayIndexOutOfBoundsException();
   	}   	
   	if (from > to){
   	 	throw new IllegalArgumentException();
   	}
   	for (int i=from; i < to ; i++){
   	 	arr[i] = val;
   	}
  }
  public static void fill(double []arr, double val){
   	for (int i=0; i < arr.length ; i++){
   	 	arr[i] = val;
   	}
  }
  public static void fill(double []arr, int from, int to, double val){
   	if (from < 0 || to < 0 ||  from > arr.length || to > arr.length){
   	 	throw new ArrayIndexOutOfBoundsException();
   	}   	
   	if (from > to){
   	 	throw new IllegalArgumentException();
   	}
   	for (int i=from; i < to ; i++){
   	 	arr[i] = val;
   	}
  }
  public static void fill(Object []arr, Object val){
   	for (int i=0; i < arr.length ; i++){
  	 	arr[i] = val;
   	}
  }
  public static void fill(Object []arr, int from, int to, Object val){
   	if (from < 0 || to < 0 ||  from > arr.length || to > arr.length){
   	 	throw new ArrayIndexOutOfBoundsException();
   	}   	
   	if (from > to){
   	 	throw new IllegalArgumentException();
   	}
   	for (int i=from; i < to ; i++){
   	 	arr[i] = val;
   	}
  }

// sort methods ...
  public static void sort(Object[] a, int start, int end, Comparator c){
    if(end < start){
      throw new IllegalArgumentException();
    }
    if(start < 0) {
      throw new ArrayIndexOutOfBoundsException(start);
    }
    if(c == null) {
      c = new Collections.StdComparator();
    }
    quicksort(a,start,end -1, c);
  }

  public static void sort(Object[] a, Comparator c){
    sort(a,0,a.length, c);
  }

  public static void sort (Object[] a) {
    sort(a,0,a.length, null);
  }

  public static void sort(Object[] a, int start, int end){
    sort(a,start,end,null);
  }

  private static void quicksort(Object[] list, int start, int end,Comparator c) {
    int split;
    if (start < end) {
      split = partition (list,start,end,c);
      quicksort (list,start,split-1,c);
      quicksort (list,split+1,end,c);
    }
  }

  private static int partition (Object[] list, int start, int end,Comparator c) {
    Object pivot = list[end];
    int bottom = start -1;
    int top = end;
    boolean done = false;

    while (!done) {
      while (!done) {
        bottom++;
        if (bottom == top) {
          done = true;
          break;
        }
        if (c.compare(list[bottom],pivot) > 0) {
          list[top] = list[bottom];
          break;
        }
      }
      while (!done) {
        top--;
        if (top == bottom) {
          done = true;
          break;
        }
        else {
          if (c.compare(list[top],pivot) < 0) {
            list[bottom] = list[top];
            break;
          }
        }
      }
    }
    list[top] = pivot;
    return top;
  }


   public static void sort(short[] s) {
     quicksort(s,0,s.length-1);
   }

   public static void sort(short[]s, int start, int end) {
     if(end < start){
       throw new IllegalArgumentException();
     }
     if(start < 0) {
       throw new ArrayIndexOutOfBoundsException(start);
     }
     quicksort(s,start,end-1);
   }

   private static void quicksort(short[] list, int start, int end) {
    int split;
    if (start < end) {
      split = partition (list,start,end);
      quicksort (list,start,split-1);
      quicksort (list,split+1,end);
    }
  }

  private static int partition (short[] list, int start, int end) {
    short pivot = list[end];
    int bottom = start -1;
    int top = end;
    boolean done = false;

    while (!done) {
      while (!done) {
        bottom++;
        if (bottom == top) {
          done = true;
          break;
        }
        if (list[bottom]>pivot) {
          list[top] = list[bottom];
          break;
        }
      }
      while (!done) {
        top--;
        if (top == bottom) {
          done = true;
          break;
        }
        else {
          if (list[top]<pivot) {
            list[bottom] = list[top];
            break;
          }
        }
      }
    }
    list[top] = pivot;
    return top;
  }

// equals methods ...
  public static boolean equals(boolean[] a, boolean[] a2) {
    if ( (a == null) && (a2 == null) ) {
      return true;
    }
    if ( ((a == null) && (a2 != null))
      || ((a != null) && (a2 == null)) ) {
      return false;
    }
    if ( a.length != a2.length ) {
      return false;
    }

    for (int i = 0; i < a.length; i++) {
      if ( a[i] != a2[i] ) {
        return false;
      }
    }

    return true;
  }

  public static boolean equals(byte[] a, byte[] a2) {
    if ( (a == null) && (a2 == null) ) {
      return true;
    }
    if ( ((a == null) && (a2 != null))
      || ((a != null) && (a2 == null)) ) {
      return false;
    }
    if ( a.length != a2.length ) {
      return false;
    }

    for (int i = 0; i < a.length; i++) {
      if (a[i] != a2[i]) {
        return false;
      }
    }

    return true;
  }

  public static boolean equals(char[] a, char[] a2) {
    if ( (a == null) && (a2 == null) ) {
      return true;
    }
    if ( ((a == null) && (a2 != null))
      || ((a != null) && (a2 == null)) ) {
      return false;
    }
    if ( a.length != a2.length ) {
      return false;
    }

    for (int i = 0; i < a.length; i++) {
      if (a[i] != a2[i]) {
        return false;
      }
    }

    return true;
  }

  public static boolean equals(double[] a, double[] a2) {
    if ( (a == null) && (a2 == null) ) {
      return true;
    }
    if ( ((a == null) && (a2 != null))
      || ((a != null) && (a2 == null)) ) {
      return false;
    }
    if ( a.length != a2.length ) {
      return false;
    }

    for (int i = 0; i < a.length; i++) {
      if ( ! (new Double(a[i]).equals(new Double(a2[i]))) ) {
        return false;
      }
    }

    return true;
  }

  public static boolean equals(float[] a, float[] a2) {
    if ( (a == null) && (a2 == null) ) {
      return true;
    }
    if ( ((a == null) && (a2 != null))
      || ((a != null) && (a2 == null)) ) {
      return false;
    }
    if ( a.length != a2.length ) {
      return false;
    }

    for (int i = 0; i < a.length; i++) {
      if ( ! (new Float(a[i]).equals(new Float(a2[i]))) ) {
        return false;
      }
    }

    return true;
  }

  public static boolean equals(int[] a, int[] a2) {
    if ( (a == null) && (a2 == null) ) {
      return true;
    }
    if ( ((a == null) && (a2 != null))
      || ((a != null) && (a2 == null)) ) {
      return false;
    }
    if ( a.length != a2.length ) {
      return false;
    }

    for (int i = 0; i < a.length; i++) {
      if ( a[i] != a2[i] ) {
        return false;
      }
    }

    return true;
  }

  public static boolean equals(long[] a, long[] a2) {
    if ( (a == null) && (a2 == null) ) {
      return true;
    }
    if ( ((a == null) && (a2 != null))
      || ((a != null) && (a2 == null)) ) {
      return false;
    }
    if ( a.length != a2.length ) {
      return false;
    }

    for (int i = 0; i < a.length; i++) {
      if ( a[i] != a2[i] ) {
        return false;
      }
    }

    return true;
  }

  public static boolean equals(Object[] a, Object[] a2) {
    if ( (a == null) && (a2 == null) ) {
      return true;
    }
    if ( (a == null) || (a2 == null) ) {
      return false;
    }
    if ( a.length != a2.length ) {
      return false;
    }

    for (int i = 0; i < a.length; i++) {
      if ( ! (a[i]==null ? a2[i]==null : a[i].equals(a2[i])) ) {
        return false;
      }
    }

    return true;
  }

  public static boolean equals(short[] a, short[] a2) {
    if ( (a == null) && (a2 == null) ) {
      return true;
    }
    if ( ((a == null) && (a2 != null))
      || ((a != null) && (a2 == null)) ) {
      return false;
    }
    if ( a.length != a2.length ) {
      return false;
    }

    for (int i = 0; i < a.length; i++) {
      if ( a[i] != a2[i] ) {
        return false;
      }
    }

    return true;
  }

  public static void sort(byte[] s) {
    quicksort(s,0,s.length-1);
  }

  public static void sort(byte[] s, int start, int end) {
    if(end < start){
      throw new IllegalArgumentException();
    } 
    if(start < 0) {
      throw new ArrayIndexOutOfBoundsException(start);
    }
    quicksort(s,start,end-1);
  }

  private static void quicksort(byte[] list, int start, int end) {
    int split;
    if (start < end) {
      split = partition (list,start,end);
      quicksort (list,start,split-1);
      quicksort (list,split+1,end);
    }
  }

  private static int partition (byte[] list, int start, int end) {
    byte pivot = list[end];
    int bottom = start - 1;
    int top = end;
    boolean done = false;

    while (!done) {
      while (!done) {
        bottom++;
        if (bottom == top) {
          done = true;
          break;
        }
        if (list[bottom]>pivot) {
          list[top] = list[bottom];
          break;
        }
      }
      while (!done) {
        top--;
        if (top == bottom) {
          done = true;
          break;
        }
        else {
          if (list[top]<pivot) {
            list[bottom] = list[top];
            break;
          }
        }
      }
    }
    list[top] = pivot;
    return top;
  }

  public static void sort(char[] s) {
    quicksort(s,0,s.length-1);
  }

  public static void sort(char[] s, int start, int end) {
    if(end < start){
      throw new IllegalArgumentException();
    }
    if(start < 0) {
      throw new ArrayIndexOutOfBoundsException(start);
    }
    quicksort(s,start,end-1);
  }

  private static void quicksort(char[] list, int start, int end) {
    int split;
    if (start < end) {
      split = partition (list,start,end);
      quicksort (list,start,split-1);
      quicksort (list,split+1,end);
    }
  }

  private static int partition (char[] list, int start, int end) {
    char pivot = list[end];
    int bottom = start -1;
    int top = end;
    boolean done = false;

    while (!done) {
      while (!done) {
        bottom++;
        if (bottom == top) {
          done = true;
          break;
        }
        if (list[bottom]>pivot) {
          list[top] = list[bottom];
          break;
        }
      }
      while (!done) {
        top--;
        if (top == bottom) {
          done = true;
          break;
        }
        else {
          if (list[top]<pivot) {
            list[bottom] = list[top];
            break;
          }
        }
      }
    }
    list[top] = pivot;
    return top;
  }

  public static void sort(double[] s) {
    quicksort(s,0,s.length-1);
  }

  public static void sort(double[] s, int start, int end) {
    if(end < start){
      throw new IllegalArgumentException();
    }
    if(start < 0) {
      throw new ArrayIndexOutOfBoundsException(start);
    }
    quicksort(s,start,end-1);
  }

  private static void quicksort(double[] list, int start, int end) {
    int split;
    if (start < end) {
      split = partition (list,start,end);
      quicksort (list,start,split-1);
      quicksort (list,split+1,end);
    }
  }

  private static int partition (double[] list, int start, int end) {
    double pivot = list[end];
    int bottom = start -1;
    int top = end;
    boolean done = false;

    while (!done) {
      while (!done) {
        bottom++;
        if (bottom == top) {
          done = true;
          break;
        }
        if (list[bottom]>pivot) {
          list[top] = list[bottom];
          break;
        }
      }
      while (!done) {
        top--;
        if (top == bottom) {
          done = true;
          break;
        }
        else {
          if (list[top]<pivot) {
            list[bottom] = list[top];
            break;
          }
        }
      }
    }
    list[top] = pivot;
    return top;
  }
  public static void sort(float[] s) {
    quicksort(s,0,s.length-1);
  }

  public static void sort(float[] s, int start, int end) {
    if(end < start){
      throw new IllegalArgumentException();
    }
    if(start < 0) {
      throw new ArrayIndexOutOfBoundsException(start);
    }
    quicksort(s,start,end-1);
  }

  private static void quicksort(float[] list, int start, int end) {
    int split;
    if (start < end) {
      split = partition (list,start,end);
      quicksort (list,start,split-1);
      quicksort (list,split+1,end);
    }
  }

  private static int partition (float[] list, int start, int end) {
    float pivot = list[end];
    int bottom = start -1;
    int top = end;
    boolean done = false;

    while (!done) {
      while (!done) {
        bottom++;
        if (bottom == top) {
          done = true;
          break;
        }
        if (list[bottom]>pivot) {
          list[top] = list[bottom];
          break;
        }
      }
      while (!done) {
        top--;
        if (top == bottom) {
          done = true;
          break;
        }
        else {
          if (list[top]<pivot) {
            list[bottom] = list[top];
            break;
          }
        }
      }
    }
    list[top] = pivot;
    return top;
  }
  public static void sort(int[] s) {
    quicksort(s,0,s.length-1);
  }

  public static void sort(int[] s, int start, int end) {
    if(end < start){
      throw new IllegalArgumentException();
    }
    if(start < 0) {
      throw new ArrayIndexOutOfBoundsException(start);
    }
    quicksort(s,start,end-1);
  }


  private static void quicksort(int[] list, int start, int end) {
    int split;
    if (start < end) {
      split = partition (list,start,end);
      quicksort (list,start,split-1);
      quicksort (list,split+1,end);
    }
  }

  private static int partition (int[] list, int start, int end) {
    int pivot = list[end];
    int bottom = start -1;
    int top = end;
    boolean done = false;

    while (!done) {
      while (!done) {
        bottom++;
        if (bottom == top) {
          done = true;
          break;
        }
        if (list[bottom]>pivot) {
          list[top] = list[bottom];
          break;
        }
      }
      while (!done) {
        top--;
        if (top == bottom) {
          done = true;
          break;
        }
        else {
          if (list[top]<pivot) {
            list[bottom] = list[top];
            break;
          }
        }
      }
    }
    list[top] = pivot;
    return top;
  }
  public static void sort(long[] s) {
    quicksort(s,0,s.length-1);
  }

  public static void sort(long[] s, int start, int end) {
    if(end < start){
      throw new IllegalArgumentException();
    }
    if(start < 0) {
      throw new ArrayIndexOutOfBoundsException(start);
    }
    quicksort(s,start,end-1);
  }


  private static void quicksort(long[] list, int start, int end) {
    int split;
    if (start < end) {
      split = partition (list,start,end);
      quicksort (list,start,split-1);
      quicksort (list,split+1,end);
    }
  }

  private static int partition (long[] list, int start, int end) {
    long pivot = list[end];
    int bottom = start -1;
    int top = end;
    boolean done = false;

    while (!done) {
      while (!done) {
        bottom++;
        if (bottom == top) {
          done = true;
          break;
        }
        if (list[bottom]>pivot) {
          list[top] = list[bottom];
          break;
        }
      }
      while (!done) {
        top--;
        if (top == bottom) {
          done = true;
          break;
        }
        else {
          if (list[top]<pivot) {
            list[bottom] = list[top];
            break;
          }
        }
      }
    }
    list[top] = pivot;
    return top;
  }
}
