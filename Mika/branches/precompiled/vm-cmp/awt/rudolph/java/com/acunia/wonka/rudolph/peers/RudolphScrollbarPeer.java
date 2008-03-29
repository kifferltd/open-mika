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

import java.awt.event.*;

/*
** java.awt drawn Scrollbar peer for our own uses
** This paints a default 'rudolph' scrollbar on a given position. Using this peer for all paint-instances
** lets you quickly make a new layout for the rudolph/wonka scrollbars needed, not only for the scrollbar class
**  but also for the ScrollPane, ListBox, ComboBox and all other classes using scrollbox functionality
*/

/*
**  *SCROLLBAR PAINT COMMANDS FOR ALL SCROLLBAR USERS
*/

public class RudolphScrollbarPeer extends RudolphPeer {

  /*
  ** static variables
  */

  public final static int HSCROLL_HEIGHT = 16;
  public final static int HSCROLL_LINEUPWIDTH = 16;
  public final static int HSCROLL_LINEDNWIDTH = 16;
  public final static int HSCROLL_MINIMUMBOXWIDTH = 16;

  public final static int VSCROLL_WIDTH = 16;
  public final static int VSCROLL_LINEUPHEIGHT = 16;
  public final static int VSCROLL_LINEDNHEIGHT = 16;
  public final static int VSCROLL_MINIMUMBOXHEIGHT = 16;

  public final static int ARROW_WIDTH = 4;

/*
 ROUTINE FOR THE BOX DRAWINGS:
 BOX: DRAW (a) TO (b) TO (c) TO (d) BACK TO (a),
 ARROW: DRAW (e) TO (f) TO (g) BACK TO (e)
     Horizontal bar
     a------b..........a-----b....a-----b
     |  / f |          |     |    | f \ |
     | e  | |          |     |    | |  e|
     |  \ g |          |     |    | g / |
     c------d..........c-----d....c-----d

 DOUBLE LINES,(x,y) COORDINATES:
 BOX: DRAW (acx0,aby0) TO (bdx0,aby0) TO (acx0,cdy0) TO (bdx0,cdy0) BACK TO (acx0,aby0)
    : DRAW (acx1,aby1) TO (bdx1,aby1) TO (acx1,cdy1) TO (bdx1,cdy1) BACK TO (acx1,aby1)
 ARROW: DRAW (ex0,ey0) TO (fgx0,fy0) TO (fgx0,fy0) BACK TO (ex0,ey0)
      : DRAW (ex1,ey1) TO (fgx1,fy1) TO (fgx1,fy1) BACK TO (ex1,ey1)

     vertical bar
      a-------b
      |   e   |
      |  / \  |
      | f---g |
      c-------d
      .       .
      .       .
      a-------b
      |       |
      |       |
      c-------d
      .       .      DOUBLE LINES,(x,y) COORDINATES:
      .       .      BOX: DRAW (acx0,aby0) TO (bdx0,aby0) TO (acx0,cdy0) TO (bdx0,cdy0) BACK TO (acx0,aby0)
      .       .         : DRAW (acx1,aby1) TO (bdx1,aby1) TO (acx1,cdy1) TO (bdx1,cdy1) BACK TO (acx1,aby1)
      a-------b      ARROW: DRAW (ex0,ey0) TO (fx0,fgy0) TO (gx0,fgy0) BACK TO (ex0,ey0)
      | f---g |           : DRAW (ex1,ey1) TO (fx1,fgy1) TO (gx1,fgy1) BACK TO (ex1,ey1)
      |  \ /  |
      |   e   |
      c-------d
*/

  /*
  **  paint a horizontal scrollbar:
  ** @variables:
  **  int x0, int y0               : offset to the topleft corner (x=0, y=0) of the field to draw the scrollbar in
  **  int barheight                : height of the scrollbar
  **  int scrollpos int scrollspan : position of the scrollbox in the inner scrollbox field, width of the scrollbox
  **  int scrollrange              : width of the inner scrollbox field (the total scrollbar width is <this> value plus the widths
  **                                 of the left and right UNIT_INCREMENT/UNIT_DECREMENT arrow boxes
  **  int clicked                  : indicator as to which part of the scrollbar is currently 'clicked' (and should therefore be drawn 'down')
  **                                 Special values:
  **                                   => AdjustmentEvent.UNIT_DECREMENT : clicked inside the left arrow box (draws left box 'down')
  **                                   => AdjustmentEvent.TRACK          : clicked inside the scrollbox (draws scrollbox 'down')
  **                                   => AdjustmentEvent.UNIT_INCREMENT : clicked inside the right arrow box (draws right box 'down')
  **                                 all other values have no effect on the paintHScrollbar routine
  **  Color[] barcolors:           : an Color[5] array specifying the colors in which to paint the bar (see RudolpPeer for detains)
  **  Graphics g                   : our Graphics instance as given in Component.paint(Graphics g)
  */
  
