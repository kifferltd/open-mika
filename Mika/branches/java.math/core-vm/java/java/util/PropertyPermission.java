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
