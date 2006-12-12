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
** $Id: BitSet.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
*/

package java.util;

import java.io.*;

/*
* the current implementation of BitSet doesn't allow the BitSet to shrink
* the size will only grow !!!
* a clear will at a position larger then the size will grow the BitSet
*
*/
public class BitSet implements Cloneable, java.io.Serializable {

  private static final long serialVersionUID = 7997698588986878753L;

  private long[] bits;
  private transient int bitsInUse;

/*
*  the size of the bitset is always rounded up to the next 64 increment
*/
  private int roundup (int n) {
    return ((n+63)/64);
  }

  public BitSet() {
    bits = new long[1];
  }
  
  public BitSet(int nbits) {
    if (nbits < 0 ) throw new NegativeArraySizeException();
    bits = new long[roundup(nbits)];
  }

  public String toString() {
    if(bitsInUse == 0){
      return "{}";
    }

    long[] bits = this.bits;
    int length = bits.length;

    StringBuffer buf = new StringBuffer(3*bits.length+2);

    buf.append('{');

    for (int i=0; i < length; ++i) {
      long l = bits[i];
      int j = 0;
      while(l != 0){
        if((l & 0x1) > 0){
          buf.append((i*64 + j));
          buf.append(", ");

        }
        l = l>>>1;
        j++;
      }
    }

    buf.setLength(buf.length()-2);

    buf.append('}');
    return buf.toString();
  }

  public synchronized boolean equals(Object obj) {
    if (!(obj instanceof BitSet)) {
      return false;
    }

    BitSet that = (BitSet)obj;
    int l = bits.length;
    int k = that.bits.length;
    if(k > l){
      for(int i=0 ; i < l ; i++){
        if(bits[i] != that.bits[i]){
          return false;
        }
      }
      for(int j=l ; j < k ; j++){
        if(that.bits[j] != 0){
          return false;
        }
      }
    }
    else {
      for(int i=0 ; i < k ; i++){
        if(bits[i] != that.bits[i]){
          return false;
        }
      }
      for(int j=k ; j < l ; j++){
        if(bits[j] != 0){
          return false;
        }
      }
    }

    return true;
  }
  
  public synchronized int hashCode() {
    long h = 1234;

    for (int i = bits.length-1 ; i >= 0; i--) {
      h ^= bits[i] * (i+1);
    }

    return (int)((h>>32)^h);

  }
  
  public synchronized Object clone() {
    try {
      BitSet clone = (BitSet)super.clone();
      clone.bits = (long[])bits.clone();
      return clone;
    }
    catch(CloneNotSupportedException cne){
      return null;
    }
  }

  
  public boolean get(int bitIndex) {
    if(bitIndex < 0){
      throw new IndexOutOfBoundsException();
    }
    if(bitsInUse < bitIndex){
      return false;
    }
    return (((bits[bitIndex/64])>>(bitIndex%64)) & 0x01) > 0;
  }
  
  private void grow() {
    if(bitsInUse > bits.length * 64){
      long[] nbits = new long[roundup(bitsInUse)];
      System.arraycopy(bits, 0, nbits, 0, bits.length);
      bits = nbits;
    }
  }

  public synchronized void set(int bitIndex) {
    if(bitIndex < 0){
      throw new IndexOutOfBoundsException();
    }
    if (bitIndex>=bitsInUse) {
       bitsInUse = bitIndex+1;
       grow();
    }
    int i = bitIndex / 64;
    bits[i] = bits[i] | (1L<<(bitIndex%64));
  }

  /**   * 
   * @since 1.4
   */
  public void clear() {
    for(int i=0; i < bits.length ; i++) {
      bits[i] = 0L;
    }
    bitsInUse = 0;
  }
  
  public synchronized void clear(int bitIndex) {
    if(bitIndex < 0){
      throw new IndexOutOfBoundsException();
    }
    if (bitsInUse > bitIndex) {
      int i = bitIndex / 64;
      bits[i] = bits[i] & ((1L<<(bitIndex%64)) ^ -1);
    }
    checkBitsInUse();
  }
  
