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
** $Id: Modifier.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.lang.reflect;

public class Modifier {

  final static int SUPER = 0x00000020;

  public final static int PUBLIC       = 0x00000001;
  public final static int PRIVATE      = 0x00000002;
  public final static int PROTECTED    = 0x00000004;
  public final static int STATIC       = 0x00000008;
  public final static int FINAL        = 0x00000010;
  public final static int SYNCHRONIZED = 0x00000020;
  public final static int VOLATILE     = 0x00000040;
  public final static int TRANSIENT    = 0x00000080;
  public final static int NATIVE       = 0x00000100;
  public final static int INTERFACE    = 0x00000200;
  public final static int ABSTRACT     = 0x00000400;
  public final static int STRICT       = 0x00000800;

  public static boolean isPublic(int mod) {
    return ((mod & PUBLIC) != 0 ? true : false);
  }

  public static boolean isPrivate(int mod) {
    return ((mod & PRIVATE) != 0 ? true : false);
  }

  public static boolean isProtected(int mod) {
    return ((mod & PROTECTED) != 0 ? true : false);
  }

  public static boolean isStatic(int mod) {
    return ((mod & STATIC) != 0 ? true : false);
  }

  public static boolean isFinal(int mod) {
    return ((mod & FINAL) != 0 ? true : false);
  }

  public static boolean isSynchronized(int mod) {
    return ((mod & SYNCHRONIZED) != 0 ? true : false);
  }

  public static boolean isVolatile(int mod) {
    return ((mod & VOLATILE) != 0 ? true : false);
  }

  public static boolean isTransient(int mod) {
    return ((mod & TRANSIENT) != 0 ? true : false);
  }

  public static boolean isAbstract(int mod) {
    return ((mod & ABSTRACT) != 0 ? true : false);
  }
  public static boolean isStrict(int mod) {
    return ((mod & STRICT) != 0 ? true : false);
  }
  public static boolean isNative(int mod) {
    return ((mod & NATIVE) != 0 ? true : false);
  }
  public static boolean isInterface(int mod) {
    return ((mod & INTERFACE) != 0 ? true : false);
  }
  public static String toString(int mod) {
//	"public protected private abstract static final transient"+
//	" volatile native synchronized interface strict"
  	StringBuffer buf = new StringBuffer();
  	if (isPublic(mod)) buf.append("public ");
  	if (isProtected(mod)) buf.append("protected ");
  	if (isPrivate(mod)) buf.append("private ");
  	if (isAbstract(mod)) buf.append("abstract ");
  	if (isStatic(mod)) buf.append("static ");
  	if (isFinal(mod)) buf.append("final ");
  	if (isTransient(mod)) buf.append("transient ");
  	if (isVolatile(mod)) buf.append("volatile ");
  	if (isSynchronized(mod)) buf.append("synchronized ");
    if (isNative(mod)) buf.append("native ");
  	if (isStrict(mod)) buf.append("strictfp ");
    if (isInterface(mod)) buf.append("interface ");
  	if (buf.length()>0) buf.setLength(buf.length()-1);
  	return buf.toString();
  }	
}
