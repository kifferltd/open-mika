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

package com.acunia.wonka.test.awt.Component;

import com.acunia.wonka.test.awt.*;
import java.awt.*;
import java.awt.event.*;

public class ComponentRepaint2 extends VisualTestImpl implements MouseListener {
  
  CustomComponent c1;
  CustomComponent c2;
  CustomComponent c3;

  public ComponentRepaint2() {
    setLayout(new BorderLayout());
 
    Panel p1 = new Panel();
    p1.setLayout(null);
    p1.setBackground(Color.black);

    c1 = new CustomComponent(new Color(255, 0, 0));
    c2 = new CustomComponent(new Color(255, 255, 0));
    c3 = new CustomComponent(new Color(0, 255, 0));
    
    p1.add(c1);
    p1.add(c2);
    p1.add(c3);

    c1.setBounds(10, 10, 50, 50);
    c2.setBounds(50, 30, 50, 50);
    c3.setBounds(90, 50, 50, 50);

    c1.addMouseListener(this);
    c2.addMouseListener(this);
    c3.addMouseListener(this);

    Label l1 = new Label("  How it is...");
    l1.setForeground(Color.white);
    l1.setBackground(Color.black);
    
    Panel p3 = new Panel();
    p3.setLayout(new BorderLayout());
    p3.add(p1, BorderLayout.CENTER);
    p3.add(l1, BorderLayout.NORTH);
   
    Label l2 = new Label("  And how it should be...");
    l2.setForeground(Color.white);
    l2.setBackground(Color.black);
   
    ReferenceComponent r = new ReferenceComponent();
    
    Panel p4 = new Panel();
    p4.setLayout(new BorderLayout());
    p4.add(r, BorderLayout.CENTER);
    p4.add(l2, BorderLayout.NORTH);
    
    Panel p2 = new Panel();
    p2.setLayout(new GridLayout(1, 3));

    Button b1 = new Button("Repaint Red");
    Button b2 = new Button("Repaint Yellow");
    Button b3 = new Button("Repaint Green");

    p2.add(b1);
    p2.add(b2);
    p2.add(b3);

    b1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        c1.repaint();
      }
    });
    b2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        c2.repaint();
      }
    });
    b3.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        c3.repaint();
      }
    });

    add(p2, BorderLayout.SOUTH);
    add(p3, BorderLayout.CENTER);
    add(p4, BorderLayout.EAST);
   
    new Thread() {
      public void run() {
        try { sleep(100); } catch(Exception e) { }
        c2.repaint();
      }
    }.start();
  }

  public String getHelpText(){
    return "This is a test for repainting overlapping components.\nNormally the left and the right set " +
           "of squares should look exactly the same.\nThe set on the left is built with Components, the " +
           "set on the right is drawn on a Graphics.\nBy pressing the buttons you can trigger repaints " +
           "of the squares on the left.\nThey should always remain overlapping as the squares on the " +
           "right\n";
  }

  public class CustomComponent extends Component {

    private Color color;
    private boolean highlight;
    
    public CustomComponent(Color color) {
      this.color = color;
    }

    public void paint(Graphics g) {
      Dimension size = this.getSize();
      if(highlight) {
        g.setColor(color.darker().darker());
      }
      else {
        g.setColor(color);
      }
      g.fillRect(0, 0, size.width, size.height);
    }

    public void setHighlight(boolean mode) {
      highlight = mode;
      this.repaint();
    }

  }

  public class ReferenceComponent extends Component {

    public void paint(Graphics g) {
      g.setColor(Color.black);
      g.fillRect(0, 0, this.getSize().width, this.getSize().height);
      g.setColor(new Color(0, 255, 0));
      g.fillRect(90, 50, 50, 50);
      g.setColor(new Color(255, 255, 0));
      g.fillRect(50, 30, 50, 50);
      g.setColor(new Color(255, 0, 0));
      g.fillRect(10, 10, 50, 50);
    }

    public Dimension getPreferredSize() {
      return new Dimension(150, 100);
    }
  }

  public void mouseClicked(MouseEvent event) { }
  public void mouseEntered(MouseEvent event) { }
  public void mouseExited(MouseEvent event) { }
  
  public void mousePressed(MouseEvent event) {
    if(event.getSource() == c1) {
      c1.setHighlight(true);
    }
    else if(event.getSource() == c2) {
      c2.setHighlight(true);
    }
    else if(event.getSource() == c3) {
      c3.setHighlight(true);
    }
  }
  
  public void mouseReleased(MouseEvent event) {
    if(event.getSource() == c1) {
      c1.setHighlight(false);
    }
    else if(event.getSource() == c2) {
      c2.setHighlight(false);
    }
    else if(event.getSource() == c3) {
      c3.setHighlight(false);
    }
  }
  
}
