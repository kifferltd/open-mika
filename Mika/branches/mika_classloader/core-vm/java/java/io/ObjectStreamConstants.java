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
** $Id: ObjectStreamConstants.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

public interface ObjectStreamConstants {

  public final static short STREAM_MAGIC = (short)0xaced;
  public final static short STREAM_VERSION = 5;

  public final static int baseWireHandle = 0x7e0000;
  public final static byte TC_ARRAY = (byte)0x75;
  public final static byte TC_BASE = (byte)0x70;
  public final static byte TC_BLOCKDATA = (byte)0x77;
  public final static byte TC_BLOCKDATALONG = (byte)0x7a;
  public final static byte TC_CLASS = (byte)0x76;
  public final static byte TC_CLASSDESC = (byte)0x72;
  public final static byte TC_ENDBLOCKDATA = (byte)0x78;
  public final static byte TC_EXCEPTION = (byte)0x7b;

  public final static byte TC_LONGSTRING = (byte)0x7c; //added in 1.3
  public final static byte TC_PROXYCLASSDESC = (byte)0x7d; //added in  1.3

  public final static byte TC_MAX = (byte)0x7d; //changed since 1.3
  public final static byte TC_NULL = (byte)0x70;
  public final static byte TC_OBJECT = (byte)0x73;
  public final static byte TC_REFERENCE = (byte)0x71;
  public final static byte TC_RESET = (byte)0x79;
  public final static byte TC_STRING = (byte)0x74;

  public final static byte SC_WRITE_METHOD = 0x01;
  public final static byte SC_BLOCK_DATA = 0x08;
  public final static byte SC_SERIALIZABLE = 0x02;
  public final static byte SC_EXTERNALIZABLE = 0x04;

  public final static int PROTOCOL_VERSION_1 = 1;
  public final static int PROTOCOL_VERSION_2 = 2;

  public static final SerializablePermission SUBCLASS_IMPLEMENTATION_PERMISSION=
    new SerializablePermission("enableSubclassImplementation");
  public static final SerializablePermission SUBSTITUTION_PERMISSION=
    new SerializablePermission("enableSubstitution");
}
