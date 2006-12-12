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

import java.awt.Color;
import java.awt.SystemColor;
import java.awt.FontMetrics;

/*
**  Basic functions for all Rudolph-xxxx-Peer classes:
** => calculates an array of peer drawing colors out ofa given foreground and background
** => provides a number of text dimension functions for the drawing of texts in a peer painter
** @note: Just like the other Rudolph-xxxx-Peer classes, the final user may change the algorithms and implementation
**     to his own likes and the definite possibilities of his platform
*/

/*
** SCROLLBAR PAINT COMMANDS FOR ALL SCROLLBAR USERS
*/

public class RudolphPeer {
  
  /*
  **    Statics
  */

  public final static int FIELD_NONESELECTED = -1;

  public final static int COLORFILTER = 2;
  public final static int COLORWIDTH = 5;

  public final static int REVERTTOBLACK = 0x60; // a color who's r+g+b is less then 96 is considered black
  public final static int REVERTTOWHITE = 0xF0; // a color who's r+g+b is less then 240 is considered white
  
  /*
  **    Calculate the Rudolph peer colors
  */

  public static Color[] getBarColors() {
    Color[] peercolors = new Color[COLORWIDTH]; //5 colors: front,back, white frame, black frame , font(crayon) color
    // color0 = <background color>
    peercolors[0] = SystemColor.control; //original front
    // color1 = <slightly darker> (selections etc..)
    peercolors[1] = SystemColor.controlShadow;
    // color2 = <light frame>
    peercolors[2] = SystemColor.controlHighlight;
    // color3 = <deep dark frame>
    if(peercolors[1].equals(SystemColor.controlDkShadow) ) {
      // color1: <slightly darker> MUST be different from color3
      //=> if the originals are equal, make color3 darker
      peercolors[3] = getDarker(SystemColor.controlDkShadow);
    }
    else {
      // Nothing to grumble: assign color 3 (<deep dark frame>) to be the system's <controlDkShadow>
      peercolors[3] = SystemColor.controlDkShadow;
    }
    // color4 = <foreground color>
    peercolors[4] = SystemColor.controlText;

    return peercolors;    
  }

  public static Color[] getBarColors(Color panelcolor, Color fontcolor) {
    //security
    if(panelcolor==null) {
      panelcolor = SystemColor.scrollbar;
    }
    if(fontcolor == null) {
      fontcolor = SystemColor.windowText;
    }      

    // assign colors    
    Color[] peercolors = new Color[5];
    // color0 = <background color>
    peercolors[0] = copyOf(panelcolor);
    // color1 = <slightly darker> (selections etc..)
    peercolors[1] = getMiddle(panelcolor,Color.gray);
    peercolors[2] = getBrighter(panelcolor);
    peercolors[3] = getMiddle(peercolors[1],Color.black);
    // color4 = <foreground color>
    peercolors[4] = copyOf(fontcolor);

    return peercolors;
  }

/*  public static Color[] getBarColors(Color panelcolor, Color fontcolor)    
  {
    //security
    if(panelcolor==null) {
      panelcolor = SystemColor.scrollbar;
    }
    if(fontcolor == null) {
      fontcolor = SystemColor.windowText;
    }      

    // assign colors    
    Color[] peercolors = new Color[5];
    // color0 = <background color>
    peercolors[0] = copyOf(panelcolor);
    // color1 = <slightly darker> (selections etc..)
    peercolors[1] = getDarker(panelcolor);
    //if(panelcolor.equals(Color.black) ) {
    if((panelcolor.getRed()+panelcolor.getGreen()+panelcolor.getBlue())<= REVERTTOBLACK) {
      //special case: the selection can't be darker then the original => make selection slightly lighter
      peercolors[2] = new Color(0xC0,0xC0,0xC0);   //light frame
      peercolors[3] = getMiddle(panelcolor,Color.gray);
    }
    else {
      // color2 = <light frame>
      peercolors[2] = getBrighter(panelcolor);
      // color3 = <deep dark frame>
      peercolors[3] = getDarker(peercolors[1]);
    }
    // color4 = <foreground color>
    peercolors[4] = copyOf(fontcolor);

    return peercolors;
  }
      
  /*
  **  set given Rudolph peer colors into Color[5] format:
  */
  
  public static Color[] getBarColors(Color panelcolor, Color background, Color whiteframe, Color blackframe, Color fontcolor) {
    Color[] peercolors = new Color[5]; //5 colors: front,back, white frame, black frame , font(crayon) color
    peercolors[0] = copyOf(panelcolor); //original front
    peercolors[1] = copyOf(background); // background
    peercolors[2] = copyOf(whiteframe);
    peercolors[3] = copyOf(blackframe);
    peercolors[4] = copyOf(fontcolor);
    return peercolors;  
  }
  
  /*
  ** alternative algorithms for brighter and darker colors
  */

  /*
  ** an alternate brighter/darker algorithm : replace each rgb brightness with brightness/filter coefficient
  */

