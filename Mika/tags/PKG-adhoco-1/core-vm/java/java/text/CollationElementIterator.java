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