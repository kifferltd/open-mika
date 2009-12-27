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

import java.awt.event.*;
import java.awt.*;

/*
** Scrollbar wrapper: handles existence, movement commands,events of the scrollbar.
** the main painting job is passed on tho the RectangleComponents and rectanglePainters
*/

public abstract class ScrollPainter {
  
  /*
  ** static variables, to be set in constructor by the derived
  */
  
  public final int minimumThickness;
  public final int lineUpSpan;
  public final int lineDnSpan;
  public final int minimumScreenSpan;

  /*
  ** basic variables for scrollbar calculations:
  */
  
  public int barPos = 0;
  public int barSpan = 10;
  public int barRange = 100;
    
  public int screenPos = 0;
  public int screenSpan = 10;
  public int screenRange = 100;
  public int paintedScreenPos = 0;
    
  public int lineStep = 1;
  public int blockStep = 10;
    
  public int currentActive;
    
  /*
  ** basic variables for scrollbar visibility:
  */
  
  // current bar thickness, current active field
  public int currentThickness;  
  public int crippledSpan = -1;
  
  /*
  ** (Vertical)Offset if needed:
  */
  
  public int barOffset;
    
  //current foreground and background color
  public Color[] barColors  = RudolphScrollbarPeer.getBarColors();

  /*
  ** Constructor
  */
  
  public ScrollPainter(int thickness, int lineup, int linedn, int box) {
    minimumThickness = thickness;
    lineUpSpan = lineup;
    lineDnSpan = linedn;
    minimumScreenSpan = box;
  }
  
  /*
  ** scrollbar width and scrollbar position out of (x,y) dimension for horizontal and vertical scrollbars
  */
    
  /*
  ** position in scrollbar from point (x,y) for horizontal and vertical scrollbar
  */
  
  abstract public int getPos(int x, int y);
  
  /*
  ** scrollbar thickness from dimension (width, height) for horizontal and vertical scrollbar
  */
  
  abstract public int getThickness(int width, int height);
  
  /*
  ** scrollbar length and thickness to (width, height) dimension for horizontal and vertical scrollbar
  */
  
  abstract public Dimension getSize(int scrollbarlength, int scrollbarthickness);

  /*
  ** GetSize derived functions: minimum size, preferred size, current size in (width,height) dimension (for horizontal and vertical scrollbar)
  */
  
  public Dimension getMinimumSize() {
    return getSize(lineUpSpan+lineDnSpan+minimumScreenSpan , minimumThickness);  
  }

  public Dimension getPreferredSize(){
    return getSize(lineUpSpan+lineDnSpan+screenRange , minimumThickness);  
  }

  public Dimension getCurrentSize() {
    return getSize(lineUpSpan+lineDnSpan+screenRange , currentThickness);  
  }

  /*
  ** replace <thickness> of given dimension by scrollban minimum thickness
  */
  
  public Dimension getPreferredSize(int width, int height) {
    return getSize(getPos(width, height), minimumThickness);  
  }
    
    
  /*
  ** scrollbar length and screen range from dimension (width, height) for horizontal and vertical scrollbar
  ** if the new scrollbar <length> is too small for the lineup and linedown boxes (and the scrollbar) to be shown,
  ** crippledSpan is initialised so that a <crippled> scrollbar will be displayed
  * /  
  public boolean setRange(int totalrange)
  {
    int totalspan;
    if(totalrange>(lineUpSpan+lineDnSpan)) {
      // the scrollbar is 'crippled' : soo small to house at least a line-up and a line-down box
      totalrange -= lineUpSpan+lineDnSpan;
      totalspan = -1;
    }
    else {
      // Ok, normal scrollbar, continue with the calculations deper down
      totalrange = -1;
      totalspan = totalrange;
    }

    if(totalrange != screenRange || totalspan != crippledSpan) {
      screenRange = totalrange;
      crippledSpan = totalspan;
      setScreen();
      return true;
    }
    return false;        
  }
  */

