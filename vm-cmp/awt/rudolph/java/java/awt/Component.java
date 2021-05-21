/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2009, 2012 by Chris Gray, /k/ Embedded Java   *
* Solutions.  All rights reserved.                                        *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

/*
 *  Parts licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package java.awt;

import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.util.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.peer.*;
import java.awt.Color;

import com.acunia.wonka.rudolph.FocusControl;
import com.acunia.wonka.rudolph.FocusControlFactory;
import com.acunia.wonka.rudolph.FocusCycle;

/**
 ** The parent class of all AWT components.
 */
public abstract class Component implements ImageObserver, MenuContainer, Serializable {

   private static final long serialVersionUID = -7644114512714619750L;

  // Counter
  static private int counter; 

  // Default font on which we can fall-back:
  public static final Font DEFAULT_FONT = new Font("helvP08", Font.PLAIN, 8);

  // (text) alignment definitions
  public static final float BOTTOM_ALIGNMENT = 1.0f;
  public static final float CENTER_ALIGNMENT = 0.5f;
  public static final float LEFT_ALIGNMENT   = 0.0f;
  public static final float RIGHT_ALIGNMENT  = 1.0f;
  public static final float TOP_ALIGNMENT    = 0.0f;

  // Component dimensions:
  int x;
  int y;
  int height;
  int width;

  // Sizes:
  Dimension prefSize;
  Dimension minSize;
  Dimension maximumSize;
  boolean isPacked;

  // Name:
  String name;
  boolean nameExplicitlySet;
  

  // Visibility:
  boolean visible = true;

  // Validation:
  boolean valid = false;
  transient boolean validate = false;

  // Enabled:
  boolean enabled = true;

  // Notification:
  transient protected boolean notified = false;

  int[] traversalIDs;

  //Locale:
  Locale locale;

  private ComponentOrientation orientation;

  // Parent container:
  transient Container parent = null;

  // Background and foreground color:
  Color background;
  Color foreground;

  // Cursor
  transient Cursor cursor;

  // Events:
  transient ComponentListener componentListener;
  transient MouseMotionListener mouseMotionListener;
  transient MouseListener mouseListener;
  transient FocusListener focusListener;
  transient ActionListener actionListener;
  transient KeyListener keyListener;
  boolean newEventsOnly;

  transient boolean eventsEnabled = true;

  // Font:
  Font font;
  Font peerFont;

  // protected ComponentPeer peer;
  transient public ComponentPeer peer;

  Vector popups;

  /*
  ** Focus :
  **
  ** These fields are updated from Event_addFocusEvent (Event.c)
  ** and are used by the taskbar to revert the focus to the
  ** original Component that had focus..
  ** The next 2 methods are used to revert focus and to get 
  ** the Component which currently has focus.
  */

  transient private static Component focusComponent = null;
  transient private static Component focusComponentPrev = null;
  boolean hasFocus;
  
  // Component is focusable (Apache Harmony)
  private boolean focusable;

  // setFocusable has been called (Apache Harmony)
  private boolean calledSetFocusable;

  // setFocusable has been called (Apache Harmony)
  private boolean overriddenIsFocusable = true;

  private FocusControl focusControl = FocusControlFactory.create();
  
  private DropTarget dropTarget;

  final transient Toolkit toolkit = Toolkit.getDefaultToolkit();

  /**
   * Possible keys are: FORWARD_TRAVERSAL_KEYS, BACKWARD_TRAVERSAL_KEYS,
   * UP_CYCLE_TRAVERSAL_KEYS
   */
  private final Map traversalKeys = new HashMap();

  static {
    // hack to ensure Synchronizer.clinit() is called before any component is created
    Synchronizer.staticLockAWT();
    Synchronizer.staticUnlockAWT();
  }

  public static void revertFocus() {
	FocusCycle.prev(focusComponent);
  }

  public static Component getFocusComponent() {
    return focusComponent;
  }
  
  protected Component() {
    toolkit.lockAWT();
    try {
      orientation = ComponentOrientation.UNKNOWN;
      traversalIDs = this instanceof Container ? KeyboardFocusManager.contTraversalIDs : KeyboardFocusManager.compTraversalIDs;
      for (int i = 0; i < traversalIDs.length; ++i) {
        traversalKeys.put(new Integer(traversalIDs[i]), null);
      }
    } finally {
      toolkit.unlockAWT();
    }

    counter++;
    name = "Component" + counter;
	
   if(focusComponent==null) {
      focusComponent = this;
   }
	
    addNotify();
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return (name != null) ? name : "";
  }

