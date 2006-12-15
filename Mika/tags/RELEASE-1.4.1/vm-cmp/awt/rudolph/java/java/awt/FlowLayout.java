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

public class FlowLayout implements LayoutManager, java.io.Serializable {

  private static final long serialVersionUID = -7262534875583282631L;

  /******************************/
  /**
   * Definitions
   * @status  Complient with java specs 1.2
   * @remark  LEADING and TRAILING are not used until Java 1.3
   */
  public static final int LEFT = 0;
  public static final int CENTER = 1;
  public static final int RIGHT = 2;
  public static final int LEADING = 3;
  public static final int TRAILING = 4;

  /******************************/
  /**
   * Variables
   */
  private int align;
  private int hgap;
  private int vgap;

  /******************************/
  /**
   * Constructors
   * @status  Complient with java specs 1.2
   * @remark  Complient with java specs 1.2
   */
  public FlowLayout() {
    this(CENTER, 2, 2);
  }

  public FlowLayout(int align) {
    this(align, 2, 2);
  }

  public FlowLayout(int align, int hgap, int vgap) {
    if ((align != LEFT) && (align != CENTER) && (align != RIGHT)) {
      throw new IllegalArgumentException("invalid alignment specified");
    }

    if ((hgap < 0) || (vgap < 0)) {
      throw new IllegalArgumentException("invalid gap value");
    }

    this.align = align;
    this.hgap = hgap;
    this.vgap = vgap;
  }

  /******************************/
  /**
   * Layout get & set
   * @status  Complient with java specs 1.2
   * @remark  Complient with java specs 1.2
   */
  public int getAlignment() {
    return align;
  }

  public void setAlignment(int align) {
    if ((align != LEFT) && (align != CENTER) && (align != RIGHT)) {
      throw new IllegalArgumentException("invalid alignment specified");
    }

    this.align = align;
  }

  public int getHgap() {
    return hgap;
  }

  public void setHgap(int hgap) {
    if (hgap < 0) {
      throw new IllegalArgumentException("invalid gap value");
    }

    this.hgap = hgap;
  }

  public int getVgap() {
    return vgap;
  }

  public void setVgap(int vgap) {
    if (vgap < 0) {
      throw new IllegalArgumentException("invalid gap value");
    }

    this.vgap = vgap;
  }

  /******************************/
  /**
   * Adding & removing components
   * @status  Complient with java specs 1.2
   * @remark  Complient with java specs 1.2
   */
  public void addLayoutComponent(String name, Component comp) {
    // not used in FlowLayout
  }

  public void removeLayoutComponent(Component comp) {
    // not used in FlowLayout
  }

  /******************************/
  /**
   * Minimum and preferred dimensions
   * @status  Complient with java specs 1.2
   * @remark  Complient with java specs 1.2
   */
  public Dimension preferredLayoutSize(Container container) {
    int plh = 0;
    int plw = 0;

    for (int i = 0; i < container.getComponentCount(); i++) {
      Component c = container.getComponent(i);
      if (c.visible) {
        Dimension d = c.getPreferredSize();
        if(d == null) d = new Dimension(0, 0);
        plh = Math.max(d.height, plh);
        plw = plw + hgap + d.width;
      }
    }

    Insets insets = container.getInsets();
    plw = plw + insets.left + hgap + insets.right;
    plh = plh + insets.top + vgap + vgap + insets.bottom;

    return new Dimension(plw, plh);
  }

  public Dimension minimumLayoutSize(Container container) {
    int plh = 0;
    int plw = 0;

    for (int i = 0; i < container.getComponentCount(); i++) {
      Component c = container.getComponent(i);
      if (c.visible) {
        Dimension d = c.getPreferredSize();
        plh = Math.max(d.height, plh);
        plw = plw + hgap + d.width;
      }
    }

    Insets insets = container.getInsets();
    plw = plw + insets.left + hgap + insets.right;
    plh = plh + insets.top + vgap + vgap + insets.bottom;

    return new Dimension(plw, plh);
  }

  /******************************/
  /**
   * Do layout
   * @status  Complient with java specs 1.2
   * @remark  Complient with java specs 1.2
   */
  public void layoutContainer(Container container) {
    Insets insets = container.getInsets();
    int offseth = 0;
    int begin = 0;
    int x = 0;
    int y = insets.top + vgap;
 
    for (int i = 0; i < container.getComponentCount(); i++) {
      Component c = container.getComponent(i);

      if (c.visible) {
        // Give the component his preferred size:
        Dimension d = c.getPreferredSize();
        if(d == null) d = new Dimension(0, 0);

        c.setSize(Math.min(d.width, container.width - vgap - vgap), Math.min(d.height, container.height - hgap - hgap));

        // Calculate relocation offsets:
        if (x + c.width + hgap > container.width - insets.left - insets.right) {
          relocateRow(container, begin, i, insets.left + hgap, y, container.width - insets.left - insets.right - x, offseth);

          begin = i;
          x = c.width;
          y = y + offseth + vgap;
          offseth = c.height;
        }
        else {
          offseth = Math.max(c.height, offseth);
          x = x + c.width + hgap;
        }
      }
    }

    relocateRow(container, begin, container.getComponentCount(), insets.left + hgap, y, container.width - insets.left - insets.right - x, offseth);
  }

  private void relocateRow(Container container, int begin, int end, int x, int y, int width, int height) {

    if (this.align == CENTER) {
      x = x + width / 2;
    }

    if (this.align == RIGHT || this.align == TRAILING) {
      x = x + width;
    }

    for (int i = begin; i < end; i++) {
      Component c = container.getComponent(i);
      if (c.visible) {
        c.setLocation(x, y + (height - c.height) / 2);
        x = x + c.width + hgap;
      }
    }
  }

  /******************************/
  /**
   * String presentation
   * @status  Complient with java specs 1.2 1999
   * @remark  Complient with java specs 1.2 1999
   */
  public String toString() {
    String str = new String();

    switch (align) {
      case LEFT:        
        str = ", align = left"; 
        break;
      case CENTER:
        str = ", align = center"; 
        break;
      case RIGHT:
        str = ", align = right"; 
        break;
      case LEADING:
        str = ", align = leading"; 
        break;
      case TRAILING:    
        str = ", align = trailing"; 
        break;
    }
    return getClass().getName() + "[hgap = " + hgap + ", vgap = " + vgap + str + "]";
  }

}