  public boolean setRange(int totalwidth) {
    if(totalwidth<=(lineUpSpan+lineDnSpan)) {
      if(totalwidth==crippledSpan){
        // the scrollbar width hasn't changes
        return false;
      }
      //else
      crippledSpan = totalwidth;
      return true;
    }
    else {
      crippledSpan=-1;
      totalwidth = totalwidth-lineUpSpan-lineDnSpan;
      if(totalwidth==screenRange) {
        // the scrollbar width hasn't changes
        return false;
      }
      //else
      screenRange=totalwidth;
      setScreen();
      return true;
    }
  }
    
  public boolean setRange(int width, int height) {
    return setRange(getPos(width, height));
  }

  /*
  ** Get /set the scrollbar thickness
  */
  
  /*
  ** get the minimum scrollbar thickness 
  */
  
  public int   getMinimumThickness() {
    return minimumThickness;  
  }
  
  /*
  ** get the current scrollbar thickness 
  */
  
  public int   getCurrentThickness() {
    return currentThickness;  
  }
  
  /*
  ** set the current scrollbar thickness to desired value (if value smaller then minimum thickness, set to minimum)
  */
  
  public void setThickness(int newthickness) {
    currentThickness =(newthickness>minimumThickness)?newthickness:minimumThickness;
  }
  
  /*
  ** id as setThickness(int), but thicknes out of a given dimension for horizontal or vertical scrollbar 
  */
  
  public void setThickness(int width, int height)  {
    setThickness(getThickness(width, height ));
  }
  
  /*
  ** set the current scrollbar thickness to minimum thickness 
  */
  
  public void setMinimumThickness() {
    currentThickness = minimumThickness;
  }
    
  /*
  ** Get /set the scrollbar offset(if needed)
  */
  
  public void setOffset(int offset) {
    barOffset = offset;
  }

  public int   getOffset() {
    return barOffset;
  }

  /*
  ** get/Set the colors
  */
  
  public void setBarColors(Color back, Color font) {
    barColors = RudolphPeer.getBarColors(back, font);  
  }
    
  public void setBarColors(Color[] newcolors) {
    if(newcolors.length>=RudolphPeer.COLORWIDTH)
    barColors = newcolors;
  }
    
  public Color getBackground(){
    return barColors[0];
  }
  public Color getForeground(){
    return barColors[4];
  }

  /*
  ** Get scrollbar calculation values
  */
  
  public int getBarPos() {
    return barPos;
  }
  public int getBarSpan() {
    return barSpan;
  }
  public int getBarRange() {
    return barRange;
  }
  public int getScreenPos() {
    return screenPos;
  }
  public int getScreenSpan() {
    return screenSpan;
  }
  public int getScreenRange() {
    return screenRange;
  }
  public int getLineStep() {
    return lineStep;  
  }
  public int getBlockStep() {
    return blockStep;  
  }
    
  /*
  ** set scrollbar calculation values: line and page increment
  */
  
  public void setLineStep(int step) {
    lineStep = step;  
  }
  public void setBlockStep(int step) {
    blockStep = step;  
  }

  /*
  ** set virtual bar scrollbox position. Return true if visual position changed => redraw would get visible results
  */
  
  public boolean setBarPos(int newpos) {
    //adjust position to boundaries
    if(newpos <0) {
      newpos =0;
    }
    else if((newpos+barSpan)>barRange) {
      newpos = barRange - barSpan;
    }

    if(newpos != barPos) {
      barPos = newpos;
      setScreen();
      return true;
    }
    //else
    return false;
  }

  /*
  ** set virtual bar scrollbox span. Return true if visual position changed => redraw would get visible results
  */

  public boolean setBarSpan(int newspan) {
    //adjust span to boundaries
    if(newspan <1) {
      // minimum 1 item visible
      newspan =1;
    }
    else if(newspan>barRange) {
      newspan = barRange;
    }    
    if(newspan != barSpan) {
      barSpan = newspan;
      //adjust position if necessary
      if((barPos+barSpan)>barRange) {
        barPos = barRange - barSpan;
      }      
      //calculate screen values
      setScreen();
      return true;
    }        
    return false;  
  }

