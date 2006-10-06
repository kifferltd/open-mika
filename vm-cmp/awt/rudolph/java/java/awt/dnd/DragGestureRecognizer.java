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
import java.awt.event.*;
import java.util.*;

public abstract class DragGestureRecognizer{
  protected DragSource dragSource;
  protected Component component;
  protected DragGestureListener dragGestureListener;
  protected int sourceActions;
  protected ArrayList events;

  protected DragGestureRecognizer(DragSource ds, Component c,
                                int sa, DragGestureListener dgl){
    if(ds==null) throw new IllegalArgumentException();
    dragSource = ds;
    component = c;
    sourceActions = sa;
    dragGestureListener = dgl;
    registerListeners();
  }

  protected DragGestureRecognizer(DragSource ds, Component c, int sa){
    if(ds==null) throw new IllegalArgumentException();
    dragSource = ds;
    component = c;
    sourceActions = sa;
    registerListeners();
  }

  protected DragGestureRecognizer(DragSource ds, Component c){
    if(ds==null) throw new IllegalArgumentException();
    dragSource = ds;
    component = c;
    registerListeners();
  }

  protected DragGestureRecognizer(DragSource ds){
    if(ds==null) throw new IllegalArgumentException();
    dragSource = ds;
  }

  protected abstract void registerListeners();

  protected abstract void unregisterListeners();

  public DragSource getDragSource(){
    return dragSource;
  }

  public Component getComponent(){
    return component;
  }

  public void setComponent(Component c){
    if(component != null) unregisterListeners();
    component = c;
    registerListeners();
  }

  public int getSourceActions(){
    return sourceActions;
  }

  public void setSourceActions(int actions){
    sourceActions = actions;
  }

  public InputEvent getTriggerEvent(){
    if(events!=null) return (InputEvent)events.get(0);
    else return null;
  }

  public void resetRecognizer(){
    com.acunia.wonka.rudolph.DropTargetEvent.setInProgress(false,null);
    events = null;
  }

  public void addDragGestureListener(DragGestureListener dgl)
                            throws TooManyListenersException{
    if(dragGestureListener!=null) throw new TooManyListenersException();
    dragGestureListener = dgl;
  }

  public void removeDragGestureListener(DragGestureListener dgl){
    if(!dragGestureListener.equals(dgl)) throw new IllegalArgumentException();
    dragGestureListener = null;
  }

  protected void fireDragGestureRecognized(int dragAction, Point p){
    dragGestureListener.dragGestureRecognized(new DragGestureEvent(this,dragAction,p,events));
    events = null;
  }

  protected void appendEvent(InputEvent awtie){
    if(awtie!=null){
      ArrayList al;
      if(events == null) al = new ArrayList(1);
      else  al = new ArrayList(events.size()+1);
      for(int i = 0; i < al.size(); i++){
        al.add(i,events.get(i));
      }
      al.add(al.size(),awtie);
      events = al;
    }
  }
}
