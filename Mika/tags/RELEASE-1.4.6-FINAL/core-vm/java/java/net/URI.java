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

/*
 * Imported by CG 20090319 based on Apache Harmony ("enhanced") revision 738561.
 */

package java.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

/**
 * This class represents an instance of a URI as defined by RFC 2396.
 */
public final class URI implements Comparable, Serializable {

    private static final long serialVersionUID = -6052424284110960213l;

    static final String unreserved = "_-!.~\'()*"; //$NON-NLS-1$

    static final String punct = ",;:$&+="; //$NON-NLS-1$

    static final String reserved = punct + "?/[]@"; //$NON-NLS-1$

    static final String someLegal = unreserved + punct;

    static final String allLegal = unreserved + reserved;

    private String string;

    private transient String scheme;

    private transient String schemespecificpart;

    private transient String authority;

    private transient String userinfo;

    private transient String host;

    private transient int port = -1;

    private transient String path;

    private transient String query;

    private transient String fragment;

    private transient boolean opaque;

    private transient boolean absolute;

    private transient boolean serverAuthority = false;

    private transient int hash = -1;

    private URI() {
    }

    public URI(String uri) throws URISyntaxException {
        new Helper().parseURI(uri, false);
    }

    public URI(String scheme, String ssp, String frag)
            throws URISyntaxException {
        StringBuffer uri = new StringBuffer();
        if (scheme != null) {
            uri.append(scheme);
            uri.append(':');
        }
        if (ssp != null) {
            // QUOTE ILLEGAL CHARACTERS
            uri.append(quoteComponent(ssp, allLegal));
        }
        if (frag != null) {
            uri.append('#');
            // QUOTE ILLEGAL CHARACTERS
            uri.append(quoteComponent(frag, allLegal));
        }

        new Helper().parseURI(uri.toString(), false);
    }

