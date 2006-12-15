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




public class SAMPLE_ObjectStream_OuterClass implements Serializable {

  private static final long serialVersionUID = 7789161491677047023L;
  public PrivateInnerClass pric;
  public PublicInnerClass puic;


  public SAMPLE_ObjectStream_OuterClass() {
    pric=new PrivateInnerClass();
    puic=new PublicInnerClass();

    puic.setD(new SAMPLE_ObjectStream_D("just a string") {
      /**
       * 
       */
      private static final long serialVersionUID = 8791293028313895333L;

      public String toString() {
        return "innerclassD: "+s;
      }
    });


    puic.setRight(pric);
    pric.setLeft(puic);

  }


  private class PrivateInnerClass extends SAMPLE_ObjectStream_A {
    private static final long serialVersionUID = 4741640009466855590L;

    public PrivateInnerClass() {
      super(5); // set prefix value=5 for superclass
      setD(new SAMPLE_ObjectStream_D("pricD"));
    }
  }


  public boolean isInstanceOfPrivateInnerClass(Object o) {
    return (o instanceof PrivateInnerClass);
  }


  public class PublicInnerClass extends SAMPLE_ObjectStream_B {
    private static final long serialVersionUID = 9179867260950039483L;
    String aString = "privateinnerclassstring";

    public PublicInnerClass() {
      super(6); // set prefix value=6 for superclass
    }

    public void setAString(String aString) {
      this.aString=aString;
    }

    public String getAString() {
      return aString;
    }

  }


  public static class StaticInnerClass {
    public static String aString = new String("this is a test");
    private static int aInt = 234;

    public static int getAInt() {
      return aInt;
    }
  }


}