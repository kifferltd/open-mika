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
// Created: 2001/03/13

package com.acunia.wonka.test.awt.List;

//import rudolph.*;


public class MultipleListTest extends SingleListTest
{
  public MultipleListTest()
  { super(4,5,true); }
/**********************************************************************************************************************************/
/**
*	Panel, title, help text, log
**/
  public java.awt.Panel getPanel() {
//    Panel p = new Panel();
  //  p.add(this);
    //return p;
 	 	return this;
 	}

  public String getTitle(){
    return "ListTest : multiple selections";
  }

  public String getHelpText(){
    String help =  "A test on list behavior for a multiple element selection list.\n";
    help += "The test draws three lists: a red one, a  blue one and a green one on different fields of a Borderlayout";
    help += " It also gives you three buttons that enable you to copy elements from one list to another\n";
    help += "\n Items to test:\n";
    help += "=> Minimum visible elements: The red list is defined with a minimum of 4 visible items, being drawn in the BorderLayout.NORTH";
    help += " field it should display exactly four of its five elements and a vertical scrollbar to scroll to the fifth\n";
    help += "=> Minimum width: The blue list is being drawn in the BorderLayout.WEST field. All of its items should be completely visible";
    help += "(with a default font big enough, this makes the remaining BroderLayout.CENTER field so small that the items";
    help += " of the green list can not displayed completely and this list is drawn with a vertical scrollbar)\n";
    help += " => Selecting: All lists allow multiple selection. An item of a list can be selected by clicking it. It's selected state";
    help += " should be cleasly visible. Clicking a selected item deselects it again \n";
    help += " More then one elements per list can be selected. Note the different in color for an element that is selected but not clicked \n";
    help += " => Moving between lists: clicking the red-to-blue button removes all selected items of the red list from that list";
    help += " and adds them to the blue one. Likewise the blue-to-green and green-to-red buttons copy elements between the lists of that colors\n";
    help += " => Scrollbar management: if copying an element to a list gives this list more elements than it can display, a vertical scrollbar";
    help += " is added to allow scrolling to any of the elements. If by removing an element from a list all elements can be displayed at once";
    help += " the scrollbar is removed again. Likewise a horizontal scrollbar is added when an element is copied into the list that is longer";
    help += " then the list length and removed again if that element is removed.\n";
    help += " => Mouse scrolling: next to scrolling through the list using the scrollbars, you can also move the selectinon";
    help += " by dragging your mouse along the borders of the list viewport\n";

    return help;
  }
     	
 	public void log(java.awt.Panel p, java.io.Writer w)throws java.io.IOException {
    w.write(logString);
    logString = "";
 	}

  public void start(java.awt.Panel p, boolean b){}
  public void stop(java.awt.Panel p){}
}
