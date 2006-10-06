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

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;
import com.acunia.wonka.test.awt.VisualTester;

public class MenuBarTest extends VisualTestImpl implements ActionListener {

  private MenuBar menubar;
  private Label selected = new Label();

  VisualTester getVt() {
    return vt;
  }
  
  public MenuBarTest() {
//    buildMenus();
    setLayout(new GridLayout(3,1));
    add(selected);
    
    Button act = new Button("Set menubar");
    Button deact = new Button("Delete menubar");

    act.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buildMenus();
      }
    });
    
    deact.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getVt().getFrame().setMenuBar(null);
      }
    });
  
    add(act);
    add(deact);
  }

  public void addItem(Menu menu, MenuItem item) {
    menu.add(item);
    item.addActionListener(this);
  }
  
  public void buildMenus() {
    menubar = new MenuBar();
    Menu menu = new Menu("File");
    Menu menu2 = new Menu("Edit");
    Menu menu3 = new Menu("Tools");
    Menu menu4 = new Menu("Recent");

    addItem(menu4, new MenuItem("Document_1.tex"));
    addItem(menu4, new MenuItem("Document_2.tex"));
    addItem(menu4, new MenuItem("my_darkest_secrets.tex"));
    
    addItem(menu, new MenuItem("Open..."));
    menu.add(menu4);
    addItem(menu, new MenuItem("New"));
    addItem(menu, new MenuItem("Close"));
    menu.addSeparator();
    addItem(menu, new MenuItem("Save"));
    addItem(menu, new MenuItem("Save as..."));
    menu.addSeparator();
    addItem(menu, new MenuItem("Print"));
    menu.addSeparator();
    addItem(menu, new MenuItem("Exit"));
    
    addItem(menu2, new MenuItem("Undo"));
    addItem(menu2, new MenuItem("Redo"));
    menu2.addSeparator();
    addItem(menu2, new MenuItem("Cut"));
    addItem(menu2, new MenuItem("Copy"));
    addItem(menu2, new MenuItem("Paste"));
    menu2.addSeparator();
    addItem(menu2, new MenuItem("Select All"));
    
    addItem(menu3, new MenuItem("Hammer"));
    addItem(menu3, new MenuItem("Saw"));
    addItem(menu3, new MenuItem("Screwdriver"));
   
    menubar.add(menu);
    menubar.add(menu2);
    menubar.add(menu3);

    vt.getFrame().setMenuBar(menubar);
  }

  public void actionPerformed(ActionEvent e) {
    selected.setText(e.getActionCommand());
  }

  public String getHelpText() {
    return "MenuBarTest";
  }
}

