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

package com.acunia.wonka.test.awt.Window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.acunia.wonka.test.awt.VisualTestImpl;
import com.acunia.wonka.test.awt.VisualTester;

public class WindowTest1 extends VisualTestImpl {

  StatusLabel status;

  char chars[] = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

  VisualTester getVt() {
    return vt;
  }
  
  class WindowHandler implements WindowListener {
    public void windowOpened(WindowEvent event) {
      status.setText("status: window opened");
    }

    public void windowClosing(WindowEvent event) {
      status.setText("status: window closing");
    }

    public void windowClosed(WindowEvent event) {
      status.setText("status: window closed");
    }

    public void windowIconified(WindowEvent event) {
      status.setText("status: window iconified");
    }

    public void windowDeiconified(WindowEvent event) {
      status.setText("status: window deiconified");
    }

    public void windowActivated(WindowEvent event) {
      status.setText("status: window activated");
    }
   
    public void windowDeactivated(WindowEvent event) {
      status.setText("status: window deactivated");
    }
  }

  class MouseHandler extends MouseAdapter {
    PopupWindow pw;
    Label l;

    MouseHandler(Label l) {
      this.l = l;
    }

    public void mousePressed(MouseEvent event) {
      Label l = (Label)event.getSource();

      status.setText("pressed mouse: "+ l);

      pw = new PopupWindow(getVt().getFrame(), l);
      pw.addWindowListener(new WindowHandler());
    }
  
    public void mouseReleased(MouseEvent event) {
      Label l = (Label)event.getSource();

      status.setText("released mouse: "+ l);

      pw.dispose();
    }
  }

  class StatusLabel extends Label {
    StatusLabel(String string) {
      super(string);
    }

    public void setText(String string) {
      super.setText(string);
      System.out.println(string);
    }
  }

  class PopupWindow extends java.awt.Window {
    
    final Font f = new Font("Courier", Font.PLAIN, 40);
    final static int w = 50;
    final static int h = 50;

    Label l;

    PopupWindow(Frame frame, Label l) {
      super(frame);
      Point p = l.getLocationOnScreen();
      this.l = l;
      this.setSize(w, h);
      this.setLocation(p.x, p.y);
      this.setVisible(true);
    }
 
    public void paint(Graphics g) {
      System.out.println("label "+ l);
     
      g.setColor(Color.red);
      g.setFont(f);

      FontMetrics fm = g.getFontMetrics();

      g.drawRect(0, 0, w - 1, h - 1);
      g.drawString(l.getText(), (w - fm.stringWidth(l.getText())) / 2, h - (fm.getHeight() - fm.getAscent() / 2));
    }
  }
  
  public WindowTest1() {
    setLayout(new BorderLayout());

    /*
    ** Create the main panel:
    */

    Panel p = new Panel(new GridLayout(5, 5));
    
    for (int i = 0; i < chars.length; i = i + 1) {
      Label l = new Label(new Character(chars[i]).toString(), Label.CENTER);
      l.addMouseListener(new MouseHandler(l));
      p.add(l);
    }

    add(p, BorderLayout.CENTER);
    
    /*
    ** Create the status label:
    */

    status = new StatusLabel("status: no messages");
    add(status, BorderLayout.SOUTH);
  }

  public String getHelpText() {
    return "bwa! hah!";
  }
 
}

