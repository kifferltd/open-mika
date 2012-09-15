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

import java.awt.Component;
import java.awt.Point;

public class MouseEvent extends InputEvent {

  /** statics */
  public static final int MOUSE_FIRST    = 500;
  public static final int MOUSE_CLICKED  = 500;
  public static final int MOUSE_PRESSED  = 501;
  public static final int MOUSE_RELEASED = 502;
  public static final int MOUSE_MOVED    = 503;
  public static final int MOUSE_ENTERED  = 504;
  public static final int MOUSE_EXITED   = 505;
  public static final int MOUSE_DRAGGED  = 506;
  public static final int MOUSE_LAST     = 506;

  /*
  ** Not according to specs.
  */
  
  public static final int MOUSE_RELEASED_AFTER_DRAG = 510;


  /** variables */
  private int x;
  private int y;
  private int clickCount;
  private boolean popupTrigger;
  //protected int InputEvent.modifiers;
  //protected long InputEvent.timeStamp;
  //protected boolean AWTEvent.consumed;

  /*****************************************************************/
  /**********************************************************************************************************************************/
  /**
  * Constructor
  */
  public MouseEvent(Component source, int id, long when, int modifiers, int x, int y, int clickCount, boolean popupTrigger) {
    super(source, id);
    timeStamp = when;
    this.modifiers = modifiers;
    this.x = x;
    this.y = y;
    this.clickCount = clickCount;
    this.popupTrigger = popupTrigger;
  }

  /*****************************************************************/
  /**  mouse position : x */
  public int getX() {
    return x;
  }

  /**  mouse position : y */
  public int getY() {
    return y;
  }

  /**  mouse position : Point(x,y) */
  public Point getPoint() {
    return new Point(x, y);
  }

  /*****************************************************************/
  /**  translate mouse position to map mouse event from absolute
  * screen coordinates to relative component coordinates
  */
  public synchronized void translatePoint(int x, int y) {
    this.x += x;
    this.y += y;
  }

  /*****************************************************************/
  /**
  *  mouse clicks
  */
  public int getClickCount() {
    return clickCount;
  }

  /*****************************************************************/
  /**
  *  Popup trigger
  */
  public boolean isPopupTrigger() {
    return popupTrigger;
  }

  /*****************************************************************/
  /**
  *  Diagnostics
  */
  public String toString() {
    String descriptor = getClass().getName();
    if(id == MOUSE_CLICKED) {
      descriptor += "[MOUSE_CLICKED";
    }
    else if(id == MOUSE_PRESSED) {
      descriptor += "[MOUSE_PRESSED";
    }
    else if(id == MOUSE_RELEASED) {
      descriptor += "[MOUSE_RELEASED";
    }
    else if(id == MOUSE_MOVED) {
      descriptor += "[MOUSE_MOVED";
    }
    else if(id == MOUSE_ENTERED) {
      descriptor += "[MOUSE_ENTERED";
    }
    else if(id == MOUSE_EXITED) {
      descriptor += "[MOUSE_EXITED";
    }
    else if(id == MOUSE_DRAGGED) {
      descriptor += "[MOUSE_DRAGGED";
    }
    else {
      descriptor += "[UNKNOWN EVENT "+id;
    }
    descriptor += " position ("+x+", "+y+")] on "+ source;
    return descriptor;
  }

  public String paramString() {
    return getClass().getName()+"[source="+source+", id="+id+", time="+timeStamp+", position=("+x+", "+y+"), modifiers="+modifiers+
             ", popup="+popupTrigger+"]";
  }

  
}
