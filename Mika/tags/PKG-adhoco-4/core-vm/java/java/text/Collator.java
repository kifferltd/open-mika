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
