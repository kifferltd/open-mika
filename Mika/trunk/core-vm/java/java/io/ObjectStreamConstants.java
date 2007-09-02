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
