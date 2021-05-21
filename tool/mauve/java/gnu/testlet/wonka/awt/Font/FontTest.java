/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
*                                                                         *
* Modified Chris Gray 2018.
*
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


// Author: J. Vandeneede
// Created: 2001/01/08

package gnu.testlet.wonka.awt.Font;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.Properties;
import java.awt.*;

public class FontTest implements Testlet
  {
  TestHarness harness;

  /**
   * tests the Font static member attributes Font.PLAIN, Font.BOLD and Font.ITALIC.
   */
  void testStaticConsts()
    {
    //constructors, equals() method and public constants
    harness.checkPoint("PLAIN(public)int");

    harness.check(Font.PLAIN  == 0x00000000, "Font.PLAIN");
    harness.checkPoint("BOLD(public)int");
    harness.check(Font.BOLD   == 0x00000001, "Font.BOLD");
    harness.checkPoint("ITALIC(public)int");
    harness.check(Font.ITALIC == 0x00000002, "Font.ITALIC");
    }

  /**
   * tests the Font constructor Font(String, int, int), also checks the initialisation
   * by calling on Font.equals();
   */
  void testConstructors()
    {
    //constructors and equals() method.
    harness.checkPoint("Font(java.lang.String,int,int)");

/*
    // legal Linux font names: 'Dialog', 'Serif', 'SansSerif', Monospaced',
    //                         'DialogInput', 'Symbol';

    Font fnt1 = new Font("Dialog", Font.PLAIN, 12);
    Font fnt2 = new Font("Dialog", Font.PLAIN, 12);
    Font fnt3 = new Font("Serif", Font.PLAIN, 12);
    Font fnt4 = new Font("Dialog", Font.BOLD, 12);
    Font fnt5 = new Font("Dialog", Font.PLAIN, 15);
*/
    // legal wonka font names: 'helvR08', 'courR10', 'courR14', courR17',
    //                         'courR20';

    Font fnt1 = new Font("helvR14", Font.PLAIN, 18);
    Font fnt2 = new Font("helvR14", Font.PLAIN, 18);
    Font fnt3 = new Font("courR14", Font.PLAIN, 14);
    Font fnt4 = new Font("helvB14", Font.BOLD, 18);
    Font fnt5 = new Font("courR21", Font.PLAIN, 21);
    Font fnt6 = new Font("any", Font.PLAIN, 19);
    harness.check(fnt1 != null, "Font(java.awt.String, int, int)");
    harness.check(!fnt1.equals(null), "Font(java.awt.String, int, int)");

    harness.checkPoint("equals(java.lang.Object)boolean");
    harness.check(fnt1.equals(fnt2), "equals(java.awt.Font)boolean");     //comparing objects
    harness.check(!fnt1.equals(fnt3), "equals(java.awt.Font)boolean");
    harness.check(!fnt1.equals(fnt4), "equals(java.awt.Font)boolean");
    harness.check(!fnt1.equals(fnt5), "equals(java.awt.Font)boolean");
    harness.check(fnt6.equals(new Font("any", Font.PLAIN, 18)), "Font(java.awt.String, int, int)");
    harness.check((new Font(null, Font.PLAIN, 17)).equals(new Font("Default", Font.PLAIN,17)), "Font(java.awt.String, int, int)");
    }

  /**
   * tests initialisation of protected attributes via subclassing
   * (through the use of the FontTestHelper class).
   */
  void testProtectedAttributes()
    {
    harness.checkPoint("Protected attributes");

    FontTestHelper fnt = new FontTestHelper();
    harness.check(fnt.testIt());
    }

  /**
   * tests the Font access methods getName(), getStyle(), getSize(), getFamily().
   */
  void testAccessMethods()
    {
    //  remark: use 'equals' to compare strings; '==' compares references!

    // existing font:
    // if specified font exists, font name is specified name
    // and font family is font name with all lowercase letters
    // un-existing font:
    // if specified font does not exist, font name is specified name and
    // family is name of a system font (not all lowercase letters)

/* // Linux tests
    String fn1 = new String("SansSerif");
    Font  fnt1 = new Font(fn1, Font.PLAIN, 12);

    harness.check(fnt1.getName().equals(fn1));
    harness.check(fnt1.getFamily().equals("sansserif"));
    harness.check(fnt1.getFamily().equalsIgnoreCase(fn1));
    harness.check(fnt1.getSize()==12);
    harness.check(fnt1.getStyle()==Font.PLAIN);

    String fn2 = new String("BullShit");
    Font  fnt2 = new Font(fn2, Font.PLAIN, 12);

    harness.check(fnt2.getName().equals(fn2));
    harness.check(!fnt2.getFamily().equals("bullshit"));
    harness.check(!fnt2.getFamily().equalsIgnoreCase(fn2));
*/ // end linux tests
// wonka tests
    String fn1 = new String("courR10");
    Font  fnt1 = new Font(fn1, Font.PLAIN, 10);
    String fn2 = new String("BullShit");
    Font  fnt2 = new Font(fn2, Font.PLAIN, 15);

    harness.checkPoint("getName()java.lang.String");
    harness.check(fnt1.getName().equals(fn1), "getName()java.lang.String");
    harness.check(fnt2.getName().equals(fn2), "getName()java.lang.String");
    harness.checkPoint("getFamily()java.lang.String");
    harness.check(fnt1.getFamily().equals("Courier"), "getFamily()java.lang.String");
    harness.check(fnt2.getFamily().equals("Helvetica"), "getFamily()java.lang.String");
    harness.checkPoint("getSize()int");
    harness.check(fnt1.getSize()==10, "getSize()int");
    harness.check(fnt2.getSize()==14, "getSize()int");
    harness.checkPoint("getStyle()int");
    harness.check(fnt1.getStyle()==Font.PLAIN, "getStyle()int");
    harness.check(fnt2.getStyle()==Font.PLAIN, "getStyle()int");

// end wonka tests
    }

  /**
   * tests the other Font access methods isPlain(), isBold(), isItalic().
   */
  void testStyleComparison()
    {
    harness.checkPoint("isPlain()boolean");

    String fn1 = new String("SansSerif");
    Font  fnt1 = new Font(fn1, Font.PLAIN, 12);

    harness.check(fnt1.isPlain(), "isPlain()boolean");
    harness.checkPoint("isBold()boolean");
    harness.check(!fnt1.isBold(), "isBold()boolean");
    harness.checkPoint("isItalic()boolean");
    harness.check(!fnt1.isItalic(), "isItalic()boolean");
    }

  /**
   * tests the decode() method.
   */
  void testDecode()
    {
    harness.checkPoint("decode(java.lang.String)java.awt.Font");

/* // linux tests
    Font fnt1 = Font.decode("Serif-plain-12");
    harness.check(fnt1.equals(new Font("Serif", Font.PLAIN, 12)));
    Font fnt2 = Font.decode("Dialog-plain-12");
    harness.check(!fnt2.equals(new Font("Serif", Font.PLAIN, 12)));
    Font fnt3 = Font.decode("Serif-bold-12");
    harness.check(!fnt3.equals(new Font("Serif", Font.PLAIN, 12)));
    Font fnt4 = Font.decode("Serif-plain-15");
    harness.check(!fnt4.equals(new Font("Serif", Font.PLAIN, 12)));
*/ // end linux tests
// wonka tests
    Font fnt1 = Font.decode("courR10-plain-10");
    harness.check(fnt1.equals(new Font("courR10", Font.PLAIN, 10)), "decode(java.lang.String)java.awt.Font");
    Font fnt2 = Font.decode("helvR08-plain-10");
    harness.check(fnt2.equals(new Font("helvR08", Font.PLAIN, 8)), "decode(java.lang.String)java.awt.Font");
    Font fnt3 = Font.decode("courR10-bold-10");
    harness.check(fnt3.equals(new Font("courR10", Font.BOLD, 10)), "decode(java.lang.String)java.awt.Font");
    Font fnt4 = Font.decode("courR10-plain-14");
    harness.check(fnt4.equals(new Font("courR10", Font.PLAIN, 10)), "decode(java.lang.String)java.awt.Font");
    Font fnt5 = Font.decode("helvR19-plain-19"); // non-existing name; assign helvR14
    harness.check(fnt5.equals(new Font("helvR19", Font.PLAIN, 18)), "decode(java.lang.String)java.awt.Font");
    Font fnt6 = Font.decode("helvR08");
    harness.check(fnt6.equals(new Font("helvR08", Font.PLAIN, 8)), "decode(java.lang.String)java.awt.Font");
    String s=null;
    Font fnt7 = Font.decode(s);
    harness.check(fnt7.equals(new Font("dialog", Font.PLAIN, 12)), "decode(java.lang.String)java.awt.Font");
    Font fnt8 = Font.decode("");
    harness.check(fnt8.equals(new Font("", Font.PLAIN, 12)), "decode(java.lang.String)java.awt.Font");
    Font fnt9 = Font.decode("Roman");
    harness.check(fnt9.equals(new Font("Roman", Font.PLAIN, 12)), "decode(java.lang.String)java.awt.Font");
    Font fnt10 = Font.decode("Roman-plain-18");
    harness.check(fnt10.equals(new Font("Roman", Font.PLAIN, 18)), "decode(java.lang.String)java.awt.Font");
    Font fnt11 = Font.decode("courR10-underline-10");
    harness.check(fnt11.equals(new Font("courR10", Font.PLAIN, 10)), "decode(java.lang.String)java.awt.Font");
    Font fnt12 = Font.decode("-");
    harness.check(fnt12.equals(new Font("", Font.PLAIN, 12)), "decode(java.lang.String)java.awt.Font");
    Font fnt13 = Font.decode("--");
    harness.check(fnt13.equals(new Font("", Font.PLAIN, 12)), "decode(java.lang.String)java.awt.Font");
    Font fnt14 = Font.decode("---");
    harness.check(fnt14.equals(new Font("", Font.PLAIN, 12)), "decode(java.lang.String)java.awt.Font");
    Font fnt15 = Font.decode("--xxx");
    harness.check(fnt15.equals(new Font("", Font.PLAIN, 12)), "decode(java.lang.String)java.awt.Font");
    Font fnt16 = Font.decode("-yyyy-");
    harness.check(fnt16.equals(new Font("", Font.PLAIN, 12)), "decode(java.lang.String)java.awt.Font");
// end wonka tests
    }

  /**
   * tests the getFont() method.
   */
  void testGetFont()
    {

    harness.checkPoint("getFont(java.lang.String,java.awt.Font)java.awt.Font");

    //(System.getProperties()).list(System.out);

/* // linux tests
    // work around for method 'System.setProperty', not yet implemented in wonka:
    // System.setProperty("myFont", "Serif-plain-15");
    Properties p = System.getProperties();
    p.put("myFont", "Serif-plain-15");
    // end work around
    Font fnt1 = Font.getFont("myFont",new Font("Dialog", Font.PLAIN, 12));
    harness.check(fnt1.equals(new Font("Serif",Font.PLAIN,15)));

    Font fnt2 = Font.getFont("yourFont",new Font("Dialog", Font.PLAIN, 12));
    harness.check(fnt2.equals(new Font("Dialog",Font.PLAIN,12)));

    Font fnt3 = Font.getFont("yourFont");
    harness.check(fnt3==null);
*/ // end linux tests
// wonka tests
    // work around for method 'System.setProperty', not yet implemented in wonka:
    // System.setProperty("myFont", "courR14-plain-14");
    Properties p = System.getProperties();
    p.put("myFont", "courR14-plain-14");
    // end work around
    Font fnt1 = Font.getFont("myFont",new Font("courR10", Font.PLAIN, 10));
    harness.check(fnt1.equals(new Font("courR14",Font.PLAIN,14)), "getFont(java.lang.String, java.awt.Font)java.awt.Font");

    Font fnt2 = Font.getFont("yourFont",new Font("courR10", Font.PLAIN, 10));
    harness.check(fnt2.equals(new Font("courR10",Font.PLAIN,10)), "getFont(java.lang.String, java.awt.Font)java.awt.Font");

    harness.checkPoint("getFont(java.lang.String)java.awt.Font");
    Font fnt3 = Font.getFont("myFont");
    harness.check(fnt3.equals(new Font("courR14",Font.PLAIN,14)), "getFont(java.lang.String)java.awt.Font");

    Font fnt4 = Font.getFont("yourFont");
    harness.check(fnt4==null, "getFont(java.lang.String)java.awt.Font");
// end wonka tests
    }

  /**
   * tests general method toString() and hashCode()
   */
  void testToString()
    {
    harness.checkPoint("toString()java.lang.String");

/* // linux tests
    harness.check((new Font("DialogInput", Font.PLAIN, 12)).toString().equals("java.awt.Font[family=dialoginput,name=DialogInput,style=plain,size=12]"));
    harness.check((new Font("DialogInput", Font.BOLD, 12)).toString().equals("java.awt.Font[family=dialoginput.bold,name=DialogInput,style=bold,size=12]"));
*/ // end linux tests
// wonka tests

//    harness.debug((new Font("helvR08",0,0)).toString());
//    harness.debug((new Font("helvR14",0,0)).toString());
//    harness.debug((new Font("helvR17",0,0)).toString());
//    harness.debug((new Font("helvR20",0,0)).toString());
//    harness.debug((new Font("helvR25",0,0)).toString());
//    harness.debug((new Font("helvB14",1,0)).toString());
//    harness.debug((new Font("helvB17",1,0)).toString());
//    harness.debug((new Font("helvB20",1,0)).toString());
//    harness.debug((new Font("helvB25",1,0)).toString());
//    harness.debug((new Font("courR10",0,0)).toString());
//    harness.debug((new Font("courR14",0,0)).toString());
//    harness.debug((new Font("courR17",0,0)).toString());
//    harness.debug((new Font("courR20",0,0)).toString());
//    harness.debug((new Font("courR25",0,0)).toString());
//    harness.debug((new Font("courB14",1,0)).toString());
//    harness.debug((new Font("courB17",1,0)).toString());
//    harness.debug((new Font("courB20",1,0)).toString());
//    harness.debug((new Font("courB25",1,0)).toString());

    harness.check((new Font("courR10", Font.PLAIN, 10)).toString().equals("java.awt.Font[family = Courier, name = courR10, style = plain, size = 10]"), "toString()java.lang.String");
    harness.check((new Font("BullShit", Font.PLAIN, 10)).toString().equals("java.awt.Font[family = Courier, name = BullShit, style = plain, size = 10]"), "toString()java.lang.String");
    harness.check((new Font("helvB14", Font.BOLD, 18)).toString().equals("java.awt.Font[family = Helvetica, name = helvB14, style = bold, size = 14]"), "toString()java.lang.String");
//    harness.check((new Font("helvI14", Font.ITALIC, 18)).toString().equals("java.awt.Font[family = Helvetica, name = helvI14, style = italic, size = 18]"), "toString()java.lang.String");
    harness.check((new Font("helvR08", 0, 8)).toString().equals("java.awt.Font[family = Helvetica, name = helvR08, style = plain, size = 8]"), "toString()java.lang.String");
// end wonka tests
    }

  /**
   * tests general method hashCode() and hasCode()
   * CG : fixed this code to (1) use fonts that are really likely to be
   * present, and (2) not make unwarranted assumptions about how the hash
   * code is calculated.
   */
  void testHashCode() {
    harness.checkPoint("hashCode()int");

    Font fnt1 = new Font("Courier", Font.PLAIN, 12);
    int h1=fnt1.hashCode();
    Font fnt2 = new Font("Helvetica", Font.PLAIN, 12);
    int h2=fnt2.hashCode();
    if (fnt1.equals(fnt2)) {
      System.err.println("Warning : " + getClass() + ".testHashCode() : both font constructors returned the same font!");
      System.err.println("          Using new Font(\"Courier\", Font.PLAIN, 12) and new Font(\"Courier\", Font.PLAIN, 12)");
      System.err.println("          Got " + fnt1 + " and " + fnt2);
      harness.check(h1, h2, "hashCode()int");
    }
    else {
      /* CG just check that different fonts give different hash codes */
      harness.check(h1 != h2, "hashCode()int");
    }
  }

  /**
   * tests getPeer() : peers are not implemented in rudolph
   */
/*
  void getPeer()
    {
    harness.checkPoint("Method getPeer()");
    }
*/

  public void test (TestHarness newharness)
    {
    harness = newharness;
    harness.setclass("java.awt.Font");
    testStaticConsts();
    testConstructors();
    testProtectedAttributes();
    testAccessMethods();
    testStyleComparison();
    testDecode();
    testGetFont();
    testToString();
    testHashCode();
    }
  }
