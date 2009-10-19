/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
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


/*
** $Id: Package.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.lang;

import java.net.URL;
import wonka.vm.SystemClassLoader;

public class Package extends Object {

  private String name;
  private String spectitle;
  private String specvendor;
  private String specversion;
  private String impltitle;
  private String implvendor;
  private String implversion;
  private URL    sealbase;

  /** The constructor has package access, so ClassLoader can use it.
  */
  Package ( String name, 
            String spectitle, String specversion, String specvendor, 
            String impltitle, String implversion, String implvendor, 
            URL sealbase )
  {
    this.name = name;
    this.spectitle = spectitle;
    this.specversion = specversion;
    this.specvendor = specvendor;
    this.impltitle = impltitle;
    this.implversion = implversion;
    this.implvendor = implvendor;
    this.sealbase = sealbase;
  }

  /** Another constructor.
   */
  Package(String name) {
    this.name = name;
  }

  /** Get the implementation title of this Package.
   */
  public String getImplementationTitle() {
    return this.impltitle;
  }

  /** Get the implementation vendor of this Package.
   */
  public String getImplementationVendor() {
    return this.implvendor;
  }

  /** Get the implementation version of this Package.
   */
  public String getImplementationVersion() {
    return this.implversion;
  }

  /** Get the specification title of this Package.
   */
  public String getSpecificationTitle() {
    return this.spectitle;
  }

  /** Get the specification vendor of this Package.
   */
  public String getSpecificationVendor() {
    return this.specvendor;
  }

  /** Get the specification version of this Package.
   */
  public String getSpecificationVersion() {
    return this.specversion;
  }

  /** Get the name of this Package.
   */
  public String getName() {
    return this.name;
  }

  /** Returns true iff this package is sealed.
   */
  public boolean isSealed() {
    return sealbase != null;
  }

  /** Returns true iff this package is sealed with the given URL.
   */
  public boolean isSealed(URL sealer) {
    return sealbase == sealer;
  }

  /** Returns true iff this package is compatible with the given version.
   ** Each component of the specification version of this Package is compared
   ** with the corresponding component of the target; if the component of this 
   ** version is greater than the target then we return true, if it is less
   ** we return false, if it is equal we move on to the next component.
   ** The spec doesn't say what to do if one version has less components
   ** than the other, so we treat "missing" components as if they were zero.
   */
  public boolean isCompatibleWith(String target)
    throws NumberFormatException
  {
    String this_remainder = specversion;
    String that_remainder = target;
    int this_component;
    int that_component;
    int this_dot;
    int that_dot;

    while (this_remainder.length() > 0 || that_remainder.length() > 0) {
      this_dot = this_remainder.indexOf('.');
      if (this_dot >= 0) {
        this_component = Integer.parseInt(this_remainder.substring(0,this_dot));
      }
      else {
        this_component = 0;
      }
      this_remainder = this_remainder.substring(this_dot+1);

      that_dot = that_remainder.indexOf('.');
      if (that_dot >= 0) {
        that_component = Integer.parseInt(that_remainder.substring(0,that_dot));
      }
      else {
        that_component = 0;
      }
      that_remainder = that_remainder.substring(that_dot+1);

      if (this_component != that_component) {

        return this_component > that_component;

      }
    }

    return true;
  }

  /** Find a package by name.  Uses the caller's ClassLoader.
   */
  public static Package getPackage(String name) {
    ClassLoader cl = ClassLoader.getCallingClassLoader();
    if (cl == null) {
      cl = SystemClassLoader.getInstance();
    }

    return cl.getPackage(name);
  }

  /** Get all known packages.  Uses the caller's ClassLoader.
   */
  public static Package[] getPackages() {
    ClassLoader cl = ClassLoader.getCallingClassLoader();
    if (cl == null) {
      cl = SystemClassLoader.getInstance();
    }

    return cl.getPackages();
  }

  /** The hashcode of a Package is simply the hashcode of its name.
   */
  public int hashCode() {
    return name.hashCode();
  }

  /** The string representation is ``package'' plus name, title, version.
   */
  public String toString() {
    return "package "+name
    + (spectitle != null ? " "+spectitle : "" )
    + (specversion != null ? " "+specversion : "" );
  }
}  
