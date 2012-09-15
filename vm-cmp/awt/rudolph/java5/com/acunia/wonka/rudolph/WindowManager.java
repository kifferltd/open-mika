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

package com.acunia.wonka.rudolph;

import java.awt.*;
import java.util.*;
import com.acunia.wonka.rudolph.peers.*;

import com.acunia.wonka.rudolph.taskbar.*;

public class WindowManager {

  private static WindowManager wm = null;
  private Vector windowList;
  private WeakHashMap decorations;
  private WeakHashMap menubars;

  public static WindowManager getInstance() {
    if(wm == null) wm = new WindowManager();
    return wm;
  }
  
  private WindowManager() {
    windowList = new Vector();
    decorations = new WeakHashMap();
    menubars = new WeakHashMap();
  }

  public Rectangle checkBounds(Window win, int x, int y, int w, int h) {
    if(win instanceof DecorationWindow || win instanceof MenuWindow) {
      return new Rectangle(x, y, w, h); 
    }
    if(!(win instanceof TaskBar)) {
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 
      int tl = TaskBar.getBarLocation();

      if(tl != TaskBar.FLOATING) {
        int ts = TaskBar.getBarSize();

        int ttop = (tl == TaskBar.TOP ? ts : 0);
        int tbot = (tl == TaskBar.BOTTOM ? ts : 0);
        int tlef = (tl == TaskBar.LEFT ? ts : 0);
        int trig = (tl == TaskBar.RIGHT ? ts : 0);

        if(x < tlef) { x = tlef; }
        if(y < ttop) { y = ttop; }
        if(x + w > screen.width - trig) { w = screen.width - x - trig; }
        if(y + h > screen.height - tbot) { h = screen.height - y - tbot; }
      }      
    }
    
    return updateWindow(win, x, y, w, h);
  }

  public void setVisible(Window win, boolean visible) {
    if(!(win instanceof DecorationWindow) && !(win instanceof MenuWindow)) {
      synchronized(win.getTreeLock()) {
        Decoration decoration = (Decoration)decorations.get(win);
        if(decoration != null) {
          decoration.top.setVisible(visible);
          decoration.bottom.setVisible(visible);
          decoration.left.setVisible(visible);
          decoration.right.setVisible(visible);
        }
        DefaultMenuBar menubar = (DefaultMenuBar)menubars.get(win);
        if(menubar != null) {
          menubar.getBarWindow().setVisible(visible);
        }
      }
    }
  }

  public void toBack(Window win) {
    if(!(win instanceof DecorationWindow) && !(win instanceof MenuWindow)) {
      synchronized(win.getTreeLock()) {
        Decoration decoration = (Decoration)decorations.get(win);
        if(decoration != null) {
          decoration.top.toBack();
          decoration.bottom.toBack();
          decoration.left.toBack();
          decoration.right.toBack();
        }
        DefaultMenuBar menubar = (DefaultMenuBar)menubars.get(win);
        if(menubar != null) {
          menubar.getBarWindow().toBack();
        }
      }
    }
  }

  public void setTitle(Window win, String title) {
    Decoration decoration = (Decoration)decorations.get(win);
    if(decoration != null) {
      decoration.top.setTitle(title);
    }
  }
  
  public void toFront(Window win) {
    if(!(win instanceof DecorationWindow) && !(win instanceof MenuWindow)) {
      synchronized(win.getTreeLock()) {
        Decoration decoration = (Decoration)decorations.get(win);
        if(decoration != null) {
          decoration.top.toFront();
          decoration.bottom.toFront();
          decoration.left.toFront();
          decoration.right.toFront();
        }
        DefaultMenuBar menubar = (DefaultMenuBar)menubars.get(win);
        if(menubar != null) {
          menubar.getBarWindow().toFront();
        }
      }
    }
    if(!(win instanceof TaskBar) && TaskBar.getTaskBar() != null) TaskBar.getTaskBar().toFront();
  }
  
