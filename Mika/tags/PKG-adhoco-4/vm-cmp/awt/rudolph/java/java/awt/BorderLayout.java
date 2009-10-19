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

import java.io.Serializable;

public class BorderLayout implements LayoutManager2, Serializable {
  /******************************/
  /**
   * Definitions
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public static final String NORTH = "North";
  public static final String SOUTH = "South";
  public static final String EAST = "East";
  public static final String WEST = "West";
  public static final String CENTER = "Center";

  /******************************/
  /**
   * variables
   */
  int hgap;
  int vgap;

  Component north;
  Component west;
  Component east;
  Component south;
  Component center;

  /******************************/
  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public BorderLayout() {
    this(0, 0);
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public BorderLayout(int hgap, int vgap) {
    if (hgap < 0){
      hgap = 0;
    }

    if (vgap < 0) {
      vgap = 0;
    }


    this.hgap = hgap;
    this.vgap = vgap;
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public int getHgap() {
    return hgap;
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public void setHgap(int hgap) {
    if (hgap < 0) {
      throw new IllegalArgumentException("invalid parameter");
    }

    this.hgap = hgap;
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public int getVgap() {
    return vgap;
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public void setVgap(int vgap) {
    if (vgap < 0) {
      throw new IllegalArgumentException("invalid parameter");
    }

    this.vgap = vgap;
  }
  
  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public Dimension preferredLayoutSize(Container container) {
    Insets insets = container.getInsets();
    int plw = 0;
    int plh = 0;

    if (east != null) {
      Dimension d = east.getPreferredSize();
      plw = plw + d.width + hgap;
      plh = Math.max(plh, d.height);
    }

    if (west != null) {
      Dimension d = west.getPreferredSize();
      plw = plw + d.width + hgap;
      plh = Math.max(plh, d.height);
    }

    if (center != null) {
      Dimension d = center.getPreferredSize();
      plw = plw + d.width;
      plh = Math.max(plh, d.height);
    }

    if (north != null) {
      Dimension d = north.getPreferredSize();
      plw = Math.max(plw, d.width);
      plh = plh + d.height + vgap;
    }

    if (south != null) {
      Dimension d = south.getPreferredSize();
      plw = Math.max(plw, d.width);
      plh = plh + d.height + vgap;
    }

    plw = plw + insets.left + insets.right;
    plh = plh + insets.top + insets.bottom;

    return new Dimension(plw, plh);
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  
  public Dimension minimumLayoutSize(Container container) {
    Insets insets = container.getInsets();
    int plw = 0;
    int plh = 0;

    if (east != null) {
      Dimension d = east.getPreferredSize();
      plw = plw + d.width + hgap;
      plh = Math.max(plh, d.height);
    }

    if (west != null) {
      Dimension d = west.getPreferredSize();
      plw = plw + d.width + hgap;
      plh = Math.max(plh, d.height);
    }

    if (center != null) {
      Dimension d = center.getPreferredSize();
      plw = plw + d.width;
      plh = Math.max(plh, d.height);
    }

    if (north != null) {
      Dimension d = north.getPreferredSize();
      plw = Math.max(plw, d.width);
      plh = plh + d.height + vgap;
    }

    if (south != null) {
      Dimension d = south.getPreferredSize();
      plw = Math.max(plw, d.width);
      plh = plh + d.height + vgap;
    }

    plw = plw + insets.left + insets.right;
    plh = plh + insets.top + insets.bottom;

    return new Dimension(plw, plh);
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public void addLayoutComponent(Component component, Object constraints) {
    if (constraints == null) {
      addLayoutComponent((String) null, (Component)component);
    }
   else if (constraints instanceof String) {
      addLayoutComponent((String) constraints, (Component)component);
    } 
    else {
      throw new IllegalArgumentException("cannot add component to BorderLayout: constraint must be a string (or null)");
    }
  }
  
  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public void addLayoutComponent(String name, Component comp) {
    if (name == null) {
      name = "Center";
    }

    if ("Center".equals(name)) { 
      center = comp;
    }
    else if ("North".equals(name)) {
      north = comp; 
    }
    else if ("East".equals(name)) {
      east = comp;
    }
    else if ("South".equals(name)) {
      south = comp;
    }
    else if ("West".equals(name)) {
      west = comp;
    }
    else {
      throw new IllegalArgumentException("cannot add component to BorderLayout: unknown constraint: " + name);
    }
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public void removeLayoutComponent(Component comp) {
    if (comp == center) {
      center = null;
    } 
    else if (comp == north) {
      north = null;
    }
    else if (comp == east) {
      east = null;
    }
    else if (comp == south) {
      south = null;
    }
    else if (comp == west) {
      west = null;
    }
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public Dimension maximumLayoutSize(Container container) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public float getLayoutAlignmentX(Container parent) {
    return Component.CENTER_ALIGNMENT;
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public float getLayoutAlignmentY(Container parent) {
    return Component.CENTER_ALIGNMENT;
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public void invalidateLayout(Container container) {
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public void layoutContainer(Container container) {
    Insets in = container.getInsets();

    int rn = in.top;
    int rw = in.left;
    int re = container.width - in.right;
    int rs = container.height - in.bottom;

    int w = 0;
    int h = 0;
    int x = 0;
    int y = 0;
    int wmax = 0;
    int hmax = 0;

/*
    if (north != null) {
      h = north.getPreferredSize().height;
      w = re - rw;
      wmax = north.getMaximumSize().width;
      x = 0;
      if (w > wmax) {
        x = (w - wmax)/2;
        w = wmax;
      }
      north.setBounds(rw + x, rn, w, h);
      rn = rn + h + vgap;
    }
*/
    if (north != null && north.isVisible()) {
      h = north.getPreferredSize().height;
      north.setBounds(rw, rn, re - rw, h);
      rn = rn + h + vgap;
    }

/*
    if (south != null) {
      h = south.getPreferredSize().height;
      w = re - rw;
      wmax = south.getMaximumSize().width;
      x = 0;
      if (w > wmax) {
        x = (w - wmax)/2;
        w = wmax;
      }
      south.setBounds(rw + x, rs - h, w, h);
      rs = rs - h - vgap;
    }
*/
    if (south != null && south.isVisible()) {
      h = south.getPreferredSize().height;
      south.setBounds(rw, rs - h, re - rw, h);
      rs = rs - h - vgap;
    }

    if (east != null && east.isVisible()) {
      w = east.getPreferredSize().width;
      h = rs - rn;
      hmax = east.getMaximumSize().height;
      y = 0;
      if (h > hmax) {
        y = (h - hmax)/2;
        h = hmax;
      }

      if((re - w) > 10000) {
        System.out.println("OOPS : " + re + " " + w + " " + container.width + " " + container + " " + east);
      }

      east.setBounds(re - w, rn + y, w, h);
      re = re - w - hgap;
    }

    if (west != null && west.isVisible()) {
      w = west.getPreferredSize().width;
      h = rs - rn;
      hmax = west.getMaximumSize().height;
      y = 0;
      if (h > hmax) {
        y = (h - hmax)/2;
        h = hmax;
      }
      west.setBounds(rw, rn + y, w, h);
      rw = rw + w + hgap;
    }

    if (center != null) {
      center.setBounds(rw, rn, re - rw, rs - rn);
    }
  }

  /**
   * @status  Complient with java specs 1.2 1999
   * @remark  Latest specs (2001) also list four supplementary definitions
   */
  public String toString() {
    return getClass().getName() + "[hgap = " + hgap + ", vgap = " + vgap + "]";
  }
}
