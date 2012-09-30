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

import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Difference with JDK 1.2.2: the <code>GridBagLayoutInfo</code> object that
 * is written to the serialzed form is always null; it is assumed that no one
 * is interested in intermediate states of a <code>GridBagLayoutInfo</code> object
 * and that a deserialized <code>GridBagLayout</code> object will always
 * recalculate it before using it.
 */
public class GridBagLayout implements LayoutManager2, java.io.Serializable {

  // static field ensuring compatibility between java and wonka generated serialized objects
  private static final long serialVersionUID = 8838754796412211005L;

  // serialized fields

  public int columnWidths[];
  public int rowHeights[];
  public double columnWeights[];
  public double rowWeights[];

  protected java.util.Hashtable comptable;
  protected GridBagConstraints defaultConstraints = null;
  protected GridBagLayoutInfo  layoutInfo = null;

  protected static final int MAXGRIDSIZE = 512;
  protected static final int MINSIZE = 1;
  protected static final int PREFERREDSIZE = 2;

  // non serialzed fields

  private transient int rows = 0;
  private transient int cols = 0;
  private transient Toolkit toolkit = Toolkit.getDefaultToolkit();

  /**
   * Method called when writing the serial form of an object of this class.
   * Instead of the actual field 'layoutInfo' a null object is written.
   * It is assumed that no one will create or will be interested in the
   * serialized form of a GridBagLayoutInfo object in a state intermediate
   * to start and end of the layout process. We do not even know how the
   * serialized form of such an object would look like, what fields it
   * would contain. No public API or serialized form is specified for class
   * GridBagLayoutInfo.
   */
  private void writeObject(ObjectOutputStream os) throws IOException{
    GridBagLayoutInfo info = layoutInfo;
    layoutInfo = null;
    os.defaultWriteObject();
    layoutInfo = info;
  }

  // ------------- public methods ----------------

  public GridBagLayout() {
    super();
    comptable = new Hashtable();
    defaultConstraints = new GridBagConstraints();  // use default as created by constructor
  }


  /**
   * @status Implemented
   * @remark If the component parameter is null a  NullPointerException is thrown by a lower
   * level method. It is not caught by this method.
   */
  public void addLayoutComponent(Component comp, Object constraints) {
    // The method just stores the constraints object if one is given. Of course the constraints
    // will be retrievable via the associated component's reference.
    if (constraints != null) {
      if (constraints instanceof GridBagConstraints) {
        setConstraints(comp, (GridBagConstraints)constraints);
      }
      else {
        throw new IllegalArgumentException("illegal parameter type");
      }
    }
  }

  /**
   * @status Implemented
   * @remark Only defined in order to satisfy the LayoutManager interface.
   * Does nothing.
   */
  public void addLayoutComponent(String name, Component comp) {
    // Adds the specified component with the specified name to the layout.
  }

  /**
   * @status Implemented
   * @remark If the component parameter is null a NullPointerException will be thrown
   * by a lower level method. It is not caught by this method.
   */
  public GridBagConstraints getConstraints(Component component) {
    // Gets a copy of the constraints for the specified component.
    return (GridBagConstraints)(lookupConstraints(component).clone());
  }

  public float getLayoutAlignmentX(Container container) {
    // Returns the alignment along the x axis.
    return Component.CENTER_ALIGNMENT;
  }

  public float getLayoutAlignmentY(Container container) {
    // Returns the alignment along the y axis.
    return Component.CENTER_ALIGNMENT;
  }

  public int[][] getLayoutDimensions() {
    // Retrieves the dimensions of each column and row in the gridbag.


    if (layoutInfo == null) {
      return new int[2][0];
    }
    else {
      int[] widths  = layoutInfo.columnWidths;
      int[] heights = layoutInfo.rowHeights;

      int[][] dimensions = new int [2][];

      dimensions[0] = new int[widths.length];
      dimensions[1] = new int[heights.length];
      for (int i=0; i < widths.length; i++ ) {
        dimensions[0][i] = widths[i];
      }
      for (int j=0; j < heights.length; j++ ) {
        dimensions[1][j] = heights[j];
      }
      return dimensions;
    }
  }

  public Point getLayoutOrigin() {
    // Determines the origin of the layout grid.
    if (layoutInfo == null) {
      return new Point(0, 0);
    }
    else {
      return new Point(layoutInfo.xLeft, layoutInfo.yTop);
    }
  }

  public double[][] getLayoutWeights() {
    // Determines the weights of the layout grid's columns and rows.

    if (layoutInfo == null) {
      return new double[2][0];         // check that both lengths are zero!
    }
    else {
      double[] xWeights = layoutInfo.columnWeights;
      double[] yWeights = layoutInfo.rowWeights;

      double[][] weights = new double[2][];

      weights[0] = new double[xWeights.length];
      weights[1] = new double[yWeights.length];
      for (int i=0; i < xWeights.length; i++ ) {
        weights[0][i] = xWeights[i];
      }
      for (int j=0; j < yWeights.length; j++ ) {
        weights[1][j] = yWeights[j];
      }
      return weights;
    }
  }

  public void invalidateLayout(Container target) {
    // Invalidates the layout, indicating that if the layout manager has cached information
    // about state or layout, it should be discarded.
    // For gridBagLayout by default this method does nothing.
    // shouldn't it set the layoutInfo = null    ??
  }

  public void layoutContainer(Container container) {
    // Lays out the container's components using the components' constraints.
    // This method is called by container when the layout is invalidated and needs
    // to be redone. It uses the components' preferred sizes.
    this.arrangeGrid(container);
  }

  public Point location(int x, int y) {
    // Determines which cell in the layout grid contains the point specified by (x, y).

    if (layoutInfo == null) {
      return new Point(0,0);
    }
    else {
      int i;
      int j;
      int k;
      int l;

      k = layoutInfo.xLeft;
      i = 0;
      while (i < layoutInfo.ncols && x >= k) {
        k += layoutInfo.columnWidths[i];
        i++;
      }

      l = layoutInfo.yTop;
      j = 0;
      while (j < layoutInfo.nrows && y >= l) {
        l += layoutInfo.rowHeights[j];
        j++;
      }

      return new Point(i, j);
    }
  }

