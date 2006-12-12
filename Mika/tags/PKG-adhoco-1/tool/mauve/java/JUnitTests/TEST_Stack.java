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
* $Id: TEST_Stack.java,v 1.1 2004/12/06 13:29:16 cvs Exp $
*

*/

package JUnitTests;

import gnu.testlet.*;
import java.util.*;

/**
 * Test case for java.util.Stack
 *
 * stack()
 *   Spec: Creates an empty stack
 *   Test: Create a new stack and check if it is empty. Since, according to Sun, the Stack
 *         is build on top of a Vector, we can check the underlying vector size to see if
 *         the stack is empty.
 *
 *
 * push()
 *   Spec: Pushes an item onto the top of the stack and return the pushed item
 *   Test1: Create an empty stack and push an item. Then verify that the method returns the 
 *          same item.
 *          This test must be executed by pushing several objects on the stack to test the 
 *          behavior with an empty and non-empty stack.
 *   Test2: Verify that the item is actually pushed on the top of the stack. According to
 *          Sun's specifications, the top of the stack is the latest element of the 
 *          extended Vector. We should test that condition rather than relying on the pop()
 *          method of the class under test (Stack).
 *          This test must be executed by pushing several objects on the stack to test the 
 *          behavior with an empty and non-empty stack.
 *
 * pop()
 *   Spec:  Removes the object at the top of the stack and returns that object as the value 
 *          of this function. An EmptyStackException is thrown if the stack is empty.
 *   Test1: Create an empty stack (as already tested above), and try to pop an element. An
 *          EmptyStackException must be thrown.
 *   Test2: Create an empty stack and add (push) several elements onto it. Then pop() them
 *          one after one, and compare them against the elements that have been added. The popped
 *          elements must be the same (according to Object.equals() function which shouldn't
 *          be overriden - object references will therefore be compared).
 *          Since the top most object is removed from the stack, and EmptyStackException must
 *          be thrown when the latest element is removed.
 *
 * peek()
 *   Spec: Looks at the object at the top of this stack without removing it from the stack.
 *         An EmptyStackException is thrown if the stack is empty.
 *   Test1: Create an empty stack and try to peek() an element from it. An EmptyStackException must
 *          be thrown.
 *   Test2: Create an empty stack and push an element onto it. Then call the peek() function 
 *          and compare (as described above) the returned element with the pushed one.
 *          This test must be done with an empty stack and a non-empty stack.
 *
 * empty()
 *   Spec: Return true if the stack is empty.
 *   Test: Create an empty stack (as tested above). The empty() function must return 0.
 *
 * search()
 *   Spec: Returns the 1-based position where an object is on the stack. If the requested object 
 *         occurs as an item in this stack, the method returns the distance from the top of the stack 
 *         of the occurence nearest to the top. The topmost item on the stack is considered to be at
 *         distance 1. The <code>equals</code> method is used to compare the requested object to the
 *         items in this stack. If the object is not found, this method returns -1.
 *   Test1: Create an empty stack and search() for any object. The method should return -1
 *   Test2: Create an empty stack and push several different elements in it. Call the search method 
 *          for the latest pushed element. The search() method should return 1.
 *   Test3: Create an empty stack and push several time the same element in it and a different element
 *          as the last one. Then call the search() method for the duplicated element. The method
 *          must return 2.
 *   Test4: Define a new object class and override its <code>equals</code> function. Creates a new
 *          empty stack and push several different (according to the new equals function) elements in it.
 *          The call the seach() method for each of these elements and verify that the returned value
 *          corresponds to their position on the stack (distance from the top).
 */
public class TEST_Stack extends Mv_Assert {
    // ---------------------------------------------------------
    // Standard JUnit test framework
    // ---------------------------------------------------------
    public TEST_Stack() {}

    // ---------------------------------------------------------
    // Actual test scenarios
    // ---------------------------------------------------------
    public void testStack1() throws Exception {
	Stack stack = new Stack();
	if (stack.isEmpty()==false) {
	    fail("Newly created Stack is empty");
	}
    }

    public void testPush1() throws Exception {
	Object o1 = new Object();
	Object o2 = new Object();
	Stack stack = new Stack();
	    
	if (o1 != stack.push(o1)) {
	    fail("Returned object invalid on empty stack");
	}
	
	if (o2 != stack.push(o2)) {
	    fail("Returned object invalid on non empty stack");
	}
    }

