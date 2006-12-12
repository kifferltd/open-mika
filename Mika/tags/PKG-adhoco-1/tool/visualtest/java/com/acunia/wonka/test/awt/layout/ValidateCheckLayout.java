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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.LayoutManager;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class ValidateCheckLayout extends VisualTestImpl implements LayoutManager {

  private LayoutManager mngr = new java.awt.FlowLayout();
  private int counter = 0;
  private Label label;
  private String result = "TEST PASSES: ";
  private String message = " validate is only called once!";

  private boolean autorunning;

  public ValidateCheckLayout(){
    super();
    this.setLayout(this);
  }

  public void start(java.awt.Panel p, boolean b) {
    autorunning = b;

    label = new Label(result);

    /*
    ** Do some stuff that should *not* validate/layout the container.
    */

    this.add(label);
    this.remove(label);
    this.add(label);
    this.add(new Label(message));

    label.setText(result);

    validate();
  }
     	
  public void addLayoutComponent(String name, Component comp){
    counter++;
    mngr.addLayoutComponent(name, comp);
  }

  public void layoutContainer(Container parent) {
    if (counter > 0) {
      result = "TEST FAILED: ";
      message = "validate is called to soon!";
      if (!autorunning) {
        vt.log("Test failed.  The method Container.validate() is called too soon: Container.addImpl() and Container.remove() should not call validate() themselves.", this);
      }
    }

    mngr.layoutContainer(parent);
  }

  public Dimension minimumLayoutSize(Container parent){
    return mngr.minimumLayoutSize(parent);
  }

  public Dimension preferredLayoutSize(Container parent){
    return mngr.preferredLayoutSize(parent);
  }

  public void removeLayoutComponent(Component comp){
    mngr.removeLayoutComponent(comp);
  }

  public String getHelpText(){
    return "This test tries to see if a validate is called after an Panel.add(comp)\n"+
      "if the test fails the labels will be set to show this, otherwise test passed is displayed" ;
  }
}
