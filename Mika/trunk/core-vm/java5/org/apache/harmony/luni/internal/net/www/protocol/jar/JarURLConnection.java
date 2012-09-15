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

package org.apache.harmony.luni.internal.net.www.protocol.jar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.apache.harmony.luni.util.Msg;
import org.apache.harmony.luni.util.Util;

/**
 * This subclass extends <code>URLConnection</code>.
 * <p>
 *
 * This class is responsible for connecting and retrieving resources from a Jar
 * file which can be anywhere that can be refered to by an URL.
 */
public class JarURLConnection extends java.net.JarURLConnection {

    static HashMap<URL, JarFile> jarCache = new HashMap<URL, JarFile>();

    private URL jarFileURL;

    private InputStream jarInput;

    private JarFile jarFile;

    private JarEntry jarEntry;

    private boolean closed;


    /**
     * @param url
     *            the URL of the JAR
     * @throws MalformedURLException
     *             if the URL is malformed
     */
    public JarURLConnection(java.net.URL url) throws MalformedURLException, IOException {
        super(url);
        jarFileURL = getJarFileURL();
        jarFileURLConnection = jarFileURL.openConnection();
    }

    /**
     * @see java.net.URLConnection#connect()
     */
    @Override
    public void connect() throws IOException {
        if (!connected) {
            findJarFile(); // ensure the file can be found
            findJarEntry(); // ensure the entry, if any, can be found
            connected = true;
        }
    }

    /**
     * Returns the Jar file refered by this <code>URLConnection</code>
     *
     * @return the JAR file referenced by this connection
     *
     * @throws IOException
     *             thrown if an IO error occurs while connecting to the
     *             resource.
     */
    @Override
    public JarFile getJarFile() throws IOException {
        connect();
        return jarFile;
    }

    /**
     * Returns the Jar file refered by this <code>URLConnection</code>
     *
     * @throws IOException
     *             if an IO error occurs while connecting to the resource.
     */
    private void findJarFile() throws IOException {
        JarFile jar = null;
        if (getUseCaches()) {
            synchronized(jarCache){
                jarFile = jarCache.get(jarFileURL);
            }
            if (jarFile == null) {
                jar = openJarFile();
                synchronized(jarCache){
                    jarFile = jarCache.get(jarFileURL);
                    if (jarFile == null){
                        jarCache.put(jarFileURL, jar);
                        jarFile = jar;
                    }else{
                        jar.close();
                    }
                }
            }
        }else{
            jarFile = openJarFile();
        }

        if (jarFile == null) {
            throw new IOException();
        }
    }

    JarFile openJarFile() throws IOException {
        JarFile jar = null;
        if (jarFileURL.getProtocol().equals("file")) { //$NON-NLS-1$
            jar = new JarFile(new File(Util.decode(jarFileURL.getFile(), false,
                    "UTF-8")), true, ZipFile.OPEN_READ);
        } else {
            final InputStream is = jarFileURL.openConnection().getInputStream();
            try {
                jar = AccessController
                    .doPrivileged(new PrivilegedAction<JarFile>() {
                        public JarFile run() {
                            try {
                                File tempJar = File.createTempFile("hyjar_", //$NON-NLS-1$
                                        ".tmp", null); //$NON-NLS-1$
                                tempJar.deleteOnExit();
                                FileOutputStream fos = new FileOutputStream(
                                        tempJar);
                                byte[] buf = new byte[4096];
                                int nbytes = 0;
                                while ((nbytes = is.read(buf)) > -1) {
                                    fos.write(buf, 0, nbytes);
                                }
                                fos.close();
                                return new JarFile(tempJar,
                                        true, ZipFile.OPEN_READ | ZipFile.OPEN_DELETE);
                            } catch (IOException e) {
                                return null;
                            }
                        }
                    });
            } finally {
                if (is != null) is.close();
            }
        }

        return jar;
    }

    /**
     * Returns the JarEntry of the entry referenced by this
     * <code>URLConnection</code>.
     *
     * @return java.util.jar.JarEntry the JarEntry referenced
     *
     * @throws IOException
     *             if an IO error occurs while getting the entry
     */
    @Override
    public JarEntry getJarEntry() throws IOException {
        connect();
        return jarEntry;

    }

    /**
     * Look up the JarEntry of the entry referenced by this
     * <code>URLConnection</code>.
     */
    private void findJarEntry() throws IOException {
        if (getEntryName() == null) {
            return;
        }
        jarEntry = jarFile.getJarEntry(getEntryName());
        if (jarEntry == null) {
            throw new FileNotFoundException(getEntryName());
        }
    }

