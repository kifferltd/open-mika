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

package com.acunia.wonka.rudolph;

import java.awt.image.*;
import java.awt.peer.*;
import java.awt.dnd.peer.*;
import java.awt.dnd.*;
import java.awt.*;
import java.net.URL;
import java.io.*;
// import java.util.*;
import java.util.zip.*;
import com.acunia.wonka.rudolph.peers.*;

public class Toolkit extends java.awt.Toolkit {

  static {
    DefaultComponent.initRudolph();
  }

  protected static Toolkit t;

  protected ImageCache imageCache = new ImageCache();
  
  protected BeepImpl beepImpl = BeepFactory.getInstance();

  public static Toolkit getInstance() {
    if(t == null) {
      t = new Toolkit();
    }
    return t;
  }

  public Toolkit() {
  }

  public void beep() {
    if (beepImpl != null) {
      beepImpl.beep();
    }
    else {
      System.out.println("Help! I can't beep!" + (char)7);
    }	  
  }

  public ButtonPeer createButton(Button button) {
    return new DefaultButton(button);
  }

  public CanvasPeer createCanvas(Canvas canvas) {
    return new DefaultCanvas(canvas);
  }
  
  public CheckboxPeer createCheckbox(Checkbox checkbox) {
    return new DefaultCheckbox(checkbox);
  }
  
