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
import java.awt.Color;
import java.awt.List;
import java.awt.Panel;


public class BorderLayoutPositions extends GridBagLayoutPositions {

  /** Variables*/
  //protected List display;
  //protected final static Color SMOKE = new Color(64,64,127);
  //protected final static Color RUST = new Co.....
  //protected final static Color WOOD .......

  /** constructor */
  public BorderLayoutPositions() {
    setBackground(Color.black);
    setForeground(Color.yellow);
    setLayout(new java.awt.BorderLayout());

    Panel main = new Panel(new java.awt.GridLayout(3,2));
      Button a = locationButton("<A>(NORTH)",Color.red,"BorderLayout.NORTH added first using add()");
      Button b = locationButton("<B>(NORTH)",Color.green,"BorderLayout.NORTH, added second using add()");
      Button c = locationButton("<C>(EAST)",Color.blue,"BorderLayout.EAST, added third using add()");
      Button d = locationButton("<D>(EAST)",Color.yellow,"BorderLayout.EAST, added last using add()");
      Button title = locationButton("Reference layout add()",Color.black,"Layout built in sequence <A>,<B>,<C>,<D> using add()");
      main.add(buildLayout(title,SMOKE, new java.awt.BorderLayout(),
                                a, java.awt.BorderLayout.NORTH, b, java.awt.BorderLayout.NORTH,
                                c, java.awt.BorderLayout.EAST, d, java.awt.BorderLayout.EAST));

      a = locationButton("<A>(NORTH)",Color.red,"BorderLayout.NORTH, added first to last place in list using add(-1)");
      b = locationButton("<B>(NORTH)",Color.green,"BorderLayout.NORTH, added second to last place in list using add(-1)");
      c = locationButton("<C>(EAST)",Color.blue,"BorderLayout.EAST, added third to last place in list using add(-1)");
      d = locationButton("<D>(EAST)",Color.yellow,"BorderLayout.EAST, added last to last place in list using add()");
      title = locationButton("positioned default by add(-1)",Color.black,"Components added in sequence <A>,<B>,<C>,<D> using add(-1)");
      main.add(buildLayout(title,RUST, new java.awt.BorderLayout(),
                                a, java.awt.BorderLayout.NORTH, -1, b, java.awt.BorderLayout.NORTH, -1,
                                c, java.awt.BorderLayout.EAST, -1, d, java.awt.BorderLayout.EAST, -1));

      a = locationButton("<A>(NORTH)",Color.red,"BorderLayout.NORTH, added last using add()");
      b = locationButton("<B>(NORTH)",Color.green,"BorderLayout.NORTH, added third using add()");
      c = locationButton("<C>(EAST)",Color.blue,"BorderLayout.EAST, added second using add()");
      d = locationButton("<D>(EAST)",Color.yellow,"BorderLayout.EAST, added first using add()");
      title = locationButton("Inverse layout",Color.black,"Layout built in sequence <D>,<C>,<B>,<A> using add()");
      main.add(buildLayout(title,RUST, new java.awt.BorderLayout(),
                                d, java.awt.BorderLayout.EAST, c, java.awt.BorderLayout.EAST,
                                b, java.awt.BorderLayout.NORTH, a, java.awt.BorderLayout.NORTH));

      a = locationButton("<A>(NORTH)",Color.red,"BorderLayout.NORTH, added first using add(0)");
      b = locationButton("<B>(NORTH)",Color.green,"BorderLayout.NORTH, added second to first place in list using add(0)");
      c = locationButton("<C>(EAST)",Color.blue,"BorderLayout.EAST, added third to first place in list using add(0)");
      d = locationButton("<D>(EAST)",Color.yellow,"BorderLayout.EAST, added fourth to first place in list using add(0)");
      title = locationButton("Positioned first by add(0)",Color.black,"Components added in sequence <A>,<B>,<C>,<D>/new added first in list using add(0)");
      main.add(buildLayout(title,SMOKE, new java.awt.BorderLayout(),
                                a, java.awt.BorderLayout.NORTH, 0, b, java.awt.BorderLayout.NORTH, 0,
                                c, java.awt.BorderLayout.EAST, 0, d, java.awt.BorderLayout.EAST, 0));

      a = locationButton("<A>(NORTH)",Color.red,"BorderLayout.NORTH, added first using add()");
      b = locationButton("<B>(NORTH)",Color.green,"BorderLayout.NORTH, added second using add()");
      c = locationButton("<C>(EAST)",Color.blue,"BorderLayout.EAST, added fourth using add()");
      d = locationButton("<D>(EAST)",Color.yellow,"BorderLayout.EAST, added third by add() sequence");
      title = locationButton("<A>,<B>,<D>,<C> by adding order",Color.black,"Layout built in sequence <A>,<B>,<D>,<C> using add()");
      main.add(buildLayout(title,SMOKE, new java.awt.BorderLayout(),
                                a, java.awt.BorderLayout.NORTH, b, java.awt.BorderLayout.NORTH,
                                d, java.awt.BorderLayout.EAST, c, java.awt.BorderLayout.EAST));

      a = locationButton("<A>(NORTH)",Color.red,"BorderLayout.NORTH, added first to first place in list using add(0)");
      b = locationButton("<B>(NORTH)",Color.green,"BorderLayout.NORTH, added second to last place in list using add(-1)");
      c = locationButton("<C>(EAST)",Color.blue,"BorderLayout.EAST, added third to to last place in list first in row using add(-1)");
      d = locationButton("<D>(EAST)",Color.yellow,"BorderLayout.EAST, added fourth to third place in list using add(2)");
      title = locationButton("<D> third by add(2)",Color.black,"Components added in sequence <A>,<B>,<C>,<D> using add(2) to set <D> third in list");
      main.add(buildLayout(title,RUST, new java.awt.BorderLayout(),
                                a, java.awt.BorderLayout.NORTH, 0, b, java.awt.BorderLayout.NORTH, -1,
                                c, java.awt.BorderLayout.EAST, -1, d, java.awt.BorderLayout.EAST, 2));
    add(main, java.awt.BorderLayout.CENTER);

    display = new List(2,false);
    display.setForeground(Color.white);
    display.add("Click on an item to get more info");
    add(display, java.awt.BorderLayout.SOUTH);

  }

