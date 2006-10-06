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
