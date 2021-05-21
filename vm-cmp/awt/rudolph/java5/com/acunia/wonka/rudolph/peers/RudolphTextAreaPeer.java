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

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

/*
** java.awt drawn TextArea peer for our own uses
** This paints a default 'rudolph' TextArea center and fills it with a given text. furthermore, it initialises (fixed) TextArea
** font and font dimensions
**  Using this peer for all paint-instances lets you quickly make a new layout for the Text areas needed,
*/

public class RudolphTextAreaPeer extends RudolphPeer {

  /*
  ** static variables
  */
  
  /*
  ** @note: the Java specs define the default font to be platform specific.
  */
  
  public static final Font DEFAULT_FONT = new Font("courP14", 0, 14); //new Font("courP17", 0, 17);

  /*
  ** @note: the Java specs define the default number of rows and colums to be platform specific.
  */
  
  public final static int DEFAULTTEXTCHARS = 20;
  public final static int DEFAULTTEXTLINES  = 5;
  //area width for border scrolling
  public final static int BORDER  = 16;

  /*
  **  paint a text area from a given set of charbufer and start/length position arrays. A cursor is specified by his y-position and offset
  **
  ** @status This is a plug-in allowing you to configure the way the text area text is displayed. Feel free to change
  ** @variables
  **   int x0, int y0:              : the offset to the topleft corner (x=0,y=0) ofthe component where the TextArea has to be drawn
  **   int width, int height,       : the width and height of the text display area (the TextArea without the scrollbars)
  **   char[] text, int[] starts, int[] lengths : the lines of text to be displayed. With the buffer/starts[]/lengths[] format
  **                                                the <x>-th line of text to be displayed is the array of chars from the text buffer
  **                                                starting at buffer char number <starts[x]> and having length <lengths[x]>
  **                                                (the corresponding String being new String(buffer, starts[x],lengths[x]) )
  **   int lineoffset,                          : the vertical offset of visible lines. The visible part of the text stretches from line
  **                                                <lineoffset> to line <lineoffset + getLines(height)>
  **   int cursorline, int cursoroffset         : the line of the cursor, the x-position of the cursor on the screen
  **   Font f, FontMetrics fm,                  : the desired font and its FontMetrics
  **   Color[] peercolors:          : an Color[5] array specifying the colors in which to paint the bar (see RudolpPeer for detains)
  **   Graphics g                   : our Graphics instance as given in Component.paint(Graphics g)
  */

  public static void paintTextArea( int x0, int y0, int width, int height,
                                    char[] text, int[] starts, int[] lengths, int lineoffset,
                                    int cursorline, int cursoroffset,
                                    Font f, FontMetrics fm, Color[] peercolors, Graphics g, boolean drawCursor) {
    //textarea box
    g.setColor(peercolors[3]); //dark
    g.drawLine(x0+1,y0+1,x0+width-2, y0+1);
    g.drawLine(x0+1,y0+1,x0+1, y0+height-2);
    g.setColor(peercolors[2]); //light
    g.drawLine(x0+width-2,y0+1,x0+width-2, y0+height-2);
    g.drawLine(x0+1,y0+height-2,x0+width-2, y0+height-2);
        
    //some variables for the text display
    int lineheight = fm.getHeight();
    int xt = x0+5;
    int yt = y0+2+lineheight-fm.getDescent();
    // last line to be displayed
    int lastline = (height-3)/lineheight+lineoffset;
    if(lastline>=starts.length) {
      lastline =starts.length;
    }
              
    //display text
    g.setFont(f);
    g.setColor(peercolors[4]);
    for(int i=lineoffset; i<lastline; i++) {
      g.drawChars(text,starts[i],lengths[i], xt, yt);
      yt+=lineheight;
    }     
         
    //cursor mark : draw a simple I before the cursor character position
    if(drawCursor && cursorline>=lineoffset && cursorline<lastline && cursoroffset>=0 && cursoroffset<(width-4)) {
      //Okay, in visible area => draw a simple I on the desired line, desired cursor offset
      yt=y0+2+(cursorline-lineoffset)*lineheight;
      xt=x0+5+cursoroffset;
      g.setColor(peercolors[3]);    
      g.drawLine(xt,yt,xt,yt+lineheight);
      g.drawLine(xt-2,yt,xt+2,yt);
      g.drawLine(xt-2,yt+lineheight,xt+2,yt+lineheight);
    }
	else
	{
		wonka.vm.Etc.woempa(9, "don't draw Cursor");
		
		wonka.vm.Etc.woempa(9, "drawCursor: "+drawCursor);
		wonka.vm.Etc.woempa(9, "cursorline: "+cursorline);
		wonka.vm.Etc.woempa(9, "lineoffset: "+lineoffset);
		wonka.vm.Etc.woempa(9, "lastline: "+lastline);
		wonka.vm.Etc.woempa(9, "cursoroffset: "+cursoroffset);
		wonka.vm.Etc.woempa(9, "width: "+width);
	}
    //Ok, done...
  }

