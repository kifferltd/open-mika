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

package java.beans;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PropertyChangeSupport implements Serializable {

    private static final long serialVersionUID = 6401253773779951803l;

    private transient List globalListeners = new ArrayList();

    private Hashtable children = new Hashtable();

    private Object source;

    // for serialization compatibility
    private int propertyChangeSupportSerializedDataVersion = 1;

    public PropertyChangeSupport(Object sourceBean) {
        if (sourceBean == null) {
            throw new NullPointerException();
        }
        this.source = sourceBean;
    }

    public void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }

    public void fireIndexedPropertyChange(String propertyName, int index,
            Object oldValue, Object newValue) {

        // nulls and equals check done in doFire...
        doFirePropertyChange(new IndexedPropertyChangeEvent(source,
                propertyName, oldValue, newValue, index));
    }

    public synchronized void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        if ((propertyName != null) && (listener != null)) {
            PropertyChangeSupport listeners = (PropertyChangeSupport) children.get(propertyName);

            if (listeners != null) {
                listeners.removePropertyChangeListener(listener);
            }
        }
    }

    public synchronized void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        if ((listener != null) && (propertyName != null)) {
            PropertyChangeSupport listeners = (PropertyChangeSupport) children.get(propertyName);

            if (listeners == null) {
                listeners = new PropertyChangeSupport(source);
                children.put(propertyName, listeners);
            }

            // RI compatibility
            if (listener instanceof PropertyChangeListenerProxy) {
                PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;

                listeners
                        .addPropertyChangeListener(new PropertyChangeListenerProxy(
                                proxy.getPropertyName(),
                                (PropertyChangeListener) proxy.getListener()));
            } else {
                listeners.addPropertyChangeListener(listener);
            }
        }
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners(
            String propertyName) {
        PropertyChangeSupport listeners = null;

        if (propertyName != null) {
            listeners = (PropertyChangeSupport) children.get(propertyName);
        }

        return (listeners == null) ? new PropertyChangeListener[0]
                : listeners.getPropertyChangeListeners();
    }

    public void firePropertyChange(String propertyName, boolean oldValue,
            boolean newValue) {
        PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }

    public void fireIndexedPropertyChange(String propertyName, int index,
            boolean oldValue, boolean newValue) {

        if (oldValue != newValue) {
            fireIndexedPropertyChange(propertyName, index, Boolean
                    .valueOf(oldValue), Boolean.valueOf(newValue));
        }
    }

    public void firePropertyChange(String propertyName, int oldValue,
            int newValue) {
        PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }

    public void fireIndexedPropertyChange(String propertyName, int index,
            int oldValue, int newValue) {

        if (oldValue != newValue) {
            fireIndexedPropertyChange(propertyName, index,
                    new Integer(oldValue), new Integer(newValue));
        }
    }

    public synchronized boolean hasListeners(String propertyName) {
        if(globalListeners.size() > 0){
            return true;
        }
        boolean result = false;
        if (propertyName != null) {
            PropertyChangeSupport listeners = (PropertyChangeSupport) children.get(propertyName);
            result = (listeners != null && listeners.hasListeners(propertyName));
        }
        return result;
    }

    public synchronized void removePropertyChangeListener(
            PropertyChangeListener listener) {
        if (listener instanceof PropertyChangeListenerProxy) {
            String name = ((PropertyChangeListenerProxy) listener)
                    .getPropertyName();
            PropertyChangeListener lst = (PropertyChangeListener) ((PropertyChangeListenerProxy) listener)
                    .getListener();

            removePropertyChangeListener(name, lst);
        } else {
            globalListeners.remove(listener);
        }
    }

    public synchronized void addPropertyChangeListener(
            PropertyChangeListener listener) {
        if (listener instanceof PropertyChangeListenerProxy) {
            String name = ((PropertyChangeListenerProxy) listener)
                    .getPropertyName();
            PropertyChangeListener lst = (PropertyChangeListener) ((PropertyChangeListenerProxy) listener)
                    .getListener();
            addPropertyChangeListener(name, lst);
        } else if(listener != null){
            globalListeners.add(listener);
        }
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        ArrayList result = new ArrayList(globalListeners);
        Iterator propertyNameIterator = children.keySet().iterator();
        while (propertyNameIterator.hasNext()) {
            final String propertyName = (String) propertyNameIterator.next();
            PropertyChangeSupport namedListener = (PropertyChangeSupport) children
                    .get(propertyName);
            PropertyChangeListener[] listeners = namedListener
                    .getPropertyChangeListeners();
            for (int i = 0; i < listeners.length; i++) {
                result.add(new PropertyChangeListenerProxy(propertyName,
                        listeners[i]));
            }
        }
        return (PropertyChangeListener[]) result.toArray(new PropertyChangeListener[0]);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        PropertyChangeListener[] gListeners = (PropertyChangeListener[]) globalListeners
                .toArray(new PropertyChangeListener[0]);
        for (int i = 0; i < gListeners.length; i++) {
            if (gListeners[i] instanceof Serializable) {
                oos.writeObject(gListeners[i]);
            }
        }
        // Denotes end of list
        oos.writeObject(null);

    }

    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        ois.defaultReadObject();
        this.globalListeners = new LinkedList();
        if (null == this.children) {
            this.children = new Hashtable();
        }
        Object listener = null;
        do {
            // Reads a listener _or_ proxy
            listener = ois.readObject();
            if (listener != null) {
                addPropertyChangeListener((PropertyChangeListener) listener);
            }
        } while (listener != null);
    }

    public void firePropertyChange(PropertyChangeEvent event) {
        doFirePropertyChange(event);
    }

    private PropertyChangeEvent createPropertyChangeEvent(String propertyName,
            Object oldValue, Object newValue) {
        return new PropertyChangeEvent(source, propertyName, oldValue, newValue);
    }

    private PropertyChangeEvent createPropertyChangeEvent(String propertyName,
            boolean oldValue, boolean newValue) {
        return new PropertyChangeEvent(source, propertyName, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
    }

    private PropertyChangeEvent createPropertyChangeEvent(String propertyName,
            int oldValue, int newValue) {
        return new PropertyChangeEvent(source, propertyName, new Integer(oldValue), new Integer(newValue));
    }

    private void doFirePropertyChange(PropertyChangeEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
            return;
        }

        // Collect up the global listeners
        PropertyChangeListener[] gListeners;
        synchronized(this) {
            gListeners = (PropertyChangeListener[]) globalListeners.toArray(new PropertyChangeListener[0]);
        }

        // Fire the events for global listeners
        for (int i = 0; i < gListeners.length; i++) {
            gListeners[i].propertyChange(event);
        }

        // Fire the events for the property specific listeners if any
        if (event.getPropertyName() != null) {
            PropertyChangeSupport namedListener = (PropertyChangeSupport) children
                    .get(event.getPropertyName());
            if (namedListener != null) {
                namedListener.firePropertyChange(event);
            }
        }

    }

}

