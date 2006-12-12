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
** abstract implementation of the VisualTest interface.
** classes extending this class MUST have at least a default constructor.
** this is needed to create objects of this class using Class.newInstance().
**
** Use this class as a base class for a VisualTest so if we add methods to the VisualTest
** Interface we only need to change the VisualTestImpl (lets hope we don't need to do it ...).
**
*/
public abstract class VisualTestImpl extends java.awt.Panel implements VisualTest{

  /**
  ** contains a reference to the VisualTester which created this test.
  ** will be set when getPanel is called.
  */
  protected VisualTester vt;
  	
  /**
  ** Default construtor.
  ** does nothing ...
  */
  public VisualTestImpl(){}
  	
  /**
  ** calls the panel constructor passing lm as an argument.
  */
  public VisualTestImpl(java.awt.LayoutManager lm){
    super(lm);
  }

  /**
  ** this method remains abstract to force the programmer to write a helpText.
  ** so please do ...
  **
  ** @return a non-null String representing test info.
  */
  public abstract String getHelpText();
       	
  /**
  ** this method set the vt field with supplied VisualTester.
  **
  ** the default implementation does:
  **		this.vt = vt;
  **		return this;
  **
  ** @param vt reference to a VisualTester
  ** @return returns a Panel to be displayed
  */
  public java.awt.Panel getPanel(VisualTester vt){
    this.vt = vt;
    return this;
  }
       	
  /**
  ** writes extra test info to Writer.
  **
  ** the default implementation writes "no logging info"
  **
  ** @param p the Panel retrieved from getPanel.
  ** @param w the Writer to write extra log info to.
  ** @param passed true if test succeeded
  */
  public String getLogInfo(java.awt.Panel p, boolean b) {
    return "no logging info !";
  }
  /**
  ** This method will be called after the Panel p was added to the VTE.
  ** if the VTE is autorunning then b will true.
  **
  ** the default implementation does nothing
  **
  ** @param p the Panel retrieved from getPanel.
  ** @param b true if autorun is on
  */
  public void start(java.awt.Panel p, boolean b){}

  /**
  ** This method will be called before the Panel p will removed from the VTE.
  ** This method can be used to stop threads created in the start method
  **
  ** the default implementation does nothing
  **
  ** @param p the Panel retrieved from getPanel.
  */
  public void stop(java.awt.Panel p){}

  /**
  ** this method is called to indicate the test should restart activities when it moved to foreground agian.
  **
  ** the default implementation does nothing ...
  */
  public void showTest(){}

  /**
  ** this method is called to indicate the test should stop activities because it will be moved to background.
  **
  ** the default implementation does nothing ...
  */
  public void hideTest(){}
}
