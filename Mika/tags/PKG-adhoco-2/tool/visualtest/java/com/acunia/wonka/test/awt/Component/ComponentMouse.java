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


// Author: N.Oberfeld
// Created: 2001/06/05

package com.acunia.wonka.test.awt.Component;

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;

public class ComponentMouse extends VisualTestImpl {


  /*****************/
  /** definitions  */
  final static Color LIGHTBLUE = new Color(160,160,255);
  final static Color INDIGO = new Color(0,0,192);
  final static Color VIOLET = new Color(128,0,128);

  /*****************/
  /** Variables    */
  private MouseTester tester;
  private TextField messenger;
  String logString;
  	
  /*****************/
  /** Constructor  */
  public ComponentMouse() {
    setLayout(new BorderLayout());
    tester = new MouseTester();
    messenger = new TextField();
    add(tester, BorderLayout.CENTER);
    add(messenger, BorderLayout.SOUTH);
       	
    logString = "ComponentGraphics log:";
    //setSize(400,300);		
    //show;
      	
  }

  /***************/
  /** messenger  */
  public void message(String text) {
    logString += "\n" +text;
    messenger.setText(text);
  }

  /*****************/
  /** log messages */
  public String getLogInfo(java.awt.Panel p, boolean b){
    String result = logString;
    logString = "    (Written to log)";
    return result;
  }

  /*************************/
  /** test title  (deprecaed)
  public String getTitle() {
    return "ComponentGraphics";
  }
  */

  /***************/
  /** help text */
  public String getHelpText() {
    return 	"Tests the mouse implementations in Component classes:\n"+
    "The test displays an array of seven colors and a field of texts underneath.\n"+
    "Clicking on one of the colors makes the text appear in <that> color"+
    "dragging the mouse over a color does the same and also changes the value of the four variables\n"+
    "<left>, <right>, <up> and <down> displayed in the text.\n"+
    "In addition, every mouse movement is mirrorred to the textfield on the bottom of the page and also logged.\n"+
    "(if the interfaces MouseListener and MouseMotionListener work correctly, this is)";
  }

  /**********/
  /** Panel */
  public java.awt.Panel getPanel(VisualTester vt) {
   return this;
  }

  /**********/
  /** start */
  public void start(java.awt.Panel p, boolean b) {
    //do nothing, we can't auromate mouse events
  }

  /*********/
  /** stop */
  public void stop(java.awt.Panel p) {
    //do nothing(see start)
  }

  /*************************/
  /** main for stand-alone */
  static public void main (String[] args) {
    new ComponentMouse();
  }



  /**************************************************************************************************************************************/
  /**************************************************************************************************************************************/
  /**************************************************************************************************************************************/
  /**************************************************************************************************************************************/
  /**
  *	Inner class MouseTester: the upper panel with the colors is a COMPONENT that has to catch mouse events
  * ( which until may, it wasn't able to...)
  **/
  /**************************************************************************************************************************************/
  //class MouseTester extends Panel implements MouseListener, MouseMotionListener
  class MouseTester extends Component implements MouseListener, MouseMotionListener
  {
    /*****************/
    /** Variables    */
    private Dimension canvas;

    private int seventh;
    private int half;
    private int paintBrush;

    private Font f1;
    private Font f2;
    private Font f3;
    //mouse movements
    private int left = -1;
    private int right = -1;
    private int up = -1;
    private int down = -1;
    private Point lastPosition;
         	
    /*********************************************************
    *	Mouse listener test constructor
    */
    MouseTester() {
      super();
      canvas = new Dimension();
      //colors & fonts
      f1 = new Font("courR14", 0, 14);
      f2 = new Font("helvR17", 0, 17);
      f3 = new Font("courR25", 0, 25);
           	
      //mouse listener
      this.addMouseListener(this);
      this.addMouseMotionListener(this);
      lastPosition = new Point();
           	
      paintBrush = -1;
    }


    /*********************************************************
    *	Mouse listener forwarding to fire the Adjustment events
    */
    public  void mouseClicked(MouseEvent e) {
      if(e.getY()<half && paintBrush != e.getX()/seventh) {
        paintBrush = e.getX()/seventh;	
        message("mouse clicked ("+e.getX()+", "+e.getY()+"), new color "+ getColorName(paintBrush));
        this.repaint();
      }
      else {
        message("mouse clicked ("+e.getX()+", "+e.getY()+")");
      }
    }

