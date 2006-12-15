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
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class DragGestureEvent extends EventObject {
  private int action;
  private Point point;
  private java.util.List list;

  public DragGestureEvent(DragGestureRecognizer dgr, int act, Point ori, java.util.List evs){
    super(dgr);
    if(dgr==null || ori==null || evs==null) throw new IllegalArgumentException();
    action =  act;
    point = ori;
    list = evs;
  }

  public DragGestureRecognizer getSourceAsDragGestureRecognizer(){
    return (DragGestureRecognizer)getSource();
  }

  public Component getComponent(){
    return ((DragGestureRecognizer)getSource()).getComponent();
  }

  public DragSource getDragSource(){
    return ((DragGestureRecognizer)getSource()).getDragSource();
  }

  public Point getDragOrigin(){
    return point;
  }

  public Iterator iterator(){
    return list.iterator();
  }

  public Object[] toArray(){
    return list.toArray();
  }

  public Object[] toArray(Object[] array){
    return list.toArray(array);
  }

  public int getDragAction(){
    return ((DragGestureRecognizer)getSource()).getSourceActions();
  }

  public InputEvent getTriggerEvent(){
    return ((DragGestureRecognizer)getSource()).getTriggerEvent();
  }

  public void startDrag(Cursor dragCursor, Transferable transferable, DragSourceListener dsl)
               throws InvalidDnDOperationException{
    getDragSource().startDrag(this, dragCursor, transferable, dsl);
  }

  public void startDrag(Cursor dragCursor, Image dragImage, Point imageOffset,
                      Transferable transferable, DragSourceListener dsl)
               throws InvalidDnDOperationException{
    getDragSource().startDrag(this, dragCursor, dragImage, imageOffset, transferable, dsl);
  }
}
