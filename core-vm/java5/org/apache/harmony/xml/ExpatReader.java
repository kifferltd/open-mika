/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * SAX wrapper around Expat. Interns strings. Does not support validation.
 * Does not support {@link DTDHandler}.
 */
public class ExpatReader implements XMLReader {

    private static final Logger logger
            = Logger.getLogger(ExpatReader.class.getName());

    /*
     * ExpatParser accesses these fields directly during parsing. The user
     * should be able to safely change them during parsing.
     */
    /*package*/ ContentHandler contentHandler;
    /*package*/ EntityResolver entityResolver;
    /*package*/ ErrorHandler errorHandler;
    /*package*/ LexicalHandler lexicalHandler;

    private boolean processNamespaces = true;
    private boolean processNamespacePrefixes = false;

    private static final String LEXICAL_HANDLER_PROPERTY
            = "http://xml.org/sax/properties/lexical-handler";

    private static class Feature {

        private static final String BASE_URI = "http://xml.org/sax/features/";

        private static final String VALIDATION = BASE_URI + "validation";
        private static final String NAMESPACES = BASE_URI + "namespaces";
        private static final String NAMESPACE_PREFIXES
                = BASE_URI + "namespace-prefixes";
        private static final String STRING_INTERNING
                = BASE_URI + "string-interning";
    }

    public boolean getFeature(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name");
        }

        if (name.equals(Feature.VALIDATION)) {
            return false;
        }

        if (name.equals(Feature.NAMESPACES)) {
            return processNamespaces;
        }

        if (name.equals(Feature.NAMESPACE_PREFIXES)) {
            return processNamespacePrefixes;
        }

        if (name.equals(Feature.STRING_INTERNING)) {
            return true;
        }

        throw new SAXNotRecognizedException(name);
    }

    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name");
        }

        if (name.equals(Feature.VALIDATION)) {
            if (value) {
                throw new SAXNotSupportedException("Cannot enable " + name);
            } else {
                // Default.
                return;
            }
        }

        if (name.equals(Feature.NAMESPACES)) {
            processNamespaces = value;
            return;
        }

        if (name.equals(Feature.NAMESPACE_PREFIXES)) {
            processNamespacePrefixes = value;
            return;
        }

        if (name.equals(Feature.STRING_INTERNING)) {
            if (value) {
                // Default.
                return;
            } else {
                throw new SAXNotSupportedException("Cannot disable " + name);
            }
        }

        throw new SAXNotRecognizedException(name);
    }

    public Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name");
        }

        if (name.equals(LEXICAL_HANDLER_PROPERTY)) {
            return lexicalHandler;
        }

        throw new SAXNotRecognizedException(name);
    }

    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name");
        }

        if (name.equals(LEXICAL_HANDLER_PROPERTY)) {
            // The object must implement LexicalHandler
            if (value instanceof LexicalHandler) {
                this.lexicalHandler = (LexicalHandler) value;
                return;
            }
            throw new SAXNotSupportedException("value doesn't implement " +
                    "org.xml.sax.ext.LexicalHandler");
        }

        throw new SAXNotRecognizedException(name);
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }

    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    /**
     * Not implemented.
     */
    public void setDTDHandler(DTDHandler ignored) {
        logger.warning("DTD handlers aren't supported.");
    }

    /**
     * Always returns null.
     */
    public DTDHandler getDTDHandler() {
        return null;
    }

    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    public ContentHandler getContentHandler() {
        return this.contentHandler;
    }

    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Returns the current lexical handler.
     *
     * @return the current lexical handler, or null if none has been registered
     * @see #setLexicalHandler
     */
    public LexicalHandler getLexicalHandler() {
        return lexicalHandler;
    }

    /**
     * Registers a lexical event handler. Supports neither
     * {@link LexicalHandler#startEntity(String)} nor
     * {@link LexicalHandler#endEntity(String)}.
     *
     * <p>If the application does not register a lexical handler, all
     * lexical events reported by the SAX parser will be silently
     * ignored.</p>
     *
     * <p>Applications may register a new or different handler in the
     * middle of a parse, and the SAX parser must begin using the new
     * handler immediately.</p>
     *
     * @param lexicalHandler listens for lexical events
     * @see #getLexicalHandler()
     */
    public void setLexicalHandler(LexicalHandler lexicalHandler) {
        this.lexicalHandler = lexicalHandler;
    }

    /**
     * Returns true if this SAX parser processes namespaces.
     *
     * @see #setNamespaceProcessingEnabled(boolean)
     */
    public boolean isNamespaceProcessingEnabled() {
        return processNamespaces;
    }

    /**
     * Enables or disables namespace processing. Set to true by default. If you
     * enable namespace processing, the parser will invoke
     * {@link ContentHandler#startPrefixMapping(String, String)} and
     * {@link ContentHandler#endPrefixMapping(String)}, and it will filter
     * out namespace declarations from element attributes.
     *
     * @see #isNamespaceProcessingEnabled()
     */
    public void setNamespaceProcessingEnabled(boolean processNamespaces) {
        this.processNamespaces = processNamespaces;
    }

    public void parse(InputSource input) throws IOException, SAXException {
        if (processNamespacePrefixes == processNamespaces) {
            /*
             * Expat has XML_SetReturnNSTriplet, but that still doesn't
             * include xmlns attributes like this feature requires. We may
             * have to implement namespace processing ourselves if we want
             * this (not too difficult). We obviously "support" namespace
             * prefixes if namespaces are disabled.
             */
            throw new SAXNotSupportedException("The 'namespace-prefix' " +
                    "feature is not supported while the 'namespaces' " +
                    "feature is enabled.");
        }

        // Try the character stream.
        Reader reader = input.getCharacterStream();
        if (reader != null) {
            try {
                parse(reader, input.getPublicId(), input.getSystemId());
            } finally {
                // TODO: Don't eat original exception when close() throws.
                reader.close();
            }
            return;
        }

        // Try the byte stream.
        InputStream in = input.getByteStream();
        String encoding = input.getEncoding();
        if (in != null) {
            try {
                parse(in, encoding, input.getPublicId(), input.getSystemId());
            } finally {
                // TODO: Don't eat original exception when close() throws.
                in.close();
            }
            return;
        }

        String systemId = input.getSystemId();
        if (systemId == null) {
            throw new SAXException("No input specified.");
        }

        // Try the system id.
        in = ExpatParser.openUrl(systemId);
        try {
            parse(in, encoding, input.getPublicId(), systemId);
        } finally {
            in.close();
        }
    }

    private void parse(Reader in, String publicId, String systemId)
            throws IOException, SAXException {
        ExpatParser parser = new ExpatParser(
                ExpatParser.CHARACTER_ENCODING,
                this,
                processNamespaces,
                publicId,
                systemId
        );
        parser.parseDocument(in);
    }

    private void parse(InputStream in, String encoding, String publicId,
            String systemId) throws IOException, SAXException {
        ExpatParser parser = new ExpatParser(
                encoding,
                this,
                processNamespaces,
                publicId,
                systemId
        );
        parser.parseDocument(in);
    }

    public void parse(String systemId) throws IOException, SAXException {
        parse(new InputSource(systemId));
    }
}
