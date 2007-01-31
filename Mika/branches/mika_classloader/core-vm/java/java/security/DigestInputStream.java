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
** $Id: DigestInputStream.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.security;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

public class DigestInputStream extends FilterInputStream {

  protected MessageDigest digest;
  private boolean digesting = false;

  public DigestInputStream(InputStream stream, MessageDigest digest) {
    super(stream);
    this.digest = digest;
  }

  public MessageDigest getMessageDigest() {
    return digest;
  }

  public void setMessageDigest(MessageDigest d) {
    digest = d;
  }

  public int read() throws IOException {
    int rd = in.read();
    if (digesting && rd != -1) {
      digest.update((byte)rd);
    }
    return rd;
  }

  public int read(byte[] b, int off, int len) throws IOException {
    int rd = in.read(b,off,len);
    if (digesting && rd != -1) {
      digest.update(b,off,len);
    }

    return rd;
  }

  public void on (boolean foo) {
    digesting = foo;
  }

  public String toString() {
    return "DigestInputStream on inputstream "+in+" and digest "+digest;
  }
  
}
