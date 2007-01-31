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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


package com.acunia.wonka.net.jar;

import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;

/**
 *
 * @version     $Id: ImmutableJarEntry.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
 *
 */
public class ImmutableJarEntry extends JarEntry {

  public ImmutableJarEntry (JarEntry je) {
    super(je);
  }

  public ImmutableJarEntry (ZipEntry ze) {
    super(ze);
  }

  public Object clone() {
     return new ImmutableJarEntry(this);
  }

  public void setComment(String comment) {
     throw new UnsupportedOperationException();
  }

  public void setExtra(byte[] extra) {
     throw new UnsupportedOperationException();
  }

  public void setMethod(int method) {
     throw new UnsupportedOperationException();
  }

  public void setCrc(long crc) {
     throw new UnsupportedOperationException();
  }

  public void setCompressedSize(long size) {
     throw new UnsupportedOperationException();
  }

  public void setSize(long size) {
     throw new UnsupportedOperationException();
  }

  public void setTime(long time) {
     throw new UnsupportedOperationException();
  }

}
