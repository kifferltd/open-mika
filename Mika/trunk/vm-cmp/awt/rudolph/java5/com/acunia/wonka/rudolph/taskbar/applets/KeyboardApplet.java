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

package com.acunia.wonka.rudolph.taskbar.applets;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.Vector;
import java.net.*;
import java.util.Properties;
import java.util.StringTokenizer;
import com.acunia.wonka.rudolph.taskbar.*;
import com.acunia.wonka.rudolph.keyboard.*;

public class KeyboardApplet extends TaskBarApplet implements MouseListener {


  static int fontSize = 12;

  private class KeyboardItem {
    public String name;
    public Keyboard keyboard;

    public KeyboardItem(String name) {
      this.name = name;
    }
  }

  private class KeyboardSelect extends Window implements ItemListener {


    private List layoutList;

    public KeyboardSelect(Frame f) {
      super(f);

      layoutList = new List(keyboardItems.size());

      layoutList.setFont(new Font("Helvetica", Font.PLAIN , KeyboardApplet.fontSize));

      for(int i=0; i < keyboardItems.size(); i++) {
        layoutList.add(((KeyboardItem)keyboardItems.elementAt(i)).name);
      }

      layoutList.addItemListener(this);

      add(layoutList);
      pack();
    }

    public void itemStateChanged(ItemEvent e) {
      KeyboardItem keybItem;
      String item = (String)e.getItem();
      showSelection = false;
      selectMenu.setVisible(false);
      for(int i=0; i < keyboardItems.size(); i++) {
        keybItem = (KeyboardItem)keyboardItems.elementAt(i);
        if(item.equals(keybItem.name)) {
          keyboard = keybItem;
          layoutList.select(i);
          if(showKbd) {
            kbdWindow.setVisible(false);
            buildKeyboard();
            kbdWindow.setVisible(true);
          }
          else {
            buildKeyboard();
          }
        }
      }
      KeyboardApplet.this.repaint();
    }

  }

  private Image kbdIcon_e;
  private Image kbdIcon_d;
  private Image kbdSelect_e;
  private Image kbdSelect_d;
  boolean showKbd = false;
  boolean showSelection = false;
  private boolean selection = true;
  Window kbdWindow;
  KeyboardSelect selectMenu;
  Vector keyboardItems;
  KeyboardItem keyboard;
  private Font kbdFont;

  private int iconWidth = 20;
  private int iconHeight = 15;
  private int selectWidth = 10;

  private Image loadImage(String property) {
    Image result = null;
    String imgname = taskbar.getProperties().getProperty(property, null);
    if(imgname != null) {
      URL url = ClassLoader.getSystemResource(imgname);
      if(url != null) {
        result = Toolkit.getDefaultToolkit().getImage(url);
      }
    }
    return result;
  }

  public KeyboardApplet(TaskBar taskbar) {
    super(taskbar);

    kbdIcon_e = loadImage("applet.keyboard.icon.open");
    kbdIcon_d = loadImage("applet.keyboard.icon.close");
    kbdSelect_e = loadImage("applet.keyboard.select.open");
    kbdSelect_d = loadImage("applet.keyboard.select.close");

    keyboardItems = new Vector();
    StringTokenizer st = new StringTokenizer(taskbar.getProperties().getProperty("applet.keyboard.layouts", "Default"));
    while(st.hasMoreTokens()) {
      keyboardItems.add(new KeyboardItem(st.nextToken()));
    }

    keyboard = new KeyboardItem(taskbar.getProperties().getProperty("applet.keyboard.defaultlayout", "Default"));
    selection = Boolean.valueOf(taskbar.getProperties().getProperty("applet.keyboard.selectable", "false")).booleanValue();


    Properties props = taskbar.getProperties();
    String prop = props.getProperty("applet.keyboard.icon.width");
    if(prop != null){
      iconWidth = Integer.parseInt(prop);
    }
    prop = props.getProperty("applet.keyboard.icon.height");
    if(prop != null){
     iconHeight = Integer.parseInt(prop);
    }
    prop = props.getProperty("applet.keyboard.select.width");
    if(prop != null){
      selectWidth = Integer.parseInt(prop);
    }

    prop = props.getProperty("applet.keyboard.select.fontSize");
    if(prop != null){
      fontSize = Integer.parseInt(prop);
    }

    kbdFont = new Font(taskbar.getProperties().getProperty("applet.keyboard.font", "Helvetica"), Font.PLAIN,
                       Integer.parseInt(taskbar.getProperties().getProperty("applet.keyboard.fontsize", "8")));

    addMouseListener(this);
  }

  public void paint(Graphics g) {
    if(selection) {
      if(showSelection) {
        g.drawImage(kbdSelect_e, iconWidth, 0, null);
      }
      else {
        g.drawImage(kbdSelect_d, iconWidth, 0, null);
      }
    }
    if(showKbd) {
      g.drawImage(kbdIcon_e, 0, 0, null);
    }
    else {
      g.drawImage(kbdIcon_d, 0, 0, null);
    }
  }