  /*
  **  paint a text area from a given set of charbufer and start/length position arrays.
  ** additionally there is a selection starting at a point charno/lineno with given horizontal offset
  ** and ending at a point charno/lineno with given horizontal offset
  ** @status This is a plug-in allowing you to configure the way the text area text is displayed. Feel free to change
  ** @variables
  **   int x0, int y0:              : the offset to the topleft corner (x=0,y=0) ofthe component where the TextArea has to be drawn
  **   int width, int height,       : the width and height of the text display area (the TextArea without the scrollbars)
  **   char[] text, int[] starts, int[] lengths      : the lines of text to be displayed. With the buffer/starts[]/lengths[] format
  **                                                   the <x>-th line of text to be displayed is the array of chars from the text buffer
  **                                                   starting at buffer char number <starts[x]> and having length <lengths[x]>
  **                                                   (the corresponding String being new String(buffer, starts[x],lengths[x]) )
  **   int lineoffset,                               : the vertical offset of visible lines. The visible part of the text stretches from line
  **                                                  <lineoffset> to line <lineoffset + getLines(height)>
  **   int startline, int startchar, int startoffset : the start of the selection defined by => the line on which the selection starts,
  **                                                   => the number of the starting character in the text buffer and
  **                                                   => the offset of the selection start text on the horizontal axis
  **   int stopline, int stopchar, int stopoffset    : the stop of the selection equally defined by => the line on which the selection stops,
  **                                                   => the number of the first character NO LONGER SELECTED in the text buffer and
  **                                                   => the offset of the selection stop text on the horizontal axis
  **   Font f, FontMetrics fm,                       : the desired font and its FontMetrics
  **   Color[] peercolors:          : an Color[5] array specifying the colors in which to paint the bar (see RudolpPeer for detains)
  **   Graphics g                   : our Graphics instance as given in Component.paint(Graphics g)
  */
  
