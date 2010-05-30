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
 * Imported by CG 20090322 based on Apache Harmony ("enhanced") revision 669414.
 * Modified to not use java.nio.
 */

package java.util.jar;

import java.io.IOException;
import java.util.Map;

class InitManifest {

    private byte[] buf;

    private int pos;

    Attributes.Name name;

    String value;

    InitManifest(byte[] buf, Attributes main, Attributes.Name ver)
            throws IOException {

        this.buf = buf;

        // check a version attribute
        if (!readHeader() || (ver != null && !name.equals(ver))) {
            throw new IOException("Missing version attribute: " + ver);
        }

        main.put(name, value);
        while (readHeader()) {
            main.put(name, value);
        }
    }

    void initEntries(Map entries, Map chunks) throws IOException {

        int mark = pos;
        while (readHeader()) {
            if (!Attributes.Name.NAME.equals(name)) {
                throw new IOException("Entry is not named");
            }
            String entryNameValue = value;

            Attributes entry = (Attributes) entries.get(entryNameValue);
            if (entry == null) {
                entry = new Attributes(12);
            }

            while (readHeader()) {
                entry.put(name, value);
            }

            if (chunks != null) {
                if (chunks.get(entryNameValue) != null) {
                    // TODO A bug: there might be several verification chunks for
                    // the same name. I believe they should be used to update
                    // signature in order of appearance; there are two ways to fix
                    // this: either use a list of chunks, or decide on used
                    // signature algorithm in advance and reread the chunks while
                    // updating the signature; for now a defensive error is thrown
                    throw new IOException("A jar verifier does not support more than one entry with the same name");
                }
                chunks.put(entryNameValue, new Manifest.Chunk(mark, pos));
                mark = pos;
            }

            entries.put(entryNameValue, entry);
        }
    }

    int getPos() {
        return pos;
    }

    /**
     * Number of subsequent line breaks.
     */
    int linebreak = 0;

    /**
     * Read a single line from the manifest buffer.
     */
    private boolean readHeader() throws IOException {
        if (linebreak > 1) {
            // break a section on an empty line
            linebreak = 0;
            return false;
        }
        readName();
        linebreak = 0;
        readValue();
        // if the last line break is missed, the line
        // is ignored by the reference implementation
        return linebreak > 0;
    }

    private byte[] wrap(int mark, int pos) {
        byte[] buffer = new byte[pos - mark];
        System.arraycopy(buf, mark, buffer, 0, pos - mark);
        return buffer;
    }

    private void readName() throws IOException {
        int i = 0;
        int mark = pos;

        while (pos < buf.length) {
            byte b = buf[pos++];

            if (b == ':') {
                byte[] nameBuffer = wrap(mark, pos - 1);

                if (buf[pos++] != ' ') {
                    throw new IOException("Invalid attribute " + nameBuffer);
                }

                name = new Attributes.Name(nameBuffer);
                return;
            }

            if (!((b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z') || b == '_'
                    || b == '-' || (b >= '0' && b <= '9'))) {
                throw new IOException("Invalid attribute character code " + b);
            }
        }
        if (i > 0) {
            throw new IOException("Invalid attribute " + wrap(mark, buf.length));
        }
    }

    private void readValue() throws IOException {
        byte next;
        boolean lastCr = false;
        int mark = pos;
        int last = pos;

        value = "";
        while (pos < buf.length) {
            next = buf[pos++];

            switch (next) {
            case 0:
                throw new IOException("NUL character in a manifest");
            case '\n':
                if (lastCr) {
                    lastCr = false;
                } else {
                    linebreak++;
                }
                continue;
            case '\r':
                lastCr = true;
                linebreak++;
                continue;
            case ' ':
                if (linebreak == 1) {
                    value += new String(wrap(mark, last), "UTF8");
                    mark = pos;
                    linebreak = 0;
                    continue;
                }
            }

            if (linebreak >= 1) {
                pos--;
                break;
            }
            last = pos;
        }

        value += new String(wrap(mark, last), "UTF8");
    }

}

