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

package com.acunia.wonka.rudolph.peers;

import java.awt.peer.*;
import java.awt.event.*;
import java.awt.*;

public class DefaultScrollbar extends DefaultComponent implements ScrollbarPeer, MouseListener, MouseMotionListener {

  //our scrollpainter
  private ScrollPainter thePainter;

  //scrollbox pointer last position
  private Point lastMousePosition;
  
  //scrollbox mover thread
  final static ScrollRunner mouseEventRunner = new ScrollRunner();

  // painting buffer
  private Image backgroundImage;
  private Graphics backgroundGraphics;

  //all variables are already used by the scrollPainter, except for the minimum
  private int scrollbarMinimum;

  public DefaultScrollbar(Scrollbar scrollbar) {
    super(scrollbar);
    
    //mouse listener
    scrollbar.addMouseListener(this);
    scrollbar.addMouseMotionListener(this);
    //last mouse click point
    lastMousePosition = new Point(0,0);
    // painting buffer
    backgroundImage = null;
    backgroundGraphics = null;
  }

  private void buildScrollbar() {
    //build scrollbar
    Scrollbar scrollbar = (Scrollbar)component;

    if(scrollbar.getOrientation() == Adjustable.HORIZONTAL) {
      thePainter = new HScrollPainter();
    }
    else {
      thePainter = new VScrollPainter();
    }
    
    int value = scrollbar.getValue();
    int minimum = scrollbar.getMinimum();
    int maximum = scrollbar.getMaximum();
    int visible = scrollbar.getVisible();

    thePainter.setBarValues(value - minimum, visible, maximum - minimum);
    
    // immediately set foreground and background colors
    thePainter.setBarColors(RudolphPeer.getBarColors());
  }
  
  public void setLineIncrement(int increment) {
    if(thePainter == null) buildScrollbar();
    thePainter.setLineStep(increment);
  }
  
  public void setPageIncrement(int increment) {
    if(thePainter == null) buildScrollbar();
    thePainter.setBlockStep(increment);
  }
  
  public void setValues(int value, int visible, int minimum, int maximum) {
    if(thePainter == null) buildScrollbar();
    //new minimum
    if(maximum > minimum) {
      scrollbarMinimum = minimum;
      thePainter.setBarValues(value - minimum, visible, maximum - minimum);
    }
    else {
      scrollbarMinimum = maximum;
      thePainter.setBarValues(value - maximum, visible, minimum - maximum);
    }
    //in any case:
    paint(getGraphics());
  }

  /*
  ** minimum size
  */
  
  public Dimension getMinimumSize() {
    if(thePainter == null) buildScrollbar();
    return thePainter.getMinimumSize();  
  }

  /*
  ** preferred size
  */
  
  public Dimension getPreferredSize() {
    if(thePainter == null) buildScrollbar();
    return thePainter.getPreferredSize(component.getSize().width, component.getSize().height);  
  }

  /*
  **  Mouse listener forwarding to fire the Adjustment events
  */
  
  public  void mouseClicked(MouseEvent e) {
    //System.out.println("mouse clicked ("+e.getX()+", "+e.getY()+")");
  }

  public void mousePressed(MouseEvent e) {
    if(thePainter == null) buildScrollbar();
    //set the clicked field active
    int active = thePainter.setActive(e.getX(),e.getY());
    // if scrollbox, note the position for mouse moving
    if(active==AdjustmentEvent.UNIT_DECREMENT && thePainter.lineUp()) {
      processAdjustmentEvent(AdjustmentEvent.UNIT_DECREMENT);
      mouseEventRunner.setRunner(thePainter, component);
    }
    else if(active==AdjustmentEvent.UNIT_INCREMENT && thePainter.lineDn() ) {
      processAdjustmentEvent(AdjustmentEvent.UNIT_INCREMENT);
      mouseEventRunner.setRunner(thePainter, component);
    }
    else if(active==AdjustmentEvent.BLOCK_DECREMENT && thePainter.pageUp() ) {
      processAdjustmentEvent(AdjustmentEvent.BLOCK_DECREMENT);
      mouseEventRunner.setRunner(thePainter, component);
    }
    else if(active==AdjustmentEvent.BLOCK_INCREMENT && thePainter.pageDn() ) {
      processAdjustmentEvent(AdjustmentEvent.BLOCK_INCREMENT);
      mouseEventRunner.setRunner(thePainter, component);
    }
    else if(active==AdjustmentEvent.TRACK ) {
      lastMousePosition.setLocation(e.getX(),e.getY());
    }
    paint(getGraphics());
  }

  public void mouseReleased(MouseEvent e) {
    if(thePainter == null) buildScrollbar();
    //no more fields active
    if(thePainter.isSelected() ) {
      mouseEventRunner.stopRunner(thePainter);
    }
    paint(getGraphics());
  }

  public void mouseEntered(MouseEvent e) {
    //System.out.println("mouse Entered ("+e.getX()+", "+e.getY()+")");
  }

