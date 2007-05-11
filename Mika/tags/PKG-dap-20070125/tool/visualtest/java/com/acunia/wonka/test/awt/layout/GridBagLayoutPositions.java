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
import java.awt.GridBagConstraints;
import java.awt.LayoutManager2;
import java.awt.List;
import java.awt.Panel;


public class GridBagLayoutPositions extends GridLayoutPositions {

  /** Variables*/
  //protected List display;
  //protected final static Color SMOKE = new Color(64,64,127);
  protected final static Color GREEN = new Color(64,127,64);

  /** constructor */
  public GridBagLayoutPositions() {
    setBackground(Color.black);
    setForeground(Color.yellow);
    setLayout(new java.awt.BorderLayout());

    Panel main = new Panel(new java.awt.GridLayout(3,2));
      GridBagConstraints ga = new GridBagConstraints();
      ga.fill = GridBagConstraints.BOTH;
      ga.weightx = 0.0;
      ga.gridx = 0;
      ga.gridy = 0;
      ga.gridwidth = 1;
      ga.gridheight = 1;

      GridBagConstraints gb = new GridBagConstraints();
      gb.fill = GridBagConstraints.BOTH;
      gb.weightx = 1.0;
      gb.gridx = 1;
      gb.gridy = 1;
      gb.gridwidth = 1;
      gb.gridheight = 1;

      GridBagConstraints gc = new GridBagConstraints();
      gc.fill = GridBagConstraints.BOTH;
      gc.weightx = 2.0;
      gc.gridx = 2;
      gc.gridy = 0;
      gc.gridwidth = 1;
      gc.gridheight = 1;

      GridBagConstraints gd = new GridBagConstraints();
      gd.fill = GridBagConstraints.BOTH;
      gd.weightx = 3.0;
      gd.gridx = 3;
      gd.gridy = 1;
      gc.gridwidth = 1;
      gd.gridheight = 1;

      Button a = locationButton("<A>",Color.red,"GridBag cell (0,0), added first using add()");
      Button b = locationButton("<B>",Color.green,"GridBag cell (1,1), added second using add()");
      Button c = locationButton("<C>",Color.blue,"GridBag cell (2,0), added third using add()");
      Button d = locationButton("<D>",Color.yellow,"GridBag cell (3,1), added last using add()");
      Button title = locationButton("Reference layout add()",Color.black,"Layout built in sequence <A>,<B>,<C>,<D> using add()");
      main.add(buildLayout(title,SMOKE, new java.awt.GridBagLayout(), a, ga, b, gb, c, gc, d, gd));

      a = locationButton("<A>",Color.red,"GridBag cell (0,0), added first to last place in list using add(-1)");
      b = locationButton("<B>",Color.green,"GridBag cell (1,1), added second to last place in list using add(-1)");
      c = locationButton("<C>",Color.blue,"GridBag cell (2,0), added third to last place in list using add(-1)");
      d = locationButton("<D>",Color.yellow,"GridBag cell (3,1), added last to last place in list using add()");
      title = locationButton("positioned default by add(-1)",Color.black,"Components added in sequence <A>,<B>,<C>,<D> using add(-1)");
      main.add(buildLayout(title,RUST, new java.awt.GridBagLayout(),a, ga, -1, b, gb, -1, c, gc, -1, d, gd, -1));

      a = locationButton("<A>",Color.red,"GridBag cell (0,0), added last using add()");
      b = locationButton("<B>",Color.green,"GridBag cell (1,1), added third using add()");
      c = locationButton("<C>",Color.blue,"GridBag cell (2,0), added second using add()");
      d = locationButton("<D>",Color.yellow,"GridBag cell (3,1), added first using add()");
      title = locationButton("Inverse layout",Color.black,"Layout built in sequence <D>,<C>,<B>,<A> using add()");
      main.add(buildLayout(title,RUST, new java.awt.GridBagLayout(),d, gd, c, gc, b, gb, a, ga));

      a = locationButton("<A>",Color.red,"GridBag cell (0,0), added first using add(0)");
      b = locationButton("<B>",Color.green,"GridBag cell (1,1), added second to first place in list using add(0)");
      c = locationButton("<C>",Color.blue,"GridBag cell (2,0), added third to first place in list using add(0)");
      d = locationButton("<D>",Color.yellow,"GridBag cell (3,1), added fourth to first place in list using add(0)");
      title = locationButton("Positioned first by add(0)",Color.black,"Components added in sequence <A>,<B>,<C>,<D>/new added first in list using add(0)");
      main.add(buildLayout(title,SMOKE, new java.awt.GridBagLayout(),a, ga, 0, b, gb, 0, c, gc, 0, d, gd, 0));

      a = locationButton("<A>",Color.red,"GridBag cell (0,0), added first using add()");
      b = locationButton("<B>",Color.green,"GridBag cell (1,1), added second using add()");
      c = locationButton("<C>",Color.blue,"GridBag cell (2,0), added fourth using add()");
      d = locationButton("<D>",Color.yellow,"GridBag cell (3,1), added third by add() sequence");
      title = locationButton("<A>,<B>,<D>,<C> by adding order",Color.black,"Layout built in sequence <A>,<B>,<D>,<C> using add()");
      main.add(buildLayout(title,SMOKE, new java.awt.GridBagLayout(),a, ga, b, gb, d, gd, c, gc));

      a = locationButton("<A>",Color.red,"GridBag cell (0,0), added first to first place in list using add(0)");
      b = locationButton("<B>",Color.green,"GridBag cell (1,1), added second to second place in list using add(1)");
      c = locationButton("<C>",Color.blue,"GridBag cell (2,0), added third to to third place in list by add(2), moved up by <D>");
      d = locationButton("<D>",Color.yellow,"GridBag cell (3,1), added fourth to third place in list using add(2)");
      title = locationButton("<D> third by add(2)",Color.black,"Components added in sequence <A>,<B>,<C>,<D> using add(2) to set <D> third in list");
      main.add(buildLayout(title,RUST, new java.awt.GridBagLayout(),a, ga, 0, b, gb, 1, c, gc, 2, d, gd, 2));
    add(main, java.awt.BorderLayout.CENTER);

    display = new List(2,false);
    display.setForeground(Color.white);
    display.add("Click on an item to get more info");
    add(display, java.awt.BorderLayout.SOUTH);

  }

