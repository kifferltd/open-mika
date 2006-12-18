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
** $Id: MessageDigestSpi.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.security;

public abstract class MessageDigestSpi {

  public MessageDigestSpi(){}

  public Object clone() throws CloneNotSupportedException {
        return super.clone();
  }

  protected abstract byte[] engineDigest();
  protected abstract void engineReset();
  protected abstract void engineUpdate(byte input);
  protected abstract void engineUpdate(byte[] input, int offset, int len);

/**
** method was added in 1.2.  It should be abstract, but it cannot be for backworth compatibility.
** The implementation does nothing. It simply returns 0.
**
** @remark should be overridden
*/
  protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
        return 0;
  }
/**
** method was added in 1.2.  It should be abstract, but it cannot be for backworth compatibility.
** The implementation does nothing. It simply returns 0.
**
** @remark should be overridden
*/
  protected int engineGetDigestLength() {
        return 0;
  }
}
