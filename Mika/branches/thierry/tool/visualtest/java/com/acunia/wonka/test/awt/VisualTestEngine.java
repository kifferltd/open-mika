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

//import com.acunia.wonka.Realm;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

public class VisualTestEngine extends Frame implements ActionListener, VisualTester {
	
  private final static String SELECTEDSTAR="(*)";
  private final static String DESELECTSTAR="( )";

  private static String disp = "display";
  private static String help = "help";
  private static String menu = "menu";
	
  private static String prev = "prev";
  private static String next = "next";
  private static String ok = "ok";
  private static String bad = "bad";
  private static String back = "back";
  private static String ret = "return";
  private static String auto = "autorun";
  private static String quit = "quit";
  private static String exit = "  exit  ";
  private static String justK = "  just kidding  ";
  static ThreadGroup tg = new ThreadGroup("VTE");

  private long millis1 = 0;
  private long millis2 = 0;

  public static void main(String[] args) {
    try {
       new File("vte.log").delete();
       VTELogger.logMessage("Starting the VisualTestEngine ...", true);
       VisualTestEngine vte = new VisualTestEngine(args);
       vte.setVisible(true);	 	
       ((CardLayout)vte.getLayout()).show(vte,disp);
    } 	
    catch (Exception e) {
      VTELogger.reportException("FAILED TO START VISUALTESTENGINE\n",e, true);
      System.out.println(e);
      e.printStackTrace();
      System.exit(0);
    }
  }

  private String[] args;
  private String[] strippedNames;
  private Vector idx_vector = new Vector(5, 3);
  private int index = -1;
  private int idx = 0;
  private boolean update = false;
  private int h=234;
  private int w=400;
  private int memoryLogIndex = -1;

  //Autorun features ...
  private long sleepingTime = 2000;
  private long autorunTime = 0;
  private boolean exitAfterAutorun;
  boolean autorun;
  int runCount;

  private Label title;
  private TextArea helpText;
  private java.awt.List testlist;
  private Panel displayPanel;
  private Runtime runtime;
	
  private VisualTest test;
  private Panel testpanel;
  private AutoRunner autorunner;

  private VTEAppletContext vteContext = new VTEAppletContext();

  public VisualTestEngine(String[] args) {
    super("Visual Test Engine");
   
    setBackground(Color.lightGray);
    setArgs(args);
    stripPackageName();
    setSize(w, h);

    displayPanel = new Panel(new BorderLayout());
    Panel helpPanel = new Panel(new BorderLayout());
    Panel menuPanel = new Panel(new BorderLayout());
    Panel north = new Panel(new GridLayout(1, 2));
    title = new Label("No test loaded." , Label.CENTER);
    title.setFont(new Font("courier", Font.PLAIN, 12));
    runtime = Runtime.getRuntime();
    Label info = new Label("free " + runtime.freeMemory() + " - total " + runtime.totalMemory(), Label.CENTER);
    north.add(title);
    north.add(info);
    displayPanel.add(north, BorderLayout.NORTH);
    displayPanel.setSize(350, 234);
 	
    // East panel:
    Panel east = new Panel(new GridLayout(6, 1));             	 	
    east.add(makeButton(next)); 	 	               	
    east.add(makeButton(ok)); 	               	
    east.add(makeButton(bad));	       	 	
    east.add(makeButton(prev)); 	
    east.add(makeButton(menu));
    east.add(makeButton(help));
    
    // Display panel:
    displayPanel.add(east, BorderLayout.EAST);
    setLayout(new CardLayout());       	
    add(displayPanel, disp);

    // Help panel:	       	
    helpText = new TextArea("No help available.", 30, 10, TextArea.SCROLLBARS_VERTICAL_ONLY);
    helpPanel.add(helpText, BorderLayout.CENTER);
    helpPanel.add(makeButton(back),	BorderLayout.EAST);
    add(helpPanel, help);

    // Make test list:	       		       	
    testlist = new java.awt.List();
    testlist.setFont(new Font("courier", Font.PLAIN, 17));
    
    for (int i = 0; i < this.args.length; i++) {
      testlist.add("( ) " + this.strippedNames[i]);
    }
    
    testlist.addActionListener(this);
 
    // Make controls:
    east = new Panel(new GridLayout(3, 1));
    east.add(makeButton(quit));       	 	
    east.add(makeButton(auto));
    east.add(makeButton(ret));            	 	

    menuPanel.add(testlist, BorderLayout.CENTER);
    menuPanel.add(east, BorderLayout.EAST);
    add(menuPanel,menu);

    // make the quit query panel
    Panel quitPanel = new Panel(new BorderLayout());
    quitPanel.setFont(new Font("courier",0,18));
    Label quitMessage = new Label("Do you really want to quit ?",Label.CENTER);
    quitMessage.setBackground(Color.red);
    quitPanel.add(quitMessage  ,BorderLayout.NORTH);
    Panel bpanel = new Panel();
    Button cbutton = makeButton(exit);
    cbutton.setBackground(Color.red);
    bpanel.add(cbutton);
    cbutton = makeButton(justK);
    cbutton.setBackground(Color.green);
    bpanel.add(cbutton);

    quitPanel.add(bpanel ,BorderLayout.CENTER);

    add(quitPanel,exit);

    if (autorun) {
      update = false;
      millis2 = System.currentTimeMillis();
      Summary.init(millis2);
      autorunner = new AutoRunner(this, sleepingTime, autorunTime, exitAfterAutorun);
      runCount = 0;
      shuffle(disp);
    }
    else { 
      LoadTest();
    }

    new TimerThread(this, info, tg);
  }

