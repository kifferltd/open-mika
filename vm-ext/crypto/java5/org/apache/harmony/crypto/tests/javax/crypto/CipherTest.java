/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.crypto.tests.javax.crypto;

//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetClass;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargets;

import org.apache.harmony.crypto.tests.support.MyCipher;

import tests.support.resource.Support_Resources;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

//@TestTargetClass(Cipher.class)
public class CipherTest extends junit.framework.TestCase {

    static Key cipherKey;
    static final String algorithm = "DESede";
    static final int keyLen = 168;

    static Key cipherKeyDES;
    static final String algorithmDES = "DES";
    static final int keyLenDES = 56;
    
    static {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(algorithm);
            kg.init(keyLen, new SecureRandom());
            cipherKey = kg.generateKey();

            kg = KeyGenerator.getInstance(algorithmDES);
            kg.init(keyLenDES, new SecureRandom());
            cipherKeyDES = kg.generateKey();
        } catch (Exception e) {
            fail("No key " + e);
        }
    }
    
    /**
     * @tests javax.crypto.Cipher#getInstance(java.lang.String)
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getInstance",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineSetMode",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineSetPadding",
            args = {java.lang.String.class}
        )
    })
     */
    public void test_getInstanceLjava_lang_String() throws Exception {
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        assertNotNull("Received a null Cipher instance", cipher);
        
        try {
            Cipher.getInstance("WrongAlgorithmName");
            fail("NoSuchAlgorithmException expected");
        } catch (NoSuchAlgorithmException e) {
            //expected
        }
//        RI throws  NoSuchAlgorithmException for wrong padding.
    }

    /**
     * @tests javax.crypto.Cipher#getInstance(java.lang.String,
     *        java.lang.String)
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getInstance",
            args = {java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineSetMode",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineSetPadding",
            args = {java.lang.String.class}
        )
    })
     */
    public void test_getInstanceLjava_lang_StringLjava_lang_String()
            throws Exception {

        Provider[] providers = Security.getProviders("Cipher.DES");

        assertNotNull("No installed providers support Cipher.DES", providers);

        for (int i = 0; i < providers.length; i++) {
            Cipher cipher = Cipher.getInstance("DES", providers[i].getName());
            assertNotNull("Cipher.getInstance() returned a null value", cipher);

            // Exception case
            try {
                cipher = Cipher.getInstance("DoBeDoBeDo", providers[i]);
                fail("Should have thrown an NoSuchAlgorithmException");
            } catch (NoSuchAlgorithmException e) {
                // Expected
            }
        }

        // Exception case
        try {
            Cipher.getInstance("DES", (String) null);
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Exception case
        try {
            Cipher.getInstance("DES", "IHaveNotBeenConfigured");
            fail("Should have thrown an NoSuchProviderException");
        } catch (NoSuchProviderException e) {
            // Expected
        }
//      RI throws  NoSuchAlgorithmException for wrong padding.
    }

    /**
     * @tests javax.crypto.Cipher#getInstance(java.lang.String,
     *        java.security.Provider)
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getInstance",
            args = {java.lang.String.class, java.security.Provider.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineSetMode",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineSetPadding",
            args = {java.lang.String.class}
        )
    })
     */
    public void test_getInstanceLjava_lang_StringLjava_security_Provider()
            throws Exception {

        Provider[] providers = Security.getProviders("Cipher.DES");

        assertNotNull("No installed providers support Cipher.DES", providers);

        for (int i = 0; i < providers.length; i++) {
            Cipher cipher = Cipher.getInstance("DES", providers[i]);
            assertNotNull("Cipher.getInstance() returned a null value", cipher);
        }
        
        // Exception case
        try {
            Cipher.getInstance("DES", (Provider) null);
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Exception case
        try {
            Cipher.getInstance("WrongAlg", providers[0]);
            fail("NoSuchAlgorithmException expected");
        } catch (NoSuchAlgorithmException e) {
            // Expected
        }
//      RI throws  NoSuchAlgorithmException for wrong padding.
    }

    /**
     * @tests javax.crypto.Cipher#getProvider()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
     */
    public void test_getProvider() throws Exception {

        Provider[] providers = Security.getProviders("Cipher.AES");

        assertNotNull("No providers support Cipher.AES", providers);

        for (int i = 0; i < providers.length; i++) {
            Provider provider = providers[i];
            Cipher cipher = Cipher.getInstance("AES", provider.getName());
            Provider cipherProvider = cipher.getProvider();
            assertTrue("Cipher provider is not the same as that "
                    + "provided as parameter to getInstance()", cipherProvider
                    .equals(provider));
        }
    }

    /**
     * @tests javax.crypto.Cipher#getAlgorithm()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
     */
    public void test_getAlgorithm() throws Exception {
        final String algorithm = "DESede/CBC/PKCS5Padding";

        Cipher cipher = Cipher.getInstance(algorithm);
        assertTrue("Cipher algorithm does not match", cipher.getAlgorithm()
                .equals(algorithm));
    }

    /**
     * @tests javax.crypto.Cipher#getBlockSize()
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getBlockSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineGetBlockSize",
            args = {}
        )
    })
     */
    public void test_getBlockSize() throws Exception {
        final String algorithm = "DESede/CBC/PKCS5Padding";

        Cipher cipher = Cipher.getInstance(algorithm);
        assertEquals("Block size does not match", 8, cipher.getBlockSize());
    }

    /**
     * @tests javax.crypto.Cipher#getOutputSize(int)
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getOutputSize",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineGetOutputSize",
            args = {int.class}
        )
    })
     */
    public void test_getOutputSizeI() throws Exception {

        SecureRandom sr = new SecureRandom();
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");
        
        try {
            cipher.getOutputSize(25);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }
        
        cipher.init(Cipher.ENCRYPT_MODE, cipherKey, sr);

        // A 25-byte input could result in at least 4 8-byte blocks
        int result = cipher.getOutputSize(25);
        assertTrue("Output size too small", result > 31);

        // A 8-byte input should result in 2 8-byte blocks
        result = cipher.getOutputSize(8);
        assertTrue("Output size too small", result > 15);
    }

    /**
     * @tests javax.crypto.Cipher#init(int, java.security.Key)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "init",
        args = {int.class, java.security.Key.class}
    )
     */
    public void test_initILjava_security_Key() throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, cipherKey);

        
        cipher = Cipher.getInstance("DES/CBC/NoPadding");
        try {
            cipher.init(Cipher.ENCRYPT_MODE, cipherKey);
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
            //expected
        }
    }

    /**
     * @tests javax.crypto.Cipher#init(int, java.security.Key,
     *        java.security.SecureRandom)
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "init",
            args = {int.class, java.security.Key.class, java.security.SecureRandom.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineInit",
            args = {int.class, java.security.Key.class, java.security.SecureRandom.class}
        )
    })
     */
    public void test_initILjava_security_KeyLjava_security_SecureRandom()
            throws Exception {
        SecureRandom sr = new SecureRandom();
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, cipherKey, sr);

        cipher = Cipher.getInstance("DES/CBC/NoPadding");
        try {
            cipher.init(Cipher.ENCRYPT_MODE, cipherKey, sr);
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
            //expected
        }
    }

    /**
     * @tests javax.crypto.Cipher#init(int, java.security.Key,
     *        java.security.spec.AlgorithmParameterSpec)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "init",
        args = {int.class, java.security.Key.class, java.security.spec.AlgorithmParameterSpec.class}
    )
     */
    public void test_initILjava_security_KeyLjava_security_spec_AlgorithmParameterSpec()
            throws Exception {
        SecureRandom sr = new SecureRandom();
        Cipher cipher = null;

        byte[] iv = null;
        AlgorithmParameterSpec ap = null;

        iv = new byte[8];
        sr.nextBytes(iv);
        ap = new IvParameterSpec(iv);

        cipher = Cipher.getInstance(algorithm + "/CBC/PKCS5Padding");

        cipher.init(Cipher.ENCRYPT_MODE, cipherKey, ap);

        byte[] cipherIV = cipher.getIV();

        assertTrue("IVs differ", Arrays.equals(cipherIV, iv));

        cipher = Cipher.getInstance("DES/CBC/NoPadding");
        try {
            cipher.init(Cipher.ENCRYPT_MODE, cipherKey, ap);
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
            //expected
        }

        cipher = Cipher.getInstance("DES/CBC/NoPadding");
        ap = new RSAKeyGenParameterSpec(10, new BigInteger("10")); 

        try {
            cipher.init(Cipher.ENCRYPT_MODE, cipherKeyDES, ap);
            fail("InvalidAlgorithmParameterException expected");
        } catch (InvalidAlgorithmParameterException e) {
            //expected
        }
    }

    /**
     * @tests javax.crypto.Cipher#init(int, java.security.Key,
     *        java.security.spec.AlgorithmParameterSpec,
     *        java.security.SecureRandom)
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "init",
            args = {int.class, java.security.Key.class, java.security.spec.AlgorithmParameterSpec.class, java.security.SecureRandom.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineInit",
            args = {int.class, java.security.Key.class, java.security.spec.AlgorithmParameterSpec.class, java.security.SecureRandom.class}
        )
    })
     */
    public void test_initILjava_security_KeyLjava_security_spec_AlgorithmParameterSpecLjava_security_SecureRandom()
            throws Exception {
        SecureRandom sr = new SecureRandom();
        Cipher cipher = null;

        byte[] iv = null;
        AlgorithmParameterSpec ap = null;

        iv = new byte[8];
        sr.nextBytes(iv);
        ap = new IvParameterSpec(iv);

        cipher = Cipher.getInstance(algorithm + "/CBC/PKCS5Padding");

        cipher.init(Cipher.ENCRYPT_MODE, cipherKey, ap, sr);

        byte[] cipherIV = cipher.getIV();

        assertTrue("IVs differ", Arrays.equals(cipherIV, iv));
        cipher = Cipher.getInstance("DES/CBC/NoPadding");
        try {
            cipher.init(Cipher.ENCRYPT_MODE, cipherKey, ap, sr);
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
            //expected
        }

        cipher = Cipher.getInstance("DES/CBC/NoPadding");
        ap = new RSAKeyGenParameterSpec(10, new BigInteger("10")); 

        try {
            cipher.init(Cipher.ENCRYPT_MODE, cipherKeyDES, ap, sr);
            fail("InvalidAlgorithmParameterException expected");
        } catch (InvalidAlgorithmParameterException e) {
            //expected
        }
    }

    /**
     * @tests javax.crypto.Cipher#update(byte[], int, int)
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "update",
            args = {byte[].class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineUpdate",
            args = {byte[].class, int.class, int.class}
        )
    })
     */
    public void test_update$BII() throws Exception {
        for (int index = 1; index < 4; index++) {
            Cipher c = Cipher.getInstance("DESEDE/CBC/PKCS5Padding");

            byte[] keyMaterial = loadBytes("hyts_" + "des-ede3-cbc.test"
                    + index + ".key");
            DESedeKeySpec keySpec = new DESedeKeySpec(keyMaterial);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DESEDE");
            Key k = skf.generateSecret(keySpec);

            byte[] ivMaterial = loadBytes("hyts_" + "des-ede3-cbc.test" + index
                    + ".iv");
            IvParameterSpec iv = new IvParameterSpec(ivMaterial);

            c.init(Cipher.DECRYPT_MODE, k, iv);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] input = new byte[256];
            String resPath = "hyts_" + "des-ede3-cbc.test" + index
                    + ".ciphertext";
            File resources = Support_Resources.createTempFolder();
            Support_Resources.copyFile(resources, null, resPath);
            InputStream is = Support_Resources.getStream(resPath);

            int bytesRead = is.read(input, 0, 256);
            while (bytesRead > 0) {
                byte[] output = c.update(input, 0, bytesRead);
                if (output != null) {
                    baos.write(output);
                }
                bytesRead = is.read(input, 0, 256);
            }

            byte[] output = c.doFinal();
            if (output != null) {
                baos.write(output);
            }

            byte[] decipheredCipherText = baos.toByteArray();
            is.close();

            byte[] plaintextBytes = loadBytes("hyts_" + "des-ede3-cbc.test"
                    + index + ".plaintext");
            assertTrue("Operation produced incorrect results", Arrays.equals(
                    plaintextBytes, decipheredCipherText));
        }// end for
        
        Cipher cipher = Cipher.getInstance("DESEDE/CBC/PKCS5Padding");
        try {
            cipher.update(new byte[64], 0, 32);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }
    }

    /**
     * @tests javax.crypto.Cipher#doFinal()
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "doFinal",
        args = {}
    )
     */
    public void test_doFinal() throws Exception {
        for (int index = 1; index < 4; index++) {
            Cipher c = Cipher.getInstance("DESEDE/CBC/PKCS5Padding");

            byte[] keyMaterial = loadBytes("hyts_" + "des-ede3-cbc.test"
                    + index + ".key");
            DESedeKeySpec keySpec = new DESedeKeySpec(keyMaterial);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DESEDE");
            Key k = skf.generateSecret(keySpec);

            byte[] ivMaterial = loadBytes("hyts_" + "des-ede3-cbc.test" + index
                    + ".iv");
            IvParameterSpec iv = new IvParameterSpec(ivMaterial);

            c.init(Cipher.ENCRYPT_MODE, k, iv);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] input = new byte[256];
            String resPath = "hyts_" + "des-ede3-cbc.test" + index
                    + ".plaintext";
            File resources = Support_Resources.createTempFolder();
            Support_Resources.copyFile(resources, null, resPath);
            InputStream is = Support_Resources.getStream(resPath);

            int bytesRead = is.read(input, 0, 256);
            while (bytesRead > 0) {
                byte[] output = c.update(input, 0, bytesRead);
                if (output != null) {
                    baos.write(output);
                }
                bytesRead = is.read(input, 0, 256);
            }
            byte[] output = c.doFinal();
            if (output != null) {
                baos.write(output);
            }
            byte[] encryptedPlaintext = baos.toByteArray();
            is.close();

            byte[] cipherText = loadBytes("hyts_" + "des-ede3-cbc.test" + index
                    + ".ciphertext");
            assertTrue("Operation produced incorrect results", Arrays.equals(
                    encryptedPlaintext, cipherText));
        }// end for

        byte[] b = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        byte[] b1 = new byte[30];

        Cipher c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        c.update(b, 0, 10, b1, 5);
        try {
            c.doFinal();
            fail("IllegalBlockSizeException expected");
        } catch (IllegalBlockSizeException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        try {
            c.doFinal();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        c.doFinal(b, 0, 16, b1, 0);

        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[8];
        sr.nextBytes(iv);
        AlgorithmParameterSpec ap = new IvParameterSpec(iv);

        c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, cipherKeyDES, ap);
        
        c.update(b1, 0, 24, b, 0);
        try {
            c.doFinal();
            fail("BadPaddingException expected");
        } catch (BadPaddingException e) {
            //expected
        }
    }

    private byte[] loadBytes(String resPath) {
        try {
            File resources = Support_Resources.createTempFolder();
            Support_Resources.copyFile(resources, null, resPath);
            InputStream is = Support_Resources.getStream(resPath);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int readlen;
            while ((readlen = is.read(buff)) > 0) {
                out.write(buff, 0, readlen);
            }
            is.close();
            return out.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
    
     /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getParameters",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineGetParameters",
            args = {}
        )
    })
     */
    public void testGetParameters() throws Exception {
        Cipher c = Cipher.getInstance("DES");
        assertNull(c.getParameters());
    }
    
    /*
     * Class under test for int update(byte[], int, int, byte[], int)
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "update",
            args = {byte[].class, int.class, int.class, byte[].class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineUpdate",
            args = {byte[].class, int.class, int.class, byte[].class, int.class}
        )
    })
     */
    public void testUpdatebyteArrayintintbyteArrayint() throws Exception {
        byte[] b = {1,2,3,4,5,6,7,8,9,10};
        byte[] b1 = new byte[6];
        Cipher c = Cipher.getInstance("DESede");

        try {
            c.update(b, 0, 10, b1, 5);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }

        c.init(Cipher.ENCRYPT_MODE, cipherKey);
        try {
            c.update(b, 0, 10, b1, 5);
            fail("ShortBufferException expected");
        } catch (ShortBufferException e) {
            //expected
        }
        
        b1 = new byte[30];
        c.update(b, 0, 10, b1, 5);
    }
    
    /*
     * Class under test for int doFinal(byte[], int, int, byte[], int)
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "doFinal",
            args = {byte[].class, int.class, int.class, byte[].class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineDoFinal",
            args = {byte[].class, int.class, int.class, byte[].class, int.class}
        )
    })
     */
    public void testDoFinalbyteArrayintintbyteArrayint() throws Exception {
        byte[] b = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        byte[] b1 = new byte[30];
        
        Cipher c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        try {
            c.doFinal(b, 0, 10, b1, 5);
            fail("IllegalBlockSizeException expected");
        } catch (IllegalBlockSizeException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        try {
            c.doFinal(b, 0, 10, b1, 5);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        c.doFinal(b, 0, 16, b1, 0);

        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[8];
        sr.nextBytes(iv);
        AlgorithmParameterSpec ap = new IvParameterSpec(iv);

        c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, cipherKeyDES, ap);
        
        try {
            c.doFinal(b1, 0, 24, new byte[42], 0);
            fail("BadPaddingException expected");
        } catch (BadPaddingException e) {
            //expected
        }

        b1 = new byte[6];
        c = Cipher.getInstance("DESede");
        c.init(Cipher.ENCRYPT_MODE, cipherKey);
        try {
            c.doFinal(b, 3, 6, b1, 5);
            fail("No expected ShortBufferException");
        } catch (ShortBufferException e) {
            //expected
        }
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxAllowedKeyLength",
        args = {java.lang.String.class}
    )
     */
    public void testGetMaxAllowedKeyLength() throws NoSuchAlgorithmException {
        try {
            Cipher.getMaxAllowedKeyLength(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            Cipher.getMaxAllowedKeyLength("//CBC/PKCS5Paddin");
            fail("No expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            Cipher.getMaxAllowedKeyLength("/DES/CBC/PKCS5Paddin/1");
            fail("No expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        }
        assertTrue(Cipher.getMaxAllowedKeyLength("/DES/CBC/PKCS5Paddin") > 0);
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxAllowedParameterSpec",
        args = {java.lang.String.class}
    )
     */
    public void testGetMaxAllowedParameterSpec()
            throws NoSuchAlgorithmException, Exception {
        try {
            Cipher.getMaxAllowedParameterSpec(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            Cipher.getMaxAllowedParameterSpec("/DES//PKCS5Paddin");
            fail("No expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            Cipher.getMaxAllowedParameterSpec("/DES/CBC/ /1");
            fail("No expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        }
        Cipher.getMaxAllowedParameterSpec("DES/CBC/PKCS5Paddin");
        Cipher.getMaxAllowedParameterSpec("RSA");
    }

    /**
     * @tests javax.crypto.Cipher#Cipher(CipherSpi cipherSpi, Provider provider,
     *        String transformation)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Cipher",
        args = {javax.crypto.CipherSpi.class, java.security.Provider.class, java.lang.String.class}
    )
     */
    public void test_Ctor() throws Exception {
        // Regression for Harmony-1184
        try {
            new testCipher(null, null, "s");
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            new testCipher(new MyCipher(), null, "s");
            fail("NullPointerException expected for 'null' provider");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            new testCipher(null, new Provider("qwerty", 1.0, "qwerty") {}, "s");
            fail("NullPointerException expected for 'null' cipherSpi");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "doFinal",
            args = {java.nio.ByteBuffer.class, java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineDoFinal",
            args = {java.nio.ByteBuffer.class, java.nio.ByteBuffer.class}
        )
    })
     */
    public void test_doFinalLjava_nio_ByteBufferLjava_nio_ByteBuffer () 
    throws NoSuchAlgorithmException, NoSuchPaddingException,
    InvalidKeyException, ShortBufferException, BadPaddingException,
    IllegalBlockSizeException, InvalidAlgorithmParameterException {
        byte[] b = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        ByteBuffer bInput = ByteBuffer.allocate(64);
        ByteBuffer bOutput = ByteBuffer.allocate(64);
        
        Cipher c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        bInput.put(b, 0, 10);
        try {
            c.doFinal(bInput, bOutput);
            fail("IllegalBlockSizeException expected");
        } catch (IllegalBlockSizeException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        try {
            c.doFinal(bInput, bOutput);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        bInput = ByteBuffer.allocate(16);
        bInput.put(b, 0, 16);
        c.doFinal(bInput, bOutput);

        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[8];
        sr.nextBytes(iv);
        AlgorithmParameterSpec ap = new IvParameterSpec(iv);

        c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, cipherKeyDES, ap);
        bInput = ByteBuffer.allocate(64);

        try {
            c.doFinal(bOutput, bInput);
            fail("BadPaddingException expected");
        } catch (BadPaddingException e) {
            //expected
        }
        
        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        bInput.put(b, 0, 16);
        try {
            c.doFinal(bInput, bInput);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        bInput.put(b, 0, 16);
        try {
            c.doFinal(bInput, bOutput.asReadOnlyBuffer());
            fail("ReadOnlyBufferException expected");
        } catch (ReadOnlyBufferException e) {
            //expected
        }

        bInput.rewind();
        bInput.put(b, 0, 16);
        bOutput = ByteBuffer.allocate(8);
        c = Cipher.getInstance("DESede");
        c.init(Cipher.ENCRYPT_MODE, cipherKey);
        try {
            c.doFinal(bInput, bOutput);
            fail("No expected ShortBufferException");
        } catch (ShortBufferException e) {
            //expected
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "init",
        args = {int.class, java.security.Key.class, java.security.AlgorithmParameters.class}
    )
     */
    public void test_initILjava_security_KeyLjava_security_AlgorithmParameters ()
    throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
    InvalidAlgorithmParameterException {
        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[8];
        sr.nextBytes(iv);
        AlgorithmParameterSpec ap = new IvParameterSpec(iv);

        Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, cipherKeyDES, ap);
        assertNotNull(c.getParameters());
        
        try {
            c.init(Cipher.DECRYPT_MODE, cipherKey, ap);
            fail("InvalidKeyException e");
        } catch (InvalidKeyException e) {
            //expected
        }
        
        try {
            c.init(Cipher.DECRYPT_MODE, cipherKeyDES, (AlgorithmParameters)null);
            fail("InvalidAlgorithmParameterException e");
        } catch (InvalidAlgorithmParameterException e) {
            //expected
        }
    }

    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "init",
            args = {int.class, java.security.Key.class, java.security.AlgorithmParameters.class, java.security.SecureRandom.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineInit",
            args = {int.class, java.security.Key.class, java.security.AlgorithmParameters.class, java.security.SecureRandom.class}
        )
    })
     */
    public void test_initILjava_security_KeyLjava_security_AlgorithmParametersLjava_security_SecureRandom ()
    throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
    InvalidAlgorithmParameterException {
        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[8];
        sr.nextBytes(iv);
        AlgorithmParameterSpec ap = new IvParameterSpec(iv);

        Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, cipherKeyDES, ap, sr);
        assertNotNull(c.getParameters());
        
        try {
            c.init(Cipher.DECRYPT_MODE, cipherKey, ap, new SecureRandom());
            fail("InvalidKeyException e");
        } catch (InvalidKeyException e) {
            //expected
        }
        
        try {
            c.init(Cipher.DECRYPT_MODE, cipherKeyDES, (AlgorithmParameters)null, sr);
            fail("InvalidAlgorithmParameterException e");
        } catch (InvalidAlgorithmParameterException e) {
            //expected
        }

        c.init(Cipher.DECRYPT_MODE, cipherKeyDES, ap, (SecureRandom)null);
        assertNotNull(c.getParameters());
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "init",
        args = {int.class, java.security.cert.Certificate.class}
    )
     */
    public void test_initILjava_security_cert_Certificate () 
    throws MalformedURLException, IOException, CertificateException, 
    NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
    
    /* Certificate creation notes: certificate should be valid 37273 starting 
     * from 13 Nov 2008
     * If it brcomes invalidated regenerate it using following commands:
     * 1. openssl genrsa -des3 -out test.key 1024
     * 2. openssl req -new -key test.key -out test.csr
     * 3. cp test.key test.key.org
     * 4. openssl rsa -in test.key.org -out test.key
     * 5. openssl x509 -req -days 37273 -in test.csr -signkey test.key -out test.cert
     * */
    
        String certName = Support_Resources.getURL("test.cert");
        InputStream is = new URL(certName).openConnection().getInputStream();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        
        Certificate cert = cf.generateCertificate(is);

        Cipher c = Cipher.getInstance("RSA");
        
        c.init(Cipher.ENCRYPT_MODE, cert);
        c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        try {
            c.init(Cipher.ENCRYPT_MODE, cert);
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
            //expected
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "init",
        args = {int.class, java.security.cert.Certificate.class, java.security.SecureRandom.class}
    )
     */
    public void test_initILjava_security_cert_Certificate_java_security_SecureRandom ()
    throws MalformedURLException, IOException, CertificateException, 
    NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        
    /* Certificate creation notes: certificate should be valid 37273 starting 
     * from 13 Nov 2008
     * If it brcomes invalidated regenerate it using following commands:
     * 1. openssl genrsa -des3 -out test.key 1024
     * 2. openssl req -new -key test.key -out test.csr
     * 3. cp test.key test.key.org
     * 4. openssl rsa -in test.key.org -out test.key
     * 5. openssl x509 -req -days 37273 -in test.csr -signkey test.key -out test.cert
     * */
    
        String certName = Support_Resources.getURL("test.cert");
        InputStream is = new URL(certName).openConnection().getInputStream();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        
        Certificate cert = cf.generateCertificate(is);

        Cipher c = Cipher.getInstance("RSA");
        
        c.init(Cipher.ENCRYPT_MODE, cert, new SecureRandom());
        c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        try {
            c.init(Cipher.ENCRYPT_MODE, cert, new SecureRandom());
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
            //expected
        }
    }

    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "unwrap",
            args = {byte[].class, java.lang.String.class, int.class}
        ),
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "",
                clazz = CipherSpi.class,
                method = "engineUnwrap",
                args = {byte[].class, java.lang.String.class, int.class}
        )
    })
     */
    public void test_unwrap$BLjava_lang_StringI () throws NoSuchAlgorithmException,
    NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
    IllegalBlockSizeException {
        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[8];
        sr.nextBytes(iv);
        AlgorithmParameterSpec ap = new IvParameterSpec(iv);

        Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding");

        c.init(Cipher.WRAP_MODE, cipherKeyDES, ap, sr);
        byte[] arDES = c.wrap(cipherKeyDES);
        byte[] ar    = c.wrap(cipherKey);
        
        try {
            c.unwrap(arDES, "DES", Cipher.SECRET_KEY);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }

        c.init(Cipher.UNWRAP_MODE, cipherKeyDES, ap, sr);
        assertTrue(cipherKeyDES.equals(c.unwrap(arDES, "DES", Cipher.SECRET_KEY)));
        assertFalse(cipherKeyDES.equals(c.unwrap(ar, "DES", Cipher.SECRET_KEY)));

        try {
            c.unwrap(arDES, "RSA38", Cipher.PUBLIC_KEY);
            fail("NoSuchAlgorithmException expected");
        } catch (NoSuchAlgorithmException e) {
            //expected
        }

        c = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        c.init(Cipher.UNWRAP_MODE, cipherKey, ap, sr);
        try {
            c.unwrap(arDES, "DESede", Cipher.SECRET_KEY);
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
            //expected
        }
    }
    
    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "update",
            args = {java.nio.ByteBuffer.class, java.nio.ByteBuffer.class}
        ),
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "",
                clazz = CipherSpi.class,
                method = "engineUpdate",
                args = {java.nio.ByteBuffer.class, java.nio.ByteBuffer.class}
        )
    })
     */
    public void test_updateLjava_nio_ByteBufferLjava_nio_ByteBuffer () throws 
    NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
    ShortBufferException, InvalidAlgorithmParameterException {
        byte[] b = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        ByteBuffer bInput = ByteBuffer.allocate(256);
        ByteBuffer bOutput = ByteBuffer.allocate(256);
        
        Cipher c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        bInput.put(b, 0, 10);
        bInput.rewind();
        bOutput.rewind();
        c.update(bInput, bOutput);

        c = Cipher.getInstance("DES/CBC/NoPadding");
        try {
            c.update(bInput, bOutput);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        bInput = ByteBuffer.allocate(16);
        bInput.put(b, 0, 16);
        bInput.rewind();
        bOutput.rewind();
        c.update(bInput, bOutput);

        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[8];
        sr.nextBytes(iv);
        AlgorithmParameterSpec ap = new IvParameterSpec(iv);

        c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, cipherKeyDES, ap);
        bInput = ByteBuffer.allocate(64);

        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        bInput.put(b, 0, 16);
        bInput.rewind();
        try {
            c.update(bInput, bInput);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        bInput.put(b, 0, 16);
        bInput.rewind();
        bOutput.rewind();
        try {
            c.update(bInput, bOutput.asReadOnlyBuffer());
            fail("ReadOnlyBufferException expected");
        } catch (ReadOnlyBufferException e) {
            //expected
        }

        bInput.rewind();
        bInput.put(b, 0, 16);
        bInput.rewind();
        bOutput = ByteBuffer.allocate(8);
        c = Cipher.getInstance("DESede");
        c.init(Cipher.ENCRYPT_MODE, cipherKey);
        try {
            c.update(bInput, bOutput);
            fail("No expected ShortBufferException");
        } catch (ShortBufferException e) {
            //expected
        }
    }
    
    class Mock_Key implements Key {
        public String getAlgorithm() {
            return null;
        }

        public byte[] getEncoded() {
            return null;
        }

        public String getFormat() {
            return null;
        }
        
    }
    
    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "wrap",
            args = {java.security.Key.class}
        ),
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "",
                clazz = CipherSpi.class,
                method = "engineWrap",
                args = {java.security.Key.class}
        )
    })
     */
    public void test_wrap_java_security_Key () throws NoSuchAlgorithmException,
    NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, 
    InvalidAlgorithmParameterException, MalformedURLException, IOException, 
    CertificateException {
        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[8];
        sr.nextBytes(iv);
        AlgorithmParameterSpec ap = new IvParameterSpec(iv);

        Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding");

        c.init(Cipher.WRAP_MODE, cipherKeyDES, ap, sr);
        assertNotNull(c.wrap(cipherKeyDES));
        assertNotNull(c.wrap(cipherKey));
        String certName = Support_Resources.getURL("test.cert");
        InputStream is = new URL(certName).openConnection().getInputStream();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        
        Certificate cert = cf.generateCertificate(is);
        assertNotNull(c.wrap(cert.getPublicKey()));

        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.WRAP_MODE, cipherKeyDES, ap, sr);
        try {
            assertNotNull(c.wrap(cert.getPublicKey()));
            fail("IllegalBlockSizeException expected");
        } catch (IllegalBlockSizeException e) {
            //expected
        }

        c.init(Cipher.DECRYPT_MODE, cipherKeyDES, ap, sr);
        
        try {
            c.wrap(cipherKeyDES);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }
        
        c.init(Cipher.WRAP_MODE, cipherKeyDES, ap, sr);
        try {
            c.wrap(new Mock_Key());
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
            //expected
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "doFinal",
        args = {byte[].class, int.class}
    )
     */
    public void test_doFinal$BI() throws Exception {
        byte[] b = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        byte[] b1 = new byte[30];
        
        Cipher c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        c.update(b, 0, 10);
        try {
            c.doFinal(b1, 5);
            fail("IllegalBlockSizeException expected");
        } catch (IllegalBlockSizeException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        try {
            c.doFinal(b1, 5);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        c.update(b, 3, 8);
        c.doFinal(b1, 0);

        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[8];
        sr.nextBytes(iv);
        AlgorithmParameterSpec ap = new IvParameterSpec(iv);

        c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, cipherKeyDES, ap);
        
        c.update(b1, 0, 24);
        try {
            c.doFinal(b, 0);
            fail("BadPaddingException expected");
        } catch (BadPaddingException e) {
            //expected
        }

        b1 = new byte[6];
        c = Cipher.getInstance("DESede");
        c.init(Cipher.ENCRYPT_MODE, cipherKey);
        c.update(b, 3, 6);
        try {
            c.doFinal(b1, 5);
            fail("No expected ShortBufferException");
        } catch (ShortBufferException e) {
            //expected
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "doFinal",
        args = {byte[].class}
    )
     */
    public void test_doFinal$B() throws Exception {
        byte[] b1 = new byte[32];
        byte[] bI1 = {1,2,3,4,5,6,7,8,9,10};
        byte[] bI2 = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
        byte[] bI3 = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        byte[] bI4 = {1,2,3};
        
        Cipher c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        try {
            c.doFinal(bI1);
            fail("IllegalBlockSizeException expected");
        } catch (IllegalBlockSizeException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        try {
            c.doFinal(bI1);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        c.doFinal(bI2);
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        c.doFinal(bI3, 0, 16, b1, 0);

        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[8];
        sr.nextBytes(iv);
        AlgorithmParameterSpec ap = new IvParameterSpec(iv);

        c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, cipherKeyDES, ap);
        
        try {
            c.doFinal(b1);
            fail("BadPaddingException expected");
        } catch (BadPaddingException e) {
            //expected
        }
    }

    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "doFinal",
            args = {byte[].class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = CipherSpi.class,
            method = "engineDoFinal",
            args = {byte[].class, int.class, int.class}
        )
    })
     */
    public void test_doFinal$BII() throws Exception {
        byte[] b = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        byte[] b1 = new byte[30];
        
        Cipher c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        try {
            c.doFinal(b, 0, 10);
            fail("IllegalBlockSizeException expected");
        } catch (IllegalBlockSizeException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        try {
            c.doFinal(b, 0, 10);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        c.doFinal(b, 0, 16);
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        c.doFinal(b, 0, 16, b1, 0);

        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[8];
        sr.nextBytes(iv);
        AlgorithmParameterSpec ap = new IvParameterSpec(iv);

        c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, cipherKeyDES, ap);
        
        try {
            c.doFinal(b1, 0, 24);
            fail("BadPaddingException expected");
        } catch (BadPaddingException e) {
            //expected
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "doFinal",
        args = {byte[].class, int.class, int.class, byte[].class}
    )
     */
    public void test_doFinal$BII$B() throws Exception {
        byte[] b = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        byte[] b1 = new byte[30];
        
        Cipher c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        try {
            c.doFinal(b, 0, 10, b1);
            fail("IllegalBlockSizeException expected");
        } catch (IllegalBlockSizeException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        try {
            c.doFinal(b, 0, 10, b1);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        c.doFinal(b, 0, 16, b1);

        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[8];
        sr.nextBytes(iv);
        AlgorithmParameterSpec ap = new IvParameterSpec(iv);

        c = Cipher.getInstance("DES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, cipherKeyDES, ap);
        
        try {
            c.doFinal(b1, 0, 24, new byte[42]);
            fail("BadPaddingException expected");
        } catch (BadPaddingException e) {
            //expected
        }

        b1 = new byte[6];
        c = Cipher.getInstance("DESede");
        c.init(Cipher.ENCRYPT_MODE, cipherKey);
        try {
            c.doFinal(b, 3, 6, b1);
            fail("No expected ShortBufferException");
        } catch (ShortBufferException e) {
            //expected
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks exception",
        method = "update",
        args = {byte[].class}
    )
     */
    public void test_update$B() throws Exception {
        Cipher cipher = Cipher.getInstance("DESEDE/CBC/PKCS5Padding");
        try {
            cipher.update(new byte[64]);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }
    }

    /* @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "update",
        args = {byte[].class, int.class, int.class, byte[].class}
    )
     */
    public void test_() throws Exception {
        byte[] b = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        byte[] b1 = new byte[30];
        
        Cipher c = Cipher.getInstance("DES/CBC/NoPadding");

        try {
            c.update(b, 0, 10, b1);
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            //expected
        }

        c = Cipher.getInstance("DES/CBC/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, cipherKeyDES);
        c.update(b, 0, 16, b1);

        b1 = new byte[3];

        try {
            c.update(b, 3, 15, b1);
            fail("No expected ShortBufferException");
        } catch (ShortBufferException e) {
            //expected
        }
    }

    class testCipher extends Cipher {
        testCipher(CipherSpi c, Provider p, String s) {
            super(c, p, s);
        }
    }
}
