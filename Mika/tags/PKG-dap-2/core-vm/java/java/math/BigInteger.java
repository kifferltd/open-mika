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
 * $Id: BigInteger.java,v 1.9 2006/06/20 11:45:04 cvs Exp $
 */

package java.math;

import java.util.Random;
import java.util.Arrays;

public class BigInteger extends Number implements Comparable {

  private static Random theRandom = new Random();
/*
  private static void debug(byte[] val, String message){
    System.out.print("DEBUGGING BYTE[]: ");
    for (int i = 0 ; i < val.length ; i++){
      System.out.print(val[i]+", ");
    }
    System.out.println(message);
  }
*/
  private static final long serialVersionUID = -8287574255936472291L;

  // represents the BigInteger '0'
  public static final BigInteger ZERO = new BigInteger(new byte[0],0);
  // represents the BigInteger '1'
  public static final BigInteger ONE = new BigInteger(1L);
  // represents the BigInteger '-1'
  static final BigInteger bigNegOne = new BigInteger(-1L);
  static final BigInteger TWO = new BigInteger(2L);
  static final BigInteger THREE = new BigInteger(3L);
  static final BigInteger FIVE = new BigInteger(5L);
  static final BigInteger SEVEN = new BigInteger(7L);

  private static final char[] DIGIT_CHARS = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','g','h',
                                             'i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};

  /**
  ** this constant is used when creating a BigInteger from a string. It contains the number of digits can be grouped together
  ** without causing Long.parseLong to throw a NumberFormatException (due to large number).
  */
  private static final int[] DIGITS_PER_LONG = { -1, -1, 62, 39, 31, 27, 24, 22, 20, 19, 18, 18, 17, 17, 16, 16, 15, 15, 15,
                                                 14, 14, 14, 14, 13, 13, 13, 13, 13, 13, 12, 12, 12, 12, 12, 12, 12, 12 };

  /**
  ** this constant is used when creating a BigInteger from a string. It contains the factors which are used to
  ** to multiply the results of Long.parseLong.
  */
  private static final long[] MULTIPLIERS = { -1, -1, 4611686018427387904L, 4052555153018976267L, 4611686018427387904L,
                               7450580596923828125L, 4738381338321616896L, 3909821048582988049L, 1152921504606846976L,
                               1350851717672992089L, 1000000000000000000L, 5559917313492231481L, 2218611106740436992L,
                               8650415919381337933L, 2177953337809371136L, 6568408355712890625L, 1152921504606846976L,
                               2862423051509815793L, 6746640616477458432L,  799006685782884121L, 1638400000000000000L,
                               3243919932521508681L, 6221821273427820544L,  504036361936467383L,  876488338465357824L,
                               1490116119384765625L, 2481152873203736576L, 4052555153018976267L, 6502111422497947648L,
                                353814783205469041L,  531441000000000000L,  787662783788549761L, 1152921504606846976L,
                               1667889514952984961L, 2386420683693101056L, 3379220508056640625L, 4738381338321616896L};

  /**
  ** this field is introduced to cache the negate() operation
  */
  private transient BigInteger negateCache;
  private transient byte[] valueCache;//null if not cached ... this field is setup native

  /**
  ** dictated by serialized form
  */
  byte [] magnitude;
  private int signum;
  private int lowestSetBit=-2;
  private int firstNonzeroByteNum=-2;
  private int bitCount=-1;
  private int bitLength=-1;

  private BigInteger(byte[] magn, int sign){
   	magnitude = magn;
   	signum = sign;	
  }

  private BigInteger(byte [] magn, boolean clone){
  	initWithByteArray(magn,clone);
  }

  BigInteger(long l){
    byte [] bytes = longToBytes(l);
  	initWithByteArray(bytes,false);
  }

  public BigInteger(String val) throws NumberFormatException {
  	this(val,10);
  }

  public BigInteger(String val, int rdx) throws NumberFormatException {
    int len = val.length();
    if(len == 0){
      throw new NumberFormatException("invalid string (empty string)");
    }
    boolean negative = false;
    val = val.toLowerCase();
    char ch = val.charAt(0);
    if(ch == '-'){
      val = val.substring(1);
      len--;
      negative = true;
    }
    else if(ch == '+'){
      val = val.substring(1);
      len--;
    }

    signum = 1;
    if(rdx < Character.MIN_RADIX || rdx > Character.MAX_RADIX){
      rdx = 10;
    }

    int digits = DIGITS_PER_LONG[rdx];
    int rem = len % digits;
    int groups = len / digits;
    int index = 0;

    if(rem != 0){
      magnitude = longToBytes(Long.parseLong(val.substring(0,rem),rdx));
      index = rem;
    }
    else if(groups != 0){
      magnitude = longToBytes(Long.parseLong(val.substring(0,digits),rdx));
      index = digits;
      groups--;
    }
    else{
      throw new NumberFormatException("invalid string (only sign character)");
    }
    if(groups > 0){
      byte[] multiplier = longToBytes(MULTIPLIERS[rdx]);
      for(int i=0; i < groups; i++){
        int end = index + digits;
        byte[] bytes = new byte[magnitude.length+8];
        javaMultiply(multiplier,bytes);
        magnitude = bytes;
        addLong(Long.parseLong(val.substring(index,end),rdx));
        index = end;
      }
    }
    //magnitude contains now the byte pattern off the specified number (with disregard to the sign)
    //step 1 make it a normelized BigInteger.
    normelizeMagnitude(false);
    //if signum == -1;
    if(negative && signum == 1){
	    negateCache = new BigInteger(magnitude,1);
	    negateCache.negateCache = this;
	    magnitude = nativeNegateBytes();
	    signum = -1;
    }
  }

