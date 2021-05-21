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

import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class VTEAppletViewer extends VisualTestImpl implements AppletStub {

  private static URL url;
  static {
    try {
      url = new URL("file","localhost","/");
    }
    catch(MalformedURLException ignore){
      System.out.println("caucht a MalformedURLException");
      ignore.printStackTrace();
    }
  }
  private VTEAppletContext vteContext;
  private Properties props = new Properties();
  private Applet applet;
  private boolean active;


  public VTEAppletViewer(VTEAppletContext vteContext, Applet applet){
    super(null);
    this.vteContext = vteContext;
    this.applet = applet;
    this.add(applet);
  }


// AppletStub methods ...
  public void appletResize(int width, int height){
    System.out.println("appletResize is called for "+applet+" w = "+width+", h = "+height);

    int x = this.getWidth();
    width = width > x ? x : width;
    x = (x - width) / 2;

    int y = this.getHeight();
    height = height > y ? y : height;
    y = (y - height) / 2;

    applet.setBounds(x, y, width, height);
  }

  public AppletContext getAppletContext(){
    return vteContext;
  }

  public URL getCodeBase(){
    return url;
  }

  public URL getDocumentBase(){
    return url;
  }

  public String getParameter(String name){
    return props.getProperty(name);
  }

  public boolean isActive(){
    return active;
  }

//VisualTest methods ...

  public void start(java.awt.Panel p, boolean b){
    if(this.getWidth() == 0 || this.getHeight() == 0){
      System.out.println("Validating VTEAppletViewer");
      this.validate();

    }

    props.setProperty("width", ""+this.getWidth());
    props.setProperty("height", ""+this.getHeight());
    props.setProperty("code", applet.getClass().getName());

    try {
      props.store(System.out,"Displaying Properties for "+applet);
    } catch(java.io.IOException ignore){}

    Dimension d = getAppletSize();

    int width = this.getWidth();
    int x = width;

    System.out.println(" preferredSize = "+d+" this ( w = "+x+", h = "+this.getHeight()+")");

    width = width > d.width ? d.width : width;
    width = width < 30 ? x : width;
    x = (x - width) / 2;

    int height = this.getHeight();
    int y = height;
    height = height > d.height ? d.height : height;
    height = height < 30 ? y : height;
    y = (y - height) / 2;

    System.out.println("setting bounds to "+x+", "+y+", "+width+", "+height);

    applet.setBounds(x, y, width, height);
    applet.init();
    active = true;
    applet.start();
  }

  public void stop(java.awt.Panel p){
    active = false;
    applet.stop();
    applet.destroy();
  }

  public String getHelpText(){
    return "you are currently looking at an applet"+applet.getClass().getName() +" loaded by the VTE\n"+
      "APPLET INFO :\n"+applet.getAppletInfo();
  }

  private Dimension getAppletSize(){
    try {
      ClassLoader cl = applet.getClass().getClassLoader();
      InputStreamReader in = new  InputStreamReader(cl.getResourceAsStream(props.getProperty("code")+".html"));
      StringBuffer buf = new StringBuffer();
      char[] c = new char[256];
      int rd = in.read(c);
      while(rd != -1){
        buf.append(c,0,rd);
        rd = in.read(c);
      }
      String s =  buf.toString();
      readParams(s);
      int w = s.indexOf("width=\"");
      int h = s.indexOf('"', w+7);
      w = Integer.parseInt(s.substring(w+7,h));
      h = s.indexOf("height=\"");
      int i = s.indexOf('"', h+8);
      System.out.println(s.substring(h+8,i)+"$"+(h+8)+"$"+i+"$"+s);
      h = Integer.parseInt(s.substring(h+8,i));
      props.setProperty("width", ""+w);
      props.setProperty("height", ""+h);
      props.store(System.out, "Properties for "+applet+" after a html pafe was read ");
      return new Dimension(w,h);
    } catch(Exception e){
       e.printStackTrace();
       System.out.println("Stack trace is for debugging purposes only");
      return applet.getPreferredSize();
    }
  }

  private void readParams(String page){
    try {
      int i = page.indexOf("param name=\"");
      while (i != -1){
        int j = i+12;
        i = page.indexOf('"',j);
        String name = page.substring(j,i);
        j = page.indexOf("value =\"")+7;
        i = page.indexOf('"',j);
        props.setProperty(name, page.substring(j,i));
        i = page.indexOf("param name=\"", i);
      }
    }
    catch(Exception e){}
  }

}














