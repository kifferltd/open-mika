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