  public BigInteger(byte[] bval)throws NumberFormatException{
  	initWithByteArray(bval,true);
  }

  public BigInteger(int sign, byte[] bval) throws NumberFormatException {
   	if (sign > 1 || sign < -1) {
   	 	throw new NumberFormatException("wrong sign specified :"+sign);
   	}
   	signum = sign;
   	if (sign == 0) {
	   	magnitude = new byte [0];
	  }
	  else {
	    magnitude = bval;
	    normelizeMagnitude(true);
	    if (sign == -1){
	      negateCache = new BigInteger(magnitude,1);
	      magnitude = nativeNegateBytes();
	      negateCache.negateCache = this;
	    }
	  }
  }

  public BigInteger(int numbits, Random rdm) throws IllegalArgumentException {
  	if (numbits < 0){
  	 	throw new IllegalArgumentException("got negative number of bits :"+numbits);
  	}
  	magnitude = new byte[(numbits-1)/8+1];
  	rdm.nextBytes(magnitude);
  	magnitude[0] = (byte)(0xff>>(8-(numbits%8)) &  ((char)magnitude[0]));
    if (magnitude.length == 1 && magnitude[0] == 0){
      magnitude = new byte[0];
    }
    else{
      signum = 1;
    }
  }

  public BigInteger(int bitLength, int cert, Random rnd) throws IllegalArgumentException {
   	if (bitLength < 2){
   	 	throw new IllegalArgumentException("bitLength should be 2 or greater: "+bitLength);
   	}
   	   	
   	BigInteger bi = new BigInteger(bitLength, rnd);
    int l = bi.magnitude.length;
    if(l > 0){
      int msb = (0xff & (char)bi.magnitude[0]);
      bi.magnitude[0] = (byte) (msb |  0xc0>>(7-((bitLength-1)%8)));
      bi.magnitude[l-1] = (byte) (0x01 | (char)bi.magnitude[l-1]);
    }
   	
   	for (;!bi.isProbablePrime(cert) ;){
   		bi = new BigInteger(bitLength, rnd);	 	
   	
   	  l = bi.magnitude.length;
      if(l > 0){
        int msb = (0xff & (char)bi.magnitude[0]);
        bi.magnitude[0] = (byte) (msb |  0xc0>>(7-((bitLength-1)%8)));
        bi.magnitude[l-1] = (byte) (0x01 | (char)bi.magnitude[l-1]);
      }
   	}
   	this.signum = 1;
   	this.magnitude = bi.magnitude;
  }

  public BigInteger abs(){
   	if (signum < 0){
   	 	return negate();
   	}
   	return this;//since it doesn't change there is no need to create a new Object;
  }

  public BigInteger and(BigInteger bi){
  	if (signum == 0 || bi.signum == 0){
  	 	return ZERO;
  	}
  	byte[] bmax = magnitude.length < bi.magnitude.length ? bi.magnitude : magnitude;
  	byte[] bmin = magnitude == bmax ? bi.magnitude : magnitude;
  	byte [] bytes = new byte[bmax.length+1];
  	bytes[0] = (byte)((signum == -1 ? -1 : 0)&(bi.signum == -1 ? -1 : 0));
  	int sign = bi.magnitude.length > magnitude.length ? signum : bi.signum;
  	sign = (sign == -1 ? -1 : 0);
  	int dif = bmax.length-bmin.length;
  	for (int j=0 ; j < dif ; j++){
      bytes[j+1] = (byte)(sign & (char)(bmax[j]));
  	}	
  	for (int i=0,j=dif ; i < bmin.length ; i++,j++){
  	 	bytes[j+1] = (byte)((char)(bmax[j]&bmin[i]));
  	}
  	return new BigInteger(bytes,false);	
  }

  public BigInteger andNot(BigInteger bi){
   	return and(bi.not());
  }

  public int bitLength(){
    if (bitLength == -1){
      int l = magnitude.length * 8;
      if (magnitude.length > 0){// inspect the Most significant byte   	
      	int hlp = 8;
     	int res = (signum == 1 ? 0 : -1);
      	int bits = magnitude[0];
      	while(bits != res && hlp > 1) {
          hlp--;
          bits = bits>>>1;
        }
      	l -= hlp;	
      }
      bitLength = l;
    }
    return bitLength;
  }

  public BigInteger clearBit(int n) throws ArithmeticException{
   	if (n < 0) {
   	  throw new ArithmeticException("cannot clear bit "+n+" (negative value)");
   	}
   	if(signum == 0){
   	 	return ZERO;
   	}
   	byte [] bytes;
   	if (n >= 8*magnitude.length-1){
   	 	bytes = createByteArray(n);
   	}
   	else{
   	 	bytes = (byte[])magnitude.clone();
   	}
   	int bt = bytes.length - 1 - (n / 8);
   	n = n % 8;
   	int mask = 0xff7f;
  	for ( ; n < 7 ; n++){
  	 	mask = mask>>1;
  	} 	
  	bytes[bt] = (byte)(((char)bytes[bt]) & mask);
  	return new BigInteger(bytes,false);
  }

