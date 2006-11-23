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


package gnu.testlet.wonka.util.StringTokenizer;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.util.StringTokenizer;
import java.util.NoSuchElementException;
/**
*  this file contains test for java.util.StringTokenizer   <br>
*
*/
public class AcuniaStringTokenizerTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.StringTokenizer");
       test_StringTokenizer();
       test_countTokens();
       test_hasMoreTokens();
       test_hasMoreElements();
       test_nextToken();
       test_nextElement();
       //profile();
       //test_behaviour();
     }

/**
* implemented. <br>
*
*/
  public void test_StringTokenizer(){
    th.checkPoint("StringTokenizer(java.lang.String)");
      try {
        new StringTokenizer(null);
        th.fail("null is not allowed");
      }catch (NullPointerException np) {
        th.check(true);
      }
      StringTokenizer st = new StringTokenizer("a\rb c\nd\te");
      th.check(st.nextElement(), "a" , "verifying default delimeter chars -- 1");
      th.check(st.nextElement(), "b" , "verifying default delimeter chars -- 2");
      th.check(st.nextElement(), "c" , "verifying default delimeter chars -- 3");
      th.check(st.nextElement(), "d" , "verifying default delimeter chars -- 4");
      th.check(st.nextElement(), "e" , "verifying default delimeter chars -- 5");

    th.checkPoint("StringTokenizer(java.lang.String,java.lang.String)");
      try {
        new StringTokenizer(null,"a");
        th.fail("null is not allowed -- 1");
      }catch (NullPointerException np) {
        th.check(true);
      }
      try {
        st = new StringTokenizer("a",null);
        th.fail("null is not allowed -- 2");
        st.nextElement();
      }catch (NullPointerException np) {
        th.check(true);
      }
      st = new StringTokenizer("a\rb c\nd\te","bcbd");
      th.check(st.nextElement(), "a\r" , "verifying delimeter chars -- 1");
      th.check(st.nextElement(), " " , "verifying delimeter chars -- 2");
      th.check(st.nextElement(), "\n" , "verifying delimeter chars -- 3");
      th.check(st.nextElement(), "\te" , "verifying delimeter chars -- 4");

    th.checkPoint("StringTokenizer(java.lang.String,java.lang.String,boolean)");
      try {
        new StringTokenizer(null,"a",true);
        th.fail("null is not allowed -- 1");
      }catch (NullPointerException np) {
        th.check(true);
      }
      try {
        st = new StringTokenizer("a",null,true);
        th.fail("null is not allowed -- 2");
        st.nextToken();
      }catch (NullPointerException np) {
        th.check(true);
      }
      st = new StringTokenizer("a\rb c\nd\te","bcbd",false);
      th.check(st.nextToken(), "a\r" , "verifying delimeter chars -- 1");
      th.check(st.nextToken(), " " , "verifying delimeter chars -- 2");
      th.check(st.nextToken(), "\n" , "verifying delimeter chars -- 3");
      th.check(st.nextToken(), "\te" , "verifying delimeter chars -- 4");
      st = new StringTokenizer("a\rb c\nd\te","bcbd",true);
      th.check(st.nextToken(), "a\r" , "verifying delimeter chars -- 1");
      th.check(st.nextToken(), "b" , "verifying delimeter chars -- 2");
      th.check(st.nextToken(), " " , "verifying delimeter chars -- 3");
      th.check(st.nextToken(), "c" , "verifying delimeter chars -- 4");
  }

/**
* implemented. <br>
*
*/
  public void test_countTokens(){
    th.checkPoint("countTokens()int");
      StringTokenizer st = new StringTokenizer("a\rb c\nd\te ");
      th.check(st.countTokens() , 5 , "counting tokens -- 1");
      st.nextToken("d");
      th.check(st.countTokens() , 1 , "counting tokens -- 2");
      st.nextToken("d\t ");
      th.check(st.countTokens() , 0 , "counting tokens -- 3");
      st = new StringTokenizer("a\rb c\nd\te "," \r\t\n",true);
      th.check(st.countTokens() , 10 , "counting tokens -- 4");
      st.nextToken("d");
      th.check(st.countTokens() , 2 , "counting tokens -- 5");
      st.nextToken("\t ");
      th.check(st.countTokens() , 3 , "counting tokens -- 6");
      st = new StringTokenizer("abc de"," ",false);
      th.check(st.countTokens() , 2 , "counting tokens -- 7");
      st = new StringTokenizer("abc de"," ",true);
      th.check(st.countTokens() , 3 , "counting tokens -- 8");
      st = new StringTokenizer(" abc   de "," ",false);
      th.check(st.countTokens() , 2 , "counting tokens -- 9");
      st = new StringTokenizer(" abc   de "," ",true);
      th.check(st.countTokens() , 7 , "counting tokens -- 10");
      st = new StringTokenizer("a bc   de "," ",false);
      th.check(st.countTokens() , 3 , "counting tokens -- 11");
      st = new StringTokenizer("a bc   de "," ",true);
      th.check(st.countTokens() , 8 , "counting tokens -- 12");
      st = new StringTokenizer("   a bc   de "," ",false);
      th.check(st.countTokens() , 3 , "counting tokens -- 13");
      st = new StringTokenizer("   a bc   de "," ",true);
      th.check(st.countTokens() ,11 , "counting tokens -- 14");
      st = new StringTokenizer("xx  a bdc   def  "," ",false);
      th.check(st.countTokens() , 4 , "counting tokens -- 15");
      st = new StringTokenizer("xx  a bdc   def  "," ",true);
      th.check(st.countTokens() ,12 , "counting tokens -- 16");

  }

