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

import com.acunia.wonka.rudolph.peers.*;

public class TextArea extends TextComponent  {

  /*
  ** Variables
  */
  
  //definitions for scrollbar visibility
  public static final int SCROLLBARS_VERTICAL_ONLY = 1;
  public static final int SCROLLBARS_HORIZONTAL_ONLY = 2;
  public static final int SCROLLBARS_NONE = 3;
  public static final int SCROLLBARS_BOTH = 0;

  private int scrollbarVisible;

  //preferred, minimum dimensions
  private int preferredChars; //line width in chars
  private int preferredLines; // text height in lines
    
  /*
  ** Constructors
  */

  public TextArea() {
    this("", RudolphTextAreaPeer.DEFAULTTEXTLINES, RudolphTextAreaPeer.DEFAULTTEXTCHARS, SCROLLBARS_BOTH);
  }  
  public TextArea(String text) {
    this(text, RudolphTextAreaPeer.DEFAULTTEXTLINES, RudolphTextAreaPeer.DEFAULTTEXTCHARS, SCROLLBARS_BOTH);  
  }
    
  public TextArea(int lines, int chars) {
    this("", lines, chars, SCROLLBARS_BOTH);  
  }
    
  public TextArea(String text, int lines, int chars) {
    this(text, lines, chars, SCROLLBARS_BOTH);
  }

  /*
  ** General constructor TextArea(desired text, minimum lines, minimum colums, visibility code)
  */
  
  public TextArea(String starttext, int lines, int chars, int scrollbarVis) {    
    super();

    scrollbarVisible = scrollbarVis;

    text = starttext;
    setText(text);
    editable = true; // by default

    //viewport lines chars
    preferredChars=chars;
    preferredLines=lines;  
    
    //validate and set visible
    validate();
    setVisible(true);
  }

  public void addNotify() {
    if(peer == null) {
      peer = getToolkit().createTextArea(this);
    }

    if (notified == false) {
      super.addNotify();
    }
  }

  /*
  ** Row and Column methods
  */

  public void setColumns(int columns) {
    preferredChars = columns;  
  }
  
  public int getColumns() {  
    return preferredChars;  
  }
  
  public void setRows(int rows) {  
    preferredLines = rows;  
  }
  
  public int getRows() {  
    return preferredLines;  
  }

  /*
  **  Scrollbar visibility (calculate out of hScroll and vScrollVisible)
  */

  public int getScrollbarVisibility() {
    return scrollbarVisible;
  }
  
  /*
  ** preferred and visible dimensions out of preferred width and lines
  */

  public Dimension getMinimumSize() {
    return getPreferredSize(preferredLines, preferredChars);
  }

  public Dimension getPreferredSize() {
    return getPreferredSize(preferredLines, preferredChars);
    //return getPreferredSize(textPainter.getMaximumLines(), preferredChars);
  }

  public Dimension getMinimumSize(int lines, int chars) {
    return ((TextAreaPeer)peer).getMinimumSize(lines, chars);
  }

  public Dimension getPreferredSize(int lines, int chars) {
    return ((TextAreaPeer)peer).getPreferredSize(lines, chars);
  }  

  /*
  ** text update methods
  */

  /*
  ** Append desired String to end of text and show this new text in TextArea window
  ** (This also fires a TextEvent to the textListener to indicate the test has changed)
  */

  public synchronized void append(String newtext) {
    text = getText();
    insert(newtext, text.length());
    text = getText();
    setCaretPosition(text.length());
  }
    
  public void insert(String newtext, int pos) {
    ((TextAreaPeer)peer).insert(newtext, pos);
    text = getText();
  }  
  
  public void replaceRange(String newtext, int start, int stop) {
    ((TextAreaPeer)peer).replaceRange(newtext, start, stop);
    text = getText();
  }

  /*
  ** debugging
  */

  public String toString() {
    String display = getClass().getName();
    Rectangle r = getBounds();
    display += " ("+r.x+", "+r.y+", "+r.width+", "+r.height+") lines("+preferredChars+", "+preferredLines+")text=["+text;
    display += (editable)?"] editable scrollbars = ":"] non-editable scrollbars = ";
    switch(scrollbarVisible) {
      case SCROLLBARS_BOTH: 
        display += "SCROLLBARS_BOTH";
        break;
      case SCROLLBARS_HORIZONTAL_ONLY:
        display += "SCROLLBARS_HORIZONTAL_ONLY";
        break;
      case SCROLLBARS_VERTICAL_ONLY:
        display += "SCROLLBARS_VERTICAL_ONLY";
        break;
      case SCROLLBARS_NONE:
        display += "SCROLLBARS_NONE";
        break;
      default: 
    }
    return display;
  }

  protected String paramString() {
    String display = getClass().getName();
    Rectangle r = getBounds();
    display += " ("+r.x+","+r.y+","+r.width+","+r.height+") ["+text+"] "+preferredLines+"lines ";
    display += (editable)?" editable scrollbars=":" non-editable scrollbars=";
    switch(scrollbarVisible) {
      case SCROLLBARS_BOTH: 
        display += "SCROLLBARS_BOTH";
        break;
      case SCROLLBARS_HORIZONTAL_ONLY:
        display += "SCROLLBARS_HORIZONTAL_ONLY";
        break;
      case SCROLLBARS_VERTICAL_ONLY:
        display += "SCROLLBARS_VERTICAL_ONLY";
        break;
      case SCROLLBARS_NONE:
        display += "SCROLLBARS_NONE";
        break;
      default: 
    }
    return display;
  }
  
}

