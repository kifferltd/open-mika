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
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class CardLayoutOverlay extends CardLayoutPositions implements ActionListener{

  /** Variables*/
  //protected List display;
  //protected final static Color SMOKE = new Color(64,64,127);
  private Button next;
  private int current;
  private TextField showing;
  private Button buttonOne;
  private Button buttonTwo;
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

  private final static String CARDONE="card <ONE>";
  private final static String CARDTWO="card <TWO>";

  /** constructor */
  public CardLayoutOverlay() {
    setBackground(Color.black);
    setForeground(Color.yellow);
    setLayout(new java.awt.BorderLayout());

    Panel buttons = new Panel(new java.awt.GridLayout(8,1));
      buttons.add(new Label());
        next = new Button("Next panel");
        next.addActionListener(this);
        next.setEnabled(true);
      buttons.add(next);
      buttons.add(new Label("Showing Panel"));
        showing = new TextField(" 0 of 0");
      buttons.add(showing);
      buttons.add(new Label());
        buttonOne = new Button(CARDONE);
        buttonOne.addActionListener(this);
      buttons.add(buttonOne);
        buttonTwo = new Button(CARDTWO);
        buttonTwo.addActionListener(this);
      buttons.add(buttonTwo);
    add(buttons, java.awt.BorderLayout.WEST);

    Panel main = new Panel(new java.awt.GridLayout(3,2));
      layout1 = new CardLayout();
      panel1 = new Panel(layout1);
      Button a = locationButton(CARDONE,Color.red,"Red "+CARDONE+" added first using add()");
      Button b = locationButton(CARDONE,Color.green,"Green "+CARDONE+" added second using add()");
      Button c = locationButton(CARDTWO,Color.blue,"Blue "+CARDTWO+" added third using add()");
      Button d = locationButton(CARDTWO,Color.yellow,"Yellow "+CARDTWO+" added last using add()");
      Button title = locationButton("Reference layout add()",Color.black,"Layout built in sequence <A>,<B>,<C>,<D> using add()");
      main.add(buildLayout(title, panel1, a, CARDONE, b, CARDONE, c, CARDTWO, d, CARDTWO));

      layout2 = new CardLayout();
      panel2 = new Panel(layout2);
      a = locationButton(CARDONE,Color.red,"Red "+CARDONE+" added first to last place in list using add(-1)");
      b = locationButton(CARDONE,Color.green,"Green "+CARDONE+" added second to last place in list using add(-1)");
      c = locationButton(CARDTWO,Color.blue,"Blue "+CARDTWO+" added third to last place in list using add(-1)");
      d = locationButton(CARDTWO,Color.yellow,"Yellow "+CARDTWO+" added last to last place in list using add()");
      title = locationButton("positioned default by add(-1)",Color.black,"Components added in sequence <A>,<B>,<C>,<D> using add(-1)");
      main.add(buildLayout(title, panel2, a, CARDONE, -1, b, CARDONE, -1, c, CARDTWO, -1, d, CARDTWO, -1));

      layout3 = new CardLayout();
      panel3 = new Panel(layout3);
      a = locationButton(CARDONE,Color.red,"Red "+CARDONE+" added last using add()");
      b = locationButton(CARDONE,Color.green,"Green "+CARDONE+" added third using add()");
      c = locationButton(CARDTWO,Color.blue,"Blue "+CARDONE+" added second using add()");
      d = locationButton(CARDTWO,Color.yellow,"Yellow "+CARDTWO+" added first using add()");
      title = locationButton("Inverse layout",Color.black,"Layout built in sequence <D>,<C>,<B>,<A> using add()");
      main.add(buildLayout(title, panel3, d, CARDTWO, c, CARDTWO, b, CARDONE, a, CARDONE));

      layout4 = new CardLayout();
      panel4 = new Panel(layout4);
      a = locationButton(CARDONE,Color.red,"Red "+CARDONE+" added first using add(0)");
      b = locationButton(CARDONE,Color.green,"Green "+CARDONE+" added second to first place in list using add(0)");
      c = locationButton(CARDTWO,Color.blue,"Blue "+CARDTWO+" added third to first place in list using add(0)");
      d = locationButton(CARDTWO,Color.yellow,"Yellow "+CARDTWO+" added fourth to first place in list using add(0)");
      title = locationButton("Positioned first by add(0)",Color.black,"Components added in sequence <A>,<B>,<C>,<D>/new added first in list using add(0)");
      main.add(buildLayout(title, panel4, a, CARDONE, 0, b, CARDONE, 0, c, CARDTWO, 0, d, CARDTWO, 0));

      layout5 = new CardLayout();
      panel5 = new Panel(layout5);
      a = locationButton(CARDONE,Color.red,"Red "+CARDONE+" added first using add()");
      b = locationButton(CARDONE,Color.green,"Green "+CARDONE+" added second using add()");
      c = locationButton(CARDTWO,Color.blue,"Blue "+CARDTWO+" added fourth using add()");
      d = locationButton(CARDTWO,Color.yellow,"Yellow "+CARDTWO+" added third by add() sequence");
      title = locationButton("<A>,<B>,<D>,<C> by adding order",Color.black,"Layout built in sequence <A>,<B>,<D>,<C> using add()");
      main.add(buildLayout(title, panel5, a, CARDONE, b, CARDONE, d, CARDTWO, c, CARDTWO));

      layout6 = new CardLayout();
      panel6 = new Panel(layout6);
      a = locationButton(CARDONE,Color.red,"Red "+CARDONE+" added first to first place in list using add(0)");
      b = locationButton(CARDONE,Color.green,"Green "+CARDONE+" added second to second place in list using add(1)");
      c = locationButton(CARDTWO,Color.blue,"Blue "+CARDTWO+" added third to third in list using add(2), later moved up by <D>");
      d = locationButton(CARDTWO,Color.yellow,"Yellow "+CARDTWO+" deliberately added fourth to third place in list using add(2)");
      title = locationButton("<D> third by add(2)",Color.black,"Components added in sequence <A>,<B>,<C>,<D> using add(2) to set <D> third in list");
      main.add(buildLayout(title, panel6, a, CARDONE, 0, b, CARDONE, 1, c, CARDTWO, 2, d, CARDTWO, 2));
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
    current = 1;
    showing.setText(" "+current+" of "+panel1.getComponentCount());
  }


  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    String command = evt.getActionCommand();
    int count = panel1.getComponentCount();

    if(display.getItemCount()>40) {
      display.removeAll();
    }
    if(src==next){
      if(current < count){
        layout1.next(panel1);
        layout2.next(panel2);
        layout3.next(panel3);
        layout4.next(panel4);
        layout5.next(panel5);
        layout6.next(panel6);
        current++;
        showing.setText(" "+current+" of "+count);
        display.add("CardLayout.next() to next panel no."+current,0);

        if(current == count){
          next.setLabel("First panel");
        }
      }
      else {
        layout1.first(panel1);
        layout2.first(panel2);
        layout3.first(panel3);
        layout4.first(panel4);
        layout5.first(panel5);
        layout6.first(panel6);
        current = 1;
        showing.setText(" "+current+" of "+count);
        display.add("CardLayout.first() to first panel",0);
        //always
          next.setLabel("Next panel");
      }
    }
    else if(command.equals(CARDONE) || command.equals(CARDTWO)){
      layout1.show(panel1,command);
      layout2.show(panel2,command);
      layout3.show(panel3,command);
      layout4.show(panel4,command);
      layout5.show(panel5,command);
      layout6.show(panel6,command);
      showing.setText(command+" of "+count);
      display.add("Showing panel "+command,0);
      current = count;
      next.setLabel("first panel");
    }
    else { // simple button
      display.add(command,0);
    }
  }

    /** VTE help */
  public String getHelpText(){
    return "A test to verify Rudolph's implementation of the Container.add(Component, name, position) form in a CardLayout :\n\n"+
           "This time, the cardlayout will store elements under a different position, but under the same name. This will still allow"+
           " you to access all elements added by using the CardLayout.first(), -.next(), -.previous() and -.last() functions,"+
           " but when calling the elements by name the layout will always return the element that was added first.\n"+
           "Again, this test shows three rows of two panels. Again to each of them are added our four elements <A>,<B>,<C> and.<D>\n"+
           "<A> and <B> are added under the name of ["+CARDONE+"] , <C> and <D> under the name of["+CARDTWO+"]\n"+
           "Using the ["+CARDONE+"]/["+CARDTWO+"] buttons, you can directly access the (first) element ["+CARDONE+"] or ["+CARDTWO+"]"+
           " by means of CardLayout.show(element name)\n\n"+
           "Using the <first/next > button, you can loop through each elements panels in order of appearance\n\n"+
           "As in all other position tests, the elements are added in order <A>+<B>+<C>+<D>, order <D>+<C>+<B>+<A> and order <A>+<B>+<D>+<C>"+
           "respectingly, the left side by changing the order in which the elements are added to the list (using add(element, name))"+
           ", the right one by specifying the order in the add(element, name, position) form\n\n"+
           "ADDING AN ELEMENT UNDER A NAME, CARDLAYOUT LOOKS AT THE LAST ELEMENT ARRIVING, NOT AT ITS POSITION IN LIST"+
           " LIKE THIS, ["+CARDONE+"] WILL SHOW THE GREEN PANEL IN ALL SITUATIONS BUT 3 WHICH WILL SHOW THE RED ONE\n"+
           "["+CARDTWO+"] WILL SHOW THE YELLOW PANEL IN ALL BUT 3 AND 5 WHICH SHOW THE BLUE ONE\n"+
           "USING THE NEXT BUTTON, FOR ALL OF THE ROWS, THE LEFT PANELS AND THEIR CORRESPONDING RIGHT PANELS SHOULD LOOK EXACTLY THE SAME";
  }
}
