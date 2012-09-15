/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package com.acunia.wonka.rudolph.peers;

import com.acunia.wonka.rudolph.Dispatcher;
import java.awt.peer.*;
import java.awt.*;

public class DefaultDialog extends DefaultWindow implements DialogPeer {
 
  protected boolean block = false;
 
  public DefaultDialog(Dialog dialog) {
    super(dialog); 
  }

  public void setResizable(boolean resizable) {
  }
  
  public void setTitle(String title) {
    wm.setTitle((Window)component, title);
  }

  public void show() {
    Dispatcher dispatcher= null;

    super.show();

    boolean modal = ((Dialog)component).isModal();
    Window owner = ((Dialog)component).getOwner();

    if(modal) {
      block = true;

      boolean spawn = java.awt.EventQueue.isDispatchThread();

      /*
      ** Disable events in the owner.
      */

      owner.disableAllEvents();
      ((Window)component).enableAllEvents();

      /*
      ** If we are running in the AWT dispatch thread, start a new dispatch thread.
      */

      if (spawn) {
        dispatcher = new com.acunia.wonka.rudolph.Dispatcher(null);
        dispatcher.start();
      }

      /*
      ** Keep waiting...
      */
      
      while(block) {
        try {
          Thread.sleep(100);
        } catch(InterruptedException e) {
        }
      }

      /*
      ** Stop the new dispatch thread (if any).
      */

      if (spawn) {
        dispatcher.stop();
      }

      /*
      ** Enable events in the owner.
      */

      owner.enableAllEvents();
      
    }
  }

  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if(!visible && block) {
      block = false;
    }
  }

  public void dispose() {
    block = false;
    super.dispose();
  }

}