  public int compareTo (Object o) {
  	return compareTo((BigInteger)o);
  }

  public int compareTo(BigInteger bi){
   	//compare signum
   	if (signum != bi.signum || signum == 0){
  		// signum != bi.signum or both are 0
  		return (signum == 0 ? (-bi.signum) : signum);
   	} //compare magnitude ...
   	if (magnitude.length != bi.magnitude.length){
      return (magnitude.length > bi.magnitude.length ? 1 : -1) * signum;
   	}
   	for (int i=0 ; i < magnitude.length ; i++){
    	if(magnitude[i] != bi.magnitude[i]){        
    	  return ((0xff & (char)(magnitude[i])) > (0xff & (char)(bi.magnitude[i]))? 1 : -1); 	 	 	   	     	
    	}
   	}
   	return 0;
  }

  public boolean equals(Object val){
   	if (!(val instanceof BigInteger)){
   	 	return false;
   	}
   	BigInteger bi = (BigInteger)val;
   	return  (signum == bi.signum) &&
   		(Arrays.equals(magnitude, bi.magnitude));  	
  }

  public BigInteger flipBit(int n) throws ArithmeticException {
   	if (n < 0) {
   	  throw new ArithmeticException("cannot flip bit "+n+" (negative value)");
   	}
   	byte [] bytes;
   	if (n >= 8*magnitude.length-1){
   	 	bytes = createByteArray(n);
   	}
   	else{
   	 	bytes = (byte[])magnitude.clone();
   	}
   	int bt = bytes.length - 1 - (n / 8);
   	n = n % 8;
   	int mask = 0x100;
  	for ( ; n < 7 ; n++){
  	 	mask = mask>>1;
  	} 	
  	bytes[bt] = (byte)(((char)bytes[bt]) ^ mask);
	  return new BigInteger(bytes,false);
  }

  public int getLowestSetBit(){
  	if (lowestSetBit == -2){
  		if (signum == 0){
  		 	lowestSetBit = -1;	
  		}else{
  		  for (int i=magnitude.length-1 ; i >= 0 ; i--){
    			if(magnitude[i] != 0){
    			   int bt = magnitude[i]>>1;
    			   int res = 7; 			
    			   for (int j=0 ; j < 7 ; j++){
               if(bt == 0){
                res = j;
               	break;
               }  	
    			   }
    			   lowestSetBit = res + (magnitude.length - 1 - i) * 8;
    			   break;
    			}	 	
  		  } 		
  		}	 	
  	}
  	return lowestSetBit;
  }

  public int hashCode(){
  	int hash = 0;
  	for (int i=0 ; i < magnitude.length ; i++){
  	 	hash ^= 59793 * magnitude[i];
  	} 	
  	return hash * signum;
  }

  public BigInteger max(BigInteger val){
    return compareTo(val) == 1 ? this : val;
  }

  public BigInteger min(BigInteger val){
    return compareTo(val) == -1 ? this : val;
  }

  public BigInteger negate(){
    if (negateCache == null){
      if (signum == 0){
        negateCache = this;
      }else {
        negateCache = new BigInteger(nativeNegateBytes(),-signum);
        negateCache.negateCache = this;	
      }
    }
    return negateCache;
  }

  public BigInteger not(){
  	byte [] bytes = new byte[magnitude.length+1];
  	bytes[0] = (byte)(signum == -1 ? 0 : -1);
  	System.arraycopy(magnitude,0,bytes,1,magnitude.length);
  	for (int i=1 ; i < bytes.length ; i++){
  	 	bytes[i] = (byte)(0xff ^ (char)(bytes[i]));
  	}   	
  	return new BigInteger(bytes,false);
  }

  public BigInteger or(BigInteger bi){
  	if (bi.signum == 0) {
  	 	return this;
  	}
  	if (signum == 0) {
  	 	return bi;
  	}
  	byte[] bmax = magnitude.length < bi.magnitude.length ? bi.magnitude : magnitude;
  	byte[] bmin = magnitude == bmax ? bi.magnitude : magnitude;
  	byte [] bytes = new byte[bmax.length+1];
  	bytes[0] = (byte)((signum == -1 ? -1 : 0)|(bi.signum == -1 ? -1 : 0));
  	int sign = bi.magnitude.length > magnitude.length ? signum : bi.signum;
  	sign = (sign == -1 ? -1 : 0);
  	int dif = bmax.length-bmin.length;
  	for (int j=0 ; j < dif ; j++){
      bytes[j+1] = (byte)(sign | (char)(bmax[j]));
  	}	
  	for (int i=0,j=dif ; i < bmin.length ; i++,j++){
  	 	bytes[j+1] = (byte)((char)(bmax[j] | bmin[i]));
  	}
  	return new BigInteger(bytes,false);	
  }

