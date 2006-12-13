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

package com.acunia.wonka.test.awt.ScrollPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class ScrollMouseEvent extends VisualTestImpl implements MouseListener{

  /****************************************************************/
  /** variables and definitions
  */
  /** size definitions */
  private final static int BUTTONWIDTH=100;
  private final static int BUTTONHEIGHT=65;
  private final static int CENTERWIDTH=40;
  private final static int CENTERHEIGHT=20;

  /** elements definitions */
  private final static int NUMBEROFBUTTONS = 15;
  /** color definitions */
  final static String[] colornames = {"Purple","Indigo", "Lightblue", "Green", "Yellow",  "Orange", "Red"};
  final static Color[] colors = {new Color(128,0,128), new Color(0,0,128), new Color(96,96,255),
                                 Color.green, Color.yellow, Color.orange,Color.red};

  /** variables */
  private List display;

  /****************************************************************/
  /** Constructor
  */
  public ScrollMouseEvent() {
    // Layout
    setLayout(new BorderLayout());
    setBackground(Color.yellow);
    // scrollpane: bars as needed
    Panel center = new Panel(new FlowLayout());
      ScrollPane asneededpane = new SizedScrollPane(250,150,Color.green); // ScrollPane.SCROLLBARS_AS_NEEDED);
      //asneededpane.setBackground(Color.green);
      int saturation= 96;
      int brightness= 48;
        Panel asneeded = new Panel(new GridLayout(5,3));
        for(int i=0; i<NUMBEROFBUTTONS; i++) {
          PaneCanvas pc = new PaneCanvas("Canvas no."+i, BUTTONWIDTH, BUTTONHEIGHT, saturation, saturation, brightness);
          pc.addMouseListener(this);
          asneeded.add(pc);
          saturation+=8;
          brightness+=6;
        }
        asneededpane.add(asneeded);
      center.add(asneededpane);
    add(center, BorderLayout.CENTER);
    // list
    display = new List(3,false);
    display.add("your mouse events displayed here");
    add(display, BorderLayout.SOUTH);

  }

  /****************************************************************/
  /** Scrollpane with preferred size
  */
  class SizedScrollPane extends ScrollPane {
    private int preferredWidth;
    private int preferredHeight;

    public SizedScrollPane(int width, int height, Color back) {
      super();;
      preferredWidth = width;
      preferredHeight = height;
      this.setSize(width, height);
      this.setBackground(back);
    }

    public Dimension getMinimumSize() {
      return new Dimension(preferredWidth, preferredHeight);
    }

    public Dimension getPreferredSize() {
      return new Dimension(preferredWidth, preferredHeight);
    }
  }

  /****************************************************************/
  /** paint compinent with preferred size
  */
  class PaneCanvas extends Component implements MouseListener {
    private int preferredWidth;
    private int preferredHeight;
    private Color backColor;

    private int buttonColor;
    private Rectangle colorButton;
    private Point lastclick;

    private String name;

    /**************************************/
    /** constructor
    */
    public PaneCanvas(String text, int width, int height, int red, int green, int blue) {
      super();
      name = text;
      preferredWidth = width;
      preferredHeight = height;
      backColor = new Color(red, green, blue);

      colorButton = new Rectangle((width-CENTERWIDTH)/2, (height-CENTERHEIGHT)/2, CENTERWIDTH, CENTERHEIGHT);
      buttonColor = colors.length-1;

      lastclick = new Point(width/2, height/2);
      this.addMouseListener(this);
    }

    /**************************************/
    /** name
    */
    public String getName() {
      return name;
    }
    /**************************************/
    /** preferred size
    */
    public Dimension getPreferredSize() {
      return new Dimension(preferredWidth, preferredHeight);
    }

    /**************************************/
    /** mouse commands
    */
    public void mouseEntered(MouseEvent evt){
    }
    public void mouseExited(MouseEvent evt){
    }
    public void mouseClicked(MouseEvent evt){
    }

    public void mousePressed(MouseEvent evt){
      lastclick.setLocation(evt.getX(),evt.getY());
      this.repaint();
    }

    public void mouseReleased(MouseEvent evt){
      if(colorButton.contains(evt.getPoint())){
        if(buttonColor>1) {
          buttonColor--;
        }
        else {
         buttonColor = colors.length-1 ;
        }
        lastclick.setLocation(evt.getX(),evt.getY());
        this.repaint();
      }
    }

    /**************************************/
    /** paint command
    */
    public void paint(Graphics g) {
      update(g);
    }

    public void update(Graphics g) {
      // sweep screen
      g.setColor(backColor);
      g.fillRect(2, 2, preferredWidth-4, preferredHeight-4);
      // name
      g.setColor(colors[buttonColor]);
      g.drawString(name+": "+colornames[buttonColor],5,15);
      // color button
      g.drawRect(colorButton.x, colorButton.y, colorButton.width, colorButton.height);
      g.drawRect(colorButton.x+2, colorButton.y+2, colorButton.width-4, colorButton.height-4);
      g.drawRect(colorButton.x+5, colorButton.y+5, colorButton.width-10, colorButton.height-10);
      // click position
      g.drawLine(lastclick.x, lastclick.y, lastclick.x+3, lastclick.y+3);
      g.drawLine(lastclick.x, lastclick.y, lastclick.x+3, lastclick.y-3);
      g.drawLine(lastclick.x+1, lastclick.y+1, lastclick.x+7, lastclick.y+2);
      g.drawLine(lastclick.x+1, lastclick.y-1, lastclick.x+7, lastclick.y-2);
    }
  }

  /**************************************/
  /** the application's mouse commands : display the events in the list
  */
  public void mouseEntered(MouseEvent evt){
    displayEvent(evt,"mouseEntered");
  }
  public void mouseExited(MouseEvent evt){
    displayEvent(evt,"mouseExited");
  }
  public void mousePressed(MouseEvent evt){
    displayEvent(evt,"mousePressed");
  }
  public void mouseReleased(MouseEvent evt){
    displayEvent(evt,"mouseReleased");
  }
  public void mouseClicked(MouseEvent evt){
    displayEvent(evt,"mouseClicked");
  }

  /****************************************************************/
  /** CollectsEvent interface display messages : do nothing, we only display our own messages
  */
  public void displayEvent(MouseEvent evt, String message) {
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    PaneCanvas source = (PaneCanvas)evt.getSource();
    message += "(source="+source.getName();
    int id = evt.getID();
    message += " event="+ id;
    if(id == MouseEvent.MOUSE_ENTERED) {
      message+=":<Mouse entered>";
    }
    else if(id == MouseEvent.MOUSE_EXITED) {
      message+=":<Mouse exited>";
    }
    else if(id == MouseEvent.MOUSE_PRESSED) {
      message+=":<Mouse pressed>";
    }
    else if(id == MouseEvent.MOUSE_RELEASED) {
      message+=":<Mouse released>";
    }
    else if(id == MouseEvent.MOUSE_CLICKED) {
      message+=":<Mouse clicked>";
    }
    else if(id == MouseEvent.MOUSE_MOVED) {
      message+=":<Mouse moved>";
    }
    else if(id == MouseEvent.MOUSE_DRAGGED) {
      message+=":<Mouse dragged>";
    }
    else {
      message+=":UNKNOWN ID";
    }

    message += " position=("+evt.getX()+","+evt.getY()+") )";
    display.add(message,0);
  }
  /****************************************************************/
  /** CollectsEvent interface display messages : do nothing, we only display our own messages
  */
  public void displayMessage(String message) {
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    display.add(message,0);
  }

  public String getHelpText() {
    return "A test on detecting mouse events and refreshing the screen of the inner panel of a ScrollPane:\n"+
    "The screen shows a ScrollPane containing 5x3 custom mouse-listening components. Each of this components has a mouse listener attached"+
    " and will react to mouse events by changing its layout and redrawing itself\n"+
    "TEST THE THROWING AND CATCHING OF MOUSE EVENTS AND THE REDRAWING OF THE PANE\n"+
    "..by clicking on one of the panels, the mouse events thrown will be forwarded to the display list on the bottom of the test. Check\n"+
    " -> if the event source corresponds to the (name of the) panel clicked\n"+
    " -> if the code for the event thrown corresponds to the actual mouse event.\n"+
    " -> if the mouse positions (roughly) correspond to the actual position inside the component.\n\n"+
    "..by regarding the reaction to the mouse events on the component itself :\n"+
    " -> pressing or releasing the mouse should redraw the screen with the cursor arrow moved to the mouse position\n"+
    " -> releasing the mouse inside the inner panel rectangle should change the color of the display drawings";
  }

}