/**
* implemented. <br>
*
*/
  public void test_hasMoreTokens(){
    th.checkPoint("hasMoreTokens()boolean");
      StringTokenizer st = new StringTokenizer("a\rb c\nd\te ");
      th.check(st.hasMoreTokens(), "hasMoretokens -- 1");
      st.nextToken("d");
      th.check(st.hasMoreTokens(), "hasMoretokens -- 2");
      th.check(st.countTokens() , 1 , "counting tokens -- 1");
      st.nextToken("\t ");
      th.check(!st.hasMoreTokens(), "hasMoretokens -- 3");
      th.check(st.countTokens() , 0 , "counting tokens -- 2");
      st = new StringTokenizer("   e");
      st.nextToken();
      th.check(!st.hasMoreTokens(), "hasMoretokens -- 4");
      st = new StringTokenizer("   efg");
      th.check(st.hasMoreTokens(), "hasMoretokens -- 5");
      st.nextToken("e");
      th.check(!st.hasMoreTokens(), "hasMoretokens -- 6");

      st = new StringTokenizer("a\rb c\nd\te "," \r\t\n",true);
      th.check(st.hasMoreTokens(), "hasMoretokens -- 7");
      st.nextToken("d");
      th.check(st.hasMoreTokens(), "hasMoretokens -- 8");
      th.check(st.nextToken() , "d", "delimeter token not skipped");
      th.check(st.countTokens() , 1 , "counting tokens -- 3");
      st.nextToken("\t ");
      th.check(st.hasMoreTokens(), "hasMoretokens -- 9");
      th.check(st.countTokens() , 2 , "counting tokens -- 4");
      st = new StringTokenizer("   e", " ", true);
      th.check(st.hasMoreTokens(), "hasMoretokens -- 10");
      th.check(st.nextToken("e"), "   ");
      th.check(st.hasMoreTokens(), "hasMoretokens -- 11");
      st.nextToken();
      th.check(!st.hasMoreTokens(), "hasMoretokens -- 12");

  }

/**
* implemented. <br>
*
*/
  public void test_hasMoreElements(){
    th.checkPoint("hasMoreElements()boolean");
      StringTokenizer st = new StringTokenizer("a\rb c\nd\te ");
      th.check(st.hasMoreTokens(), "hasMoreElements -- 1");
      st.nextToken("d");
      th.check(st.hasMoreTokens(), "hasMoreElements -- 2");
      th.check(st.countTokens() , 1 , "counting tokens -- 1");
      st.nextToken("\t ");
      th.check(!st.hasMoreTokens(), "hasMoreElements -- 3");
      th.check(st.countTokens() , 0 , "counting tokens -- 2");
      st = new StringTokenizer("   e");
      st.nextToken();
      th.check(!st.hasMoreTokens(), "hasMoreElements -- 4");
      st = new StringTokenizer("   efg");
      th.check(st.hasMoreTokens(), "hasMoreElements -- 5");
      st.nextToken("e");
      th.check(!st.hasMoreTokens(), "hasMoreElements -- 6");

      st = new StringTokenizer("a\rb c\nd\te "," \r\t\n",true);
      th.check(st.hasMoreTokens(), "hasMoreElements -- 7");
      st.nextToken("d");
      th.check(st.hasMoreTokens(), "hasMoreElements -- 8");
      th.check(st.nextToken() , "d", "delimeter token not skipped");
      th.check(st.countTokens() , 1 , "counting tokens -- 3");
      st.nextToken("\t ");
      th.check(st.hasMoreTokens(), "hasMoreElements -- 9");
      th.check(st.countTokens() , 2 , "counting tokens -- 4");
      st = new StringTokenizer("   e", " ", true);
      th.check(st.hasMoreTokens(), "hasMoreElements -- 10");
      th.check(st.nextToken("e"), "   ");
      th.check(st.hasMoreTokens(), "hasMoreElements -- 11");
      st.nextToken();
      th.check(!st.hasMoreTokens(), "hasMoreElements -- 12");

  }

