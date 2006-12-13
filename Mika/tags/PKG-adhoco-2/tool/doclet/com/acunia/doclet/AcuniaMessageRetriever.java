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
 * @(#)AcuniaMessageRetriever.java	
 */

package com.acunia.doclet;

import com.sun.tools.doclets.*;
import com.sun.javadoc.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.text.MessageFormat;


public class AcuniaMessageRetriever extends MessageRetriever{
    private ResourceBundle messageRB;

    public AcuniaMessageRetriever(String resourcelocation) {
	super("com.sun.tools.javadoc.resources.standard");
	try {
	    messageRB = ResourceBundle.getBundle(resourcelocation);
	} catch (MissingResourceException e) {
            throw new Error("Fatal: Resource for javadoc doclets is missing: "+
                             resourcelocation);
	}
    }

    public AcuniaMessageRetriever(ResourceBundle rb) {
        super(rb);
        messageRB = rb;
    }

/**
** 	the getText method will first look in the local messageRB.  If no entry found
**	we will use the messageRB from the standard javadoc ...
**
*/
    public String getText(String key, String a1, String a2, String a3) {
	try {
	    String message = messageRB.getString(key);
	    String[] args = new String[3];
	    args[0] = a1;
	    args[1] = a2;
	    args[2] = a3;
	    return MessageFormat.format(message, args);
	} catch (MissingResourceException e) {
		return super.getText(key, a1, a2, a3);
	}
    }
    
}
