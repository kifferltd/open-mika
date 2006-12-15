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

package com.acunia.wonka.test.awt.Component;

import java.awt.*;
import java.awt.event.*;
import com.acunia.wonka.test.awt.*;

public class UpdateOrphanComponent extends VisualTestImpl {

  private Button bu;
  Label la;
  Checkbox cb;
  TextField tf;
  TextArea ta;
  List li;
  Scrollbar sb;

  Label l;   // the label used to display the status information

  public UpdateOrphanComponent() {

    setLayout(new BorderLayout());

    l = new Label();
    add(l, BorderLayout.SOUTH);

    /*
    ** First, we repaint the components implicitly *before* they have been added and removed.
    */

    l.setText("If you read this, the first round of tests failed.");
     
    bu = new Button("Here I am, all Button-ish");
    bu.setLabel("let's update an invisible Button");
   
    la = new Label("Here I am, all Label-ish");
    la.setText("let's update an invisible Label");
    
    cb = new Checkbox("Here I am, all Checkbox-ish");
    cb.setLabel("let's update an invisible Checkbox");
 
    tf = new TextField("Here I am, all TextField-ish");
    tf.setText("let's update an invisible TextField");
 
    ta = new TextArea("Here I am, all TextArea-ish");
    ta.setText("let's update an invisible TextArea");
 
    li = new List();
    li.add("let's update an invisible list");
 
    sb = new Scrollbar(Scrollbar.HORIZONTAL, 0, 64, 0, 255);
    sb.setOrientation(Scrollbar.VERTICAL);
    sb.setValue(50);
  
    /* 
    ** Second, we repaint the components explicitly *before* they have been added and removed.
    */
    
    l.setText("If you read this, the second round of tests failed.");
    
    bu = new Button("Here I am, all Button-ish");
    bu.setSize(50, 50);
    bu.repaint();
   
    la = new Label("Here I am, all Label-ish");
    la.setSize(50, 50);
    la.repaint();
    
    cb = new Checkbox("Here I am, all Checkbox-ish");
    cb.setSize(50, 50);
    cb.repaint();
 
    tf = new TextField("Here I am, all TextField-ish");
    tf.setSize(50, 50);
    tf.repaint();
 
    ta = new TextArea("Here I am, all TextArea-ish");
    ta.setSize(50, 50);
    ta.repaint();
 
    li = new List();
    li.add("let's update an invisible list");
    li.setSize(50, 50);
    li.repaint();
 
    sb = new Scrollbar(Scrollbar.HORIZONTAL, 0, 64, 0, 255);
    sb.setSize(50, 50);
    sb.repaint();

    /* 
    ** Third, we invalidate/validate the components explicitly *before* they have been added and removed.
    */
    
    l.setText("If you read this, the third round of tests failed.");
    
    bu = new Button("Here I am, all Button-ish");
    bu.setSize(50, 50);
    bu.invalidate();
    bu.validate();
   
    la = new Label("Here I am, all Label-ish");
    la.setSize(50, 50);
    la.invalidate();
    la.validate();
    
    cb = new Checkbox("Here I am, all Checkbox-ish");
    cb.setSize(50, 50);
    cb.invalidate();
    cb.validate();
 
    tf = new TextField("Here I am, all TextField-ish");
    tf.setSize(50, 50);
    tf.invalidate();
    tf.validate();
 
    ta = new TextArea("Here I am, all TextArea-ish");
    ta.setSize(50, 50);
    ta.invalidate();
    ta.validate();
 
    li = new List();
    li.add("let's update an invisible list");
    li.setSize(50, 50);
    li.invalidate();
    li.validate();
 
    sb = new Scrollbar(Scrollbar.HORIZONTAL, 0, 64, 0, 255);
    sb.setSize(50, 50);
    sb.invalidate();
    sb.validate();

    /*
    ** Fourth, we repaint the components implicitly *after* they have been added and removed.
    */
    
    l.setText("If you read this, the fourth round of tests failed.");
 
    bu = new Button("Here I am, all Button-ish");
    add(bu);
    remove(bu);
    bu.setLabel("let's update an invisible Button");
   
    la = new Label("Here I am, all Label-ish");
    add(la);
    remove(la);
    la.setText("let's update an invisible Label");
    
    cb = new Checkbox("Here I am, all Checkbox-ish");
    add(cb);
    remove(cb);
    cb.setLabel("let's update an invisible Checkbox");
 
    tf = new TextField("Here I am, all TextField-ish");
    add(tf);
    remove(tf);
    tf.setText("let's update an invisible TextField");
 
    ta = new TextArea("Here I am, all TextArea-ish");
    add(ta);
    remove(ta);
    ta.setText("let's update an invisible TextArea");
 
    li = new List();
    add(li);
    remove(li);
    li.add("let's update an invisible list");
 
    sb = new Scrollbar(Scrollbar.HORIZONTAL, 0, 64, 0, 255);
    add(sb);
    remove(sb);
    sb.setOrientation(Scrollbar.VERTICAL);
    sb.setValue(50);   
  
    /* 
    ** Fifth, we repaint the components explicitly *after* they have been added and removed.
    */
    
    l.setText("If you read this, the fifth round of tests failed.");
    
    bu = new Button("Here I am, all Button-ish");
    add(bu);
    remove(bu);
    bu.repaint();
   
    la = new Label("Here I am, all Label-ish");
    add(la);
    remove(la);
    la.repaint();
    
    cb = new Checkbox("Here I am, all Checkbox-ish");
    add(cb);
    remove(cb);
    cb.repaint();
 
    tf = new TextField("Here I am, all TextField-ish");
    add(tf);
    remove(tf);
    tf.repaint();
 
    ta = new TextArea("Here I am, all TextArea-ish");
    add(ta);
    remove(ta);
    ta.repaint();
 
    li = new List();
    li.add("let's update an invisible list");
    add(li);
    remove(li);
    li.repaint();
 
    sb = new Scrollbar(Scrollbar.HORIZONTAL, 0, 64, 0, 255);
    add(sb);
    remove(sb);
    sb.repaint();

    /* 
    ** Sixth, we invalidate/validate the components explicitly *after* they have been added and removed.
    */
    
    l.setText("If you read this, the sixth round of tests failed.");
    
    bu = new Button("Here I am, all Button-ish");
    add(bu);
    remove(bu);
    bu.invalidate();
    bu.validate();
   
    la = new Label("Here I am, all Label-ish");
    add(la);
    remove(la);
    la.invalidate();
    la.validate();
    
    cb = new Checkbox("Here I am, all Checkbox-ish");
    add(cb);
    remove(cb);
    cb.invalidate();
    cb.validate();
 
    tf = new TextField("Here I am, all TextField-ish");
    add(tf);
    remove(tf);
    tf.invalidate();
    tf.validate();
 
    ta = new TextArea("Here I am, all TextArea-ish");
    add(ta);
    remove(ta);
    ta.invalidate();
    ta.validate();
 
    li = new List();
    li.add("let's update an invisible list");
    add(li);
    remove(li);
    li.invalidate();
    li.validate();
 
    sb = new Scrollbar(Scrollbar.HORIZONTAL, 0, 64, 0, 255);
    add(sb);
    remove(sb);
    sb.invalidate();
    sb.validate();

    /*
    ** Seventh, we update the components after they have been removed from within an event handler.
    */

    l.setText("Click the component above to remove it, to redraw it, and to show the next component.");

    add(bu);

    bu.addMouseListener(new MouseListener(){
      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }

      public void mousePressed(MouseEvent e) {
      }

      public void mouseReleased(MouseEvent e) {
      }

      public void mouseClicked(MouseEvent e) {
        Component c = (Component)e.getSource();

        // Remove the component from its parent:
        remove(c);
 
        // Repaint the component in various ways:
        c.setBackground(Color.red);
        c.repaint();
        c.invalidate();
        c.validate();

        if (c instanceof Button) {
          add(la);
          la.addMouseListener(this);
        }
        else if (c instanceof Label) {
          add(cb);
          cb.addMouseListener(this);
        }
        else if (c instanceof Checkbox) {
          add(ta);
          ta.addMouseListener(this);
        }
        else if (c instanceof TextArea) {
          add(li);
          li.addMouseListener(this);
        }
        else if (c instanceof List) {
          add(sb);
          sb.addMouseListener(this);
        }
        else if (c instanceof Scrollbar) {
          add(tf);
          tf.addMouseListener(this);
        }
        else if (c instanceof TextField) {
          l.setText("If you read this, all tests passed successfully.");
        }  
        else {
          System.out.println("RemoveOrphanComponent: we shouldn't get here ...");
          System.exit(-1);
        }
 
        validate();
      }
    });
  }
 
  public String getHelpText() {
    return "Checks for a segfault when updating orphan components that still have to be added to their parent container or that have been removed from their container.";
  }
}

