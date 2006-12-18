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


package com.acunia.wonka.test.awt.event;

import com.acunia.wonka.test.awt.*;
import java.awt.event.*;
import java.awt.*;

public class ActionEventTest extends VisualTestImpl implements ActionListener ,CollectsEvents {

  private Button button1;
  private Button button2;
  private TextField text1;
  private TextField text2;
  private List single;
  private List multiple;
  private ActionGeneratorComponent actionGenerator;
  private ActionDisplay display1;
  private List display2;

  public ActionEventTest() {
    setForeground(new Color(80,32,40));
    setBackground(new Color(160,64,96));
    int step = 100/4;
    int red = 155;
    int blue = 155;
    display1 = new ActionDisplay(new Color(255,64,255), getForeground());
    setLayout(new BorderLayout());
    Panel boxpanel = new Panel(new GridLayout(2,2));
      button1 = new Button("Button1");
      button1.addActionListener(display1);
      button1.addActionListener(this);
      button1.setBackground(new Color(red,64,blue));
      red+=step;
      button2 = new Button("Button2");
      button2.addActionListener(display1);
      button2.addActionListener(this);
      button2.setBackground(new Color(red,64,blue));
      blue+=step;
      text1 = new TextField("textfield no.1");
      text1.addActionListener(display1);
      text1.addActionListener(this);
      text1.setBackground(new Color(red,64,blue));
      red+=step;
      text2 = new TextField("textfield no.2");
      text2.addActionListener(display1);
      text2.addActionListener(this);
      text2.setBackground(new Color(red,64,blue));
      blue+=step;
      boxpanel.add(button1);
      boxpanel.add(button2);
      boxpanel.add(text1);
      boxpanel.add(text2);
    add(boxpanel, BorderLayout.NORTH);

    Panel center = new Panel(new BorderLayout());
      Panel lists = new Panel(new GridLayout(1,3));
        single = new List(5,false);
        single.setBackground(new Color(170,140,96));
        single.addActionListener(display1);
        single.addActionListener(this);
        single.setBackground(new Color(red,64,blue));
        red+=step;
        multiple = new List(5,true);
        multiple.setBackground(new Color(160,200,112));
        multiple.addActionListener(display1);
        multiple.addActionListener(this);
        multiple.setBackground(new Color(red,64,blue));
        blue+=step;
          for(int i=1; i<=6; i++) {
            single.add("Single_"+i);
            multiple.add("Multiple"+i);
          }
        actionGenerator = new ActionGeneratorComponent("<PointActions>",new Color(red,64,blue),getForeground(),this);
        actionGenerator.addActionListener(display1);
        actionGenerator.addActionListener(this);
        lists.add(single);
        lists.add(actionGenerator);
        lists.add(multiple);
      center.add(lists, BorderLayout.CENTER);
      center.add(display1,BorderLayout.SOUTH);
    add(center, BorderLayout.CENTER);

    display2=new List(3,false);
    display2.add("Your ActionEvents displayed HERE");
    add(display2, BorderLayout.SOUTH);
  }

  /************************************************************************************************************/
  /** ItemListener event (there one and only) : Display it in the list
  */
  public void actionPerformed(ActionEvent evt) {
    String[] messagestrings = ActionDisplay.displayActionEvent(evt);
    if(display2.getItemCount()>40) {
      display2.removeAll();
    }
    for(int i=messagestrings.length-1; i>=0; i--) {
      display2.add(messagestrings[i],0);
    }
  }
  /************************************************************************************************************/
  /** CollectsEvent interface display messages : do nothing, we only display our own messages
  */
  public void displayMessage(String[] messagestrings) {
    if(display2.getItemCount()>40) {
      display2.removeAll();
    }
    for(int i=messagestrings.length-1; i>=0; i--) {
      display2.add(messagestrings[i],0);
    }
  }

