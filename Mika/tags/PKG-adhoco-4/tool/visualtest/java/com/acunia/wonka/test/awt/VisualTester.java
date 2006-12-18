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


package com.acunia.wonka.test.awt;

/**
** This interface should provide the API which can be used by the VisualTest
** to interact with the actual TestApplication.
**
*/
public interface VisualTester {

  /**
  ** this method passes a String to the VisualTester to be logged.
  ** the String should only contain information. It is up to VisualTester wrap it
  ** in a suited format ...
  **
  ** @param logInfo a non-null String to be logged.
  ** @param vtest reference to the test calling the log method.
  */
  public void log(String logInfo, VisualTest vtest);
    	
  /**
  ** this method passes a String and an Exception to the VisualTester to be logged.
  ** the String should only contain information. It is up to VisualTester wrap it
  ** in a suited format ...
  **
  ** The VisualTester will (should) log a stack trace of the exception.
  **
  ** @param logInfo a non-null String to be logged.
  ** @param vtest reference to the test calling the log method.
  ** @param exception the non-null Exception to be logged
  */
  public void logException(String logInfo, VisualTest vtest, Throwable exception);
    	
  /**
  ** this method ask the VisualTester which VisualTest he is running
  **
  ** @return the test currently running or null if no test is running
  */
  public VisualTest getCurrentTest();

  /**
  ** this method ask the VisualTester in which Frame he displays the VisualTests.
  **
  ** @return Frame the used by the VisualTester.
  */
  public java.awt.Frame getFrame();
}
