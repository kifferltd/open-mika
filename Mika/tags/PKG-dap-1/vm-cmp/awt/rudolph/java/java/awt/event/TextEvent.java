/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/



package java.awt.event;

public class TextEvent extends java.awt.AWTEvent {

  public static final int TEXT_FIRST = 900;
  public static final int TEXT_VALUE_CHANGED = 900;
  public static final int TEXT_LAST = 900;

  public TextEvent(Object source, int id) {
    super(source, id);
  }

  /*****************************************************************/
  /**
  * returns default String description
  * @status  implemented
  * @remark  overrides Object.toString()
  */
  public String toString() {
    return getClass().getName()+"[TEXT_VALUE_CHANGED] on "+source;
  }

  /*****************************************************************/
  /**
  * returns parameter String description
  * @status  implemented
  * @remark  overrides AWTEvent.paramString()
  */
  public String paramString() {
    return getClass().getName()+"[type=TEXT_VALUE_CHANGED] from source "+source;
  }
}
