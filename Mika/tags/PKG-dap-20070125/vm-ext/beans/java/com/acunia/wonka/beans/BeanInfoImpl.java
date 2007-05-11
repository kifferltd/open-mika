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

package com.acunia.wonka.beans;

import java.awt.Image; // issue with no AWT build

import java.beans.BeanInfo;
import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;

/**
 * The default implemnetation of the BeanInfo interface
 */
public class BeanInfoImpl implements BeanInfo
{
    private BeanInfo[] additionalBeanInfo;
    private BeanDescriptor beanDescriptor;
    private int defaultEventIndex;
    private int defaultPropertyIndex;
    private EventSetDescriptor[] eventSetDescriptors;
    private Image icon;
    private MethodDescriptor[] methodDescriptors;
    private PropertyDescriptor[] propertyDescriptors;
    
    /**
     * Create a BeanInfoImpl
     */
    public BeanInfoImpl(BeanInfo[] additionalBeanInfo, BeanDescriptor beanDescriptor, int defaultEventIndex, int defaultPropertyIndex, EventSetDescriptor[] eventSetDescriptors, Image icon, MethodDescriptor[] methodDescriptors, PropertyDescriptor[] propertyDescriptors)
    {
        this.additionalBeanInfo = additionalBeanInfo;
        this.beanDescriptor = beanDescriptor;
        this.defaultEventIndex = defaultEventIndex;
        this.eventSetDescriptors = eventSetDescriptors;
        this.icon = icon;
        this.methodDescriptors = methodDescriptors;
        this.propertyDescriptors = propertyDescriptors;
    }
    
    /**
     * Return a collection of BeanInfo objects that give more information about the bean
     */
    public BeanInfo[] getAdditionalBeanInfo()
    {
        return additionalBeanInfo;
    }
    
    /**
     * Get the BeanDescriptor object
     */
    public BeanDescriptor getBeanDescriptor()
    {
        return beanDescriptor;
    }
    
    /**
     * Get the index of the event most commonly used by humans using this bean
     */
    public int getDefaultEventIndex()
    {
        return defaultEventIndex;
    }
    
    /**
     * Get the index of the property most commonly used by humans using this bean
     */
    public int getDefaultPropertyIndex()
    {
        return defaultPropertyIndex;
    }
    
    /**
     * Get the event set descriptors
     */
    public EventSetDescriptor[] getEventSetDescriptors()
    {
        return eventSetDescriptors;
    }
    
    /**
     * Get the icon assocaited with the bean
     */
    public Image getIcon(int iconKind)
    {
        return icon;
    }
    
    /**
     * Get the method desciptors
     */
    public MethodDescriptor[] getMethodDescriptors()
    {
        return methodDescriptors;
    }
    
    /**
     * Get the property descriptors
     */
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return propertyDescriptors;
    }

}
