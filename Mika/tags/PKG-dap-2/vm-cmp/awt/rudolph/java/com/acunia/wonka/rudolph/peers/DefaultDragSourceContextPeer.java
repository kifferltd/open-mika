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
package com.acunia.wonka.rudolph.peers;
import java.awt.datatransfer.*;
import java.awt.dnd.peer.*;
import java.awt.dnd.*;
import java.awt.*;

public class DefaultDragSourceContextPeer implements DragSourceContextPeer{
//CHANGE THIS LOCALE VARIABEL CURSOR WITH THE SYSTEMS ONE
  private Cursor cursor;
  private DragGestureEvent dge;

  public DefaultDragSourceContextPeer(DragGestureEvent dge){
    this.dge = dge;
  }

  public Cursor getCursor(){
    return cursor;
  }

  public void setCursor(Cursor c) throws InvalidDnDOperationException{
    if(c == null) c = DragSource.DefaultCopyNoDrop;
    cursor = c;
System.out.println("de gevraagde cursor = "+c);
  }

  public void startDrag(DragSourceContext dsc, Cursor c, Image dragImage,
                       Point imageOffset) throws InvalidDnDOperationException{
    if(dsc.getTrigger().getTriggerEvent()==null) throw new InvalidDnDOperationException("DragGestureEvent has a null trigger");
    
    int action = dsc.getTrigger().getDragAction();
    setDragDropInProgress(dsc);
    setCursor(c);
//if you look in the Thread.dumpStack(); than you can see that this function use dsc.getTransferable().getTransferDataFlavors();
    DataFlavor[] dfa = dsc.getTransferable().getTransferDataFlavors();
  }

  public void transferablesFlavorsChanged(){
    ;
  }


  private void setDragDropInProgress(DragSourceContext dsc){
    if(!com.acunia.wonka.rudolph.DropTargetEvent.setInProgress(true,dsc)) throw new InvalidDnDOperationException("Drag and drop in progress");
  }
}
