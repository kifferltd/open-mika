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

package com.acunia.wonka.test.awt.Container;

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.VisualTestImpl;

public class RemoveAddTest extends VisualTestImpl implements ActionListener {
	
   private static final String buttonText1 = " click to switch ";
   private static final String buttonText2 = " click to switch back";

	 private Panel panel1;
	 private Panel panel2;

	 public RemoveAddTest() {
		  setLayout(null);
      setBackground(Color.white);
		
		  panel1 = new Panel();
		  panel1.setLayout(null);
		  panel1.setBounds(30, 30, 200, 150);
      panel1.setBackground(Color.red);

		  Button button = new Button(buttonText1);
		  button.setActionCommand(buttonText1);
		  button.setBounds(10, 30, 180, 40);
		  button.addActionListener(this);
		  panel1.add(button);	

		  panel2 = new Panel();
		  panel2.setLayout(null);
		  panel2.setBounds(20, 20, 200, 150);
      panel2.setBackground(Color.blue);

		  button = new Button(buttonText2);
		  button.setActionCommand(buttonText2);
		  button.setBounds(10, 100, 180, 40);
		  button.addActionListener(this);
		  panel2.add(button);	

		  add(panel1);
	 }
	
  public void actionPerformed(ActionEvent event) {
    String cmd = event.getActionCommand();
  		
    if (buttonText1.equals(cmd)) {
      add(panel2);
      remove(panel1);
    }

    else if (buttonText2.equals(cmd)) {
      add(panel1);
      remove(panel2);
    }
  }

  public String getHelpText(){
    return "This will add and remove a panel when one of the buttons is pushed"+
           "the test passes if the other panel is shown ...";
  }

}




