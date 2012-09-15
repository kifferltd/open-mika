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
** TextArea Painter for line-wrapped texts:
** => features:
**      The text stays in the horizontal area of the viewport, but is 'wrapped' to fit the viewport: lines longer then the viewport
**      are split into parts of several words that do fit. Note that this implies that the actual number of lines in the painter
**      can be bigger then the number of lines of the original text
** => Programming:
**   - setImage just stores the COMPLETE text in the buffer, including the line break characters. textlineEnd[] now depends
**   on the screen width and will be calculated in the calculateVisibleLineparts algotithm just as well
**   - calculateVisibleLineparts is written to  calculate the lineparts that fit in the viewport (as described above)
**   - rewriting the two setScreenPosition() functions to calculate the position from a buffer including the end marks
*/

public class TextAreaPainter_WrappingVarFont extends TextAreaPainter {  
  
  /*
  ** Constructor
  */

  public TextAreaPainter_WrappingVarFont(Font textfont, FontMetrics metrics, String text, Dimension size, Color[] colors) {
    super(textfont,metrics, text, size,colors);
  }
  
  /*
  ** Store the new text in buffer. Store the full text including the '/n' endline marks and then call calculateVisibleLineparts
  */

  public void setImage(String text) {        
    //initialise text buffer we include the breaks for easyer line calculation      
    textBuffer = new char[text.length()];    
    //fill the buffer (the whole buffer and nothing but the buffer...)
    text.getChars(0,textBuffer.length,textBuffer,0);
          
    //calculate the lines
    if(viewport.width>0) {
      //build new visible text arrays
      calculateVisibleLineparts();
    }
    else {
      //by default set end buffer to int[0] so getLines() returns a value
      textlineEnd = new int[0];
    }
  }
    
  /*
  ** calculate lines, startChars and StopChars for given text, and viewport
  ** with viewport.width in characters, the words width, is simply the number of characters in that word, the space width becomes 1(char)
  ** Note that the actual number of pieces will be bigger then the number of text lines
  */

