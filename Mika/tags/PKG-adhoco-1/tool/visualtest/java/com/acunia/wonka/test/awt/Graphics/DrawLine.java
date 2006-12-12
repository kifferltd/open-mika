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
// Created: 2001/03/13

package com.acunia.wonka.test.awt.Graphics;
import java.awt.*;
import com.acunia.wonka.test.awt.*;


public class DrawLine extends VisualTestImpl  {

  class MyButton extends Button {
    public MyButton(String title){
      super(title);
    }

    public void paint (Graphics g) {
      g.setColor(Color.red);
      g.drawLine(0,0,this.getWidth()-1,this.getHeight()-1);

      g.setColor(Color.yellow);
      g.drawLine(this.getWidth()-1,0, 0, this.getHeight()-1);

      g.setColor(Color.orange);
      g.drawLine((this.getWidth())/2,0 ,(this.getWidth())/2,this.getHeight()-1);
    }
  }

  class MyPanel extends Panel {
    public MyPanel(Color b, Color f) {
      super();
      this.setBackground(b);
      this.setForeground(f);
    }

    public void paint (Graphics g) {

      g.setColor(Color.red);
      g.drawLine(0,0,this.getWidth()-1,this.getHeight()-1);

      g.setColor(Color.yellow);
      g.drawLine(this.getWidth()-1,0, 0, this.getHeight()-1);

      g.setColor(Color.orange);
      g.drawLine((this.getWidth())/2,0 ,(this.getWidth())/2,this.getHeight()-1);
    }
  }

  MyButton but;
  MyPanel  pan;

  public DrawLine() {

    super();

    setLayout(new GridLayout(2,1));

    pan = new MyPanel(Color.blue, Color.yellow);
    add(pan);

    but = new MyButton("BUTTON");
    add(but);

  }


  public String getTitle(){
    return "DrawLine";
  }
  public String getHelpText(){
    return ("A successful test looks like this: the upper half of the screen shows a " +
            "blue panel and the lower half is a grey button; in both surfaces three " +
            "identical lines are drawn: a red line extending from the top-left " +
            "corner of the component to its bottom-right corner; a yellow line extending " +
            "from the bottom-left corner to the top-right corner; an orange vertical " +
            "line dividing the component in two equal parts; when pushing the button, the " +
            "lines remain visible (with sun java they disappear).");
  }
     	
  public java.awt.Panel getPanel(VisualTester vt){
    return this;
  }
     	
  public String getLogInfo(java.awt.Panel p, boolean b){
    return "no logging info !";
  }
  public void start(java.awt.Panel p, boolean b){
//  card.repaint();
// but.repaint();
  }

  public void stop(java.awt.Panel p){}


  static public void main (String[] args) {
    new DrawLine();
  }
}
