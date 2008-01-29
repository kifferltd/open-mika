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

import java.awt.peer.*;

public class CheckboxGroup implements java.io.Serializable {

  private static final long serialVersionUID = 3729780091441768983L;
  
  Checkbox selectedCheckbox = null;

  public CheckboxGroup() {
  }
    
  public synchronized void setSelectedCheckbox(Checkbox checkbox) {

    boolean success = true;

    if (selectedCheckbox != null) {
      if (selectedCheckbox.group == this) {
        selectedCheckbox.state = false;
        ((CheckboxPeer)selectedCheckbox.getPeer()).setState(false);
      }
      else {
        success = false;
      }
    }

    if (success == true) {
      checkbox.state = true;
      ((CheckboxPeer)checkbox.getPeer()).setState(true);
      selectedCheckbox = checkbox;
    }

  }

  public Checkbox getSelectedCheckbox() {
    return selectedCheckbox;
  }

  public synchronized void setCurrent(Checkbox checkbox) {
    setSelectedCheckbox(checkbox);
  }

  public Checkbox getCurrent() {
    return selectedCheckbox;
  }

  /**
   * Diagnostics
   */
  public String toString() {
    return getClass().getName();
  }
  public String paramString() {
    return getClass().getName() + "selected box:" + selectedCheckbox;
  }
}
