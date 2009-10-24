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

import java.awt.peer.*;
import java.awt.event.*;
import java.awt.*;

public class DefaultContainer extends DefaultComponent implements ContainerPeer {

  private int cw;  // cached width
  private int ch;  // cached height
  boolean validate = false;

  native public void createPeer(boolean nested);

  // Custom 
  native public void addComponent(Component component, int pos);
  native public void addContainer(Component component, int pos);
  native public void removeComponent(Component component);

  native public void scale(int w, int h);
  
  public DefaultContainer(Container container) {
    super(container);

    if (container instanceof Window) {
      // Create the native peer:
      createPeer(false);
    }
    else {
      // Create the native peer:
      createPeer(true);    
    }
  }

  public void beginValidate() {
    validate = true;
  }
  
  public void endValidate() {
    validate = false;
  }
  
  public Insets getInsets() {
    return null;
  }
 
  public void setBounds(int x, int y, int w, int h) {
    if (cw != w || ch != h) {
      scale(w, h);   // scale might take a while to complete as memory needs to be reallocated which is why we cache width and height
      ch = h;
      cw = w;
    }

    super.setBounds(x, y, w, h);
  }

  private void componentAdded(Component component) {
    Component[] components = component.getParent().getComponents();
    int pos;
    
    for(pos=0; pos < components.length && components[pos] != component; pos++);

    if (component instanceof Container) {
      addContainer(component, pos);
    }
    else {
      addComponent(component, pos);
    }

    if(pos != components.length - 1) {
      refresh(DefaultComponent.REFRESH_GLOBAL);
    }
  }

  private void componentRemoved(Component component) {
    removeComponent(component);
    component.validate();
    refresh(REFRESH_GLOBAL);
      // NOTE: apparently, removing a container does trigger a refresh from within the peer (normally without a validate). 
      //       What is more, from experiments, we can see that adding a component does not trigger such a refresh.
  }

  public boolean handleEvent(AWTEvent event) {
    if (event instanceof ContainerEvent) {
      ContainerEvent e = (ContainerEvent)event;
      switch (event.getID()) {
        case ContainerEvent.COMPONENT_ADDED:
          componentAdded(e.getChild());
          break;
        case ContainerEvent.COMPONENT_REMOVED:
          componentRemoved(e.getChild());
          break;
      }
    }

    return false;   // pass the event to the component's parent
  }

  /*
  ** Deprecated
  */
  
  public Insets insets() {
    return null;
  }
  
  public void doRepaint() {
    synchronized(component.getTreeLock()) {
      Graphics g = getGraphics();
      if (g != null) {
        component.update(g);
        
        /*
        ** Do a global refresh to repair the children of this Container.
        */
        
        refresh(DefaultComponent.REFRESH_GLOBAL);
      }
    }
  }
}

