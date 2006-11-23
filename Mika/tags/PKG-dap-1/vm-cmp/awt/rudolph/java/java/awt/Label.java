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

public class Label extends Component {
  // Local variables:
  String text;
  int alignment = LEFT;

  // Alignment types:
  public static final int LEFT = 0;
  public static final int CENTER = 1;
  public static final int RIGHT = 2;

  private LabelPeer peer;

  public Label() {
    this("", LEFT);
  }
  
  public Label(String label) {
    this(label, LEFT);
  }
  
  public Label(String text, int alignment) {
    setText(text);

    switch (alignment) {
      case LEFT:
      case CENTER:
      case RIGHT:
        setAlignment(alignment);
        break;
      default:
         throw new IllegalArgumentException("invalid alignment: " + alignment);
    }
  }

  public void addNotify() {
    if(peer == null) {
      peer = getToolkit().createLabel(this);
    }
    
    if (notified == false) {
      super.addNotify();
    }
  }
  
  public int getAlignment() {
    return this.alignment;
  }
  
  public String getText() {
    return this.text;
  }
  
  public synchronized void setAlignment(int alignment) {
    switch (alignment) {
      case LEFT: 
      case CENTER:
      case RIGHT:
        this.alignment = alignment;
        break;
      default:
        throw new IllegalArgumentException("improper alignment: " + alignment);
    }

    valid = false;
    
    peer.setAlignment(alignment);
  }
  
  public synchronized void setText(String text) {
    this.text = text;

    invalidate();
    // valid = false;
    // if(parent != null) parent.valid = false;

    peer.setText(text);
  }

  public String toString() {
    return getClass().getName() +" - text: "+ text +", bounds: x = "+ x +", y = "+ y +", w = "+ width +", h = "+ height;
  }

  protected String paramString() {
    return getClass().getName() +" text=["+ text +"] alignment="+alignment+", bounds:("+ x +", "+ y +", "+ width +", "+ height+")";
  }
}

