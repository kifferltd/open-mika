/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Imported by CG 20090319 based on Apache Harmony ("enhanced") revision 642007.
 */

package java.util.jar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.harmony.luni.util.Util;
import org.apache.harmony.luni.util.Base64;
import org.apache.harmony.security.utils.JarUtils;

/**
 * Non-public class used by {@link JarFile} and {@link JarInputStream} to manage
 * the verification of signed jars. <code>JarFile</code> and
 * <code>JarInputStream</code> objects will be expected to have a
 * <code>JarVerifier</code> instance member which can be used to carry out the
 * tasks associated with verifying a signed jar. These tasks would typically
 * include:
 * <ul>
 * <li>verification of all signed signature files
 * <li>confirmation that all signed data was signed only by the party or
 * parties specified in the signature block data
 * <li>verification that the contents of all signature files (i.e.
 * <code>.SF</code> files) agree with the jar entries information found in the
 * jar manifest.
 * </ul>
 */
class JarVerifier {

    private final String jarName;

    private Manifest man;

    private HashMap metaEntries = new HashMap(5);

    private final Hashtable signatures = new Hashtable(5);

    private final Hashtable certificates = new Hashtable(5);

    private final Hashtable verifiedEntries = new Hashtable();

    int mainAttributesEnd;

    /**
     * Stores and a hash and a message digest and verifies that massage digest
     * matches the hash.
     */
    class VerifierEntry extends OutputStream {

        private String name;

        private MessageDigest digest;

        private byte[] hash;

        private Certificate[] certificates;

        VerifierEntry(String name, MessageDigest digest, byte[] hash,
                Certificate[] certificates) {
            this.name = name;
            this.digest = digest;
            this.hash = hash;
            this.certificates = certificates;
        }

        /**
         * Updates a digest with one byte.
         */
        public void write(int value) {
            digest.update((byte) value);
        }

        /**
         * Updates a digest with byte array.
         */
        public void write(byte[] buf, int off, int nbytes) {
            digest.update(buf, off, nbytes);
        }

        /**
         * Verifies that the digests stored in the manifest match the decrypted
         * digests from the .SF file. This indicates the validity of the
         * signing, not the integrity of the file, as it's digest must be
         * calculated and verified when its contents are read.
         * 
         * @throws SecurityException
         *             if the digest value stored in the manifest does <i>not</i>
         *             agree with the decrypted digest as recovered from the
         *             <code>.SF</code> file.
         * @see #initEntry(String)
         */
        void verify() {
            byte[] d = digest.digest();
            if (!MessageDigest.isEqual(d, Base64.decode(hash))) {
                throw new SecurityException("digest does not match: " + 
                        JarFile.MANIFEST_NAME + " " + name + " " + jarName);
            }
            verifiedEntries.put(name, certificates);
        }

    }

    /**
     * Constructs and answers with a new instance of JarVerifier.
     * 
     * @param name
     *            the name of the jar file being verified.
     */
    JarVerifier(String name) {
        jarName = name;
    }

    /**
     * Called for each new jar entry read in from the input stream. This method
     * constructs and returns a new {@link VerifierEntry} which contains the
     * certificates used to sign the entry and its hash value as specified in
     * the jar manifest.
     * 
     * @param name
     *            the name of an entry in a jar file which is <b>not</b> in the
     *            <code>META-INF</code> directory.
     * @return a new instance of {@link VerifierEntry} which can be used by
     *         callers as an {@link OutputStream}.
     */
    VerifierEntry initEntry(String name) {
        // If no manifest is present by the time an entry is found,
        // verification cannot occur. If no signature files have
        // been found, do not verify.
        if (man == null || signatures.size() == 0) {
            return null;
        }

        Attributes attributes = man.getAttributes(name);
        // entry has no digest
        if (attributes == null) {
            return null;
        }

        Vector certs = new Vector();
        Iterator it = signatures.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            HashMap hm = (HashMap)entry.getValue();
            if (hm.get(name) != null) {
                // Found an entry for entry name in .SF file
                String signatureFile = (String)entry.getKey();

                Vector newCerts = getSignerCertificates(signatureFile, certificates);
                Iterator iter = newCerts.iterator();
                while (iter.hasNext()) {
                    certs.add(iter.next());
                }
            }
        }

        // entry is not signed
        if (certs.size() == 0) {
            return null;
        }
        Certificate[] certificatesArray = new Certificate[certs.size()];
        certs.toArray(certificatesArray);

