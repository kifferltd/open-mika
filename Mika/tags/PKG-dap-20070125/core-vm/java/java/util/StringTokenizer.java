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


package java.util;

public class StringTokenizer implements Enumeration {

  private String base;
  private String delimeted;
  private int delimeter;

  private boolean delimTokens;

  private int index=0;

  public StringTokenizer(String str){
    this(str, " \t\n\r" , false);
  }

  public StringTokenizer(String str, String delim){
    this(str, delim, false);
  }

  public StringTokenizer(String str, String delim, boolean returnTokens){
    this.base = str;
    this.delimTokens = returnTokens;
    setDelimeters(delim);
  }

  public int countTokens(){
    int delimeted_length = delimeted.length();
    if (index >= delimeted_length){
        return 0;
    }
    if (delimeter == -1){
        return 1;
    }
    int count = 0;
    if(delimTokens){
      int prev = index-1;
      int pos = delimeted.indexOf(delimeter, index);
      while (pos != -1){
         count++;
         if (pos - prev > 1){
           count++;
         }
         prev = pos;
         pos = delimeted.indexOf(delimeter, pos+1);
      }
      if (prev < delimeted_length-1 & (prev != index-1 | count == 0)){
        count++;
      }
    }else{
      int pos = delimeted.indexOf(delimeter, index);
      int prev = index-1;
      while (pos != -1){
         if(pos - prev > 1){
           count++;
         }
         prev = pos;
         pos = delimeted.indexOf(delimeter, pos+1);
      }
      if (prev < delimeted_length-1 & (prev != index-1 | count == 0)){
        count++;
      }
    }
    return count;
  }

  public boolean hasMoreElements(){
    return hasMoreTokens();
  }

  public boolean hasMoreTokens(){
    char delim[] = delimeted.toCharArray();
    int delimeted_length = delim.length;
    int idx = index;
    if (!delimTokens && delimeter != -1){
      while(idx < delimeted_length && delim[idx] == delimeter){
        idx++;
      }
    }
    index = idx;
    return idx < delimeted_length;
  }

  public Object nextElement(){
    return nextToken();
  }

  public String nextToken(){
    char delim[] = delimeted.toCharArray();
    int delimeted_length = delim.length;
    int idx = index;
    
    if(delimeter != -1){
      
      if (!delimTokens) {
        while(idx < delimeted_length && delim[idx] == delimeter){
          idx++;
        }
      }

      if (idx < delimeted_length) {
        int pos = idx;
        idx = delimeted.indexOf(delimeter, idx);
        if (pos == idx){
          idx++;
        }
        else if (idx == -1){
          idx = delimeted_length;
        }
        index = idx;
        return base.substring(pos, idx);
      }
    }
    else {
      if(idx < delimeted_length) {
        index = delimeted_length;
        return base;
      }
    }
    throw new NoSuchElementException();
  }

  public String nextToken(String delim){
    setDelimeters(delim);
    return nextToken();
  }

  private void setDelimeters(String delim){
    int delim_length = delim.length();
    if(delim_length == 0){
      delimeter = -1;
    } else{
      char dlm = delim.charAt(0);
      delimeter = dlm;
      base = base.substring(index);
      delimeted = base;
      index = 0;
      for (int i=1 ; i < delim_length ; i++){
        delimeted = delimeted.replace(delim.charAt(i) , dlm);
      }
    }
  }

}