  public Dimension getPreferredSize() {
    return new Dimension((selection ? iconWidth + selectWidth : iconWidth), iconHeight);
  }

  public Dimension getMinimumSize() {
     return getPreferredSize();
  }

  public Dimension getMaximumSize() {
     return getPreferredSize();
  }

  void buildKeyboard() {
    try {
      Class cl = Class.forName("com.acunia.wonka.rudolph.keyboard.Keyboard" + keyboard.name);
      Constructor con = cl.getConstructor(new Class[]{});
      keyboard.keyboard = (Keyboard)con.newInstance(new Object[]{});

      kbdWindow = new Window(taskbar);
      kbdWindow.setFont(kbdFont);
      kbdWindow.add(keyboard.keyboard);
      kbdWindow.addMouseListener(this);
      keyboard.keyboard.addMouseListener(this);

      /*
       ** Get the location of the keyboard window.
       */

      int top = 0;
      int bottom = Toolkit.getDefaultToolkit().getScreenSize().height;

      String loc = taskbar.getProperties().getProperty("applet.keyboard.location", "bottom");

      if(loc.equals("top")) {
        kbdWindow.setBounds(0, top, keyboard.keyboard.getPreferredSize().width, keyboard.keyboard.getPreferredSize().height);
      }
      else if(loc.equals("bottom")) {
        kbdWindow.setBounds(0, bottom - keyboard.keyboard.getPreferredSize().height,
                            keyboard.keyboard.getPreferredSize().width, keyboard.keyboard.getPreferredSize().height);
      }
      else if(loc.equals("taskbar")) {
        kbdWindow.setBounds(0, 0, keyboard.keyboard.getPreferredSize().width, keyboard.keyboard.getPreferredSize().height);
      }
      else if(loc.equals("custom")) {
        int x = Integer.parseInt(taskbar.getProperties().getProperty("applet.keyboard.x", "0"));
        int y = Integer.parseInt(taskbar.getProperties().getProperty("applet.keyboard.y", "0"));
        int w = Integer.parseInt(taskbar.getProperties().getProperty("applet.keyboard.width", "240"));
        int h = Integer.parseInt(taskbar.getProperties().getProperty("applet.keyboard.height", "81"));
        kbdWindow.setBounds(x, y, w, h);
      }

    } catch(Exception e) {
      System.err.println("-- Could not load keyboard layout: " + keyboard.name + " -->");
      e.printStackTrace();
      System.err.println("<--");
    }
  }

  public void mouseClicked(MouseEvent event) {
    if(event.getSource() == this) {
      if(event.getX() <= iconWidth) {

        /*
        ** The keyboard icon is pressed.
        */

        if(showKbd) {
          showKbd = false;
          kbdWindow.setVisible(false);
          keyboard.keyboard.close();
        }
        else {
          if(kbdWindow == null) {

            /*
            ** No keyboard window yet, build it.
            */

            buildKeyboard();
          }

          showKbd = true;
          keyboard.keyboard.open();
          kbdWindow.setVisible(true);
        }
      }
      else {

        /*
        ** The selection icon is pressed.
        */

        if(showSelection) {
          showSelection = false;
          selectMenu.setVisible(false);
        }
        else {
          if(selectMenu == null) {
            selectMenu = new KeyboardSelect(taskbar);
            int x = getLocationOnScreen().x + kbdIcon_e.getWidth(null);
            int y = getLocationOnScreen().y + kbdIcon_e.getHeight(null);
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            if(x + selectMenu.getSize().width > size.width) x = size.width - selectMenu.getSize().width;
            if(y + selectMenu.getSize().height > size.height) y -= selectMenu.getSize().height + kbdIcon_e.getHeight(null);
            selectMenu.setLocation(x, y);
          }

          showSelection = true;
          selectMenu.setVisible(true);
          selectMenu.toFront();
        }
      }
      repaint();
    }

    if(kbdWindow != null && showKbd) kbdWindow.setVisible(true);
    if(selectMenu != null && showSelection) selectMenu.setVisible(true);
  }
  
  public void mouseEntered(MouseEvent event) {
  }
  
  public void mouseExited(MouseEvent event) {
  }
  
  public void mousePressed(MouseEvent event) {
    if(kbdWindow != null && showKbd) kbdWindow.setVisible(true);
    if(selectMenu != null && showSelection) selectMenu.setVisible(true);
  }

  public void mouseReleased(MouseEvent event) {
    if(kbdWindow != null && showKbd) kbdWindow.setVisible(true);
    if(selectMenu != null && showSelection) selectMenu.setVisible(true);
  }
  
}