    /**
     * Creates an input stream for reading from this URL Connection.
     *
     * @return the input stream
     *
     * @throws IOException
     *             if an IO error occurs while connecting to the resource.
     */
    @Override
    public InputStream getInputStream() throws IOException {

        if (closed) {
            throw new IllegalStateException(Msg.getString("KA027"));
        }
        connect();
        if (jarInput != null) {
            return jarInput;
        }
        if (jarEntry == null) {
            throw new IOException(Msg.getString("K00fc")); //$NON-NLS-1$
        }
        return jarInput = new JarURLConnectionInputStream(jarFile
                .getInputStream(jarEntry), jarFile);
    }

    /**
     * Returns the content type of the resource.
     * For jar file itself "x-java/jar" should be returned,
     * for jar entries the content type of the entry should be returned.
     * Returns non-null results ("content/unknown" for unknown types).
     *
     * @return the content type
     */
    @Override
    public String getContentType() {
        if (url.getFile().endsWith("!/")) { //$NON-NLS-1$
            // the type for jar file itself is always "x-java/jar"
            return "x-java/jar"; //$NON-NLS-1$
        } else {
            String cType = null;
            String entryName = getEntryName();

            if (entryName != null) {
                // if there is an Jar Entry, get the content type from the name
                cType = guessContentTypeFromName(entryName);
            } else {
                try {
                    connect();
                    cType = jarFileURLConnection.getContentType();
                } catch (IOException ioe) {
                    // Ignore
                }
            }
            if (cType == null) {
                cType = "content/unknown"; //$NON-NLS-1$
            }
            return cType;
        }
    }

    /**
     * Returns the content length of the resource. Test cases reveal that if the
     * URL is refering to a Jar file, this method returns a content-length
     * returned by URLConnection. For jar entry it should return it's size.
     * Otherwise, it will return -1.
     *
     * @return the content length
     */
    @Override
    public int getContentLength() {
        try {
            connect();
            if (jarEntry == null) {
                return jarFileURLConnection.getContentLength();
            } else {
                return (int) getJarEntry().getSize();
            }
        } catch (IOException e) {
            //Ignored
        }
        return -1;
    }

    /**
     * Returns the object pointed by this <code>URL</code>. If this
     * URLConnection is pointing to a Jar File (no Jar Entry), this method will
     * return a <code>JarFile</code> If there is a Jar Entry, it will return
     * the object corresponding to the Jar entry content type.
     *
     * @return a non-null object
     *
     * @throws IOException
     *             if an IO error occured
     *
     * @see ContentHandler
     * @see ContentHandlerFactory
     * @see java.io.IOException
     * @see #setContentHandlerFactory(ContentHandlerFactory)
     */
    @Override
    public Object getContent() throws IOException {
        connect();
        // if there is no Jar Entry, return a JarFile
        if (jarEntry == null) {
            return jarFile;
        }
        return super.getContent();
    }

    /**
     * Returns the permission, in this case the subclass, FilePermission object
     * which represents the permission necessary for this URLConnection to
     * establish the connection.
     *
     * @return the permission required for this URLConnection.
     *
     * @throws IOException
     *             thrown when an IO exception occurs while creating the
     *             permission.
     */

    @Override
    public Permission getPermission() throws IOException {
        return jarFileURLConnection.getPermission();
    }

    @Override
    public boolean getUseCaches() {
        return jarFileURLConnection.getUseCaches();
    }

    @Override
    public void setUseCaches(boolean usecaches) {
        jarFileURLConnection.setUseCaches(usecaches);
    }

    @Override
    public boolean getDefaultUseCaches() {
        return jarFileURLConnection.getDefaultUseCaches();
    }

    @Override
    public void setDefaultUseCaches(boolean defaultusecaches) {
        jarFileURLConnection.setDefaultUseCaches(defaultusecaches);
    }

    /**
     * Closes the cached files.
     */
    public static void closeCachedFiles() {
        Set<Map.Entry<URL, JarFile>> s = jarCache.entrySet();
        synchronized(jarCache){
            Iterator<Map.Entry<URL, JarFile>> i = s.iterator();
            while(i.hasNext()){
                try {
                    ZipFile zip = i.next().getValue();
                    if (zip != null) {
                        zip.close();
                    }
                } catch (IOException e) {
                    // Ignored
                }
            }
       }
    }

    private class JarURLConnectionInputStream extends FilterInputStream {
        InputStream inputStream;

        JarFile jarFile;

        protected JarURLConnectionInputStream(InputStream in, JarFile file) {
            super(in);
            inputStream = in;
            jarFile = file;
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (!getUseCaches()) {
                closed = true;
                jarFile.close();
            }
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public int read(byte[] buf, int off, int nbytes) throws IOException {
            return inputStream.read(buf, off, nbytes);
        }

        @Override
        public long skip(long nbytes) throws IOException {
            return inputStream.skip(nbytes);
        }
    }
}
