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

package com.acunia.wonka.test.awt.misc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Panel;

import com.acunia.wonka.test.awt.VisualTestImpl;
import com.acunia.wonka.test.awt.VisualTester;

public class MemoryCorruption extends VisualTestImpl {

  public MemoryCorruption() {

    setSize(0, 0);

    setLayout(null);

    setSize(300, 300);
       
    /*
    ** Add some labels at random places:
    */

    Label l1 = new Label("my evil label");
    l1.setLocation(-1, 0);
    add(l1);

    Label l2 = new Label("my evil label");
    l2.setLocation(0, -1);
    add(l2);

    Label l3 = new Label("my evil label");
    l3.setLocation(400, 400);
    add(l3);

    Label l4 = new Label("my evil label");
    l4.setBounds(-10, -10, 500, 500);
    add(l4);

    Label l5 = new Label("my evil label");
    l5.setBounds(50, 50, 30, 30);
    add(l5);

    Label l6 = new Label("my evil label");
    l6.setBounds(100, 100, 300, -200);
    add(l6);

    Label l7 = new Label("my evil label");
    l7.setBounds(200, 200, 10000, 10000);
    add(l7);

    Label l8 = new Label("my evil label");
    l8.setBounds(200, 200, -10000, -10000);
    add(l8);

    Label l9 = new Label("my evil label");
    l9.setBounds(-10000, 10000, 10000, 10000);
    add(l9);

    /*
    ** Add a few Panels to test the Container_scale():
    */

    Panel p1 = new Panel();
    p1.setBackground(Color.green);
    p1.add(l1);
    p1.add(l2);
    p1.add(l3);
    p1.add(l3);
    add(p1);

    Panel p2 = new Panel();
    p2.setBackground(Color.red);
    p2.add(l1);
    p2.add(l2);
    p2.add(l3);
    p2.add(l3);
    add(p2);
 
    Panel p3 = new Panel();
    p3.setBackground(Color.blue);
    p3.add(l1);
    p3.add(l2);
    p3.add(l3);
    p3.add(l3);
    add(p3);

    /*
    ** Scale some Panels at will:
    */

    p1.setBounds(0, 0, 0, 0);
    p1.setBounds( 0,  0, 40, 40);
    p1.setBounds(-5, -5, 35, 35);
    p1.setBounds(0, 0, 1, -1);
    p1.setBounds(0, 0, -1, 1);

    p2.setBounds(100, 100, 0, 100);
    p2.setBounds(100, 100, 100, 0);
    p2.setBounds(100, 100, 500, 100);
    p2.setBounds(100, 100, 100, 500);
    p2.setBounds(100, 100, -10, -10);
    
    p3.setBounds(300, 300, getWidth() - 300, getHeight() - 300);
    p3.setBounds(300, 300, getWidth() - 301, getHeight() - 301);
    p3.setBounds(300, 300, getWidth() - 299, getHeight() - 299);
    p3.setBounds(300, 300, getWidth() - 300, getHeight() - 299);
    p3.setBounds(300, 300, getWidth() - 299, getHeight() - 300);

  }

  public void paint(Graphics g) {

    g.setColor(Color.red);

    /*
    ** drawLine:
    */

    g.drawLine(-5, -5, 10, 10);
    g.drawLine(10, 10, -5, -5);
    g.drawLine(10, 10, 300, 500);
    g.drawLine(10, 10, 500, 300);

    /*
    ** fillRect:
    */

    g.fillRect(-5, -5, 10, 10);
    g.fillRect(10, 10, -5, -5);
    g.fillRect(350, 350, 40, 40);

    /*
    ** drawString:
    */

    g.drawString("olleke bolleke", -5, -5);
    g.drawString("olleke bolleke", -5, 100);
    g.drawString("olleke bolleke", 100, -5);
    g.drawString("olleke bolleke", -100, -100);
    g.drawString("olleke bolleke", -200, -200);
    g.drawString("olleke bolleke", -300, -300);
    g.drawString("olleke bolleke", -400, -400);
    g.drawString("olleke bolleke", -500, -500);
    g.drawString("olleke bolleke", -600, -600);
    g.drawString("olleke bolleke", -700, -700);
    g.drawString("olleke bolleke", -800, -800);
    g.drawString("olleke bolleke", -900, -900);
    g.drawString("olleke bolleke", 100, 100);
    g.drawString("olleke bolleke", 200, 200);
    g.drawString("olleke bolleke", 300, 300);
    g.drawString("olleke bolleke", 400, 400);
    g.drawString("olleke bolleke", 500, 500);
    g.drawString("olleke bolleke", 600, 600);
    g.drawString("olleke bolleke", 700, 700);
    g.drawString("olleke bolleke", 800, 800);
    g.drawString("olleke bolleke", 900, 900);

  }

  static public void main(String[] args) {

    MemoryCorruption tf = new MemoryCorruption();
    tf.show();

  }

  public String getTitle(){

    return "MemoryCorruption";

  }

  public String getHelpText(){

    return "An evil test program; tries to make the native peers write beyond its buffers.  If the test fails, memory will be corrupted and the VisualTestEngine is likely to crash.  If we don't crash, everything is should be OK.  You can expect to see random components and colors on the screen.  Nevermind these.";

  }

  public java.awt.Panel getPanel(VisualTester vt) {

    Panel p = new Panel();
    p.add(this);
    return p;

  }

  public String getLogInfo(java.awt.Panel p, boolean b) {
    return "no log information";
  }
    	
  public void start(java.awt.Panel p, boolean b) {
  }

  public void stop(java.awt.Panel p) { 
  }

  public void showTest(){
  }

  public void hideTest(){
  }

}
