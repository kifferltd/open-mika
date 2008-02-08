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
** Scrolling textArea painter, simplification for fixed width:
** => MaximumTextWidth and textOfset in chars instead of pixels
** => calculation of line widths simply by multiplying characters in line with painterCharWidth
*/

public class TextAreaPainter_ScrollingFixFont extends TextAreaPainter {

  /*
  ** Variables 
  */
  
  /*
  ** maximum text width NOW IN CHARS INSTEAD OF PIXELS
  */
  
  protected int maximumTextWidth;
  
  /*
  ** fm.getMaxAdvance for fixed width characters 
  */
  
  protected int painterCharWidth;
  
  /*
  ** horizontal textscreen offset (pixels)
  */
  
  protected int textOffset;
  
  /*
  ** Constructor
  */
  
  public TextAreaPainter_ScrollingFixFont(Font textfont, FontMetrics metrics, int fixedwidth, String text, Dimension size, Color[] colors) {
    super(metrics,size,colors);
        
    painterFont = textfont; //RudolphPeer.DEFAULT_FONT;
    painterMetrics = metrics;
    painterCharWidth = fixedwidth;
    viewport.width/=fixedwidth; // fixed width is calculated in chars instead of pixels

    //text and text arrays
    setImage(text);

    //also calculate the new visible line parts
    textOffset=0;
    calculateVisibleLineparts();    
  }

  /*
  ** Text methods : overwrite setImage to just as well calculate the maximum width
  */

  public void setImage(String text) {
    super.setImage(text);
    calculateMaximumWidth();
  }  

  /*
  ** Special data access functions: since all widths are equal, we can calculate the horizontal lines in chars instead of pixels
  */
  
  public int getMaxAdvance() {
    // we always advance 1 char per click (we calculate in chars now, didn't I tell yet?
    return 1;
  }
    
  /*
  ** new screen size in chars and text lines instead of pixels and lines 
  */
  
  public void setSize(Dimension newsize) {
    //width and height in characters
    viewport.width = (RudolphTextAreaPeer.getInnerwidth(newsize.width))/painterCharWidth; //the effective width in chars
    viewport.height =  RudolphTextAreaPeer.getLines(newsize.height,painterMetrics);

    //also calculate the new visible line parts
    calculateVisibleLineparts();    
  }
    
  /*
  ** return Maximum width (in chars) 
  */

  public int getMaximumWidth() {
    return maximumTextWidth;
  }
    
  /*
  ** Get and set text offset (in chars)
  */
    
  public void setTextOffset(int offset) {
    textOffset = offset;
    calculateVisibleLineparts();
  }
    
  public void setOffset(int offsetx, int offsety) {  
    textOffset = offsetx;
    lineOffset = offsety;    
    calculateVisibleLineparts();
  }

  public int getTextOffset() {
    return textOffset;  
  }

  public Point getOffset() {
    return new Point(textOffset, lineOffset);  
  }
      
  /*
  ** calculate maximum line width (in chars)
  */

  protected void calculateMaximumWidth() {
    int start = textlineEnd[0];
    maximumTextWidth = start;
    for(int i=1;i<textlineEnd.length;i++) {
      if((textlineEnd[i]-start)>maximumTextWidth) {
        maximumTextWidth= textlineEnd[i]-start;
      }
      start = textlineEnd[i];
    }
  }

  /*
  ** calculate lines, startChars and StopChars for given text, viewport and offset
  */

  protected void calculateVisibleLineparts() {
    //security
    if(textBuffer.length<=0 || viewport.width<=0) {
      return;
    }

    int start=0;
    int currentwidth;
          
    for(int i=0;i<textlineEnd.length;i++) {
      if((start+textOffset)>=textlineEnd[i]) {
        // line ends left of viewport => nothing visible
        viewTextOffset[i] = textlineEnd[i];
        viewTextLength[i] = 0;
      }
      else {
        viewTextOffset[i] =  start+textOffset;
        if((textlineEnd[i]-viewTextOffset[i])>viewport.width) {
          // line ends right of viewport
          viewTextLength[i] = viewport.width;    
        }
        else {
          // line ends in viewport => length to show = line length = line end-line start
          viewTextLength[i] = textlineEnd[i] - viewTextOffset[i];     
        }
      }
      //start next line = end this
      start=textlineEnd[i];
    }
  }

  /*
  ** screen position to position in displayed text and vice versa
  ** in RrudolphTextAreaPeer painter:
  ** -> pointdata[3] is discrete position, invariable to offsets
  ** -> pointdata[2] is used to store the NUMBER OF CHARS counting from the beginning of the line
  **     (calculating to pixel screen offset is done in the paint() method)
  */

