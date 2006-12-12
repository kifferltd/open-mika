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

package com.acunia.wonka.test.awt.SystemColor;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.SystemColor;

import com.acunia.wonka.test.awt.VisualTestImpl;


public class SystemColors extends VisualTestImpl {

  public SystemColors() {
    setLayout(new GridLayout(8, 2));

    Label l1 = new Label("desktop");
    l1.setBackground(SystemColor.desktop);
    add(l1);

    Label l2 = new Label("window");
    l2.setBackground(SystemColor.window);
    add(l2);

    Label l3 = new Label("windowText");
    l3.setBackground(SystemColor.windowText);
    l3.setForeground(Color.white);
    add(l3);

    Label l4 = new Label("control");
    l4.setBackground(SystemColor.control);
    add(l4);

    Label l5 = new Label("controlText");
    l5.setBackground(SystemColor.controlText);
    l5.setForeground(Color.white);
    add(l5);

    Label l6 = new Label("controlShadow");
    l6.setBackground(SystemColor.controlShadow);
    add(l6);

    Label l7 = new Label("controlDkShadow");
    l7.setBackground(SystemColor.controlDkShadow);
    add(l7);

    Label l8 = new Label("activeCaption");
    l8.setBackground(SystemColor.activeCaption);
    add(l8);

    Label l9 = new Label("activeCaptionText");
    l9.setBackground(SystemColor.activeCaptionText);
    add(l9);

    Label l10 = new Label("activeCaptionBorder");
    l10.setBackground(SystemColor.activeCaptionBorder);
    add(l10);

    Label l11 = new Label("inactiveCaption");
    l11.setBackground(SystemColor.inactiveCaption);
    add(l11);

    Label l12 = new Label("inactiveCaptionText");
    l12.setBackground(SystemColor.inactiveCaptionText);
    add(l12);

    Label l13 = new Label("inactiveCaptionBorder");
    l13.setBackground(SystemColor.inactiveCaptionBorder);
    add(l13);

    Label l14 = new Label("Scrollbar");
    l14.setBackground(SystemColor.scrollbar);
    add(l14);

    Label l15 = new Label("info");
    l15.setBackground(SystemColor.info);
    add(l15);

    Label l16 = new Label("infoText");
    l16.setBackground(SystemColor.infoText);
    l16.setForeground(Color.white);
    add(l16);
  }

  public String getHelpText(){
    return "A test to (a) verify wether rudolph's internal color model is nicely coupled to java.awt.SystemColor and (b) to inspect the default system colors in java.awt.SystemColor.";
  }
}
