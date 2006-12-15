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
** TextArea Painter for line-wrapped texts, simplification for fixed-width fonts
** => Programming:
**   - As we the number of lines varies with the screen width, textlineEnd[] wil no longer be a fixed value of the text.
**     Instead we store the text as a whole, including the '\n' endline characters and recalculating textlineEnd[] along
**     with the visible parts
**   - rewritten the two setScreenPosition functions to take into account the new storage method
**   - with viewport.width in characters, calculateVisibleLineparts can just take the number of chars in the word for the words width,
**     the space width becomes 1(char)
*/

public class TextAreaPainter_WrappingFixFont extends TextAreaPainter {

  protected int painterCharWidth;      // = fm.getMaxAdvance for fixed width characters, -1 for variable width
  
  /*
  ** Constructor
  */

  public TextAreaPainter_WrappingFixFont(Font textfont, FontMetrics metrics, int fixedwidth, String text, Dimension size, Color[] colors) {
    super(metrics,size,colors);
    viewport.width/=fixedwidth; // fixed width is calculated in chars instead of pixels
        
    painterFont = textfont; //RudolphPeer.DEFAULT_FONT;
    painterMetrics = metrics;
    painterCharWidth = fixedwidth;
     
    //text and text arrays
    setImage(text);
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

    //also calculate the nwe visible line parts
    calculateVisibleLineparts();    
  }
    
  /*
  ** Store the new text in buffer. Store the full text including the '/n' endline marks and call calculateVisibleLineparts
  */

  public void setImage(String text) {        
    //initialise text buffer and fill with complete text
    textBuffer = new char[text.length()];// we include the breaks for easyer line calculation
    text.getChars(0,textBuffer.length,textBuffer,0);
          
    //calculate the lines
    if(viewport.width>0) {
      //build new visible text arrays
      calculateVisibleLineparts();
    }
    else {
      //by default set end buffer to int[0] so getLines returns a value
      textlineEnd = new int[0];
    }
  }
    
  /*
  ** calculate lines, startChars and StopChars for given text, and viewport
  ** with viewport.width in characters, the words width, is simply the number of characters in that word, the space width becomes 1(char)
  ** Note that the actual number of pieces will be bigger then the number of text lines
  */

  protected void calculateVisibleLineparts() {
    //security
    if(textBuffer.length<=0 || viewport.width<=0) {
      textlineEnd = new int[0];
      viewTextOffset = new int[0];
      viewTextLength = new int[0];
      return;
    }
    // ALGORITHM:
    // For simplicity, we use a tree-step algorithm:
    // STEP ONE: we break the text into words, storing each word in a temporary array. As we're using a char[] buffer and start-stop-position
    // arrays, we'll 'store' the words as a series of 3 arrays: int wordstart, int wordlength and boolean newline
    // STEP TWO: we'll re-calculate the length of line through its subsequent word length, splitting the line in the middle every time the
    // length exceeds the viewport length (splitting is simply done by inserting a new 'true' in the newline buffer)
    // STEP THREE: we assing a new pair of viewTextOffset/viewTextLength arrays and fill them with the lines
    // STEP ONE:
    // calculate number of words == number of space characters + number of lines
    int i; //we'll use the for(int i==.. ;i<... ;i++) for-next loop so often, we can just use a common i
    int words=1;
    for(i=0;i<textBuffer.length; i++) {
      if(textBuffer[i]==' ' || textBuffer[i]=='\n'|| textBuffer[i]=='.' || textBuffer[i]==',' || textBuffer[i]=='?' || textBuffer[i]==':' || textBuffer[i]==';') {
        words++;
      }
    }  
      
    // build arrays for word start, stop and newline
    int[] wordstart = new int[words];
    int[] wordstop = new int[words];
    boolean[] newline = new boolean[words];
      
    // fill arrays with words
    int line=0;
    //first line (length will be calculated out of next line start)
    wordstart[0]=0;
    newline[0]=true;
    //all lines in between
    for(i=1; i<(textBuffer.length-1); i++) {
      if(textBuffer[i]=='\n' || textBuffer[i]==' ' || textBuffer[i]=='.' || textBuffer[i]==',' || textBuffer[i]=='?' || textBuffer[i]==':' || textBuffer[i]==';') {
        if(textBuffer[i]=='\n' || textBuffer[i]==' ' ) {
          wordstop[line]=i;
        }
        else {
          wordstop[line]=i+1;
        }

        line++;
        wordstart[line]=i+1; //skip the /n-character
        newline[line] = (textBuffer[i]=='\n'); //true if end of line, false otherwise
      }
    }    
    wordstop[line]= textBuffer.length;

    //STEP TWO:    
    // add <newline=true; > break commands whenever the width of the chars up to now exceeds the viewport width
    //first simplification: A word longer then the current viewport will always start on a new line and occupy the whole line
    //so the word after them has to start on a new line just as well
    int last = words-1;
    for(i=0; i<last; i++) {
      if((wordstop[i]-wordstart[i])>=viewport.width) {
        newline[i]=true;
        wordstop[i]= wordstart[i]+viewport.width;
        newline[i+1]=true;
      }
    }
    //last line
    if((wordstop[last]-wordstart[last])>=viewport.width) {
      newline[last]=true;
      wordstop[last]= wordstart[last]+viewport.width;
    }

    // insert a new line flag every time the current length doesn't fit on one line
    line=1;
    int linestop= viewport.width;
    for(i=1; i<words; i++) {
      if(newline[i] || wordstop[i]>=linestop) {
        // either new line or too large for viewport:
        newline[i]=true;
        linestop=wordstart[i]+viewport.width;
        line++;
      }  
    }
        
    //STEP THREE:    
    // Copy the line start and stop into the viewTextOffset[]/viewTextLength[] buffers
    textlineEnd = new int[line];
    viewTextOffset = new int[line];
    viewTextLength = new int[line];
        
    line=0;
    viewTextOffset[0]=wordstart[0];
    for(i=1; i<words; i++) {
      if(newline[i]) {
        textlineEnd[line]=wordstop[i-1];
        viewTextLength[line]=wordstop[i-1]-viewTextOffset[line];
        line++;
        viewTextOffset[line]=wordstart[i];
      }
    }
    //last line, skip ending /n
    textlineEnd[line]=(textBuffer[textBuffer.length-1]=='\n')?textBuffer.length-1:textBuffer.length;
    viewTextLength[line]=textlineEnd[line]-viewTextOffset[line];
  }

