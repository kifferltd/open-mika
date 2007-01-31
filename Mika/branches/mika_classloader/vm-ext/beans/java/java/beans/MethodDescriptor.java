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

import java.lang.reflect.Method;

/**
 * Describes a particular Java Bean method
 */
public class MethodDescriptor extends FeatureDescriptor
{
    private Method method;
    private ParameterDescriptor[] parameterDescriptors;
    
    /**
     * Create a MethodDescriptor from the specified method
     */
    public MethodDescriptor(Method method)
    {
        super();
        name = method.getName();
        this.method = method;
        parameterDescriptors = null;
    }
    
    /**
     * Create a MethodDescriptor from the specified method with descriptions for the params
     */
    public MethodDescriptor(Method method, ParameterDescriptor[] parameterDescriptors)
    {
        super();
        name = method.getName();
        this.method = method;
        this.parameterDescriptors = parameterDescriptors;
    }
    
    /**
     * Returns the method
     */
    public Method getMethod()
    {
        return method;
    }
    
    /**
     * Returns the ParameterDescriptors
     */
    public ParameterDescriptor[] getParameterDescriptors()
    {
        return parameterDescriptors;
    }
}
