/**************************************************************************
* Copyright (c) 2003 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

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
