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

public class ContainerEvent extends ComponentEvent {

  /*********************************************************************/
  /** definitions */
  public static final int CONTAINER_FIRST = 300;
  public static final int COMPONENT_ADDED = 300;
  public static final int CONTAINER_LAST = 301;
  public static final int COMPONENT_REMOVED = 301;

  /*********************************************************************/
  /** variables */
  // Object java.util.EventObject.source;
  // int java.awt.AWTEvent.id;
  java.awt.Component child;

  /*********************************************************************/
  /** Constructor */
  public ContainerEvent(java.awt.Component source, int id, java.awt.Component child) {
    super(source, id);
    this.child = child;
  }

  /*********************************************************************/
  /** Data access
  */
  /** source as Container  */
  public java.awt.Container getContainer() {
    return (java.awt.Container) source;
  };

  /** child member */
  public java.awt.Component getChild() {
    return child;
  }


  /*********************************************************************/
  /** Diagnostics   */
  public String toString() {
    String commandstring = "[unknown command + id]";
    if(id == COMPONENT_ADDED) {
      commandstring = "[COMPONENT_ADDED]";
    }
    else if(id == COMPONENT_REMOVED) {
      commandstring = "[COMPONENT_REMOVED]";
    }
    return getClass().getName() +commandstring+source;
  }

  //public String paramString() {return super.paramString(); } // mapped to AWTEvent
}
