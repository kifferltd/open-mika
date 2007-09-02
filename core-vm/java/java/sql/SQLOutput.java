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
** $Id: SQLOutput.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.sql;

import java.io.*;
import java.math.*;

public interface SQLOutput {

  public void writeArray(Array x) throws SQLException;
  public void writeAsciiStream(InputStream x) throws SQLException;
  public void writeBigDecimal(BigDecimal x) throws SQLException;
  public void writeBinaryStream(InputStream x) throws SQLException;
  public void writeBlob(Blob x) throws SQLException;
  public void writeBoolean(boolean x) throws SQLException;
  public void writeByte(byte x) throws SQLException;
  public void writeBytes(byte[] x) throws SQLException;
  public void writeCharacterStream(Reader x) throws SQLException;
  public void writeClob(Clob x) throws SQLException;
  public void writeDate(Date x) throws SQLException;
  public void writeDouble(double x) throws SQLException;
  public void writeFloat(float x) throws SQLException;
  public void writeInt(int x) throws SQLException;
  public void writeLong(long x) throws SQLException;
  public void writeObject(SQLData x) throws SQLException;
  public void writeRef(Ref x) throws SQLException;
  public void writeShort(short x) throws SQLException;
  public void writeString(String x) throws SQLException;
  public void writeStruct(Struct x) throws SQLException;
  public void writeTime(Time x) throws SQLException;
  public void writeTimestamp(Timestamp x) throws SQLException;

}

