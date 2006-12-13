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

import java.awt.*;

/*
**  The TextArea Painter class handles the calculation of the text visible in the TextArea's viewport, including the calculation of the
** insert point (from mouse coordinates) or the selected text; also handles the colors in which the area will be displayed;
** (note that the actual painting is forwarded to the RudolphTextAreaPeer plug-in);
**  it is a helper class to transfer screen point settings between textArea and painter
** the data to be transferred is:
** => the line of the current point
** => the x-width of the current point, either in (fixed-widht) characters, or in pixels
** => the position of the character the point is in in the painters text buffer (As the buffer does not store endline characters,
**   this may be less then the position in the original text)
*/

public abstract class TextAreaPainter {
  
  /*
  ** Variables 
  */
  
  /*
  ** the viewport in pixels 
  */
  
  protected Dimension viewport;

  /*
  ** maximum textlines width == textlineEnd.length
  */
  
  /*
  ** vertical textlines offset
  */

  protected int lineOffset;
     
  /*
  ** Font and colors 
  */
  
  protected Font painterFont;
  protected FontMetrics painterMetrics;
  //protected int painterCharWidth;      // = fm.getMaxAdvance for fixed width characters, -1 for variable width
  protected Color[] textColors = RudolphPeer.getBarColors();

  /*
  ** text buffer and text start/stop position arrays 
  */
  
  //text to display and text offset arrays: (replace by textbuffer and offset asap)
  protected char[] textBuffer;
  protected int[] textlineEnd;
  protected int[] viewTextOffset;
  protected int[] viewTextLength;
  
  protected boolean drawCursor = false;

  /*
  ** Constructor
  */
  
  public TextAreaPainter(Font textfont, FontMetrics metrics, String text, Dimension size, Color[] colors) {
    // initialise Dimensions,
    viewport = new Dimension(RudolphTextAreaPeer.getInnerwidth(size.width), RudolphTextAreaPeer.getLines(size.height,metrics));
    if(viewport.width<0) {
      viewport.width=0;
    }
        
    // default colors, font
    textColors = colors;
    painterFont = textfont; //RudolphPeer.DEFAULT_FONT;
    painterMetrics = metrics;
    //System.out.println("... new TextAreaPainter: font:"+painterFont+" charheight:"+painterMetrics.getHeight()
    //                       +" size=("+size.width+", "+size.height+")");
      
    //text and text arrays
    setImage(text);
  }
    
  /*
  ** Constructor short form for derived classes
  */
  
  public TextAreaPainter(FontMetrics metrics, Dimension size, Color[] colors) {
    // initialise Dimensions,
    viewport = new Dimension(RudolphTextAreaPeer.getInnerwidth(size.width), RudolphTextAreaPeer.getLines(size.height,metrics));
    if(viewport.width<0) {
      viewport.width=0;
    }
    // default colors, font
    textColors = colors;
  }

  /*
  ** get and set screen sizes
  */

  public Dimension getScreenSize(int chars, int lines) {
    return new Dimension(RudolphTextAreaPeer.getScreenSize( chars*painterMetrics.charWidth('a')), RudolphTextAreaPeer.getHeight(lines,painterMetrics));  
  }
    
  public int getMaxAdvance() {
    // one click at the horizontal painter should ALWAYS advance you one char
    return painterMetrics.getMaxAdvance();
  }
    
  public int getBorder() {
    // We assume a border for mouse scrolling of half the font's height
    return painterMetrics.getHeight()/2;
  }
  
  /*
  ** new screen size in pixels 
  */

  public void setSize(Dimension newsize) {
    //width in pixels
    viewport.width = RudolphTextAreaPeer.getInnerwidth(newsize.width); //the effective width without the painters borders
    //height in character
    viewport.height =  RudolphTextAreaPeer.getLines(newsize.height,painterMetrics);
    //new visible text line parts
    calculateVisibleLineparts();    
  }
    
  /*
  ** return viewport (width in pixels, height in lines)
  */

  public int getViewportWidth() {
    return viewport.width;  
  }
  
  public int getViewportLines() {
    return viewport.height;  
  }

  /*
  ** return Maximum width in pixels, height in lines
  */

  public int getMaximumWidth(){
    return viewport.width;
  }
  
  public int getMaximumLines(){
    return textlineEnd.length;
  }
    
  /*
  **  text offset (in pixels), line offset(in lines)
  */
  
  public int getTextOffset() {
    // no scrolling text => text offset is always 0
    return 0;  
  }
  
  public int getLineOffset() {
    return lineOffset;  
  }
  
  public Point getOffset() {
    //return text offset == 0 and line offset
    return new Point(0, lineOffset);  
  }
    
  public void setTextOffset(int offset) {
    // do nothing: as no scrolling text, offset is never used
  }
  
  public void setLineOffset(int offset) {
    lineOffset = offset;  
  }
    
