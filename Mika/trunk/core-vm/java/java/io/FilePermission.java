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
 * $Id: FilePermission.java,v 1.2 2006/02/23 12:32:12 cvs Exp $
 */

package java.io;

import java.security.Permission;
import java.security.PermissionCollection;

public final class FilePermission extends Permission {

  private static final long serialVersionUID = 7930732926638008763L;

  private static final String CURRENTDIR = new File("").getAbsolutePath();

  private String actions;

  private boolean read;
  private boolean write;
  private boolean execute;
  private boolean delete;

  static final String SEP = File.separator;
  static final String SEP_STAR = SEP + "*";
  static final String SEP_DASH = SEP + "-";
  private static final int SEPLENGTH = SEP.length();

  private static final char  SEP_CHAR = SEP.charAt(0);

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
      else if (s0.equals("write")) {
        write = true;
      } 
      else if (s0.equals("execute")) {
        execute = true;
      } 
      else if (s0.equals("delete")) {
        delete = true;
      }
      else { throw new IllegalArgumentException(s0+" is no valid action"); }
    }
    StringBuffer buf = new StringBuffer(26);
    if(read) {
     	buf.append("read,");
    }
    if(write) {
     	buf.append("write,");
    }
    if(execute) {
     	buf.append("execute,");
    }
    if(delete) {
     	buf.append("delete,");
    }
    if(!(execute || delete || read || write)) {
	throw new IllegalArgumentException("no actions specified");
    }
    else {
     	actions = buf.substring(0,buf.length()-1);   //-1 to lose the trailing ','
    }
 }

  private static String toAbsolutePath(String path){
    if(path.startsWith(SEP)){
      return path;
    }
    if(path.startsWith(".")){
      String newPath = CURRENTDIR;
      int dotCount = 1;
      int i = 1;
      int path_length = path.length();
      for(; i < path_length ; i++){
        char ch = path.charAt(i);
        if(ch != '.'){
          if(path.indexOf(SEP,i) != i){
            break;
          }
          if(dotCount == 2){
            int slash = newPath.lastIndexOf(SEP,newPath.length());
            if(slash >= SEPLENGTH){
              newPath = newPath.substring(0,slash+SEPLENGTH);
            }
          }
          i += SEPLENGTH-1;
        }
        else{
          if(dotCount > 1){
            i -= dotCount;
            break;
          }
          dotCount++;
        }
      }
      if(newPath == CURRENTDIR){
        return path.substring(i);
      }
      return newPath+path.substring(i);
    }
    return path;
  }

  public FilePermission(String path, String actions) {
  // Note: if we were to support relative pathnames, we would do so
  // by prefixing the current directory right here.
    super(toAbsolutePath(path));
    parseActions(actions);
  }

  public boolean implies (Permission p) {
      if (!( p instanceof FilePermission)) {
        return false;
      }
      FilePermission fp = (FilePermission)p;
      if ((fp.read && !this.read) || (fp.write && !this.write) || (fp.execute && !this.execute) || (fp.delete && !this.delete)) {
        return false;
       }

      String thisname = super.getName();
      String othername = p.getName();

      if (thisname.equals("<<ALL FILES>>")) {
         return true;
      }

      if (thisname.endsWith(SEP_DASH)) {
        int l = thisname.length()-1;
        boolean b = othername.startsWith(thisname.substring(0,l));// this still includes the path itself
        b &= (othername.length() != l);
        return b;
      }
      else if (thisname.endsWith(SEP_STAR)) {
        int i = thisname.lastIndexOf(SEP_CHAR)+1;
        int pos = othername.indexOf(SEP_CHAR,i+1);
        int l = othername.length();
        return othername.startsWith(thisname.substring(0,i)) //the name should startwith path/
            && (pos < 0 || pos == l-1)  // but it may not contain a / unless it is the last one
            && ( l != i); // thtis case means the othername is path/ (is not included in path/*)

      }
      else {
        return othername.equals(thisname);
      }
  }

  public boolean equals (Object o) {
      if (!(o instanceof FilePermission)) {
	return false;
      }
      FilePermission fp = (FilePermission) o;
      if ((fp.read != this.read) || (fp.write != this.write) || (fp.execute != this.execute) || (fp.delete != this.delete)) {
         return false;
      }
      return fp.getName().equals(super.getName());
  }

  public int hashCode() {
    int h = super.getName().hashCode();

    if (this.read) {
      h ^= 0x80402010;
    }
    if (this.write) {
      h ^= 0x20408010;
    }
    if (this.execute) {
      h ^= 0x80102040;
    }
    if (this.delete) {
      h ^= 0x08040201;
    }
    return h;
  }

  public String getActions() {
    return actions;
  }

  public PermissionCollection newPermissionCollection() {
    return new wonka.security.FilePermissionCollection();
  }
}
