/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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
