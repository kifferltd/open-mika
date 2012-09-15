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

public class DefaultTextArea extends DefaultTextComponent implements TextAreaPeer, MouseListener, MouseMotionListener, FocusListener {
  
  // border scrolling area
  private final static int TEXTSCROLL_STOP  = -1;
  private final static int TEXTSCROLL_UP    = 1;
  private final static int TEXTSCROLL_DOWN  = 2;
  private final static int TEXTSCROLL_LEFT  = 3;
  private final static int TEXTSCROLL_RIGHT = 4;

  //Painters
  private HAreaScrollPainter hScroll;
  private VAreaScrollPainter vScroll;
  private boolean vScrollVisible;
  private TextAreaPainter textPainter;

  //colors
  private Color[] textAreaColors;
  //screen dimensions
  private Dimension textScreen;
  private Dimension totalScreen;
    
  //last mouse position and mouse event runner
  private Point lastMousePosition;
  final static ScrollRunner mouseEventThread = new ScrollRunner();

  // painting buffer
  private Image backgroundImage;
  private Graphics backgroundGraphics;
  // selective painting flags
  private boolean repaintText;
  private boolean repaintHScroll;
  private boolean repaintVScroll;

  //protected Rectangle cursorData; //PointData cursorData;
  protected int cursorLine;
  protected int cursorBufferOffset;
  protected int cursorHorizontalOffset;
  //protected Rectangle selStartData;//PointData selStartData;
  protected int startLine;
  protected int startBufferOffset;
  protected int startHorizontalOffset;
  //protected Rectangle selStopData;//PointData selStopData;
  protected int stopLine;
  protected int stopBufferOffset;
  protected int stopHorizontalOffset;

  private boolean initialized = false;

  private int[] tempData; //private Rectangle tempData; //PointData tempData;

  public DefaultTextArea(TextArea textArea) {
    super(textArea);
  }

  private synchronized void init() {
    initialized = true;

    position = 0;
    cursorLine = -1;
    cursorBufferOffset = -1;
    cursorHorizontalOffset = -1;

    selectionStart = 0;
    startLine = -1;
    startBufferOffset = -1;
    startHorizontalOffset = -1;

    selectionStop = 0;
    stopLine = -1;
    stopBufferOffset = -1;
    stopHorizontalOffset = -1;

    //tempData= new Rectangle(); //new PointData();
    tempData=new int[4];   // see notes on the TextAreaPainter.setScreenPosition(int[]) format

    //colors & font
    textAreaColors = RudolphPeer.getBarColors();
    // previously super.setFont(RudolphTextAreaPeer.DEFAULT_FONT);

    //initialise the dimensions
    textScreen = new Dimension();
    totalScreen = new Dimension();         
        
    //vertical scrollbar (we ALWAYS use one to enable vertical text scrolling
    vScroll = new VAreaScrollPainter();     
    
    int scrollbarVis = ((TextArea)component).getScrollbarVisibility();
    
    //According to display type, assign text painter, horizontal scrollbar and vertical scrollbar visibility
    if(scrollbarVis == TextArea.SCROLLBARS_NONE) { //no scrollbars (vertical bar is present but not visible)
      hScroll = null;
      vScrollVisible = false;
      textPainter = TextAreaPainter.getNewPainter(((TextArea)component).getFont(),text,textAreaColors,textScreen,false);
    }
    else if(scrollbarVis == TextArea.SCROLLBARS_HORIZONTAL_ONLY) { // only horizontal scrollbar (vertical is present but not visible)
      hScroll = new HAreaScrollPainter();
      hScroll.setBarColors(textAreaColors);
      vScrollVisible = false;
      textPainter = TextAreaPainter.getNewPainter(((TextArea)component).getFont(),text,textAreaColors,textScreen,true);
      hScroll.setLineStep(textPainter.getMaxAdvance());
    }
    else if(scrollbarVis == TextArea.SCROLLBARS_VERTICAL_ONLY) { // only vertical scrollbar
      hScroll = null;
      vScrollVisible = true;
      vScroll.setBarColors(textAreaColors);
      textPainter = TextAreaPainter.getNewPainter(((TextArea)component).getFont(),text,textAreaColors,textScreen,false);
    }
    else { // if(scrollbarVis == SCROLLBARS_BOTH) // if not defined otherwise, the textarea ALWAYS shows horizontal AND vertical bars
      hScroll = new HAreaScrollPainter();
      hScroll.setBarColors(textAreaColors);
      vScrollVisible = true;
      vScroll.setBarColors(textAreaColors);
      textPainter = TextAreaPainter.getNewPainter(((TextArea)component).getFont(),text,textAreaColors,textScreen,true);
      hScroll.setLineStep(textPainter.getMaxAdvance());
    }          
  
    //add mouse listeners
    ((TextArea)component).addMouseListener(this);
    ((TextArea)component).addMouseMotionListener(this);
    //If version with keyboard, also add key listener
    //addKeyListener(this);        
    //last mouse click point
    lastMousePosition = new Point(0,0);

    // painting buffer
    backgroundImage = null;
    backgroundGraphics = null;
    //no repainting untill screen calculations and screen buffer
    repaintText = false;
    repaintHScroll = false;
    repaintVScroll = false;
	
	((TextArea) component).addFocusListener(this);
  }

  public Dimension getMinimumSize(int rows, int cols) {
    if(!initialized) init(); 
    return getPreferredSize(rows, cols);
  }
  
  public Dimension getPreferredSize(int rows, int cols) {
    if(!initialized) init(); 
    Dimension size = textPainter.getScreenSize(cols, rows);
    if(hScroll != null) {
      size.height +=RudolphScrollbarPeer.HSCROLL_HEIGHT;
    }
    if(vScrollVisible) {
      size.width += RudolphScrollbarPeer.VSCROLL_WIDTH;
    }
    return size;
  }
  
  /*
  ** Insert desired String to into the current text at the desired position and show this new text in TextArea window
  ** (This also fires a TextEvent to the textListener to indicate the test has changed)
  ** @note: Java 1.2 specifications remains wonderfully vague about what happens when the insert position is out of range.
  ** @note: here we'll throw an IllegalArgumentexception with some diagnostics
  */
  
  public synchronized void insert(String newtext, int pos) {
    if(!initialized) init(); 
    //security on position
    if(updatePosition(pos)) {
      //okay, collapse selection to position
      selectionStart = position; //updateStart(pos);
      selectionStop = position;  //updateStop(pos);
      // (when Selectionstart == selectionStop, startLine, stopLine and the likes are never asked, so never bother assigning them)    

      // insert new text (as all changes to the text happen AFTER pos, its value remains unchanged)
      text = new String(new StringBuffer(text).insert(pos,newtext));
      // text to painter and recalculate sizes and scrollbars
      textPainter.setImage(text);
      if(hScroll != null) {
        hScroll.setBarRange(textPainter.getMaximumWidth());
      }
      vScroll.setBarRange(textPainter.getMaximumLines());

      //tell the text listeners
      fireTextEvent();
      // show the changes
      repaintText = true;
      repaintHScroll = true;
      repaintVScroll = true;
      paint(getGraphics());
    }
    else {  // not in range
//      throw new IllegalArgumentException("TextArea.insert(): position "+pos+" not in range of current text ("+text.length()+" chars)");
    }
  }  
    
