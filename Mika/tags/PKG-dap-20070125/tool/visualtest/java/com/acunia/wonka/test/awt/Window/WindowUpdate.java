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

package com.acunia.wonka.test.awt.Window;

import com.acunia.wonka.test.awt.*;
import java.awt.*;
import java.awt.event.*;

public class WindowUpdate extends VisualTestImpl implements ActionListener {
    /** definitions */
  final static Color[] COLORS = {new Color(128,0,128), new Color(0,0,128), new Color(96,96,255),
                                                          Color.green, Color.yellow, Color.orange,Color.red};
  private final static Color DARKRED = new Color(96,48,48);
  private final static Color LIGHTRED = new Color(192,96,96);
  private final static Color DARKGREEN = new Color(48,96,48);
  private final static Color LIGHTGREEN = new Color(96,192,96);
  private final static Color DARKBLUE = new Color(48,48,96);
  private final static Color LIGHTBLUE = new Color(96,96,192);
  // window size
  private final static int WINWIDTH=70;
  private final static int WINHEIGHT=50;
  private final static int WINSTEP=15;

  /** variables */
  // buttons:
  private Button leftOne;
  private Button rightOne;
  private Button upOne;
  private Button downOne;
  private Button frontOne;
  private Button repaintOne;

  private Button leftTwo;
  private Button rightTwo;
  private Button upTwo;
  private Button downTwo;
  private Button frontTwo;
  private Button repaintTwo;

  private Button repaintMain;
  private Button repaintBottom;
  //windows
  private Window one;
  private InnerPaintComponent onePanel;
  private int framexOne;
  private int frameyOne;
  private Window two;
  private InnerPaintComponent twoPanel;
  private int framexTwo;
  private int frameyTwo;
  //main panel
  private InnerPaintComponent mainPanel;
  private int mainWidth;
  private int mainHeight;
  private int mainLeft;
  private int mainRight;
  private int mainTop;
  private int mainBottom;
  // display list
  private List display;


  /****************/
  /** constructor */
  public WindowUpdate() {
    // build windows
    one = null;
    onePanel = new InnerPaintComponent("Red window", LIGHTRED, DARKRED);
    two = null;
    twoPanel = new InnerPaintComponent("Blue window", LIGHTBLUE, DARKBLUE);
    setBackground(new Color(192,192,96));
    setLayout(new BorderLayout());

    // repaint central on top
    Panel top = new Panel(new FlowLayout());
      top.add(new Label("Main component"));
        repaintMain = new Button("repaint() main");
        repaintMain.setBackground(LIGHTGREEN);
        repaintMain.addActionListener(this);
      top.add(repaintMain);
        repaintBottom = new Button("repaint() bottom half");
        repaintBottom.setForeground(Color.white);
        repaintBottom.setBackground(DARKGREEN);
        repaintBottom.addActionListener(this);
      top.add(repaintBottom);
    add(top, BorderLayout.NORTH);
    // red window one on the left side
    Panel onebuttons = new Panel(new GridLayout(8,1) );
      onebuttons.setBackground(LIGHTRED);
      onebuttons.add(new Label("RED WINDOW"));
        frontOne = new Button("to front");
        frontOne.addActionListener(this);
      onebuttons.add(frontOne);
        repaintOne = new Button("repaint()");
        repaintOne.setForeground(Color.white);
        repaintOne.setBackground(DARKRED);
        repaintOne.addActionListener(this);
      onebuttons.add(repaintOne);
      onebuttons.add(new Label());
        leftOne = new Button("left =>");
        leftOne.addActionListener(this);
      onebuttons.add(leftOne);
        rightOne = new Button("<= right");
        rightOne.addActionListener(this);
      onebuttons.add(rightOne);
        upOne = new Button("up =^");
        upOne.addActionListener(this);
      onebuttons.add(upOne);
        downOne = new Button("down =v");
        downOne.addActionListener(this);
      onebuttons.add(downOne);
    add(onebuttons, BorderLayout.WEST);

    // Blue window two on the right side
    Panel twobuttons = new Panel(new GridLayout(8,1) );
      twobuttons.setBackground(LIGHTBLUE);
      twobuttons.add(new Label("BLUE WINDOW"));
        frontTwo = new Button("to front");
        frontTwo.addActionListener(this);
      twobuttons.add(frontTwo);
        repaintTwo = new Button("repaint()");
        repaintTwo.setForeground(Color.white);
        repaintTwo.setBackground(DARKBLUE);
        repaintTwo.addActionListener(this);
      twobuttons.add(repaintTwo);
      twobuttons.add(new Label());
        leftTwo = new Button("left =>");
        leftTwo.addActionListener(this);
      twobuttons.add(leftTwo);
        rightTwo = new Button("<= right");
        rightTwo.addActionListener(this);
      twobuttons.add(rightTwo);
        upTwo = new Button("up =^");
        upTwo.addActionListener(this);
      twobuttons.add(upTwo);
        downTwo = new Button("down =v");
        downTwo.addActionListener(this);
      twobuttons.add(downTwo);
    add(twobuttons, BorderLayout.EAST);

    // central main display
      mainPanel = new InnerPaintComponent("Main panel",LIGHTGREEN,DARKGREEN);
    add(mainPanel, BorderLayout.CENTER);

    // message display list
      display = new List(3,false);
    add(display,BorderLayout.SOUTH);
  }

