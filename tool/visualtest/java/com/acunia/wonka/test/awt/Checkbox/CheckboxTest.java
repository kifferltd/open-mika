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


package com.acunia.wonka.test.awt.Checkbox;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.ItemSelectable;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class CheckboxTest extends VisualTestImpl implements ItemListener{

  final static Color BACKGROUND=new Color(128,64,160);
  Checkbox[] options;

  private InnerMessagePanel messagePanel;

  public CheckboxTest() {
    setBackground(BACKGROUND);
    setFont(new Font("courR17",Font.PLAIN,17));
    CheckboxGroup group  = new CheckboxGroup();
    options = new Checkbox[6];
    for(int i=0; i<3;i++) {
      options[i]=new Checkbox("option_"+(i+1),true,group);
    }
    for(int i=3; i<6;i++) {
      options[i]=new Checkbox("option_"+(i+1),true);
    }

    messagePanel = new InnerMessagePanel();
    this.setLayout(new BorderLayout());
    Panel p = new Panel(new GridLayout(2, 3));
      for(int i=0; i<6;i++) {
        p.add(options[i]);
        options[i].addItemListener(this);
      }
    add(p,BorderLayout.NORTH);
    add(messagePanel, BorderLayout.CENTER);
  }

  public void itemStateChanged(ItemEvent evt) {
    messagePanel.paintEvent(evt);
  }

  public String getHelpText() {
    return "A test for checkboxes and radio-buttons: check and uncheck the radio-buttons and checkboxes below.\n"+
           "The ItemEvent thrown by the checkbox will be analysed and displayed in the panel below.  Compare its output against that of Blackdown/Sun.\n";
  }

  /****************************************************************************************************************************************/
  /**  inner class with painting and mouse movements(based on panel since some Wonka versions have/had difficulties repainting Components)*/
  class InnerMessagePanel extends Panel {
    /** variables */
    private Rectangle bounds;
    private Rectangle inside;
    private String line1;
    private String line2;
    private String line3;
    private String line4;
    /** constructor */
    public InnerMessagePanel() {
      super();
      bounds = new Rectangle(0,0,0,0);
      inside = new Rectangle(0,0,0,0);
      line1 = "Select one of the checkboxes";
      line2 = "  and see the result";
      line3 = "    HERE...";
      line4 = "           ...";
    }

    /** Sizes */
    public Dimension getMinimumSize() {
      return new Dimension(70,50);
    }

    public Dimension getPreferredSize() {
      return new Dimension(70,50);
    }

  /****************************************************************************************************************************************/
    /**     display event diagnostics and repaint    */
    /** Following functions will be tested :         */
    /** (java.util)EventObject.getSource()           */
    /** (java awt.event)ItemEvent.getItem()          */
    /** (java awt.event)ItemEvent.getItemSelectable()*/
    /** (java awt.event)ItemEvent.getStateChange()   */
    public void paintEvent(ItemEvent evt) {
      // line 1: EventObject.getSource
      Object source = evt.getSource();
      if(source==null){
        line1 = "evt.getSource() == NULL";
      }
      else if (source instanceof Checkbox) {
        line1 = "evt.getSource() = "+((Checkbox)source).getLabel();
      }
      else {
        line1 = "evt.getSource() type = "+source.getClass().getName();
      }
      //line2:ItemEvent.getItem()
      source = evt.getItem();
      if(source==null){
        line2 = "evt.getItem() == NULL";
      }
      else if (source instanceof Checkbox) {
        line2 = "evt.getItem() = "+((Checkbox)source).getLabel();
      }
      else {
        line2 = " evt.getItem() of type = "+source.getClass().getName();
      }
      // line 3: ItemSelectable()
      ItemSelectable selectable=evt.getItemSelectable();
      Object[] selection = selectable.getSelectedObjects();
      if(selectable==null){
        line3 = "evt.getItemSelectable() == NULL";
      }
      else if (selection == null) {
        line3 = "getItemSelectable():no selections";
      }
      else {
        //line3 = "getItemSelectable():selection=";
        line3 = "selections {";
        for(int i=0; i<selection.length; i++) {
          line3+= " "+selection[i];
        }
        line3+="}";
      }
      // line 4: ItemEvent.getStateChange
      if(evt.getStateChange()==ItemEvent.SELECTED) {
        line4 = "evt.getStateChange() = SELECTED ("+evt.getStateChange()+")";
      }
      else if(evt.getStateChange()==ItemEvent.DESELECTED) {
        line4 = "evt.getStateChange() = DESELECTED ("+evt.getStateChange()+")";
      }
      else {
        line4 = "(unknown state: evt.getStateChange() = "+evt.getStateChange()+")";
      }
System.out.println(line1);
System.out.println(line2);
System.out.println(line3);
System.out.println(line4);
      this.repaint();
    }

    /** paint */
    public void paint(Graphics g) {
			update(g);
  	}
  	
  	public void update(Graphics g) {
      // first time initialiser
      if(bounds.width==0 ){
        bounds.setBounds(1,1, this.getSize().width-2, this.getSize().height-2);
        inside.setBounds(7,7, this.getSize().width-14, this.getSize().height-14);
      }
      //g.setColor(BACKGROUND);
      g.clearRect(1,1, bounds.width, bounds.height);
      g.setColor(Color.white);
      g.drawRect(inside.x, inside.y, inside.width, inside.height);
      g.drawString(line1,10,20);
      g.drawString(line2,10,40);
      g.drawString(line3,10,60);
      g.drawString(line4,10,80);
      int third=inside.width/3;
      int x=10;
      int y= inside.height-35;
      g.drawString("Selected options :",20,y);
      y+=18;
      for(int i=0; i<3; i++) {
        if(options[i].getState() ) {
          //checkbox is selected
          g.drawString(options[i].getLabel(),x,y);
        }
        x+=third;
      }
      x=10;
      y+=18;
      for(int i=3; i<6; i++) {
        if(options[i].getState() ) {
          //checkbox is selected
          g.drawString(options[i].getLabel(),x,y);
        }
        x+=third;
      }
    }
    //(end inner class)
  }

  //end test
}
