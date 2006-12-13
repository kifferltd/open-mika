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
package com.acunia.wonka.rudolph;
import java.awt.datatransfer.*;
import java.awt.dnd.peer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.*;

public class DropTargetEvent extends java.awt.dnd.DropTargetEvent{
  private static com.acunia.wonka.rudolph.DropTargetEvent dte;
  private static boolean inProgress = false;
  private static DragSourceContext dragSourceContext;

  private DropTargetEvent(java.awt.dnd.DropTargetContext dtc){
    super(dtc);
  }

  public static com.acunia.wonka.rudolph.DropTargetEvent getDropTargetEvent(){
    return dte;
  }

  public static void setDropTargetContext(java.awt.dnd.DropTargetContext dtc){
    dte = new DropTargetEvent(dtc);
  }

  public static boolean setInProgress(boolean inProgr, DragSourceContext dsc){
    if(inProgress && inProgr) return false;
    if(inProgress && !inProgr) dragSourceContext.setCursor(Cursor.getDefaultCursor());
    inProgress = inProgr;
    dragSourceContext = dsc;
    return true;
  }
  
  public static DragSourceContext getDragSourceContext(){
    if(!inProgress) return null;
    return dragSourceContext;
  }

  public static boolean getInProgress(){
    return inProgress;
  }
  
  public void dispatch(MouseEvent event){
    if(inProgress)((com.acunia.wonka.rudolph.DropTargetContext)getDropTargetContext()).getDropTargetContextPeer().getEventDispatcher().dispatchEvent(this, event);
  }
}



  