  public void mouseExited(MouseEvent e) {
    if(thePainter == null) buildScrollbar();
    //mouse out of mouse panel counts as mouse released
    if(thePainter.isSelected() ) {
      mouseEventRunner.stopRunner(thePainter);
      paint(getGraphics());
    }
  }

  public void mouseDragged(MouseEvent e) {
    if(thePainter == null) buildScrollbar();
    if(thePainter.getActive() == AdjustmentEvent.TRACK) {
      if(thePainter.moveScreenPos(e.getX()-lastMousePosition.x, e.getY()-lastMousePosition.y)) {
        // Ok, we were able to move the bar
        //tell the listeners  (this also repaints)
        processAdjustmentEvent(AdjustmentEvent.TRACK);
      }
      else if(thePainter.setActive(e.getX(),e.getY()) != AdjustmentEvent.TRACK) {
        thePainter.setNoSelected();
        paint(getGraphics());
      }
      lastMousePosition.setLocation(e.getX(),e.getY());
    }
    paint(getGraphics());
  }

  public void mouseMoved(MouseEvent e) {
    //System.out.println("mouse Moved ("+e.getX()+", "+e.getY()+")");
  }

  /*
  ** Paint the scrollbar
  */  
  
  public void paint(Graphics g) {
    if(g == null) return;

    if(thePainter == null) buildScrollbar();
    //update the size if needed
    if(backgroundImage == null || !component.getSize().equals(thePainter.getCurrentSize() ) ) {
      // If the screen size has changed: calculate the scrollbar values for that size and initialise a new background screen
      thePainter.setRange(component.getSize().width, component.getSize().height);     // new length
      thePainter.setThickness(component.getSize().width, component.getSize().height); // new thickness

      //new background image
      backgroundImage=createImage(component.getSize().width, component.getSize().height);
      if( backgroundImage!= null &&  backgroundGraphics!= null) {
        backgroundGraphics.dispose();
      }
      backgroundGraphics = backgroundImage.getGraphics();  
    }

    //update the colors if needed        
    Color back = (component.getBackground() != null)? component.getBackground():SystemColor.scrollbar;
    Color font = (component.getForeground() != null)? component.getForeground():SystemColor.menuText;
    if(!back.equals(thePainter.getBackground()) || !font.equals(thePainter.getForeground())) {
      thePainter.setBarColors(component.getBackground(), component.getForeground() );      
    }

    //tell the painter to paint the bar in background image.
    thePainter.paint(backgroundGraphics);

    // paint background image on the screen
    g.drawImage(backgroundImage, 0, 0,  component);       
  }

  public void processAdjustmentEvent(int type) {
    AdjustmentEvent e = new AdjustmentEvent((Adjustable)component, AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED, type, thePainter.getBarPos()+scrollbarMinimum);
    component.dispatchEvent(e);
    paint(getGraphics());
  }

  /*
  ** Extends the Component's setColor function to pass the colors further to the scrollPainter
  */

  public void setBackground(Color c) {
    if(thePainter == null) buildScrollbar();
    super.setBackground(c); // component
    thePainter.setBarColors(c, component.getForeground());
  }
    
  public void setForeground(Color c) {
    if(thePainter == null) buildScrollbar();
    super.setForeground(c); // component
    thePainter.setBarColors(component.getBackground(), c);
  }

  /*
  ** internal scrollpainter class for horizontal scrollbar 
  */

  class HScrollPainter extends ScrollPainter {
    
    /*
    ** Constructor : set height, lineup/lineDn and minimum box width for horizontal scrollbar
    */
    
    public HScrollPainter() {
      super(RudolphScrollbarPeer.HSCROLL_HEIGHT, RudolphScrollbarPeer.HSCROLL_LINEUPWIDTH,
       RudolphScrollbarPeer.HSCROLL_LINEDNWIDTH, RudolphScrollbarPeer.HSCROLL_MINIMUMBOXWIDTH);
    }
        
    /*
    ** scrollbar width and scrollbar position out of (x,y) dimension
    ** the abstract ScrollPainter classes filled in for horizontal scrollbar
    */

    public int getPos(int x, int y) {
      return x;
    }
    
    /*
    ** (horizontal bar) scrollbar height as thickness from dimension (width, height)
    */

    public int getThickness(int width, int height) {
      return height;
    }
    
    /*
    ** scrollbar length and thickness to (width, height) dimension for horizontal and vertical scrollbar
    */

    public Dimension getSize(int scrollbarlength, int scrollbarthickness) {
      return new Dimension(scrollbarlength, scrollbarthickness);
    }
    
    /*
    ** replace <thickness> of given dimension by scrollban minimum thickness
    */

    public Dimension getPreferredSize(int width, int height) {
      return new Dimension(width, minimumThickness);  
    }
        
    /*
    ** GetSize derived functions:specific overwrite for horizontal scrollbar 
    */
    
    public Dimension getMinimumSize() {
      return new Dimension(lineUpSpan+lineDnSpan+minimumScreenSpan , minimumThickness);  
    }
    
    public Dimension getPreferredSize() {
      return new Dimension(lineUpSpan+lineDnSpan+screenRange , minimumThickness);  
    }
    
