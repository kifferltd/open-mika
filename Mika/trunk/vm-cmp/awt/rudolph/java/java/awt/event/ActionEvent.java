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

import java.awt.AWTEvent;

public class ActionEvent extends AWTEvent {

  public static final int ACTION_FIRST     = 1001;
  public static final int ACTION_LAST       = 1001;
  public static final int ACTION_PERFORMED = ACTION_FIRST; //Event.ACTION_EVENT

  public static final int SHIFT_MASK  = 1;
  public static final int CTRL_MASK   = 2;
  public static final int ALT_MASK    = 4;
  public static final int META_MASK   = 8;

  String command;
  int modifiers;
  long when;

  public ActionEvent(Object source, int id, String command) {
    this(source, id, command, 0);
  }

  public ActionEvent(Object source, int id, String command, int modifiers) {
    super(source, id);

    this.command = command;
    this.modifiers = modifiers;
  }

  public ActionEvent(Object source, int id, String command, long when, int modifiers) {
    super(source, id);

    this.command = command;
    this.when = when;
    this.modifiers = modifiers;
  }

  public String getActionCommand() {
    return command;
  }

  public int getModifiers() {
    return modifiers;
  }

  /*********************************************************************************************/
  /** diagnostics */
  public String toString() {
    String display= getClass().getName() +"[ACTION_PERFORMED, ActionCommand = "+command+", Modifiers=[";
    if(modifiers == 0) {
      display+="None";
    }
    else {
      display+=" ";
    }
    if((modifiers & ActionEvent.SHIFT_MASK) > 0) {
      display+="[SHIFT] ";
    }
    if((modifiers & ActionEvent.CTRL_MASK) > 0) {
      display+="[CTRL] ";
    }
    if((modifiers & ActionEvent.ALT_MASK) > 0) {
      display+="[ALT] ";
    }
    if((modifiers & ActionEvent.META_MASK) > 0) {
      display+="[META] ";
    }
    if((modifiers & (ActionEvent.SHIFT_MASK+ActionEvent.CTRL_MASK+ActionEvent.ALT_MASK+ActionEvent.META_MASK))!= modifiers) {
      display+="[other] ";
    }
    display+="] on ";
    display+=source.getClass().getName();
    return display;
  }

  public String paramString() {
    return getClass().getName() +"[source="+source+", command="+command+", modifiers="+modifiers+"]";
  }

}