  /*
  ** replace the desired section of the current text with the given String and show this new text in TextArea window
  ** (This also fires a TextEvent to the textListener to indicate the test has changed)
  ** @note: Java 1.2 specifications remains wonderfully vague about what happens when the insert position is out of range.
  ** @note: or the start position is bigger then the end position. here we'll throw an IllegalArgumentexception with some diagnostics
  */

  public synchronized void replaceRange(String newtext, int start, int stop) {
    if(!initialized) init(); 
    //boundaries check
    if(start>stop) {
//      throw new IllegalArgumentException("TextArea.ReplaceRange(): end position "+stop+"  comes before start position "+start);
      return;
    }
    else if(start<0 || start>text.length() || stop<0 || stop>text.length()) {
//      throw new IllegalArgumentException("TextArea.ReplaceRange(): selection("+start+","+stop+") not in range of current text (0, "+text.length()+")");
      return;
    }
    //else
      //set new text
      text = new String(new StringBuffer(text).replace(start,stop,newtext));
      // text to painter and recalculate sizes and scrollbars
      textPainter.setImage(text);
      if(hScroll != null) {
        hScroll.setBarRange(textPainter.getMaximumWidth());
      }
      vScroll.setBarRange(textPainter.getMaximumLines());

      // set new cursor to end of new text, collapse selection to cursor
      updatePosition(start+newtext.length());
      selectionStart = position; //updateStart(pos);
      selectionStop = position;  //updateStop(pos);

      //tell the text listeners
      fireTextEvent();
      // show the changes
      repaintText = true;
      repaintHScroll = true;
      repaintVScroll = true;
      paint(getGraphics());
  }

  /*
  ** Deprecated
  */

  public void insertText(String text, int pos) {
  }
  
  public Dimension minimumSize(int rows, int cols) {
    return null;
  }
  
  public Dimension preferredSize(int rows, int cols) {
    return null;
  }
  
  public void replaceText(String str, int start, int end) {
  }
  
  /*
  ** KeyEvents
  */
  
  public void keyPressed(KeyEvent evt) {
	boolean imagechanged = false;

    switch(evt.getKeyCode()) {
      // key left: move carret left and set selection to carer
      case KeyEvent.VK_LEFT:
        if(evt.isShiftDown()) {
          // we want a selection
          textPainter.setScreenPosition(position-1, tempData);
          imagechanged = setNewSelection(tempData);
        }
        else {
          // cursor to position-1, collapse selection to cursor
          imagechanged = updatePosition(position-1);
          selectionStart = position;
          selectionStop = position;
        }
        break;

      // key right: move carret right and set selection to caret
      case KeyEvent.VK_RIGHT:
        if(evt.isShiftDown()) {
          // we want a selection
          textPainter.setScreenPosition(position+1, tempData);
          imagechanged = setNewSelection(tempData);
        }
        else {
          // cursor to position-1, collapse selection to cursor
          imagechanged = updatePosition(position+1);
          selectionStart = position;
          selectionStop = position;
        }
        break;

      // key home: move carret completely left
      case KeyEvent.VK_HOME:
        if(evt.isShiftDown()) {
          textPainter.setScreenPositionLine(cursorLine, 0, tempData);
          imagechanged = setNewSelection(tempData);
        }
        else {
          textPainter.setScreenPositionLine(cursorLine, 0, tempData);
          imagechanged = updateCursor(tempData);
        }
        break;

      case KeyEvent.VK_END:
        if(evt.isShiftDown()) {
          textPainter.setScreenPositionLine(cursorLine, -1, tempData);
          imagechanged = setNewSelection(tempData);
        }
        else {
          textPainter.setScreenPositionLine(cursorLine, -1, tempData);
          imagechanged = updateCursor(tempData);
        }
        break;

      case KeyEvent.VK_UP:
        if(evt.isShiftDown()) {
          textPainter.setScreenPositionLine(cursorLine-1, cursorHorizontalOffset+1, tempData);
          imagechanged = setNewSelection(tempData);
        }
        else {
          textPainter.setScreenPositionLine(cursorLine-1, cursorHorizontalOffset+1, tempData);
          imagechanged = updateCursor(tempData);
        }
        break;

      case KeyEvent.VK_DOWN:
        if(evt.isShiftDown()) {
          textPainter.setScreenPositionLine(cursorLine+1, cursorHorizontalOffset+1, tempData);
          imagechanged = setNewSelection(tempData);
        }
        else {
          textPainter.setScreenPositionLine(cursorLine+1, cursorHorizontalOffset+1, tempData);
          imagechanged = updateCursor(tempData);
        }
        break;

      case KeyEvent.VK_PAGE_UP:
        if(evt.isShiftDown()) {
          textPainter.setScreenPositionLine(cursorLine-textPainter.getViewportLines(), cursorHorizontalOffset+1, tempData);
          imagechanged = setNewSelection(tempData);
        }
        else {
          textPainter.setScreenPositionLine(cursorLine-textPainter.getViewportLines(), cursorHorizontalOffset+1, tempData);
          imagechanged = updateCursor(tempData);
        }
        break;

      case KeyEvent.VK_PAGE_DOWN:
        if(evt.isShiftDown()) {
          textPainter.setScreenPositionLine(cursorLine+textPainter.getViewportLines(), cursorHorizontalOffset+1, tempData);
          imagechanged = setNewSelection(tempData);
        }
        else {
          textPainter.setScreenPositionLine(cursorLine+textPainter.getViewportLines(), cursorHorizontalOffset+1, tempData);
          imagechanged = updateCursor(tempData);
        }
        break;
	  }
	
    if(imagechanged) {
      // adjust viewport if needed
      repaintVScroll = adjustVerticalViewport(cursorLine);
      if(hScroll != null) {
        repaintHScroll = adjustHorizontalViewport(cursorHorizontalOffset);
      }
      //repaint text
      repaintText = true;
      paint(getGraphics());
    }	
  }

  public void keyReleased(KeyEvent evt) {
  }

