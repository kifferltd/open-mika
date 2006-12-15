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

// Author: J. Vandeneede
// Created: 2001/03/13

package com.acunia.wonka.test.awt.TextArea;

import com.acunia.wonka.test.awt.*;
import java.awt.*;
import java.awt.event.*;

public class TextAreaBothFixed extends VisualTestImpl  implements VisualTest, ActionListener, TextListener {
  private TextField TF;
  private TextArea RTA;
	private Button toField;
	private Button toArea;
	private Button reset;
  private String areaText = new String(
   	"I) Here Comes Charly\n"
  	 +"====================\n\n"
  	 +"These two very old people are the father and mother of Mr Bucket. Their names are Grandpa Joe and Grandma Josephine.\n"
  	 +"And these two very old people are the father and mother of Mrs Bucket. Their names are Grandpa George and Grandma Georgina.\n"
  	 +"This is Mr Bucket. This is Mrs Bucket.\n"
  	 +"Mr and Mrs Bucket have a small boy whose name is Charly Bucket.\n\n"
  	 +"This is Charly.\n"
  	 +"How d'you do? And how d'you do?. And how d'you do again? he is pleased to meet you." );
  private Font cour14=new Font("courR14", 0, 14);
  private Font helv14=new Font("helvR14", 0, 14);

  private String logString;

  	
  	
  	
/******************************************************************/
/**
*	Constructors
**/
  /** Test Engine constructor */
  public TextAreaBothFixed() {this(TextArea.SCROLLBARS_BOTH, true);	}

  /** stand-alone constructor with startup options on initial type of scrollbar and fixed/nonfixed font*/
  public TextAreaBothFixed(int scrollbarmode, boolean fixed)   {
    //initialise VisualTest
    super();
    setLayout(new BorderLayout());
    setBackground(new Color(200,200,120));
    setForeground(new Color(90,90,180));
    RTA= new TextArea(areaText, 5, 20,scrollbarmode);
    RTA.setFont((fixed)?cour14:helv14);
		
    add(RTA,BorderLayout.CENTER);
    TF = new TextField();
    add(TF,BorderLayout.SOUTH);

    reset = new Button("RESET");
    reset.addActionListener(this);
    add(reset, BorderLayout.NORTH);
    toField = new Button("DOWN");
    toField.addActionListener(this);
    add(toField, BorderLayout.EAST);
    toArea = new Button(" UP ");
    toArea.addActionListener(this);
    add(toArea, BorderLayout.WEST);

    // text listener
    RTA.addTextListener(this);
    //if stand-alone: size and show as separate Panel
    setSize(399, 199);
    show();
  }


/******************************************************************/
/**
*	action listener from up-down buttons
**/
   public void actionPerformed(ActionEvent e)
   {
   	String currenttext = RTA.getSelectedText();
 		int pos = RTA.getCaretPosition();
 		int start = RTA.getSelectionStart();
 		int stop = RTA.getSelectionEnd();
 		String fieldtext = TF.getText();
    if(e.getSource() == reset) {
   	  //TextArea to textField	
  		RTA.setText(areaText);
   	}
    else if(e.getSource() == toField) {
   	  //TextArea to textField	
  		TF.setText(currenttext);
   	}
   	else if(start == stop) {
   	  //fiels to area, no selection: insert area text   	
   		RTA.insert(fieldtext,pos);    																													
   	}
   	else {
   	  //replace selected text by field text
   		RTA.replaceRange(fieldtext,start,stop);
   	}
   }


/******************************************************************/
/**
*	text listener from textfield
**/
public void textValueChanged(TextEvent e) {
  System.out.println(e.toString());
}
/******************************************************************/
/**
*	Panel, title, help text, log
**/
  public java.awt.Panel getPanel() {
    return this;
  }

  public String getTitle(){
    return "TextAreaTest";
  }

  public String getHelpText(){
    String help =  "Displays a text area with horizontal and vertical bars, fixed width font (Courier 14 pt)\n"+
    "Items to test: \n"+
    " => Scrolling along horizontal scrollbars:\n"+
    "    -  pressing the arrow boxes should advance one character, keeping the boxes pressed should continue moving the bar\n"+
    "    -  pressing the spaces between boxes and scrollbox should advance one screen width, keeping the mouse down should continue moving the bar\n"+
    "    -  the scrollbox can be moved by clicking and dragging it\n"+
    " also test that the text moves along with the scrollbox\n"+
    " => Scrolling along vertical scrollbars:\n"+
    "    -  pressing the arrow boxes should advance one line, keeping the boxes pressed should continue moving the bar\n"+
    "    -  pressing the spaces between boxes and scrollbox should advance one page, keeping the mouse down should continue moving the bar\n"+
    "    -  the scrollbox can be moved by clicking and dragging it\n"+
    " also test that the text moves along with the scrollbox\n"+
    " => Scrolling by text dragging\n"+
    "   - selecting and dragging over the right border of the text area should move the scrollbar right. The scrolling can be stopped by moving left again\n"+
    "   - selecting and dragging over the left border of the text area should move the scrollbar right. The scrolling can be stopped by moving right again\n"+
    "   - selecting and dragging over the upper border of the text area should move the scrollbar right. The scrolling can be stopped by moving down again\n"+
    "   - selecting and dragging over the lower border of the text area should move the scrollbar right. The scrolling can be stopped by moving up again\n"+
    " also test that the scrollboxes move along with the text\n"+
    " => Text selection\n"+
    "   - Dragging over a text should select that text. Pressing the <DOWN> button should copy that text into the lower TextField\n"+
    " => Text insert and replace\n"+
    "   - Pressing the <UP> button should insert the contents of the TextField into the text area at cursor position\n"+
    "   - If a part of the TextArea textis selected, pressing the button should replace that text with the contents of the TexField\n";

    return help;
  }

       	
  public void log(java.awt.Panel p, java.io.Writer w)throws java.io.IOException {
    w.write(logString);
    logString = "";
  }

  public void start(java.awt.Panel p, boolean b){}
  public void stop(java.awt.Panel p){}


/**********************************************************************************************************************************/
/**
*	if stand-alone, get the scrollbar mode desired and call the constructor for it
**/

  static public void main (String[] args) {
    int preference = java.awt.TextArea.SCROLLBARS_BOTH;
    if(args.length >0) {
      if(args[0].startsWith("n") || args[0].startsWith("N"))
      preference = java.awt.TextArea.SCROLLBARS_NONE;
      else if(args[0].startsWith("h") || args[0].startsWith("H"))
      preference = java.awt.TextArea.SCROLLBARS_HORIZONTAL_ONLY;
      else if(args[0].startsWith("v") || args[0].startsWith("V"))
      preference = java.awt.TextArea.SCROLLBARS_VERTICAL_ONLY;
    }
         		
    boolean fixedwidth = true;
    if(args.length >1 )//&& args[1].equalsIgnoreCase("disabled"))
    fixedwidth = false;
    new TextAreaBothFixed( preference, fixedwidth);
  }

}


