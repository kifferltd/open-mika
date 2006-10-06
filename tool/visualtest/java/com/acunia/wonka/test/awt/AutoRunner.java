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

public class AutoRunner implements Runnable {

  private boolean stop = false;
  private boolean exit;
  private long runtime;
  private long sleep;
  private VisualTestEngine vte;
 	
  public AutoRunner (VisualTestEngine vte, long sleep, long runtime, boolean exitAferAutorun) {
    this.vte = vte;
    this.sleep = sleep;
    this.runtime = runtime;
    this.exit = exitAferAutorun;
    if(runtime != 0){
       this.runtime = System.currentTimeMillis() + runtime * 60 * 1000;
    }
    Thread t = new Thread(VisualTestEngine.tg, this, sleep < 0 ? "AutoRunner -- benchMark" : "AutoRunner");
    t.setPriority(6);
    t.start();
    VTELogger.logMessage("starting Autorun ...\n", false);
  }

  public void stop() {
    System.out.println("Stopping autorun");
    stop = true;
  }
 	
  public void run() {
    if(sleep < 0){
      benchMark();
    }
    try {
      while(!stop) {
        vte.nextAuto();
        try {
          Thread.sleep(sleep);
        }
        catch(InterruptedException ie) { }

        if(runtime != 0 && runtime < System.currentTimeMillis()){
          if(exit && !stop){
            System.out.println("Autorun timeout expired --> shutting down VTE");
            VTELogger.logMessage("Autorun timeout expired --> shutting down VTE", true);
            vte.loadSummary("autorun timeout expired");
            System.exit(0);
          }
          stop = true;
        }
      }
      vte.stop();
    }
    catch(Throwable t){
      vte.autorun = false;
      VTELogger.reportException("AutoRunner: Autorun thread is stopped due to exception",t,true);
    }

    VTELogger.logMessage("stopping autorun ...", true);
  }

  public void benchMark(){
    long time = System.currentTimeMillis();
    int count = 0;
    int testNeeded = (int)-sleep;
    try {
      while(!stop && count < testNeeded) {
        vte.nextAuto();
        count++;
      }
    }
    catch(Throwable t){
      VTELogger.reportException("AutoRunner: Autorun thread benchMark is stopped due to exception",t,true);
    }
    vte.autorun = false;
    long endtime = System.currentTimeMillis() - time;
    String report = "stopping benchMark ...\nran "+count+" tests (needed "+testNeeded+")\ntime elapsed = "+endtime;
    System.out.println(report);
    VTELogger.logMessage(report, true);
  }

}
