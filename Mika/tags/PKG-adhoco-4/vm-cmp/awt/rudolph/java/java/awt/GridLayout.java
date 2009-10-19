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