  public void keyTyped(KeyEvent evt) {
    boolean imagechanged = false;

	wonka.vm.Etc.woempa(9, "DefaultTextArea.keyTyped: "+evt.toString());

    switch(evt.getKeyCode()) {	
      case KeyEvent.VK_BACK_SPACE:
        if(selectionStart < selectionStop ){
          //deleteSelection();
          text = new String(new StringBuffer(text).delete(selectionStart, selectionStop));
          textPainter.setImage(text);
          updatePosition(selectionStart);
          selectionStart = position;
          selectionStop = position;
          imagechanged = true;
          fireTextEvent();
        }
        else if(position>=1 && position<=text.length()) {
          text = new String(new StringBuffer(text).deleteCharAt(position-1));
          textPainter.setImage(text);
          updatePosition(position-1);
          selectionStart = position;
          selectionStop = position;
          imagechanged = true;
          fireTextEvent();
        }
		
        break;

      case KeyEvent.VK_DELETE:
        if(selectionStart < selectionStop ){
          //deleteSelection();
          text = new String(new StringBuffer(text).delete(selectionStart, selectionStop));
          textPainter.setImage(text);
          updatePosition(selectionStart);
          selectionStart = position;
          selectionStop = position;
          imagechanged = true;
          fireTextEvent();
        }
        else if(position>=0 && position<text.length()) {
          text = new String(new StringBuffer(text).deleteCharAt(position));
          textPainter.setImage(text);
          updatePosition(position);
          selectionStart = position;
          selectionStop = position;
          imagechanged = true;
          fireTextEvent();
        }
        break;

      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_CONTROL:
	  case KeyEvent.VK_TAB:
          case KeyEvent.VK_UNDEFINED:
        break;

      case KeyEvent.VK_ENTER:
      //special case: map the enter to a '/n' line break
        if(selectionStart < selectionStop ){
          StringBuffer buf =new StringBuffer(text);
          buf.delete(selectionStart, selectionStop);
          buf.insert(selectionStart, '\n' );
          text = new String(buf);
          textPainter.setImage(text);
          updatePosition(selectionStart+1);
          selectionStart = position;
          selectionStop = position;
          imagechanged = true;
          fireTextEvent();
        }
        else if(position>=0 && position<=text.length() ) {
          text = new String(new StringBuffer(text).insert(position, '\n') );
          textPainter.setImage(text);
          updatePosition(position+1);
          selectionStart = position;
          selectionStop = position;
          imagechanged = true;
          fireTextEvent();
        }

        break;

      default:
		wonka.vm.Etc.woempa(9, "DefaultTextArea.keyTyped:default char: "+evt.getKeyChar()+" position: "+position);  
		  
        if(selectionStart < selectionStop ){
          StringBuffer buf =new StringBuffer(text);
          buf.delete(selectionStart, selectionStop);
          buf.insert(selectionStart, evt.getKeyChar() );
          text = new String(buf);
          textPainter.setImage(text);
          updatePosition(selectionStart+1);
          selectionStart = position;
          selectionStop = position;
          imagechanged = true;
          fireTextEvent();
        }
        else if(position>=0 && position<=text.length() ) {
          text = new String(new StringBuffer(text).insert(position, evt.getKeyChar()) );
	  textPainter.setImage(text);
          updatePosition(position+1);
          selectionStart = position;
          selectionStop = position;
          imagechanged = true;
          fireTextEvent();
	}
    }

    if(imagechanged) {
      // adjust viewport if needed
      repaintVScroll = adjustVerticalViewport(cursorLine);
      if(hScroll != null) {
        repaintHScroll = adjustHorizontalViewport(cursorHorizontalOffset);
      }
      //repaint text
      repaintText = true;
      paint(getGraphics());
    }
  }

  /*
  ** Extends the Component's setColor function to pass the colors further to the scrollPainter
  */
  
  public void setBackground(Color c) {
    if(!initialized) init(); 
    // make new colors and tell them to the textarea and scrollbar painters
    textAreaColors = RudolphPeer.getBarColors(c, component.getForeground());
    if(hScroll != null) {
      hScroll.setBarColors(textAreaColors);
    }
    vScroll.setBarColors(textAreaColors);
    textPainter.setTextColors(textAreaColors);

    // repaint all in new colors
    repaintText = true;
    repaintHScroll = true;
    repaintVScroll = true;
    if (component.isVisible()) {
      paint(getGraphics());
    }
    super.setBackground(c); // component
  }
    
  public void setForeground(Color c) {
    // make new colors and tell them to the textarea and scrollbar painters
    textAreaColors[4] = c;
    if(hScroll != null) {
      hScroll.setBarColors(textAreaColors);
    }
    vScroll.setBarColors(textAreaColors);
    textPainter.setTextColors(textAreaColors);

    // repaint all in new colors
    repaintText = true;
    repaintHScroll = true;
    repaintVScroll = true;
    paint(getGraphics());
    super.setForeground(c); // component
  }

  /*
  ** Extends the Component's setFont function to pass the desired font towards the scrollPainter (and from there to the actual painting
  **  algorithm in the RudolphTextAreaPeer)
  ** @remark though you can specify a font, at present the text is ALWAYS drawn in RudolphTextAreaPeer.DEFAULT_FONT for more speed
  ** @remark However, the font facility is already implemented here for later use
  */

  public void setFont(Font f) {
    if(!initialized) init(); 
    // tell new font to the text painter
    textPainter = TextAreaPainter.getNewPainter(f,text,textAreaColors,textScreen,(hScroll!=null));
    // the font also changed the texts dimensions and viewports
    if(hScroll != null) {
      hScroll.setLineStep(textPainter.getMaxAdvance());
      hScroll.setScreenWidth(textScreen.width, textPainter.getViewportWidth(), textPainter.getMaximumWidth());
      textPainter.setTextOffset(hScroll.getBarPos() );
    }
    vScroll.setScreenHeight( textScreen.height, textPainter.getViewportLines(),textPainter.getMaximumLines());
    textPainter.setLineOffset(vScroll.getBarPos() );

    //also recalculate PointData for position and selection(line wrapping might hace changed their lines)
    updatePosition(position);
    updateStart(selectionStart);
    updateStop(selectionStop);

    // repaint all in new font
    repaintText = true;
    repaintHScroll = true;
    repaintVScroll = true;
    paint(getGraphics());
    super.setFont(f);
  }

  /*
  ** Paint
  */
  
