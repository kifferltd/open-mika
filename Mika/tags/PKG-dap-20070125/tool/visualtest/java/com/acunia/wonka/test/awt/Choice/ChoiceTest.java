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


package com.acunia.wonka.test.awt.Choice;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class ChoiceTest extends VisualTestImpl implements ActionListener, ItemListener {
  private TextField newItem;
  private Button addButton;
  private Choice items;
  private Label selection;
  private Button removeButton;
  private int selectedIndex;

  private final static int ITEMCOUNT=10;
  private final static String ITEMNAME="item";

  public ChoiceTest() {
    this(ITEMCOUNT,ITEMNAME);
  }
  public ChoiceTest(int itemcount, String itemname) {
    super();
    //layout
    setLayout(new BorderLayout());
      Panel top = new Panel(new GridLayout(2,1));
        newItem = new TextField();
      top.add(newItem);
        addButton = new Button("add this");
        addButton.addActionListener(this);
      top.add(addButton);
    add(top, BorderLayout.NORTH);

      items = new Choice();
      items.addItemListener(this);
    add(items, BorderLayout.CENTER);

      Panel bottom = new Panel(new GridLayout(2,1));
        selection= new Label("No selection made yet");
        selectedIndex = -1;
      bottom.add(selection);
        removeButton = new Button("remove this");
        removeButton.addActionListener(this);
      bottom.add(removeButton);
    add(bottom, BorderLayout.SOUTH);

    // fill box
    for(int i=0; i<itemcount; i++) {
      items.add(itemname+"_"+i);
    }

  }

  public String getHelpText() {
    return "A test for class 'Choice'. The central part of the panel shows a Choice that can be expanded by pressing its right " +
           "button. At the top of the panel, a textfield allows you to enter the label of a new element for the choice. The " +
           "new element can be added by pressing the button labeled 'add this', below the textfield." +
           "At the bottom of the panel another button labeled 'remove this' allows to remove the selected element from the " +
           "choice. The selected element is indicated in the central part of the panel, above the 'remove' button.";

  }
  /** Our Action listener:
  * when 'add' pressed, add contents of textframe
  * when delete pressed, deete current selection
  */
  public void actionPerformed(ActionEvent evt) {
    if(evt.getSource()==addButton && newItem.getText()!="") {
      items.add(newItem.getText());
    }
    else if(evt.getSource()==removeButton && selectedIndex>=0) {
      items.remove(selectedIndex);
      selectedIndex = -1;
      items.select(-1);
    }
  }

  /** Our item listener:
  * when choosen an item from the Choice, dispay it
  */
  public void itemStateChanged(ItemEvent evt) {
    // we don't need the event data here, we can just as well get everything from the Choice
    selectedIndex=items.getSelectedIndex();
    selection.setText( ((evt.getStateChange()==ItemEvent.SELECTED)?"selected :":"deselected :")+evt.getItem());
  }

}
