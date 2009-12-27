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


// Author: N. Oberfeld
// Created: 2001/09/26

package com.acunia.wonka.test.awt.event;

/*****************************************************************************************************************************************/
/**
* The CollectEvent interface is used to provide a framework for interaction between an event generating test field
* and a VisualTestImpl test class analysing these events. It is designed symmetrically so the function it provides
* can be used by the event generating auxilliary class as well as the visual test that catches them.
* It provides two functions: a display function to which an other class can send the (event) message to display
* and a copy of the VisualTestImpl helptext function on which the event generating auxilliary can send a description of
* its behavior to the main VisualTestImpl class
*/

public interface CollectsEvents {
  /** Display a text sent by another class */
  public void displayMessage(String[] message);

  /** display a help text as in to the VisualTestImpl function getHelpText() (a visual test already implements this function)*/
  public String getHelpText();
}
