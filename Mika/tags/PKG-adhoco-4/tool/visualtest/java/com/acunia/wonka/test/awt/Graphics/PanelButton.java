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
// Created: 2001/05/03

package com.acunia.wonka.test.awt.Graphics;

import java.awt.*;
import com.acunia.wonka.test.awt.*;
import com.acunia.wonka.test.awt.Graphics.ClickButton.*;



public class PanelButton extends VisualTestImpl  {

  ClickButton button1;
  ClickButton button2;

  public PanelButton() {

    super();


    setLayout(new GridLayout(2, 1));

    button1 = new ClickButton("Button1", Color.green, Color.red);
    button2 = new ClickButton("Button2", Color.blue, Color.yellow);
    add(button1);
    add(button2);
  }

  public String getTitle(){
    return "PanelButton";
  }
  public String getHelpText(){
    return ("You should see a screen filled with two large buttons of equal size. " +
            "The top button is supposed to be green with red text 'Button1' on it, and " +
            "the bottom button is supposed to be blue with yellow text 'Button2' " +
            "on it. While a button is pressed down " +
            "the label should be shown in bold. When the button is released, the label " +
            "should appear in normal plain style. The font used is Helvetica of size 18. " +
            "If you see just a green and a yellow rectangular area, something is wrong." +
            "If the bold label is not cleared when a button is released, something is " +
            "wrong too.");
  }
     	
  public java.awt.Panel getPanel(VisualTester vt){
    return this;
  }
     	
  public String getLogInfo(java.awt.Panel p, boolean b){
    return "no logging info !";
  }
  public void start(java.awt.Panel p, boolean b){
  }
  public void stop(java.awt.Panel p){}

  public static void main (String[] args) {
    new PanelButton();
  }

}
