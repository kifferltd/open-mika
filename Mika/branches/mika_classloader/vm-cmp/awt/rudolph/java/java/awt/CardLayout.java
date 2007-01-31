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

import java.util.Hashtable;
import java.io.Serializable;

public class CardLayout implements LayoutManager2, Serializable {

  private static final long serialVersionUID = -4328196481005934313L;
  
  int hgap;
  int vgap;
  Hashtable tab = new Hashtable();
  
  private transient Hashtable tableC2N = new Hashtable();

  public CardLayout () {
    this(0, 0);
  }

  public CardLayout (int hgap, int vgap) {
    this.hgap = hgap;
    this.vgap = vgap;
  }

  public void addLayoutComponent(Component comp, Object constraints) {
    if (constraints instanceof String) {
      addLayoutComponent((String)constraints, comp);
    }
    else {
      throw new IllegalArgumentException("non-string constraint");
    }
  }

  public void addLayoutComponent(String name, Component comp) {
    if (comp.getParent().getComponentCount() != 1) comp.setVisible(false); else comp.setVisible(true);
    tab.put(name, comp);
    tableC2N.put(comp, name);
  }

  public void first(Container parent) {
    Component fc = null;
    int cc = parent.getComponentCount();

    for (int i = 0; i < cc; i++) {
      Component c = parent.getComponent(i);

      if (fc == null) {
        fc = c;
      }

      if (c.isVisible() == true) {
        if (c != fc) {
          c.setVisible(false);
          fc.setVisible(true);
        }

        parent.validate();

        return;
      }
    }
  }
 
  public int getHgap () {
    return hgap;
  }

  public float getLayoutAlignmentX(Container parent) {
    return (Component.CENTER_ALIGNMENT);
  }
 
  public float getLayoutAlignmentY(Container parent) {
    return (Component.CENTER_ALIGNMENT);
  }

  Dimension getLayoutSize(Container parent, boolean preferred) {
    Dimension d = new Dimension();
    int cc = parent.getComponentCount();

    for (int i = 0; i < cc; i++) {
      Component c = parent.getComponent(i);
      Dimension cd = preferred ? c.getPreferredSize() : c.getMinimumSize();
      d.width = Math.max(d.width, cd.width);
      d.height = Math.max(d.height, cd.height);
    }
  
    // use getInsets() instead of fields (might be redefined)
    Insets in = parent.getInsets();
    d.width += in.left + in.right;
    d.height += in.top + in.bottom;
  
    return d;
  }

  public int getVgap() {
    return vgap;
  }

  public void invalidateLayout(Container parent) {
    // Not used in CardLayout.
  }

  public void last(Container parent) {
    Component lc = null;
    int cc = parent.getComponentCount();

    for (int i = cc - 1; i >= 0; i--) {
      Component c = parent.getComponent(i);
      if (lc == null) {
        lc = c;
      }

      if (c.isVisible() == true) {
        if (c != lc) {
          c.setVisible(false);
          lc.setVisible(true);
        }

        parent.validate();

        return;
      }
    }
  }

  public void layoutContainer(Container parent) {
    Insets in = parent.getInsets();
    int cc = parent.getComponentCount();

    for (int i = 0; i < cc; i++) {
      parent.getComponent(i).setBounds(in.left + hgap, in.top + vgap, parent.width - 2 * hgap - in.left - in.right, parent.height - 2 * vgap - in.top - in.bottom );
    }
  }

  public Dimension maximumLayoutSize(Container parent) {

    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

  }

  public Dimension minimumLayoutSize(Container parent) {
    return getLayoutSize(parent, false);
  }

  public void next(Container parent) {
    Component lc = null;
    int cc = parent.getComponentCount();

    for (int i = 0; i < cc; i++) {
      Component c = parent.getComponent(i);
      if (c.isVisible() == true) {
        lc = c;
      }
      else if (lc != null) {
        lc.setVisible(false);
        c.setVisible(true);
       
        parent.validate();

        return;
      }
    }
  }

  public Dimension preferredLayoutSize(Container parent) {
    return getLayoutSize(parent, true);
  }

  public void previous(Container parent) {
    Component lc = null;
    int cc = parent.getComponentCount();

    for (int i = cc - 1; i >= 0; i--) {
      Component c = parent.getComponent(i);
      if (c.isVisible() == true) {
        lc = c;
      }
      else if (lc != null) {
        lc.setVisible(false);
        c.setVisible(true);

        parent.validate();

        return;
      }
    }
  }

  public void removeLayoutComponent(Component c) {
    String name = (String)tableC2N.get(c);
    if(name != null) tab.remove(name);
    tableC2N.remove(c);
    if (c.isVisible() == true) {
      previous(c.getParent());
    }
  }

  public void setHgap(int hgap) {
    this.hgap = hgap;
  }

  public void setVgap(int vgap) {
    this.vgap = vgap;
  }

  public void show(Container parent, String name) {
   
    if (name == null) {
      return;
    }

    Component nc = (Component)tab.get(name);

    if (nc == null) {
      return;
    }

    int cc = parent.getComponentCount();
  
    for ( int i = 0; i < cc; i++) {
      Component c = parent.getComponent(i);
      if (c.isVisible() == true) {
        c.setVisible(false);
      }
    }

    nc.setVisible(true);
    parent.validate();
  }

  public String toString() {
    return ("CardLayout: hgap: " + hgap + ", vgap: " + vgap);
  }
}
