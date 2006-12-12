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
// Created: 2001/11/21

package com.acunia.wonka.test.awt.GridBagLayout;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class DynamicLayout extends VisualTestImpl implements ActionListener  {
  /****************************************************************/
  /** variables
  */
  private List display;
  private Panel testPanel;
  private Button currentElement;

  private Button remove;
  private Button undo;
  private Button reset;
  private Button getLayout;
  private Checkbox none;
  private Checkbox horizontal;
  private Checkbox vertical;
  private Checkbox both;

  /****************************************************************/
  /** constructor
  */
  public DynamicLayout() {
    super();
    setLayout(new BorderLayout());
    setBackground(Color.yellow);

    //header
    add(new Label("GridBagLayout demo...",Label.CENTER), BorderLayout.NORTH);

    // left panel with command buttons
    Panel buttons = new Panel(new GridLayout(9,1));
      // layout checkboxes
      CheckboxGroup layouts = new CheckboxGroup();
      none = new Checkbox("fill NONE",false,layouts);
      buttons.add(none);
      horizontal = new Checkbox("fill HORIZONTAL",true,layouts);
      buttons.add(horizontal);
      vertical = new Checkbox("fill VERTICAL",true,layouts);
      buttons.add(vertical);
      both = new Checkbox("fill BOTH",true,layouts);
      buttons.add(both);

      // rebuild
      reset = new Button("(Re)Build layout");
      reset.addActionListener(this);
      buttons.add(reset);
      // layout data
      getLayout = new Button("Print layout data");
      getLayout.addActionListener(this);
      buttons.add(getLayout);

      buttons.add(new Label());

      //remove element
      remove = new Button("Remove current");
      remove.addActionListener(this);
      buttons.add(remove);
      // undo
      undo = new Button("Undo remove");
      undo.addActionListener(this);
      undo.setEnabled(false);
      buttons.add(undo);
    add(buttons, BorderLayout.EAST);

    // list
    display = new List(3,false);
    display.add("Click on a GridbagLayout button to see its constraints HERE");
    add(display, BorderLayout.SOUTH);


    // center test panel with gridbag layout
    testPanel = new Panel(new GridBagLayout());
    testPanel.setBackground(new Color(200,200,120) );
    invokeLunaticLayout(testPanel);
    add(testPanel, BorderLayout.CENTER);


  }

  /****************************************************************/
  /** auxilliary: invoka a 'lunatic' layout on the current test panel
  */
  private void invokeLunaticLayout(Container cont) {
                                           // colweights  0   1   2   3   4   5   6     rowweights   0   1   2   3   4
    makeButton(cont, "No.01", 0, 0, 3, 2, 1.0, 0.0);   //  .0  .0  .0                                 .0  .0
    makeButton(cont, "No.02", 3, 0, 1, 3, 0.5, 0.0);   //              .5                             .0  .0  .0
    makeButton(cont, "No.03", 4, 0, 3, 1, 1.0, 0.5);   //                  .0  .0  .0                 .5
    makeButton(cont, "No.04", 5, 1, 1, 1, 1.0, 1.0);   //                      1                          1
    makeButton(cont, "No.05", 4, 1, 1, 4, 1.0, 1.0);   //                  1                              .0  .0  .0  .0
    makeButton(cont, "No.06", 0, 2, 1, 3, 0.5, 1.0);   //  .5                                                 1
    makeButton(cont, "No.07", 2, 2, 1, 1, 2.0, 1.0);   //          2                                          1
    makeButton(cont, "No.08", 6, 1, 1, 3, 1.0, 1.0);   //                          1                      .0  .0  .0
    makeButton(cont, "No.09", 1, 3, 3, 2, 2.0, 0.0);   //      .0  .0  .0                                         .0  .0
    makeButton(cont, "No.10", 5, 3, 1, 1, 1.0, 0.0);  //                      1                                  .0
    makeButton(cont, "No.11", 5, 4, 2, 1, 1.0, 1.0);  //                      .5  .5                                 1
    cont.validate();
    currentElement = null;
  }

  /****************************************************************/
  /** auxilliary: build a gridbagLayout button
  */
  public void makeButton(Container cont, String label,
                         int x, int y, int w, int h, double wx, double wy) {
    
    GridBagConstraints c = new GridBagConstraints();
    Button comp;
    c.anchor = GridBagConstraints.CENTER;
    if(none.getState()){
      c.fill = GridBagConstraints.NONE;
    }
    else if(horizontal.getState()){
      c.fill = GridBagConstraints.HORIZONTAL;
    }
    else if(vertical.getState()){
      c.fill = GridBagConstraints.VERTICAL;
    }
    else{
      c.fill = GridBagConstraints.BOTH;
    }
    c.gridx = x;
    c.gridy = y;
    c.gridwidth = w;
    c.gridheight = h;
    c.weightx = wx;
    c.weighty = wy;
    comp = new Button(label);
    comp.setActionCommand("Button "+label+" Position=("+x+","+y+") size=("+w+","+h+") weight=("+wx+", "+wy+")");
    comp.addActionListener(this);
    comp.setFont(new Font("courP14",0,14));
    comp.setBackground(new Color(x*40, 100, y*40));
    comp.setForeground(Color.black);
    cont.add(comp, c);
  }

  /****************************************************************/
  /** Button pressed
  */
  public void actionPerformed(ActionEvent evt) {
    Button source = (Button)evt.getSource();
    if(source == remove) {
      if(currentElement != null) {
        testPanel.remove(currentElement);
        testPanel.validate();
      }
      displayMessage("Removed : "+currentElement.getActionCommand());
      remove.setEnabled(false);
      undo.setEnabled(true);
    }
    else if (source == undo) {
      if(currentElement != null) {
        testPanel.add(currentElement);
        testPanel.validate();
      }
      displayMessage("added : "+currentElement.getActionCommand());
      remove.setEnabled(true);
      undo.setEnabled(false);
    }
    else if (source == getLayout) {
      displayGridLayout();
    }
    else if (source == reset) {
      testPanel.removeAll();
      invokeLunaticLayout(testPanel);
      remove.setEnabled(false);
      undo.setEnabled(false);
      displayMessage("(Re)Built layout");
    }
    else {
      inverseButton(currentElement);
      inverseButton(source);
      displayMessage("Pressed : "+source.getActionCommand());
      currentElement = source;
      remove.setEnabled(true);
      undo.setEnabled(false);
    }
  }

  /****************************************************************/
  /** auxilliary: display grid layout
  */
  private void displayGridLayout() {
    GridBagLayout toscan = (GridBagLayout)testPanel.getLayout();
    int[][] dimensions = toscan.getLayoutDimensions();
    double[][] weights = toscan.getLayoutWeights();
    if(dimensions == null){
      displayMessage("GridBagLayout Dimensions returned NULL");
    }
    else if(weights == null) {
      displayMessage("GridBagLayout Weights returned NULL");
    }
    else {
      String message = "...for: "+dimensions[1].length+" rows: { ";
      if(dimensions[1].length>0){
        message+=dimensions[1][0]+"("+weights[1][0];
      }
      for(int i=1; i<dimensions[1].length; i++) {
        message+="),  "+dimensions[1][i]+"("+weights[1][i];
      }
      displayMessage(message+") }");
      message = "Grid data for: "+dimensions[0].length+" columns: { ";
      if(dimensions[0].length>0){
        message+=dimensions[0][0]+"("+weights[0][0];
      }
      for(int i=1; i<dimensions[1].length; i++) {
        message+="),  "+dimensions[0][i]+"("+weights[0][i];
      }
      displayMessage(message+") }");
    }
  }
  /****************************************************************/
  /** auxilliary: inverse button colors
  */
  private void inverseButton(Button target) {
    if(target != null) {
      Color c1 = target.getBackground();
      Color c2 = target.getForeground();
      target.setForeground(c1);
      target.setBackground(c2);
    }
  }

  /****************************************************************/
  /** Display a message in the display list
  */
  public void displayMessage(String message) {
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    display.add(message,0);
  }

  public String getHelpText(){
    return ("You should see a gridbag of 7 columns by 5 rows. The coordinates x and y and " +
            "dimension w and h should be visible as a label on 10 buttons " +
            "(disgard the right column of vte buttons). The buttons should carry the " +
            "following labels: \n    0,0;3,2 " +
                              "\n    3,0;1,3 " +
                              "\n    4,0;3,1 " +
                              "\n    5,1;1,1 " +
                              "\n    4,1;1,4 " +
                              "\n    2,2;1,1 " +
                              "\n    6,1;1,3 " +
                              "\n    1,3;3,2 " +
                              "\n    5,3;1,1 " +
                              "\n    5,4;2,1 \n" +
            "and should be positioned and sized accordingly.\n"+
            "Click on a button to see its data in the list below.\n"+
            "The <Print layout data> button shows the widths and weights of all rows and columns used.\n"+
            "The <remove> button removes the currently selected button from the list, the <undo> button adds it again."+
            "Like that you can see the change in layout both visually as well as in the row/coulnms grid\n"+
            "The <(re)build> button rebuilds the layout in its original form its fill policy given by the buttons above:"+
            "no filling, horizontal, vertical or both");
  }

  public void start(java.awt.Panel p, boolean b) {
  }

  public void stop(java.awt.Panel p) {
  }
}
