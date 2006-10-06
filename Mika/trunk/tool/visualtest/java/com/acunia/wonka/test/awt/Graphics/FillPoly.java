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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.acunia.wonka.test.awt.VisualTestImpl;
import com.acunia.wonka.test.awt.VisualTester;

public class FillPoly extends VisualTestImpl   implements ActionListener, ItemListener {
  /****************************************************************/
  /** static colors for demo */
  private final static Color[] COLORS = {Color.red , Color.orange, Color.yellow, Color.green,
                                         new Color(96,96,255), new Color(0,0,128),new Color(128,0,128)};
  /** click vincinity */
  private final static int VINCINITY=7;
  /****************************************************************/
  /** Variables */
	private PolygonPanel blackboard;
	
	private Button addBefore;
	private Button addAfter;
	private Button delete;
	private Button mode;
	private Checkbox fillNative;
	private Checkbox fillDemo;
	
	
  public FillPoly() {
    super();
    // polygon panel blackboard
    blackboard = new PolygonPanel();
    setLayout(new BorderLayout());
    add(blackboard, BorderLayout.CENTER);

    //buttons
    CheckboxGroup group = new CheckboxGroup();
		Panel buttonrow = new Panel(new GridLayout(2,3));
        addBefore = new Button("Add point (before)");
        addBefore.addActionListener(this);
      buttonrow.add(addBefore);
        addAfter = new Button("Add point (after)");
        addAfter.addActionListener(this);
      buttonrow.add(addAfter);
        delete = new Button("Delete point");
        delete.addActionListener(this);
      buttonrow.add(delete);
        mode = new Button((blackboard.isFillMode())?"draw":"fill");
        mode.addActionListener(this);
      buttonrow.add(mode);
        fillNative = new Checkbox("Native fill", group, true);
        fillNative.addItemListener(this);
      buttonrow.add(fillNative);
        fillDemo = new Checkbox("Demo fill", group, false);
        fillDemo.addItemListener(this);
      buttonrow.add(fillDemo);
    add(buttonrow, BorderLayout.SOUTH);
  }

  /** Button pressed: look at the button and either move all selected from red to blue or from blue to red*/
  public void actionPerformed(ActionEvent evt)
  {
  	if(evt.getSource() == addBefore)
  		blackboard.addPoint(true);
  	if(evt.getSource() == addAfter)
  		blackboard.addPoint(false);
  	else if(evt.getSource() == delete )
  		blackboard.delete();
  	else if(evt.getSource() == mode)
  	{
    	blackboard.swapFillMode();
    	mode.setLabel( (blackboard.isFillMode())?"draw":"fill" );	
    }
  }

  /**Checkbox checked: as we know there are only two possibilities: native-fill checked or not,
   so we don't bother about the ItemEvent data and directly tell the desired fill state to the polygon*/
  public void itemStateChanged(ItemEvent evt) {
    blackboard.setNativeFill(fillNative.getState());
  }

  /** log messages */
  public void log(java.awt.Panel p, java.io.Writer w, boolean b)throws java.io.IOException {
    w.write("DrawOffset ");
  }


  public String getHelpText(){
    return "Polygon filling demonstration\n\n"+
           "The main 'drawing board' allows you to draw an ontline of a geometrical figure. The button lower right button (showing 'fill')"+
           " allows you to draw this figure as a filled polygon. (After filling the button's text changes to 'draw'"+
           " and pressing this button a second time will display the outlines of the figure again) \n\n"+
           " The checkboxes <fill native> and <fill demo> allow you to select wether you want to use the native Graphic.fillPolygon()"+
           " or the test's own demo filling algorithm \n\n"+
           "You can form the figure by :\n"+
           "=> Clicking one of its corner points and dragging  it to a new position\n"+
           "=> Pressing <add point(before)> and <add point(after)> to add a new corner point before or after the current selected one\n"+
           "=> Pressing <delete point> to delete the current corner point";
  }

  public java.awt.Panel getPanel(VisualTester vt) {
    return this;
  }

  public void start(java.awt.Panel p, boolean b){}
  public void stop(java.awt.Panel p){}

  static public void main (String[] args)
  {
  	new FillPoly();
  }

