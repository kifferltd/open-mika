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


package java.awt.event;

/************************************************************************/
/**
* Stub adapter implementation of a WindowListener
* Using the adapter methodology, instead of declaring your main class a WindowListener
* and having to implement all listener method, use a derived inner class of this adapter
* and add the events to this adapter. This allows you just to override those WindowListener
* functions you realy need
*/
public abstract class WindowAdapter implements WindowListener {

/** @remark empty stub implementation, override in derived class when needed */
  public void windowOpened(WindowEvent event) {}

/** @remark empty stub implementation, override in derived class when needed */
  public void windowClosing(WindowEvent event) {}

/** @remark empty stub implementation, override in derived class when needed */
  public void windowClosed(WindowEvent event) {}

/** @remark empty stub implementation, override in derived class when needed */
  public void windowIconified(WindowEvent event) {}

/** @remark empty stub implementation, override in derived class when needed */
  public void windowDeiconified(WindowEvent event) {}
 
/** @remark empty stub implementation, override in derived class when needed */
  public void windowActivated(WindowEvent event) {}

/** @remark empty stub implementation, override in derived class when needed */
  public void windowDeactivated(WindowEvent event) {}
}