  public void addWindow(Window win) {
    if(win instanceof DecorationWindow || win instanceof MenuWindow) return;
    if(!(win instanceof TaskBar)) { 
      synchronized(win.getTreeLock()) {
        TaskBar.getTaskBar();
        if(windowList.size() == 0) {
          Dispatcher.getMainDispatcher().start();
          Painter.getInstance().start();
        }
        if(win instanceof Frame) {
          windowList.add(win);
        }
        if(win instanceof Dialog || win instanceof Frame) {
          addWindowDecorations(win);
        }
      }
    }
  }

  public void removeWindow(Window win) {
    if(win instanceof DecorationWindow || win instanceof MenuWindow) return;
    if(!(win instanceof TaskBar)) { 
      synchronized(win.getTreeLock()) {
        if(win instanceof Frame) {
          windowList.remove(win);
        }
        if(win instanceof Dialog || win instanceof Frame) {
          removeWindowDecorations(win);
        }
        if(windowList.size() == 0) {
          Dispatcher.getMainDispatcher().stop();
          Painter.getInstance().stop();
        }
      }
    }
  }

  public Vector getWindows() {
    return windowList;
  }

  public void maximize(Window win) {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    win.setSize(screen.width, screen.height);
  }

  public void setMenuBar(Frame frame, DefaultMenuBar menubar) {
    DefaultMenuBar oldmenubar = (DefaultMenuBar)menubars.get(frame);
    Point loc = frame.getLocationOnScreen();
    Dimension dim = frame.getSize();

    if(menubar == null && oldmenubar != null) {

      /*
      ** Remove the current menubar.
      */

      menubars.remove(frame);
      Window oldbarwindow = oldmenubar.getBarWindow();
      int dy = oldbarwindow.getSize().height;

      
      Rectangle d = checkBounds(frame, loc.x, loc.y - dy, dim.width, dim.height + dy);
      frame.setBounds(loc.x, loc.y - dy, dim.width, dim.height + dy);
      
      oldbarwindow.setVisible(false);
    }
    else if(menubar != null && oldmenubar == null) {

      /*
      ** Add a menubar to a frame without one.
      */
      
      menubars.put(frame, menubar);
      Window bar = menubar.getBarWindow();
      int dy = bar.getPreferredSize().height;
      frame.setBounds(loc.x, loc.y + dy, dim.width, dim.height - dy);
      bar.setBounds(loc.x, loc.y, dim.width, dy);
      bar.setVisible(frame.isVisible());
    }
    else if(menubar != null && oldmenubar != null) {

      /*
      ** Replace the existing one.
      */
      
      setMenuBar(frame, null);
      setMenuBar(frame, menubar);
    }
  }

  private void addWindowDecorations(Window win) {
    Decoration decoration = new Decoration();
    decoration.top = new TitleWindow(win);
    decoration.bottom = new DecorationWindow();
    decoration.left = new DecorationWindow();
    decoration.right = new DecorationWindow();
    decorations.put(win, decoration);
  }

  private void removeWindowDecorations(Window win) {
    Decoration decoration = (Decoration)decorations.get(win);
    if(decoration != null) {
      decoration.top.dispose();
      decoration.bottom.dispose();
      decoration.left.dispose();
      decoration.right.dispose();
      decorations.remove(win);
    }
  }
  
  private Rectangle updateWindow(Window win, int x, int y, int w, int h) {
    Decoration decoration = (Decoration)decorations.get(win);
    int dy = 0;
    
    DefaultMenuBar menubar = (DefaultMenuBar)menubars.get(win);
    if(menubar != null) {
      dy = menubar.getBarWindow().getPreferredSize().height;
      menubar.getBarWindow().setBounds(x, y - dy, w, dy);
    }

    if(decoration != null) {
      decoration.top.setBounds(x - 1, y - 16 - dy, w + 2, 16);
      decoration.bottom.setBounds(x - 1, y + h, w + 2, 1);
      decoration.left.setBounds(x - 1, y - dy, 1, h + dy);
      decoration.right.setBounds(x + w, y - dy, 1, h + dy);
    }
    
    return new Rectangle(x, y, w, h);
  }
  
  private class Decoration {
    public TitleWindow top;
    public DecorationWindow bottom;
    public DecorationWindow left;
    public DecorationWindow right;
  }

}

