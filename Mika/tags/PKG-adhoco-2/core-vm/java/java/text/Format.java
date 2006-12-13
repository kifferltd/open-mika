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
** $Id: Format.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.text;

public abstract class Format implements java.io.Serializable, Cloneable {

  private static final long serialVersionUID = -299282585814624189L;

  public Object clone() {
    try {
      return super.clone();
    }
    catch(CloneNotSupportedException cnse){}
    return null;
  }
  
  public Format() { }
  
  public final String format(Object obj) {
    return format(obj, new StringBuffer(), new FieldPosition(0)).toString();
  }
  
  public abstract StringBuffer format(Object obj, StringBuffer appendBuf, FieldPosition pos);
  

  public Object parseObject(String source) throws ParseException {
    ParsePosition pos = new ParsePosition(0);
    Object o = parseObject(source, pos);
    if (o == null){
      throw new ParseException(pos.toString(), pos.getErrorIndex());
    }
    return o;
  }
  
  public abstract Object parseObject(String source, ParsePosition pos);
  
}