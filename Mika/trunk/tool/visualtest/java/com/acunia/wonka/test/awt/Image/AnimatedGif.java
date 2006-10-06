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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class AnimatedGif extends VisualTestImpl {

  private ImageComponent pic1;
  private ImageComponent pic2;
  private ImageComponent pic3;

  public class ImageComponent extends Component {
    Image   img;
    double  scale_x;
    double  scale_y;

    public ImageComponent(Image img) {
      this(img, 1.0, 1.0);
    }
    
    public ImageComponent(Image img, double sx, double sy) {
      scale_x = sx;
      scale_y = sy;
      this.img = img;
      this.repaint();
    }

    public Dimension getPreferredSize() {
      return new Dimension((int)(img.getWidth(null) * scale_x), (int)(img.getHeight(null) * scale_y));
    }
    
    public void update(Graphics g) {
      paint(g);
    }

    public void paint(Graphics g) {
      if(img != null) {
        g.drawImage(img, 0, 0, (int)(img.getWidth(null) * scale_x), (int)(img.getHeight(null) * scale_y), this);
      }
    }

  }
  
  public AnimatedGif() {
    super();
    String path = System.getProperty("vte.image.path", "{}/test/image");
    Image img = Toolkit.getDefaultToolkit().getImage(path + "/rudolph3.gif");
    if(img == null) {
      return;
    }

    MediaTracker m = new MediaTracker(this);
    m.addImage(img, 0);
    try {
      m.waitForAll();
    }
    catch(InterruptedException e) {
    }
    Toolkit.getDefaultToolkit().prepareImage(img, -1, -1, null);

    pic1 = new ImageComponent(img);
    pic2 = new ImageComponent(img, 0.75, 0.75);
    pic3 = new ImageComponent(img, 0.50, 0.50);
    Label copyright = new Label("image (c) Kitty Roach");
    copyright.setForeground(new Color(180, 180, 180));
   
    setLayout(null);

    add(pic1);
    add(pic2);
    add(pic3);
    add(copyright);

    this.setBackground(Color.white);

    pic1.setBounds(10, 10, pic1.getPreferredSize().width, pic1.getPreferredSize().height);
    pic2.setBounds(140, 10, pic2.getPreferredSize().width, pic2.getPreferredSize().height);
    pic3.setBounds(240, 10, pic3.getPreferredSize().width, pic3.getPreferredSize().height);
    copyright.setBounds(10, 90, copyright.getPreferredSize().width, copyright.getPreferredSize().height);
   
    validate();
  }
    
  public String getHelpText(){
    return "";
  }

}