  public void paint(Graphics g) {
    if(g == null) return;

    //textPainter.setCursor(((TextArea)component).isEditable());
    
    // set scrollpane window to new size if necessary
    if(component.getSize().width<=0 || component.getSize().height<=0) {
      //System.out.println("TextArea: skipping paint() for lack of size");
      return; // nothing to paint when no place to paint it
    }
    if(backgroundImage == null || !totalScreen.equals(component.getSize())) {
      // new field and scrollbar calculations
      setFields(component.getSize());
      //new background image
      backgroundImage = createImage(totalScreen.width, totalScreen.height);
      if( backgroundImage!= null &&  backgroundGraphics!= null) {
        backgroundGraphics.dispose();
      }
      backgroundGraphics = backgroundImage.getGraphics();  

      //redraw all: scrollbars and text
      repaintText = true;
      repaintHScroll = true;
      repaintVScroll = true;
    }
            
    //set background colors if necessary
    Color back = component.getBackground();
    Color fore = component.getForeground();
    boolean setcolors = false;
    if(fore!=null && !fore.equals(textAreaColors[4])) {
       textAreaColors[4]=fore;
       setcolors = true;
    }
    if(back != null && !back.equals(textAreaColors[0])) {
       textAreaColors = RudolphPeer.getBarColors(back, textAreaColors[4]);
       setcolors = true;
    }
    if(setcolors){
      if(hScroll != null) {
        hScroll.setBarColors(textAreaColors);
      }
      vScroll.setBarColors(textAreaColors);
      textPainter.setTextColors(textAreaColors);
    }
            
    //paint viewport(if there is something to paint, this is)
    if(repaintText && textScreen.width>2 && textScreen.height > 2) {    
      //text painter
      //backgroundGraphics.clearRect(1,1,textScreen.width-2, textScreen.height-2);
      backgroundGraphics.setColor(textAreaColors[0]);
      backgroundGraphics.fillRect(1, 1, textScreen.width - 2, textScreen.height - 2);
               
	  wonka.vm.Etc.woempa(9, "before textPainter.paint");
	  
      if(selectionStart == selectionStop) {
        // no selection, display cursor
        textPainter.paint(textScreen.width, textScreen.height, cursorLine, cursorBufferOffset, cursorHorizontalOffset, backgroundGraphics);
      }
      else {
        //display textarea with selected text
        textPainter.paint(textScreen.width, textScreen.height, startLine, startBufferOffset, startHorizontalOffset,
                                                               stopLine, stopBufferOffset, stopHorizontalOffset, backgroundGraphics);
      }
      repaintText = false;
    }

    //paint scrollbars if needed
    if(hScroll != null && repaintHScroll ) {
      hScroll.paint(backgroundGraphics);
      repaintHScroll = false;
    }
    if(vScrollVisible && repaintVScroll) {
      vScroll.paint(backgroundGraphics);
      repaintVScroll = false;
    }  

    // paint background image
    g.drawImage(backgroundImage, 0, 0, component);       
	
    super.paint(g);
  }

  /*
  ** Auxilliary: set cursor, selection start&stop according to new value
  ** the data struct newposition is an int[4] return from a setScreenPosition function with following elements:
  ** => newposition[0] : position of the selected character in the original text
  ** => newposition[1] : line of the selected character on the screen
  ** => newposition[2] : horizontal offset in pixels (or offset calculation value) of the selected character from the left side of the screen
  ** => newposition[3] : position of the selected character in the TextPainter's text buffer (the text without the \n line breaks)
  */

  private boolean setNewSelection(int[] newposition) { // Rectangle p = tempData) {
    if(!initialized) init(); 
    // see in which case we are
    if(newposition[0]== position) {
      // nothing changed
      return false;
    }
    //else {...
      if(selectionStart == selectionStop && newposition[0]<position) {
        // new selection starting at new position, ending at cursor
        selectionStart = newposition[0];
        startLine = newposition[1];
        startHorizontalOffset = newposition[2];
        startBufferOffset = newposition[3];

        selectionStop = position;
        stopLine=cursorLine;
        stopHorizontalOffset=cursorHorizontalOffset;
        stopBufferOffset=cursorBufferOffset;
      }
      else if(selectionStart == selectionStop && newposition[0]>position) {
        // new selection starting at cursor, ending at new position
        selectionStart = position;
        startLine=cursorLine;
        startHorizontalOffset=cursorHorizontalOffset;
        startBufferOffset=cursorBufferOffset;

        selectionStop = newposition[0];
        stopLine = newposition[1];
        stopHorizontalOffset = newposition[2];
        stopBufferOffset = newposition[3];
      }
      else if(position == selectionStart && newposition[0]>selectionStop){
        // inverse the selection: the old stop becomes the new start, new position becomes the new stop
        selectionStart = selectionStop;
        startLine=stopLine;
        startHorizontalOffset=stopHorizontalOffset;
        startBufferOffset=stopBufferOffset;

        selectionStop = newposition[0];
        stopLine = newposition[1];
        stopHorizontalOffset = newposition[2];
        stopBufferOffset = newposition[3];
      }
      else if(position == selectionStart && newposition[0]==selectionStop){
        //invalidate selection by setting them all to new position
        selectionStart=newposition[0];
        //selectionStop=newposition[0];
      }
      else if(position == selectionStart){
        // nothing special, just use new data for new start
        selectionStart = newposition[0];
        startLine = newposition[1];
        startHorizontalOffset = newposition[2];
        startBufferOffset = newposition[3];
      }
      //else if(position == selectionStop && newposition[0]<selectionStart) {
      else if(newposition[0]<selectionStart) {
        // big swap, old start becomes new stop, newposition becones new start
        selectionStop = selectionStart;
        stopLine=startLine;
        stopHorizontalOffset=startHorizontalOffset;
        stopBufferOffset=startBufferOffset;

        selectionStart = newposition[0];
        startLine = newposition[1];
        startHorizontalOffset = newposition[2];
        startBufferOffset = newposition[3];
      }
      //else if(position == selectionStop && newposition[0] == selectionStart) {
      else if(newposition[0] == selectionStart) {
        //invalidate selection by setting them all to new position
        //selectionStart=newposition[0];
        selectionStop=newposition[0];
      }
      //else if(position == selectionStop) {
      else {
        // just set stop to new value
        selectionStop = newposition[0];
        stopLine = newposition[1];
        stopHorizontalOffset = newposition[2];
        stopBufferOffset = newposition[3];
      }
      // in all cases, new position becomes cursor
      position = newposition[0];
      cursorLine=newposition[1];
      cursorHorizontalOffset=newposition[2];
      cursorBufferOffset=newposition[3];
    return true;
    //}
  }

  /*
  ** Auxilliary: recalculate screen and repaint for horizontal or vertical scrollbar
  ** (hiis function is class-protected so it can be called from withing the scroll runner thread just as well
  */

  public void repaint(ScrollPainter alignation) {
    if(alignation == vScroll) {
      // vertical scrolling
      textPainter.setLineOffset( vScroll.getBarPos() );        
      repaintVScroll= true;
    }
    else {
      // horizontal scrolling
      textPainter.setTextOffset( hScroll.getBarPos() );        
      repaintHScroll= true;
    }
    // also repaint the (scrolled) text area
    repaintText = true;
    paint(getGraphics());
  }

  /*
  ** Auxilliary check if position (line,offset) in current viewport and adjust viewport if not
  ** (return true if adjustment performed => repaint needed
  */

  private boolean adjustHorizontalViewport(int offset) {
    // we assume a check on hScroll beforehand
    boolean viewportadjusted = false;
    if(offset<textPainter.getTextOffset()) {
      textPainter.setTextOffset(cursorHorizontalOffset);
      hScroll.setBarPos(cursorHorizontalOffset);
      viewportadjusted = true;
    }
    else {
      int topline= offset - textPainter.getViewportWidth()+1;
      if(topline>textPainter.getTextOffset()) {
        textPainter.setTextOffset(topline);
        hScroll.setBarPos(topline);
        viewportadjusted = true;
      }
    }
    return viewportadjusted;
  }

