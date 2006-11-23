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

public class TextAreaTest extends VisualTestImpl  implements VisualTest , ActionListener {

  private Button[] scrollbars;
  	
  private Button fixwidth;
  private Button varwidth;
  private boolean isFixedWidth;
  	
  private Button up;
  private Button down;
  	
  private Panel areaPanel;
  private CardLayout areaCardLayout;
  private TextArea[] areas;
  private int currentArea;
  	
  private TextField theField;
  	
  private String[] labels={"BOTH BARS","VERTICAL","HORIZONTAL","NO BARS"};
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

  	
  	
  	
/**********************************************************************************************************************************/
/**
*	Constructors
**/
  /** Test Engine constructor */
  public TextAreaTest() {this(TextArea.SCROLLBARS_BOTH, true);	}

  /** stand-alone constructor with startup options on initial type of scrollbar and fixed/nonfixed font*/
  public TextAreaTest(int scrollbarmode, boolean fixed)   {
    //initialise VisualTest
    super();
    setBackground(new Color(255,220,150));
    setForeground(new Color(64,64,192));
    setLayout(new BorderLayout());

    if(scrollbarmode<0) {
      currentArea=0;
    }
    else if(scrollbarmode>3) {
      currentArea=3;
    }
    else {
      currentArea=scrollbarmode;
    }
    isFixedWidth=fixed;

    // initialise elements and build screen
    //top row
    Panel buttonrow = new Panel(new GridLayout(1,6));
    //scrollbar buttons
    scrollbars = new Button[4];
    for (int i=0; i<4;i++) {
      scrollbars[i]=new Button(labels[i]); //"mode=="+i);
      scrollbars[i].addActionListener(this);
      buttonrow.add(scrollbars[i]);
    }
    //fixed-width / variable width buttons
    fixwidth = new Button("Courier");
    fixwidth.addActionListener(this);
    buttonrow.add(fixwidth);
    varwidth = new Button("Helvetica");			
    varwidth.addActionListener(this);
    buttonrow.add(varwidth);
    add(buttonrow, BorderLayout.NORTH);

    // central card layout and its TextAreas
    areaCardLayout = new CardLayout();
    areaPanel= new Panel(areaCardLayout);
    //text areas
    areas=new TextArea[4];
    for (int i=0; i<4;i++) {
      areas[i]=new TextArea("",10,20,i);
      areaPanel.add(areas[i],labels[i]);
    }
    // current area
    currentArea=0;
    areas[currentArea].setText(areaText);
    areas[currentArea].setFont((isFixedWidth)?cour14:helv14);
    areaCardLayout.show(areaPanel,labels[currentArea]);
    add(areaPanel,BorderLayout.CENTER);

    //up/down buttons
    down = new Button("DOWN");
    down.addActionListener(this);
    add(down, BorderLayout.EAST);
    up = new Button("UP");
    up.addActionListener(this);
    add(up, BorderLayout.WEST);

    // lowermost TextField
    theField = new TextField();
    add(theField,BorderLayout.SOUTH);
    			
    // logging string
    logString = new String();
    //if stand-alone: size and show as separate Panel
    setSize(399, 199);
    show();
  }

/**********************************************************************************************************************************/
/**
*	When textfield enter pressed: replace selected area by new text (or insert new text at textarea carret wwhen no selection made)
**/
  public void actionPerformed(ActionEvent e) {
    //check if the button clicked is one of the four TextArea buttons
    int newarea=-1;
    for(int i=0;i<4&&newarea<0;i++) 		{
      if(e.getSource()==scrollbars[i])
      newarea=i;
    }
    //if so, copy the data from one textarea into another
    int cursor = areas[currentArea].getCaretPosition();
    if(newarea>=0 && newarea!=currentArea) {
      // data from old area
      logString+="\n\n       ============================";
      String text = areas[currentArea].getText();
      int start = areas[currentArea].getSelectionStart();
      int stop = areas[currentArea].getSelectionEnd();
      logString +=".......old TextArea("+currentArea+"):found positions start="+start+", stop="+stop+", cursor="+cursor;
      // copy into new area
      logString +=".......new TextArea("+newarea+"):setting data ";
      areas[newarea].setFont((isFixedWidth)?cour14:helv14);
      areas[newarea].setText(text);
      areas[newarea].setSelectionStart(start);
      areas[newarea].setSelectionEnd(stop);
      areas[newarea].setCaretPosition(cursor);
      areaCardLayout.show(areaPanel,labels[newarea]);
      currentArea=newarea;
      logString +="       =======  TextArea("+newarea+"):  d o n e    ====== \n";
    }
    else if(e.getSource() == fixwidth) { //Desired button is fixedwidth char button
      areas[currentArea].setFont(cour14);
      isFixedWidth=true;
    }
    else if(e.getSource() == varwidth) { //Desired button is variable width char button
      areas[currentArea].setFont(helv14);
      isFixedWidth=false;
    }
    else if(e.getSource() == up) { //'DOWN' button copies selection from TextArea to textField	
     areas[currentArea].setCaretPosition((cursor>20)?cursor-20:0);
     theField.setText("From position "+cursor+" moved cursor 20 chars up ");
    }
    else if(e.getSource() == down) {
     areas[currentArea].setCaretPosition(cursor+20);
     theField.setText("From position "+cursor+" moved cursor 20 chars down ");
    }	    																													
    /*
    else if(e.getSource() == down) { //'DOWN' button copies selection from TextArea to textField	
     theField.setText(areas[currentArea].getSelectedText());
    }
    else if(areas[currentArea].getSelectionStart() == areas[currentArea].getSelectionEnd()) { //&& e.getSource() == up)
      //Up -button but no selection: insert TextField text at cursor position
      areas[currentArea].insert(theField.getText(), areas[currentArea].getCaretPosition());
    }	    																													
    else { // if(//&& e.getSource() == up && theArea.getSelectionStart() > theArea.getSelectionEnd())
      //Up -button and a selected text: replace this text from the TextArea by the TextField text
      areas[currentArea].replaceRange(theField.getText(), areas[currentArea].getSelectionStart(), areas[currentArea].getSelectionEnd() );
    }
    */
  }   	
/**********************************************************************************************************************************/
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
    String help =  "All-in-one test for java.awt.TextArea, it displays a series of buttons, a textfield and a central textarea. "+
    "The textarea displays a multi-line text.\n";
    help += "You can do the following tests:\n";