  public static void paintTextArea( int x0, int y0, int width, int height,
                                    char[] text, int[] starts, int[] lengths, int lineoffset,
                                    int startline, int startchar, int startoffset, int stopline, int stopchar, int stopoffset,
                                    Font f, FontMetrics fm, Color[] peercolors, Graphics g, boolean drawCursor) {
    //textarea box
    g.setColor(peercolors[3]); //dark
    g.drawLine(x0+1,y0+1,x0+width-2, y0+1);
    g.drawLine(x0+1,y0+1,x0+1, y0+height-2);
    g.setColor(peercolors[2]); //light
    g.drawLine(x0+width-2,y0+1,x0+width-2, y0+height-2);
    g.drawLine(x0+1,y0+height-2,x0+width-2, y0+height-2);
    width-=7; // to inner width inside the box
        
    // calculate herler variables for the texts
    int lineheight = fm.getHeight();
    int xt = x0+5;
    int yt = y0+2+lineheight-fm.getDescent();
    int lastline = (height-3)/lineheight+lineoffset;
    if(lastline>=starts.length)
    lastline =starts.length;
    //selection rectangle
    int xs = x0+2;
    int ys = y0+2;
    // counter
    int i = lineoffset;

    // draw text up to start of selection    
    g.setFont(f);
    g.setColor(peercolors[4]);//font color
    // draw from offset to insert pos
    while(i<lastline && i<startline) {
      g.drawChars(text,starts[i],lengths[i], xt, yt);
      ys+=lineheight;
      yt+=lineheight;
      i++;
    }     
         
    // insert and replace in same line and visible
    if(startline==stopline && startline>=lineoffset && startline<lastline) {
      if(stopoffset<0 || startoffset>width) {
        // selection before or after viewport  => full line of not yet or no longer selected text
        g.drawChars(text,starts[i],lengths[i], xt, yt);       
      }
      else if(startoffset<=0 && stopoffset>width) {
        // selection starts before viewport and ends after it

        //full line selection box
        g.setColor(peercolors[1]);//background dark
        g.fillRect(xt,ys,width-3,lineheight);
        //full line of selection text
        g.setColor(peercolors[2]);//light
        g.drawChars(text,starts[i],lengths[i], xt, yt);
      }
      else if(startoffset<=0) {
        // implicit also && xr<=width)
        //starts before viewport, but ends inside

        //selection box from viewport to stopdata
        g.setColor(peercolors[1]);//background dark
        g.fillRect(xt,ys,stopoffset,lineheight);
        //selection text from viewport to stopdata
        g.setColor(peercolors[2]);//light
        int temp = stopchar-starts[i];
        if(temp > text.length - starts[i]) temp = text.length-starts[i];
        g.drawChars(text,starts[i],temp, xt, yt);
        //text after selection
        g.setColor(peercolors[4]);//font color
        temp = starts[i]+lengths[i]-stopchar;
        if(temp > text.length - stopchar) temp = text.length-stopchar;
        g.drawChars(text,(stopchar < text.length ? stopchar : 0), (stopchar < text.length ? temp : 0), xt+stopoffset, yt);
      }
      else if(startoffset>0 && stopoffset>width) {
        // starts in viewport, but ends after it

        // text up to selection
        g.drawChars(text,starts[i],startchar-starts[i], xt, yt);       
        //selection box
        g.setColor(peercolors[1]);//background dark
        g.fillRect(xt+startoffset,ys,width-startoffset-2,lineheight);
        //selection text
        g.setColor(peercolors[2]);//light
        g.drawChars(text,startchar,starts[i]+lengths[i]-startchar, xt+startoffset, yt);
        //set color for text after selection
        g.setColor(peercolors[4]);//font color
      }
      else {
        //if(startoffset>0 && stopoffset<=width) //normal case, both starts and stop in viewport

        // text up to selection
        int temp = startchar - starts[i];
        if(temp > text.length - starts[i]) temp = text.length - starts[i];
        g.drawChars(text,starts[i],temp, xt, yt);       
        //selection box
        g.setColor(peercolors[1]);//background dark
        g.fillRect(xt+startoffset,ys,stopoffset-startoffset,lineheight);
        //selection text
        g.setColor(peercolors[2]);//light
        temp = stopchar - startchar;
        if(temp > text.length - startchar) temp = text.length - startchar;
        g.drawChars(text,(startchar < text.length ? startchar : 0), (startchar < text.length ? temp : 0), xt+startoffset, yt);
        //text after selection
        g.setColor(peercolors[4]);//font color
        temp = starts[i]+lengths[i]-stopchar;
        if(temp > text.length - stopchar) temp = text.length - stopchar;
        g.drawChars(text,(stopchar < text.length ? stopchar : 0), (stopchar < text.length ? temp : 0), xt+stopoffset, yt);
      }       
      //next line
      //ys+=lineheight; //no longer needed
      yt+=lineheight;
      i++;
    }         
    // else if we have a (at least partly)visible multi-line selection
    else if(stopline>=lineoffset && startline<lastline)
    {
      //first line: startdata to end:
      //----------------------------  
      if(startline>=lineoffset)
      {
        if(startoffset<0) {
          // selection starts before viewport => draw the selected text over the whole line

          //selection box
          g.setColor(peercolors[1]);//background dark
          g.fillRect(xt,ys,width,lineheight);
          //selection text
          g.setColor(peercolors[2]);//light
          g.drawChars(text,starts[i],lengths[i], xt, yt);
        }
        else if(startoffset > width) {
          //selection starts after viewport: draw then text before the selection

          // text up to selection
          g.setColor(peercolors[4]);//foreground
          g.drawChars(text,starts[i],lengths[i], xt, yt);       
        }
        else {
          //selection starts IN viewport : draw text before selection and selected fart after it

          // text up to selection
          g.setColor(peercolors[4]);//foreground
          g.drawChars(text,starts[i],startchar-starts[i], xt, yt);       
          //selection box
          g.setColor(peercolors[1]);//background dark
          g.fillRect(xt+startoffset,ys,width-startoffset,lineheight);
          //selection text
          g.setColor(peercolors[2]);//light
          if(startchar < text.length && lengths[i]+starts[i] < text.length && startchar <= lengths[i]+starts[i])
            g.drawChars(text,startchar,lengths[i]+starts[i]-startchar, xt+startoffset, yt);
        }
        //next line
        ys+=lineheight;
        yt+=lineheight;
        i++;
      }
             
      //lines between insert and replace line
      //-------------------------------------
      while(i<stopline && i<lastline) {
        //selection box over the complete length
        g.setColor(peercolors[1]);//background dark
        g.fillRect(xt,ys,width,lineheight);       
        //selection text
        g.setColor(peercolors[2]);//light
        g.drawChars(text,starts[i],lengths[i], xt, yt);
        //next line
        ys+=lineheight;
        yt+=lineheight;
        i++;     
      }
      //last line: if visible, draw the last selected part and the part after the replace mark
      //--------------------------------------------------------------------------------------
      if(i<lastline) {
        if(stopoffset<0) {
          // selection end not yet in viewport => draw the normal text after the selection

          // text after selection
          g.setColor(peercolors[4]);//foreground
          g.drawChars(text,starts[i], lengths[i], xt, yt);                             
        }
        else if(stopoffset>width) {
          //selection end after viewport, draw the stil selected text

          //selection box from left to right offset
          g.setColor(peercolors[1]);//background dark
          g.fillRect(xt,ys, width, lineheight);
          //last selected text
          g.setColor(peercolors[2]);//light
          g.drawChars(text,starts[i],lengths[i], xt, yt);       
          //next texts will be drawn in textcolor
          g.setColor(peercolors[4]);//foreground
        }
        else {
          // selected text inside viewport: draw still selected part and the normal text after if

          //selection box from left to selection offset
          g.setColor(peercolors[1]);//background dark
          g.fillRect(xt,ys, stopoffset, lineheight);
          //last selected text
          g.setColor(peercolors[2]);//light
          g.drawChars(text,starts[i],stopchar-starts[i], xt, yt);       
          // text after selection
          g.setColor(peercolors[4]);//foreground
          if(stopchar < text.length && lengths[i]+starts[i] < text.length && stopchar <= lengths[i]+starts[i])
            g.drawChars(text,stopchar, starts[i]+lengths[i]-stopchar, xt+stopoffset, yt);                    
        }
        //next line
        yt+=lineheight;
        i++;     
      }
    }

    //draw part after replace
    while(i<lastline) {
      //still implicit i<texts.length && y<ymax
      g.drawChars(text,starts[i],lengths[i], xt, yt);
      yt+=lineheight;
      i++;
    }     

  //Ok, done...
  }