  private boolean adjustVerticalViewport(int line) {
    boolean viewportadjusted = false;
    if(line < textPainter.getLineOffset() ) {
      // above current screen => move screen up
      textPainter.setLineOffset(cursorLine);
      vScroll.setBarPos(cursorLine);
      viewportadjusted = true;
    }
    else {
      int topline = line - textPainter.getViewportLines()+1;
      if(topline > textPainter.getLineOffset()) {
        // below current screen => move screen down
        textPainter.setLineOffset(topline);
        vScroll.setBarPos(topline);
        viewportadjusted = true;
      }
    }
    return viewportadjusted;
  }

  /*
  ** Auxilliary functions: set new tempData[] type data for position, selection start, selection stop, return true if valid
  */

  private boolean updatePosition(int newpos){
    if(newpos<0 || newpos>text.length()) {
      return false;
    }
    //else
     textPainter.setScreenPosition(newpos, tempData);      
     position=tempData[0];
     cursorLine=tempData[1];
     cursorHorizontalOffset=tempData[2];
     cursorBufferOffset=tempData[3];
     super.setCaretPosition(newpos);
    return true;
  }

  private boolean updateStart(int newpos){
    if(newpos<0 || newpos>text.length()) {
      return false;
    }
    //else
     textPainter.setScreenPosition(newpos, tempData);      
     selectionStart=tempData[0];
     startLine=tempData[1];
     startBufferOffset=tempData[2];
     startHorizontalOffset=tempData[3];
    return true;
  }

  private boolean updateStop(int newpos){
    if(newpos<0 || newpos>text.length()) {
      return false;
    }
    //else
     textPainter.setScreenPosition(newpos, tempData);      
     selectionStop=tempData[0];
     stopLine=tempData[1];
     stopBufferOffset=tempData[2];
     stopHorizontalOffset=tempData[3];
    return true;
  }

  /*
  ** set cursor to data and collapse selection to it
  */

  private boolean updateCursor(int[] newdata) {
    if(position == newdata[0]) {
      return false;
    }
    //else
      position = newdata[0];
      cursorLine = newdata[1];
      cursorHorizontalOffset = newdata[2];
      cursorBufferOffset = newdata[3];
      selectionStart = position;
      selectionStop = position;
      return true;
  }
  
  /*
  ** For a given size: calculate scrollbar position and text area width
  ** and set the scrollposition if necessary
  */

  private void setFields(Dimension newsize) {
//System.out.println("... TextArea calculating settings for new size"+newsize);    
    totalScreen.setSize(newsize);
    textScreen.setSize(newsize);
    //if horizontal scrollbar, set new offset and subtract scrollbar height from textarea size
    if(hScroll != null) {
      textScreen.height -= hScroll.getMinimumThickness();
      hScroll.setOffset(textScreen.height);
    }
    //if horizontal scrollbar: set new offset and subtract scrollbar width from textarea size
    if(vScrollVisible) {
      textScreen.width -= vScroll.getMinimumThickness();
      vScroll.setOffset(textScreen.width);
    }
        
    //new scrollbar width and viewport
    textPainter.setSize(textScreen);
    if(hScroll != null) {
      hScroll.setScreenWidth(textScreen.width, textPainter.getViewportWidth(), textPainter.getMaximumWidth());
    }
    vScroll.setScreenHeight( textScreen.height, textPainter.getViewportLines(),textPainter.getMaximumLines());
        
    //also recalculate PointData for position and selection(line wrapping might hace changed their lines)
    updatePosition(position);
    updateStart(selectionStart);
    updateStop(selectionStop);
  }

  /*
  ** Keyboard support :
  ** overrides the TextComponent keyboard to deal with multi-line display and up-down keys
  ** => use cursor keys left-right-home-end AND up-down-pgUp,pgDn
  ** => use cursor keys with shift for selection
  ** => use delete and backspace for cursor or selection
  ** => insert char for cursor and selection
  */

  /*
  ** Mouse clicked (nothing special) 
  */
  
  public  void mouseClicked(MouseEvent e) {
  }

  /*
  ** Mouse entered (nothing special) 
  */
  
  public  void mouseEntered(MouseEvent e) {
  }

  public  void mouseMoved(MouseEvent e) {
  }

  /*
  ** Mouse Pressed:
  ** =>if in text: set insert position
  ** =>if in scrollbar scrollbox: set mouse position for scrollbox dragging
  ** =>if in scrollbar up/down boxes or above/below scrollbox: call thread for viewport movements
  */
  
