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