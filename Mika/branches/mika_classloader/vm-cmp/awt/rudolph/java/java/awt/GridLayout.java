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

public class GridLayout implements LayoutManager, java.io.Serializable {

  private static final long serialVersionUID = -7411804673224730901L;

  int hgap;
  int vgap;
  int rows;
  int cols;

  public GridLayout() {
    this(1, 0, 0, 0);
  }

  public GridLayout(int rows, int cols) {
    this(rows, cols, 0, 0);
  }

  public GridLayout(int rows, int cols, int hgap, int vgap) {

    if ((rows < 0) || (cols < 0)){
      throw new IllegalArgumentException("invalid parameters");
    }

    if(hgap < 0){
       hgap = 0;
    }
    if (vgap < 0){
      vgap = 0;
    }

    if ((rows == 0) && (cols == 0)) {
      throw new IllegalArgumentException("rows and cols cannot both be zero");
    }

    this.rows = rows;
    this.cols = cols;
    this.hgap = hgap;
    this.vgap = vgap;
  }

  public int getRows() {
    return rows;
  }

  public void setRows(int rows) {
    if ((rows == 0) && (this.cols == 0)) {
      throw new IllegalArgumentException("rows and cols cannot both be zero");
    }
    this.rows = rows;
  }

  public int getColumns() {
    return cols;
  }

  public void setColumns(int cols) {
    if ((cols == 0) && (this.rows == 0)) {
      throw new IllegalArgumentException("rows and cols cannot both be zero");
    }
    this.cols = cols;
  }

  public int getHgap() {
    return hgap;
  }

  public void setHgap(int hgap) {
    this.hgap = hgap;
  }

  public int getVgap() {
    return vgap;
  }
    
  public void setVgap(int vgap) {
    this.vgap = vgap;
  }

  /**
   * @remark not used in GridLayout
   */
  public void addLayoutComponent(String name, Component comp) {
  }

  /**
   * @remark not used in GridLayout
   */
  public void addLayoutComponent(Component comp, Object contstraints) {
  }

  /**
   * @remark not used in GridLayout
   */
  public void removeLayoutComponent(Component comp) {
  }

  private Dimension layoutSize(Container parent, boolean min) {
    Dimension d;
    Insets insets = parent.getInsets();

    int ncomponents = parent.getComponentCount();
    int nrows = rows;
    int ncols = cols;

    if (nrows > 0) {
      ncols = (ncomponents % nrows == 0) ? ncomponents / nrows : ncomponents / nrows + 1;
    }
    else {
      nrows = (ncomponents % ncols == 0) ? ncomponents / ncols : ncomponents / ncols + 1;
    }
    
    int w = 0;
    int h = 0;

    for (int i = 0 ; i < ncomponents ; i++) {
      Component comp = parent.getComponent(i);

      if (min) {
        d = comp.getMinimumSize();
      }
      else {
        d = comp.getPreferredSize();
      }

      if (w < d.width) {
         w = d.width;  
      }
      if (h < d.height) {
         h = d.height;  
      }
    }

    return new Dimension(insets.left + insets.right + ncols * w + (ncols - 1) * hgap, insets.top + insets.bottom + nrows * h + (nrows - 1) * vgap);
  }

  public Dimension minimumLayoutSize(Container parent) {
    return layoutSize(parent, true);
  }  

  public Dimension preferredLayoutSize(Container parent) {
    return layoutSize(parent, false);
  }

  public Dimension maximumLayoutSize(Container parent) {
     return layoutSize(parent, false);
     // FIXME: returns preferred layout size
  }

  public void layoutContainer(Container parent) {
    Insets insets = parent.getInsets();
    int ncomponents = parent.getComponentCount();
    int nrows = rows;
    int ncols = cols;

    if (ncomponents == 0) {
      return;
    }

    if (nrows > 0) {
      ncols = (ncomponents % nrows == 0) ? ncomponents / nrows : ncomponents / nrows + 1;
    }
    else {
      nrows = (ncomponents % ncols == 0) ? ncomponents / ncols : ncomponents / ncols + 1;
    }

    int w = parent.width - (insets.left + insets.right);
    int h = parent.height - (insets.top + insets.bottom);
    w = (w - (ncols - 1) * hgap) / ncols;
    h = (h - (nrows - 1) * vgap) / nrows;

    for (int c = 0, x = insets.left ; c < ncols ; c++, x += w + hgap) {
      for (int r = 0, y = insets.top ; r < nrows ; r++, y += h + vgap) {
        int i = r * ncols + c;
        if (i < ncomponents) {
          parent.getComponent(i).setBounds(x, y, w, h);
        }
      }
    }
  }
  
  public String toString() {
    return getClass().getName() + "[hgap = " + hgap + ", vgap = " + vgap + ", rows = " + rows + ", cols = " + cols + "]";
  }
}
