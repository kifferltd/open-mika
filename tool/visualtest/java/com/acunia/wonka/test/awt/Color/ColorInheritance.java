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


// Author: D. Buytaert
// Created: 2001/04/20

package com.acunia.wonka.test.awt.Color;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Scrollbar;
import java.awt.TextArea;
import java.awt.TextField;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class ColorInheritance extends VisualTestImpl {

  public ColorInheritance() {
    setSize(400, 234);
    setLayout(new GridLayout(4, 2));

    setBackground(Color.yellow);
    setForeground(Color.red);

//    add(new TextArea("A test for foreground and background color inheritance.\n The background color is set to yellow and the foreground color to red.  All components should correctly inherit these colors from their parent frame."));
    add(new TextArea("Mr and Mrs Dursley, of number four privet drive, were proud to say that they were perfectly normal, thank you very much."));
    add(new Button("button"));
    add(new Label("label"));
    add(new TextField("textfield"));
    List  lst = new List();
    lst.add("List");
    lst.add("....");
    lst.add("oswald");
    lst.add("wonka");
    lst.add("rudolph");
    add(lst);
    add(new Checkbox("checkbox"));
    add(new Scrollbar(Scrollbar.HORIZONTAL));
    add(new Scrollbar(Scrollbar.VERTICAL));

  }

  public String getHelpText(){
    return "A test for foreground and background color inheritance.  The background color is set to yellow and the foreground color to red. All components should correctly inherit these colors from their parent frame.";  	
  }
}