  /**   * 
   * @since 1.4
   */
  public void clear(int begin, int end) {
    if(begin < 0 || end < begin) {
      throw new IndexOutOfBoundsException();
    }
    if (begin <= bitsInUse) {
      int bidx = begin / 64;
      int eidx = end / 64;
      if (bidx == eidx) {
        bits[bidx] &= ((-1L>>(begin%64)) & (-1L<<(end%64))) ^ -1L;           
      } else {
        int stop = eidx > bits.length ? bits.length : eidx;
        bits[bidx] &= (-1L>>(begin%64)) ^ -1L;
        for (int i=begin+1 ; i<stop; i++) {
          bits[i] = 0;
        }
        if(eidx < bits.length) {
          bits[eidx] &= (-1L<<(end%64)) ^ -1L;  
        }
      }      
      if(end >= bitsInUse) {
        checkBitsInUse();
      }
    }
  }
  
  /**   * 
   * @since 1.4
   */
  public void flip(int bitIndex) {
    int idx = bitIndex / 64;
    if(idx >= bits.length) {
      long[] newArray = new long[idx+1];
      System.arraycopy(bits,0,newArray,0,bits.length);
      bits = newArray;
    }
    bits[idx] ^= 0x1L<<(bitIndex % 64);
  }
  /**   * 
   * @since 1.4
   */
  public void flip(int begin, int end) {
    if(begin < 0 || end < begin) {
      throw new IndexOutOfBoundsException();
    }
    int eidx = end / 64;
    if(eidx >= bits.length) {
      long[] newArray = new long[eidx+1];
      System.arraycopy(bits,0,newArray,0,bits.length);
      bits = newArray;
    }

    int bidx = begin / 64;
    if (bidx == eidx) {
      bits[bidx] ^= ((-1L>>(begin%64)) & (-1L<<(end%64)));           
    } else {
      int stop = eidx > bits.length ? bits.length : eidx;
      bits[bidx] ^= -1L>>(begin%64);
      for (int i=begin+1 ; i<stop; i++) {
        bits[i] ^= -1L;
      }
      if(eidx < bits.length) {
        bits[eidx] ^= (-1L<<(end%64));  
      }
    }      
    if(end >= bitsInUse) {
      checkBitsInUse();
    }        
  }
  
  /**   * 
   * @since 1.4
   */
  public BitSet get(int begin, int end) {
    BitSet set = new BitSet(end - begin);
    if(begin <= bitsInUse) {
      int bidx = begin / 64;
      int eidx = (end / 64);
      int rshift = begin % 64;
      int lshift = 64 - rshift;
      long word = bits[bidx];
      int stop = bits.length > eidx ? bits.length : eidx;
      int j=0;
      for (int i=bidx+1; i < stop ; i++, j++) {
        long nword = bits[i];
        set.bits[j] = word<<rshift | nword>>lshift;  
        word = nword;
      }
      //TODO ...
      //We still need to place some depending on eidx
      set.checkBitsInUse();
    }
    
    return set;
  }
  
  /**   * 
   * @since 1.4
   */
  public boolean isEmpty() {
    return bitsInUse != 0;
  }
  
  /**   * 
   * @since 1.4
   */
  public boolean intersects(BitSet set) {
    int length = bits.length <= set.bits.length ? 
        bits.length : set.bits.length;   
    for(int i=0; i < length ; i++) {
      if((bits[i] & set.bits[i]) > 0) {
        return true;
      }
    }
    return false;
  }
  
  /**   * 
   * @since 1.4
   */
  public int nextSetBit(int fromIndex) {
    int idx = fromIndex / 64;
    if(idx < bits.length) {
      int shift = fromIndex % 64;
      long word = bits[idx]>> shift;
      int leftover = 64 - shift;
      if(word == 0L) {
        fromIndex += leftover;
        leftover = 64;
        while(++idx < bits.length && 0L == (word = bits[idx])) {
          fromIndex += 64;
        }
      }
      if(word != 0L) {
        for (int i=0; i < leftover ; i++) {
          if((word & 0x01) == 1) {
            return fromIndex + i;
          }
          word >>= 1;
        }        
      }
    }
    return -1;
  }

  /**   * 
   * @since 1.4
   */
  public int nextClearBit(int fromIndex) {
    if(fromIndex < bitsInUse) {
      int idx = fromIndex / 64;
      if(idx < bits.length) {
        int shift = fromIndex % 64;
        long word = bits[idx]>> shift;
        int leftover = 64 - shift;
        if(word == -1L) {
          fromIndex += leftover;
          leftover = 64;
          while(++idx < bits.length && -1L == (word = bits[idx])) {
            fromIndex += 64;
          }
        }
        if(word != -1L) {
          for (int i=0; i < leftover ; i++) {
            if((word & 0x01) == 0) {
              return fromIndex + i;
            }
            word >>= 1;
          }        
        }
      }
    }
    return fromIndex;
  }


