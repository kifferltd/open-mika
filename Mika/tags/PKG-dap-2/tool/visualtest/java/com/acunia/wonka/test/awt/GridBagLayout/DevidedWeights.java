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
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;

public class DevidedWeights extends GridBagWeights {
  /****************************************************************/
  /** variables
  */
  /****************************************************************/
  /** constructor
  * we'll specify 4 elements red,green,blue and black each spanning 2 rows or 2 columns in a 3x3 GridbagLayout
  * the center is occupyed by a fifth element:purple
  * The elements are laid out in a way :
  *  1 1 2
  *  3 5 2
  *  3 4 4
  */
  public DevidedWeights() {
    super();
    setLayout(new BorderLayout());
    setBackground(Color.yellow);



    // left panel with command buttons
    add(buildButtonPanel(), BorderLayout.EAST);

    // center test panel with gridbag layout
    Panel mid = new Panel(new BorderLayout());
      mid.add(buildTopPanel(), BorderLayout.NORTH);

      GridBagLayout gbl = new GridBagLayout();
      GridBagConstraints gbc = new GridBagConstraints();
      testPanel = new Panel(gbl);
      testPanel.setBackground(new Color(200,200,120) );
        gbc.weightx=1.0;
        gbc.weighty=1.0;
        gbc.fill = GridBagConstraints.BOTH;

        //first red
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.gridwidth=2;
        gbc.gridheight=1;
        currentElement = new FillComponent("RED:(0,0) to (1,0)", Color.white, new Color(96,32,32));
        testPanel.add(currentElement);
        gbl.setConstraints(currentElement, gbc);

        //second blue
        gbc.gridx=2;
        gbc.gridy=0;
        gbc.gridwidth=1;
        gbc.gridheight=2;
        currentElement = new FillComponent("BLUE:(2,0) to (2,1)", Color.white, new Color(32,32,96));
        testPanel.add(currentElement);
        gbl.setConstraints(currentElement, gbc);

        //third green
        gbc.gridx=0;
        gbc.gridy=1;
        gbc.gridwidth=1;
        gbc.gridheight=2;
        currentElement = new FillComponent("GREEN:(0,1)to(0,2)", Color.white, new Color(32,96,32));
        testPanel.add(currentElement);
        gbl.setConstraints(currentElement, gbc);

        //fourth black
        gbc.gridx=1;
        gbc.gridy=2;
        gbc.gridwidth=2;
        gbc.gridheight=1;
        currentElement = new FillComponent("BLACK:(1,2)to(2,2)", Color.white, new Color(32,32,32));
        testPanel.add(currentElement);
        gbl.setConstraints(currentElement, gbc);

        //fifth purple
        gbc.gridx=1;
        gbc.gridy=1;
        gbc.gridwidth=1;
        gbc.gridheight=1;
        currentElement = new FillComponent("PURPLE:(1,1)", Color.white, new Color(80,32,80));
        testPanel.add(currentElement);
        gbl.setConstraints(currentElement, gbc);

      mid.add(testPanel, BorderLayout.CENTER);
      mid.add(buildWeightsPanel(), BorderLayout.SOUTH);

    add(mid, BorderLayout.CENTER);

    displayData(currentElement);
    currentElement.inverse(true);
  }


  /****************************************************************/
  /** VTE help*/
  public String getHelpText(){
    return "A GridBagLayout test on the GridBag size calculation by weight:\n" +
            "The screen shows a central panel five elements ranged using a GridBagLayout. Each element has a central square of"+
            ELEMENTSIZE+" x "+ELEMENTSIZE+" pixels that shows its preferred size. \n"+
            "You can click an element to select it. The selected element will change color and displayed in the right panel."+
            " will appear its name, and weightX, weightY settings\n"+
            "Using the (+) and (-) buttons on the left panel you can change the preferred weights of the selected element."+
            " This forces the layout to change size. \n\n"+
            "ITEMS TO TEST:\n\n"+
            "-> Division of excess grid weights: in the bottom area area watch the weightX and weightY of the selected element"+
            " For an element spanning two rows or two columns, check how the weight of that element is devided amongst that rows/panels.\n"+
            "-> Division of the free widths: also regard the total width of each gridbag column and the 'free' width after"+
            " subtracting the (uniform) element width and check that the total leftover width is devided amongst the columns"+
            " proportional to their column weightX.\n"+
            "-> Division of the free heights: just like above regard the total heights and free heights of each row and make sure"+
            " that the leftover height is devided amongst the rows proportional to their row weightY.";
  }

  public void start(java.awt.Panel p, boolean b) {
      currentElement.repaint();
      displayData(currentElement);
  }

  public void stop(java.awt.Panel p) {
  }
}
