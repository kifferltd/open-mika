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


package com.acunia.wonka.test.awt.Graphics.dottedtext;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;

public class PixelDisplay extends Canvas implements Runnable {

  public static final int DIRECTION_LEFT_RIGHT = 0;
  public static final int DIRECTION_RIGHT_LEFT = 1;
  public static final int DIRECTION_UP_DOWN = 2;
  public static final int DIRECTION_DOWN_UP = 3;

  private boolean showBorder;
  private boolean goOn;
  public Thread runner;
  private int startColumn = 0;
  private int startRow = 0;
  private DotArray array;
  private int dotSize = 10;
  //  private Color bgColor = new Color(0x8C998D);
  private Color activeFG = new Color(0xffff00);
  private Color inactiveFG = new Color(0x00ff00);
  private int delay = 200;
  private int pauseAtStart = 500;
  private int shiftDirection = DIRECTION_RIGHT_LEFT;
  private Color shadowUp = new Color(100,100,100);
  private Color shadowDown = new Color(200,200,200);
  private boolean previous[][] = null;

  public void setDotSize(int size) {
    this.dotSize = size;
  }

  public void setBgColor(Color color) {
//    bgColor = color;
  //  this.setBackground(bgColor);
  }


  public void setActiveColor(Color color) {
    activeFG = color;
  }

  public void setInactiveColor(Color color) {
    inactiveFG = color;
  }

  public void setDelay(int delay) {
    this.delay = delay;
  }

  public void setPauseAtStart(int start) {
    pauseAtStart = start;
  }

  public void setShowBorder(boolean show) {
    showBorder = show;
  }

  public void setShadowUp(Color color) {
    this.shadowUp = color;
  }

  public void setShadowDown(Color color) {
    this.shadowDown = color;
  }

  public void setShiftDirection(int direction) {
    shiftDirection = direction;
  }

  public int getDotSize() {
    return dotSize;
  }

  public Color getBgColor() {
//    return bgColor;
      return null;
  }

  public Color getActiveColor(){
    return activeFG;
  }

  public Color getInactiveColor() {
    return inactiveFG;
  }

  public int getDelay() {
    return delay;
  }

  public int getPauseAtStart() {
    return pauseAtStart;
  }

  public boolean getShowBorder() {
    return showBorder;
  }

  public int getShiftDirection() {
    return shiftDirection;
  }

  public Color getShadowUp() {
    return shadowUp;
  }

  public Color getShadowDown() {
    return shadowDown;
  }

  public PixelDisplay(DotArray array) {
    this.setBackground(Color.lightGray);
    this.setShowBorder(true);
    this.array = array;
//    (this).start();     // this makes wonka crash. sun java runs this.
  }

  public void start() {
    if (runner == null) {
      startRow = 0;
      startColumn = 0;
      goOn=true;
      runner = new Thread(this,"PixelDisplay Thread");
   //   runner.setPriority(10);
      runner.start();
    }
  }

  public void stop() {
    goOn = false;
  }

  public void run() {
    while(goOn) {
      switch(shiftDirection) {
        case DIRECTION_RIGHT_LEFT:
          startColumn+=4;
          if(startColumn>array.getArray()[0].length) {
            startColumn=0;
            try {
              Thread.sleep(pauseAtStart);
            }
            catch(Exception e) {}
          }
          break;
        case DIRECTION_LEFT_RIGHT:
          startColumn-=2;
          if(startColumn < 0) {
            startColumn = array.getArray()[0].length - 1;
            try {
              Thread.sleep(pauseAtStart);
            }
            catch(Exception e) {}
          }
          break;
        case DIRECTION_DOWN_UP:
          startRow+=2;
          if(startRow>array.getArray().length) {
            startRow=0;
            try {
              Thread.sleep(pauseAtStart);
            }
            catch(Exception e) {}
          }
          break;
        case DIRECTION_UP_DOWN:
          startRow-=2;
          if(startRow<0) {
            startRow=array.getArray().length - 1;
            try {
              Thread.sleep(pauseAtStart);
            }
            catch(Exception e) {}
          }
          break;
      }
      repaint();
      try {
        Thread.sleep(delay);
      }
      catch(Exception e) {}
    }
    runner = null;
  }

  public void update(Graphics g) {
    paint(g);
  }