    public void testPush2() throws Exception {
	Object o1 = new Object();
	Object o2 = new Object();
	Stack stack = new Stack();
	    
	stack.push(o1);
	if ( (stack.size() <= 0) || (stack.elementAt(0) != o1)) {
	    fail("Pushed element is not at the top on an empty stack");
	}
	    
	stack.push(o2);
	if ( (stack.size() <= 1) || (stack.elementAt(1) != o2)) {
	    fail("Pushed element is not at the top on a non-empty stack");
	}
    }

    public void testPop1() throws Exception {
	try {
	    Stack stack = new Stack();
	    Object o = stack.pop();
	}
	catch(EmptyStackException ese) {
	    return;
	}

	fail("pop() didn't throw any EmptyStackException on an empty stack");
    }

    public void testPop2() throws Exception {
	Object o1 = new Object();
	Object o2 = new Object();

	boolean expectException = false;
	try {
	    Stack stack = new Stack();
	    stack.push(o1);
	    stack.push(o2);

	    if (o2 != stack.pop()) {
		fail("Popped element is not the expected one - case #1");
	    }

	    if (o1 != stack.pop()) {
		fail("Popped element is not the expected one - case #2");
	    }

	    expectException = true;
	    stack.pop();
	}
	catch(EmptyStackException ese) {
	    if (expectException == false) {
		fail("Caught unexpected EmptyStackException");
	    }
	}
    }

    public void testPeek1() throws Exception {
	try {
	    Stack stack = new Stack();
	    Object o = stack.peek();
	}
	catch(EmptyStackException ese) {
	    return;
	}

	fail("peek() didn't throw any EmptyStackException on an empty stack");
    }

    public void testPeek2() throws Exception {
	Object o1 = new Object();
	Object o2 = new Object();
	Stack stack = new Stack();
	
	stack.push(o1);
	if (o1 != stack.peek()) {
	    fail("peek() didn't return the right object on a one-element stack");
	}
	
	stack.push(o2);
	if (o2 != stack.peek()) {
	    fail("peek() didn't return the right object on a two-element stack");
	}
    }

    public void testEmpty1() throws Exception {
	Stack stack = new Stack();
	if (stack.empty() != true) {
	    fail("empty() doesn't return true on an empty stack");
	}
    }

    public void testSearch1() throws Exception {
	Stack stack = new Stack();
	if (stack.search(new Object()) != -1) {
	    fail("search() didn't return -1 on an empty stack");
	}
	
	stack.push(new Object());
	if (stack.search(new Object()) != -1) {
	    fail("search() didn't return -1 for an unknown object");
	}
    }

    public void testSearch2() throws Exception {
	Object o1 = new Object();
	Object o2 = new Object();
	Stack stack = new Stack();
	stack.push(o1);
	stack.push(o2);
	
	if (stack.search(o2) != 1) {
	    fail("search() didn't return 1 for the latest pushed element");
	}
	
	if (stack.search(o1) != 2) {
	    fail("search() didn't return 2 for the second latest pushed element");
	}
    } 

    public void testSearch3() throws Exception {
	Object o1 = new Object();
	Object o2 = new Object();
	Stack stack = new Stack();
	stack.push(o1);
	stack.push(o1);
	stack.push(o2);
	
	if (stack.search(o1) != 2) {
	    fail("search() didn't return the right position for an element being pushed() twice on the stack");
	}
    }

    public void testSearch4() throws Exception {
	search_test4_Element e11 = new search_test4_Element(1);
	search_test4_Element e12 = new search_test4_Element(1);
	search_test4_Element e2  = new search_test4_Element(2);
	
	Stack stack = new Stack();
	stack.push(e11);
	stack.push(e2);
	stack.push(e12);
	
	if (stack.search(e11) != 1) {
	    fail("search failed - case #1 - see test code");
	}
	
	if (stack.search(e2) != 2) {
	    fail("serach failed - case #2 - see test code)");
	}
    }

    public class search_test4_Element {
	private int id;
	search_test4_Element(int id) {
	    this.id = id;
	}
	public boolean equals(Object o) {
	    if (o instanceof search_test4_Element)
		return (((search_test4_Element) o).id == this.id);	   
	    else
		return false;	   
	}
    }
}