  public static Color getDarker(Color c) {
    int r=c.getRed()/COLORFILTER;
    int g=c.getGreen()/COLORFILTER;
    int b=c.getBlue()/COLORFILTER;
    if(r+g+b>REVERTTOBLACK) {
      return new Color(r,g,b);
    }
    else {
      return Color.black;
    }
  }

  /*
  ** an alternate brighter/darker algorithm : replace each rgb darkness with darkness/filter coefficient
  ** or for brightnes x=(Color getRed() for instance..) x becomes 0xff- (0xff-x)/filter coef
  ** or x becomes 0xff(1-1/COLORFILTER) +x/COLORFILTER ==0xff((COLORFILTER-1)/COLORFILTER) + x/COLORFILTER
  */

  public static Color getBrighter(Color c) {
    int offset = 0xff*(COLORFILTER-1)/COLORFILTER;
    int r=offset+c.getRed()/COLORFILTER;
    int g=offset+c.getGreen()/COLORFILTER;
    int b=offset+c.getBlue()/COLORFILTER;
    if(r+g+b<REVERTTOWHITE) {
      return new Color(r,g,b);
    }
    else {
      return Color.white;
    }
  }

  /*
  ** a general medium function
  ** x becomes c1x+(c2x-c1x)/ filter coef or x becomes (c1x(COLORFILTER-1)+c2x)/COLORFILTER
  ** note that(without the adjustments) getDarker(c) equals getMiddle(color.black,c) and getBrighter(c) equals getMiddle(Color.white,c)
  */

  public static Color getMiddle(Color c1, Color c2) {
    int base = COLORFILTER-1;
    int r=(base*c1.getRed()+c2.getRed())/COLORFILTER;
    int g=(base*c1.getGreen()+c2.getGreen())/COLORFILTER;
    int b=(base*c1.getBlue()+c2.getBlue())/COLORFILTER;
    return new Color(r,g,b);
  }

  /*
  ** Color clone algorithm: builds a NEW Color-instance equal to the old one
  */

  public static Color copyOf(Color c) {
    return new Color(c.getRed(), c.getGreen(), c.getBlue());
  }
    
  /*
  **    Stub methods for calculating a line of chars to a screen width and back
  **  Since there are fonts that have a fixed char width and fonts in which every char has his own width, the calculations
  ** can not be so straightforward as with the lines of text. Consequently we will split up the functions between
  ** algorithms with a fixed known width and a variable one. As for the variable width, the text itself has to be given
  ** along the two functions can be simply recognised through their arguments
  */

  /*
  **    The character containing the caret in a given character array(string)starting at a given offset
  **. This is also the number of chars of that string before the caret
  ** In some ways, this is the inverse of FontMetrics.charswidth(char line, int lineoffset, int linelength);
  ** if FontMetrics fm.charsWidth(line, start, len) = w pixels then RudolphPeer.getChars(w,start,line,fm) = (start+len+1):
  ** the last character of the string starting at position <start> and having length <len>
  **
  ** @note :If you know that the font you have is a fixed font, consider replacing last = RudolphPeer.getChars(w,start,line,fm)
  **        with the direct calculation last=start+(w-1)/fm
  **
  ** @returns -2 string null or offset bigger then line.length
  **         -1 caret before string starts (caret<0)
  **         0 <-> line.length()-1 : the number of the char containing the carret = the number of chars BEFORE the carret
  **         line.length(): the whole line before the carret
  */

  public static int getChars(int width, int lineoffset, char[] line, FontMetrics fm) {
    if(line==null || lineoffset>=line.length) {
      return -2;
    }
    if(width<0) {
      return -1;
    }
         
    // count the chars and their width untill carret reached
    // add as many chars to the offset untill the chars total length does JUST NOT surpass the carret width     
    int len=fm.charWidth(line[lineoffset] );
    int lastchar = line.length-1;
    while(len<width && lineoffset<lastchar) {
      lineoffset++;
      len+=fm.charWidth(line[lineoffset]);
    }
    // if longer then complete line: return line length
    return (len<width)?line.length:lineoffset;
  }

  /*
  **    The character containing the caret in a given character array(string)starting at a given offset
  **. This is also the number of chars of that string before the caret
  ** A special case of the above, optimalised for fixed width fonts. fontMetrics fm is replaced by the characters fixed
  ** width fm.getMaxAdvance
  ** returns -2 string null or offset bigger then line.length
  **         -1 caret before string starts (caret<0)
  **         0 <-> line.length()-1 : the number of the char containing the carret = the number of chars BEFORE the carret
  **         line.length(): the whole line before the carret
  */

  public static int getChars(int width, int lineoffset, char[] line, int fontwidth) {
    if(line==null || lineoffset>=line.length) {
      return -2;
    }
    if(width<0) {
      return -1;
    }
    //else
    int len=lineoffset+width/fontwidth;
    return (len>line.length)?line.length:len ;
  }
}
