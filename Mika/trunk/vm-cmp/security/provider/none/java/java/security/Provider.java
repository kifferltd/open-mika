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

/*
 * Imported by CG 20090519 based on Apache Harmony ("enhanced") revision 769463 .
 * Modified to implement security checks only if 
 * wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS is true.
 * Stripped-down (well, "gutted") version CG 20090612 for vm-cmp/security/none.
 */

package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * {@code Provider} is the abstract superclass for all security providers in the
 * Java security infrastructure.
 */

public abstract class Provider extends Properties {
    private static final long serialVersionUID = -4298000515446427739L;

  /**
  ** Fields dictated by the Serialized Form ...
  */
    private String name;

    private double version;

    private String info;

    private static void permissionCheck(String permission) {
      if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
          sm.checkSecurityAccess(permission);
        }
      }
    }

    /**
     * Constructs a new instance of {@code Provider} with its name, version and
     * description.
     *
     * @param name
     *            the name of the provider.
     * @param version
     *            the version of the provider.
     * @param info
     *            a description of the provider.
     */
    protected Provider(String name, double version, String info) {
        this.name = name;
        this.version = version;
        this.info = info;
        putProviderInfo();
    }

    /**
     * Returns the name of this provider.
     *
     * @return the name of this provider.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version number for the services being provided.
     *
     * @return the version number for the services being provided.
     */
    public double getVersion() {
        return version;
    }

    /**
     * Returns a description of the services being provided.
     *
     * @return a description of the services being provided.
     */
    public String getInfo() {
        return info;
    }

    /**
     * Returns a string containing a concise, human-readable description of
     * this {@code Provider} including its name and its version.
     *
     * @return a printable representation for this {@code Provider}.
     */
    public String toString() {
        return name + " version: " + version;
    }

    /**
     * Clears all properties used to look up services implemented by this
     * {@code Provider}.
     * <p>
     * If a {@code SecurityManager} is installed, code calling this method needs
     * the {@code SecurityPermission} {@code clearProviderProperties.NAME}
     * (where NAME is the provider name) to be granted, otherwise a {@code
     * SecurityException} will be thrown.
     *
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and the caller does
     *             not have permission to invoke this method.
     */
    public synchronized void clear() {
        permissionCheck("clearProviderProperties." + name);
        super.clear();
        putProviderInfo();
    }

    public synchronized void load(InputStream inStream) throws IOException {
        Properties tmp = new Properties();
        tmp.load(inStream);
        myPutAll(tmp);
    }

    /**
     * Copies all from the provided map to this {@code Provider}.
     * <p>
     * If a {@code SecurityManager} is installed, code calling this method needs
     * the {@code SecurityPermission} {@code putProviderProperty.NAME} (where
     * NAME is the provider name) to be granted, otherwise a {@code
     * SecurityException} will be thrown.
     *
     * @param t
     *            the mappings to copy to this provider.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and the caller does
     *             not have permission to invoke this method.
     */
    public synchronized void putAll(Map t) {

        // Implementation note:
        // checkSecurityAccess method call is NOT specified
        // Do it as in put(Object key, Object value).
        permissionCheck("putProviderProperty."+name);

        myPutAll(t);
    }

    private void myPutAll(Map t) {
        Iterator it = t.entrySet().iterator();
        Object key;
        Object value;
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            key = entry.getKey();
            if (key instanceof String && ((String) key).startsWith("Provider.")) {
                // Provider service type is reserved
                continue;
            }
            value = entry.getValue();
            super.put(key, value);
        }
    }

    public Set entrySet(){
        return Collections.unmodifiableSet(super.entrySet());
    }

    public Set keySet(){
        return Collections.unmodifiableSet(super.keySet());
    }

    public Collection values(){
        return Collections.unmodifiableCollection(super.values());
    }

    /**
     * Maps the specified {@code key} property name to the specified {@code
     * value}.
     * <p>
     * If a {@code SecurityManager} is installed, code calling this method needs
     * the {@code SecurityPermission} {@code putProviderProperty.NAME} (where
     * NAME is the provider name) to be granted, otherwise a {@code
     * SecurityException} will be thrown.
     *
     * @param key
     *            the name of the property.
     * @param value
     *            the value of the property.
     * @return the value that was previously mapped to the specified {@code key}
     *         ,or {@code null} if it did not have one.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and the caller does
     *             not have permission to invoke this method.
     */
    public synchronized Object put(Object key, Object value) {
        permissionCheck("putProviderProperty."+name);
        if (key instanceof String && ((String) key).startsWith("Provider.")) {
            // Provider service type is reserved
            return null;
        }
        return super.put(key, value);
    }

    /**
     * Removes the specified {@code key} and its associated value from this
     * {@code Provider}.
     * <p>
     * If a {@code SecurityManager} is installed, code calling this method needs
     * the {@code SecurityPermission} {@code removeProviderProperty.NAME} (where
     * NAME is the provider name) to be granted, otherwise a {@code
     * SecurityException} will be thrown.
     *
     * @param key
     *            the name of the property
     * @return the value that was mapped to the specified {@code key} ,or
     *         {@code null} if no mapping was present
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and the caller does
     *             not have the permission to invoke this method.
     */
    public synchronized Object remove(Object key) {
        permissionCheck("removeProviderProperty."+name);
        if (key instanceof String && ((String) key).startsWith("Provider.")) {
            // Provider service type is reserved
            return null;
        }
        return super.remove(key);
    }

    // These attributes should be placed in each Provider object: 
    // Provider.id name, Provider.id version, Provider.id info, 
    // Provider.id className
    private void putProviderInfo() {
        super.put("Provider.id name", null != name ? name : "null");
	super.put("Provider.id info", null != info ? info : "null");
        super.put("Provider.id className", this.getClass().getName());
    }

}

