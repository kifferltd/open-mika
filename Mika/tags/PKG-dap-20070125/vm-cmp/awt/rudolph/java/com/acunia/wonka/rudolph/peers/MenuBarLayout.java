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

import java.awt.*;

public class MenuBarLayout implements LayoutManager {

  public MenuBarLayout() {
  }

  public void addLayoutComponent(String name, Component comp) {
  }

  public void addLayoutComponent(Component comp, Object contstraints) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  private Dimension layoutSize(Container parent) {
    Dimension d;
    Insets insets = parent.getInsets();

    int ncomponents = parent.getComponentCount();
    
    int w = 0;
    int h = 0;

    for (int i = 0 ; i < ncomponents ; i++) {
      Component comp = parent.getComponent(i);

      d = comp.getPreferredSize();

      if (h < d.height) {
         h = d.height;  
      }
      
      w += d.width;  
    }

    return new Dimension(insets.left + insets.right + w, insets.top + insets.bottom + h);
  }

  public Dimension minimumLayoutSize(Container parent) {
    return layoutSize(parent);
  }  

  public Dimension preferredLayoutSize(Container parent) {
    return layoutSize(parent);
  }

  public Dimension maximumLayoutSize(Container parent) {
     return layoutSize(parent);
  }

  public void layoutContainer(Container parent) {
    Insets insets = parent.getInsets();
    int ncomponents = parent.getComponentCount();
    
    int h = parent.getSize().height - (insets.top + insets.bottom);
    int w = insets.left;
    int pw;
    
    for (int i = 0 ; i < ncomponents ; i++) {
      Component comp = parent.getComponent(i);

      pw = comp.getPreferredSize().width;

      comp.setBounds(w, insets.top, pw, h);

      w += pw;
    }
  }
  
}

