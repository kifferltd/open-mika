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
 * Imported by CG 20090628 based on Apache Harmony ("enhanced") revision 769463 .
 * Stripped-down (well, "gutted") version CG 20090628 for vm-cmp/security/none.
 */

package java.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OptionalDataException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.SocketPermission;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code CodeSource} encapsulates the location from where code is loaded and
 * the certificates that were used to verify that code. This information is used
 * by {@code SecureClassLoader} to define protection domains for loaded classes.
 *
 * @see SecureClassLoader
 * @see ProtectionDomain
 */
public class CodeSource implements Serializable {

    private static final long serialVersionUID = 4977541819976013951L;

    // Location of this CodeSource object
    private URL location;

    /**
     * Constructs a new instance of {@code CodeSource} with the specified
     * {@code URL} and the {@code Certificate}s.
     *
     * @param location
     *            the {@code URL} representing the location from where code is
     *            loaded, maybe {@code null}.
     * @param certs
     *            the {@code Certificate} used to verify the code, loaded from
     *            the specified {@code location}, maybe {@code null}.
     */
    public CodeSource(URL location, java.security.cert.Certificate[] certs) {
        this.location = location;
    }

    /**
     * Compares the specified object with this {@code CodeSource} for equality.
     * Returns {@code true} if the specified object is also an instance of
     * {@code CodeSource}, points to the same {@code URL} location and the two
     * code sources encapsulate the same {@code Certificate}s. The order of the
     * {@code Certificate}s is ignored by this method.
     *
     * @param obj
     *            object to be compared for equality with this {@code
     *            CodeSource}.
     * @return {@code true} if the specified object is equal to this {@code
     *         CodeSource}, otherwise {@code false}.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        return false;
    }

    /**
     * @return {@code null}
     */
    public final java.security.cert.Certificate[] getCertificates() {
        return null;
    }

    /**
     * Returns the location of this {@code CodeSource}.
     *
     * @return the location of this {@code CodeSource}, maybe {@code null}.
     */
    public final URL getLocation() {
        return location;
    }

    public int hashCode() {
        return location == null ? 0 : location.hashCode();
    }

    /**
     * @return {@code false}.
     */
    public boolean implies(CodeSource cs) {
        return false;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * this {@code CodeSource} including its location, its certificates and its
     * signers.
     *
     * @return a printable representation for this {@code CodeSource}.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("CodeSource, url=");
        buf.append(location == null ? "<null>" : location.toString());

        return buf.toString();
    }

}

