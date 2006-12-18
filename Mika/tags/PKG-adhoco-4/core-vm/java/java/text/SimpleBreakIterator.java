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