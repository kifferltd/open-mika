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
import java.util.ArrayList;

/**
 * Describes a group of events that a java bean fires
 */
public class EventSetDescriptor extends FeatureDescriptor
{
    protected Class sourceClass;
    protected Class listenerType;
    protected Method[] listenerMethods;
    protected Method addListenerMethod;
    protected Method removeListenerMethod;
    protected Method getListenerMethod;
    protected boolean unicast;
    protected boolean inDefaultEventSet;
    
    /**
     * Create an EventSetDescriptor using the classes and string names
     */
    public EventSetDescriptor(Class sourceClass, String eventSetName, Class listenerType, String listenerMethodName) throws IntrospectionException
    {
        super();
        name = eventSetName;
        this.sourceClass = sourceClass;
        this.listenerType = listenerType;
        String capName = eventSetName;
        if (Character.isLowerCase(capName.charAt(0)))
            capName = Character.toUpperCase(capName.charAt(0)) + capName.substring(1);
        setAddRemoveMethods("add"+capName+"Listener", "remove"+capName+"Listener");
        setGetMethod("get"+capName+"Listeners");
        String[] listenerMethodNames = {listenerMethodName};
        setListenerMethods(capName, listenerMethodNames);
        unicast = false;
        inDefaultEventSet = true;
    }
    
    /**
     * Create an EventSetDescriptor using the classes and string names
     */
    public EventSetDescriptor(Class sourceClass, String eventSetName, Class listenerType, String[] listenerMethodNames, String addListenerMethodName, String removeListenerMethodName) throws IntrospectionException
    {
        super();
        name = eventSetName;
        this.sourceClass = sourceClass;
        this.listenerType = listenerType;
        setAddRemoveMethods(addListenerMethodName, removeListenerMethodName);
        String capName = eventSetName;
        if (Character.isLowerCase(capName.charAt(0)))
            capName = Character.toUpperCase(capName.charAt(0)) + capName.substring(1);
        setGetMethod("get"+capName+"Listeners");
        setListenerMethods(capName, listenerMethodNames);
        unicast = false;
        inDefaultEventSet = true;
    }
    
    /**
     * Create an EventSetDescriptor using the classes and string names
     */
    public EventSetDescriptor(Class sourceClass, String eventSetName, Class listenerType, String[] listenerMethodNames, String addListenerMethodName, String removeListenerMethodName, String getListenerMethodName) throws IntrospectionException
    {
        super();
        name = eventSetName;
        this.sourceClass = sourceClass;
        this.listenerType = listenerType;
        setAddRemoveMethods(addListenerMethodName, removeListenerMethodName);
        setGetMethod(getListenerMethodName);
        String capName = eventSetName;
        if (Character.isLowerCase(capName.charAt(0)))
            capName = Character.toUpperCase(capName.charAt(0)) + capName.substring(1);
        setListenerMethods(capName, listenerMethodNames);
        unicast = false;
        inDefaultEventSet = true;
    }
    
    /**
     * Create an EventSetDescriptor using the classes and methods
     */
    public EventSetDescriptor(String eventSetName, Class listenerType, Method[] listenerMethods, Method addListenerMethod, Method removeListenerMethod) throws IntrospectionException
    {
        super();
        name = eventSetName;
        this.sourceClass = null;
        this.listenerType = listenerType;
        this.listenerMethods = listenerMethods;
        this.addListenerMethod = addListenerMethod;
        this.removeListenerMethod = removeListenerMethod;
        getListenerMethod = null;
        unicast = false;
        inDefaultEventSet = true;
    }
    
    /**
     * Create an EventSetDescriptor using the classes and methods
     */
    public EventSetDescriptor(String eventSetName, Class listenerType, Method[] listenerMethods, Method addListenerMethod, Method removeListenerMethod, Method getListenerMethod) throws IntrospectionException
    {
        super();
        name = eventSetName;
        this.sourceClass = null;
        this.listenerType = listenerType;
        this.listenerMethods = listenerMethods;
        this.addListenerMethod = addListenerMethod;
        this.removeListenerMethod = removeListenerMethod;
        this.getListenerMethod = getListenerMethod;
        unicast = false;
        inDefaultEventSet = true;
    }
    