  public  void mousePressed(MouseEvent e) {
    int x=e.getX();
    int y=e.getY();
    if(x<textScreen.width && y<textScreen.height) { //inside  text area
      lastMousePosition.setLocation(x,y);//detecting movement when scrolling
      //get point in x,y coordinates,position,width...      
      textPainter.setScreenPosition(x,y,tempData);
      // set data to cursor and collapse selection to that point, repaint if needed
      if(updateCursor(tempData) ){
        repaintText = true;
        paint(getGraphics());
      }     
    }
    else if(hScroll!= null && y>textScreen.height){
      //inside horizontal scrollbar
      int active = hScroll.setActive(x,y);
      if(active == AdjustmentEvent.TRACK) {//scrollbox clicked
        lastMousePosition.setLocation(x,y);
        repaintHScroll = true;
        paint(getGraphics()); //show active scroll box of bar
      }
      else if(active == AdjustmentEvent.UNIT_DECREMENT && hScroll.lineUp() ) {
        // launch scroll thread
        mouseEventThread.setRunner(hScroll,component);
        // managed to set scrollbar one char left
        repaint(hScroll);
      }
      else if(active == AdjustmentEvent.UNIT_DECREMENT) {
        // simply repaint the scrollbar with the 'up' block pressed
        repaintHScroll = true;
        paint(getGraphics());
      }
      else if(active==AdjustmentEvent.UNIT_INCREMENT && hScroll.lineDn() ) {
        // launch scroll thread
        mouseEventThread.setRunner(hScroll,component);
        // managed to set scrollbar one char right
        repaint(hScroll);
      }
      else if(active==AdjustmentEvent.UNIT_INCREMENT) {
       // simply repaint the scrollbar with the 'down' block pressed
        repaintHScroll = true;
        paint(getGraphics());
      }
      else if(active==AdjustmentEvent.BLOCK_DECREMENT && hScroll.pageUp() ) {
        // launch scroll thread
        mouseEventThread.setRunner(hScroll,component);
        // set text and repaint
        repaint(hScroll);
        // the area between button and scrollbox can not be 'pressed',so don'tbother to redraw
      }
      else if(active==AdjustmentEvent.BLOCK_INCREMENT && hScroll.pageDn() ) {
        // launch scroll thread
        mouseEventThread.setRunner(hScroll,component);
        // set text and repaint
        repaint(hScroll);
        // the area between button and scrollbox can not be 'pressed',so don'tbother to redraw
      }
    }
    else if(vScrollVisible && x>textScreen.width) {
      //inside vertical scrollbar
      int active = vScroll.setActive(x,y);
      if(active == AdjustmentEvent.TRACK) {
        //scrollbox clicked
        lastMousePosition.setLocation(x,y);
        repaintVScroll = true;
        paint(getGraphics()); //show active scroll box of bar
      }
      else if(active == AdjustmentEvent.UNIT_DECREMENT && vScroll.lineUp() ) {
        // launch scroll thread
        mouseEventThread.setRunner(vScroll,component);
        // set text and repaint
         repaint(vScroll);
      }
      else if(active == AdjustmentEvent.UNIT_DECREMENT) {
        // simply repaint the scrollbar with the 'up' block pressed
        repaintVScroll = true;
        paint(getGraphics());
      }
      else if(active==AdjustmentEvent.UNIT_INCREMENT && vScroll.lineDn() ) {
        // launch scroll thread
        mouseEventThread.setRunner(vScroll,component);
        // set text and repaint
        repaint(vScroll);
      }
      else if(active==AdjustmentEvent.UNIT_INCREMENT) {
        // simply repaint the scrollbar with the 'down' block pressed
        repaintVScroll = true;
        paint(getGraphics());
      }
      else if(active==AdjustmentEvent.BLOCK_DECREMENT && vScroll.pageUp() ) {
        // launch scroll thread
        mouseEventThread.setRunner(vScroll,component);
        // set text and repaint
        repaint(vScroll);
        // the area between button and scrollbox can not be 'pressed',so don'tbother to redraw
      }
      else if(active==AdjustmentEvent.BLOCK_INCREMENT && vScroll.pageDn() ) {
        // launch scroll thread
        mouseEventThread.setRunner(vScroll,component);
        // set text and repaint
        repaint(vScroll);
        // the area between button and scrollbox can not be 'pressed',so don'tbother to redraw
      }
    }       
  }

  /*
  ** Mouse released:
  ** =>set scrolling to stop and scrollbars to no-selected
  */

  public  void mouseReleased(MouseEvent e) {
    //scrollbars are no longer selected
    if(hScroll!= null) {
      if(hScroll.isSelected()) {
        hScroll.setNoSelected();
        repaintHScroll=true;
      }
      mouseEventThread.stopRunner(hScroll);
    }
    if(vScrollVisible && vScroll.isSelected()) {
      vScroll.setNoSelected();
      repaintVScroll = true;
    }
    mouseEventThread.stopRunner(vScroll);

    if(repaintHScroll || repaintVScroll) {
      paint(getGraphics());
    }
  }


  /*
  ** Mouse exited as per mouseReleased, scrolling stops
  */

  public  void mouseExited(MouseEvent e) {
    //scrollbars are no longer selected
    if(hScroll!= null) {
      if(hScroll.isSelected()) {
        hScroll.setNoSelected();
        repaintHScroll=true;
      }
      mouseEventThread.stopRunner(hScroll);
    }
    if(vScrollVisible && vScroll.isSelected()) {
      vScroll.setNoSelected();
      repaintVScroll = true;
    }
    mouseEventThread.stopRunner(vScroll);

    if(repaintHScroll || repaintVScroll) {
      paint(getGraphics());
    }
  }
      
  /*
  ** Mouse dragged:
  ** => look for text scrolling mode and if needed, start thread for viewport scrolling
  ** => if in viewport, 'drag' a selection from insert point to current mouse position
  ** => if in scrollbar box: move box and update viewport
  */

  public  void mouseDragged(MouseEvent e) {
    int x=e.getX();
    int y=e.getY();
    if(hScroll!= null && hScroll.getActive() == AdjustmentEvent.TRACK) {
      if(hScroll.moveBar(x-lastMousePosition.x))  {
        //set the new text area line offset
        textPainter.setTextOffset( hScroll.getBarPos() );        
        lastMousePosition.setLocation(x,y);
        repaintText = true;
        repaintHScroll = true;
        paint(getGraphics()); //show new scrollbar and screen selection
      }
    }
    else if(vScrollVisible && vScroll.getActive() == AdjustmentEvent.TRACK) {
      if(vScroll.moveBar(y-lastMousePosition.y)) {
        //set the new text area line offset
        textPainter.setLineOffset( vScroll.getBarPos() );
        lastMousePosition.setLocation(x,y);
        repaintText = true;
        repaintVScroll = true;
        paint(getGraphics()); //show new scrollbar and screen selection
      }
    }
    else if(x<textScreen.width && y<textScreen.height) {
      //inside  text area
      //Horizontal mouse scrolling
      if(hScroll != null) {
        if(y<0 || y>textScreen.height) {
          // no longer in list area, so stop
          mouseEventThread.stopRunner(hScroll);
        }
        else if(x>0 && x<textPainter.getBorder() && x<lastMousePosition.x && hScroll.lineUp()) {
          // in upper area moved up => set scrollbar one up & start thread (moving up)
          mouseEventThread.setRunner(hScroll,component,ScrollRunner.SCROLL_UP);
          repaint(vScroll);
          lastMousePosition.x=x;
        }
        else if(x>textPainter.getBorder() && x<lastMousePosition.x){
          //moved up when not in upper area => stop (moving down) thread
          mouseEventThread.stopRunner(hScroll);
          lastMousePosition.x=x;
        }
        else if(x<textScreen.width && x>(textScreen.width-textPainter.getBorder()) && x>lastMousePosition.x && hScroll.lineDn()){
          // in lower area moved down: set scrollbar one down and start thread (moving down)
          mouseEventThread.setRunner(hScroll,component,ScrollRunner.SCROLL_DOWN);
          repaint(vScroll);
          lastMousePosition.x=x;
        }
        else if(x<(textScreen.width-textPainter.getBorder()) &&  x>lastMousePosition.x){
          //moved down when not in lower area => stop (moving up) thread
          mouseEventThread.stopRunner(hScroll);
          lastMousePosition.x=x;
        }    

      }
      //ALWAYS vertical mouse scrolling (even if we don't see the vertical scrollbar, it's there invisible specially to do the calculations)
      if(x<0 || x>textScreen.width) {
        // no longer in list area, so stop
        mouseEventThread.stopRunner(vScroll);
      }
      else if(y>0 && y<textPainter.getBorder() && y<lastMousePosition.y && vScroll.lineUp()) {
        // in upper area moved up => set scrollbar one up & start thread (moving up)
        mouseEventThread.setRunner(vScroll,component,ScrollRunner.SCROLL_UP);
        repaint(vScroll);
        lastMousePosition.y=y;
      }
      else if(y>textPainter.getBorder() && y<lastMousePosition.y){
        //moved up when not in upper area => stop (moving down) thread
        mouseEventThread.stopRunner(vScroll);
        lastMousePosition.y=y;
      }
      else if(y<textScreen.height && y>(textScreen.height-textPainter.getBorder()) && y>lastMousePosition.y && vScroll.lineDn()){
        // in lower area moved down: set scrollbar one down and start thread (moving down)
        mouseEventThread.setRunner(vScroll,component,ScrollRunner.SCROLL_DOWN);
        repaint(vScroll);
        lastMousePosition.y=y;
      }
      else if(y<(textScreen.height-textPainter.getBorder()) &&  y>lastMousePosition.y){
        //moved down when not in lower area => stop (moving up) thread
        mouseEventThread.stopRunner(vScroll);
        lastMousePosition.y=y;
      }       //(end vertical scrollbar

      // finaly get a selection out of the text the cursor was draged over
      textPainter.setScreenPosition(x,y,tempData);
      if (setNewSelection(tempData) ) {
        // the new position changed the selection (or cursor position)
        //new selection position
        lastMousePosition.setLocation(x,y);
        // repaint
        repaintText = true;
        paint(getGraphics());
      }
    }
  }
  
