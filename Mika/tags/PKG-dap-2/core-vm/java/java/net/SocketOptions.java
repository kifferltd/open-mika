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



package java.net;

public interface SocketOptions {
// Provisional values
  public static final int TCP_NODELAY = 0x0001;
  public static final int SO_BINDADDR = 0x000F;
  public static final int SO_REUSEADDR = 0x04;
  public static final int IP_MULTICAST_IF = 0x10;
  public static final int SO_LINGER = 0x0080;
  public static final int SO_TIMEOUT = 0x1006;
  public static final int SO_SNDBUF =0x1001;
  public static final int SO_RCVBUF = 0x1002;
  public static final int SO_KEEPALIVE = 0x0008;

  public void setOption(int optID, Object value) throws SocketException;
  public Object getOption(int optID)  throws SocketException;
}

