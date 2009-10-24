/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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

