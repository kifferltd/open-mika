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

package java.applet;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Locale;
import java.awt.Panel;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.IllegalComponentStateException;

import com.acunia.wonka.rudolph.AppletAudioClip;
/**
** The use of most Applet methods rely on the stub provided by the setStub method.
** There are no checks to verify wether to stub is null or not.  This will result in NUllPointerException
** when if the setStub method was not called.
**
*/
public class Applet extends Panel {

  private static final long serialVersionUID = -5836846270535785031L;

  private AppletStub stub;

  /** note default constructor only. */
  public Applet(){}

  public final void setStub(AppletStub stub){
    if (stub == null){
      throw new NullPointerException("a non null-stub is required");
    }
    this.stub = stub;
  }

//METHODS TO OVERRIDE

  public void destroy(){}
  public void init(){}
  public void start(){}
  public void stop(){}

  public String getAppletInfo(){
    return null;
  }

  public String[][] getParameterInfo(){
     return null;
  }

// Stub based methods
  public AppletContext getAppletContext(){
    return stub.getAppletContext();
  }

  public AudioClip getAudioClip(URL url){
    return stub.getAppletContext().getAudioClip(url);
  }

  public AudioClip getAudioClip(URL url, String name){
    try {
      return stub.getAppletContext().getAudioClip(new URL(url, name));
    } catch(MalformedURLException me){
      return null;
    }
  }

  public URL getCodeBase(){
    return stub.getCodeBase();
  }

  public URL getDocumentBase(){
    return stub.getDocumentBase();
  }

  public Image getImage(URL url){
    return stub.getAppletContext().getImage(url);
  }

  public Image getImage(URL url, String name){
    try {
      return stub.getAppletContext().getImage(new URL(url, name));
    } catch(MalformedURLException me){
      return null;
    }
  }

  public String getParameter(String name){
    return stub.getParameter(name);
  }

  public boolean isActive(){
    return stub.isActive();
  }

  public void play(URL url){
    AudioClip ac = stub.getAppletContext().getAudioClip(url);
    if(ac != null){
      ac.play();
    }
  }

  public void play(URL url, String name){
    try {
      AudioClip ac = stub.getAppletContext().getAudioClip(new URL(url, name));
      if(ac != null){
        ac.play();
      }
    } catch(MalformedURLException me){
    }
  }

  public void resize(Dimension d){
     stub.appletResize(d.width, d.height);
  }

  public void resize(int width, int height){
     stub.appletResize(width, height);
  }

  public void showStatus(String message){
     stub.getAppletContext().showStatus(message);
  }
// other convenience methods ...
  public Locale getLocale(){
    try {
       return super.getLocale();
    } catch(IllegalComponentStateException e){
      return Locale.getDefault();
    }
  }

  public static final AudioClip newAudioClip(URL url) {
    return new AppletAudioClip(url);
  }
}
