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

package com.acunia.wonka.test.awt.misc;

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;

public class FileDialogTest extends VisualTestImpl implements ActionListener {
  
  private Label file = new Label("File :");
  private Label dir = new Label("Directory :");
  private FileDialog fd;
  
  public FileDialogTest() {
    setLayout(new GridLayout(4, 1));
    
    Button load = new Button("Load");
    Button save = new Button("Save");
    add(load);
    add(save);
    add(file);
    add(dir);
    
    load.addActionListener(this);
    load.setActionCommand("load");
    save.addActionListener(this);
    save.setActionCommand("save");
  }

  public void actionPerformed(ActionEvent e) {
    int mode;
    if(e.getActionCommand().equals("load")) {
      mode = FileDialog.LOAD;
    }
    else {
      mode = FileDialog.SAVE;
    }
    if(fd == null) {
      fd = new FileDialog(vt.getFrame());
    }
   
    fd.setTitle("FileDialog Test");
    fd.setMode(mode);
    fd.show();
    file.setText("File : " + fd.getFile());
    dir.setText("Directory : " + fd.getDirectory());
  }

  public String getHelpText() {
    return "FileDialogTest";
  }

}