  public void setForeground(Color color) {
    toolkit.lockAWT();
    try {
      this.foreground = color;
      peer.setForeground(color);
    }
    finally {
      toolkit.unlockAWT();
    }
  }

  public Color getForeground() {
    if (this.foreground != null) return this.foreground;
    return (parent != null) ? parent.getForeground() : null;
  }

  public void setBackground(Color color) {
    background = color;
    peer.setBackground(color);
  }

  public Color getBackground() {
    if (this.background != null) return this.background;
    return (parent != null) ? parent.getBackground() : null;
  }

  public java.awt.image.ColorModel getColorModel() {
    return peer.getColorModel();
  }

  public synchronized void setFont(Font font) {
    toolkit.lockAWT();
    try {
      this.font = font;
      valid = false;
      peer.setFont(font);
    }
    finally {
      toolkit.unlockAWT();
    }
  }

  public Font getFont() {
    if (this.font != null) {
      return this.font;
    }
    else if (parent != null) {
      return parent.getFont();
    }
    else {
      return DEFAULT_FONT;
    }
  }

  public FontMetrics getFontMetrics(Font font) {
    return peer.getFontMetrics((font == null) ? Component.DEFAULT_FONT : font);
  }

  public synchronized void setCursor(Cursor cursor) {
    this.cursor = cursor;
    peer.setCursor(cursor);
  }

  public Cursor getCursor() {
    return cursor;
  }

