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

public class FocusEvent extends ComponentEvent {

  public static final int FOCUS_GAINED = 1004;
  public static final int FOCUS_FIRST = 1004;
  public static final int FOCUS_LAST = 1005;
  public static final int FOCUS_LOST = 1005;
  
  protected boolean temporary;
  
  public FocusEvent(java.awt.Component source, int id) {
    super(source, id);
    temporary = false;
  }

  public FocusEvent(java.awt.Component source, int id, boolean temporary) {
    super(source, id);

    this.temporary = temporary;
  }

  public boolean isTemporary() {
    return this.temporary;
  }  

  /**
   * Diagnostics
   */
  public String toString() {
    String commandstring = "[Unknown command "+id+"]";
    if(id == FOCUS_GAINED) {
      commandstring = "[FOCUS_GAINED]";
    }
    else if(id == FOCUS_LOST) {
      commandstring = "[FOCUS_LOST]";
    }
    commandstring +=(temporary)?" temporary" : " permanent" ;
    return getClass().getName() +commandstring+ source;
  }

  public String paramString() {
    return getClass().getName() +"[Function id="+id+"] Temporary="+temporary+"from="+source;
  }
}
