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



package com.acunia.wonka.test.awt.Component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class ComponentType extends VisualTestImpl implements Runnable {

  int grid = 8;
  private int stop = 0;

  class ComponentWorker extends Thread {
    public void run() {
      /*
      ** Remove a random component:
      */
      remove(getComponent((int)Math.floor(Math.random() * grid * grid)));

      /*
      ** Add a component:
      */
      add(new ComponentComponent());

      validate();
    }
  }

  class ComponentComponent extends Component {

    ComponentComponent() {
      super();
    }

    public void paint(Graphics g) {
      Dimension dim = this.getSize();
      if (dim.width > 0 && dim.height > 0) {
        g.setColor(new Color((int)Math.floor(Math.random() * 256), (int)Math.floor(Math.random() * 256), (int)Math.floor(Math.random() * 256)));
        g.fillRect(0, 0, dim.width, dim.height);
        g.setColor(Color.black);
        g.drawRect(0, 0, dim.width - 1, dim.height - 1);
      }
    }
  }

  public String getHelpText() {
    return "ComponentType tests various things.  Among them it tests whether we can directly extend java.awt.Component to render on it.  The test also tests the thread safety of Rudolph's main renderer thread by concurrently adding and removing components while continoously invoking invalidate/repaint requests.  The former works when you colorful blocks are displayed on the screen.  The later works when Rudolph plays nice with the refresh requests and simply when it doesn't crash ...";
  }

  public ComponentType() {
    setLayout(new GridLayout(grid, grid, 2, 2));
    for (int i = 0 ; i < grid * grid; i++){
      add(new ComponentComponent());
    }  	
  }

  public Dimension getPreferredSize() {
    return new Dimension(230, 230);
  }

  public void start(java.awt.Panel p, boolean autorun){
    try {
      (new Thread(this, "Component Managing Thread")).start();
    }
    catch(ClassCastException cce) {
    }
  }

  public void stop(java.awt.Panel p){
    try {
      stop = 1;
    }
    catch(ClassCastException cce) {
    }
  }

  public void run() {
    while (stop == 0) {
      try { 
        Thread.sleep(800);
        Thread worker = new ComponentWorker();
        worker.start();
        validate();
      } 
      catch (Exception e) {
      }
    }
  }
}
