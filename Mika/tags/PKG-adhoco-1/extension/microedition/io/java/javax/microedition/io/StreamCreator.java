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


package javax.microedition.io;

import java.io.*;

/**
** the StreamCreator class is used to provide an API for Connections implemtentations.
** insteadof creating a InputConnection, OutputConnection and StreamConnection for each protocol
** we make 3 general classes and give the a StreamCreator todo the work.  This means we only create one class
** for each protocol ...
*/
abstract class StreamCreator {

  protected boolean closed;

  StreamCreator() {}

  public void close(){
    closed = true;
  }

  public final DataInputStream openDataInputStream() throws IOException {
    return new DataInputStream(openInputStream());
  }

  public final DataOutputStream openDataOutputStream() throws IOException {
    return new DataOutputStream(openOutputStream());
  }

  public final InputStream openInputStream() throws IOException {
    if(closed){
      throw new IOException("connection is closed");
    }
    return getInputStream();
  }

  public final OutputStream openOutputStream() throws IOException {
    if(closed){
      throw new IOException("connection is closed");
    }
    return getOutputStream();
  }

  public String getEncoding(){
    return null;
  }

  public long getLength(){
    return -1;
  }

  public String getType(){
    return null;
  }

  protected abstract InputStream getInputStream() throws IOException;
  protected abstract OutputStream getOutputStream() throws IOException;

}