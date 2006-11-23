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


package gnu.testlet.Junittests.io;

//import java.io.*;


import gnu.testlet.*;
import java.io.*;



public class TEST_ObjectOutputStream_Exceptions extends Mv_Assert {
  private ByteArrayOutputStream baos;
  private ObjectOutputStream oos;

  public TEST_ObjectOutputStream_Exceptions() {
    super();
  }

  public void setUp() {
    openOutputStream();
  }

  public void tearDown() {
    closeOutputStream();
  }


  private void closeOutputStream() {
    try {
      baos.close();
    } catch(IOException ioe) {
      fail(ioe.toString());
    }
  }



  private void openOutputStream() {
    try {
      baos = new ByteArrayOutputStream();
      oos = new ObjectOutputStream(baos);
    }catch(IOException ioe) {
      ioe.printStackTrace();
      fail("unexpected io exception occurred while setting up");
    }
  }


  protected void runTest() {
    try {
      SAMPLE_ObjectOutputStream_NotSerializable badObj = new SAMPLE_ObjectOutputStream_NotSerializable();
      oos.writeObject(badObj);
    }
    catch(NotSerializableException nse) {
      //everything OK
       return;
    }
    catch(IOException ioe) {
      fail(ioe.toString());
    }
    fail("NotSerializableException expected but not thrown");

  }






}

