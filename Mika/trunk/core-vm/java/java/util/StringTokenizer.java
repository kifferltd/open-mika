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
