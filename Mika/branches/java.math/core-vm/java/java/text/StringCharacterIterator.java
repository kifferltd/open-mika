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
** $Id: StringCharacterIterator.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

public final class StringCharacterIterator implements CharacterIterator {

  private int beginIndex;
  private int current;
  private int endIndex;
  private String source;

  public StringCharacterIterator(String text){
    source = text;
    endIndex = text.length();
  }

  public StringCharacterIterator(String text, int index){
    endIndex = text.length();
    if(index < 0 || index > endIndex){
      throw new IllegalArgumentException();
    }
    source = text;
    current = index;
  }

  public StringCharacterIterator(String text, int begin, int end, int index){
    if(begin < 0 || index < begin || index > end || begin > end || end > text.length()){
      throw new IllegalArgumentException();
    }
    source = text;
    current = index;
    beginIndex = begin;
    endIndex = end;
  }
  public Object clone(){
    try {
      return super.clone();
    }
    catch(CloneNotSupportedException cnse){
      return null;
    }
  }

  public char current(){
    if(current >= endIndex){
      return DONE;
    }
    return source.charAt(current);
  }

  public boolean equals(Object o){
    if(!(o instanceof StringCharacterIterator)){
      return false;
    }
    StringCharacterIterator sci = (StringCharacterIterator)o;
    return this.beginIndex == sci.beginIndex
        && this.endIndex == sci.endIndex
        && this.current == sci.current
        && this.source.equals(sci.source);
  }

  public char first(){
    if(endIndex == beginIndex){
      return DONE;
    }
    current = beginIndex;
    return source.charAt(current);
  }

  public int getBeginIndex(){
    return beginIndex;
  }

  public int getEndIndex(){
    return endIndex;
  }
  public int getIndex(){
    return current;
  }

  public int hashCode(){
    return endIndex ^ beginIndex ^ current ^ source.hashCode();
  }

  public char last(){
    if(endIndex == beginIndex){
      return DONE;
    }
    current = endIndex-1;
    return source.charAt(current);
  }
  public char next(){
    if(current >= endIndex-1){
      current = endIndex;
      return DONE;
    }
    return source.charAt(++current);
  }

  public char previous(){
    if(current <= beginIndex){
      return DONE;
    }
    return source.charAt(--current);
  }

  public char setIndex(int idx){
    if(idx < beginIndex || idx > endIndex){
      throw new IllegalArgumentException();
    }
    current = idx;
    if(current == endIndex){
      return DONE;
    }
    return source.charAt(current);
  }

  public void setText(String txt){
    endIndex = txt.length();
    beginIndex = 0;
    current = 0;
    source = txt;
  }
}
