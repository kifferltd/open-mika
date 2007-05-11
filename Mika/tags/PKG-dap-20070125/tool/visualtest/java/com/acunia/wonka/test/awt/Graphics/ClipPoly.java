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


// Author: J. Vandeneede
// Created: 2001/03/13


package com.acunia.wonka.test.awt.Graphics;

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;

public class ClipPoly extends VisualTestImpl   implements ActionListener
{
  class ClipPolygonPanel extends Container  implements MouseListener, MouseMotionListener
  {
	  // polygon points
    private Polygon refPoly;
    private Polygon screenPoly;
    private Point currentPoint;
    private boolean dragging = false;
    private boolean fill = false;
    // viewport and child coordinates and sizes
    private Dimension viewport;
  /****************************************************************************************************************************************/
  /**
  * Constructors
  */
    public ClipPolygonPanel()
    {
      viewport = new Dimension();
      currentPoint = new Point();
		
      int[] px ={6,8,8,6,4,4,6,7,5,7,5};
      int[] py ={1,2,4,5,4,2,1,4,2,2,4};
      refPoly = new Polygon(px,py,px.length);	
      screenPoly = new Polygon();

      this.addMouseListener(this);
      this.addMouseMotionListener(this);
    }

  /****************************************************************************************************************************************/
  /**
  * points and viewport commands from TestFill buttons
  */
  	public void translate(int dx, int dy)
  	{
  		screenPoly.translate(viewport.width*dx/12,viewport.height*dy/6);
  		this.repaint();
  	}
	
  	public void center()
  	{
  		setScreen(viewport);
  		this.repaint();
  	}
	
  	public boolean isFillMode() {return fill;}
	
  	public void swapFillMode()
  	{
  		fill = !fill;
  		this.repaint();
  	}
	
  	public void setFillMode(boolean newmode)
  	{
  		if(fill!=newmode)
  		{
  			fill = newmode;
  			this.repaint();
  		}
  	}
	
  /****************************************************************************************************************************************/
  /**
  * Mouse listeners
  */
    public  void mouseEntered(MouseEvent e)
    {
  //System.out.println("\n\n..Polygon mouse entered ("+e.getX()+", "+e.getY()+")");
    }
    public  void mouseExited(MouseEvent e)
    {
  //System.out.println("\n\n..Polygon mouse exited ("+e.getX()+", "+e.getY()+")");
    }
    public void mouseMoved(MouseEvent e)
    {
  //System.out.println("\n\n..Polygon mouse moved ("+e.getX()+", "+e.getY()+")");
    }
  
    public  void mouseClicked(MouseEvent e)
    {
  //System.out.println("\n\n..Polygon mouse clicked ("+e.getX()+", "+e.getY()+")");
    }
 
    public  void mousePressed(MouseEvent e)
    {
  		if(screenPoly.contains(e.getX(), e.getY()) )
	  	{
        System.out.println("Point inside polygon, dragging mode = "+dragging );			
  			if(!dragging)
      	{
      		dragging = true;
  		 		currentPoint.setLocation(e.getX(), e.getY());
      		this.repaint();
    		}
      }
      else if(dragging)
    	{
        System.out.println("Point outside polygon, dragging mode = "+dragging );			
    		dragging = false;
    		this.repaint();
      }
      else
        System.out.println("Point outside polygon, dragging mode = "+dragging );			
  	}

    public  void mouseDragged(MouseEvent e)
    {
    	if(dragging)
    	{
    		screenPoly.translate(e.getX()-currentPoint.x, e.getY()-currentPoint.y);
    		currentPoint.setLocation(e.getX(), e.getY());
    		this.repaint();
    	}
  	}


    public  void mouseReleased(MouseEvent e)
    {
   		dragging = false;
  		this.repaint();
  	}

  /****************************************************************************************************************************************/
  /**
  * Set image size
  */
  	private void setScreen(Dimension newscreen)
  	{
      System.out.println("Setting new screensize to <"+newscreen.width+", "+newscreen.height+">" );
  		screenPoly = new Polygon();
  		for(int i=0; i<refPoly.npoints; i++)
  			screenPoly.addPoint(refPoly.xpoints[i]*newscreen.width/12,refPoly.ypoints[i]*newscreen.height/6);
			
  		viewport.setSize(newscreen);
  	}
  /****************************************************************************************************************************************/
  /**
  * Paint
  */
	  	public void paint(Graphics g)	
		  {	
  			update(g);
    	}
  	
