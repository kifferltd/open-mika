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


// Author: J. Vandeneede
// Created: 2001/03/13

package com.acunia.wonka.test.awt.Scrollbar;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import com.acunia.wonka.test.awt.VisualTest;
import com.acunia.wonka.test.awt.VisualTestImpl;
import com.acunia.wonka.test.awt.VisualTester;


public class ScrollbarTest extends VisualTestImpl  implements AdjustmentListener,VisualTest
{

    Panel card = new Panel();
    Scrollbar sh1 = new Scrollbar(java.awt.Scrollbar.HORIZONTAL);
    Scrollbar sh2 = new Scrollbar(java.awt.Scrollbar.HORIZONTAL);
    Scrollbar sv1 = new Scrollbar(java.awt.Scrollbar.VERTICAL);
    Scrollbar sv2 = new Scrollbar(java.awt.Scrollbar.VERTICAL);
    String logtest;

    public ScrollbarTest()
    {
        // build layout
        card.setLayout(new CardLayout());
        setLayout(new BorderLayout());
        sh1.addAdjustmentListener(this);
        add(sh1, BorderLayout.NORTH);
        sh2.addAdjustmentListener(this);
        add(sh2, BorderLayout.SOUTH);
        sv1.addAdjustmentListener(this);
        add(sv1, BorderLayout.EAST);
        sv2.addAdjustmentListener(this);
        add(sv2, BorderLayout.WEST);
        add(card, BorderLayout.CENTER);
        card.setBackground(Color.green);
//        setSize(199, 199);

        logtest = new String(); //""

 //       show();

    }

  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    Scrollbar changed = (Scrollbar)evt.getAdjustable();//(Scrollbar)evt.getSource();
      Scrollbar mirror =  getMirror(changed);

      String eventtype;

      switch (evt.getAdjustmentType() )
      {
      	case AdjustmentEvent.UNIT_DECREMENT:
      		eventtype = "UNIT_DECREMENT (left/upper button pressed)";
      		break;
      	case AdjustmentEvent.BLOCK_DECREMENT:
      		eventtype = "BLOCK_DECREMENT (clicked scrollbar left of/above scrollbox)";
      		break;
      	case AdjustmentEvent.TRACK:
      		eventtype = "TRACK (dragged scrollbox)";
      		break;
      	case AdjustmentEvent.BLOCK_INCREMENT:
      		eventtype = "BLOCK_INCREMENT (clicked scrollbar right of/under scrollbox)";
      		break;
      	case AdjustmentEvent.UNIT_INCREMENT:
      		eventtype = "UNIT_INCREMENT (right/lower button pressed)";
      		break;
      	default:
      		eventtype = "Unknown command (if this occurs, shoot the Wonka inplementation programmer)";
      }

      //print scrollbar values
      logtest += " Settings for Scrollbar <"+getName(changed)+"> : \n";
      logtest += " => Event type : "+eventtype + " value : "+evt.getValue()+ "\n";
      logtest += " Current value:"+evt.getValue();
      logtest += " mirrorred to"+getName(mirror)+ "\n\n";

      // mirror settings of changed scrollbar to opposite
      mirror.setValues(changed.getValue(),changed.getVisibleAmount(),changed.getMinimum(),changed.getMaximum());
  }

  private Scrollbar getMirror(Scrollbar source)
  {
    Scrollbar mirror = sv2; //         if(source == sv1)

    if(source == sv2)
      mirror = sv1;
    else if(source == sh1)
      mirror = sh2;
    else if(source == sh2)
      mirror = sh1;
    return mirror;
  }

  private String getName(Scrollbar source)
  {
    String name = "sv1";  // if(source == sv1)

    if(source == sv2)
      name = "sv2";
    else if(source == sh1)
      name = "sh1";
    else if(source == sh2)
      name = "sh2";
    return name;
  }


 	public String getTitle(){
 	 	return "ScrollbarTest"; // "ScrollbarTest : coupled horizontal and vertical scrollbars";
 	}
 	public String getHelpText(){
 	 	return getTitle()+":\nShould display a BorderLayout containing two horizontal and two vertical scrollbars.\n"
 	 	+"The bars are coupled  so that every change in one of the bars results in an equal change of its counterpiece.";
 	}
     	
 	public java.awt.Panel getPanel(VisualTester vt){
 	 	return this;
 	}
     	
 	public void log(java.awt.Panel p, java.io.Writer w, boolean b)throws java.io.IOException {
 		w.write(logtest);
 		logtest = "";
 	}

  public void start(java.awt.Panel p, boolean b){}
  public void stop(java.awt.Panel p){}


  static public void main (String[] args)
  {
        new ScrollbarTest();
  }
}