  public BigInteger setBit(int n){
   	if (n < 0) {
 	   	throw new ArithmeticException("cannot set bit "+n+" (negative value)");
   	}
   	byte [] bytes;
   	if (n >= 8*magnitude.length-1){
   	 	bytes = createByteArray(n);
   	}
   	else{
   	 	bytes = (byte[])magnitude.clone();
   	}
   	int bt = bytes.length - 1 - (n / 8);
   	n = n % 8;
   	int mask = 0x80;
  	for ( ; n < 7 ; n++){
  	 	mask = mask>>1;
  	} 	
  	bytes[bt] = (byte)(((char)bytes[bt]) | mask);
	  return new BigInteger(bytes,false);
  }

  public BigInteger shiftLeft(int n){
   	if (n <= 0){
   	 	return shiftRight(-n);
   	} 	
   	int bt = (n / 8);
   	n = n % 8;
   	byte [] bytes;
   	if (n != 0){ //shift bits ...
 	   	bytes = new byte[magnitude.length + 1 + bt];
   		System.arraycopy(magnitude,0,bytes,1,magnitude.length);
   		if (signum == -1) {
   		 	bytes[0] = (byte)0xff;
   		}
   		bt = 0;
 	   	for (int i=bytes.length-1 ; i > -1 ; i--){
 	   	 	bt |= (0xff & ((char)bytes[i]))<<8;   	   	 	
 	   	 	bt  = bt>>(8-n);
 	   	 	bytes[i] = (byte)bt;
 	   	 	bt  = bt>>(n);   	   	 	
 	   	}
   	}
   	else{
   	  bytes = new byte[magnitude.length + bt];
   		System.arraycopy(magnitude,0,bytes,0,magnitude.length);   	
   	}   		
		return new BigInteger(bytes,false);
  }

  public BigInteger shiftRight(int n){
  	if(n ==	0){
  	 	return this;
  	}     	
   	if (n < 0) {
   	  return shiftLeft(-n);
   	}
   	int bt = (n / 8);
   	if (bt >= magnitude.length){
   	 	return (signum == -1 ? bigNegOne : ZERO );
   	}
   	byte [] bytes = new byte[magnitude.length - bt];
   	System.arraycopy(magnitude,0,bytes,0,magnitude.length - bt);
   	n = n % 8;
   	if (n != 0){ //shift bits ...
 	   	bt = (signum == -1 ? 0x0ff00 : 0);
 	   	for (int i=0 ; i < bytes.length ; i++){
 	   	 	bt |= (0xff & ((char)bytes[i]));
 	   	 	bytes[i] = (byte)(bt>>n);
 	   	 	bt  = bt<<(8);   	   	 	
 	   	}
   	}
  	return new BigInteger(bytes,false);
  }

  public boolean testBit(int n){
   	if (n < 0) {
   	  throw new ArithmeticException("cannot test bit "+n+" (negative value)");
   	}
   	if (n >= 8*magnitude.length){
   		return (signum == -1);
   	}
   	int bt = magnitude.length - 1 - (n / 8);
   	n = n % 8;
   	int mask = 0x80;
  	for ( ; n < 7 ; n++){
  	 	mask = mask>>1;
  	} 	
  	return (((char)magnitude[bt]) & mask)!= 0;
  }

  public byte[] toByteArray(){
  	if (signum == 0){
  	 	return new byte[1];
  	}
  	if ((magnitude[0] >= 0 && signum == 1)||(magnitude[0] < 0 && signum == -1)){
  	 	byte [] b = new byte[magnitude.length];
  	 	System.arraycopy(magnitude,0,b,0,magnitude.length);
  	 	return b;
  	}
   	return (byte[])magnitude.clone();
  }

  public static BigInteger valueOf(long val){
  	if (val == 0){
  	 	return ZERO;
  	}
  	if (val == 1){
  	 	return ONE;
  	}
  	if (val == 2){
  	 	return TWO;
  	}
  	if (val == -1){
  	 	return bigNegOne;
  	}
  	return new BigInteger(val);

  }

  public BigInteger xor(BigInteger bi){
  	if (bi.signum == 0) {
  	 	return this;
  	}
  	if (signum == 0) {
  	 	return bi;
  	}
  	byte[] bmax = magnitude.length < bi.magnitude.length ? bi.magnitude : magnitude;
  	byte[] bmin = magnitude == bmax ? bi.magnitude : magnitude;
  	byte [] bytes = new byte[bmax.length+1];
  	bytes[0] = (byte)((signum == -1 ? -1 : 0)^(bi.signum == -1 ? -1 : 0));
  	int sign = bi.magnitude.length > magnitude.length ? signum : bi.signum;
  	sign = (sign == -1 ? -1 : 0);
  	int dif = bmax.length-bmin.length;
  	for (int j=0 ; j < dif ; j++){
      bytes[j+1] = (byte)(sign ^ (char)(bmax[j]));
  	}	
  	for (int i=0,j=dif ; i < bmin.length ; i++,j++){
  	 	bytes[j+1] = (byte)((char)(bmax[j]^bmin[i]));
  	}
   	return new BigInteger(bytes,false);	
  }

  public int signum(){
   	return signum;
  } 	

