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
** $Id: UnsupportedMethodError.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package wonka.vm;

/**
** This Error is thrown when a method is called, but not implemented in the VM libraries.
** Instead of leaving a //TODO or empty an method throw UnsupportedMethodError. This makes it easier to track such methods
** an will be seen at runtime.
*/
public final class UnsupportedMethodError extends VirtualMachineError {

  public UnsupportedMethodError(String message){
    super(message);
  }

}

