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

package java.awt;

import java.util.Vector;
import java.awt.event.*;
import java.awt.peer.*;

public class Window extends Container {

  private WindowListener windowListener;
  private String warningString;
  private Vector windowList;
  private boolean showed;
  private Frame owner;
  private Component focusOwner;
  private boolean disposed = false;

  public Window() {
    super(); 
    
    setVisible(false);
    windowList = new Vector();  
    warningString = "";
    owner = null;
  }

  public void addNotify() {
    if(peer == null) {
      peer = getToolkit().createWindow(this);
    }

    if(notified == false) {
      super.addNotify();
    }
  }
  
  protected void finalize() throws Throwable {
    dispose();
    super.finalize();
  }

  public Color getForeground() {
      // TODO: move to peer?
    Color result = super.getForeground();
    return (result != null ? result : new SystemColor(SystemColor.WINDOW_TEXT));
  }

  public Color getBackground() {
      // TODO: move to peer?
    Color result = super.getBackground();
    return (result != null ? result : new SystemColor(SystemColor.WINDOW));
  }

   /**
   * @status  not compliant
   * @remark  We don't perform security checks yet when a SecurityManager is present, and therefore the warning string is always the empty string.
   */ 
  public Window(Frame frame) {
    this();

    if (frame == null) {
      throw new IllegalArgumentException("the owner frame is null");
    }
    else {
      /*
      ** Assign the parent and the owner.
      */

      parent = owner = frame;

      /*
      ** Add the new window to the owner's list.
      */

      frame.addWindow(this);

      /*
      ** Post a "window openened" event.
      */

      postWindowEvent(WindowEvent.WINDOW_OPENED);
    }
  }

  public void setVisible(boolean visible) {
    super.setVisible(visible);

    if (visible == true) {
      validateTree();
      toFront();

      if (showed == false) {
        showed = true;
        postWindowEvent(WindowEvent.WINDOW_OPENED);
      }
      else {
        postWindowEvent(WindowEvent.WINDOW_DEICONIFIED);
      }
    }
    else {
      postWindowEvent(WindowEvent.WINDOW_ICONIFIED);
    }
  }

  public boolean isShowing() {
    return visible;
  }
 
  public synchronized void addWindowListener(WindowListener listener) {
    if (listener != null) {
      windowListener = AWTEventMulticaster.add(windowListener, listener);
    }
  }

  public void dispose() {

    if (!disposed) {
      disposed = true;
    }
    else {
      return;
    }

    synchronized(getTreeLock()) {
    
      /*
      ** Hide the window:
      */

      setVisible(false);

      /*
      ** Remove the window from the owner's list:
      */

      if (owner != null) {
        owner.removeWindow(this);
      }

      /*
      ** Remove the window peers from the internal component tree.
      */
      
      ((WindowPeer)peer).dispose();

      /*
      ** Send a "window closed" event:
      */

      postWindowEvent(WindowEvent.WINDOW_CLOSED);

    }
  }

  protected void dispatchEventImpl(AWTEvent event) {
    super.dispatchEventImpl(event);

    if (event instanceof FocusEvent) {
      switch (event.getID()) {
        case FocusEvent.FOCUS_GAINED: 
          postWindowEvent(WindowEvent.WINDOW_ACTIVATED);
          focusOwner = this;
          break;
        case FocusEvent.FOCUS_LOST:
          postWindowEvent(WindowEvent.WINDOW_DEACTIVATED);
          focusOwner = null;
          break;
      }
    }
  }
 
  public Component getFocusOwner() {
    return focusOwner;
  }
  
  public Toolkit getToolkit() {
    return Toolkit.getDefaultToolkit();
  }

  public final String getWarningString() {
    return warningString;
  }
  
  public void pack() {
    setSize(getPreferredSize());
    validate();
  }

  synchronized void postWindowEvent(int id) {
    if (windowListener != null) {
      processWindowEvent(new WindowEvent(this, id));
    }
  }

  protected void processEvent(AWTEvent event) {
    if (event instanceof WindowEvent) {
      processWindowEvent((WindowEvent)event);
    }
    else {
      super.processEvent(event);
    }
  }
  
  protected void processWindowEvent(WindowEvent event) {
    if (windowListener != null) {
      switch(event.getID()) {
        case WindowEvent.WINDOW_ACTIVATED:
          windowListener.windowActivated(event);
          break;
        case WindowEvent.WINDOW_CLOSED:
          windowListener.windowClosed(event);
          break; 
        case WindowEvent.WINDOW_CLOSING:
          windowListener.windowClosing(event);
          break;
        case WindowEvent.WINDOW_DEACTIVATED:
          windowListener.windowDeactivated(event);
          break;
        case WindowEvent.WINDOW_DEICONIFIED:
          windowListener.windowDeiconified(event);
          break;
        case WindowEvent.WINDOW_ICONIFIED:
          windowListener.windowIconified(event);
          break;
        case WindowEvent.WINDOW_OPENED:
          windowListener.windowOpened(event);
          break;
      }
    }
  }
  
  public synchronized void removeWindowListener(WindowListener listener) {
    if (listener != null) {
      windowListener = AWTEventMulticaster.remove(windowListener, listener);
    }
  }

  public void toBack() {
    ((WindowPeer)peer).toBack();
  }
    
  public void toFront() {
    ((WindowPeer)peer).toFront();
  }

  protected void addWindow(Window window) {
    windowList.add(window);
  }

  protected void removeWindow(Window window) {
    windowList.remove(window);
  }

  public Window getOwner() {
    return (Window)owner;
  }

  public Window[] getOwnedWindows() {
    return (Window [])windowList.toArray();
  }

  public void enableAllEvents() {
    super.enableAllEvents();
    for (int i = 0; i < windowList.size(); i++) {
      Window w = (Window)windowList.elementAt(i);
      w.enableAllEvents();
    } 
  }

  public void disableAllEvents() {
    super.disableAllEvents();
    for (int i = 0; i < windowList.size(); i++) {
      Window w = (Window)windowList.elementAt(i);
      w.disableAllEvents();
    } 
  }
}