        String algorithms = attributes.getValue("Digest-Algorithms");
        if (algorithms == null) {
            algorithms = "SHA SHA1";
        }
        StringTokenizer tokens = new StringTokenizer(algorithms);
        while (tokens.hasMoreTokens()) {
            String algorithm = tokens.nextToken();
            String hash = attributes.getValue(algorithm + "-Digest");
            if (hash == null) {
                continue;
            }
            byte[] hashBytes;
            try {
                hashBytes = hash.getBytes("ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e.toString());
            }

            try {
                return new VerifierEntry(name, MessageDigest
                        .getInstance(algorithm), hashBytes, certificatesArray);
            } catch (NoSuchAlgorithmException e) {
                // ignored
            }
        }
        return null;
    }

    /**
     * Add a new meta entry to the internal collection of data held on each jar
     * entry in the <code>META-INF</code> directory including the manifest
     * file itself. Files associated with the signing of a jar would also be
     * added to this collection.
     * 
     * @param name
     *            the name of the file located in the <code>META-INF</code>
     *            directory.
     * @param buf
     *            the file bytes for the file called <code>name</code>.
     * @see #removeMetaEntries()
     */
    void addMetaEntry(String name, byte[] buf) {
        metaEntries.put(Util.toASCIIUpperCase(name), buf);
    }

