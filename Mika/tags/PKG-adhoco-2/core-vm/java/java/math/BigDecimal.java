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
 * $Id: BigDecimal.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
 */

package java.math;

public class BigDecimal extends Number implements Comparable {

  public static final int ROUND_UP          = 0;
  public static final int ROUND_DOWN        = 1;
  public static final int ROUND_CEILING     = 2;
  public static final int ROUND_FLOOR       = 3;
  public static final int ROUND_HALF_UP     = 4;
  public static final int ROUND_HALF_DOWN   = 5;
  public static final int ROUND_HALF_EVEN   = 6;
  public static final int ROUND_UNNECESSARY = 7;

  private static final BigInteger TEN = new BigInteger("10");

  private static final long serialVersionUID = 6108874887143696463L;

  /** Field dictated by Serialized Form */
  private int scale;
  /** Field dictated by Serialized Form */
  private BigInteger intVal;

  public BigDecimal(BigInteger bi){
    this(bi,0);
  }

  public BigDecimal(BigInteger bi, int scl) throws NumberFormatException {
    if(bi == null){
      throw new NullPointerException();
    }
    if (scl < 0){
      throw new NumberFormatException("scale cannot be negative");
    }
    intVal = bi;
    scale = scl;
  }

  public BigDecimal(String val) throws NumberFormatException {
    int dot = val.indexOf('.');
    if (dot != -1){
      scale = val.length()-1-dot;
      val = val.substring(0,dot)+val.substring(dot+1);
    }
    intVal = new BigInteger(val,10);
    if(scale < 0){
      throw new NumberFormatException("problem constructing "+val);
    }
  }

  public BigDecimal(double d) throws NumberFormatException {
    if (Double.isNaN(d) || Double.isInfinite(d)){
      throw new NumberFormatException("double value cannot be infinite or NaN "+d);
    }
    String val = Double.toString(d);
    int exp = val.indexOf('E');
    if(exp != -1){
      scale = Integer.parseInt(val.substring(exp+1));
    }
    int dot = val.indexOf('.');
    StringBuffer buf = new StringBuffer(val.substring(0,dot));
    String decPart = val.substring(dot+1,(exp == -1 ? val.length(): exp));

    if(decPart.length() > 1 || !decPart.startsWith("0")){
      buf.append(decPart);
    }
    else{
      dot++;
    }
    intVal = new BigInteger(buf.toString());
    scale = (exp == -1 ? val.length(): exp) - dot - 1 - scale;
    if(scale < 0){
      intVal = intVal.multiply(intToBig(-scale));
      scale = 0;
    }
  }

  public BigDecimal abs(){
    if (intVal.signum() == -1){
      return new BigDecimal(intVal.negate(),scale);
    }
    return this;
  }

  public BigDecimal add(BigDecimal val){
    BigInteger a = val.intVal;
    BigInteger b = intVal;
    if (scale != val.scale){
      if(scale > val.scale){
        a = a.multiply(intToBig(scale-val.scale));
      }else{
        b = b.multiply(intToBig(val.scale-scale));
      }
    }
    return new BigDecimal(b.add(a),scale > val.scale ? scale : val.scale);
  }

  public int compareTo (Object o) {
    return compareTo((BigDecimal)o);
  }

  public int compareTo (BigDecimal o) {
    int otherSign = o.intVal.signum();
    int thisSign = intVal.signum();
    if(thisSign != otherSign){
      return (thisSign == 0 ? -otherSign : thisSign);
    }
    if(thisSign == 0){
      return 0;
    }
    //both BigDecimal have the same sign and are non-zero
    BigInteger a = o.intVal;
    BigInteger b = intVal;
    if (scale != o.scale){
      if(scale > o.scale){
        a = a.multiply(intToBig(scale-o.scale));
      }else{
        b = b.multiply(intToBig(o.scale-scale));
      }
    }
    return b.compareTo(a);
  }

  public BigDecimal divide(BigDecimal val,int roundMode){
    return divide(val, this.scale, roundMode);
  }

