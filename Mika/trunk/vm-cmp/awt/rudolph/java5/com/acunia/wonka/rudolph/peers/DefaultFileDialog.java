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

package com.acunia.wonka.rudolph.peers;

import java.awt.peer.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Arrays;

public class DefaultFileDialog extends DefaultDialog implements FileDialogPeer {

  TextField pathtext;
  TextField filetext;
  TextField filtertext;
  List folderlist;
  private List fileslist;
  private Button okbutton;
  private Button cancelbutton;
  private EventHandler eventHandler = new EventHandler();
  
  FilenameFilter filter;
  String dir = "";
  String file;

  private boolean gui = false;
  boolean loopback = false;

  public DefaultFileDialog(FileDialog fileDialog) {
    super(fileDialog);
  }

  public void setDirectory(String dir) {
    if(loopback) return;
    this.dir = dir;
  }
  
  public void setFile(String file) {
    if(loopback) return;
    this.file = file;
  }
  
  public void setFilenameFilter(FilenameFilter filter) {
    this.filter = filter;
  }

  public void setVisible(boolean visible) {
    if(visible && !gui) {
      buildDialog();
      updateLists();
    }
    super.setVisible(visible);
  }

  private void buildDialog() {
 
    gui = true;

    InsetsPanel mainpanel = new InsetsPanel(0, 4, 0, 4);
    mainpanel.setLayout(new BorderLayout(1, 1));

    Panel pathpanel = new Panel();
    pathpanel.setLayout(new BorderLayout());
    pathtext = new TextField(dir);
    
    pathtext.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        dir = pathtext.getText();
        updateLists();
      }
    });
    
    Label pathlabel = new Label("Enter path or folder name:");

    pathpanel.add(pathlabel, BorderLayout.NORTH);
    pathpanel.add(pathtext, BorderLayout.CENTER);
    
    Panel filepanel = new Panel();
    filepanel.setLayout(new BorderLayout());
    filetext = new TextField();
    
    filetext.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        file = pathtext.getText();
      }
    });
    
    Label filelabel = new Label("Enter file name:");

    filepanel.add(filelabel, BorderLayout.NORTH);
    filepanel.add(filetext, BorderLayout.CENTER);

    Panel filterpanel = new Panel();
    filterpanel.setLayout(new BorderLayout());
    Label filterlabel = new Label("Filter");
    filtertext = new TextField();
    
    filtertext.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        if(filtertext.getText() != "") {
          filter = new Filter(filtertext.getText());
        }
        else {
          filter = null;
        }
        updateLists();
      }
    });

    filterpanel.add(filterlabel, BorderLayout.NORTH);
    filterpanel.add(filtertext, BorderLayout.CENTER);

    Panel bottompanel = new Panel();
    bottompanel.setLayout(new BorderLayout());

    bottompanel.add(filepanel, BorderLayout.CENTER);
    bottompanel.add(filterpanel, BorderLayout.EAST);
    
    Panel selectpanel = new Panel();
    selectpanel.setLayout(new GridLayout(1, 2));

    InsetsPanel selpathpanel = new InsetsPanel(0, 0, 0, 2);
    selpathpanel.setLayout(new BorderLayout());

    Label folderlabel = new Label("Folders");
    folderlist = new List();
    folderlist.addItemListener(eventHandler);

    selpathpanel.add(folderlabel, BorderLayout.NORTH);
    selpathpanel.add(folderlist, BorderLayout.CENTER);
    
    InsetsPanel selfilepanel = new InsetsPanel(0, 2, 0, 0);
    selfilepanel.setLayout(new BorderLayout());
    Label fileslabel = new Label("Files");
    fileslist = new List();
    fileslist.addItemListener(eventHandler);
    
    selfilepanel.add(fileslabel, BorderLayout.NORTH);
    selfilepanel.add(fileslist, BorderLayout.CENTER);

    selectpanel.add(selpathpanel);
    selectpanel.add(selfilepanel);
    
    mainpanel.add(pathpanel, BorderLayout.NORTH);
    mainpanel.add(selectpanel, BorderLayout.CENTER);
    mainpanel.add(bottompanel, BorderLayout.SOUTH);
    
    InsetsPanel buttonpanel = new InsetsPanel(2, 2, 2, 2);
    buttonpanel.setLayout(new GridLayout(1, 2, 2, 2));
    okbutton = new Button(((FileDialog)component).getMode() == FileDialog.LOAD ? "Load" : "Save");
    cancelbutton = new Button("Cancel");
    okbutton.addActionListener(eventHandler); 
    okbutton.setActionCommand("ok"); 
    cancelbutton.addActionListener(eventHandler); 
    cancelbutton.setActionCommand("cancel"); 

    buttonpanel.add(okbutton);
    buttonpanel.add(cancelbutton);

    ((Dialog)component).add(mainpanel, BorderLayout.CENTER);
    ((Dialog)component).add(buttonpanel, BorderLayout.SOUTH);
  }

  void updateLists() {
    folderlist.removeAll();
    folderlist.add("..");
    fileslist.removeAll();
    File currentdir = new File(dir);
    String list[] = currentdir.list();
    Arrays.sort(list);
    for(int i=0; i<list.length; i++) {
      File current = new File(dir + "/" + list[i]);
      if(current.isFile()) {
        if((filter != null && filter.accept(currentdir, list[i])) || filter == null) {
          fileslist.add(list[i]);
        }
      }
      else {
        folderlist.add(list[i]);
      }
    }
  }

  /*
  ** A small and simple class to handle all the events in the
  ** FileDialog.
  */

  private class EventHandler implements ItemListener, ActionListener {
    
    public synchronized void itemStateChanged(ItemEvent event) {
      if(event.getItemSelectable() == folderlist) {
        File f = new File(dir += "/" + (String)event.getItem());
        try {
          dir = f.getCanonicalPath();
        }
        catch(Exception e) {
        }
        loopback = true;
        ((FileDialog)component).setDirectory(dir);
        loopback = false;
        pathtext.setText(dir);
        updateLists();
      }
      else {
        file = (String)event.getItem();
        filetext.setText(file);
        loopback = true;
        ((FileDialog)component).setFile(file);
        loopback = false;
      }
    }
    
    public synchronized void actionPerformed(ActionEvent e) {
      if(e.getActionCommand().equals("cancel")) {
        file = null;
      }
      if(file == "") file = null;
      component.setVisible(false);
      loopback = true;
      ((FileDialog)component).setFile(file);
      loopback = false;
    }
  }

  /*
  ** A Panel with specified insets.. This will be removed once
  ** the whole layout is done over with a GridbagLayout.
  */
  
  private class InsetsPanel extends Panel {
    
    private Insets insets;
    
    public InsetsPanel(int x1, int y1, int x2, int y2) {
      insets = new Insets(x1, y1, x2, y2);
    }
    
    public Insets getInsets() {
      return insets;
    }
  }

  /*
  ** A simple FilenameFilter.
  */

  private class Filter implements FilenameFilter {

    private String tokens[];
    
    public Filter(String filter) {
      StringTokenizer stok = new StringTokenizer(filter, "*?", true);
      tokens = new String[stok.countTokens()];
      int i = 0;
      while(stok.hasMoreTokens()) {
        tokens[i++] = stok.nextToken();
      }
    }

    public boolean accept(File dir, String name) {
      return match(name, 0);
    }
    
    public boolean match(String name, int level) {
      if (level == tokens.length) {

        /*
        ** There's no more stuff left. 
        */
        
        return true;
      }

      if(tokens[level].equals("?")) {

        /*
        ** Skip a character and this token and continue.
        */
        
        return match(name.substring(1), level + 1);
      }
      else if(tokens[level].equals("*")) {

        /*
        ** Skip a lot.
        */
        
        if(level == tokens.length - 1) {

          /*
          ** This is the last token, so there's 
          ** a match.
          */

          return true;
        }
          
        boolean result = false;
        int name_length = name.length();
        for(int i=0; i < name_length && !result; i++) {
          result = match(name.substring(i), level + 1);
        }
        
        return result;
      }
      else {

        /*
        ** Check if this token matches.
        */
        
        if(name.startsWith(tokens[level])) {

          /*
          ** There's a match, continue with the remaining.
          */
          
          return match(name.substring(tokens[level].length()), level + 1);
        }
        else {

          /*
          ** No match.
          */
          
          return false;
        }
      }
    }
  }
}