  public void setBounds(int x, int y, int width, int height) {
   
    toolkit.lockAWT();
    try {

      boolean l = (this.x != x || this.y != y) ? true : false;
      boolean s = (this.width != width || this.height != height) ? true : false;
   
      if (l || s) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // Invalidate the component:
        invalidate();

        if (componentListener != null) {
          if (s) {
            dispatchEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED));
          }
          
          if (l) {
            dispatchEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_MOVED));
          }
        }
       
        peer.setBounds(x, y, width, height);
      }
    }
    finally {
      toolkit.unlockAWT();
    }
  }

  public void setBounds(Rectangle rectangle) {
    setBounds(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
  }

  public Rectangle getBounds() {
    return new Rectangle(x, y, width, height);
  }

  public void setSize(int w, int h) {
    setBounds(this.x, this.y, w, h);
  }

  public void setSize(Dimension dimension) {
    setBounds(this.x, this.y, dimension.width, dimension.height);
  }

  public Dimension getSize() {
    return new Dimension(width, height);
  }

  /*
  ** Depricated methods.
  */

  public Dimension size() {
    return new Dimension(width, height);
  }
  
  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }

  public ComponentPeer getPeer() {
    return peer;
  }

  public boolean contains(int x, int y) {
    return (x < width && y < height);
  }

  public boolean contains(Point point) {
    return contains(point.x, point.y);
  }

  public Dimension getMinimumSize() {
    return minimumSize();
  }

  public Dimension getPreferredSize() {
    return preferredSize(); 
  }

  public Dimension getMaximumSize() {
    return maximumSize();
  }

  public Dimension minimumSize() {
    toolkit.lockAWT();
    try {
      if (minSize == null || valid == false) {
        minSize = peer.getMinimumSize();
      }
      return minSize;
    }
    finally {
      toolkit.unlockAWT();
    }
  }

  public Dimension preferredSize() {
    toolkit.lockAWT();
    try {
      if (prefSize == null || valid == false) {
        prefSize = peer.getPreferredSize();
      }
      return prefSize;
    }
    finally {
      toolkit.unlockAWT();
    }
  }

  public Dimension maximumSize() {
    return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
  }

  public void setLocation(int x, int y) {
    setBounds(x, y, this.width, this.height);
  }

  public void setLocation(Point point) {
    setBounds(point.x, point.y, this.width, this.height);
  }

  public Point getLocation() {
    toolkit.lockAWT();
    try {
      return new Point(x, y);
    }
    finally {
      toolkit.unlockAWT();
    }
  }

  public Point getLocationOnScreen() {
    toolkit.lockAWT();
    try {
      return peer.getLocationOnScreen();
    }
    finally {
      toolkit.unlockAWT();
    }
  }

  public Component getComponentAt(int x, int y) {
    return (contains(x, y) ? this : null);
  }

  public Component getComponentAt(Point point) {
    return (contains(point.x, point.y) ? this : null);
  }
    
  public void list(java.io.PrintStream out, int indent) {
  }

  public void list(java.io.PrintWriter out, int indent) {
  }

  public void list() {
    list(System.out,0);
  }

  public void list(java.io.PrintStream out) {
    list(out,0);
  }

  public void list(java.io.PrintWriter out) {
    list(out,0);
  }

  public Container getParent() {
    return parent;
  }

  public void doLayout() {
  }

  public float getAlignmentX() {
    return CENTER_ALIGNMENT;
  }

  public float getAlignmentY() {
    return CENTER_ALIGNMENT;
  }

  public void addNotify() {
    if (notified == false) {
      notified = true;
    }

    if (peer == null) {
      peer = getToolkit().createComponent(this);
    }
  }

  public void removeNotify() {
    moveFocus();
  }

  public synchronized void add(PopupMenu popup) {
    
    /*
    ** TODO: Do something useful. For now just ignore this since we don't need it..
    */
    
  }

  public synchronized void remove(MenuComponent popup) {
    
    /*
    ** TODO: Do something useful. For now just ignore this since we don't need it..
    */
    
  }

  public void setLocale(java.util.Locale locale){
    this.locale = locale;
  }

  public java.util.Locale getLocale() throws IllegalComponentStateException{
    if (locale != null) {
      return locale;
    }

    if (parent != null) {
      return parent.getLocale();
    }

    throw new IllegalComponentStateException("no locale set for this component or its parents");
  }

  public void requestFocus() {
    if (focusComponent != this) {
	  if(this.peer!=null) {
		this.peer.requestFocus();				
	  }				
		
      focusComponentPrev = focusComponent;
      focusComponent = this;
      if (focusComponentPrev != null) {
        focusComponentPrev.dispatchEvent
                  (new FocusEvent(focusComponentPrev, FocusEvent.FOCUS_LOST));
      }
      dispatchEvent(new FocusEvent(focusComponent, FocusEvent.FOCUS_GAINED));
    }
  }

  public void transferFocus() {
	  FocusCycle.next(this);
  }
  
  void transferFocus(int dir) {
    Container root = null;
    if (this instanceof Container) {
      Container cont = (Container) this;
      if (cont.isFocusCycleRoot()) {
          root = cont.getFocusTraversalRoot();
      }
    }
    if (root == null) {
      root = getFocusCycleRootAncestor();
    }
    // transfer focus up cycle if root is unreachable
    Component comp = this;
    while ((root != null)
       && !(root.isFocusCycleRoot() && root.isShowing() && root.isEnabled() && root.isFocusable())) {
      comp = root;
      root = root.getFocusCycleRootAncestor();
    }
    if (root == null) {
      return;
    }
    FocusTraversalPolicy policy = root.getFocusTraversalPolicy();
    Component nextComp = null;
    switch (dir) {
      case KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS:
        nextComp = policy.getComponentAfter(root, comp);
        break;
      case KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS:
        nextComp = policy.getComponentBefore(root, comp);
        break;
    }
    if (nextComp != null) {
      nextComp.requestFocus(false);
    }
  }

  public void setFocusable(boolean focusable) {
    boolean oldFocusable;
    toolkit.lockAWT();
    try {
      calledSetFocusable = true;
      oldFocusable = this.focusable;
      this.focusable = focusable;
      if (!focusable) {
        moveFocus();
      }
    }
    finally {
      toolkit.unlockAWT();
    }
        //firePropertyChange("focusable", oldFocusable, focusable); //$NON-NLS-1$
    }

    public boolean isFocusable() {
        toolkit.lockAWT();
        try {
            return isFocusTraversable();
        } finally {
            toolkit.unlockAWT();
        }
    }

  /**
   * @status not implemented
   * @remark not compliant with specifications
   */
  public boolean isFocusTraversable() {
        overriddenIsFocusable = false;
	  if(this.peer!=null)
	  {
		return this.peer.isFocusTraversable();
	  }
	  else
	  {
		return false;
	  }
  }

  public void validate() {
    valid = true;
  }

  public void invalidate() {
    toolkit.lockAWT();
    try {
      if (valid) {
        minSize = null;
        prefSize = null;
        valid = false;
        // if parent exists, invalidate parent component
        if ((parent != null) && (parent.valid)) {
          parent.invalidate();
        }
      }
    } finally {
      toolkit.unlockAWT();
    }
  }

  public boolean isValid() {
    return valid;
  }

  public void setEnabled(boolean condition) {
    enabled = condition;
    peer.setEnabled(condition);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public synchronized void addComponentListener(ComponentListener listener) {
    componentListener = AWTEventMulticaster.add(componentListener, listener);

  }

  public synchronized void removeComponentListener(ComponentListener listener) {
    componentListener = AWTEventMulticaster.remove(componentListener, listener);
  }

  public synchronized void addFocusListener(java.awt.event.FocusListener listener) {
    focusListener = AWTEventMulticaster.add(focusListener, listener);
  }

  public synchronized void removeFocusListener(java.awt.event.FocusListener listener) {
    focusListener = AWTEventMulticaster.remove(focusListener, listener);
  }

  public synchronized void addKeyListener(java.awt.event.KeyListener listener) {
    keyListener = AWTEventMulticaster.add(keyListener, listener);
  }

  public synchronized void removeKeyListener(java.awt.event.KeyListener listener) {
    keyListener = AWTEventMulticaster.remove(keyListener, listener);
  }

  public synchronized void addMouseListener(MouseListener listener) {
    mouseListener = AWTEventMulticaster.add(mouseListener, listener);
  }

  public synchronized void removeMouseListener(MouseListener listener) {
    mouseListener = AWTEventMulticaster.remove(mouseListener, listener);
  }

  public synchronized void addMouseMotionListener(MouseMotionListener listener) {
    mouseMotionListener = AWTEventMulticaster.add(mouseMotionListener, listener);
  }

  public synchronized void removeMouseMotionListener(java.awt.event.MouseMotionListener listener) {
    mouseMotionListener = AWTEventMulticaster.remove(mouseMotionListener, listener);
  }

  /**
   * @status dummy implementation
   * @remark not compliant with specifications: just prints a message
   */
  protected final void enableEvents(long eventTypes) {
  }

  protected final void disableEvents(long eventTypes) {
  }

  protected void dispatchEventImpl(AWTEvent event) {
    if (enabled == false && event instanceof InputEvent) {
      // If a component is not enabled it can not generate events
      // nor respond to events (or user input for that matter).
      return;
    }

    peer.handleEvent((AWTEvent)event);
    if(dropTarget != null && event instanceof MouseEvent){
      com.acunia.wonka.rudolph.DropTargetEvent.setDropTargetContext(dropTarget.getDropTargetContext());
      com.acunia.wonka.rudolph.DropTargetEvent.getDropTargetEvent().dispatch(((MouseEvent)event));
    }

    if(dropTarget == null && event instanceof MouseEvent && event.getID() == MouseEvent.MOUSE_RELEASED_AFTER_DRAG) {
      com.acunia.wonka.rudolph.DropTargetEvent.setInProgress(false,null);
    }
    else {
      processEvent(event);
    }
  }

  public final void dispatchEvent(AWTEvent event) {
    dispatchEventImpl(event);
  }

  protected void processEvent(AWTEvent event) {
    if (event instanceof MouseEvent) {
      switch(event.getID()) {
        case MouseEvent.MOUSE_ENTERED:
        case MouseEvent.MOUSE_EXITED:
        case MouseEvent.MOUSE_PRESSED:
        case MouseEvent.MOUSE_RELEASED:
        case MouseEvent.MOUSE_CLICKED:
          processMouseEvent((MouseEvent) event);
          break;
        case MouseEvent.MOUSE_MOVED:
        case MouseEvent.MOUSE_DRAGGED:
          processMouseMotionEvent((MouseEvent) event);
          break;
      }
    }
    else if (event instanceof KeyEvent) {
      processKeyEvent((KeyEvent) event);
    }
    else if (event instanceof FocusEvent) {
      processFocusEvent((FocusEvent) event);
    }
    else if (event instanceof ItemEvent) {
      processEvent(event);
    }
    else if (event instanceof ComponentEvent) {
      processComponentEvent((ComponentEvent) event);
    }
  }

  protected void processComponentEvent(ComponentEvent event) {
    if (componentListener != null) {
      switch(event.getID()) {
        case ComponentEvent.COMPONENT_RESIZED:
          componentListener.componentResized(event);
          break;
        case ComponentEvent.COMPONENT_MOVED:
          componentListener.componentMoved(event);
          break;
        case ComponentEvent.COMPONENT_SHOWN:
          componentListener.componentShown(event);
          break;
        case ComponentEvent.COMPONENT_HIDDEN:
          componentListener.componentHidden(event);
          break;
      }
    }
  }

  protected void processFocusEvent(FocusEvent event) {
    if (focusListener != null) {
      switch(event.getID()) {
      case FocusEvent.FOCUS_GAINED:
        focusListener.focusGained(event);
        break;
      case FocusEvent.FOCUS_LOST:
        focusListener.focusLost(event);
        break;
      }
    }
  }

  protected void processKeyEvent(java.awt.event.KeyEvent event) {
    if (keyListener != null) { // && (!event.isConsumed())) {
      switch(event.getID()) {
        case KeyEvent.KEY_TYPED:
          keyListener.keyTyped(event);
          break;
        case KeyEvent.KEY_PRESSED:
          keyListener.keyPressed(event);
          break;
        case KeyEvent.KEY_RELEASED:
          keyListener.keyReleased(event);
          break;
      }
    }
    
    if(!event.isConsumed())
    {
        this.focusControl.processKeyEvent(event);
    }
  }

  protected void processMouseEvent(MouseEvent event) {
    if (mouseListener != null) {
      switch(event.getID()) {
        case MouseEvent.MOUSE_EXITED:
          mouseListener.mouseExited(event);
          break;
        case MouseEvent.MOUSE_ENTERED:
          mouseListener.mouseEntered(event);
          break;
        case MouseEvent.MOUSE_PRESSED:
          mouseListener.mousePressed(event);
          break;
        case MouseEvent.MOUSE_RELEASED:
          mouseListener.mouseReleased(event);
          break;
        case MouseEvent.MOUSE_CLICKED:
          mouseListener.mouseClicked(event);
          break;
      }
    }
  }
  
  protected void processMouseMotionEvent(java.awt.event.MouseEvent event) {
    if (mouseMotionListener != null) {
      switch(event.getID()) {
        case MouseEvent.MOUSE_MOVED:
          mouseMotionListener.mouseMoved(event);
          break;
        case MouseEvent.MOUSE_DRAGGED:
          mouseMotionListener.mouseDragged(event);
          break;
      }
    }
  }
  
  /**
   * @status not implemented
   * @remark not compliant with specifications
   */
  public void print(Graphics context) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @status not implemented
   * @remark not compliant with specifications
   */
  public void printAll(Graphics context) {
    throw new RuntimeException("Not implemented");
  }

  public String toString() {
    String display = (name == null)? getClass().getName() : name;
    return display +" - bounds: x = "+ x +", y = "+ y +", w = "+ width +", h = "+ height;
  }

  /**
   * @status not implemented
   * @remark not compliant with specifications
   */
  protected String paramString() {
    return getClass().getName() +"<"+name+"> - bounds("+ x +", "+ y +", "+ width +", "+ height+")";
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean condition) {
    toolkit.lockAWT();
    try {
      if (condition) {
        // Show component:
  
        if (!visible) {

          visible = true;

          invalidate();
          
          peer.setVisible(condition);

          validate();
          
          // Component listener:
          if (componentListener != null) {
            dispatchEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_SHOWN));
          }
        }
      }
      else {
        // Hide component:
        if (visible) {
          visible = false;

          peer.setVisible(condition);

          invalidate();

          // Component listener:
          if (componentListener != null) {
            dispatchEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_HIDDEN));
          }
        }
      }
    }
    finally {
        toolkit.unlockAWT();
    }
  }

  public void show() {
    setVisible(true);
  }

  public void hide() {
    setVisible(false);
    moveFocusOnHide();
  }

  public boolean isDisplayable() {
    return (peer != null);
  }

  /**
   * @status not implemented
   * @remark not compliant with specifications
   */
  public boolean isShowing() {
    return isVisible();
  }

  /**
   * @status implemented
   * @remark not compliant with specifications: a Graphics object is 
   *         always constructed and returned, even if the component 
   *         is not visible or its peer component does not exist.
   */
  public Graphics getGraphics() {
    return peer.getGraphics();
  }

  public Toolkit getToolkit() {
    return Toolkit.getDefaultToolkit();
  }

  public Image createImage(int w, int h) {
    return peer.createImage(w, h);
  }

  public Image createImage(java.awt.image.ImageProducer producer) {
    return peer.createImage(producer);
  }

  public boolean prepareImage(Image image, ImageObserver observer) {
    return prepareImage(image, -1, -1, observer);
  }

  public boolean prepareImage(Image image, int w, int h, ImageObserver observer) {
    return peer.prepareImage(image, w, h, observer);
  }

  public int checkImage(Image image, ImageObserver observer) {
    return checkImage(image, -1, -1, observer);
  }

  public int checkImage(Image image, int w, int h, ImageObserver observer) {
    return peer.checkImage(image, w, h, observer);
  }

  /*
  ** Called by Image.getWidth(ImageObserver) and getHeight(ImageObserver). Graphics.drawImage(..... ImageObserver)
  ** and Component prepareImage(ImageObserver) / CheckImage(ImageObserver) to send diagnostic data to the
  ** <this>-ImageObserver-interface Component.
  ** designed to be overridden by subclasses to get the diagnostics
  ** returns true = <yes, there are still more of these messages to come> unless the flags contain ImageObserver.ALLBITS
  */

  public boolean imageUpdate(Image image, int flags, int x, int y, int w, int h) {
    if((flags & ImageObserver.FRAMEBITS) != 0)
      repaint();
    return((flags & ImageObserver.ALLBITS) == 0);
  }

  public void repaint() {
    peer.repaint(0, 0, 0, width, height);
  }

  public void repaint(long ms) {
    peer.repaint(ms, 0, 0, width, height);
  }

  public void repaint(int x, int y, int w, int h) {
    peer.repaint(0, x, y, w, h);
  }

  public void repaint(long ms, int x, int y, int w, int h) {
    peer.repaint(ms, x, y, w, h);
  }

  public void paint(Graphics context) {
  }

  public void update(Graphics g) {
    g.clearRect(0, 0, width, height);
    paint(g);
  }

  public void paintAll(Graphics context) {
    // Validate container
    validate();
  }
  
