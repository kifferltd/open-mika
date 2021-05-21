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

package com.acunia.wonka.rudolph.taskbar;

import java.awt.*;
import java.util.*;

public class TaskBarLayout implements LayoutManager2 {

  public static final String LEFT = "Left";
  public static final String CENTER = "Center";
  public static final String RIGHT = "Right";

  public static final String TOP = LEFT;
  public static final String BOTTOM = RIGHT;

  private Vector applets = new Vector();
  private Vector consts = new Vector();
  
  private int gap;

  public TaskBarLayout() {
    this(2);
  }

  public TaskBarLayout(int gap) {
    if (gap < 0) {
      throw new IllegalArgumentException("invalid gap value");
    }

    this.gap = gap;
  }

  public void addLayoutComponent(Component comp, Object constraints) {
    if(constraints instanceof TaskBarConstraints) {
      applets.add(comp);
      consts.add(constraints);
    }
  }
  
  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public Dimension preferredLayoutSize(Container container) {
    int psize = 0;
    int plh = 0;
    int plw = 0;

    TaskBar taskbar = (TaskBar)container;
    int orientation = orientation = TaskBar.getBarOrientation();

    for (int i = 0; i < taskbar.getAppletCount(); i++) {
      TaskBarApplet c = taskbar.getApplet(i);
      if (c.isVisible()) {
        psize += (orientation == TaskBar.HORIZONTAL ? c.getPreferredSize().width : c.getPreferredSize().height) + gap;
      }
    }

    Insets insets = taskbar.getInsets();

    if(orientation == TaskBar.HORIZONTAL) {
      plw = psize + insets.left + gap + insets.right;
      plh = taskbar.getSize().height;
    }
    else {
      plw = taskbar.getSize().width;
      plh = psize + insets.top + gap + insets.bottom;
    }
    
    return new Dimension(plw, plh);
  }

  public Dimension minimumLayoutSize(Container container) {
    int psize = 0;
    int plh = 0;
    int plw = 0;

    TaskBar taskbar = (TaskBar)container;
    int orientation = orientation = TaskBar.getBarOrientation();

    for (int i = 0; i < taskbar.getAppletCount(); i++) {
      TaskBarApplet c = taskbar.getApplet(i);
      if (c.isVisible()) {
        psize += (orientation == TaskBar.HORIZONTAL ? c.getMinimumSize().width : c.getMinimumSize().height) + gap;
      }
    }

    Insets insets = taskbar.getInsets();

    if(TaskBar.getBarOrientation() == TaskBar.HORIZONTAL) {
      plw = psize + insets.left + gap + insets.right;
      plh = taskbar.getSize().height;
    }
    else {
      plw = taskbar.getSize().width;
      plh = psize + insets.top + gap + insets.bottom;
    }
    
    return new Dimension(plw, plh);
  }

  public Dimension maximumLayoutSize(Container container) {
    int psize = 0;
    int plh = 0;
    int plw = 0;

    TaskBar taskbar = (TaskBar)container;
    int orientation = orientation = TaskBar.getBarOrientation();

    for (int i = 0; i < taskbar.getAppletCount(); i++) {
      TaskBarApplet c = taskbar.getApplet(i);
      if (c.isVisible()) {
        psize += (orientation == TaskBar.HORIZONTAL ? c.getMaximumSize().width : c.getMaximumSize().height) + gap;
      }
    }

    Insets insets = taskbar.getInsets();

    if(TaskBar.getBarOrientation() == TaskBar.HORIZONTAL) {
      plw = psize + insets.left + gap + insets.right;
      plh = taskbar.getSize().height;
    }
    else {
      plw = taskbar.getSize().width;
      plh = psize + insets.top + gap + insets.bottom;
    }
    
    return new Dimension(plw, plh);
  }
  
  public void layoutContainer(Container container) {
    TaskBar taskbar = (TaskBar)container;
    Insets insets = taskbar.getInsets();
    int pos;
    int height;
    
    if(TaskBar.getBarOrientation() == TaskBar.HORIZONTAL) {
      pos = insets.left;
      height = taskbar.getSize().height - insets.top - insets.bottom;
    }
    else {
      pos = insets.top;
      height = taskbar.getSize().width - insets.left - insets.right;
    }      
 
    for (int i = 0; i < applets.size(); i++) {
      TaskBarApplet c = (TaskBarApplet)applets.elementAt(i);
      TaskBarConstraints co = (TaskBarConstraints)consts.elementAt(i);

      if(co != null) pos = co.position;

      if (c.isVisible()) {
        int size = 0;
        if(TaskBar.getBarOrientation() == TaskBar.HORIZONTAL) {
          size = c.getPreferredSize().width;
          int cheight = c.getPreferredSize().height;
          c.setBounds(pos, insets.top + (height < cheight ? 0 : (height - cheight) / 2), size, (height < cheight ? height : cheight));
        }
        else {
          size = c.getPreferredSize().height;
          int cheight = c.getPreferredSize().width;
          c.setBounds(insets.left + (height < cheight ? 0 : (height - cheight) / 2), pos, (height < cheight ? height : cheight), size);
        }
        pos += size + gap;
      }
    }
  }
  
  public float getLayoutAlignmentX(Container parent) {
    return parent.getAlignmentX();
  }

  public float getLayoutAlignmentY(Container parent) {
    return parent.getAlignmentY();
  }

  public void invalidateLayout(Container container) {
  }

}
