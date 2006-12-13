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

import java.awt.datatransfer.*;
import java.awt.dnd.peer.*;
import java.awt.dnd.*;
import java.awt.image.*;
import java.awt.peer.*;
import java.awt.event.*;
import java.net.URL;

public abstract class Toolkit extends Object {

  static Clipboard clipsystem;

  public abstract void beep();

  public abstract int checkImage(Image image, int w, int h, ImageObserver observer);

  public abstract ButtonPeer createButton(Button button);

  public abstract CanvasPeer createCanvas(Canvas canvas);
  
  public abstract CheckboxPeer createCheckbox(Checkbox checkbox);
  
  public abstract CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem checkBoxMenuItem);
  
  public abstract ChoicePeer createChoice(Choice choice);
  
  public abstract ComponentPeer createComponent(Component component);
  
  public abstract DialogPeer createDialog(Dialog dialog);
  
  public abstract FileDialogPeer createFileDialog(FileDialog fileDialog);
  
  public abstract FramePeer createFrame(Frame frame);
  
  public abstract LabelPeer createLabel(Label label);
  
  public abstract ListPeer createList(List list);
  
  public abstract MenuPeer createMenu(Menu menu);
  
  public abstract MenuBarPeer createMenuBar(MenuBar menuBar);
  
  public abstract MenuItemPeer createMenuItem(MenuItem menuItem);
  
  public abstract PanelPeer createPanel(Panel panel);
  
  public abstract PopupMenuPeer createPopupMenu(PopupMenu popupMenu);
  
  public abstract ScrollbarPeer createScrollbar(Scrollbar scrollbar);
  
  public abstract ScrollPanePeer createScrollPane(ScrollPane scrollPane);
  
  public abstract TextAreaPeer createTextArea(TextArea textArea);
  
  public abstract TextFieldPeer createTextField(TextField textField);
 
  public abstract WindowPeer createWindow(Window window);
  
  public abstract Image createImage(ImageProducer producer);

  public abstract Image createImage(String filename);

  public abstract Image createImage(byte[] imageData);

  public abstract Image createImage(byte[] imageData, int offset, int count);

  public abstract Image createImage(java.net.URL url);

  public abstract ColorModel getColorModel();
  
  public static synchronized Toolkit getDefaultToolkit() {
    return (java.awt.Toolkit)com.acunia.wonka.rudolph.Toolkit.getInstance();
  }

  public abstract String[] getFontList();
  
  public abstract FontMetrics getFontMetrics(Font font);

  public abstract Image getImage(String filename);

  public abstract Image getImage(URL url);
  
  public abstract int getMenuShortcutKeyMask();

  protected static Container getNativeContainer(Component component) {
    return null;
  }
  
  //public abstract PrintJob getPrintJob(Frame frame, String title, Properties properties);

  public static String getProperty(String key, String defaultValue) {
    return defaultValue;
  }
  
  public abstract int getScreenResolution();
  
  public abstract Dimension getScreenSize();
  
  public Clipboard getSystemClipboard(){
    if(clipsystem==null) clipsystem = new Clipboard("systemClipboard");
    return clipsystem;
  }

  public EventQueue getSystemEventQueue() {
    return null;
  }

  protected abstract EventQueue getSystemEventQueueImpl();

  protected void loadSystemColors(int[] systemColors) {
  }

  public abstract boolean prepareImage(Image image, int w, int h, ImageObserver observer);

  public abstract void sync();
 
  /*
  ** 1.2 methods
  */
   
  public java.awt.Cursor createCustomCursor(Image cursor, Point hotSpot, String name) {
    com.acunia.wonka.rudolph.CustomCursor customCursor = new com.acunia.wonka.rudolph.CustomCursor(cursor, hotSpot, name);
    return (java.awt.Cursor) customCursor;
  }

  public void addAWTEventListener(AWTEventListener listener, long eventMask) {
  }
  
  public void removeAWTEventListener(AWTEventListener listener, long eventMask) {
  }

  public AWTEventListener[] getAWTEventListeners() {
    return new AWTEventListener[0];
  }

  public AWTEventListener[] getAWTEventListeners(long eventMask) {
    return new AWTEventListener[0];
  }

  public abstract DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException;

  public DragGestureRecognizer createDragGestureRecognizer(Class abstractRecognizerClass, DragSource ds,
                                                         Component c, int srcActions, DragGestureListener dgl){
    if(java.awt.dnd.MouseDragGestureRecognizer.class.isAssignableFrom(abstractRecognizerClass)){
      com.acunia.wonka.rudolph.MouseDragGestureRecognizer mgr = new com.acunia.wonka.rudolph.MouseDragGestureRecognizer(ds,c,srcActions, dgl);
      return ((java.awt.dnd.MouseDragGestureRecognizer) mgr);
    }
    com.acunia.wonka.rudolph.DragGestureRecognizer dgr = new com.acunia.wonka.rudolph.DragGestureRecognizer(ds,c,srcActions, dgl);
    return ((java.awt.dnd.DragGestureRecognizer) dgr);
  }

}

