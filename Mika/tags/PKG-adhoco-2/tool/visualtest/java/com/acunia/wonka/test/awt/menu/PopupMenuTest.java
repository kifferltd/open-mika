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

package com.acunia.wonka.test.awt.menu;

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;

public class PopupMenuTest extends VisualTestImpl implements MouseListener, ActionListener {

  private PopupMenu popup;
  private Label selected = new Label();

  public PopupMenuTest() {
    buildMenus();
    setLayout(new BorderLayout());
    add(selected, BorderLayout.NORTH);
  }

  public void addItem(Menu menu, MenuItem item) {
    menu.add(item);
    item.addActionListener(this);
  }
  
  public void buildMenus() {
    popup = new PopupMenu();
    Menu menu = new Menu("Testink");
    Menu menu2 = new Menu("Foobar");
    Menu menu3 = new Menu("More junk");
    Menu menu4 = new Menu("Recent");

    addItem(menu4, new MenuItem("Document_1.tex"));
    addItem(menu4, new MenuItem("Document_2.tex"));
    addItem(menu4, new MenuItem("my_darkest_secrets.tex"));
    
    addItem(menu, new MenuItem("Hello"));
    addItem(menu, new MenuItem("World"));
    
    addItem(menu2, new MenuItem("This"));
    addItem(menu2, new MenuItem("Is"));
    menu2.addSeparator();
    addItem(menu2, new MenuItem("A"));
    addItem(menu2, new MenuItem("Test"));
    
    addItem(menu3, new MenuItem("Open..."));
    menu3.add(menu4);
    addItem(menu3, new MenuItem("Save..."));
    addItem(menu3, new MenuItem("Close"));
    
    addItem(popup, new MenuItem("Hello"));
    addItem(popup, new MenuItem("World"));
    popup.addSeparator();
    addItem(popup, new MenuItem("People"));
    popup.addSeparator();
    popup.add(menu);
    popup.addSeparator();
    popup.add(menu2);
    popup.add(menu3);

    add(popup);

    addMouseListener(this);
  }

  public void actionPerformed(ActionEvent e) {
    selected.setText(e.getActionCommand());
  }

  public void mouseClicked(MouseEvent event) {
    popup.show(this, event.getX(), event.getY()); 
  }
  
  public void mouseEntered(MouseEvent event) {
  }
  
  public void mouseExited(MouseEvent event) {
  }
  
  public void mousePressed(MouseEvent event) {
  }
  
  public void mouseReleased(MouseEvent event) {
  }
  
  public String getHelpText() {
    return "PopupMenuTest";
  }
}