  public static void paintHScrollbar( int x0, int y0, int barheight,
                                      int scrollpos, int scrollspan, int scrollrange,
                                      int clicked,
                                      Color[] barcolors, Graphics g) {
    //safety check on scrollbar width
    if(scrollrange<0) {
      // crippled scrollbar : revert to the special crippled functions
      paintCrippledHScrollbar(x0, y0, barheight, scrollrange+HSCROLL_LINEUPWIDTH+HSCROLL_LINEUPWIDTH, clicked, barcolors, g);
      return;
    }
    else if(scrollrange<scrollspan) {
      // scrollbox bigger then screen: adjust
      scrollpos=0;
      scrollspan = scrollrange;      
    }
/*
 BOX: DRAW (acx0,aby0) TO (bdx0,aby0) TO (acx0,cdy0) TO (bdx0,cdy0) BACK TO (acx0,aby0)
    : DRAW (acx1,aby1) TO (bdx1,aby1) TO (acx1,cdy1) TO (bdx1,cdy1) BACK TO (acx1,aby1)
 ARROW: DRAW (ex0,ey0) TO (fgx0,fy0) TO (fgx0,fy0) BACK TO (ex0,ey0)
      : DRAW (ex1,ey0) TO (fgx1,fy1) TO (fgx1,fy1) BACK TO (ex1,ey1)
*/
    //Calculate some values repeatedly needed later on
    int aby0 = y0+1;
    int aby1 = y0+2;
    int cdy0 = y0+barheight-1;
    int cdy1 = y0+barheight-2;

    int ey = y0+barheight/2;
    int fy0, gy0, fy1, gy1;
    if(barheight<HSCROLL_HEIGHT) {
      fy0 = y0+3;
      fy1 = y0+4;
      gy0 = y0+barheight-3;
      gy1 = y0+barheight-4;
    }
    else {
      fy0 = ey - ARROW_WIDTH;
      fy1 = ey - ARROW_WIDTH + 1;
      gy0 = ey + ARROW_WIDTH;
      gy1 = ey + ARROW_WIDTH - 1;
    }

    int acx0, bdx0, acx1, bdx1;
    int ex0,fgx0,ex1,fgx1;

    //  paint the line-up click box
    acx0=x0+1;
    acx1=x0+2;
    bdx0=x0+HSCROLL_LINEUPWIDTH-1;
    bdx1=x0+HSCROLL_LINEUPWIDTH-2;
    ex0 = acx0+ARROW_WIDTH;
    ex1 = acx1+ARROW_WIDTH;
    fgx0 = bdx0-ARROW_WIDTH;
    fgx1 = bdx1-ARROW_WIDTH;

    //left button box
    g.setColor(barcolors[0]);
    g.fillRect(acx1, aby1, HSCROLL_LINEUPWIDTH - 4, barheight - 4);
    if(clicked == AdjustmentEvent.UNIT_DECREMENT) {
      //the box is clicked: paint it 'down'
      // topleft to topright
      g.setColor(barcolors[3]);
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
     // bottom left to bottom right
      g.setColor(barcolors[2]);
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // upper arrow leg
      g.setColor(barcolors[3]);//dark
      g.drawLine(ex0,ey,fgx0,fy0);
      g.drawLine(ex1,ey,fgx1,fy1);
      //lower arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex0,ey,fgx0,gy0);
      g.drawLine(ex1,ey,fgx1,gy1);
      //vertical arrow leg
      g.setColor(barcolors[2]);//bright
      g.drawLine(fgx0,fy0,fgx0,gy0);
      g.drawLine(fgx1,fy1,fgx1,gy1);
    }
    else {
      //the box is not clicked: paint it 'up'
      // topleft to topright
      g.setColor(barcolors[2]); //bright
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
      // bottom left to bottom right
      g.setColor(barcolors[3]); //dark
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // upper arrow leg
      g.setColor(barcolors[2]);//bright
      g.drawLine(ex0,ey,fgx0,fy0);
      g.drawLine(ex1,ey,fgx1,fy1);
      //lower arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex0,ey,fgx0,gy0);
      g.drawLine(ex1,ey,fgx1,gy1);
      //vertical arrow leg
      g.setColor(barcolors[3]);//dark
      g.drawLine(fgx0,fy0,fgx0,gy0);
      g.drawLine(fgx1,fy1,fgx1,gy1);
    }

    // fill the space between upper button and scrollbox in <slightly darker background> color
    g.setColor(barcolors[1]);
    g.fillRect(bdx0+1, aby0, scrollpos+1, barheight - 1);

        
    //  paint the scrollbox bubble
    acx0 = bdx0+scrollpos+1;
    acx1 = bdx0+scrollpos+2;
    bdx0 = acx0+scrollspan;
    bdx1 = acx0+scrollspan-1;
    // scroll box in background color
    g.setColor(barcolors[0]);
    g.fillRect(acx1, aby1, scrollspan-2, barheight - 4);
    if(clicked == AdjustmentEvent.TRACK) {
      //the box is clicked: paint it 'down'
      // topleft to topright
      g.setColor(barcolors[3]);
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
      //frame is bright on right and lower border,
      // bottom left to bottom right
      g.setColor(barcolors[2]);
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
    }
    else {
      //the box is not clicked: paint it 'up'
      // topleft to topright
      g.setColor(barcolors[2]);//bright
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
      // bottom left to bottom right
      g.setColor(barcolors[3]);  //dark
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
    }
        
    // fill the space between scrollbox and lower button in <slightly darker background> color
    g.setColor(barcolors[1]);
    g.fillRect(bdx0+1,aby0, scrollrange-scrollpos-scrollspan, barheight - 1);
        
    //  paint the line-down block
    acx0 = x0 + HSCROLL_LINEUPWIDTH + scrollrange;
    acx1 = x0 + HSCROLL_LINEUPWIDTH + scrollrange+1;
    bdx0 = acx0+HSCROLL_LINEDNWIDTH-1;
    bdx1 = acx0+HSCROLL_LINEDNWIDTH-2;
    //arrow
    fgx0 = acx0+ARROW_WIDTH;
    fgx1 = acx1+ARROW_WIDTH;
    ex0 = bdx0-ARROW_WIDTH;
    ex1 = bdx1-ARROW_WIDTH;
    //  line-down arrow block in background color
    g.setColor(barcolors[0]);
    g.fillRect(acx1, aby1, HSCROLL_LINEUPWIDTH-4, barheight - 4);
    if(clicked == AdjustmentEvent.UNIT_INCREMENT) {
      //the box is clicked: paint it 'down'
      // topleft to topright
      g.setColor(barcolors[3]);
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
     // bottom left to bottom right
      g.setColor(barcolors[2]);
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // upper arrow leg
      g.setColor(barcolors[1]);
      g.drawLine(ex0,ey,fgx0,fy0);
      g.drawLine(ex1,ey,fgx1,fy1);
      //lower arrow leg
      g.setColor(barcolors[2]);
      g.drawLine(ex0,ey,fgx0,gy0);
      g.drawLine(ex1,ey,fgx1,gy1);
      //vertical arrow leg
      g.setColor(barcolors[3]);//bright
      g.drawLine(fgx0,fy0,fgx0,gy0);
      g.drawLine(fgx1,fy1,fgx1,gy1);
    }
    else  {
      // not selected : draw frame 'up'
      // topleft to topright
      g.setColor(barcolors[2]);
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
     // bottom left to bottom right
      g.setColor(barcolors[3]);
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // upper arrow leg
      g.setColor(barcolors[1]);
      g.drawLine(ex0,ey,fgx0,fy0);
      g.drawLine(ex1,ey,fgx1,fy1);
      //lower arrow leg
      g.setColor(barcolors[3]);//medium
      g.drawLine(ex0,ey,fgx0,gy0);
      g.drawLine(ex1,ey,fgx1,gy1);
      //vertical arrow leg
      g.setColor(barcolors[2]);
      g.drawLine(fgx0,fy0,fgx0,gy0);
      g.drawLine(fgx1,fy1,fgx1,gy1);
    }
  }

