/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
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

package java.awt;

public interface Adjustable {

  /**
   * @status Declared with integer values HORIZONTAL = 0 / VERTICAL = 1
   * @remark The Java specification does not provide obligatory values for HORIZONTAL and VERTICAL but sun Java seems to have the values 0 and 1, so we stick to these.
   */

  public static final int HORIZONTAL = 0;

  public static final int VERTICAL = 1;

  public void addAdjustmentListener(java.awt.event.AdjustmentListener listener);
  
  public int getBlockIncrement();
  
  public int getMaximum();
  
  public int getMinimum();
  
  public int getOrientation();
  
  public int getUnitIncrement();
  
  public int getValue();
  
  public int getVisibleAmount();
  
  public void removeAdjustmentListener(java.awt.event.AdjustmentListener listener);
  
  public void setBlockIncrement(int increment);
  
  public void setMaximum(int maximum);

  public void setMinimum(int minimum);

  public void setUnitIncrement(int increment);
  
  public void setValue(int value);
  
  public void setVisibleAmount(int amount);
}