  /****************************************************************/
  /****************************************************************/
  /**
  * Inner class to draw the polygon points on the panel
  */
  /****************************************************************/
  class PolygonPanel extends Container  implements MouseListener, MouseMotionListener {
  	// polygon points
	  private int[] pointx = {2,7,1,5,6};
  	private int[] pointy = {2,3,4,1,5};
	  private int currentPoint;
  	private boolean fill = false;
  	private boolean nativeFill = true;
	  // viewport and child coordinates and sizes
	  private Dimension viewport;
	
    /**************************************************************/
    /** Constructor */
	  public PolygonPanel()
  	{		
		  viewport = new Dimension();
	  	viewport.setSize(9,6);
  		currentPoint = -1;
	  	fill = false;

      this.addMouseListener(this);
      this.addMouseMotionListener(this);
  	}

    /**************************************************************/
    /**
    * commands from main screen buttons
    */
    /**************************************************************/
    /** add new point before or after the current one */
  	public void addPoint(boolean before) {
		  if(currentPoint <= 0 && before)//before first point
  			addPoint(pointx.length,(pointx[pointx.length-1]+pointx[0])/2,(pointy[pointx.length-1]+pointy[0])/2);//add last
	  	else if (currentPoint<=0)
		  	addPoint(1,(pointx[1]+pointx[0])/2,(pointy[1]+pointy[0])/2);		
  		else if(currentPoint == pointx.length-1 && !before)//after last point
	  		addPoint(pointx.length,(pointx[pointx.length-1]+pointx[0])/2,(pointy[pointx.length-1]+pointy[0])/2);//
  		else if (before)
	  		addPoint(currentPoint,(pointx[currentPoint]+pointx[currentPoint-1])/2,(pointy[currentPoint]+pointy[currentPoint-1])/2);
  		else
	  		addPoint(currentPoint+1,(pointx[currentPoint]+pointx[currentPoint+1])/2,(pointy[currentPoint]+pointy[currentPoint+1])/2);			
  	}

    /**************************************************************/
    /** delete current point */
   	public void delete() {
	  	if(currentPoint>=0 && pointx.length>2) //selection & always at least 2 points
		  	delete(currentPoint);
  	}
	
    /**************************************************************/
    /** fill mode */
  	public boolean isFillMode() {
  	  return fill;
  	}
  	
	  public void swapFillMode() {
  		fill = !fill;
	  	this.repaint();
  	}
	
	  public void setNativeFill(boolean mode) {
  		nativeFill = mode;
  		if(fill){
  	  	this.repaint();  		
  		}
  	}
    /**************************************************************/
    /** Auxilliary: insert a point in the points array
    * (this is build a new set of pointx/pointy buffers containing all points including the current one) */
  	private void addPoint(int pos, int x, int y)
	  {
  		int[]newx = new int[pointx.length+1];
	  	int[]newy = new int[pointx.length+1];
		  int i;
  		for(i=0;i<pos;i++) {
		  	newx[i]=pointx[i];
			  newy[i]=pointy[i];
  		}
	  	newx[pos]=x;
		  newy[pos]=y;
  		for(i=pos;i<pointx.length; i++) {
  			newx[i+1]=pointx[i];
	  		newy[i+1]=pointy[i];		
		  }
  		
  		pointx=newx;
	  	pointy=newy;
		  
  		currentPoint = pos;
	  	this.repaint();
  	}
  	
    /**************************************************************/
    /** Auxilliary: insert a point in the points array
    * (this is build a new set of pointx/pointy buffers containing all points except the deleted) */
  	private void delete(int pos) {
  		int[]newx = new int[pointx.length-1];
	  	int[]newy = new int[pointx.length-1];
		  int i;
  		for(i=0;i<pos;i++) {
		  	newx[i]=pointx[i];
			  newy[i]=pointy[i];
  		}
	  	for(i=pos;i<newx.length; i++) {
			  newx[i]=pointx[i+1];
  			newy[i]=pointy[i+1];		
	  	}
		
  		pointx=newx;
	  	pointy=newy;
		
  		currentPoint = -1;
	  	this.repaint();
  	}
    /**************************************************************/
    /**
    * Mouse listener and mouse motion listener forwards
    */
    /**************************************************************/
    /** (on entered, do nothing...) */
    public  void mouseEntered(MouseEvent e){}
    /** (on exited, do nothing...) */
    public  void mouseExited(MouseEvent e){}
    /** (on moved, do nothing...) */
    public void mouseMoved(MouseEvent e){}
    /** (on clicked, do nothing...) */
    public  void mouseClicked(MouseEvent e){}
    /** (on mouse up, do nothing...) */
    public  void mouseReleased(MouseEvent e){}

