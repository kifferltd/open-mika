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

public interface VisualTest {

 /**
 ** This method retrieves a title which will be displayed by the VisualTestEngine.
 ** This should be the name of the test ...
 **
 ** @return a non-null String repesenting the name of the test.
 */
 //public String getTitle();
      	
 /**
 ** Asks the VisualTest for a help text.
 ** This method is very important.  It should return a string explaining what
 ** the test should do.  It should also explain how you could determine if the test
 ** succeeded or not.
 **
 ** @return a non-null String repesenting extra on the test.
 */
  public String getHelpText();
      	
 /**
 ** This method should provide a panel representing the test.
 ** This panel will be used in the VisualTestEngine.
 **
 ** @param vt VisualTester calling this test.
 ** @return a non-null panel to be displayed by the VisualTestEngine.
 */
  public java.awt.Panel getPanel(VisualTester vt);
      	
 /**
 ** this method will be called after the tester presses the OK or BAD button.
 ** this method should return a string discribe additional info test information.
 ** This string will be written to log file together with more general information
 ** like testName, a timestamp ...
 **
 ** @param p the Panel retrieved from getPanel.
 ** @param w the Writer to write extra log info to.
 ** @param passed true if test succeeded
 ** @return a non-null string to add to log message.
 */
  public String getLogInfo(java.awt.Panel p, boolean passed);

 /**
 ** This method will be called after the Panel p was added to the VTE.
 ** if the VTE is autorunning then b will true.
 **
 ** @param p the Panel retrieved from getPanel.
 ** @param b true if autorun is on
 */
  public void start(java.awt.Panel p, boolean b);

 /**
 ** This method will be called before the Panel p will removed from the VTE.
 ** This method can be used to stop threads created in the start method
 **
 ** @param p the Panel retrieved from getPanel.
 */
  public void stop(java.awt.Panel p);

 /**
 ** This method will be called when the current test was moved to the background and
 ** needs to be shown again.  This occurs when the VisualTester came into the foreground, but now leaves
 ** the screen for the test ...
 */
  public void showTest();

 /**
 ** This method will be called if the VisualTester want the display. 'showTest' is called to indicate
 ** the test can continue any activities.  hideTest/showTest could be used to stop and restart animations ...
 */
  public void hideTest();
}
