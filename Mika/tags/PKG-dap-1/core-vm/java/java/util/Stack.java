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
** $Id: Stack.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package java.util;

/**
 * Stack is an implementation of a LIFO (last in, first out) queue on top of a Vector
 */
public class Stack extends Vector {

  private static final long serialVersionUID = 1224463164541339165L;

    /**
     * Push an object on top of the stack
     *
     * @param item The item to push on the stack
     */
    public Object push(Object item) {
	// No need to synchronize this method since Vector.addElement() 
	// is already synchronized.
	addElement(item);
	return item;
    }
  
    /**
     * Return and remove the object at the top of the stack.
     *
     * @return    the object at the top of the stack
     * @exception EmptyStackException if the stack is empty
     */
    public synchronized Object pop()
	throws EmptyStackException
    {
	// It is probably better to make use of the peek() function
	// to retrieve the element so that the Stack handling logic
	// is kept at a single place
	Object result = peek();
       	removeElementAt(elementCount-1);

	return result;
    }
    
    /**
     * Return the element at the top of the stack (without removing it)
     *
     * @return    the object at the top of the stack
     * @exception EmptyStackException if the stack is empty
     */
    public synchronized Object peek()
	throws EmptyStackException
    {
	if (elementCount==0) throw new EmptyStackException();
	return elementAt(elementCount-1);
    }

    /**
     * Return true if the stack is empty
     *
     * @return <code>true</code> if the stack is empty, <code>false</code> otherwise
     */
    public boolean empty() {
	return isEmpty();
    }
  
    /**
     * Return the distance from the top of the stack where the first instance of the 
     * requested object is located. The top of the stack is at position 1. 
     * The equal function is used to locate the object on the stack.
     * This method returns -1 if the object is not found.
     *
     * @param  o the requested object
     * @return the distance of the first instance of the requested object from the 
     *         top of the stack, or -1 if the object is not found
     */
    public synchronized int search(Object o) {
	int i = lastIndexOf(o);
	if (i >= 0)
	    return elementCount - i;
	else
	    return -1;
    }
}
