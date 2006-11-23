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

public class DialogTest extends VisualTestImpl {
  
  VisualTester getVt() {
    return vt;
  }
  
  public DialogTest() {
    Button modal = new Button("Modal");
    Button modeless = new Button("Modeless");
    add(modal);
    add(modeless);

    modal.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        new TestDialog(getVt().getFrame(), true);
      }
    });
    
    modeless.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        new TestDialog(getVt().getFrame(), false);
      }
    });
  }

  public String getHelpText() {
    return "DialogTest";
  }

  private class TestDialog extends Dialog implements ActionListener {

    TestDialog(Frame frame, boolean modal) {
      super(frame, "A Dialog", modal);
      this.setBackground(Color.yellow);
      this.add(new Label(modal ? "Modal dialog" : "Modeless dialog"), BorderLayout.CENTER);
      Button close = new Button("close");
      close.addActionListener(this);
      this.add(close, BorderLayout.SOUTH);
      this.pack();

      Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
      this.setLocation((screensize.width - this.getSize().width) / 2, (screensize.height - this.getSize().height) / 2);
      
      this.show();
    }

    public void actionPerformed(ActionEvent e) {
      this.dispose();
    }

    public Insets getInsets() {
      return new Insets(10, 10, 10, 10);
    }
  }
  
}

