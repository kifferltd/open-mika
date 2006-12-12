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
** $Id: Array.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

import java.util.*;

public interface Array {

  public Object getArray() throws SQLException;
  public Object getArray(long index, int count) throws SQLException;
  public Object getArray(long index, int count, Map map) throws SQLException;
  public Object getArray(Map map) throws SQLException;
  public int getBaseType() throws SQLException;
  public String getBaseTypeName() throws SQLException;
  public ResultSet getResultSet() throws SQLException;
  public ResultSet getResultSet(long index, int count) throws SQLException;
  public ResultSet getResultSet(long index, int count, Map map) throws SQLException;
  public ResultSet getResultSet(Map map) throws SQLException;
  
}

