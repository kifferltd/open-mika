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
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package org.apache.harmony.crypto.tests.support;

import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;

/**
 * Additional class for verification of 
 * KeyGeneratorSpi and KeyGenerator functionality
 * 
 */

public class MyKeyGeneratorSpi  extends KeyGeneratorSpi {
    
    @Override
    protected SecretKey engineGenerateKey() {
        return null;
    }

    @Override
    protected void engineInit(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        if (params == null) {
            throw new InvalidAlgorithmParameterException("params is null");
        }
    }

    @Override
    protected void engineInit(int keysize, SecureRandom random) {
        if (keysize <= 77) {
            throw new IllegalArgumentException("Invalid keysize");
        }
    }

    @Override
    protected void engineInit(SecureRandom random) {
        throw new IllegalArgumentException("Invalid random");
    }
}
