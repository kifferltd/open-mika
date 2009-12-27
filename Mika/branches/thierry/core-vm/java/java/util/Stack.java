/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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
