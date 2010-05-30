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
 * Imported by CG 20090322 based on Apache Harmony ("enhanced") revision 724313.
 */

package java.util.jar;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.harmony.archive.util.Util;

/**
 * JarFile is used to read jar entries and their associated data from jar files.
 * 
 * @see JarInputStream
 * @see JarEntry
 */
public class JarFile extends ZipFile {

    public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF"; //$NON-NLS-1$

    static final String META_DIR = "META-INF/"; //$NON-NLS-1$

    private Manifest manifest;

    private ZipEntry manifestEntry;

    JarVerifier verifier;

    static final class JarFileInputStream extends FilterInputStream {
        private long count;

        private ZipEntry zipEntry;

        private JarVerifier.VerifierEntry entry;

        JarFileInputStream(InputStream is, ZipEntry ze,
                JarVerifier.VerifierEntry e) {
            super(is);
            zipEntry = ze;
            count = zipEntry.getSize();
            entry = e;
        }

        public int read() throws IOException {
            if (count > 0) {
                int r = super.read();
                if (r != -1) {
                    entry.write(r);
                    count--;
                } else {
                    count = 0;
                }
                if (count == 0) {
                    entry.verify();
                }
                return r;
            } else {
                return -1;
            }
        }

        public int read(byte[] buf, int off, int nbytes) throws IOException {
            if (count > 0) {
                int r = super.read(buf, off, nbytes);
                if (r != -1) {
                    int size = r;
                    if (count < size) {
                        size = (int) count;
                    }
                    entry.write(buf, off, size);
                    count -= size;
                } else {
                    count = 0;
                }
                if (count == 0) {
                    entry.verify();
                }
                return r;
            } else {
                return -1;
            }
        }

        public long skip(long nbytes) throws IOException {
            long cnt = 0, rem = 0;
            byte[] buf = new byte[4096];
            while (cnt < nbytes) {
                int x = read(buf, 0,
                        (rem = nbytes - cnt) > buf.length ? buf.length
                                : (int) rem);
                if (x == -1) {
                    return cnt;
                }
                cnt += x;
            }
            return cnt;
        }
    }

    /**
     * Create a new JarFile using the contents of file.
     * 
     * @param file
     *            java.io.File
     * @exception java.io.IOException
     *                If the file cannot be read.
     */
    public JarFile(File file) throws IOException {
        this(file, true);
    }

    /**
     * Create a new JarFile using the contents of file.
     * 
     * @param file
     *            java.io.File
     * @param verify
     *            verify a signed jar file
     * @exception java.io.IOException
     *                If the file cannot be read.
     */
    public JarFile(File file, boolean verify) throws IOException {
        super(file);
        if (verify) {
            verifier = new JarVerifier(file.getPath());
        }
        readMetaEntries();
    }

    /**
     * Create a new JarFile using the contents of file.
     * 
     * @param file
     *            java.io.File
     * @param verify
     *            verify a signed jar file
     * @param mode
     *            the mode to use, either OPEN_READ or OPEN_READ | OPEN_DELETE
     * @exception java.io.IOException
     *                If the file cannot be read.
     */
    public JarFile(File file, boolean verify, int mode) throws IOException {
        super(file, mode);
        if (verify) {
            verifier = new JarVerifier(file.getPath());
        }
        readMetaEntries();
    }

    /**
     * Create a new JarFile from the contents of the file specified by filename.
     * 
     * @param filename
     *            java.lang.String
     * @exception java.io.IOException
     *                If fileName cannot be opened for reading.
     */
    public JarFile(String filename) throws IOException {
        this(filename, true);

    }

    /**
     * Create a new JarFile from the contents of the file specified by filename.
     * 
     * @param filename
     *            java.lang.String
     * @param verify
     *            verify a signed jar file
     * @exception java.io.IOException
     *                If fileName cannot be opened for reading.
     */
    public JarFile(String filename, boolean verify) throws IOException {
        super(filename);
        if (verify) {
            verifier = new JarVerifier(filename);
        }
        readMetaEntries();
    }

    /**
     * Return an enumeration containing the JarEntrys contained in this JarFile.
     * 
     * @return java.util.Enumeration
     * @exception java.lang.IllegalStateException
     *                If this JarFile has been closed.
     */
    public Enumeration entries() {
        class JarFileEnumerator implements Enumeration {
            Enumeration ze;

            JarFile jf;

            JarFileEnumerator(Enumeration zenum, JarFile jf) {
                ze = zenum;
                this.jf = jf;
            }

            public boolean hasMoreElements() {
                return ze.hasMoreElements();
            }

            public Object nextElement() {
                JarEntry je = new JarEntry((ZipEntry)ze.nextElement());
                je.parentJar = jf;
                je.verifier = jf.verifier;
                return je;
            }
        }
        return new JarFileEnumerator(super.entries(), this);
    }