  public String getHelpText(){
    return "A test to verify Rudolph's implementation of the Container.add(Component, GridBagConstraints, position) form in a BorderLayout :\n\n"+
           "As the aim of the BorderLayout is to glue a component to one of the component's borders, this layout only knows five elements:"+
           "NORTH, SOUTH, EAST, WEST and CENTER. A new element can only be one of these and will be displayed on this element's location\n"+
           "If a new element is added to a location for which an element already exists, it replaces that element\n\n"+
           "This test shows three rows of two panels. All panels try to place their elements <A> and <B> to the NORTH location and their"+
           " elements <C> and <D> in the EAST location. As always two elements will be assigned to the same location, the last one added"+
           "throws the first out of the layout and takes its place\n"+
           "As in all other position tests, the elements are added in order <A>+<B>+<C>+<D>, order <D>+<C>+<B>+<A> and order <A>+<B>+<D>+<C>"+
           "respectingly, the left side by changing the order in which the elements are added to the list (using add(element, constraints))"+
           ", the right one by specifying the order in the add(element, constraints, order) form\n\n"+
           "AS THE BORDERLAYOUT DISREGARDS THE POSITION IN FAVOR OF THE ORDER OF ADDING, THE TOPLEFT AND ALL RIGHT LAYOUTS SHOULD SHOW THE ELEMENTS"+
           "<B> and <D> BECAUSE THEY WERE ADDED OVER THE PREVIOUS <A> AND <C>. THE MIDDLE LEFT AND BOTTOM LEFT LAYOUTS SHOULD SHOW THE ELEMENTS"+
           "ADDED LAST: <A>/<C> and <B>/<C> RESPECTINGLY";
  }
}
