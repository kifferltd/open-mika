/* Copyright (c) 2002,2003, Stefan Haustein, Oberhausen, Rhld., Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The  above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE. */

//Contributors: Jonathan Cox, Bogdan Onoiu, Jerry Tian

package org.kxml2.wap;

import java.io.*;
import java.util.*;

import org.xmlpull.v1.*;

// TODO: make some of the "direct" WBXML token writing methods public??

/**
 * A class for writing WBXML.
 *
 */



public class WbxmlSerializer implements XmlSerializer {
    
    
    Hashtable stringTable = new Hashtable();
    
    OutputStream out;
    
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    ByteArrayOutputStream stringTableBuf = new ByteArrayOutputStream();
    
    String pending;
    int depth;
    String name;
    String namespace;
    Vector attributes = new Vector();
    
    Hashtable attrStartTable = new Hashtable();
    Hashtable attrValueTable = new Hashtable();
    Hashtable tagTable = new Hashtable();
    
    private int attrPage;
    private int tagPage;
    
    private String encoding;
    
    
    public XmlSerializer attribute(String namespace, String name, String value) {
        attributes.addElement(name);
        attributes.addElement(value);
        return this;
    }
    
    
    public void cdsect (String cdsect) throws IOException{
        text (cdsect);
    }
    
    
    
    /* silently ignore comment */
    
    public void comment (String comment) {
    }
    
    
    public void docdecl (String docdecl) {
        throw new RuntimeException ("Cannot write docdecl for WBXML");
    }
    
    
    public void entityRef (String er) {
        throw new RuntimeException ("EntityReference not supported for WBXML");
    }
    
    public int getDepth() {
        return depth;
    }
    
    
    public boolean getFeature (String name) {
        return false;
    }
    
    public String getNamespace() {
        throw new RuntimeException("NYI");
    }
    
    public String getName() {
        throw new RuntimeException("NYI");
    }
    
    public String getPrefix(String nsp, boolean create) {
        throw new RuntimeException ("NYI");
    }
    
    
    public Object getProperty (String name) {
        return null;
    }
    
    public void ignorableWhitespace (String sp) {
    }
    
    
    public void endDocument() throws IOException {
        writeInt(out, stringTableBuf.size());
        
        // write StringTable
        
        out.write(stringTableBuf.toByteArray());
        
        // write buf
        
        out.write(buf.toByteArray());
        
        // ready!
        
        out.flush();
    }
    
    
    /** ATTENTION: flush cannot work since Wbxml documents require
      buffering. Thus, this call does nothing. */
    
    public void flush() {
    }
    
    
    public void checkPending(boolean degenerated) throws IOException {
        if (pending == null)
            return;
        
        int len = attributes.size();
        
        int[] idx = (int[]) tagTable.get(pending);
        
        // if no entry in known table, then add as literal
        if (idx == null) {
            buf.write(
            len == 0
            ? (degenerated ? Wbxml.LITERAL : Wbxml.LITERAL_C)
            : (degenerated ? Wbxml.LITERAL_A : Wbxml.LITERAL_AC));
            
            writeStrT(pending, false);
        }
        else {
            if(idx[0] != tagPage){
                tagPage=idx[0];
                buf.write(Wbxml.SWITCH_PAGE);
                buf.write(tagPage);
            }
            
            buf.write(
            len == 0
            ? (degenerated ? idx[1] : idx[1] | 64)
            : (degenerated
            ? idx[1] | 128
            : idx[1] | 192));
           
        }
        
        for (int i = 0; i < len;) {
            idx = (int[]) attrStartTable.get(attributes.elementAt(i));
            
            if (idx == null) {
                buf.write(Wbxml.LITERAL);
                writeStrT((String) attributes.elementAt(i), false);
            }
            else {
                if(idx[0] != attrPage){
                        attrPage = idx[0];
                    buf.write(0);
                    buf.write(attrPage);
                }
                buf.write(idx[1]);
            }
            idx = (int[]) attrValueTable.get(attributes.elementAt(++i));
            if (idx == null) {
                writeStr((String) attributes.elementAt(i));
            }
            else {
                if(idx[0] != attrPage){
                        attrPage = idx[0];
                    buf.write(0);
                    buf.write(attrPage);                    
                }
                buf.write(idx[1]);
            }
            ++i;
        }
        
        if (len > 0)
            buf.write(Wbxml.END);
        
        pending = null;
        attributes.removeAllElements();
    }
    
    
    public void processingInstruction(String pi) {
        throw new RuntimeException ("PI NYI");
    }
    
    
    public void setFeature(String name, boolean value) {
        throw new IllegalArgumentException ("unknown feature "+name);
    }
    
    
    