  public double doubleValue() {
    //lets transform this BigInteger to String. make the string a double format ...
    StringBuffer buf = new StringBuffer(this.toString(10));
    int offset = signum == -1 ? 1 : 0;
    int length = buf.length() - offset;
    if(length > 17){
      buf.setLength(17+offset);
    }
    buf.insert(offset + 1, '.');
    buf.append('E');
    buf.append(length-1);
    try {
      return Double.parseDouble(buf.toString());
    }
    catch (NumberFormatException nfe){
      return (signum == -1 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
    }
  }

  public float floatValue() {
  	return (float)doubleValue();
  }

  public int intValue() {
    int start = (magnitude.length - 4) > 0 ? (magnitude.length - 4) : 0;
  	int result = (signum == -1 ? -1 : 0);
  	for ( ; start < magnitude.length ; start++){
  		result = (0xffffff00 & (result<<8)) | (0xff & ((char)magnitude[start]));	 	
  	}
  	return result;
  }

  public long longValue() {
    int start = (magnitude.length -8) > 0 ? (magnitude.length -8) : 0;
  	long result = (signum == -1 ? -1 : 0);
  	for ( ; start < magnitude.length ; start++){
  		result = (0xffffffffffffff00L & (result<<8)) | (0xff & ((char)magnitude[start]));	 	
  	}
  	return result;
  }

/**
** based on the Miller-Rabin algorithm.
** a^(n-1) = 1 (mod n), where 0 < a < n. if the result is not 1 then is not a prime ...
** the chance that n is a prime after k succesfull steps is 1 - 0.5^k
**
** if this is not a really large number we might consider another aproach ...
**
**  ??? TODO ??? do we need these extra steps ...
**
** Choose a random number, p, to test. Calculate b, where b is the number of times 2 divides p-1.
** Then calculate m, such that p = 1 + 2b*m.
**
** 1. Choose a random number, a, such that a is less than p.
**
** 2. Set j = 0, and set z = am mod p.
**
** 3. If z = 1, or if z = p - 1, then p passes the test and may be prime.
**
** 4. If j > 0 and z = 1, then p is not prime.
**
** 5. Set j = j +1. If j < b and z � z2 mod p go back to step 4. If z = p - 1, then p passes the test and may be prime.
**
** 6. If j = b and z � p -1, then p is not prime.
**
** The odds of a composite passing decreases faster with this test than with previous ones.
** Three-quarters of the possible values of a are guaranteed to be witnesses. This means that
** a composite number will slip through t tests no more than 1/4t of the time, where t is the
** number of iterations. Actually, these numbers are very pessimistic. For most random
** numbers, something like 99.9 percent of the possible a values are witnesses.
*/

  public boolean isProbablePrime(int cert){
    //step 1: reject all multiple of 2 except 2 itself.
    BigInteger thisAbs = this.abs();
    if(!testBit(0)){
      return thisAbs.equals(TWO);
    }
    //step 2: eliminate some trivial cases and reduce the chance to generate '0' or '1' as random value
    if(thisAbs.equals(ONE)){
      return false;
    }
    if(THREE.equals(thisAbs) || FIVE.equals(thisAbs) || SEVEN.equals(thisAbs)){
      return true;
    }
    //step 3: Miller-Rabin.
    int length = (thisAbs.bitLength()-1);
    BigInteger thisM1 = thisAbs.subtract(ONE);
    for(int i = 0 ; i < cert ; i++){
      BigInteger random = new BigInteger(length, theRandom);
      while(random.signum == 0 || random.equals(ONE)){
        random = new BigInteger(length, theRandom);
      }
      if(!ONE.equals(random.modPow(thisM1, thisAbs))){
        return false;
      }
    }
    return true;
  }	

  public int bitCount(){
    if(bitCount == -1){
      if(signum == 0){
        bitCount = 0;
      }
      else if(signum == 1){
        int count = 0;
        for(int i = 0 ; i < magnitude.length ; i++){
          int b = (char)magnitude[i];
          for(int j = 0 ; j < 8 ; j++){
            count += (b & 0x01);
            b = b>>1;
          }
        }
      }
      else {
        int count = 0;
        for(int i = 0 ; i < magnitude.length ; i++){
          int b = (char)magnitude[i];
          for(int j = 0 ; j < 8 ; j++){
            count +=  1 - (b & 0x01);
            b = b>>1;
          }
        }
      }
    }
    return bitCount;
  }

  public native BigInteger add(BigInteger bi);
  
  public BigInteger subtract(BigInteger bi) {
      BigInteger result = nativeSubtract(bi);
      if (result.compareTo(BigInteger.ZERO) != 0) {
         result.addLeadingByte();
      }
      return result;
  }
 
  private native BigInteger nativeSubtract(BigInteger bi);
  
  public BigInteger multiply(BigInteger bi) {
      BigInteger result = nativeMultiply(bi);
      if (result.compareTo(BigInteger.ZERO) != 0) {
         result.addLeadingByte();
      }
      return result;
  }

  private native BigInteger nativeMultiply(BigInteger val);
  
  public native BigInteger divide(BigInteger bi);
  
  public BigInteger mod(BigInteger bi) {
      BigInteger result = nativeMod(bi);
      if (result.compareTo(BigInteger.ZERO) != 0) {
         result.addLeadingByte();
      }
      return result;
  }

  private native BigInteger nativeMod(BigInteger bi);

  public native BigInteger remainder(BigInteger bi);

  public BigInteger[] divideAndRemainder(BigInteger bi){
    if(bi.signum == 0){
      throw new ArithmeticException("cannot divide by '0'");
    }

  	BigInteger [] big = new BigInteger[2];
    //check Trivial cases to avoid Object duplication
    if(signum == 0){
      big[0] = this;
      big[1] = this;
      return big;
    }

    if(bi.magnitude.length == 1){
      int num = bi.magnitude[0];
      if(num == 1){
        big[0] = this;
        big[1] = ZERO;
        return big;
      }
      else if(num == -1){
        big[0] = this.negate();
        big[1] = ZERO;
        return big;
      }
    }

    if(magnitude.length < bi.magnitude.length){
        big[0] = ZERO;
        big[1] = this;
    }
    else {
      big[0] = divide(bi);
      big[1] = remainder(bi);
    }
	  return big;
  }

  public BigInteger gcd(BigInteger bi){
    if(bi.signum == 0 || signum == 0){
      return (bi.signum == signum ? ZERO : ONE);
    }
    BigInteger valG = this.abs();
    BigInteger valL = bi.abs();
    BigInteger mod;
    if(valG.compareTo(valL) == -1){
       mod = valL;
       valL = valG;
       valG = mod;
    }
    while(true){
      mod = valG.mod(valL);
      if (mod.signum == 0){
        return valL;
      }
      valG = valL;
      valL = mod;
    }
  }

/**
** the modInverse is calculted by using the 'Extended Euclid's algorithm'.
** vectors[0] = u  (u will have the result inside).
** vectors[1] = v
** with this * u1 + bi * u2 = u3 = gcd(this,bi).
** v stores temporary results and this * v1 + bi * v2 = v3
*/
  public BigInteger modInverse(BigInteger bi){
    if(bi.signum != 1){
      throw new ArithmeticException();
    }
    BigInteger[][] vectors = new BigInteger[2][3];
    vectors[0][0] = ONE;
    vectors[0][1] = ZERO;
    vectors[0][2] = this.abs();
    vectors[1][0] = ZERO;
    vectors[1][1] = ONE;
    vectors[1][2] = bi;

    while(vectors[1][2].signum == 1){
      BigInteger q  = vectors[0][2].divide(vectors[1][2]);
      BigInteger t0 = vectors[0][0].subtract(vectors[1][0].multiply(q));
      BigInteger t1 = vectors[0][1].subtract(vectors[1][1].multiply(q));
      BigInteger t2 = vectors[0][2].subtract(vectors[1][2].multiply(q));
      vectors[0][0] = vectors[1][0];
      vectors[0][1] = vectors[1][1];
      vectors[0][2] = vectors[1][2];
      vectors[1][0] = t0;
      vectors[1][1] = t1;
      vectors[1][2] = t2;
    }
    if(!ONE.equals(vectors[0][2])){
      throw new ArithmeticException("modInverse cannot be found");
    }
    if(signum == -1){
      vectors[0][0] = vectors[0][0].negate();
    }
    return  vectors[0][0].mod(bi);
  }

/*
  public BigInteger modPow(BigInteger exp, BigInteger mod){
    
    if(exp.signum == -1){
      return this.modInverse(mod);
    }
    return this.pow(exp,mod).mod(mod);
            
  }
*/
  
  public BigInteger modPow(BigInteger exponent, BigInteger m) {
    if (m.signum != 1) {
      throw new ArithmeticException();
    }
    if(exponent.signum == -1){
      return this.modInverse(m);
    }
    int[] zVal = null;
    int[] yAccum = null;
    int[] yVal;

    int[] mmag = BigIntegerJava.makeMagnitude(m);
    int length = mmag.length;
    // Montgomery exponentiation is only possible if the modulus is odd,
    // but AFAIK, this is always the case for crypto algo's
    boolean useMonty = ((mmag[length - 1] & 1) == 1);
    long mQ = 0;
    if (useMonty) {
      mQ = BigIntegerJava.getMQuote(m);

      // tmp = this * R mod m
      zVal = BigIntegerJava.makeMagnitude(this.shiftLeft(32 * length).mod(m));

      useMonty = (zVal.length == length);

      if (useMonty) {
        yAccum = new int[length + 1];
      }
    }

    if (!useMonty) {
      int[] mag = BigIntegerJava.makeMagnitude(this);
      if (mag.length <= length) {

        zVal = new int[length];
        System.arraycopy(mag, 0, zVal, zVal.length - mag.length, mag.length);
      } else {
        //
        // in normal practice we'll never see this...
        //
        int[] tmp = BigIntegerJava.makeMagnitude(this.remainder(m));
        zVal = new int[length];

        System.arraycopy(tmp, 0, zVal, zVal.length - tmp.length,
            tmp.length);
      }

      yAccum = new int[length * 2];
    }

    yVal = new int[length];

    //
    // from LSW to MSW
    //
    int[] exp = BigIntegerJava.makeMagnitude(exponent);
    for (int i = 0; i < exp.length; i++) {
      int v = exp[i];
      int bits = 0;

      if (i == 0) {
        while (v > 0) {
          v <<= 1;
          bits++;
        }

        //
        // first time in initialise y
        //
        System.arraycopy(zVal, 0, yVal, 0, zVal.length);

        v <<= 1;
        bits++;
      }

      while (v != 0) {
        if (useMonty) {
          // Montgomery square algo doesn't exist, and a normal
          // square followed by a Montgomery reduction proved to
          // be almost as heavy as a Montgomery mulitply.
          BigIntegerJava.multiplyMonty(yAccum, yVal, yVal, mmag, mQ);
        } else {
          BigIntegerJava.square(yAccum, yVal);
          BigIntegerJava.remainder(yAccum, mmag);
          System.arraycopy(yAccum, yAccum.length - yVal.length, yVal, 0,
              yVal.length);
          BigIntegerJava.zero(yAccum);
        }
        bits++;

        if (v < 0) {
          if (useMonty) {
            BigIntegerJava.multiplyMonty(yAccum, yVal, zVal, mmag, mQ);
          } else {
            BigIntegerJava.multiply(yAccum, yVal, zVal);
            BigIntegerJava.remainder(yAccum, mmag);
            System.arraycopy(yAccum, yAccum.length - yVal.length, yVal, 0,
                yVal.length);
            BigIntegerJava.zero(yAccum);
          }
        }

        v <<= 1;
      }

      while (bits < 32) {
        if (useMonty) {
          BigIntegerJava.multiplyMonty(yAccum, yVal, yVal, mmag, mQ);
        } else {
          BigIntegerJava.square(yAccum, yVal);
          BigIntegerJava.remainder(yAccum, mmag);
          System.arraycopy(yAccum, yAccum.length - yVal.length, yVal, 0,
              yVal.length);
          BigIntegerJava.zero(yAccum);
        }
        bits++;
      }
    }

    if (useMonty) {
      // Return y * R^(-1) mod m by doing y * 1 * R^(-1) mod m
      BigIntegerJava.zero(zVal);
      zVal[zVal.length - 1] = 1;
      BigIntegerJava.multiplyMonty(yAccum, yVal, zVal, mmag, mQ);
    }

    return new BigInteger(1, toByteArray(yVal));
  }
    
  private static byte[] toByteArray(int[] val) {
    int length = val.length;
    byte[] bytes = new byte[length*4];
    for(int i=0, j=0 ; i < length ; i++) {
      int word = val[i];
      bytes[j++] = (byte)(word>>24); 
      bytes[j++] = (byte)(word>>16); 
      bytes[j++] = (byte)(word>>8); 
      bytes[j++] = (byte) word;       
    }
    return bytes;
  }
    
  public BigInteger pow(int exp){
    if(exp < 0){
      throw new ArithmeticException("exponent cannot be negative "+exp);
    }
    if(exp == 0){
      return ONE;
    }

    if(exp == 1){
      return this;
    }

    if(magnitude.length == 1){
      int num = magnitude[0];
      if(num == 1){
        return this;
      }
      if(num == -1){
        return ((exp%2) == 1 ? this : ONE);
      }
    }
    BigInteger result = this;

    //first locate the highest set bit ...
    int bit;
    for (bit = 1; bit <= exp; bit <<= 1);
    bit>>=2;
    //now lets calculate the 'this' to the power exp ...
    while(bit > 0){
      result = result.multiply(result);
      if((exp & bit) != 0){
        result = this.multiply(result);
      }
      bit>>=1;
    }
    return result;
  }
/*
  private BigInteger pow(BigInteger exp, BigInteger m){
    if(exp.signum == -1){
      throw new ArithmeticException("exponent cannot be negative "+exp);
    }
    if(exp.signum == 0){
      return ONE;
    }

    if(magnitude.length == 1){
      int num = magnitude[0];
      if(num == 1){
        return this;
      }
      if(num == -1){
        return ((exp.magnitude[exp.magnitude.length-1] & 0x01) == 1 ?this : ONE);
      }
    }

    if(exp.magnitude.length == 1 && exp.magnitude[0]== 1){
      return this;
    }

    BigInteger result = this;

    //first locate the highest set bit ...
    int bit = exp.bitLength() - 2;
    //now lets calculate the 'this' to the power exp ...
    while(bit >= 0){
      result = result.multiply(result).mod(m);
      if(exp.testBit(bit)){
        result = this.multiply(result).mod(m);
      }
      bit--;
    }
    return result;
  }
*/
  public String toString(){
    return toString(10);
  }

  public String toString(int rdx){
  	if (signum == 0) {
  	 	return "0";
  	}
  	if (signum == -1){
  	 	return '-'+negate().toString(rdx);
  	}	
  	
  	if(rdx < Character.MIN_RADIX || rdx > Character.MAX_RADIX){
  	  rdx = 10;
  	}

  	StringBuffer b = new StringBuffer();
  	byte[] bytes = (byte[])magnitude.clone();
  	int start = 0;
  	do {
    	b.insert(0,DIGIT_CHARS[oneByteDivision(rdx, bytes, start)]);
  	  start += (bytes[start] == 0 ? 1 : 0); 	  	
  	}	while(start < bytes.length);
  	return b.toString();
  }

// private convenience methods

  //private native static void arraycopy(byte[] src, int off, byte[] dst, int offdef, int length);

  /**
  ** this could be done native (and is now ) but in java ...
  ** this needed for String --> BigInteger
  */
  private void javaMultiply(byte[] val, byte[] bytes){
    int pos = bytes.length;

    for(int i = val.length -1 ; i >= 0 ; i--){
      int carry = 0;
      int valByte = 0xff & (char)val[i];
      for(int j = magnitude.length -1 ; j >= 0 ; j--){
        int index = pos + j - magnitude.length;
        carry = (carry>>8);
        carry += valByte * (0xff & (char)magnitude[j]);
        carry += (0xff & (char)bytes[index]);
        bytes[index] = (byte)carry;
      }
      pos--;
      bytes[pos - magnitude.length] = (byte)(carry>>8);
    }
  }

  private byte[] longToBytes(long l){
    byte[] bytes = new byte[8];

    for (int i=7 ; i > -1 ; i--){
     	bytes[i] = (byte)l;
     	l = l>>>8;
    }
    return bytes;
  }

  /**
  ** normelizes the magnitude (will strip all leading zero bytes)
  ** this functions threats the bytes in magnitude as if the belonged to a positive
  ** BigInteger. If clone is true the magnitude is replaced in any case.
  */
  private void normelizeMagnitude(boolean clone){
    int l = magnitude.length;
    int i=0;

  	while (i<l){
  		if(magnitude[i] != 0){
  		  break;
  		}
  		i++;
  	}
    if (i != 0 || clone){
      if(i == l){
        signum = 0;
        magnitude = ZERO.magnitude;
      }
      else{
        byte[] bytes = new byte[l-i];
        System.arraycopy(magnitude,i,bytes,0,l-i);
        magnitude = bytes;
        addLeadingByte();
      }
    }
  }
  
  // if sign positive and magnitude[0] starts with 1
  // add a zero byte
  // if sign negative and magnitude[0] starts with 0
  // add 0xff byte
  private void addLeadingByte() {
    if (signum == 1) {
      if (magnitude[0]>>>7 > 0) {
         byte[] bytes = new byte[magnitude.length+1];
         bytes[0] = 0;
         System.arraycopy(magnitude,0,bytes,1,magnitude.length);
         magnitude = bytes;
      }
    }
    else if (signum == -1) {
      if (magnitude[0]>>>7 == 0) {
         byte[] bytes = new byte[magnitude.length+1];
         bytes[0] = -1;
         System.arraycopy(magnitude,0,bytes,1,magnitude.length);
         magnitude = bytes;
      }
    }
  }

  private void addLong(long l){
    int carry=0;
    int i = magnitude.length-1;

    for (int j=0 ; j < 8 ; i--,j++){
      int overflow = (0xff & ((int)l)) + (0xff & (char)magnitude[i]) + carry;
      carry = (0x0100 & overflow)>>8;
      magnitude[i] = (byte) overflow;
      l = (l>>8);

    }
    for ( ; i >= 0 && carry > 0; i--){
      int overflow = (0x0ff & (char)magnitude[i]) + carry;
      carry = (0x0100 & overflow)>>8;
      magnitude[i] = (byte) overflow;
    }
  }

  /**
  ** divide by a one byte value (used int toString())
  ** b should be positive ...
  ** bytes contains the dividend and should have a length > 0.
  ** the start parameter is added so we can reuss the byte array 'bytes' in the toString method
  */
  private int oneByteDivision(int b, byte[] bytes, int start){
    int remain = (0xff & (char) bytes[start]);
    if(remain >= b){
      remain = 0;
    }
    else {
      bytes[start++] = (byte)0;
    }
    for(int i = start; i < bytes.length ; i++){
      remain = ((0xff & (char)bytes[i]) | (remain<<8) );
      bytes[i] = (byte)(remain/b);
      remain = remain % b;
    }
    return remain;
  }

  /** only call this method to create a larger byte[] than magnitude */
  private byte[] createByteArray(int nrOfBits){
   	int bts = (nrOfBits+1)/8 + 1;	
    byte [] bytes = new byte [bts];
    System.arraycopy(magnitude,0, bytes,bts-magnitude.length,magnitude.length);
    if (signum == -1){
    	bts -= magnitude.length;
     	for (int i=0 ; i < bts ; i++){
     	 	bytes[i] = (byte)(0xff);
     	}
    }
    return bytes;
  }

  private void initWithByteArray(byte [] bval, boolean clone){
    int l = bval.length;
    if (l == 0){
     	throw new NumberFormatException("byte array has length null");
    }
    int i=0;
    if(bval[0] < 0){
    	signum = -1;	
     	while (i<l-1){
     		if(bval[i] != -1){
     		  	break;
     		}
     		i++;
     	}
    }
    else {
    	while (i<l){
    		if(bval[i] != 0){
    		  	break;
    		}
    		i++;
    	}
    	signum =  (i==l ? 0 : 1);		 	
    }
    if (clone || i!=0){
      magnitude = new byte[l-i];
      System.arraycopy(bval,i,magnitude,0,l-i);
    }
    else {
      magnitude = bval;
    }		
    // add zero byte if needed
    addLeadingByte();    
  }

  private native byte[] nativeNegateBytes();
}