  public long getRoundTrip() {
    return millis2 - millis1;
  }

// ActionListener methods ...
  public void actionPerformed(ActionEvent e) {
    String id = e.getActionCommand();
    if (autorun) {
      index = 0;
      idx_vector.clear();
      autorun = false;
      autorunner.stop();

//      return;
    }

    if (ok.equals(id)) {	
      Summary.testOK();
      log(true, args[idx]);
    }
    else if (bad.equals(id)) {
      Summary.testBad();
      log(false, args[idx]);		
    }
    else if (menu.equals(id)) {
      testlist.removeAll();
      for (int i = 0; i < this.args.length; i++) {
        testlist.add("( ) " + this.strippedNames[i]);
      }
      testlist.select(idx);
      test.hideTest();
      shuffle(id);
    }
    else if (help.equals(id)) {
      test.hideTest();
      shuffle(id);
    }
    else if (back.equals(id)) {
      test.showTest();
      shuffle(disp);
    }
    else if (ret.equals(id)) {
      idx_vector.clear();
      if (update) {
        update = false;
        --index;
        LoadTest();
      }
      test.showTest();
      shuffle(disp);
    }
    else if (next.equals(id)) {
      if (++idx == args.length) {
        idx = 0;
      }
      LoadTest();
    }
    else if (prev.equals(id)) {
      if (--idx == -1) {
        idx = args.length - 1;
      }
      LoadTest();
    }
    else if (testlist.equals(e.getSource())) {
      int testindex = testlist.getSelectedIndex();
      Integer testindex_integer = new Integer(testindex);

      String testname = testlist.getSelectedItem();
      String newname;
      if(testname.startsWith(SELECTEDSTAR)) {
        // deselect a selected item
        idx_vector.remove(testindex_integer);
        // set test string deselected
        newname = DESELECTSTAR + testname.substring(3);
      }
      else {
        // select a deselected item
        idx_vector.add(testindex_integer);
        idx = testindex;
        // set test string selected
        newname = SELECTEDSTAR + testname.substring(3);
      }
      //System.out.println("replaced pos "+testindex+" with <"+newname+"> ");
      testlist.replaceItem(newname, testindex);
      testlist.select(testindex);
      update = true;
    }
    else if (auto.equals(id)) {
      index = idx_vector.size() - 1;
      update = false;
      autorun = true;
      millis2 = System.currentTimeMillis();
      autorunner = new AutoRunner(this, sleepingTime, autorunTime, exitAfterAutorun);
      runCount = 0;
      test.showTest();
      shuffle(disp);	
    }
    else if (quit.equals(id)) {
      shuffle(exit);
    }
    else if (justK.equals(id)) {
      shuffle(menu);
    }
    else if (exit.equals(id)) {
      loadSummary("'exit' button was pushed");
      System.exit(0);
    }
  }
		
// VisualTester methods ...

  public void log(String log, VisualTest v) {
    VTELogger.log(log, v);
  }	

  public void logException(String log, VisualTest v, Throwable ex) {
    VTELogger.logException(log, v, ex);
  }	

	public VisualTest getCurrentTest(){
	  return test;
	}

  public Frame getFrame() {
    return this;
  }

// private methods
  private Button makeButton(String s) {
    Button b = new Button(s);
    b.addActionListener(this);
    return b;		         	
  }

