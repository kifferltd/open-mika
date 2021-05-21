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


package gnu.testlet.wonka.util.Stack;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.util.*;

/**
*  this file contains test for java.util.Stack <br>
*
*/
public class SMStackTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.util.Stack");
       test_empty();
       test_peek();
       test_pop();
       test_push();
       test_search();
     }


/**
* implemented.
*
*/
  public void test_empty(){
    th.checkPoint("empty()boolean");
    Stack s = new Stack();
    th.check(s.empty() , "a new stack is empty");
    s.push(this);
    th.check(!s.empty() , "this stack is not empty");
    s.pop();
    th.check(s.empty() , "the stack is empty now");
  }
/**
* implemented.
*
*/
  public void test_peek(){
    th.checkPoint("peek()java.lang.Object");
    Stack s = new Stack();
    try { s.peek();
    	  th.fail("should throw EmptyStackException");
        }
    catch (EmptyStackException	ee){ th.check(true);}
    s.add("a"); s.add("b") ; s.add("c");
    th.check("c".equals(s.peek()) , "top element is c, but got:"+s.peek());
    s.add(null);
    th.check(s.peek()== null , "top element is null");
  }
/**
* implemented.
*
*/
  public void test_pop(){
    th.checkPoint("pop()java.lang.Object");
    Stack s = new Stack();
    try { s.pop();
    	  th.fail("should throw EmptyStackException -- 1");
        }
    catch (EmptyStackException	ee){ th.check(true);}
    s.add("a"); s.add("b") ; s.add("c");
    th.check("c".equals(s.pop()) , "popped element is c");
    th.check(!s.contains("c") , "element should be removed -- 1");
    s.add(null);
    th.check(s.pop()== null , "popped element is null");
    th.check(!s.contains("c") , "element should be removed -- 2");
    th.check("b".equals(s.pop()) , "popped element is b");
    th.check(!s.contains("b") , "element should be removed -- 3");
    th.check("a".equals(s.pop()) , "popped element is a");
    th.check(!s.contains("a") , "element should be removed -- 4");
    try { s.pop();
    	  th.fail("should throw EmptyStackException -- 2");
        }
    catch (EmptyStackException	ee){ th.check(true);}



  }
/**
* implemented.
*
*/
  public void test_push(){
    th.checkPoint("push(java.lang.Object)java.lang.Object");
    Stack s = new Stack();
    th.check("c".equals(s.push("c")) , "pushed element is c");
    th.check(s.contains("c") , "element should be added -- 1");
    th.check("b".equals(s.push("b")) , "pushed element is b");
    th.check(s.contains("b") , "element should be added -- 2");
    th.check("a".equals(s.push("a")) , "pushed element is a");
    th.check(s.contains("a") , "element should be added -- 3");
    th.check(s.push(null) == null , "null is allowed");
    th.check(s.lastElement()== null ,"added on the last place");
    th.check(s.toString().equals("[c, b, a, null]"), "got:"+s.toString());
  }
/**
* implemented.
*
*/
  public void test_search(){
    th.checkPoint("search(java.lang.Object)int");
    Stack s = new Stack();
    try {
    	th.check(s.search("a") == -1 , "empty stack should'n cause problems -- 1");
    	th.check(s.search(null) == -1 , "empty stack should'n cause problems -- 2");
    	}
    catch(Exception e) { th.fail("got unwanted Exception:"+e); }
    	
    s.add("a"); s.add("b"); s.add("c"); s.add("a");
    s.add("a"); s.add(null); s.add(null); s.add("top");
    th.check( s.search("a") == 4, "checking position -- 1" );
    th.check( s.search("b") == 7, "checking position -- 2" );
    th.check( s.search("c") == 6 , "checking position -- 3" );
    th.check( s.search("top") == 1, "checking position -- 4" );
    th.check( s.search(null) == 2, "checking position -- 5" );
    th.check( s.search("ab") == -1, "checking position -- 6" );
    s.pop(); s.pop(); s.pop();
    th.check( s.search("a") == 1, "checking position -- 7" );
    th.check( s.search(null) == -1, "checking position -- 8" );
  }

}
