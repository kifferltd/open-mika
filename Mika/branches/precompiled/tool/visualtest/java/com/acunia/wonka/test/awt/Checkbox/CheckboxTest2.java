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


package com.acunia.wonka.test.awt.Checkbox;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class CheckboxTest2 extends VisualTestImpl implements ItemListener{

  public CheckboxTest2() {
    setLayout(new GridLayout(0, 1));
    
    CheckboxGroup group = new CheckboxGroup();

    Checkbox cb1 = new Checkbox("Checkbox 1");
    cb1.setCheckboxGroup(group);

    Checkbox cb2 = new Checkbox("Checkbox 2");
    cb2.setState(true);
    cb2.setCheckboxGroup(group);

    add(cb1);
    add(cb2);

    System.out.println("Selected : " + group.getSelectedCheckbox());
  }

  public void itemStateChanged(ItemEvent evt) {
  }

  public String getHelpText() {
    return "";
  }
}
