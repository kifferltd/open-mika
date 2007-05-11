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

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class CompositeFilters extends VisualTestImpl {

  Image original;
  Rectangle crop;
  Point lastClick;

  Scrollbar slideR;
  Scrollbar slideG;
  Scrollbar slideB;
  Scrollbar scaleX;
  Scrollbar scaleY;
  
  Checkbox flipV;
  Checkbox flipH;
  DstImagePanel dest = new DstImagePanel(); 

  public class SrcImagePanel extends Panel implements MouseListener, MouseMotionListener {
    public SrcImagePanel() {
      super();
      this.setBackground(Color.black);
      this.addMouseListener(this);
      this.addMouseMotionListener(this);
      crop = new Rectangle(10, 10, 30, 30);
      lastClick = new Point();
    }
    
    public Dimension getMinimumSize() {
      return new Dimension(original.getWidth(null) + 4, original.getHeight(null) + 4);
    }

    public Dimension getPreferredSize() {
      return new Dimension(original.getWidth(null) + 4, original.getHeight(null) + 4);
    }

    public void update(Graphics g) {
      g.drawImage(original, 2, 2, null);
      g.setColor(Color.white);
      g.drawRect(2 + crop.x, 2 + crop.y, crop.width, crop.height);
    }

    public void paint(Graphics g) {
      update(g);
    }

    public void mouseClicked(MouseEvent event) { }
    public void mouseEntered(MouseEvent event) { }
    public void mouseExited(MouseEvent event) { }
    public void mouseMoved(MouseEvent event) {  }

    public void mousePressed(MouseEvent event) {
      lastClick.setLocation(event.getX() - 2, event.getY() - 2);
      crop.setBounds(lastClick.x, lastClick.y, 0, 0);
      this.repaint();
    }

    public void mouseReleased(MouseEvent event) {
      this.repaint();
      dest.repaint();
    }

    public void mouseDragged(MouseEvent event) {
      int x1 = lastClick.x;
      int y1 = lastClick.y;
      int x2 = event.getX() - 2;
      int y2 = event.getY() - 2;
      crop.setBounds(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
      this.repaint();
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

  public class ShaderFilter extends java.awt.image.RGBImageFilter {
    private int        r, g, b;
    private ColorModel model = ColorModel.getRGBdefault();

    public ShaderFilter(int r, int g, int b) {
      this.r = r;
      this.g = g;
      this.b = b;
    }
  
    public int filterRGB(int x, int y, int rgb) {
      int r2 = (model.getRed(rgb) * r) >>> 8;
      int g2 = (model.getGreen(rgb) * g) >>> 8;
      int b2 = (model.getBlue(rgb) * b) >>> 8;
      return (255 << 24) | (r2 << 16) | (g2 << 8) | b2;
    }

  }

  public class DstImagePanel extends Panel {
    public DstImagePanel() {
      super();
      this.setBackground(new Color(0x00202020));
    }
    
    public void update(Graphics g) {
      g.setColor(new Color(0x00202020));
      g.fillRect(0, 0, 500, 500);
      ImageProducer ip;
      ip = original.getSource();
      ip = new FilteredImageSource(ip, new java.awt.image.CropImageFilter(crop.x, crop.y, crop.width, crop.height));
      Image cropped = this.createImage(ip);
      ip = new FilteredImageSource(ip, new java.awt.image.ReplicateScaleFilter(cropped.getWidth(null) * scaleX.getValue() / 10, 
                                                                               cropped.getHeight(null) * scaleY.getValue() / 10));
      ip = new FilteredImageSource(ip, new FlipFilter(flipH.getState(), flipV.getState()));
      ip = new FilteredImageSource(ip, new ShaderFilter(slideR.getValue(), slideG.getValue(), slideB.getValue()));
      g.drawImage(this.createImage(ip), 2, 2, null);
    }
    
    public void paint(Graphics g) {
      update(g);
    }
  }
  
  public CompositeFilters() {
    super();
    original = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/lena1.png"));
    setBackground(Color.black);
    setLayout(new BorderLayout());

    Panel left = new Panel(new BorderLayout());
    left.add(new SrcImagePanel(), BorderLayout.NORTH);
    
    Panel boxes = new Panel(new GridLayout(2,1));
    boxes.setBackground(Color.black);
    boxes.setForeground(Color.white);
    
    flipH = new Checkbox("Flip Horizontal");
    flipV = new Checkbox("Flip Vertical");
    
    boxes.add(flipH);
    boxes.add(flipV);

    left.add(boxes, BorderLayout.CENTER);

    Panel sliders = new Panel(new GridLayout(3, 1));
    sliders.setBackground(Color.black);
    sliders.setForeground(Color.white);

    slideR = new Scrollbar(Scrollbar.HORIZONTAL, 255, 5, 0, 255);
    slideG = new Scrollbar(Scrollbar.HORIZONTAL, 255, 5, 0, 255);
    slideB = new Scrollbar(Scrollbar.HORIZONTAL, 255, 5, 0, 255);

    sliders.add(slideR);
    sliders.add(slideG);
    sliders.add(slideB);
    
    Panel scalers = new Panel(new GridLayout(1, 2));
    scalers.setBackground(Color.black);
    scalers.setForeground(Color.white);

    scaleX = new Scrollbar(Scrollbar.VERTICAL, 10, 5, 1, 40);
    scaleY = new Scrollbar(Scrollbar.VERTICAL, 10, 5, 1, 40);

    scalers.add(scaleX);
    scalers.add(scaleY);
    
    add(left, BorderLayout.WEST);
    add(dest, BorderLayout.CENTER);
    add(sliders, BorderLayout.SOUTH);
    add(scalers, BorderLayout.EAST);
    
    repaint();
  }
    
  public String getHelpText(){
    return "CompositeFilters";
  }

}

