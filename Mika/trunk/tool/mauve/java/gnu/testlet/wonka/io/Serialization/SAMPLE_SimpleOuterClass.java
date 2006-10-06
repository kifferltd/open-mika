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

public class SAMPLE_SimpleOuterClass implements java.io.Serializable {
  int              outerInt    = 0xcafe;
  PublicInnerClass publicInner = new PublicInnerClass();
  OtherInnerClass  otherInner  = new OtherInnerClass();

  public class PublicInnerClass implements java.io.Serializable {
    private static final long serialVersionUID = -325976680086387867L;

    int innerInt = 0xbabe;

    public boolean equals (Object object) {
      PublicInnerClass other = (PublicInnerClass)object;
      return this.innerInt == other.innerInt;
    }
  }

  public static class StaticInnerClass implements java.io.Serializable {
    String innerString = "owhataday";
  }

  public class OtherInnerClass extends SM_Simple {
    private static final long serialVersionUID = 3852425859246527311L;
    int otherInnerInt = 0xabe;

    public boolean equals (Object object) {
      OtherInnerClass other = (OtherInnerClass)object;
      System.out.println(this.otherInnerInt == other.otherInnerInt);
      return this.otherInnerInt == other.otherInnerInt;
    }

  }

  public boolean equals (Object object) {
    SAMPLE_SimpleOuterClass other = (SAMPLE_SimpleOuterClass)object;
    boolean result = (this.outerInt == other.outerInt)
                  && (this.publicInner.equals(other.publicInner))
                  && (this.otherInner.equals(other.otherInner));

    System.out.println("SAMPLE_SimpleOuterClass.equals: " + result);
    return result;
  }
}