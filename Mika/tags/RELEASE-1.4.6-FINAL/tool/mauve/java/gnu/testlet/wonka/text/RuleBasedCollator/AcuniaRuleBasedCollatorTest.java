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


package gnu.testlet.wonka.text.RuleBasedCollator;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.text.*;


public class AcuniaRuleBasedCollatorTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness) {
    th = harness;
    th.setclass("java.text.RuleBasedCollator");
    test_();
    try {
      String str = " < a ; \u00E0 ; \u00e1 < x < b < c < ch < d < e < f < g < h < i < z ; z\u0316\u0314\u0315 < y ; y\u0300\u0301";
      RuleBasedCollator rbc = new RuleBasedCollator(str);
      rbc.setStrength(Collator.PRIMARY);
      //th.debug(rbc.getRules());
      th.check(rbc.compare("ac", "x") < 0 , "comparing - plain order -- 1");
      th.check(rbc.compare("a", "xd") < 0 , "comparing - plain order -- 2");
      th.check(rbc.compare("ae", "xd") < 0 , "comparing - plain order -- 3");
      th.check(rbc.compare("ae", "ae") , 0 , "comparing - plain order -- 4");
      th.check(rbc.compare("a\u00E0", "\u00E0\u00E1") , 0 , "comparing - plain order -- 5");
      th.check(rbc.compare("b", "xd") > 0 , "comparing - plain order -- 6");
      th.check(rbc.compare("ax", "\u00E0\u00E1") > 0 , "comparing - plain order -- 7");
      compareLT(rbc,"cd","ch");
      compareLT(rbc,"ci","ch");
      compareGT(rbc,"d","ch");
      compareGT(rbc,"j","\u00E0");
      compareGT(rbc,"y\u0301\u0300","y\u0300\u0301");
      rbc.setStrength(Collator.SECONDARY);
      compareLT(rbc,"a" ,"\u00E0");
      compareGT(rbc,"\u00E1" ,"\u00E0");
      th.check(rbc.compare("z\u0316\u0314\u0315", "z\u0314\u0315\u0316"), 0 , "comparing - canonical ordering -- 1");
      th.check(rbc.compare("z\u0316\u0314\u0315", "z\u0316\u0315\u0314"), 0 , "comparing - canonical ordering -- 2");
      th.check(rbc.compare("z\u0316\u0314\u0315", "z\u0315\u0314\u0316"), 0 , "comparing - canonical ordering -- 3");
      th.check(rbc.compare("z\u0314\u0315\u0316", "z\u0316\u0315\u0314"), 0 , "comparing - canonical ordering -- 4");
      th.check(rbc.compare("\u00e0", "a\u0300"), 0 , "comparing - decomposition -- 1");
      //th.debug(str);
      str = "abcdefgacadxaech\u00E0\u00E1zz\u0316\u0315\u0314yy\u0300\u0301ay\u0301\u0300";
      //th.debug(str);
      CollationElementIterator cei = rbc.getCollationElementIterator(str);
      int order = cei.next();
/*
      while(order != CollationElementIterator.NULLORDER){
        System.out.println(Integer.toHexString(order));
        order = cei.next();
      }
*/
      str = "< a < b ; x , y < f , z < g < h & b ; u ; l , m < e , v & b ; k , w < c < d";
      rbc = new RuleBasedCollator(str);
      compareLT(rbc,"c","ch");
      compareLT(rbc,"ag","ch");
      compareGT(rbc,"d","ch");
      compareGT(rbc,"g","d");
      compareGT(rbc,"e","a");
      str = "abkwcdulmevxyfzgh";
      cei = rbc.getCollationElementIterator(str);
      order = cei.next();
/*
      while(order != CollationElementIterator.NULLORDER){
        System.out.println(Integer.toHexString(order));
        order = cei.next();
      }
*/
      str = "< a < b < e & b < c < d & ad ; x < f < g";
      rbc = new RuleBasedCollator(str);
      compareLT(rbc,"x","b");
      compareLT(rbc,"x","ae");
      compareLT(rbc,"d","e");
      compareLT(rbc,"e","g");
      compareGT(rbc,"g","c");
      compareGT(rbc,"ae","x");
      compareGT(rbc,"x","ad");
      compareGT(rbc,"e","a");
      str = "abcdexfg";
      cei = rbc.getCollationElementIterator(str);
      order = cei.next();
/*      System.out.println();
      while(order != CollationElementIterator.NULLORDER){
        System.out.print(Integer.toHexString(order)+", ");
        order = cei.next();
      }
      System.out.println();
*/
      rbc.setStrength(Collator.PRIMARY);
      th.check(rbc.compare("x","ad"), 0 , "comparing - combined pattern -- 1");
    }
    catch (ParseException pe){
      th.debug(pe);
      pe.printStackTrace();
    }
  }

  private void compareLT(RuleBasedCollator rbc, String one, String two){
    int result = rbc.compare(one, two);
    th.check(result  < 0 , "comparing '"+one+"' with '"+two+"' "+result+" ?<? 0");
  }

  private void compareGT(RuleBasedCollator rbc, String one, String two){
    int result = rbc.compare(one, two);
    th.check(result  > 0 , "comparing '"+one+"' with '"+two+"' "+result+" ?>? 0");
  }

/**
*   not implemented. <br>
*
*/
  public void test_(){
    th.checkPoint("()");

  }

}