/*  private void readObject(java.io.ObjectInputStream s) throws ClassNotFoundException, java.io.IOException {
    System.out.println("Not yet implemented");
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    System.out.println("Not yet implemented");
  }*/

  /*
  ** All the following methods are very very deprecated...
  */

  public boolean action(Event evt, Object arg) {
    System.out.println("Not implemented - Deprecated");
    return false;
  }

  public Rectangle bounds() {
    return getBounds();
  }

  public void deliverEvent(Event e) {
    System.out.println("Not implemented - Deprecated");
  }

  public void disable() {
    enabled = false;
    peer.setEnabled(false);
  }

  public void enable() {
    enabled = true;
    peer.setEnabled(true);
  }

  public void enable(boolean cond) {
    setEnabled(cond);
  }

  public boolean gotFocus(Event evt, Object arg) {
    System.out.println("Not implemented - Deprecated");
    return false;
  }

  public boolean handleEvent(Event evt) {
    System.out.println("Not implemented - Deprecated");
    return false;
  }

  public boolean inside(int x, int y) {
    return contains(x, y);
  }
  
  public boolean keyDown(Event evt, int key) {
    System.out.println("Not implemented - Deprecated");
    return false;
  }

  public boolean keyUp(Event evt, int key) {
    System.out.println("Not implemented - Deprecated");
    return false;
  }

  public void layout() {
    System.out.println("Not implemented - Deprecated");
  }

  public Component locate(int x, int y) {
    return getComponentAt(x, y);
  }

  public Point location() {
    return getLocation();
  }

  public boolean lostFocus(Event evt, Object arg) {
    System.out.println("Not implemented - Deprecated");
    return false;
  }

  public boolean mouseDown(Event evt, int x, int y) {
    System.out.println("Not implemented - Deprecated");
    return false;
  }
  
  public boolean mouseDrag(Event evt, int x, int y) {
    System.out.println("Not implemented - Deprecated");
    return false;
  }
  
  public boolean mouseEnter(Event evt, int x, int y) {
    System.out.println("Not implemented - Deprecated");
    return false;
  }
  
  public boolean mouseExit(Event evt, int x, int y) {
    System.out.println("Not implemented - Deprecated");
    return false;
  }
  
  public boolean mouseMove(Event evt, int x, int y) {
    System.out.println("Not implemented - Deprecated");
    return false;
  }
  
  public boolean mouseUp(Event evt, int x, int y) {
    System.out.println("Not implemented - Deprecated");
    return false;
  }
  
  public void move(int x, int y) {
    setLocation(x, y);
  }

  public void nextFocus() {
    transferFocus();
  }

  public void reshape(int x, int y, int w, int h) {
    setBounds(x, y, w, h);
  }

  public void resize(int w, int h) {
    setSize(w, h);
  }

  public void resize(Dimension d) {
    setSize(d);
  }

  public void show(boolean cond) {
    setVisible(cond);
  }

  /*
  **methods 1.2
  */

  public void setDropTarget(DropTarget dt){
    dropTarget = dt;
  }

  public DropTarget getDropTarget(){
    return dropTarget;
  }
  
  /**
   ** Called by EventQueue.postEvent() to see whether newEvent should be
   ** merged with oldEvent (which was found in the queue). oldEvent and
   ** newEvent are guaranteed to have the same source and ID. Returns 
   ** null if no merging is possible, or a merged event with which 
   ** postEvent() will overwrite oldEvent on the queue.
   ** <p>The default implementation currently does nothing; it would be
   ** a good idea to merge paint/repaint events here. User-defined
   ** components may override this.
   */

  protected AWTEvent coalesceEvents(AWTEvent oldEvent, AWTEvent newEvent)  
  {
      return null;
  }

  // (from Apache Harmony)
    /**
     * This method is called when some property of a component changes, making
     * it unfocusable, e. g. hide(), removeNotify(), setEnabled(false),
     * setFocusable(false) is called, and therefore automatic forward focus
     * traversal is necessary
     */
    void moveFocus() {
        /* temporary solution for Rudolph */
	  FocusCycle.next(this);
        /*
        ** Apache code
        // don't use transferFocus(), but query focus traversal policy directly
        // and if it returns null, transfer focus up cycle
        // and find next focusable component there
        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        Container root = kfm.getCurrentFocusCycleRoot();
        Component nextComp = this;
        boolean success = !isFocusOwner();
        while (!success) {
            if (root != nextComp.getFocusCycleRootAncestor()) {
                // component was probably removed from container
                // so focus will be lost in some time
                return;
            }
            nextComp = root.getFocusTraversalPolicy().getComponentAfter(root, nextComp);
            if (nextComp == this) {
                nextComp = null; // avoid looping
            }
            if (nextComp != null) {
                success = nextComp.requestFocusInWindow();
            } else {
                nextComp = root;
                root = root.getFocusCycleRootAncestor();
                // if no acceptable component is found at all - clear global
                // focus owner
                if (root == null) {
                    if (nextComp instanceof Window) {
                        Window wnd = (Window) nextComp;
                        wnd.setFocusOwner(null);
                        wnd.setRequestedFocus(null);
                    }
                    kfm.clearGlobalFocusOwner();
                    return;
                }
            }
        }
        */
    }

    /**
     * For Container there's a difference between moving focus when being made
     * invisible or made unfocusable in some other way, because when container
     * is made invisible, component still remains visible, i. e. its hide() or
     * setVisible() is not called.
     */
    void moveFocusOnHide() {
        moveFocus();
    }

    /**
     * @return true if focusability was explicitly set via a call to
     *         setFocusable() or via overriding isFocusable() or
     *         isFocusTraversable()
     */
    boolean isFocusabilityExplicitlySet() {
        return calledSetFocusable || overriddenIsFocusable;
    }

    public Container getFocusCycleRootAncestor() {
      toolkit.lockAWT();
      try {
        for (Container c = parent; c != null; c = c.getParent()) {
          if (c.isFocusCycleRoot()) {
            return c;
          }
        }
        return null;
      } finally {
        toolkit.unlockAWT();
      }
    }

    public boolean isFocusCycleRoot(Container container) {
      toolkit.lockAWT();
      try {
        return getFocusCycleRootAncestor() == container;
      } finally {
        toolkit.unlockAWT();
      }
    }

   /**
     * Gets only parent of a child component, but not owner of a window.
     * 
     * @return parent of child component, null if component is a top-level
     *         (Window instance)
     */
    Container getRealParent() {
        return (!(this instanceof Window) ? getParent() : null);
    }

    public Object getTreeLock() {
      return toolkit.awtTreeLock;
    }

    /**
     * @return true if component has a focusable peer
     */
    boolean isPeerFocusable() {
        // The recommendations for Windows and Unix are that
        // Canvases, Labels, Panels, Scrollbars, ScrollPanes, Windows,
        // and lightweight Components have non-focusable peers,
        // and all other Components have focusable peers.
        if (this instanceof Canvas || this instanceof Label || this instanceof Panel
                || this instanceof Scrollbar || this instanceof ScrollPane
                || this instanceof Window || isLightweight()) {
            return false;
        }
        return true;
    }

   public boolean isLightweight() {
        toolkit.lockAWT();
        try {
            // [CG 20120818] fake it for now
            return false;
        } finally {
            toolkit.unlockAWT();
        }
    }

    Window getWindowAncestor() {
        Component par;
        for (par = this; par != null && !(par instanceof Window); par = par.getParent()) {
            ;
        }
        return (Window) par;
    }

    /**
     * @Deprecated
     */
    public boolean postEvent(Event evt) {
        boolean handled = handleEvent(evt);
        if (handled) {
            return true;
        }
        // propagate non-handled events up to parent
        Component par = parent;
        // try to call postEvent only on components which
        // override any of deprecated method handlers
        // while (par != null && !par.deprecatedEventHandler) {
        // par = par.parent;
        // }
        // translate event coordinates before posting it to parent
        if (par != null) {
            evt.translate(x, y);
            par.postEvent(evt);
        }
        return false;
    }

    boolean requestFocusImpl(boolean temporary, boolean crossWindow, boolean rejectionRecovery) {
        if (!rejectionRecovery && isFocusOwner()) {
            return true;
        }
        Window wnd = getWindowAncestor();
        Container par = getRealParent();
        if ((par != null) && par.isRemoved) {
            return false;
        }
        if (!isShowing() || !isFocusable() || !wnd.isFocusableWindow()) {
            return false;
        }
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().requestFocus(this,
                temporary, crossWindow, true);
    }

    public boolean isFocusOwner() {
        toolkit.lockAWT();
        try {
            return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this;
        } finally {
            toolkit.unlockAWT();
        }
    }

    public void transferFocusUpCycle() {
        toolkit.lockAWT();
        try {
            KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            Container root = kfm.getCurrentFocusCycleRoot();
            
            if(root == null) {
                return;
            }
            
            boolean success = false;
            Component nextComp = null;
            Container newRoot = root;
            do {
                nextComp = newRoot instanceof Window ? newRoot.getFocusTraversalPolicy()
                        .getDefaultComponent(newRoot) : newRoot;
                newRoot = newRoot.getFocusCycleRootAncestor();
                if (nextComp == null) {
                    break;
                }
                success = nextComp.requestFocusInWindow();
                if (newRoot == null) {
                    break;
                }
                kfm.setGlobalCurrentFocusCycleRoot(newRoot);
            } while (!success);
            if (!success && root != newRoot) {
                kfm.setGlobalCurrentFocusCycleRoot(root);
            }
        } finally {
            toolkit.unlockAWT();
        }
    }

    public void transferFocusBackward() {
        toolkit.lockAWT();
        try {
            transferFocus(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        } finally {
            toolkit.unlockAWT();
        }
    }

    public Set getFocusTraversalKeys(int id) {
        toolkit.lockAWT();
        try {
            Integer kId = new Integer(id);
            KeyboardFocusManager.checkTraversalKeysID(traversalKeys, kId);
            Set keys = (Set) traversalKeys.get(kId);
            if (keys == null && parent != null) {
                keys = parent.getFocusTraversalKeys(id);
            }
            if (keys == null) {
                keys = KeyboardFocusManager.getCurrentKeyboardFocusManager()
                        .getDefaultFocusTraversalKeys(id);
            }
            return keys;
        } finally {
            toolkit.unlockAWT();
        }
    }

   /**
     * "Recursive" isEnabled().
     * 
     * @return true if not only component itself is enabled but its heavyweight
     *         parent is also "indirectly" enabled
     */
    boolean isIndirectlyEnabled() {
        Component comp = this;
        while (comp != null) {
            if (!comp.isLightweight() && !comp.isEnabled()) {
                return false;
            }
            comp = comp.getRealParent();
        }
        return true;
    }

   boolean isKeyEnabled() {
        if (!isEnabled()) {
            return false;
        }
        return isIndirectlyEnabled();
    }

    protected boolean requestFocusInWindow(boolean temporary) {
        toolkit.lockAWT();
        try {
            Window wnd = getWindowAncestor();
            if ((wnd == null) || !wnd.isFocused()) {
                return false;
            }
            return requestFocusImpl(temporary, false, false);
        } finally {
            toolkit.unlockAWT();
        }
    }

    public boolean requestFocusInWindow() {
      return requestFocusInWindow(false);
    }

    protected boolean requestFocus(boolean temporary) {
        toolkit.lockAWT();
        try {
            return requestFocusImpl(temporary, true, false);
        } finally {
            toolkit.unlockAWT();
        }
    }


}

