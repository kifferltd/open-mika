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
import com.acunia.wonka.test.awt.*;
import java.awt.*;
import java.awt.event.*;


public class ReplaceListTest extends VisualTestImpl  implements ActionListener
{

  private List redList;
  private List blueList;
	private Button toRed;
	private Button toBlue;	
	private List display;
	private String logString;
	
  public ReplaceListTest() {
    super();
    //lists by definitions
    setBackground(new Color(225,225,64));
    setLayout(new BorderLayout());

    Panel main = new Panel(new GridLayout(1,2));
      redList = new List(7,false);
      blueList = new List(7,false);
      redList.setBackground(new Color(255,128,128));
      blueList.setBackground(new Color(128,128,255));
      for(int i=1; i<=10;i++)
      {
      	redList.add("Red list item No."+i);
      	blueList.add("BLUE LIST ITEM NUMBER "+i);
      }
      main.add(redList);
      main.add(blueList);
    add(main, BorderLayout.CENTER);
    Panel buttons = new Panel(new GridLayout(1,2));
      toBlue = new Button("Copy to blue ==>");
      toBlue.setBackground(new Color(192,64,64));
      toBlue.addActionListener(this);
      toRed = new Button("<== Copy to red");
      toRed.setBackground(new Color(64,64,192));
      toRed.addActionListener(this);
      buttons.add(toBlue);
      buttons.add(toRed);
    add(buttons, BorderLayout.NORTH);
    display = new List(3, false);
      display.add("click on the copy button of a list");
      display.add("to copy the selected item of this list");
      display.add("over the selected item of the other list");
    add(display, BorderLayout.SOUTH);

    logString = new String("Logging messages from List.ReplaceListTest\n");
  }

  /** Button pressed: look at the button and either move all selected from red to blue or from blue to red*/
  public void actionPerformed(ActionEvent evt) {
    int redpos = redList.getSelectedIndex();
    String redselect = redList.getSelectedItem();
    int bluepos = blueList.getSelectedIndex();
    String blueselect = blueList.getSelectedItem();

  	if(evt.getSource() == toBlue) {
      //replace blue selection by red selection
      if(redselect == null){
        displayMessage("FAIL: no red selection to copy, select one first");
      }
      else{
        displayMessage("Blue selection = <"+blueselect+"> at position"+bluepos);
        displayMessage("Red selection = <"+redselect+"> at position"+redpos);
        blueList.replaceItem(redselect, bluepos);
      }
      displayMessage("Replacing blue selection by red selection");
  	}
  	else{
      //replace red selection by blue selection
      //replace blue selection by red selection
      if(blueselect == null){
        displayMessage("FAIL: no blue selection to copy, select one first");
      }
      else{
        displayMessage("Red selection = <"+redselect+"> at position"+redpos);
        displayMessage("Blue selection = <"+blueselect+"> at position"+bluepos);
        redList.replaceItem(blueselect, redpos);
      }
      displayMessage("Replacing red selection by blue selection");
  	}
  }

  private void displayMessage(String message){
    if(display.getItemCount()>20){
      display.removeAll();
    }
    display.add(message, 0);
    logString += message + "\n";
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
    return "A test on replacing a list element by another using List.replaceItem(String newitem, int pos).\n"+
      "The test shows a red list and a blue list with a red and blue <copy> button on top\n"+
      "Pressing the red button replaced the selected element of the blue lidt with that of the red list and vice versa";
  }

     	
 	public void log(java.awt.Panel p, java.io.Writer w)throws java.io.IOException {
    w.write(logString);
    logString = "";
 	}

  public void start(java.awt.Panel p, boolean b){}
  public void stop(java.awt.Panel p){}


}
