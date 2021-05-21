package org.bouncycastle.jce.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.MacSpi;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.digests.*;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.engines.DESedeEngine;
// BEGIN android-removed
// import org.bouncycastle.crypto.engines.IDEAEngine;
// import org.bouncycastle.crypto.engines.RC2Engine;
// import org.bouncycastle.crypto.engines.RC532Engine;
// import org.bouncycastle.crypto.engines.SkipjackEngine;
// END android-removed
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.macs.CFBBlockCipherMac;
// BEGIN android-removed
// import org.bouncycastle.crypto.macs.GOST28147Mac;
// END android-removed
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.macs.ISO9797Alg3Mac;
import org.bouncycastle.crypto.macs.OldHMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class JCEMac
    extends MacSpi implements PBE
{
    private Mac macEngine;

    private int                     pbeType = PKCS12;
    private int                     pbeHash = SHA1;
    private int                     keySize = 160;

    protected JCEMac(
        Mac macEngine)
    {
        this.macEngine = macEngine;
    }

    protected JCEMac(
        Mac macEngine,
        int pbeType,
        int pbeHash,
        int keySize)
    {
        this.macEngine = macEngine;
        this.pbeType = pbeType;
        this.pbeHash = pbeHash;
        this.keySize = keySize;
    }

    protected void engineInit(
        Key                     key,
        AlgorithmParameterSpec  params)
        throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        CipherParameters        param;

        if (key == null)
        {
            throw new InvalidKeyException("key is null");
        }
        
        if (key instanceof JCEPBEKey)
        {
            JCEPBEKey   k = (JCEPBEKey)key;
            
            if (k.getParam() != null)
            {
                param = k.getParam();
            }
            else if (params instanceof PBEParameterSpec)
            {
                param = PBE.Util.makePBEMacParameters(k, params);
            }
            else
            {
                throw new InvalidAlgorithmParameterException("PBE requires PBE parameters to be set.");
            }
        }
        else if (params instanceof IvParameterSpec)
        {
            param = new ParametersWithIV(new KeyParameter(key.getEncoded()), ((IvParameterSpec)params).getIV());
        }
        else if (params == null)
        {
            param = new KeyParameter(key.getEncoded());
        }
        else
        {
            throw new InvalidAlgorithmParameterException("unknown parameter type.");
        }

        macEngine.init(param);
    }

    protected int engineGetMacLength() 
    {
        return macEngine.getMacSize();
    }

    protected void engineReset() 
    {
        macEngine.reset();
    }

    protected void engineUpdate(
        byte    input) 
    {
        macEngine.update(input);
    }

    protected void engineUpdate(
        byte[]  input,
        int     offset,
        int     len) 
    {
        macEngine.update(input, offset, len);
    }

    protected byte[] engineDoFinal() 
    {
        byte[]  out = new byte[engineGetMacLength()];

        macEngine.doFinal(out, 0);

        return out;
    }

    /**
     * the classes that extend directly off us.
     */

    /**
     * DES
     */
    public static class DES
        extends JCEMac
    {
        public DES()
        {
            super(new CBCBlockCipherMac(new DESEngine()));
        }
    }

    /**
     * DESede
     */
    public static class DESede
        extends JCEMac
    {
        public DESede()
        {
            super(new CBCBlockCipherMac(new DESedeEngine()));
        }
    }

    /**
     * SKIPJACK
     */
    // BEGIN android-removed
    // public static class Skipjack
    //     extends JCEMac
    // {
    //     public Skipjack()
    //     {
    //         super(new CBCBlockCipherMac(new SkipjackEngine()));
    //     }
    // }
    // END android-removed

    /**
     * IDEA
     */
    // BEGIN android-removed
    // public static class IDEA
    //     extends JCEMac
    // {
    //     public IDEA()
    //     {
    //         super(new CBCBlockCipherMac(new IDEAEngine()));
    //     }
    // }
    // END android-removed

    /**
     * RC2
     */
    // BEGIN android-removed
    // public static class RC2
    //     extends JCEMac
    // {
    //     public RC2()
    //     {
    //         super(new CBCBlockCipherMac(new RC2Engine()));
    //     }
    // }
    // END android-removed

    /**
     * RC5
     */
    // BEGIN android-removed
    // public static class RC5
    //     extends JCEMac
    // {
    //     public RC5()
    //     {
    //         super(new CBCBlockCipherMac(new RC532Engine()));
    //     }
    // }
    // END android-removed

    /**
     * GOST28147
     */
    // BEGIN android-removed
    // public static class GOST28147
    //     extends JCEMac
    // {
    //     public GOST28147()
    //     {
    //         super(new GOST28147Mac());
    //     }
    // }
    // END android-removed
    
    /**
     * DES
     */
    public static class DESCFB8
        extends JCEMac
    {
        public DESCFB8()
        {
            super(new CFBBlockCipherMac(new DESEngine()));
        }
    }

    /**
     * DESede
     */
    public static class DESedeCFB8
        extends JCEMac
    {
        public DESedeCFB8()
        {
            super(new CFBBlockCipherMac(new DESedeEngine()));
        }
    }

    /**
     * SKIPJACK
     */
    // BEGIN android-removed
    // public static class SkipjackCFB8
    //     extends JCEMac
    // {
    //     public SkipjackCFB8()
    //     {
    //         super(new CFBBlockCipherMac(new SkipjackEngine()));
    //     }
    // }
    // END android-removed

    /**
     * IDEACFB8
     */
    // BEGIN android-removed
    // public static class IDEACFB8
    //     extends JCEMac
    // {
    //     public IDEACFB8()
    //     {
    //         super(new CFBBlockCipherMac(new IDEAEngine()));
    //     }
    // }
    // END android-removed

    /**
     * RC2CFB8
     */
    // BEGIN android-removed
    // public static class RC2CFB8
    //     extends JCEMac
    // {
    //     public RC2CFB8()
    //     {
    //         super(new CFBBlockCipherMac(new RC2Engine()));
    //     }
    // }
    // END android-removed

    /**
     * RC5CFB8
     */
    // BEGIN android-removed
    // public static class RC5CFB8
    //     extends JCEMac
    // {
    //     public RC5CFB8()
    //     {
    //         super(new CFBBlockCipherMac(new RC532Engine()));
    //     }
    // }
    // END android-removed
    
    
    /**
     * DESede64
     */
    public static class DESede64
        extends JCEMac
    {
        public DESede64()
        {
            super(new CBCBlockCipherMac(new DESedeEngine(), 64));
        }
    }
    
    /**
     * DES9797Alg3
     */
    public static class DES9797Alg3
        extends JCEMac
    {
        public DES9797Alg3()
        {
            super(new ISO9797Alg3Mac(new DESEngine()));
        }
    }

    /**
     * MD2 HMac
     */
    // BEGIN android-removed
    // public static class MD2
    //     extends JCEMac
    // {
    //     public MD2()
    //     {
    //         super(new HMac(new MD2Digest()));
    //     }
    // }
    // END android-removed

    /**
     * MD4 HMac
     */
    // BEGIN android-removed
    // public static class MD4
    //     extends JCEMac
    // {
    //     public MD4()
    //     {
    //         super(new HMac(new MD4Digest()));
    //     }
    // }
    // END android-removed

    /**
     * MD5 HMac
     */
    public static class MD5
        extends JCEMac
    {
        public MD5()
        {
            super(new HMac(new MD5Digest()));
        }
    }

    /**
     * SHA1 HMac
     */
    public static class SHA1
        extends JCEMac
    {
        public SHA1()
        {
            super(new HMac(new SHA1Digest()));
        }
    }

    /**
     * SHA-224 HMac
     */
    public static class SHA224
        extends JCEMac
    {
        public SHA224()
        {
            super(new HMac(new SHA224Digest()));
        }
    }
    
    /**
     * SHA-256 HMac
     */
    public static class SHA256
        extends JCEMac
    {
        public SHA256()
        {
            super(new HMac(new SHA256Digest()));
        }
    }

    /**
     * SHA-384 HMac
     */
    public static class SHA384
        extends JCEMac
    {
        public SHA384()
        {
            super(new HMac(new SHA384Digest()));
        }
    }

    public static class OldSHA384
        extends JCEMac
    {
        public OldSHA384()
        {
            super(new OldHMac(new SHA384Digest()));
        }
    }
    
    /**
     * SHA-512 HMac
     */
    public static class SHA512
        extends JCEMac
    {
        public SHA512()
        {
            super(new HMac(new SHA512Digest()));
        }
    }

    /**
     * SHA-512 HMac
     */
    public static class OldSHA512
        extends JCEMac
    {
        public OldSHA512()
        {
            super(new OldHMac(new SHA512Digest()));
        }
    }
    
