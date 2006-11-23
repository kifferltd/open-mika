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

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Label;
import java.awt.TextField;

import com.acunia.wonka.test.awt.VisualTestImpl;


public class FlowLayout extends VisualTestImpl {

  public FlowLayout() {

    setLayout(new java.awt.FlowLayout());

    setBackground(Color.blue);

    add(new TextField("Perhaps"));
    add(new Button("the former"));
    add(new Label("president's"));
    add(new Checkbox("amazing"));
    add(new TextField("sax"));
    add(new Button("skills"));
    add(new Label("will"));
    add(new Checkbox("be"));
    add(new TextField("judged"));
    add(new Button("quite"));
    add(new Label("favorably."));

    add(new TextField("How"));
    add(new Button("razorback-jumping"));
    add(new Label("frogs"));
    add(new Checkbox("can"));
    add(new TextField("level"));
    add(new Button("six"));
    add(new Label("piqued"));
    add(new Checkbox("gymnasts."));

    add(new TextField("Pack"));
    add(new Button("my"));
    add(new Label("box"));
    add(new Checkbox("with"));
    add(new TextField("five"));
    add(new Button("dozen"));
    add(new Label("liquor"));
    add(new Checkbox("jugs."));
  }

  public String getHelpText(){
    return "A test to verify Rudolph's FlowLayout implementation.";
  }

}
