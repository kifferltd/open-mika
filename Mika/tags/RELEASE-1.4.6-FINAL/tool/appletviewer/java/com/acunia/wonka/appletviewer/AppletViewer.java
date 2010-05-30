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


package com.acunia.wonka.appletviewer;

import java.applet.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.util.*;

public class AppletViewer extends Frame implements ActionListener, AppletStub, AppletContext, AudioClip {
  
  private static Toolkit toolkit = Toolkit.getDefaultToolkit();
  private static Hashtable applets = new Hashtable(17);
  private static String encoding = "8859_1";
  private static int counter = 1;

  private URL codebase;
  private URL documentbase;
  private Class appletClass;
  private Properties parameters;
  private boolean active;
  private String align = null;
  Applet applet;
  int hspace;
  int vspace;
  int appletH = 234;
  int appletW = 400;

  //GUI elements ...
  private Label status;
  private Button start;
  private Button stop;
  private Button destroy;
  private Panel buttons;
  //end GUI elements ...

  private AppletViewer(String location) throws Exception {
    super("AppletViewer");
    setupAppletData(location);

    start = new Button("start");
    start.addActionListener(this);
    stop = new Button("stop");
    stop.addActionListener(this);
    destroy = new Button("destroy");
    destroy.addActionListener(this);
    buttons = new Panel(new GridLayout(3,1));
    buttons.add(start);
    buttons.add(stop);
    buttons.add(destroy);
    this.add(buttons, BorderLayout.EAST);
    status = new Label(applet.getName());
    this.add(status,BorderLayout.SOUTH);
    this.setTitle("AppletViewer: '"+applet.getName()+"'");
    this.add(new WrapperPanel(this),BorderLayout.CENTER);
    this.setFrameSize();
  }

// ActionListener API ...
  public void actionPerformed(ActionEvent event){
    Object button = event.getSource();
    if(button == start){
      active = true;
      applet.start();
    }
    else if(button == stop){
      active = false;
      applet.stop();
    }
    else if(button == destroy){
      active = false;
      applet.stop();
      applet.destroy();
      this.dispose();
    }
  }
// end ActionListener.

//AppletContext API ...
  public Applet getApplet(String name){
    return (Applet)applets.get(name);
  }

  public Enumeration getApplets(){
    return applets.elements();
  }

  public AudioClip getAudioClip(URL url){
    return this;
  }

  public Image getImage(URL url){
    return toolkit.createImage(url);
  }

  public void showDocument(URL url) {
    System.err.println("showDocument is called "+url);
    //TODO ...
    throw new UnsupportedOperationException("'showDocument(URL url)' is not supported");
  }
  public void showDocument(URL url, String frame){
    try {
      showDocument(new URL(url,frame));
    }
    catch(MalformedURLException murle){}
  }

  public void showStatus(String msg){
    System.out.println("showStatus: "+msg);
    status.setText(msg);
  }
// end AppletContext.

//AppletStub API ...
  public void appletResize(int width, int height){
    //System.out.println("appletResize is called for "+applet+" w = "+width+", h = "+height);
    if(width > 0 && height > 0){
      appletW = width;
      appletH = height;
      setFrameSize();
    }
  }

  public AppletContext getAppletContext(){
    return this;
  }

  public URL getCodeBase(){
    return codebase;
  }

  public URL getDocumentBase(){
    return documentbase;
  }

  public String getParameter(String name){
    return parameters.getProperty(name);
  }

  public boolean isActive(){
    return active;
  }
// end AppletStub.

//AudioClip API ...
  public void play(){}
  public void stop(){}
  public void loop(){}
// end AudioClip

  private void readParams(String page){
    String lowerCasePage = page.toLowerCase();
    parameters = new Properties();
    try {
      int i = lowerCasePage.indexOf("param name=");
      while (i != -1){
        i += 11;
        int j = lowerCasePage.indexOf("value=",i);
        String name = unquoteString(page.substring(i,j).trim());
        j += 6;
        i = lowerCasePage.indexOf('>',j);
        parameters.setProperty(name, unquoteString(page.substring(j,i).trim()));
        i = lowerCasePage.indexOf("param name=", i);
      }
    }
    catch(RuntimeException e){
      e.printStackTrace();

    }
  }

  private void setFrameSize(){
    int width = appletW + buttons.getPreferredSize().width + 2 * hspace;
    int height = appletH + status.getPreferredSize().height + 2 * vspace;
    this.setSize(width,height);
  }

  private void setupAppletData(String location) throws Exception {
    try {
      documentbase = new URL(location);
    }
    catch(MalformedURLException mue){
      documentbase = new URL(new File("").toURL(), location);
    }
    InputStream in = documentbase.openStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] bytes = new byte[4096];
    int len = in.read(bytes);
    while(len != -1){
      out.write(bytes, 0, len);
      len = in.read(bytes);
    }
    String content = new String(out.toByteArray(),encoding);
    String base = documentbase.getFile();
    int index = base.lastIndexOf('/');
    if(index != -1){
      base = base.substring(0,index+1);
    }
    documentbase = new URL(documentbase.getProtocol(), documentbase.getHost(), base);

