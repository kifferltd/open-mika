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


// Author: J. Bensch
// Created: 2001/08/17

package com.acunia.wonka.test.awt.Font;

import com.acunia.wonka.test.awt.*;
import java.awt.*;
import java.awt.event.*;

public class BaseLine extends VisualTestImpl implements ActionListener, MouseListener {
  private Word activeWord;
  private Button inc, dec;

  class Word extends Canvas {
    String word;
    Font font;
    FontMetrics f_metrics;
    int fontSize = 0, index = 0;
    boolean clicked = false;
    float[] alignments = { -1.0f, Component.CENTER_ALIGNMENT, Component.TOP_ALIGNMENT, Component.BOTTOM_ALIGNMENT};

    Word(String word, int size) {
      this.word = word;
      this.fontSize = size;
      font = new Font("Serif", Font.PLAIN, size);
      f_metrics = this.getFontMetrics(font);
    }

    public float getAlignmentY() {
      if (alignments[index] == -1.0f) {
        return (float)f_metrics.getAscent() / (float)f_metrics.getHeight();
      }
      else {
        return alignments[index];
      }
    }

    public Dimension getPreferredSize() {
      int space = f_metrics.charWidth(' ');
      return new Dimension(space + f_metrics.stringWidth(word), f_metrics.getHeight());
    }
  
    public void setActive(boolean clicked) {
      this.clicked = clicked;
      this.repaint();
    }

    public void paint(Graphics g) {
      FontMetrics f_metrics;
      if (clicked) {
        g.setColor(Color.green);
      }
      else {
        g.setColor(Color.lightGray);
      }
      g.fillRect(0, 0, this.getSize().width, this.getSize().height);
      g.setColor(Color.black);
      g.setFont(font);
      f_metrics = g.getFontMetrics();
      g.drawString(word, f_metrics.charWidth(' '), f_metrics.getAscent());
      g.drawLine(0, f_metrics.getAscent(), this.getSize().width, f_metrics.getAscent());
    }

    public void handleButtonClick(int dec_inc) {
      fontSize +=  dec_inc;
      font = new Font(font.getFamily(), font.getStyle(), fontSize);
      f_metrics = this.getFontMetrics(font);
      this.invalidate();
      this.getParent().getParent().validate();
      this.repaint();
    }
  }

  class BaseLineLayout implements LayoutManager2 {
    int maxAscent = -1, maxDescent = 0;

    public Dimension layoutSize(Container parent_cont, int size) {
      Insets insets = parent_cont.getInsets();
      int width = 0;

      synchronized (parent_cont.getTreeLock()) {
        Dimension d = new Dimension(0, 0);
        maxAscent = 0;
        maxDescent = 0;

        for (int i = 0; i < parent_cont.getComponentCount(); i++) {
          Component c = parent_cont.getComponent(i);

          switch (size) {
            case 0: d = c.getMinimumSize(); break;
            case 1: d = c.getPreferredSize(); break;
            case 2: d = c.getMaximumSize(); break;
          }

          int a = (int) (c.getAlignmentY()*d.height);
          maxAscent = Math.max(maxAscent, a);
          maxDescent = Math.max(maxDescent, d.height - a);
          width += d.width;
        }
        return new Dimension(insets.left + insets.right + width, insets.top + insets.bottom + maxAscent + maxDescent);
      }
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void layoutContainer(Container parent_cont) {
      Insets insets = parent_cont.getInsets();
      if (maxAscent < 0) {
        layoutSize(parent_cont, 1);
      }

      synchronized (parent_cont.getTreeLock()) {
        int l = insets.left;

        for (int i = 0; i < parent_cont.getComponentCount(); i++) {
           Component c = parent_cont.getComponent(i);
           Dimension d = c.getPreferredSize();
           c.setBounds(l, insets.top + (int)(maxAscent - d.height * c.getAlignmentY()), d.width, d.height);
           l += d.width;
        }
      }
    }

    public Dimension minimumLayoutSize(Container parent_cont) {
      return layoutSize(parent_cont, 0);
    }

    public Dimension preferredLayoutSize(Container parent_cont) {
      return layoutSize(parent_cont, 1);
    }

    public void removeLayoutComponent(Component comp) {
    }

    public void addLayoutComponent(Component comp, Object constraints) {
    }

    public float getLayoutAlignmentX(Container target) {
      return 0.5f;
    }

    public float getLayoutAlignmentY(Container parent_cont) {
      if (maxAscent < 0)
        layoutSize(parent_cont, 1);
      if (maxAscent > 0) 
        return (float)maxAscent / (float)(maxAscent + maxDescent);
      return 0.5f;
    }

    public void invalidateLayout(Container target) {
      maxAscent = -1;
    }

    public Dimension maximumLayoutSize(Container parent_cont) {
      return layoutSize(parent_cont, 2);
    }
  }
  
  public BaseLine() {
    Panel p_north = new Panel(), p_center = new Panel();
    inc = new Button("Increase");
    dec = new Button("Decrease");
    Word charlie = new Word("Charlie", 24), wonka = new Word("Wonka", 20), woempa = new Word("Woempa", 14);
    charlie.addMouseListener(this);
    wonka.addMouseListener(this);
    woempa.addMouseListener(this);
    Dimension dim = new Dimension(400, 234);
    setSize(dim);
    setLayout(new BorderLayout());
    p_north.setLayout(new BaseLineLayout());
    p_north.add(charlie);
    p_north.add(wonka);
    p_north.add(woempa);
    inc.addActionListener(this);
    inc.setActionCommand("inc");
    dec.addActionListener(this);
    dec.setActionCommand("dec");
    p_center.add(inc);
    p_center.add(dec);
    add(p_north, BorderLayout.NORTH);
    add(p_center, BorderLayout.CENTER);
    activeWord = charlie;
    activeWord.setActive(true);
    setVisible(true);
  }

  public void actionPerformed(ActionEvent ae) {
    String command = ae.getActionCommand();
    if (command == "inc") {
      activeWord.handleButtonClick(1);
    }
    else {
      activeWord.handleButtonClick(-1);
    }
  }

  public void mouseClicked(MouseEvent me) {
    activeWord.setActive(false);
    activeWord = (Word)me.getSource();
    activeWord.setActive(true);
  }
  
  public void mousePressed(MouseEvent me) {
  }

  public void mouseReleased(MouseEvent me) {
  }

  public void mouseEntered(MouseEvent me) {
  }

  public void mouseExited(MouseEvent me) {
  }

  public String getHelpText() {
    return ("This is a test that lines out the baseline for fonts of different sizes. It auto-rescales so the font wouldn't clip at the top of the screen. You can change the font's size by focusing it and clicking \"Increase\" or \"Decrease\" afterwards.");
  }
}

