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
