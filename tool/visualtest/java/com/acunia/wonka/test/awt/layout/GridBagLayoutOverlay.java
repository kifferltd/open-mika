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
import java.awt.List;
import java.awt.Panel;


public class GridBagLayoutOverlay extends GridBagLayoutPositions {

  /** Variables*/
  //protected List display;
  //protected final static Color SMOKE = new Color(64,64,127);

  /** constructor */
  public GridBagLayoutOverlay() {
    setBackground(Color.black);
    setForeground(Color.yellow);
    setLayout(new java.awt.BorderLayout());

    Panel main = new Panel(new java.awt.GridLayout(3,2));
      GridBagConstraints ga = new GridBagConstraints();
      ga.fill = GridBagConstraints.BOTH;
      ga.gridx = 0;
      ga.gridy = 0;
      ga.gridwidth = 2;
      ga.gridheight = 2;

      GridBagConstraints gb = new GridBagConstraints();
      gb.fill = GridBagConstraints.BOTH;
      gb.gridx = 1;
      gb.gridy = 1;
      gb.gridwidth = 2;
      gb.gridheight = 2;

      GridBagConstraints gc = new GridBagConstraints();
      gc.fill = GridBagConstraints.BOTH;
      gc.gridx = 2;
      gc.gridy = 0;
      gc.gridwidth = 2;
      gc.gridheight = 2;

      GridBagConstraints gd = new GridBagConstraints();
      gd.fill = GridBagConstraints.BOTH;
      gd.gridx = 3;
      gd.gridy = 1;
      gd.gridwidth = 2;
      gd.gridheight = 2;

      Button a = locationButton("<A>",Color.red,"position(0,0)to(1,1), added first using add()");
      Button b = locationButton("<B>",Color.green,"position(1,1)to(2,2), added second using add()");
      Button c = locationButton("<C>",Color.blue,"position(2,0)to(3,1)), added third using add()");
      Button d = locationButton("<D>",Color.yellow,"position(3,1)to(4,2), added last using add()");
      Button title = locationButton("Reference layout add()",Color.black,"Layout built in sequence <A>,<B>,<C>,<D> using add()");
      main.add(buildLayout(title,SMOKE, new java.awt.GridBagLayout(), a, ga, b, gb, c, gc, d, gd));

      a = locationButton("<A>",Color.red,"position(0,0)to(1,1), added first to last place in list using add(-1)");
      b = locationButton("<B>",Color.green,"position(1,1)to(2,2), added second to last place in list using add(-1)");
      c = locationButton("<C>",Color.blue,"position(2,0)to(3,1), added third to last place in list using add(-1)");
      d = locationButton("<D>",Color.yellow,"position(3,1)to(4,2), added last to last place in list using add()");
      title = locationButton("positioned default by add(-1)",Color.black,"Components added in sequence <A>,<B>,<C>,<D> using add(-1)");
      main.add(buildLayout(title,RUST, new java.awt.GridBagLayout(),a, ga, -1, b, gb, -1, c, gc, -1, d, gd, -1));

      a = locationButton("<A>",Color.red,"position(0,0)to(1,1), added last using add()");
      b = locationButton("<B>",Color.green,"position(1,1)to(2,2), added third using add()");
      c = locationButton("<C>",Color.blue,"position(2,0)to(3,1), added second using add()");
      d = locationButton("<D>",Color.yellow,"position(3,1)to(4,2), added first using add()");
      title = locationButton("Inverse layout",Color.black,"Layout built in sequence <D>,<C>,<B>,<A> using add()");
      main.add(buildLayout(title,RUST, new java.awt.GridBagLayout(),d, gd, c, gc, b, gb, a, ga));

      a = locationButton("<A>",Color.red,"position(0,0)to(1,1), added first using add(0)");
      b = locationButton("<B>",Color.green,"position(1,1)to(2,2), added second to first place in list using add(0)");
      c = locationButton("<C>",Color.blue,"position(2,0)to(3,1), added third to first place in list using add(0)");
      d = locationButton("<D>",Color.yellow,"position(3,1)to(4,2), added fourth to first place in list using add(0)");
      title = locationButton("Positioned first by add(0)",Color.black,"Components added in sequence <A>,<B>,<C>,<D> but using add(0) to add to beginning of list");
      main.add(buildLayout(title,SMOKE, new java.awt.GridBagLayout(),a, ga, 0, b, gb, 0, c, gc, 0, d, gd, 0));

      a = locationButton("<A>",Color.red,"position(0,0)to(1,1), added first using add()");
      b = locationButton("<B>",Color.green,"position(1,1)to(2,2), added second using add()");
      c = locationButton("<C>",Color.blue,"position(2,0)to(3,1), added fourth using add()");
      d = locationButton("<D>",Color.yellow,"position(3,1)to(4,2), added third by add() sequence");
      title = locationButton("<A>,<B>,<D>,<C> by adding order",Color.black,"Layout built in sequence <A>,<B>,<D>,<C> using add()");
      main.add(buildLayout(title,SMOKE, new java.awt.GridBagLayout(),a, ga, b, gb, d, gd, c, gc));

      a = locationButton("<A>",Color.red,"position(0,0)to(1,1), added first to first place in list using add(0)");
      b = locationButton("<B>",Color.green,"position(1,1)to(2,2), added second to second place in list using add(1)");
      c = locationButton("<C>",Color.blue,"position(2,0)to(3,1), added third to third in list using add(2), later moved up by <D>");
      d = locationButton("<D>",Color.yellow,"position(3,1)to(4,2), deliberately added fourth to third place in list using add(2)");
      title = locationButton("<D> third by add(2)",Color.black,"Components added in sequence <A>,<B>,<C>,<D> using add(2) to set <D> third in list");
      main.add(buildLayout(title,RUST, new java.awt.GridBagLayout(),a, ga, 0, b, gb, 1, c, gc, 2, d, gd, 2));
    add(main, java.awt.BorderLayout.CENTER);

    display = new List(2,false);
    display.setForeground(Color.white);
    display.add("Click on an item to get more info");
    add(display, java.awt.BorderLayout.SOUTH);

  }

