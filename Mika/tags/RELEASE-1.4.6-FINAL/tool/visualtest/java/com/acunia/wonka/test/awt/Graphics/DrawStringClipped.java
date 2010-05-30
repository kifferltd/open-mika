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


// Author: J. Vandeneede
// Created: 2001/07/09

package com.acunia.wonka.test.awt.Graphics;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;
import com.acunia.wonka.test.awt.VisualTester;


public class DrawStringClipped extends VisualTestImpl implements ActionListener  {

  MyButton but;
  MyPanel pan;
  MyImagePanel im;

  class MyButton extends Button {

    String title;
    Font f1;
    Font f2;
    Color c1;
    int f2Ascent;

    public MyButton(String myLabel, String myTitle){
      super(myLabel);
      title = myTitle;
      this.setBackground(Color.white);
      f1 = new Font("helvB14",1,18);
      f2 = new Font("helvB24",1,30);
      this.setFont(f1);
      c1 = new Color(200,40,70);
    }

    public void paint (Graphics g) {
      Dimension d = this.getSize();
      if (f2Ascent == 0) {
        f2Ascent = g.getFontMetrics(f2).getAscent();
        }
      g.setColor(c1);
      g.setFont(f2);
      g.drawString(title, (int)(Math.floor(Math.random()*d.width*1.33) - d.width/3), (int)(Math.floor(Math.random()*d.height*1.2) - d.height/5 + f2Ascent));
    }
  }

  class MyPanel extends Panel {

    String label;
    String title;
    int labelWidth = 0;
    Font f1;
    Font f2;
    int f2Ascent= 0;

    public MyPanel(String myLabel, String myTitle){
      super();
      label = myLabel;
      title = myTitle;
      this.setBackground(new Color(255,160,0));
      f1 = new Font("helvB14",1,18);
      f2 = new Font("helvB24",1,30);
      this.setFont(f1);
    }

    public void paint (Graphics g) {
      Dimension d = this.getSize();
      if (labelWidth == 0 || f2Ascent == 0) {
        labelWidth = g.getFontMetrics(f1).stringWidth(label);
        f2Ascent   = g.getFontMetrics(f2).getAscent();
      }
      g.drawString(label, ((d.width - labelWidth)/2), (d.height/2));
      g.setFont(f2);
      g.setColor(Color.green);
      g.drawString(title, (int)(Math.floor(Math.random()*d.width*1.33) - d.width/3), (int)(Math.floor(Math.random()*d.height*1.2) - d.height/5 + f2Ascent));
    }
  }

  class MyImagePanel extends Panel {

    String label;
    String title;
    int    labelWidth = 0;
    Font f1;
    Font f2;
    int  f2Ascent = 0;
    Color c1 = Color.black;
    Color c2 = Color.yellow;
    Color backC;
    Image backBuffer = null;
    Graphics backG = null;

    public MyImagePanel(String myLabel, String myTitle){
      super();
      label = myLabel;
      title = myTitle;
      backC = new Color(0,160,255);
      f1 = new Font("helvB14",1,18);
      f2 = new Font("helvB24",1,30);
    }

    public void update (Graphics g) {
      paint(g);
    }

    public void paint (Graphics g) {
      int w = this.getSize().width;
      int h = this.getSize().height;
      if (labelWidth == 0 || f2Ascent == 0) {
        labelWidth = g.getFontMetrics(f1).stringWidth(label);
        f2Ascent   = g.getFontMetrics(f2).getAscent();
        }

      try {
        if (backBuffer == null
               || backBuffer.getWidth(null) != w
               || backBuffer.getHeight(null) != h) {
          backBuffer = this.createImage(w, h);
          if (backBuffer != null) {
            backG = backBuffer.getGraphics();
          }
        }
        if (backBuffer != null) {
          backG.setColor(backC);
          backG.fillRect(0, 0, w, h);
          backG.setColor(c1);
          backG.setFont(f1);
          // print the label centered in the image
          backG.drawString(label, ((w - labelWidth)/2),(h/2));
          backG.setColor(c2);
          backG.setFont(f2);
          // print the title at a random place
          backG.drawString(title, (int)(Math.floor(Math.random()*w*1.33) - w/3), (int)(Math.floor(Math.random()*h*1.2) - h/5 + f2Ascent));
        }
        g.drawImage(backBuffer, 0, 0, null);
      }
      catch (Throwable t) {
        System.out.println("caught unwanted Exception "+t.toString());
        t.printStackTrace();
      };
    }
  }

  public DrawStringClipped() {

    super();
    setLayout(new GridLayout(3,1));
    pan = new MyPanel("Panel", "Tropical");
    but = new MyButton("Component", "Acunia");
    im = new MyImagePanel("Image", "Cuba Libre");
    but.addActionListener(this);
    add(pan);
    add(but);
    add(im);
  }

  public void actionPerformed(ActionEvent evt) {
    pan.repaint();
    im.repaint();
    but.repaint();
  }


  public String getHelpText(){
    return ("A successful test looks like this: the screen is divided vertically in three areas " +
            "of equal size. The upper area is painted orange, has black label \"Canvas\" and represents " +
            "a panel or container which usually is the basis for a GUI. The second area is painted " +
            "white, has black label \"Component\" and represents a customized button. The third area " +
            "is painted blue, has black label \"Image\" and represents an off-screen buffer. Whenever " +
            "one clicks on the central area, the button, colored strings are drawn on the three areas " +
            "at random coordinates, which may be outside the three visible areas. As a consequence " +
            "the strings may disappear partly or completely. The strings are clipped on the borders " +
            "of each area. No parts of the strings should be visible outside the borders of each area.");
  }
     	
  public java.awt.Panel getPanel(VisualTester vt){
    return this;
  }
     	
  public void start(java.awt.Panel p, boolean b){
  }

  public void stop(java.awt.Panel p){
  }

}
