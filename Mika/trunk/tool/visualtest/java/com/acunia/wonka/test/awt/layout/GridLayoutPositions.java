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

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;


public class GridLayoutPositions extends VisualTestImpl implements ActionListener {

  /** Variables*/
  protected List display;
  protected final static Color SMOKE = new Color(64,64,127);
  protected final static Color RUST = new Color(127,64,64);
  protected final static Color WOOD = new Color(32,64,32);

  /** constructor */
  public GridLayoutPositions() {
    setBackground(Color.black);
    setForeground(Color.yellow);
    setLayout(new java.awt.BorderLayout());
    Panel main = new Panel(new java.awt.GridLayout(3,2));
      Button a = locationButton("<A>",Color.red,"element <A> added first by position of add()");
      Button b = locationButton("<B>",Color.green,"element <B> added second by position of add()");
      Button c = locationButton("<C>",Color.blue,"element <C> added third by position of add()");
      Button d = locationButton("<D>",Color.yellow,"element <D> added last by position of add()");
      Button title = locationButton("Reference layout add()",Color.black,"Layout built in sequence <A>,<B>,<C>,<D> using add()");
      main.add(buildLayout(title,SMOKE, new java.awt.GridLayout(1,4), a, b, c, d));

      a = locationButton("<A>",Color.red,"element <A> added first to last place in list using add(-1)");
      b = locationButton("<B>",Color.green,"element <B> added second to last place in list using add(-1)");
      c = locationButton("<C>",Color.blue,"element <C> added third to last place in list using add(-1)");
      d = locationButton("<D>",Color.yellow,"element <D> added last to last place in list using add()");
      title = locationButton("positioned default by add(-1)",Color.black,"Components added in sequence <A>,<B>,<C>,<D> using add(-1)");
      main.add(buildLayout(title,RUST, new java.awt.GridLayout(1,4),a, -1, b, -1, c, -1, d, -1));

      a = locationButton("<A>",Color.red,"element <A> added last by position of add()");
      b = locationButton("<B>",Color.green,"element <B> added third by position of add()");
      c = locationButton("<C>",Color.blue,"element <C> added second by position of add()");
      d = locationButton("<D>",Color.yellow,"element <D> added first by position of add()");
      title = locationButton("Inverse layout",Color.black,"Layout built in sequence <D>,<C>,<B>,<A> using add()");
      main.add(buildLayout(title,RUST, new java.awt.GridLayout(1,4),d, c, b, a));

      a = locationButton("<A>",Color.red,"element <A> added first using add(0)");
      b = locationButton("<B>",Color.green,"element <B> added second  to (then) first place in list using add(0)");
      c = locationButton("<C>",Color.blue,"element <C> added third to (then) first place in list using add(0)");
      d = locationButton("<D>",Color.yellow,"element <D> added fourth to (then) first place in list using add(0)");
      title = locationButton("Positioned first by add(0)",Color.black,"Components added in sequence <A>,<B>,<C>,<D>/new added first in list using add(0)");
      main.add(buildLayout(title,SMOKE, new java.awt.GridLayout(1,4),a, 0, b, 0, c, 0, d, 0));

      a = locationButton("<A>",Color.red,"element <A> added first by position of add()");
      b = locationButton("<B>",Color.green,"element <B> added second by position of add()");
      c = locationButton("<C>",Color.blue,"element <C> added fourth by position of add()");
      d = locationButton("<D>",Color.yellow,"element <D> added third by add() sequence");
      title = locationButton("<A>,<B>,<D>,<C> by adding order",Color.black,"Layout built in sequence <A>,<B>,<D>,<C> using add()");
      main.add(buildLayout(title,SMOKE, new java.awt.GridLayout(1,4),a, b, d, c));

      a = locationButton("<A>",Color.red,"element <A> added first to first place in list using add(0)");
      b = locationButton("<B>",Color.green,"element <B> added second to second place in list using add(1)");
      c = locationButton("<C>",Color.blue,"element <C> added third to third place using add(2), moved up when adding <D>");
      d = locationButton("<D>",Color.yellow,"element <D> added fourth to third place in list using add(2)");
      title = locationButton("<D> third by add(2)",Color.black,"Components added in sequence <A>,<B>,<C>,<D> using add(2) to set <D> third in list");
      main.add(buildLayout(title,RUST, new java.awt.GridLayout(1,4),a, 0, b, 1, c, 2, d, 2));
    add(main, java.awt.BorderLayout.CENTER);

    display = new List(3,false);
    display.setForeground(Color.white);
    display.add("Click on a button to see its construction");
    add(display, java.awt.BorderLayout.SOUTH);
  }