  /*
  ** screen position to position in displayed text and vice versa As we include the line-breaks in the buffer, there is no difference
  ** between buffer position and TextAreaPainter text string position
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
         
    // x-position of the line in in chars
    pointdata[2] = RudolphTextAreaPeer.getTextPos(x)/painterCharWidth;
    if(pointdata[2]>viewTextLength[pointdata[1]]) {
      // we clicked past the end of the current line => adjust to that line
      pointdata[2]=viewTextLength[pointdata[1]];
    }
    // text position
    pointdata[3] = viewTextOffset[pointdata[1]] + pointdata[2];

    // as the wrapping text buffer DOES include \n line breaks, the screen text position IS the buffer position
    pointdata[0] = pointdata[3];
  }

  /*
  ** screen position to position in displayed text
  */

  public void setScreenPosition(int pos, int[] pointdata) {
    //System.out.println( "FixedWrappingTAPainter: setScreenPosition : finding data for TextPoint pos "+pos);
    if(viewport.width<=0 || viewport.height<=0 ||textlineEnd.length<=0 ) {
      return;
    }
    //security
    if(pos<0) {
      //no selection
      pointdata[0] = 0;
      pointdata[1] = 0;
      pointdata[2] = 0;
      pointdata[3] = 0;
    }
    else if( pos >=textBuffer.length) {
      //passed the end of buffer
      pointdata[0] = textBuffer.length;
      pointdata[1] = textlineEnd.length-1;
      pointdata[2] = viewTextLength[pointdata[1]];
      pointdata[3] = textBuffer.length;
    }
    else {
      //Ok, in range
      pointdata[0] = pos;
      //calculate line out of textlineEnd[]
      pointdata[1]=0;
      while(pos>textlineEnd[pointdata[1]]) {
        pointdata[1]++;
      }
      pointdata[2] = pos-viewTextOffset[pointdata[1]];  
      // the wrapping text buffer also includes end lines, so the textbuffer position is the text string position
      pointdata[3] = pos;
    }
  //System.out.println( "  => found line:"+pointdata[1]+ " Char:"+pointdata[3]+", offset:"+pointdata[2]);
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
      pointdata[0]=textBuffer.length;
      pointdata[1]=textlineEnd.length-1;
      pointdata[2]=viewTextLength[pointdata[1]];
      pointdata[3]=textBuffer.length;
    }
    else if(offset<0) {
      pointdata[0]=textlineEnd[line];
      pointdata[1]=line;
      pointdata[2]=viewTextLength[line];
      pointdata[3]=textlineEnd[line];
    }
    else {
      pointdata[1]=line;
      if(offset>viewTextLength[line]) {
        pointdata[2]=viewTextLength[line];
        pointdata[0]=textlineEnd[line];
      }
      else {
        pointdata[0]=(line>0)? textlineEnd[line-1]+offset+1: offset;
        pointdata[2]=offset;
      }
      pointdata[3]=pointdata[0];
    }
  }

  /*
  **  new paint for selections: Cursor and selection offset are given in chars, we have to calculate them back to pixels
  */

  public void paint(int width, int height, int cursorline, int cursoroffset, int cursorscreenpos, Graphics g) {
    RudolphTextAreaPeer.paintTextArea( 0,0,width,height,
                                       textBuffer,viewTextOffset,viewTextLength,lineOffset,
                                       cursorline, cursorscreenpos*painterCharWidth,
                                       painterFont, painterMetrics, textColors, g, drawCursor);  
  }

  public void paint(int width, int height, int startline, int startbufferoffset, int starthorizontaloffset,
                                           int stopline,  int stopbufferoffset,  int stophorizontaloffset,Graphics g) {
    RudolphTextAreaPeer.paintTextArea( 0,0,width,height,
                                       textBuffer,viewTextOffset,viewTextLength,lineOffset,
                                       startline, startbufferoffset, starthorizontaloffset * painterCharWidth,
                                       stopline,  stopbufferoffset,  stophorizontaloffset * painterCharWidth,
                                       painterFont, painterMetrics, textColors, g, drawCursor);  
  }
}

