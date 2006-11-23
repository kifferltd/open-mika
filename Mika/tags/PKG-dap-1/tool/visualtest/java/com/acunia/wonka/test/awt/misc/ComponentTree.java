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

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;

public class ComponentTree extends VisualTestImpl {
  Panel p;
  Panel q;
  Panel o;

  public ComponentTree() {
    o = this;
    p = new Panel(new FlowLayout());
    q = new Panel(new FlowLayout());
    Button b = new Button("Hello World !");
    q.add(b);
    p.add(q);

    System.out.println("Components (p): " + p.getComponents());
    System.out.println("Components (q): " + q.getComponents());

    Button go = new Button("Go for it.");
    go.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        o.remove(p);
        o.add(p);
        System.out.println("Components (p): " + p.getComponents());
        System.out.println("Components (q): " + q.getComponents());
      }
    });
        
    setLayout(new BorderLayout());
    add(p, BorderLayout.CENTER);
    add(go, BorderLayout.SOUTH);
  }

  public String getHelpText() {
    return "Blah blah woof woof.";
  }
}

