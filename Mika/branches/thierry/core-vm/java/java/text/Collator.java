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
** $Id: Collator.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
*/

package java.text;

import java.util.Comparator;
import java.util.Locale;

public abstract class Collator implements Comparator,Cloneable {

  public final static int NO_DECOMPOSITION = 0;
  public final static int CANONICAL_DECOMPOSITION = 1;
  public final static int FULL_DECOMPOSITION = 2;

  public final static int PRIMARY = 0;
  public final static int SECONDARY = 1;
  public final static int TERTIARY = 2;
  public final static int IDENTICAL = 3;

  public static Locale[] getAvailableLocales(){
    //TODO ...
    return new Locale[0];
  }

  public static Collator getInstance(){
    return getInstance(Locale.getDefault());
  }

  public static Collator getInstance(Locale loc){
    //TODO ...
    try {
      RuleBasedCollator rbc = new RuleBasedCollator("< a,A < b,B < c,C < d,D < e,E < f,F < g,G < h,H < i,I < j,J "+
                   "< k,K < l,L < m,M < n,N < o,O < p,P < q,Q < r,R < s,S < t,T < u,U < v,V < w,W < x,X < y,Y < z,Z");
      return rbc;
    }
    catch(ParseException pe){
      System.out.println("Ooops getInstance fails "+pe);
      pe.printStackTrace();
      return null;
    }
  }

  int decomposition = CANONICAL_DECOMPOSITION;
  int strength = TERTIARY;

  protected Collator(){}

  Collator(int decomp, int str){
    decomposition = decomp;
    strength = str;
  }

  public Object clone(){
    try {
      return super.clone();
    }
    catch(CloneNotSupportedException cnse){
      return null;
    }
  }

  public abstract int compare(String one, String two);

  public int compare(Object one, Object two){
    return compare((String)one,(String)two);
  }

  public boolean equals(Object o){
    if(!(o instanceof Collator)){
      return false;
    }
    Collator col = (Collator) o;
    return this.decomposition == col.decomposition
        && this.strength == col.strength;
  }

  public boolean equals(String one, String two){
    return compare(one, two) == 0;
  }

  public abstract CollationKey getCollationKey(String src);

  public int getDecomposition(){
    return decomposition;
  }

  public int getStrength(){
    return strength;
  }

  public abstract int hashCode();

  public void setDecomposition(int mode){
    if(mode < 0 || mode > 2){
      throw new IllegalArgumentException();
    }
    decomposition = mode;
  }

  public void setStrength(int strength){
    if(strength < 0 || strength > 3){
      throw new IllegalArgumentException();
    }
    this.strength = strength;
  }

}
