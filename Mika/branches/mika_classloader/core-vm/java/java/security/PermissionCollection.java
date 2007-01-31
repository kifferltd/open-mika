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


/**
 * $Id: PermissionCollection.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
 */

package java.security;

import java.io.Serializable;
import java.util.Enumeration;

public abstract class PermissionCollection implements Serializable {

  private boolean readOnly;

  public PermissionCollection() {
  }

  public abstract void add (Permission permission);
  public abstract boolean implies (Permission permission);
  public abstract Enumeration elements();

  public void setReadOnly() {
    this.readOnly = true;
  }

  public boolean isReadOnly() {
    return this.readOnly;
  }

  public String toString() {
    StringBuffer answerBuffer = new StringBuffer(super.toString());
    answerBuffer.append(" (");
    answerBuffer.append(GetSystemProperty.LINE_SEPARATOR);
    for (Enumeration e = elements() ; e.hasMoreElements() ;) {
      answerBuffer.append(e.nextElement().toString());
      answerBuffer.append(GetSystemProperty.LINE_SEPARATOR);
    }
    answerBuffer.append(')');
    answerBuffer.append(GetSystemProperty.LINE_SEPARATOR);
    return answerBuffer.toString();
  }
}