  /*
  ** text methods inherited from superclass TextComponent
  */
  
  /*
  ** TextComponent.setText : Overwritten to also update the visible parts
  */

  public synchronized void setText(String newtext) {
    if(!initialized) init(); 
    super.setText(newtext);
    //set new text
    text = newtext;
    // let painter calculate new image and tell scrollbars
    textPainter.setImage(text);
    //  tell scrollbars and set offset to null
    if(hScroll != null) {
      hScroll.setBarRange(textPainter.getMaximumWidth());
      hScroll.setBarPos(0); //(also set scrollbars to end of text
    }
    //vScroll not always visible, but always present (Calculating positions for mouse selection scrolling)
    vScroll.setBarRange(textPainter.getMaximumLines());
    vScroll.setBarPos(0);
    textPainter.setOffset(0,0);
        
    // set cursor to end of text, collapse selection to cursor
    // updatePosition(newtext.length());
    updatePosition(0);
    
    /*
    if(((TextArea)component).isEditable()) {
      setCaretPosition(newtext.length());
    }
    else {
      setCaretPosition(0);
    }
    */

    selectionStart = position;
    selectionStop = position;
            
    // repaint
    repaintText = true;
    repaintHScroll = true;
    repaintVScroll = true;
    paint(getGraphics());
    
    //tell the text listeners
    fireTextEvent();
  }

  /*
  ** TextComponent.setCaretPosition : Overwritten to also calculate the caret's PointData and show the caret on screen
  */

  public void setCaretPosition(int newpos) {
    if(!initialized) init(); 
    if(newpos!=position && updatePosition(newpos)) {
      // adjust viewport if needed
      repaintVScroll = adjustVerticalViewport(cursorLine);
      if(hScroll != null) {
        repaintHScroll = adjustHorizontalViewport(cursorHorizontalOffset);
      }
      repaintText = true;
      paint(getGraphics());
    }
  }

  /*
  ** TextComponent.setSelectionStart : Overwritten to also calculate the caret's PointData and show the caret on screen
  */

  /*
  ** TODO: These methods do not exist in the peer !!!!!!!!!!!
  */
  
  public synchronized void setSelectionStart(int newpos) {
    if(!initialized) init(); 
    if(newpos!=position && updateStart(newpos)) {
      // adjust viewport if needed
      repaintVScroll = adjustVerticalViewport(startLine);
      if(hScroll != null) {
        repaintHScroll = adjustHorizontalViewport(startHorizontalOffset);
      }
      repaintText = true;
      paint(getGraphics());
    }
  }

  /*
  ** TextComponent.setSelectionStop : Overwritten to also calculate the caret's PointData and show the caret on screen
  */

  public synchronized void setSelectionEnd(int newpos) {
    if(!initialized) init(); 
    if(newpos!=position && updateStop(newpos)) {
      // adjust viewport if needed
      repaintVScroll = adjustVerticalViewport(stopLine);
      if(hScroll != null) {
        repaintHScroll = adjustHorizontalViewport(stopHorizontalOffset);
      }
      repaintText = true;
      paint(getGraphics());
    }
  }

  /*
  **  Own protected helper function: fires a textValueChanged(textEvent) on the registered textListener(s)
  */


  protected void fireTextEvent() {
    component.dispatchEvent(new TextEvent(component, TextEvent.TEXT_VALUE_CHANGED));
  }

  /*
  ** internal scrollpainter for horizontal scrollbar 
  */
  
  class HAreaScrollPainter extends ScrollPainter {
  
    /*
    ** Constructor : set height, lineup/lineDn and minimum box width for horizontal scrollbar
    */
    
    public HAreaScrollPainter() {
      super(RudolphScrollbarPeer.HSCROLL_HEIGHT, RudolphScrollbarPeer.HSCROLL_LINEUPWIDTH,
      RudolphScrollbarPeer.HSCROLL_LINEDNWIDTH, RudolphScrollbarPeer.HSCROLL_MINIMUMBOXWIDTH);
    }
        
    /*
    ** scrollbar width and scrollbar position out of (x,y) dimension
    ** the abstract ScrollPainter classes filled in for horizontal scrollbar
    */
    
    /*
    ** (horizontal bar) x- position in scrollbar from point (x,y) 
    */
    
    public int getPos(int x, int y) {
      return x;
    }
    
    /*
    ** (horizontal bar) scrollbar height as thickness from dimension (width, height)
    */
    
    public int getThickness(int width, int height) {
      return height;
    }
    
    /*
    ** scrollbar length and thickness to (width, height) dimension for horizontal and vertical scrollbar
    */
    
    public Dimension getSize(int scrollbarlength, int scrollbarthickness) {
      return new Dimension(scrollbarlength, scrollbarthickness);
    }
    
    /*
    ** replace <thickness> of given dimension by scrollban minimum thickness
    */
    
    public Dimension getPreferredSize(int width, int height){
      return new Dimension(width, minimumThickness);  
    }
        
    /*
    ** GetSize derived functions:specific overwrite for horizontal scrollbar
    */

    public Dimension getMinimumSize() {
      return new Dimension(lineUpSpan+lineDnSpan+minimumScreenSpan , minimumThickness);  
    }
    
    public Dimension getPreferredSize() {
      return new Dimension(lineUpSpan+lineDnSpan+screenRange , minimumThickness);  
    }
    
    public Dimension getCurrentSize() {
      return new Dimension(lineUpSpan+lineDnSpan+screenRange , minimumThickness);  
    }
        
    public void setThickness(int width, int height) {
      currentThickness =(height>minimumThickness)?height:minimumThickness;
    }

