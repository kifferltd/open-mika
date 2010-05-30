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
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;


public class SingleListTest extends VisualTestImpl  implements ActionListener
{

  java.awt.List redList;
  java.awt.List blueList;
  java.awt.List greenList;
	
	Button toRed;
	Button toBlue;
	Button toGreen;
	
	String logString;
	
  public SingleListTest()
  { this(4,5,false); }
	
  public SingleListTest(int minitems, int desired, boolean multiple)
  {
    super();
    //lists by definitions
    setLayout(new BorderLayout());

    redList = new java.awt.List(minitems, multiple);
    blueList = new java.awt.List(minitems, multiple);
    greenList = new java.awt.List(minitems, multiple);

    for(int i=1; i<=desired;i++)
    {
    	redList.add("RED-LIST ITEM "+i + " (standard length)");
    	blueList.add("BLUE-LIST ITEM"+i+" (short)");
    	greenList.add("GREEN-LIST ITEM"+i+" (Extra long to test horizontal scrolling)");
    }
    redList.setBackground(new Color(255,128,128));
    blueList.setBackground(new Color(128,128,255));
    greenList.setBackground(new Color(128,225,128));

    toBlue = new Button("Red to BLUE");
    toBlue.addActionListener(this);
    toBlue.setBackground(new Color(255,128,128));
    toGreen = new Button("Blue to GREEN");
    toGreen.addActionListener(this);
    toGreen.setBackground(new Color(128,128,225));
    toRed = new Button("Green to RED");
    toRed.addActionListener(this);
    toRed.setBackground(new Color(128,255,128));
		Panel buttonrow = new Panel(new GridLayout(1,2));
    buttonrow.add(toBlue);
    buttonrow.add(toGreen);
    buttonrow.add(toRed);
	
    add(redList, BorderLayout.NORTH);
    add(blueList, BorderLayout.WEST);
    add(greenList, BorderLayout.CENTER);
    add(buttonrow, BorderLayout.SOUTH);

    logString = new String();
    //if stand-alone: size and show as separate Panel
		setSize(389, 219);
    show();
  }

  /** Button pressed: look at the button and either move all selected from red to blue or from blue to red*/
  public void actionPerformed(ActionEvent evt)
  {
  	if(evt.getSource() == toBlue) //red to blue
  		moveItems(redList, blueList);
  	else if(evt.getSource() == toGreen) //blue to big
  		moveItems(blueList, greenList);
  	else                           //big to red
  		moveItems(greenList, redList);
  }


  private void moveItems(java.awt.List from, java.awt.List to)
  {
		Object[] selected = from.getSelectedObjects();
		
		for(int i=0; i<selected.length; i++)
		{
			to.add((String)selected[i],0); //ad as first in row
			from.remove((String)selected[i]);
		}
  }

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
    return "ListTest : single selections";
  }

  public String getHelpText(){
    String help =  "A test on list behavior for a single element selection list.\n";
    help += "The test draws three lists: a red one, a  blue one and a green one on different fields of a Borderlayout";
    help += " It also gives you three buttons that enable you to copy elements from one list to another\n";
    help += "\n Items to test:\n";
    help += "=> Minimum visible elements: The red list is defined with a minimum of 4 visible items, being drawn in the BorderLayout.NORTH";
    help += " field it should display exactly four of its five elements and a vertical scrollbar to scroll to the fifth\n";
    help += "=> Minimum width: The blue list is being drawn in the BorderLayout.WEST field. All of its items should be completely visible";
    help += "(with a default font big enough, this makes the remaining BroderLayout.CENTER field so small that the items";
    help += " of the green list can not displayed completely and this list is drawn with a vertical scrollbar)\n";
    help += " => Selecting: An item of a list can be selected by clicking it. It's selected state should be cleasly visible.";
    help += " Clicking a selected item deselects it again. Only one element can be selected at a time: selecting an element automatically deselects the previous one. \n";
    help += " => Moving between lists: clicking the red-to-blue button removes the selected item of the red list from that list";
    help += " and adds it to the blue one. Likewise the blue-to-green and green-to-red buttons copy elements between the lists of that colors\n";
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

/**********************************************************************************************************************************/
/**
*	Main for standalone functions
**/
  static public void main (String[] args)
  {
    int min=3;
    int desired=5;
    boolean multiple=false;
    if(args.length>0)
    	min = Integer.parseInt(args[0]);
    if(args.length>1)
    	desired = Integer.parseInt(args[1]);
    if(args.length>2)
    	multiple = true;
  	new SingleListTest(min, desired, multiple);
  }
}
