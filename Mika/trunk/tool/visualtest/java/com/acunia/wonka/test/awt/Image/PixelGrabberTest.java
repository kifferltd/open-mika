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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import com.acunia.wonka.test.awt.*;

public class PixelGrabberTest extends VisualTestImpl implements MouseListener {

  Image original;
  Image newImage;
  int   currentColor;
  int   w;
  int   h;
  int[] pixels;
  int   level = 10;
  PixelGrabber pg;
  
  public PixelGrabberTest() {
    super();

    String path = System.getProperty("vte.image.path", "{}/test/image");
    original = Toolkit.getDefaultToolkit().getImage(path + "/lena1.png");
    
    w = original.getWidth(null);
    h = original.getHeight(null);
    
    pg = new PixelGrabber(original, 0, 0, w, h, true);

    try {
      pg.grabPixels();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    w = pg.getWidth();
    h = pg.getHeight();
    pixels = (int[])pg.getPixels();

    newImage = createImage(new MemoryImageSource(w, h, pixels, 0, w));

    addMouseListener(this);
    
    this.setBackground(Color.black);
    this.repaint();
  }
  
  public void paint(Graphics g) {
    g.drawImage(newImage, 0, 0, getWidth(), getHeight(), null);
    g.setColor(new Color(currentColor));
    g.fillRect(10, 10, 20, 20);

    g.setColor(Color.black);
    g.fillRect(10, 40, 20, 60);
    
    g.setColor(Color.white);
    g.drawRect(10, 10, 20, 20);
    
    g.drawRect(10, 40, 20, 20);
    g.drawRect(10, 60, 20, 20);
    g.drawRect(10, 80, 20, 20);

    FontMetrics fm = g.getFontMetrics();
    int oy = (20 - fm.getHeight()) / 2 + fm.getHeight();

    g.drawString("+", 10 + (20 - fm.stringWidth("+")) / 2, 40 + oy);
    g.drawString("" + level, 10 + (20 - fm.stringWidth("" + level)) / 2, 60 + oy);
    g.drawString("-", 10 + (20 - fm.stringWidth("-")) / 2, 80 + oy);
  }
  
  public String getHelpText(){
    return "PixelGrabber";
  }

  public void mouseClicked(MouseEvent event) {
    int x = event.getX(); 
    int y = event.getY(); 
    boolean button = false;
    
    if(x > 10 && x < 30) {
      if(y > 40 && y < 60) {
        if(level < 255) level++;
        button = true;
      }
      else if(y > 60 && y < 80) {
        return;
      }
      else if(y > 80 && y < 100) {
        if(level > 1) level--;
        button = true;
      }
    }
    
    x = (x * w) / getWidth();
    y = (y * h) / getHeight();
    
    if((x >= 0 && x < w && y >= 0 && y < h) || button) {
      if(!button) currentColor = pixels[(y * w) + x];
      int[] newPixels = (int[])pixels.clone();
      ColorModel model = pg.getColorModel();
      int r1 = model.getRed(currentColor);
      int g1 = model.getGreen(currentColor);
      int b1 = model.getBlue(currentColor);
      int f = level;
      for(int i=0; i < newPixels.length; i++) {
        int color = newPixels[i];
        int r2 = model.getRed(color);
        int g2 = model.getGreen(color);
        int b2 = model.getBlue(color);
        if(r1 - f < r2 && r1 + f > r2 &&
           g1 - f < g2 && g1 + f > g2 &&
           b1 - f < b2 && b1 + f > b2) newPixels[i] = 0;
      } 
      newImage = createImage(new MemoryImageSource(w, h, newPixels, 0, w));
      repaint();
    }
  }
  
  public void mouseEntered(MouseEvent event) {
  }
  
  public void mouseExited(MouseEvent event) {
  }
  
  public void mousePressed(MouseEvent event) {
  }
  
  public void mouseReleased(MouseEvent event) {
  }
}

