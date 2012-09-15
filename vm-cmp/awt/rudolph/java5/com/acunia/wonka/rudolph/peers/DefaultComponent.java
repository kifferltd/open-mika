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
import java.awt.image.*;
import java.awt.*;
import com.acunia.wonka.rudolph.Repainter;

public class DefaultComponent implements ComponentPeer {

  // Refresh types:
  static final int REFRESH_LOCAL = 1;
  static final int REFRESH_GLOBAL = 2;
  static boolean renderer = false;

  Component component;
  
  protected final static Object focusSync = new Object();
  protected static DefaultComponent currentFocusedComponent;
  
  /** Holds value of property focused. */
  private boolean focused;
  
  public DefaultComponent(Component component) {
    this.component = component;
    component.peer = this;
    createPeer();    
  }
  
  static public void initRudolph() {
    if (!System.getProperty("com.acunia.wonka.awt", "true").trim().equalsIgnoreCase("false")) {
      com.acunia.wonka.rudolph.Dispatcher.getMainDispatcher();
    }
  }

  private native void createPeer();

  protected native void finalize();

  private native int getAbsX();
  
  private native int getAbsY();

  public int checkImage(Image image, int width, int height, ImageObserver obs) {
    return Toolkit.getDefaultToolkit().checkImage(image, width, height, obs);
  }
  
  public native Image createImage(int w, int h);

  public Image createImage(java.awt.image.ImageProducer producer) {
    return (java.awt.Image)(new com.acunia.wonka.rudolph.Image(producer));
  }
  
  public void dispose() {
  }
  
  public ColorModel getColorModel() {
    return null;
  }
  
  public FontMetrics getFontMetrics(Font font) {
    return new FontMetrics(font);
  }
  
  public native Graphics getGraphics();
  
  public Point getLocationOnScreen() {
    return new Point(getAbsX(), getAbsY());
  }
  
  public Dimension getMinimumSize() {
    return component.getSize();
  }
  
  public Dimension getPreferredSize() {
    return component.getSize();
  }
  
  public Toolkit getToolkit() {
    return null;
  }
  
  public boolean handleEvent(AWTEvent event) {
    return false;
  }
  
  public boolean isFocusTraversable() {
    return false;
  }

  public void paint(Graphics g) {
    if(g == null) return;

	if(isFocused())
	{
		wonka.vm.Etc.woempa(9, "draw a focus rect\n");
		
		Dimension dim = component.getSize();
		int w = dim.width;
		int h = dim.height;
		
		g.setColor(component.getForeground());		
		g.drawRect(0, 0, w-1, h-1);
	}		
	
    component.paint(g);
    refresh(DefaultComponent.REFRESH_LOCAL);
  }
  
  public boolean prepareImage(Image image, int w, int h, ImageObserver observer) {
    return Toolkit.getDefaultToolkit().prepareImage(image, w, h, observer);
  }

  public void print(Graphics g) {
  }

  public void doRepaint() {
    synchronized(component.getTreeLock()) {
      Graphics g = getGraphics();
      if (g != null) {
        component.update(g);
        Toolkit.getDefaultToolkit().sync();
      }
    }
  }
  
  public void repaint(long ms, int x, int y, int w, int h) {
    Repainter.getInstance().repaint(this);
  }
  
  public void requestFocus() {
	  if(isFocusTraversable())
	  {
		  wonka.vm.Etc.woempa(9, "wait for focusSync");
		  synchronized(focusSync) {			  
			  if(currentFocusedComponent!=null) {				  
				currentFocusedComponent.setFocused(false);
			  }

			  setFocused(true);
			  currentFocusedComponent = this;		  
		  }
	  }
	  else
	  {
		  wonka.vm.Etc.woempa(9, "focus isn't traversable");
	  }
  }
  
  public void setBounds(int x, int y, int w, int h) {
    refresh(DefaultComponent.REFRESH_GLOBAL);
  }
  
  public void setBackground(Color c) {
    refresh(DefaultComponent.REFRESH_LOCAL);
  }
  
  public void setCursor(Cursor cursor) {
    wonka.vm.Etc.woempa(9, "UNIMPLEMENTED: java.awt.Component.setCursor");
  }
  
  public void setEnabled(boolean enable) {
  }
  
  public void setFont(Font font) {
    refresh(DefaultComponent.REFRESH_LOCAL);
  }

  public void setForeground(Color c) {
    refresh(DefaultComponent.REFRESH_LOCAL);
  }
  
  public void setVisible(boolean visible) {
    if(visible){
      refresh(DefaultComponent.REFRESH_LOCAL);
    }
    else {
      Container parent = component.getParent();
      if(parent != null){
        ((DefaultComponent)parent.getPeer()).refresh(REFRESH_LOCAL);
      }
    }
  }

  /*
  ** Deprecated methods
  */
  
  public void disable() {
  }

  public void enable() {
  }

  public void hide() {
  }

  public Dimension minimumSize() {
    return null;
  }

  public Dimension preferredSize() {
    return null;
  }

  public void reshape(int x, int y, int w, int h) {
  }

  public void show() {
  }

  protected void refresh(int type) {
    synchronized(component.getTreeLock()) {
      /* Refresh component if required: */
      Container parent = component.getParent();

      if ((parent != null) && ((DefaultContainer)parent.getPeer()).validate) {
        // prune - if the parent container is invalid, we are likely to
        //         be in the middle of updating/validating an entire
        //         container in which case we postpone to update the
        //         individual components until the parent container is
        //         entirely validated/layouted.
        tag(type, false);
      }
      else if ((parent == null) && !(component instanceof Window)) {
        // prune - ignore refresh as the component is still floating
        //         in the void (not attached to a parent container).
        tag(type, false);
      }
      else if (component.isVisible()) {
        /* Re-render the off-screen buffer: */
        if (renderer == false) {
          renderer = true;
          tag(type, true);
          renderer = false;
        }
        else {
          tag(type, false);
        }
      }
    }
  }

  private native void tag(int status, boolean render);

  
  public String toString()
  {
	return "peer of "+this.component.toString();
  }

  /** Getter for property focused.
   * @return Value of property focused.
   *
   */
  public boolean isFocused()
  {
	  return this.focused;
  }
  
  /** Setter for property focused.
   * @param focused New value of property focused.
   *
   */
  public void setFocused(boolean focused)
  {
	  if(isFocusTraversable())
	  {	  
		  wonka.vm.Etc.woempa(9, "("+this.toString()+").setFocused -> "+focused);
		  this.focused = focused;
		  paint(getGraphics());
	  }
	  else
	  {
		  throw new IllegalStateException("can't change focus of a none focustraversable component");
	  }
  }  
}

