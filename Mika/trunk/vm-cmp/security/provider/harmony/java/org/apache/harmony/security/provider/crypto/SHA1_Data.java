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
/**
* @author Yuri A. Kropachev
* @version $Revision$
*/

/*
 * Imported by CG 20090513 based on Apache Harmony ("enhanced") revision 476395.
 * Removed provision for using a native library instead of /dev/[u]random.
 */


package org.apache.harmony.security.provider.crypto;


/**
 * This interface contains : <BR>
 * - a set of constant values, H0-H4, defined in "SECURE HASH STANDARD", FIPS PUB 180-2 ;<BR>
 * - implementation constant values to use in classes using SHA-1 algorithm.    <BR>
 */


public interface SHA1_Data {


    /**
     *  constant defined in "SECURE HASH STANDARD"
     */
    static final int H0 = 0x67452301;


    /**
     *  constant defined in "SECURE HASH STANDARD"
     */
    static final int H1 = 0xEFCDAB89;


    /**
     *  constant defined in "SECURE HASH STANDARD"
     */
    static final int H2 = 0x98BADCFE;


    /**
     *  constant defined in "SECURE HASH STANDARD"
     */
    static final int H3 = 0x10325476;


    /**
     *  constant defined in "SECURE HASH STANDARD"
     */
    static final int H4 = 0xC3D2E1F0;


    /**
     * offset in buffer to store number of bytes in 0-15 word frame
     */
    static final int BYTES_OFFSET = 81;


    /**
     * offset in buffer to store current hash value
     */
    static final int HASH_OFFSET = 82;

    /**
     * # of bytes in H0-H4 words; <BR>
     * in this implementation # is set to 20 (in general # varies from 1 to 20)
     */
    static final int DIGEST_LENGTH = 20;

    /**
     *  names of random devices on Linux platform
     */
    static final String DEVICE_NAMES[] = { "/dev/urandom", "/dev/random"};
}

