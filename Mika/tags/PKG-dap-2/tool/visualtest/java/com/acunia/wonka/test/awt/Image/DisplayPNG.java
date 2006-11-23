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


//Author: J. Bensch
//Created: 2001/08/17

package com.acunia.wonka.test.awt.Image;

import java.util.Random;
import com.acunia.wonka.test.awt.*;
import java.awt.*;
import java.io.*;

public class DisplayPNG extends VisualTestImpl {
  int cellSize = 50;
  
  class Thumbnail extends Canvas {
    Image image;
    String name;

    Thumbnail(Image image, String name) {
      this.image = image;
      this.name = name;
      //prepareImage(image, this);
    }

    public Dimension getPreferredSize() {
      return new Dimension(50, 50);
    }
    public void paint(Graphics g) {
      update(g);
    }

    public void update(Graphics g) {
  //    Font font = new Font("Courier", Font.PLAIN, 10);
  //    FontMetrics fm = g.getFontMetrics();
      int w = this.getSize().width;
      int h = this.getSize().height;
      int iw = image.getWidth(this);
      int ih = image.getHeight(this);
   
      if (iw > 0 && ih > 0) {
        if (iw > h) {
          ih = ih * w / iw;
          iw = w;
        }
        if (ih > h && ih > 0) {
          iw = iw * h / ih;
          ih = h;
        }
        g.setColor(Color.lightGray);
        g.fillRect(0, 0, w, h);
        g.drawImage(image, (w - iw) / 2, (h - ih) / 2, iw, ih, this);
      }
  //    h = getSize().height;
  //    g.setColor(Color.black);
  //    g.clearRect(0, h, w, fm.getHeight());
  //    g.drawString(name, (w - fm.stringWidth(name)) / 2, h - fm.getHeight() + fm.getAscent());
  //    g.drawRect(0, 0, w - 1, h - 1);
    }
  }

  public DisplayPNG() {
    Dimension dim = new Dimension(400, 234);
    Random rand_nr = new Random();
    int number = 0;
    // You need hardcode the path to the images-directory. 
    String pathname = "/png/";
    File dir = new File(pathname);
    String[] png_names = dir.list();

    for (int i = 0; i < 7 * 4; i++) {
      number = Math.abs(rand_nr.nextInt())% png_names.length;
      if (png_names[number].endsWith(".png") || png_names[number].endsWith(".jpg") || png_names[number].endsWith(".gif")) {
	Component thumby = new Thumbnail(Toolkit.getDefaultToolkit().getImage(dir.getAbsolutePath() + File.separator + png_names[number]), png_names[number]);
	thumby.setSize(cellSize, cellSize);
	add(thumby);
      }
    }
    setLayout(new FlowLayout());
    setSize(dim);
    setVisible(true);
  }

  public String getHelpText() {
    return ("This test randomly return images (.jpg, .png or .gif) from a directory and displays them in a FlowLayout on the screen.");
  }
}

