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

public class ItemEventTest extends VisualTestImpl implements ItemListener ,CollectsEvents {

  private Checkbox box1;
  private Checkbox box2;
  private Checkbox radio1;
  private Checkbox radio2;
  private List single;
  private List multiple;
  private ItemGeneratorComponent itemGenerator;
  private ItemDisplay display1;
  private List display2;

  public ItemEventTest() {
    setForeground(new Color(108,56,32));
    setBackground(new Color(220,220,128));
    int step = 100/4;
    int red = 155;
    int green = 155;
    setLayout(new BorderLayout());

    display1 = new ItemDisplay(new Color(255,255,64), getForeground());
    setLayout(new BorderLayout());
    Panel boxpanel = new Panel(new GridLayout(2,2));
      CheckboxGroup group  = new CheckboxGroup();
      box1 = new Checkbox("box1",true);
      box1.addItemListener(display1);
      box1.addItemListener(this);
      box1.setBackground(new Color(red,green,64));
      red+= step;
      box2 = new Checkbox("box2",false);
      box2.addItemListener(display1);
      box2.addItemListener(this);
      box2.setBackground(new Color(red,green,64));
      green+= step;

      radio1 = new Checkbox("radio1",true,group);
      radio1.addItemListener(display1);
      radio1.addItemListener(this);
      radio1.setBackground(new Color(red,green,64));
      red+= step;
      radio2 = new Checkbox("radio2",false,group);
      radio2.addItemListener(display1);
      radio2.addItemListener(this);
      radio2.setBackground(new Color(red,green,64));
      green+= step;
      boxpanel.add(radio1);
      boxpanel.add(box1);
      boxpanel.add(radio2);
      boxpanel.add(box2);

    add(boxpanel, BorderLayout.NORTH);
    Panel center = new Panel(new BorderLayout());
      Panel lists = new Panel(new GridLayout(1,3));
        single = new List(5,false);
        single.setBackground(new Color(140,96,170));
        single.addItemListener(display1);
        single.addItemListener(this);
        single.setBackground(new Color(red,green,64));
        red+= step;
        multiple = new List(5,true);
        multiple.setBackground(new Color(200,112,160));
        multiple.addItemListener(display1);
        multiple.addItemListener(this);
        multiple.setBackground(new Color(red,green,64));
        green+= step;
        for(int i=1; i<=6; i++) {
          single.add("Single_"+i);
          multiple.add("Multiple"+i);
        }

        itemGenerator = new ItemGeneratorComponent("<PointItems>",new Color(red,green,64),getForeground(),this);
        itemGenerator.addItemListener(display1);
        itemGenerator.addItemListener(this);

        lists.add(single);
        lists.add(itemGenerator);
        lists.add(multiple);
      center.add(lists, BorderLayout.CENTER);
      center.add(display1,BorderLayout.SOUTH);
    add(center, BorderLayout.CENTER);
    display2=new List(3,false);
    display2.add("Your ItemEvents displayed HERE");
    add(display2, BorderLayout.SOUTH);
  }