  /*
  ** set virtual bar range.  Return true if visual position changed => redraw would get visible results
  ** (this is always when the range itself is changed)
  */

  public boolean setBarRange(int newrange)
  {
    if(newrange<1) {
    newrange=1;
    }
        
    if (newrange!= barRange) {
      barRange = newrange;
      if(barSpan > barRange) {
        barSpan = barRange;
        barPos = 0;
      }
      else if((barPos+barSpan) > barRange) {
        barPos = barRange - barSpan;
      }              
      //if needed and desired, mirror the new values to the screen settings
      setScreen();
      return true;
    }
    return false;  
  }

  /*
  ** set new virtual bar values: position, span, range.
  ** Return true if visual position changed => redraw would get visible results
  ** (this is always when the range itself is changed)
  */
  
  public boolean setBarValues(int newpos, int newspan, int newrange) {
    if(newrange<1) {
      newrange=1;    
    }
    if(newspan<1) {
      newspan=1;
    }
    else if(newspan>newrange) {
      newspan = newrange;
    }
    if(newpos<0) {
      newpos=0;
    }
    else if((newpos+newspan)>newrange) {
      newpos = newrange-newspan;
    }
          
    if(newrange!=barRange || newspan!=barSpan || newpos != barPos) //data changed
    {
      barRange = newrange;
      barSpan = newspan;
      barPos = newpos;
            
      setScreen();
      return true;      
    }
    return false;
  }

  /*
  ** On the screen side, we can only resize the screen and move the scrollbar up and down
  */
  
  /*
  ** resize the screen and recalculate screenPos and screenSpan to new screenRange if needed
  */

  public boolean setScreenRange(int range) {
    if (range <1) {
      range =1;
    }
    if(range != screenRange) {
      screenRange = range;
      setScreen();
      return true;
    }
    return false;
  }  

  /*
  ** move the scrollbar (and recalculate barPos if needed)
  */

  public boolean setScreenPos(int newpos)  {
    //adjust new position to boundaries
    if(newpos<0) {
      newpos = 0;
    }
    else if((newpos+screenSpan)>screenRange) {
      newpos = screenRange - screenSpan;
    }
           
    if(screenPos != newpos) {
      screenPos = newpos;
      return setBar();
    }
    return false;
  }

  /*
  **  move up/down commands from parent
  */

  public boolean lineUp() {
    //return setBarPos(barPos - lineStep);
    if(barPos>0) {
      // still space to subtract a whole step => do so
      barPos= (barPos>lineStep)? barPos-lineStep: 0;
      setScreen();
      return true;
    }
    // else
    return false;
  }

  public boolean lineDn() {
    int lastpos=barRange-barSpan;
    if(barPos<=lastpos) {
      barPos=((barPos+lineStep)<lastpos)? barPos+lineStep: lastpos;
      setScreen();
      return true;
    }
    // else
    return false;
  }

  public boolean pageUp() {
    if(barPos>0) {
      // still space to subtract a whole step => do so
      barPos= (barPos>blockStep)? barPos-blockStep: 0;
      setScreen();
      return true;
    }
    // else
    return false;
  }

  public boolean pageDn() {
    int lastpos=barRange-barSpan;
    if(barPos<=lastpos) {
      barPos=((barPos+blockStep)<lastpos)? barPos+blockStep: lastpos;
      setScreen();
      return true;
    }
    // else
    return false;
  }

  /*
  ** basic functions : calculate (visible)screen box position, screen box length out of given bar pos,span, range and screen width
  ** call if: barPos, barSpan or barRange changed / screenRange changed
  */

