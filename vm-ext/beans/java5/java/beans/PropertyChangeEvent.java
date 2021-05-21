/**************************************************************************
* Copyright (c) 2003 by Acunia N.V. All rights reserved.                  *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
 * Implementation of PropertyChangeEvent for pmx-jrt-wonka
 *
 * author Mark Anderson
 */

package java.beans;

import java.util.EventObject;

public class PropertyChangeEvent extends EventObject
{

    private String propertyName;
    private Object oldValue;
    private Object newValue;
    private Object propagationId;

    public PropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue)
    {
	super(source);
	this.propertyName = propertyName;
	this.oldValue = oldValue;
	this.newValue = newValue;
	this.propagationId = null;
    }

    public Object getNewValue()
    {
	return newValue;
    }

    public Object getOldValue()
    {
	return oldValue;
    }

    public Object getPropagationId()
    {
	return propagationId;
    }

    public void setPropagationId(Object propagationId)
    {
	this.propagationId = propagationId;
    }

    public String getPropertyName()
    {
	return propertyName;
    }
}