  public BigDecimal divide(BigDecimal val, int newScale ,int roundMode){
    if(roundMode < 0 || roundMode > ROUND_UNNECESSARY){
      throw new IllegalArgumentException("invalid roundingMode");
    }
    if(newScale < 0){
      throw new ArithmeticException("negative scale is not allowed");
    }
    int rescale = val.scale + newScale - scale;
    BigInteger[] big = (rescale >= 0) ? intVal.multiply(intToBig(rescale)).divideAndRemainder(val.intVal):
      intVal.divideAndRemainder(val.intVal.multiply(intToBig(-rescale)));

    switch(roundMode){
      case ROUND_UP:
        if(!big[1].equals(BigInteger.ZERO)){
          big[0] = big[0].add((intVal.signum() == val.intVal.signum()) ? BigInteger.ONE : BigInteger.bigNegOne);
        }
        break;
      case ROUND_DOWN:
        break;
      case ROUND_CEILING:
        if(intVal.signum() == val.intVal.signum() && !big[1].equals(BigInteger.ZERO)){
          big[0] = big[0].add(BigInteger.ONE);
        }
        break;
      case ROUND_FLOOR:
        if(intVal.signum() != val.intVal.signum() && !big[1].equals(BigInteger.ZERO)){
          big[0] = big[0].add(BigInteger.bigNegOne);
        }
        break;
      case ROUND_UNNECESSARY:
        if(!big[1].equals(BigInteger.ZERO)){
          throw new ArithmeticException("Rounding was Needed");
        }
        break;
      default:
        BigInteger[] remains = big[1].multiply(TEN).divideAndRemainder(val.intVal);
        String remain = remains[0].abs().toString(10);
        char ch = remain.charAt(0);
        if(ch >= '5'){
          if (ch == '5' && remains[1].equals(BigInteger.ZERO)){
            switch(roundMode){
              case ROUND_HALF_UP:
                big[0] = big[0].add((intVal.signum() == val.intVal.signum()) ? BigInteger.ONE : BigInteger.bigNegOne);
                break;
              case ROUND_HALF_DOWN :
                break;
              case ROUND_HALF_EVEN:
                if(big[0].testBit(0)){
                  big[0] = big[0].add((intVal.signum() == val.intVal.signum()) ? BigInteger.ONE : BigInteger.bigNegOne);
                }
                break;
            }
          }
          else {
            big[0] = big[0].add((intVal.signum() == val.intVal.signum()) ? BigInteger.ONE : BigInteger.bigNegOne);
          }
        }
    }//end switch
    return new BigDecimal(big[0],newScale);
  }

