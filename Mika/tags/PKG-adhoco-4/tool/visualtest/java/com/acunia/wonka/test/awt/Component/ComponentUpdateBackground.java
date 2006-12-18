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

public class ComponentUpdateBackground extends VisualTestImpl implements ActionListener {


    /** variables */
  private ImplicitPaintComponent panelImplicit;
  private ExplicitGetPaintComponent panelExplicitGet;
  private ExplicitSetPaintComponent panelExplicitSet;
  private Button background;
  private Checkbox backgroundImplicit;
  private Checkbox backgroundExplicitGet;
  private Checkbox backgroundExplicitSet;
  private Checkbox backgroundAll;
  private Checkbox alsoRepaint;

  private int currentBackground;
  List display;

    /** constructor */
  public ComponentUpdateBackground() {
    setLayout(new BorderLayout());
    currentBackground = 0;
    setBackground(PaintComponent.MIDCOLORS[0]);
    setForeground(PaintComponent.CONTRASTCOLORS[0]);

      display = new List(5,false);
      display.add("click on the background button");
      display.add("to see the paint() -and update() events");
      display.add("when changing the background of the selected panels");
    add(display,BorderLayout.SOUTH);

    Panel top = new Panel(new GridLayout(2,4) );
      top.add(new Label());
        background = new Button("change background()...");
        background.setBackground(PaintComponent.MIDCOLORS[currentBackground]);
        background.addActionListener(this);
      top.add(background);
      top.add(new Label());
        alsoRepaint = new Checkbox("also repaint()");
      top.add(alsoRepaint);

        backgroundImplicit = new Checkbox("Implicit panel");
      top.add(backgroundImplicit);
        backgroundExplicitGet = new Checkbox("Explicit get");
      top.add(backgroundExplicitGet);
        backgroundExplicitSet = new Checkbox("Explicit set");
      top.add(backgroundExplicitSet);
        backgroundAll = new Checkbox("whole screen");
      top.add(backgroundAll);
    add(top,BorderLayout.NORTH);

    Panel components = new Panel(new GridLayout(1,3) );
      panelImplicit = new ImplicitPaintComponent("framework background");
      panelImplicit.setBackground(PaintComponent.MIDCOLORS[currentBackground]);
      components.add(panelImplicit);
      panelExplicitGet = new ExplicitGetPaintComponent("getBackground()");
      panelExplicitGet.setBackground(PaintComponent.MIDCOLORS[currentBackground]);
      components.add(panelExplicitGet);
      panelExplicitSet = new ExplicitSetPaintComponent("setBackground()");
      panelExplicitSet.setBackground(PaintComponent.MIDCOLORS[currentBackground]);
      components.add(panelExplicitSet);
    add(components, BorderLayout.CENTER);
  }

  /****************************************************************************************************************************************/
  /**
  * on click background
  */
  public void actionPerformed(ActionEvent evt) {
    display.removeAll();
    display.add("Sending setBackground()-command to selected components");
    if(alsoRepaint.getState()) {
      display.add("also sending repaint() command");
    }

    currentBackground = (currentBackground+1)%PaintComponent.COLORCOUNT;
    if(backgroundAll.getState()) {
      setBackground(PaintComponent.MIDCOLORS[currentBackground]);
      setForeground(PaintComponent.CONTRASTCOLORS[currentBackground]);
      if(alsoRepaint.getState()) {
        repaint();
      }
    }
    else {
      background.setBackground(PaintComponent.MIDCOLORS[currentBackground]);
      background.setForeground(PaintComponent.CONTRASTCOLORS[currentBackground]);
    }
    if(backgroundImplicit.getState()) {
      panelImplicit.setBackground(PaintComponent.MIDCOLORS[currentBackground]);
      if(alsoRepaint.getState()) {
        panelImplicit.repaint();
      }
    }
    if(backgroundExplicitGet.getState()) {
      panelExplicitGet.setBackground(PaintComponent.MIDCOLORS[currentBackground]);
      if(alsoRepaint.getState()) {
        panelExplicitGet.repaint();
      }
    }
    if(backgroundExplicitSet.getState()) {
      panelExplicitSet.setBackground(PaintComponent.MIDCOLORS[currentBackground]);
      if(alsoRepaint.getState()) {
        panelExplicitSet.repaint();
      }
    }
    display.repaint();
  }

