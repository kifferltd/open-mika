package com.acunia.wonka.test.awt.dnd;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import com.acunia.wonka.test.awt.VisualTestImpl;

public class TestText extends VisualTestImpl {
  Component component1, component2, component3, component4, component5;
  Button button1;

  public TestText() {
    setLayout(new GridLayout(3,2));

    setBackground(Color.white);

    component1 = new DropText("Drop here");
    component2 = new DropText("or drop here :)");
    component3 = new DragDropText("you can drag this one or just drop here");
    component4 = new DragText("Drag this field");
    component5 = new DragText("or drag this one");
    button1 = new Button("button, so you have another component ;)");

    add(component1);
    add(button1);
    add(component2);
    add(component3);
    add(component4);
    add(component5);

    component1.setVisible(true);
    component2.setVisible(true);
    component3.setVisible(true);
    component4.setVisible(true);
    component5.setVisible(true);
    button1.setVisible(true);
    setVisible(true);
  }
  
  public String getHelpText(){
    return "";
  }

}