  public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem checkBoxMenuItem) {
    return new DefaultCheckboxMenuItem(checkBoxMenuItem);
  }
  
  public ChoicePeer createChoice(Choice choice) {
    return new DefaultChoice(choice);
  }
  
  public ComponentPeer createComponent(Component component) {
    if (component instanceof Container) {
      return new DefaultContainer((Container)component);
    }
    else {
      return new DefaultComponent(component);
    }
  }
  
  public DialogPeer createDialog(Dialog dialog) {
    return new DefaultDialog(dialog);
  }
  
  public FileDialogPeer createFileDialog(FileDialog fileDialog) {
    return new DefaultFileDialog(fileDialog);
  }
  
  public FramePeer createFrame(Frame frame) {
    return new DefaultFrame(frame);
  }
  
  public LabelPeer createLabel(Label label) {
    return new DefaultLabel(label);
  }
  
  public ListPeer createList(List list) {
    return new DefaultList(list);
  }
  
  public MenuPeer createMenu(Menu menu) {
    return new DefaultMenu(menu);
  }
  
  public MenuBarPeer createMenuBar(MenuBar menuBar) {
    return new DefaultMenuBar(menuBar);
  }
  
  public MenuItemPeer createMenuItem(MenuItem menuItem) {
    return new DefaultMenuItem(menuItem);
  }
  
  public PanelPeer createPanel(Panel panel) {
    return new DefaultPanel(panel);
  }
  
  public PopupMenuPeer createPopupMenu(PopupMenu popupMenu) {
    return new DefaultPopupMenu(popupMenu);
  }
  
  public ScrollbarPeer createScrollbar(Scrollbar scrollbar) {
    return new DefaultScrollbar(scrollbar);
  }
  
  public ScrollPanePeer createScrollPane(ScrollPane scrollPane) {
    return new DefaultScrollPane(scrollPane);
  }
  
  public TextAreaPeer createTextArea(TextArea textArea) {
    return new DefaultTextArea(textArea);
  }
  
  public TextFieldPeer createTextField(TextField textField) {
    return new DefaultTextField(textField);
  }
 
  public WindowPeer createWindow(Window window) {
    return new DefaultWindow(window);
  }

  /**
   * @status  not implemented
   * @remark  not implemented
   */ 
  public int checkImage(java.awt.Image image, int w, int h, ImageObserver observer) {
    /*
    if(w != -1 || h != -1) {
      System.out.println("[com.acunia.wonka.rudolph.Toolkit.checkImage()] Rescaling image not supported");
      return 0;
    }
    */
    return ((Image)image).flags;
  }

  /**
   * @status  implemented
   * @remark  implemented
   */
  public java.awt.Image createImage(ImageProducer producer) {
    com.acunia.wonka.rudolph.Image image = new com.acunia.wonka.rudolph.Image(producer);
    return (java.awt.Image)image;
  }

  /**
   * @status  implemented
   * @remark  only works for Portable Network Graphics (PNG) images and GIF, not for JPEG.
   */ 
  public synchronized java.awt.Image createImage(byte[] imageData) {

    if(imageData.length < 5) {
      return null;
    }
    
    CRC32 crc = new CRC32();
    com.acunia.wonka.rudolph.Image image = null;
    
    crc.update(imageData);
    image = (com.acunia.wonka.rudolph.Image)imageCache.get((int)crc.getValue());

    if(image == null) {

      /*
      ** Image is not yet in the cache.
      */

      ImageProducer source = null;
      boolean cache = false;
      
      if(imageData[1] == 'P' && imageData[2] == 'N' && imageData[3] == 'G') {
        source = new PNGImageSource(imageData);
        cache = true;
      }
      else if(imageData[0] == 'G' && imageData[1] == 'I' && imageData[2] == 'F') {
        source = new GIFImageSource((byte[])imageData.clone());
        // cache = (((GIFImageSource)source).isAnimated() ? false : true);
        cache = true;
      }
      else if(imageData[0] == (byte)0xFF && imageData[1] == (byte)0xD8 && imageData[2] == (byte)0xFF) {
        source = new JPEGImageSource((byte[])imageData.clone());
        cache = true;
      }
        
      if(source != null) {
        image = new com.acunia.wonka.rudolph.Image(source);

        if(image != null && cache) {
          imageCache.put((int)crc.getValue(), image);
          image.setKey((int)crc.getValue());
        }
      }
    }
    
    return (java.awt.Image)image;
  }

  /**
   * @status  implemented
   * @remark  implemented
   */ 
  public java.awt.Image createImage(byte[] imageData, int offset, int count) {
    byte[] data = new byte[count];
    for(int i=0; i < count; i++) {
      data[i] = imageData[offset + i];
    }
    return createImage(data);
  }

  public java.awt.Image createImage(java.net.URL url) {
    if (url == null) {
      return new BrokenImage();
    }
    try {
      int len = url.openConnection().getContentLength();
      if(len > -1){
        byte[] bytes = new byte[len];
        InputStream in = url.openStream();
        int rd = in.read(bytes,0,len);
        while(rd < len){
          int b = in.read(bytes,rd,len -rd);
          if(b == -1){
            byte[] old = bytes;
            bytes = new byte[rd];
            System.arraycopy(old,0,bytes,0,rd);
          }
          rd += b;
        }
        return createImage(bytes);
      }
      ByteArrayOutputStream bas = new ByteArrayOutputStream(2048);  
      InputStream in = url.openStream();
      byte[] bytes = new byte[1024];
      len = in.read(bytes, 0, 1024);
      while (len != -1) {
        bas.write(bytes,0,len);
        len = in.read(bytes, 0, 1024);
      }
      return createImage(bas.toByteArray());
    }  
    catch (IOException e) {
      return null;
    }
  }

  public java.awt.Image createImage(String filename) {
    if (filename == null) {
      return null;
    }
    ByteArrayOutputStream bas = new ByteArrayOutputStream(2048);  
    try {
      InputStream in = new FileInputStream(filename);
      byte[] bytes = new byte[1024];
      int len = in.read(bytes, 0, 1024);
      while (len != -1) {
        bas.write(bytes,0,len);
        len = in.read(bytes, 0, 1024);
      }
    }  
    catch (IOException e) {
      return null;
    }
    return createImage(bas.toByteArray());
  }

  public java.awt.Image getImage(URL url) {
    return createImage(url);
  }
  
  /**
   * @status  implemented
   * @remark  implemented
   */
  public ColorModel getColorModel() {
    return new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000);
  }
  
  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public String[] getFontList() {
    return null;
  }
  
  /**
   * @status  not implemented
   * @remark  not implemented
   */
  public FontMetrics getFontMetrics(Font font) {
    return new FontMetrics((font == null) ? Component.DEFAULT_FONT : font);
  }

  /**
   * @status  partly implemented
   * @remark  only works for Portable Network Graphics (PNG) images and not for JPEG or GIF.
   */  
  public java.awt.Image getImage(String filename) {
    try {
      RandomAccessFile raf = new RandomAccessFile(filename, "r");
      byte[] ba = new byte[(int)raf.length()];
      raf.readFully(ba);
      raf.close();
      return Toolkit.getDefaultToolkit().createImage(ba);
    }
    catch (IOException e) {
      return null;       
    }
  }

  /**
   * @status  not implemented
   * @remark  not implemented
   */  
  public int getMenuShortcutKeyMask() {
    return 0;
  }

  /**
   * @status  implemented
   * @remark  always returns 72 dpi.
   */
  public int getScreenResolution() {
    return 72;
  }
  
  private native int getScreenWidth();
  private native int getScreenHeight();

  /**
   * @status  implemented
   * @remark  implemented
   */
  public Dimension getScreenSize() {
    return new Dimension(getScreenWidth(), getScreenHeight());
  }
  
  /**
   * @status  not implemented
   * @remark  not implemented
   */  
  protected EventQueue getSystemEventQueueImpl() {
    return null;
  }

  /**
   * @status  implemented
   * @remark  not yet according to specs. The observer isn't used yet.
   */  
  public boolean prepareImage(java.awt.Image image, int w, int h, ImageObserver observer) {
    Image im = (com.acunia.wonka.rudolph.Image)image;  
    im.getSource().startProduction(im.consumer);
    /*
    if(w != -1 || h != -1) {
      com.acunia.wonka.rudolph.Image img = (com.acunia.wonka.rudolph.Image)image;
      System.out.println("[com.acunia.wonka.rudolph.Toolkit.prepareImage()] Rescaling image not supported " + w + " " + h + " (" + img.width + " " + img.height + ")");
      return false;
    }
    */
    return true;
  }

  public native void sync();
  
  public final native EventQueue getSystemEventQueue();
  
  public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException{
    com.acunia.wonka.rudolph.peers.DefaultDragSourceContextPeer ddcp = new DefaultDragSourceContextPeer(dge);
    return ((DragSourceContextPeer) ddcp);
  }

}

