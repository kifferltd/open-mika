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


public class MouseDragGestureRecognizer extends DragGestureRecognizer
      implements MouseListener, MouseMotionListener{
  private boolean initialiseDrag = false;
  private boolean entered = false;

  protected MouseDragGestureRecognizer(DragSource ds, Component c,
                                     int act, DragGestureListener dgl){
    super(ds,c,act,dgl);
  }

  protected MouseDragGestureRecognizer(DragSource ds,
                                     Component c, int act){
    super(ds,c,act);
  }

  protected MouseDragGestureRecognizer(DragSource ds, Component c){
    super(ds, c);
  }

  protected MouseDragGestureRecognizer(DragSource ds){
    super(ds);
  }

  protected void registerListeners(){
    component.addMouseMotionListener(this);
    component.addMouseListener(this);
  }

  protected void unregisterListeners(){
    component.removeMouseMotionListener(this);
    component.removeMouseListener(this);
  }

  public void mouseClicked(MouseEvent e){
  }
  
  public void mousePressed(MouseEvent e){
    if(entered) initialiseDrag = true;
  }
  
  public void mouseReleased(MouseEvent e){
    initialiseDrag = false;
    if(entered) resetRecognizer();
  }

  public void mouseEntered(MouseEvent e){
    entered = true;
  }
  
  public void mouseExited(MouseEvent e){
    entered = false;
  }
  
  public void mouseDragged(MouseEvent e){
    if(initialiseDrag){
      initialiseDrag=false;
      appendEvent(e);
      fireDragGestureRecognized(getSourceActions(), e.getPoint());
    }
  }

  public void mouseMoved(MouseEvent e){
  }
}

