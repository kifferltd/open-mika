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
  	if (isNative(mod)) buf.append("native ");
  	if (isSynchronized(mod)) buf.append("synchronized ");
  	if (isInterface(mod)) buf.append("interface ");
  	if (isStrict(mod)) buf.append("strict ");
  	if (buf.length()>0) buf.setLength(buf.length()-1);
  	return buf.toString();
  }	
}