  /*
  **  Simplified version of the above
  ** As we have no left-right scrolling, we know that the selection is always completely visible, thus we can throw away
  ** the complex visibility tests on start-and stop data
  ** @status This is a plug-in allowing you to configure the way the text area text is displayed. Feel free to change
  ** @variables
  **   int x0, int y0:              : the offset to the topleft corner (x=0,y=0) ofthe component where the TextArea has to be drawn
  **   int width, int height,       : the width and height of the text display area (the TextArea without the scrollbars)
  **   char[] text, int[] starts, int[] lengths      : the lines of text to be displayed. With the buffer/starts[]/lengths[] format
  **                                                   the <x>-th line of text to be displayed is the array of chars from the text buffer
  **                                                   starting at buffer char number <starts[x]> and having length <lengths[x]>
  **                                                   (the corresponding String being new String(buffer, starts[x],lengths[x]) )
  **   int lineoffset,                               : the vertical offset of visible lines. The visible part of the text stretches from line
  **                                                  <lineoffset> to line <lineoffset + getLines(height)>
  **   int startline, int startchar, int startoffset : the start of the selection defined by => the line on which the selection starts,
  **                                                   => the number of the starting character in the text buffer and
  **                                                   => the offset of the selection start text on the horizontal axis
  **   ( Through the simplifications assumed above, we know that startoffset between 0 and width)
  **   int stopline, int stopchar, int stopoffset    : the stop of the selection equally defined by => the line on which the selection stops,
  **                                                   => the number of the first character NO LONGER SELECTED in the text buffer and
  **                                                   => the offset of the selection stop text on the horizontal axis
  **   ( Through the simplifications assumed above, we know that stopoffset between 0 and width)
  **   Font f, FontMetrics fm,                       : the desired font and its FontMetrics
  **   Color[] peercolors:          : an Color[5] array specifying the colors in which to paint the bar (see RudolpPeer for detains)
  **   Graphics g                   : our Graphics instance as given in Component.paint(Graphics g)
  */

