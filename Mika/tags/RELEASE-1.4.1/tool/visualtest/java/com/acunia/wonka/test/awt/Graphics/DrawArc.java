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

package com.acunia.wonka.test.awt.Graphics;
import java.awt.*;
import com.acunia.wonka.test.awt.*;

public class DrawArc extends VisualTestImpl  {

  public DrawArc() {
    super();
    setBackground(Color.black);
  }

  public void paint(Graphics g) {
    g.setColor(Color.white);
    for(int i=1; i < 10; i++ ) {
      g.drawArc(20 + i * 30, 20, 25, 25, 0, i * 36); 
      g.drawArc(20 + i * 30, 50, 25, 25, i * 36, 30); 
      g.fillArc(20 + i * 30, 80, 25, 25, 0, i * 36); 
      g.fillArc(20 + i * 30, 110, 25, 25, i * 36, 30); 
    }
  }

  public String getTitle(){
    return "DrawArc";
  }
  public String getHelpText(){
    return "drawing arcs";
  }
     	
  public java.awt.Panel getPanel(VisualTester vt){
    return this;
  }
     	
  public String getLogInfo(java.awt.Panel p, boolean b){
    return "no logging info !";
  }
  public void start(java.awt.Panel p, boolean b){
  }

  public void stop(java.awt.Panel p){}
  
  static public void main (String[] args) {
    new DrawArc();
  }
}
