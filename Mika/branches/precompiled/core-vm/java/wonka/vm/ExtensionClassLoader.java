/**************************************************************************
* Copyright (c) 2003 by Acunia N.V. All rights reserved.                  *
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
** $Id: ExtensionClassLoader.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package wonka.vm;

import java.net.URL;

/** The application class loader is a singleton.
** It searches for class and resource files in the class path given in
** the command that launched Wonka.
*/
public final class ExtensionClassLoader extends java.net.URLClassLoader {

  /**
   ** The one and only instance of ExtensionClassLoader.
  */
  private static ExtensionClassLoader theExtensionClassLoader;

  public synchronized static ExtensionClassLoader getInstance(URL[] urls, ClassLoader parent) {
    if (theExtensionClassLoader == null) {
      theExtensionClassLoader = new ExtensionClassLoader(urls, parent);
    }

    return theExtensionClassLoader;
  }


  /**
  ** A private constructor, so that only getInstance() can create an instance.
  */
  private ExtensionClassLoader (URL[] urls, ClassLoader parent) throws SecurityException {
    super(urls, parent);
  }

  public String toString() {
    return "Extension Class Loader";
  }
}
