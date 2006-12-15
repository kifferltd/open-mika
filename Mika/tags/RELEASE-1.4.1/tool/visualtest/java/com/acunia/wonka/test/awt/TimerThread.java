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


package com.acunia.wonka.test.awt;

import java.awt.Label;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TimerThread implements Runnable, MouseListener {

  private static final long MAX_TIME = 10 * 60 * 1000; //10 minutes
  private static final long MIN_TIME =      10 * 1000; //10 seconds

  private long report_interval = MAX_TIME;
  private VisualTestEngine vte;
  private Label info;
  private boolean stop = false;
  private long total = Runtime.getRuntime().totalMemory();
  private boolean show_memory = true;
  private long lastLog;
  private long starttime;
 	
  public TimerThread(VisualTestEngine vte, Label i, ThreadGroup tg) {
    this.vte = vte;
    info = i;
    info.addMouseListener(this);
    Thread t = new Thread(tg, this, "VisualTestEngine Timer Thread");
    t.setPriority(8);
    t.start();
  }
 	
  public void mouseClicked(MouseEvent event) {
    show_memory = show_memory ? false : true;
    info.invalidate();
  }
  
  public void mouseEntered(MouseEvent event) {}
  public void mouseExited(MouseEvent event) {}
  public void mousePressed(MouseEvent event) {}
  public void mouseReleased(MouseEvent event) {}

  public void stop() {
    stop = true;
  }

  private String status() {
    String msg = "TimerThread autorun status: \n";
    msg = msg + "\tcompleted "+vte.runCount+" runs in ";
    long millis = System.currentTimeMillis() - starttime;
    msg = msg + millis + " ms = ";
    int sex = (int)(millis / 1000);
    int seconds = sex % 60;
    int minutes = ((sex - seconds) / 60) % 60;
    int hours = ((sex - seconds - minutes * 60) / 3600) % 24;
    int days = (sex / 3600 / 24);
    if (days > 0) {
      msg = msg + days + " days, ";
    }
    if (hours > 0) {
      msg = msg + hours + " hours, ";
    }
    if (minutes > 0) {
      msg = msg + minutes + " minutes, ";
    }
    msg = msg + seconds + " seconds.\n";
    msg = msg + "\tcurrently testing "+vte.getCurrentTest();

    return msg;
  }
	
  public void run() {
    starttime = System.currentTimeMillis();
    while (!stop) {
      try {
      	Thread.sleep(500);
      	String stats;
      	long free = Runtime.getRuntime().freeMemory();
        Summary.freeMemory(free);
        if (show_memory == true) {	
          stats = "memory: "+ (total - free) + " / " + total +" bytes";
        }
        else {
          stats = "round trip time: "+ vte.getRoundTrip() +" ms";
        }
        info.setText(stats);
        info.invalidate();

        if(vte.autorun) {
          long time = System.currentTimeMillis();
          if(time > lastLog + report_interval){
            VisualTest vt = vte.getCurrentTest();
            if(vt != null){
              vte.log(status() + "\n\t" + stats, vt);
            }
            lastLog = time;
          }
          setInterval(free);
        }
      }
      catch(Exception e){
        e.printStackTrace();
        VTELogger.reportException("TimerThread: exception occured",e,true);
      } 	
    }
  }

  private void setInterval(long free){
    float factor = (float)free / (float)total;
    if(factor > 0.60f){
      report_interval = MAX_TIME;
    }
    else {
      report_interval = (int)((1.0f+factor) * factor * factor * factor * MAX_TIME);
      if(report_interval < MIN_TIME){
        report_interval = MIN_TIME;
      }
    }
  }
}