  public static void paintTextAreaNoScroll( int x0, int y0, int width, int height,
                                            char[] text, int[] starts, int[] lengths, int lineoffset,
                                            int startline, int startchar, int startoffset, int stopline, int stopchar, int stopoffset,
                                            Font f, FontMetrics fm, Color[] peercolors, Graphics g) {
    //textarea box
    g.setColor(peercolors[3]); //dark
    g.drawLine(x0+1,y0+1,x0+width-2, y0+1);
    g.drawLine(x0+1,y0+1,x0+1, y0+height-2);
    g.setColor(peercolors[2]); //light
    g.drawLine(x0+width-2,y0+1,x0+width-2, y0+height-2);
    g.drawLine(x0+1,y0+height-2,x0+width-2, y0+height-2);
        
    // text variable calculations
    int lineheight = fm.getHeight();
    int xt = x0+5;
    int yt = y0+2+lineheight-fm.getDescent();
    int lastline = (height-3)/lineheight+lineoffset;
    if(lastline>=starts.length)
    lastline =starts.length;
    //selection rectangle
    int xs = x0+2;
    int ys = y0+2;
    // counter
    int i = lineoffset;
        
    // text display...
    g.setFont(f);
    g.setColor(peercolors[4]);//font color
    // draw from offset to selection start
    while(i<lastline && i<startline) {
      g.drawChars(text,starts[i],lengths[i], xt, yt);
      ys+=lineheight;
      yt+=lineheight;
      i++;
    }     
         
    // Paint the selected text
    if(startline==stopline && startline>=lineoffset && startline<lastline) {
      // insert and replace in same line and visible
      //if(startdata.charoffset>0 && stopdata.charoffset<=width) //normal case
      xs=xt+startoffset;
      // text up to selection
      g.drawChars(text,starts[i],startchar-starts[i], xt, yt);       
      //selection box
      g.setColor(peercolors[1]);//background dark
      g.fillRect(xs,ys,stopoffset-startoffset,lineheight);
      //selection text
      g.setColor(peercolors[2]);//light
      g.drawChars(text,startchar,stopchar-startchar, xs, yt);
      //text after selection
      g.setColor(peercolors[4]);//font color
      g.drawChars(text,stopchar, starts[i]+lengths[i]-stopchar, xt+stopoffset, yt);
             
      //next line
      //ys+=lineheight; //no longer needed
      yt+=lineheight;
      i++;
    }
    else if(stopline>=lineoffset && startline<lastline) {
      // Multi-line selections (we KNOW however that the first and last lines are partly selected
      //and the lines in between are selected completely

      //first line: startdata to end:
      //----------------------------  
      if(startline>=lineoffset) {
        xs=xt+startoffset;
        // text up to selection
        g.setColor(peercolors[4]);//foreground
        g.drawChars(text,starts[i],startchar-starts[i], xt, yt);       
        //selection box
        g.setColor(peercolors[1]);//background dark
        g.fillRect(xs,ys,width-startoffset-6,lineheight);
        //selection text
        g.setColor(peercolors[2]);//light
        g.drawChars(text,startchar,lengths[i]+starts[i]-startchar, xs, yt);
        //next line
        ys+=lineheight;
        yt+=lineheight;
        i++;
      }
      //lines between insert and replace line
      //-------------------------------------
      xs=width-6; // inner text width; temporarily poses for fillrect.width of complete textline
      while(i<stopline && i<lastline) {
        //selection box over the complete length
        g.setColor(peercolors[1]);//background dark
        g.fillRect(xt,ys,xs,lineheight);       
        //selection text
        g.setColor(peercolors[2]);//light
        g.drawChars(text,starts[i],lengths[i], xt, yt);
        //next line
        ys+=lineheight;
        yt+=lineheight;
        i++;     
      }
      //last line: if visible, draw the last selected part and the part after the replace mark
      //--------------------------------------------------------------------------------------
      if(i<lastline) {
        //selection box from left to selection offset
        g.setColor(peercolors[1]);//background dark
        g.fillRect(xt,ys, stopoffset, lineheight);
        //last selected text
        g.setColor(peercolors[2]);//light
        g.drawChars(text,starts[i],stopchar-starts[i], xt, yt);       
        // text after selection
        g.setColor(peercolors[4]);//foreground
        g.drawChars(text,stopchar, starts[i]+lengths[i]-stopchar, xt+stopoffset, yt);                    
        //next line
        yt+=lineheight;
        i++;     
      }
    }
         
    //draw part after replace
    //g.setColor(peercolors[4]);//font color
    while(i<lastline) {
      // implicit also i<texts.length && y<ymax
      g.drawChars(text,starts[i],lengths[i], xt, yt);
      yt+=lineheight;
      i++;
    }     
    //Ok, done...
  }

  /*
  ** the number of lines to fit in a given screen width/ the minimum width of a screen containing a number of lines
  ** (the vertical area for the screen starts at y0+1 and ends at y0+height -2)
  */

  public static int getLines(int height, FontMetrics fm) {
    return (fm.getHeight()>0)?(height-3)/fm.getHeight():0;
  }
  public static int getHeight(int lines, FontMetrics fm) {
    return lines*fm.getHeight()+3;
  }
  public static int getLine(int height, FontMetrics fm) {
    return (fm.getHeight()>0)?(height-1)/fm.getHeight():0;
  }
  public static int getYPos(int lines, FontMetrics fm) {
    return lines*fm.getHeight()+1;
  }

  /*
  ** from width of the inner area for displaying texts to horizontal size of the total display area and vice versa
  ** (the vertical area for the screen starts at x0+1 and ends at x0+width -2)
  */

  public static int getInnerwidth(int screensize) {
    return (screensize>6)?screensize-6:0;
  }
  public static int getScreenSize(int innerwidth) {
    return innerwidth+6;
  }
   public static int getTextPos(int x) {
    return (x>5)? x-5:0;
  }

}
