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

public class TextAreaNoBarsVariable extends TextAreaBothFixed {
/**********************************************************************************************************************************/
/**
*	Constructors
**/
  /** Test Engine constructor */
  public TextAreaNoBarsVariable() {
    super(TextArea.SCROLLBARS_NONE,false);
  }

  public String getHelpText(){
    String help =  "Displays a text area without bars, variable width font (Helvetica 14 pt)\n"+
    "The text is wrapped to the area's width and can be moved up and down by dragging the text selection\n"+
    "Items to test: \n"+
    " => Horizontal text wrap:\n"+
    "    -  The text should be wrapped along the textarea's width\n"+
    " => Scrolling by text dragging\n"+
    "   - selecting and dragging over the upper border of the text area should move the scrollbar right. The scrolling can be stopped by moving down again\n"+
    "   - selecting and dragging over the lower border of the text area should move the scrollbar right. The scrolling can be stopped by moving up again\n"+
    " => Text selection\n"+
    "   - Dragging over a text should select that text. Pressing the <DOWN> button should copy that text into the lower TextField\n"+
    " => Text insert and replace\n"+
    "   - Pressing the <UP> button should insert the contents of the TextField into the text area at cursor position\n"+
    "   - If a part of the TextArea textis selected, pressing the button should replace that text with the contents of the TexField\n";

    return help;
  }
}