  public Dimension maximumLayoutSize(Container container) {
    // Returns the maximum dimensions for this layout given the components in the specified target container.
    //

    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public Dimension minimumLayoutSize(Container container) {
    // Determines the minimum size of the target container using this grid bag layout.

    if (container == null) {
      return new Dimension(0,0);
    }
    else {
      GridBagLayoutInfo info = getLayoutInfo(container, MINSIZE);
      return getMinSize(container, info);
    }
  }

  public Dimension preferredLayoutSize(Container container) {
    // Determines the preferred size of the target container using this grid bag layout.

    if (container == null) {
      return new Dimension(0,0);
    }
    else {
      GridBagLayoutInfo info = getLayoutInfo(container, PREFERREDSIZE);
      return getMinSize(container, info);
    }
  }

  public void removeLayoutComponent(Component comp) {
    // Removes the specified component from this layout.
    // According to the specs, this method does nothing for this layout manager
    // shoudn't it remove the component with its constraints from the hashtable 'comptable'?
  }

  /**
   * @status Implemented
   * @remark If the component parameter is null a NullPointerException is thrown by a lower
   * level method. It is not caught by this method. In addition, to satisfy the Sun specs
   * of this method, a NullPointerException is thrown by this method if the constraints
   * parameter is null.
   */
  public void setConstraints(Component comp, GridBagConstraints constraints) {
    // Sets the constraints for the specified component in this layout.
    if (constraints == null) {
      throw new NullPointerException();
    }
    else {
      GridBagConstraints cclone = (GridBagConstraints)constraints.clone();
      int x = cclone.gridx;
      int y = cclone.gridy;
      if (x < 0)
        x = GridBagConstraints.RELATIVE;
      if (y < 0)
        y = GridBagConstraints.RELATIVE;

      int w = cclone.gridwidth;
      int h = cclone.gridheight;
      if (w == 0)
        w = GridBagConstraints.REMAINDER;
      if (w < 0 && w != GridBagConstraints.REMAINDER && w != GridBagConstraints.RELATIVE)
        w = 1;
      if (h == 0)
        h = GridBagConstraints.REMAINDER;
      if (h < 0 && h != GridBagConstraints.REMAINDER && h != GridBagConstraints.RELATIVE)
        h = 1;

      cclone.gridx = x;
      cclone.gridy = y;
      cclone.gridwidth = w;
      cclone.gridheight = h;

      comptable.put(comp, cclone);    // overwrites if already present
    }
  }

  // TODO
  public String toString() {
    // Returns a string representation of this grid bag layout's values.
    return getClass().getName() + "[columnWidths = " + columnWidths + ", rowHeights = " + rowHeights + ", columnWeights = " + columnWeights + ", rowWeights = " + rowWeights + "]";
  }


  // ------------- protected methods ----------------

  /**
   * @status Implemented
   * @remark Throws an 'IllegalArgumentException' if an illegal value for an anchor
   * constraint is detected.
   */
  protected  void adjustForGravity(GridBagConstraints constraints, Rectangle displayArea) {

    // Apply gridbag constraints (insets, fill, ipad, and anchor) to a displayArea in order
    // to derive its sub area that will actually be occupied by a component.

    int restWidth;
    int restHeight;
    Dimension compSize = constraints.compSize;

    displayArea.x += constraints.insets.left;
    displayArea.width -= (constraints.insets.left + constraints.insets.right);
    displayArea.y += constraints.insets.top;
    displayArea.height -= (constraints.insets.top + constraints.insets.bottom);


    restWidth = displayArea.width - compSize.width - constraints.ipadx;
    if ( (restWidth > 0) && !(constraints.fill == GridBagConstraints.HORIZONTAL ||
          constraints.fill == GridBagConstraints.BOTH          ) ) {
      displayArea.width = compSize.width + constraints.ipadx;
    }
    else {
      restWidth = 0;
    }

    restHeight = displayArea.height - compSize.height - constraints.ipady;
    if ( (restHeight > 0) && !(constraints.fill == GridBagConstraints.VERTICAL ||
          constraints.fill == GridBagConstraints.BOTH       ) ) {
      displayArea.height = compSize.height + constraints.ipady;
    }
    else {
      restHeight = 0;
    }

    switch (constraints.anchor) {
      case GridBagConstraints.CENTER:
        displayArea.x += restWidth/2;
        displayArea.y += restHeight/2;
        break;
      case GridBagConstraints.NORTH:
        displayArea.x += restWidth/2;
        break;
      case GridBagConstraints.NORTHEAST:
        displayArea.x += restWidth;
        break;
      case GridBagConstraints.EAST:
        displayArea.x += restWidth;
        displayArea.y += restHeight/2;
        break;
      case GridBagConstraints.SOUTHEAST:
        displayArea.x += restWidth;
        displayArea.y += restHeight;
        break;
      case GridBagConstraints.SOUTH:
        displayArea.x += restWidth/2;
        displayArea.y += restHeight;
        break;
      case GridBagConstraints.SOUTHWEST:
        displayArea.y += restHeight;
        break;
      case GridBagConstraints.WEST:
        displayArea.y += restHeight/2;
        break;
      case GridBagConstraints.NORTHWEST:
        break;
      default:
        throw new IllegalArgumentException("illegal anchor value");
    }
  }

  protected  void arrangeGrid(Container container) {

    /*
     * Equivalent of 'layoutContainer'
     * Lays out the container's components based on their preferred sizes (making use
     * of 'getLayoutInfo()', resulting in a, say, preferred gridbag size.
     * Adapts the gridbag's columnWidths and rowHeights to the actual window area
     * available as defined by the container's size, resulting
     * in the actual gridbag size.
     * Calculates actual available pixel areas for each component based on the actual
     * gribag size, and adapts each component's bounds to its actual pixel area, using the
     * 'adjustForGravity()' method.
     */

    int i;
    int k;
    int l;
    int first;
    int last;
    int extraWidth;
    int deltaWidth;
    int restWidth;
    int extraHeight;
    int deltaHeight;
    int restHeight;
    double dz;
    double totalWeight;

    Component          components[] = container.getComponents();
    Component          component;
    GridBagLayoutInfo  info;
    Dimension          infoDim;
    GridBagConstraints constraints;
    Rectangle          bounds = new Rectangle();
    Rectangle          displayArea;
    Insets             containerInsets = container.getInsets();

    /*
     * If the container has no components, then nothing needs to be done.
     * If any columnWidths or rowHeights are given however, the gridBag is laid out
     * anyway.
     */

    if (components.length == 0 &&
        (this.columnWidths == null || this.columnWidths.length == 0) &&
        (this.rowHeights == null || this.rowHeights.length == 0)) {
      return;
    }

    /*
     * get the preferred size for the gridbag.
     */

    info = getLayoutInfo(container, PREFERREDSIZE);
    if (info.ncols == 0 && info.nrows == 0) {
      return;
    }

    layoutInfo = info;
    infoDim    = getMinSize(container, info);

    /*
     * If the current preferred dimensions of the gridbag, including the container's
     * insets, don't match the container dimensions, then, if the total weight of all
     * columns (rows) is greater than zero, adjust the layoutInfo
     * widths (heights) arrays according to their column (row) weights. These
     * weights determine how columns (rows) of the gridbag stretch and shrink.
     * Distribute any extra (positive or negative) space over the columns (rows) of
     * the gridbag. If after that, any extra space remains, then disribute it
     * symmetrically among the first and last columns (rows) of the gridbag.
     *
     * If the current preferred dimensions of the gridbag, including the container's
     * insets, don't match the container dimensions, then, if the total weight of all
     * columns (rows) is zero, then center the gridbag horizontally (vertically) in
     * the container.
     */

    /* // dump container and gridbag sizes before resizing the components.
       System.out.println("GridBagLayout.arrangeGrid:");
       System.out.println("  container width=" + container.width + " container height=" + container.height);
       System.out.println("  gridbag width  =" + infoDim.width +   " gridgbag height =" + infoDim.height);
     */

    extraWidth = container.width - infoDim.width;
    restWidth  = extraWidth;
    if (extraWidth != 0) {
      totalWeight = 0.0;
      for (i = 0; i < info.ncols; i++)
        totalWeight += info.columnWeights[i];
      if (totalWeight > 0.0) {
        dz=0;
        for (i = 0; i < info.ncols; i++) {
          if (info.columnWeights[i] > 0) {
            // include any remains of the non-integer width increment for the previous column, in
            // the non-integer width increment of the current column
            dz = (( ((double)extraWidth) * info.columnWeights[i]) / totalWeight + dz );
            deltaWidth = (int)dz;
            dz -= deltaWidth;
            if (info.columnWidths[i] + deltaWidth < 0) {
              restWidth += (info.columnWidths[i] + deltaWidth);
              info.columnWidths[i] = 0;
            }
            else {
              restWidth -= deltaWidth;
              info.columnWidths[i] += deltaWidth;
            }
          }
        }
        info.columnWidths[0] += restWidth/2;
        info.columnWidths[info.ncols-1] += (restWidth - restWidth/2);
        restWidth = 0;
      }
    }

    extraHeight = container.height - infoDim.height;
    restHeight  = extraHeight;
    if (extraHeight != 0) {
      totalWeight = 0.0;
      for (i = 0; i < info.nrows; i++)
        totalWeight += info.rowWeights[i];
      if (totalWeight > 0.0) {
        dz=0;
        for (i = 0; i < info.nrows; i++) {
          if (info.rowWeights[i] > 0) {
            // include any remains of the non-integer height increment for the previous row, in
            // the non-integer height increment of the current row
            dz = (( ((double)extraHeight) * info.rowWeights[i]) / totalWeight + dz);
            deltaHeight = (int)dz;
            dz -= deltaHeight;
            if (info.rowHeights[i] + deltaHeight < 0) {
              restHeight += (info.rowHeights[i] + deltaHeight);
              info.rowHeights[i] = 0;
            }
            else {
              restHeight -= deltaHeight;
              info.rowHeights[i] += deltaHeight;
            }
          }
        }
        info.rowHeights[0] += restHeight/2;
        info.rowHeights[info.nrows-1] += (restHeight - restHeight/2);
        restHeight = 0;
      }
    }

    info.xLeft = containerInsets.left + restWidth/2;
    info.yTop  = containerInsets.top  + restHeight/2;

    /*
     * Finally, calculate bounds for each (visible) component in the container,
     * based on its display area in the gridbag.
     * A displayarea's size is based on the widths and heights
     * of the grid cells that are covered by a component.
     * Recall from 'getGridBagLayoutInfo()' that the width and height of a grid
     * cell was based on the preferred size, the insets and ipad constraints of the
     * "largest" component in the corresponding grid column and of the "largest"
     * component in the corresponding grid row. Consequently, the actual component
     * that populates a particular cell(s) will in general be smaller than the
     * corresponding pixel size of that cell(s). Besides, by adapting
     * the gridbag to the actual container size, even these "largest" components
     * may not fit anymore in their cell(s).
     * Every component's bounds thus have to be adapted to the pixel size of the
     * gridbag cell(s) it populates. The component's gridbag constraints have to be
     * accounted for in this calculation.
     */

    for (i = 0 ; i < components.length ; i++) {
      component = components[i];
      if (component.isVisible()) {
        constraints = lookupConstraints(component);
        displayArea = constraints.displayArea;

        /*
         * Calculate the display area in pixels
         */
        first = displayArea.x;
        last  = displayArea.x + displayArea.width;

        bounds.x = info.xLeft;
        for(k = 0; k < first; k++) {
          bounds.x += info.columnWidths[k];
        }

        bounds.width = 0;
        for(k = first; k < last; k++) {
          bounds.width += info.columnWidths[k];
        }

        first = displayArea.y;
        last  = displayArea.y + displayArea.height;

        bounds.y = info.yTop;
        for(l = 0; l < first; l++) {
          bounds.y += info.rowHeights[l];
        }

        bounds.height = 0;
        for(l = first; l < last; l++) {
          bounds.height += info.rowHeights[l];
        }

        /*
         * Adjust the position and size of a component in its pixel area by applying
         * its individual preferred size and constraints to its pixel area.
         */
        adjustForGravity(constraints, bounds);

        /*
         * If the resulting bounds's sizes are strictly positive
         * then make the component fit to that display area.
         * Else shrink the component to zero size
         */
        if ((bounds.width > 0) && (bounds.height > 0)) {
          component.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        else {
          component.setBounds(0, 0, 0, 0);
        }
      }  // end if (component.isVisible())
    }
  }

  /**
   * @remark Calculates the gridbag by tentatively laying out all the components in the
   * gridbag using their preferred sizes, and independent of the actual size of the
   * components' container.
   */
  protected java.awt.GridBagLayoutInfo getLayoutInfo(Container container, int sizeflag) {

    if (sizeflag != MINSIZE && sizeflag != PREFERREDSIZE) {
      throw new IllegalArgumentException("illegal parameter value"); // what else can I do?
    }

      toolkit.lockAWT();
      try {

        int ncols = 0;                  // current number of gridbag columns
        int nrows = 0;                  // current number of gridbag rows
        int gridx;                      // gridx constraint of current component
        int gridy;                      // gridy constraint of current component
        int gridwidth;                  // gridwidth constraint of current component
        int gridheight;                 // gridheight constraint of current component
        int i;
        int j;
        int col;                           // index in the gridbag columns
        int row;                           // index in the gridbag rows
        double spanWeight;                 // weight of the range of columns (rows) covered by a component
        double extraWeight;                // difference between a component's column (row) weight and the spanWeight.
        double restWeight;                 // rest of the extraWeight after distribution over the columns (rows) covered by a component
        double deltaWeight;                // fraction of extraWeight added to a column (row) covered by a component
        int compWidth;                     // width of a component including its preferred size, inset and ipadx constraints
        int compHeight;                    // height of a component including its preferred size, inset and ipady constraints
        int spanWidth;                     // width of the range of columns covered by a component
        int extraWidth;                    // difference between compWidth and spanWidth
        int restWidth;                     // rest of extraWidth after distribution over the columns covered by a component
        int deltaWidth;                    // fraction of extraWidth added to a column covered by a component
        int spanHeight;                    // height of the range of rows covered by a component
        int extraHeight;                   // difference between compHeight and spanHeight
        int restHeight;                    // rest of extraHeight after distribution over the rows covered by a component
        int deltaHeight;                   // fraction of extraHeight added to a row covered by a component

        GridBagLayoutInfo  info;           // the GriBagLayoutInfo to be returned
        GridBagConstraints constr = null;  // variable used to point to the components constraints.
        Component[]        components = container.getComponents(); // the container's component's
        Dimension          compSize;       // the minimum or preferred size of a component
        Rectangle          displayArea;    // the range of columns and rows covered by a component in the gridbag.

        int[]              maxColInRow = new int[MAXGRIDSIZE];  // array containing for each row of the gridbag, the index of the maximum occupied column + 1
        int[]              maxRowInCol = new int[MAXGRIDSIZE];  // array containing for each column of the gridbag, the index of the maximum occupied row + 1

        int firsty;                        // first gridbag row occupied by current component
        int lasty;                         // gridbag row next to the last row occupied by current component
        int firstx;                        // first gridbag column occupied by current component
        int lastx;                         // gridbag column next to the last column occupied by current component
        int x = 0;                         // current column for positioning component with relative coordinates
        int y = 0;                         // current row for positioning component with relative coordinates

        boolean incrementColumn = true;    // indicates current direction of relative positioning (more info below)

        /*
         * 1. Determine the required number of columns and rows in the gridbag.
         *
         * This is straightforward if all gridx, gridy, gridwidth and gridheight
         * constraints are absolute values.
         * If some gridx and gridy constraints
         * have value RELATIVE and some gridwidth and gridheight constraints have
         * value REMAINDER, calculations become more difficult since
         * some components have to be positioned relative to other components.
         * Positions and sizes of such components have to be calculated in order to
         * derive their contribution to the final gridbag size.
         * Let us call a component with gridx and/or gridy constraints of value
         * RELATIVE, a component with relative coordinates. The x(y) position of
         * such a component is not influenced by the (absolute or relative)
         * position of components that are added to the container later than this
         * component. Neither is the x (y) position of such a component influenced
         * by components that are already absolutely positioned on a row and column
         * that are higher than the current row and the current column.
         * If the component is the first component added, its position is (0,0).
         * Components with relative coordinates that are added after one or more components
         * are present already, are positioned relative to these present components.
         * That relative position depends mainly on the presence of components with
         * gridwidth or gridheight constraints of value GridBagConstraints.REMAINDER.
         * The boolean variable "incrementColumn", remembers the fact that it was a
         * component's gridwidth constraint or a component's gridheight constraint that
         * most recently occured with a value GridBagConstraints.REMAINDER.
         * It will become true when a gridwidth constraint of value
         * GridBagConstraints.REMAINDER occurs. It will become false when a gridheight
         * constraint of value GridBagConstraints.REMAINDER occurs. Its initial value
         * is chosen to be 'true'.
         * For components to be positioned relative, a current column 'x' and row 'y' is
         * maintained.
         * If a component causes 'incrementColumn' to become 'true' then, as long as no
         * other gridwidth or gridheight constraint of value GridBagConstraints.REMAINDER
         * occurs, subsequent components with relative coordinates, must all be placed
         * on the row next to that of the component with the special gridwidth
         * constraint. To determine the column to be assigned to that component, one
         * must consider the contiguous area of free cells near the end of each of
         * the rows covered by that conmponent, and select the first column that is free
         * in all these areas simultaneously.
         * If a component causes 'incrementColumn' to become 'false', then, as long as no other
         * gridwidth or gridheight constraint of value GridBagConstraints.REMAINDER occurs,
         * subsequent components with relative coordinates, must all be placed
         * on the column next to that of the component with the special gridheight
         * constraint. To determine the row to be assigned to that component, one
         * must consider the contiguous area of free cells near the end of each of
         * the columns covered by that conmponent, and select the first row that is free
         * in all these areas simultaneously.
         */

        for (i = 0; i < components.length; i++) {
          if (components[i].isVisible()) {

            constr = lookupConstraints(components[i]);

            gridx = constr.gridx;
            gridy = constr.gridy;
            gridwidth  = constr.gridwidth;
            gridheight = constr.gridheight;

            /* // dump status before positioning the current component
               System.out.println("++++++++++++++++++++++++++++++++++++");
               System.out.println("Component number="+(i+1)+"/"+components.length);
               System.out.println("constr.gridx    ="+gridx    +" constr.gridy="+gridy);
               System.out.println("constr.gridwidth="+gridwidth+" constr.gridheight="+gridheight);
               System.out.println("First free row in each column & first free column in each row ");
               System.out.print("  ");
               for (j = 0; j < ncols; j++)
               System.out.print(maxRowInCol[j] + " ");
               System.out.println();
               for (j = 0; j < nrows; j++)
               System.out.println(maxColInRow[j]+ " ");
               System.out.println();
             */

            /*
             * The actual width (height) of a component having gridwidth (gridheight) constraint
             * of value REMAINDER or RELATIVE is not known since the final number of columns and
             * rows of the gridbag is not known yet.
             * For now it is sufficient to assume a width (height) of 1 for the display area
             * of such a component. If the grid column (row) of that component, or a higher
             * columns (row) contains another component with a greater gridwidth (gridheight), then the width
             * (height) of the former component will maximally be that of the latter component.
             * That latter component's width (height) then determines the gridbag width
             * (height).
             * If it turns out that there are no other columns (rows) behind this component,
             * then the width (height) of that component will effectively be 1.
             */

            if (gridwidth == GridBagConstraints.REMAINDER || gridwidth == GridBagConstraints.RELATIVE) {
              gridwidth = 1;
            }
            if (gridheight == GridBagConstraints.REMAINDER || gridheight == GridBagConstraints.RELATIVE) {
              gridheight = 1;
            }


            // calculate the top-left grid coordinate (firstx,firsty) to be occupied by the current component

            if (gridx == GridBagConstraints.RELATIVE && gridy == GridBagConstraints.RELATIVE){
              if (incrementColumn) {
                firsty = y;
                lasty = firsty + gridheight;
                // choose as firstx for this component, the first column not occupied by any
                // component, in the rows, starting with the current row, occupied by this component.
                firstx = 0;
                for (j = firsty; j < lasty; j++) {
                  if (maxColInRow[j] > firstx)
                    firstx = maxColInRow[j];
                }
              }
              else {
                firstx = x;
                lastx = firstx + gridwidth;
                // choose as firsty for this component, the first row not occupied by any
                // component, in the columns, starting with the current column, occupied by this component
                firsty = 0;
                for (j = firstx; j < lastx; j++) {
                  if (maxRowInCol[j] > firsty)
                    firsty = maxRowInCol[j];
                }
              }
            }
            else if (gridx == GridBagConstraints.RELATIVE) {
              // y coordinate is specified as absolute value
              // don't treat a component different if incrementColumn is true or false;
              // even if it is false, shift the component some columns to the right if required.
              firsty = gridy;
              lasty  = firsty + gridheight;
              firstx = 0;
              for (j = firsty; j < lasty; j++) {
                if (maxColInRow[j] > firstx)
                  firstx = maxColInRow[j];
              }
            }
            else if (gridy == GridBagConstraints.RELATIVE) {
              // x coordinate is specified as absolute value
              // don't treat a component different if incrementColumn is false or true;
              // even if it is true, shift the component some rows downwards if required.
              firstx = gridx;
              lastx  = firstx + gridwidth;
              firsty = 0;
              for (j = firstx; j < lastx; j++) {
                if (maxRowInCol[j] > firsty)
                  firsty = maxRowInCol[j];
              }
            }
            else {
              // no relative positioning at all.
              firstx = gridx;
              firsty = gridy;
            }

            // calculate the bottom-right grid coordinate (lastx,lasty) to be occupied by the
            // current component and update the currently known gridbag dimensions if needed
            // (the goal of this iteration over the components).

            lastx = firstx + gridwidth;
            lasty = firsty + gridheight;
            if (lastx > ncols)
              ncols = lastx;
            if (lasty > nrows)
              nrows = lasty;

            // All subsequent processing is preparation for the next iteration.

            // update the cached values of highest occupied columns (rows) in the rows (columns)
            // occupied by the present component.
            // difference with sun jdk 1.2.2 implementation: "only update the cache if the
            // new values are higher than the old ones".

            for (j = firsty; j < lasty; j++) {
              if (lastx > maxColInRow[j])       // different from sun jdk 1.2.2
                maxColInRow[j] = lastx;
            }
            for (j = firstx; j < lastx; j++) {
              if (lasty > maxRowInCol[j])       // different from sun jdk 1.2.2
                maxRowInCol[j] = lasty;
            }


            // set initial values for the next relative position
            if (constr.gridwidth == GridBagConstraints.REMAINDER  &&
                constr.gridheight == GridBagConstraints.REMAINDER) {
              // the current component must occupy all remaining columns and all remaining rows;
              // no other component can be positioned relative to (after and below) this component;
              // let's hope no one tries to and let's return to initial conditions to play save.
              x = 0;
              y = 0;
              incrementColumn = true;
            }
            else
              if (constr.gridwidth == GridBagConstraints.REMAINDER) {
                x = 0;
                y = lasty;
                incrementColumn = true;
              }
              else
                if (constr.gridheight == GridBagConstraints.REMAINDER) {
                  x = lastx;
                  y = 0;
                  incrementColumn = false;
                }
                else
                  if (incrementColumn) {
                    x = lastx;
                  }
                  else {
                    y = lasty;
                  }

            /* // dump status after positioning the current component
               System.out.println("DisplayArea:");
               System.out.println(" left col ="+firstx+" right  col ="+(lastx-1));
               System.out.println(" top  row ="+firsty+" bottom row ="+(lasty-1));
               System.out.println("Current gridSize: "+ncols+" x "+nrows);
               System.out.println("First free row in each column & first free column in each row ");
               System.out.print("  ");
               for (j = 0; j < ncols; j++)
               System.out.print(maxRowInCol[j] + " ");
               System.out.println();
               for (j = 0; j < nrows; j++)
               System.out.println(maxColInRow[j]);
               System.out.println();
             */
          }
        }

        // the size of the gridbag must at least be the size of the widths and heights
        // arrays, if they are given.

        if (this.columnWidths != null && this.columnWidths.length > ncols)
          ncols = this.columnWidths.length;
        if (this.rowHeights != null && this.rowHeights.length > nrows)
          nrows = this.rowHeights.length;

        info = new GridBagLayoutInfo(ncols, nrows);


        /*
         * 2. Calculate display areas for all visible components.
         *
         * To do so, position components with relative coordinates as it was done in the
         * first iteration. Sinds the actual size of the gridbag is known at this point,
         * actual widths and heights for components having gridwidth and/or gridheight
         * constraints of value GridBagConstraints.RELATIVE and GridBagConstraints.REMAINDER
         * can now be assigned.
         */

        incrementColumn = true;
        x = y = 0;
        maxColInRow = new int[MAXGRIDSIZE];
        maxRowInCol = new int[MAXGRIDSIZE];

        for (i = 0; i < components.length; i++) {
          if (components[i].isVisible()) {
            constr = lookupConstraints(components[i]);

            gridx = constr.gridx;
            gridy = constr.gridy;
            gridwidth  = constr.gridwidth;
            gridheight = constr.gridheight;

            /* // dump status before positioning the current component
               System.out.println("++++++++++++++++++++++++++++++++++++");
               System.out.println("Component number="+(i+1)+"/"+components.length);
               System.out.println("constr.gridx    ="+gridx    +" constr.gridy="+gridy);
               System.out.println("constr.gridwidth="+gridwidth+" constr.gridheight="+gridheight);
               System.out.println("First free row in each column & first free column in each row ");
               System.out.print("  ");
               for (j = 0; j < ncols; j++)
               System.out.print(maxRowInCol[j] + " ");
               System.out.println();
               for (j = 0; j < nrows; j++)
               System.out.println(maxColInRow[j]+ " ");
               System.out.println();
             */

            /*
             * This time, the final number of columns and rows of the gridbag are known,
             * so that actual width (height) of the display area can be calculated for a
             * component having gridwidth (gridheight) constraint of value
             * GridBagConstraints.REMAINDER or GridBagConstraints.RELATIVE. Start
             * calculating the lastx (lasty) value of their displayArea.
             */

            if (gridwidth == GridBagConstraints.RELATIVE)
              lastx = ncols -1;
            else if (gridwidth == GridBagConstraints.REMAINDER)
              lastx = ncols;
            else
              lastx = 0;

            if (gridheight == GridBagConstraints.RELATIVE)
              lasty = nrows - 1;
            else if (gridheight == GridBagConstraints.REMAINDER)
              lasty = nrows;
            else
              lasty = 0;

            // calculate the top-left grid coordinate (firstx,firsty) to be occupied by the current component

            if (gridx == GridBagConstraints.RELATIVE && gridy == GridBagConstraints.RELATIVE){
              if (incrementColumn) {
                firsty = y;
                if (gridheight != GridBagConstraints.RELATIVE && gridheight != GridBagConstraints.REMAINDER)
                  lasty = firsty + gridheight;

                // choose as firstx for this component, the first column not occupied by any
                // component, in the rows, starting with the current row, occupied by this component.
                firstx = 0;
                for (j = firsty; j < lasty; j++) {
                  if (maxColInRow[j] > firstx)
                    firstx = maxColInRow[j];
                }
              }
              else {
                firstx = x;
                if (gridwidth != GridBagConstraints.RELATIVE && gridwidth != GridBagConstraints.REMAINDER)
                  lastx = firstx + gridwidth;

                // choose as firsty for this component, the first row not occupied by any
                // component, in the columns, starting with the current column, occupied by this component
                firsty = 0;
                for (j = firstx; j < lastx; j++) {
                  if (maxRowInCol[j] > firsty)
                    firsty = maxRowInCol[j];
                }
              }
            }
            else if (gridx == GridBagConstraints.RELATIVE) {
              // y coordinate is specified as absolute value
              // don't treat a component different if incrementColumn is true or false;
              // even if it is false, shift the component some columns to the right if required.
              firsty = gridy;
              if (gridheight != GridBagConstraints.RELATIVE && gridheight != GridBagConstraints.REMAINDER)
                lasty = firsty + gridheight;

              firstx = 0;
              for (j = firsty; j < lasty; j++) {
                if (maxColInRow[j] > firstx)
                  firstx = maxColInRow[j];
              }
            }
            else if (gridy == GridBagConstraints.RELATIVE) {
              // x coordinate is specified as absolute value
              // don't treat a component different if incrementColumn is false or true;
              // even if it is true, shift the component some rows downwards if required.
              firstx = gridx;
              if (gridwidth != GridBagConstraints.RELATIVE && gridwidth != GridBagConstraints.REMAINDER)
                lastx = firstx + gridwidth;

              firsty = 0;
              for (j = firstx; j < lastx; j++) {
                if (maxRowInCol[j] > firsty)
                  firsty = maxRowInCol[j];
              }
            }
            else {
              // no relative positioning at all.
              firstx = gridx;
              firsty = gridy;
            }

            // calculate the absolute gridwidth and gridheight for the current component
            // if it is unknown yet.

            if (gridwidth == GridBagConstraints.RELATIVE || gridwidth == GridBagConstraints.REMAINDER)
              gridwidth = lastx - firstx;
            if (gridheight == GridBagConstraints.RELATIVE || gridheight == GridBagConstraints.REMAINDER)
              gridheight = lasty - firsty;

            // store the component's display area as a rectangle with its constraints.
            constr.displayArea = new Rectangle(firstx, firsty, gridwidth, gridheight);

            // store the component's size with its constraints for later use
            if (sizeflag == PREFERREDSIZE)
              constr.compSize = components[i].getPreferredSize();
            else
              constr.compSize = components[i].getMinimumSize();

            // All subsequent processing is preparing for the next iteration.

            // update the cached values of highest occupied columns (rows) in the rows (columns)
            // occupied by the present component.
            // difference with sun jdk 1.2.2 implementation: "only update the cache if the
            // new values are higher than the old ones".

            lastx = firstx + gridwidth;    // lastx might not be known yet in some cases
            lasty = firsty + gridheight;   // lasty might not be known yet in some cases

            for (j = firsty; j < lasty; j++) {
              if (lastx > maxColInRow[j])       // different from sun jdk 1.2.2
                maxColInRow[j] = lastx;
            }
            for (j = firstx; j < lastx; j++) {
              if (lasty > maxRowInCol[j])       // different from sun jdk 1.2.2
                maxRowInCol[j] = lasty;
            }

            // set initial values for the next relative position
            if (constr.gridwidth == GridBagConstraints.REMAINDER  &&
                constr.gridheight == GridBagConstraints.REMAINDER) {
              // the current component must cover all remaining columns and all remaining rows in
              // in the gridbag; no other component can be positioned relative to (after and below)
              // this component; let's hope no one tries to and let's return to initial conditions
              // to play save.
              y = 0;
              x = 0;
              incrementColumn = true;
            }
            else
              if (constr.gridwidth == GridBagConstraints.REMAINDER) {
                x = 0;
                y = lasty;
                incrementColumn = true;
              }
              else
                if (constr.gridheight == GridBagConstraints.REMAINDER) {
                  x = lastx;
                  y = 0;
                  incrementColumn = false;
                }
                else
                  if (incrementColumn) {
                    x = lastx;
                  }
                  else {
                    y = lasty;
                  }

            /* // dump status after positioning the current component
               System.out.println("DisplayArea:");
               System.out.println(" left col ="+firstx+" right  col ="+(lastx-1));
               System.out.println(" top  row ="+firsty+" bottom row ="+(lasty-1));
               System.out.println("Current gridSize: "+ncols+" x "+nrows);
               System.out.println("First free row in each column & first free column in each row ");
               System.out.print("  ");
               for (j = 0; j < ncols; j++)
               System.out.print(maxRowInCol[j] + " ");
               System.out.println();
               for (j = 0; j < nrows; j++)
               System.out.println(maxColInRow[j]);
               System.out.println();
             */
          }

        }


        /*
         * 3. Calculate weights of all gridbag columns & rows based on the weight
         * constraints of the components in the container.
         * Start with all components width displayarea width (height) constraint equal to 1; then
         * consider components width gridwith (gridheight) constraint > 1, in the order that
         * they were inserted in the container
         */

        //       possible optimisation to applying overwrites after weights are calculated:
        //       apply overwrites before weights are calculated, i.e. initialise the weights
        //       with the overwrites here:
        //
        //        if (columnWeights != null)
        //          System.arraycopy(this.columnWeights, 0, info.columnWeights, 0, this.columnWeights.length);
        //        if (rowWeights != null)
        //          System.arraycopy(this.rowWeights, 0, info.rowWeights, 0, this.rowWeights.length);

        for (i = 0; i < components.length; i++) {
          if (components[i].isVisible()) {
            constr = lookupConstraints(components[i]);
            displayArea = constr.displayArea;

            if (displayArea.width == 1 && constr.weightx > info.columnWeights[displayArea.x])
              info.columnWeights[displayArea.x] = constr.weightx;
            if (displayArea.height == 1 && constr.weighty > info.rowWeights[displayArea.y])
              info.rowWeights[displayArea.y] = constr.weighty;
          }
        }

        for (i = 0; i < components.length; i++) {
          if (components[i].isVisible()) {

            constr = lookupConstraints(components[i]);
            displayArea = constr.displayArea;

            if (displayArea.width > 1) {
              firstx = displayArea.x;
              lastx  = displayArea.x + displayArea.width;
              spanWeight = 0.0;
              for (col = firstx; col < lastx; col++){
                spanWeight += info.columnWeights[col];
              }
              extraWeight = constr.weightx - spanWeight;
              if (extraWeight > 0.0) {
                restWeight = extraWeight;
                if (spanWeight > 0.0){
                  for (col = firstx; col < lastx; col++){
                    if (info.columnWeights[col] > 0.0) {
                      deltaWeight = (extraWeight * info.columnWeights[col]) / spanWeight;
                      info.columnWeights[col] += deltaWeight;
                      restWeight -= deltaWeight;
                    }
                  }
                }
                info.columnWeights[lastx - 1] += restWeight;
              }
            }


            if (displayArea.height > 1) {
              firsty = displayArea.y;
              lasty  = displayArea.y + displayArea.height;
              spanWeight = 0.0;
              for (row = firsty; row < lasty; row++){
                spanWeight += info.rowWeights[row];
              }
              extraWeight = constr.weighty - spanWeight;
              if (extraWeight > 0.0) {
                restWeight = extraWeight;
                if (spanWeight > 0.0) {
                  for (row = firsty; row < lasty; row++){
                    if (info.rowWeights[row] > 0.0) {
                      deltaWeight = (extraWeight * info.rowWeights[row]) / spanWeight;
                      info.rowWeights[row] += deltaWeight;
                      restWeight -= deltaWeight;
                    }
                  }
                }
                info.rowWeights[lasty - 1] += restWeight;
              }
            }

          } // end if (comnponents[i].isVisible())

        }      // end for (int i = 0; i < components.length; i++) {

        /*  // dump calculated column and row weights
            for (col = 0; col < info.ncols; col++)
            System.out.println("info.columnWeights["+col+"]="+info.columnWeights[col]);
            for (row = 0; row < info.nrows; row++)
            System.out.println("info.rowWeights["+row+"]="+info.rowWeights[row]);
         */

        // Overwrite the calculated values if overwrites for the weights are given
        // Nothing is known about the length of the overwrite arrays; the maximum
        // number of columnWeights (rowWeights) that can be overwritten is the length
        // of the shortest of the arrays this.columnWeights and info.columnWeights
        // (this.rowWeights and info.rowWeights)

        if (this.columnWeights != null) {
          lastx = this.columnWeights.length < info.columnWeights.length ? this.columnWeights.length : info.columnWeights.length;
          for (col = 0; col < lastx; col++) {
            if (this.columnWeights[col] > info.columnWeights[col])
              info.columnWeights[col] = this.columnWeights[col];
          }
        }

        if (this.rowWeights != null) {
          lasty = this.rowWeights.length < info.rowWeights.length ? this.rowWeights.length : info.rowWeights.length;
          for (row = 0; row < lasty; row++) {
            if (this.rowWeights[row] > info.rowWeights[row])
              info.rowWeights[row] = this.rowWeights[row];
          }
        }

        /*
         * 4. Calculate widths of all columns and heights of all rows of the gridbag; make
         * use of the width (height) of all the components in a column (row) to do so,
         * and take inset and ipad constraints of components into account.
         * It is not specified how to distribute a component's width (height) over several
         * columns (rows) of the grid, in case the components' gridwidth (gridheight)
         * constraint is > 1; let's do it the same way the weights are distributed higher;
         * that seems a good idea.
         * Start with all components completely contained in one grid column (row) i.e.
         * components with gridwith (gridheight) constraint equal to 1; then
         * consider components covering several grid columns (rows) i.e. components with
         * gridwith (gridheight) constraint > 1, in the order that they were inserted in
         * the container; the chance that such a component adds any extra width (height)
         * to the columns (rows) of its span is rather small if one
         * of these columns (rows) contains a complete component already;
         * remark that initial values in info.columnWidths and info.rowHeights are zero.
         */

        for (i = 0; i < components.length; i++) {
          if (components[i].isVisible()) {
            constr = lookupConstraints(components[i]);
            displayArea = constr.displayArea;
            compSize    = constr.compSize;

            if (displayArea.width == 1) {
              compWidth = compSize.width + constr.insets.left + constr.insets.right + constr.ipadx;
              if (compWidth > info.columnWidths[displayArea.x])
                info.columnWidths[displayArea.x] = compWidth;
            }
            if (displayArea.height == 1) {
              compHeight = compSize.height + constr.insets.top + constr.insets.bottom + constr.ipady;
              if (compHeight > info.rowHeights[displayArea.y])
                info.rowHeights[displayArea.y] = compHeight;
            }
          }
        }

        for (i = 0; i < components.length; i++) {
          if (components[i].isVisible()) {

            constr = lookupConstraints(components[i]);
            displayArea = constr.displayArea;
            compSize    = constr.compSize;

            if (displayArea.width > 1) {
              compWidth = compSize.width + constr.insets.left + constr.insets.right + constr.ipadx;
              firstx = displayArea.x;
              lastx  = displayArea.x + displayArea.width;
              spanWidth = 0;
              for (col = firstx; col < lastx; col++){
                spanWidth += info.columnWidths[col];
              }
              extraWidth = compWidth - spanWidth;
              if (extraWidth > 0) {
                spanWeight = 0.0;
                for (col = firstx; col < lastx; col++){
                  spanWeight += info.columnWeights[col];
                }
                restWidth = extraWidth;
                if (spanWeight > 0.0){
                  for (col = firstx; col < lastx; col++){
                    if (info.columnWeights[col] > 0.0) {
                      deltaWidth = (int)( (((double)extraWidth) * info.columnWeights[col]) / spanWeight );
                      info.columnWidths[col] += deltaWidth;
                      restWidth -= deltaWidth;
                    }
                  }
                }
                info.columnWidths[lastx - 1] += restWidth;
              }
            }


            if (displayArea.height > 1) {
              compHeight = compSize.height + constr.insets.top + constr.insets.bottom + constr.ipady;
              firsty = displayArea.y;
              lasty  = displayArea.y + displayArea.height;
              spanHeight = 0;
              for (row = firsty; row < lasty; row++){
                spanHeight += info.rowHeights[row];
              }
              extraHeight = compHeight - spanHeight;
              if (extraHeight > 0) {
                spanWeight = 0.0;
                for (row = firsty; row < lasty; row++){
                  spanWeight += info.rowWeights[row];
                }
                restHeight = extraHeight;
                if (spanWeight > 0.0){
                  for (row = firsty; row < lasty; row++){
                    if (info.rowWeights[row] > 0.0) {
                      deltaHeight = (int)( (((double)extraHeight) * info.rowWeights[row]) / spanWeight );
                      info.rowHeights[row] += deltaHeight;
                      restHeight -= deltaHeight;
                    }
                  }
                }
                info.rowHeights[lasty - 1] += restHeight;
              }
            }

          }

        }      // for (int i = 0; i < components.length; i++)

        // Overwrite the calculated values if overwrites for the widths and heights are given

        if (this.columnWidths != null) {
          // length of info.columnswidths is at least that of this.columnWidths
          lastx = this.columnWidths.length;
          for (col = 0; col < lastx; col++) {
            if (this.columnWidths[col] > info.columnWidths[col])
              info.columnWidths[col] = this.columnWidths[col];
          }
        }

        if (this.rowHeights != null) {
          // length of info.rowHeights is at least that of this.rowHeights
          lasty = this.rowHeights.length;
          for (row = 0; row < lasty; row++) {
            if (this.rowHeights[row] > info.rowHeights[row])
              info.rowHeights[row] = this.rowHeights[row];
          }
        }

        /*  // dump calculated column widths and row heights
            for (col = 0; col < info.ncols; col++)
            System.out.println("info.columnWidths["+col+"]="+info.columnWidths[col]);
            for (row = 0; row < info.nrows; row++)
            System.out.println("info.rowHeights["+row+"]="+info.rowHeights[row]);
         */

        return info;

      } finally {
        toolkit.unlockAWT();
      }
  }

  /**
   * @remark Retrieves the minimum dimension of a gridbag taking into account the widths
   * and heights of the columns and rows in the GridBagLayoputInfo, and the insets of the
   * container.
   */
  protected  Dimension getMinSize(Container container, GridBagLayoutInfo info) {
    // Retrieves the minimum dimension of a gridbag.
    if (container == null || info == null) {
      return new Dimension(0, 0);
    }
    else {
      Insets insets = container.getInsets();
      int w = 0;
      int h = 0;
      for (int i = 0; i < info.ncols; i++) {
        w += info.columnWidths[i];
      }
      for (int j = 0; j < info.nrows; j++) {
        h += info.rowHeights[j];
      }

      return new Dimension(insets.left + w + insets.right, insets.top + h + insets.bottom);
    }
  }

  /**
   * @status Implemented
   * @remark If the 'component' parameter is null, a NullPointerException will be thrown
   * by a lower level method. It is not caught by this method.
   */
  protected  GridBagConstraints lookupConstraints(Component component) {
    // Retrieves the constraints (not a copy) for the specified component.
    GridBagConstraints c = (GridBagConstraints)(comptable.get(component));
    if (c == null) {
      setConstraints(component, defaultConstraints);
      c = (GridBagConstraints)comptable.get(component);
    }
    return c;
  }

}

