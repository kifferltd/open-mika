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