  /****************************************************************************************************************************************/
  /**
  * As vt is only available after building the screen, we have to use this auxilliary function
  * to build the first window when pressing one of the left-side keys
  */
  public void checkWindowOne() {
    if(one==null) {
      if(vt==null){
        displayMessage("Skipped windows for lack of VT instance");
      }
      else{
        Frame vtframe = vt.getFrame();
        if(vtframe==null){
          // unable to access the main wonka frame yet
          displayMessage("Skipped Windows for lack of vt.getFrame()");
        }
        else{
          getMainBounds();
          framexOne = mainLeft + mainWidth/2 - WINWIDTH/2 - 20;
          frameyOne = mainTop + mainHeight/2 - WINHEIGHT/2 - 20;

          one = new Window(vtframe);
          one.add(onePanel);
          one.pack();
          one.setLocation(framexOne, frameyOne);
          one.setVisible(true);
          displayMessage("Built window one");
        }
      }
    }
  }


  /****************************************************************************************************************************************/
  /**
  * As vt is only available after building the screen, we have to use this auxilliary function
  * to build the second window when pressing one of the right-side keys
  */
  public void checkWindowTwo() {
    if(two == null) {
      if(vt==null){
        displayMessage("Skipped windows for lack of VT instance");
      }
      else{
        Frame vtframe = vt.getFrame();
        if(vtframe==null){
          // unable to access the main wonka frame yet
          displayMessage("Skipped Windows for lack of vt.getFrame()");
        }
        else{
          getMainBounds();
          framexTwo = mainLeft + mainWidth/2 - WINWIDTH/2 + 20;
          frameyTwo = mainTop + mainHeight/2 - WINHEIGHT/2 + 20;

          two = new Window(vtframe);
          two.add(twoPanel);
          two.pack();
          two.setLocation(framexTwo, frameyTwo);
          two.setVisible(true);
          displayMessage("Built window two");
        }

      }
    }
  }

  public void getMainBounds() {
    Rectangle bounds = mainPanel.getBounds();
    Point offset = mainPanel.getLocationOnScreen();
    mainWidth= bounds.width;
    mainHeight = bounds.height;
    mainLeft = offset.x;
    mainRight = offset.x+ mainWidth-WINWIDTH;
    mainTop = offset.y;
    mainBottom = offset.y+ mainHeight-WINHEIGHT;
  }
  /****************************************************************************************************************************************/
  /**
  * On button commands: move frames, repaint frames
  */
  public void actionPerformed(ActionEvent evt) {
    Component source = (Component)evt.getSource();
    if(source == repaintMain) {
      mainPanel.repaint();
    }
    else if(source == repaintBottom) {
      mainPanel.repaint(0, mainHeight/2, mainWidth, mainHeight/2);
    }
    else if(source == rightOne) {
      checkWindowOne();
      framexOne=(framexOne>(mainLeft+WINSTEP))? framexOne-WINSTEP:mainLeft;
      one.setLocation(framexOne, frameyOne);
    }
    else if (source == leftOne) {
      checkWindowOne();
      framexOne=(framexOne<(mainRight-WINSTEP))? framexOne+WINSTEP:mainRight;
      one.setLocation(framexOne, frameyOne);
    }
    else if (source == upOne) {
      checkWindowOne();
      frameyOne = (frameyOne>(mainTop+WINSTEP))? frameyOne-WINSTEP: mainTop;
      one.setLocation(framexOne, frameyOne);
    }
    else if (source == downOne) {
      checkWindowOne();
      frameyOne=(frameyOne<(mainBottom-WINSTEP))? frameyOne+WINSTEP: mainBottom;
      one.setLocation(framexOne, frameyOne);
    }
    else if (source == frontOne) {
      checkWindowOne();
      one.toFront();
    }
    else if (source == repaintOne) {
      checkWindowOne();
      onePanel.repaint();
    }

    else if (source == rightTwo) {
      checkWindowTwo();
      framexTwo = (framexTwo>(mainLeft+WINSTEP))? framexTwo-WINSTEP: mainLeft;
      two.setLocation(framexTwo, frameyTwo);
    }
    else if (source == leftTwo) {
      checkWindowTwo();
      framexTwo=(framexTwo<(mainRight-WINSTEP))? framexTwo+WINSTEP: mainRight;
      two.setLocation(framexTwo, frameyTwo);
    }
    else if (source == upTwo) {
      checkWindowTwo();
      frameyTwo = (frameyTwo>(mainTop+WINSTEP))? frameyTwo-WINSTEP: mainTop;
      two.setLocation(framexTwo, frameyTwo);
    }
    else if (source == downTwo) {
      checkWindowTwo();
      frameyTwo=(frameyTwo<(mainBottom-WINSTEP))? frameyTwo+WINSTEP: mainBottom;
      two.setLocation(framexTwo, frameyTwo);
    }
    else if (source == frontTwo) {
      checkWindowTwo();
      two.toFront();
    }
    else if (source == repaintTwo) {
      checkWindowTwo();
      twoPanel.repaint();
    }

  }

