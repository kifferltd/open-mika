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
import java.awt.event.*;

public class DefaultDropTargetContextPeer implements DropTargetContextPeer{
  DragSourceContext dsc;
  private DropTarget dropTarget;
  private int targetActions;
  private int dragAction;
  private int dropAction;
  private EventDispatcher eventDispatcher;

  public DefaultDropTargetContextPeer(DropTarget dropTarget){
    this.dropTarget=dropTarget;
    eventDispatcher= new EventDispatcher();
  }

  public void acceptDrag(int dragAction){
    this.dragAction = dragAction;
  }

  public void acceptDrop(int dropAction){
    this.dropAction = dropAction;
  }

  public void dropComplete(boolean success){
    com.acunia.wonka.rudolph.DropTargetEvent.setInProgress(false,null);
  }

  public DropTarget getDropTarget(){
    return dropTarget;
  }

  public int getTargetActions(){
    return targetActions;
  }

  public Transferable getTransferable() throws InvalidDnDOperationException{
    if(dsc==null) return null;
    Transferable transf = dsc.getTransferable();
    return transf;
  }

  public DataFlavor[] getTransferDataFlavors(){
    if(getTransferable()==null) return null;
    return getTransferable().getTransferDataFlavors();
  }

  public boolean isTransferableJVMLocal(){
    return true;
  }

  public void rejectDrag(){
    ;
  }

  public void rejectDrop(){
    ;
  }

  public void setTargetActions(int actions){
    targetActions = actions;
  }

  protected void updateDragSourceContext(DragSourceContext dsc){
    this.dsc = dsc;
  }

  protected void processEnterMessage(DropTargetDragEvent dtde){
    dropTarget.dragEnter(dtde);

    /*select the cursor in function of the posibility to drop*/
    boolean accept = false;
    int source = dsc.getSourceActions();
    if(source==DnDConstants.ACTION_COPY_OR_MOVE){
      if(dragAction==DnDConstants.ACTION_COPY_OR_MOVE || dropAction==DnDConstants.ACTION_COPY || dropAction==DnDConstants.ACTION_MOVE)
        accept = true;
    } else{
      if(dragAction==DnDConstants.ACTION_COPY_OR_MOVE){
        if(source==DnDConstants.ACTION_COPY || source==DnDConstants.ACTION_MOVE)
          accept = true;
      } else{
        if(source==dragAction)accept = true;
      }
    }
    if(accept){
      if(dropAction==DnDConstants.ACTION_MOVE) dsc.setCursor(DragSource.DefaultMoveDrop);
      else {
        if(dropAction==DnDConstants.ACTION_LINK || dropAction==DnDConstants.ACTION_REFERENCE) dsc.setCursor(DragSource.DefaultLinkDrop);
        else dsc.setCursor(DragSource.DefaultCopyDrop);
      }
    } else dsc.setCursor(null);
  }

  protected void processMotionMessage(DropTargetDragEvent dtde){
    dropTarget.dragOver(dtde);
  }

  protected void processExitMessage(DropTargetEvent dte){
    dsc.setCursor(null);
    dropTarget.dragExit(dte);
  }

  protected void processDropMessage(DropTargetDropEvent dtde){
    dsc.setCursor(null);
    dropTarget.drop(dtde);
  }

  public EventDispatcher getEventDispatcher(){
    return eventDispatcher;
  }

  public class EventDispatcher{
  
    EventDispatcher(){
      ;
    }

    public void dispatchEvent(DropTargetEvent dte, MouseEvent me){
      DropTargetDragEvent dtde;
      // [CG 20050612] This generates warnings from Jikes, 'coz getDragSourceContext()
      // is a static method not an instance method. BTW why do we have a static
      // method in one class (com.acunia.wonka.rudolph.DropTargetEvent) with
      // the same name as an instance method in another (java.awt.dnd.DragSourceDragEvent)?
      // It's bloody confusing ...
      // updateDragSourceContext(((com.acunia.wonka.rudolph.DropTargetEvent)dte).getDragSourceContext());
      updateDragSourceContext(com.acunia.wonka.rudolph.DropTargetEvent.getDragSourceContext());
      int dropAction = dsc.getSourceActions();
      switch(me.getID()) {
        case MouseEvent.MOUSE_ENTERED:
System.out.println("MOUSE_ENTERED");
System.out.println("control is pressed: "+me.isControlDown());
System.out.println("shift is pressed: "+me.isShiftDown());
          if(me.isControlDown() && !me.isShiftDown()) dropAction = DnDConstants.ACTION_COPY;
          if(me.isShiftDown() && !me.isControlDown()) dropAction = DnDConstants.ACTION_MOVE;
System.out.println("dsc.getSourceActions() = "+dsc.getSourceActions());
System.out.println("dropAction = "+dropAction);
          dtde = new DropTargetDragEvent(dte.getDropTargetContext(),me.getPoint(),dropAction,dsc.getSourceActions());
          dispatchEnterEvent(dtde);
          break;
        case MouseEvent.MOUSE_EXITED:
          dispatchExitEvent(dte);
          break;
        case MouseEvent.MOUSE_DRAGGED:
System.out.println("control is pressed: "+me.isControlDown());
System.out.println("shift is pressed: "+me.isShiftDown());
          if(me.isControlDown() && !me.isShiftDown()) dropAction = DnDConstants.ACTION_COPY;
          if(me.isShiftDown() && !me.isControlDown()) dropAction = DnDConstants.ACTION_MOVE;
System.out.println("dsc.getSourceActions() = "+dsc.getSourceActions());
System.out.println("dropAction = "+dropAction);
          dtde = new DropTargetDragEvent(dte.getDropTargetContext(),me.getPoint(),dropAction,dsc.getSourceActions());
          dispatchMotionEvent(dtde);
          break;
        case MouseEvent.MOUSE_RELEASED_AFTER_DRAG:
System.out.println("control is pressed: "+me.isControlDown());
System.out.println("shift is pressed: "+me.isShiftDown());
          if(me.isControlDown() && !me.isShiftDown()) dropAction = DnDConstants.ACTION_COPY;
          if(me.isShiftDown() && !me.isControlDown()) dropAction = DnDConstants.ACTION_MOVE;
System.out.println("dsc.getSourceActions() = "+dsc.getSourceActions());
System.out.println("dropAction = "+dropAction);
          DropTargetDropEvent dtdrope = new DropTargetDropEvent(dte.getDropTargetContext(),me.getPoint(),dropAction,dsc.getSourceActions(),true);
          dispatchDropEvent(dtdrope);
          break;
      }
    }

    private void dispatchEnterEvent(DropTargetDragEvent dtde){
      processEnterMessage(dtde);
    }

    private void dispatchMotionEvent(DropTargetDragEvent dtde){
      processMotionMessage(dtde);
    }

    private void dispatchExitEvent(DropTargetEvent dte){
      processExitMessage(dte);
    }
    
    private void dispatchDropEvent(DropTargetDropEvent dtde){
      processDropMessage(dtde);
    }
  }
}