    public boolean setRange(int width, int height) {
      return setRange(width);
    }
        
    /*
    ** set a new screen height and visibility after textarea rescaling
    */

    public boolean setScreenWidth(int totalwidth, int newvisible, int newrange) {
      screenRange = totalwidth - lineUpSpan - lineDnSpan;
      barSpan = newvisible;
      barRange = newrange;
      blockStep = (newvisible>lineStep)?newvisible-lineStep:lineStep;
      if (screenRange <=0) {
        // crippled mode
        screenRange = 0;
        screenSpan = 0;
        screenPos = 0;              
        crippledSpan = totalwidth;      
      }
      else {
        //set new screen range and visibility bar span
        crippledSpan = -1;      
        //safety check
        if(barSpan > barRange) {
          barSpan = barRange;
          barPos = 0;
        }
        else if((barPos+barSpan)>barRange) {
          barPos = barRange - barSpan;
        }
        //calculate screen scrollbox span and -position
        setScreen(); //setBar(minimumspan);
      }
      //changing the width always forces a redraw, so return true for every case
      return true;
    }  
        
    /*
    ** basic functions : get field in which probing point exists
    ** additional feature: next to the x-position(scrollbar length)
    ** we also look if the y-position is within the range (offset , offset + height)
    */
    
    synchronized public int setActive(int x, int y) {
      currentActive = (y>barOffset)?getField(x): RudolphScrollbarPeer.FIELD_NONESELECTED;
      return currentActive;
    }
    
    /*
    ** paint command
    */

    public void paint(Graphics g) {
      if(crippledSpan<0) {
        RudolphScrollbarPeer.paintHScrollbar(0,barOffset,minimumThickness, paintedScreenPos,screenSpan,screenRange, currentActive,barColors,g);  
      }
      else {
        RudolphScrollbarPeer.paintCrippledHScrollbar(0,barOffset,minimumThickness, crippledSpan, currentActive,barColors,g);  
      }
    }
  //end of inner class HAreaScrollPainter
  }

  /*
  ** internal scrollpainter for vertical scrollbar 
  */
  
  class VAreaScrollPainter extends ScrollPainter {
    
    /*
    ** Constructor : set height, lineup/lineDn and minimum box width for horizontal scrollbar
    */
    
    public VAreaScrollPainter() {
      super(RudolphScrollbarPeer.VSCROLL_WIDTH, RudolphScrollbarPeer.VSCROLL_LINEUPHEIGHT,
      RudolphScrollbarPeer.VSCROLL_LINEDNHEIGHT, RudolphScrollbarPeer.VSCROLL_MINIMUMBOXHEIGHT);
    }
        
    /*
    ** scrollbar width and scrollbar position out of (x,y) dimension
    ** the abstract ScrollPainter classes filled in for horizontal scrollbar
    */
    
    /*
    ** (horizontal bar) x- position in scrollbar from point (x,y) 
    */
    
    public int getPos(int x, int y) {
      return y;
    }
    
    /*
    ** (horizontal bar) scrollbar height as thickness from dimension (width, height)
    */
    
    public int getThickness(int width, int height) {
      return width;
    }
    
    /*
    ** scrollbar length and thickness to (width, height) dimension for horizontal and vertical scrollbar
    */
    
    public Dimension getSize(int scrollbarlength, int scrollbarthickness) {
      return new Dimension(scrollbarthickness, scrollbarlength);
    }
    
    /*
    ** replace <thickness> of given dimension by scrollban minimum thickness
    */
    
    public Dimension getPreferredSize(int width, int height) {
      return new Dimension(minimumThickness,height);  
    }
        
    /*
    ** GetSize derived functions:specific overwrite for horizontal scrollbar
    */
    
    public Dimension getMinimumSize() {
      return new Dimension(minimumThickness, lineUpSpan+lineDnSpan+minimumScreenSpan);  
    }
    
    public Dimension getPreferredSize() {
      return new Dimension(minimumThickness, lineUpSpan+lineDnSpan+screenRange);  
    }
    
    public Dimension getCurrentSize() {
      return new Dimension(minimumThickness, lineUpSpan+lineDnSpan+screenRange);  
    }
        
    public void setThickness(int width, int height) {
      currentThickness =(width>minimumThickness)?width:minimumThickness;
    }
        
    public boolean setRange(int width, int height) {
      return setRange(height);
    }
        
    /*
    ** set a new screen height and visibility after textarea rescaling
    */

    public boolean setScreenHeight(int totalheight, int newvisible, int newrange) {
      screenRange = totalheight - RudolphScrollbarPeer.VSCROLL_LINEUPHEIGHT - RudolphScrollbarPeer.VSCROLL_LINEUPHEIGHT;
      barSpan = newvisible;
      barRange = newrange;
      blockStep=(newvisible>1)?newvisible-1:1;
      if (screenRange <=0) {
        //crippled scrollbar
        screenRange = 0;
        screenSpan = 0;
        screenRange = 0;              
        crippledSpan = totalheight;      
      }
      else {
        //set new screen range and visibility bar span
        crippledSpan = -1;      
        //safety check
        if(barSpan > barRange) {
          barSpan = barRange;
          barPos = 0;
        }
        else if((barPos+barSpan)>barRange) {
          barPos = barRange - barSpan;
        }
        //calculate screen scrollbox span and -position
        setScreen(); //setBar(minimumspan);
      }
      //always return true for redrawing
      return true;
    }  
        
    /*
    ** basic functions : get field in which probing point exists: Special case: we take into account the offset
    */

    synchronized public int setActive(int x, int y) {
      currentActive = (x>barOffset)?getField(y):RudolphScrollbarPeer.FIELD_NONESELECTED;
      return currentActive;
    }

    /*
    ** paint command
    */

    public void paint(Graphics g) {
      if(crippledSpan<0) {
        RudolphScrollbarPeer.paintVScrollbar(barOffset,0,minimumThickness, paintedScreenPos,screenSpan,screenRange,  currentActive,barColors,g);  
      }
      else {
        RudolphScrollbarPeer.paintCrippledVScrollbar(barOffset,0,minimumThickness, crippledSpan, currentActive, barColors,g);  
      }
    }
  //end of inner class HAreaScrollPainter
  }

  public boolean inRange(MouseEvent e) {
    return (e.getX() < textScreen.width && e.getY() < textScreen.height);
  }
  
  public void focusGained(FocusEvent event)
  {
	  textPainter.setCursor(((TextArea)component).isEditable());
	  this.textPainter.setScreenPositionLine(0, 2, tempData);
	  	  
	  tempData[0] = 0;
	  tempData[3] = 0;
	  	  	  
	  updateCursor(tempData);
      repaintText = true;        
	  paint(getGraphics());
  }
  
  public void focusLost(FocusEvent event)
  {
	  this.textPainter.setCursor(false);
	  repaintText = true;        
	  paint(getGraphics());
  }  
}