    help += "\n=> Changing the textArea layout using the upper row buttons : \n";
    help += "   A textarea can have a horizontal scrollbar, a vertical scrollbar, both or none of them."+
    " All of that defines their visibility and the way it reacts on certain commande. furthermore the text can have different fonts\n";
    help += "   The buttons allow you to switch between the differnt textarea types and choose either a fixed or variable width font:\n";
    help += "   - (BOTH BARS) switches to a TextArea with horizontal and vertical bars (Text scrolls horizontally and vertically)\n";
    help += "   - (VERTICAL) switches to a TextArea with only one vertical bar (Text is wrapped to viewport and scrolls vertically)\n";
    help += "   - (HORIZONTAL) switches to a TextArea with only one horizontal bar "+
                         "(Text scrolls horizontally, can move vertically through mouse scrolling)\n";
    help += "   - (NO BARS) switches to a TextArea without bars. The text is wrapped to the viewport width. "+
                         "It can move vertically through mouse scrolling\n";
    help += "   - (Courier) Displays the text area text in a 14pt Courier font (Fixed width)\n";
    help += "   - (Helvetica) Displays the text area text in a 14pt Helvetica font (Fixed width)\n";

    help += "\n=> Textarea scrolling and selecting : \n";
    help += "   You can use the scrollbars to scroll the text area of a textarea. Furthermore you can select a part of the displayed text "+
    "and change the visible area by dragging a selection of text across the area border\n";
    help += "   - Scrollbar scrolling: by clicking or moving the scrollbars, you can move the visible area of the text\n";
    help += "   - Selecting text: by clicking inside the text area, you place the 'text cursor' between the two closest characters "+
    "of the text. By dragging the mouse over a part of the text, you select this part.\n";
    help += "   - horizontal mouse scrolling: by dragging the mouse from inside the text area over the bottom of the area, the text in the "+
    " area moves up to keep the selection in the viewport, likewise when dragging the mouse over the top of the area, the text moves down "+
    " for textareas without a vertical bar, this is the only means of viewing the text above or under the current viewport\n";
    help += "   - vertical mouse scrolling: If the text is not line-wrapped (a horizontal scrollbar is present), dragging the mouse "+
    " over the left border moves the text right and dragging the mouse over the right border moves the text left, similar to the "+
    " horizontal mouse scrolling\n";

    help += "\n=> Inserting and replacing text: \n";
    help += "   - the button (DOWN) displays the currently selected text in the TextField on the bottom of the screen. "+
    "(if no selection is made, pressing the button clears the textfield)\n";
    help += "   - if a selection is made, the button (UP) replaces the selected text with the contents of the TextField below "+
    "(if the textfield is empty, the selection is deleted). If no selection is made, pressing the button inserts the contents "+
    "of the textfiels into the textarea's cursor position\n";

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
  	boolean fixed=true;
  	if(args.length >0) {
  		if(args[0].startsWith("n") || args[0].startsWith("N"))
  		preference = java.awt.TextArea.SCROLLBARS_NONE;
  		else if(args[0].startsWith("h") || args[0].startsWith("H"))
  		preference = java.awt.TextArea.SCROLLBARS_HORIZONTAL_ONLY;
  		else if(args[0].startsWith("v") || args[0].startsWith("V"))
  		preference = java.awt.TextArea.SCROLLBARS_VERTICAL_ONLY;
 		}
 		if(args.length >1) {
  		if(!args[1].startsWith("f") && !args[1].startsWith("F")) {
  			fixed = false;
  		}
 		}
  	new TextAreaTest(preference,fixed);
  }
}


