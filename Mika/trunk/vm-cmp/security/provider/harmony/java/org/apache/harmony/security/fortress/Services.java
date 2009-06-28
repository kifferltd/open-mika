/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package org.apache.harmony.security.fortress;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.harmony.security.Util;


/**
 * This class contains information about all registered providers and preferred
 * implementations for all "serviceName.algName".
 * 
 */

public class Services {

    // The HashMap that contains information about preferred implementations for
    // all serviceName.algName in the registered providers
    private static final Map services = new HashMap(512);

    // Need refresh flag
    private static boolean needRefresh; // = false;

    /**
     * Refresh number
     */
    static int refreshNumber = 1;

    // Registered providers
    private static final List providers = new ArrayList(20);

    // Hash for quick provider access by name
    private static final Map providersNames = new HashMap(20);

    static {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                loadProviders();
                return null;
            }
        });
    }

    // Load statically registered providers and init Services Info
    private static void loadProviders() {
        String providerClassName = null;
        int i = 1;
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        Provider p;

        while ((providerClassName = Security.getProperty("security.provider." 
                + i++)) != null) {
            try {
                p = (Provider) Class
                        .forName(providerClassName.trim(), true, cl)
                        .newInstance();
                providers.add(p);
                providersNames.put(p.getName(), p);
                initServiceInfo(p);
            } catch (ClassNotFoundException e) { // ignore Exceptions
            } catch (IllegalAccessException e) {
			} catch (InstantiationException e) {
			}
        }
        Engine.door.renumProviders();
    }

    /**
     * Returns registered providers
     * 
     * @return
     */
    public static Provider[] getProviders() {
        return (Provider[])providers.toArray(new Provider[providers.size()]);
    }

    /**
     * Returns registered providers as List
     * 
     * @return
     */
    public static List getProvidersList() {
        return new ArrayList(providers);
    }

    /**
     * Returns the provider with the specified name
     * 
     * @param name
     * @return
     */
    public static Provider getProvider(String name) {
        if (name == null) {
            return null;
        }
        return (Provider)providersNames.get(name);
    }

    /**
     * Inserts a provider at a specified position
     * 
     * @param provider
     * @param position
     * @return
     */
    public static int insertProviderAt(Provider provider, int position) {
        int size = providers.size();
        if ((position < 1) || (position > size)) {
            position = size + 1;
        }
        providers.add(position - 1, provider);
        providersNames.put(provider.getName(), provider);
        setNeedRefresh();
        return position;
    }

    /**
     * Removes the provider
     * 
     * @param providerNumber
     */
    public static void removeProvider(int providerNumber) {
        Provider p = (Provider)providers.remove(providerNumber - 1);
        providersNames.remove(p.getName());
        setNeedRefresh();
    }

    /**
     * 
     * Adds information about provider services into HashMap.
     * 
     * @param p
     */
    public static void initServiceInfo(Provider p) {
        Provider.Service serv;
        String key;
        String type;
        String alias;
        StringBuffer sb = new StringBuffer(128);

        for (Iterator it1 = p.getServices().iterator(); it1.hasNext();) {
            serv = (Provider.Service)it1.next();
            type = serv.getType();
            sb.delete(0, sb.length());
            key = sb.append(type).append(".").append( //$NON-NLS-1$
                    Util.toUpperCase(serv.getAlgorithm())).toString();
            if (!services.containsKey(key)) {
                services.put(key, serv);
            }
            for (Iterator it2 = Engine.door.getAliases(serv); it2.hasNext();) {
                alias = (String)it2.next();
                sb.delete(0, sb.length());
                key = sb.append(type).append(".").append(Util.toUpperCase(alias)) //$NON-NLS-1$
                        .toString();
                if (!services.containsKey(key)) {
                    services.put(key, serv);
                }
            }
        }
    }

    /**
     * 
     * Updates services hashtable for all registered providers
     *  
     */
    public static void updateServiceInfo() {
        services.clear();
        for (Iterator it = providers.iterator(); it.hasNext();) {
            initServiceInfo((Provider)it.next());
        }
        needRefresh = false;
    }

    /**
     * Returns true if services contain any provider information  
     * @return
     */
    public static boolean isEmpty() {
        return services.isEmpty();
    }
    
    /**
     * 
     * Returns service description.
     * Call refresh() before.
     * 
     * @param key
     * @return
     */
    public static Provider.Service getService(String key) {
        return (Provider.Service)services.get(key);
    }

    /**
     * Prints Services content  
     */
    // FIXME remove debug function
    public static void printServices() {
        refresh();
        Set s = services.keySet();
        for (Iterator i = s.iterator(); i.hasNext();) {
            String key = (String)i.next();
            System.out.println(key + "=" + services.get(key)); //$NON-NLS-1$
        }
    }

    /**
     * Set flag needRefresh 
     *
     */
    public static void setNeedRefresh() {
        needRefresh = true;
    }

    /**
     * Refresh services info
     *
     */
    public static void refresh() {
        if (needRefresh) {
            refreshNumber++;
            updateServiceInfo();
        }
    }
}

