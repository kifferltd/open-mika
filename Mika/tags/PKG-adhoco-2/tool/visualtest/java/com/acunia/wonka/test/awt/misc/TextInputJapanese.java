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

package com.acunia.wonka.test.awt.misc;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.TextField;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class TextInputJapanese extends VisualTestImpl {

  private TextField tf;
  
  public TextInputJapanese() {
    setLayout(new BorderLayout());
    tf = new TextField();
    tf.setFont(new Font("nippon13", 0, 13));
    add(tf, BorderLayout.CENTER);
  }

  public String getHelpText() {
    return "This test is meant for testing the Wonka virtual keyboard. It provides " +
           "a textfield to show the characters that correspond to the keystrokes on the " +
           "the virtual keyboard.\n" +
           "To enable the virtual keyboard (before starting vte), edit file taskbar.properties " +
           "and set properties: \n" +
	   "  taskbar = true\n" +
	   "  applet.keyboard.selectable = true\n" +
           "When running vte now, a taskbar will be visible at the top of the screen. " +
           "It should contain a keyboard icon with a menu pull down button to its right. " +
	   "Click the keyboard icon. A keyboard with western keys pops up. You can change it " +
	   "to a japanese keyboard by clicking the pulldown button and selecting 'Japanese' " +
	   "in the appearing menu. " +
	   "You might aswell preset the default keyboard in file taskbar.properties. \n" +
	   "Click or touch the textfield to give it the focus. " +
	   "Characters appear in the textfield as you press keys on the keyboard (with " +
	   "mouse or touchscreen pointer)."  ;
  }
}

