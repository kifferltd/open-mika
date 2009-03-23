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
 * Imported by CG 20090322 based on Apache Harmony ("enhanced") revision 757150.
 * Modified to not use java.nio (because not in OSGi RFC 26). For this I've used
 * the Mika code, hence the read/write methods are still copyright Punch-/k/.
 */

package java.util.jar;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The Manifest class is used to obtain attribute information for a JarFile and
 * its entries.
 */
public class Manifest implements Cloneable {
    static final int LINE_LENGTH_LIMIT = 72;

    private static final byte[] LINE_SEPARATOR = new byte[] { '\r', '\n' };

    private static final byte[] VALUE_SEPARATOR = new byte[] { ':', ' ' };

    private static final Attributes.Name NAME_ATTRIBUTE = new Attributes.Name(
            "Name"); //$NON-NLS-1$

    private Attributes mainAttributes = new Attributes();

    private HashMap entries = new HashMap();

    static class Chunk {
        int start;
        int end;

        Chunk(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private HashMap chunks;

    /**
     * The end of the main attributes section in the manifest is needed in
     * verification.
     */
    private int mainEnd;

    /**
     * Constructs a new Manifest instance.
     */
    public Manifest() {
        super();
    }

    /**
     * Constructs a new Manifest instance using the attributes obtained from is.
     * 
     * @param is
     *            InputStream to parse for attributes
     * 
     * @throws IOException
     *             if an IO error occurs while creating this Manifest
     * 
     */
    public Manifest(InputStream is) throws IOException {
        super();
        read(is);
    }

    /**
     * Constructs a new Manifest instance. The new instance will have the same
     * attributes as those found in the parameter Manifest.
     * 
     * @param man
     *            Manifest instance to obtain attributes from
     */
    public Manifest(Manifest man) {
        mainAttributes = (Attributes) man.mainAttributes.clone();
        entries = (HashMap) ((HashMap) man.getEntries()).clone();
    }

    Manifest(InputStream is, boolean readChunks) throws IOException {
        if (readChunks) {
            chunks = new HashMap();
        }
        read(is);
    }

    /**
     * Resets the both the mainAttributes as well as the entry Attributes
     * associated with this Manifest.
     */
    public void clear() {
        entries.clear();
        mainAttributes.clear();
    }

    /**
     * Returns the Attributes associated with the parameter entry name
     * 
     * @param name
     *            The name of the entry to obtain Attributes for.
     * @return The Attributes for the entry or null if the entry does not exist.
     */
    public Attributes getAttributes(String name) {
        return (Attributes)getEntries().get(name);
    }

    /**
     * Returns a Map containing the Attributes for each entry in the Manifest.
     * 
     * @return A Map of entry attributes
     */
    public Map getEntries() {
        initEntries();
        return entries;
    }

    private void initEntries() {
    }

    /**
     * Returns the main Attributes of the JarFile.
     * 
     * @return Main Attributes associated with the source JarFile
     */
    public Attributes getMainAttributes() {
        return mainAttributes;
    }

    /**
     * Creates a copy of this Manifest. The returned Manifest will equal the
     * Manifest from which it was cloned.
     * 
     * @return A copy of the receiver.
     */
    public Object clone() {
        return new Manifest(this);
    }

/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2008 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

    /**
     * Writes out the attribute information of the receiver to the specified
     * OutputStream
     * 
     * @param os
     *            The OutputStream to write to.
     * 
     * @throws IOException
     *             If an error occurs writing the Manifest
     */
    public void write(OutputStream os) throws IOException {       
        writeAttributes(os, mainAttributes);
        Iterator it = entries.entrySet().iterator();
        Map.Entry me;
        StringBuffer buf;
        while (it.hasNext()) {
                me = (Map.Entry) it.next();
                buf = new StringBuffer("\r\n");
                buf.append("Name: ");
                buf.append((String)me.getKey());
                os.write(LINE_SEPARATOR);
                writeBuffer(os,buf);
                writeAttributes(os, (Attributes)me.getValue());                         
        }       
    }

    private void writeAttributes(OutputStream os, Attributes at) throws IOException {
        StringBuffer buf;
        Iterator it = at.entrySet().iterator();
        Map.Entry me;
        while (it.hasNext()) {
                me = (Map.Entry) it.next();
                buf = new StringBuffer(((Attributes.Name)me.getKey()).toString());
                buf.append(": ");
                buf.append((String)me.getValue());
                writeBuffer(os,buf);
        }
    }

    private void writeBuffer(OutputStream os, StringBuffer buf) throws IOException {
        int len = buf.length();
        int offset = 0;
        byte [] bytes = new String(buf).getBytes("UTF8");
        while(len > 0) {
            if ( len > LINE_LENGTH_LIMIT ) {
              os.write(bytes,offset,LINE_LENGTH_LIMIT);
              os.write(LINE_SEPARATOR);
              os.write(' ');
              len -= LINE_LENGTH_LIMIT;
              offset += LINE_LENGTH_LIMIT;
            }
            else {           
                os.write(bytes,offset,len);
                os.write(LINE_SEPARATOR);
                break;
            }
        }
    }

    /**
     * Constructs a new Manifest instance obtaining Attribute information from
     * the parameter InputStream.
     * 
     * @param is
     *            The InputStream to read from
     * @throws IOException
     *             If an error occurs reading the Manifest.
     */
    public void read(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));
    	Attributes attributes = mainAttributes;
    	String at = br.readLine();
    	boolean validEntry = true;
    	
        while (at != null) {
            String att = at;
             at = br.readLine();
             while (at != null && at.startsWith(" ")) {
                att = att+at.substring(1);
                at = br.readLine();
            }
            if (att.trim().length() > 0) { //Skip empty line
                int sep = att.indexOf(": ");
                if (validEntry) {
                    String name = att.substring(0,sep);
                    String value = att.substring(sep+2);

                    if (attributes == null) {
                        if(name.equals("Name")){
                            attributes = (Attributes) entries.get(value);
                            if (attributes == null) {
                                attributes = new Attributes(13);
                                entries.put(value.trim(), attributes);
                            }
                        }
                        else {
                            validEntry = false;
                        }
                    }
                    else {
                        attributes.putValue(name,value);
                    }
                }
            }
            else {
                attributes = null;
                validEntry = true;
            }
        }
    }
    
    /**
     * Returns the hashCode for this instance.
     * 
     * @return This Manifest's hashCode
     */
    public int hashCode() {
        return mainAttributes.hashCode() ^ getEntries().hashCode();
    }

    /**
     * Determines if the receiver is equal to the parameter Object. Two
     * Manifests are equal if they have identical main Attributes as well as
     * identical entry Attributes.
     * 
     * @param o
     *            The Object to compare against.
     * @return <code>true</code> if the manifests are equal,
     *         <code>false</code> otherwise
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        if (!mainAttributes.equals(((Manifest) o).mainAttributes)) {
            return false;
        }
        return getEntries().equals(((Manifest) o).getEntries());
    }

    Chunk getChunk(String name) {
        return (Chunk)chunks.get(name);
    }

    void removeChunks() {
        chunks = null;
    }

    int getMainAttributesEnd() {
        return mainEnd;
    }

}