/**
* implemented. <br>
*
*/
  public void test_nextToken(){
    th.checkPoint("nextToken()java.lang.String");
      StringTokenizer st = new StringTokenizer("ab\rb  cd\ndef\t \nef ");
      th.check(st.nextToken() , "ab" , "checking nextToken -- 1");
      th.check(st.nextToken() , "b" , "checking nextToken -- 2");
      th.check(st.nextToken() , "cd" , "checking nextToken -- 3");
      th.check(st.nextToken() , "def" , "checking nextToken -- 4");
      th.check(st.nextToken() , "ef" , "checking nextToken -- 5");
      try {
        st.nextToken();
        th.fail("should throw NoSuchElementException -- 1");
      }catch  (Exception e){
        th.check(e instanceof NoSuchElementException ,"verify Exception type -- 1");
      }

      st = new StringTokenizer("ab\rb  cd\nde\t \nef "," \r\t\n", true);
      th.check(st.nextToken() , "ab" ,"checking nextToken -- a1");
      th.check(st.nextToken() , "\r" ,"checking nextToken -- a2");
      th.check(st.nextToken() , "b" , "checking nextToken -- a3");
      th.check(st.nextToken() , " " , "checking nextToken -- a4");
      th.check(st.nextToken() , " " , "checking nextToken -- a5");

      th.check(st.nextToken() , "cd" ,"checking nextToken -- a6");
      th.check(st.nextToken() , "\n" ,"checking nextToken -- a7");
      th.check(st.nextToken() , "de" ,"checking nextToken -- a8");
      th.check(st.nextToken() , "\t" ,"checking nextToken -- a9");
      th.check(st.nextToken() , " " , "checking nextToken -- a10");
      th.check(st.nextToken() , "\n" ,"checking nextToken -- a11");
      th.check(st.nextToken() , "ef" ,"checking nextToken -- a12");
      th.check(st.nextToken() , " " , "checking nextToken -- a13");

      try {
        st.nextToken();
        th.fail("should throw NoSuchElementException -- 2");
      }catch  (Exception e){
        th.check(e instanceof NoSuchElementException ,"verify Exception type -- 2");
      }

    th.checkPoint("nextToken(java.lang.String)java.lang.String");
      st = new StringTokenizer("ab\rb  cd\ndef\t \nef ");
      th.check(st.nextToken("\r") , "ab",  "checking nextToken -- 1");
      th.check(st.nextToken("c") , "\rb  ","checking nextToken -- 2");
      th.check(st.nextToken("d") , "c",    "checking nextToken -- 3");
      th.check(st.nextToken("d") , "\n",   "checking nextToken -- 4");
      th.check(st.nextToken("f") , "de",   "checking nextToken -- 5");
      th.check(st.nextToken("f") ,"\t \ne","checking nextToken -- 6");
      th.check(st.nextToken("f") ," ",     "checking nextToken -- 7");

      try {
        st.nextToken("f");
        th.fail("should throw NoSuchElementException -- 1");
      }catch  (Exception e){
        th.check(e instanceof NoSuchElementException ,"verify Exception type -- 1");
      }

      st = new StringTokenizer("ab\rb  cd\ndef\t \nef " ,"", true);
      th.check(st.nextToken("\r") , "ab",  "checking nextToken -- a1");
      th.check(st.nextToken("c") , "\rb  ","checking nextToken -- a2");
      th.check(st.nextToken("d") , "c",    "checking nextToken -- a3");
      th.check(st.nextToken("d") , "d",    "checking nextToken -- a4");
      th.check(st.nextToken("d") , "\n",   "checking nextToken -- a5");
      th.check(st.nextToken("f") , "de",   "checking nextToken -- a6");
      th.check(st.nextToken("f") ,"f",     "checking nextToken -- a7");
      th.check(st.nextToken("f") ,"\t \ne","checking nextToken -- a8");
      th.check(st.nextToken("f") ,"f",     "checking nextToken -- a9");
      th.check(st.nextToken("f") ," ",     "checking nextToken -- a10");

      try {
        st.nextToken("f");
        th.fail("should throw NoSuchElementException -- 2");
      }catch  (Exception e){
        th.check(e instanceof NoSuchElementException ,"verify Exception type -- 2");
      }
  }

