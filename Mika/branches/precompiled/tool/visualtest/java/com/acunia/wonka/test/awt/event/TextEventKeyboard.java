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

public class TextEventKeyboard extends VisualTestImpl implements TextListener{

  private NamedTextField visibleField;
  private NamedTextField maskedField;
  private NamedTextField crossedField;
  private NamedTextArea area;
  private List display;

  public TextEventKeyboard() {
    setForeground(Color.white);
    setFont(new Font("courR14", 0, 14));// 14 poinr courier for better visibility
    int step = 150/6;
    int green = 100;
    int shade = 80;
    setLayout(new BorderLayout());
    Panel fields = new Panel(new GridLayout(4,2));
        Label l1=new Label("Normal text field");
        l1.setBackground(new Color(shade, green, shade));
      fields.add(l1);
        visibleField = new NamedTextField("<VISIBLE FIELD>");
        visibleField.addTextListener(this);
        visibleField.setBackground(new Color(shade,green,shade));
      fields.add(visibleField);
      green+=step;
        Label l2=new Label("masked (stars)");
        l2.setBackground(new Color(shade, green, shade));
      fields.add(l2);
        maskedField = new NamedTextField("<MASKED BY [*]>");
        maskedField.setEchoChar('*');
        maskedField.addTextListener(this);
        maskedField.setBackground(new Color(shade,green,shade));
      fields.add(maskedField);
      green+=step;
        Label l3=new Label("masked (points)");
        l3.setBackground(new Color(shade, green, shade));
      fields.add(l3);
        crossedField = new NamedTextField("<MASKED BY [.]>");
        crossedField.addTextListener(this);
        crossedField.setEchoChar('.');
        crossedField.setBackground(new Color(shade,green,shade));
      fields.add(crossedField);
      green+=step;
        Label l4=new Label("Text Area:");
        l4.setBackground(new Color(shade, green, shade));
      fields.add(l4);
        Label l5=new Label();
        l5.setBackground(new Color(shade, green, shade));
      fields.add(l5);
      green+=step;
    add(fields, BorderLayout.NORTH);

      area = new NamedTextArea("<TextArea>");
      area.addTextListener(this);
      area.setFont(new Font("courR14", 0, 14));// 14 poinr courier for better visibility
      area.setBackground(new Color(shade,green,shade));
      //green+=step;
    add(area, BorderLayout.CENTER);

      display=new List(2,false);
      display.setBackground(new Color(shade,green,shade));
      display.setFont(new Font("courR14", 0, 14));// 14 poinr courier for better visibility
      display.add("Your ActionEvents displayed HERE");
    add(display, BorderLayout.SOUTH);
  }

  /************************************************************************************************************/
  /** text event to change text of the textfield and area
  */
  public void textValueChanged(TextEvent evt) {
    if(display.getItemCount()>20){
      display.removeAll();
    }
    TextComponent source = (TextComponent)evt.getSource();
    String contents = source.getText();
    display.add(source+" : ["+contents+"] len="+contents.length(), 0);
  }


  public String getHelpText() {
    return "The aim: test the throwing of TextEvents in TextAreas and TextFields by keyboard input:\n\n"+
           "The test: \n"+
           "Change the text in the three TextFields and the TextArea and see the TextEvent thrown"+
           " that gives this field or area's new length \n\n"+
           "See that the second TextField masks all new characters into stars and the third TextField masks all new characters into plusses";
  }

  /**
  * inner class TextArea with name
  */
  class NamedTextArea extends TextArea{
    private String name;

    public NamedTextArea(String name){
      super("???",5,30,TextArea.SCROLLBARS_VERTICAL_ONLY);
      this.name = name;
    }

    public String toString() {
      return name;
    }
  }

  /**
  * inner class TextField with name
  */
  class NamedTextField extends TextField{
    private String name;

    public NamedTextField(String name){
      super("???");
      this.name = name;
    }

    public String toString() {
      return name;
    }
  }

}