  private void setArgs(String[] arg) {
    try {	
      if (arg.length == 0) {
        Properties al = new Properties();
        al.load(getClass().getResourceAsStream("/vte.properties"));		
				System.getProperties().putAll(al);
     
        autorun = Boolean.getBoolean("property.autorun");
				al.remove("property.autorun");
 
        exitAfterAutorun = Boolean.getBoolean("property.exitAfterAutorun");
				al.remove("property.exitAfterAutorun");

        autorunTime = Long.getLong("property.autorunTime", 0).longValue();
        al.remove("property.autorunTime");

        sleepingTime = Long.getLong("property.sleepTime", 2000).longValue();
        al.remove("property.sleepTime");
        al.remove("property.path");

        memoryLogIndex = Integer.getInteger("property.memoryLogIndex", -1).intValue();
        al.remove("property.memoryLogIndex");

        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

        w = Integer.getInteger("property.width", -1).intValue();
        if(w < 0){
          w = (int)size.getWidth();
        }
        al.remove("property.width");

        h = Integer.getInteger("property.height", -1).intValue();
        if(h < 0){
          h = (int)size.getHeight();
        }
        al.remove("property.height");
     	  	
        System.out.println("Request size is w = "+w+", h = "+h);
             	
     	  ArrayList l = new ArrayList();
        arg = new String[al.size()];
        Enumeration e = al.keys();
        while (e.hasMoreElements()) {
         	l.add(e.nextElement());
        }
        int i = 0;
        Collections.sort(l);
     	  Iterator it = l.iterator();
     	  while (it.hasNext()){
          arg[i] = (String)it.next();
	        i++;
          //System.out.println("added "+arg[i-1]+" on position "+i);
	      }	
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      VTELogger.reportException("PROBLEM OCCURED WHILE SETTING UP PROPERTIES AND ARGUMENTS\n",e, true);
    }
   	if (arg.length == 0) {
    	throw new IllegalArgumentException("No tests specified.");
    }
    args = arg;
  }

  private synchronized void LoadTest() {
    System.out.println("Loading test " + args[idx]);
    if (test != null) {
      // the call to testPanel.setVisible(false) should not be needed; this operation
      // should already have been done by displayPanel.remove(); but that does not
      // work if the test in the panel being removed used double buffering.
   	
      displayPanel.remove(testpanel);
      test.stop(testpanel);
      test = null;
    }

    if(autorun && idx == memoryLogIndex){
      this.validate();
      System.gc();
      System.gc();
      System.gc();
      VTELogger.logMessage("Logging memory -- no test Loaded", true);
    }

    while (true) {
      try {
        Object o = Class.forName(args[idx]).newInstance();
        if (o instanceof VisualTest){
          test = (VisualTest)o;
        }
        else{
          Applet applet = (Applet)o;
          test = new VTEAppletViewer(vteContext, applet);//we do cast here to trigger an exception if it is not applet
          applet.setStub((AppletStub)test);
        }
        title.setText(strippedNames[idx]);
        testpanel = test.getPanel(this);
       	helpText.setText(test.getHelpText());
       	displayPanel.add(testpanel, BorderLayout.CENTER);
	      this.validate();
	      test.start(testpanel, autorun);
    	}
     	catch (Throwable e) {
      	e.printStackTrace();
      	StringBuffer buf = new StringBuffer("\nwarning Exception cought while initializing ");
        buf.append(args[idx]);
      	if (args.length == 1){
      	  buf.append("\nNo more Tests left... Bye!\n");
      	 	System.out.print(buf);	
      	 	VTELogger.reportException(buf.toString(),e, true);
          System.exit(0);
      	}
      	buf.append("\nremoving ");
        buf.append(args[idx]);
      	buf.append(" from argument list\n");
      	String[] newArgs = new String[args.length - 1];
      	String[] newNames = new String[args.length - 1];
       	System.arraycopy(args, 0, newArgs, 0, idx);
      	System.arraycopy(args, idx + 1, newArgs, idx, args.length - idx - 1);
       	System.arraycopy(strippedNames, 0, newNames, 0, idx);
      	System.arraycopy(strippedNames, idx + 1, newNames, idx, args.length - idx - 1);
      	args = newArgs;
      	strippedNames = newNames;
        testlist.removeAll();
        for (int i = 0; i < this.args.length; i++) {
          testlist.add("( )" + this.strippedNames[i]);
        }
      	if (idx == args.length) {
          idx = 0;
        }
        VTELogger.reportException(buf.toString(),e, false);
      	continue;
      }
      break;
    }
    Summary.testLoaded();
  }

  public synchronized void loadSummary(String reason) {
    System.out.println("Loading summary");
    if (test != null) {
      displayPanel.remove(testpanel);
      test.stop(testpanel);
      test = null;
    }

    try {
      test = new Summary(reason);
      title.setText("Summary");
      testpanel = test.getPanel(this);
      helpText.setText(test.getHelpText());
      displayPanel.add(testpanel, BorderLayout.CENTER);
      this.validate();
      shuffle(disp);
    }
    catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private void shuffle(String id) {
    ((CardLayout)this.getLayout()).show(this, id);
  }

  private void log(boolean result, String name) {           	
    VTELogger.logTestResult(result, name, test, testpanel);
  }

  private void stripPackageName(){
    strippedNames = new String[args.length];
    for (int i=0 ; i < args.length ; i++){
      String arg = args[i];
      if (arg.startsWith("com.acunia.wonka.test.awt.")){
        String tmp = arg.substring(26);
        System.out.println("stripping '"+arg+"' to '"+tmp+"'");
        arg = tmp;
      }
      strippedNames[i] = arg;
    }
  }

// default acces API
  void stop() {
    autorun = false;
  }

  void nextAuto() {
    if ((autorun) && (index >= 0)) {
      if ((index) == idx_vector.size()) {
        millis1 = millis2;
        millis2 = System.currentTimeMillis();
        index = 0;
        runCount++;
      }
      idx = ((Integer)idx_vector.get(index)).intValue();
      LoadTest();
      index++;
    }
    else {
      if (++idx == args.length) {
        runCount++;
        idx = 0;
      }
      LoadTest();
    }
  }
}