    //lets parse the content off the html page ...
    index = content.indexOf("<applet");
    int end = -1;
    int close = -1;
    if(index != -1){
      end = content.indexOf("</applet>", index);
      close = content.indexOf(">", index);
    }
    else {
      index = content.indexOf("<APPLET");
      if(index != -1){
        end = content.indexOf("</APPLET>", index);
        close = content.indexOf(">", index);
      }
      else {
        throw new IllegalArgumentException("no applet specified in html code");
      }
    }
    if(end < close || close == -1 || end == -1){
      throw new IllegalArgumentException("bad html syntax encountered");
    }
    String code = null;
    String archive = null;
    String cb = null;
    String name = null;

    StringTokenizer st = new StringTokenizer(content.substring(index+7,close));
    while(st.hasMoreTokens()){
      String token = st.nextToken();
      index = token.indexOf('=');
      if(index == -1){
        System.err.println("AppletViewer: bad token encountered '"+token+"'");
        continue;
      }
      String option = token.substring(0,index);
      if(option.equalsIgnoreCase("code")){
        code = unquoteString(token.substring(index+1));
        if(code.endsWith(".class")){
          code = code.substring(0,code.length()-6);
        }
      }
      else if(option.equalsIgnoreCase("archive")){
        archive = unquoteString(token.substring(index+1));
        while(archive.endsWith(",") && st.hasMoreTokens()){
          archive = archive + st.nextToken();
        }
      }
      else if(option.equalsIgnoreCase("name")){
        name = unquoteString(token.substring(index+1));
      }
      else if(option.equalsIgnoreCase("codebase")){
        cb = unquoteString(token.substring(index+1));
      }
      else if(option.equalsIgnoreCase("width")){
        try {
          int value = Integer.parseInt(unquoteString(token.substring(index+1)));
          if(value > 0){
            appletW = value;
          }
        }
        catch(RuntimeException rt){}
      }
      else if(option.equalsIgnoreCase("height")){
        try {
          int value = Integer.parseInt(unquoteString(token.substring(index+1)));
          if(value > 0){
            appletH= value;
          }
        }
        catch(RuntimeException rt){}
      }
      else if(option.equalsIgnoreCase("vspace")){
        try {
          int value = Integer.parseInt(unquoteString(token.substring(index+1)));
          if(value > 0){
            vspace = value;
          }
        }
        catch(RuntimeException rt){}
      }
      else if(option.equalsIgnoreCase("hspace")){
        try {
          int value = Integer.parseInt(unquoteString(token.substring(index+1)));
          if(value > 0){
            hspace = value;
          }
        }
        catch(RuntimeException rt){}
      }
      else if(option.equalsIgnoreCase("align")){
        align = unquoteString(token.substring(index+1));
        //TODO ... do alignment !
      }
    }

    readParams(content.substring(close,end));

    codebase = (cb != null) ? new URL(documentbase, cb) : documentbase;

    AppletClassLoader loader =  AppletClassLoader.createAppletClassLoader(codebase, documentbase, archive);
    appletClass = Class.forName(code, true, loader);
    applet = (Applet) appletClass.newInstance();

    /**
    ** we make the applet invisible.  This will prevent the paint method to be called before init is called.
    */
    applet.setVisible(false);
    applet.setStub(this);

    if (name == null){
      name = "applet nr "+counter+" of type "+appletClass;
      counter++;
    }
    applet.setName(name);
    applets.put(applet.getName(),applet);
  }

  private String unquoteString(String quoted){
    if(quoted.startsWith("\"")){
      quoted = quoted.substring(1);
    }
    if(quoted.endsWith("\"")){
      quoted = quoted.substring(0,quoted.length()-1);
    }
    return quoted;
  }

  protected static class WrapperPanel extends Panel {

    private AppletViewer view;

    WrapperPanel(AppletViewer view){
      super(null);
      this.setBackground(Color.white);
      this.view = view;
      this.add(view.applet);
    }

    public void setBounds(int x, int y, int width, int height){
      super.setBounds(x,y,width,height);
      view.applet.setBounds(view.hspace, view.vspace, view.appletW, view.appletH);
    }
  }

// The main method ...

  public static void main(String[] args){
    for(int i=0 ; i < args.length ; i++){
      try {
        AppletViewer view = new AppletViewer(args[i]);
        view.applet.init();
        view.applet.setVisible(true);
        view.active = true;
        view.applet.start();
        view.setVisible(true);
        view.validate();
      }
      catch(Exception e){
        System.err.println("failed to load '"+args[i]+"' due to "+e);
        e.printStackTrace();
      }
    }
  }
}

