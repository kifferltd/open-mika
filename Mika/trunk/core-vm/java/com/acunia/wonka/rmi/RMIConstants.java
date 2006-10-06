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


package com.acunia.wonka.rmi;

public interface RMIConstants {

  public static final int CALL = 0x50;
  public static final int RETURN_DATA = 0x51;

  public static final int RETURN_VALUE = 0x01;
  public static final int EXCEPTION = 0x02;

  public static final int PING = 0x52;
  public static final int PING_ACK = 0x53;
  public static final int DGC_ACK = 0x54;

  public static final int PROTOCOL_ACK = 0x4e;
  public static final int PROTOCOL_NOT_SUPPORTED = 0x4f;

  public static final long HASH_DIRTY = -669196253586618813L;
  public static final int OPERATION_DIRTY = 1;
  public static final int OPERATION_CLEAN = 0;
}