  public void setOffset(int offsetx, int offsety) {
    // disregard text offset (not used), but set line offset
    lineOffset = offsety;    
  }
    
  /*
  ** The colors
  */
  
  public void setTextColors(Color back, Color font) {
    textColors = RudolphPeer.getBarColors(back, font);  
  }
    
  public void setTextColors(Color[] newcolors) {
    if(newcolors.length >=5) {
      textColors = newcolors;
    }
  }
    
  public Color[] getTextColors() {
    return textColors;
  }
  
  public Color getBackground() {
    return textColors[0];
  }
  
  public Color getForeground() {
    return textColors[4];
  }

  /*
  ** Text methods
  */
  
  public void setImage(String text) {  
    //Calculate number of lines
    int breaks = 0;
    int pos=0;
    while((pos=text.indexOf('\n',pos+1))>=0) {
      breaks++;
    }
          
    //initialise line buffers
    textBuffer = new char[text.length()-breaks];// we suppress all breaks in the buffer text
    textlineEnd = new int[breaks+1];
    viewTextOffset = new int[breaks+1];
    viewTextLength = new int[breaks+1];
        
    //fill in the buffers up to the last line
    int bufstart=0;
    int textstart=0;
    for(int i=0; i<breaks; i++) {
      pos = text.indexOf('\n',textstart);
      text.getChars(textstart,pos,textBuffer,bufstart);
      textlineEnd[i] = bufstart + pos - textstart;
      bufstart=textlineEnd[i];
      textstart =pos+1;
    }
    //last line
    text.getChars(textstart,text.length(),textBuffer,bufstart);
    textlineEnd[breaks] = textBuffer.length;
          
//System.out.println("textarea new text <"+new String(textBuffer,0,textBuffer.length)+"> "+textlineEnd.length+" lines");    
    if(viewport.width>0) {
      //if there is a space to show them in, build a new set of visible text arrays
      calculateVisibleLineparts();
    }
  }
  
  /*
  ** Calculate the part of the text that is visible in the current textrea viewport
  ** these function MUST be overwritten by the subclasses for wrapping/scrolling with fixedwidth font/variable width font
  */

  abstract protected void calculateVisibleLineparts() ;
  
  /*
  ** Calculate a position on the screen into an array of data : (vertical)line,(horizontal) character or offset and character in text buffer
  ** Calculate a character in the original text into an array of data :
  ** the returned data is a 4-int array containing
  ** int 0: position of the selected character in the original text
  ** int 1: (vertical)line the selected char is in
  ** int 2: (Fixed width font) : the number of the selected char in the current line
  **        (variable width font) : the offset in pixels from text start to the selected char
  ** int 3: position of the selected character in the painter's text buffer
  ** these functions MUST be overwritten by the subclasses for wrapping/scrolling with fixedwidth font/variable width font
  */
  
  abstract public void setScreenPosition(int x, int y, int[] pointdata) ;
  abstract public void setScreenPosition(int pos, int[] pointdata) ;
  abstract public void setScreenPositionLine(int line, int offset, int[] pointdata) ;

