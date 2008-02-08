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
** $Id: DecimalFormatSymbols.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

import java.util.Locale;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;

public final class DecimalFormatSymbols implements Cloneable,Serializable {

  private static final long serialVersionUID = 5772796243397350300L;

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
    in.defaultReadObject();
    if(serialVersionOnStream < 1){
      serialVersionOnStream = 1;
      exponential = 'E';
      monetarySeparator = decimalSeparator;
    }

  }

  private String currencySymbol = "$";
  private char decimalSeparator = '.';
  private char digit = '#';
  private char exponential = 'E';
  private char groupingSeparator = ',';
  private String infinity = "\u221E";
  private String intlCurrencySymbol = "USD";
  private char minusSign = '-';
  private char monetarySeparator = '.';
  private String NaN = "\ufffd";
  private char patternSeparator = ';';
  private char percent = '%';
  private char perMill = '\u2030';
  private int serialVersionOnStream = 1;
  private char zeroDigit = '0';

  public DecimalFormatSymbols(){
    this(Locale.getDefault());
  }
  public DecimalFormatSymbols(Locale loc){

  }

  public Object clone(){
    Object clone = null;
    try {
      clone = super.clone();
    } catch(CloneNotSupportedException e){}
    return clone;
  }

  public boolean equals(Object o){
    if(!(o instanceof DecimalFormatSymbols)){
      return false;
    }
    DecimalFormatSymbols dfs = (DecimalFormatSymbols)o;
    return this.currencySymbol.equals(dfs.currencySymbol)
        && this.decimalSeparator == dfs.decimalSeparator
        && this.digit == dfs.digit
        && this.exponential == dfs.exponential
        && this.groupingSeparator == dfs.groupingSeparator
        && this.infinity.equals(dfs.infinity)
        && this.intlCurrencySymbol.equals(dfs.intlCurrencySymbol)
        && this.minusSign == dfs.minusSign
        && this.monetarySeparator == dfs.monetarySeparator
        && this.NaN.equals(dfs.NaN)
        && this.patternSeparator == dfs.patternSeparator
        && this.percent == dfs.percent
        && this.perMill == dfs.perMill
        && this.zeroDigit == dfs.zeroDigit;
  }

  public String getCurrencySymbol(){
    return currencySymbol;
  }

  public char getDecimalSeparator(){
    return decimalSeparator;
  }

  public char getDigit(){
    return digit;
  }

  public char getGroupingSeparator(){
    return groupingSeparator;
  }

  public String getInfinity(){
    return infinity;
  }

  public String getInternationalCurrencySymbol(){
    return intlCurrencySymbol;
  }
  public char getMinusSign(){
    return minusSign;
  }

  public char getMonetaryDecimalSeparator(){
    return monetarySeparator;
  }

  public String getNaN(){
    return NaN;
  }

  public char getPatternSeparator(){
    return patternSeparator;
  }

  public char getPercent(){
    return percent;
  }

  public char getPerMill(){
    return perMill;
  }

  public char getZeroDigit(){
    return zeroDigit;
  }

  public int hashCode(){
    return currencySymbol.hashCode() ^ decimalSeparator
         ^ digit ^ exponential ^ groupingSeparator
         ^ infinity.hashCode() ^ intlCurrencySymbol.hashCode()
         ^ minusSign ^ monetarySeparator ^ NaN.hashCode()
         ^ patternSeparator ^ percent ^ perMill ^ zeroDigit;
  }

  public void setCurrencySymbol(String sym){
    this.currencySymbol = sym;
  }

  public void setDecimalSeparator(char sep){
    this.decimalSeparator= sep;
  }

  public void setDigit(char digit){
    this.digit = digit;
  }

  public void setGroupingSeparator(char sep){
    this.groupingSeparator = sep;
  }

  public void setInfinity(String inf){
    this.infinity = inf;
  }

  public void setInternationalCurrencySymbol(String sym){
    this.intlCurrencySymbol = sym;
  }
  public void setMinusSign(char min){
    this.minusSign = min;
  }

  public void setMonetaryDecimalSeparator(char sep){
    this.monetarySeparator = sep;
  }

  public void setNaN(String nan){
    this.NaN = nan;
  }

  public void setPatternSeparator(char sep){
    this.patternSeparator = sep;
  }

  public void setPercent(char percent){
    this.percent = percent;
  }

  public void setPerMill(char mil){
    this.perMill = mil;
  }

  public void setZeroDigit(char zero){
    this.zeroDigit = zero;
  }
}

