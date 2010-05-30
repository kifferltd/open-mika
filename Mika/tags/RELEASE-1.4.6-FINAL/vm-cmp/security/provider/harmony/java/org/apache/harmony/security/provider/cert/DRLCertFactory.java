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
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package org.apache.harmony.security.provider.cert;

import java.security.AccessController;
import java.security.Provider;

/**
 * Master class (provider) for X509 Certificate Factory
 * Implementation.
 */
public final class DRLCertFactory extends Provider {

    /**
     * @serial
     */
    private static final long serialVersionUID = -7269650779605195879L;

    /**
     * Constructs the instance of the certificate factory provider.
     */
    public DRLCertFactory() {
        // specification of the provider name, version, and description.
        super("DRLCertFactory", 1.0, "Certificate Factory supports CRLs and Certificates in (PEM) ASN.1 DER encoded form, and Certification Paths in PkiPath and PKCS7 formats.");
                
        AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                // register the service
                put("CertificateFactory.X509",
                    "org.apache.harmony.security.provider.cert.X509CertFactoryImpl");
                // mapping the alias
                put("Alg.Alias.CertificateFactory.X.509", "X509");
                    return null;
            }
        });
    }
}


