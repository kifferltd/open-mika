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


package gnu.testlet.wonka.util.ResourceBundle;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.*;

/**
*  this file contains test for ResourceBundle  <br>
*  <br>
*  by extending ResourceBundle we are allowed to access all protected methods and fields
*/

public class SMResourceBundleTest extends ResourceBundle implements Testlet {

  protected TestHarness th;	

  public Enumeration getKeys(){
  	return null;
  }
  protected Object handleGetObject(String key) {
   	String [] sa = null;
   	if (key.equals("")) {
   		sa = new String[1];
   		sa[0] = "nothing";
   	}
   	return  sa;
  }

  public void test (TestHarness harness) {
      th = harness;
      th.setclass("java.util.ResourceBundle");
      test_getBundle();
      test_getObject();
      test_getString();
      test_getStringArray();
      test_handleGetObject();
      test_setParent();
  }

/**
* not implemented
* done in getBundle.java
*/
  public void test_getBundle(){
	th.checkPoint("()");
  }

/**
* implemented
*
*/
  public void test_getObject(){
	th.checkPoint("getObject(java.lang.String)java.lang.Object");
        Locale jap = new Locale("jp","ja","win_95");
        String s = "gnu.testlet.wonka.util.ResourceBundle.Resource5";
        ResourceBundle res = getBundle(s , jap);
        th.check(s.equals(res.getObject(s)),"checking all parents checked for the Object");
 	try {
 		res.getObject("not there");
 		th.fail("should throw a MissingResourceException");
 	}
 	catch(MissingResourceException mre) { th.check(true);}
  }

/**
* implemented
*
*/
  public void test_getString(){
	th.checkPoint("getString(java.lang.String)java.lang.String");
        Locale jap = new Locale("jp","ja","win_95");
        String s = "gnu.testlet.wonka.util.ResourceBundle.Resource5";
        ResourceBundle res = getBundle(s , jap);
        th.check(s.equals(res.getString(s)),"checking all parents checked for the Object");
 	try {
 		res.getObject("not there");
 		th.fail("should throw a MissingResourceException");
 	}
 	catch(MissingResourceException mre) { th.check(true);}

  }

/**
* implemented
*
*/
  public void test_getStringArray(){
	th.checkPoint("getStringArray(java.lang.String)java.lang.String[]");
        String s = "gnu.testlet.wonka.util.ResourceBundle.Resource5";
        th.check("nothing" , this.getStringArray("")[0] ,"checking all parents checked for the Object");

  }

/**
* not implemented
* abstract Method ...
*/
  public void test_handleGetObject(){
	th.checkPoint("()");

  }

/**
* implemented
*
*/
  public void test_setParent(){
	th.checkPoint("setParent(java.util.ResourceBundle)void");
        Locale jap = new Locale("jp","ja","win_95");
        String s = "gnu.testlet.wonka.util.ResourceBundle.Resource5";
        ResourceBundle res = getBundle(s , jap);
        this.setParent(res);
        th.check(s , getObject(s) ,"checking all parents checked for the Object");

  }

}

