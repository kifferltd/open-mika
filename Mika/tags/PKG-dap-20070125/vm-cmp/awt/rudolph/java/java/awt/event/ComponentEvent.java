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

public class ComponentEvent extends java.awt.AWTEvent {

  public static final int COMPONENT_FIRST   = 100;
  public static final int COMPONENT_MOVED   = 100;
  public static final int COMPONENT_RESIZED = 101;
  public static final int COMPONENT_SHOWN   = 102;
  public static final int COMPONENT_HIDDEN  = 103;
  public static final int COMPONENT_LAST    = 103;
  
  public ComponentEvent(Component source, int id) {
    super(source, id);
  }
  
  public Component getComponent() {
    return (Component) source;
  };
  
  /**
   * Diagnostics
   */
  public String toString() {
    String commandstring = "[Unknown command "+id+"]";
    if(id == COMPONENT_MOVED) {
      commandstring = "[COMPONENT_MOVED]";
    }
    else if(id == COMPONENT_RESIZED) {
      commandstring = "[COMPONENT_RESIZED]";
    }
    else if(id == COMPONENT_SHOWN) {
      commandstring = "[COMPONENT_SHOWN]";
    }
    else if(id == COMPONENT_HIDDEN) {
      commandstring = "[COMPONENT_HIDDEN]";
    }
    return getClass().getName() +commandstring+source;
  }

  public String paramString() {
    return getClass().getName()+"[type "+id+"] from source "+source;
  }
}