    /** On mouse pressed, find the clicked point */
    public  void mousePressed(MouseEvent e) {
  		int x = e.getX();
	  	int y = e.getY();
		  boolean redraw = false;		
		  // see if clicked is the current point
  		if(currentPoint>=0)	{
		  	if(!checkPointClicked(x,y,currentPoint) ) {
  				currentPoint=-1;
	  			redraw=true;
		  	}
  		}  		
  		// see if another point is clicked
	  	for(int i=0; i<pointx.length && currentPoint<0; i++ ) {
			  if(checkPointClicked(x,y,i)) {
	  			currentPoint = i;
		  		redraw=true;
  			}	
  		}
  		// repaint if needed
	  	if(redraw) {
		  	this.repaint();
		  }
  	}


    /** On mouse dragged, move the current point */
    public  void mouseDragged(MouseEvent e) {
  		if(currentPoint <0)
			return;
  		//move current point
	  	pointx[currentPoint] = e.getX();
		  pointy[currentPoint] = e.getY();
  		this.repaint();
	  }


    /**************************************************************/
    /** Auxilliary: check if click in vincinity of a given point */	
  	private boolean checkPointClicked(int x, int y, int pointno) {
 		  x -=pointx[pointno];
   		y -=pointy[pointno];
   		return (x>-VINCINITY && x<VINCINITY && y>-VINCINITY && y<VINCINITY);
	  }
	
    /**************************************************************/
    /** Auxilliary: blow up initial figure to screen size */	
	  private void setScreen(Dimension newscreen)	{
      System.out.println("Setting new screensize to <"+newscreen.width+", "+newscreen.height+">" );
  		for(int i=0; i<pointx.length; i++)
	  	{
		  	pointx[i]=(pointx[i]*newscreen.width)/viewport.width;
			  pointy[i]=(pointy[i]*newscreen.height)/viewport.height;
  		}
	  	viewport.setSize(newscreen);
  	}
    /**************************************************************/
    /** Paint  */
  	public void paint(Graphics g) {	
			update(g);
  	}
  	
  	public void update(Graphics g) {
     	//check sizes
     	if(!viewport.equals(this.getSize()))
   	  	setScreen(this.getSize() );
        	
     	//viewport
     	g.setColor(Color.white);
     	g.fillRect(1,1,	viewport.width-2, viewport.height-2);
     	g.setColor(Color.black);
   	  g.drawRect(2,2,	viewport.width-4, viewport.height-4);
  			
			//polygon fill
			if(fill && nativeFill) { // draw polygon filled using native algorithm
	  		g.setColor(Color.blue);
		  	g.fillPolygon(pointx,pointy,pointy.length);
			}
			else if(fill) { // fill with our own
				fillPolygonDemo(pointx, pointy, pointy.length, g);
			}
  		else {  //draw points
      	for(int i=0; i<pointx.length; i++) {
     			// line to next point
         	g.setColor(Color.black);
         	for(int j=1; j<pointx.length; j++)
     				g.drawLine(pointx[j],pointy[j],pointx[j-1],pointy[j-1]);
   	  		g.drawLine(pointx[pointx.length-1],pointy[pointx.length-1],pointx[0],pointy[0]); //last line
    				
  				if(i==currentPoint) {
  					g.setColor(Color.red);
	  				g.drawLine(pointx[i]-3,pointy[i]-3,pointx[i]+3,pointy[i]+3);
		  			g.drawLine(pointx[i]-3,pointy[i]+3,pointx[i]+3,pointy[i]-3);
			  		g.drawLine(pointx[i]-5,pointy[i],pointx[i]+5,pointy[i]);
				  	g.drawLine(pointx[i],pointy[i]-5,pointx[i],pointy[i]+5);
  				}
  				else {
	  				g.setColor((i>0)?Color.black:Color.blue);
		  			g.drawLine(pointx[i]-5,pointy[i],pointx[i]+5,pointy[i]);
			  		g.drawLine(pointx[i],pointy[i]-5,pointx[i],pointy[i]+5);
  				}
  			}
			}
  			
  	}
  	
