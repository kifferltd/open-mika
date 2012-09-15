/**************************************************************************
* Copyright (c) 2003 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package com.acunia.wonka.rudolph.popupkeyboard;

import java.awt.*;

public class KeyboardButtonText extends KeyboardButton {

  protected String key;

  public KeyboardButtonText(String key, int[] xPoints, int[] yPoints, int event, char keychar) {
    super(xPoints, yPoints, event, keychar);
    this.key = key;
  }

  public void paint_img(Graphics g) {
    super.paint_img(g);
    
    FontMetrics fm = g.getFontMetrics();
    
    g.setColor(Color.black);
    g.drawString(key, xPoints[0] + ((xPoints[1] - xPoints[0]) - fm.stringWidth(key)) / 2 + 1, 
                      yPoints[0] + ((yPoints[2] - yPoints[0]) - (fm.getAscent())) / 2 + fm.getAscent());
  }

  public void paint(Graphics g) {
    super.paint(g);
    
    FontMetrics fm = g.getFontMetrics();
    
    if(pressed) {
      g.setColor(Color.white);
    }
    else {
      g.setColor(Color.black);
    }
    g.drawString(key, xPoints[0] + ((xPoints[1] - xPoints[0]) - fm.stringWidth(key)) / 2 + 1, 
                      yPoints[0] + ((yPoints[2] - yPoints[0]) - (fm.getAscent())) / 2 + fm.getAscent());
  }

}

