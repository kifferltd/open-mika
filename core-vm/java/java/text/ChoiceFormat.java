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
** $Id: ChoiceFormat.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

import java.util.Arrays;
import java.util.StringTokenizer;

public class ChoiceFormat extends NumberFormat {

  private static final long serialVersionUID = 1795184449645032964L;

  public final static double nextDouble(double d){
    return nextDouble(d,true);
  }

  public static double nextDouble(double d, boolean next){
    if(!Double.isNaN(d)){
      if(Double.isInfinite(d)){
        if(d > 0 && !next){
          return Double.MAX_VALUE;
        }
        if(d < 0 && next){
          return -Double.MAX_VALUE;
        }
        return d;
      }
      if(d == 0.0){
        return next ? Double.MIN_VALUE : -Double.MIN_VALUE;
      }
      if(!next && d <= - Double.MAX_VALUE){
        return Double.NEGATIVE_INFINITY;
      }
      return Double.longBitsToDouble(Double.doubleToLongBits(d)+(next ? 1 : -1));
    }
    return Double.NaN;
  }

  public final static double previousDouble(double d){
    return nextDouble(d,false);
  }

  //dictated by serialized form
  private String[] choiceFormats;
  private double[] choiceLimits;

  public ChoiceFormat(String newPattern){
    applyPattern(newPattern);
  }

  public ChoiceFormat(double[] limits, String[] formats){
    setChoices(limits,formats);
  }

  private void checkLimits(double[] limits){
    double prev = limits[0];
    for(int i = 1 ; i < limits.length ; i++){
      double next = limits[i];
      if(next < prev){
        throw new IllegalArgumentException();
      }
      prev = next;
    }
  }

  private double parseDouble(String value){
    value = value.trim();
    try {
      return Double.parseDouble(value);
    }
    catch(NumberFormatException nfe){
      if(value.equals("-\u221E")){
        return Double.NEGATIVE_INFINITY;
      }
      if(value.equals("\u221E")){
        return Double.POSITIVE_INFINITY;
      }
      throw new IllegalArgumentException("bad double value encountered");
    }
  }

  public void applyPattern(String pattern){
    StringTokenizer st = new StringTokenizer(pattern,"|");
    int len = st.countTokens();
    if(len == 0){
      throw new IllegalArgumentException("pattern contains no choices");
    }
    String[] formats = new String[len];
    double[] limits = new double[len];
    for(int i = 0 ; i < len ; i++){
      String token = st.nextToken();
      int idx = token.indexOf('<');
      if(idx == -1){
        idx = token.indexOf('#');
        if(idx == -1){
          idx = token.indexOf('\u2264');
          if(idx ==- 1){
            throw new IllegalArgumentException("bad pattern applied");
          }
        }
        double d = parseDouble(token.substring(0,idx));
        limits[i] = d;
      }
      else {
        double d = nextDouble(parseDouble(token.substring(0,idx)));
        limits[i] = d;
      }
      formats[i] = token.substring(idx+1);
    }
    checkLimits(limits);
    choiceFormats = formats;
    choiceLimits = limits;
  }

  public Object clone(){
    ChoiceFormat cf = (ChoiceFormat)super.clone();
    cf.choiceLimits = (double[])choiceLimits.clone();
    cf.choiceFormats = (String[])choiceFormats.clone();
    return cf;
  }

  public boolean equals(Object o){
    if(!(o instanceof ChoiceFormat)){
      return false;
    }
    ChoiceFormat cf = (ChoiceFormat)o;
    return Arrays.equals(this.choiceFormats,cf.choiceFormats)
        && Arrays.equals(this.choiceLimits, cf.choiceLimits);
  }

  public StringBuffer format(long num, StringBuffer app, FieldPosition pos){
    return format((double)num,app,pos);
  }

  public StringBuffer format(double num, StringBuffer app, FieldPosition pos){
    if(Double.isNaN(num)){
      app.append(choiceFormats[0]);
    }
    else {
      for(int i = 0 ; i < choiceLimits.length ; i++){
        double limit = choiceLimits[i];
        if(num < limit){// || (num == limit && limit == prev)){
          app.append(choiceFormats[(i > 0 ? i-1 : 0)]);
          return app;
        }
      }
      int l = choiceFormats.length;
      if(l > 0){
        app.append(choiceFormats[l-1]);
      }
    }
    return app;
  }

  public Object[] getFormats(){
     return choiceFormats;
  }

  public double[] getLimits(){
    return choiceLimits;
  }

  public int hashCode(){
    int hash = 1;
    if(choiceLimits != null){
      for (int i = 0 ; i < choiceLimits.length ; i++){
        hash += (Double.doubleToLongBits(choiceLimits[i])) ^ choiceFormats[i].hashCode();
      }
    }
    return hash;
  }

  public Number parse(String srcStr, ParsePosition pos){
    int start = pos.getIndex();
    for(int i = 0 ; i < choiceFormats.length ; i++){
      if(srcStr.regionMatches(start,choiceFormats[i],0 ,choiceFormats[i].length())){
        pos.setIndex(start+choiceFormats[i].length());
        return new Double(choiceLimits[i]);
      }
    }
    return new Double(Double.NaN);
  }

  public void setChoices(double[] limits, String[] formats){
    if(limits.length != formats.length || limits.length == 0){
      throw new IllegalArgumentException();
    }
    checkLimits(limits);
    choiceFormats = formats;
    choiceLimits = limits;
  }

  public String toPattern(){
    StringBuffer pattern = new StringBuffer(128);
    for (int i = 0 ; i < choiceLimits.length ; i++){
      pattern.append(choiceLimits[i]);
      pattern.append('#');
      pattern.append(choiceFormats[i]);
      pattern.append('|');
    }
    pattern.setLength(pattern.length()-1);
    return pattern.toString();
  }
}