  /*
  ** These are debug functions for a basic TextArea behavior: only print the lines as far as they are visible, no scrolling
  ** these functions MUST be overwritten
  * /
  
  /*
  ** calculate lines, startChars and StopChars for given text, viewport and offset
  ** in the basic version, this only displays the first characters of the text, up to the right screen end
  * /
  
  protected void calculateVisibleLineparts() {
    //System.out.println("TextAreaPainter,calculating visible lines for width="+viewport.width);
    //security
    if(textBuffer.length<=0 || viewport.width<=0) {
      textlineEnd = new int[0];
      viewTextOffset = new int[0];
      viewTextLength = new int[0];
      return;
    }

    int start=0;
    for(int i=0;i<textlineEnd.length;i++) {
      viewTextOffset[i] = start;
      // last char that can be displayed on the line
      viewTextLength[i] = RudolphPeer.getChars(viewport.width,start,textBuffer,painterMetrics);
      if(viewTextLength[i]>textlineEnd[i]) {
        // last char later then line end => length = complete line length
        viewTextLength[i] = textlineEnd[i]-start;
      }
      else {
        viewTextLength[i]-=start;         // text longer then line => line length = last char of line - first of line
      }
      start=textlineEnd[i];
    }  
  }
  
  /*
  ** screen position to position in displayed text and vice versa
  ** => newposition[0] : position of the selected character in the original text
  ** => newposition[1] : line of the selected character on the screen
  ** => newposition[2] : horizontal offset in pixels (or offset calculation value) of the selected character from the left side of the screen
  ** => newposition[3] : position of the selected character in the TextPainter's text buffer (the text without the \n line breaks)
  * /
  
  public void setScreenPosition(int x, int y, int[] pointData) {
    //security
    if(viewport.width<=0 || viewport.height<=0 ||textlineEnd.length<=0 ) {
      return;
    }
    // no of lines on screen, with offset
    pointdata[1] = RudolphTextAreaPeer.getLines(y, painterMetrics) + lineOffset;
    if(pointdata[1]>=textlineEnd.length) // deeper then last line
    pointdata[1]= textlineEnd.length-1;
        
    // x-position of the line in pixels, in chars
    pointdata[3]= RudolphPeer.getChars(x,viewTextOffset[pointdata[1]],textBuffer,painterMetrics);
    int chars=pointdata[3]-viewTextOffset[pointdata[1]];
    if(pointdata[3]>textlineEnd[pointdata[1]]) {
      //calculated maximum width is larger then actual textline width => adjust to end of line
      pointdata[3]= textlineEnd[pointdata[1]];
      chars = pointdata[3]-viewTextOffset[pointdata[1]];
    }
    pointdata[2]=painterMetrics.charsWidth(textBuffer, viewTextOffset[pointdata[1]], chars );
        
    // in text string instead of text buffer, each line is ended by a '\n' sign => add 1 extra char for every line before current pos
    return pointdata[3] + pointdata[1];
  }

  public void setScreenPosition(int pos, int[] pointdata) {
    //System.out.println( "TextAreaPainter.setScreenPosition : finding data for TextPoint pos "+pos);
    //security
    if(viewport.width<=0 || viewport.height<=0 ||textlineEnd.length<=0 ) {
      return;
    }
    //security
    if(pos<0) {
      //no selection
//setData(int line, int chars, int offset)  => setBounds(0,line, offset, chars)
      pointdata[0] = 0;
      pointdata[1] = -1;
      pointdata[3] = -1;
      pointdata[2] = -1;

    }
    else if( pos >=(textBuffer.length+textlineEnd.length-1)) {
      //passed the end of buffer
//setData(int line, int chars, int offset)  => setBounds(0,line, offset, chars)
      pointdata[1]=textlineEnd.length-1;
      pointdata[3]= textBuffer.length;
      pointdata[2] = painterMetrics.charsWidth(textBuffer,viewTextOffset[pointdata[1]],textBuffer.length-viewTextOffset[pointdata[1]]);
    }
    else {
      // scan all lines until we found a line end bigger then the desired possition (as the textbuffer does not count the \n
      // endings at each line, subtract an ending char from the pos for each line to obtain the buffer position just as well
      pointdata[1]=0;
      while(pos>textlineEnd[pointdata[1]]) {
        pointdata[1]++;
        pos--;
      }
      pointdata[3] = pos;
      pointdata[2]= painterMetrics.charsWidth(textBuffer,viewTextOffset[pointdata[1]],pos-viewTextOffset[pointdata[1]]);  
    }
  }

  /*
  ** paint
  */

  abstract public void paint (int width, int height, int cursorline, int cursoroffset, int cursorscreenpos, Graphics g) ;

  abstract public void paint(int width, int height, int startline, int startbufferoffset, int starthorizontaloffset,
                                           int stopline,  int stopbufferoffset,  int stophorizontaloffset,Graphics g) ;

  /*
  ** Static function to build a wrapping or scrolling, fixed or variable charwidth TextArea painter, depending on the type of font
  ** and on the presence of a horizontal scrollbar
  ** inthe fiinal version, this will be th only <real> function from class TextAreaPainter that is callde directly.
  ** as all other functions relate to the derived scrolling/wrapping/fixedwidth/variable-width classes
  ** and call the derived functions of these
  */
  
  public static TextAreaPainter getNewPainter(Font newfont, String text, Color[] colors, Dimension screen, boolean scrolling) {
    TextAreaPainter newpainter;

    if( newfont == null) {
      newfont = RudolphTextAreaPeer.DEFAULT_FONT;
    }
    FontMetrics metrics = new FontMetrics(newfont);
    int maxwidth = metrics.getMaxAdvance();
    if(scrolling && maxwidth> metrics.charWidth('i') ) {
      // => variable width & scrolling
      newpainter = new TextAreaPainter_ScrollingVarFont(newfont,metrics, text, screen,colors);
    }
    else if(scrolling) {
      //fixed width scrolling
      newpainter = new TextAreaPainter_ScrollingFixFont(newfont,metrics,maxwidth, text, screen,colors);
    }
    else if( maxwidth> metrics.charWidth('i') ) {
      //  => variable width && wrapping
      newpainter = new TextAreaPainter_WrappingVarFont(newfont,metrics,text, screen, colors);
    }
    else {
      //fixed width wrapping
      newpainter = new TextAreaPainter_WrappingFixFont(newfont,metrics,maxwidth,text, screen, colors);
    }
    return newpainter;
  }

  public void setCursor(boolean enabled) {
    drawCursor = enabled;
  }

}

