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

public class Insets implements Cloneable, java.io.Serializable {

  public int top;
  public int left;
  public int bottom;
  public int right;
  
  public Insets(int top, int left, int bottom, int right) {
    this.top = top;
    this.left = left;
    this.bottom = bottom;
    this.right = right;
  }

  public Object clone(){
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public boolean equals(Object object) {
    if (object instanceof Insets) {
      Insets in = (Insets)object;
      return (in.top == this.top
              && in.left == this.left
              && in.bottom == this.bottom
              && in.right == this.right    );
    }
    else {
      return false;
    }
  }

  public String toString() {
    return getClass().getName() + "[top = " + top + ", left = " + left + ", bottom = " + bottom + ", right = " + right + "]";
  }
  
}