  public void setScreenPosition(int x, int y, int[] pointdata) {
    //security
    if(viewport.width<=0 || viewport.height<=0 ||textlineEnd.length<=0 ) {
      return;
    }
    // no of lines on screen, with offset
    pointdata[1] = RudolphTextAreaPeer.getLine(y, painterMetrics) + lineOffset;
    if(pointdata[1]>=textlineEnd.length) {
      // deeper then last line
      pointdata[1]= textlineEnd.length-1;
    }
        
    // x-position of the line in chars (the absolute position including the offset)
    pointdata[3] = RudolphTextAreaPeer.getTextPos(x)/painterCharWidth+viewTextOffset[pointdata[1]];   
    if(pointdata[3]>textlineEnd[pointdata[1]]) {
      //passed end of line if needed
      pointdata[3] = textlineEnd[pointdata[1]];
    }      

    pointdata[2] =(pointdata[1]>0)?pointdata[3]-textlineEnd[pointdata[1]-1]:pointdata[3];

    // in text string instead of text buffer, each line is ended by a '\n' sign => add 1 extra char for every line before current pos
    pointdata[0] = pointdata[3] + pointdata[1];
  }

  /*
  ** screen position to position in displayed text and vice versa
  ** in RrudolphTextAreaPeer painter:
  ** -> pointdata[3] is discrete position, invariable to offsets
  ** -> pointdata[2] is used to store the NUMBER OF CHARS counting from the beginning of the line
  **     (calculating to pixel screen offset is done in the paint() method)
  */

  public void setScreenPosition(int pos, int[] pointdata) {
    //security
    if(viewport.width<=0 || viewport.height<=0 ||textlineEnd.length<=0 ) {
      return;
    }
    //security
    if(pos<0) {
      //no selection
      pointdata[0] = 0;
      pointdata[1] = 0;
      pointdata[3] = 0;
      pointdata[2] = 0;
    }
    else if( pos >=(textBuffer.length+textlineEnd.length-1)) {
      //passed the end of buffer
      pointdata[0] = textBuffer.length+textlineEnd.length;
      pointdata[1] = textlineEnd.length-1;
      pointdata[3] = textBuffer.length;
      pointdata[2] = viewTextLength[textlineEnd.length-1];
    }
    else {
      pointdata[0] = pos;
      // calculate text's line
      pointdata[1]=0;
      while(pos>textlineEnd[pointdata[1]]) {
        pointdata[1]++;
        pos--;
      }
      pointdata[3] = pos;
      pointdata[2] =(pointdata[1]>0)?pos-textlineEnd[pointdata[1]-1]:pos;
    }
  }
    
  /*
  ** char position in line, offset to pointdata :
  ** pointData[1]: line and pointData[2] offset(in chars) are given.
  ** Up to us to calculate the corresponding text position and buffer position
  ** special case: when offset== -1 calculate the end of the line
  */

  public void setScreenPositionLine(int line, int offset, int[] pointdata) {
    if(line<0) {
      // set all to beginning of text (first line, first pos)
      pointdata[0]=0;
      pointdata[1]=0;
      pointdata[2]=0;
      pointdata[3]=0;
    }
    else if(line>=textlineEnd.length) {
      // set all to end of text
      pointdata[1]=textlineEnd.length-1;
      pointdata[2]=(pointdata[1]>0)?textlineEnd[pointdata[1]]-textlineEnd[pointdata[1]-1]:textlineEnd[pointdata[1]];
      pointdata[3]=textBuffer.length;
      pointdata[0]=pointdata[3]+pointdata[1];
    }
    else if (offset<0){
      pointdata[3]=textlineEnd[line];
      pointdata[2]=(line>0)?textlineEnd[line]-textlineEnd[line-1]:textlineEnd[line];
      pointdata[1]=line;
      pointdata[0]=pointdata[3]+line;
    }
    else {
      pointdata[1]=line;
      pointdata[3]=(line>0)? textlineEnd[line-1]+offset: offset;
      if(pointdata[3]>textlineEnd[line]) {
        pointdata[2]=(line>0)?textlineEnd[line]-textlineEnd[line-1]:textlineEnd[line];
        pointdata[3]=textlineEnd[line];
      }
      else {
        pointdata[2]=offset;
      }
      pointdata[0]=pointdata[3]+line;
    }
  }

  /*
  **  new paint for selections: Cursor and text offset are given in chars, we have to calculate them back to pixels
  */

  public void paint(int width, int height, int cursorline, int cursoroffset, int cursorscreenpos, Graphics g) {
    RudolphTextAreaPeer.paintTextArea( 0,0,width,height,
                                       textBuffer,viewTextOffset,viewTextLength,lineOffset,
                                       cursorline, (cursorscreenpos-textOffset)*painterCharWidth,
                                       painterFont, painterMetrics, textColors, g, drawCursor);  
  }

  public void paint(int width, int height, int startline, int startbufferoffset, int starthorizontaloffset,
                                           int stopline,  int stopbufferoffset,  int stophorizontaloffset,Graphics g) {
    RudolphTextAreaPeer.paintTextArea( 0,0,width,height,
                                       textBuffer,viewTextOffset,viewTextLength,lineOffset,
                                       startline, startbufferoffset, (starthorizontaloffset-textOffset)*painterCharWidth,
                                       stopline,  stopbufferoffset,  (stophorizontaloffset-textOffset)*painterCharWidth,
                                       painterFont, painterMetrics, textColors, g, drawCursor);  
  }
}