    public  void mousePressed(MouseEvent e) {
      message("mouse pressed ("+e.getX()+", "+e.getY()+")");
    }

    public  void mouseReleased(MouseEvent e) {
      message("mouse released ("+e.getX()+", "+e.getY()+")");
    }


    public  void mouseEntered(MouseEvent e) {
      message("mouse Entered ("+e.getX()+", "+e.getY()+")");
    }

    public  void mouseExited(MouseEvent e) {
      message("mouse Exited ("+e.getX()+", "+e.getY()+")");
    }


    public  void mouseDragged(MouseEvent e) {
      if(e.getY()<half && paintBrush != e.getX()/seventh) {
        paintBrush = e.getX()/seventh;	
        message(getMouseMovement(e.getX(), e.getY() )+" new color "+ getColorName(paintBrush));
      }
      else {
        message( getMouseMovement(e.getX(), e.getY()) );
      }
      this.repaint();
    }

    public  void mouseMoved(MouseEvent e) {
      message( getMouseMovement(e.getX(), e.getY()) ) ;
      this.repaint();
    }

    private String getMouseMovement(int x, int y) {
      String move = "Mouse moved: ";
      int d;	
      if(x<lastPosition.x) {
        //moved left
        d=lastPosition.x-x;
        left+=d;
        move+="<"+d+"> pixels Left";
        lastPosition.x = x;
      }
      else if(x>lastPosition.x) {
        //moved right
        d=x-lastPosition.x;
        right+=d;
        move+="<"+d+"> pixels right";
        lastPosition.x = x;
      }
      if(y<lastPosition.y) {
        //moved up
        d=lastPosition.y-y;
        up+=d;
        move+="<"+d+"> pixels up";
        lastPosition.y = y;
      }
      else if(y>lastPosition.y) {
        //moved down
        d=y-lastPosition.y;
        down+=d;
        move+="<"+d+"> pixels down";
        lastPosition.y = y;
      }
          	
      return move;
    }

    /*********************************************************
    *	Paint
    */
    public void paint(Graphics g) {
      // set the canvas dimensions
      if(!canvas.equals(this.getSize()) )
      setSize(this.getSize());
          		
      // draw the colors
      int current = 5;
      for(int i=0; i<7;i++) {
        g.setColor(getColor(i));
        g.fillRect(current,5,seventh-10, half-10);
        g.setColor(Color.black);
        g.drawRect(current,5,seventh-10, half-10);
        current+=seventh;
      }
      // paintbrush color tests
      g.setColor(getColor(paintBrush) );
      g.fillRect(2,half,canvas.width-4,half-2);
          	
      g.setColor((paintBrush<5 && paintBrush>=0)?Color.black:Color.white);
      g.drawLine(5,half+2,canvas.width-5,half+2);
      g.drawRect(5,canvas.height-10,canvas.width-10,5);
          	
      g.setFont(f2);
      g.drawString("selected color:", 20, half+20);
      g.setFont(f1);
      g.drawString("("+getColor(paintBrush)+")", 50, half+40);
      g.setFont(f3);
      g.drawString(getColorName(paintBrush),150,half+25);
      g.setFont(f1);
      g.drawString("Mouse moved left: "+left+" right: "+right,15,half+55);
      g.drawString("Mouse moved up: "+up+" down: "+down,15,half+70);
    }

    public void setSize(Dimension newsize) {
      seventh = newsize.width/7;
      half = newsize.height/2;
      canvas.setSize(newsize);
      message("Canvas set ("+canvas.width+","+canvas.height+")");
    }

    private Color getColor(int i) {
      if(i<0)
      return Color.black ;
      i%=7; //seven colors
      if(i<1)
      return Color.red;
      else if(i<2)
      return Color.orange;
      else if(i<3)
      return Color.yellow;
      else if(i<4)
      return Color.green;
      else if(i<5)
      return LIGHTBLUE;
      else if(i<6)
      return INDIGO;
      else
      return VIOLET;	
    }

    private String getColorName(int i) {
      if(i<0)
      return "(black)";
      i%=7; //seven colors
      if(i<1)
      return "<RED>";
      else if(i<2)
      return "<ORANGE>";
      else if(i<3)
      return "<YELLOW>";
      else if(i<4)
      return "<GREEN>";
      else if(i<5)
      return "<BLUE>";
      else if(i<6)
      return "<INDIGO>";
      else
      return "<VIOLET>";
    }
    /* end inner class */
  }
  /* end ComponentMouse */
}

