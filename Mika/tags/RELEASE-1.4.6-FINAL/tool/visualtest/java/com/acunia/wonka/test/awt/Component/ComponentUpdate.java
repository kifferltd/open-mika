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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class ComponentUpdate extends VisualTestImpl implements ActionListener {
    /** variables */

  private InnerComponent innerComponent;
  private InnerPaintComponent paintComponent;
  private Button repaint;
  List display;

    /** constructor */
  public ComponentUpdate() {
    setLayout(new BorderLayout());
    setBackground(new Color(16,16,48));
    setForeground(Color.white);

    Panel top = new Panel(new FlowLayout() );
        repaint = new Button("repaint()...");
        repaint.addActionListener(this);
      top.add(repaint);
    add(top,BorderLayout.NORTH);

      display = new List(5,false);
    add(display,BorderLayout.SOUTH);

    Panel components = new Panel(new GridLayout(1,2));
        innerComponent = new InnerComponent();
      components.add(innerComponent);
        paintComponent = new InnerPaintComponent();
      components.add(paintComponent);
    add(components, BorderLayout.CENTER);
  }

  /****************************************************************************************************************************************/
  /**
  * on click repaint
  */
  public void actionPerformed(ActionEvent evt) {
      display.removeAll();
      display.add("Sending repaint()-command without bounds for whole screen");
      innerComponent.repaint();
      paintComponent.repaint();
  }

  /*********************************************************************************/
  /**  inner class with painting and mouse movements , based on panel*/
  class InnerPaintComponent extends PaintComponent {
    private Rectangle bounds;

    /** constructor */
    public InnerPaintComponent() {
      super("PaintComponent",display);
    }

    /** common command for Paint() and update() */
    protected boolean paintArea(Color background, Color foreground, Graphics g){
      bounds = g.getClipBounds();
      boolean calculated = false;
      if(frameRect.width<=0){
        frameRect.setBounds(0,0,this.getSize().width, this.getSize().height);
        calculated=true;
      }
      if(bounds==null){
        bounds = frameRect;
        display.add("NULL bounds: Reverting to full screen (0, 0, "+bounds.width+", "+bounds.height+")");
      }
      else {
        display.add("bounds ("+bounds.x+", "+bounds.y+", "+bounds.width+", "+bounds.height+")");
      }

      g.setColor(background);
      g.fillRect(5,5, frameRect.width-10, frameRect.height-10);
      g.setColor(foreground);
      g.drawString("Paint component:", 10,20);
      g.drawString("updates "+updateCount, 10,35);
      g.drawString("paints = "+paintCount, 10,50);
      return calculated;
    }
    //(end inner class)
  }
  /*********************************************************************************/
  /**  inner class with painting and mouse movements , based on panel*/
  class InnerComponent extends Panel {
    protected Rectangle bounds;
    protected int updateCount;
    protected int paintCount;
    protected Rectangle frameRect;

     public InnerComponent() {
      super();
      updateCount=0;
      paintCount=0;
      frameRect = new Rectangle();
    }

    /** paint */
    public void paint(Graphics g) {
      display.add("Inner class: Received call to paint()");
      paintCount++;
      if(paintArea(PaintComponent.DARKCOLORS[(paintCount-1)%PaintComponent.COLORCOUNT], Color.white, g) ) {
        display.add("Inner class: on paint() calculated sizes");
      }
      display.add("Inner class: paint() executed, total paints = "+paintCount);
  	}
    	
    /** update */
  	public void update(Graphics g) {
      display.add("Inner class: Received call to update()");
      updateCount++;
      if(paintArea(PaintComponent.LIGHTCOLORS[(updateCount-1)%PaintComponent.COLORCOUNT], Color.black, g) ) {
        display.add("Inner class: on update() calculated sizes");
      }
      display.add("Inner class: update() executed, total updates = "+updateCount);
    }

    private boolean paintArea(Color background, Color foreground, Graphics g){
      bounds = g.getClipBounds();
      boolean calculated = false;
      if(frameRect.width<=0){
        frameRect.setBounds(0,0,this.getSize().width, this.getSize().height);
        calculated=true;
      }
      if(bounds==null){
        bounds = frameRect;
        display.add("NULL bounds: Reverting to full screen (0, 0, "+bounds.width+", "+bounds.height+")");
      }
      else {
        display.add("bounds ("+bounds.x+", "+bounds.y+", "+bounds.width+", "+bounds.height+")");
      }

      g.setColor(background);
      g.fillRect(5,5, frameRect.width-10, frameRect.height-10);
      g.setColor(foreground);
      g.drawString("Paint component:", 10,20);
      g.drawString("updates "+updateCount, 10,35);
      g.drawString("paints = "+paintCount, 10,50);
      return calculated;
    }
    //(end inner class)
  }
  
  public String getHelpText() {
    return "A test on the interaction between the update() and paint() commands in different situations\n"+
           " The main part of this test are the two central Components. To refresh the content of the components, a paint() and an update()"+
           " function are implemented, both painting the current clipping bound in a background color and also displaying the number"+
           " of paints and updates made:\n"+
           "With each paint() or update() the background color changes along the colors of the rainbow (red, orange, yellow....)"+
           " Paint commands display white text on dark colors, update-commands black text on light colors. Like this it is possible to see"+
           " wether the clipping area is being paint()-ed or update()-ed \n"+
           "(If the clipping bound is <NULL>, it is being replaced by the complete screen)\n\n"+
           "There are two display component. One derived from Component, the other from an extern class PaintComponent. For some reason the Sun"+
           "sdk always sends a call tp paint() to the external PaintComponent, even on a call to repaint() when the inner component receives"+
           " a call to update()";
  }

  public void main(String[] args) {
    Frame main=new Frame("Component paint test");
    main.setSize(400,234);
    main.add(new ComponentUpdate());
    main.show();
  }


}
