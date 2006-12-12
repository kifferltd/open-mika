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

class ScrollPaneLayout implements LayoutManager2 {
  
  public static final String HSB = "HSB";
  public static final String VSB = "VSB";
  public static final String VIEW = "VIEW";

  Component hsb;
  Component vsb;
  Component view;

  public ScrollPaneLayout() {
  }

  public Dimension preferredLayoutSize(Container container) {
    Insets insets = container.getInsets();
    int plw = 0;
    int plh = 0;

    if (vsb != null && vsb.isVisible()) {
      Dimension d = hsb.getPreferredSize();
      plw = plw + d.width;
      plh = Math.max(plh, d.height);
    }

    if (view != null) {
      Dimension d = view.getSize();
      plw = plw + d.width;
      plh = Math.max(plh, d.height);
    }

    if (hsb != null) {
      Dimension d = hsb.getPreferredSize();
      plw = Math.max(plw, d.width);
      plh = plh + d.height;
    }

    plw = plw + insets.left + insets.right;
    plh = plh + insets.top + insets.bottom;
 
    return new Dimension(plw, plh);
  }

  public void addLayoutComponent(Component component, Object constraints) {
    if (constraints == null) {
      addLayoutComponent((String) null, (Component)component);
      throw new IllegalArgumentException("cannot add component to ScrollPaneLayout: constraint must be a string");
    }
    else if (constraints instanceof String) {
      addLayoutComponent((String) constraints, (Component)component);
    } 
    else {
      throw new IllegalArgumentException("cannot add component to ScrollPaneLayout: constraint must be a string");
    }
  }
  
  public void addLayoutComponent(String name, Component comp) {
    if ("HSB".equals(name)) { 
      hsb = comp;
    }
    else if ("VSB".equals(name)) {
      vsb = comp; 
    }
    else if ("VIEW".equals(name)) {
      view = comp;
    }
    else {
      throw new IllegalArgumentException("cannot add component to ScrollPaneLayout: unknown constraint: " + name);
    }
  }

  public void removeLayoutComponent(Component comp) {
    if (comp == hsb) {
      hsb = null;
    } 
    else if (comp == vsb) {
      vsb = null;
    }
    else if (comp == view) {
      view = null;
    }
  }

  public Dimension maximumLayoutSize(Container container) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public Dimension minimumLayoutSize(Container container) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public float getLayoutAlignmentX(Container parent) {
    return parent.getAlignmentX();
  }

  public float getLayoutAlignmentY(Container parent) {
    return parent.getAlignmentY();
  }

  public void invalidateLayout(Container container) {
  }

  public void layoutContainer(Container container) {
    Insets in = container.getInsets();

    int rn = in.top;
    int rw = in.left;
    int re = container.width - in.right;
    int rs = container.height - in.bottom;

    if (hsb != null && hsb.isVisible()) {
      Dimension d = hsb.getPreferredSize();
      hsb.setBounds(rw, rs - d.height, re - rw, d.height);
      rs = rs - d.height;
    }

    if (vsb != null && vsb.isVisible()) {
      Dimension d = vsb.getPreferredSize();
      vsb.setBounds(re - d.width, rn, d.width, rs - rn);
      re = re - d.width;
    }

    if (view != null) {
      view.setBounds(rw, rn, re - rw, rs - rn);
    }
  }

  public String toString() {
    return getClass().getName();
  }
}
