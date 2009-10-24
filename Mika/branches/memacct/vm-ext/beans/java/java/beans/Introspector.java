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
import java.util.HashMap;
import java.util.Iterator;

import com.acunia.wonka.beans.BeanInfoImpl;

/**
 * Used to learn about the properties, events and methods supported by a Java Bean
 *
 * TODO: Implement caching
 *       Implement getBeanInfo(beanClass, stopClass) and getBeanInfo(beanClass, args)
 *       Support indexed properties
 */
public class Introspector
{
    public static final int USE_ALL_BEANINFO = 1;
    public static final int IGNORE_IMMEDIATE_BEANINFO = 2;
    public static final int IGNORE_ALL_BEANINFO = 3;
    
    private static String[] beanInfoSearchPath = {"com.acunia.wonka.java.beans.info"};
    
    /**
     * Utility method to take a string and convert it to Java capitalization
     */
    public static String decapitalize(String name)
    {
        // 1st case, name already starts with lower case
        if (Character.isLowerCase(name.charAt(0)))
            return name;
        else
        {
            // 2nd case, more than one char, 1st and 2nd uppercase
            if (name.length() > 1 && Character.isUpperCase(name.charAt(1)))
                return name;
            else
            {
                // normal case, change 1st char to lower case
                return Character.toLowerCase(name.charAt(0)) + name.substring(1);
            }
        }
    }
    
    /**
     * Sets the array of package names which are search for BeanInfo classes
     */
    public static void setBeanInfoSearchPath(String[] path)
    {
        beanInfoSearchPath = path;
    }
    
    /**
     * Gets the array of package names which are searched for BeanInfo classes
     */
    public static String[] getBeanInfoSearchPath()
    {
        return beanInfoSearchPath;
    }
    
    /**
     * Flushes the Introspector cache
     */
    public static void flushCaches()
    {
        // TODO: Implement caching
    }
    
    /**
     * Flush the specified class from the Introspector cache
     */
    public static void flushFromCaches(Class clz)
    {
        //TODO: Implement caching
    }
    
    /**
     * Introspect the JavaBean
     */
    public static BeanInfo getBeanInfo(Class beanClass) throws IntrospectionException
    {
        // STEP 1 - look for BeanInfo in beanClass package
        String beanInfoClassName = beanClass.getName() + "BeanInfo";
        String beanClassPackage = beanClass.getPackage().getName();
        Object object = getBeanInfoInstance(beanClassPackage + "." + beanInfoClassName);
        if (object != null)
            return (BeanInfo) object;
        // STEP 2 - look for BeanInfo on search path
        if (beanInfoSearchPath != null)
        {
            for (int i=0; i<beanInfoSearchPath.length; i++)
            {
                object = getBeanInfoInstance(beanInfoSearchPath[i] + "." + beanInfoClassName);
                if (object != null)
                    return (BeanInfo) object;
            }
        }
        // STEP 3 - Introspect and return a default BeanInfo
        BeanDescriptor beanDescriptor = new BeanDescriptor(beanClass);
        EventSetDescriptor[] eventSetDescriptors = getEventSetDescriptors(beanClass);
        MethodDescriptor[] methodDescriptors = getMethodDescriptors(beanClass);
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(beanClass);
        // get the superclass BeanInfo
        BeanInfo[] additionalBeanInfo = null;
        Class superclass = beanClass.getSuperclass();
        if (superclass != null)
        {
            additionalBeanInfo = new BeanInfo[1];
            additionalBeanInfo[0] = getBeanInfo(superclass);
        }
        return new BeanInfoImpl(additionalBeanInfo, beanDescriptor, -1, -1, eventSetDescriptors, null, methodDescriptors, propertyDescriptors);
    }
    
    /**
     * Tries to load the specified class
     */
   private static Object getBeanInfoInstance(String className)
   {
       try
       {
           Class beanInfoClass = Class.forName(className);
           return beanInfoClass.newInstance();
       }
       catch (ClassNotFoundException e)
       {
           return null;
       }
       catch (IllegalAccessException e)
       {
           return null;
       }
       catch (InstantiationException e)
       {
           return null;
       }
   }
   