  /*
  **  paint a 'crippled' horizontal scrollbar consisting of just a left arrow box and a right one
  ** @variables:
  **  int x0, int y0               : offset to the topleft corner (x=0, y=0) of the field to draw the scrollbar in
  **  int barheight                : height of the scrollbar
  **  int totalwidth               : width of the total scrollbar (in which to paint the left and right boxes)
  **  int clicked                  : indicator as to which part of the scrollbar is currently 'clicked' (and should therefore be drawn 'down')
  **                                 Special values:
  **                                   => AdjustmentEvent.UNIT_DECREMENT : clicked inside the left arrow box (draws left box 'down')
  **                                   => AdjustmentEvent.UNIT_INCREMENT : clicked inside the right arrow box (draws right box 'down')
  **                                 all other values have no effect on the paintCrippledHScrollbar routine ( with no scrollbox,
  **                                 AdjustmentEvent.TRACK is equally disregarded)
  **  Color[] barcolors:           : an Color[5] array specifying the colors in which to paint the bar (see RudolpPeer for detains)
  **  Graphics g                   : our Graphics instance as given in Component.paint(Graphics g)
  */

  public static void paintCrippledHScrollbar( int x0, int y0, int barheight,
                                              int totalwidth,
                                              int clicked,
                                              Color[] barcolors, Graphics g) {
/*
 BOX: DRAW (acx0,aby0) TO (bdx0,aby0) TO (acx0,cdy0) TO (bdx0,cdy0) BACK TO (acx0,aby0)
    : DRAW (acx1,aby1) TO (bdx1,aby1) TO (acx1,cdy1) TO (bdx1,cdy1) BACK TO (acx1,aby1)
 ARROW: DRAW (ex0,ey0) TO (fgx0,fy0) TO (fgx0,fy0) BACK TO (ex0,ey0)
      : DRAW (ex1,ey0) TO (fgx1,fy1) TO (fgx1,fy1) BACK TO (ex1,ey1)
*/
    //Calculate some values repeatedly needed later on
    int arrowwidth=(totalwidth/8>ARROW_WIDTH)?ARROW_WIDTH:totalwidth/8;
    int aby0 = y0+1;
    int aby1 = y0+2;
    int cdy0 = y0+barheight-1;
    int cdy1 = y0+barheight-2;

    int ey = y0+barheight/2;
    int fy0, gy0, fy1, gy1;
    if(barheight<(totalwidth/4+6)) {
      fy0 = y0+3;
      fy1 = y0+4;
      gy0 = y0+barheight-3;
      gy1 = y0+barheight-4;
    }
    else {
      fy0 = ey - arrowwidth;
      fy1 = ey - arrowwidth + 1;
      gy0 = ey + arrowwidth;
      gy1 = ey + arrowwidth - 1;
    }

    int acx0, bdx0, acx1, bdx1;
    int ex0,fgx0,ex1,fgx1;

    //fill whole the box
    acx0=x0+1;
    acx1=x0+2;
    g.setColor(barcolors[0]);
    g.fillRect(acx1, aby1, totalwidth - 4, barheight - 4);

    //  paint the line-up click box
    bdx0=x0+totalwidth/2-1;
    bdx1=x0+totalwidth/2-2;
    ex0 = acx0+arrowwidth;
    ex1 = acx1+arrowwidth;
    fgx0 = bdx0-arrowwidth;
    fgx1 = bdx1-arrowwidth;

    if(clicked == AdjustmentEvent.UNIT_DECREMENT) {
      //the box is clicked: paint it 'down'
      // topleft to topright
      g.setColor(barcolors[3]);
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
     // bottom left to bottom right
      g.setColor(barcolors[2]);
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // upper arrow leg
      g.setColor(barcolors[3]);//dark
      g.drawLine(ex0,ey,fgx0,fy0);
      g.drawLine(ex1,ey,fgx1,fy1);
      //lower arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex0,ey,fgx0,gy0);
      g.drawLine(ex1,ey,fgx1,gy1);
      //vertical arrow leg
      g.setColor(barcolors[2]);//bright
      g.drawLine(fgx0,fy0,fgx0,gy0);
      g.drawLine(fgx1,fy1,fgx1,gy1);
    }
    else {
      //the box is not clicked: paint it 'up'
      // topleft to topright
      g.setColor(barcolors[2]); //bright
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
      // bottom left to bottom right
      g.setColor(barcolors[3]); //dark
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // upper arrow leg
      g.setColor(barcolors[2]);//bright
      g.drawLine(ex0,ey,fgx0,fy0);
      g.drawLine(ex1,ey,fgx1,fy1);
      //lower arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex0,ey,fgx0,gy0);
      g.drawLine(ex1,ey,fgx1,gy1);
      //vertical arrow leg
      g.setColor(barcolors[3]);//dark
      g.drawLine(fgx0,fy0,fgx0,gy0);
      g.drawLine(fgx1,fy1,fgx1,gy1);
    }

    //  paint the line-down click box
    acx0=bdx0+2;
    acx1=bdx0+3;
    bdx0=x0+totalwidth-1;
    bdx1=x0+totalwidth-2;
    ex0 = bdx0-arrowwidth;
    ex1 = bdx1-arrowwidth;
    fgx0 = acx0+arrowwidth;
    fgx1 = acx1+arrowwidth;

    if(clicked == AdjustmentEvent.UNIT_INCREMENT) {
      //the box is clicked: paint it 'down'
      // topleft to topright
      g.setColor(barcolors[3]);
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
     // bottom left to bottom right
      g.setColor(barcolors[2]);
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // upper arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex0,ey,fgx0,fy0);
      g.drawLine(ex1,ey,fgx1,fy1);
      //lower arrow leg
      g.setColor(barcolors[2]);//bright
      g.drawLine(ex0,ey,fgx0,gy0);
      g.drawLine(ex1,ey,fgx1,gy1);
      //vertical arrow leg
      g.setColor(barcolors[3]);//dark
      g.drawLine(fgx0,fy0,fgx0,gy0);
      g.drawLine(fgx1,fy1,fgx1,gy1);
    }
    else {
      //the box is not clicked: paint it 'up'
      // topleft to topright
      g.setColor(barcolors[2]); //bright
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
      // bottom left to bottom right
      g.setColor(barcolors[3]); //dark
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // upper arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex0,ey,fgx0,fy0);
      g.drawLine(ex1,ey,fgx1,fy1);
      //lower arrow leg
      g.setColor(barcolors[3]);//dark
      g.drawLine(ex0,ey,fgx0,gy0);
      g.drawLine(ex1,ey,fgx1,gy1);
      //vertical arrow leg
      g.setColor(barcolors[2]);//bright
      g.drawLine(fgx0,fy0,fgx0,gy0);
      g.drawLine(fgx1,fy1,fgx1,gy1);
    }
  }

