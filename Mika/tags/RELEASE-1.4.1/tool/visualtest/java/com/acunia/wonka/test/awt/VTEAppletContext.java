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
import java.net.*;
import java.util.*;

public class VTEAppletContext implements AppletContext {

  private Hashtable applets;
  private VTEAudioClip vteAudio = new VTEAudioClip();

  public Applet getApplet(String name){
    return (Applet)applets.get(name);
  }

  public Enumeration getApplets(){
    return applets.elements();
  }

  public AudioClip getAudioClip(URL url){
    return vteAudio;
  }

  public Image getImage(URL url){
    return null;
  }

  public void showDocument(URL url) {
    System.out.println("showDocument is called "+url);
  }
  public void showDocument(URL url, String frame){
    System.out.println("showDocument is called "+url+" (frame = "+frame+")");
  }
  public void showStatus(String msg){
    System.out.println("showStatus is called "+msg);
  }

  protected static class VTEAudioClip implements AudioClip {

     public VTEAudioClip(){}

     public void play(){}
     public void stop(){}
     public void loop(){}

  }

}














