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

package java.awt.dnd;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.peer.*;
import java.util.*;

public class DragSourceContext implements DragSourceListener{
  protected static final int DEFAULT = 0;
  protected static final int ENTER = 0;
  protected static final int OVER = 0;
  protected static final int CHANGED = 0;
  private DragGestureEvent trigger;
  private DragSourceContextPeer dscp;
  private Image image;
  private Point offset;
  private Transferable transferable;
  private DragSourceListener dragSourceListener;

  public DragSourceContext(java.awt.dnd.peer.DragSourceContextPeer dscp, DragGestureEvent trigger,
                         Cursor dragCursor, Image dragImage, Point offset, Transferable t,
                         DragSourceListener dsl){
    this.trigger = trigger;
    this.dscp=dscp;
    this.dscp.setCursor(dragCursor);
    image = dragImage;
    this.offset = offset;
    transferable = t;
    dragSourceListener = dsl;
  }

  public DragSource getDragSource(){
    return trigger.getDragSource();
  }

  public Component getComponent(){
    return trigger.getComponent();
  }

  public DragGestureEvent getTrigger(){
    return trigger;
  }

  public int getSourceActions(){
    return trigger.getDragAction();
  }

  public void setCursor(Cursor c){
    dscp.setCursor(c);
  }

  public Cursor getCursor(){
    return dscp.getCursor();
  }

  public void addDragSourceListener(DragSourceListener dsl)
                           throws TooManyListenersException{
    if(dragSourceListener==null) throw new TooManyListenersException();
    dragSourceListener = dsl;
  }

  public void removeDragSourceListener(DragSourceListener dsl){
    if(dragSourceListener.equals(dsl))dragSourceListener = null;
  }

  public void transferablesFlavorsChanged(){
    dscp.transferablesFlavorsChanged();
  }

  public void dragEnter(DragSourceDragEvent dsde){
    if(dragSourceListener!=null) dragSourceListener.dragEnter(dsde);
  }

  public void dragOver(DragSourceDragEvent dsde){
    if(dragSourceListener!=null) dragSourceListener.dragOver(dsde);
  }

  public void dragExit(DragSourceEvent dse){
    if(dragSourceListener!=null) dragSourceListener.dragExit(dse);
  }

  public void dropActionChanged(DragSourceDragEvent dsde){
    if(dragSourceListener!=null) dragSourceListener.dropActionChanged(dsde);
  }

  public void dragDropEnd(DragSourceDropEvent dsde){
    if(dragSourceListener!=null) dragSourceListener.dragDropEnd(dsde);
  }

  public Transferable getTransferable(){
    return transferable;
  }

  protected void updateCurrentCursor(int dropOp, int targetAct, int status){
//DO SOMETHING WITH THIS
    ;
  }
}
