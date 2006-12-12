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

package com.acunia.wonka.test.awt.Container;

import java.awt.*;
import com.acunia.wonka.test.awt.VisualTestImpl;

public class SetVisibleTest extends VisualTestImpl implements Runnable {

  private Thread thread;
  private Label text;

  private boolean stop;
  private boolean pause;
  private boolean visible;

  public SetVisibleTest() {
    text = new Label(" Blinking text !  ",Label.CENTER);
    text.setBackground(Color.white);
    setBackground(Color.blue);
    this.add(text);
  }

  public String getHelpText(){
    return "This test will call setVisible(true) and setVisible(false) "+
           "A thread will update a Label ... so it should be blinking";
  }

  public void start(java.awt.Panel p, boolean b){
    if(thread == null){
      thread = new Thread (this,"SetVisibleTest thread");
      thread.start();
    }
  }

  public void stop(java.awt.Panel p){
    stop = true;
    if(thread != null){
      thread.interrupt();
      thread = null;
    }
  }

  public void showTest(){
    pause = false;
    if(thread != null){
      thread.interrupt();
    }
  }


  public void hideTest(){
    pause = true;
    if(thread != null){
      thread.interrupt();
    }
  }

  public void run(){
    while(!stop){
      try {
        if(pause){
          Thread.sleep(2000);
        }
        else {
          text.setVisible(visible);
          visible = !visible;
          Thread.sleep(500);
        }
      }
      catch(InterruptedException ie){}
    }
  }
}