    public URI(String scheme, String userinfo, String host, int port,
            String path, String query, String fragment)
            throws URISyntaxException {

        if (scheme == null && userinfo == null && host == null && path == null
                && query == null && fragment == null) {
            this.path = ""; //$NON-NLS-1$
            return;
        }

        if (scheme != null && path != null && path.length() > 0
                && path.charAt(0) != '/') {
            throw new URISyntaxException(path, "Relative path");
        }

        StringBuffer uri = new StringBuffer();
        if (scheme != null) {
            uri.append(scheme);
            uri.append(':');
        }

        if (userinfo != null || host != null || port != -1) {
            uri.append("//"); //$NON-NLS-1$
        }

        if (userinfo != null) {
            // QUOTE ILLEGAL CHARACTERS in userinfo
            uri.append(quoteComponent(userinfo, someLegal));
            uri.append('@');
        }

        if (host != null) {
            // check for ipv6 addresses that hasn't been enclosed
            // in square brackets
            if (host.indexOf(':') != -1 && host.indexOf(']') == -1
                    && host.indexOf('[') == -1) {
                host = "[" + host + "]"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            uri.append(host);
        }

        if (port != -1) {
            uri.append(':');
            uri.append(port);
        }

        if (path != null) {
            // QUOTE ILLEGAL CHARS
            uri.append(quoteComponent(path, "/@" + someLegal)); //$NON-NLS-1$
        }

        if (query != null) {
            uri.append('?');
            // QUOTE ILLEGAL CHARS
            uri.append(quoteComponent(query, allLegal));
        }

        if (fragment != null) {
            // QUOTE ILLEGAL CHARS
            uri.append('#');
            uri.append(quoteComponent(fragment, allLegal));
        }

        new Helper().parseURI(uri.toString(), true);
    }

    public URI(String scheme, String host, String path, String fragment)
            throws URISyntaxException {
        this(scheme, null, host, -1, path, null, fragment);
    }

    public URI(String scheme, String authority, String path, String query,
            String fragment) throws URISyntaxException {
        if (scheme != null && path != null && path.length() > 0
                && path.charAt(0) != '/') {
            throw new URISyntaxException(path, "Relative path");
        }

        StringBuffer uri = new StringBuffer();
        if (scheme != null) {
            uri.append(scheme);
            uri.append(':');
        }
        if (authority != null) {
            uri.append("//"); //$NON-NLS-1$
            // QUOTE ILLEGAL CHARS
            uri.append(quoteComponent(authority, "@[]" + someLegal)); //$NON-NLS-1$
        }

        if (path != null) {
            // QUOTE ILLEGAL CHARS
            uri.append(quoteComponent(path, "/@" + someLegal)); //$NON-NLS-1$
        }
        if (query != null) {
            // QUOTE ILLEGAL CHARS
            uri.append('?');
            uri.append(quoteComponent(query, allLegal));
        }
        if (fragment != null) {
            // QUOTE ILLEGAL CHARS
            uri.append('#');
            uri.append(quoteComponent(fragment, allLegal));
        }

        new Helper().parseURI(uri.toString(), false);
    }

    private class Helper {

        private void parseURI(String uri, boolean forceServer)
                throws URISyntaxException {
            String temp = uri;
            // assign uri string to the input value per spec
            string = uri;
            int index, index1, index2, index3;
            // parse into Fragment, Scheme, and SchemeSpecificPart
            // then parse SchemeSpecificPart if necessary

            // Fragment
            index = temp.indexOf('#');
            if (index != -1) {
                // remove the fragment from the end
                fragment = temp.substring(index + 1);
                validateFragment(uri, fragment, index + 1);
                temp = temp.substring(0, index);
            }

            // Scheme and SchemeSpecificPart
            index = index1 = temp.indexOf(':');
            index2 = temp.indexOf('/');
            index3 = temp.indexOf('?');

            // if a '/' or '?' occurs before the first ':' the uri has no
            // specified scheme, and is therefore not absolute
            if (index != -1 && (index2 >= index || index2 == -1)
                    && (index3 >= index || index3 == -1)) {
                // the characters up to the first ':' comprise the scheme
                absolute = true;
                scheme = temp.substring(0, index);
                if (scheme.length() == 0) {
                    throw new URISyntaxException(uri, "Scheme expected", index);
                }
                validateScheme(uri, scheme, 0);
                schemespecificpart = temp.substring(index + 1);
                if (schemespecificpart.length() == 0) {
                    throw new URISyntaxException(uri, "Scheme-specific part expected", index + 1);
                }
            } else {
                absolute = false;
                schemespecificpart = temp;
            }

            if (scheme == null || schemespecificpart.length() > 0
                    && schemespecificpart.charAt(0) == '/') {
                opaque = false;
                // the URI is hierarchical

                // Query
                temp = schemespecificpart;
                index = temp.indexOf('?');
                if (index != -1) {
                    query = temp.substring(index + 1);
                    temp = temp.substring(0, index);
                    validateQuery(uri, query, index2 + 1 + index);
                }

                // Authority and Path
                if (temp.startsWith("//")) { //$NON-NLS-1$
                    index = temp.indexOf('/', 2);
                    if (index != -1) {
                        authority = temp.substring(2, index);
                        path = temp.substring(index);
                    } else {
                        authority = temp.substring(2);
                        if (authority.length() == 0 && query == null
                                && fragment == null) {
                            throw new URISyntaxException(uri,
                                    "Authority expected", uri.length());
                        }

                        path = ""; //$NON-NLS-1$
                        // nothing left, so path is empty (not null, path should
                        // never be null)
                    }

                    if (authority.length() == 0) {
                        authority = null;
                    } else {
                        validateAuthority(uri, authority, index1 + 3);
                    }
                } else { // no authority specified
                    path = temp;
                }

                int pathIndex = 0;
                if (index2 > -1) {
                    pathIndex += index2;
                }
                if (index > -1) {
                    pathIndex += index;
                }
                validatePath(uri, path, pathIndex);
            } else { // if not hierarchical, URI is opaque
                opaque = true;
                validateSsp(uri, schemespecificpart, index2 + 2 + index);
            }

            parseAuthority(forceServer);
        }

        private void validateScheme(String uri, String scheme, int index)
                throws URISyntaxException {
            // first char needs to be an alpha char
            char ch = scheme.charAt(0);
            if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))) {
                throw new URISyntaxException(uri, "Illegal character in scheme", 0);
            }

