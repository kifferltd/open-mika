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
import java.awt.peer.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import com.acunia.wonka.rudolph.*;

public class DropTarget implements DropTargetListener, Serializable{
  private Component component;
  private int actions = DnDConstants.ACTION_COPY_OR_MOVE;
  private boolean active = true;
  private FlavorMap flavorMap = SystemFlavorMap.getDefaultFlavorMap();
  private DropTargetListener dtListener;
  private DropTargetContext dropTargetContext;
  private ComponentPeer peer;
  private DropTargetAutoScroller scroller;
  private Point p = new Point();

  public DropTarget(Component c, int ops, DropTargetListener dtl,
                  boolean act, java.awt.datatransfer.FlavorMap fm){
    component = c;
    actions = ops;
    dtListener = dtl;
    active = act;
    flavorMap = fm;
    scroller = createDropTargetAutoScroller(component, p);
    dropTargetContext = createDropTargetContext();
    component.setDropTarget(this);
//maybe add somethings like addDropTarget(this) on DropTargetPeer
  }

  public DropTarget(Component c, int ops, DropTargetListener dtl,
                  boolean act){
    component = c;
    actions = ops;
    dtListener = dtl;
    active = act;
    scroller = createDropTargetAutoScroller(component, p);
    dropTargetContext = createDropTargetContext();
    component.setDropTarget(this);
    component.setEnabled(active);
  }

  public DropTarget(Component c, int ops, DropTargetListener dtl){
    component = c;
    actions = ops;
    dtListener = dtl;
    scroller = createDropTargetAutoScroller(component, p);
    dropTargetContext = createDropTargetContext();
    component.setDropTarget(this);
    component.setEnabled(active);
  }

  public DropTarget(Component c, DropTargetListener dtl){
    component = c;
    dtListener = dtl;
    scroller = createDropTargetAutoScroller(component, p);
    dropTargetContext = createDropTargetContext();
    component.setDropTarget(this);
    component.setEnabled(active);
  }

  public DropTarget(){
    scroller = createDropTargetAutoScroller(component, p);
    dropTargetContext = createDropTargetContext();
  }

  public void setComponent(Component c){
    component = c;
    scroller = createDropTargetAutoScroller(component, p);
//    dropTargetContext = createDropTargetContext();
//    component.setDropTarget(this);
//    component.setEnabled(active);
  }

  public Component getComponent(){
    return component;
  }

  public void setDefaultActions(int ops){
    actions = ops;
  }

  public int getDefaultActions(){
    return actions;
  }

  public void setActive(boolean isActive){
    active = isActive;
  }

  public boolean isActive(){
    return active;
  }

  public void addDropTargetListener(DropTargetListener dtl)
                           throws TooManyListenersException{
    if(dtListener!=null) throw new TooManyListenersException();
    else dtListener = dtl;
  }

  public void removeDropTargetListener(DropTargetListener dtl){
    if(dtListener.equals(dtl)) dtListener = null;
  }

  public void dragEnter(DropTargetDragEvent dtde){
    if(dtListener!=null) dtListener.dragEnter(dtde);
  }

  public void dragOver(DropTargetDragEvent dtde){
    if(dtListener!=null) dtListener.dragOver(dtde);
  }

  public void dropActionChanged(DropTargetDragEvent dtde){
    if(dtListener!=null) dtListener.dropActionChanged(dtde);
  }

  public void dragExit(DropTargetEvent dte){
    if(dtListener!=null) dtListener.dragExit(dte);
  }

  public void drop(DropTargetDropEvent dtde){
    if(dtListener!=null) dtListener.drop(dtde);
  }

  public java.awt.datatransfer.FlavorMap getFlavorMap(){
    return flavorMap;
  }

  public void setFlavorMap(java.awt.datatransfer.FlavorMap fm){
    flavorMap = fm;
  }

  public void addNotify(java.awt.peer.ComponentPeer peer){
    this.peer = peer;
  }

  public void removeNotify(java.awt.peer.ComponentPeer peer){
    if((this.peer).equals(peer)) this.peer = null;
  }

  public DropTargetContext getDropTargetContext(){
    return dropTargetContext;
  }

  protected java.awt.dnd.DropTargetContext createDropTargetContext(){
    com.acunia.wonka.rudolph.DropTargetContext dtc;
    dtc = new com.acunia.wonka.rudolph.DropTargetContext(this);
    return ((java.awt.dnd.DropTargetContext) dtc);
  }

  protected DropTarget.DropTargetAutoScroller createDropTargetAutoScroller(Component c, Point p){
    return new DropTargetAutoScroller(c,p);
  }

  protected void initializeAutoscrolling(Point p){
    scroller.stop();
    scroller.updateLocation(p);
  }

  protected void updateAutoscroll(Point dragCursorLocn){
    scroller.updateLocation(dragCursorLocn);
  }

  protected void clearAutoscroll(){
    scroller.stop();
  }


  protected static class DropTargetAutoScroller implements ActionListener{
    private Component component;
    private Point beginpoint;
    private Point endpoint;
    private boolean updaten;

    protected DropTargetAutoScroller(Component c, Point p){
      component = c;
      beginpoint = p;
      updaten = true;
    }

    protected void updateLocation(Point newLocn){
      if(updaten) endpoint = newLocn;
      else beginpoint = newLocn;
      updaten=true;
    }

    protected void stop(){
      updaten = false;
    }

    public void actionPerformed(ActionEvent e){
      if(updaten){
        //change the point in function of Actionevent e
        ;
      }
    }
  }
}

