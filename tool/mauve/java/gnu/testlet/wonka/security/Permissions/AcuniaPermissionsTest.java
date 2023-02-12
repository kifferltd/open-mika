/**************************************************************************
* Copyright (c) 2022 by Chris Gray, KIFFER Ltd.  All rights reserved.     *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

package gnu.testlet.wonka.security.Permissions; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.security.*; // at least the class you are testing ...
import java.net.SocketPermission;
import java.net.NetPermission;
import java.io.FilePermission;
import java.util.Enumeration;
import java.util.Vector;
import java.util.PropertyPermission;
import java.util.NoSuchElementException;
import java.lang.reflect.ReflectPermission;

/**
*  this file contains test for java.security.Permissions   <br>
*
*/
public class AcuniaPermissionsTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.security.Permissions");
       test_Permissions();
       test_add();
       test_implies();
       test_elements();
       th.setclass("java.security.PermissionCollection");
       test_isReadOnly();
       test_setReadOnly();
       test_toString();
     }


/**
*  not implemented. <br>
*  nothing to test ...
*/
  public void test_Permissions(){
    th.checkPoint("Permissions()");
  }

/**
*  implemented. <br>
*  see also elements and implies
*/
  public void test_add(){
    th.checkPoint("add(java.security.Permission)void");
    Permissions ps = new Permissions();
    ps.setReadOnly();
    try {
    	ps.add(new SecurityPermission("com.*"));
     	Enumeration e = ps.elements();
     	th.check(! e.hasMoreElements());
    }
    catch (SecurityException se) { th.check(true); }
  }

/**
*   implemented. <br>
*
*/
  public void test_elements(){
    th.checkPoint("elements()java.util.Enumeration");
    Permissions ps = new Permissions();
    Enumeration e = ps.elements();
    th.check(!e.hasMoreElements());
    try {
	e.nextElement();
	th.fail("should throw a NoSuchElementException");
    }
    catch(NoSuchElementException nse) { th.check(true); }
    Permission p = new SecurityPermission("com.*");
    Vector v = new Vector(11);
    p = new NetPermission("com.*");
    v.add(p); ps.add(p);
    p = new FilePermission("com.*","read,write");
    v.add(p); ps.add(p);
    p = new PropertyPermission("com.*","read");
    v.add(p); ps.add(p);
    p = new AllPermission();
    v.add(p); ps.add(p);
    p = new SocketPermission("*.com","connect");
    v.add(p); ps.add(p);
    p = new SecurityPermission("com.acunia");
    v.add(p); ps.add(p);
    p = new FilePermission("com.*","delete");
    v.add(p); ps.add(p);
    p = new SecurityPermission("*");
    v.add(p); ps.add(p);
    p = new SecurityPermission("toppie");
    v.add(p); ps.add(p);
    e = ps.elements();
    boolean ok=true;
    while (e.hasMoreElements()){
     	Object o = e.nextElement();
     	//th.debug("got "+o);
     	if (!v.remove(o)){
     	 	th.debug("element "+o+" not found in vector");
     	 	ok = false;
     	}
    }
    th.check(ok, "bad references returned");
    th.check(v.isEmpty(), "not all element were removed "+v);

  }

/**
* implemented. <br>
*
*/
  public void test_implies(){
    th.checkPoint("implies(java.security.Permission)boolean");
    Permissions ps = new Permissions();
    Permission p = new SecurityPermission("com.*");
    p = new NetPermission("com.*");
    ps.add(p);
    th.check(ps.implies(p) , "implies - 2");
    p = new FilePermission("com.*","read,write");
    ps.add(p);
    th.check(ps.implies(p) , "implies - 3");
    p = new PropertyPermission("com.*","read");
    ps.add(p);
    th.check(ps.implies(p) , "implies - 4");
    p = new NetPermission("not");
    ps.add(p);
    th.check(ps.implies(p) , "implies - 5");
    p = new SocketPermission("*.com","connect");
    ps.add(p);
    th.check(ps.implies(p) , "implies - 7");
    p = new SecurityPermission("*");
    ps.add(p);
    th.check(ps.implies(p) , "implies - 8");
    p = new FilePermission("com/-","delete");
    ps.add(p);
    th.check(ps.implies(p) , "implies - 9");
    p = new FilePermission("com/*","write,read");
    ps.add(p);
    th.check(ps.implies(p) , "implies - 10");
    th.check(ps.implies(new FilePermission("com/file1","read,write,delete")),"implies - 11");
    th.check(!ps.implies(new ReflectPermission("java.lang.*")) ,"not implied - 1");
    th.check(ps.implies(new SecurityPermission("java.lang.*")) ,"implied - 12");
    th.check(!ps.implies(new NetPermission("java.lang.*")) ,"not implied - 2");
    th.check(!ps.implies(new FilePermission("java.lang.*","execute")) ,"not implied - 4");
    th.check(!ps.implies(new SocketPermission("*.ac.be","connect")) ,"not implied - 5");
    th.check(!ps.implies(new UnresolvedPermission("java","java",null,null)) ,"not implied - 6");
    p = new AllPermission();
    ps.add(p);
    th.check(ps.implies(new ReflectPermission("java.lang.*")) ,"implied - 13");
    th.check(ps.implies(new NetPermission("java.lang.*")) ,"implied - 14");

  }

/**
*  implemented. <br>
*  see isReadOnly ...
*/
  public void test_setReadOnly(){
    th.checkPoint("setReadOnly()void");
  }

/**
*   implemented. <br>
*/
  public void test_isReadOnly(){
    th.checkPoint("isReadOnly()boolean");
    Permissions ps = new Permissions();
    th.check(! ps.isReadOnly() 	, "not readOnly");
    ps.setReadOnly();
    th.check(  ps.isReadOnly() 	, "set to readOnly");

  }
/**
*   implemented. <br>
*/
  public void test_toString(){
    th.checkPoint("toString()java.lang.String");
    Permissions ps = new Permissions();
    th.check( ps.toString() , "java.security.Permissions@"+Integer.toHexString(ps.hashCode())+" (\n)\n");
  }
}
