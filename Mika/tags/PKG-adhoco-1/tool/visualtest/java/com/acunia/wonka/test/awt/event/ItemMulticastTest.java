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

public class ItemMulticastTest extends VisualTestImpl implements ActionListener, CollectsEvents {

  private Button[] add;
  private Button[] remove;
  private ItemDisplay[] display;
  private ItemGeneratorComponent itemGenerator;
  final static int LISTENERS = 5;

  public ItemMulticastTest() {
    setForeground(new Color(108,56,32));
    setBackground(new Color(160,96,64));
    int step = 100/LISTENERS;
    int red = 155;
    int green = 155;
    setLayout(new BorderLayout());

    itemGenerator = new ItemGeneratorComponent("<Item Slider>",Color.black,new Color(200,120,160),this);
    add(itemGenerator, BorderLayout.CENTER);

    Panel listeners = new Panel(new GridLayout(LISTENERS,1));
      Panel[] row = new Panel[LISTENERS];
      add = new Button[LISTENERS];
      display = new ItemDisplay[LISTENERS];
      remove = new Button[LISTENERS];
      for(int i=0; i<LISTENERS; i++) {
        row[i] = new Panel(new BorderLayout() );
          display[i] = new ItemDisplay("Press <add> to add a listener to this panel", new Color(red,green,64),new Color(128,50,80));
          row[i].add(display[i],BorderLayout.CENTER);
          red+= step;
          add[i] = new Button("Add");
          add[i].setBackground(new Color(red,green,64));
          add[i].addActionListener(this);
          row[i].add(add[i],BorderLayout.WEST);
          green+= step;
          remove[i] = new Button("Remove");
          remove[i].setBackground(new Color(red,green,64));
          remove[i].addActionListener(this);
          row[i].add(remove[i], BorderLayout.EAST);
        listeners.add(row[i]);

      }
    add(listeners, BorderLayout.SOUTH);

  }


  /************************************************************************************************************/
  /** ActionListener interface actionPerformed:
  * with the <add> or <remove> button clicked, add or remove the Item listener to its panel
  */
  public void actionPerformed(ActionEvent evt) {
    boolean found = false;
    Object source = evt.getSource();
    for(int i=0; i<LISTENERS && !found; i++) {
      if(source == add[i]) {
        itemGenerator.addItemListener(display[i]);
        display[i].displayMessage("Listener added. press <remove> to remove it again");
        found = true;
      }
      else if(source == remove[i]) {
        itemGenerator.removeItemListener(display[i]);
        display[i].displayMessage("Listener removed. press <add> to add it again");
        found = true;
      }
    }
  }

  /************************************************************************************************************/
  /** CollectsEvent interface display messages : do nothing, we only display our own messages
  */
  public void displayMessage(String[] messagestrings) {
  }

  public String getHelpText() {
    return "The aim: test the throwing of ItemEvents through the AWTEventMulticaster functions:\n\n"+
           "The screen consists out of one black event field and "+LISTENERS+" yellow ItemDisplay panels. Each of this panels"+
           " is flanked by an <add> and a <remove> button.\n"+
           "The event field throws an ItemEvent every time the mouse is pressed or released inside it. with the <add> and <remove>"+
           " buttons, you can add an ItemListener to a panel in order to get the ItemEvents displayed on that panel,"+
           " or remove the listener again again\n."+
           "Adding and removing is done by calls to the static AWTEventMulticaster.Add()and -remove() functions\n"+
           "\n Items to test : \n -------------\n"+
           " => Pressing <add> for a panel and subsequently clicking in the event field to check if the Item event is displayed"+
           " on the newly selected panel, as well as on all other previously selected panels\n"+
           " => Pressing <remove> for a panel and subsequently clicking in the event field to check if the Item event is no longer"+
           " displayed on that panel, nor on the panels previously deselected, yet remains displayed on all other panels still selected\n"+
           " => Pressing <add> for the same panel over and over again to see that the panel is not added twice\n"+
           " => Pressing <remove> for the same panel over and over again, or pressing <remove> on a panel to which no listener is added yet"+
           " to check that a panel is not removed twice\n"+
           " \n ps. as the Add and remove routines have a slightly different algorithm for the first and second listener then for all"+
           " subsequent listeners, specially check the behavior when \n"+
           "    - adding the first panel, adding the second panel, adding the third panel\n"+
           "    - removing the third-last panel, removing the second-last panel, removing the last panel\n"+
           "    - giving a remove-command when no panels are selected";


  }

  /*********************************************************************************************************/
  /** Own version of  MouseGeneratorComponent that throws Itemevents on every mousepressed and mousereleased
  */
  class ItemGeneratorComponent extends MouseGeneratorComponent implements MouseListener, ItemSelectable {
    transient ItemListener multiListener;

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
