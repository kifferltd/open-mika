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

package com.acunia.wonka.test.awt.ScrollPane;

import java.awt.Dimension;
import java.awt.Panel;
import java.awt.ScrollPane;

import com.acunia.wonka.test.awt.VisualTestImpl;
import com.acunia.wonka.test.awt.Component.ComponentType;

public class ScrollPaneTest2 extends VisualTestImpl {

  private VisualTestImpl vt;

  class MyScrollPane extends ScrollPane {
    public MyScrollPane() {
      super();
      this.setSize(getPreferredSize());
    }

    public Dimension getMinimumSize() {
      return getPreferredSize();
    }
  
    public Dimension getMaximumSize() {
      return getPreferredSize();
    }
  
    public Dimension getPreferredSize() {
      return new Dimension(170, 170);
    }
  }

  public ScrollPaneTest2() {
    
    MyScrollPane sp = new MyScrollPane();
 
    vt = new ComponentType();

    sp.add(vt);
  
    add(sp);
  }

  public void start(Panel p, boolean autorun) {
    vt.start(null, false);
  }

  public void stop(Panel p) {
    vt.stop(null);
  }


  public String getHelpText() {
    return "beu!";
  }

}
