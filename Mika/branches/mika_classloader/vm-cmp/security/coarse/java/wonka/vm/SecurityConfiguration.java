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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


/*
** $Id: SecurityConfiguration.java,v 1.1.1.1 2004/07/12 14:07:48 cvs Exp $
*/

package wonka.vm;

/**
 ** This interface gathers together a number of constants which affect the
 ** behaviour of Wonka's security framework.
 */
public interface SecurityConfiguration {

  /**
   ** The two flags USE_SECURITY_MANAGER and USE_ACCESS_CONTROLLER
   ** determine how access control checks are performed by the Wonka
   ** class libaries.  If both are false then no checks are performed.
   **
   ** If USE_ACCESS_CONTROLLER is true, the Wonka classes will call the 
   ** AccessController directly to perform security checks.
   ** This flag overrides USE_SECURITY_MANAGER.
   */
  public final boolean USE_ACCESS_CONTROLLER = false;

  /**
   ** If USE_SECURITY_MANAGER is true, the Wonka classes will use the idiom
   **    SecurityManager s = System.getSecurityManager();
   **    if (s != null) {
   **      s.checkFoo(bar);
   **     }
   ** to perform security checks.
   */
  public final boolean USE_SECURITY_MANAGER = true;

  /**
   ** If SET_SECURITY_MANAGER is true, com.acunia.wonka.Init will instantiate 
   ** the default SecurityManager before invoking the starting class.  The
   ** default SecurityManager uses the AccessController.
   **
   ** If SET_SECURITY_MANAGER is false, it is up to the user to install a
   ** SecurityManager if one is required. 
   */
  public final boolean SET_SECURITY_MANAGER = false;

}

