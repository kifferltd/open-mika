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

public class TextMulticastTest extends VisualTestImpl implements ActionListener {

  private Button[] add;
  private Button[] remove;
  private TextDisplay[] display;
  private NamedTextArea area;
  private Button append;
  private Button delete;
  private Button reset;
  private int count;
  final static int LISTENERS = 5;

  public TextMulticastTest() {
    setForeground(new Color(80,32,32));
    setBackground(new Color(128,64,64));
    int step = 100/LISTENERS;
    int red = 155;
    int shade = 100;

    setLayout(new BorderLayout());

    Panel buttons = new Panel(new GridLayout(1,3));
      append = new Button("Add one Oompa-Loompa");
      append.addActionListener(this);
      append.setBackground(new Color(red-step,shade,shade));
      buttons.add(append);
      delete = new Button("remove one Oompa-Loompa");
      delete.addActionListener(this);
      buttons.add(delete);
      reset = new Button("reset Oompa-Loompas");
      reset.addActionListener(this);
      reset.setBackground(new Color(red,shade-step,shade-step));
      buttons.add(reset);
    add(buttons, BorderLayout.NORTH);

    area = new NamedTextArea("<TextArea>","1 Oompa-Loompa..... ");
    count =2;
    add(area, BorderLayout.CENTER);

    Panel listeners = new Panel(new GridLayout(LISTENERS,1));
      Panel[] row = new Panel[LISTENERS];
      add = new Button[LISTENERS];
      display = new TextDisplay[LISTENERS];
      remove = new Button[LISTENERS];
      for(int i=0; i<LISTENERS; i++) {
        row[i] = new Panel(new BorderLayout() );
          display[i] = new TextDisplay("Press <add> to add a listener to this panel", new Color(red,shade,shade),getForeground());
          row[i].add(display[i],BorderLayout.CENTER);
          red+= step;
          add[i] = new Button("Add");
          add[i].setBackground(new Color(red,shade,shade));
          add[i].addActionListener(this);
          row[i].add(add[i],BorderLayout.WEST);
          remove[i] = new Button("Remove");
          remove[i].setBackground(new Color(red,shade,shade));
          remove[i].addActionListener(this);
          row[i].add(remove[i], BorderLayout.EAST);
          shade+= step;
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
        area.addTextListener(display[i]);
        display[i].displayMessage("Listener added. press <remove> to remove it again");
        found = true;
      }
      else if(source == remove[i]) {
        area.removeTextListener(display[i]);
        display[i].displayMessage("Listener removed. press <add> to add it again");
        found = true;
      }
    }
    if(!found){
      if(source == append){
        area.append( count+" Oompa-Loompas.... ");
        count++;
      }
      else if(source == delete){
        area.replaceRange("",0,20);
      }
      else if(source == reset){
        area.setText("1 Oompa-Loompa..... ");
        count=2;
      }
    }
  }

  /************************************************************************************************************/
  /** CollectsEvent interface display messages : do nothing, we only display our own messages
  */
  public void displayMessage(String[] messagestrings) {
  }

  public String getHelpText() {
    return "The aim: test the throwing of TextEvents through the AWTEventMulticaster functions:\n\n"+
           "The screen consists out a TextArea, a row of buttons and "+LISTENERS+" ItemDisplay panels. Each of this panels"+
           " is flanked by an <add> and a <remove> button.\n"+
           "The TextArea throws a TextEvent every time the text inside it is changed, either through the keyboard or through the three"+
           " buttons. Using the Add/Delete buttons next to the panels you can add a TextListener to that panel in order to get the"+
           " textEvents displayed on that panel, or you can remove this listener again\n."+
           "(Adding and removing of TextListeners is done by calls to the static AWTEventMulticaster.Add()and -remove() functions)\n"+
           "The top row buttons: <Add one Oompa-Loompa> adds one string <oompa-loompa> to the textarea, <remove one oompa-loompa>"+
           " removes the first of that strings from the textarea, <reset oompa-loompas> resets the text to its original string."+
           " All of these actions throw a TextEvent \n"+
           "\n Items to test : \n -------------\n"+
           " => Pressing <add> for a panel and subsequently clicking the buttons to check if a TextEvent is thrown and displayed"+
           " on the newly selected panel, as well as on all other previously selected panels\n"+
           " => Pressing <remove> for a panel and subsequently clicking a button to check if the TextEvent thrown is no longer"+
           " displayed on that panel, nor on the panels previously deselected, yet remains displayed on all other panels still selected\n"+
           " => Pressing <add> for the same panel over and over again to see that the panel is not added twice\n"+
           " => Pressing <remove> for the same panel over and over again, or pressing <remove> on a panel to which no listener is added yet"+
           " to check that a panel is not removed twice\n"+
           " \n ps. as the Add and remove routines have a slightly different algorithm for the first and second listener then for all"+
           " subsequent listeners, specially check the behavior when \n"+
           "    - adding the first panel, adding the second panel, adding the third panel\n"+
           "    - removing the third-last panel, removing the second-last panel, removing the last panel\n"+
           "    - giving a remove-command when no panels are selected"+
           "\n Disclaimer : No Oompa-Loompas were harmed during the making of this test.";


  }

  /**
  * inner class AWTEventdisplay with text listener
  */
  class TextDisplay extends AWTEventDisplay implements TextListener {
    /** as all events look the same, we have to add a number to disguise them*/
    private int eventNo;

    public TextDisplay(String title, Color back, Color front){
      super(title, back, front);
      eventNo=1;
    }

    public void textValueChanged(TextEvent evt) {
      message = displayTextEventShortcut(evt);
      this.repaint();
    }

    public String displayTextEventShortcut(TextEvent evt) {
      String line = "(event no."+eventNo+") ";
      eventNo++;

      Object source = evt.getSource();
      if(source==null){
        line += "Source() == NULL";
      }
      else {
        line += "Source() = "+source;
      }
      int id = evt.getID();
      if(id==TextEvent.TEXT_VALUE_CHANGED) {
        line+= " : TEXT_VALUE_CHANGED ("+id+")";
      }
      else {
        line+= " unknown Id ("+id+")";
      }
      return line;
    }
  }

  /**
  * inner class TextArea with name
  */
  class NamedTextArea extends TextArea{
    private String name;

    public NamedTextArea(String name, String text){
      super(text,3,30,TextArea.SCROLLBARS_HORIZONTAL_ONLY);
      this.name = name;
    }

    public String toString() {
      return name;
    }
  }




}
