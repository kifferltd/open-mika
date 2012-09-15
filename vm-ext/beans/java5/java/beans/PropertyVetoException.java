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

package java.beans;

import java.io.Serializable;

/**
 * Thrown when a proposed change to a property represents an unaccepatable value
 */
public class PropertyVetoException extends Exception implements Serializable
{
    private PropertyChangeEvent event;
    
    public PropertyVetoException(String mess, PropertyChangeEvent evt)
    {
        super(mess);
        event = evt;
    }
    
    public PropertyChangeEvent getPropertyChangeEvent()
    {
        return event;
    }
}