/**
* implemented. <br>
*
*/
  public void test_nextElement(){
    th.checkPoint("nextElement()java.lang.String");
      StringTokenizer st = new StringTokenizer("ab\rb  cd\ndef\t \nef ");
      th.check(st.nextElement() , "ab" , "checking nextElement -- 1");
      th.check(st.nextElement() , "b" , "checking nextElement -- 2");
      th.check(st.nextElement() , "cd" , "checking nextElement -- 3");
      th.check(st.nextElement() , "def" , "checking nextElement -- 4");
      th.check(st.nextElement() , "ef" , "checking nextElement -- 5");
      try {
        st.nextElement();
        th.fail("should throw NoSuchElementException -- 1");
      }catch  (Exception e){
        th.check(e instanceof NoSuchElementException ,"verify Exception type -- 1");
      }

      st = new StringTokenizer("ab\rb  cd\nde\t \nef "," \r\t\n", true);
      th.check(st.nextElement() , "ab" ,"checking nextElement -- a1");
      th.check(st.nextElement() , "\r" ,"checking nextElement -- a2");
      th.check(st.nextElement() , "b" , "checking nextElement -- a3");
      th.check(st.nextElement() , " " , "checking nextElement -- a4");
      th.check(st.nextElement() , " " , "checking nextElement -- a5");

      th.check(st.nextElement() , "cd" ,"checking nextElement -- a6");
      th.check(st.nextElement() , "\n" ,"checking nextElement -- a7");
      th.check(st.nextElement() , "de" ,"checking nextElement -- a8");
      th.check(st.nextElement() , "\t" ,"checking nextElement -- a9");
      th.check(st.nextElement() , " " , "checking nextElement -- a10");
      th.check(st.nextElement() , "\n" ,"checking nextElement -- a11");
      th.check(st.nextElement() , "ef" ,"checking nextElement -- a12");
      th.check(st.nextElement() , " " , "checking nextElement -- a13");

      try {
        st.nextElement();
        th.fail("should throw NoSuchElementException -- 2");
      }catch  (Exception e){
        th.check(e instanceof NoSuchElementException ,"verify Exception type -- 2");
      }

  }

  public void profile(){
    long time = System.currentTimeMillis();
    for (int i = 0 ; i < 250 ; i++){
      test_nextElement();
    }
    System.out.println("time nextElement = "+(System.currentTimeMillis()-time));

    time = System.currentTimeMillis();
    for (int i = 0 ; i < 250 ; i++){
      test_countTokens();
    }
    System.out.println("time countTokens = "+(System.currentTimeMillis()-time));

    time = System.currentTimeMillis();
    for (int i = 0 ; i < 250 ; i++){
      test_hasMoreTokens();
    }
    System.out.println("time hasMoreTokens = "+(System.currentTimeMillis()-time));

    time = System.currentTimeMillis();
    for (int i = 0 ; i < 250 ; i++){
      test_nextToken();
    }
    System.out.println("time nextToken = "+(System.currentTimeMillis()-time));

    time = System.currentTimeMillis();
    for (int i = 0 ; i < 250 ; i++){
      test_StringTokenizer();
    }
    System.out.println("time StringTokenizer = "+(System.currentTimeMillis()-time));

    time = System.currentTimeMillis();
    for (int i = 0 ; i < 1500 ; i++){
      StringTokenizer st = new StringTokenizer("let tell a story.  So we have a really long text wich can be tokenized.\n Hello world is common application.  It covers most of the basic functionalty offered by devellopment environments.\nIf you haven't noticed yet: this is a lot of crap");
      int nr = st.countTokens();
      for (int j = 0 ; j < nr ; j++){
        st.nextToken();
      }
    }
    System.out.println("time common use = "+(System.currentTimeMillis()-time));

    time = System.currentTimeMillis();
    for (int i = 0 ; i < 2500 ; i++){
      StringTokenizer st = new StringTokenizer("com.acunia.wonka.service.testlet.TestStringTokenizer" ,".",false);
      int nr = st.countTokens();
      for (int j = 0 ; j < nr ; j++){
        st.nextToken();
      }
    }
    System.out.println("time common use 2 = "+(System.currentTimeMillis()-time));
    System.out.println(new java.util.Date());
  }


/**
** this test take a lot of time to complete and doesn't give a problem with wonka ...
*/
  private void test_behaviour(){
/**
**   These lines of code are taken from a message posted to newsgroups
**   they demonstrate a possible memory leak:
**
**       From: gah@ugcs.caltech.edu (glen herrmannsfeldt)     01/28/02 19:34
**    Subject: fragmentation and StringTokenizer
** Newsgroups: comp.lang.java.machine, comp.lang.java.programmer
*/
    int i;
    String x,y;
    String a[];
    StringTokenizer st;
    a = new String[10000/*00*/];
    x = "yes this is a very long string";
    for(i=0;i<9;i++) x += x;
    try {
      for(i=0;i<10000/*00*/;i++) {
        y = new String(x);
        st = new StringTokenizer(y);
        a[i]=st.nextToken();
      }
    }
    catch(Exception e) {
      System.err.println(e);
    }
    catch(OutOfMemoryError e) {
      th.fail("should not run out of memory");
      System.err.println(i);
      System.err.println(e);
    }
/** end of quote */
  }
}
