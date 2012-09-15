/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.nio.internal;

import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelectionKey;

/*
 * Default implementation of SelectionKey
 */
final class SelectionKeyImpl extends AbstractSelectionKey {

    private AbstractSelectableChannel channel;

    int oldInterestOps;

    private int interestOps;

    private int readyOps;

    private SelectorImpl selector;

    public SelectionKeyImpl(AbstractSelectableChannel channel, int operations,
            Object attachment, SelectorImpl selector) {
        super();
        this.channel = channel;
        interestOps = operations;
        this.selector = selector;
        attach(attachment);
    }

    public SelectableChannel channel() {
        return channel;
    }

    public int interestOps() {
        checkValid();
        synchronized (selector.keysLock) {
            return interestOps;
        }
    }

    public SelectionKey interestOps(int operations) {
        checkValid();
        if ((operations & ~(channel().validOps())) != 0) {
            throw new IllegalArgumentException();
        }
        synchronized (selector.keysLock) {
            interestOps = operations;
        }
        return this;
    }

    public int readyOps() {
        checkValid();
        return readyOps;
    }

    public Selector selector() {
        return selector;
    }

    /*
     * package private method for setting the ready operation by selector
     */
    void setReadyOps(int readyOps) {
        this.readyOps = readyOps;
    }

    private void checkValid() {
        if (!isValid()) {
            throw new CancelledKeyException();
        }
    }

}