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

package com.acunia.wonka.test.awt.event;

import com.acunia.wonka.test.awt.*;
import java.awt.*;
import java.awt.event.*;

public class MouseCoordinates extends VisualTestImpl implements MouseListener {
  Label l1;
  Button b1;
  TextArea t1;
  String lastEvent;
  String previousEvent;

  public MouseCoordinates() {
    setLayout(new GridLayout(3, 1));

    l1 = new Label("CLICK YOUR MOUSE HERE",Label.CENTER);
    l1.addMouseListener(this);
    add(l1);

    b1 = new Button("CLICK YOUR MOUSE HERE");
    b1.setFont(new Font("helvB17", Font.BOLD, 25));
    b1.addMouseListener(this);
    add(b1);

    t1 = new TextArea("Read your mouse events HERE", 3, 20, TextArea.SCROLLBARS_VERTICAL_ONLY);
    add(t1);
  }

  public void mouseClicked(MouseEvent event) {
    displayMouseEvent("Function <mouseClicked(MouseEvent)> ...",event);
  }
  
  public void mouseEntered(MouseEvent event) {
    displayMouseEvent("Function <mouseEntered(MouseEvent)> ...",event);
  }
  
  public void mouseExited(MouseEvent event) {
    displayMouseEvent("Function <mouseExited(MouseEvent)> ...",event);
  }
  
  public void mousePressed(MouseEvent event) {
    displayMouseEvent("Function <mousePressed(MouseEvent)> ...",event);
  }
  
  public void mouseReleased(MouseEvent event) {
    displayMouseEvent("Function <mouseReleased(MouseEvent)> ...",event);
  }

  public String getHelpText(){
    return "using your mouse in either the uppermost panel or the central button shoule fire a MouseEvent that is then displayed in the textArea below."
     +"  The coordinates should be relative to the upper left corner of component clicked \n"
     +"  The Mouse events that should be detected are: \n"
     +"  Mouse clicked,\nMouse pressed,\nMouse released,\nMouse entered and\nMouse Exited";
  }

  private void displayMouseEvent(String eventstring, MouseEvent event) {
    previousEvent = lastEvent;
    lastEvent = eventstring;
    lastEvent += (event.getSource()==l1)?"\nMouseEvent from Label :":"\nMouseEvent from Button : ";
    switch(event.getID()) {
      case MouseEvent.MOUSE_CLICKED:
        lastEvent+="\nevent getId() detected Mouse CLICKED";
        break;
      case MouseEvent.MOUSE_PRESSED:
        lastEvent+="\nevent getId() detected Mouse PRESSED";
        break;
      case MouseEvent.MOUSE_RELEASED:
        lastEvent+="\nevent getId() detected Mouse RELEASED";
        break;
      case MouseEvent.MOUSE_ENTERED:
        lastEvent+="\nevent getId() detected Mouse ENTERED)";
        break;
      case MouseEvent.MOUSE_EXITED:
        lastEvent+="\nevent getId() detected Mouse EXITED";
        break;
      default:
        lastEvent+="\nevent getId() detected UNKNOWN MOUSE COMMAND";
        break;
    }
     lastEvent+="\n at position("+event.getX()+", "+event.getY()+")";
     lastEvent+="\n and getModifiers() = "+event.getModifiers();
     lastEvent+="\n and isControlDown() = "+event.isControlDown();
     lastEvent+="\n and isShiftDown() = "+event.isShiftDown();
//     lastEvent+="\n and isAltGraphDown() = "+event.isAltGraphDown();
     lastEvent+="\n and isConsumed() = "+event.isConsumed();
     lastEvent+="\n and isMetaDown() = "+event.isMetaDown();
//   lastEvent +="\n clicked"+event.getClickCount()+" times";
  // lastEvent +=(event.getModifiers()==InputEvent.BUTTON1_MASK)?"\n =>Left button clicked":"\n =>other button clicked";
  // lastEvent +="\n(event.toString() = ["+event.toString()+"] )";
System.out.println(lastEvent);
    t1.setText(lastEvent+ "\n\n  Previous event: \n"+previousEvent);
  }
}