    /**
     * Setup the listener methods given the names
     */
    private void setListenerMethods(String eventName, String[] listenerMethodNames) throws IntrospectionException
    {
        String eventClass = eventName + "Event";
        // get all methods with a single param of type eventClass
        Method[] methods = listenerType.getMethods();
        ArrayList eventMethods = new ArrayList();
        for (int i=0; i< methods.length; i++)
        {
            Class[] params = methods[i].getParameterTypes();
            //Class retType = methods[i].getReturnType();
            if (params.length == 1)
            {
                String paramName = params[0].getName();
                if (paramName.equals(eventClass) || paramName.endsWith("."+eventClass))
                {
                    // check if it has been specified in the list
                    if (nameSpecified(listenerMethodNames, methods[i].getName()))
                        eventMethods.add(methods[i]);
                }
            }
        }
        // create listenerMethods array from this in eventMethod list
        listenerMethods = new Method[eventMethods.size()];
        for (int i=0; i<listenerMethods.length; i++)
            listenerMethods[i] = (Method) eventMethods.get(i);
    }
    
    /**
     * Returns true if the specified method name is in the array
     */
    private boolean nameSpecified(String[] methodNameList, String methodName)
    {
        boolean found = false;
        for (int i=0; i<methodNameList.length && !found; i++)
            found = methodName.equals(methodNameList[i]);
        return found;
    }
    
    /**
     * Setup the add/remove methods given the names
     */
    private void setAddRemoveMethods(String addMethodName, String removeMethodName) throws IntrospectionException
    {
        try
        {
            Class[] args = {listenerType};
            addListenerMethod = sourceClass.getMethod(addMethodName, args);
            removeListenerMethod = sourceClass.getMethod(removeMethodName, args);
        }
        catch (NoSuchMethodException e)
        {
            throw new IntrospectionException(e.getMessage());
        }
    }
    
    /**
     * Setup the get listener method given the name
     */
    private void setGetMethod(String getMethodName)
    {
        try
        {
            getListenerMethod = sourceClass.getMethod(getMethodName, null);
        }
        catch (NoSuchMethodException e)
        {
            // can be null as it was only added in 1.4
            getListenerMethod = null;
        }
    }
    
    /**
     * Returns the listener type
     */
    public Class getListenerType()
    {
        return listenerType;
    }
    
    /**
     * Returns the listener methods
     */
    public Method[] getListenerMethods()
    {
        return listenerMethods;
    }
    
    /**
     * Returns MethodDescriptors for the listener Methods
     */
    public MethodDescriptor[] getListenerMethodDescriptors()
    {
        MethodDescriptor[] descriptors = new MethodDescriptor[listenerMethods.length];
        for (int i=0; i<descriptors.length; i++)
            descriptors[i] = new MethodDescriptor(listenerMethods[i]);
        return descriptors;
    }
    
    /**
     * Get the addListener method
     */
    public Method getAddListenerMethod()
    {
        return addListenerMethod;
    }
    
    /**
     * Get the removeListener method
     */
    public Method getRemoveListenerMethod()
    {
        return removeListenerMethod;
    }
    
    /**
     * Get the getListener method
     */
    public Method getGetListenerMethod()
    {
        return getListenerMethod;
    }
    
    /**
     * Sets event sources to be unicast
     */
    public void setUnicast(boolean unicast)
    {
        this.unicast = unicast;
    }
    
    /**
     * Returns the value of unicast
     */
    public boolean isUnicast()
    {
        return unicast;
    }
    
    /**
     * Marks whether the event is in the default set
     */
    public void setInDefaultEventSet(boolean inDefaultEventSet)
    {
        this.inDefaultEventSet = inDefaultEventSet;
    }
    
    /**
     * Returns the value of inDefaultEventSet
     */
    public boolean isInDefaultEventSet()
    {
        return inDefaultEventSet;
    }
}