  /*
  **  paint a vertical scrollbar
  ** @variables:
  **  int x0, int y0               : offset to the topleft corner (x=0, y=0) of the field to draw the scrollbar in
  **  int barheight                : height of the scrollbar
  **  int scrollpos int scrollspan : position of the scrollbox in the inner scrollbox field, width of the scrollbox
  **  int scrollrange              : width of the inner scrollbox field (the total scrollbar width is <this> value plus the widths
  **                                 of the left and right UNIT_INCREMENT/UNIT_DECREMENT arrow boxes
  **  int clicked                  : indicator as to which part of the scrollbar is currently 'clicked' (and should therefore be drawn 'down')
  **                                 Special values:
  **                                   => AdjustmentEvent.UNIT_DECREMENT : clicked inside the left arrow box (draws left box 'down')
  **                                   => AdjustmentEvent.TRACK          : clicked inside the scrollbox (draws scrollbox 'down')
  **                                   => AdjustmentEvent.UNIT_INCREMENT : clicked inside the right arrow box (draws right box 'down')
  **                                 all other values have no effect on the paintHScrollbar routine
  **  Color[] barcolors:           : an Color[5] array specifying the colors in which to paint the bar (see RudolpPeer for detains)
  **  Graphics g                   : our Graphics instance as given in Component.paint(Graphics g)
  */
  
