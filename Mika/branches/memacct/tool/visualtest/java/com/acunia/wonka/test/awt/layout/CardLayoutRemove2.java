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
import java.awt.CardLayout;
import java.awt.Label;
import java.awt.Panel;

import com.acunia.wonka.test.awt.VisualTestImpl;


public class CardLayoutRemove2 extends VisualTestImpl {

  private java.awt.CardLayout cl;
  Thread t;
  Panel panel;
  boolean remove = true;

  public CardLayoutRemove2() {
    cl = new CardLayout();
    setLayout(cl);
    Panel p = new Panel();
    p.add(new Button("ok"));
    p.add(new Button("ok"));
    p.add(new Label("if you see this the test succeeded"));
    add(p,"1");
    panel = new Panel();
    panel.add(new Button("no"));
    panel.add(new Label("Please wait ... "));
    panel.add(new Label("this card will be removed"));
    add(panel,"2");
    cl.next(this);

  }

  public void start(java.awt.Panel p, boolean b){
    if(!b){
      System.out.println("STARTING REMOVER THREAD");
      remove = true;
      t = new Thread(new PanelRemover(this),"REMOVER THREAD");
      t.start();
    }
  }

  public void stop(java.awt.Panel p){
    remove = false;
    try{
      t.interrupt();
    }
    catch(RuntimeException re){}
  }


  public String getHelpText(){
    return "this test will try to remove the panel shown by the card layout. When it is removed it the cardlayout should"+
    " draw another card on the screen";
  }

  private static class PanelRemover implements Runnable {

    CardLayoutRemove2 clr;

    public PanelRemover(CardLayoutRemove2 clr2){
      clr = clr2;
    }

    public void run(){
      try{
        Thread.sleep(2000);
      }
      catch(InterruptedException ie){}
      if(clr.remove){
        System.out.println("REMOVING PANEL");
        clr.remove(clr.panel);
      }
      clr.t = null;
    }
  }

}
