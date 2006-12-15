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

/*
** $Id: Adjustable.java,v 1.1 2005/06/15 09:05:17 cvs Exp $
*/

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
