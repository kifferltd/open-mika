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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

/************************************************************************************************************/
/** Base class for all xxxxEvent display classes
*/
public class AWTEventDisplay extends Component implements CollectsEvents {
  /** size definitions */
  protected final static int MINIMUMWIDTH=70;
  protected final static int MINIMUMHEIGHT=25;

  /** variables */
  protected Color background;
  protected Color foreground;
  protected String message;

  protected Dimension frame;
  protected Rectangle inside;

  /************************************************************************************************************/
  /** constructor
  */
  public AWTEventDisplay(String firstmessage, Color back, Color text) {
    super();
    background = back;
    foreground = text;

    frame = new Dimension();
    inside = new Rectangle();
    message = firstmessage;
    // this.add-xxxx-Listener(this); // done by the test class, if he wants to
  }

  public AWTEventDisplay(Color back, Color text) {
    this("Your ItemEvents displayed HERE", back, text);
  }
  /************************************************************************************************************/
  /** Sizes
  */
  public Dimension getMinimumSize() {
    return new Dimension(MINIMUMWIDTH, MINIMUMHEIGHT);
  }

  public Dimension getPreferredSize() {
    return new Dimension(MINIMUMWIDTH, MINIMUMHEIGHT);
  }

  /************************************************************************************************************/
  /**Add xxxx-Listener functions here
  */

  /************************************************************************************************************/
  /** CollectsEvents help text
  */
  public String getHelpText() {
    return "Displays a panel with a short text about the Event received.";
  }

  /************************************************************************************************************/
  /** CollectsEvent interface display messagStrings : just display the first line
  */
  public void displayMessage(String[] messagestrings) {
    if(messagestrings.length >0) {
      message = messagestrings[0];
      repaint();
    }
  }

  /************************************************************************************************************/
  /** CollectsEvent interface display one messagestring : just display it
  */
  public void displayMessage(String messagestring) {
    message = messagestring;
    repaint();
  }

  /************************************************************************************************************/
  /** paint the panel
  */
  public void paint(Graphics g) {
		update(g);
	}
  	
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
    g.drawString(message,20,17);
  }




  /****************************************************************************************************************************************/
  /**     display event diagnostics : Override this for any specific AWT xxxx-Event
  * Following functions will be tested :
  * (java.util)EventObject.getSource()
  * (java awt)AWTEvent.getID()
  */

  public static String[] displayEvent(AWTEvent evt) {
    String[] lines = new String[1];
    // line 1: EventObject.getSource() /AWTEvent.getID()
    Object source = evt.getSource();
    if(source==null){
      lines[0] = "evt.getSource() == NULL";
    }
    else if(source instanceof Component && ((Component)source).getName() != null){
      lines[0] = "getSource() = "+((Component)source).getName();
    }
    else {
      lines[0] = "getSource()= "+source;
    }
    int id = evt.getID();
    lines[0] += " AWTEvent ID = ("+ id+")";
    return lines;
  }

  /****************************************************************************************************************************************/
  /**     display event diagnostics in a short line
  */
  public static String displayEventShortcut(AWTEvent evt) {
    String line;
    Object source = evt.getSource();
    if(source==null){
      line = "Source() == NULL ";
    }
    else if(source instanceof Component && ((Component)source).getName() != null){
      line = "Source() = "+((Component)source).getName();
    }
    else {
      line = "Source() = "+source;
    }

    line+= " AWTEvent ID = ("+ evt.getID()+")";

    return line;
  }

  //end test
}
