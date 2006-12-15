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


//Author: J. Bensch
//Created: 2001/08/10

package com.acunia.wonka.test.awt.Font;

import com.acunia.wonka.test.awt.*;
import java.awt.*;
import java.awt.event.*;

public class SetFont3 extends VisualTestImpl {

  class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String someAction = e.getActionCommand();
      SetFont3 DM = (SetFont3)((Component)e.getSource()).getParent().getParent();
      char sA = someAction.charAt(0);
      int sAction = sA;
      DM.switchFont(sAction);
    }
  }

  class myButton extends Button {
    public myButton(String bname, String cmd, Panel panel) {
      super(bname);
      this.addActionListener(myListener);
      this.setActionCommand(cmd);
      panel.setBackground(Color.yellow);
      panel.add(this);
    }
    public myButton(String bname, Panel panel) {
      super(bname);
      panel.setBackground(Color.red);
      panel.add(this);
    }
    public myButton(String bname, char position, Panel buttonPanel, Panel panel) {
      super(bname);
      panel.setBackground(Color.magenta);
      if (position == 'n') {
        buttonPanel.add(this);
        panel.add(buttonPanel, BorderLayout.NORTH);
      }
      else {
        buttonPanel.add(this);
        panel.add(buttonPanel, BorderLayout.SOUTH);
      }
    }
    ButtonListener myListener = new ButtonListener();
  }

    private Font fH, fCo, fCl, fCo2; 
    private myButton b1, b2, b3, bF, bG;
    private Button lastB = new Button("Dummy");
    private Button lastLM = new Button("Dummy");
    public SetFont3() {
    super();
    Panel p1 = new Panel(), p2 = new Panel(), p3 = new Panel(), p4 = new Panel(), p8 = new Panel(), p9 = new Panel();

    Dimension dim = new Dimension(400, 234);
    p1.setLayout(new BorderLayout());

    b1 = new myButton("Just a string.", 'n', p8, p1); /* n = north */
    b2 = new myButton("Scale string.", 's', p9, p1);  /* s = south */
    b3 = new myButton("A rather long string for font testing.", p3 );
    new myButton("Helvetica, 16", "1", p2);
    new myButton("Courier, 12", "2", p2);
    new myButton("Clean, 14", "3", p2);
    bF = new myButton("FlowLayout", "4", p4);
    bG = new myButton("GridLayout", "5", p4);
    new myButton("Courier, 10", "7", p2);

    fH = new Font("Helvetica", Font.ITALIC, 16);
    fCo = new Font("Courier", Font.PLAIN, 12);
    fCl = new Font("Clean", Font.BOLD, 14);
    fCo2 = new Font("Courier", Font.PLAIN, 10);

    setLayout(new BorderLayout());
    setBackground(Color.darkGray);
    setSize(dim);

    add(p1, BorderLayout.WEST);
    add(p2, BorderLayout.SOUTH);
    add(p3, BorderLayout.NORTH);
    add(p4, BorderLayout.CENTER);
    setVisible(true);
  }

  public void switchFont(int sAction) {
    Container frame = b1.getParent().getParent().getParent();
    boolean isButton = true;
    switch (sAction) {
      case 49: setEnv(isButton, b1, fH, Color.green); break;
      case 50: setEnv(isButton, b2, fCo, Color.green); break;
      case 51: setEnv(isButton, b3, fCl, Color.green); break;
      case 52: isButton = false; setEnv(isButton, bF, null, Color.blue); setLayout(new FlowLayout()); break;
      case 53: isButton = false; setEnv(isButton, bG, null, Color.blue); setLayout(new GridLayout(4, 5, 0, 0)); break;
      case 55: lastB.setBackground(null); b1.setFont(fCo2); b2.setFont(fCo2); b3.setFont(fCo2); break;
    }
    frame.validate();
  }

  public void setEnv(boolean isButton, Button theButton, Font font, Color color) {    
    if (isButton) {
      lastB.setBackground(null);
      lastB = theButton;
      theButton.setFont(font);
    }
    else {
      lastLM.setBackground(null);
      lastLM = theButton;
    }
    theButton.setBackground(color);
  }

  public String getHelpText() {
    return ("A test which lets you set several fonts with different sizes on buttons. Nothing extraordinary you'd say? Agreed, but the thing is that the buttons will scale and the text won't be clipped off at the button's borders. You can also change the Frame's LayoutManager. Somehow it isn't possible to switch back to BorderLayout...");
  }
  
}

