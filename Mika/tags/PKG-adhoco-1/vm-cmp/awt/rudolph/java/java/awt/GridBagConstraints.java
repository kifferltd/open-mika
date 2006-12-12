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

/*
** $Id: GridBagConstraints.java,v 1.1 2005/06/15 09:05:17 cvs Exp $
*/

package java.awt;

public class GridBagConstraints implements Cloneable, java.io.Serializable {

  // static field ensuring compatibility between java and wonka generated serialised objects
  private static final long serialVersionUID = -1000070633030801713L;

  // SIZE TYPE CONSTANT FIELDS
  // Specify that this component is the next-to-last component in its column or row (gridwidth, gridheight), or that this component be placed next to the previously added component (gridx, gridy).
  public static final int RELATIVE = -1;
  // Specify that this component is the last component in its column or row.
  public static final int REMAINDER = 0;

  // FILL TYPE CONSTANT FIELDS
  // Do not resize the component.
  public static final int NONE       = 0;
  // Resize the component both horizontally and vertically.
  public static final int BOTH       = 1;
  // Resize the component horizontally but not vertically.
  public static final int HORIZONTAL = 2;
  // Resize the component vertically but not horizontally.
  public static final int VERTICAL   = 3;

  // ANCHOR TYPE CONSTANT FIELDS
  // Put the component in the center of its display area.
  public static final int CENTER    = 10;
  // Put the component at the top of its display area, centered horizontally.
  public static final int NORTH     = 11;
  // Put the component at the top-right corner of its display area.
  public static final int NORTHEAST = 12;
  // Put the component on the right side of its display area, centered vertically.
  public static final int EAST      = 13;
  // Put the component at the bottom-right corner of its display area.
  public static final int SOUTHEAST = 14;
  // Put the component at the bottom of its display area, centered horizontally.
  public static final int SOUTH     = 15;
  // Put the component at the bottom-left corner of its display area.
  public static final int SOUTHWEST = 16;
  // Put the component on the left side of its display area, centered vertically.
  public static final int WEST      = 17;
  // Put the component at the top-left corner of its display area.
  public static final int NORTHWEST = 18;

  // Serialized fields

  // This field is used when the component is smaller than its display area.
  public int anchor = CENTER;
  // This field is used when the component's display area is larger than the component's requested size.
  public int fill = NONE;
  // Specifies the number of cells in a row for the component's display area.
  public int gridwidth = 1;
  // Specifies the number of cells in a column for the component's display area.
  public int gridheight = 1;

  // Specifies the cell at the left of the component's display area, where the leftmost cell has gridx=0.
  public int gridx = RELATIVE;
  // Specifies the cell at the top of the component's display area, where the topmost cell has gridy=0.
  public int gridy = RELATIVE;

  // This field specifies the external padding of the component, the minimum amount of space between the component and the edges of its display area.
  public Insets insets = null;
  // This field specifies the internal padding of the component, how much space to add to the minimum width of the component.
  public int ipadx = 0;
  // This field specifies the internal padding, that is, how much space to add to the minimum height of the component.
  public int ipady = 0;
  // Specifies how to distribute extra horizontal space.
  public double weightx = 0.0;
  // Specifies how to distribute extra vertical space.
  public double weighty = 0.0;

  // Temporary place holders required for serialized form but not used in this implementation.
  // It is assumed that no one will create or will be interested in serialized forms of this
  // object in a state, intermediate to start and end of the calculation of the gridbag layout.
  int tempX = 0;
  int tempY = 0;
  int tempWidth = 0;
  int tempHeight = 0;
  int minWidth = 0;
  int minHeight = 0;

  // Non serialized fields

  // Temporary place holder for the coordinates and size of the display area of the associated component.
  transient Rectangle displayArea = null;
  // Temporary place holder for the size of the associated component.
  transient Dimension compSize    = null;


  // Creates a GridBagConstraint object with all of its fields set to their default value.
  public GridBagConstraints() {
    super();
    this.insets = new Insets(0, 0, 0, 0);
//    this.displayArea = new Rectangle(0, 0, 0, 0);
  }

  // Creates a GridBagConstraints object with all of its fields set to the passed-in arguments.
  // jdk 1.3 method.
  public GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady) {
    super();
    this.gridx = gridx;
    this.gridy = gridy;
    this.gridwidth = gridwidth;
    this.gridheight = gridheight;
    this.weightx = weightx;
    this.weighty = weighty;
    this.anchor = anchor;
    this.insets = insets;
    this.fill = fill;
    this.ipadx = ipadx;
    this.ipady = ipady;
  }


  // Creates a copy of this grid bag constraint.
  public Object clone() {
    try {
      GridBagConstraints c = (GridBagConstraints)super.clone();
      // no need to copy all fields; super.clone() makes a bitwise copy of the memory allocated to 'this'
      c.insets = (Insets)this.insets.clone();
      return c;
    }
    catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }


}