  /*********************************************************************************/
  /**  inner class with painting and mouse movements , based on panel*/
  class InnerPaintComponent extends Panel {
    private int updateCount;
    private int paintCount;
    private String name;
    private Color paintColor;
    private Color updateColor;

    /** constructor */
    public InnerPaintComponent(String componentname, Color paint, Color update) {
      super();
      name = componentname;
      paintCount=0;
      updateCount=0;
      paintColor=paint;
      updateColor=update;
    }

    /*set preferred size to 50 pixels high & wide*/
    public Dimension getPreferredSize() {
      return new Dimension(WINWIDTH, WINHEIGHT);
    }

    /** paint */
    public void paint(Graphics g) {
      paintCount++;
      displayMessage(name+ ": Received call to paint(), total paints = "+paintCount);
      Rectangle bounds = paintArea(g.getClipBounds(), paintColor, g);
      displayMessage(name+ ": Executed paint() for bounds ("+bounds.x+", "+bounds.y+", "+bounds.width+", "+bounds.height+")");
  	}
  	
  	public void update(Graphics g) {
      updateCount++;
      displayMessage(name+ ": Received call to update(), total updates = "+updateCount);
      Rectangle bounds = paintArea(g.getClipBounds(), updateColor, g);
      displayMessage(name+ ": Executed update() for bounds ("+bounds.x+", "+bounds.y+", "+bounds.width+", "+bounds.height+")");
    }

    private Rectangle paintArea(Rectangle bounds, Color background, Graphics g){
      if(bounds==null){
        bounds = new Rectangle(0,0,this.getSize().width,this.getSize().height);
        displayMessage("NULL bounds: Reverting to full screen (0, 0, "+bounds.width+", "+bounds.height+")");
      }
      g.setColor(background);
      g.fillRect(bounds.x, bounds.y, bounds.width-1, bounds.height-1);
      g.setColor(Color.white);
      g.drawRect(bounds.x+1, bounds.y+1, bounds.width-2, bounds.height-2);
      g.drawString(name, 3, 14);
      g.setColor(COLORS[updateCount%(COLORS.length)]);
      g.drawString("Updates "+updateCount, 3, 28);
      g.setColor(COLORS[paintCount%(COLORS.length)]);
      g.drawString("Paints = "+paintCount, 3, 42);
      return bounds;
    }
    //(end inner class)
  }
  /****************************************************************************************************************************************/

  void displayMessage(String message) {
    if(display.getItemCount()>40) {
      display.removeAll();
    }
    display.add(message,0);
System.out.println(message);
  }

  public String getHelpText() {
    return "A test on the interaction between the update() and paint() commands for systems with overlaying windows in different virtual engines\n"+
           " Two windows will be shown on a central panel. Both windows and panel only consists out of one basic paint component."+
           " To refresh the content of the component, a paint() and an update() function are implemented, both behaving slightly different:\n"+
           "=> the paint() function gets the graphics clipping bounds (Graphics.getClipBounds()) and repaints the area inside this clipping bounds in a light color"+
           " (it also draws a white line around the clipping rectangle). Then it displays a message <Executed paint()> in the display list below\n"+
           "=> the update() function also gets the graphics clipping bounds but repaints the area inside this clipping bounds in a darker color"+
           " and displays an <Executed update()> message. (it also draws a white line around the clipping rectangle) \n"+
           "(If the clipping bound is <NULL>, it is being replaced by the complete screen)\n\n"+
           "Move the windows over the screen, bring them to front and to back, repaint them, repaint the screen andwatch when a paint()"+
           " and when an update() is called for what area. Play around with different virtual engines and note the differences";
  }

  /**
  ** on stop, close all windows that aren't closed yet
  */
  public void stop(java.awt.Panel p){
    if(one != null) {
      one.dispose();
      one = null;
    }
    if(two != null) {
      two.dispose();
      two = null;
    }
  }
}
