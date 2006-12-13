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


package com.acunia.wonka.test.awt.event;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/************************************************************************************************************/
/** Base class for all xxxxEvent display classes
*/
public class WindowEventDisplay extends AWTEventDisplay implements WindowListener {

  /** variables */
//  protected Color background;
//  protected Color foreground;
//  protected String message;

//  protected Dimension frame;
//  protected Rectangle inside;
  protected int displayWidth;
  protected int displayHeight;
  /************************************************************************************************************/
  /** constructor
  */
  public WindowEventDisplay(String firstmessage, Color back, Color text, int width, int height) {
    super(firstmessage, back, text);
    displayWidth = width;
    displayHeight = height;
  }

  public WindowEventDisplay(String firstmessage, Color back, Color text) {
    this(firstmessage, back, text, 70, 25);
  }

  public WindowEventDisplay(Color back, Color text) {
    this("Your WindowEvents displayed HERE", back, text, 70, 25);
  }
  /************************************************************************************************************/
  /** Sizes
  */
  public Dimension getMinimumSize() {
    return new Dimension(displayWidth, displayHeight);
  }

  public Dimension getPreferredSize() {
    return new Dimension(displayWidth, displayHeight);
  }

  /************************************************************************************************************/
  /**Add xxxx-Listener functions here
  */
  public void windowActivated(WindowEvent evt) {
    message = "Window activated: "+displayWindowShortcut(evt);
    repaint();
  }

  public void windowDeactivated(WindowEvent evt) {
    message = "Window deactivated: "+displayWindowShortcut(evt);
    repaint();
  }

  public void windowIconified(WindowEvent evt) {
    message = "Window iconified: "+displayWindowShortcut(evt);
    repaint();
  }

  public void windowDeiconified(WindowEvent evt) {
    message = "Window de-iconified: "+displayWindowShortcut(evt);
    repaint();
  }

  public void windowOpened(WindowEvent evt) {
    message = "Window opened: "+displayWindowShortcut(evt);
    repaint();
  }

  public void windowClosing(WindowEvent evt) {
    message = "Window closing: "+displayWindowShortcut(evt);
    repaint();
  }

  public void windowClosed(WindowEvent evt) {
    message = "Window closed: "+displayWindowShortcut(evt);
    repaint();
  }


  /************************************************************************************************************/
  /** paint the panel
  * /
  public void paint(Graphics g) {
		update(g);
	}
*/  	
	public void update(Graphics g) {
    // first time initialiser
    if(frame.width==0 ){
      frame.setSize(getSize().width-2, getSize().height-2);
      inside.setBounds(5,5, getSize().width-10, getSize().height-10);
    }
    g.setColor(background);
    g.fillRect(1,1, frame.width, frame.height);
    g.setColor(foreground);
    g.drawRect(inside.x, inside.y, inside.width, inside.height);
    g.drawString(message,20,inside.height/2 +10);
  }




  /****************************************************************************************************************************************/
  /**     display event diagnostics : Override this for any specific AWT xxxx-Event
  * Following functions will be tested :
  * (java.util)EventObject.getSource()
  * (java awt)AWTEvent.getID()
  */

  public static String[] displayWindowEvent(WindowEvent evt) {
    String[] lines = new String[2];
    // line 1: source window
    Object source = evt.getSource();
    if(source==null) {
      lines[0] = "evt.getSource() == NULL";
    }
    else if(!(source instanceof Window)) {
      lines[0] = "non-window source = "+source;
    }
    else if( ((Component)source).getName() != null){
      lines[0] = "getSource() = "+((Component)source).getName();
    }
    else {
      lines[0] = "getSource() "+source;
    }
    // event type
    int id = evt.getID();
    if( id== WindowEvent.WINDOW_ACTIVATED) {
      lines[1] = "event <WINDOW_ACTIVATED>";
    }
    else if( id== WindowEvent.WINDOW_DEACTIVATED) {
      lines[1] = "event <WINDOW_DEACTIVATED>";
    }
    else if( id== WindowEvent.WINDOW_ICONIFIED) {
      lines[1] = "event <WINDOW_ICONIFIED>";
    }
    else if( id== WindowEvent.WINDOW_DEICONIFIED) {
      lines[1] = "event <WINDOW_DEICONIFIED>";
    }
    else if( id== WindowEvent.WINDOW_OPENED) {
      lines[1] = "event <WINDOW_OPENED>";
    }
    else if( id== WindowEvent.WINDOW_CLOSING) {
      lines[1] = "event <WINDOW_CLOSING>";
    }
    else if( id== WindowEvent.WINDOW_CLOSED) {
      lines[1] = "event <WINDOW_CLOSED>";
    }
    else {
      lines[1] = "Unknown WindowEvent ID  ("+ id+")";
    }

    return lines;
  }

  /****************************************************************************************************************************************/
  /**     display event diagnostics in a short line
  */
  public static String displayWindowShortcut(WindowEvent evt) {
    String line;
    Window source = evt.getWindow();
    if(source==null){
      line = "Source() == <NULL ";
    }
    else if(source.getName() != null){
      line = "From <"+source.getName();
    }
    else {
      line = "From <"+source;
    }

    int id = evt.getID();
    if( id== WindowEvent.WINDOW_ACTIVATED) {
      line += "> : event <WINDOW_ACTIVATED>";
    }
    else if( id== WindowEvent.WINDOW_DEACTIVATED) {
      line += "> : event <WINDOW_DEACTIVATED>";
    }
    else if( id== WindowEvent.WINDOW_ICONIFIED) {
      line += "> : event <WINDOW_ICONIFIED>";
    }
    else if( id== WindowEvent.WINDOW_DEICONIFIED) {
      line += "> : event <WINDOW_DEICONIFIED>";
    }
    else if( id== WindowEvent.WINDOW_OPENED) {
      line += "> : event <WINDOW_OPENED>";
    }
    else if( id== WindowEvent.WINDOW_CLOSING) {
      line += "> : event <WINDOW_CLOSING>";
    }
    else if( id== WindowEvent.WINDOW_CLOSED) {
      line += "> : event <WINDOW_CLOSED>";
    }
    else {
      line += "> : Unknown WindowEvent ID  ("+ id+")";
    }

    return line;
  }

  //end test
}
