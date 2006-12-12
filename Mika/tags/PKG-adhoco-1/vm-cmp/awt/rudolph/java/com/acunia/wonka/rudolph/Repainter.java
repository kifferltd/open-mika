package com.acunia.wonka.rudolph;

import java.util.*;
import com.acunia.wonka.rudolph.peers.*;

public class Repainter extends Thread {

  private Vector list = new Vector();
  
  private static Repainter rp;

  public static Repainter getInstance() {
    if(rp == null) {
      rp = new Repainter();
      rp.setDaemon(false);
      rp.setPriority(9);
      rp.start();
    }
    return rp;
  }

  private Repainter() {
    super("Dali");
  }

  public synchronized void repaint(DefaultComponent peer) {
    if(!list.contains(peer)) {
      list.add(peer);
    }
    notify();
  }

  public void run() {
    DefaultComponent component;
    boolean keepgoing;

    while(true) {
      try {
        synchronized(this) {
          wait();
          keepgoing = true;
        }
        while(keepgoing) {

          synchronized(list) {
            if(list.size() != 0) {
              component = (DefaultComponent)list.elementAt(0);
              list.remove(0);
            }
            else {
              keepgoing = false;
              break;
            }
          }

          try {
            component.doRepaint();
          }
          catch(Exception t) {
            t.printStackTrace();
          }
        }
      }
      catch (Exception exc) {
        try {
          exc.printStackTrace();
        }
        catch (Exception exc2) {
        }
      }
      catch (Error err) {
        try {
          System.out.println("Repainter thread threw " + err + "!");
          err.printStackTrace();
        }
        finally {
          System.exit(253);
        }
      }
    }
  }
}

