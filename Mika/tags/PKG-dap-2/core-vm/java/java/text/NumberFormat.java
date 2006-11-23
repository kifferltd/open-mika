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
** $Id: NumberFormat.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

import java.util.Locale;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class NumberFormat extends Format implements Cloneable {

  public static final int FRACTION_FIELD = 1;
  public static final int INTEGER_FIELD = 0;

  public static Locale[] getAvailableLocales() {
    return new Locale[0];
  }

  public static final NumberFormat getCurrencyInstance() {
    return getCurrencyInstance(Locale.getDefault());
  }

  public static NumberFormat getCurrencyInstance(Locale loc) {
    DecimalFormatSymbols dfs = new DecimalFormatSymbols(loc);
    DecimalFormat df = new DecimalFormat(dfs);
    dfs.setDecimalSeparator(dfs.getMonetaryDecimalSeparator());
    df.setNegativeSuffix(")");
    String cur = dfs.getCurrencySymbol();
    df.setPositivePrefix(cur);
    df.setNegativePrefix("("+cur);
    NumberFormat nf = df;
    nf.minimumIntegerDigits = 1;
    nf.maximumIntegerDigits = 340;
    nf.maximumFractionDigits = 2;
    nf.minimumFractionDigits = 2;
    nf.groupingUsed = true;
    return df;
  }

  public static final NumberFormat getInstance() {
    return getInstance(Locale.getDefault());
  }

  public static NumberFormat getInstance(Locale loc){
    DecimalFormatSymbols dfs = new DecimalFormatSymbols(loc);
    NumberFormat nf = new DecimalFormat(dfs);
    nf.minimumIntegerDigits = 1;
    nf.maximumIntegerDigits = 340;
    nf.maximumFractionDigits = 3;
    nf.minimumFractionDigits = 0;
    //nf.groupingUsed = true;
    return nf;
  }

  public static final NumberFormat getNumberInstance() {
    return getInstance(Locale.getDefault());
  }

  public static NumberFormat getNumberInstance(Locale loc){
    return getInstance(loc);
  }

  public static final NumberFormat getPercentInstance() {
    return getPercentInstance(Locale.getDefault());
  }

  public static NumberFormat getPercentInstance(Locale loc){
    DecimalFormatSymbols dfs = new DecimalFormatSymbols(loc);
    DecimalFormat df = new DecimalFormat(dfs);
    df.setMultiplier(100);
    String per = ""+dfs.getPercent();
    df.setPositiveSuffix(per);
    df.setNegativeSuffix(per);
    df.setDecimalSeparatorAlwaysShown(false);
    NumberFormat nf = df;
    nf.minimumIntegerDigits = 1;
    nf.maximumIntegerDigits = 340;
    nf.minimumFractionDigits = 0;
    nf.maximumFractionDigits = 0;
    nf.groupingUsed = true;
    nf.parseIntegerOnly =true;
    return df;
  }

  private static final long serialVersionUID = -2308460125733713944L;

//dictated by serialized form ...
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    if(serialVersionOnStream < 1){
      serialVersionOnStream = 1;
      maximumFractionDigits = maxFractionDigits;
      maximumIntegerDigits = maxIntegerDigits;
      minimumFractionDigits = minFractionDigits;
      minimumIntegerDigits = minIntegerDigits;
    }
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    int max = Byte.MAX_VALUE;
    maxFractionDigits = (byte)(maximumFractionDigits > max ? max : maximumFractionDigits);
    maxIntegerDigits = (byte)(maximumIntegerDigits > max ? max : maximumIntegerDigits);
    minFractionDigits = (byte)(minimumFractionDigits > max ? max : minimumFractionDigits);
    minIntegerDigits = (byte)(minimumIntegerDigits > max ? max : minimumIntegerDigits);
    out.defaultWriteObject();

}

  private boolean groupingUsed;
  private byte maxFractionDigits;
  int maximumFractionDigits;
  int maximumIntegerDigits;
  private byte maxIntegerDigits;
  private byte minFractionDigits;
  int minimumFractionDigits;
  int minimumIntegerDigits;
  private byte minIntegerDigits;
  private boolean parseIntegerOnly;
  private int serialVersionOnStream = 1;

  public NumberFormat() { }

  public abstract StringBuffer format(double number, StringBuffer buf, FieldPosition pos);
  public abstract StringBuffer format(long number, StringBuffer buf, FieldPosition pos);
  public abstract Number parse(String s, ParsePosition pos);

  public Object clone() {
    return super.clone();
  }
  
  public boolean equals(Object obj) {
    if (!(obj instanceof NumberFormat)){
      return false;
    }
    NumberFormat nf = (NumberFormat)obj;
    return this.groupingUsed == nf.groupingUsed
        && this.parseIntegerOnly == nf.parseIntegerOnly
        && this.maximumFractionDigits == nf.maximumFractionDigits
        && this.maximumIntegerDigits == nf.maximumIntegerDigits
        && this.minimumFractionDigits == nf.minimumFractionDigits
        && this.minimumIntegerDigits == nf.minimumIntegerDigits;
  }
  
  public final String format(double number) {
    return format(number, new StringBuffer(), new FieldPosition(FRACTION_FIELD)).toString();
  }

  public final String format(long number) {
    return format(number, new StringBuffer(), new FieldPosition(INTEGER_FIELD)).toString();
  }

  public final StringBuffer format(Object number, StringBuffer buf, FieldPosition pos){
    if(number instanceof Long){
      return format(((Long)number).longValue(), buf, pos);
    }
    else if (number instanceof Double){
      return format(((Double)number).doubleValue(), buf, pos);
    }
    else if(number instanceof Number){
      return format(((Number)number).doubleValue(), buf, pos);
    }
    throw new IllegalArgumentException("Got Bad Class : " + number.getClass());
  }
  

  public int getMaximumFractionDigits(){
    return maximumFractionDigits;
  }

  public int getMaximumIntegerDigits(){
    return maximumIntegerDigits;
  }

  public int getMinimumFractionDigits(){
    return minimumFractionDigits;
  }

  public int getMinimumIntegerDigits(){
    return minimumIntegerDigits;
  }

  public int hashCode(){
    int hash = (groupingUsed ? 0xffff0000 : 0);
    hash ^= (parseIntegerOnly ? 0x0ffff : 0);
    return hash ^ maximumFractionDigits ^ maximumIntegerDigits ^ minimumFractionDigits ^ minimumIntegerDigits;
  }

  public boolean isGroupingUsed(){
     return groupingUsed;
  }

  public boolean isParseIntegerOnly(){
    return parseIntegerOnly;
  }

  public Number parse(String s) throws ParseException {
    ParsePosition pos = new ParsePosition(0);
    Number n = parse(s, pos);
    if(n == null){
      throw new ParseException("error during parsing",pos.getErrorIndex());
    }
    return n;
  }

  public final Object parseObject(String srcStr, ParsePosition pos){
    return parse(srcStr,pos);
  }

  public void setGroupingUsed(boolean use){
    groupingUsed = use;
  }

  public void setParseIntegerOnly(boolean only){
    parseIntegerOnly = only;
  }

  public void setMaximumFractionDigits(int val){
    if(val < 0){
      val = 0;
    }
    if(val < minimumFractionDigits){
      minimumFractionDigits = val;
    }
    maximumFractionDigits = val;
  }

  public void setMaximumIntegerDigits(int val){
    if(val < 0){
      val = 0;
    }
    if(val < minimumIntegerDigits){
      minimumIntegerDigits = val;
    }
    maximumIntegerDigits = val;
  }

  public void setMinimumFractionDigits(int val){
    if(val < 0){
      minimumFractionDigits = 0;
    }
    else {
      if (val > maximumFractionDigits){
        maximumFractionDigits = val;
      }
      minimumFractionDigits = val;
    }
  }

  public void setMinimumIntegerDigits(int val){
    if(val < 0){
      minimumIntegerDigits = 0;
    }
    else {
      if(val > maximumIntegerDigits){
        maximumIntegerDigits = val;
      }
      minimumIntegerDigits = val;
    }
  }
}

