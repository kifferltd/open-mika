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


package gnu.testlet.wonka.io.StreamTokenizer; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.StreamTokenizer  <br>
*/
public class SMStreamTokenizerTest implements Testlet
{
  protected TestHarness th;
  protected char buffer[] = (" -1.22.45-234.44 \"this is \n'a test \"'\u0256  \r \n   buffer\n a1N.D- CONTAINS sEcrEts/: don't\n tell them").toCharArray();

  public static final int TT_NUMBER = StreamTokenizer.TT_NUMBER;
  public static final int TT_WORD = StreamTokenizer.TT_WORD;


  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.StreamTokenizer");
       test_StreamTokenizer();
       test_nextToken();
     }


/**
*   not implemented. <br>
*
*/
  public void test_StreamTokenizer(){
    th.checkPoint("StreamTokenizer(java.io.Reader)");
    try {
     	new StreamTokenizer((Reader)null);
     	th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe) {
     	th.check(true , "caught exception ...");
    }

  }

/**
*   not implemented. <br>
*
*/
  public void test_nextToken(){
    th.checkPoint("nextToken()int");
    CharArrayReader car = new CharArrayReader(buffer);
    StreamTokenizer st = new StreamTokenizer(car);
//    st.parseNumbers();
//    st.debug();
    try {
    	th.check(st.nextToken(), TT_NUMBER, "checking return value ... -- 1");
    	th.check(st.nval , -1.22 , "parsing numbers -- 1");
    	th.check(st.nextToken(), TT_NUMBER, "checking return value ... -- 2");
    	th.check(st.nval , 0.45 , "parsing numbers -- 2");
    	th.check(st.nextToken(), TT_NUMBER, "checking return value ... -- 3");
    	th.check(st.nval , -234.44 , "parsing numbers -- 3");
    	th.check(st.nextToken(), (int)'"' ,"checking return value ... -- 4");
    	th.check(st.sval, "this is " , "parsing quote -- 1");
    	th.check(st.nextToken(), (int)'\'' ,"checking return value ... -- 5");
    	th.check(st.sval, "a test \"" , "parsing quote -- 2");
    	th.check(st.nextToken(), TT_WORD ,"checking return value ... -- 6");
    	th.check(st.sval, "\u0256" , "parsing words -- 1");   	
    	th.check(st.nextToken(), StreamTokenizer.TT_WORD ,"checking return value ... -- 7");
    	th.check(st.sval, "buffer" , "parsing words -- 1");
    	st.eolIsSignificant(true);
    	th.check(st.nextToken(), StreamTokenizer.TT_EOL ,"checking return value ... -- 8");
    	st.pushBack();
    	th.check(st.nextToken(), StreamTokenizer.TT_EOL ,"checking return value ... -- 9");
    	th.check(st.nextToken(), StreamTokenizer.TT_WORD ,"checking return value ... -- 10");
    	th.check(st.sval, "a1N.D-" , "parsing words -- 2");
    	st.lowerCaseMode(true);
    	th.check(st.nextToken(), StreamTokenizer.TT_WORD ,"checking return value ... -- 11");
    	th.check(st.sval, "contains" , "parsing words -- 3");
    	th.check(st.nextToken(), StreamTokenizer.TT_WORD ,"checking return value ... -- 12");
    	th.check(st.sval, "secrets" , "parsing words -- 4");
    	th.check(st.nextToken(), StreamTokenizer.TT_EOL ,"checking return value ... -- 13");
    	th.check(st.sval, null ,"checking sval ... -- 13");
    	th.check(st.nextToken(), StreamTokenizer.TT_WORD ,"checking return value ... -- 14");
    	th.check(st.sval, "tell" , "parsing words -- 5");
    	th.check(st.nextToken(), StreamTokenizer.TT_WORD ,"checking return value ... -- 15");
    	th.check(st.sval, "them" , "parsing words -- 6");
    	th.check(st.nextToken(), StreamTokenizer.TT_EOF ,"checking return value ... -- 16");
    	st.pushBack();
    	th.check(st.nextToken(), StreamTokenizer.TT_EOF ,"checking return value ... -- 17");
    	th.check(st.nextToken(), StreamTokenizer.TT_EOF ,"checking return value ... -- 18");
    	th.check(st.nextToken(), StreamTokenizer.TT_EOF ,"checking return value ... -- 19");
    }
    catch (IOException ioe){ th.fail("got unwanted IOException -- 1"); }
    car = new CharArrayReader(buffer);
    st = new StreamTokenizer(car);
//    st.parseNumbers();
    st.wordChars((int)'0', (int)'4');
    st.ordinaryChar((int)'/');
//    st.debug();
    buffer = "abcd //asdd\n //\n /*sdsd*d/sdsf\nffdfdf\r dgfg\n */ ff/* ddffgg*/\nd".toCharArray();
    car = new CharArrayReader(buffer);
    try {
    	th.check(st.nextToken(),TT_NUMBER , "checking return value ... -- 20");
    	th.check(st.nval, -1.22 , "parsing words -- 7");
    	th.check(st.nextToken(), StreamTokenizer.TT_NUMBER, "checking return value ... -- 21");
    	th.check(st.nval, 0.45 , "parsing words -- 7");
    	st = new StreamTokenizer(car);
    	st.ordinaryChar((int)'/');
        st.slashSlashComments(true);
        st.slashStarComments(true);
    	th.check(st.nextToken(), StreamTokenizer.TT_WORD, "checking return value ... -- 22");
    	th.check(st.sval, "abcd" , "parsing words -- 8");
    	th.check(st.nextToken(), StreamTokenizer.TT_WORD, "checking return value ... -- 23");
    	th.check(st.sval, "ff" , "parsing words -- 9");
    	th.check(st.nextToken(), StreamTokenizer.TT_WORD, "checking return value ... -- 24");
    	th.check(st.sval, "d" , "parsing words -- 10");

    }
    catch (IOException ioe){ th.fail("got unwanted IOException -- 2"); }

  }
}