  public String getHelpText() {
    return "A test on the interaction between the update() and paint() commands when changing the background of a component\n"+
           " Three components in the center area receive paint() and update() commands. On a paint() command they draw a dark rectangle"+
           " on the screen and in that display the number of updates and paints up to now. On an update command, they do the same,"+
           " but for a lighter screen rectangle.\n"+
           "Around the rectangle there is a border that should be painted in the current component's background color.\n"+
           "(The colors change in order of the colors of the rainbow: red-orange-yellow-green-blue-indigo-violet... and then red again)\n"+
           "The <background> keys changes the background of selected components. The checkboxes allow you to specify of which components"+
           " the background will be changed: the left, middle, right component, or the whole test frame.\n"+
           "With the repaint-checkbox, you can also set a repaint-command to the components selected.\n"+
           "The three display components vary in the way they paint their background:\n"+
           " -> The left implicit panel does not paint the background at all, but leaves the painting to the framework\n"+
           " -> The center getBackground panel explicitly paints the screen in the color retrieved by a call to getBackground()"+
           " -> The center setBackground panel explicitly catches the setBackground() comand and stores the new background color"+
           " as a separate variable. When painting or updating, it explicitly paints the screen in that color"+
           "Try various combinations of changing the background color and repainting and regard the calls to update() and paint()"+
           " displayed in the list below";
  }
  /****************************************************************/
  /** Overwritten the PaintComponent class throw a message
  when receiving a setBackground command*/
  class ImplicitPaintComponent extends PaintComponent {
    public ImplicitPaintComponent(String text) {
      super(text, display);
    }

    /**Overridden setBackground() to display a message when called */
    public void setBackground(Color newbackground) {
      super.setBackground(newbackground);
      display.add(name+": set background to "+newbackground);
    }
  }

  /****************************************************************/
  /** Overwritten the PaintComponent class to explicitly
   clear the screen in background color when updating*/
  class ExplicitGetPaintComponent extends PaintComponent {
    public ExplicitGetPaintComponent(String text) {
      super(text, display);
    }

    /**Overridden setBackground() to display a message when called */
    public void setBackground(Color newbackground) {
      super.setBackground(newbackground);
      display.add(name+": set background to "+newbackground);
    }

    /**Overridden paint command to explicitly paint background in getBackground color */
    protected boolean paintArea(Color background, Color foreground, Graphics g){
      boolean calculated=false;
      if(frameRect.width<=0){
        frameRect.setBounds(0,0,this.getSize().width, this.getSize().height);
        calculated=true;
      }
      // explicit background clear
      g.setColor(this.getBackground());
      g.fillRect(0,0, frameRect.width-1, frameRect.height-1);
      // normal code
      g.setColor(background);
      g.fillRect(5,5, frameRect.width-10, frameRect.height-10);
      g.setColor(foreground);
      g.drawString(name, 10,20);
      g.drawString("updates "+updateCount, 10,35);
      g.drawString("paints = "+paintCount, 10,50);
      return calculated;
    }
  }

  /****************************************************************/
  /** Overwritten the PaintComponent class to explicitly store
   the background color to clear the screen in*/
  class ExplicitSetPaintComponent extends PaintComponent {
    private Color screenBackground;

    public ExplicitSetPaintComponent(String text) {
      super(text, display);
      screenBackground = this.getBackground();
    }

    /**Overridden setBackground() store desired color in discrete screenBackground variable */
    public void setBackground(Color newbackground) {
      screenBackground = newbackground;
      //super.setBackground(newbackground);
      display.add(name+": set background to "+newbackground);
    }


    /**Overridden paint command to explicitly paint background in locally stored background color */
    protected boolean paintArea(Color background, Color foreground, Graphics g){
      boolean calculated=false;
      if(frameRect.width<=0){
        frameRect.setBounds(0,0,this.getSize().width, this.getSize().height);
        calculated=true;
      }
      // explicit background clear
      g.setColor(screenBackground);
      g.fillRect(0,0, frameRect.width-1, frameRect.height-1);
      // normal code
      g.setColor(background);
      g.fillRect(5,5, frameRect.width-10, frameRect.height-10);
      g.setColor(foreground);
      g.drawString(name, 10,20);
      g.drawString("updates "+updateCount, 10,35);
      g.drawString("paints = "+paintCount, 10,50);
      return calculated;
    }
  }
}
