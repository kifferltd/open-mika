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
 * $Id: PropertyPermission.java,v 1.2 2006/02/23 12:32:12 cvs Exp $
 */

package java.util;

import java.security.Permission;
import java.security.BasicPermission;
import java.security.PermissionCollection;

/**
 ** This class is like a simplified version of java.io.FilePermission, q.v..
 */
public final class PropertyPermission extends BasicPermission {

  private static final long serialVersionUID = 885438825399942851L;

  private String actions;

  private boolean read;
  private boolean write;

  private void parseActions(String s) {
    String sx = s.toLowerCase();
    while (sx != "") {
      int i = sx.indexOf(',');
      String s0;

      if (i<0) {
        s0 = sx.trim();
        sx = "";
      }
      else {
        s0 = sx.substring(0,i).trim();
        sx = sx.substring(i+1);
      }

      if (s0.equals("read")) {
        read = true;
      } 
      else {
      	if (s0.equals("write")) {
        	write = true;
        }
        else {
         	throw new IllegalArgumentException("udefined action encountered: "+s0);
        }
      }
    }
    if(read) {
	this.actions = (write ? "read,write" : "read");    	
    }	
    else {
     	this.actions = "write";		 		
    }
  }

  public PropertyPermission(String path, String actions) {
    super (path);
    parseActions(actions);
  }

  public boolean implies (Permission p) {
    try {
      PropertyPermission pp = (PropertyPermission)p;
      if ((pp.read && !this.read)||(pp.write && !this.write)) {
         return false;
       }

      String thisname = super.getName();
      String othername = p.getName();

      if (thisname == "*") {
         return true;
      }
      if (thisname.endsWith(".*")) {
        return othername.length() >= thisname.length() &&
        othername.startsWith(thisname.substring(0,thisname.length()-1));
      }
      else {
        return othername.equals(thisname);
      }
    } catch (ClassCastException e) {

      return false;

    }
  }

  public boolean equals (Object o) {
      if(!(o instanceof PropertyPermission)) {
         return false;       	
      }
      PropertyPermission pp = (PropertyPermission)o;
      if ((pp.read != this.read) || (pp.write != this.write)) {
         return false;
      }
      return pp.getName().equals(super.getName());
  }

  public int hashCode() {
    int h = super.getName().hashCode();
/**
  unfortunenatly sun implementation doesn't use the specified actions to calculate the hashCode
    if (this.read) {
      h ^= 0x80402010;
    }
    if (this.write) {
      h ^= 0x20408010;
    }
*/
    return h;
  }

  public String getActions() {
    return actions;
  }

  public PermissionCollection newPermissionCollection() {
    return new com.acunia.wonka.security.PropertyPermissionCollection();
  }
}