    /**
     * Return the JarEntry specified by name or null if no such entry exists.
     * 
     * @param name
     *            the name of the entry in the jar file
     * @return java.util.jar.JarEntry
     */
    public JarEntry getJarEntry(String name) {
        return (JarEntry) getEntry(name);
    }

    /**
     * Returns the Manifest object associated with this JarFile or null if no
     * manifest entry exists.
     * 
     * @return java.util.jar.Manifest
     */
    public Manifest getManifest() throws IOException {
        if (manifest != null) {
            return manifest;
        }
        try {
            InputStream is = super.getInputStream(manifestEntry);
            if (verifier != null) {
                byte[] buf = new byte[is.available()];
                is.mark(buf.length);
                is.read(buf, 0, buf.length);
                is.reset();
                verifier.addMetaEntry(manifestEntry.getName(), buf);
            }
            try {
                manifest = new Manifest(is, verifier != null);
            } finally {
                is.close();
            }
            manifestEntry = null;
        } catch (NullPointerException e) {
            manifestEntry = null;
        }
        return manifest;
    }

    private void readMetaEntries() throws IOException {
    /*[CG 20090322]
      The Harmony code used a native method to get a list of entries in the META-INF/ directory.
      This code relies on the HY_ZIP_API or the Harmony porting library, which in our case we
      have not got; so for now we simply iterate over the entries and take only those which
      start with "META_INF/".
    */
        // WAS: ZipEntry[] metaEntries = getMetaEntriesImpl(null);
        int dirLength = META_DIR.length();

        boolean signed = false;

        // WAS: if (null != metaEntries) {
            // WAS: for (ZipEntry entry : metaEntries) {
        Enumeration e = entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            String entryName = entry.getName();
            if (entryName.startsWith(META_DIR)) {
                if (manifestEntry == null
                        && manifest == null
                        && Util.ASCIIIgnoreCaseRegionMatches(entryName,
                                dirLength, MANIFEST_NAME, dirLength,
                                MANIFEST_NAME.length() - dirLength)) {
                    manifestEntry = entry;
                    if (verifier == null) {
                        break;
                    }
                } else if (verifier != null
                        && entryName.length() > dirLength
                        && (Util.ASCIIIgnoreCaseRegionMatches(entryName,
                                entryName.length() - 3, ".SF", 0, 3) 
                                || Util.ASCIIIgnoreCaseRegionMatches(entryName,
                                        entryName.length() - 4, ".DSA", 0, 4)
                        || Util.ASCIIIgnoreCaseRegionMatches(entryName,
                                entryName.length() - 4, ".RSA", 0, 4))) {
                    signed = true;
                    InputStream is = super.getInputStream(entry);
                    byte[] buf = new byte[is.available()];
                    try {
                        is.read(buf, 0, buf.length);
                    } finally {
                        is.close();
                    }
                    verifier.addMetaEntry(entryName, buf);
                }
            }
        }
        if (!signed) {
            verifier = null;
        }
    }

    /**
     * Return an InputStream for reading the decompressed contents of ze.
     * 
     * @param ze
     *            the ZipEntry to read from
     * @return java.io.InputStream
     * @exception java.io.IOException
     *                If an error occurred while creating the InputStream.
     */
    public InputStream getInputStream(ZipEntry ze) throws IOException {
        if (manifestEntry != null) {
            getManifest();
        }
        if (verifier != null) {
            verifier.setManifest(getManifest());
            if (manifest != null) {
                verifier.mainAttributesEnd = manifest.getMainAttributesEnd();
            }
            if (verifier.readCertificates()) {
                verifier.removeMetaEntries();
                if (manifest != null) {
                    manifest.removeChunks();
                }
                if (!verifier.isSignedJar()) {
                    verifier = null;
                }
            }
        }
        InputStream in = super.getInputStream(ze);
        if (in == null) {
            return null;
        }
        if (verifier == null || ze.getSize() == -1) {
            return in;
        }
        JarVerifier.VerifierEntry entry = verifier.initEntry(ze.getName());
        if (entry == null) {
            return in;
        }
        return new JarFileInputStream(in, ze, entry);
    }

    /**
     * Return the JarEntry specified by name or null if no such entry exists
     * 
     * @param name
     *            the name of the entry in the jar file
     * @return java.util.jar.JarEntry
     */
    public ZipEntry getEntry(String name) {
        ZipEntry ze = super.getEntry(name);
        if (ze == null) {
            return ze;
        }
        JarEntry je = new JarEntry(ze);
        je.parentJar = this;
        return je;
    }

}