  /** override buildLayout to enable element name constraints */
  protected Panel buildLayout(Button title, Color background, LayoutManager2 panellayout,
                             Button first, Object firstconstraints,
                              Button second, Object secondconstraints,
                               Button third, Object thirdconstraints,
                                 Button fourth, Object fourthconstraints) {
    Panel bigpicture = new Panel(new java.awt.BorderLayout());
      bigpicture.add(title,java.awt.BorderLayout.NORTH);
      Panel layout = new Panel(panellayout);
        layout.setBackground(background);
        layout.setForeground(Color.black);
        layout.add(first, firstconstraints);
        layout.add(second, secondconstraints);
        layout.add(third, thirdconstraints);
        layout.add(fourth, fourthconstraints);
      bigpicture.add(layout, java.awt.BorderLayout.CENTER);
    return bigpicture;
  }

  /** override buildLayout to enable element name constraints */
  protected Panel buildLayout(Button title, Color background, LayoutManager2 panellayout,
                             Button first, Object firstconstraints, int firstpos,
                              Button second, Object secondconstraints, int secondpos,
                               Button third, Object thirdconstraints, int thirdpos,
                                 Button fourth, Object fourthconstraints, int fourthpos) {
    Panel bigpicture = new Panel(new java.awt.BorderLayout());
      bigpicture.add(title,java.awt.BorderLayout.NORTH);
      Panel layout = new Panel(panellayout);
        layout.setBackground(background);
        layout.setForeground(Color.black);
        layout.add(first, firstconstraints, firstpos);
        layout.add(second, secondconstraints, secondpos);
        layout.add(third, thirdconstraints, thirdpos);
        layout.add(fourth, fourthconstraints, fourthpos);
      bigpicture.add(layout, java.awt.BorderLayout.CENTER);
    return bigpicture;
  }

  public String getHelpText(){
    return "A test to verify Rudolph's implementation of the Container.add(Component, GridBagConstraints, position) form in a GridBagLayout :\n\n"+
           "As the aim of the GridBagLayout is to specify a grid and to place a component on a distinctive place in that grid,"+
           " the order in which the elements are placed on a call to update or validate should not mather.\n"+
           "This test shows three rows of two panels. All panels try to place four elements <A> to <D> each inside a distinctive cell"+
           " in the layout grid (Positions (0,0), (1,1), (2,0) and (3.1) respectingly )\n"+
           "As in all other position tests, the elements are added in order <A>+<B>+<C>+<D>, order <D>+<C>+<B>+<A> and order <A>+<B>+<D>+<C>"+
           "respectingly, the left side by changing the order in which the elements are added to the list (using add(element, constraints))"+
           ", the right one by specifying the order in the add(element, constraints, order) form"+
           "AS THE GRIDBAGLAYOUT IN THIS FORM DOES SIMPLY DISPLAYS EVERY ELEMENT ON ITS F.I.X.E.D PLACE, IT DOES N.O.T CARE"+
           " ABOUT ORDER OF DISPLAY THEREFORE THE DISPLAY IN ALL SIX PANELS SHOULD LOOK \n"+
           "   EXACTLY THE SAME"+
           "   =====================";
  }
}
