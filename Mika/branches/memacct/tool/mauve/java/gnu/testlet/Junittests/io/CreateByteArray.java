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

import java.io.*;

public class CreateByteArray {
  private String [] arrayString={"secondtest","secondteststring","......"};
  private int [] arrayInt={21,22,23,24,25,26,27,28,29,20,Integer.MAX_VALUE,Integer.MIN_VALUE};

  public CreateByteArray() throws Exception{
    ByteArrayOutputStream baos=new ByteArrayOutputStream();
    ObjectOutputStream oos=new ObjectOutputStream(baos);


    oos.writeObject(SAMPLE_SerialObject.getRootObject());


    byte [] byteArray = baos.toByteArray();
    System.out.println("Length: "+byteArray.length);
    for (int i=0; i<byteArray.length;i++) {
      System.out.print("0x"+Integer.toHexString(byteArray[i]&255)+", ");
      if ((i+1)%16==0) {
       System.out.println();
      }
    }

    baos.close();

    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);

    oos.writeObject(SAMPLE_SerialObject.getOuterClass());

    byteArray = baos.toByteArray();
    System.out.println("\n\n\nLength: "+byteArray.length);
    for (int i=0; i<byteArray.length;i++) {
      System.out.print("0x"+Integer.toHexString(byteArray[i]&255)+", ");
      if ((i+1)%16==0) {
        System.out.println();
      }
    }
    baos.close();
    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);

    oos.writeObject(SAMPLE_SerialObject.getRootObject());

    byteArray = baos.toByteArray();
    System.out.println("\n\n\nLength: "+byteArray.length);
    for (int i=0; i<byteArray.length;i++) {
      System.out.print("0x"+Integer.toHexString(byteArray[i]&255)+", ");
      if ((i+1)%16==0) {
        System.out.println();
      }
    }
    baos.close();
  }



  public static void main(String [] arg) throws Exception {
    new CreateByteArray();
    System.out.println("CreateByteArray.main()"+SAMPLE_SerialObject.icDataInt.length);
    System.out.println("CreateByteArray.main()"+SAMPLE_SerialObject.dataInt.length);
  }


}
