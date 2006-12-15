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
**  TextArea Painter for scrolling texts:
** => features:
**   scrolling along a horizontal scrollbar: the lext lines are no longer limited to the viewports width.
**   A horizontal scrollbar searches out the parts of the text to be shown. As the scrollbar itself is handled by the main class,
**   the desired visible part is given to the painter by specifying a horizontal offset
** => Programming:
**   - Next to the viewport width, also notion of a maximum line width and a current line offset
**   - implementation of getMaximumWidth, getTextOffset, setTextOffset, getOffset & setOffset. if the offset is changed also recalculate
**      the visible parts
**   - rewriting calculateVisibleLineparts / new algotithm for viewTextOffset and viewTextLength arrays
**     in order to let them show the lines starting at a certain offset
**   - rewriting setScreenPosition functions to take into account the offset of the text lines
*/

public class TextAreaPainter_ScrollingVarFont extends TextAreaPainter {

  /*
  ** Variables
  */   

  /*
  ** maximum text width
  */

  protected int maximumTextWidth;
  
  /*
  ** horizontal textscreen offset (pixels)
  */
  
  protected int textOffset;

  /*
  ** Constructor
  */

  public TextAreaPainter_ScrollingVarFont(Font textfont, FontMetrics metrics, String text, Dimension size, Color[] colors) {
    super(textfont,metrics, text, size,colors);

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
  ** return Maximum width in pixels (with horizontal scrolling, we now have a maximum width and offset next to the viewport width)
  */

  public int getMaximumWidth() {
    return maximumTextWidth;
  }
    
  /*
  ** Offset calculations: (with horizontal scrolling, we now have a maximum width and offset next to the viewport width)
  ** Note that changing the text offset also changes the viewport
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
  ** Calculate the maximum line length of the current text
  */
  
  protected void calculateMaximumWidth() {
    int currentwidth;
    int start = textlineEnd[0];
    maximumTextWidth=painterMetrics.charsWidth(textBuffer,0,start);
    for(int i=1;i<textlineEnd.length;i++) {
      currentwidth =painterMetrics.charsWidth(textBuffer, start, textlineEnd[i]-start);
      if(currentwidth>maximumTextWidth) {
        maximumTextWidth = currentwidth;
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
    for(int i=0;i<textlineEnd.length;i++){
      viewTextOffset[i]=(textOffset>0)?RudolphPeer.getChars(textOffset,start,textBuffer,painterMetrics)+1:start;
      if(viewTextOffset[i]>=textlineEnd[i]) {
        viewTextOffset[i] = textlineEnd[i];
        viewTextLength[i] = 0;
      }
      else {
        viewTextLength[i] = RudolphPeer.getChars(viewport.width,viewTextOffset[i],textBuffer,painterMetrics);
        if(viewTextLength[i]>=textlineEnd[i]) {
          // ends inside viewport: length = line from offset to end
          viewTextLength[i] = textlineEnd[i]-viewTextOffset[i];
        }
        else {
          // visible part end not yet passed end of line=>subtract offset to get length
          viewTextLength[i]-= viewTextOffset[i];
        }
      }
    // for next line
    start=textlineEnd[i];
    }
  }
      
  /*
  ** screen position to position in displayed text and vice versa
  ** as base class, but we don't bother with the char offset anymore (calculated when painting)
  ** since Rectangle.height relates to abssolute positoin in the textbuffer, it is not affected by the offset
  ** => pointdata[0] : position of the selected character in the original text
  ** => pointdata[1] : line of the selected character on the screen
  ** => pointdata[2] : horizontal offset in pixels (or offset calculation value) of the selected character from the left side of the screen
  ** => pointdata[3] : position of the selected character in the TextPainter's text buffer (the text without the \n line breaks)
  */

  public void setScreenPosition(int x, int y, int[] pointdata) {
    //security
    if(viewport.width<=0 || viewport.height<=0 ||textlineEnd.length<=0 ) {
      return;
    }
    // no of lines on screen, with offset
    pointdata[1] = RudolphTextAreaPeer.getLines(y, painterMetrics) + lineOffset;
    if(pointdata[1]>=textlineEnd.length) {
      // calculated y deeper then last line => set to last line
      pointdata[1]= textlineEnd.length-1;
    }
       
    // total chars in the buffer before the given position
    pointdata[3]= RudolphPeer.getChars(RudolphTextAreaPeer.getTextPos(x), viewTextOffset[pointdata[1]], textBuffer,painterMetrics);    

    // Calculate back the offset in pixels to the end of the clicked character

    try {
      pointdata[2]= painterMetrics.charsWidth(textBuffer,viewTextOffset[pointdata[1]],pointdata[3]-viewTextOffset[pointdata[1]]) + textOffset;  
    }
    catch(ArrayIndexOutOfBoundsException e) {
      pointdata[2] = 0;
    }

    // in text string instead of text buffer, each line is ended by a '\n' sign => add 1 extra char for every line before current pos
    pointdata[0] = pointdata[3] + pointdata[1];
  }

  /*
  ** screen position to position in displayed text and vice versa
  ** as base class, but we don't bother with the char offset anymore (calculated when painting)
  ** since Rectangle.height relates to abssolute positoin in the textbuffer, it is not affected by the offset
  ** => pointdata[0] : position of the selected character in the original text
  ** => pointdata[1] : line of the selected character on the screen
  ** => pointdata[2] : horizontal offset in pixels (or offset calculation value) of the selected character from the left side of the screen
  ** => pointdata[3] : position of the selected character in the TextPainter's text buffer (the text without the \n line breaks)
  */

  public void setScreenPosition(int pos, int[] pointdata) {
    //security
    if(viewport.width<=0 || viewport.height<=0 ||textlineEnd.length<=0 ) {
      return;
    }

    if(pos<0) {
      pointdata[0] = 0;
      pointdata[1] = 0;
      pointdata[3] = 0;
      pointdata[2] = 0;
    }
    else if( textlineEnd.length <= 1 && pos >=textBuffer.length) {
      //Only one line and we passed it
      pointdata[0] = textBuffer.length;
      pointdata[1] = 0;// first line, that's obvious
      pointdata[2] = painterMetrics.charsWidth(textBuffer, 0, textBuffer.length);
      pointdata[3] = textBuffer.length;
    }
    else if( pos >=(textBuffer.length+textlineEnd.length-1)) {
      //passed the end of buffer
      pointdata[0] = textBuffer.length+textlineEnd.length;
      pointdata[1] = textlineEnd.length-1;
      // width: total chars width from start of last screen line, over <length of last screen line> chars
      pointdata[2] = painterMetrics.charsWidth(textBuffer, textlineEnd[pointdata[1]-1], textBuffer.length - textlineEnd[pointdata[1]-1]);
      pointdata[3] = textBuffer.length;
    }
    else {
      pointdata[0] = pos;
      // calculate lines from text pos (NOT buffer pos):
      // while text pos > # chars before end of current line
      // ->look next line
      // -> decrease pos by 1 \n character for the line break jumped
      pointdata[1] = 0;
      while(pos>textlineEnd[pointdata[1]]) {
        pointdata[1]++;
        pos--;
      }
      pointdata[3] = pos;
      // offset in pixels calculated through FontMetrics.CharsWidth(buffer, start, length)
      pointdata[2]=(pointdata[1]>0)? painterMetrics.charsWidth(textBuffer,textlineEnd[pointdata[1]-1],pos-textlineEnd[pointdata[1]-1]):
                                       painterMetrics.charsWidth(textBuffer,0,pos);  
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
      pointdata[2]=(pointdata[1]>0)? painterMetrics.charsWidth(textBuffer, textlineEnd[pointdata[1]-1], textBuffer.length-textlineEnd[pointdata[1]-1]) :
                                      painterMetrics.charsWidth(textBuffer, 0, textBuffer.length);
      pointdata[3]=textBuffer.length;
      pointdata[0]=pointdata[3]+pointdata[1];
    }
    else if(offset<0) {
      pointdata[1]=line;
      pointdata[3]=textlineEnd[line];
      pointdata[2]=(line>0)? painterMetrics.charsWidth(textBuffer, textlineEnd[line-1], textlineEnd[line] - textlineEnd[line-1]):
                              painterMetrics.charsWidth(textBuffer, 0, textlineEnd[line]);
      pointdata[0]=pointdata[3]+line;
    }
    else if(offset==0) {
      //speedy form, no offset in pixels means no offset in chars
      pointdata[3]=(line>0)?textlineEnd[line-1] : 0;
      pointdata[2]=0;
      pointdata[1]=line;
      pointdata[0]=pointdata[3]+line;
    }
    else{
      pointdata[1]=line;
      pointdata[3]=(line>0)?RudolphPeer.getChars(offset, textlineEnd[line-1], textBuffer,painterMetrics):
                             RudolphPeer.getChars(offset, 0, textBuffer,painterMetrics);
      if(pointdata[3]>textlineEnd[line]) {
        pointdata[3]=textlineEnd[line];
        pointdata[2]=(line>0)? painterMetrics.charsWidth(textBuffer, textlineEnd[line-1], textlineEnd[line] - textlineEnd[line-1]):
                                painterMetrics.charsWidth(textBuffer, 0, textlineEnd[line]);
      }
      else {
        pointdata[2]=offset;
      }
      pointdata[0]=pointdata[3]+line;
    }
  }

  /*
  **  new paint for selections: viewTextOffset and ViewTextLength take care of the lines to display, but we still have the text offset
  ** to subtract from the cursor and selection positions(in pixels)
  ** Moreover, since each line starts at the beginning of the screen with a whole character, we have to calculate the pixel offset
  ** for cursor and selections on the fly
  */

  public void paint (int width, int height, int cursorline, int cursoroffset, int cursorscreenpos, Graphics g){
    if(cursorline >= viewTextOffset.length) {
      cursorline = viewTextOffset.length - 1;
    }

    int offset = viewTextOffset[cursorline];
    
    if(cursoroffset >= offset && offset <= textBuffer.length && cursoroffset <= textBuffer.length) {
      cursorscreenpos = painterMetrics.charsWidth(textBuffer, offset, cursoroffset - offset);
    }
    else {
	  wonka.vm.Etc.woempa(9, "set cursorscreenpos to -1");
	  wonka.vm.Etc.woempa(9, "cursoroffset: "+cursoroffset);
	  wonka.vm.Etc.woempa(9, "offset: "+offset);
	  wonka.vm.Etc.woempa(9, "textBuffer.length: "+textBuffer.length);
	  
      cursorscreenpos=-1;
    }
    //paint area with cursor
    RudolphTextAreaPeer.paintTextArea( 0,0,width,height,
                                       textBuffer, viewTextOffset, viewTextLength, lineOffset,
                                       cursorline, cursorscreenpos,
                                       painterFont, painterMetrics, textColors, g, drawCursor);  
  }

  public void paint(int width, int height, int startline, int startbufferoffset, int startscreenpos,
                                           int stopline,  int stopbufferoffset,  int stopscreenpos, Graphics g) {
     // recalculate screen pos to new value using the buffer pos and the current buffer viewtextoffset
     if(startbufferoffset>viewTextOffset[startline]){
       int temp = startbufferoffset-viewTextOffset[startline];
       if(temp > textBuffer.length - viewTextOffset[startline]) temp = textBuffer.length - viewTextOffset[startline];
       startscreenpos = painterMetrics.charsWidth(textBuffer,viewTextOffset[startline],temp);
     }
     else{
       startscreenpos=-1; // strictly startscreenpos-= textOffset, but any number smaller then 0 will do
     }
     if(stopbufferoffset>viewTextOffset[stopline]){
       int temp = stopbufferoffset-viewTextOffset[stopline];
       if(temp > textBuffer.length - viewTextOffset[stopline]) temp = textBuffer.length - viewTextOffset[stopline];
       stopscreenpos = painterMetrics.charsWidth(textBuffer,viewTextOffset[stopline],temp);
     }
     else{
       stopscreenpos=-1; // strictly startscreenpos-= textOffset, but any number smaller then 0 will do
     }
     //stopscreenpos = painterMetrics.charsWidth(textBuffer,viewTextOffset[stopline],stopbufferoffset-viewTextOffset[stopline]);
     RudolphTextAreaPeer.paintTextArea(
                                       0,0,width,height,
                                       textBuffer,viewTextOffset,viewTextLength,lineOffset,
                                       startline, startbufferoffset, startscreenpos,
                                       stopline,  stopbufferoffset,  stopscreenpos,
                                       painterFont, painterMetrics, textColors, g, drawCursor
                                     );  
  }

}

