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
 * Implementation of VetoableChangeSupport
 *
 * author Mark Anderson
 */

package java.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

public class VetoableChangeSupport implements Serializable
{

    private Object sourceBean;
    private HashMap namedListeners;
    private ArrayList globalListeners;
    
    public VetoableChangeSupport(Object sourceBean)
    {
	this.sourceBean = sourceBean;
	namedListeners = new HashMap();
	globalListeners = new ArrayList();
    }

    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
	globalListeners.add(listener);
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
	Iterator i = globalListeners.iterator();
	while (i.hasNext())
	    {
		VetoableChangeListener l = (VetoableChangeListener) i.next();
		if (l.equals(listener))
		    i.remove();
	    }  
    }

    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener)
    {
	if (hasListeners(propertyName))
	    {
		ArrayList list = (ArrayList) namedListeners.get(propertyName);
		list.add(listener);
	    }
	else
	    {
		ArrayList list = new ArrayList();
		list.add(listener);
		namedListeners.put(propertyName, list);
	    }
    }

    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener)
    {
	if (hasListeners(propertyName))
	    {
		ArrayList list = (ArrayList) namedListeners.get(propertyName);
		Iterator i = list.iterator();
		while (i.hasNext())
		    {
			VetoableChangeListener l = (VetoableChangeListener) i.next();
			if (l.equals(listener))
			    i.remove();
		    }
	    }
    }
    
    public void fireVetoableChange(PropertyChangeEvent evt) throws PropertyVetoException
    {
	Iterator i = globalListeners.iterator();
	while (i.hasNext())
	    ((VetoableChangeListener) i.next()).vetoableChange(evt);
	i = namedListeners.values().iterator();
	while (i.hasNext())
	    {
		ArrayList list = (ArrayList) i.next();
    Iterator i2 = list.iterator();
		while (i2.hasNext())
		   ((VetoableChangeListener) i2.next()).vetoableChange(evt);
	    }
    }

    public void fireVetoableChange(String propertyName, int oldValue, int newValue) throws PropertyVetoException
    {
	fireVetoableChange(propertyName, new Integer(oldValue), new Integer(newValue));
    }

    public void fireVetoableChange(String propertyName, boolean oldValue, boolean newValue) throws PropertyVetoException
    {
	fireVetoableChange(propertyName, new Boolean(oldValue), new Boolean(newValue));
    }

    public void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException
    {
	if (oldValue != null && newValue != null && oldValue.equals(newValue))
	    return; 
	else if (!hasListeners(propertyName))
	    return;
	else
	    {
		PropertyChangeEvent evt = new PropertyChangeEvent(sourceBean, propertyName, oldValue, newValue);
                ArrayList list = (ArrayList) namedListeners.get(propertyName);
		Iterator i = list.iterator();
		while (i.hasNext())
		    ((VetoableChangeListener) i.next()).vetoableChange(evt);
	    }
    }

    public boolean hasListeners(String propertyName)
    {
	return namedListeners.containsKey(propertyName);
    }
}