   /**
    * Creates the EventSetDescriptor array for the bean
    */
   private static EventSetDescriptor[] getEventSetDescriptors(Class beanClass) throws IntrospectionException
   {
       Method[] methods = beanClass.getMethods();
       ArrayList eventList = new ArrayList();
       for (int i=0; i<methods.length; i++)
       {
           // check for an add listener method
           String methodName = methods[i].getName();
           Class[] params = methods[i].getParameterTypes();
           if (validEventListenerParams(params))
           {
               String listenerClassName = params[0].getName();
               if (methodName.equals("add"+listenerClassName))
               {
                   // try to find remove method
                   Method removeMethod = null;
                   for (int j=0; j<methods.length && removeMethod == null; j++)
                   {
                       if (removeMethodMatches(methods[j], listenerClassName, params))
                           removeMethod = methods[j];
                   }
                   // create EventSetDescriptor
                   String eventSetName = listenerClassName.substring(0, listenerClassName.indexOf("Listener"));
                   EventSetDescriptor event = new EventSetDescriptor(eventSetName, params[0], params[0].getMethods(), methods[i], removeMethod);
                   // check if unicast event
                   Class[] addExceptions = methods[i].getExceptionTypes();
                   boolean unicast = false;
                   for (int j=0; j<addExceptions.length && !unicast; j++)
                   {
                       if (addExceptions[j].getName().equals("TooManyListenersException") && addExceptions[j].getPackage().getName().equals("java.util"))
                           unicast = true;
                   }
                   event.setUnicast(unicast);
                   eventList.add(event);
               }
           }
       }
       // convert event list to array
       EventSetDescriptor[] events = new EventSetDescriptor[eventList.size()];
       for (int i=0; i<events.length; i++)
           events[i] = (EventSetDescriptor) eventList.get(i);
       return events;
   }
   
   /**
    * Checks that the parameter of an event method is valid
    */
   private static boolean validEventListenerParams(Class[] params)
   {
       // check only one param
       if (params.length != 1)
           return false;
       // check name ends with listener
       String paramClassName = params[0].getName();
       if (!paramClassName.endsWith("Listener"))
           return false;
       // check we extend java.util.EventListener
       Class[] interfaces = params[0].getInterfaces();
       boolean found = false;
       for (int i=0; i<interfaces.length && !found; i++)
       {
           if (interfaces[i].getName().equals("EventListener") && interfaces[i].getPackage().getName().equals("java.util"))
               found = true;
       }
       return found;
   }
   
   /**
    * Checks if a matching remove method for an event add method has been found
    */
   private static boolean removeMethodMatches(Method method, String listenerClassName, Class[] testParams)
   {
       // check method name
       if (!method.getName().equals("remove"+listenerClassName))
           return false;
       // check method params
       Class[] methodParams = method.getParameterTypes();
       if (methodParams.length != 1 && !methodParams[0].equals(testParams[0]))
           return false;
       return true;
   }
   
   /**
    * Returns the method descriptors for the bean
    */
   private static MethodDescriptor[] getMethodDescriptors(Class beanClass)
   {
       Method[] methods = beanClass.getMethods();
       MethodDescriptor[] methodDescriptors = new MethodDescriptor[methods.length];
       for (int i=0; i<methods.length; i++)
           methodDescriptors[i] = new MethodDescriptor(methods[i]);
       return methodDescriptors;
   }
   
   /**
    * Retruns the property descriptors for the beans
    */
   private static PropertyDescriptor[] getPropertyDescriptors(Class beanClass) throws IntrospectionException
   {
       Method[] methods = beanClass.getMethods();
       HashMap properties = new HashMap();
       for (int i=0; i<methods.length; i++)
       {
           // check for get/set/is method
           String methodName = methods[i].getName();
           if (methodName.startsWith("get"))
           {
               String propertyName = Introspector.decapitalize(methodName.substring(3));
               // check if we have already recorded this property
               if (properties.containsKey(propertyName))
               {
                   PropertyDescriptor p = (PropertyDescriptor) properties.get(propertyName);
                   p.setReadMethod(methods[i]);
               }
               else
                   properties.put(propertyName, new PropertyDescriptor(propertyName, methods[i], null));
           }
           else if (methodName.startsWith("set"))
           {
               String propertyName = Introspector.decapitalize(methodName.substring(3));
               // check for single param
               Class[] params = methods[i].getParameterTypes();
               if (params.length == 1)
               {
                   // check if we have already recorded this property
                   if (properties.containsKey(propertyName))
                   {
                       PropertyDescriptor p = (PropertyDescriptor) properties.get(propertyName);
                       p.setWriteMethod(methods[i]);
                   }
                   else
                       properties.put(propertyName, new PropertyDescriptor(propertyName, null, methods[i]));
               }
           }
           else if (methodName.startsWith("is"))
           {
               String propertyName = Introspector.decapitalize(methodName.substring(2));
               // check if we have already recorded this property
               if (properties.containsKey(propertyName))
               {
                   PropertyDescriptor p = (PropertyDescriptor) properties.get(propertyName);
                   p.setReadMethod(methods[i]);
               }
               else
                   properties.put(propertyName, new PropertyDescriptor(propertyName, methods[i], null));
           }
       }
       // convert to array
       PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[properties.entrySet().size()];
       Iterator it = properties.values().iterator();
       int i = 0;
       while (it.hasNext())
       {
           propertyDescriptors[i] = (PropertyDescriptor) it.next();
           i++;
       }
       return propertyDescriptors;
   }
}