            try {
                URIEncoderDecoder.validateSimple(scheme, "+-."); //$NON-NLS-1$
            } catch (URISyntaxException e) {
                throw new URISyntaxException(uri, "Illegal character in scheme", index 
                        + e.getIndex());
            }
        }

        private void validateSsp(String uri, String ssp, int index)
                throws URISyntaxException {
            try {
                URIEncoderDecoder.validate(ssp, allLegal);
            } catch (URISyntaxException e) {
                throw new URISyntaxException(uri, e.getReason() + " in schemeSpecificPart", index + e.getIndex());
            }
        }

        private void validateAuthority(String uri, String authority, int index)
                throws URISyntaxException {
            try {
                URIEncoderDecoder.validate(authority, "@[]" + someLegal); //$NON-NLS-1$
            } catch (URISyntaxException e) {
                throw new URISyntaxException(uri, e.getReason() + " in authority",
                       index + e.getIndex());
            }
        }

        private void validatePath(String uri, String path, int index)
                throws URISyntaxException {
            try {
                URIEncoderDecoder.validate(path, "/@" + someLegal); //$NON-NLS-1$
            } catch (URISyntaxException e) {
                throw new URISyntaxException(uri, e.getReason() + " in path", index + e.getIndex());
            }
        }

        private void validateQuery(String uri, String query, int index)
                throws URISyntaxException {
            try {
                URIEncoderDecoder.validate(query, allLegal);
            } catch (URISyntaxException e) {
                throw new URISyntaxException(uri, e.getReason() + " in query", index + e.getIndex());

            }
        }

        private void validateFragment(String uri, String fragment, int index)
                throws URISyntaxException {
            try {
                URIEncoderDecoder.validate(fragment, allLegal);
            } catch (URISyntaxException e) {
                throw new URISyntaxException(uri, e.getReason() + " in fragment", index + e.getIndex());
            }
        }

        /**
         * determine the host, port and userinfo if the authority parses
         * successfully to a server based authority
         * 
         * behavour in error cases: if forceServer is true, throw
         * URISyntaxException with the proper diagnostic messages. if
         * forceServer is false assume this is a registry based uri, and just
         * return leaving the host, port and userinfo fields undefined.
         * 
         * and there are some error cases where URISyntaxException is thrown
         * regardless of the forceServer parameter e.g. malformed ipv6 address
         */
        private void parseAuthority(boolean forceServer)
                throws URISyntaxException {
            if (authority == null) {
                return;
            }

            String temp, tempUserinfo = null, tempHost = null;
            int index, hostindex = 0;
            int tempPort = -1;

            temp = authority;
            index = temp.indexOf('@');
            if (index != -1) {
                // remove user info
                tempUserinfo = temp.substring(0, index);
                validateUserinfo(authority, tempUserinfo, 0);
                temp = temp.substring(index + 1); // host[:port] is left
                hostindex = index + 1;
            }

            index = temp.lastIndexOf(':');
            int endindex = temp.indexOf(']');

            if (index != -1 && endindex < index) {
                // determine port and host
                tempHost = temp.substring(0, index);

                if (index < (temp.length() - 1)) { // port part is not empty
                    try {
                        tempPort = Integer.parseInt(temp.substring(index + 1));
                        if (tempPort < 0) {
                            if (forceServer) {
                                throw new URISyntaxException(
                                        authority,
                                        "Invalid port number", hostindex + index + 1); //$NON-NLS-1$
                            }
                            return;
                        }
                    } catch (NumberFormatException e) {
                        if (forceServer) {
                            throw new URISyntaxException(authority, "Invalid port number",
                                    hostindex + index + 1); //$NON-NLS-1$
                        }
                        return;
                    }
                }
            } else {
                tempHost = temp;
            }

            if (tempHost.equals("")) { //$NON-NLS-1$
                if (forceServer) {
                    throw new URISyntaxException(authority, "Expected host",
                            hostindex); //$NON-NLS-1$
                }
                return;
            }

            if (!isValidHost(forceServer, tempHost)) {
                return;
            }

            // this is a server based uri,
            // fill in the userinfo, host and port fields
            userinfo = tempUserinfo;
            host = tempHost;
            port = tempPort;
            serverAuthority = true;
        }

        private void validateUserinfo(String uri, String userinfo, int index)
                throws URISyntaxException {
            for (int i = 0; i < userinfo.length(); i++) {
                char ch = userinfo.charAt(i);
                if (ch == ']' || ch == '[') {
                    throw new URISyntaxException(uri, "Illegal character in userinfo",
                            index + i);
                }
            }
        }

        /**
         * distinguish between IPv4, IPv6, domain name and validate it based on
         * its type
         */
        private boolean isValidHost(boolean forceServer, String host)
                throws URISyntaxException {
            if (host.charAt(0) == '[') {
                // ipv6 address
                if (host.charAt(host.length() - 1) != ']') {
                    throw new URISyntaxException(host,
                            "Expected a closing square bracket for ipv6 address", 0);
                }
                if (!isValidIP6Address(host)) {
                    throw new URISyntaxException(host, "Malformed ipv6 address");
                }
                return true;
            }

            // '[' and ']' can only be the first char and last char
            // of the host name
            if (host.indexOf('[') != -1 || host.indexOf(']') != -1) {
                throw new URISyntaxException(host, "Illegal character in host name", 0);
            }

            int index = host.lastIndexOf('.');
            if (index < 0 || index == host.length() - 1
                    || !Character.isDigit(host.charAt(index + 1))) {
                // domain name
                if (isValidDomainName(host)) {
                    return true;
                }
                if (forceServer) {
                    throw new URISyntaxException(host,
                            "Illegal character in host name", 0);
                }
                return false;
            }

            // IPv4 address
            if (isValidIPv4Address(host)) {
                return true;
            }
            if (forceServer) {
                throw new URISyntaxException(host, "Malformed ipv4 address", 0);
            }
            return false;
        }

        private boolean isValidDomainName(String host) {
            try {
                URIEncoderDecoder.validateSimple(host, "-."); //$NON-NLS-1$
            } catch (URISyntaxException e) {
                return false;
            }

            String label = null;
            StringTokenizer st = new StringTokenizer(host, "."); //$NON-NLS-1$
            while (st.hasMoreTokens()) {
                label = st.nextToken();
                if (label.startsWith("-") || label.endsWith("-")) { //$NON-NLS-1$ //$NON-NLS-2$
                    return false;
                }
            }

            if (!label.equals(host)) {
                char ch = label.charAt(0);
                if (ch >= '0' && ch <= '9') {
                    return false;
                }
            }
            return true;
        }

        private boolean isValidIPv4Address(String host) {
            int index;
            int index2;
            try {
                int num;
                index = host.indexOf('.');
                num = Integer.parseInt(host.substring(0, index));
                if (num < 0 || num > 255) {
                    return false;
                }
                index2 = host.indexOf('.', index + 1);
                num = Integer.parseInt(host.substring(index + 1, index2));
                if (num < 0 || num > 255) {
                    return false;
                }
                index = host.indexOf('.', index2 + 1);
                num = Integer.parseInt(host.substring(index2 + 1, index));
                if (num < 0 || num > 255) {
                    return false;
                }
                num = Integer.parseInt(host.substring(index + 1));
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        private boolean isValidIP6Address(String ipAddress) {
            int length = ipAddress.length();
            boolean doubleColon = false;
            int numberOfColons = 0;
            int numberOfPeriods = 0;
            String word = ""; //$NON-NLS-1$
            char c = 0;
            char prevChar = 0;
            int offset = 0; // offset for [] ip addresses

            if (length < 2) {
                return false;
            }

            for (int i = 0; i < length; i++) {
                prevChar = c;
                c = ipAddress.charAt(i);
                switch (c) {

                    // case for an open bracket [x:x:x:...x]
                    case '[':
                        if (i != 0) {
                            return false; // must be first character
                        }
                        if (ipAddress.charAt(length - 1) != ']') {
                            return false; // must have a close ]
                        }
                        if ((ipAddress.charAt(1) == ':')
                                && (ipAddress.charAt(2) != ':')) {
                            return false;
                        }
                        offset = 1;
                        if (length < 4) {
                            return false;
                        }
                        break;

                    // case for a closed bracket at end of IP [x:x:x:...x]
                    case ']':
                        if (i != length - 1) {
                            return false; // must be last character
                        }
                        if (ipAddress.charAt(0) != '[') {
                            return false; // must have a open [
                        }
                        break;

                    // case for the last 32-bits represented as IPv4
                    // x:x:x:x:x:x:d.d.d.d
                    case '.':
                        numberOfPeriods++;
                        if (numberOfPeriods > 3) {
                            return false;
                        }
                        if (!isValidIP4Word(word)) {
                            return false;
                        }
                        if (numberOfColons != 6 && !doubleColon) {
                            return false;
                        }
                        // a special case ::1:2:3:4:5:d.d.d.d allows 7 colons
                        // with
                        // an IPv4 ending, otherwise 7 :'s is bad
                        if (numberOfColons == 7
                                && ipAddress.charAt(0 + offset) != ':'
                                && ipAddress.charAt(1 + offset) != ':') {
                            return false;
                        }
                        word = ""; //$NON-NLS-1$
                        break;

                    case ':':
                        numberOfColons++;
                        if (numberOfColons > 7) {
                            return false;
                        }
                        if (numberOfPeriods > 0) {
                            return false;
                        }
                        if (prevChar == ':') {
                            if (doubleColon) {
                                return false;
                            }
                            doubleColon = true;
                        }
                        word = ""; //$NON-NLS-1$
                        break;

                    default:
                        if (word.length() > 3) {
                            return false;
                        }
                        if (!isValidHexChar(c)) {
                            return false;
                        }
                        word += c;
                }
            }

            // Check if we have an IPv4 ending
            if (numberOfPeriods > 0) {
                if (numberOfPeriods != 3 || !isValidIP4Word(word)) {
                    return false;
                }
            } else {
                // If we're at then end and we haven't had 7 colons then there
                // is a problem unless we encountered a doubleColon
                if (numberOfColons != 7 && !doubleColon) {
                    return false;
                }

                // If we have an empty word at the end, it means we ended in
                // either a : or a .
                // If we did not end in :: then this is invalid
                if (word == "" && ipAddress.charAt(length - 1 - offset) != ':' //$NON-NLS-1$
                        && ipAddress.charAt(length - 2 - offset) != ':') {
                    return false;
                }
            }

            return true;
        }

        private boolean isValidIP4Word(String word) {
            char c;
            if (word.length() < 1 || word.length() > 3) {
                return false;
            }
            for (int i = 0; i < word.length(); i++) {
                c = word.charAt(i);
                if (!(c >= '0' && c <= '9')) {
                    return false;
                }
            }
            if (Integer.parseInt(word) > 255) {
                return false;
            }
            return true;
        }

        private boolean isValidHexChar(char c) {

            return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')
                    || (c >= 'a' && c <= 'f');
        }
    }

    /*
     * Quote illegal chars for each component, but not the others
     * 
     * @param component java.lang.String the component to be converted @param
     * legalset java.lang.String the legal character set allowed in the
     * component s @return java.lang.String the converted string
     */
    private String quoteComponent(String component, String legalset) {
        try {
            /*
             * Use a different encoder than URLEncoder since: 1. chars like "/",
             * "#", "@" etc needs to be preserved instead of being encoded, 2.
             * UTF-8 char set needs to be used for encoding instead of default
             * platform one
             */
            return URIEncoderDecoder.quoteIllegal(component, legalset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public int compareTo(Object o) throws ClassCastException {
        URI uri = (URI) o;
        int ret = 0;

        // compare schemes
        if (scheme == null && uri.scheme != null) {
            return -1;
        } else if (scheme != null && uri.scheme == null) {
            return 1;
        } else if (scheme != null && uri.scheme != null) {
            ret = scheme.compareToIgnoreCase(uri.scheme);
            if (ret != 0) {
                return ret;
            }
        }

        // compare opacities
        if (!opaque && uri.opaque) {
            return -1;
        } else if (opaque && !uri.opaque) {
            return 1;
        } else if (opaque && uri.opaque) {
            ret = schemespecificpart.compareTo(uri.schemespecificpart);
            if (ret != 0) {
                return ret;
            }
        } else {

            // otherwise both must be hierarchical

            // compare authorities
            if (authority != null && uri.authority == null) {
                return 1;
            } else if (authority == null && uri.authority != null) {
                return -1;
            } else if (authority != null && uri.authority != null) {
                if (host != null && uri.host != null) {
                    // both are server based, so compare userinfo, host, port
                    if (userinfo != null && uri.userinfo == null) {
                        return 1;
                    } else if (userinfo == null && uri.userinfo != null) {
                        return -1;
                    } else if (userinfo != null && uri.userinfo != null) {
                        ret = userinfo.compareTo(uri.userinfo);
                        if (ret != 0) {
                            return ret;
                        }
                    }

                    // userinfo's are the same, compare hostname
                    ret = host.compareToIgnoreCase(uri.host);
                    if (ret != 0) {
                        return ret;
                    }

                    // compare port
                    if (port != uri.port) {
                        return port - uri.port;
                    }
                } else { // one or both are registry based, compare the whole
                    // authority
                    ret = authority.compareTo(uri.authority);
                    if (ret != 0) {
                        return ret;
                    }
                }
            }

            // authorities are the same
            // compare paths
            ret = path.compareTo(uri.path);
            if (ret != 0) {
                return ret;
            }

            // compare queries

            if (query != null && uri.query == null) {
                return 1;
            } else if (query == null && uri.query != null) {
                return -1;
            } else if (query != null && uri.query != null) {
                ret = query.compareTo(uri.query);
                if (ret != 0) {
                    return ret;
                }
            }
        }

        // everything else is identical, so compare fragments
        if (fragment != null && uri.fragment == null) {
            return 1;
        } else if (fragment == null && uri.fragment != null) {
            return -1;
        } else if (fragment != null && uri.fragment != null) {
            ret = fragment.compareTo(uri.fragment);
            if (ret != 0) {
                return ret;
            }
        }

        // identical
        return 0;
    }

    public static URI create(String uri) {
        URI result = null;
        try {
            result = new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        return result;
    }

    private URI duplicate() {
        URI clone = new URI();
        clone.absolute = absolute;
        clone.authority = authority;
        clone.fragment = fragment;
        clone.host = host;
        clone.opaque = opaque;
        clone.path = path;
        clone.port = port;
        clone.query = query;
        clone.scheme = scheme;
        clone.schemespecificpart = schemespecificpart;
        clone.userinfo = userinfo;
        clone.serverAuthority = serverAuthority;
        return clone;
    }

    /*
     * Takes a string that may contain hex sequences like %F1 or %2b and
     * converts the hex values following the '%' to lowercase
     */
    private String convertHexToLowerCase(String s) {
        StringBuffer result = new StringBuffer(""); //$NON-NLS-1$
        if (s.indexOf('%') == -1) {
            return s;
        }

        int index = 0, previndex = 0;
        while ((index = s.indexOf('%', previndex)) != -1) {
            result.append(s.substring(previndex, index + 1));
            result.append(s.substring(index + 1, index + 3).toLowerCase());
            index += 3;
            previndex = index;
        }
        return result.toString();
    }

    /*
     * Takes two strings that may contain hex sequences like %F1 or %2b and
     * compares them, ignoring case for the hex values hex values must always
     * occur in pairs like above
     */
    private boolean equalsHexCaseInsensitive(String first, String second) {
        if (first.indexOf('%') != second.indexOf('%')) {
            return first.equals(second);
        }

        int index = 0, previndex = 0;
        while ((index = first.indexOf('%', previndex)) != -1
                && second.indexOf('%', previndex) == index) {
            boolean match = first.substring(previndex, index).equals(
                    second.substring(previndex, index));
            if (!match) {
                return false;
            }

            match = first.substring(index + 1, index + 3).equalsIgnoreCase(
                    second.substring(index + 1, index + 3));
            if (!match) {
                return false;
            }

            index += 3;
            previndex = index;
        }
        return first.substring(previndex).equals(second.substring(previndex));
    }

    public boolean equals(Object o) {
        if (!(o instanceof URI)) {
            return false;
        }
        URI uri = (URI) o;

        if (uri.fragment == null && fragment != null || uri.fragment != null
                && fragment == null) {
            return false;
        } else if (uri.fragment != null && fragment != null) {
            if (!equalsHexCaseInsensitive(uri.fragment, fragment)) {
                return false;
            }
        }

        if (uri.scheme == null && scheme != null || uri.scheme != null
                && scheme == null) {
            return false;
        } else if (uri.scheme != null && scheme != null) {
            if (!uri.scheme.equalsIgnoreCase(scheme)) {
                return false;
            }
        }

        if (uri.opaque && opaque) {
            return equalsHexCaseInsensitive(uri.schemespecificpart,
                    schemespecificpart);
        } else if (!uri.opaque && !opaque) {
            if (!equalsHexCaseInsensitive(path, uri.path)) {
                return false;
            }

            if (uri.query != null && query == null || uri.query == null
                    && query != null) {
                return false;
            } else if (uri.query != null && query != null) {
                if (!equalsHexCaseInsensitive(uri.query, query)) {
                    return false;
                }
            }

            if (uri.authority != null && authority == null
                    || uri.authority == null && authority != null) {
                return false;
            } else if (uri.authority != null && authority != null) {
                if (uri.host != null && host == null || uri.host == null
                        && host != null) {
                    return false;
                } else if (uri.host == null && host == null) {
                    // both are registry based, so compare the whole authority
                    return equalsHexCaseInsensitive(uri.authority, authority);
                } else { // uri.host != null && host != null, so server-based
                    if (!host.equalsIgnoreCase(uri.host)) {
                        return false;
                    }

                    if (port != uri.port) {
                        return false;
                    }

                    if (uri.userinfo != null && userinfo == null
                            || uri.userinfo == null && userinfo != null) {
                        return false;
                    } else if (uri.userinfo != null && userinfo != null) {
                        return equalsHexCaseInsensitive(userinfo, uri.userinfo);
                    } else {
                        return true;
                    }
                }
            } else {
                // no authority
                return true;
            }

        } else {
            // one is opaque, the other hierarchical
            return false;
        }
    }

    public String getAuthority() {
        return decode(authority);
    }

    /**
     * Returns the fragment component.
     * 
     * @return String
     */
    public String getFragment() {
        return decode(fragment);
    }

    /**
     * Returns the host component.
     * 
     * @return String
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the path component.
     * 
     * @return String
     */
    public String getPath() {
        return decode(path);
    }

    /**
     * Returns the port number.
     * 
     * @return int
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the query component.
     * 
     * @return String
     */
    public String getQuery() {
        return decode(query);
    }

    /**
     * Returns the authority component in raw form.
     * 
     * @return String
     */
    public String getRawAuthority() {
        return authority;
    }

    /**
     * Returns the fragment component in raw form.
     * 
     * @return String
     */
    public String getRawFragment() {
        return fragment;
    }

    /**
     * Returns the path component in raw form.
     * 
     * @return String
     */
    public String getRawPath() {
        return path;
    }

    /**
     * Returns the query component in raw form.
     * 
     * @return String
     */
    public String getRawQuery() {
        return query;
    }

    /**
     * Returns the scheme-specific part component in raw form.
     * 
     * @return String
     */
    public String getRawSchemeSpecificPart() {
        return schemespecificpart;
    }

    /**
     * Returns the user-info component in raw form.
     * 
     * @return String
     */
    public String getRawUserInfo() {
        return userinfo;
    }

    /**
     * Returns the scheme.
     * 
     * @return String
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Returns the scheme-specific part component.
     * 
     * @return String
     */
    public String getSchemeSpecificPart() {
        return decode(schemespecificpart);
    }

    /**
     * Returns the userinfo.
     * 
     * @return String
     */
    public String getUserInfo() {
        return decode(userinfo);
    }

    public int hashCode() {
        if (hash == -1) {
            hash = getHashString().hashCode();
        }
        return hash;
    }

    /**
     * Indicates whether this URI is absolute
     * 
     * @return boolean
     */
    public boolean isAbsolute() {
        return absolute;
    }

    /**
     * Indicates whether this URI is opaque
     * 
     * @return true if the URI is opaque, otherwise false
     */
    public boolean isOpaque() {
        return opaque;
    }

    /*
     * normalize path, and return the resulting string
     */
    private String normalize(String path) {
        // count the number of '/'s, to determine number of segments
        int index = -1;
        int pathlen = path.length();
        int size = 0;
        if (pathlen > 0 && path.charAt(0) != '/') {
            size++;
        }
        while ((index = path.indexOf('/', index + 1)) != -1) {
            if (index + 1 < pathlen && path.charAt(index + 1) != '/') {
                size++;
            }
        }

        String[] seglist = new String[size];
        boolean[] include = new boolean[size];

        // break the path into segments and store in the list
        int current = 0;
        int index2 = 0;
        index = (pathlen > 0 && path.charAt(0) == '/') ? 1 : 0;
        while ((index2 = path.indexOf('/', index + 1)) != -1) {
            seglist[current++] = path.substring(index, index2);
            index = index2 + 1;
        }

        // if current==size, then the last character was a slash
        // and there are no more segments
        if (current < size) {
            seglist[current] = path.substring(index);
        }

        // determine which segments get included in the normalized path
        for (int i = 0; i < size; i++) {
            include[i] = true;
            if (seglist[i].equals("..")) { //$NON-NLS-1$
                int remove = i - 1;
                // search back to find a segment to remove, if possible
                while (remove > -1 && !include[remove]) {
                    remove--;
                }
                // if we find a segment to remove, remove it and the ".."
                // segment
                if (remove > -1 && !seglist[remove].equals("..")) { //$NON-NLS-1$
                    include[remove] = false;
                    include[i] = false;
                }
            } else if (seglist[i].equals(".")) { //$NON-NLS-1$
                include[i] = false;
            }
        }

        // put the path back together
        StringBuffer newpath = new StringBuffer();
        if (path.startsWith("/")) { //$NON-NLS-1$
            newpath.append('/');
        }

        for (int i = 0; i < seglist.length; i++) {
            if (include[i]) {
                newpath.append(seglist[i]);
                newpath.append('/');
            }
        }

        // if we used at least one segment and the path previously ended with
        // a slash and the last segment is still used, then delete the extra
        // trailing '/'
        if (!path.endsWith("/") && seglist.length > 0 //$NON-NLS-1$
                && include[seglist.length - 1]) {
            newpath.deleteCharAt(newpath.length() - 1);
        }

        String result = newpath.toString();

        // check for a ':' in the first segment if one exists,
        // prepend "./" to normalize
        index = result.indexOf(':');
        index2 = result.indexOf('/');
        if (index != -1 && (index < index2 || index2 == -1)) {
            newpath.insert(0, "./"); //$NON-NLS-1$
            result = newpath.toString();
        }
        return result;
    }

    public URI normalize() {
        if (opaque) {
            return this;
        }
        String normalizedPath = normalize(path);
        // if the path is already normalized, return this
        if (path.equals(normalizedPath)) {
            return this;
        }
        // get an exact copy of the URI re-calculate the scheme specific part
        // since the path of the normalized URI is different from this URI.
        URI result = duplicate();
        result.path = normalizedPath;
        result.setSchemeSpecificPart();
        return result;
    }

    /**
     * Return this uri instance if it has already been determined as a
     * ServerAuthority Otherwise try to parse it again as a server authority to
     * produce a URISyntaxException with the proper diagnostic message.
     */
    public URI parseServerAuthority() throws URISyntaxException {
        if (!serverAuthority) {
            new Helper().parseAuthority(true);
        }
        return this;
    }

    public URI relativize(URI relative) {
        if (relative.opaque || opaque) {
            return relative;
        }

        if (scheme == null ? relative.scheme != null : !scheme
                .equals(relative.scheme)) {
            return relative;
        }

        if (authority == null ? relative.authority != null : !authority
                .equals(relative.authority)) {
            return relative;
        }

        // normalize both paths
        String thisPath = normalize(path);
        String relativePath = normalize(relative.path);

        /*
         * if the paths aren't equal, then we need to determine if this URI's
         * path is a parent path (begins with) the relative URI's path
         */
        if (!thisPath.equals(relativePath)) {
            // if this URI's path doesn't end in a '/', add one
            if (!thisPath.endsWith("/")) { //$NON-NLS-1$
                thisPath = thisPath + '/';
            }
            /*
             * if the relative URI's path doesn't start with this URI's path,
             * then just return the relative URI; the URIs have nothing in
             * common
             */
            if (!relativePath.startsWith(thisPath)) {
                return relative;
            }
        }

        URI result = new URI();
        result.fragment = relative.fragment;
        result.query = relative.query;
        // the result URI is the remainder of the relative URI's path
        result.path = relativePath.substring(thisPath.length());
        result.setSchemeSpecificPart();
        return result;
    }

    public URI resolve(URI relative) {
        if (relative.absolute || opaque) {
            return relative;
        }

        URI result;
        if (relative.path.equals("") && relative.scheme == null //$NON-NLS-1$
                && relative.authority == null && relative.query == null
                && relative.fragment != null) {
            // if the relative URI only consists of fragment,
            // the resolved URI is very similar to this URI,
            // except that it has the fragement from the relative URI.
            result = duplicate();
            result.fragment = relative.fragment;
            // no need to re-calculate the scheme specific part,
            // since fragment is not part of scheme specific part.
            return result;
        }

        if (relative.authority != null) {
            // if the relative URI has authority,
            // the resolved URI is almost the same as the relative URI,
            // except that it has the scheme of this URI.
            result = relative.duplicate();
            result.scheme = scheme;
            result.absolute = absolute;
        } else {
            // since relative URI has no authority,
            // the resolved URI is very similar to this URI,
            // except that it has the query and fragment of the relative URI,
            // and the path is different.
            result = duplicate();
            result.fragment = relative.fragment;
            result.query = relative.query;
            if (relative.path.startsWith("/")) { //$NON-NLS-1$
                result.path = relative.path;
            } else {
                // resolve a relative reference
                int endindex = path.lastIndexOf('/') + 1;
                result.path = normalize(path.substring(0, endindex)
                        + relative.path);
            }
            // re-calculate the scheme specific part since
            // query and path of the resolved URI is different from this URI.
            result.setSchemeSpecificPart();
        }
        return result;
    }

    /**
     * Helper method used to re-calculate the scheme specific part of the
     * resolved or normalized URIs
     */
    private void setSchemeSpecificPart() {
        // ssp = [//authority][path][?query]
        StringBuffer ssp = new StringBuffer();
        if (authority != null) {
            ssp.append("//" + authority); //$NON-NLS-1$
        }
        if (path != null) {
            ssp.append(path);
        }
        if (query != null) {
            ssp.append("?" + query); //$NON-NLS-1$
        }
        schemespecificpart = ssp.toString();
        // reset string, so that it can be re-calculated correctly when asked.
        string = null;
    }

    public URI resolve(String relative) {
        return resolve(create(relative));
    }

    /*
     * Encode unicode chars that are not part of US-ASCII char set into the
     * escaped form
     * 
     * i.e. The Euro currency symbol is encoded as "%E2%82%AC".
     * 
     * @param component java.lang.String the component to be converted @param
     * legalset java.lang.String the legal character set allowed in the
     * component s @return java.lang.String the converted string
     */
    private String encodeOthers(String s) {
        try {
            /*
             * Use a different encoder than URLEncoder since: 1. chars like "/",
             * "#", "@" etc needs to be preserved instead of being encoded, 2.
             * UTF-8 char set needs to be used for encoding instead of default
             * platform one 3. Only other chars need to be converted
             */
            return URIEncoderDecoder.encodeOthers(s);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private String decode(String s) {
        if (s == null) {
            return s;
        }

        try {
            return URIEncoderDecoder.decode(s);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public String toASCIIString() {
        return encodeOthers(toString());
    }

    public String toString() {
        if (string == null) {
            StringBuffer result = new StringBuffer();
            if (scheme != null) {
                result.append(scheme);
                result.append(':');
            }
            if (opaque) {
                result.append(schemespecificpart);
            } else {
                if (authority != null) {
                    result.append("//"); //$NON-NLS-1$
                    result.append(authority);
                }

                if (path != null) {
                    result.append(path);
                }

                if (query != null) {
                    result.append('?');
                    result.append(query);
                }
            }

            if (fragment != null) {
                result.append('#');
                result.append(fragment);
            }

            string = result.toString();
        }
        return string;
    }

    /*
     * Form a string from the components of this URI, similarly to the
     * toString() method. But this method converts scheme and host to lowercase,
     * and converts escaped octets to lowercase.
     */
    private String getHashString() {
        StringBuffer result = new StringBuffer();
        if (scheme != null) {
            result.append(scheme.toLowerCase());
            result.append(':');
        }
        if (opaque) {
            result.append(schemespecificpart);
        } else {
            if (authority != null) {
                result.append("//"); //$NON-NLS-1$
                if (host == null) {
                    result.append(authority);
                } else {
                    if (userinfo != null) {
                        result.append(userinfo + "@"); //$NON-NLS-1$
                    }
                    result.append(host.toLowerCase());
                    if (port != -1) {
                        result.append(":" + port); //$NON-NLS-1$
                    }
                }
            }

            if (path != null) {
                result.append(path);
            }

            if (query != null) {
                result.append('?');
                result.append(query);
            }
        }

        if (fragment != null) {
            result.append('#');
            result.append(fragment);
        }

        return convertHexToLowerCase(result.toString());
    }

    public URL toURL() throws MalformedURLException {
        if (!absolute) {
            throw new IllegalArgumentException("URI is not absolute" + ": " + toString());
        }
        return new URL(toString());
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        try {
            new Helper().parseURI(string, false);
        } catch (URISyntaxException e) {
            throw new IOException(e.toString());
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException,
            ClassNotFoundException {
        // call toString() to ensure the value of string field is calculated
        toString();
        out.defaultWriteObject();
    }
}