	  // end of inner class PolygonPanel	
  }
  /****************************************************************/
  /****************************************************************/
  /****************************************************************/
  /** Our own temporary fill-polygon algorithm in Java
  rewrite this algorithm in c and paste it into graphics.c
  in file
  open-wonka/awt/rudolph/src/native/com/acunia/wonka
  as soon as it works
  */
  /****************************************************************/
  /****************************************************************/
  void fillPolygonDemo(int[] pointx, int[] pointy, int size, Graphics g) {
    // linked list buffer for downward ordening of the corner points
    int firstpoint;
    int[] nextpoint = new int[size];

    // linestart and linestop buffers for the linepieces forming the boundaries of the piece of polygon currently drawn
    // (as the piece of polygon that is drawn shifts downwards, the boundaries change dynamically)
    int[] linestarts = new int[size];
    int[] linestops = new int[size];
    int numberoflines;

    // data buffer and linked list buffer for the draw from->to nodes of the scanline currently drawn
    int[] nodes = new int[size];
    int[] nextnode = new int[size];

    // step 1: sort all points in a linked list
    firstpoint = sortIncreasing(pointy, nextpoint, size);

    //step2: build the first calculation situation : two lines down from top
    // step3: from the starting point on,build the linepieces array and fill the piece of polygon up to the next lower point
    int currentpoint, previous, next;
    int scanline, scanstart, scanstop;
    int i;

    currentpoint = firstpoint;
    numberoflines = 0;
    int linepiece=0;  // debug only
    do{
      // step3a: build the line list for this piece of polygon
      scanstart = pointy[currentpoint];
      scanstop = pointy[nextpoint[currentpoint]];
      previous = (currentpoint>0)? currentpoint-1: size-1;
      next =  (currentpoint<(size-1))? currentpoint+1: 0;

      // if line this->previous goes down, add it
      if(pointy[previous]>pointy[currentpoint]){
        linestarts[numberoflines]=currentpoint;
        linestops[numberoflines++]=previous;
      }
      else if(pointy[previous]<pointy[currentpoint]){
        for(i=0; (linestarts[i]!=previous || linestops[i]!=currentpoint); i++);
        linestarts[i]=linestarts[--numberoflines];
        linestops[i]=linestops[numberoflines];
      }

      // if line this->next goes down, add it
      if(pointy[next]>pointy[currentpoint]){
        linestarts[numberoflines]=currentpoint;
        linestops[numberoflines++]=next;
      }
      else if(pointy[next]<pointy[currentpoint]){
      // if it goes up, delete it
        for(i=0; (linestarts[i]!=next || linestops[i]!=currentpoint); i++);
        linestarts[i]=linestarts[--numberoflines];
        linestops[i]=linestops[numberoflines];
      }
      // step3b: fill the piece determined by the line list and the start and stop line
      g.setColor(COLORS[linepiece%7]);

      for(scanline=scanstart; scanline<scanstop; scanline++) {
        // get the nodes for the intersections of the scanlines with the current page
        for(i=0; i<numberoflines; i++){
         //nodes[i]=findIntersection(scanline, pointx[linestarts[i]], pointy[linestarts[i]], pointx[linestops[i]], pointy[linestops[i]]);
          nodes[i] = pointx[linestarts[i]];
          if(scanline > pointy[linestarts[i]]) {
            nodes[i] += (pointx[linestops[i]]-pointx[linestarts[i]]) * (scanline-pointy[linestarts[i]]) / (pointy[linestops[i]]-pointy[linestarts[i]]);
          }
        }
        // sort them in a linked list
        previous = sortIncreasing(nodes, nextnode, numberoflines);
        // draw from node to next node as long as there are
        while(previous>=0){
          next = nextnode[previous];
          g.drawLine(nodes[previous],scanline, nodes[next],scanline);
          previous = nextnode[next];
        }
      }
      linepiece++;
      currentpoint = nextpoint[currentpoint];
    }
    while(nextpoint[currentpoint]>=0);

    // check
    g.setColor(Color.black);
    g.drawPolygon(pointx, pointy, size);
  }


  /****************************************************************/
  /**our own linked-list implementation:
  * value[] the values to be sorted,
  * order[] the linked list to be returned
  * returns the first element of the liinked list
  */
  private int sortIncreasing(int[] value, int[] order, int size){
    int first = 0;
    order[first] = -1;
    int previous;

    for(int i=1; i<size; i++){
      //add next
      if(value[i]<=value[first]){
        order[i]=first;
        first = i;
      }
      else {
        previous = first;
        while(order[previous]>=0 && value[order[previous]]<=value[i]){
          previous = order[previous];
        }
        order[i]=order[previous];
        order[previous]=i;
      }
    }
    return first;
  }
}



	