  public static void paintVScrollbar( int x0, int y0, int barwidth,
                                      int scrollpos, int scrollspan, int scrollrange,
                                      int clicked,
                                      Color[] barcolors, Graphics g) {    
    //safety check on scrollbar width
    if(scrollrange<0) {
      // crippled scrollbar : revert to the special crippled functions
      paintCrippledVScrollbar(x0, y0, barwidth, scrollrange+HSCROLL_LINEUPWIDTH+HSCROLL_LINEUPWIDTH, clicked, barcolors, g);
      return;
    }
    else if(scrollrange<scrollspan) {
      // scrollbox bigger then screen: adjust
      scrollpos=0;
      scrollspan = scrollrange;      
    }
    //Calculate some values repeatedly needed later on
    int acx0 = x0+1;
    int acx1 = x0+2;
    int bdx0 = x0+barwidth-1;
    int bdx1 = x0+barwidth-2;

    int ex = x0+barwidth/2;
    int fx0, gx0, fx1, gx1;
    if(barwidth<VSCROLL_WIDTH) {
      fx0 = x0+3;
      fx1 = x0+4;
      gx0 = x0+barwidth-3;
      gx1 = x0+barwidth-4;
    }
    else {
      fx0 = ex - ARROW_WIDTH;
      fx1 = ex - ARROW_WIDTH + 1;
      gx0 = ex + ARROW_WIDTH;
      gx1 = ex + ARROW_WIDTH - 1;
    }

    int aby0, cdy0, aby1, cdy1;
    int ey0,fgy0,ey1,fgy1;


    //  paint the line-up click box
    aby0 = y0+1;
    aby1 = y0+2;
    cdy0 = y0+VSCROLL_LINEUPHEIGHT-1;
    cdy1 = y0+VSCROLL_LINEUPHEIGHT-2;
     ey0 = aby0+ ARROW_WIDTH;
     ey1 = aby1+ ARROW_WIDTH;
    fgy0 = cdy0- ARROW_WIDTH;
    fgy1 = cdy1- ARROW_WIDTH;
    //clear upper box
    g.setColor(barcolors[0]);
    g.fillRect(acx1, aby1, barwidth - 4, VSCROLL_LINEUPHEIGHT - 4);

    if(clicked == AdjustmentEvent.UNIT_DECREMENT) {
      //the box is clicked: paint it 'down'
      // topleft to topright
      g.setColor(barcolors[3]);
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
     // bottom left to bottom right
      g.setColor(barcolors[2]);
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // left arrow leg
      g.setColor(barcolors[3]);//dark
      g.drawLine(ex,ey0,fx0,fgy0);
      g.drawLine(ex,ey1,fx1,fgy1);
      //right arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex,ey0,gx0,fgy0);
      g.drawLine(ex,ey1,gx1,fgy1);
      //horizontal arrow leg
      g.setColor(barcolors[2]);//bright
      g.drawLine(fx0,fgy0,gx0,fgy0);
      g.drawLine(fx1,fgy1,gx1,fgy1);
    }
    else {
      //the box is not clicked: paint it 'up'
      // topleft to topright
      g.setColor(barcolors[2]); //bright
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
      // bottom left to bottom right
      g.setColor(barcolors[3]); //dark
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // left arrow leg
      g.setColor(barcolors[2]);//light
      g.drawLine(ex,ey0,fx0,fgy0);
      g.drawLine(ex,ey1,fx1,fgy1);
      //right arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex,ey0,gx0,fgy0);
      g.drawLine(ex,ey1,gx1,fgy1);
      //horizontal arrow leg
      g.setColor(barcolors[3]);//dark
      g.drawLine(fx0,fgy0,gx0,fgy0);
      g.drawLine(fx1,fgy1,gx1,fgy1);
    }


    // fill the space between upper button and scrollbox in <slightly darker background> color
    g.setColor(barcolors[1]);
    g.fillRect(acx0, cdy0+1, barwidth-1, scrollpos+1);

        
    //  paint the scrollbox bubble
    aby0 = cdy0+scrollpos+1;
    aby1 = cdy0+scrollpos+2;
    cdy0 = aby0+scrollspan;
    cdy1 = aby0+scrollspan-1;
    // scroll box in background color
    g.setColor(barcolors[0]);
    g.fillRect(acx1, aby1, barwidth-4, scrollspan-2);
    if(clicked == AdjustmentEvent.TRACK) {
      //the box is clicked: paint it 'down'
      // topleft to topright
      g.setColor(barcolors[3]);
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
     // bottom left to bottom right
      g.setColor(barcolors[2]);
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);

    }
    else {
      //the box is not clicked: paint it 'up'
      // topleft to topright
      g.setColor(barcolors[2]); //bright
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
      // bottom left to bottom right
      g.setColor(barcolors[3]); //dark
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
    }

