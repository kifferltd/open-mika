/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
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

import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.MouseDragGestureRecognizer;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CanvasPeer;
import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ComponentPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.DialogPeer;
import java.awt.peer.FileDialogPeer;
import java.awt.peer.FontPeer;
import java.awt.peer.FramePeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuItemPeer;
import java.awt.peer.MenuPeer;
import java.awt.peer.PanelPeer;
import java.awt.peer.PopupMenuPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextFieldPeer;
import java.awt.peer.WindowPeer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public abstract class Toolkit {
  private static final String RESOURCE_PATH = "mika.awt.resources.AWTProperties"; //$NON-NLS-1$

  private static final ResourceBundle properties = loadResources(RESOURCE_PATH);

  // [CG 20120817] Harmony delegates this to a ContextStorage object
  // TODO: tell synchronizer about our dispatch thread so that it can give it priority.
  // protected static final Synchronizer synchronizer = new Synchronizer();

  private boolean bDynamicLayoutSet = true;

  KeyboardFocusManager currentKeyboardFocusManager;

    private class AWTTreeLock {
    }

    final Object awtTreeLock = new AWTTreeLock();

  /**
   * The set of desktop properties that user set directly.
   */
  private final HashSet userPropSet = new HashSet();

  protected final Map desktopProperties;

  protected final PropertyChangeSupport desktopPropsSupport;

  // [CG 20120819] Perhaps a BitSet would be better?
  private HashMap keyLockingState = new HashMap();

  /*
   * A lot of methods must throw HeadlessException
   * if <code>GraphicsEnvironment.isHeadless()</code> returns <code>true</code>.
   */
  static void checkHeadless() throws HeadlessException {
      if (GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance())
          throw new HeadlessException();
  }

  final void lockAWT() {
    Synchronizer.staticLockAWT();
  }

  final void unlockAWT() {
    Synchronizer.staticUnlockAWT();
  }

  /**
   * InvokeAndWait under AWT lock. W/o this method system can hang up.
   * Added to support modality (Dialog.show() & PopupMenu.show()) from
   * not event dispatch thread. Use in other cases is not recommended.
   *
   * Still can be called only for whole API methods that
   * cannot be called from other classes API methods.
   * Examples:
   *      show() for modal dialogs    - correct, only user can call it,
   *                                      directly or through setVisible(true)
   *      setBounds() for components  - incorrect, setBounds()
   *                                      can be called from layoutContainer()
   *                                      for layout managers
  final void unsafeInvokeAndWait(Runnable runnable) throws InterruptedException,
          InvocationTargetException {
      synchronizer.storeStateAndFree();
      try {
          EventQueue.invokeAndWait(runnable);
      } finally {
          synchronizer.lockAndRestoreState();
      }
  }

  final Synchronizer getSynchronizer() {
      return synchronizer;
  }
   */

  public static String getProperty(String propName, String defVal) {
        if (propName == null) {
            throw new NullPointerException("Property name is null");
        }
        Synchronizer.staticLockAWT();
        try {
            String retVal = null;
            if (properties != null) {
                try {
                    retVal = properties.getString(propName);
                } catch (MissingResourceException e) {
                } catch (ClassCastException e) {
                }
            }
            return (retVal == null) ? defVal : retVal;
        } finally {
            Synchronizer.staticUnlockAWT();
        }
  }

  public static Toolkit getDefaultToolkit() {
      String className = GetSystemProperty.TOOLKIT;
      if (className != null) {
        // class to be loaded needs to be on bootstrap class path!
        try {
          Class toolkitClass = Class.forName(className);
        }
        catch (Throwable t) {
          throw new AWTError("Unable to load default toolkit " + className + " on bootclasspath - got " + t);
        }
      }
      Toolkit toolkit = com.acunia.wonka.rudolph.Toolkit.getInstance();
      return (java.awt.Toolkit) toolkit;
  }

  private static ResourceBundle loadResources(String path) {
        try {
            return ResourceBundle.getBundle(path);
        } catch (MissingResourceException e) {
            return null;
        }
  }

  public Toolkit() {        
    desktopProperties = new HashMap();
    desktopPropsSupport = new PropertyChangeSupport(this);
    init();
  }

  void init() {
        lockAWT();
        try {
            new EventQueue(this); // create the system EventQueue
        } finally {
            unlockAWT();
        }
  }

  public abstract void sync();

  protected abstract TextAreaPeer createTextArea(TextArea textArea) throws HeadlessException;

  public abstract int checkImage(Image image, int w, int h, ImageObserver observer);

  protected abstract DialogPeer createDialog(Dialog dialog) throws HeadlessException;
  
  public abstract Image createImage(ImageProducer producer);

  public abstract Image createImage(String filename);

  public abstract Image createImage(byte[] imageData, int offset, int count);

  public abstract Image createImage(URL url);

  public abstract ColorModel getColorModel() throws HeadlessException;

  /**
   * @deprecated
   */
  public abstract FontMetrics getFontMetrics(Font font);

  public abstract boolean prepareImage(Image image, int w, int h, ImageObserver observer);

  public abstract void beep();

  protected abstract ButtonPeer createButton(Button button) throws HeadlessException;

  protected abstract CanvasPeer createCanvas(Canvas canvas);
  
  protected abstract CheckboxPeer createCheckbox(Checkbox checkbox);
  
  protected abstract CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem checkBoxMenuItem) throws HeadlessException;
  
  protected abstract ChoicePeer createChoice(Choice choice) throws HeadlessException;
  
  public abstract DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException;

  protected abstract FileDialogPeer createFileDialog(FileDialog fileDialog) throws HeadlessException;

  protected abstract FramePeer createFrame(Frame frame) throws HeadlessException;
  
  protected abstract LabelPeer createLabel(Label label) throws HeadlessException;
  
  protected abstract ListPeer createList(List list) throws HeadlessException;
  
  protected abstract MenuPeer createMenu(Menu menu) throws HeadlessException;
  
  protected abstract MenuBarPeer createMenuBar(MenuBar menuBar) throws HeadlessException;
  
  protected abstract MenuItemPeer createMenuItem(MenuItem menuItem) throws HeadlessException;
  
  protected abstract PanelPeer createPanel(Panel panel);
  
  protected abstract PopupMenuPeer createPopupMenu(PopupMenu popupMenu) throws HeadlessException;
  
  protected abstract ScrollPanePeer createScrollPane(ScrollPane scrollPane) throws HeadlessException;

  protected abstract ScrollbarPeer createScrollbar(Scrollbar scrollbar) throws HeadlessException;
  
  protected abstract TextFieldPeer createTextField(TextField textField) throws HeadlessException;
 
  protected abstract WindowPeer createWindow(Window window) throws HeadlessException;
  
  /**
   * @deprecated
   */
  public abstract String[] getFontList();

  /**
   * @deprecated
   */
  protected abstract FontPeer getFontPeer(String a0, int a1);

  public abstract Image getImage(String filename);

  public abstract Image getImage(URL url);
  
  public abstract PrintJob getPrintJob(Frame frame, String title, Properties properties);

  public abstract int getScreenResolution();
  
  public abstract Dimension getScreenSize() throws HeadlessException;

  public abstract Clipboard getSystemClipboard() throws HeadlessException;

  protected abstract EventQueue getSystemEventQueueImpl();

  public void addPropertyChangeListener(String propName, PropertyChangeListener l) {
    lockAWT();
    try {
      if (desktopProperties.isEmpty()) {
        initializeDesktopProperties();
      }
    } finally {
      unlockAWT();
    }
    if (l != null) { // there is no guarantee that null listener will not be added
      desktopPropsSupport.addPropertyChangeListener(propName, l);
    }
  }

  // FIXME The return type here is wrong - should be LightweightPeer.
  // However this is not compatible with the way the method is [ab]used by Rudolph.
  protected ComponentPeer createComponent(Component comp) {
    throw new RuntimeException("not implemented");
  }

  public Image createImage(byte[] imagedata) {
    return createImage(imagedata, 0, imagedata.length);
  }

  protected static Container getNativeContainer(Component c) {
    Synchronizer.staticLockAWT();
    try {
      //TODO: implement
      throw new RuntimeException("not implemented");
    } finally {
      Synchronizer.staticUnlockAWT();
    }
  }

  public PropertyChangeListener[] getPropertyChangeListeners() {
    return desktopPropsSupport.getPropertyChangeListeners();
  }

  public PropertyChangeListener[] getPropertyChangeListeners(String propName) {
    return desktopPropsSupport.getPropertyChangeListeners(propName);
  }

  public void removePropertyChangeListener(String propName, PropertyChangeListener l) {
    desktopPropsSupport.removePropertyChangeListener(propName, l);
  }

  public Cursor createCustomCursor(Image img, Point hotSpot, String name)
      throws IndexOutOfBoundsException, HeadlessException {
    lockAWT();
    try {
      int w = img.getWidth(null);
      int h = img.getHeight(null);

      if (w < 0 || h < 0) {
        // Fix for HARMONY-4491
        hotSpot.x = 0;
        hotSpot.y = 0;
      } else if (hotSpot.x < 0 || hotSpot.x >= w
            || hotSpot.y < 0 || hotSpot.y >= h) {
        throw new IndexOutOfBoundsException("invalid hotSpot");
      }
      // return new Cursor(name, img, hotSpot);
      return new com.acunia.wonka.rudolph.CustomCursor(img, hotSpot, name);
    } finally {
      unlockAWT();
    }
  }

  // public DragGestureRecognizer createDragGestureRecognizer(Class abstractRecognizerClass, DragSource ds,
  //                                                        Component c, int srcActions, DragGestureListener dgl){
  //   if(java.awt.dnd.MouseDragGestureRecognizer.class.isAssignableFrom(abstractRecognizerClass)){
  //     com.acunia.wonka.rudolph.MouseDragGestureRecognizer mgr = new com.acunia.wonka.rudolph.MouseDragGestureRecognizer(ds,c,srcActions, dgl);
  //     return ((java.awt.dnd.MouseDragGestureRecognizer) mgr);
  //   }
  //   com.acunia.wonka.rudolph.DragGestureRecognizer dgr = new com.acunia.wonka.rudolph.DragGestureRecognizer(ds,c,srcActions, dgl);
  //   return ((java.awt.dnd.DragGestureRecognizer) dgr);
  // }


  public DragGestureRecognizer createDragGestureRecognizer(
            Class recognizerAbstractClass, DragSource ds, Component c, int srcActions,
            DragGestureListener dgl) {
    if (recognizerAbstractClass == null) {
      return null;
    }
    if (recognizerAbstractClass.isAssignableFrom(MouseDragGestureRecognizer.class)) {
      return (DragGestureRecognizer) new DefaultMouseDragGestureRecognizer(ds, c, srcActions, dgl);
    }
    return null;
  }

  public Dimension getBestCursorSize(int prefWidth, int prefHeight) throws HeadlessException {
    // custom cursors not supported
    return new Dimension(0, 0);
  }

  public final Object getDesktopProperty(String propName) {
    lockAWT();
    try {
      if (desktopProperties.isEmpty()) {
        initializeDesktopProperties();
      }
      if (propName.equals("awt.dynamicLayoutSupported")) { //$NON-NLS-1$
        // dynamicLayoutSupported is special case
        return Boolean.valueOf(isDynamicLayoutActive());
      }
      Object val = desktopProperties.get(propName);
      if (val == null) {
        // try to lazily load prop value
        // just for compatibility, our lazilyLoad is empty
        val = lazilyLoadDesktopProperty(propName);
      }
      return val;
    } finally {
      unlockAWT();
    }
  }

    public boolean getLockingKeyState(int keyCode) throws UnsupportedOperationException {

        if (keyCode != KeyEvent.VK_CAPS_LOCK &&
            keyCode != KeyEvent.VK_NUM_LOCK &&
            keyCode != KeyEvent.VK_SCROLL_LOCK &&
            keyCode != KeyEvent.VK_KANA_LOCK) {
            throw new IllegalArgumentException();
        }

        return ((Boolean) keyLockingState.get(new Integer(keyCode))).booleanValue();
    }

    public int getMaximumCursorColors() throws HeadlessException {
      // custom cursors not supported
        return 0;
    }

    public int getMenuShortcutKeyMask() throws HeadlessException {
        return InputEvent.CTRL_MASK;
    }

    // public PrintJob getPrintJob(Frame a0, String a1, JobAttributes a2, PageAttributes a3) throws NotImplementedException {
    //     throw RuntimeException("not implemented");
    // }

    public Insets getScreenInsets(GraphicsConfiguration gc) throws HeadlessException {
        if (gc == null) {
            throw new NullPointerException();
        }
        lockAWT();
        try {
            return new Insets(0, 0, 0, 0); //TODO: get real screen insets
        } finally {
            unlockAWT();
        }
    }

    public final EventQueue getSystemEventQueue() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkAwtEventQueueAccess();
        }
        return getSystemEventQueueImpl();
    }

    public Clipboard getSystemSelection() throws HeadlessException {
        // TODO
        return null;

       /*
        lockAWT();
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkSystemClipboardAccess();
            }
            if (systemSelection == null) {
                systemSelection = dtk.getNativeSelection();
            }
            return systemSelection;
        } finally {
            unlockAWT();
        }
        */
    }

    protected void initializeDesktopProperties() {
        lockAWT();
        try {
            //wtk.getSystemProperties().init(desktopProperties);
        } finally {
            unlockAWT();
        }
    }

    public boolean isDynamicLayoutActive() throws HeadlessException {
        lockAWT();
        try {
            // always return true
            return true;
        } finally {
            unlockAWT();
        }
    }

    protected boolean isDynamicLayoutSet() throws HeadlessException {
        lockAWT();
        try {
            return bDynamicLayoutSet;
        } finally {
            unlockAWT();
        }
    }

    public boolean isFrameStateSupported(int state) throws HeadlessException {
        // not supported by rudolph
        return false;
    }

    protected Object lazilyLoadDesktopProperty(String propName) {
        return null;
    }

    protected void loadSystemColors(int[] colors) throws HeadlessException {
        // not supported by rudolph
    }

    protected final void setDesktopProperty(String propName, Object value) {
        Object oldVal;
        lockAWT();
        try {
            oldVal = getDesktopProperty(propName);
            userPropSet.add(propName);
            desktopProperties.put(propName, value);
        } finally {
            unlockAWT();
        }
        desktopPropsSupport.firePropertyChange(propName, oldVal, value);
    }

    public void setDynamicLayout(boolean dynamic) throws HeadlessException {
        lockAWT();
        try {
            bDynamicLayoutSet = dynamic;
        } finally {
            unlockAWT();
        }
    }

    public void setLockingKeyState(int keyCode, boolean on) throws UnsupportedOperationException  {

        if (keyCode != KeyEvent.VK_CAPS_LOCK &&
            keyCode != KeyEvent.VK_NUM_LOCK &&
            keyCode != KeyEvent.VK_SCROLL_LOCK &&
            keyCode != KeyEvent.VK_KANA_LOCK) {
            throw new IllegalArgumentException();
        }

        keyLockingState.put(new Integer(keyCode), Boolean.valueOf(on));
    }

    public void addAWTEventListener(AWTEventListener listener, long eventMask) {
        // TODO
        throw new RuntimeException("not implemented");

        /*
        lockAWT();
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(awtEventsManager.permission);
            }
            awtEventsManager.addAWTEventListener(listener, eventMask);
        } finally {
            unlockAWT();
        }
        */
    }

    public void removeAWTEventListener(AWTEventListener listener) {
        // TODO
        throw new RuntimeException("not implemented");

        /*
        lockAWT();
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(awtEventsManager.permission);
            }
            awtEventsManager.removeAWTEventListener(listener);
        } finally {
            unlockAWT();
        }
        */
    }

    public AWTEventListener[] getAWTEventListeners() {
        // TODO
        throw new RuntimeException("not implemented");

        /*
        lockAWT();
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(awtEventsManager.permission);
            }
            return awtEventsManager.getAWTEventListeners();
        } finally {
            unlockAWT();
        }
        */
    }

    public AWTEventListener[] getAWTEventListeners(long eventMask) {
        // TODO
        throw new RuntimeException("not implemented");

        /*
        lockAWT();
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(awtEventsManager.permission);
            }
            return awtEventsManager.getAWTEventListeners(eventMask);
        } finally {
            unlockAWT();
        }
        */
    }

    void dispatchAWTEvent(AWTEvent event) {
        // TODO
        throw new RuntimeException("not implemented");

        /*
        awtEventsManager.dispatchAWTEvent(event);
        */
    }

}