  public double doubleValue() {
    if(scale == 0){
      return intVal.doubleValue();
    }
    int offset = intVal.signum() == -1 ? 1 : 0;
    String string = intVal.toString(10);
    StringBuffer buf = new StringBuffer(string);
    int length = buf. length() - offset;
    if(length <= scale){
      buf.insert(offset+1,'.');
      if(length > 17){
        buf.setLength(offset+18);
      }
      if(scale > length){
        buf.append("E-");
        buf.append(scale + 1 - length);
      }
    }
    else {
      if(length > 17){
        buf.insert(offset+1,'.');
        buf.setLength(offset+18);
        buf.append('E');
        buf.append(length-scale);
      }
      else{
        buf.insert(length + offset - scale,'.');
      }
    }
    try {
      return Double.parseDouble(buf.toString());
    }
    catch (NumberFormatException nfe){
      return (intVal.signum() == -1 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
    }
  }

  public boolean equals(Object o){
    if (!(o instanceof BigDecimal)){
            return false;
    }
    BigDecimal bd = (BigDecimal)o;
    return (scale == bd.scale && intVal.equals(bd.intVal));
  }

  public float floatValue() {
    return (float)doubleValue();
  }

  public int hashCode(){
    return scale ^ intVal.hashCode();
  }

  public int intValue() {
    return (int)longValue();
  }

  public long longValue() {
    if(scale == 0){
      return intVal.longValue();
    }
    return this.setScale(0,ROUND_DOWN).intVal.longValue();
  }

  public BigDecimal max(BigDecimal val){
    return (compareTo(val) > 0 ? this : val);
  }

  public BigDecimal min(BigDecimal val){
    return (compareTo(val) < 0 ? this : val);
  }

  public BigDecimal movePointLeft(int n){
    if(n <= 0){
      return movePointRight(-n);
    }
    return new BigDecimal(intVal,scale + n);
  }

  public BigDecimal movePointRight(int n){
    if (n==0){
      return this;
    }
    if(n < 0){
      return movePointLeft(-n);
    }
    int newScale = scale - n;
    if(newScale < 0){
      return new BigDecimal(intVal.multiply(intToBig(-newScale)),0);
    }
    return new BigDecimal(intVal, newScale);
  }

  public BigDecimal multiply(BigDecimal val){
    return new BigDecimal(intVal.multiply(val.intVal),scale+val.scale);
  }

  public BigDecimal negate(){
    return new BigDecimal(intVal.negate(),scale);
  }

  public int scale(){
    return scale;
  }

  public BigDecimal setScale(int newScale){
    return setScale(newScale,ROUND_UNNECESSARY);
  }

  public BigDecimal setScale(int newScale, int roundMode){
    if (newScale < 0){
      throw new ArithmeticException();
    }
    if (newScale == scale){
      return this;
    }
    if(newScale > scale){
      return new BigDecimal(intVal.multiply(intToBig(newScale - scale)),newScale);
    }

    if(roundMode < 0 || roundMode > ROUND_UNNECESSARY){
      throw new IllegalArgumentException("bad Rounding mode");
    }

    BigInteger[] big = intVal.divideAndRemainder(intToBig(scale-newScale));

    switch(roundMode){
      case ROUND_UP:
        if(!big[1].equals(BigInteger.ZERO)){
          big[0] = big[0].add((intVal.signum() == 1) ? BigInteger.ONE : BigInteger.bigNegOne);
        }
        break;
      case ROUND_DOWN:
        break;
      case ROUND_CEILING:
        if(intVal.signum() == 1 && !big[1].equals(BigInteger.ZERO)){
          big[0] = big[0].add(BigInteger.ONE);
        }
        break;
      case ROUND_FLOOR:
        if(intVal.signum() == -1 && !big[1].equals(BigInteger.ZERO)){
          big[0] = big[0].add(BigInteger.bigNegOne);
        }
        break;
      case ROUND_UNNECESSARY:
        if(!big[1].equals(BigInteger.ZERO)){
          throw new ArithmeticException();
        }
        break;
      default:
        String remain = big[1].abs().toString(10);
        char ch = remain.charAt(0);
        if(ch >= '5'){
          if (ch == '5' && remain.replace('0',' ').trim().length() == 1){
            switch(roundMode){
              case ROUND_HALF_UP:
                big[0] = big[0].add((intVal.signum() == 1) ? BigInteger.ONE : BigInteger.bigNegOne);
                break;
              case ROUND_HALF_DOWN :
                break;
              case ROUND_HALF_EVEN:
                if(big[0].testBit(0)){
                  big[0] = big[0].add((intVal.signum() == 1) ? BigInteger.ONE : BigInteger.bigNegOne);
                }
                break;
            }
          }
          else {
            big[0] = big[0].add((intVal.signum() == 1) ? BigInteger.ONE : BigInteger.bigNegOne);
          }
        }
    }
    return new BigDecimal(big[0], newScale);
  }

  public int signum(){
    return intVal.signum();
  }

  public BigDecimal subtract(BigDecimal val){
    //TODO rework ...
    return add(val.negate());
  }

  public static BigDecimal valueOf(long val){
    return valueOf(val,0);
  }

  public static BigDecimal valueOf(long val, int scale){
    return new BigDecimal(BigInteger.valueOf(val),scale);
  }

  public BigInteger toBigInteger(){
    if (scale == 0){
      return intVal;
    }
    return this.setScale(0,ROUND_DOWN).intVal;
  }

  public String toString(){
    if (scale == 0) {
      return intVal.toString();
    }
    String val = intVal.abs().toString();
    StringBuffer buf = new StringBuffer(val.length()+1);
    int start;
    if (intVal.signum() == -1){
      buf.append('-');
    }
    int dot = val.length() - scale;
    if (dot > 0 ){
      buf.append(val.substring(0,dot));
      buf.append('.');
      buf.append(val.substring(dot));
    }
    else {
      buf.append("0.");
      for ( ; dot < 0 ; dot++){
        buf.append('0');
      }
      buf.append(val.substring(0));
    }
    return buf.toString();
  }

  private BigInteger intToBig(int i){
    return TEN.pow(i);
  }

   public BigInteger unscaledValue() {
    return intVal;
  }

}

