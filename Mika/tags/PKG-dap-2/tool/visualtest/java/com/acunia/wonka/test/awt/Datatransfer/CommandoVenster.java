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
package com.acunia.wonka.test.awt.Datatransfer;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CommandoVenster extends Panel{
  MyButtonListener buttonListener;
  Button clipboardButton;
  Button clipboardButton1;
  Button dataFlavorButton;
  Button dataFlavorButton2;
  Button dataFlavorButton3;
  Button dataFlavorButton4;
  Button dataFlavorButton5;
  Button stringSelectionButton;
  Button systemFlavorMapButton;
  Button button1;
  Button button2;
  Button button3;
  Button button4;
  Button button5;
  Button button6;
  Button button7;
  Button button8;
  Button button9;
  Button button10;
  Button button11;
  Button button12;
  Button button13;
  Button button14;
  Button button15;
  Clipboard clipboard;
  DataFlavor dataFlavor1;
  DataFlavor dataFlavor2;
  DataFlavor dataFlavor3;
  FileOutputStream ostream;
  ObjectOutputStream os;
  FileInputStream istream;
  ObjectInputStream is;
  Panel knoppenPanel;
  Panel overzichtPanel;
  StringSelection owner1;
  StringSelection owner2;
  StringSelection owner3;
  StringSelection stringSelection1;
  StringSelection stringSelection2;
  SystemFlavorMap flavormap = (SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap();
  TextArea tekstScherm;


  public CommandoVenster(){
    super(new GridLayout(1,2));
    knoppenPanel = new Panel();
    add(knoppenPanel);
    overzichtPanel = new Panel();
    overzichtPanel.setLayout(new GridLayout(2,1));
    add(overzichtPanel);
    buttonListener = new MyButtonListener();
    opbouwKnoppen("clipboard");
    opbouwOverzicht();
    clipboard = new Clipboard("clipbord");
    owner1 = new StringSelection("1");
    owner2 = new StringSelection("2");
    owner3 = new StringSelection("3");
    opbouwKnoppen("Clipboard");
    setVisible(true);
    try{
      ostream = new FileOutputStream("t.txt");
      os = new ObjectOutputStream(ostream);
      istream = new FileInputStream("t.txt");
      is = new ObjectInputStream(istream);
    }catch(FileNotFoundException fnfe){System.out.println(fnfe.getMessage());}
    catch(IOException ioe){System.out.println(ioe.getMessage());}
  }

  private void opbouwOverzicht(){
    tekstScherm = new TextArea();
    overzichtPanel.add(tekstScherm);
    Panel keuzeKlasse = new Panel(new GridLayout(5,2));
    overzichtPanel.add(keuzeKlasse);

    clipboardButton = new Button("Clipboard");
    clipboardButton.addActionListener(buttonListener);
    clipboardButton.setActionCommand("Clipboard");
    keuzeKlasse.add(clipboardButton);
    
    clipboardButton1 = new Button("Clipboard vb");
    clipboardButton1.addActionListener(buttonListener);
    clipboardButton1.setActionCommand("Clipboard vb 1");
    keuzeKlasse.add(clipboardButton1);

    dataFlavorButton = new Button("DataFlavor 1");
    dataFlavorButton.addActionListener(buttonListener);
    dataFlavorButton.setActionCommand("DataFlavor 1");
    keuzeKlasse.add(dataFlavorButton);

    dataFlavorButton2 = new Button("DataFlavor 2");
    dataFlavorButton2.addActionListener(buttonListener);
    dataFlavorButton2.setActionCommand("DataFlavor 2");
    keuzeKlasse.add(dataFlavorButton2);

    dataFlavorButton3 = new Button("DataFlavor 3");
    dataFlavorButton3.addActionListener(buttonListener);
    dataFlavorButton3.setActionCommand("DataFlavor 3");
    keuzeKlasse.add(dataFlavorButton3);

    dataFlavorButton4 = new Button("DataFlavor 4");
    dataFlavorButton4.addActionListener(buttonListener);
    dataFlavorButton4.setActionCommand("DataFlavor 4");
    keuzeKlasse.add(dataFlavorButton4);

    dataFlavorButton5 = new Button("DataFlavor 5");
    dataFlavorButton5.addActionListener(buttonListener);
    dataFlavorButton5.setActionCommand("DataFlavor 5");
    keuzeKlasse.add(dataFlavorButton5);

    stringSelectionButton = new Button("StringSelection");
    stringSelectionButton.addActionListener(buttonListener);
    stringSelectionButton.setActionCommand("StringSelection");
    keuzeKlasse.add(stringSelectionButton);

    systemFlavorMapButton = new Button("SystemFlavorMap");
    systemFlavorMapButton.addActionListener(buttonListener);
    systemFlavorMapButton.setActionCommand("SystemFlavorMap");
    keuzeKlasse.add(systemFlavorMapButton);

  }

  void opbouwKnoppen(String keuze){
    knoppenPanel.setVisible(false);
    knoppenPanel.removeAll();
    if(keuze=="Clipboard"){
      knoppenPanel.setLayout(new GridLayout(5,1));

      button1 = new Button("getName");
      button1.addActionListener(buttonListener);
      button1.setActionCommand("getName");
      knoppenPanel.add(button1);

      button2 = new Button("getContents");
      button2.addActionListener(buttonListener);
      button2.setActionCommand("getContents");
      knoppenPanel.add(button2);

      button3 = new Button("setContents");
      button3.addActionListener(buttonListener);
      button3.setActionCommand("setContents");
      knoppenPanel.add(button3);

      button4 = new Button("setContents2");
      button4.addActionListener(buttonListener);
      button4.setActionCommand("setContents2");
      knoppenPanel.add(button4);

      button5 = new Button("setContents3");
      button5.addActionListener(buttonListener);
      button5.setActionCommand("setContents3");
      knoppenPanel.add(button5);
    }
    if(keuze=="DataFlavor 1"){
      knoppenPanel.setLayout(new GridLayout(12,1));

      button1 = new Button("DataFlavor(String,String) 1");
      button1.addActionListener(buttonListener);
      button1.setActionCommand("DataFlavor(String,String) 1");
      knoppenPanel.add(button1);

      button2 = new Button("DataFlavor(String,String) 2");
      button2.addActionListener(buttonListener);
      button2.setActionCommand("DataFlavor(String,String) 2");
      knoppenPanel.add(button2);

      button3 = new Button("DataFlavor(Class,String) 2");
      button3.addActionListener(buttonListener);
      button3.setActionCommand("DataFlavor(Class,String) 2");
      knoppenPanel.add(button3);

      button4 = new Button("DataFlavor(String2,String) 2");
      button4.addActionListener(buttonListener);
      button4.setActionCommand("DataFlavor(String2,String) 2");
      knoppenPanel.add(button4);

      button5 = new Button("DataFlavor(Class,String) 3");
      button5.addActionListener(buttonListener);
      button5.setActionCommand("DataFlavor(Class,String) 3");
      knoppenPanel.add(button5);

      button6 = new Button("2=clone(1)");
      button6.addActionListener(buttonListener);
      button6.setActionCommand("2=clone(1)");
      knoppenPanel.add(button6);

      button7 = new Button("getMimeType 1");
      button7.addActionListener(buttonListener);
      button7.setActionCommand("getMimeType 1");
      knoppenPanel.add(button7);

      button8 = new Button("getMimeType 2");
      button8.addActionListener(buttonListener);
      button8.setActionCommand("getMimeType 2");
      knoppenPanel.add(button8);

      button9 = new Button("getMimeType 3");
      button9.addActionListener(buttonListener);
      button9.setActionCommand("getMimeType 3");
      knoppenPanel.add(button9);

      button10 = new Button("getRepresentationClass 1");
      button10.addActionListener(buttonListener);
      button10.setActionCommand("getRepresentationClass 1");
      knoppenPanel.add(button10);

      button11 = new Button("getRepresentationClass 2");
      button11.addActionListener(buttonListener);
      button11.setActionCommand("getRepresentationClass 2");
      knoppenPanel.add(button11);

      button12 = new Button("getRepresentationClass 3");
      button12.addActionListener(buttonListener);
      button12.setActionCommand("getRepresentationClass 3");
      knoppenPanel.add(button12);
    }
    if(keuze=="DataFlavor 2"){
      knoppenPanel.setLayout(new GridLayout(12,1));

      button1 = new Button("getHumanPresentableName 1");
      button1.addActionListener(buttonListener);
      button1.setActionCommand("getHumanPresentableName 1");
      knoppenPanel.add(button1);

      button2 = new Button("getHumanPresentableName 2");
      button2.addActionListener(buttonListener);
      button2.setActionCommand("getHumanPresentableName 2");
      knoppenPanel.add(button2);

      button3 = new Button("getHumanPresentableName 3");
      button3.addActionListener(buttonListener);
      button3.setActionCommand("getHumanPresentableName 3");
      knoppenPanel.add(button3);

      button4 = new Button("setHumanPresentableName('test1') 1");
      button4.addActionListener(buttonListener);
      button4.setActionCommand("setHumanPresentableName('test1') 1");
      knoppenPanel.add(button4);

      button5 = new Button("setHumanPresentableName('test2') 1");
      button5.addActionListener(buttonListener);
      button5.setActionCommand("setHumanPresentableName('test2') 1");
      knoppenPanel.add(button5);

      button6 = new Button("setHumanPresentableName('test2') 2");
      button6.addActionListener(buttonListener);
      button6.setActionCommand("setHumanPresentableName('test2') 2");
      knoppenPanel.add(button6);

      button7 = new Button("equals(DataFlavor) 1&2");
      button7.addActionListener(buttonListener);
      button7.setActionCommand("equals(DataFlavor) 1&2");
      knoppenPanel.add(button7);

      button8 = new Button("equals(DataFlavor) 2&3");
      button8.addActionListener(buttonListener);
      button8.setActionCommand("equals(DataFlavor) 2&3");
      knoppenPanel.add(button8);

      button9 = new Button("isMimeTypeEqual('') test1");
      button9.addActionListener(buttonListener);
      button9.setActionCommand("isMimeTypeEqual('') test1");
      knoppenPanel.add(button9);

      button10 = new Button("isMimeTypeEqual('') test2");
      button10.addActionListener(buttonListener);
      button10.setActionCommand("isMimeTypeEqual('') test2");
      knoppenPanel.add(button10);

      button11 = new Button("isMimeTypeEqual(DataFlavor) 1&2");
      button11.addActionListener(buttonListener);
      button11.setActionCommand("isMimeTypeEqual(DataFlavor) 1&2");
      knoppenPanel.add(button11);

      button12 = new Button("isMimeTypeEqual(DataFlavor) 2&3");
      button12.addActionListener(buttonListener);
      button12.setActionCommand("isMimeTypeEqual(DataFlavor) 2&3");
      knoppenPanel.add(button12);
    }
    if(keuze=="DataFlavor 3"){
      knoppenPanel.setLayout(new GridLayout(12,1));

      button1 = new Button("readExternal");
      button1.addActionListener(buttonListener);
      button1.setActionCommand("readExternal");
      knoppenPanel.add(button1);

      button2 = new Button("writeExternal");
      button2.addActionListener(buttonListener);
      button2.setActionCommand("writeExternal");
      knoppenPanel.add(button2);

      button3 = new Button("equals(DataFlavor) 1&2");
      button3.addActionListener(buttonListener);
      button3.setActionCommand("equals(DataFlavor) 1&2");
      knoppenPanel.add(button3);

      button4 = new Button("equals(Object) 1&2");
      button4.addActionListener(buttonListener);
      button4.setActionCommand("equals(Object) 1&2");
      knoppenPanel.add(button4);

      button5 = new Button("equals(String) 1&2");
      button5.addActionListener(buttonListener);
      button5.setActionCommand("equals(String) 1&2");
      knoppenPanel.add(button5);

      button6 = new Button("1 match(2)");
      button6.addActionListener(buttonListener);
      button6.setActionCommand("1 match(2)");
      knoppenPanel.add(button6);

      button7 = new Button("getDefaultRepresentationClass");
      button7.addActionListener(buttonListener);
      button7.setActionCommand("getDefaultRepresentationClass");
      knoppenPanel.add(button7);

      button8 = new Button("getDefaultRepresentationClassAsString");
      button8.addActionListener(buttonListener);
      button8.setActionCommand("getDefaultRepresentationClassAsString");
      knoppenPanel.add(button8);
    }
    if(keuze=="DataFlavor 4"){
      knoppenPanel.setLayout(new GridLayout(12,1));

      button1 = new Button("getParameter('class') 1");
      button1.addActionListener(buttonListener);
      button1.setActionCommand("getParameter('class')");
      knoppenPanel.add(button1);

      button2 = new Button("getParameter('class') 2");
      button2.addActionListener(buttonListener);
      button2.setActionCommand("getParameter('class') 2");
      knoppenPanel.add(button2);

      button3 = new Button("getParameter('charset') 1");
      button3.addActionListener(buttonListener);
      button3.setActionCommand("getParameter('charset')");
      knoppenPanel.add(button3);

      button4 = new Button("getParameter('charset') 2");
      button4.addActionListener(buttonListener);
      button4.setActionCommand("getParameter('charset') 2");
      knoppenPanel.add(button4);

      button5 = new Button("getParameter('') 1");
      button5.addActionListener(buttonListener);
      button5.setActionCommand("getParameter('')");
      knoppenPanel.add(button5);

      button6 = new Button("getParameter('') 2");
      button6.addActionListener(buttonListener);
      button6.setActionCommand("getParameter('') 2");
      knoppenPanel.add(button6);

      button7 = new Button("getPrimaryType() 1");
      button7.addActionListener(buttonListener);
      button7.setActionCommand("getPrimaryType()");
      knoppenPanel.add(button7);

      button8 = new Button("getPrimaryType() 2");
      button8.addActionListener(buttonListener);
      button8.setActionCommand("getPrimaryType() 2");
      knoppenPanel.add(button8);

      button9 = new Button("getSubType() 1");
      button9.addActionListener(buttonListener);
      button9.setActionCommand("getSubType()");
      knoppenPanel.add(button9);
    
      button10 = new Button("getSubType() 2");
      button10.addActionListener(buttonListener);
      button10.setActionCommand("getSubType() 2");
      knoppenPanel.add(button10);
    
      button11 = new Button("hashCode()");
      button11.addActionListener(buttonListener);
      button11.setActionCommand("hashCode()");
      knoppenPanel.add(button11);
    
      button12 = new Button("toString()");
      button12.addActionListener(buttonListener);
      button12.setActionCommand("toString()");
      knoppenPanel.add(button12);
    }
    if(keuze=="DataFlavor 5"){
      knoppenPanel.setLayout(new GridLayout(12,1));

      button1 = new Button("isFlavorJavaFileListType()");
      button1.addActionListener(buttonListener);
      button1.setActionCommand("isFlavorJavaFileListType()");
      knoppenPanel.add(button1);
    
      button2 = new Button("isFlavorRemoteObjectType()");
      button2.addActionListener(buttonListener);
      button2.setActionCommand("isFlavorRemoteObjectType()");
      knoppenPanel.add(button2);
    
      button3 = new Button("isFlavorSerializedObjectType()");
      button3.addActionListener(buttonListener);
      button3.setActionCommand("isFlavorSerializedObjectType()");
      knoppenPanel.add(button3);
    
      button4 = new Button("isMimeTypeSerializedObject()");
      button4.addActionListener(buttonListener);
      button4.setActionCommand("isMimeTypeSerializedObject()");
      knoppenPanel.add(button4);
    
      button5 = new Button("isRepresentationClassInputStream()");
      button5.addActionListener(buttonListener);
      button5.setActionCommand("isRepresentationClassInputStream()");
      knoppenPanel.add(button5);
    
      button6 = new Button("isRepresentationClassRemote()");
      button6.addActionListener(buttonListener);
      button6.setActionCommand("isRepresentationClassRemote()");
      knoppenPanel.add(button6);
    
      button7 = new Button("isRepresentationClassSerializable()");
      button7.addActionListener(buttonListener);
      button7.setActionCommand("isRepresentationClassSerializable()");
      knoppenPanel.add(button7);

      button8 = new Button("selectBestTextFlavor()");
      button8.addActionListener(buttonListener);
      button8.setActionCommand("selectBestTextFlavor()");
      knoppenPanel.add(button8);

      button9 = new Button("getReaderForText()");
      button9.addActionListener(buttonListener);
      button9.setActionCommand("getReaderForText()");
      knoppenPanel.add(button9);

/*public DataFlavor(String mimeType,
                  String humanPresentableName,
                  ClassLoader classLoader)
           throws ClassNotFoundException  
 * getReaderForText(Transferable transferable)
 * getTextPlainUnicodeFlavor()
 * selectBestTextFlavor(DataFlavor[] availableFlavors)
 */    
    }
    if(keuze=="StringSelection"){
      knoppenPanel.setLayout(new GridLayout(12,1));
      button1 = new Button("StringSelection('tekst1')");
      button1.addActionListener(buttonListener);
      button1.setActionCommand("StringSelection('tekst1')");
      knoppenPanel.add(button1);

      button2 = new Button("StringSelection(null)");
      button2.addActionListener(buttonListener);
      button2.setActionCommand("StringSelection(null)");
      knoppenPanel.add(button2);

      button3 = new Button("getTransferDataFlavors() 1");
      button3.addActionListener(buttonListener);
      button3.setActionCommand("getTransferDataFlavors() 1");
      knoppenPanel.add(button3);

      button11 = new Button("getTransferDataFlavors() 2");
      button11.addActionListener(buttonListener);
      button11.setActionCommand("getTransferDataFlavors() 2");
      knoppenPanel.add(button11);

      button4 = new Button("isDataFlavorSupported(stringFlavor)");
      button4.addActionListener(buttonListener);
      button4.setActionCommand("isDataFlavorSupported(stringFlavor)");
      knoppenPanel.add(button4);
  
      button5 = new Button("isDataFlavorSupported(plainTextFlavor)");
      button5.addActionListener(buttonListener);
      button5.setActionCommand("isDataFlavorSupported(plainTextFlavor)");
      knoppenPanel.add(button5);
  
      button6 = new Button("isDataFlavorSupported(wrong DataFlavor)");
      button6.addActionListener(buttonListener);
      button6.setActionCommand("isDataFlavorSupported(wrong DataFlavor)");
      knoppenPanel.add(button6);
  
      button7 = new Button("isDataFlavorSupported(null)");
      button7.addActionListener(buttonListener);
      button7.setActionCommand("isDataFlavorSupported(null)");
      knoppenPanel.add(button7);
  
      button8 = new Button("getTransferData(stringFlavor) 1");
      button8.addActionListener(buttonListener);
      button8.setActionCommand("getTransferData(stringFlavor) 1");
      knoppenPanel.add(button8);
  
      button12 = new Button("getTransferData(stringFlavor) 2");
      button12.addActionListener(buttonListener);
      button12.setActionCommand("getTransferData(stringFlavor) 2");
      knoppenPanel.add(button12);

      button9 = new Button("getTransferData(plainTextFlavor)");
      button9.addActionListener(buttonListener);
      button9.setActionCommand("getTransferData(plainTextFlavor)");
      knoppenPanel.add(button9);
  
      button10 = new Button("getTransferData(wrong DataFlavor)");
      button10.addActionListener(buttonListener);
      button10.setActionCommand("getTransferData(wrong DataFlavor)");
      knoppenPanel.add(button10);
    }
    if(keuze=="SystemFlavorMap"){
      knoppenPanel.setLayout(new GridLayout(14,1));
      button1 = new Button("decodeDataFlavor(String1)");
      button1.addActionListener(buttonListener);
      button1.setActionCommand("decodeDataFlavor(String1)");
      knoppenPanel.add(button1);

      button2 = new Button("decodeJavaMIMEType(String1)");
      button2.addActionListener(buttonListener);
      button2.setActionCommand("decodeJavaMIMEType(String1)");
      knoppenPanel.add(button2);

      button3 = new Button("decodeDataFlavor(String2)");
      button3.addActionListener(buttonListener);
      button3.setActionCommand("decodeDataFlavor(String2)");
      knoppenPanel.add(button3);

      button4 = new Button("decodeJavaMIMEType(String2)");
      button4.addActionListener(buttonListener);
      button4.setActionCommand("decodeJavaMIMEType(String2)");
      knoppenPanel.add(button4);
  
      button5 = new Button("encodeDataFlavor(DataFlavor 1)");
      button5.addActionListener(buttonListener);
      button5.setActionCommand("encodeDataFlavor(DataFlavor 1)");
      knoppenPanel.add(button5);
  
      button6 = new Button("encodeJavaMIMEType(String mimeType) 1");
      button6.addActionListener(buttonListener);
      button6.setActionCommand("encodeJavaMIMEType(String mimeType) 1");
      knoppenPanel.add(button6);
  
      button7 = new Button("encodeDataFlavor(DataFlavor 2)");
      button7.addActionListener(buttonListener);
      button7.setActionCommand("encodeDataFlavor(DataFlavor 2)");
      knoppenPanel.add(button7);

      button8 = new Button("encodeJavaMIMEType(String mimeType) 2");
      button8.addActionListener(buttonListener);
      button8.setActionCommand("encodeJavaMIMEType(String mimeType) 2");
      knoppenPanel.add(button8);

      button9 = new Button("getNativesForFlavors(null)");
      button9.addActionListener(buttonListener);
      button9.setActionCommand("getNativesForFlavors(null)");
      knoppenPanel.add(button9);

      button10 = new Button("getNativesForFlavors(DataFlavor[])");
      button10.addActionListener(buttonListener);
      button10.setActionCommand("getNativesForFlavors(DataFlavor[])");
      knoppenPanel.add(button10);

      button11 = new Button("getFlavorsForNatives(null)");
      button11.addActionListener(buttonListener);
      button11.setActionCommand("getFlavorsForNatives(null)");
      knoppenPanel.add(button11);

      button12 = new Button("getFlavorsForNatives(String[])");
      button12.addActionListener(buttonListener);
      button12.setActionCommand("getFlavorsForNatives(String[])");
      knoppenPanel.add(button12);

      button13 = new Button("isJavaMIMEType(String 1)");
      button13.addActionListener(buttonListener);
      button13.setActionCommand("isJavaMIMEType(String 1)");
      knoppenPanel.add(button13);

      button14 = new Button("isJavaMIMEType(String 2)");
      button14.addActionListener(buttonListener);
      button14.setActionCommand("isJavaMIMEType(String 2)");
      knoppenPanel.add(button14);
    }
    if(keuze=="Clipboard vb 1"){
      knoppenPanel.setLayout(new GridLayout(1,1));
      knoppenPanel.add(new ClipboardVb(tekstScherm));
    }
    knoppenPanel.setVisible(true);
    setVisible(true);
    validate();
    repaint();
  }


  public class MyButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {      
      try{
        if (e.getActionCommand().equals("exit")) { 
          System.out.println("exit");
          tekstScherm.append('\n'+"exit");
          System.exit(0);
        }
    
//voor clipboard
        else if (e.getActionCommand().equals("getContents")) { 
          System.out.println("getContents: "+clipboard.getContents(new Object()));
          tekstScherm.append('\n'+"getContents: "+clipboard.getContents(new Object()));
        }
        else if (e.getActionCommand().equals("getName")) { 
          System.out.println("getName: "+clipboard.getName());
          tekstScherm.append('\n'+"getName: "+clipboard.getName());
        }
        else if (e.getActionCommand().equals("setContents")) { 
          System.out.println("setContents");
          clipboard.setContents(owner1,owner1);
          tekstScherm.append('\n'+"setContents");
        }
        else if (e.getActionCommand().equals("setContents2")) { 
          System.out.println("setContents2");
          clipboard.setContents(owner2,owner2);
          tekstScherm.append('\n'+"setContents2");
        }
        else if (e.getActionCommand().equals("setContents3")) { 
          System.out.println("setContents3");
          clipboard.setContents(owner3,owner3);
          tekstScherm.append('\n'+"setContents3");
        }  
  
//voor keuze klasse
        else if (e.getActionCommand().equals("Clipboard")) { 
          System.out.println("opbouw knoppen Clipboard");
          opbouwKnoppen("Clipboard");
          tekstScherm.append('\n'+"opbouw knoppen Clipboard");
        }  
        else if (e.getActionCommand().equals("Clipboard vb 1")) { 
          System.out.println("opbouw knoppen Clipboard vb 1");
          opbouwKnoppen("Clipboard vb 1");
          tekstScherm.append('\n'+"opbouw knoppen Clipboard vb 1");
        }  
        else if (e.getActionCommand().equals("Clipboard vb 2")) { 
          System.out.println("opbouw knoppen Clipboard vb 2");
          opbouwKnoppen("Clipboard vb 2");
          tekstScherm.append('\n'+"opbouw knoppen Clipboard vb 2");
        }  
        else if (e.getActionCommand().equals("DataFlavor 1")) {
          System.out.println("opbouw knoppen DataFlavor 1");
          opbouwKnoppen("DataFlavor 1");
          tekstScherm.append('\n'+"opbouw knoppen DataFlavor 1");
        }
        else if (e.getActionCommand().equals("DataFlavor 2")) {
          System.out.println("opbouw knoppen DataFlavor 2");
          opbouwKnoppen("DataFlavor 2");
          tekstScherm.append('\n'+"opbouw knoppen DataFlavor 2");
        }
        else if (e.getActionCommand().equals("DataFlavor 3")) {
          System.out.println("opbouw knoppen DataFlavor 3");
          opbouwKnoppen("DataFlavor 3");
          tekstScherm.append('\n'+"opbouw knoppen DataFlavor 3");
        }
        else if (e.getActionCommand().equals("DataFlavor 4")) {
          System.out.println("opbouw knoppen DataFlavor 4");
          opbouwKnoppen("DataFlavor 4");
          tekstScherm.append('\n'+"opbouw knoppen DataFlavor 4");
        }
        else if (e.getActionCommand().equals("DataFlavor 5")) {
          System.out.println("opbouw knoppen DataFlavor 5");
          opbouwKnoppen("DataFlavor 5");
          tekstScherm.append('\n'+"opbouw knoppen DataFlavor 5");
        }
        else if (e.getActionCommand().equals("StringSelection")) {
          System.out.println("opbouw knoppen StringSelection");
          opbouwKnoppen("StringSelection");
          tekstScherm.append('\n'+"opbouw knoppen StringSelection");
        }

        else if (e.getActionCommand().equals("SystemFlavorMap")) {
          System.out.println("opbouw knoppen SystemFlavorMap");
          System.out.println("SystemFlavorMap.getDefaultFlavorMap() = "+SystemFlavorMap.getDefaultFlavorMap());
          opbouwKnoppen("SystemFlavorMap");
          tekstScherm.append('\n'+"opbouw knoppen SystemFlavorMap");
          tekstScherm.append('\n'+"SystemFlavorMap.getDefaultFlavorMap() = "+SystemFlavorMap.getDefaultFlavorMap());
        }

//voor DataFlavor
        else if (e.getActionCommand().equals("DataFlavor(String,String) 1")) {
          dataFlavor1 = new DataFlavor("application/x-java-serialized-object;class=java.lang.String","");
          System.out.println("dataFlavor1 = new DataFlavor('application/x-java-serialized-object;class=java.lang.String','')");
          tekstScherm.append('\n'+"dataFlavor1 = new DataFlavor('application/x-java-serialized-object;class=java.lang.String','')");
        }
        else if (e.getActionCommand().equals("DataFlavor(String,String) 2")) {
          dataFlavor2 = new DataFlavor("application/x-java-serialized-object;class=java.io.StringReader","");
          System.out.println("dataFlavor2 = new DataFlavor('application/x-java-serialized-object;class=java.io.StringReader','')");
          tekstScherm.append('\n'+"dataFlavor2 = new DataFlavor('application/x-java-serialized-object;class=java.io.StringReader','')");
        }
        else if (e.getActionCommand().equals("DataFlavor(String2,String) 2")) {
          dataFlavor2 = new DataFlavor("text/plain; charset=unicode","");
          System.out.println("dataFlavor2 = new DataFlavor('text/plain; charset=unicode','')");
          tekstScherm.append('\n'+"dataFlavor2 = new DataFlavor('text/plain; charset=unicode','')");
        }
        else if (e.getActionCommand().equals("DataFlavor(Class,String) 3")) {
          dataFlavor3 = new DataFlavor(java.io.StringReader.class,"test3");
          System.out.println("dataFlavor3 = new DataFlavor(java.io.StringReader.class,'test3')");
          tekstScherm.append('\n'+"dataFlavor3 = new DataFlavor(java.io.StringReader.class,'test3')");
        }
        else if (e.getActionCommand().equals("DataFlavor(Class,String) 2")) {
          dataFlavor2 = new DataFlavor(java.lang.String.class,"testen");
          System.out.println("dataFlavor2 = new DataFlavor(java.lang.String.class,'testen')");
          tekstScherm.append('\n'+"dataFlavor2 = new DataFlavor(java.lang.String.class,'testen')");
        }
        else if (e.getActionCommand().equals("getMimeType 1")) {
          System.out.println("dataFlavor1.getMimeType() = "+dataFlavor1.getMimeType());
          tekstScherm.append('\n'+"dataFlavor1.getMimeType() = "+dataFlavor1.getMimeType());
        }
        else if (e.getActionCommand().equals("getMimeType 2")) {
          System.out.println("dataFlavor2.getMimeType() = "+dataFlavor2.getMimeType());
          tekstScherm.append('\n'+"dataFlavor2.getMimeType() = "+dataFlavor2.getMimeType());
        }
        else if (e.getActionCommand().equals("getMimeType 3")) {
          System.out.println("dataFlavor3.getMimeType() = "+dataFlavor3.getMimeType());
          tekstScherm.append('\n'+"dataFlavor3.getMimeType() = "+dataFlavor3.getMimeType());
        }
        else if (e.getActionCommand().equals("getRepresentationClass 1")) {
          System.out.println("dataFlavor1.getRepresentationClass() = "+dataFlavor1.getRepresentationClass());
          tekstScherm.append('\n'+"dataFlavor1.getRepresentationClass() = "+dataFlavor1.getRepresentationClass());
        }
        else if (e.getActionCommand().equals("getRepresentationClass 2")) {
          System.out.println("dataFlavor2.getRepresentationClass() = "+dataFlavor2.getRepresentationClass());
          tekstScherm.append('\n'+"dataFlavor2.getRepresentationClass() = "+dataFlavor2.getRepresentationClass());
        }
        else if (e.getActionCommand().equals("getRepresentationClass 3")) {
          System.out.println("dataFlavor3.getRepresentationClass() = "+dataFlavor3.getRepresentationClass());
          tekstScherm.append('\n'+"dataFlavor3.getRepresentationClass() = "+dataFlavor3.getRepresentationClass());
        }
        else if (e.getActionCommand().equals("getHumanPresentableName 1")) {
          System.out.println("dataFlavor1.getHumanPresentableName() = "+dataFlavor1.getHumanPresentableName());
          tekstScherm.append('\n'+"dataFlavor1.getHumanPresentableName() = "+dataFlavor1.getHumanPresentableName());
        }
        else if (e.getActionCommand().equals("getHumanPresentableName 2")) {
          System.out.println("dataFlavor2.getHumanPresentableName() = "+dataFlavor2.getHumanPresentableName());
          tekstScherm.append('\n'+"dataFlavor2.getHumanPresentableName() = "+dataFlavor2.getHumanPresentableName());
        }
        else if (e.getActionCommand().equals("getHumanPresentableName 3")) {
          System.out.println("dataFlavor3.getHumanPresentableName() = "+dataFlavor3.getHumanPresentableName());
          tekstScherm.append('\n'+"dataFlavor3.getHumanPresentableName() = "+dataFlavor3.getHumanPresentableName());
        }
        else if (e.getActionCommand().equals("setHumanPresentableName('test1') 1")) {
          System.out.println("dataFlavor1.setHumanPresentableName('test1')");
          dataFlavor1.setHumanPresentableName("test1");
          tekstScherm.append('\n'+"dataFlavor1.setHumanPresentableName('test1')");
        }
        else if (e.getActionCommand().equals("setHumanPresentableName('test2') 1")) {
          System.out.println("dataFlavor1.setHumanPresentableName('test2')");
          dataFlavor1.setHumanPresentableName("test2");
          tekstScherm.append('\n'+"dataFlavor1.setHumanPresentableName('test2')");
        }
        else if (e.getActionCommand().equals("setHumanPresentableName('test2') 2")) {
          System.out.println("dataFlavor2.setHumanPresentableName('test2')");
          dataFlavor2.setHumanPresentableName("test2");
          tekstScherm.append('\n'+"dataFlavor2.setHumanPresentableName('test2')");
        }
        else if (e.getActionCommand().equals("equals(DataFlavor) 1&2")) {
          System.out.println("dataFlavor1.equals(dataFlavor2) = "+dataFlavor1.equals(dataFlavor2));
          tekstScherm.append('\n'+"dataFlavor1.equals(dataFlavor2) = "+dataFlavor1.equals(dataFlavor2));
        }
        else if (e.getActionCommand().equals("equals(DataFlavor) 2&3")) {
          System.out.println("dataFlavor2.equals(dataFlavor3) = "+dataFlavor2.equals(dataFlavor3));
          tekstScherm.append('\n'+"dataFlavor2.equals(dataFlavor3) = "+dataFlavor2.equals(dataFlavor3));
        }
        else if (e.getActionCommand().equals("isMimeTypeEqual('') test1")) {
          System.out.println("dataFlavor1.isMimeTypeEqual('application/x-java-serialized-object;class=java.io.StringReader') = "+dataFlavor1.isMimeTypeEqual("application/x-java-serialized-object;class=java.io.StringReader"));
          tekstScherm.append('\n'+"dataFlavor1.isMimeTypeEqual('application/x-java-serialized-object;class=java.io.StringReader') = "+dataFlavor1.isMimeTypeEqual("application/x-java-serialized-object;class=java.io.StringReader"));
        }
        else if (e.getActionCommand().equals("isMimeTypeEqual('') test2")) {
          System.out.println("dataFlavor1.isMimeTypeEqual('application/x-java-serialized-object;class=java.lang.String') = "+dataFlavor1.isMimeTypeEqual("application/x-java-serialized-object;class=java.lang.String"));
          tekstScherm.append('\n'+"dataFlavor1.isMimeTypeEqual('application/x-java-serialized-object;class=java.lang.String') = "+dataFlavor1.isMimeTypeEqual("application/x-java-serialized-object;class=java.lang.String"));
        }
        else if (e.getActionCommand().equals("isMimeTypeEqual(DataFlavor) 1&2")) {
          System.out.println("dataFlavor1.equals(dataFlavor2) = "+dataFlavor1.equals(dataFlavor2));
          tekstScherm.append('\n'+"dataFlavor1.equals(dataFlavor2) = "+dataFlavor1.equals(dataFlavor2));
        }
        else if (e.getActionCommand().equals("isMimeTypeEqual(DataFlavor) 2&3")) {
          System.out.println("dataFlavor2.equals(dataFlavor3) = "+dataFlavor2.equals(dataFlavor3));
          tekstScherm.append('\n'+"dataFlavor2.equals(dataFlavor3) = "+dataFlavor2.equals(dataFlavor3));
        }
//READ EN WRITE GAAT NOG NIET JUIST
        else if (e.getActionCommand().equals("readExternal")) {
          dataFlavor2.readExternal(is);
          System.out.println("readExternal");
          tekstScherm.append('\n'+"readExternal");
        }
        else if (e.getActionCommand().equals("writeExternal")) {
          dataFlavor2.writeExternal(os);
          System.out.println("dataFlavor2.writeExternal");
          tekstScherm.append('\n'+"dataFlavor2.writeExternal");
        }
        else if (e.getActionCommand().equals("2=clone(1)")) {
          dataFlavor2=(DataFlavor) dataFlavor1.clone();
          System.out.println("dataFlavor2=(DataFlavor) dataFlavor1.clone()");
          tekstScherm.append('\n'+"dataFlavor2=(DataFlavor) dataFlavor1.clone()");
        }
        else if (e.getActionCommand().equals("equals(DataFlavor) 1&2")) {
          System.out.println("dataFlavor1.equals(dataFlavor2) = "+dataFlavor1.equals(dataFlavor2));
          tekstScherm.append('\n'+"dataFlavor1.equals(dataFlavor2) = "+dataFlavor1.equals(dataFlavor2));
        }
        else if (e.getActionCommand().equals("equals(Object) 1&2")) {
          System.out.println("dataFlavor1.equals((Object) dataFlavor2) = "+dataFlavor1.equals((Object) dataFlavor2));
          tekstScherm.append('\n'+"dataFlavor1.equals((Object) dataFlavor2) = "+dataFlavor1.equals((Object) dataFlavor2));
        }
        else if (e.getActionCommand().equals("equals(String) 1&2")) {
          System.out.println("dataFlavor1.equals(dataFlavor2.getMimeType()) = "+dataFlavor1.equals(dataFlavor2.getMimeType()));
          tekstScherm.append('\n'+"dataFlavor1.equals(dataFlavor2.getMimeType()) = "+dataFlavor1.equals(dataFlavor2.getMimeType()));
        }
        else if (e.getActionCommand().equals("getDefaultRepresentationClass")) {
          System.out.println("dataFlavor1.getDefaultRepresentationClass() = "+dataFlavor1.getDefaultRepresentationClass());
          tekstScherm.append('\n'+"dataFlavor1.getDefaultRepresentationClass() = "+dataFlavor1.getDefaultRepresentationClass());
        }
        else if (e.getActionCommand().equals("getDefaultRepresentationClassAsString")) {
          System.out.println("dataFlavor1.getDefaultRepresentationClassAsString() = "+dataFlavor1.getDefaultRepresentationClassAsString());
          tekstScherm.append('\n'+"dataFlavor1.getDefaultRepresentationClassAsString() = "+dataFlavor1.getDefaultRepresentationClassAsString());
        }
        else if (e.getActionCommand().equals("getParameter('class')")) {
          System.out.println("dataFlavor1.getParameter('class') = "+dataFlavor1.getParameter("class"));
          tekstScherm.append('\n'+"dataFlavor1.getParameter('class') = "+dataFlavor1.getParameter("class"));
        }
        else if (e.getActionCommand().equals("getParameter('charset')")) {
          System.out.println("dataFlavor1.getParameter('charset') = "+dataFlavor1.getParameter("charset"));
          tekstScherm.append('\n'+"dataFlavor1.getParameter('charset') = "+dataFlavor1.getParameter("charset"));
        }
        else if (e.getActionCommand().equals("getParameter('')")) {
          System.out.println("dataFlavor1.getParameter('') = "+dataFlavor1.getParameter(""));
          tekstScherm.append('\n'+"dataFlavor1.getParameter('') = "+dataFlavor1.getParameter(""));
        }
        else if (e.getActionCommand().equals("getParameter('class') 2")) {
          System.out.println("dataFlavor2.getParameter('class') = "+dataFlavor2.getParameter("class"));
          tekstScherm.append('\n'+"dataFlavor2.getParameter('class') = "+dataFlavor2.getParameter("class"));
        }
        else if (e.getActionCommand().equals("getParameter('charset') 2")) {
          System.out.println("dataFlavor2.getParameter('charset') = "+dataFlavor2.getParameter("charset"));
          tekstScherm.append('\n'+"dataFlavor2.getParameter('charset') = "+dataFlavor2.getParameter("charset"));
        }
        else if (e.getActionCommand().equals("getParameter('') 2")) {
          System.out.println("dataFlavor2.getParameter('') = "+dataFlavor2.getParameter(""));
          tekstScherm.append('\n'+"dataFlavor2.getParameter('') = "+dataFlavor2.getParameter(""));
        }
        else if (e.getActionCommand().equals("getPrimaryType()")) {
          System.out.println("dataFlavor1.getPrimaryType() = "+dataFlavor1.getPrimaryType());
          tekstScherm.append('\n'+"dataFlavor1.getPrimaryType() = "+dataFlavor1.getPrimaryType());
        }
        else if (e.getActionCommand().equals("getPrimaryType() 2")) {
          System.out.println("dataFlavor2.getPrimaryType() = "+dataFlavor2.getPrimaryType());
          tekstScherm.append('\n'+"dataFlavor2.getPrimaryType() = "+dataFlavor2.getPrimaryType());
        }
        else if (e.getActionCommand().equals("getSubType()")) {
          System.out.println("dataFlavor1.getSubType() = "+dataFlavor1.getSubType());
          tekstScherm.append('\n'+"dataFlavor1.getSubType() = "+dataFlavor1.getSubType());
        }
        else if (e.getActionCommand().equals("getSubType() 2")) {
          System.out.println("dataFlavor2.getSubType() = "+dataFlavor2.getSubType());
          tekstScherm.append('\n'+"dataFlavor2.getSubType() = "+dataFlavor2.getSubType());
        }
        else if (e.getActionCommand().equals("hashCode()")) {
          System.out.println("dataFlavor2.hashCode() = "+dataFlavor2.hashCode());
          tekstScherm.append('\n'+"dataFlavor2.hashCode() = "+dataFlavor2.hashCode());
        }
        else if (e.getActionCommand().equals("isFlavorJavaFileListType()")) {
          System.out.println("dataFlavor2.isFlavorJavaFileListType() = "+dataFlavor2.isFlavorJavaFileListType());
          tekstScherm.append('\n'+"dataFlavor2.isFlavorJavaFileListType() = "+dataFlavor2.isFlavorJavaFileListType());
        }
        else if (e.getActionCommand().equals("isFlavorRemoteObjectType()")) {
          System.out.println("dataFlavor2.isFlavorRemoteObjectType() = "+dataFlavor2.isFlavorRemoteObjectType());
          tekstScherm.append('\n'+"dataFlavor2.isFlavorRemoteObjectType() = "+dataFlavor2.isFlavorRemoteObjectType());
        }
        else if (e.getActionCommand().equals("isFlavorSerializedObjectType()")) {
          System.out.println("dataFlavor2.isFlavorSerializedObjectType() = "+dataFlavor2.isFlavorSerializedObjectType());
          tekstScherm.append('\n'+"dataFlavor2.isFlavorSerializedObjectType() = "+dataFlavor2.isFlavorSerializedObjectType());
        }
        else if (e.getActionCommand().equals("isMimeTypeSerializedObject()")) {
          System.out.println("dataFlavor2.isMimeTypeSerializedObject() = "+dataFlavor2.isMimeTypeSerializedObject());
          tekstScherm.append('\n'+"dataFlavor2.isMimeTypeSerializedObject() = "+dataFlavor2.isMimeTypeSerializedObject());
        }
        else if (e.getActionCommand().equals("isRepresentationClassInputStream()")) {
          System.out.println("dataFlavor2.isRepresentationClassInputStream() = "+dataFlavor2.isRepresentationClassInputStream());
          tekstScherm.append('\n'+"dataFlavor2.isRepresentationClassInputStream() = "+dataFlavor2.isRepresentationClassInputStream());
        }
        else if (e.getActionCommand().equals("isRepresentationClassRemote()")) {
          System.out.println("dataFlavor2.isRepresentationClassRemote() = "+dataFlavor2.isRepresentationClassRemote());
          tekstScherm.append('\n'+"dataFlavor2.isRepresentationClassRemote() = "+dataFlavor2.isRepresentationClassRemote());
        }
        else if (e.getActionCommand().equals("isRepresentationClassSerializable()")) {
          System.out.println("dataFlavor2.isRepresentationClassSerializable() = "+dataFlavor2.isRepresentationClassSerializable());
          tekstScherm.append('\n'+"dataFlavor2.isRepresentationClassSerializable() = "+dataFlavor2.isRepresentationClassSerializable());
        }
        else if (e.getActionCommand().equals("toString()")) {
          System.out.println("dataFlavor2.toString() = "+dataFlavor2.toString());
          tekstScherm.append('\n'+"dataFlavor2.toString() = "+dataFlavor2.toString());
        }
        else if (e.getActionCommand().equals("1 match(2)")) {
          System.out.println("dataFlavor1.match(dataFlavor2) = "+dataFlavor1.match(dataFlavor2));
          tekstScherm.append('\n'+"dataFlavor1.match(dataFlavor2) = "+dataFlavor1.match(dataFlavor2));
        }
        else if (e.getActionCommand().equals("selectBestTextFlavor()")) {
          System.out.println("DataFlavor.selectBestTextFlavor((new StringSelection('hello')).getTransferDataFlavors()) = "+DataFlavor.selectBestTextFlavor((new StringSelection("hello")).getTransferDataFlavors()));
          tekstScherm.append('\n'+"DataFlavor.selectBestTextFlavor((new StringSelection('hello')).getTransferDataFlavors()) = "+DataFlavor.selectBestTextFlavor((new StringSelection("hello")).getTransferDataFlavors()));
        }
        else if (e.getActionCommand().equals("getReaderForText()")) {
          System.out.println("dataFlavor2.getReaderForText(new StringSelection('hello')) = "+dataFlavor2.getReaderForText(new StringSelection("hello")));
          tekstScherm.append('\n'+"dataFlavor2.getReaderForText(new StringSelection('hello')) = "+dataFlavor2.getReaderForText(new StringSelection("hello")));
        }
//voor stringselection
        else if (e.getActionCommand().equals("StringSelection('tekst1')")) {
          stringSelection1= new StringSelection("tekst1");
          System.out.println("stringSelection1= new StringSelection('tekst1')");
          tekstScherm.append('\n'+"stringSelection1= new StringSelection('tekst1')");
        }
        else if (e.getActionCommand().equals("StringSelection(null)")) {
          stringSelection2= new StringSelection(null);
          System.out.println("stringSelection2= new StringSelection(null)");
          tekstScherm.append('\n'+"stringSelection2= new StringSelection(null)");
        }
        else if (e.getActionCommand().equals("getTransferDataFlavors() 1")) {
          DataFlavor[] standaardDataFlavor = stringSelection1.getTransferDataFlavors();
          System.out.println("DataFlavor[0] = "+standaardDataFlavor[0].toString()+'\n'+"DataFlavor[1] = "+standaardDataFlavor[1].toString());
          tekstScherm.append('\n'+"DataFlavor[0] = "+standaardDataFlavor[0].toString()+'\n'+"DataFlavor[1] = "+standaardDataFlavor[1].toString());
        }
        else if (e.getActionCommand().equals("getTransferDataFlavors() 2")) {
          DataFlavor[] standaardDataFlavor = stringSelection2.getTransferDataFlavors();
          System.out.println("DataFlavor[0] = "+standaardDataFlavor[0].toString()+'\n'+"DataFlavor[1] = "+standaardDataFlavor[1].toString());
          tekstScherm.append('\n'+"DataFlavor[0] = "+standaardDataFlavor[0].toString()+'\n'+"DataFlavor[1] = "+standaardDataFlavor[1].toString());
        }
        else if (e.getActionCommand().equals("isDataFlavorSupported(stringFlavor)")) {
          System.out.println("stringSelection1.isDataFlavorSupported(DataFlavor.stringFlavor) = "+stringSelection1.isDataFlavorSupported(DataFlavor.stringFlavor));
          tekstScherm.append('\n'+"stringSelection1.isDataFlavorSupported(DataFlavor.stringFlavor) = "+stringSelection1.isDataFlavorSupported(DataFlavor.stringFlavor));
        }
        else if (e.getActionCommand().equals("isDataFlavorSupported(plainTextFlavor)")) {
          System.out.println("stringSelection1.isDataFlavorSupported(DataFlavor.plainTextFlavor) = "+stringSelection1.isDataFlavorSupported(DataFlavor.plainTextFlavor));
          tekstScherm.append('\n'+"stringSelection1.isDataFlavorSupported(DataFlavor.plainTextFlavor) = "+stringSelection1.isDataFlavorSupported(DataFlavor.plainTextFlavor));
        }
        else if (e.getActionCommand().equals("isDataFlavorSupported(wrong DataFlavor)")) {
          System.out.println("stringSelection1.isDataFlavorSupported(new DataFlavor('uussbb','wrong type')) = "+stringSelection1.isDataFlavorSupported(new DataFlavor("uussbb","wrong type")));
          tekstScherm.append('\n'+"stringSelection1.isDataFlavorSupported(new DataFlavor('uussbb','wrong type')) = "+stringSelection1.isDataFlavorSupported(new DataFlavor("uussbb","wrong type")));
        }
        else if (e.getActionCommand().equals("isDataFlavorSupported(null)")) {
          System.out.println("stringSelection1.isDataFlavorSupported(null) = "+stringSelection1.isDataFlavorSupported(null));
          tekstScherm.append('\n'+"stringSelection1.isDataFlavorSupported(null) = "+stringSelection1.isDataFlavorSupported(null));
        }
        else if (e.getActionCommand().equals("getTransferData(stringFlavor) 1")) {
          System.out.println("stringSelection1.getTransferData(DataFlavor.stringFlavor): "+stringSelection1.getTransferData(DataFlavor.stringFlavor));
          tekstScherm.append('\n'+"stringSelection1.getTransferData(DataFlavor.stringFlavor): "+stringSelection1.getTransferData(DataFlavor.stringFlavor));
        }
        else if (e.getActionCommand().equals("getTransferData(stringFlavor) 2")) {
          System.out.println("stringSelection2.getTransferData(DataFlavor.stringFlavor): "+stringSelection2.getTransferData(DataFlavor.stringFlavor));
          tekstScherm.append('\n'+"stringSelection2.getTransferData(DataFlavor.stringFlavor): "+stringSelection2.getTransferData(DataFlavor.stringFlavor));
        }
        else if (e.getActionCommand().equals("getTransferData(plainTextFlavor)")) {
          System.out.println("stringSelection1.getTransferData(DataFlavor.plainTextFlavor): "+stringSelection1.getTransferData(DataFlavor.plainTextFlavor));
          tekstScherm.append('\n'+"stringSelection1.getTransferData(DataFlavor.plainTextFlavor): "+stringSelection1.getTransferData(DataFlavor.plainTextFlavor));
        }
        else if (e.getActionCommand().equals("getTransferData(wrong DataFlavor)")) {
          System.out.println("stringSelection1.getTransferData(new DataFlavor('uussbb','wrong type')): "+stringSelection1.getTransferData(new DataFlavor("uussbb","wrong type")));
          tekstScherm.append('\n'+"stringSelection1.getTransferData(new DataFlavor('uussbb','wrong type')): "+stringSelection1.getTransferData(new DataFlavor("uussbb","wrong type")));
        }
//voor SystemFlavorMap
        else if (e.getActionCommand().equals("decodeDataFlavor(String1)")) {
          System.out.println("SystemFlavorMap.decodeDataFlavor('JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String') = "
	                     +SystemFlavorMap.decodeDataFlavor("JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String"));
          tekstScherm.append('\n'+"SystemFlavorMap.decodeDataFlavor('JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String') = "
	                     +SystemFlavorMap.decodeDataFlavor("JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String"));
        }
        else if (e.getActionCommand().equals("decodeJavaMIMEType(String1)")) {
          System.out.println("SystemFlavorMap.decodeJavaMIMEType('JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String') = "
	                     +SystemFlavorMap.decodeJavaMIMEType("JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String"));
          tekstScherm.append('\n'+"SystemFlavorMap.decodeJavaMIMEType('JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String') = "
	                     +SystemFlavorMap.decodeJavaMIMEType("JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String"));
        }
        else if (e.getActionCommand().equals("decodeDataFlavor(String2)")) {
          System.out.println("SystemFlavorMap.decodeDataFlavor('') = "+SystemFlavorMap.decodeDataFlavor(""));
          tekstScherm.append('\n'+"SystemFlavorMap.decodeDataFlavor('') = "+SystemFlavorMap.decodeDataFlavor(""));
        }
        else if (e.getActionCommand().equals("decodeJavaMIMEType(String2)")) {
          System.out.println("SystemFlavorMap.decodeJavaMIMEType('') = "+SystemFlavorMap.decodeJavaMIMEType(""));
          tekstScherm.append('\n'+"SystemFlavorMap.decodeJavaMIMEType('') = "+SystemFlavorMap.decodeJavaMIMEType(""));
        }
        else if (e.getActionCommand().equals("encodeDataFlavor(DataFlavor 1)")) {
          System.out.println("SystemFlavorMap.encodeDataFlavor(DataFlavor.stringFlavor) = "+SystemFlavorMap.encodeDataFlavor(DataFlavor.stringFlavor));
          tekstScherm.append('\n'+"SystemFlavorMap.encodeDataFlavor(DataFlavor.stringFlavor) = "+SystemFlavorMap.encodeDataFlavor(DataFlavor.stringFlavor));
        }
        else if (e.getActionCommand().equals("encodeDataFlavor(DataFlavor 2)")) {
          System.out.println("SystemFlavorMap.encodeDataFlavor(DataFlavor.plainTextFlavor) = "+SystemFlavorMap.encodeDataFlavor(DataFlavor.plainTextFlavor));
          tekstScherm.append('\n'+"SystemFlavorMap.encodeDataFlavor(DataFlavor.plainTextFlavor) = "+SystemFlavorMap.encodeDataFlavor(DataFlavor.plainTextFlavor));
        }
        else if (e.getActionCommand().equals("encodeJavaMIMEType(String mimeType) 1")) {
          System.out.println("SystemFlavorMap.encodeJavaMIMEType('application/x-java-serialized-object; class=java.lang.String') = "
	                     +SystemFlavorMap.encodeJavaMIMEType("application/x-java-serialized-object; class=java.lang.String"));
          tekstScherm.append('\n'+"SystemFlavorMap.encodeJavaMIMEType('application/x-java-serialized-object; class=java.lang.String') = "
	                     +SystemFlavorMap.encodeJavaMIMEType("application/x-java-serialized-object; class=java.lang.String"));
        }
        else if (e.getActionCommand().equals("encodeJavaMIMEType(String mimeType) 2")) {
          System.out.println("SystemFlavorMap.encodeJavaMIMEType('JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String') = "
	                     +SystemFlavorMap.encodeJavaMIMEType("JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String"));
          tekstScherm.append('\n'+"SystemFlavorMap.encodeJavaMIMEType('JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String') = "
	                     +SystemFlavorMap.encodeJavaMIMEType("JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String"));
        }
        else if (e.getActionCommand().equals("getNativesForFlavors(null)")) {
          Object[] natives = flavormap.getNativesForFlavors(null).values().toArray();
          for(int i=0; i<natives.length; i++){
            System.out.println("native "+i+" = "+natives[i]);
            tekstScherm.append('\n'+"native "+i+" = "+natives[i]);
          }
        }
        else if (e.getActionCommand().equals("getNativesForFlavors(DataFlavor[])")) {
          DataFlavor[] dataflavors = {DataFlavor.stringFlavor};
          Object[] natives = flavormap.getNativesForFlavors(dataflavors).values().toArray();
          for(int i=0; i<natives.length; i++){
            System.out.println("native "+i+" = "+natives[i]);
            tekstScherm.append('\n'+"native "+i+" = "+natives[i]);
          }
        }
        else if (e.getActionCommand().equals("getFlavorsForNatives(null)")) {
          Object[] flavors = flavormap.getFlavorsForNatives(null).values().toArray();
          for(int i=0; i<flavors.length; i++){
            System.out.println("flavor "+i+" = "+flavors[i]);
            tekstScherm.append('\n'+"flavor "+i+" = "+flavors[i]);
            if(flavors[i]!=null){
              System.out.println("((DataFlavor)flavors["+i+"]).getHumanPresentableName() = "+((DataFlavor)flavors[i]).getHumanPresentableName());
              tekstScherm.append('\n'+"((DataFlavor)flavors["+i+"]).getHumanPresentableName() = "+((DataFlavor)flavors[i]).getHumanPresentableName());
            }
          }
        }
        else if (e.getActionCommand().equals("getFlavorsForNatives(String[])")) {
          String[] strings = {"COMPOUND_TEXT"};
          Object[] flavors = flavormap.getFlavorsForNatives(strings).values().toArray();
          for(int i=0; i<flavors.length; i++){
            System.out.println("flavor "+i+" = "+(flavors[i]));
            tekstScherm.append('\n'+"flavor "+i+" = "+flavors[i]);
            if(flavors[i]!=null){
              System.out.println("((DataFlavor)flavors["+i+"]).getHumanPresentableName() = "+((DataFlavor)flavors[i]).getHumanPresentableName());
              tekstScherm.append('\n'+"((DataFlavor)flavors["+i+"]).getHumanPresentableName() = "+((DataFlavor)flavors[i]).getHumanPresentableName());
            }
          }
        }
        else if (e.getActionCommand().equals("isJavaMIMEType(String 1)")) {
          System.out.println("flavormap.isJavaMIMEType('JAVA_DATAFLAVOR:') = "
                             +SystemFlavorMap.isJavaMIMEType("JAVA_DATAFLAVOR:"));
          tekstScherm.append('\n'+"flavormap.isJavaMIMEType('JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String') = "
                             +SystemFlavorMap.isJavaMIMEType("JAVA_DATAFLAVOR:application/x-java-serialized-object; class=java.lang.String"));
        }
        else if (e.getActionCommand().equals("isJavaMIMEType(String 2)")) {
          System.out.println("flavormap.isJavaMIMEType('application/x-java-serialized-object; class=java.lang.String') = "
                             +SystemFlavorMap.isJavaMIMEType("application/x-java-serialized-object; class=java.lang.String"));
          tekstScherm.append('\n'+"flavormap.isJavaMIMEType('application/x-java-serialized-object; class=java.lang.String') = "
                             +SystemFlavorMap.isJavaMIMEType("application/x-java-serialized-object; class=java.lang.String"));
        }
      } catch(NullPointerException f) {
        System.out.println("NullPointerException: "+f.getMessage());
        tekstScherm.append('\n'+"NullPointerException: "+f.getMessage());
      } catch(UnsupportedFlavorException ufe) {
        System.out.println("UnsupportedFlavorException = "+ufe.getMessage());
        tekstScherm.append('\n'+"UnsupportedFlavorException = "+ufe.getMessage());
      } catch(IOException ioe) {
        System.out.println("UnsupportedFlavorException = "+ioe.getMessage());
        tekstScherm.append('\n'+"UnsupportedFlavorException = "+ioe.getMessage());
      } catch(IllegalArgumentException iae) {
        System.out.println("IllegalArgumentException = "+iae.getMessage());
        tekstScherm.append('\n'+"IllegalArgumentException = "+iae.getMessage());
      } catch(CloneNotSupportedException cnse) {
        System.out.println("CloneNotSupportedException = "+cnse.getMessage());
        tekstScherm.append('\n'+"CloneNotSupportedException = "+cnse.getMessage());
      } catch(ClassNotFoundException cnfe) {
        System.out.println("ClassNotFoundException = "+cnfe.getMessage());
        tekstScherm.append('\n'+"ClassNotFoundException = "+cnfe.getMessage());
      } catch(ClassCastException cce) {
        System.out.println("ClassCastException = "+cce.getMessage());
        tekstScherm.append('\n'+"ClassCastException = "+cce.getMessage());
      }
    } //actionPerformed
  }//MyButtonListener
}

