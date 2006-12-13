/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package com.acunia.wonka.test.awt.Image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class ImageFilter extends VisualTestImpl {

  public class TransFilter extends java.awt.image.ImageFilter {

    public TransFilter() {
      super();
    }

    public void setDimensions(int w, int h) {
      super.setDimensions(h, w);
    }
  
    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
      int[] newpixels = new int[pixels.length];
      for(int i=0; i < h; i++) {
        for(int j=0; j < w; j++) {
          newpixels[j * h + i] = pixels[i * scansize + j + off];
        }
      }
      super.setPixels(y, x, h, w, model, newpixels, 0, h);
    }
  
    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
      int[] newpixels = new int[pixels.length];
      for(int i=0; i < h; i++) {
        for(int j=0; j < w; j++) {
          newpixels[j * h + i] = pixels[i * scansize + j + off];
        }
      }
      super.setPixels(y, x, h, w, model, newpixels, 0, h);
    }
  
  }

  public class FlipFilter extends java.awt.image.ImageFilter {
    
    private boolean hor;
    private boolean ver;
    private int     width;
    private int     height;
 
    public FlipFilter(boolean hor, boolean ver) {
      super();
      this.hor = hor;
      this.ver = ver;
    }

    public void setDimensions(int w, int h) {
      super.setDimensions(w, h);
      this.width = w;
      this.height = h;
    }
    
    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
      int[] newpixels = new int[pixels.length];
      int nx = (hor ? (width - x - w) : x);
      int ny = (ver ? (height - y - h) : y);
      
      for(int i=0; i < h; i++) {
        for(int j=0; j < w; j++) {
          newpixels[(ver ? (h-1) - i : i) * w + (hor ? (w-1) - j : j)] = pixels[i * scansize + j + off];
        }
      }
      super.setPixels(nx, ny, w, h, model, newpixels, 0, w);
    }
  
    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
      int[] newpixels = new int[pixels.length];
      int nx = (hor ? width - x - w : x);
      int ny = (ver ? height - y - h : y);
      
      for(int i=0; i < h; i++) {
        for(int j=0; j < w; j++) {
          newpixels[(ver ? (h-1) - i : i) * w + (hor ? (w-1) - j : j)] = pixels[i * scansize + j + off];
        }
      }
      super.setPixels(nx, ny, w, h, model, newpixels, 0, w);
    }
  
  }

  private Image original;
  private Image trans;
  private Image flip1;
  private Image flip2;
  private Image flip3;
    
  public ImageFilter() {
    super();
    String path = System.getProperty("vte.image.path", "{}/test/image");
    original = Toolkit.getDefaultToolkit().getImage(path + "/lena1.png");
    trans = this.createImage(new FilteredImageSource(original.getSource(), new TransFilter()));
    flip1 = this.createImage(new FilteredImageSource(original.getSource(), new FlipFilter(true, false)));
    flip2 = this.createImage(new FilteredImageSource(original.getSource(), new FlipFilter(false, true)));
    flip3 = this.createImage(new FilteredImageSource(original.getSource(), new FlipFilter(true, true)));
    this.setBackground(Color.black);
    this.repaint();
  }
    
  public void paint(Graphics g) {
    g.setColor(Color.white);
     
    g.drawImage(original, 10, 10, null);
    g.drawString("   Original   ", 45, 85);
      
    g.drawImage(flip1, 150, 10, null);
    g.drawString("  Horizontal  ", 185, 85);
      
    g.drawImage(flip2, 10, 100, null);
    g.drawString("   Vertical   ", 45, 175);
      
    g.drawImage(flip3, 150, 100, null);
    g.drawString("     Both     ", 185, 175);
      
    g.drawImage(trans, 290, 10, null);
    g.drawString("  Transposed  ", 295, 140);
  }
    
  public String getHelpText(){
    return "";
  }

}