// BEGIN android-removed
//    /**
//     * RIPEMD128 HMac
//     */
//    public static class RIPEMD128
//        extends JCEMac
//    {
//        public RIPEMD128()
//        {
//           super(new HMac(new RIPEMD128Digest()));
//        }
//    }
//
//    /**
//     * RIPEMD160 HMac
//     */
//    public static class RIPEMD160
//        extends JCEMac
//    {
//        public RIPEMD160()
//        {
//           super(new HMac(new RIPEMD160Digest()));
//        }
//    }
//
//    /**
//     * Tiger HMac
//     */
//    public static class Tiger
//        extends JCEMac
//    {
//        public Tiger()
//        {
//            super(new HMac(new TigerDigest()));
//        }
//    }
//
//    //
//    // PKCS12 states that the same algorithm should be used
//    // for the key generation as is used in the HMAC, so that
//    // is what we do here.
//    //
//
//    /**
//     * PBEWithHmacRIPEMD160
//     */
//    public static class PBEWithRIPEMD160
//        extends JCEMac
//    {
//        public PBEWithRIPEMD160()
//        {
//            super(new HMac(new RIPEMD160Digest()), PKCS12, RIPEMD160, 160);
//        }
//    }
// END android-removed

    /**
     * PBEWithHmacSHA
     */
    public static class PBEWithSHA
        extends JCEMac
    {
        public PBEWithSHA()
        {
            super(new HMac(new SHA1Digest()), PKCS12, SHA1, 160);
        }
    }

    /**
     * PBEWithHmacTiger
     */
// BEGIN android-removed
//    public static class PBEWithTiger
//        extends JCEMac
//    {
//        public PBEWithTiger()
//        {
//            super(new HMac(new TigerDigest()), PKCS12, TIGER, 192);
//        }
//    }
// END android-removed
}
