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

import java.awt.peer.*;
import java.io.*;

public class FileDialog extends Dialog {

  private static final long serialVersionUID = 5035145889651310422L;
  
  public static final int LOAD = 0;
  public static final int SAVE = 1;

  private int mode;
  private FilenameFilter filter;
  private String dir = "";
  private String file;

  private int width = 250;
  private int height = 300;

  public FileDialog(Frame owner) {
    this(owner, "");
  }
  
  public FileDialog(Frame owner, String title) {
    this(owner, title, LOAD);
  }
  
  public FileDialog(Frame owner, String title, int mode) {
    super(owner, title, true);
    this.mode = mode;
    
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    width = Math.min(width, screen.width);
    height = Math.min(height, screen.height);
    int x = (screen.width - width) / 2;
    int y = (screen.height - height) / 2;
    setBounds((x > 0 ? x : 0), (y > 0 ? y : 0), width, height);
   
    try {
      dir = (new File("")).getCanonicalPath();
    }
    catch(Exception e) {
    }
    ((FileDialogPeer)peer).setDirectory(dir);
    ((FileDialogPeer)peer).setFile(file);
  }

  public void addNotify() {
    if(peer == null) {
      peer = getToolkit().createFileDialog(this);
    }

    if(notified == false) {
      super.addNotify();
    }
  }

  public String getDirectory() {
    return dir + "/";
  }
  
  public synchronized void setDirectory(String dir) {
    this.dir = dir;
    ((FileDialogPeer)peer).setDirectory(dir);
  }

  public String getFile() {
    return file;
  }

  public synchronized void setFile(String file) {
    this.file = file;
    ((FileDialogPeer)peer).setFile(file);
  }
  
  public FilenameFilter getFilenameFilter() {
    return filter;
  }
  
  public synchronized void setFilenameFilter(FilenameFilter filter) {
    this.filter = filter;
  }

  public int getMode() {
    return mode;
  }

  public void setMode(int mode) {
    if(mode != LOAD && mode != SAVE) {
      throw new IllegalArgumentException("Only LOAD and SAVE are allowed");
    }
    this.mode = mode;
  }

  protected String paramString() {
    return "";
  }

}

