package org.bouncycastle.jce.provider;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHPrivateKeySpec;
import javax.crypto.spec.DHPublicKeySpec;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
// BEGIN android-removed
// import org.bouncycastle.jce.interfaces.ElGamalPrivateKey;
// import org.bouncycastle.jce.interfaces.ElGamalPublicKey;
// import org.bouncycastle.jce.spec.ECPrivateKeySpec;
// import org.bouncycastle.jce.spec.ECPublicKeySpec;
// import org.bouncycastle.jce.spec.ElGamalPrivateKeySpec;
// import org.bouncycastle.jce.spec.ElGamalPublicKeySpec;
// import org.bouncycastle.jce.spec.GOST3410PrivateKeySpec;
// import org.bouncycastle.jce.spec.GOST3410PublicKeySpec;
// END android-removed

public abstract class JDKKeyFactory
    extends KeyFactorySpi
{
    protected boolean elGamalFactory = false;
    
    public JDKKeyFactory()
    {
    }

    protected KeySpec engineGetKeySpec(
        Key    key,
        Class    spec)
    throws InvalidKeySpecException
    {
       if (spec.isAssignableFrom(PKCS8EncodedKeySpec.class) && key.getFormat().equals("PKCS#8"))
       {
               return new PKCS8EncodedKeySpec(key.getEncoded());
       }
       else if (spec.isAssignableFrom(X509EncodedKeySpec.class) && key.getFormat().equals("X.509"))
       {
               return new X509EncodedKeySpec(key.getEncoded());
       }
       else if (spec.isAssignableFrom(RSAPublicKeySpec.class) && key instanceof RSAPublicKey)
       {
            RSAPublicKey    k = (RSAPublicKey)key;

            return new RSAPublicKeySpec(k.getModulus(), k.getPublicExponent());
       }
       else if (spec.isAssignableFrom(RSAPrivateKeySpec.class) && key instanceof RSAPrivateKey)
       {
            RSAPrivateKey    k = (RSAPrivateKey)key;

            return new RSAPrivateKeySpec(k.getModulus(), k.getPrivateExponent());
       }
       else if (spec.isAssignableFrom(RSAPrivateCrtKeySpec.class) && key instanceof RSAPrivateCrtKey)
       {
            RSAPrivateCrtKey    k = (RSAPrivateCrtKey)key;

            return new RSAPrivateCrtKeySpec(
                            k.getModulus(), k.getPublicExponent(),
                            k.getPrivateExponent(),
                            k.getPrimeP(), k.getPrimeQ(),
                            k.getPrimeExponentP(), k.getPrimeExponentQ(),
                            k.getCrtCoefficient());
       }
       else if (spec.isAssignableFrom(DHPrivateKeySpec.class) && key instanceof DHPrivateKey)
       {
           DHPrivateKey k = (DHPrivateKey)key;
           
           return new DHPrivateKeySpec(k.getX(), k.getParams().getP(), k.getParams().getG());
       }
       else if (spec.isAssignableFrom(DHPublicKeySpec.class) && key instanceof DHPublicKey)
       {
           DHPublicKey k = (DHPublicKey)key;
           
           return new DHPublicKeySpec(k.getY(), k.getParams().getP(), k.getParams().getG());
       }

        throw new RuntimeException("not implemented yet " + key + " " + spec);
    }

    protected Key engineTranslateKey(
        Key    key)
        throws InvalidKeyException
    {
        if (key instanceof RSAPublicKey)
        {
            return new JCERSAPublicKey((RSAPublicKey)key);
        }
        else if (key instanceof RSAPrivateCrtKey)
        {
            return new JCERSAPrivateCrtKey((RSAPrivateCrtKey)key);
        }
        else if (key instanceof RSAPrivateKey)
        {
            return new JCERSAPrivateKey((RSAPrivateKey)key);
        }
        else if (key instanceof DHPublicKey)
        {
            // BEGIN android-removed
            // if (elGamalFactory)
            // {
            //     return new JCEElGamalPublicKey((DHPublicKey)key);
            // }
            // else
            // {
            // END android-removed
                return new JCEDHPublicKey((DHPublicKey)key);
            // BEGIN android-removed
            // }
            // END android-removed
        }
        else if (key instanceof DHPrivateKey)
        {
            // BEGIN android-removed
            // if (elGamalFactory)
            // {
            //     return new JCEElGamalPrivateKey((DHPrivateKey)key);
            // }
            // else
            // {
            // END android-removed
                return new JCEDHPrivateKey((DHPrivateKey)key);
            // BEGIN android-removed
            // }
            // END android-removed
        }
        else if (key instanceof DSAPublicKey)
        {
            return new JDKDSAPublicKey((DSAPublicKey)key);
        }
        else if (key instanceof DSAPrivateKey)
        {
            return new JDKDSAPrivateKey((DSAPrivateKey)key);
        }
        // BEGIN android-removed
        // else if (key instanceof ElGamalPublicKey)
        // {
        //     return new JCEElGamalPublicKey((ElGamalPublicKey)key);
        // }
        // else if (key instanceof ElGamalPrivateKey)
        // {
        //    return new JCEElGamalPrivateKey((ElGamalPrivateKey)key);
        // }
        // END android-removed

        throw new InvalidKeyException("key type unknown");
    }

    /**
     * create a public key from the given DER encoded input stream. 
     */ 
    static PublicKey createPublicKeyFromDERStream(
        byte[]         in)
        throws IOException
    {
        return createPublicKeyFromPublicKeyInfo(
                new SubjectPublicKeyInfo((ASN1Sequence)(new ASN1InputStream(in).readObject())));
    }

    /**
     * create a public key from the given public key info object.
     */ 
    static PublicKey createPublicKeyFromPublicKeyInfo(
        SubjectPublicKeyInfo         info)
    {
        DERObjectIdentifier     algOid = info.getAlgorithmId().getObjectId();
        
        if (RSAUtil.isRsaOid(algOid))
        {
            return new JCERSAPublicKey(info);
        }
        else if (algOid.equals(PKCSObjectIdentifiers.dhKeyAgreement))
        {
            return new JCEDHPublicKey(info);
        }
        else if (algOid.equals(X9ObjectIdentifiers.dhpublicnumber))
        {
            return new JCEDHPublicKey(info);
        }
        // BEGIN android-removed
        // else if (algOid.equals(OIWObjectIdentifiers.elGamalAlgorithm))
        // {
        //     return new JCEElGamalPublicKey(info);
        // }
        // END android-removed
        else if (algOid.equals(X9ObjectIdentifiers.id_dsa))
        {
            return new JDKDSAPublicKey(info);
        }
        else if (algOid.equals(OIWObjectIdentifiers.dsaWithSHA1))
        {
            return new JDKDSAPublicKey(info);
        }
        // BEGIN android-removed
        // else if (algOid.equals(X9ObjectIdentifiers.id_ecPublicKey))
        // {
        //     return new JCEECPublicKey(info);
        // }
        // else if (algOid.equals(CryptoProObjectIdentifiers.gostR3410_94))
        // {
        //     return new JDKGOST3410PublicKey(info);
        // }
        // else if (algOid.equals(CryptoProObjectIdentifiers.gostR3410_2001))
        // {
        //     return new JCEECPublicKey(info);
        // }
        else
        {
            throw new RuntimeException("algorithm identifier " + algOid + " in key not recognised");
        }
    }

    /**
     * create a private key from the given DER encoded input stream. 
     */ 
    static PrivateKey createPrivateKeyFromDERStream(
        byte[]         in)
        throws IOException
    {
        return createPrivateKeyFromPrivateKeyInfo(
                new PrivateKeyInfo((ASN1Sequence)(new ASN1InputStream(in).readObject())));
    }

    /**
     * create a private key from the given public key info object.
     */ 
    static PrivateKey createPrivateKeyFromPrivateKeyInfo(
        PrivateKeyInfo      info)
    {
        DERObjectIdentifier     algOid = info.getAlgorithmId().getObjectId();
        
        if (RSAUtil.isRsaOid(algOid))
        {
              return new JCERSAPrivateCrtKey(info);
        }
        else if (algOid.equals(PKCSObjectIdentifiers.dhKeyAgreement))
        {
              return new JCEDHPrivateKey(info);
        }
        // BEGIN android-removed
        // else if (algOid.equals(OIWObjectIdentifiers.elGamalAlgorithm))
        // {
        //       return new JCEElGamalPrivateKey(info);
        // }
        // END android-removed
        else if (algOid.equals(X9ObjectIdentifiers.id_dsa))
        {
              return new JDKDSAPrivateKey(info);
        }
        // BEGIN android-removed
        // else if (algOid.equals(X9ObjectIdentifiers.id_ecPublicKey))
        // {
        //       return new JCEECPrivateKey(info);
        // }
        // else if (algOid.equals(CryptoProObjectIdentifiers.gostR3410_94))
        // {
        //       return new JDKGOST3410PrivateKey(info);
        // }
        // else if (algOid.equals(CryptoProObjectIdentifiers.gostR3410_2001))
        // {
        //       return new JCEECPrivateKey(info);
        // }
        // END android-removed
        else
        {
            throw new RuntimeException("algorithm identifier " + algOid + " in key not recognised");
        }
    }

    public static class RSA
        extends JDKKeyFactory
    {
        public RSA()
        {
        }

        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    //
                    // in case it's just a RSAPrivateKey object...
                    //
                    try
                    {
                        return new JCERSAPrivateCrtKey(
                            new RSAPrivateKeyStructure(
                                (ASN1Sequence)new ASN1InputStream(((PKCS8EncodedKeySpec)keySpec).getEncoded()).readObject()));
                    }
                    catch (Exception ex)
                    {
                        throw new InvalidKeySpecException(ex.toString());
                    }
                }
            }
            else if (keySpec instanceof RSAPrivateCrtKeySpec)
            {
                return new JCERSAPrivateCrtKey((RSAPrivateCrtKeySpec)keySpec);
            }
            else if (keySpec instanceof RSAPrivateKeySpec)
            {
                return new JCERSAPrivateKey((RSAPrivateKeySpec)keySpec);
            }
    
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    
        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            else if (keySpec instanceof RSAPublicKeySpec)
            {
                return new JCERSAPublicKey((RSAPublicKeySpec)keySpec);
            }
    
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }

    public static class DH
        extends JDKKeyFactory
    {
        public DH()
        {
        }

        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            else if (keySpec instanceof DHPrivateKeySpec)
            {
                return new JCEDHPrivateKey((DHPrivateKeySpec)keySpec);
            }
    
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    
        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            else if (keySpec instanceof DHPublicKeySpec)
            {
                return new JCEDHPublicKey((DHPublicKeySpec)keySpec);
            }
    
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }

    public static class DSA
        extends JDKKeyFactory
    {
        public DSA()
        {
        }

        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            else if (keySpec instanceof DSAPrivateKeySpec)
            {
                return new JDKDSAPrivateKey((DSAPrivateKeySpec)keySpec);
            }
    
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    
        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            else if (keySpec instanceof DSAPublicKeySpec)
            {
                return new JDKDSAPublicKey((DSAPublicKeySpec)keySpec);
            }
    
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }

    public static class GOST3410
        extends JDKKeyFactory
    {
        public GOST3410()
        {
        }
        
        protected PrivateKey engineGeneratePrivate(
                KeySpec    keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                            ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            // BEGIN android-removed
            // else if (keySpec instanceof GOST3410PrivateKeySpec)
            // {
            //     return new JDKGOST3410PrivateKey((GOST3410PrivateKeySpec)keySpec);
            // }
            // END android-removed
            
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
        
        protected PublicKey engineGeneratePublic(
                KeySpec    keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                            ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            // BEGIN android-removed
            // else if (keySpec instanceof GOST3410PublicKeySpec)
            // {
            //     return new JDKGOST3410PublicKey((GOST3410PublicKeySpec)keySpec);
            // }
            // END android-removed
            
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }
    
    public static class ElGamal
        extends JDKKeyFactory
    {
        public ElGamal()
        {
            elGamalFactory = true;
        }

        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            // BEGIN android-removed
            // else if (keySpec instanceof ElGamalPrivateKeySpec)
            // {
            //     return new JCEElGamalPrivateKey((ElGamalPrivateKeySpec)keySpec);
            // }
            // else if (keySpec instanceof DHPrivateKeySpec)
            // {
            //     return new JCEElGamalPrivateKey((DHPrivateKeySpec)keySpec);
            // }
            // END android-removed
    
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    
        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            // BEGIN android-removed
            // else if (keySpec instanceof ElGamalPublicKeySpec)
            // {
            //     return new JCEElGamalPublicKey((ElGamalPublicKeySpec)keySpec);
            // }
            // else if (keySpec instanceof DHPublicKeySpec)
            // {
            //     return new JCEElGamalPublicKey((DHPublicKeySpec)keySpec);
            // }
            // END android-removed
    
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }


    /**
     * This isn't really correct, however the class path project API seems to think such
     * a key factory will exist.
     */
    public static class X509
        extends JDKKeyFactory
    {
        public X509()
        {
        }
    
        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
    
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    
        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
    
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }
    
    public static class EC
        extends JDKKeyFactory
    {
        String  algorithm;

        public EC()
        {
            this("EC");
        }

        public EC(
            String  algorithm)
        {
            this.algorithm = algorithm;
        }

        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            // BEGIN android-removed
            // else if (keySpec instanceof ECPrivateKeySpec)
            // {
            //     return new JCEECPrivateKey(algorithm, (ECPrivateKeySpec)keySpec);
            // }
            // else if (keySpec instanceof java.security.spec.ECPrivateKeySpec)
            // {
            //     return new JCEECPrivateKey(algorithm, (java.security.spec.ECPrivateKeySpec)keySpec);
            // }
            // END android-removed
    
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    
        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            // BEGIN android-removed
            // else if (keySpec instanceof ECPublicKeySpec)
            // {
            //     return new JCEECPublicKey(algorithm, (ECPublicKeySpec)keySpec);
            // }
            // else if (keySpec instanceof java.security.spec.ECPublicKeySpec)
            // {
            //     return new JCEECPublicKey(algorithm, (java.security.spec.ECPublicKeySpec)keySpec);
            // }
            // END android-removed
    
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }

    public static class ECDSA
        extends EC
    {
        public ECDSA()
        {
            super("ECDSA");
        }
    }

    public static class ECGOST3410
        extends EC
    {
        public ECGOST3410()
        {
            super("ECGOST3410");
        }
    }
    
    public static class ECDH
        extends EC
    {
        public ECDH()
        {
            super("ECDH");
        }
    }

    public static class ECDHC
        extends EC
    {
        public ECDHC()
        {
            super("ECDHC");
        }
    }
}
