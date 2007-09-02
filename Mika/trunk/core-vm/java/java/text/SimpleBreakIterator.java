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
** $Id: SimpleBreakIterator.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

class SimpleBreakIterator extends BreakIterator {

  private CharacterIterator iterator;
  private String bounderies;

  SimpleBreakIterator(String bounds){
    //if bounds == null --> break after all chars
    bounderies = bounds;
  }

  public Object clone() {
    SimpleBreakIterator sbi = (SimpleBreakIterator) super.clone();
    if(iterator != null){
      sbi.iterator = (CharacterIterator) iterator.clone();
    }
    return sbi;
  }

  public int current(){
    if(iterator == null){
      return DONE;
    }
    return iterator.getIndex();
  }

  public int first(){
    if(iterator == null){
      return DONE;
    }
    iterator.first();
    return iterator.getIndex();
  }

  public int following(int pos){
    if(iterator == null){
      return DONE;
    }
    iterator.setIndex(pos);
    return next();
  }
  public CharacterIterator getText(){
    return iterator;
  }

  public int last(){
    if(iterator == null){
      return DONE;
    }
    iterator.last();
    return iterator.getIndex();
  }

  public int next(){
    if(iterator == null){
      return DONE;
    }
    if(bounderies == null){
      int next = iterator.next();
      if(next != CharacterIterator.DONE){
        return iterator.getIndex();
      }
    }
    else {
      int next = iterator.next();
      while(next != CharacterIterator.DONE){
        if(bounderies.indexOf(next) != -1){
          return iterator.getIndex();
        }
        next = iterator.next();
      }
    }
    return DONE;
  }

  public int next(int pos){
    if(pos > 0){
      pos--;
      for(int i = 0 ; i < pos ; i++){
        next();
      }
      return next();
    }
    else if (pos < 0){
      pos = -pos -1;
      for(int i = 0 ; i < pos ; i++){
        previous();
      }
      return previous();
    }
    return current();
  }

  public int previous(){
    if(iterator == null){
      return DONE;
    }
    if(bounderies == null){
      int prev = iterator.previous();
      if(prev != CharacterIterator.DONE){
        return iterator.getIndex();
      }
    }
    else {
      int prev = iterator.previous();
      while(prev != CharacterIterator.DONE){
        if(bounderies.indexOf(prev) != -1){
          return iterator.getIndex();
        }
        prev = iterator.previous();
      }
    }
    return DONE;
  }

  public void setText(CharacterIterator newText){
    iterator = newText;
  }

}