  protected void calculateVisibleLineparts()  {
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
    // arrays, we'll 'store' the words as a series of 3 int-arrays: wordstart and wordstop position, wordlength in pixels
    // and one boolean[] newline
    // STEP TWO: we'll re-calculate the length of line through its subsequent word length, splitting the line in the middle every time the
    // length exceeds the viewport length (splitting is simply done by inserting a new 'true' in the newline buffer for every new line)
    // STEP THREE: we assing a new pair of viewTextOffset/viewTextLength arrays and fill them with the lines
      
    // STEP ONE:
    // calculate number of words == number of space characters + number of lines
    int i; //we'll use the for(int i==.. ;i<... ;i++) for-next loop so often, we can just use a common i
    int words=1;
    for(i=0;i<textBuffer.length; i++) {
      if(textBuffer[i]==' ' || textBuffer[i]=='\n'
      || textBuffer[i]=='.' || textBuffer[i]==',' || textBuffer[i]=='?' || textBuffer[i]==':' || textBuffer[i]==';')
      words++;
    }  

    // build arrays for word start, stop and newline
    int[] wordstart = new int[words];
    int[] wordlen = new int[words];  //length in characters
    int[] wordwidth = new int[words]; // width in pixels
    char[] startchar = new char[words]; //true if starts on a new line
      
    // fill arrays with words
    int line=0;
    //first line (length & pixel width will be calculated out of next line start)
    wordstart[0]=0;
    startchar[0]='\n';// first line starts on new line
    //all lines in between
    for(i=1; i<(textBuffer.length-1); i++) {
      if(textBuffer[i]=='\n' || textBuffer[i]==' ' || textBuffer[i]=='.' || textBuffer[i]==',' || textBuffer[i]=='?' || textBuffer[i]==':' || textBuffer[i]==';') {

        if(textBuffer[i]=='\n' || textBuffer[i]==' ' ) {
          // a space or endchar terminated word stops on the cpace or endchar and does not include that char
          wordlen[line] = i-wordstart[line];
        }
        else {
          // a word ending on a lexigraphical sign includes that sign
          wordlen[line] = i-wordstart[line]+1;
        }
        // word width in pixels
        wordwidth[line]= painterMetrics.charsWidth(textBuffer,wordstart[line],wordlen[line]);
        // start for next line
        line++;
        wordstart[line]=i+1; //we start at the next character
        startchar[line] = textBuffer[i]; // we store the ending char of last line to remember if we should start with a new line or a new space
      }
    }    
    //last line length
    wordlen[line]= textBuffer.length-wordstart[line];
    wordwidth[line]=painterMetrics.charsWidth(textBuffer,wordstart[line],wordlen[line]);
        
    //STEP TWO:    
    // add <newline=true; > break commands whenever the width of the chars up to now exceeds the viewport width
    //first simplification: A word longer then the current viewport will always start on a new line and occupy the whole line
    //so the word after them has to start on a new line just as well
    int last = words-1;
    for(i=0; i<last; i++) {
      if(wordwidth[i]>=viewport.width) {
        startchar[i]='\n';
        wordlen[i]= RudolphPeer.getChars(viewport.width, wordstart[i], textBuffer, painterMetrics) -wordstart[i];
        wordwidth[i]=viewport.width;
        startchar[i+1]='\n';
      }
    }
    if(wordwidth[i]>=viewport.width) {
      startchar[last]='\n';
      wordlen[last]= RudolphPeer.getChars(viewport.width, wordstart[last], textBuffer, painterMetrics) -wordstart[last];
      wordwidth[last]=viewport.width;
    }

    line=1;
    int curwidth=wordwidth[0];
    int spacechar=painterMetrics.charWidth(' ');
    for(i=1; i<words; i++) {
      if(startchar[i]=='\n') {
        // the word already starts at new line, so reset the linewidth counter and increase the line counter
        curwidth=wordwidth[i];
        line++;
      }
      else if (startchar[i]==' ') {
        // the word starts with a space, so look if we can include space and word length into the current line width
        curwidth+=wordwidth[i]+spacechar;
        if(curwidth>viewport.width) {
          // we can't, so start on a new line
          startchar[i]='\n';
          curwidth=wordwidth[i];
          line++;      
        }
      }
      else
      {
        // the word starts with lexigraphical sign, so look if we can include word length into the current line width
        curwidth+=wordwidth[i];
        if(curwidth>viewport.width) {
          // we can't, so start on a new line
          startchar[i]='\n';
          curwidth=wordwidth[i];
          line++;      
        }
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
      if(startchar[i]=='\n') {
        //new line starts here, so terminate previous line by end of previous word
        textlineEnd[line]=wordstart[i-1]+wordlen[i-1];
        viewTextLength[line]=textlineEnd[line] - viewTextOffset[line];
        // add a new line starting with the new word position
        line++;
        viewTextOffset[line]=wordstart[i];
      }
    }
    //last line, if the line ends on a final newline sign, skip that sign
    textlineEnd[line]=(textBuffer[textBuffer.length-1]=='\n')?textBuffer.length-1:textBuffer.length;
    viewTextLength[line]=textlineEnd[line]-viewTextOffset[line];    
  }
  
  /*
  ** screen position to position in displayed text and vice versa. As the text buffer now includes the /n endline chars,
  ** there is no difference between buffer position and TextAreaPainter text string position
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
      // deeper then last line
      pointdata[1]= textlineEnd.length-1;
    }    

    // Calculate buffer position by means of RudolphPeer function
    x= RudolphTextAreaPeer.getTextPos(x); // adjust to painter margins
    pointdata[3]= RudolphPeer.getChars(RudolphTextAreaPeer.getTextPos(x), viewTextOffset[pointdata[1]], textBuffer,painterMetrics);    
    if(pointdata[3]>textlineEnd[pointdata[1]]) {
      //passed end of line
      pointdata[3]=textlineEnd[pointdata[1]];
    }
    // (in wrapping textarea: textlineEnd[i]=viewTextOffset[i]+viewTextLengt[i] = last VISIBLE char of line)
    if(pointdata[3]>textlineEnd[pointdata[1]]) {
      // If passed the end of line, adjust to the end
      pointdata[3]=textlineEnd[pointdata[1]];
    }
    // calculate the horizontal pos back from the distance between total chars and end of last line using FontMetrics.charWidth
    pointdata[2]= painterMetrics.charsWidth(textBuffer,viewTextOffset[pointdata[1]], pointdata[3]-viewTextOffset[pointdata[1]]);  

    // as line wrapping buffer INCLUDES \n line breaks, text string position = buffer position
    pointdata[0] =  pointdata[3];
  }

  /*
  ** position in text to screen position line + screen offset(pixels)
  */

  public void setScreenPosition(int pos, int[] pointdata) {
    //security
    if(viewport.width<=0 || viewport.height<=0 ||textlineEnd.length<=0 ) {
      return;
    }

    if(pos<0) {
      //no selection
      pointdata[0]= 0;
      pointdata[1]= 0;
      pointdata[2]= 0;
      pointdata[3]= 0;
    }
    else if( pos >=textBuffer.length) {
      //passed the end of buffer
      pointdata[0]=textBuffer.length;
      pointdata[1]=textlineEnd.length-1;
      pointdata[2]=painterMetrics.charsWidth(textBuffer,viewTextOffset[pointdata[1]],viewTextLength[pointdata[1]]);
      pointdata[3]=textBuffer.length;
    }
    else {
      // in range
      pointdata[0] = pos;
      // calculate line by comparing to line-end lengths
      pointdata[1] = 0;
      while(pos>(viewTextOffset[pointdata[1]]+viewTextLength[pointdata[1]])) {
        pointdata[1]++;
      }
      pointdata[2]= painterMetrics.charsWidth(textBuffer,viewTextOffset[pointdata[1]],pos-viewTextOffset[pointdata[1]]);  
      pointdata[3] = pos;
    }
  }

  /*
  ** char position in line, offset to pointdata :
  ** pointData[1]: line and pointData[2] offset(in chars) are given.
  ** Up to us to calculate the corresponding text position and buffer position
  ** special case: when offset== -1 calculate the end of the line
  ** as all lines smaller then screen width, we know that the TOTAL length of a line equals viewTextLength[line]
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
      pointdata[2]=(pointdata[1]>0)? painterMetrics.charsWidth(textBuffer, textlineEnd[pointdata[1]-1]+1, viewTextLength[pointdata[1]]):
                                      painterMetrics.charsWidth(textBuffer, 0, textBuffer.length);
      pointdata[3]=pointdata[0];
    }
    else if(offset<0) {
      pointdata[0]=textlineEnd[line];
      pointdata[1]=line;
      pointdata[2]=(line>0)? painterMetrics.charsWidth(textBuffer, textlineEnd[line-1]+1, viewTextLength[line]):
                              painterMetrics.charsWidth(textBuffer, 0, textlineEnd[line]);
      pointdata[3]=pointdata[0];
    }
    else if(offset==0) {
      //speedy form, no offset in pixels means no offset in chars
      pointdata[0]=(line>0)?textlineEnd[line-1]+1 : 0;
      pointdata[1]=line;
      pointdata[2]=0;
      pointdata[3]=pointdata[0];
    }
    else {
      pointdata[1]=line;
      pointdata[0]=(line>0)?RudolphPeer.getChars(offset, textlineEnd[line-1]+1, textBuffer,painterMetrics):
                             RudolphPeer.getChars(offset, 0, textBuffer,painterMetrics);
      if(pointdata[0]>textlineEnd[line]) {
        pointdata[0]=textlineEnd[line];
        pointdata[2]=(line>0)? painterMetrics.charsWidth(textBuffer, textlineEnd[line-1]+1, viewTextLength[line]):
                                painterMetrics.charsWidth(textBuffer, 0, textlineEnd[line]);
      }
      else {
        pointdata[2]=offset;
      }
      pointdata[3]=pointdata[0];
    }
  }

  /*
  ** paint
  */

  public void paint (int width, int height, int cursorline, int cursoroffset, int cursorscreenpos, Graphics g){
    RudolphTextAreaPeer.paintTextArea( 0, 0, width, height,
                                       textBuffer, viewTextOffset, viewTextLength, lineOffset,
                                       cursorline, cursorscreenpos,
                                       painterFont, painterMetrics, textColors, g, drawCursor);  
  }

  public void paint(int width, int height, int startline, int startbufferoffset, int starthorizontaloffset,
                                           int stopline,  int stopbufferoffset,  int stophorizontaloffset,Graphics g) {
    RudolphTextAreaPeer.paintTextArea( 0,0,width,height,
                                       textBuffer,viewTextOffset,viewTextLength,lineOffset,
                                       startline, startbufferoffset, starthorizontaloffset,
                                       stopline,  stopbufferoffset,  stophorizontaloffset,
                                       painterFont, painterMetrics, textColors, g, drawCursor);  
  }
}