    public Dimension getCurrentSize() {
      return new Dimension(lineUpSpan+lineDnSpan+screenRange , currentThickness);  
    }
        
    public void setThickness(int width, int height) {
      currentThickness =(height>minimumThickness)?height:minimumThickness;
    }
        
    //public int getRange(int x, int y)   { return x-2*RudolphScrollbarPeer.HSCROLL_LINEUPWIDTH;  }
    public boolean setRange(int width, int height) {
      return setRange(width);
    }
        
    /*
    ** basic functions : get field in which probing point exists
    ** additional feature: next to the x-position(scrollbar length)
    ** we also look if the y-position is within the range (offset , offset + height)
    */
    
    public int getField(int x, int y) {  
      return getField(x);
    }
    public synchronized int setActive(int x, int y) {
      currentActive = getField(x);
      return currentActive;
    }

    /*
    ** move the scrollbar (and recalculate barPos if needed)
    */

    //  public boolean moveScreenPos(int dx    {     return moveBar(dx;  }
    public boolean moveScreenPos(int dx, int dy) {
      return (dx!=0)?moveBar(dx):false;
    }

    /*
    ** paint command
    */

    public void paint(Graphics g) {
      if(crippledSpan<0) {
        RudolphScrollbarPeer.paintHScrollbar(0,0,currentThickness, paintedScreenPos,screenSpan,screenRange, currentActive,barColors,g);  
      }
      else {
        RudolphScrollbarPeer.paintCrippledHScrollbar(0, 0, currentThickness, crippledSpan, currentActive, barColors, g);  
      }
    }
    //(end HScrollPainter)
  }

  /*
  ** internal scrollpainter class for vertical scrollbar 
  */

  class VScrollPainter extends ScrollPainter {
    
    /*
    ** Constructor : set height, lineup/lineDn and minimum box width for horizontal scrollbar
    */
    
    public VScrollPainter() {
      super(RudolphScrollbarPeer.VSCROLL_WIDTH, RudolphScrollbarPeer.VSCROLL_LINEUPHEIGHT,
       RudolphScrollbarPeer.VSCROLL_LINEDNHEIGHT, RudolphScrollbarPeer.VSCROLL_MINIMUMBOXHEIGHT);
    }
        
    /*
    ** scrollbar width and scrollbar position out of (x,y) dimension
    ** the abstract ScrollPainter classes filled in for horizontal scrollbar
    */
    
    /*
    ** (horizontal bar) x- position in scrollbar from point (x,y) 
    */
    
    public int getPos(int x, int y) {
      return y;
    }
    
    /*
    ** (horizontal bar) scrollbar height as thickness from dimension (width, height)
    */
    
    public int getThickness(int width, int height) {
      return width;
    }
    
    /*
    ** scrollbar length and thickness to (width, height) dimension for horizontal and vertical scrollbar
    */
    
    public Dimension getSize(int scrollbarlength, int scrollbarthickness) {
      return new Dimension(scrollbarthickness, scrollbarlength);
    }
    
    /*
    ** replace <thickness> of given dimension by scrollban minimum thickness
    */
    
    public Dimension getPreferredSize(int width, int height) {
      return new Dimension(minimumThickness,height);  
    }
        
    /*
    ** GetSize derived functions:specific overwrite for horizontal scrollbar 
    */
    
    public Dimension getMinimumSize() {
      return new Dimension(minimumThickness, lineUpSpan+lineDnSpan+minimumScreenSpan);  
    }
    
    public Dimension getPreferredSize() {
      return new Dimension(minimumThickness, lineUpSpan+lineDnSpan+screenRange);  
    }
    
    public Dimension getCurrentSize() {
      return new Dimension(currentThickness, lineUpSpan+lineDnSpan+screenRange);  
    }
        
    public void setThickness(int width, int height) {
      currentThickness =(width>minimumThickness)?width:minimumThickness;
    }
        
    public boolean setRange(int width, int height) {
      return setRange(height);
    }
        
    /*
    ** basic functions : get field in which probing point exists
    */
    
    public int getField(int x, int y) {
      return getField(y);  
    }
    public synchronized int setActive(int x, int y) {
      currentActive = getField(y);  
      return currentActive;
    }

    /*
    ** move the scrollbar (and recalculate barPos if needed)
    */

    //  public boolean moveScreenPos(int dy)   {     return moveBar(dy);
    public boolean moveScreenPos(int dx, int dy) {
      return (dy!=0)?moveBar(dy):false;  
    }
    
    /*
    ** active part of screen
    */
    
    //  public void setActive(int x, int y) { CurrentActive = getField(x,y); }
        
    /*
    ** paint command
    */
    
    public void paint(Graphics g) {
      if(crippledSpan<0) {
        RudolphScrollbarPeer.paintVScrollbar(0,0,currentThickness,  paintedScreenPos,screenSpan,screenRange, currentActive,barColors,g);  
      }
      else {
        RudolphScrollbarPeer.paintCrippledVScrollbar(0, 0, currentThickness, crippledSpan, currentActive, barColors, g);  
      }
    }
    //(end VScrollPainter)
  }
  //end Scrollbar
}

