/*
 * $HeadURL: http://svn.apache.org/repos/asf/httpcomponents/httpclient/trunk/module-client/src/main/java/org/apache/http/conn/EofSensorWatcher.java $
 * $Revision: 552264 $
 * $Date: 2007-07-01 02:37:47 -0700 (Sun, 01 Jul 2007) $
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

package org.apache.http.conn;

import java.io.InputStream;
import java.io.IOException;


/**
 * A watcher for {@link EofSensorInputStream EofSensorInputStream}.
 * Each stream will notify it's watcher at most once.
 *
 * @author <a href="mailto:rolandw at apache.org">Roland Weber</a>
 *
 *
 * <!-- empty lines to avoid svn diff problems -->
 * @version $Revision: 552264 $
 *
 * @since 4.0
 */
public interface EofSensorWatcher {

    /**
     * Indicates that EOF is detected.
     *
     * @param wrapped   the underlying stream which has reached EOF
     *
     * @return  <code>true</code> if <code>wrapped</code> should be closed,
     *          <code>false</code> if it should be left alone
     *
     * @throws IOException
     *         in case of an IO problem, for example if the watcher itself
     *         closes the underlying stream. The caller will leave the
     *         wrapped stream alone, as if <code>false</code> was returned.
     */
    boolean eofDetected(InputStream wrapped)
        throws IOException
        ;


    /**
     * Indicates that the {@link EofSensorInputStream stream} is closed.
     * This method will be called only if EOF was <i>not</i> detected
     * before closing. Otherwise, {@link #eofDetected eofDetected} is called.
     *
     * @param wrapped   the underlying stream which has not reached EOF
     *
     * @return  <code>true</code> if <code>wrapped</code> should be closed,
     *          <code>false</code> if it should be left alone
     *
     * @throws IOException
     *         in case of an IO problem, for example if the watcher itself
     *         closes the underlying stream. The caller will leave the
     *         wrapped stream alone, as if <code>false</code> was returned.
     */
    boolean streamClosed(InputStream wrapped)
        throws IOException
        ;


    /**
     * Indicates that the {@link EofSensorInputStream stream} is aborted.
     * This method will be called only if EOF was <i>not</i> detected
     * before aborting. Otherwise, {@link #eofDetected eofDetected} is called.
     * <p/>
     * This method will also be invoked when an input operation causes an
     * IOException to be thrown to make sure the input stream gets shut down. 
     *
     * @param wrapped   the underlying stream which has not reached EOF
     *
     * @return  <code>true</code> if <code>wrapped</code> should be closed,
     *          <code>false</code> if it should be left alone
     *
     * @throws IOException
     *         in case of an IO problem, for example if the watcher itself
     *         closes the underlying stream. The caller will leave the
     *         wrapped stream alone, as if <code>false</code> was returned.
     */
    boolean streamAbort(InputStream wrapped)
        throws IOException
        ;


} // interface EofSensorWatcher