  public String getHelpText(){
    return "A test to verify Rudolph's implementation of the Container.add(Component, GridBagConstraints, position) form in a GridBagLayout\n\n"+
           "Offifially it's considered <as good as a programming error> when you let two or more GridBagLayout elements overlap at least partly"+
           " However, it is still possible and in this case it is up to the virtual engine to decide what to do.\n"+
           "Most often however the engine's GridBagLayout code adopts a policy of <last one sits on top>, so in this case the order"+
           " in which the elements are stored internally becomes important.\n"+
           "Again, this test shows three rows of two panels. Again to each of them are added our four elements <A>,<B>,<C> and.<D>"+
           " at positions of(0,0), (1,1), (2,0) and(3,1) respectingly. This time however all elements are two cells deep and two cells wide"+
           " so that each of them overlaaps another one.\n"+
           "As in all other position tests, the elements are added in order <A>+<B>+<C>+<D>, order <D>+<C>+<B>+<A> and order <A>+<B>+<D>+<C>"+
           "respectingly, the left side by changing the order in which the elements are added to the list (using add(element, constraints))"+
           ", the right one by specifying the order in the add(element, constraints, order) form"+
           "DISCLAIMER: ALTHOUGH IT IS POSSIBLE, OVERLAPPING GRIDS IS N.O.T. ALLOWED BY THE GRIDBAGLAYOUT !!!. THEREFORE THE RESULTS"+
           " OF THIS TEST HAVE NO BINDING MEANING WHATSOEVER, EXCEPT THAT THEY SHOW WHAT Y.O.U.R VIRTUAL ENGINE DOES IN THIS CASE";
  }
}
