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


package com.acunia.wonka.test.awt.event;

import com.acunia.wonka.test.awt.*;
import java.awt.event.*;
import java.awt.*;

public class TextEventTest extends VisualTestImpl implements ActionListener , TextListener{

  private NamedTextField field;
  private NamedTextArea area;
  private Button append;
  private Button insert;
  private Button replace;
  private Button clearArea;
  private Button clearField;
  private int eventNo;

  private List display2;
  private int count=1;
  final static String WELCOME = "Wellcome to Willy Wonka's wonderful chocolate factory";
  final static String OOMPA = " <Oompa-Loompa> ";

  public TextEventTest() {
    setForeground(new Color(80,32,32));
    setBackground(new Color(128,64,64));
    int step = 100/4;
    int red = 155;
    int shade = 100;
    setLayout(new BorderLayout());
    Panel center = new Panel(new BorderLayout());
      Panel texts = new Panel(new BorderLayout());
        area = new NamedTextArea("<TextArea>", WELCOME);
        area.addTextListener(this);
        area.setForeground(new Color(160,128,128));
        //red+=step;
        texts.add(area, BorderLayout.CENTER);
        field = new NamedTextField("<TextField>", OOMPA+count);
        count++;
        field.addTextListener(this);
        field.setBackground(new Color(red,shade,shade));
        red+=step;
        texts.add(field, BorderLayout.SOUTH);
      center.add(texts, BorderLayout.CENTER);

    display2=new List(3,false);
    display2.setBackground(new Color(red,shade,shade));
    red+=step;

      Panel buttons = new Panel(new GridLayout(5,1));
        append = new Button("append text");
        append.addActionListener(this);
        append.setBackground(new Color(red,shade,shade));
        shade+=step;
        buttons.add(append);
        insert = new Button("insert text");
        insert.addActionListener(this);
        insert.setBackground(new Color(red,shade,shade));
        shade+=step;
        buttons.add(insert);
        replace = new Button("replace selection");
        replace.addActionListener(this);
        replace.setBackground(new Color(red,shade,shade));
        shade+=step;
        buttons.add(replace);
        clearArea = new Button("clear area");
        clearArea.addActionListener(this);
        clearArea.setBackground(new Color(red,shade,shade));
        shade+=step;
        buttons.add(clearArea);
        clearField = new Button("Clear Field");
        clearField.addActionListener(this);
        clearField.setBackground(new Color(red,shade,shade));
        red+=step;
        buttons.add(clearField);
      center.add(buttons, BorderLayout.EAST);
    add(center, BorderLayout.CENTER);

    display2.add("Your ActionEvents displayed HERE");
    add(display2, BorderLayout.SOUTH);
    eventNo = 1;
  }

  /************************************************************************************************************/
  /** Action event to change text of the textfield and area
  */
  public void actionPerformed(ActionEvent evt) {
    Object source = evt.getSource();
    if(source == append) {
      area.append(OOMPA);
    }
    else if(source == insert) {
      area.insert(OOMPA, area.getCaretPosition());
    }
    else if(source == replace) {
      area.replaceRange(OOMPA, area.getSelectionStart(), area.getSelectionEnd());
    }
    else if(source == clearArea) {
      area.setText(WELCOME);
    }
    else if(source == clearField) {
      field.setText(OOMPA+count);
      count++;
    }
  }

  /************************************************************************************************************/
  /** text event to change text of the textfield and area
  */
  public void textValueChanged(TextEvent evt) {
    if(display2.getItemCount()>20){
      display2.removeAll();
    }
    display2.add(displayTextEventShortcut(evt), 0);
  }

  /************************************************************************************************************/
  /** CollectsEvent interface display messages : do nothing, we only display our own messages
  */
  public void displayMessage(String[] messagestrings) {
    if(display2.getItemCount()>20) {
      display2.removeAll();
    }
    for(int i=messagestrings.length-1; i>=0; i--) {
      display2.add(messagestrings[i],0);
    }
  }

  public String getHelpText() {
    return "The aim: test the throwing and the correct layout of TextEvents:\n\n"+
           "The test: The upper part of the screen consists out of a TextArea and a textField on the left, a series of buttons on the right"+
           " The lower part shows a list that displays all the textEvents thrown by the field and the area\n"+
           "\nItems to test:\n"+
           "- Event generating and catching: both the TextArea aas well as the TextField should throw a TextEvent"+
           " every time you change the text either by typing in it using the keyboard, or by using one of the buttons on the right.\n"+
           "- Correct event data: The text event only has two items to check: the source (that must be either <TextArea>  or <TextField>"+
           "  and the event ID which must be 900 = <TEXT_VALUE_CHANGED>\n"+
           "\n Current items\n -------------\n"+
           "As Wonka doesn't support keyboard events yet, the only way TextEvents can be thrown is through changing the text using the"+
           " buttons on the right:\n"+
           "=> button [append text]: appends the text "+OOMPA+" to the end of the text area.\n"+
           "=> button [insert text]: inserts the text "+OOMPA+" into the text area at cursoe position.\n"+
           "=> button [replace text]: replaces the selected text of the text area by the line"+OOMPA+
           "\n=> button [Clear area]: sets the text of the text area back to "+WELCOME+
           "\n=> button [Clear field]: sets the text of the text field to "+OOMPA+" + i where i is an increasing digit";
  }

  /**
  * inner class TextArea with name
  */
  class NamedTextArea extends TextArea{
    private String name;

    public NamedTextArea(String name, String text){
      super(text,5,30,TextArea.SCROLLBARS_VERTICAL_ONLY);
      this.name = name;
    }

    public String toString() {
      return name;
    }
  }

  /**
  * inner class TextField with name
  */
  class NamedTextField extends TextField{
    private String name;

    public NamedTextField(String name, String text){
      super(text);
      this.name = name;
    }

    public String toString() {
      return name;
    }
  }

  public String displayTextEventShortcut(TextEvent evt) {
    String line = "(event no "+eventNo+")";
    eventNo++;

    Object source = evt.getSource();
    if(source==null){
      line += "Source() == NULL ";
    }
    else {
      line += "Source() = "+source;
    }
    int id = evt.getID();
    if(id==TextEvent.TEXT_VALUE_CHANGED) {
      line+= " TEXT_VALUE_CHANGED ("+id+")";
    }
    else {
      line+= " unknown Id ("+id+")";
    }
    return line;
  }

}
