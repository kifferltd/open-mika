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


package com.acunia.wonka.test.awt.layout;

import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class CardLayoutPositions extends GridLayoutPositions implements ActionListener{

  /** Variables*/
  //protected List display;
  //protected final static Color SMOKE = new Color(64,64,127);
  private Button first;
  private Button second;
  private Button third;
  private Button fourth;
  private Button buttonA;
  private Button buttonB;
  private Button buttonC;
  private Button buttonD;
  private Button lastClicked;
  private CardLayout layout1;
  private CardLayout layout2;
  private CardLayout layout3;
  private CardLayout layout4;
  private CardLayout layout5;
  private CardLayout layout6;
  private Panel panel1;
  private Panel panel2;
  private Panel panel3;
  private Panel panel4;
  private Panel panel5;
  private Panel panel6;

  private final static String CARDA="card <A>";
  private final static String CARDB="card <B>";
  private final static String CARDC="card <C>";
  private final static String CARDD="card <D>";

  /** constructor */
  public CardLayoutPositions() {
    setBackground(Color.black);
    setForeground(Color.yellow);
    setLayout(new java.awt.BorderLayout());

    Panel buttons = new Panel(new java.awt.GridLayout(9,1));
        first = new Button("First panel");
        first.addActionListener(this);
        first.setEnabled(false);
      buttons.add(first);
        second = new Button("Second panel");
        second.addActionListener(this);
        second.setEnabled(true);
      buttons.add(second);
        third = new Button("Third panel");
        third.addActionListener(this);
        third.setEnabled(false);
      buttons.add(third);
        fourth = new Button("Fourth panel");
        fourth.addActionListener(this);
        fourth.setEnabled(true);
      buttons.add(fourth);
        lastClicked = first;
      buttons.add(new Label());
        buttonA = new Button(CARDA);
        buttonA.addActionListener(this);
      buttons.add(buttonA);
        buttonB = new Button(CARDB);
        buttonB.addActionListener(this);
      buttons.add(buttonB);
        buttonC = new Button(CARDC);
        buttonC.addActionListener(this);
      buttons.add(buttonC);
        buttonD = new Button(CARDD);
        buttonD.addActionListener(this);
      buttons.add(buttonD);
    add(buttons, java.awt.BorderLayout.WEST);

    Panel main = new Panel(new java.awt.GridLayout(3,2));
      layout1 = new CardLayout();
      panel1 = new Panel(layout1);
      Button a = locationButton(CARDA,Color.red,CARDA+" added first using add()");
      Button b = locationButton(CARDB,Color.green,CARDB+" added second using add()");
      Button c = locationButton(CARDC,Color.blue,CARDC+" added third using add()");
      Button d = locationButton(CARDD,Color.yellow,CARDD+" added last using add()");
      Button title = locationButton("Reference layout add()",Color.black,"Layout built in sequence <A>,<B>,<C>,<D> using add()");
      main.add(buildLayout(title, panel1, a, CARDA, b, CARDB, c, CARDC, d, CARDD));

      layout2 = new CardLayout();
      panel2 = new Panel(layout2);
      a = locationButton(CARDA,Color.red,CARDA+" added first to last place in list using add(-1)");
      b = locationButton(CARDB,Color.green,CARDB+" added second to last place in list using add(-1)");
      c = locationButton(CARDC,Color.blue,CARDC+" added third to last place in list using add(-1)");
      d = locationButton(CARDD,Color.yellow,CARDD+" added last to last place in list using add()");
      title = locationButton("positioned default by add(-1)",Color.black,"Components added in sequence <A>,<B>,<C>,<D> using add(-1)");
      main.add(buildLayout(title, panel2, a, CARDA, -1, b, CARDB, -1, c, CARDC, -1, d, CARDD, -1));

      layout3 = new CardLayout();
      panel3 = new Panel(layout3);
      a = locationButton(CARDA,Color.red,CARDA+" added last using add()");
      b = locationButton(CARDB,Color.green,CARDB+" added third using add()");
      c = locationButton(CARDC,Color.blue,CARDC+" added second using add()");
      d = locationButton(CARDD,Color.yellow,CARDD+" added first using add()");
      title = locationButton("Inverse layout",Color.black,"Layout built in sequence <D>,<C>,<B>,<A> using add()");
      main.add(buildLayout(title, panel3, d, CARDD, c, CARDC, b, CARDB, a, CARDA));

      layout4 = new CardLayout();
      panel4 = new Panel(layout4);
      a = locationButton(CARDA,Color.red,CARDA+" added first using add(0)");
      b = locationButton(CARDB,Color.green,CARDB+" added second to first place in list using add(0)");
      c = locationButton(CARDC,Color.blue,CARDC+" added third to first place in list using add(0)");
      d = locationButton(CARDD,Color.yellow,CARDD+" added fourth to first place in list using add(0)");
      title = locationButton("Positioned first by add(0)",Color.black,"Components added in sequence <A>,<B>,<C>,<D>/new added first in list using add(0)");
      main.add(buildLayout(title, panel4, a, CARDA, 0, b, CARDB, 0, c, CARDC, 0, d, CARDD, 0));

      layout5 = new CardLayout();
      panel5 = new Panel(layout5);
      a = locationButton(CARDA,Color.red,CARDA+" added first using add()");
      b = locationButton(CARDB,Color.green,CARDD+" added second using add()");
      c = locationButton(CARDC,Color.blue,CARDC+" added fourth using add()");
      d = locationButton(CARDD,Color.yellow,CARDD+" added third by add() sequence");
      title = locationButton("<A>,<B>,<D>,<C> by adding order",Color.black,"Layout built in sequence <A>,<B>,<D>,<C> using add()");
      main.add(buildLayout(title, panel5, a, CARDA, b, CARDB, d, CARDD, c, CARDC));

      layout6 = new CardLayout();
      panel6 = new Panel(layout6);
      a = locationButton(CARDA,Color.red,CARDA+" added first to first place in list using add(0)");
      b = locationButton(CARDB,Color.green,CARDB+" added second to second place in list using add(1)");
      c = locationButton(CARDC,Color.blue,CARDC+" added third to third in list using add(2), later moved up by <D>");
      d = locationButton(CARDD,Color.yellow,CARDD+" deliberately added fourth to third place in list using add(2)");
      title = locationButton("<D> third by add(2)",Color.black,"Components added in sequence <A>,<B>,<C>,<D> using add(2) to set <D> third in list");
      main.add(buildLayout(title, panel6, a, CARDA, 0, b, CARDB, 1, c, CARDC, 2, d, CARDD, 2));
    add(main, java.awt.BorderLayout.CENTER);

    display = new List(2,false);
    display.setForeground(Color.white);
    display.add("Click on an item to get more info");
    add(display, java.awt.BorderLayout.SOUTH);

    layout1.first(panel1);
    layout2.first(panel2);
    layout3.first(panel3);
    layout4.first(panel4);
    layout5.first(panel5);
    layout6.first(panel6);
  }

  /** override buildLayout to enable element name constraints */
  /** As in GridBagLayout, but here we need the layout Panel for card access*/
  protected Panel buildLayout(Button title, Panel layout,
                             Button first, Object firstconstraints,
                              Button second, Object secondconstraints,
                               Button third, Object thirdconstraints,
                                 Button fourth, Object fourthconstraints) {
    Panel bigpicture = new Panel(new java.awt.BorderLayout());
      bigpicture.add(title,java.awt.BorderLayout.NORTH);
        layout.setForeground(Color.black);
        layout.add(first, firstconstraints);
        layout.add(second, secondconstraints);
        layout.add(third, thirdconstraints);
        layout.add(fourth, fourthconstraints);
      bigpicture.add(layout, java.awt.BorderLayout.CENTER);
    return bigpicture;
  }

  /** override buildLayout to enable element name constraints */
  /** As in GridBagLayout, but here we need the layout Panel for card access*/
  protected Panel buildLayout(Button title, Panel layout,
                             Button first, Object firstconstraints, int firstpos,
                              Button second, Object secondconstraints, int secondpos,
                               Button third, Object thirdconstraints, int thirdpos,
                                 Button fourth, Object fourthconstraints, int fourthpos) {
    Panel bigpicture = new Panel(new java.awt.BorderLayout());
      bigpicture.add(title,java.awt.BorderLayout.NORTH);
        layout.setForeground(Color.black);
        layout.add(first, firstconstraints, firstpos);
        layout.add(second, secondconstraints, secondpos);
        layout.add(third, thirdconstraints, thirdpos);
        layout.add(fourth, fourthconstraints, fourthpos);
      bigpicture.add(layout, java.awt.BorderLayout.CENTER);
    return bigpicture;
  }

  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    String command = evt.getActionCommand();
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    if(src==first){
      layout1.first(panel1);
      layout2.first(panel2);
      layout3.first(panel3);
      layout4.first(panel4);
      layout5.first(panel5);
      layout6.first(panel6);
      display.add("Showing first panel",0);
      setButtons(first, false, true, false, true);
    }
    else if(src==second){
      if(lastClicked == first){
        layout1.next(panel1);
        layout2.next(panel2);
        layout3.next(panel3);
        layout4.next(panel4);
        layout5.next(panel5);
        layout6.next(panel6);
        display.add("Moved down (CardLayout.next() ) to second panel",0);
      }
      else {
        layout1.previous(panel1);
        layout2.previous(panel2);
        layout3.previous(panel3);
        layout4.previous(panel4);
        layout5.previous(panel5);
        layout6.previous(panel6);
        display.add("Moved up (CardLayout.previous() ) to second panel",0);
      }
      setButtons(second, true, false, true, true);
    }
    else if(src==third){
      if(lastClicked == second){
        layout1.next(panel1);
        layout2.next(panel2);
        layout3.next(panel3);
        layout4.next(panel4);
        layout5.next(panel5);
        layout6.next(panel6);
        display.add("Moved down (CardLayout.next() ) to third panel",0);
      }
      else {
        layout1.previous(panel1);
        layout2.previous(panel2);
        layout3.previous(panel3);
        layout4.previous(panel4);
        layout5.previous(panel5);
        layout6.previous(panel6);
        display.add("Moved up (CardLayout.previous() ) to third panel",0);
      }
      setButtons(third, true, true, false, true);
    }
    else if(src==fourth){
      layout1.last(panel1);
      layout2.last(panel2);
      layout3.last(panel3);
      layout4.last(panel4);
      layout5.last(panel5);
      layout6.last(panel6);
      display.add("Showing last panel",0);
      setButtons(fourth, true, false, true, false);
    }
    else if(command.equals(CARDA) || command.equals(CARDB) || command.equals(CARDC) || command.equals(CARDD)){
      layout1.show(panel1,command);
      layout2.show(panel2,command);
      layout3.show(panel3,command);
      layout4.show(panel4,command);
      layout5.show(panel5,command);
      layout6.show(panel6,command);
      display.add("Showing panel "+command,0);
      setButtons(null, true, false, false, true);
    }
    else { // simple button
      display.add(command,0);
    }
  }

  private void setButtons(Button current, boolean enablefirst, boolean enablesecond, boolean enablethird, boolean enablefourth){
    setButton(first, enablefirst, current==first);
    setButton(second, enablesecond, current==second);
    setButton(third, enablethird, current==third);
    setButton(fourth, enablefourth, current==fourth);
    lastClicked = current;
  };
  private void setButton(Button target, boolean enabled, boolean iscurrent){
    if(iscurrent){
      target.setEnabled(false);
      target.setForeground(Color.white);
    }
    else if(!enabled){
      target.setEnabled(false);
      target.setForeground(RUST);
    }
    else{
      target.setEnabled(true);
      target.setForeground(Color.yellow);
    }
  };

    /** VTE help */
  public String getHelpText(){
    return "A test to verify Rudolph's implementation of the Container.add(Component, name, position) form in a CardLayout :\n\n"+
           "The cardlayout is special in a way that it stores its elements both under a specific name as well as by its position in the"+
           " internal list. Likewise you can show an element <directly> by calling CardLayout.show(element name) and also can access"+
           " it by its position in the list by calling the CardLayout.first(), -.next(), -.previous() and -.last() functions\n\n"+
           "Again, this test shows three rows of two panels. Again to each of them are added our four elements <A>,<B>,<C> and.<D>\n"+
           "Using the <first>,<second>, <third> and <fourth> buttons, you can use a combination of CardLayout.first(), -.next(), -.previous()"+
           " and -.last() calls to access the first, second, third or fourth element in the layout\n"+
           "Using the <card- > buttons, you can directly access the element of that name by using CardLayout.show(element name)\n\n"+
           "As in all other position tests, the elements are added in order <A>+<B>+<C>+<D>, order <D>+<C>+<B>+<A> and order <A>+<B>+<D>+<C>"+
           "respectingly, the left side by changing the order in which the elements are added to the list (using add(element, name))"+
           ", the right one by specifying the order in the add(element, name, position) form\n\n"+
           "IN ALL OF THE SITUATIONS, FOR ALL OF THE ROWS, THE LEFT PANELS AND THEIR CORRESPONDING RIGHT PANELS SHOULD LOOK EXACTLY THE SAME";
  }
}
