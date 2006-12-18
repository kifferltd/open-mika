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

import java.awt.Color;
import java.awt.Label;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class ChangingTextTest extends VisualTestImpl implements Runnable {

  private Thread thread;
  private Label text;

  private final String[] strings = new String[]{ " This text will change ", " changing text ",
                                        " updating text " , " done " , " ChangingTextTest " };

  private boolean stop;
  private boolean pause;
  public ChangingTextTest() {
    text = new Label("      changing text !     ",Label.CENTER);
    text.setBackground(Color.white);
    setBackground(Color.blue);
    this.add(text);
  }

  public String getHelpText(){
    return "This test will call setText('String')"+
           "A thread will update a Label ... so the text should change";
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
    int i = 0;
    while(!stop){
      try {
        if(pause){
          Thread.sleep(2000);
        }
        else {
          text.setText(strings[i]);
          i = (i+1) % 5;
          Thread.sleep(1000);
        }
      }
      catch(InterruptedException ie){}
    }
  }
}