  public void setScreen() {
    if(barSpan >= barRange) {
      // bar covers the whole screen
      barPos = 0;
      barSpan = barRange;
      screenPos=0;
      paintedScreenPos = 0;
      screenSpan = screenRange;
    }
    else if(( minimumScreenSpan*barRange)>(screenRange*barSpan) ) {
      //extended case: the desired scrollbox width is smaller then the minimum
      screenSpan = minimumScreenSpan;//(definition)
      if(barPos<=0) {
        screenPos=0;
      }
      else if(barPos>=(barRange-barSpan)) {
        screenPos=screenRange-screenSpan;
      }
      else {
        screenPos = (barPos*(screenRange-minimumScreenSpan))/(barRange-barSpan);
      }
      paintedScreenPos = screenPos;
    }
    else {
      //simple case
//System.out.println("For scrollbar height "+screenRange+", barpos"+barPos+", span="+barSpan+" range="+barRange);
      screenSpan = (barSpan*screenRange)/barRange;
      if(barPos<=0) {
        screenPos=0;
      }
      else if(barPos>=(barRange-barSpan)) {
        screenPos=screenRange-screenSpan;
      }
      else {
        screenPos = (barPos*screenRange)/barRange;
      }
      paintedScreenPos = screenPos;
//System.out.println("..... found screen pos="+screenPos+", span="+screenSpan);
    }
  }

  /*
  ** basic functions : calculate bar position and visible screen position out of given screen position
  ** @returns: true if visible screen position changed (redraw will show an effect)
  ** call if: screenPos changed
  */

  public boolean setBar() {
    if(screenPos<0) {
      screenPos=0;
      if(barPos>0) {
        barPos = 0;
        paintedScreenPos=0;
        return true;
      }
    }
    else if((screenPos+ screenSpan) >= screenRange) {
      barPos = barRange-barSpan;
      screenPos = screenRange - screenSpan;
      if(paintedScreenPos != screenPos) {
        paintedScreenPos=screenPos;
        return true;
      }
    }
    else if(( minimumScreenSpan*barRange)>(screenRange*barSpan) ) {
      //extended case: the desired scrollbox width is smaller then the minimum
      int newpos = screenPos * (barRange-barSpan) / (screenRange - minimumScreenSpan);
      if(newpos != barPos) {
        //new values for bar and visible screen
        barPos = newpos;
        paintedScreenPos = (newpos * (screenRange-minimumScreenSpan))/(barRange-barSpan);
        return true;
      }
    }
    else {
      //simple case
      int newpos = screenPos * barRange/screenRange;
      if(newpos != barPos) {
      //new values for bar and visible screen
        barPos = newpos;
        paintedScreenPos = (newpos * screenRange)/barRange;
        return true;
      }
    }
    return false;
  }

  /*
  ** basic functions : move screen box position and calculate bar position /visible screen position for new value
  ** return true if visible screen position changed (redraw will show an effect)
  ** (this function is equivalent to <screenpos+=dl; + setBar();> trimmed into one)
  */

  public boolean moveBar(int dl) {
    if((screenPos+dl)<0) {
      screenPos=0;
      if(barPos>0) {
        barPos = 0;
        paintedScreenPos=0;
        return true;
      }
    }
    else if((screenPos+dl+screenSpan) >= screenRange) {
      barPos = barRange-barSpan;
      screenPos = screenRange - screenSpan;
      if(paintedScreenPos != screenPos) {
        paintedScreenPos=screenPos;
        return true;
      }
    }
    else if(( minimumScreenSpan*barRange)>(screenRange*barSpan) )//extended case: the desired scrollbox width is smaller then the minimum
    {
      screenPos += dl;
      int newpos = screenPos * (barRange-barSpan) / (screenRange - minimumScreenSpan);
      if(newpos != barPos) {
        //new values for bar and visible screen
        barPos = newpos;
        paintedScreenPos = (newpos * (screenRange-minimumScreenSpan))/(barRange-barSpan);
        return true;
      }
    }
    else {
      //simple case
      screenPos += dl;
      int newpos = screenPos * barRange/screenRange;
      if(newpos != barPos) {
        //new values for bar and visible screen
        barPos = newpos;
        paintedScreenPos = (newpos * screenRange)/barRange;
        return true;
      }
    }
    return false;
  }

