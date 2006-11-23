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
** $Id: FieldPosition.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

public class FieldPosition extends Object {

  private int field;
  private int beginIndex;
  private int endIndex;

  public FieldPosition(int field){
    this.field = field;
  }
  
  public int getBeginIndex() {
    return beginIndex;
  }

  public int getEndIndex() {
    return endIndex;
  }
  public void setBeginIndex(int bgidx) {
    beginIndex = bgidx;
  }

  public void setEndIndex(int endidx) {
    endIndex = endidx;
  }

  public int getField() {
    return field;
  }

  public boolean equals(Object o){
    if(!(o instanceof FieldPosition)){
      return false;
    }
    FieldPosition fp = (FieldPosition)o;
    return (this.field == fp.field &&
            this.endIndex == fp.endIndex &&
            this.beginIndex == fp.beginIndex);
  }

  public int hashCode(){
    return field ^ beginIndex ^ endIndex;
  }

  public String toString(){
    return this.getClass().getName()+"[field="+field+",beginIndex="+beginIndex+",endIndex="+endIndex+']';
  }

}