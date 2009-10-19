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
** $Id: CollationElementIterator.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

public final class CollationElementIterator {

  public final static int NULLORDER = 0xffffffff;

  public final static int primaryOrder(int order){
    //TODO ???
    return (order>>>16);
  }

  public final static short secondaryOrder(int order){
    //TODO ???
    return ((short)((order & 0x0ff00)>>8));
  }

  public final static short tertiaryOrder(int order){
    //TODO ???
    return ((short)(order & 0x0ff));
  }

  private CharacterIterator source;
  private RuleBasedCollator collator;
  private int[] orders;
  private int index;


  CollationElementIterator(CharacterIterator source, RuleBasedCollator collator){
    this.source = source;
    this.collator = collator;
  }

  public int getMaxExpansion(int element){
    if(orders == null){
      orders = collator.getOrders(source);
      index = 0;
      if(orders == null){
        return NULLORDER;
      }
    }
    return orders.length;
  }

  public int getOffset(){
    return source.getIndex();
  }

  public int next(){
    if(orders != null && index < orders.length){
      return orders[index++];
    }
    orders = collator.getOrders(source);
    index = 0;
    if(orders != null && index < orders.length){
      return orders[index++];
    }
    return NULLORDER;
  }

  public int previous(){
    //TODO ...
    return NULLORDER;
  }

  public void reset(){
    source.first();
  }

  public void setOffset(int newOffset){
    source.setIndex(newOffset);
  }

  public void setText(String text){
    source = new StringCharacterIterator(text);
  }

  public void setText(CharacterIterator newChariter){
    source = newChariter;
  }
}
