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

import java.io.IOException;

class BasicInputConnection implements InputConnection {

  StreamCreator creator;

  BasicInputConnection(StreamCreator creator){
    this.creator = creator;
  }

  public void close() throws IOException {
    creator.close();
  }

  public java.io.DataInputStream openDataInputStream() throws IOException {
    return creator.openDataInputStream();
  }

  public java.io.InputStream openInputStream() throws IOException {
    return creator.openInputStream();
  }

  public String getEncoding(){
    return creator.getEncoding();
  }

  public long getLength(){
    return creator.getLength();
  }

  public String getType(){
    return creator.getType();
  }
}