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
