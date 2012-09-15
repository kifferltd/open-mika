/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpclient/trunk/module-client/src/main/java/org/apache/http/impl/auth/NTLMScheme.java $
 * $Revision: 655048 $
 * $Date: 2008-05-10 04:22:12 -0700 (Sat, 10 May 2008) $
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.impl.auth;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.NTCredentials;
import org.apache.http.impl.auth.AuthSchemeBase;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.CharArrayBuffer;

public class NTLMScheme extends AuthSchemeBase {

    enum State {
        UNINITIATED,
        CHALLENGE_RECEIVED,
        MSG_TYPE1_GENERATED,
        MSG_TYPE2_RECEVIED,
        MSG_TYPE3_GENERATED,
        FAILED,
    }
    
    private final NTLMEngine engine;
    
    private State state;
    private String challenge;
    
    public NTLMScheme(final NTLMEngine engine) {
        super();
        if (engine == null) {
            throw new IllegalArgumentException("NTLM engine may not be null");
        }
        this.engine = engine;
        this.state = State.UNINITIATED;
        this.challenge = null;
    }
    
    public String getSchemeName() {
        return "ntlm";
    }

    public String getParameter(String name) {
        // String parameters not supported
        return null;
    }

    public String getRealm() {
        // NTLM does not support the concept of an authentication realm
        return null;
    }

    public boolean isConnectionBased() {
        return true;
    }

    @Override
    protected void parseChallenge(
            final CharArrayBuffer buffer, int pos, int len) throws MalformedChallengeException {
        String challenge = buffer.substringTrimmed(pos, len);
        if (challenge.length() == 0) {
            if (this.state == State.UNINITIATED) {
                this.state = State.CHALLENGE_RECEIVED;
            } else {
                this.state = State.FAILED;
            }
            this.challenge = null;
        } else {
            this.state = State.MSG_TYPE2_RECEVIED;
            this.challenge = challenge;
        }
    }

    public Header authenticate(
            final Credentials credentials, 
            final HttpRequest request) throws AuthenticationException {
        NTCredentials ntcredentials = null;
        try {
            ntcredentials = (NTCredentials) credentials;
        } catch (ClassCastException e) {
            throw new InvalidCredentialsException(
             "Credentials cannot be used for NTLM authentication: " 
              + credentials.getClass().getName());
        }
        String response = null;
        if (this.state == State.CHALLENGE_RECEIVED || this.state == State.FAILED) {
            response = this.engine.generateType1Msg(
                    ntcredentials.getDomain(), 
                    ntcredentials.getWorkstation());
            this.state = State.MSG_TYPE1_GENERATED;
        } else if (this.state == State.MSG_TYPE2_RECEVIED) {
            response = this.engine.generateType3Msg(
                    ntcredentials.getUserName(), 
                    ntcredentials.getPassword(), 
                    ntcredentials.getDomain(), 
                    ntcredentials.getWorkstation(),
                    this.challenge);
            this.state = State.MSG_TYPE3_GENERATED;
        } else {
            throw new AuthenticationException("Unexpected state: " + this.state);
        }
        CharArrayBuffer buffer = new CharArrayBuffer(32);
        if (isProxy()) {
            buffer.append(AUTH.PROXY_AUTH_RESP);
        } else {
            buffer.append(AUTH.WWW_AUTH_RESP);
        }
        buffer.append(": NTLM ");
        buffer.append(response);
        return new BufferedHeader(buffer);
    }

    public boolean isComplete() {
        return this.state == State.MSG_TYPE3_GENERATED || this.state == State.FAILED;
    }

}