  /**   * 
   * @since 1.4
   */
  public void set(int bitIndex, boolean value){
    int idx = bitIndex / 64;
    long mask = 0x1L<<(bitIndex % 64);
    if(idx >= bits.length) {
      if (!value) {
        return;
      }
      long[] newBits = new long[idx+1];
      System.arraycopy(bits,0, newBits, 0, bits.length);
      bits = newBits;
      newBits[idx] = mask;
    } else {
      long word = bits[idx];
      bits[idx] = value ? (word | mask) : (word & (mask ^ -1L));
    }
    checkBitsInUse();
  }

  /**   * 
   * @since 1.4
   */
  public void set(int fromIndex, int toIndex){
    set(fromIndex, toIndex, true);      
  }

  /**   * 
   * @since 1.4
   */
  public void set(int begin, int end, boolean value) {
    if(begin < 0 || end < begin) {
      throw new IndexOutOfBoundsException();
    }
    int eidx = end / 64;
    if(eidx >= bits.length) {
      if (value) {
        long[] newArray = new long[eidx+1];
        System.arraycopy(bits,0,newArray,0,bits.length);
        bits = newArray;
      }
    }

    int bidx = begin / 64;
    if (bidx == eidx) {
      long mask = ((-1L>>(begin%64)) & (-1L<<(end%64)));
      bits[bidx] = value ? bits[bidx] | mask : bits[bidx] & (mask ^ -1L);           
    } else {
      int stop = eidx > bits.length ? bits.length : eidx;
      long mask = -1L>>(begin%64);
      bits[bidx] = value ? bits[bidx] | mask : bits[bidx] & (mask ^ -1L);
      long word = value ? -1L : 0L;
      for (int i=begin+1 ; i<stop; i++) {
        bits[i] = word;
      }
      if(eidx < bits.length) {
        mask = -1L>>(begin%64);
        bits[eidx] = value ? bits[bidx] | mask : bits[bidx] & (mask ^ -1L);  
      }
    }      
    if(end >= bitsInUse) {
      checkBitsInUse();
    }        
  }
  
  public void and(BitSet bitset) {
    int k = bitset.bits.length;
    long[] bits = this.bits;
    int l = bits.length;

    if(k < l){
      for(int j=k ; j < l ; j++){
        bits[j] = 0L;
      }
      l = k;
    }
    long[] others = bitset.bits;
    for(int i=0 ; i < l ; i++){
      bits[i] = bits[i] & others[i];
    }
    checkBitsInUse();
  }

  public void or(BitSet bitset) {
    int k = bitset.bits.length;
    long[] bits = this.bits;
    int l = bits.length;

    if(k < l){
      l = k;
    }
    long[] others = bitset.bits;
    for(int i=0 ; i < l ; i++){
      bits[i] = bits[i] | others[i];
    }
    checkBitsInUse();
  }

  public synchronized void xor(BitSet bitset) {
    int k = bitset.bits.length;
    long[] bits = this.bits;
    int l = bits.length;

    if(k < l){
      l = k;
    }
    long[] others = bitset.bits;
    for(int i=0 ; i < l ; i++){
      bits[i] = bits[i] ^ others[i];
    }
    checkBitsInUse();
  }

  public int size() {
    return bits.length * 64;
  }

/*
* this method is added in JDK 1.2
*/
  public int length() {
    return bitsInUse;
  }	

  /**   * 
   * @since 1.4
   */
  public int cardinality() {
    int cardinality = 0;
    for(int i=0; i < bits.length ; i++) {
      long lbits = bits[i];
      while(lbits != 0) {
        if((lbits & 0x1) == 1) {
          cardinality++;
        }
        lbits >>= 1;
      }
    }
    return cardinality;
  }
  
  public void andNot(BitSet bs) {
    long[] others = bs.bits;
    int k = others.length;
    long[] bits = this.bits;
    int l = bits.length;

    if(k < l){
      l = k;
    }
    for(int i=0 ; i < l ; i++){
      bits[i] = bits[i] & (others[i] ^ -1);
    }
    checkBitsInUse();
  }

  private void checkBitsInUse(){
    long[] bits = this.bits;
    int l = bits.length - 1;

    if(l >= 0){
      while(bits[l] == 0L){
        if(--l < 0){
          bitsInUse = 0;
          return;
        }
      }

      long value = bits[l]>>>1;
      int i = 1;

      while(value != 0){
        value = (value)>>>1;
        i++;
      }
      bitsInUse = l * 64 + i;
    }
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    checkBitsInUse();
  }
}
