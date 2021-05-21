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

public abstract class InputEvent extends ComponentEvent {

  /** statics */
  public static final int BUTTON1_MASK = 16;
  public static final int BUTTON2_MASK = 8;
  public static final int BUTTON3_MASK = 4;
  
  public static final int ALT_GRAPH_MASK = 32;
  public static final int ALT_MASK = 8;
  public static final int META_MASK = 4;
  public static final int CTRL_MASK = 2;
  public static final int SHIFT_MASK = 1;

  public static final int SHIFT_DOWN_MASK = 64;

  public static final int CTRL_DOWN_MASK = 128;

  public static final int META_DOWN_MASK = 256;

  public static final int ALT_DOWN_MASK = 512;

  public static final int BUTTON1_DOWN_MASK = 1024;

  public static final int BUTTON2_DOWN_MASK = 2048;

  public static final int BUTTON3_DOWN_MASK = 4096;

  public static final int ALT_GRAPH_DOWN_MASK = 8192;

  static final int MASKS = SHIFT_MASK | CTRL_MASK | META_MASK | ALT_MASK
            | ALT_GRAPH_MASK | BUTTON1_MASK | BUTTON2_MASK | BUTTON3_MASK;

  static final int DOWN_MASKS = SHIFT_DOWN_MASK | CTRL_DOWN_MASK
            | META_DOWN_MASK | ALT_DOWN_MASK | ALT_GRAPH_DOWN_MASK
            | BUTTON1_DOWN_MASK | BUTTON2_DOWN_MASK | BUTTON3_DOWN_MASK;



  /****************************************************************/
  /** variables */
  //protected Object  EventObject.source;
  //protected int     AWTEvent.id;
  //protected boolean AWTEvent.consumed;
  protected int modifiers;
  protected long timeStamp;

  /****************************************************************/
  /**
  * Constructor
  * NOTE: as InputEvent just is a common base class for the MouseEvent and KeyEvent classes, it has no real constructor, but uses the
  * constructors from those classes (that also fill in the values for the modifiers and the time stamp). Nevertheless, we provide a
  * stub constructor mapping to the super class ComponentEvent
  */
  public InputEvent(java.awt.Component source, int id) {
    super(source, id);
  }

  /****************************************************************/
  /**
  * Event handling: Input events explicitly allow outside classes to call on the protected AWTEvent Consume() and isConsumed() functions
  */
  public void consume() {
    super.consume();
  }

  /****************************************************************/
  /**
   * public access to protected <Consumed> flag
   */
  public boolean isConsumed() {
    return consumed;
  }

  /****************************************************************/
  /**
  * Return the event time stamp
  */
  public long getWhen() {
    return timeStamp;
  }

    public int getModifiersEx() {
    	return modifiers & DOWN_MASKS;
    }

    public static String getModifiersExText(int modifiers) {
        return MouseEvent.addMouseModifiersExText(
                KeyEvent.getKeyModifiersExText(modifiers), modifiers);
    }

  /****************************************************************/
  /**
  * Event Modifiers handling :
  */
  /****************************************************************/

  /**  Return the 32-bit modifiers flag  */
  public int getModifiers() {
    return modifiers;
  }
  
  /**  check if the ALT-bit (0x1000) of the 32-bit modifiers flag is pressed */
  public boolean isAltDown() {
    return ((modifiers & ALT_MASK)!=0);
  }

  /**  check if the META-bit (0x100) of the 32-bit modifiers flag is pressed */
  public boolean isMetaDown() {
    return ((modifiers & META_MASK)!=0);
  }

  /**  check if the CONTROLL-bit (0x10) of the 32-bit modifiers flag is pressed */
  public boolean isControlDown() {
    return ((modifiers & CTRL_MASK)!=0);
  }

  /**  check if the SHIFT-bit (0x1) of the 32-bit modifiers flag is pressed */
  public boolean isShiftDown() {
    return ((modifiers & SHIFT_MASK)!=0);
  }

}
