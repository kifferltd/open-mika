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


package gnu.testlet.wonka.io.Serialization;

import java.io.*;
import java.util.Vector;

public class CreateByteArray implements Serializable {

  public static final int TESTVALUE;

  static {
    TESTVALUE = new Object().hashCode();
  }

  private static void printStream(byte [] s ,String message) {


    System.out.println(message+"--> Length of array: "+s.length);
    for (int i=0; i<s.length;i++) {
      System.out.print(Byte.toString(s[i])+", ");
      if ((i+1)%16==0) {
       System.out.println();
      }
    }
    System.out.println("\n\n\n");


  }

  private static void create() throws Exception{

    ByteArrayOutputStream baos=new ByteArrayOutputStream();
    ObjectOutputStream oos=new ObjectOutputStream(baos);

    oos.writeBoolean(false);
    oos.writeBoolean(true);
    oos.writeInt(10);
    oos.writeLong(16);
    oos.writeShort(5);
    oos.flush();
    printStream( baos.toByteArray(), "DATABLOCK 1" );
    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);
    oos.writeDouble(1.0);
    oos.writeFloat(-1.0f);
    oos.flush();
    printStream( baos.toByteArray(), "DATABLOCK 2" );
    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);
    oos.writeObject(new SM_Simple());
    oos.flush();
    printStream( baos.toByteArray(), "Simple" );
    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);
    oos.writeObject(new SM_ClassString());
    oos.flush();
    printStream( baos.toByteArray(), "ClassString" );
    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);
    oos.writeObject(new SM_ClassArray());
    oos.flush();
    printStream( baos.toByteArray(), "ClassArray");
    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);
    Vector v = new Vector();
    v.add("Hello world");
    v.add(new Integer(666));
    oos.writeObject(v);
    oos.flush();
    printStream( baos.toByteArray(), "Vector" );
    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);
    oos.writeObject(new StringBuffer("Hello"));
    oos.flush();
    printStream( baos.toByteArray(), "StringBuffer" );
    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);
    oos.writeObject(new ClassSubclass());
    oos.flush();
    printStream( baos.toByteArray(), "ClassSubClass" );
    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);
    oos.writeObject(new SAMPLE_SimpleOuterClass());
    oos.flush();
    printStream( baos.toByteArray(), "SAMPLE_SimpleOuterClass" );

    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);
    oos.writeObject(new SM_ClassUnderscore());
    oos.flush();
    printStream( baos.toByteArray(), "SM_ClassUnderscore" );

    byte[] blockdatabytes = new byte[1022];
    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);
    oos.write(blockdatabytes);
    oos.writeInt(0x01020304);
    oos.flush();
    printStream( baos.toByteArray(), "BLOCKDATALONG" );

    StringBuffer[] stringbufferArray = { new StringBuffer(), new StringBuffer() };
    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);
    oos.writeObject(stringbufferArray);
    oos.flush();
    printStream( baos.toByteArray(), "SBA" );
    baos=new ByteArrayOutputStream();
    oos=new ObjectOutputStream(baos);
    oos.writeObject(new CreateByteArray());
    oos.flush();
    printStream( baos.toByteArray(), "CreateByteArray" );

  }

  public static void main(String [] arg) throws Exception {
    CreateByteArray.create();
  }
}