  protected Panel buildLayout(Button title, Color background, LayoutManager panellayout,
                             Button first, Button second, Button third, Button fourth) {
    Panel bigpicture = new Panel(new java.awt.BorderLayout());
      bigpicture.add(title,java.awt.BorderLayout.NORTH);
      Panel layout = new Panel(panellayout);
        layout.setBackground(background);
        layout.setForeground(Color.black);
        layout.add(first);
        layout.add(second);
        layout.add(third);
        layout.add(fourth);
      bigpicture.add(layout, java.awt.BorderLayout.CENTER);
    return bigpicture;
  }

  protected Panel buildLayout(Button title, Color background, LayoutManager panellayout,
                      Button first, int firstpos, Button second, int secondpos, Button third, int thirdpos, Button fourth, int fourthpos) {
    Panel bigpicture = new Panel(new java.awt.BorderLayout());
      bigpicture.add(title,java.awt.BorderLayout.NORTH);
      Panel layout = new Panel(panellayout);
        layout.setBackground(background);
        layout.setForeground(Color.black);
        layout.add(first, firstpos);
        layout.add(second, secondpos);
        layout.add(third, thirdpos);
        layout.add(fourth, fourthpos);
      bigpicture.add(layout, java.awt.BorderLayout.CENTER);
    return bigpicture;
  }

  protected Button locationButton(String text, String addmode){
      Button b = new Button(text);
      b.setActionCommand(addmode);
      b.addActionListener(this);
      return b;
  }
  protected Button locationButton(String text, Color background, String addmode){
      Button b = locationButton(text, addmode);
      b.setBackground(background);
      return b;
  }

  public void actionPerformed(ActionEvent evt) {
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    display.add(evt.getActionCommand(),0);
  }

  public String getHelpText(){
    return "A test to verify Rudolph's implementation of the Container.add(Component, position) form in a GridLayout :\n\n"+
           "The form add(component) by default adds a component on the back of the component list for the specific layout,"+
           "The form add(component, pos) adds the component at the desired position in the internal component list."+
           " As the GridLayout manager uses its internal list of elements to calculate which element to come on which position,"+
           " you can change the layout consideranbly by specifying a distinctive position in the add(Component, position) form"+
           " just as you can define the layout by the order the add(component) commands are received\n\n"+
           "This test shows three rows of two panels. All panels try to place four elements <A> to <D> in a 1-row 4-column GridLayout."+
           " Of each row, the left panel will specify the layout by using four calls to add(element) in a distinctive order."+
           " The rigth panel will add the elements in fixed order <A>,<B>,<C> and <D> but use the form add(element, position) to specify"+
           " a distinctive position in the internal layout list \n"+
           "=> the topleft panel will add the elements in order <A>,<B>,<C> and <D> using the simple add(element),"+
           " the topright panel will also add the elements in order <A>,<B>,<C> and <D>, using the form add(elements,-1) to add all elements"+
           " to the end of the list by default.\n\n"+
           "=> the center left panel will add the elements in inverse order <D>,<C>,<B>,<A> (using add(element) ),"+
           " the center right panel will also add the elements in order <A>,<B>,<C> and <D>, but use the form add(elements,0)"+
           " to add every element to start of the list, thus creating an internal list <D>,<C>,<B>,<A>.\n\n"+
           "=> the bottomleft panel will add the elements in order <A>,<B>,<D>,<C> (using add(element) ), the bottomright panel"+
           " will create an internal list of that form by adding the elements in order <A>+<B>+<C>+<D>, using add(element, positon) "+
           " with positions 0,1 and 2 for the <A>,<B> and <C>-elements and 2 to add the <D>-element on third position,"+
           " (moving the <C> element one position up)\n\n"+
           "IN ALL OF THESE CASES YOU SHOULD SEE THE RIGHT SIDE PANELS DISPLAYING COMPLETELY THE SAME IMAGES AS THEIR CORRESPONDING LEFT ONES";
  }
}
