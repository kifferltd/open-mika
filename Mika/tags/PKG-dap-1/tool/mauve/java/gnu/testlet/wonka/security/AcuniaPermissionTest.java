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


/*
** $Id: AcuniaPermissionTest.java,v 1.3 2006/10/04 14:24:18 cvsroot Exp $
*/


package gnu.testlet.wonka.security;

import gnu.testlet.*;
import com.acunia.wonka.*;
import java.util.Dictionary;
import java.util.Enumeration;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.PropertyPermission;
import gnu.testlet.wonka.security.BasicPermission.ExBasicPermission;
/**
* this test was origanally written by Chris Gray. <br>
* the code was adapted to run under the mauve framework <br>
* <br>
* this file contains test on permissions
*/
public class AcuniaPermissionTest implements Testlet	{

  protected TestHarness th;
  private void test_implies(Permission a, Permission b, boolean expected) {
    boolean result = a.implies(b);
   th.check(result == expected , a+" implies "+b+": "+result+", expected: "+expected);

  }

  private void test_implies(PermissionCollection c, Permission b, boolean expected) {
    boolean result = c.implies(b);
    th.check(result==expected,c+" implies "+b+": "+result+", expected: "+expected);
  }

  public void test(TestHarness harness) {
    th = harness;
    th.setclass("permissions");
    String arch = System.getProperty("os.arch");
    Runtime rt = Runtime.getRuntime();
    Boolean impl;

    ExBasicPermission ba_foo_bar_baz = new ExBasicPermission("foo.bar.baz");
    ExBasicPermission ba_foo_bar_quux = new ExBasicPermission("foo.bar.quux");
    ExBasicPermission ba_foo_bar_bacon = new ExBasicPermission("foo.bar.bacon");
    ExBasicPermission ba_foo_bar_star = new ExBasicPermission("foo.bar.*");
    ExBasicPermission ba_foo_balls = new ExBasicPermission("foo.balls");
    ExBasicPermission ba_off_the_wall = new ExBasicPermission("off.the.wall");
    java.io.FilePermission fi_fish_fillet = new java.io.FilePermission("wonka","execute");
    test_implies(ba_foo_bar_baz,ba_foo_bar_baz,true);
    test_implies(ba_foo_bar_baz,ba_foo_bar_star,false);
    test_implies(ba_foo_bar_star,ba_foo_bar_baz,true);
    test_implies(ba_foo_bar_star,ba_foo_balls,false);

    PermissionCollection ba_pc = ba_foo_bar_baz.newPermissionCollection();
    ba_pc.add(ba_foo_bar_baz);
    ba_pc.add(ba_foo_bar_quux);
    ba_pc.add(ba_foo_bar_star);
    try {
      ba_pc.add(fi_fish_fillet);  // not a BasicPermission!
      th.fail(ba_pc+" accepted add("+fi_fish_fillet+") ==> ERROR");
    } catch (IllegalArgumentException e) {
      th.check(true, "Threw "+e+" ==> OK");
    }

    th.checkPoint("BasicPermissionCollection: ");
    Enumeration bae = ba_pc.elements();
    while (bae.hasMoreElements()) {
      th.debug("  "+bae.nextElement());
    }
    test_implies(ba_pc,ba_foo_bar_baz,true);
    test_implies(ba_pc,ba_foo_bar_quux,true);
    test_implies(ba_pc,ba_foo_bar_star,true);
    test_implies(ba_pc,ba_foo_bar_bacon,true);
    test_implies(ba_pc,ba_foo_balls,false);
    test_implies(ba_pc,ba_off_the_wall,false);

    th.checkPoint("FilePermission");
    java.io.FilePermission fi_foo_bar_baz = new java.io.FilePermission("/foo/bar/baz","read,write");
    java.io.FilePermission fi_foo_bar_quux = new java.io.FilePermission("/foo/bar/quux","execute");
    java.io.FilePermission fi_foo_bar_bacon = new java.io.FilePermission("/foo/bar/bacon","read");
    java.io.FilePermission fi_foo_bar_star = new java.io.FilePermission("/foo/bar/*","read");
    java.io.FilePermission fi_foo_balls = new java.io.FilePermission("/foo/balls","read");
    java.io.FilePermission fi_foo_falls = new java.io.FilePermission("/foo/falls","delete");
    java.io.FilePermission fi_off_the_wall = new java.io.FilePermission("/off/the/wall","write");
    java.io.FilePermission fi_delete_all  = new java.io.FilePermission("<<ALL FILES>>","delete");
    ExBasicPermission ba_fish_fillet = new ExBasicPermission("wonk");

    test_implies(fi_foo_bar_baz,fi_foo_bar_baz,true);
    test_implies(fi_foo_bar_baz,fi_foo_bar_star,false);
    test_implies(fi_foo_bar_star,fi_foo_bar_baz,false);
    test_implies(fi_foo_bar_star,fi_foo_falls,false);

    th.checkPoint("FilePermissionCollection");
    java.security.PermissionCollection fi_pc = fi_foo_bar_baz.newPermissionCollection();
    fi_pc.add(fi_foo_bar_baz);
    fi_pc.add(fi_foo_bar_quux);
    fi_pc.add(fi_foo_bar_star);
    fi_pc.add(fi_delete_all);
    try {
      fi_pc.add(ba_fish_fillet);  // not a FilePermission!
      th.fail(fi_pc+" accepted add("+ba_fish_fillet+") ==> ERROR");
    } catch (IllegalArgumentException e) {
      th.check(true , "Threw "+e+" ==> OK");
    }

    Enumeration fi_pce = fi_pc.elements();
    while (fi_pce.hasMoreElements()) {
      th.debug("  "+fi_pce.nextElement());
    }
    test_implies(fi_pc,fi_foo_bar_baz,true);
    test_implies(fi_pc,fi_foo_bar_quux,true);
    test_implies(fi_pc,fi_foo_bar_star,true);
    test_implies(fi_pc,fi_foo_bar_bacon,true);
    test_implies(fi_pc,fi_foo_falls,true);
    test_implies(fi_pc,fi_foo_balls,false);
    test_implies(fi_pc,fi_off_the_wall,false);

    th.checkPoint("PropertyPermissionCollection");
    java.util.PropertyPermission pr_foo_bar_baz = new java.util.PropertyPermission("foo.bar.baz","read,write");
    java.util.PropertyPermission pr_foo_bar_quux = new java.util.PropertyPermission("foo.bar.quux","read,write");
    java.util.PropertyPermission pr_foo_bar_bacon = new java.util.PropertyPermission("foo.bar.bacon","write");
    java.util.PropertyPermission pr_foo_bar_star = new java.util.PropertyPermission("foo.bar.*","read");
    java.util.PropertyPermission pr_foo_balls = new java.util.PropertyPermission("foo.balls","read,write");
    try {
    	java.util.PropertyPermission pr_off_the_wall = new java.util.PropertyPermission("off.the.wall","pruts");
    	th.fail("bad action should trigger IllegalArgumentException");
    }
    catch (IllegalArgumentException iae) { th.check(true); }
    test_implies(pr_foo_bar_baz,pr_foo_bar_baz,true);
    test_implies(pr_foo_bar_baz,pr_foo_bar_star,false);
    test_implies(pr_foo_bar_star,pr_foo_bar_baz,false);

    PermissionCollection pr_pc = pr_foo_bar_baz.newPermissionCollection();
    pr_pc.add(pr_foo_bar_baz);
    pr_pc.add(pr_foo_bar_quux);
    pr_pc.add(pr_foo_bar_star);
    try {
      pr_pc.add(fi_fish_fillet);  // not a PropertyPermission!
      th.fail(pr_pc+" accepted add("+fi_fish_fillet+") ==> ERROR");
    } catch (IllegalArgumentException e) {
      th.check(true ,"Threw "+e+" ==> OK");
    }

    th.checkPoint("PropertyPermissionCollection:");
    Enumeration pr_pce = pr_pc.elements();
    while (pr_pce.hasMoreElements()) {
      th.debug("  "+pr_pce.nextElement());
    }
    test_implies(pr_pc,pr_foo_bar_baz,true);
    test_implies(pr_pc,pr_foo_bar_quux,true);
    test_implies(pr_pc,pr_foo_bar_star,true);
    test_implies(pr_pc,pr_foo_bar_bacon,false);
    test_implies(pr_pc,pr_foo_balls,false);

    java.security.AllPermission ap1 = new java.security.AllPermission("foo","bar");
    java.security.AllPermission ap2 = new java.security.AllPermission();
    test_implies(ap1,ap2,true);
    th.check(ap1.equals(ap2),"ap1.equals(ap2) should be true");


  }

}

