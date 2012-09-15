/*
 * Copyright (C) 2007 The Android Open Source Project
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

import junit.framework.Assert;
import junit.framework.TestCase;
import org.kxml2.io.KXmlParser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.ServerSocket;
import java.net.Socket;

public class ExpatParserTest extends TestCase {

    private static final String SNIPPET = "<dagny dad=\"bob\">hello</dagny>";

    public void testExceptions() {
        // From startElement().
        ContentHandler contentHandler = new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName,
                    String qName, Attributes attributes)
                    throws SAXException {
                throw new SAXException();
            }
        };
        try {
            parse(SNIPPET, contentHandler);
            fail();
        } catch (SAXException checked) { /* expected */ }

        // From endElement().
        contentHandler = new DefaultHandler() {
            @Override
            public void endElement(String uri, String localName,
                    String qName)
                    throws SAXException {
                throw new SAXException();
            }
        };
        try {
            parse(SNIPPET, contentHandler);
            fail();
        } catch (SAXException checked) { /* expected */ }

        // From characters().
        contentHandler = new DefaultHandler() {
            @Override
            public void characters(char ch[], int start, int length)
                    throws SAXException {
                throw new SAXException();
            }
        };
        try {
            parse(SNIPPET, contentHandler);
            fail();
        } catch (SAXException checked) { /* expected */ }
    }

    public void testSax() {
        try {
            // Parse String.
            TestHandler handler = new TestHandler();
            parse(SNIPPET, handler);
            validate(handler);

            // Parse Reader.
            handler = new TestHandler();
            parse(new StringReader(SNIPPET), handler);
            validate(handler);

            // Parse InputStream.
            handler = new TestHandler();
            parse(new ByteArrayInputStream(SNIPPET.getBytes()),
                    Encoding.UTF_8, handler);
            validate(handler);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void validate(TestHandler handler) {
        assertEquals("dagny", handler.startElementName);
        assertEquals("dagny", handler.endElementName);
        assertEquals("hello", handler.text.toString());
    }

    static class TestHandler extends DefaultHandler {

        String startElementName;
        String endElementName;
        StringBuilder text = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {

            assertNull(this.startElementName);
            this.startElementName = localName;

            // Validate attributes.
            assertEquals(1, attributes.getLength());
            assertEquals("", attributes.getURI(0));
            assertEquals("dad", attributes.getLocalName(0));
            assertEquals("bob", attributes.getValue(0));
            assertEquals(0, attributes.getIndex("", "dad"));
            assertEquals("bob", attributes.getValue("", "dad"));
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            assertNull(this.endElementName);
            this.endElementName = localName;
        }

        @Override
        public void characters(char ch[], int start, int length)
                throws SAXException {
            this.text.append(ch, start, length);
        }
    }

    public void testPullParser() {
        try {
            XmlPullParser parser = newPullParser();

            // Test reader.
            parser.setInput(new StringReader(SNIPPET));
            validate(parser);

            // Test input stream.
            parser.setInput(new ByteArrayInputStream(SNIPPET.getBytes()),
                    "UTF-8");
            validate(parser);
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void validate(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        assertEquals(XmlPullParser.START_DOCUMENT, parser.getEventType());

        assertEquals(0, parser.getDepth());

        assertEquals(XmlPullParser.START_TAG, parser.next());

        assertEquals(1, parser.getDepth());

        assertEquals("dagny", parser.getName());
        assertEquals(1, parser.getAttributeCount());
        assertEquals("dad", parser.getAttributeName(0));
        assertEquals("bob", parser.getAttributeValue(0));
        assertEquals("bob", parser.getAttributeValue(null, "dad"));

        assertEquals(XmlPullParser.TEXT, parser.next());

        assertEquals(1, parser.getDepth());

        assertEquals("hello", parser.getText());

        assertEquals(XmlPullParser.END_TAG, parser.next());

        assertEquals(1, parser.getDepth());

        assertEquals("dagny", parser.getName());

        assertEquals(XmlPullParser.END_DOCUMENT, parser.next());

        assertEquals(0, parser.getDepth());
    }

    static final String XML =
        "<one xmlns='ns:default' xmlns:n1='ns:1' a='b'>\n"
              + "  <n1:two c='d' n1:e='f' xmlns:n2='ns:2'>text</n1:two>\n"
              + "</one>";

    public void testExpatPullParserNamespaces() throws Exception {
        XmlPullParser pullParser = newPullParser();
        pullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        pullParser.setInput(new StringReader(XML));
        testPullParserNamespaces(pullParser);
    }

    public void testKxmlPullParserNamespaces() throws Exception {
        XmlPullParser pullParser = new KXmlParser();
        pullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        pullParser.setInput(new StringReader(XML));
        testPullParserNamespaces(pullParser);
    }

    private void testPullParserNamespaces(XmlPullParser parser) throws Exception {
        assertEquals(0, parser.getDepth());
        assertEquals(0, parser.getNamespaceCount(0));

        try {
            parser.getNamespaceCount(1);
            fail();
        } catch (IndexOutOfBoundsException e) { /* expected */ }

        // one
        assertEquals(XmlPullParser.START_TAG, parser.next());
        assertEquals(1, parser.getDepth());

        checkNamespacesInOne(parser);

        // n1:two
        assertEquals(XmlPullParser.START_TAG, parser.nextTag());

        assertEquals(2, parser.getDepth());
        checkNamespacesInTwo(parser);

        // Body of two.
        assertEquals(XmlPullParser.TEXT, parser.next());

        // End of two.
        assertEquals(XmlPullParser.END_TAG, parser.nextTag());

        // Depth should still be 2.
        assertEquals(2, parser.getDepth());

        // We should still be able to see the namespaces from two.
        checkNamespacesInTwo(parser);

        // End of one.
        assertEquals(XmlPullParser.END_TAG, parser.nextTag());

        // Depth should be back to 1.
        assertEquals(1, parser.getDepth());

        // We can still see the namespaces in one.
        checkNamespacesInOne(parser);

        // We shouldn't be able to see the namespaces in two anymore.
        try {
            parser.getNamespaceCount(2);
            fail();
        } catch (IndexOutOfBoundsException e) { /* expected */ }

        assertEquals(XmlPullParser.END_DOCUMENT, parser.next());

        // We shouldn't be able to see the namespaces in one anymore.
        try {
            parser.getNamespaceCount(1);
            fail();
        } catch (IndexOutOfBoundsException e) { /* expected */ }

        assertEquals(0, parser.getNamespaceCount(0));
    }

    private void checkNamespacesInOne(XmlPullParser parser) throws XmlPullParserException {
        assertEquals(2, parser.getNamespaceCount(1));

        // Prefix for default namespace is null.
        assertNull(parser.getNamespacePrefix(0));
        assertEquals("ns:default", parser.getNamespaceUri(0));

        assertEquals("n1", parser.getNamespacePrefix(1));
        assertEquals("ns:1", parser.getNamespaceUri(1));

        assertEquals("ns:default", parser.getNamespace(null));

        // KXML returns null.
        // assertEquals("ns:default", parser.getNamespace(""));
    }

    private void checkNamespacesInTwo(XmlPullParser parser) throws XmlPullParserException {
        // These should still be valid.
        checkNamespacesInOne(parser);

        assertEquals(3, parser.getNamespaceCount(2));

        // Default ns should still be in the stack
        assertNull(parser.getNamespacePrefix(0));
        assertEquals("ns:default", parser.getNamespaceUri(0));
    }

    public void testNamespaces() {
        try {
            NamespaceHandler handler = new NamespaceHandler();
            parse(XML, handler);
            handler.validate();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    static class NamespaceHandler implements ContentHandler {

        Locator locator;
        boolean documentStarted;
        boolean documentEnded;
        Map<String, String> prefixMappings = new HashMap<String, String>();

        boolean oneStarted;
        boolean twoStarted;
        boolean oneEnded;
        boolean twoEnded;

        public void validate() {
            assertTrue(documentEnded);
        }

        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        public void startDocument() throws SAXException {
            documentStarted = true;
            assertNotNull(locator);
            assertEquals(0, prefixMappings.size());
            assertFalse(documentEnded);
        }

        public void endDocument() throws SAXException {
            assertTrue(documentStarted);
            assertTrue(oneEnded);
            assertTrue(twoEnded);
            assertEquals(0, prefixMappings.size());
            documentEnded = true;
        }

        public void startPrefixMapping(String prefix, String uri)
                throws SAXException {
            prefixMappings.put(prefix, uri);
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            assertNotNull(prefixMappings.remove(prefix));
        }

        public void startElement(String uri, String localName, String qName,
                Attributes atts) throws SAXException {

            if (localName == "one") {
                assertEquals(2, prefixMappings.size());

                assertEquals(1, locator.getLineNumber());

                assertFalse(oneStarted);
                assertFalse(twoStarted);
                assertFalse(oneEnded);
                assertFalse(twoEnded);

                oneStarted = true;

                assertSame("ns:default", uri);
                // TODO The result of the RI is "one"
                assertEquals("", qName);

                // Check atts.
                assertEquals(1, atts.getLength());

                assertSame("", atts.getURI(0));
                assertSame("a", atts.getLocalName(0));
                assertEquals("b", atts.getValue(0));
                assertEquals(0, atts.getIndex("", "a"));
                assertEquals("b", atts.getValue("", "a"));

                return;
            }

            if (localName == "two") {
                assertEquals(3, prefixMappings.size());

                assertTrue(oneStarted);
                assertFalse(twoStarted);
                assertFalse(oneEnded);
                assertFalse(twoEnded);

                twoStarted = true;

                assertSame("ns:1", uri);
                // TODO The result of the RI is "n1:two"
                Assert.assertEquals("", qName);

                // Check atts.
                assertEquals(2, atts.getLength());

                assertSame("", atts.getURI(0));
                assertSame("c", atts.getLocalName(0));
                assertEquals("d", atts.getValue(0));
                assertEquals(0, atts.getIndex("", "c"));
                assertEquals("d", atts.getValue("", "c"));

                assertSame("ns:1", atts.getURI(1));
                assertSame("e", atts.getLocalName(1));
                assertEquals("f", atts.getValue(1));
                assertEquals(1, atts.getIndex("ns:1", "e"));
                assertEquals("f", atts.getValue("ns:1", "e"));

                // We shouldn't find these.
                assertEquals(-1, atts.getIndex("ns:default", "e"));
                assertEquals(null, atts.getValue("ns:default", "e"));

                return;
            }

            fail();
         }

        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (localName == "one") {
                assertEquals(3, locator.getLineNumber());

                assertTrue(oneStarted);
                assertTrue(twoStarted);
                assertTrue(twoEnded);
                assertFalse(oneEnded);

                oneEnded = true;

                assertSame("ns:default", uri);
                assertEquals("", qName);

                return;
            }

            if (localName == "two") {
                assertTrue(oneStarted);
                assertTrue(twoStarted);
                assertFalse(twoEnded);
                assertFalse(oneEnded);

                twoEnded = true;

                assertSame("ns:1", uri);
                assertEquals("", qName);

                return;
            }

            fail();
        }

        public void characters(char ch[], int start, int length)
                throws SAXException {
            String s = new String(ch, start, length).trim();

            if (!s.equals("")) {
                assertTrue(oneStarted);
                assertTrue(twoStarted);
                assertFalse(oneEnded);
                assertFalse(twoEnded);
                assertEquals("text", s);
            }
        }

        public void ignorableWhitespace(char ch[], int start, int length)
                throws SAXException {
            fail();
        }

        public void processingInstruction(String target, String data)
                throws SAXException {
            fail();
        }

        public void skippedEntity(String name) throws SAXException {
            fail();
        }
    }

    public void testDtd() throws Exception {
        Reader in = new StringReader(
            "<?xml version=\"1.0\"?><!DOCTYPE foo PUBLIC 'bar' 'tee'><a></a>");
        ExpatReader reader = new ExpatReader();
        TestDtdHandler handler = new TestDtdHandler();
        reader.setContentHandler(handler);
        reader.setLexicalHandler(handler);
        reader.parse(new InputSource(in));

        assertEquals("foo", handler.name);
        assertEquals("bar", handler.publicId);
        assertEquals("tee", handler.systemId);

        assertTrue(handler.ended);
    }

    static class TestDtdHandler extends DefaultHandler2 {

        String name;
        String publicId;
        String systemId;

        boolean ended;

        Locator locator;

        @Override
        public void startDTD(String name, String publicId, String systemId) {
            this.name = name;
            this.publicId = publicId;
            this.systemId = systemId;
        }

        @Override
        public void endDTD() {
            ended = true;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }
    }

    public void testCdata() throws Exception {
        Reader in = new StringReader(
            "<a><![CDATA[<b></b>]]> <![CDATA[<c></c>]]></a>");

        ExpatReader reader = new ExpatReader();
        TestCdataHandler handler = new TestCdataHandler();
        reader.setContentHandler(handler);
        reader.setLexicalHandler(handler);

        reader.parse(new InputSource(in));

        assertEquals(2, handler.startCdata);
        assertEquals(2, handler.endCdata);
        assertEquals("<b></b> <c></c>", handler.buffer.toString());
    }

    static class TestCdataHandler extends DefaultHandler2 {

        int startCdata, endCdata;
        StringBuffer buffer = new StringBuffer();

        @Override
        public void characters(char ch[], int start, int length) {
            buffer.append(ch, start, length);
        }

        @Override
        public void startCDATA() throws SAXException {
            startCdata++;
        }

        @Override
        public void endCDATA() throws SAXException {
            endCdata++;
        }
    }

    public void testProcessingInstructions() throws IOException, SAXException {
        Reader in = new StringReader(
            "<?bob lee?><a></a>");

        ExpatReader reader = new ExpatReader();
        TestProcessingInstrutionHandler handler
                = new TestProcessingInstrutionHandler();
        reader.setContentHandler(handler);

        reader.parse(new InputSource(in));

        assertEquals("bob", handler.target);
        assertEquals("lee", handler.data);
    }

    static class TestProcessingInstrutionHandler extends DefaultHandler2 {

        String target;
        String data;

        @Override
        public void processingInstruction(String target, String data) {
            this.target = target;
            this.data = data;
        }
    }

    public void testExternalEntity() throws IOException, SAXException {
        class Handler extends DefaultHandler {

            List<String> elementNames = new ArrayList<String>();
            StringBuilder text = new StringBuilder();

            public InputSource resolveEntity(String publicId, String systemId)
                    throws IOException, SAXException {
                if (publicId.equals("publicA") && systemId.equals("systemA")) {
                    return new InputSource(new StringReader("<a/>"));
                } else if (publicId.equals("publicB")
                        && systemId.equals("systemB")) {
                    /*
                     * Explicitly set the encoding here or else the parser will
                     * try to use the parent parser's encoding which is utf-16.
                     */
                    InputSource inputSource = new InputSource(
                            new ByteArrayInputStream("bob".getBytes("utf-8")));
                    inputSource.setEncoding("utf-8");
                    return inputSource;
                }

                throw new AssertionError();
            }

            @Override
            public void startElement(String uri, String localName, String qName,
                    Attributes attributes) throws SAXException {
                elementNames.add(localName);
            }

            @Override
            public void endElement(String uri, String localName, String qName)
                    throws SAXException {
                elementNames.add("/" + localName);
            }

            @Override
            public void characters(char ch[], int start, int length)
                    throws SAXException {
                text.append(ch, start, length);
            }
        }

        Reader in = new StringReader("<?xml version=\"1.0\"?>\n"
            + "<!DOCTYPE foo [\n"
            + "  <!ENTITY a PUBLIC 'publicA' 'systemA'>\n"
            + "  <!ENTITY b PUBLIC 'publicB' 'systemB'>\n"
            + "]>\n"
            + "<foo>\n"
            + "  &a;<b>&b;</b></foo>");

        ExpatReader reader = new ExpatReader();
        Handler handler = new Handler();
        reader.setContentHandler(handler);
        reader.setEntityResolver(handler);

        reader.parse(new InputSource(in));

        assertEquals(Arrays.asList("foo", "a", "/a", "b", "/b", "/foo"),
                handler.elementNames);
        assertEquals("bob", handler.text.toString().trim());
    }

    public void testExternalEntityDownload() throws IOException, SAXException {
        class Server implements Runnable {

            private final ServerSocket serverSocket;

            Server() throws IOException {
                serverSocket = new ServerSocket(8080);
            }

            public void run() {
                try {
                    Socket socket = serverSocket.accept();

                    final InputStream in = socket.getInputStream();
                    Thread inputThread = new Thread() {
                        public void run() {
                            try {
                                byte[] buffer = new byte[1024];
                                while (in.read(buffer) > -1) { /* ignore */ }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    inputThread.setDaemon(true);
                    inputThread.start();

                    OutputStream out = socket.getOutputStream();

                    String body = "<bar></bar>";
                    String response = "HTTP/1.0 200 OK\n"
                        + "Content-Length: " + body.length() + "\n"
                        + "\n"
                        + body;

                    out.write(response.getBytes("UTF-8"));
                    out.close();
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        class Handler extends DefaultHandler {

            List<String> elementNames = new ArrayList<String>();

            public InputSource resolveEntity(String publicId, String systemId)
                    throws IOException, SAXException {
                // The parser should have resolved the systemId.
                assertEquals("http://localhost:8080/systemBar", systemId);
                return new InputSource(systemId);
            }

            @Override
            public void startElement(String uri, String localName, String qName,
                    Attributes attributes) throws SAXException {
                elementNames.add(localName);
            }

            @Override
            public void endElement(String uri, String localName, String qName)
                    throws SAXException {
                elementNames.add("/" + localName);
            }
        }

        // Start server to serve up the XML for 'systemBar'.
        Thread serverThread = new Thread(new Server());
        serverThread.setDaemon(true);
        serverThread.start();

        // 'systemBar', the external entity, is relative to 'systemFoo':
        Reader in = new StringReader("<?xml version=\"1.0\"?>\n"
            + "<!DOCTYPE foo [\n"
            + "  <!ENTITY bar SYSTEM 'systemBar'>\n"
            + "]>\n"
            + "<foo>&bar;</foo>");

        ExpatReader reader = new ExpatReader();

        Handler handler = new Handler();

        reader.setContentHandler(handler);
        reader.setEntityResolver(handler);

        InputSource source = new InputSource(in);
        source.setSystemId("http://localhost:8080/systemFoo");
        reader.parse(source);

        assertEquals(Arrays.asList("foo", "bar", "/bar", "/foo"),
                handler.elementNames);
    }

    /**
     * Parses the given xml string and fires events on the given SAX handler.
     */
    private static void parse(String xml, ContentHandler contentHandler)
            throws SAXException {
        try {
            XMLReader reader = new ExpatReader();
            reader.setContentHandler(contentHandler);
            reader.parse(new InputSource(new StringReader(xml)));
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Parses xml from the given reader and fires events on the given SAX
     * handler.
     */
    private static void parse(Reader in, ContentHandler contentHandler)
            throws IOException, SAXException {
        XMLReader reader = new ExpatReader();
        reader.setContentHandler(contentHandler);
        reader.parse(new InputSource(in));
    }

    /**
     * Parses xml from the given input stream and fires events on the given SAX
     * handler.
     */
    private static void parse(InputStream in, Encoding encoding,
            ContentHandler contentHandler) throws IOException, SAXException {
        try {
            XMLReader reader = new ExpatReader();
            reader.setContentHandler(contentHandler);
            InputSource source = new InputSource(in);
            source.setEncoding(encoding.expatName);
            reader.parse(source);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Supported character encodings.
     */
    private enum Encoding {

        US_ASCII("US-ASCII"),
        UTF_8("UTF-8"),
        UTF_16("UTF-16"),
        ISO_8859_1("ISO-8859-1");

        final String expatName;

        Encoding(String expatName) {
            this.expatName = expatName;
        }
    }

    /**
     * Creates a new pull parser with namespace support.
     */
    private static XmlPullParser newPullParser() {
        ExpatPullParser parser = new ExpatPullParser();
        parser.setNamespaceProcessingEnabled(true);
        return parser;
    }
}
