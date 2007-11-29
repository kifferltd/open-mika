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
** $Id: ArrayIndexOutOfBoundsException.java,v 1.2 2006/03/27 08:19:05 cvs Exp $
*/

package java.lang;

public class ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException {
  private static final long serialVersionUID = -5116101128118950844L;

  public ArrayIndexOutOfBoundsException() {
  }

  public ArrayIndexOutOfBoundsException(int index) {
    super(index+" is out of bounds :>(");
  }

  public ArrayIndexOutOfBoundsException(String s) {
    super(s);
  }

}