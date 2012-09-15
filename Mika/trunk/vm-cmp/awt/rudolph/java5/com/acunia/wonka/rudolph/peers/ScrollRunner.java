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

import java.awt.event.*;
import java.awt.*;

/*
** Interface for Scrollbar, List, Textarea... using ScrollRunner to manage its mouse-down scrolling
** All classes calling ScrollRunner are called back by <theParent.repaint(thePainter)>,
** so they ust implement such function
*/

public class ScrollRunner extends Thread {
  
  /*
  ** constants
  */
  
  public final static long SCROLLSENSITIVITY = 100L;
  
  /*
  ** border scrolling area
  */
  
  public final static int SCROLL_STOP = -1;
  public final static int SCROLL_UP = 1;
  public final static int SCROLL_DOWN = 2;

  /*
  ** variables
  */
  
  //our scrollpainter
  private ScrollPainter thePainter;
  private Component theParent;
  private int mouseScroll;

  /*
  ** Constructors
  */
  
  /*
  ** Default values: vertical scrollbar, range 0-100, visible scrollbox 1
  */
  
  public ScrollRunner() {
    super("<List Scrolling Runner>");
    thePainter = null;
    theParent = null;
    mouseScroll = SCROLL_STOP;
    setDaemon(true);
    this.start();
  }

  /*
  ** set variables & (re)start thread
  */

  public synchronized void setRunner(ScrollPainter painter, Component parent, int mouse) {
    thePainter = painter;
    theParent = parent;
    mouseScroll = mouse;
    notifyAll();
  }

  public synchronized void setRunner(ScrollPainter painter, Component parent) {
    thePainter = painter;
    theParent = parent;
    mouseScroll = SCROLL_STOP;
    notifyAll();
  }

  /*
  ** set variables & (re)start thread
  */

  public synchronized void updateMouseScroll(int newvalue) {
    mouseScroll = newvalue;
  }

  /*
  ** set variables & (re)start thread
  */

  public synchronized void stopRunner(ScrollPainter painter) {
    painter.setNoSelected();
    mouseScroll = SCROLL_STOP;
    if(theParent!= null && thePainter!=null) {
      notifyAll();
    }
  }

  /*
  **  Mouse listener runnable method:
  ** as long as the mouse is clicked on either a lineup/linedown box or in the pageup/pagedown areas
  ** move the scrollbox up or down every <SCROLLSENSITIVITY> / 1000 seconds
  */
  
  public void run() {
    int active;
    boolean running;
    while(true) {
      while(thePainter==null || theParent==null) {
      synchronized(this) {
          try {
            wait();
						wait(250);
          }
          catch(InterruptedException e) {
            System.out.println(e.toString() );
          }
        }
      }
      // new scrollbar thread
      // new scrollbar commands\
      active= thePainter.getActive();
      running = (active>RudolphScrollbarPeer.FIELD_NONESELECTED || mouseScroll>SCROLL_STOP);

      while(running) {
        // move the scrollbar
        if(theParent instanceof Scrollbar) {
          if(active == AdjustmentEvent.UNIT_DECREMENT && thePainter.lineUp()) {
            ((DefaultScrollbar)theParent.getPeer()).processAdjustmentEvent(active);
          }
          else if(active == AdjustmentEvent.UNIT_INCREMENT && thePainter.lineDn()) {
            ((DefaultScrollbar)theParent.getPeer()).processAdjustmentEvent(active);
          }
          else if(active == AdjustmentEvent.BLOCK_DECREMENT  && thePainter.pageUp()) {
            ((DefaultScrollbar)theParent.getPeer()).processAdjustmentEvent(active);
          }
          else if(active == AdjustmentEvent.BLOCK_INCREMENT  && thePainter.pageDn()) {
            ((DefaultScrollbar)theParent.getPeer()).processAdjustmentEvent(active);
          }
          else {
            running = false;
          }        
        }
        else if (theParent instanceof List) {
          if(active == AdjustmentEvent.UNIT_DECREMENT && thePainter.lineUp()) {
            ((DefaultList)theParent.getPeer()).repaint(thePainter);
          }
          else if(active == AdjustmentEvent.UNIT_INCREMENT && thePainter.lineDn()) {
            ((DefaultList)theParent.getPeer()).repaint(thePainter);
          }
          else if(active == AdjustmentEvent.BLOCK_DECREMENT  && thePainter.pageUp()) {
            ((DefaultList)theParent.getPeer()).repaint(thePainter);
          }
          else if(active == AdjustmentEvent.BLOCK_INCREMENT  && thePainter.pageDn()) {
            ((DefaultList)theParent.getPeer()).repaint(thePainter);
          }
          else if(mouseScroll == SCROLL_UP  && thePainter.lineUp()) {
            ((DefaultList)theParent.getPeer()).repaint(thePainter);
          }
          else if(mouseScroll == SCROLL_DOWN  && thePainter.lineDn()) {
            ((DefaultList)theParent.getPeer()).repaint(thePainter);
          }
          else {
            running = false;
          }
        }
        else if (theParent instanceof TextArea) {
          if(active == AdjustmentEvent.UNIT_DECREMENT && thePainter.lineUp()) {
            ((DefaultTextArea)theParent.getPeer()).repaint(thePainter);
          }
          else if(active == AdjustmentEvent.UNIT_INCREMENT && thePainter.lineDn()) {
            ((DefaultTextArea)theParent.getPeer()).repaint(thePainter);
          }
          else if(active == AdjustmentEvent.BLOCK_DECREMENT  && thePainter.pageUp()) {
            ((DefaultTextArea)theParent.getPeer()).repaint(thePainter);
          }
          else if(active == AdjustmentEvent.BLOCK_INCREMENT  && thePainter.pageDn()) {
            ((DefaultTextArea)theParent.getPeer()).repaint(thePainter);
          }
          else if(mouseScroll == SCROLL_UP  && thePainter.lineUp()) {
            ((DefaultTextArea)theParent.getPeer()).repaint(thePainter);
          }
          else if(mouseScroll == SCROLL_DOWN  && thePainter.lineDn()) {
            ((DefaultTextArea)theParent.getPeer()).repaint(thePainter);
          }
          else {
            running = false;
          }
        }
        else {
          running = false;
        }
        // wait and get the latest events
        if(running){
          // sleep
          synchronized(this) {
            try {
              wait(SCROLLSENSITIVITY);
            }
            catch(InterruptedException e) {
              System.out.println(e.toString() );
            }
            // new scrollbar commands\
            active= thePainter.getActive();
            running = (active>RudolphScrollbarPeer.FIELD_NONESELECTED || mouseScroll>SCROLL_STOP);
          }
        }
      }
      // ok, this painter and scrollbar is done with...
      thePainter=null;
      theParent=null;
    }
  }

}

