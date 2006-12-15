/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


// Author: D. Buytaert
// Created: 2001/05/03

package com.acunia.wonka.test.awt.Component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.List;
import java.awt.Rectangle;

class PaintComponent extends Component {
    /** statics */
  final static Dimension DEFAULTSIZE = new Dimension(100,55);
  final static Color[] LIGHTCOLORS  = { new Color(255,92,92), new Color(255,192,128), new Color(255,255,160), new Color(128,255,128),
                                                new Color(160,160,255), new Color(92,92,192), new Color(192,96,192) };
  final static Color[] DARKCOLORS  = { new Color(128,32,32), new Color(160,92,0), new Color(192,192,32), new Color(32,128,32),
                                               new Color(64,64,128), new Color(0,0,64), new Color(64,0,64) };
  final static Color[] MIDCOLORS = {Color.red, Color.orange, Color.yellow, Color.green,
                                            new Color(96,96,255), new Color(0,0,160), new Color(128,0,128) };
  final static Color[] CONTRASTCOLORS = {Color.white, Color.black, Color.black, Color.black, Color.black, Color.white, Color.white};
  final static int COLORCOUNT = MIDCOLORS.length;

    /** variables */
  protected int updateCount;
  protected int paintCount;
  protected String name;
  protected Rectangle frameRect;
  protected List displayList;

  /** constructor */
  public PaintComponent(String text, List displaylist) {
    super();
    updateCount=0;
    paintCount=0;
    name=text;
    displayList = displaylist;
    frameRect = new Rectangle();
  }

  /*get sizes*/
  public Dimension getPreferredSize() {
    return DEFAULTSIZE;
  }
  public Dimension getMinimumSize() {
    return DEFAULTSIZE;
  }

  /** paint */
  public void paint(Graphics g) {
    displayList.add(name+" Received call to paint()");
    paintCount++;
    if(paintArea(DARKCOLORS[(paintCount-1)%COLORCOUNT], Color.white, g) ) {
      displayList.add(name+" on paint() calculated sizes");
    }
    displayList.add(name+" paint() executed, total paints = "+paintCount);
	}
  	
  /** update */
	public void update(Graphics g) {
    displayList.add(name+" Received call to update()");
    updateCount++;
    if(paintArea(LIGHTCOLORS[(updateCount-1)%COLORCOUNT], Color.black, g) ) {
      displayList.add(name+" on update() calculated sizes");
    }
    displayList.add(name+" update() executed, total updates = "+updateCount);
  }

  protected boolean paintArea(Color background, Color foreground, Graphics g){
    boolean calculated=false;
    if(frameRect.width<=0){
      frameRect.setBounds(0,0,this.getSize().width, this.getSize().height);
      calculated=true;
    }

    g.setColor(background);
    g.fillRect(5,5, frameRect.width-10, frameRect.height-10);
    g.setColor(foreground);
    g.drawString(name, 10,20);
    g.drawString("updates "+updateCount, 10,35);
    g.drawString("paints = "+paintCount, 10,50);
    return calculated;
  }
  //(end inner class)
}
