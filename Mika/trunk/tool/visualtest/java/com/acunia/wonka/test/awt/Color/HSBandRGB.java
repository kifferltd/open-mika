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

package com.acunia.wonka.test.awt.Color;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class HSBandRGB extends VisualTestImpl {

  Image original;

  Scrollbar slideH;
  Scrollbar slideS;
  Scrollbar slideB;
  
  DstImagePanel dest = new DstImagePanel(); 

  public class DstImagePanel extends Panel {
    public DstImagePanel() {
      super();
      this.setBackground(Color.black);
    }
    
    public void update(Graphics g) {
      g.setColor(Color.black);
      g.fillRect(0, 0, 500, 500);
      ImageProducer ip;
      ip = original.getSource();
      ip = new FilteredImageSource(ip, new HSBFilter(slideH.getValue(), slideS.getValue(), slideB.getValue()));
      g.drawImage(this.createImage(ip), 10, 10, null);
    }
    
    public void paint(Graphics g) {
      update(g);
    }
  }
  
  public class HSBFilter extends java.awt.image.RGBImageFilter {

    private ColorModel model = ColorModel.getRGBdefault();
    private int h, s, b;

    public HSBFilter(int h, int s, int b) {
      this.h = h;   this.s = s;   this.b = b;
    }
  
    public int filterRGB(int x, int y, int rgb) {
      float HSB[] = new float[3];
      Color.RGBtoHSB(model.getRed(rgb), model.getGreen(rgb), model.getBlue(rgb), HSB);
      HSB[0] += (float)(h - 200)/(float)200;
      if(HSB[0] < 0) HSB[0] += 1;
      if(HSB[0] > 1) HSB[0] -= 1;
      HSB[1] *= (float)s/(float)200;
      HSB[2] *= (float)b/(float)200;
      return Color.HSBtoRGB(HSB[0], HSB[1], HSB[2]); 
    }
  }
  
  public HSBandRGB() {
    super();
    original = Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("/lena1.png"));
    setLayout(new BorderLayout());

    Panel bottom = new Panel(new BorderLayout());
    
    Panel sliders = new Panel(new GridLayout(3, 1));
    //sliders.setBackground(Color.black);
    //sliders.setForeground(Color.white);

    slideH = new Scrollbar(Scrollbar.HORIZONTAL, 200, 5, 100, 300);
    slideS = new Scrollbar(Scrollbar.HORIZONTAL, 200, 5, 0, 200);
    slideB = new Scrollbar(Scrollbar.HORIZONTAL, 200, 5, 0, 200);

    sliders.add(slideH);
    sliders.add(slideS);
    sliders.add(slideB);

    Button button = new Button("Repaint");
    button.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent evt) { dest.repaint(); } });

    bottom.add(sliders, BorderLayout.CENTER);
    bottom.add(button, BorderLayout.EAST);
    add(dest, BorderLayout.CENTER);
    add(bottom, BorderLayout.SOUTH);
    
    repaint();
  }
    
  public String getHelpText(){
    return "HSB & RGB";
  }

}