    public void setOutput (Writer writer) {
        throw new RuntimeException ("Wbxml requires an OutputStream!");
    }
    
    public void setOutput (OutputStream out, String encoding) throws IOException {
        
        this.encoding = encoding == null ? "UTF-8" : encoding;
        this.out = out;
        
        buf = new ByteArrayOutputStream();
        stringTableBuf = new ByteArrayOutputStream();
        
        // ok, write header
    }
    
    
    public void setPrefix(String prefix, String nsp) {
        throw new RuntimeException("NYI");
    }
    
    public void setProperty(String property, Object value) {
        throw new IllegalArgumentException ("unknown property "+property);
    }
    
    
    public void startDocument(String s, Boolean b) throws IOException{
        out.write(0x03); // version 1.3
        // http://www.openmobilealliance.org/tech/omna/omna-wbxml-public-docid.htm
        out.write(0x01); // unknown or missing public identifier

        // default encoding is UTF-8
        
        if(s != null){
            encoding = s;
        }
        
        if (encoding.toUpperCase().equals("UTF-8")){
            out.write(106);
        }else if (encoding.toUpperCase().equals("ISO-8859-1")){
            out.write(0x04);
        }else{
            throw new UnsupportedEncodingException(s);
        }
    }
    
    
    public XmlSerializer startTag(String namespace, String name) throws IOException {
        
        if (namespace != null && !"".equals(namespace))
            throw new RuntimeException ("NSP NYI");
        
        //current = new State(current, prefixMap, name);
        
        checkPending(false);
        pending = name;
        depth++;
        
        return this;
    }
    
    public XmlSerializer text(char[] chars, int start, int len) throws IOException {

        checkPending(false);
        
        writeStr(new String(chars, start, len));
        
        return this;
    }
    
    public XmlSerializer text(String text) throws IOException {
        
        checkPending(false);
        
        writeStr(text);
    
        return this;
    }
    

    /** Used in text() and attribute() to write text */
    
    private void writeStr(String text) throws IOException{
        int p0 = 0;
        int lastCut = 0;
        int len = text.length();
        
        while(p0 < len){
            while(p0 < len && text.charAt(p0) < 'A' ){ // skip interpunctation
                p0++;
            }
            int p1 = p0;
            while(p1 < len && text.charAt(p1) >= 'A'){
                p1++;
            }
            
            if(p1 - p0 > 10) {

                if(p0 > lastCut && text.charAt(p0-1) == ' ' 
                    && stringTable.get(text.substring(p0, p1)) == null){
                    
                       buf.write(Wbxml.STR_T);
                       writeStrT(text.substring(lastCut, p1), false);
                }
                else {

                    if(p0 > lastCut && text.charAt(p0-1) == ' '){
                        p0--;
                    }

                    if(p0 > lastCut){
                        buf.write(Wbxml.STR_T);
                        writeStrT(text.substring(lastCut, p0), false);
                    }
                    buf.write(Wbxml.STR_T);
                    writeStrT(text.substring(p0, p1), true);
                }
                lastCut = p1;
            }
            p0 = p1;
        }

        if(lastCut < len){
            buf.write(Wbxml.STR_T);
            writeStrT(text.substring(lastCut, len), false);
        }
    }
    
    

    public XmlSerializer endTag(String namespace, String name) throws IOException {
        
        //        current = current.prev;
        
        if (pending != null)
            checkPending(true);
        else
            buf.write(Wbxml.END);
        
        depth--;
        
        return this;
    }
    