  /************************************************************************************************************/
  /** ItemListener event (there one and only) : Display it in the list
  */
  public void itemStateChanged(ItemEvent evt) {
    String[] messagestrings = ItemDisplay.displayItemEvent(evt);
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
    return "The aim: test the throwing and the correct layout of ItemEvents:\n\n"+
           "The test: The upper part of the screen consists out of:\n"+
           "- Two grouped checkboxes and two independent checkboxes\n"+
           "- a single selection list and a multiple selection list\n"+
           "- an implementation of the event field, overridden to throw an ItemEvents every time the mouse is pressed and every time it is released\n"+
           "All of this components have an item listener linked to both the lower list and the event panel"+
           "The lower part consists out of an event panel and a list both displaying the item events they get from the components above\n\n"+
           "Items to test:\n"+
           "- Event generating and catching: selecting or deselecting the checkboxes, selecting or deselecting an item on the lists,"+
           "clicking or releasing a point on the grid.. all should throw an ItemEvent, displayed BOTH in the event panel as in the lower list\n"+
           "- Correct event data: Every event is analysed and displayed in four lines in the lower list:\n"+
           "   => first line: the object generating the event: the checkbox, list or event field clicked\n"+
           "   => second line: the item of that list generating the event\n"+
           "       ... for a Checkbox : this is the checkbox itself (Displayed on the screen is its label)\n"+
           "       ... for a List : this is a String representing the text of the item currently clicked"+
           "       ... for the event field : this is a Point giving the current mouse position in grid coordinates"+
           " (you should see a text equal to Point.toString() for this particular point )"+
           "   => third line: the state of that item: selected or deselected\n"+
           "   => fourth line: the object list of all selected objects of that list\n"+
           "       ... for a Checkbox : If selected, the list contains one element: the checkbox label, if not selected, the list is empty\n"+
           "       ... for a single selection List : If an item is selected, it should contain this item's text string. If not it is empty\n"+
           "       ... for a multiple selection List : The object list contains th string representation of all elements currently selected"+
           " (when no item is selected, it is empty) \n"+
           "       ... for the event field : By layout the list ALWAYS contains two Points instances representing the clicked Point"+
           " both in grid as in screen coordinates \n"+
           "- grouped checkbox: when selecting a new checkbox of the group, you also deselect the previous selection."+
           " Nevertheless you must only throw ONE event: that the new one is selected. Consequently a group NEVER throws an event"+
           " where the state of the item is <deselected>. All group events need to display in line 3: <getStareChanged() = SELECTED> i\n"+
           "- single selection lists: when selecting a new item, you automatically also deselect the old one. Yet, you only get"+
           " one message telling that the new item is selected, without caring about the previous item now deselected."+
           " (the only way to get an item==<deselected> message is by clicking the selected item, deliberately deselecting it.)\n"+
           " \n the Choice-class also throwing an ItemEvent is not covered here for reasons of it not being implemented yet in Wonka\n";


  }

  /*********************************************************************************************************/
  /** Own version of  MouseGeneratorComponent that throws Itemevents on every mousepressed and mousereleased
  */
  class ItemGeneratorComponent extends MouseGeneratorComponent implements MouseListener, ItemSelectable {
    private ItemListener multiListener;

    public ItemGeneratorComponent(String componentname, Color back, Color front, CollectsEvents parentinstance){
      super(componentname, back, front, parentinstance);
      this.addMouseListener(this);
      multiListener=null;
    }


    /** add item listener handbook design is => iListener = AWTEventMulticaster.add(iListener, newilistener);*/
    public void addItemListener(ItemListener newlistener) {
      multiListener = AWTEventMulticaster.add(multiListener, newlistener);
    }
    /** remove item listener  handbook design is => ilistener = AWTEventMulticaster.remove(ilistener, newilistener);*/
    public void removeItemListener(ItemListener oldlistener) {
      multiListener = AWTEventMulticaster.remove(multiListener, oldlistener);
    }

    /** Override mouse-entered and mouse-exided events to do nothing*/
    public void mouseEntered(MouseEvent event) {
      // in this special case: do nothing
    }

    public void mouseExited(MouseEvent event) {
      // in this special case: do nothing
    }
    /** Override mouse-clicked NOT to throw the mouse event*/
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
        multiListener.itemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, gridPoint, ItemEvent.SELECTED) );
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
        multiListener.itemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, gridPoint, ItemEvent.DESELECTED) );
      }
      this.repaint();
    }

    /** Implement ItemSelectable getSelectedObjects(): return object array that contains the current point in grid as in screen coordinates*/
    public Object[] getSelectedObjects() {
      Object[] points = new Object[2];
      points[0]=gridPoint;
      points[1]=screenPoint;
      return points;
    }

  }



}
