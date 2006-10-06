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

package java.awt;

import java.awt.event.*;
import java.awt.peer.*; 

/*
** Scrollbar wrapper: handles existence, movement commands,events of the scrollbar.
** the main painting job is passed on tho the RectangleComponents and rectanglePainters
*/

public class Scrollbar extends Component implements Adjustable {
  
  /*
  ** variables
  */
  
  final static int DEFAULTVALUE           = 0;
  final static int DEFAULTVISIBLEAMOUNT   = 10;
  final static int DEFAULTMINIMUM         = 0;
  final static int DEFAULTMAXIMUM         = 100;
  final static int DEFAULTUNITINCREMENT   = 1;
  final static int DEFAULTBLOCKINCREMENT  = 10;
  final static int DEFAULTORIENTATION     = Adjustable.VERTICAL;

  private int scrollbarMinimum;
  private int scrollbarMaximum;
  private int scrollbarVisible;
  private int scrollbarOrientation;
  private int scrollbarValue;
  private int scrollbarBlockInc;
  private int scrollbarUnitInc;

  // adjustment listener
  public transient AdjustmentListener multiListener;
    
  /*
  ** Constructors
  */
  
  public Scrollbar() {
    this(DEFAULTORIENTATION, DEFAULTVALUE, DEFAULTVISIBLEAMOUNT, DEFAULTMINIMUM, DEFAULTMAXIMUM);   //default values
  }

  /*
  ** Default values but speccified orientation: horizontal or vertical 
  */
  
  public Scrollbar(int orientation) {
    this(orientation, DEFAULTVALUE, DEFAULTVISIBLEAMOUNT, DEFAULTMINIMUM, DEFAULTMAXIMUM);   //default values
  }

  /*
  ** full initialisation (generous autochecks on min, max and value)
  */
  
  public Scrollbar(int orientation, int value, int visible, int minimum, int maximum) {
    scrollbarOrientation = orientation;
    scrollbarValue = value;
    scrollbarVisible = visible;
    
    if(minimum < maximum) {
      scrollbarMinimum = minimum;
      scrollbarMaximum = maximum;
    }
    else {
      scrollbarMinimum = maximum;
      scrollbarMaximum = minimum;
    }
   
    multiListener = null;
  }

  public void addNotify() {
    if(peer == null) {
      peer = getToolkit().createScrollbar(this);
    }

    if (notified == false) {
      super.addNotify();
    }
  }

  /*
  ** add listener
  */
  
  public void addAdjustmentListener(AdjustmentListener newlistener) {
    multiListener = AWTEventMulticaster.add(multiListener, newlistener);
  }

  /*
  ** Remove listener
  */
  
  public void removeAdjustmentListener(AdjustmentListener newlistener) {
    multiListener = AWTEventMulticaster.remove(multiListener, newlistener);
  }


  /*
  ** overrides Component.processEvent so sub- and superclasses have their AdjustmentEvents dispatched through our (multi-)listener
  */

  protected void processEvent(AWTEvent e) {
    if(e instanceof AdjustmentEvent) {
      scrollbarValue = ((AdjustmentEvent)e).getValue();
      if(multiListener != null) {
        multiListener.adjustmentValueChanged((AdjustmentEvent)e);
      }
    }
    else {
      super.processEvent(e);
    }
  }

  /*
  ** Process adjustmentevent as described in Adjustable interface
  */

  protected void processAdjustmentEvent(AdjustmentEvent e) {
    if(multiListener != null) {
      multiListener.adjustmentValueChanged(e);
    }
  }

  /*
  ** Get /set data items and mirror this to the barScreen
  */
  
  /*
  ** Orientation:
  */

  public int getOrientation() {
    return scrollbarOrientation;
  }

  public void setOrientation(int orientation) {
    scrollbarOrientation = orientation;

    /*
    ** TODO: See what to do with this code.. At the moment it's 
    ** not possible to change the orientation once the peer has
    ** a fix on the current orientation...
    */
    
    /*
    //security : no need to rebuild a bar if already the right orientation
    if(orientation == getOrientation()) {
      return;
    }
    //get the data from the old bar  
    int pos = thePainter.getBarPos();
    int span = thePainter.getBarSpan();
    int range = thePainter.getBarRange();
    //(re)build the scrollbar barScreen for new orientation
    if(orientation == Adjustable.HORIZONTAL) {
      thePainter = new HScrollPainter();
    }
    else {
      thePainter = new VScrollPainter();
    }
    //(re)calculate position and size for scrollbox
    thePainter.setBarValues(pos,span, range);
    //repaint the scrollbox
    repaint();
    */
  }

  /*
  ** Maximum value
  */
  
  public int getMaximum() {
    return scrollbarMaximum;
  }

  public void setMaximum(int maximum) {
    scrollbarMaximum = maximum;
    setValues(scrollbarValue, scrollbarVisible, scrollbarMinimum, scrollbarMaximum);
  }

  /*
  ** Minimum value
  */
  
  public int getMinimum() {
    return scrollbarMinimum;
  }

  public void setMinimum(int minimum) {
    scrollbarMinimum = minimum;
    setValues(scrollbarValue, scrollbarVisible, scrollbarMinimum, scrollbarMaximum);
  }

  /*
  ** current value
  */
  
  public int getValue() {
    return scrollbarValue;
  }

  public void setValue(int value) {
    scrollbarValue = value;
    setValues(scrollbarValue, scrollbarVisible, scrollbarMinimum, scrollbarMaximum);
  }

  /*
  ** visible amount of scrollbox
  */
  
  public int getVisibleAmount() {
    return scrollbarVisible;
  }
  
  public int getVisible() {
    return getVisibleAmount();
  }
  
  public void setVisibleAmount(int visible) {
    scrollbarVisible = visible;
    setValues(scrollbarValue, scrollbarVisible, scrollbarMinimum, scrollbarMaximum);
  }

  /*
  ** All settings in one
  */
  
  public void setValues(int value, int visible, int minimum, int maximum) {
    ((ScrollbarPeer)peer).setValues(value, visible, minimum, maximum);
  }
  
  /*
  ** unit increment (this does not affect the current scrollbox, so no update is necessary)
  */
  
  public int getUnitIncrement() {
    return scrollbarUnitInc;
  }

  public void setUnitIncrement(int increment) {
    scrollbarUnitInc = increment;
    ((ScrollbarPeer)peer).setLineIncrement(increment);
  }

  /*
  ** block increment (this does not affect the current scrollbox, so no update is necessary)
  */
  
  public int getBlockIncrement() {
    return scrollbarBlockInc;
  }
  
  public void setBlockIncrement(int increment) {
    scrollbarBlockInc = increment;
    ((ScrollbarPeer)peer).setPageIncrement(increment);
  }

  /*
  ** Debug information
  */
  
  protected String paramString() {
    String param = "Scrollbar< orientation: "+getOrientation()+" min: "+scrollbarMinimum;
    param += "component <"+super.paramString()+">";
    return param;
  }

  public String toString() {
    String param = scrollbarOrientation == Adjustable.HORIZONTAL ? "Horizontal Scrollbar" : "Vertical Scrollbar";
    param+="Covering "+ scrollbarMinimum+" to "+scrollbarMaximum;
    return param;
  }

}