    /** 
     * @throws IOException */
    
    public void writeWapExtension(int type, Object data) throws IOException {
        checkPending(false);
        buf.write(type);
        switch(type){
        case Wbxml.EXT_0:
        case Wbxml.EXT_1:
        case Wbxml.EXT_2:
            break;
        
        case Wbxml.OPAQUE:
            byte[] bytes = (byte[]) data;
            writeInt(buf, bytes.length);
            buf.write(bytes);
            break;
            
        case Wbxml.EXT_I_0:
        case Wbxml.EXT_I_1:
        case Wbxml.EXT_I_2:
            writeStrI(buf, (String) data);
            break;

        case Wbxml.EXT_T_0:
        case Wbxml.EXT_T_1:
        case Wbxml.EXT_T_2:
            writeStrT((String) data, false);
            break;
            
        default: 
            throw new IllegalArgumentException();
        }
    }
    
    // ------------- internal methods --------------------------
    
    static void writeInt(OutputStream out, int i) throws IOException {
        byte[] buf = new byte[5];
        int idx = 0;
        
        do {
            buf[idx++] = (byte) (i & 0x7f);
            i = i >> 7;
        }
        while (i != 0);
        
        while (idx > 1) {
            out.write(buf[--idx] | 0x80);
        }
        out.write(buf[0]);
    }
    
    void writeStrI(OutputStream out, String s) throws IOException {
        byte[] data = s.getBytes(encoding);
        out.write(data);
        out.write(0);
    }
    
    private final void writeStrT(String s, boolean mayPrependSpace) throws IOException {
        
        Integer idx = (Integer) stringTable.get(s);
        
        if (idx != null) {
            writeInt(buf, idx.intValue());
        }
        else{
            int i = stringTableBuf.size();
            if(s.charAt(0) >= '0' && mayPrependSpace){
                s = ' ' + s;
                writeInt(buf, i+1);
            }
            else{
                writeInt(buf, i);
            }
            
               stringTable.put(s, new Integer(i));
               if(s.charAt(0) == ' '){
                   stringTable.put(s.substring(1), new Integer(i+1));
               }
               int j = s.lastIndexOf(' ');
               if(j > 1){
                   stringTable.put(s.substring(j), new Integer(i+j));
                   stringTable.put(s.substring(j+1), new Integer(i+j+1));
               }
                
            writeStrI(stringTableBuf, s);
            stringTableBuf.flush();
        }
        
    }
    
    /**
     * Sets the tag table for a given page.
     * The first string in the array defines tag 5, the second tag 6 etc.
     */
    
    public void setTagTable(int page, String[] tagTable) {
        // TODO: clear entries in tagTable?
        
        for (int i = 0; i < tagTable.length; i++) {
            if (tagTable[i] != null) {
                Object idx = new int[]{page, i+5};
                this.tagTable.put(tagTable[i], idx);
            }
        }
    }
    
    /**
     * Sets the attribute start Table for a given page.
     * The first string in the array defines attribute
     * 5, the second attribute 6 etc.
     *  Please use the
     *  character '=' (without quote!) as delimiter
     *  between the attribute name and the (start of the) value
     */
    public void setAttrStartTable(int page, String[] attrStartTable) {
        
        for (int i = 0; i < attrStartTable.length; i++) {
            if (attrStartTable[i] != null) {
                Object idx = new int[] {page, i + 5};
                this.attrStartTable.put(attrStartTable[i], idx);
            }
        }
    }
    
    /**
     * Sets the attribute value Table for a given page.
     * The first string in the array defines attribute value 0x85,
     * the second attribute value 0x86 etc.
     */
    public void setAttrValueTable(int page, String[] attrValueTable) {
        // clear entries in this.table!
        for (int i = 0; i < attrValueTable.length; i++) {
            if (attrValueTable[i] != null) {
                Object idx = new int[]{page, i + 0x085};
                this.attrValueTable.put(attrValueTable[i], idx);
            }
        }
    }
}