  public void paint(Graphics g)  {
//    this.setBackground(Color.blue);
     Image offscreen = this.createImage(this.getWidth(), this.getHeight());
 //   Image offscreen = this.createImage(250, 150);
    paintOffscreen(offscreen.getGraphics());
//    paintOffscreen(g);
    g.drawImage(offscreen, 0, 0, null);
//    g.drawImage(offscreen, 0, 0, this.getWidth()-1, this.getHeight()-1, 0,0,this.getWidth()-1, this.getHeight()-1,null,null);
  }

  public void paintOffscreen(Graphics g) {
  //  if (previous == null) {
      paintInit(g);
  //  }
  //  else  {
  //    paintLoop(g);
  //  }
  }

  public void paintInit(Graphics g){
    int maxWidth = (getWidth()-2)/(dotSize+1);
    int maxHeight = (getHeight()-2)/(dotSize+1);
    int r, c, ri, ci;
    previous = new boolean[maxHeight+1][maxWidth+1];
    try {
      for(r=startRow, ri=0; ri<maxHeight; r++, ri++) {
        if(r >= array.getArray().length) {
          r=0;
        }
        for(c=startColumn, ci=0; ci<maxWidth; c++, ci++) {
          if(c >= array.getArray()[r].length) {
            c=0;
          }
          previous[ri][ci] = array.getArray()[r][c];
          if(array.getArray()[r][c]) {
            g.setColor(activeFG);
            g.fillRect(1+ci*(dotSize+1),1+ri*(dotSize+1),dotSize,dotSize);
          }
        }
      }

      if(showBorder) {
        g.setColor(shadowUp);
        g.drawLine(0, 0, 0, getHeight()-1);
        g.drawLine(0, 0, getWidth()-1, 0);
        g.setColor(shadowDown);
        g.drawLine(getWidth()-1, getHeight()-1, 0, getHeight()-1);
        g.drawLine(getWidth()-1, getHeight()-1, getWidth()-1, 0);
        g.setColor(Color.red);
      }
    }
    catch (Exception e) {
      stop();
    }
  }

  public void paintLoop(Graphics g) {
    int maxWidth = (getWidth()-2)/(dotSize+1);
    int maxHeight = (getHeight()-2)/(dotSize+1);
    int r, c, ri, ci;
    if (maxWidth+1 != previous[0].length || maxHeight+1 != previous.length) {
      paintInit(g);
    }
    else {
      try {
        for(r=startRow, ri=0; ri<maxHeight; r++, ri++) {
          if(r >= array.getArray().length) {
            r=0;
          }
          for(c=startColumn, ci=0; ci<maxWidth; c++, ci++)  {
            if(c >= array.getArray()[r].length) {
              c=0;
            }
            if (array.getArray()[r][c] != previous[ri][ci])  {
              previous[ri][ci] = array.getArray()[r][c];
              if(array.getArray()[r][c]) {
                g.setColor(activeFG);
                g.fillRect(1+ci*(dotSize+1),1+ri*(dotSize+1),dotSize,dotSize);
              }
              else {
                g.setColor(inactiveFG);
                g.fillRect(1+ci*(dotSize+1),1+ri*(dotSize+1),dotSize,dotSize);
              }
            }
          }
        }  // end for()
        if(showBorder) {
          g.setColor(shadowUp);
          g.drawLine(0, 0, 0, getHeight()-1);
          g.drawLine(0, 0, getWidth()-1, 0);
          g.setColor(shadowDown);
          g.drawLine(getWidth()-1, getHeight()-1, 0, getHeight()-1);
          g.drawLine(getWidth()-1, getHeight()-1, getWidth()-1, 0);
        }
      }
      catch (Exception e) {
        System.out.println("pad was stopped");
        stop();
      }
    }
  }


  public DotArray getDotArray() {
    return array;
  }

  public void setDotArray(DotArray array) {
    this.array = array;
  }

  public Dimension getMinimumSize() {
    return new Dimension(100,20);
  }

  public Dimension getPreferredSize() {
    return new Dimension(getMinimumSize().width, getMinimumSize().height);
  }

  public static void main(String args[]) {
    DotArray da = new DotArray(200, 70);
    da.drawString("Studio Brussel", 5, 0, new Arial_10_0());
    Frame f = new Frame();
    f.setLayout(new BorderLayout());
    f.setSize(300, 200);
    PixelDisplay pd = new PixelDisplay(da);
    pd.setShiftDirection(PixelDisplay.DIRECTION_DOWN_UP);
    pd.setDelay(800);
    f.add(pd, BorderLayout.CENTER);
    f.setLocation(20, 20);
    f.show();
    pd.start();
  }

}
