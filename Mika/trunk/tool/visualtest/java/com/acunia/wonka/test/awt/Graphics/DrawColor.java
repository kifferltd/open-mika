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
// Created: 2001/05/09

package com.acunia.wonka.test.awt.Graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.TextField;

import com.acunia.wonka.test.awt.VisualTestImpl;
import com.acunia.wonka.test.awt.VisualTester;


public class DrawColor extends VisualTestImpl {
  	
  static Color LIGHTBLUE = new Color(160,160,255);
  static Color INDIGO = new Color(0,0,192);
  static Color VIOLET = new Color(128,0,128);
	
	private TextTester tester;
	private TextField messenger;
	String logString;
	
  public DrawColor() {
    setLayout(new BorderLayout());
    tester = new TextTester();
    messenger = new TextField();
    add(tester, BorderLayout.CENTER);
    add(messenger, BorderLayout.SOUTH);
   	
    logString = "ComponentGraphics log:";
    //setSize(400,300);		
    //show;
  	
  }

  /** log messages */
  public String getLogInfo(java.awt.Panel p, boolean b){
    String result = logString;
    logString = "    (Written to log):";
    return result;
  }

  public String getTitle() {
    return "ComponentGraphics";
  }

  public String getHelpText() {
    return "";
  }

  public java.awt.Panel getPanel(VisualTester vt) {
    return this;
  }

  public void start(java.awt.Panel p, boolean b){}
  public void stop(java.awt.Panel p){}


  /****************************************************************************************************************************************/
  /****************************************************************************************************************************************/
  /**
  * internal class to display the colors
  */
  /****************************************************************************************************************************************/
  class TextTester extends Component
  {
    private Dimension canvas;

    private int seventh;
    private int half;

    Font f1;
    private Font f2;
    Font f3;

    TextTester() {
     	super();
     	canvas = new Dimension();
     	
     	//colors & fonts
     	f1 = new Font("courP14", 0, 14);
     	f2 = new Font("helvP14", 0, 18);
     	f3 = new Font("courP24", 0, 24);
    }
  /*********************************************************
  *	Paint
  */
    public void paint(Graphics g)
    {
    	// set the canvas dimensions
    	if(!canvas.equals(this.getSize()) )
    		setSize(this.getSize());
    		
    	// draw the colors
    	int current = 5;
    	for(int i=0; i<7;i++)
    	{
    		g.setColor(getColor(i));
    		g.drawLine(half-5,current, half+5, current+seventh-7);
    		g.drawLine(half+5,current, half-5, current+seventh-7);
    		g.drawRect(5,current,half-10, seventh-7);
    		g.fillRect(half+5,current,half-10, seventh-7);
      	g.setFont(f2);
      	g.drawString(getColorName(i),20,current+17);
    		current+=seventh;
    	}
    }

    public void setSize(Dimension newsize)
    {
    		seventh = newsize.height/7;
    		half = newsize.width/2;
    		canvas.setSize(newsize);
    }

    private Color getColor(int i)
    {
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

    private String getColorName(int i)
    {
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

  }

  static public void main (String[] args)
  {
    new DrawColor();
  }

}

