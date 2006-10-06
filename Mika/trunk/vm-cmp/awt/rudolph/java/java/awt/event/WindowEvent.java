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

import java.awt.Window;

public class WindowEvent extends ComponentEvent {

  public static final int WINDOW_FIRST = 200;
  public static final int WINDOW_OPENED = 200;
  public static final int WINDOW_CLOSING = 201;
  public static final int WINDOW_CLOSED = 202;
  public static final int WINDOW_ICONIFIED = 203;
  public static final int WINDOW_DEICONIFIED = 204;
  public static final int WINDOW_ACTIVATED = 205;
  public static final int WINDOW_DEACTIVATED = 206;
  public static final int WINDOW_LAST = 206;

  public WindowEvent(java.awt.Window source, int id) {
    super(source, id);
  }

  public Window getWindow(){
    return (Window)source;
  }

  /**
   * Diagnostics
   */
  public String toString() {
    String commandstring = "[Unknown command "+id+"]";
    if(id == WINDOW_OPENED) {
      commandstring = "[WINDOW_OPENED]";
    }
    else if(id == WINDOW_CLOSING) {
      commandstring = "[WINDOW_CLOSING]";
    }
    else if(id == WINDOW_CLOSED) {
      commandstring = "[WINDOW_CLOSED]";
    }
    else if(id == WINDOW_ICONIFIED) {
      commandstring = "[WINDOW_ICONIFIED]";
    }
    else if(id == WINDOW_DEICONIFIED) {
      commandstring = "[WINDOW_DEICONIFIED]";
    }
    else if(id == WINDOW_ACTIVATED) {
      commandstring = "[WINDOW_ACTIVATED]";
    }
    else if(id == WINDOW_DEACTIVATED) {
      commandstring = "[WINDOW_DEACTIVATED]";
    }
    return getClass().getName() +commandstring+source;
  }
  //public String paramString() {return super.paramString(); } // mapped to AWTEvent
}
