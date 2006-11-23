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


/*
** $Id: SerialPortEvent.java,v 1.1.1.1 2004/07/12 14:07:48 cvs Exp $
*/

package javax.comm;

import java.util.*;

public class SerialPortEvent extends EventObject {

  public static final int DATA_AVAILABLE = 1;
  public static final int OUTPUT_BUFFER_EMPTY = 2;
  public static final int CTS = 3;
  public static final int DSR  = 4;
  public static final int RI = 5;
  public static final int CD = 6;
  public static final int OE = 7;
  public static final int PE = 8;
  public static final int FE = 9;
  public static final int BI = 10;

  private int     eventtype;
  private boolean oldvalue;
  private boolean newvalue;

  public SerialPortEvent (Object o) {
    super(o);
  }

  public int getEventType() {
    return this.eventtype;
  }

  public boolean getNewValue() {
  return this.newvalue;
  }

  public boolean getOldValue() {
  return this.oldvalue;
  }

}