    // fill the space between scrollbox and lower button in <slightly darker background> color
    g.setColor(barcolors[1]);
    g.fillRect(acx0,cdy0+1, barwidth - 1,scrollrange-scrollpos-scrollspan);

    //  paint the line-down block
    aby0 = y0+VSCROLL_LINEUPHEIGHT+scrollrange;
    aby1 = y0+VSCROLL_LINEUPHEIGHT+scrollrange+1;
    cdy0 = aby0+VSCROLL_LINEDNHEIGHT-1;
    cdy1 = aby0+VSCROLL_LINEDNHEIGHT-2;
    //arrow
    fgy0 = aby0+ARROW_WIDTH;
    fgy1 = aby1+ARROW_WIDTH;
     ey0 = cdy0-ARROW_WIDTH;
     ey1 = cdy1-ARROW_WIDTH;

    g.setColor(barcolors[0]);
    g.fillRect(acx1, aby1, barwidth - 4, VSCROLL_LINEDNHEIGHT - 4);
    if(clicked == AdjustmentEvent.UNIT_INCREMENT) {
      //the box is clicked: paint it 'down'
      // topleft to topright
      g.setColor(barcolors[3]);
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
     // bottom left to bottom right
      g.setColor(barcolors[2]);
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // left arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex,ey0,fx0,fgy0);
      g.drawLine(ex,ey1,fx1,fgy1);
      //right arrow leg
      g.setColor(barcolors[2]);//medium
      g.drawLine(ex,ey0,gx0,fgy0);
      g.drawLine(ex,ey1,gx1,fgy1);
      //horizontal arrow leg
      g.setColor(barcolors[3]);//dark
      g.drawLine(fx0,fgy0,gx0,fgy0);
      g.drawLine(fx1,fgy1,gx1,fgy1);
    }
    else {
      //the box is not clicked: paint it 'up'
      // topleft to topright
      g.setColor(barcolors[2]); //bright
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
      // bottom left to bottom right
      g.setColor(barcolors[3]); //dark
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // left arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex,ey0,fx0,fgy0);
      g.drawLine(ex,ey1,fx1,fgy1);
      //right arrow leg
      g.setColor(barcolors[3]);//medium
      g.drawLine(ex,ey0,gx0,fgy0);
      g.drawLine(ex,ey1,gx1,fgy1);
      //horizontal arrow leg
      g.setColor(barcolors[2]);//dark
      g.drawLine(fx0,fgy0,gx0,fgy0);
      g.drawLine(fx1,fgy1,gx1,fgy1);
    }
  }

  /*
  **  paint a 'crippled' vertical scrollbar consisting of just an up and a down button
  **  int x0, int y0               : offset to the topleft corner (x=0, y=0) of the field to draw the scrollbar in
  **  int barheight                : height of the scrollbar
  **  int totalwidth               : width of the total scrollbar (in which to paint the left and right boxes)
  **  int clicked                  : indicator as to which part of the scrollbar is currently 'clicked' (and should therefore be drawn 'down')
  **                                 Special values:
  **                                   => AdjustmentEvent.UNIT_DECREMENT : clicked inside the left arrow box (draws left box 'down')
  **                                   => AdjustmentEvent.UNIT_INCREMENT : clicked inside the right arrow box (draws right box 'down')
  **                                 all other values have no effect on the paintCrippledHScrollbar routine ( with no scrollbox,
  **                                 AdjustmentEvent.TRACK is equally disregarded)
  **  Color[] barcolors:           : an Color[5] array specifying the colors in which to paint the bar (see RudolpPeer for detains)
  **  Graphics g                   : our Graphics instance as given in Component.paint(Graphics g)
  */
  
  public static void paintCrippledVScrollbar( int x0, int y0, int barwidth,
                                              int totalheight,
                                              int clicked,
                                              Color[] barcolors, Graphics g) {
/*
      a-------b      BOX: DRAW (acx0,aby0) TO (bdx0,aby0) TO (acx0,cdy0) TO (bdx0,cdy0) BACK TO (acx0,aby0)
      | f---g |         : DRAW (acx1,aby1) TO (bdx1,aby1) TO (acx1,cdy1) TO (bdx1,cdy1) BACK TO (acx1,aby1)
      |  \ /  |      ARROW: DRAW (ex,ey0) TO (fx0,fgy0) TO (gx0,fgy0) BACK TO (ex,ey0)
      |   e   |           : DRAW (ex,ey1) TO (fx1,fgy1) TO (gx1,fgy1) BACK TO (ex,ey1)
      c-------d
*/
    //Calculate some values repeatedly needed later on
    int arrowheight=(totalheight/8>ARROW_WIDTH)?ARROW_WIDTH:totalheight/8;
    int acx0 = x0+1;
    int acx1 = x0+2;
    int bdx0 = x0+barwidth-1;
    int bdx1 = x0+barwidth-2;

    int ex = x0+barwidth/2;
    int fx0, gx0, fx1, gx1;
    if(barwidth<(totalheight/4+6)) {
      fx0 = x0+3;
      fx1 = x0+4;
      gx0 = x0+barwidth-3;
      gx1 = x0+barwidth-4;
    }
    else {
      fx0 = ex - arrowheight;
      fx1 = ex - arrowheight + 1;
      gx0 = ex + arrowheight;
      gx1 = ex + arrowheight - 1;
    }

    int aby0, cdy0, aby1, cdy1;
    int ey0,fgy0,ey1,fgy1;

    //fill whole the box
    aby0 = y0+1;
    aby1 = y0+2;
    g.setColor(barcolors[0]);
    g.fillRect(acx1, aby1, totalheight - 4, barwidth - 4);

    //  paint the line-up click box
    cdy0 = y0+totalheight/2-1;
    cdy1 = y0+totalheight/2-2;
     ey0 = aby0+arrowheight;
     ey1 = aby1+arrowheight;
    fgy0 = cdy0-arrowheight;
    fgy1 = cdy1-arrowheight;

    if(clicked == AdjustmentEvent.UNIT_DECREMENT) {
      //the box is clicked: paint it 'down'
      // topleft to topright
      g.setColor(barcolors[3]);
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
     // bottom left to bottom right
      g.setColor(barcolors[2]);
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // left arrow leg
      g.setColor(barcolors[3]);//dark
      g.drawLine(ex,ey0,fx0,fgy0);
      g.drawLine(ex,ey1,fx1,fgy1);
      //right arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex,ey0,gx0,fgy0);
      g.drawLine(ex,ey1,gx1,fgy1);
      //horizontal arrow leg
      g.setColor(barcolors[2]);//bright
      g.drawLine(fx0,fgy0,gx0,fgy0);
      g.drawLine(fx1,fgy1,gx1,fgy1);
    }
    else {
      //the box is not clicked: paint it 'up'
      // topleft to topright
      g.setColor(barcolors[2]); //bright
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
      // bottom left to bottom right
      g.setColor(barcolors[3]); //dark
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // left arrow leg
      g.setColor(barcolors[2]);//light
      g.drawLine(ex,ey0,fx0,fgy0);
      g.drawLine(ex,ey1,fx1,fgy1);
      //right arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex,ey0,gx0,fgy0);
      g.drawLine(ex,ey1,gx1,fgy1);
      //horizontal arrow leg
      g.setColor(barcolors[3]);//dark
      g.drawLine(fx0,fgy0,gx0,fgy0);
      g.drawLine(fx1,fgy1,gx1,fgy1);
    }

    //  paint the line-down click box
    aby0=cdy0+2;
    aby1=cdy0+3;
    cdy0=y0+totalheight-1;
    cdy1=y0+totalheight-2;
    ey0 = cdy0-arrowheight;
    ey1 = cdy1-arrowheight;
    fgy0 = aby0+arrowheight;
    fgy1 = aby1+arrowheight;

    if(clicked == AdjustmentEvent.UNIT_INCREMENT) {
      //the box is clicked: paint it 'down'
      // topleft to topright
      g.setColor(barcolors[3]);
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
     // bottom left to bottom right
      g.setColor(barcolors[2]);
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // left arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex,ey0,fx0,fgy0);
      g.drawLine(ex,ey1,fx1,fgy1);
      //right arrow leg
      g.setColor(barcolors[2]);//medium
      g.drawLine(ex,ey0,gx0,fgy0);
      g.drawLine(ex,ey1,gx1,fgy1);
      //horizontal arrow leg
      g.setColor(barcolors[3]);//dark
      g.drawLine(fx0,fgy0,gx0,fgy0);
      g.drawLine(fx1,fgy1,gx1,fgy1);
    }
    else {
      //the box is not clicked: paint it 'up'
      // topleft to topright
      g.setColor(barcolors[2]); //bright
      g.drawLine(acx0, aby0, bdx0, aby0);
      g.drawLine(acx1, aby1, bdx1, aby1);
      //topleft to bottom left
      g.drawLine(acx0, aby0, acx0, cdy0);
      g.drawLine(acx1, aby1, acx1, cdy1);
      // bottom left to bottom right
      g.setColor(barcolors[3]); //dark
      g.drawLine(acx0,cdy0,bdx0,cdy0);
      g.drawLine(acx1,cdy1,bdx1,cdy1);
      //topright to bottom right
      g.drawLine(bdx0,aby0,bdx0,cdy0);
      g.drawLine(bdx1,aby1,bdx1,cdy1);
      // left arrow leg
      g.setColor(barcolors[1]);//medium
      g.drawLine(ex,ey0,fx0,fgy0);
      g.drawLine(ex,ey1,fx1,fgy1);
      //right arrow leg
      g.setColor(barcolors[3]);//medium
      g.drawLine(ex,ey0,gx0,fgy0);
      g.drawLine(ex,ey1,gx1,fgy1);
      //horizontal arrow leg
      g.setColor(barcolors[2]);//dark
      g.drawLine(fx0,fgy0,gx0,fgy0);
      g.drawLine(fx1,fgy1,gx1,fgy1);
    }
  }

}
