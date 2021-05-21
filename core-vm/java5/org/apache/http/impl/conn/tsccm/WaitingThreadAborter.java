/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpclient/trunk/module-client/src/main/java/org/apache/http/impl/conn/tsccm/WaitingThreadAborter.java $
 * $Revision: 649220 $
 * $Date: 2008-04-17 11:40:24 -0700 (Thu, 17 Apr 2008) $
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

package org.apache.http.impl.conn.tsccm;

/** A simple class that can interrupt a {@link WaitingThread}. */
public class WaitingThreadAborter {
    
    private WaitingThread waitingThread;
    private boolean aborted;
    
    /**
     * If a waiting thread has been set, interrupts it.
     */
    public void abort() {
        aborted = true;
        
        if (waitingThread != null)
            waitingThread.interrupt();
        
    }
    
    /**
     * Sets the waiting thread.  If this has already been aborted,
     * the waiting thread is immediately interrupted.
     * 
     * @param waitingThread The thread to interrupt when aborting.
     */
    public void setWaitingThread(WaitingThread waitingThread) {
        this.waitingThread = waitingThread;
        if (aborted)
            waitingThread.interrupt();
    }

}
