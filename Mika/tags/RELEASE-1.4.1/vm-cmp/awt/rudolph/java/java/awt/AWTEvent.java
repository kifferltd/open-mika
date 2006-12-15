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

package java.awt;

public class AWTEvent extends java.util.EventObject {

  private static final long serialVersionUID = -1825314779160409405L;
  
  /****************************************************************/
  /** definitions */
  public final static long ACTION_EVENT_MASK       = 0x00000000000000080;
  public final static long ADJUSTMENT_EVENT_MASK   = 0x00000000000000100;
  public final static long ITEM_EVENT_MASK         = 0x00000000000000200;
  public final static long TEXT_EVENT_MASK         = 0x00000000000000400;
  public final static long COMPONENT_EVENT_MASK    = 0x00000000000000001;
  public final static long CONTAINER_EVENT_MASK    = 0x00000000000000002;
  public final static long FOCUS_EVENT_MASK        = 0x00000000000000004;
  public final static long KEY_EVENT_MASK          = 0x00000000000000008;
  public final static long MOUSE_EVENT_MASK        = 0x00000000000000010;
  public final static long MOUSE_MOTION_EVENT_MASK = 0x00000000000000020;
  public final static long WINDOW_EVENT_MASK       = 0x00000000000000040;

  public final static int RESERVED_ID_MAX = 1999;
  
  /****************************************************************/
  /** variables */
  //protected Object  EventObject.source;
  protected int id;
  protected boolean consumed;

  /****************************************************************/
  /** constructor */
  public AWTEvent(Object source, int id) {
    super(source);

    this.id = id;
  }
  
  public AWTEvent(Event event) {
    super(event.target);
    this.id = event.id;
  }

  /****************************************************************/
  /** get function ID */
  public int getID() {
    return id;
  }

  /****************************************************************/
  /** Consume/is consumed */

  /** protected function consume: designed to be overridden by derived when they want their own event consuming */
  protected void consume() {
    consumed = true;
  }

  /** protected acces to flag consumed */
  protected boolean isConsumed() {
    return consumed;
  }


  /****************************************************************/
  /** Diagnostics */
  public String toString() {

    String name = (source instanceof Component)? ((Component)source).getName() : null;
    if (name == null){
      name = source.toString();
    }

    return this.getClass().getName() + ": " + name;
  }
   
  public String paramString() {
    return getClass().getName() +"[Function id="+id+"] from="+source;
  }
}