  /*
  ** Idem for non-select screen position (replace by subclas specific as soon as we know wether getPos(dx, dy)=x or getPos(dx, dy)=y
  */

  public boolean moveScreenPos(int dx, int dy) {
    return moveBar(getPos(dx, dy) );  
  }
  
  /*
  ** Get set active part of screen
  ** as mouse events and scroll thread can try to access the currentActive value at the same time,
  ** all Active functions have to be synchronised
  */

  public synchronized int getActive(){
    return currentActive;
  }

  public synchronized void setNoSelected() {
    currentActive = RudolphScrollbarPeer.FIELD_NONESELECTED;
  }

  public synchronized boolean isNoSelected() {
    return(currentActive<0);
  }

  public synchronized boolean isSelected() {
    return(currentActive>=0);
  }

  /*
  ** Scrollbar click functions for x,y locations:
  ** THESE FUNCTIONS USE getField() => REPLACE BY OVERWRITTEN getPos(x,y)=x OR getPos(x,y)=y FOR ALL HORIZONTAL AND VERTICAL SCROLLBARS
  */
  
  public int getField(int x, int y) {
    return getField(getPos(x,y));  
  }

  public synchronized int setActive(int x, int y) {
    currentActive = getField(getPos(x,y));  
    return currentActive;
  }
    
  /*
  ** Find the 'field' of the scrollbar in which the given x,y point is located. This is a basic function for clicking the scrollbar
  ** in any base class using this painter
  */

  public int getField(int pos) {
    int field = RudolphScrollbarPeer.FIELD_NONESELECTED;
    if(crippledSpan>0 && pos>=0 && pos <= crippledSpan) {
      // crippled scrollbar: there are only two possibilities: either UNIT_DECREMENT when in upper half or UNIT_INCREMENT in lower
      field = (pos<crippledSpan/2)?AdjustmentEvent.UNIT_DECREMENT:AdjustmentEvent.UNIT_INCREMENT;
    }
    else if(pos>=0 && pos<=(lineUpSpan+screenRange+lineDnSpan) ) {
      // the point is either in one of the line-up/line down blocks,in the scrollbox or in the area above or under the box
      if(pos<=lineUpSpan) {
        // point in upper (line-up) block
        field = AdjustmentEvent.UNIT_DECREMENT;
      }
      else if(pos<(lineUpSpan + screenPos)) {
        // point between line-up block and scrollbox
        field = AdjustmentEvent.BLOCK_DECREMENT;
      }
      else if(pos<=(lineUpSpan + screenPos + screenSpan)) {
        //point in scrollbox
        field = AdjustmentEvent.TRACK;
      }
      else if(pos<(lineUpSpan + screenRange)) {
        // point between scrollbox and lower line box
        field = AdjustmentEvent.BLOCK_INCREMENT;
      }
      else  {
        //point in lower (line-lown)box
        // if(pos>=(lineUpSpan + screenRange))
        field = AdjustmentEvent.UNIT_INCREMENT;
      }
    }
    return field;
  }
  
  /*
  ** paint command
  */
  
  abstract public void paint(Graphics g);
    
  /*
  ** Debugging
  */

  public String toString() {
    String param = "ScrollPainter: virtual bar <pos = "+barPos+", span = "+barSpan+", range = "+barRange+">";
    param += "screen <pos = "+screenPos+", span = "+screenSpan+", range = "+screenRange+"> ";
    param += "increment <unit = "+lineStep+",block = "+blockStep+">";
    param += "current field <"+currentActive+">";
    return param;
  }
  
  public String paramString() {
    String param = "ScrollPainter:bar<pos"+barPos+",span"+barSpan+",range"+barRange+">";
    param += "screen<pos"+screenPos+",span"+screenSpan+",range"+screenRange+">";
    param += "increment<unit"+lineStep+",block"+blockStep+">";
    param += "currentActive:"+currentActive;
    return param;
  }
}