    /**
     * If the associated jar file is signed, check on the validity of all of the
     * known signatures.
     * 
     * @return <code>true</code> if the associated jar is signed and an
     *         internal check verifies the validity of the signature(s).
     *         <code>false</code> if the associated jar file has no entries at
     *         all in its <code>META-INF</code> directory. This situation is
     *         indicative of an invalid jar file.
     *         <p>
     *         Will also return true if the jar file is <i>not</i> signed.
     *         </p>
     * @throws SecurityException
     *             if the jar file is signed and it is determined that a
     *             signature block file contains an invalid signature for the
     *             corresponding signature file.
     */
    synchronized boolean readCertificates() {
        if (metaEntries == null) {
            return false;
        }
        Iterator it = metaEntries.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            if (key.endsWith(".DSA") || key.endsWith(".RSA")) {
                verifyCertificate(key);
                // Check for recursive class load
                if (metaEntries == null) {
                    return false;
                }
                it.remove();
            }
        }
        return true;
    }

    /**
     * @param certFile
     */
    private void verifyCertificate(String certFile) {
        // Found Digital Sig, .SF should already have been read
        String signatureFile = certFile.substring(0, certFile.lastIndexOf('.'))
                + ".SF";
        byte[] sfBytes = (byte[])metaEntries.get(signatureFile);
        if (sfBytes == null) {
            return;
        }

        byte[] manifest = (byte[])metaEntries.get(JarFile.MANIFEST_NAME);
        // Manifest entry is required for any verifications.
        if (manifest == null) {
            return;
        }

        byte[] sBlockBytes = (byte[])metaEntries.get(certFile);
        try {
            Certificate[] signerCertChain = JarUtils.verifySignature(
                    new ByteArrayInputStream(sfBytes),
                    new ByteArrayInputStream(sBlockBytes));
            /*
             * Recursive call in loading security provider related class which
             * is in a signed jar.
             */
            if (null == metaEntries) {
                return;
            }
            if (signerCertChain != null) {
                certificates.put(signatureFile, signerCertChain);
            }
        } catch (IOException e) {
            return;
        } catch (GeneralSecurityException e) {
            throw new SecurityException(jarName + " failed verification of " + signatureFile);
        }

        // Verify manifest hash in .sf file
        Attributes attributes = new Attributes();
        HashMap entries = new HashMap();
        try {
            InitManifest im = new InitManifest(sfBytes, attributes, Attributes.Name.SIGNATURE_VERSION);
            im.initEntries(entries, null);
        } catch (IOException e) {
            return;
        }

        boolean createdBySigntool = false;
        String createdBy = attributes.getValue("Created-By"); //$NON-NLS-1$
        if (createdBy != null) {
            createdBySigntool = createdBy.indexOf("signtool") != -1; //$NON-NLS-1$
        }

        // Use .SF to verify the mainAttributes of the manifest
        // If there is no -Digest-Manifest-Main-Attributes entry in .SF
        // file, such as those created before java 1.5, then we ignore
        // such verification.
        if (mainAttributesEnd > 0 && !createdBySigntool) {
            String digestAttribute = "-Digest-Manifest-Main-Attributes"; //$NON-NLS-1$
            if (!verify(attributes, digestAttribute, manifest, 0,
                    mainAttributesEnd, false, true)) {
                /* [MSG "archive.31", "{0} failed verification of {1}"] */
                throw new SecurityException(jarName + " failed verification of " + signatureFile);
            }
        }

        // Use .SF to verify the whole manifest.
        String digestAttribute = createdBySigntool ? "-Digest" //$NON-NLS-1$
                : "-Digest-Manifest"; //$NON-NLS-1$
        if (!verify(attributes, digestAttribute, manifest, 0, manifest.length,
                false, false)) {
            Iterator it = entries.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                Manifest.Chunk chunk = man.getChunk((String)entry.getKey());
                if (chunk == null) {
                    return;
                }
                if (!verify((Attributes) entry.getValue(), "-Digest", manifest, //$NON-NLS-1$
                        chunk.start, chunk.end, createdBySigntool, false)) {
                  throw new SecurityException("digest does not match: " + 
                            signatureFile + " " + entry.getKey() + " " + jarName);
                }
            }
        }
        metaEntries.put(signatureFile, null);
        signatures.put(signatureFile, entries);
    }

    /**
     * Associate this verifier with the specified {@link Manifest} object.
     * 
     * @param mf
     *            a <code>java.util.jar.Manifest</code> object.
     */
    void setManifest(Manifest mf) {
        man = mf;
    }

    /**
     * Returns a <code>boolean</code> indication of whether or not the
     * associated jar file is signed.
     * 
     * @return <code>true</code> if the jar is signed, <code>false</code>
     *         otherwise.
     */
    boolean isSignedJar() {
        return certificates.size() > 0;
    }

    private boolean verify(Attributes attributes, String entry, byte[] data,
            int start, int end, boolean ignoreSecondEndline, boolean ignorable) {
        String algorithms = attributes.getValue("Digest-Algorithms"); //$NON-NLS-1$
        if (algorithms == null) {
            algorithms = "SHA SHA1"; //$NON-NLS-1$
        }
        StringTokenizer tokens = new StringTokenizer(algorithms);
        while (tokens.hasMoreTokens()) {
            String algorithm = tokens.nextToken();
            String hash = attributes.getValue(algorithm + entry);
            if (hash == null) {
                continue;
            }

            MessageDigest md;
            try {
                md = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                continue;
            }
            if (ignoreSecondEndline && data[end - 1] == '\n'
                    && data[end - 2] == '\n') {
                md.update(data, start, end - 1 - start);
            } else {
                md.update(data, start, end - start);
            }
            byte[] b = md.digest();
            byte[] hashBytes;
            try {
                hashBytes = hash.getBytes("ISO-8859-1"); //$NON-NLS-1$
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e.toString());
            }
            return MessageDigest.isEqual(b, Base64.decode(hashBytes));
        }
        return ignorable;
    }

    /**
     * Returns all of the {@link java.security.cert.Certificate} instances that
     * were used to verify the signature on the jar entry called
     * <code>name</code>.
     * 
     * @param name
     *            the name of a jar entry.
     * @return an array of {@link java.security.cert.Certificate}.
     */
    Certificate[] getCertificates(String name) {
        Certificate[] verifiedCerts = (Certificate[])verifiedEntries.get(name);
        if (verifiedCerts == null) {
            return null;
        }
        return (Certificate[])verifiedCerts.clone();
    }

    /**
     * Remove all entries from the internal collection of data held about each
     * jar entry in the <code>META-INF</code> directory.
     * 
     * @see #addMetaEntry(String, byte[])
     */
    void removeMetaEntries() {
        metaEntries = null;
    }

    /**
     * Returns a <code>Vector</code> of all of the
     * {@link java.security.cert.Certificate}s that are associated with the
     * signing of the named signature file.
     * 
     * @param signatureFileName
     *            the name of a signature file
     * @param certificates
     *            a <code>Map</code> of all of the certificate chains
     *            discovered so far while attempting to verify the jar that
     *            contains the signature file <code>signatureFileName</code>.
     *            This object will have been previously set in the course of one
     *            or more calls to
     *            {@link #verifyJarSignatureFile(String, String, String, Map, Map)}
     *            where it was passed in as the last argument.
     * @return all of the <code>Certificate</code> entries for the signer of
     *         the jar whose actions led to the creation of the named signature
     *         file.
     */
    public static Vector getSignerCertificates(
            String signatureFileName, Map certificates) {
        Vector result = new Vector();
        Certificate[] certChain = (Certificate[])certificates.get(signatureFileName);
        if (certChain != null) {
            for (int i = 0; i < certChain.length; ++i) {
                Certificate element = certChain[i];
                result.add(element);
            }
        }
        return result;
    }
}

