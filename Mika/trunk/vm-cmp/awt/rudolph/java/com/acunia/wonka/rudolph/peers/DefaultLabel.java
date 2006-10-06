/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package com.acunia.wonka.rudolph.peers;

import java.awt.peer.*;
import java.awt.*;

public class DefaultLabel extends DefaultComponent implements LabelPeer {

  private String text;
  private int alignment;
  
  public DefaultLabel(Label label) {
    super(label);
  }
  
  public void setAlignment(int alignment) {
    this.alignment = alignment;
    paint(getGraphics());
  }
  
  public void setText(String text) {
    this.text = text;
    paint(getGraphics());
  }

  /*
  ** From DefaultComponent :
  */
  
  public Dimension getPreferredSize() {
    Label label = (Label)component;
    Font f = label.getFont();
    FontMetrics fm = getFontMetrics((f != null) ? f : Component.DEFAULT_FONT);
    int cx = fm.stringWidth(text);
    int cy = fm.getHeight() + 4;
    return new Dimension(cx, cy);
  }

  public void paint(Graphics g) {
    if(g == null) return;

	boolean debug = ((this.text!=null) && (this.text.indexOf("Helv")>-1));
	
	
	if(debug)
	{
		wonka.vm.Etc.woempa(9, "start with painting of "+this.toString()+"\n");
	}
	

    alignment = ((Label)component).getAlignment();
    text = ((Label)component).getText();

    if(text != null) {
      
      Dimension size = component.getSize();

	  Color back = component.getBackground();	  	  	  	  
	  
	  if((debug) && (back!=null))
	  {
		wonka.vm.Etc.woempa(9, "draw with back: "+back.toString()+"\n");
	  }	  
	  else if((debug) && (back==null))
	  {
		  wonka.vm.Etc.woempa(9, "draw with back is null\n");
	  }
	  
      g.setColor(back);
      g.fillRect(0, 0, size.width, size.height);
      

      Font f = component.getFont();
      FontMetrics fm = getFontMetrics((f != null) ? f : Component.DEFAULT_FONT);
      int sx = fm.stringWidth(text);
      
      switch(alignment) {
        case Label.LEFT:   
          sx = 0;
          break;
        case Label.CENTER:
          sx = (size.width - sx) / 2;
          break;
        case Label.RIGHT:
          sx = size.width - sx;
          break;
      }

	  Color fore = component.getForeground();
	  if((debug) && (fore!=null))
	  {
		wonka.vm.Etc.woempa(9, "draw with fore: "+fore.toString()+"\n");
	  }	  
	  else if((debug) && (fore==null))
	  {
		  wonka.vm.Etc.woempa(9, "draw with fore is null\n");
	  }
	  
      g.setColor(fore);
      g.drawString(text, sx, (size.height - fm.getHeight()) / 2 + fm.getAscent());

  //    super.paint(g);
	  
		if(debug)
		{
			wonka.vm.Etc.woempa(9, "end with painting of "+this.toString()+"\n");
		}
	  
    }
  }

}

