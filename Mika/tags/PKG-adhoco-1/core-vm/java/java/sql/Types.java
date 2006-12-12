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
** $Id: Types.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.sql;

public class Types {

  public static final int NULL = 0;
  public static final int CHAR = 1;
  public static final int NUMERIC = 2;
  public static final int DECIMAL = 3;
  public static final int INTEGER = 4;
  public static final int SMALLINT = 5;
  public static final int FLOAT = 6;
  public static final int REAL = 7;
  public static final int DOUBLE = 8;
  public static final int VARCHAR = 12;
  
  public static final int LONGVARCHAR = -1;
  public static final int BINARY = -2;
  public static final int VARBINARY = -3;
  public static final int LONGVARBINARY = -4;
  public static final int BIGINT = -5;
  public static final int TINYINT = -6;
  public static final int BIT = -7;
  
  public static final int DATE = 91;
  public static final int TIME = 92;
  public static final int TIMESTAMP = 93;
  
  public static final int OTHER = 1111;

  public static final int JAVA_OBJECT = 2000;
  public static final int DISTINCT = 2001;
  public static final int STRUCT = 2002;
  public static final int ARRAY = 2003;
  public static final int BLOB = 2004;
  public static final int CLOB = 2005;
  public static final int REF = 2006;
  
}

