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

import java.awt.Image;

/**
 * Defines the interface to BeanInfo objects
 */
public interface BeanInfo
{
    /**
     * Constant for a 16x16 colour icon
     */
    public static final int ICON_COLOR_16x16 = 1;
    
    /**
     * Constant for a 32x32 colour icon
     */
    public static final int ICON_COLOR_32x32 = 2;
    
    /**
     * Constant for a 16x16 mono icon
     */
    public static final int ICON_MONO_16x16 = 3;
    
    /**
     * Constant for a 32x32 mono icon
     */
    public static final int ICON_MONO_32x32 = 4; 
    
    /**
     * Return a collection of BeanInfo objects that give more information about the bean
     */
    public BeanInfo[] getAdditionalBeanInfo();
    
    /**
     * Get the BeanDescriptor object
     */
    public BeanDescriptor getBeanDescriptor();
    
    /**
     * Get the index of the event most commonly used by humans using this bean
     */
    public int getDefaultEventIndex();
    
    /**
     * Get the index of the property most commonly used by humans using this bean
     */
    public int getDefaultPropertyIndex();
    
    /**
     * Get the event set descriptors
     */
    public EventSetDescriptor[] getEventSetDescriptors();
    
    /**
     * Get the icon assocaited with the bean
     */
    public Image getIcon(int iconKind);
    
    /**
     * Get the method desciptors
     */
    public MethodDescriptor[] getMethodDescriptors();
    
    /**
     * Get the property descriptors
     */
    public PropertyDescriptor[] getPropertyDescriptors();
}
