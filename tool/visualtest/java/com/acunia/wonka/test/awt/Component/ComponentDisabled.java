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
// Created: 2001/05/03

package com.acunia.wonka.test.awt.Component;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.TextField;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class ComponentDisabled extends VisualTestImpl {

  public ComponentDisabled() {
    setLayout(new GridLayout(0, 4, 2, 2));
    
    Button b1 = new Button("Button");
    Button b2 = new Button("Button");
    b2.setEnabled(false);
    add(b1);
    add(b2);
    
    Choice h1 = new Choice();
    Choice h2 = new Choice();
    h2.setEnabled(false);
    h1.add("one");  
    h1.add("two");  
    h1.add("three");  
    h2.add("one");  
    h2.add("two");  
    h2.add("three");  
    add(h1);
    add(h2);

    Checkbox c1 = new Checkbox("Checkbox");
    Checkbox c2 = new Checkbox("Checkbox");
    c2.setEnabled(false);
    add(c1);
    add(c2);

    Checkbox d1 = new Checkbox("Checkbox");
    Checkbox d2 = new Checkbox("Checkbox");
    d1.setState(true);
    d2.setState(true);
    d2.setEnabled(false);
    add(d1);
    add(d2);
    
    TextField t1 = new TextField("TextField");
    TextField t2 = new TextField("TextField");
    t2.setEnabled(false);
    add(t1);
    add(t2);

    List l1 = new List();
    List l2 = new List();
    l2.setEnabled(false);
    l1.add("one");
    l1.add("two");
    l1.add("three");
    l1.add("four");
    l1.add("five");
    l1.add("six");
    l1.add("seven");
    l1.add("eight");
    l1.add("nine");
    l1.add("ten");
    l2.add("one");
    l2.add("two");
    l2.add("three");
    l2.add("four");
    l2.add("five");
    l2.add("six");
    l2.add("seven");
    l2.add("eight");
    l2.add("nine");
    l2.add("ten");
    add(l1);
    add(l2);
    
  }

  public String getHelpText(){
    return "";
  }

}