  public String getHelpText() {
    return "The aim: test the throwing and the correct layout of ActionEvents:\n\n"+
           "The test: The upper part of the screen consists out of:\n"+
           "- Two buttons and two text fields\n"+
           "- a single selection list and a multiple selection list\n"+
           "- an implementation of the event field, overridden to throw an ActionEvent every time the mouse is pressed and every time it is released\n"+
           "All of this components have an item listener linked to both the lower list and the event panel"+
           "The lower part consists out of an event panel and a list both displaying the item events they get from the components above\n\n"+
           "Items to test:\n"+
           "- Event generating and catching: clicking the buttons, double-clicking an item on the lists, pressing <enter> on the textfields and"+
           "pressing or releasing a point on the grid all should throw an ActionEvent, displayed BOTH in the event panel as well as in the lower list\n"+
           "   => check if the events are thrown and are shown on both the panel and the list"+
           "- Correct event data: Every event is analysed and displayed in three lines in the lower list:\n"+
           "   => first line: the object generating the event: the button, textfield, list or event field clicked\n"+
           "   Note that for the event field, the throwing object is NOT the field itself, but the point on the grid that is clicked/released\n"+
           "   => first line: also the ID number of the event thrown\n THIS NUMBER MUST CORRESPOND TO <ActionEvent.ACTION_PERFORMED> == 1001\n"+
           "   => second line: the event's action command (as per Actionevent.getActioncommand() )\n"+
           "     this command must be a String of the following contents\n"+
           "       ... for the Buttons: the button's label"+
           "       ... for the TextFields: the field's current text"+
           "       ... for the Lists: the name String of the item clicked"+
           "       ... for the event field: the color of the field"+
           "   => third line: event's modifier integer, and an indication wether this includes one or more of the following action modifiers:"+
           " <shift-key>-pressed, <controll-key>-pressed, <alt-key>-pressed and/or <meta-key>-pressed\n"+
           "\n Current items\n -------------\n"+
           "As Wonka doesn't support keyboard events yet, the textfields can't throw ActionEvents.\n"+
           "For the same reason, the modifier value will always be <0 = no key pressed>"+
           "\n Ps: The MenuItem, that also should throw an ActionEvent when clicked id not covered in this test,"+
           " for reason of it not being implemented in Wonka yet.";


  }

  /*********************************************************************************************************/
  /** Own version of  MouseGeneratorComponent that throws Itemevents on every mousepressed and mousereleased
  */
  class ActionGeneratorComponent extends MouseGeneratorComponent implements MouseListener {
    transient ActionListener multiListener;

    public ActionGeneratorComponent(String componentname, Color back, Color front, CollectsEvents parentinstance){
      super(componentname, back, front, parentinstance);
      this.addMouseListener(this);
      multiListener=null;
    }


    /** use AWTEventMulticast functions to return either the new action listener or a multicaster containing it;*/
    public void addActionListener(ActionListener newlistener) {
      multiListener = AWTEventMulticaster.add(multiListener, newlistener);
    }

    /** either delete action listener or remove it from the multicast listener instance*/
    public void removeActionListener(ActionListener oldlistener) {
      multiListener = AWTEventMulticaster.remove(multiListener, oldlistener);
    }

    /** Override mouse-entered and mouse-exided events to do nothing*/
    public void mouseEntered(MouseEvent event) {
      // in this special case: do nothing
    }

    public void mouseExited(MouseEvent event) {
      // in this special case: do nothing
    }

    /** Override mouse-clicked NOT to throw the mouse event, but instead fire an actionPreformed giving the new color*/
    public void mouseClicked(MouseEvent event) {
      //parent.displayMessage(displayMouseEvent(event,name)); D.O.N.'T.
      if(currentColor>0) {
        currentColor--;
      }
      else{
        currentColor=colors.length-1;
      }

      this.repaint();
    }

    /** Override mouse-pressed event to send an itemStateChanged message telling that the current grid point is selected*/
    public void mousePressed(MouseEvent event) {
      gridPoint = screenToGrid(event.getPoint());
      screenPoint = gridToScreen(gridPoint);
      mousePoint = event.getPoint();
      gridPoint.y = -gridPoint.y;
      connected=true;
      if(multiListener != null) {
        multiListener.actionPerformed(new ActionEvent(gridPoint, ActionEvent.ACTION_PERFORMED,colornames[currentColor]));
      }
      this.repaint();
    }

    /** Override mouse-released event to send an itemStateChanged message telling that the current grid point is deselected*/
    public void mouseReleased(MouseEvent event) {
      gridPoint = screenToGrid(event.getPoint());
      screenPoint = gridToScreen(gridPoint);
      mousePoint = event.getPoint();
      gridPoint.y = -gridPoint.y;
      connected=false;
      if(multiListener != null) {
        multiListener.actionPerformed(new ActionEvent(gridPoint, ActionEvent.ACTION_PERFORMED,colornames[currentColor]));
      }
      this.repaint();
    }

  }



}
