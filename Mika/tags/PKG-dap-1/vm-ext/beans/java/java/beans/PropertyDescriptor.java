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
 * Describes a property with a getter and setter method
 */
public class PropertyDescriptor extends FeatureDescriptor
{
    protected Class beanClass = null;
    protected Method getter = null;
    protected Method setter = null;
    protected boolean bound;
    protected boolean constrained;
    protected Class propertyEditorClass;
    
    /**
     * Create a PropertyDescriptor with the specified name for the specified class
     */
    public PropertyDescriptor(String propertyName, Class beanClass) throws IntrospectionException
    {
        super();
        name = propertyName;
        this.beanClass = beanClass;
        String capName = propertyName;
        if (propertyName == null || propertyName.length() == 0)
            throw new IntrospectionException("Invalid property name");
        if (Character.isLowerCase(capName.charAt(0)))
            capName = Character.toUpperCase(capName.charAt(0)) + capName.substring(1);
        setMethods("get"+capName, "is"+capName, "set"+capName);
        bound = false;
        constrained = false;
        propertyEditorClass = null;
    }
    
    /**
     * Create a PropertyDescriptor with the specified name with the specified getter and setter methods
     * on the specified class
     */
    public PropertyDescriptor(String propertyName, Class beanClass, String getterName, String setterName) throws IntrospectionException
    {
        super();
        name = propertyName;
        this.beanClass = beanClass;
        setMethods(getterName, null, setterName);
        bound = false;
        constrained = false;
        propertyEditorClass = null;
    }
    
    /**
     * Create a PropertyDescriptor with the specified name, class and methods
     */
    public PropertyDescriptor(String propertyName, Method getter, Method setter) throws IntrospectionException
    {
        super();
        name = propertyName;
        this.beanClass = null;
        this.getter = getter;
        this.setter = setter;
        bound = false;
        constrained = false;
        propertyEditorClass = null;
    }
    
    /**
     * Tries to find the specified methods in the beanClass
     */
    private void setMethods(String getterName, String isName, String setterName) throws IntrospectionException
    {
        try
        {
            // try the getter method if not null
            if (getterName !=null)
            {
                try
                {
                   getter = beanClass.getMethod(getterName, null);
                }
                catch (NoSuchMethodException e)
                {
                    // coould fail if we have an is method instead of a get
                    // but if no isName specified then throw exception
                    if (isName == null)
                            throw new IntrospectionException("No getter method");
                }
                // try the is method
                // for boolean properties this is preferred even if their is a get
                if (isName != null)
                {
                    try
                    {
                        getter = beanClass.getMethod(isName, null);
                    }
                    catch (NoSuchMethodException e)
                    {
                        // will fail for non boolean properties
                        // but throw exception if there is not a get
                        if (getter == null)
                            throw new IntrospectionException("No getter method");
                    }
                }
            }
            // find the setter method if not null
            if (setterName != null)
            {
                // if a getter exists, use its return type to find the setter
                // otherwise, search through all methods for the setter
                if (getter != null)
                {
                    Class retType = getter.getReturnType();
                    Class[] args = {retType};
                    setter = beanClass.getMethod(setterName, args);
                }
                else
                {
                    Method[] methods = beanClass.getMethods();
                    boolean found = false;
                    for (int i=0; i < methods.length && !found; i++)
                    {
                        if (methods[i].getName().equals(setterName))
                        {
                            setter = methods[i];
                            found = true;
                        }
                    }
                    // if not found then throw exception
                    if (!found)
                        throw new IntrospectionException("No setter method");
                }
            }
        }
        catch(NoSuchMethodException e) 
        {
            throw new IntrospectionException(e.getMessage());
        }
    }
    
    /**
     * Returns the property type ie. the return type of the getter
     */
    public Class getPropertyType()
    {
        if (getter != null)
            return getter.getReturnType();
        else
            return null;
    }
    
    /**
     * Return the getter method
     */
    public Method getReadMethod()
    { 
        return getter;
    }
    
    /** 
     * Set the getter method
     */
    public void setReadMethod(Method getter) throws IntrospectionException
    {
        this.getter = getter;
    }
    
    /**
     * Return the setter method
     */
    public Method getWriteMethod()
    { 
        return setter;
    }
    
    /** 
     * Set the setter method
     */
    public void setWriteMethod(Method setter) throws IntrospectionException
    {
        this.setter = setter;
    }
    
    /**
     * Returns whether the property is bound
     */
    public boolean isBound()
    {
        return bound;
    }
    
    /**
     * Sets whether the property is bound
     */
    public void setBound(boolean bound)
    {
        this.bound = bound;
    }
    
    /**
     * Returns whether the property is constrained
     */
    public boolean isConstrained()
    {
        return constrained;
    }
    
    /**
     * Sets whether the property is constrained
     */
    public void setConstrained(boolean constrained)
    {
        this.constrained = constrained;
    }
    
    /**
     * Sets the property editor class
     */
    public void setPropertyEditorClass(Class propertyEditorClass)
    {
        this.propertyEditorClass = propertyEditorClass;
    }
    
    /**
     * Gets the property editor class
     */
    public Class getPropertyEditorClass()
    {
        return propertyEditorClass;
    }
    
    /**
     * Returns whether two PropertyDescriptors are equal
     */
    public boolean equals(Object obj)
    {
        PropertyDescriptor pd = (PropertyDescriptor) obj;
        // two property descriptors are equal if the read, write,
        // property types, property editor and flags are equivalent
        if (!this.getReadMethod().equals(pd.getReadMethod()))
            return false;
        if (!this.getWriteMethod().equals(pd.getWriteMethod()))
            return false;
        if (!this.getPropertyType().equals(pd.getPropertyType()))
            return false;
        if (!this.getPropertyEditorClass().equals(pd.getPropertyEditorClass()))
            return false;
        if (this.isConstrained() != pd.isConstrained())
            return false;
        if (this.isBound() != pd.isBound())
            return false;
        // all tests pass so return true
        return true;
    }
}
