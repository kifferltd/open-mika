/*************************************************************************
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
** $Id: DecimalFormat.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

//import java.util.Arrays;
//import java.util.StringTokenizer;

import java.io.IOException;
import java.io.ObjectInputStream;

public class DecimalFormat extends NumberFormat {

  private static final DecimalFormatSymbols DEFAULT = new DecimalFormatSymbols();

  /**
  ** dictated by the serialized form ...
  */
  private static final long serialVersionUID = 864413376551465018L;

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    if(serialVersionOnStream < 1){
      useExponentialNotation = false;
      serialVersionOnStream = 1;
    }
  }

  private boolean decimalSeparatorAlwaysShown;
  private byte groupingSize = 3;
  private byte minExponentDigits;
  private int multiplier = 1;
  private String negativePrefix = "-";
  private String negativeSuffix = "";
  private String positivePrefix = "";
  private String positiveSuffix = "";
  private int serialVersionOnStream = 1;
  private DecimalFormatSymbols symbols;
  private boolean useExponentialNotation;

  public DecimalFormat(){
    symbols = new DecimalFormatSymbols();
    super.setMaximumFractionDigits(340);
    super.setMaximumIntegerDigits(309);
    super.setMinimumIntegerDigits(1);
  }

  DecimalFormat(DecimalFormatSymbols dfs){
    symbols = dfs;
  }

  public DecimalFormat(String pattern){
    this(pattern, new DecimalFormatSymbols());
  }

  public DecimalFormat(String pattern, DecimalFormatSymbols dfs){
    applyPattern(pattern, dfs);
    super.setMaximumIntegerDigits(309);
    symbols = dfs;
    //debug(pattern);
  }

  public void applyLocalizedPattern(String pattern){
    applyPattern(pattern, symbols);
    //debug(pattern);
  }

  public void applyPattern(String pattern){
    applyPattern(pattern, DEFAULT);
    //debug(pattern);
  }

  public Object clone(){
    DecimalFormat df = (DecimalFormat) super.clone();
    df.symbols = (DecimalFormatSymbols) symbols.clone();
    return df;
  }

  public boolean equals(Object o){
    if(!(o instanceof DecimalFormat)){
      return false;
    }
    DecimalFormat df = (DecimalFormat)o;
    return this.symbols.equals(df.symbols)
        && this.decimalSeparatorAlwaysShown == df.decimalSeparatorAlwaysShown
        && this.minExponentDigits == df.minExponentDigits
        && this.multiplier == df.multiplier
        && this.negativePrefix.equals(df.negativePrefix)
        && this.negativeSuffix.equals(df.negativeSuffix)
        && this.positivePrefix.equals(df.positivePrefix)
        && this.positiveSuffix.equals(df.positiveSuffix)
        && this.useExponentialNotation == df.useExponentialNotation;
  }

  public StringBuffer format(double number, StringBuffer dest, FieldPosition pos){
    //TODO: take min en max digits into account !!!
    //System.out.println("formatting number '"+number+"'");
    boolean neg = number < 0.0;
    double d = 0.5 * Math.pow(10,-getMaximumFractionDigits());
    number = (number * multiplier) + (neg ? -d : d);
    //System.out.println("formatting number '"+number+"' "+getMaximumFractionDigits());
    String value = String.valueOf(number);
    if(neg){
      value = value.substring(1);
    }
    int p = value.indexOf('E');
    if(useExponentialNotation){
      if(p == -1){
        value = insertExpononent(value);
      }
      return formatExponent(value, dest, pos, neg);
    }
    else if(p != -1){// we don't need a an Expononent
      StringBuffer buf = new StringBuffer(128);
      int exp = Integer.parseInt(value.substring(p+1));
      p = value.indexOf('.');
      if(exp < 0){
        //we have p digits before the '.'
        int add = 1-p-exp;
        addChars('0', add, buf);
        buf.append(value);
        p += (add < 0 ? 0 : add);
        buf.deleteCharAt(p);
        buf.insert(p+exp,'.');
      }
      else {
        int add = exp + value.length() - p;
        buf.append(value);
        addChars('0', add, buf);
        buf.deleteCharAt(p);
        buf.insert(p+exp,'.');
      }
      value = buf.toString();
    }
    return format(value, dest, pos, neg);
  }

  public StringBuffer format(long number, StringBuffer dest, FieldPosition pos){
    //System.out.println("formatting number '"+number+"'");
    boolean neg = number < 0;
    String value = String.valueOf(number * multiplier)+'.';
    if(neg){
      value = value.substring(1);
    }
    if(useExponentialNotation){
      formatExponent(insertExpononent(value), dest, pos, neg);
    }
    return format(value, dest, pos, neg);
  }

  private StringBuffer format(String number, StringBuffer dest, FieldPosition pos, boolean neg){
    //System.out.println("formatting number '"+number+"'");
    dest.append((neg ? negativePrefix : positivePrefix));
    int start = dest.length();
    int p = number.indexOf('.');
    int min = getMinimumIntegerDigits();
    if(min > p){ //we need to add zeros
      addChars(symbols.getZeroDigit(), min - p, dest);
      dest.append(number);
      p = min;
    }
    else {
      dest.append(number);
      int max = getMaximumIntegerDigits();
      if(max < p){
        dest.delete(start,start + p - max);
        p = max;
      }
    }

    if(min == 0 && p == 1 && dest.charAt(start) == '0'){
      p--;
      dest.deleteCharAt(start);
    }

    if(isGroupingUsed()){
      min = (p-1) / groupingSize;
      int in = p+start;
      char ch = symbols.getGroupingSeparator();
      for(int i = 0 ; i < min ; i++){
        in -= groupingSize;
        dest.insert(in,ch);
      }
      p += min;
    }

    min = getMinimumFractionDigits();
    int frac = dest.length() - start - p - 1;
    if(min > frac){ //we need to add zeros
      addChars(symbols.getZeroDigit(), min - frac, dest);
    }
    else {
      int max = getMaximumFractionDigits();
      if(max < frac){

        dest.setLength(start + p + max + 1);
      }
    }
    if(!decimalSeparatorAlwaysShown){//check and remove dot if needed
      boolean cut = true;
      for(int i = p+1+start ; i < dest.length() ; i++){
        if(dest.charAt(i) != '0'){
          cut = false;
          break;
        }
      }
      if(cut){
        dest.setLength(p+start);
      }
    }

    switch(pos.getField()){
      case FRACTION_FIELD:
        pos.setBeginIndex(start+p+1);
        pos.setEndIndex(dest.length());
        break;
      case INTEGER_FIELD:
        pos.setBeginIndex(start);
        pos.setEndIndex(start+p);
        break;
      default:
    }
    int stop = dest.length();
    dest.append((neg ? negativeSuffix : positiveSuffix));
    return dest;
  }

