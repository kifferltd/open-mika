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


// Author: D. Buytaert
// Created: 2001/05/03

package com.acunia.wonka.test.awt.Component;

import com.acunia.wonka.test.awt.*;
import java.awt.*;
import java.awt.event.*;

public class ComponentUpdate2 extends VisualTestImpl implements ActionListener {
    /** variables */

  private UpdateComponent update;
  private UpdateComponent paint;
  private Button repaint;
  private Button reset;
  private Button validate;
  private Button validateEast;

    /** constructor */
  public ComponentUpdate2() {
    super(new BorderLayout());
    setBackground(Color.white);
    update = new UpdateComponent(true);
    paint = new UpdateComponent(false);
    repaint = new Button("repaint");
    reset = new Button("reset");
    validate = new Button("validate");
    validateEast = new Button("validate Eastern component");
    repaint.addActionListener(this);
    reset.addActionListener(this);
    validate.addActionListener(this);
    validateEast.addActionListener(this);
    this.add(update, BorderLayout.WEST);
    this.add(paint, BorderLayout.EAST);
    Panel p = new Panel(new GridLayout(4,1));
    p.add(repaint);
    p.add(reset);
    p.add(validate);
    p.add(validateEast);
    this.add(p, BorderLayout.CENTER);
  }


  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    if(src == validate){
      this.invalidate();
      this.validate();
    }
    else if (src == validateEast){
      paint.invalidate();
      paint.validate();
    }
    else {
      if(src == reset){
        paint.firsttime = true;
      }
      update.repaint();
      paint.repaint();
    }
  }

  static class UpdateComponent extends Canvas {

    private boolean type;
    boolean firsttime = true;

    public UpdateComponent(boolean type){
      this.type = type;
      setBackground(type ? Color.green : Color.red);
      setForeground(Color.blue);
    }

    public void update(Graphics g){
      if(type){
        super.update(g);
      }
      else if(firsttime){
        Dimension d = getSize();
        g.setColor(Color.green);
        g.fillRect(0,0, d.width , d.height);
        firsttime = false;
      }
    }

    public void paint(Graphics g){
      Dimension d = getSize();
      g.drawRect(0,0, d.width -1 , d.height -1);
      g.drawRect(1,1, d.width -3 , d.height -3);
      g.drawRect(2,2, d.width -5 , d.height -5);
    }

    public Dimension getPreferredSize(){
      return new Dimension(75,75);
    }
    public Dimension getMinimumSize(){
      return new Dimension(75,75);
    }
  }

  public String getHelpText(){
    return "This test shows the behaviour of update.  The green block on the right has a RED background, but"+
           " overrides update to avoid the repainting of the background.  This test succeeds if at all time the background is green."+
           " Clicking on the reset button puts the test in correct state again";
  }

  public void showTest(){
    this.validate();
    paint.firsttime = true;
    paint.repaint();
  }

  public void start(Panel p, boolean ar){
    this.validate();
    paint.firsttime = true;
    paint.repaint();
  }

  public static void main(String[] args) {
    Frame main=new Frame("Component paint test");
    main.setSize(400,234);
    main.add(new ComponentUpdate2());
    main.setVisible(true);
  }


}
