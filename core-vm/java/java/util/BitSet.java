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
      throw new IndexOutOfBoundsException("index "+bitIndex);
    }
    if(bitsInUse <= bitIndex){
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
      bits[i] = bits[i] & ((1L<<(bitIndex%64)) ^ -1L);
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
        bits[bidx] &= ((-1L<<(begin%64)) & (-1L>>>(64-(end%64)))) ^ -1L;           
      } else {
        int stop = eidx > bits.length ? bits.length : eidx;
        int rshift = begin % 64;
        bits[bidx] &= rshift > 0 ? ((-1L<<(begin%64)) ^ -1L) : 0;
        for (int i=bidx+1 ; i<stop; i++) {
          bits[i] = 0;
        }      
        int shift = 64 - (end%64);
        if(shift < 64 && eidx < bits.length) {
          bits[eidx] &= (-1L>>>(shift)) ^ -1L;  
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
    if(bitIndex < 0) {
      throw new IndexOutOfBoundsException(String.valueOf(bitIndex));
    }
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
      bits[bidx] ^= ((-1L<<(begin%64)) & (-1L>>>(64-(end%64))));           
    } else {
      int stop = eidx > bits.length ? bits.length : eidx;
      int rshift = begin % 64;
      bits[bidx] ^= rshift > 0 ? -1L<<(rshift) : -1;
      for (int i=bidx+1 ; i<stop; i++) {
        bits[i] ^= -1L;
      }
      int shift = 64 - (end%64);
      if(shift < 64 && eidx < bits.length) {
        bits[eidx] ^= (-1L>>>(shift));  
      }
    }      
    if(end >= bitsInUse) {
      checkBitsInUse();
    }        
  }
  
  /*****************************************************************************
   * @since 1.4
   */
  public BitSet get(int begin, int end) {
    if (begin < 0 || end < begin) {
      throw new IndexOutOfBoundsException();
    }
    int size = end - begin;
    BitSet set = new BitSet(size);
    if (size == 0) {
      return set;
    }
    if (begin <= bitsInUse) {
      int bidx = begin / 64;
      int eidx = (end / 64);
      long word = bits[bidx];
      int rshift = begin % 64;

      if (bidx == eidx) {
        set.bits[0] = (word >>> rshift) & (-1L >>> (64 + rshift - (end % 64)));
      } else {
        int lshift = 64 - rshift;
        int need = (size % 64) - lshift;
        int stop = bits.length < eidx ? bits.length : eidx;
        int j = 0;
        if (++bidx == eidx && need > 0) {
          set.bits[j++] = (-1L >>> 64 + need)
              & (word >>> rshift | (bits[bidx]) << lshift);
        } else {
          if (rshift == 0) {
            for (int i = bidx-1; i < stop; i++) {
              set.bits[j++] = bits[i];
            }
            word = set.bits[j-1];
          } else {
            for (int i = bidx; i <= stop; i++) {
              long nword = bits[i];
              set.bits[j++] = (word >>> rshift) | (nword << lshift);
              word = nword;
            }
          }
          if (need >= 0) {
            // already wrote to many bits.
            set.bits[j - 1] &= -1L >>> (64 - (size % 64));
          } else if (need < 0 && j < set.bits.length) {
            // we need some more bits
            need = lshift + need;
            set.bits[j] = (-1L >>> (65 - need)) | (word >>> rshift);
          }
        }
      }
      set.checkBitsInUse();
    }

    return set;
  }
  
  /**   * 
   * @since 1.4
   */
  public boolean isEmpty() {
    return bitsInUse == 0;
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
    
    //grow the array if needed.
    if(eidx >= bits.length && value) {
      long[] newArray = new long[eidx+1];
      System.arraycopy(bits,0,newArray,0,bits.length);
      bits = newArray;
    }

    int bidx = begin / 64;
    if (bidx == eidx) {
      //special case bidx and eidx are the same.
      long mask = ((-1L<<(begin%64)) & (-1L>>>(64-(end%64))));
      bits[bidx] = value ? bits[bidx] | mask : bits[bidx] & (mask ^ -1L);           
    } else {
      //first set bits[bidx].
      int stop = eidx > bits.length ? bits.length : eidx;
      long mask = -1L<<(begin%64);
      bits[bidx] = value ? bits[bidx] | mask : bits[bidx] & (mask ^ -1L);
      long word = value ? -1L : 0L;
      for (int i=bidx+1 ; i<stop; i++) {
        bits[i] = word;
      }
      int shift = end%64;
      if(shift > 0 && eidx < bits.length) {
        mask = -1L>>>(64-(shift));
        bits[eidx] = value ? bits[eidx] | mask : bits[eidx] & (mask ^ -1L);  
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
    int l = bits.length;

    if(k > l){
      long[] newbits = new long[k];
      System.arraycopy(bits,0, newbits, 0, l);
      bits = newbits;
    }

    long[] bits = this.bits;
    long[] others = bitset.bits;
    for(int i=0 ; i < k ; i++){
      bits[i] = bits[i] | others[i];
    }
    checkBitsInUse();
  }

  public synchronized void xor(BitSet bitset) {
    int k = bitset.bits.length;
    int l = bits.length;

    if(k > l){
      long[] newbits = new long[k];
      System.arraycopy(bits,0, newbits, 0, l);
      bits = newbits;
    }
    long[] bits = this.bits;
    long[] others = bitset.bits;
    for(int i=0 ; i < k ; i++){
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
        lbits >>>= 1;
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
