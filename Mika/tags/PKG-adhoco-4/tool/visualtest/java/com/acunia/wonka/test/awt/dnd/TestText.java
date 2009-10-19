/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package com.acunia.wonka.test.awt.dnd;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class TestText extends VisualTestImpl {
  Component component1, component2, component3, component4, component5;
  Button button1;

  public TestText() {
    setLayout(new GridLayout(3,2));

    setBackground(Color.white);

    component1 = new DropText("Drop here");
    component2 = new DropText("or drop here :)");
    component3 = new DragDropText("you can drag this one or just drop here");
    component4 = new DragText("Drag this field");
    component5 = new DragText("or drag this one");
    button1 = new Button("button, so you have another component ;)");

    add(component1);
    add(button1);
    add(component2);
    add(component3);
    add(component4);
    add(component5);

    component1.setVisible(true);
    component2.setVisible(true);
    component3.setVisible(true);
    component4.setVisible(true);
    component5.setVisible(true);
    button1.setVisible(true);
    setVisible(true);
  }
  
  public String getHelpText(){
    return "";
  }

}
