/*
 * $HeadURL:$
 * $Revision:$
 * $Date:$
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.http.client.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A collection of utilities to workaround limitations of Java clone framework.
 */
public class CloneUtils {

    public static Object clone(final Object obj) throws CloneNotSupportedException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Cloneable) {
            Class<?> clazz = obj.getClass ();
            Method m;
            try {
                m = clazz.getMethod("clone", (Class[]) null);
            } catch (NoSuchMethodException ex) {
                throw new NoSuchMethodError(ex.getMessage());
            }
            try {
                return m.invoke(obj, (Object []) null);
            } catch (InvocationTargetException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof CloneNotSupportedException) {
                    throw ((CloneNotSupportedException) cause); 
                } else {
                    throw new Error("Unexpected exception", cause);
                }
            } catch (IllegalAccessException ex) {
                throw new IllegalAccessError(ex.getMessage());
            }
        } else {
            throw new CloneNotSupportedException();
        }
    }
    
    /**
     * This class should not be instantiated.
     */
    private CloneUtils() {
    }

}