/**
** for now scientific format is not supported
*/
  private StringBuffer formatExponent(String number, StringBuffer dest, FieldPosition pos, boolean neg){
    //TODO if needed ...
     throw new UnsupportedOperationException("No exponential formats supported yet");
  }

/**
** for now scientific format is not supported
*/
  private String insertExpononent(String s){
    //TODO if needed ...
    return s;
  }

  public DecimalFormatSymbols getDecimalFormatSymbols(){
    return symbols;
  }

  public int getGroupingSize(){
    return groupingSize;
  }

  public int getMultiplier(){
    return multiplier;
  }

  public String getNegativePrefix(){
    return negativePrefix;
  }

  public String getNegativeSuffix(){
    return negativeSuffix;
  }

  public String getPositivePrefix(){
    return positivePrefix;
  }

  public String getPositiveSuffix(){
    return positiveSuffix;
  }

  public int hashCode(){
    int hash = (decimalSeparatorAlwaysShown ? 0xaaaaaaaa :  0x55555555);
    hash ^=  (useExponentialNotation ? 0xcccccccc : 0x33333333);
    return symbols.hashCode() ^ minExponentDigits ^ multiplier
         ^ negativePrefix.hashCode() ^ negativeSuffix.hashCode()
         ^ positivePrefix.hashCode() ^ positiveSuffix.hashCode();
  }

  public boolean isDecimalSeparatorAlwaysShown(){
    return decimalSeparatorAlwaysShown;
  }

  public Number parse(String str, ParsePosition pos){
    Number Num = null;
    //System.out.println("parsing '"+str+"'");

    int p = pos.getIndex();
    int len = str.length();

    while(p < len){
      if(Character.isWhitespace(str.charAt(p))){
        p++;
      }
      else{
        break;
      }
    }
    try {
      //first try to find out if we have a prefix ..
      boolean positive = true;
      boolean returnlong = true;
      int preflen = -1;
      if(str.regionMatches(p, positivePrefix, 0, positivePrefix.length())){
        //System.out.println("matched positivePrefix '"+positivePrefix+"'");
        preflen = positivePrefix.length();
      }
      if(str.regionMatches(p, negativePrefix, 0, negativePrefix.length())){
        //System.out.println("matched positivePrefix '"+positivePrefix+"'");
        if(preflen < negativePrefix.length()){
          positive = false;
          preflen = negativePrefix.length();
        }
      }
      if(preflen == -1){
        //System.out.println("no valid Prefix found ...");
        pos.setErrorIndex(p);
      }
      else {
        p += preflen;
        //System.out.println("found prefix '"+str.substring(0,preflen)+"' "+p+" "+positive);
        StringBuffer buf = new StringBuffer(64);
        p = parseIntegerPart(buf,str,p);
        //System.out.println("found Integer part '"+buf+"' "+p);
        if(p < 0){
          pos.setErrorIndex(-p);
        }
        else {
          //parse Fraction ...
          if(p < len && str.charAt(p) == symbols.getDecimalSeparator()){
            int start = ++p;
            buf.append('.');
            int count = 0;
            while(p< len && count < maximumFractionDigits){
              char ch = str.charAt(p);
              //System.out.println("char is "+ch+", pos = "+p+", count = "+count+" string = "+str);
              if(ch >= '0' && ch <= '9'){
                buf.append(ch);
                count++;
                p++;
              }
              else {
                break;
              }
            }
            //check minimum ...
            if(count < minimumFractionDigits){
              pos.setErrorIndex(p);
              //System.out.println("returning ERROR -- fraction to small size = "+count+", but needed "+minimumFractionDigits);
              return null;
            }

            if(start < p){
              returnlong = false;
            }
            else{
              buf.setLength(buf.length()-1);
            }
          }

          //System.out.println("found number part '"+buf+"' "+p);
          //parse Suffix ...
          preflen = -1;
          //System.out.println("string '"+str+"' "+p+", '"+positivePrefix+"'");
          //System.out.println("trying to match '"+str.substring(p)+"' ? '"+positiveSuffix+"' , '"+negativeSuffix+"'");
          if(str.regionMatches(p, positiveSuffix, 0, positiveSuffix.length())){
            //System.out.println("matched positiveSuffix '"+positiveSuffix+"'");
            if(!positive && !positiveSuffix.equals(negativeSuffix) && positiveSuffix.length() > 0){
              pos.setErrorIndex(p);
              //System.out.println("returning ERROR");
              return null;
            }
            preflen = positiveSuffix.length();
          }
          if(str.regionMatches(p, negativeSuffix, 0, negativeSuffix.length())){
            //System.out.println("matched negativeSuffix '"+negativeSuffix+"'");
            if(preflen < negativeSuffix.length()){
              if(positive && !positivePrefix.equals(negativePrefix)){
                pos.setErrorIndex(p);
                return null;
              }
              positive = false;
              preflen = negativeSuffix.length();
            }
          }
          if(preflen == -1){
            //System.out.println("no valid Suffix found ...");
            pos.setErrorIndex(p);
            return null;
          }
          p += preflen;
          if(!positive){
            buf.insert(0,'-');
          }
          if(returnlong){
            Num = new Long(Long.parseLong(buf.toString())*multiplier);
          }
          else{
            Num = new Double(Double.parseDouble(buf.toString())*multiplier);
          }
          pos.setIndex(p);
        }
      }
    }
    catch(RuntimeException rt){
     pos.setErrorIndex(p);
     //System.out.println("Parsing failed returning 'null'");
     //rt.printStackTrace();
     return null;
    }
    return Num;
  }

  private int parseIntegerPart(StringBuffer buf, String number, int pos){
    int len = number.length();
    int count=0;
    if(isGroupingUsed()){
      //System.out.println("grouping is used ... "+pos);
      char sep = symbols.getGroupingSeparator();
      int group = 0;
      boolean first = true;
      while(pos < len && count < maximumIntegerDigits){
        char ch = number.charAt(pos);
        if(ch >= '0' && ch <= '9'){
          if(group == groupingSize){
            return -pos;
          }
          buf.append(ch);
          count++;
          group++;
        }
        else if(ch == sep){
          if(first || group == groupingSize){
            first = false;
            group = 0;
          }
          else{
            return -pos;
          }
        }
        else {
          break;
        }
        pos++;
      }
    }
    else{
      //System.out.println("grouping is not used ... "+pos);
      while(pos < len && count < maximumIntegerDigits){
        char ch = number.charAt(pos);
        //System.out.println("char "+pos+" is "+ch);
        if(ch >= '0' && ch <= '9'){
          buf.append(ch);
          count++;
          pos++;
        }
        else {
          break;
        }
      }
    }
    if(count < minimumIntegerDigits){
      //System.out.println("RETURNING ERROR: minimum not reached in '"+number+"' at pos"+pos+" --> "+count+" <--> "+minimumIntegerDigits);
      //debug("patter unknown");
      return -pos;
    }
    return pos;
  }


  public void setDecimalFormatSymbols(DecimalFormatSymbols syms){
    symbols = syms;
  }

  public void setDecimalSeparatorAlwaysShown(boolean val){
    decimalSeparatorAlwaysShown = val;
  }
  public void setGroupingSize(int size){
    this.groupingSize = (byte)size;
  }

  public void setMaximumFractionDigits(int max){
    if(max >= 340){
      max = 340;
    }
    super.setMaximumFractionDigits(max);
  }

  public void setMaximumIntegerDigits(int max){
    if(max >= 309){
      max = 309;
    }
    super.setMaximumIntegerDigits(max);
  }

  public void setMinimumFractionDigits(int min){
    if(min >= 340){
      min = 340;
    }
    super.setMinimumFractionDigits(min);
  }

  public void setMinimumIntegerDigits(int min){
    if(min >= 309){
      min = 309;
    }
    super.setMinimumIntegerDigits(min);
  }

  public void setMultiplier(int mul){
    this.multiplier = mul;
  }

  public void setNegativePrefix(String prefix){
    this.negativePrefix = prefix;
  }

  public void setNegativeSuffix(String suffix){
    this.negativeSuffix = suffix;
  }

  public void setPositivePrefix(String prefix){
    this.positivePrefix = prefix;
  }

  public void setPositiveSuffix(String suffix){
    this.positiveSuffix = suffix;
  }

  public String toLocalizedPattern(){
    return toPattern(symbols);
  }

  public String toPattern(){
    return toPattern(DEFAULT);
  }

  private String toPattern(DecimalFormatSymbols dfs){
    StringBuffer buf = new StringBuffer(128);
    buf.append(positivePrefix);
    int pos = buf.length();
    int min = getMinimumIntegerDigits();
    addChars(dfs.getZeroDigit(), min, buf);
    if(isGroupingUsed()){
      addChars(dfs.getDigit(), groupingSize + 1 - min, buf);
      int i = 1;
      if (min > groupingSize){
        i = min / groupingSize;
        min += pos;
      }
      else {
        min = groupingSize + 1;
      }
      char ch = dfs.getGroupingSeparator();
      for(int j = 0 ; j < i ; j++){
        min -= groupingSize;
        buf.insert(min,ch);
      }
    }
    min = getMinimumFractionDigits();
    int max = getMaximumFractionDigits();
    if(decimalSeparatorAlwaysShown || min > 0 || max > 0){
      buf.append(dfs.getDecimalSeparator());
      addChars(dfs.getZeroDigit(), min, buf);
      addChars(dfs.getDigit(), max - min, buf);
    }
    String format = buf.substring(pos);
    buf.append(positiveSuffix);
    if(!"-".equals(negativePrefix) || !"".equals(negativeSuffix)){
      buf.append(dfs.getPatternSeparator());
      buf.append(negativePrefix);
      buf.append(format);
      buf.append(negativeSuffix);
    }
    return buf.toString();
  }

  private void addChars(char digit, int size, StringBuffer buf){
    for(int i = 0 ; i < size ; i++){
      buf.append(digit);
    }
  }


  private void applyPattern(String pattern, DecimalFormatSymbols dfs){
    String specials = dfsToString(dfs);
    //System.out.println("applying pattern '"+pattern+"'");
    //step 1: get the Prefix
    StringBuffer buf = new StringBuffer();
    int pos = locateString(0, pattern, specials, buf);
    positivePrefix = buf.toString();
    //System.out.println("prefix 1'"+positivePrefix+"' "+pos);
    //step 2: get the Integer Part
    pos = locateIntegerPart(pos, pattern, true, dfs);
    //step 3: get the Fraction Part
    pos = locateFractionPart(pos, pattern, true, dfs);
    //step 4: get the suffix
    buf.setLength(0);
    if(pos < pattern.length()){
      char ch = pattern.charAt(pos);
      if(ch == dfs.getPercent()){
        //System.out.println("percent sign found "+pos);
        multiplier = 100;
        buf.append(ch);
        pos++;
      }
      else if(ch == dfs.getPerMill()){
        //System.out.println("permill sign found "+pos);
        multiplier = 1000;
        buf.append(ch);
        pos++;
      }
      else {
        multiplier = 1;
        pos = locateString(pos, pattern, specials, buf);
      }
      //System.out.println("setting positiveSuffix to '"+buf+"' "+pos);
      positiveSuffix = buf.toString();

      if(pos < pattern.length() && pattern.charAt(pos) == dfs.getPatternSeparator()){
        //System.out.println("found a second pattern");
        pos++;
        buf.setLength(0);
        pos = locateString(pos, pattern, specials, buf);
        //System.out.println("prefix2 '"+buf+"' "+pos);
        negativePrefix = buf.toString();
        pos = locateIntegerPart(pos, pattern, false, dfs);
        pos = locateFractionPart(pos, pattern, false, dfs);
        buf.setLength(0);
        pos = locateString(pos, pattern, specials, buf);
        if (multiplier > 1){
          //System.out.println("setting negativeSuffix to '"+positiveSuffix+"'");
          negativeSuffix = positiveSuffix;
        }
        else {
          //System.out.println("setting negativeSuffix to '"+positiveSuffix+"'");
          negativeSuffix = buf.toString();
        }
      }
      else {
        if(multiplier > 1){
          //System.out.println("setting Suffixes to '"+positiveSuffix+"'");
          negativeSuffix = positiveSuffix;
        }
        negativePrefix = "-";
      }
    }
    else {
      positiveSuffix = "";
      negativeSuffix = positiveSuffix;
      negativePrefix = "-";
      multiplier = 1;
    }
  }

  /**
  ** tries to locate the prefix in the string starting from start ...
  */
  private int locateString(int start, String pattern, String specials, StringBuffer buf){
    int stop = pattern.length();

    //System.out.println("looking for String at "+start);
    for(int i = start ; i < stop ; i++){
      char ch = pattern.charAt(i);
      //System.out.println("character at "+i+" is '"+ch+"'");
      if(ch == '\''){
        if(i+2 < stop && pattern.charAt(i+2) == '\''){
          buf.append(pattern.charAt(i+1));
          i += 2;
        }
      }
      else if(specials.indexOf(ch) != -1){
        //System.out.println("stopped looking for String at "+i);
        return i;
      }
      else {
        buf.append(ch);
      }
    }
    //System.out.println("stopped looking for String at "+stop);
    return stop;
  }

  /**
  ** locates the IntegerPart in the pattern
  **
  */
  private int locateIntegerPart(int start, String pattern, boolean set, DecimalFormatSymbols dfs){
    int min = 0;
    int size = -1;
    int stop = pattern.length();
    char digit = dfs.getDigit();
    char zero = dfs.getZeroDigit();
    char group = dfs.getGroupingSeparator();

    //System.out.println("looking for integer part at "+start);
    for(int i = start ; i < stop ; i++){
      char ch = pattern.charAt(i);
      //System.out.println("character at "+i+" is '"+ch+"' "+size);
      if (ch == digit){
        if(min > 0){
          throw new IllegalArgumentException("bad pattern");
        }
      }
      else if(ch == zero){
          min++;
      }
      else if(ch == group){
        size=0;
        continue;
      }
      else {
        stop = i;
      }

      if(size != -1){
        if(stop != i){
          size++;
        }
        else if(size == 0){
          throw new IllegalArgumentException("bad pattern");
        }
      }
    }
    if(set){
      setMinimumIntegerDigits(min);
      if(size != -1){
        setGroupingUsed(true);
        groupingSize = (byte)size;
        if(groupingSize < 0){
          groupingSize = Byte.MAX_VALUE;
        }
      }
      else{
        setGroupingUsed(false);
        groupingSize = 0;
      }
    }
    //System.out.println("stopped looking for integer part at "+stop);
    return stop;
  }

  /**
  ** locates the Fraction Part in the pattern
  **
  */
  private int locateFractionPart(int start, String pattern, boolean set, DecimalFormatSymbols dfs){
    int stop = pattern.length();

    //System.out.println("looking for fraction part at "+start);

    if(start >= stop || pattern.charAt(start) != dfs.getDecimalSeparator()){
      if(set){
        //System.out.println("No fraction found !");
        super.setMaximumFractionDigits(0);
        decimalSeparatorAlwaysShown = false;
      }
      return start;
    }
    int min = 0;
    int digits = 0;
    char digit = dfs.getDigit();
    char zero = dfs.getZeroDigit();


    for(int i = start+1 ; i < stop ; i++){
      char ch = pattern.charAt(i);
      if(ch == digit){
        digits++;
      }
      else if(ch == zero){
        if(digits > 0){
          throw new IllegalArgumentException("bad pattern");
        }
        min++;
      }
      else {
          stop = i;
      }
    }
    if(set){
      setMinimumFractionDigits(min);
      setMaximumFractionDigits(min+digits);
      decimalSeparatorAlwaysShown = (min > 0);

    }
    //System.out.println("stopped looking for fraction part at "+stop+" "+set+" "+min+" "+digits);
    return stop;
  }
  /**
  ** builds a string containing all special characters that end a prefix
  **
  */
  private String dfsToString(DecimalFormatSymbols dfs){
    StringBuffer buf = new StringBuffer();
    buf.append(dfs.getDigit());
    buf.append(dfs.getZeroDigit());
    buf.append(dfs.getDecimalSeparator());
    buf.append(dfs.getGroupingSeparator());
    buf.append(dfs.getPatternSeparator());
    buf.append(dfs.getPercent());
    buf.append(dfs.getPerMill());
    return buf.toString();
  }

//DEBUGGING purpose only ...
  void debug(String pattern){
    System.out.println(
      "applied '"+pattern+"' on "+this.toString()
      +"\ndecimalSeparatorAlwaysShown "+decimalSeparatorAlwaysShown
      +"\ngroupingSize "+ groupingSize
      +"\nmultiplier "+ multiplier
      +"\nnegativePrefix "+ negativePrefix
      +"\nnegativeSuffix "+ negativeSuffix
      +"\npositivePrefix "+ positivePrefix
      +"\npositiveSuffix "+ positiveSuffix
      +"\nDecimalFormatSymbols "+dfsToString(symbols)
      +"\nuseExponentialNotation "+useExponentialNotation
      +"\ngetMaximumFractionDigits "+getMaximumFractionDigits()
      +"\ngetMinimumFractionDigits "+getMinimumFractionDigits()
      +"\ngetMaximumIntegerDigits "+getMaximumIntegerDigits()
      +"\ngetMinimumIntegerDigits "+getMinimumIntegerDigits()

    );
  }

}

