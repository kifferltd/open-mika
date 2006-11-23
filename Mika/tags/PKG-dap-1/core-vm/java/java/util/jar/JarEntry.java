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


package java.util.jar;

import java.util.zip.ZipEntry;
import java.security.cert.Certificate;
import java.io.IOException;

/**
 *
 * @version     $Id: JarEntry.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
 *
 */
public class JarEntry extends ZipEntry {

  private Attributes attributes=null;

  public JarEntry (ZipEntry child) {
    super (child);
  }

  public JarEntry (String jname) {
  	super(jname);
  }

  public JarEntry (JarEntry je) {
  	super(je);
  	attributes = je.attributes;
  }


/**
** THIS METHOD ALWAYS RETURNS NULL
** --> java.cert.Certificate is not yet supported !!!
*/
  public Certificate[] getCertificates() {
    return null;
  }

  public Attributes getAttributes() throws IOException {
   	return attributes;
  }

  void setAttributes(Attributes attr) {
   	attributes = attr;
  }

}
