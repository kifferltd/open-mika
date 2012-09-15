/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpcore/trunk/module-main/src/main/java/org/apache/http/entity/HttpEntityWrapper.java $
 * $Revision: 496070 $
 * $Date: 2007-01-14 04:18:34 -0800 (Sun, 14 Jan 2007) $
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

/**
 * Base class for wrapping entities.
 * Keeps a {@link #wrappedEntity wrappedEntity} and delegates all
 * calls to it. Implementations of wrapping entities can derive
 * from this class and need to override only those methods that
 * should not be delegated to the wrapped entity.
 *
 * @version $Revision: 496070 $
 * 
 * @since 4.0
 */
public class HttpEntityWrapper implements HttpEntity {

    /** The wrapped entity. */
    protected HttpEntity wrappedEntity;

    /**
     * Creates a new entity wrapper.
     *
     * @param wrapped   the entity to wrap
     */
    public HttpEntityWrapper(HttpEntity wrapped) {
        super();

        if (wrapped == null) {
            throw new IllegalArgumentException
                ("wrapped entity must not be null");
        }
        wrappedEntity = wrapped;

    } // constructor


    public boolean isRepeatable() {
        return wrappedEntity.isRepeatable();
    }

    public boolean isChunked() {
        return wrappedEntity.isChunked();
    }

    public long getContentLength() {
        return wrappedEntity.getContentLength();
    }

    public Header getContentType() {
        return wrappedEntity.getContentType();
    }

    public Header getContentEncoding() {
        return wrappedEntity.getContentEncoding();
    }

    public InputStream getContent()
        throws IOException {
        return wrappedEntity.getContent();
    }

    public void writeTo(OutputStream outstream)
        throws IOException {
        wrappedEntity.writeTo(outstream);
    }

    public boolean isStreaming() {
        return wrappedEntity.isStreaming();
    }

    public void consumeContent()
        throws IOException {
        wrappedEntity.consumeContent();
    }

} // class HttpEntityWrapper
