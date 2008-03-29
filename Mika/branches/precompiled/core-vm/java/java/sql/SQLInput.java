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
** $Id: SQLInput.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

import java.io.*;
import java.math.*;

public interface SQLInput {

  public Array readArray() throws SQLException;
  public InputStream readAsciiStream() throws SQLException;
  public BigDecimal readBigDecimal() throws SQLException;
  public InputStream readBinaryStream() throws SQLException;
  public Blob readBlob() throws SQLException;
  public boolean readBoolean() throws SQLException;
  public byte readByte() throws SQLException;
  public byte[] readBytes() throws SQLException;
  public Reader readCharacterStream() throws SQLException;
  public Clob readClob() throws SQLException;
  public Date readDate() throws SQLException;
  public double readDouble() throws SQLException;
  public float readFloat() throws SQLException;
  public int readInt() throws SQLException;
  public long readLong() throws SQLException;
  public Object readObject() throws SQLException;
  public Ref readRef() throws SQLException;
  public short readShort() throws SQLException;
  public String readString() throws SQLException;
  public Time readTime() throws SQLException;
  public Timestamp readTimestamp() throws SQLException;
  public boolean wasNull() throws SQLException;
  
}

