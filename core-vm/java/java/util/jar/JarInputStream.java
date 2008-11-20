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
 * Imported by CG 20081119 based on Apache Harmony ("enhanced") revision 641928.
 */

package java.util.jar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarInputStream extends ZipInputStream {

  // [CG 20081119] Taken from org.apache.harmony.luni.util.Util
    public static String toASCIIUpperCase(String s) {
    int len = s.length();
    StringBuffer buffer = new StringBuffer(len);
      for (int i = 0; i < len; i++) {
        char c = s.charAt(i);
        if ('a' <= c && c <= 'z') {
          buffer.append((char) (c - ('a' - 'A')));
        } else {
          buffer.append(c);
        }
      }
      return buffer.toString();
    }

    private Manifest manifest;

    private boolean eos = false;

    private JarEntry mEntry;

    private JarEntry jarEntry;

    private boolean isMeta;

    // [CG 20081119] We'll come back to this later
    // private JarVerifier verifier;

    private OutputStream verStream;

    /**
     * Constructs a new JarInputStream from stream
     */
    public JarInputStream(InputStream stream, boolean verify)
            throws IOException {
        super(stream);
    // [CG 20081119] We'll come back to this later
    //    if (verify) {
    //        verifier = new JarVerifier("JarInputStream"); //$NON-NLS-1$
    //    }
        if ((mEntry = getNextJarEntry()) == null) {
            return;
        }
        String name = toASCIIUpperCase(mEntry.getName());
        if (name.equals(JarFile.META_DIR)) {
            mEntry = null; // modifies behavior of getNextJarEntry()
            closeEntry();
            mEntry = getNextJarEntry();
            name = mEntry.getName().toUpperCase();
        }
        if (name.equals(JarFile.MANIFEST_NAME)) {
            mEntry = null;
    // [CG 20081119] We'll worry about verifying later
    //        manifest = new Manifest(this, verify);
            manifest = new Manifest(this);

            closeEntry();
    // [CG 20081119] We'll come back to this later
    //        if (verify) {
    //            verifier.setManifest(manifest);
    //            if (manifest != null) {
    //                verifier.mainAttributesEnd = manifest
    //                        .getMainAttributesEnd();
    //            }
    //        }

        } else {
            Attributes temp = new Attributes(3);
            temp.map.put("hidden", null); //$NON-NLS-1$
            mEntry.setAttributes(temp);
            /*
             * if not from the first entry, we will not get enough
             * information,so no verify will be taken out.
             */
    // [CG 20081119] We'll come back to this later
            // verifier = null;
        }
    }

    public JarInputStream(InputStream stream) throws IOException {
        this(stream, true);
    }

    /**
     * Returns the Manifest object associated with this JarInputStream or null
     * if no manifest entry exists.
     * 
     * @return java.util.jar.Manifest
     */
    public Manifest getManifest() {
        return manifest;
    }

    /**
     * Returns the next JarEntry contained in this stream or null if no more
     * entries are present.
     * 
     * @return java.util.jar.JarEntry
     * @exception java.io.IOException
     *                If an error occurs while reading the entry
     */
    public JarEntry getNextJarEntry() throws IOException {
        return (JarEntry) getNextEntry();
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (mEntry != null) {
            return -1;
        }
        int r = super.read(buffer, offset, length);
        if (verStream != null && !eos) {
            if (r == -1) {
                eos = true;
    // [CG 20081119] We'll come back to this later
    //            if (verifier != null) {
    //                if (isMeta) {
    //                    verifier.addMetaEntry(jarEntry.getName(),
    //                            ((ByteArrayOutputStream) verStream)
    //                                    .toByteArray());
    //                    try {
    //                        verifier.readCertificates();
    //                    } catch (SecurityException e) {
    //                        verifier = null;
    //                        throw e;
    //                    }
    //                } else {
    //                    ((JarVerifier.VerifierEntry) verStream).verify();
    //                }
    //            }
            } else {
                verStream.write(buffer, offset, r);
            }
        }
        return r;
    }

    /**
     * Returns the next ZipEntry contained in this stream or null if no more
     * entries are present.
     * 
     * @return java.util.zip.ZipEntry
     * @exception java.io.IOException
     *                If an error occurs while reading the entry
     */
    public ZipEntry getNextEntry() throws IOException {
        if (mEntry != null) {
            jarEntry = mEntry;
            mEntry = null;
            jarEntry.setAttributes(null);
        } else {
            jarEntry = (JarEntry) super.getNextEntry();
            if (jarEntry == null) {
                return null;
            }
    // [CG 20081119] We'll come back to this later
    //        if (verifier != null) {
    //            isMeta = toASCIIUpperCase(jarEntry.getName()).startsWith(
    //                    JarFile.META_DIR);
    //            if (isMeta) {
    //                verStream = new ByteArrayOutputStream();
    //            } else {
    //                verStream = verifier.initEntry(jarEntry.getName());
    //            }
    //        }
        }
        eos = false;
        return jarEntry;
    }

    protected ZipEntry createZipEntry(String name) {
        JarEntry entry = new JarEntry(name);
        if (manifest != null) {
            entry.setAttributes(manifest.getAttributes(name));
        }
        return entry;
    }
}