    	public void update(Graphics g)
    	{
       	//check sizes
       	if(!viewport.equals(this.getSize()))
       		setScreen(this.getSize());
       	//viewport
       	g.setColor(Color.white);
       	g.fillRect(1,1,	viewport.width-2, viewport.height-2);
      	g.setColor(Color.black);
      	g.drawLine(0,0,viewport.width-1,0);
      	g.drawLine(0,0,0,viewport.height-1);
      	g.drawLine(viewport.width-1,0,viewport.width-1,viewport.height-1);
      	g.drawLine(0,viewport.height-1,viewport.width-1,viewport.height-1);
        // color = red when dragged, blue otherwise
        g.setColor((dragging)?Color.red:Color.blue);
     		//polygon fill
     		if(fill)
   	  		g.fillPolygon(screenPoly);
     		else   //draw points
     			g.drawPolygon(screenPoly);  			
    	}
		
  }
  
	ClipPolygonPanel blackboard;
	
	java.awt.Button left;
	java.awt.Button right;
	java.awt.Button up;
	java.awt.Button down;
	java.awt.Button center;
	java.awt.Button mode;
	
  public ClipPoly()
  {
    super();
    blackboard = new ClipPolygonPanel();
    setLayout(new BorderLayout());
    add(blackboard, BorderLayout.CENTER);

    //buttons
    left = new Button("left");
    left.addActionListener(this);
    up = new Button("up");
    up.addActionListener(this);
    right = new Button("right");
    right.addActionListener(this);
    center = new Button("Center image");
    center.addActionListener(this);
    down = new Button("down");
    down.addActionListener(this);
    mode = new Button((blackboard.isFillMode())?"draw":"fill");
    mode.addActionListener(this);

		Panel buttonrow = new Panel(new GridLayout(2,3));
    buttonrow.add(left);
    buttonrow.add(up);
    buttonrow.add(right);
    buttonrow.add(center);
    buttonrow.add(down);
    buttonrow.add(mode);

    add(buttonrow, BorderLayout.SOUTH);
  }

  /** Button pressed: look at the button and either move all selected from red to blue or from blue to red*/
  public void actionPerformed(ActionEvent evt)
  {
  	if(evt.getSource() == left)
  		blackboard.translate(-1,0);
  	else if(evt.getSource() == right)
  		blackboard.translate(1,0);
  	else if(evt.getSource() == up)
  		blackboard.translate(0,-1);
  	else if(evt.getSource() == down)
  		blackboard.translate(0,1);
  	else if(evt.getSource() == center)
  		blackboard.center();
  	else if(evt.getSource() == mode)
  	{
    	blackboard.swapFillMode();
    	mode.setLabel( (blackboard.isFillMode())?"draw":"fill" );	
    }
  }


  /** log messages */
  public void log(java.awt.Panel p, java.io.Writer w, boolean b)throws java.io.IOException {
    w.write("ClipPoly ");
  }

  public String getTitle(){
    return "Polygon clipping test";
  }

  public String getHelpText(){
    return "Clipping test for Polygon class and functions g.drawPolygon() and g.fillPolygon() \n"+	
    				"move the java.awt.Polygon figure over the screen, either using the up/down/left/right buttons \n"+
    				"or by clicking in the figure and dragging it\n"+
    				"\nTest issues:\n"+
    				"=> When moving the polygon over the screen, watch the correct clipping, both in its filled as in its drawn form"+
    				"=> the clicking is also a test for Polygon.contains() When clicking on any blue area of the Polygon"+
    				", the polygon should change color.The white areas of the rectangle are NOT part of the polygon, so clicking them"+
    				" should leave the polygon as it is";
  }

  public java.awt.Panel getPanel(VisualTester vt) {
    return this;
  }

  public void start(java.awt.Panel p, boolean b){}
  public void stop(java.awt.Panel p){}

  static public void main (String[] args)
  {
  	new ClipPoly();
  }
}

