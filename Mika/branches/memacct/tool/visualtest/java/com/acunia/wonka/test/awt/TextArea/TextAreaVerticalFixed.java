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

import java.awt.TextArea;

public class TextAreaVerticalFixed extends TextAreaBothFixed {
/**********************************************************************************************************************************/
/**
*	Constructors
**/
  /** Test Engine constructor */
  public TextAreaVerticalFixed() {
    super(TextArea.SCROLLBARS_VERTICAL_ONLY, true);	
  }

  public String getHelpText(){
    String help =  "Displays a text area with a vertical bar, fixed width font (Courier 14 pt)\n"+
    "The text is wrapped to the area's width and can be moved up and down by using the scrollbar or dragging the text selection\n"+
    "Items to test: \n"+
    " => Horizontal text wrap:\n"+
    "    -  The text should be wrapped along the textarea's width\n"+
    " => Scrolling along vertical scrollbars:\n"+
    "    -  pressing the arrow boxes should advance one line, keeping the boxes pressed should continue moving the bar\n"+
    "    -  pressing the spaces between boxes and scrollbox should advance one page, keeping the mouse down should continue moving the bar\n"+
    "    -  the scrollbox can be moved by clicking and dragging it\n"+
    " also test that the text moves along with the scrollbox\n"+
    " => Scrolling by text dragging\n"+
    "   - selecting and dragging over the upper border of the text area should move the scrollbar right. The scrolling can be stopped by moving down again\n"+
    "   - selecting and dragging over the lower border of the text area should move the scrollbar right. The scrolling can be stopped by moving up again\n"+
    " also test if the vertical scrollbar moves along with the text\n"+
    " => Text selection\n"+
    "   - Dragging over a text should select that text. Pressing the <DOWN> button should copy that text into the lower TextField\n"+
    " => Text insert and replace\n"+
    "   - Pressing the <UP> button should insert the contents of the TextField into the text area at cursor position\n"+
    "   - If a part of the TextArea textis selected, pressing the button should replace that text with the contents of the TexField\n";

    return help;
  }
}


