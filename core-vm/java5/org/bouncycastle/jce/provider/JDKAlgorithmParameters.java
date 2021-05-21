package org.bouncycastle.jce.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.RC2ParameterSpec;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
// BEGIN android-removed
// import org.bouncycastle.asn1.cryptopro.GOST3410PublicKeyAlgParameters;
// import org.bouncycastle.asn1.misc.CAST5CBCParameters;
// END android-removed
import org.bouncycastle.asn1.misc.IDEACBCPar;
// BEGIN android-removed
// import org.bouncycastle.asn1.oiw.ElGamalParameter;
// END android-removed
import org.bouncycastle.asn1.pkcs.DHParameter;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RC2CBCParameter;
import org.bouncycastle.asn1.pkcs.RSAESOAEPparams;
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DSAParameter;
// BEGIN android-removed
// import org.bouncycastle.jce.spec.ElGamalParameterSpec;
// import org.bouncycastle.jce.spec.GOST3410ParameterSpec;
// import org.bouncycastle.jce.spec.GOST3410PublicKeyParameterSetSpec;
// END android-removed
import org.bouncycastle.jce.spec.IESParameterSpec;

public abstract class JDKAlgorithmParameters
    extends AlgorithmParametersSpi
{
    public static class IVAlgorithmParameters
        extends JDKAlgorithmParameters
    {
        private byte[]  iv;

        protected byte[] engineGetEncoded() 
            throws IOException
        {
            return engineGetEncoded("ASN.1");
        }

        protected byte[] engineGetEncoded(
            String format) 
            throws IOException
        {
            if (format == null)
            {
                return engineGetEncoded("ASN.1");
            }
            
            if (format.equals("RAW"))
            {
                byte[]  tmp = new byte[iv.length];

                System.arraycopy(iv, 0, tmp, 0, iv.length);
                return tmp;
            }
            else if (format.equals("ASN.1"))
            {
                return new DEROctetString(engineGetEncoded("RAW")).getEncoded();
            }

            return null;
        }

        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == IvParameterSpec.class)
            {
                return new IvParameterSpec(iv);
            }

            throw new InvalidParameterSpecException("unknown parameter spec passed to IV parameters object.");
        }

        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof IvParameterSpec))
            {
                throw new InvalidParameterSpecException("IvParameterSpec required to initialise a IV parameters algorithm parameters object");
            }

            this.iv = ((IvParameterSpec)paramSpec).getIV();
        }

        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            //
            // check that we don't have a DER encoded octet string
            //
            if ((params.length % 8) != 0
                    && params[0] == 0x04 && params[1] == params.length - 2)
            {
                ASN1InputStream         aIn = new ASN1InputStream(params);
                ASN1OctetString         oct = (ASN1OctetString)aIn.readObject();

                params = oct.getOctets();
            }

            this.iv = new byte[params.length];

            System.arraycopy(params, 0, iv, 0, iv.length);
        }

        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equals("RAW"))
            {
                engineInit(params);
                return;
            }
            else if (format.equals("ASN.1"))
            {
                ASN1InputStream         aIn = new ASN1InputStream(params);
                
                try
                {
                    ASN1OctetString         oct = (ASN1OctetString)aIn.readObject();
    
                    engineInit(oct.getOctets());
                }
                catch (Exception e)
                {
                    throw new IOException("Exception decoding: " + e);
                }
                
                return;
            }

            throw new IOException("Unknown parameters format in IV parameters object");
        }

        protected String engineToString() 
        {
            return "IV Parameters";
        }
    }

// BEGIN android-removed
//    public static class IDEAAlgorithmParameters
//        extends JDKAlgorithmParameters
//    {
//        private byte[]  iv;
//
//        protected byte[] engineGetEncoded() 
//            throws IOException
//        {
//            return engineGetEncoded("ASN.1");
//        }
//
//        protected byte[] engineGetEncoded(
//            String format) 
//            throws IOException
//        {
//            if (format == null)
//            {
//                return engineGetEncoded("ASN.1");
//            }
//            
//            if (format.equals("RAW"))
//            {
//                byte[]  tmp = new byte[iv.length];
//
//                System.arraycopy(iv, 0, tmp, 0, iv.length);
//                return tmp;
//            }
//            else if (format.equals("ASN.1"))
//            {
//                return new IDEACBCPar(engineGetEncoded("RAW")).getEncoded();
//            }
//
//            return null;
//        }
//
//        protected AlgorithmParameterSpec engineGetParameterSpec(
//            Class paramSpec) 
//            throws InvalidParameterSpecException
//        {
//            if (paramSpec == IvParameterSpec.class)
//            {
//                return new IvParameterSpec(iv);
//            }
//
//            throw new InvalidParameterSpecException("unknown parameter spec passed to IV parameters object.");
//        }
//
//        protected void engineInit(
//            AlgorithmParameterSpec paramSpec) 
//            throws InvalidParameterSpecException
//        {
//            if (!(paramSpec instanceof IvParameterSpec))
//            {
//                throw new InvalidParameterSpecException("IvParameterSpec required to initialise a IV parameters algorithm parameters object");
//            }
//
//            this.iv = ((IvParameterSpec)paramSpec).getIV();
//        }
//
//        protected void engineInit(
//            byte[] params) 
//            throws IOException
//        {
//            this.iv = new byte[params.length];
//
//            System.arraycopy(params, 0, iv, 0, iv.length);
//        }
//
//        protected void engineInit(
//            byte[] params,
//            String format) 
//            throws IOException
//        {
//            if (format.equals("RAW"))
//            {
//                engineInit(params);
//                return;
//            }
//            else if (format.equals("ASN.1"))
//            {
//                ASN1InputStream         aIn = new ASN1InputStream(params);
//                IDEACBCPar              oct = new IDEACBCPar((ASN1Sequence)aIn.readObject());
//
//                engineInit(oct.getIV());
//                return;
//            }
//
//            throw new IOException("Unknown parameters format in IV parameters object");
//        }
//
//        protected String engineToString() 
//        {
//            return "IDEA Parameters";
//        }
//    }
//
//    public static class RC2AlgorithmParameters
//        extends JDKAlgorithmParameters
//    {
//        private short[] table = {
//           0xbd, 0x56, 0xea, 0xf2, 0xa2, 0xf1, 0xac, 0x2a, 0xb0, 0x93, 0xd1, 0x9c, 0x1b, 0x33, 0xfd, 0xd0,
//           0x30, 0x04, 0xb6, 0xdc, 0x7d, 0xdf, 0x32, 0x4b, 0xf7, 0xcb, 0x45, 0x9b, 0x31, 0xbb, 0x21, 0x5a,
//           0x41, 0x9f, 0xe1, 0xd9, 0x4a, 0x4d, 0x9e, 0xda, 0xa0, 0x68, 0x2c, 0xc3, 0x27, 0x5f, 0x80, 0x36,
//           0x3e, 0xee, 0xfb, 0x95, 0x1a, 0xfe, 0xce, 0xa8, 0x34, 0xa9, 0x13, 0xf0, 0xa6, 0x3f, 0xd8, 0x0c,
//           0x78, 0x24, 0xaf, 0x23, 0x52, 0xc1, 0x67, 0x17, 0xf5, 0x66, 0x90, 0xe7, 0xe8, 0x07, 0xb8, 0x60,
//           0x48, 0xe6, 0x1e, 0x53, 0xf3, 0x92, 0xa4, 0x72, 0x8c, 0x08, 0x15, 0x6e, 0x86, 0x00, 0x84, 0xfa,
//           0xf4, 0x7f, 0x8a, 0x42, 0x19, 0xf6, 0xdb, 0xcd, 0x14, 0x8d, 0x50, 0x12, 0xba, 0x3c, 0x06, 0x4e,
//           0xec, 0xb3, 0x35, 0x11, 0xa1, 0x88, 0x8e, 0x2b, 0x94, 0x99, 0xb7, 0x71, 0x74, 0xd3, 0xe4, 0xbf,
//           0x3a, 0xde, 0x96, 0x0e, 0xbc, 0x0a, 0xed, 0x77, 0xfc, 0x37, 0x6b, 0x03, 0x79, 0x89, 0x62, 0xc6,
//           0xd7, 0xc0, 0xd2, 0x7c, 0x6a, 0x8b, 0x22, 0xa3, 0x5b, 0x05, 0x5d, 0x02, 0x75, 0xd5, 0x61, 0xe3,
//           0x18, 0x8f, 0x55, 0x51, 0xad, 0x1f, 0x0b, 0x5e, 0x85, 0xe5, 0xc2, 0x57, 0x63, 0xca, 0x3d, 0x6c,
//           0xb4, 0xc5, 0xcc, 0x70, 0xb2, 0x91, 0x59, 0x0d, 0x47, 0x20, 0xc8, 0x4f, 0x58, 0xe0, 0x01, 0xe2,
//           0x16, 0x38, 0xc4, 0x6f, 0x3b, 0x0f, 0x65, 0x46, 0xbe, 0x7e, 0x2d, 0x7b, 0x82, 0xf9, 0x40, 0xb5,
//           0x1d, 0x73, 0xf8, 0xeb, 0x26, 0xc7, 0x87, 0x97, 0x25, 0x54, 0xb1, 0x28, 0xaa, 0x98, 0x9d, 0xa5,
//           0x64, 0x6d, 0x7a, 0xd4, 0x10, 0x81, 0x44, 0xef, 0x49, 0xd6, 0xae, 0x2e, 0xdd, 0x76, 0x5c, 0x2f,
//           0xa7, 0x1c, 0xc9, 0x09, 0x69, 0x9a, 0x83, 0xcf, 0x29, 0x39, 0xb9, 0xe9, 0x4c, 0xff, 0x43, 0xab
//        };
//
//        private short[] ekb = {
//           0x5d, 0xbe, 0x9b, 0x8b, 0x11, 0x99, 0x6e, 0x4d, 0x59, 0xf3, 0x85, 0xa6, 0x3f, 0xb7, 0x83, 0xc5,
//           0xe4, 0x73, 0x6b, 0x3a, 0x68, 0x5a, 0xc0, 0x47, 0xa0, 0x64, 0x34, 0x0c, 0xf1, 0xd0, 0x52, 0xa5,
//           0xb9, 0x1e, 0x96, 0x43, 0x41, 0xd8, 0xd4, 0x2c, 0xdb, 0xf8, 0x07, 0x77, 0x2a, 0xca, 0xeb, 0xef,
//           0x10, 0x1c, 0x16, 0x0d, 0x38, 0x72, 0x2f, 0x89, 0xc1, 0xf9, 0x80, 0xc4, 0x6d, 0xae, 0x30, 0x3d,
//           0xce, 0x20, 0x63, 0xfe, 0xe6, 0x1a, 0xc7, 0xb8, 0x50, 0xe8, 0x24, 0x17, 0xfc, 0x25, 0x6f, 0xbb,
//           0x6a, 0xa3, 0x44, 0x53, 0xd9, 0xa2, 0x01, 0xab, 0xbc, 0xb6, 0x1f, 0x98, 0xee, 0x9a, 0xa7, 0x2d,
//           0x4f, 0x9e, 0x8e, 0xac, 0xe0, 0xc6, 0x49, 0x46, 0x29, 0xf4, 0x94, 0x8a, 0xaf, 0xe1, 0x5b, 0xc3,
//           0xb3, 0x7b, 0x57, 0xd1, 0x7c, 0x9c, 0xed, 0x87, 0x40, 0x8c, 0xe2, 0xcb, 0x93, 0x14, 0xc9, 0x61,
//           0x2e, 0xe5, 0xcc, 0xf6, 0x5e, 0xa8, 0x5c, 0xd6, 0x75, 0x8d, 0x62, 0x95, 0x58, 0x69, 0x76, 0xa1,
//           0x4a, 0xb5, 0x55, 0x09, 0x78, 0x33, 0x82, 0xd7, 0xdd, 0x79, 0xf5, 0x1b, 0x0b, 0xde, 0x26, 0x21,
//           0x28, 0x74, 0x04, 0x97, 0x56, 0xdf, 0x3c, 0xf0, 0x37, 0x39, 0xdc, 0xff, 0x06, 0xa4, 0xea, 0x42,
//           0x08, 0xda, 0xb4, 0x71, 0xb0, 0xcf, 0x12, 0x7a, 0x4e, 0xfa, 0x6c, 0x1d, 0x84, 0x00, 0xc8, 0x7f,
//           0x91, 0x45, 0xaa, 0x2b, 0xc2, 0xb1, 0x8f, 0xd5, 0xba, 0xf2, 0xad, 0x19, 0xb2, 0x67, 0x36, 0xf7,
//           0x0f, 0x0a, 0x92, 0x7d, 0xe3, 0x9d, 0xe9, 0x90, 0x3e, 0x23, 0x27, 0x66, 0x13, 0xec, 0x81, 0x15,
//           0xbd, 0x22, 0xbf, 0x9f, 0x7e, 0xa9, 0x51, 0x4b, 0x4c, 0xfb, 0x02, 0xd3, 0x70, 0x86, 0x31, 0xe7,
//           0x3b, 0x05, 0x03, 0x54, 0x60, 0x48, 0x65, 0x18, 0xd2, 0xcd, 0x5f, 0x32, 0x88, 0x0e, 0x35, 0xfd
//        };
//
//        private byte[]  iv;
//        private int     parameterVersion = 58;
//
//        protected byte[] engineGetEncoded() 
//        {
//            byte[]  tmp = new byte[iv.length];
//
//            System.arraycopy(iv, 0, tmp, 0, iv.length);
//            return tmp;
//        }
//
//        protected byte[] engineGetEncoded(
//            String format) 
//            throws IOException
//        {
//            if (format.equals("RAW"))
//            {
//                return engineGetEncoded();
//            }
//            else if (format.equals("ASN.1"))
//            {
//                if (parameterVersion == -1)
//                {
//                    return new RC2CBCParameter(engineGetEncoded()).getEncoded();
//                }
//                else
//                {
//                    return new RC2CBCParameter(parameterVersion, engineGetEncoded()).getEncoded();
//                }
//            }
//
//            return null;
//        }
//
//        protected AlgorithmParameterSpec engineGetParameterSpec(
//            Class paramSpec) 
//            throws InvalidParameterSpecException
//        {
//            if (paramSpec == RC2ParameterSpec.class)
//            {
//                if (parameterVersion != -1)
//                {
//                    if (parameterVersion < 256)
//                    {
//                        return new RC2ParameterSpec(ekb[parameterVersion], iv);
//                    }
//                    else
//                    {
//                        return new RC2ParameterSpec(parameterVersion, iv);
//                    }
//                }
//            }
//
//            if (paramSpec == IvParameterSpec.class)
//            {
//                return new IvParameterSpec(iv);
//            }
//
//            throw new InvalidParameterSpecException("unknown parameter spec passed to RC2 parameters object.");
//        }
//
//        protected void engineInit(
//            AlgorithmParameterSpec paramSpec) 
//            throws InvalidParameterSpecException
//        {
//            if (paramSpec instanceof IvParameterSpec)
//            {
//                this.iv = ((IvParameterSpec)paramSpec).getIV();
//            }
//            else if (paramSpec instanceof RC2ParameterSpec)
//            {
//                int effKeyBits = ((RC2ParameterSpec)paramSpec).getEffectiveKeyBits();
//                if (effKeyBits != -1)
//                {
//                    if (effKeyBits < 256)
//                    {
//                        parameterVersion = table[effKeyBits];
//                    }
//                    else
//                    {
//                        parameterVersion = effKeyBits;
//                    }
//                }
//
//                this.iv = ((RC2ParameterSpec)paramSpec).getIV();
//            }
//            else
//            {
//                throw new InvalidParameterSpecException("IvParameterSpec or RC2ParameterSpec required to initialise a RC2 parameters algorithm parameters object");
//            }
//        }
//
//        protected void engineInit(
//            byte[] params) 
//            throws IOException
//        {
//            this.iv = new byte[params.length];
//
//            System.arraycopy(params, 0, iv, 0, iv.length);
//        }
//
//        protected void engineInit(
//            byte[] params,
//            String format) 
//            throws IOException
//        {
//            if (format.equals("RAW"))
//            {
//                engineInit(params);
//                return;
//            }
//            else if (format.equals("ASN.1"))
//            {
//                ASN1InputStream         aIn = new ASN1InputStream(params);
//                RC2CBCParameter         p = RC2CBCParameter.getInstance(aIn.readObject());
//
//                if (p.getRC2ParameterVersion() != null)
//                {
//                    parameterVersion = p.getRC2ParameterVersion().intValue();
//                }
//
//                iv = p.getIV();
//
//                return;
//            }
//
//            throw new IOException("Unknown parameters format in IV parameters object");
//        }
//
//        protected String engineToString() 
//        {
//            return "RC2 Parameters";
//        }
//    }
//
//    public static class CAST5AlgorithmParameters
//        extends JDKAlgorithmParameters
//    {
//        private byte[]  iv;
//        private int     keyLength = 128;
//
//        protected byte[] engineGetEncoded() 
//        {
//            byte[]  tmp = new byte[iv.length];
//
//            System.arraycopy(iv, 0, tmp, 0, iv.length);
//            return tmp;
//        }
//
//        protected byte[] engineGetEncoded(
//            String format) 
//            throws IOException 
//        {
//            if (format.equals("RAW"))
//            {
//                return engineGetEncoded();
//            }
//            // BEGIN android-removed
//            // else if (format.equals("ASN.1"))
//            // {
//            //     return new CAST5CBCParameters(engineGetEncoded(), keyLength).getEncoded();
//            // }
//            // END android-removed
//
//            return null;
//        }
//
//        protected AlgorithmParameterSpec engineGetParameterSpec(
//            Class paramSpec) 
//            throws InvalidParameterSpecException
//        {
//            if (paramSpec == IvParameterSpec.class)
//            {
//                return new IvParameterSpec(iv);
//            }
//
//            throw new InvalidParameterSpecException("unknown parameter spec passed to CAST5 parameters object.");
//        }
//
//        protected void engineInit(
//            AlgorithmParameterSpec paramSpec) 
//            throws InvalidParameterSpecException
//        {
//            if (paramSpec instanceof IvParameterSpec)
//            {
//                this.iv = ((IvParameterSpec)paramSpec).getIV();
//            }
//            else
//            {
//                throw new InvalidParameterSpecException("IvParameterSpec required to initialise a CAST5 parameters algorithm parameters object");
//            }
//        }
//
//        protected void engineInit(
//            byte[] params) 
//            throws IOException
//        {
//            this.iv = new byte[params.length];
//
//            System.arraycopy(params, 0, iv, 0, iv.length);
//        }
//
//        protected void engineInit(
//            byte[] params,
//            String format) 
//            throws IOException
//        {
//            if (format.equals("RAW"))
//            {
//                engineInit(params);
//                return;
//            }
//            // BEGIN android-removed
//            // else if (format.equals("ASN.1"))
//            // {
//            //     ASN1InputStream         aIn = new ASN1InputStream(params);
//            //     CAST5CBCParameters      p = CAST5CBCParameters.getInstance(aIn.readObject());
//            //
//            //     keyLength = p.getKeyLength();
//            //
//            //     iv = p.getIV();
//            //
//            //     return;
//            // }
//            // END android-removed
//
//            throw new IOException("Unknown parameters format in IV parameters object");
//        }
//
//        protected String engineToString() 
//        {
//            return "CAST5 Parameters";
//        }
//    }
// END android-removed

    public static class PKCS12PBE
        extends JDKAlgorithmParameters
    {
        PKCS12PBEParams params;

        protected byte[] engineGetEncoded() 
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);

            try
            {
                dOut.writeObject(params);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Oooops! " + e.toString());
            }

            return bOut.toByteArray();
        }

        protected byte[] engineGetEncoded(
            String format) 
        {
            if (format.equals("ASN.1"))
            {
                return engineGetEncoded();
            }

            return null;
        }

        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == PBEParameterSpec.class)
            {
                return new PBEParameterSpec(params.getIV(),
                                params.getIterations().intValue());
            }

            throw new InvalidParameterSpecException("unknown parameter spec passed to PKCS12 PBE parameters object.");
        }

        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof PBEParameterSpec))
            {
                throw new InvalidParameterSpecException("PBEParameterSpec required to initialise a PKCS12 PBE parameters algorithm parameters object");
            }

            PBEParameterSpec    pbeSpec = (PBEParameterSpec)paramSpec;

            this.params = new PKCS12PBEParams(pbeSpec.getSalt(),
                                pbeSpec.getIterationCount());
        }

        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            ASN1InputStream        aIn = new ASN1InputStream(params);

            this.params = PKCS12PBEParams.getInstance(aIn.readObject());
        }

        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equals("ASN.1"))
            {
                engineInit(params);
                return;
            }

            throw new IOException("Unknown parameters format in PKCS12 PBE parameters object");
        }

        protected String engineToString() 
        {
            return "PKCS12 PBE Parameters";
        }
    }

    public static class DH
        extends JDKAlgorithmParameters
    {
        DHParameterSpec     currentSpec;

        /**
         * Return the PKCS#3 ASN.1 structure DHParameter.
         * <p>
         * <pre>
         *  DHParameter ::= SEQUENCE {
         *                   prime INTEGER, -- p
         *                   base INTEGER, -- g
         *                   privateValueLength INTEGER OPTIONAL}
         * </pre>
         */
        protected byte[] engineGetEncoded() 
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            DHParameter             dhP = new DHParameter(currentSpec.getP(), currentSpec.getG(), currentSpec.getL());

            try
            {
                dOut.writeObject(dhP);
                dOut.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error encoding DHParameters");
            }

            return bOut.toByteArray();
        }

        protected byte[] engineGetEncoded(
            String format) 
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                return engineGetEncoded();
            }

            return null;
        }

        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == DHParameterSpec.class)
            {
                return currentSpec;
            }

            throw new InvalidParameterSpecException("unknown parameter spec passed to DH parameters object.");
        }

        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof DHParameterSpec))
            {
                throw new InvalidParameterSpecException("DHParameterSpec required to initialise a Diffie-Hellman algorithm parameters object");
            }

            this.currentSpec = (DHParameterSpec)paramSpec;
        }

        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            ASN1InputStream        aIn = new ASN1InputStream(params);

            try
            {
                DHParameter dhP = new DHParameter((ASN1Sequence)aIn.readObject());

                if (dhP.getL() != null)
                {
                    currentSpec = new DHParameterSpec(dhP.getP(), dhP.getG(), dhP.getL().intValue());
                }
                else
                {
                    currentSpec = new DHParameterSpec(dhP.getP(), dhP.getG());
                }
            }
            catch (ClassCastException e)
            {
                throw new IOException("Not a valid DH Parameter encoding.");
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                throw new IOException("Not a valid DH Parameter encoding.");
            }
        }

        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                engineInit(params);
            }
            else
            {
                throw new IOException("Unknown parameter format " + format);
            }
        }

        protected String engineToString() 
        {
            return "Diffie-Hellman Parameters";
        }
    }

    public static class DSA
        extends JDKAlgorithmParameters
    {
        DSAParameterSpec     currentSpec;

        /**
         * Return the X.509 ASN.1 structure DSAParameter.
         * <p>
         * <pre>
         *  DSAParameter ::= SEQUENCE {
         *                   prime INTEGER, -- p
         *                   subprime INTEGER, -- q
         *                   base INTEGER, -- g}
         * </pre>
         */
        protected byte[] engineGetEncoded() 
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            DSAParameter            dsaP = new DSAParameter(currentSpec.getP(), currentSpec.getQ(), currentSpec.getG());

            try
            {
                dOut.writeObject(dsaP);
                dOut.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error encoding DSAParameters");
            }

            return bOut.toByteArray();
        }

        protected byte[] engineGetEncoded(
            String format) 
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                return engineGetEncoded();
            }

            return null;
        }

        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == DSAParameterSpec.class)
            {
                return currentSpec;
            }

            throw new InvalidParameterSpecException("unknown parameter spec passed to DSA parameters object.");
        }

        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof DSAParameterSpec))
            {
                throw new InvalidParameterSpecException("DSAParameterSpec required to initialise a DSA algorithm parameters object");
            }

            this.currentSpec = (DSAParameterSpec)paramSpec;
        }

        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            ASN1InputStream        aIn = new ASN1InputStream(params);

            try
            {
                DSAParameter dsaP = new DSAParameter((ASN1Sequence)aIn.readObject());

                currentSpec = new DSAParameterSpec(dsaP.getP(), dsaP.getQ(), dsaP.getG());
            }
            catch (ClassCastException e)
            {
                throw new IOException("Not a valid DSA Parameter encoding.");
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                throw new IOException("Not a valid DSA Parameter encoding.");
            }
        }

        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                engineInit(params);
            }
            else
            {
                throw new IOException("Unknown parameter format " + format);
            }
        }

        protected String engineToString() 
        {
            return "DSA Parameters";
        }
    }
    
// BEGIN android-removed
//    public static class GOST3410
//        extends JDKAlgorithmParameters
//    {
//        GOST3410ParameterSpec     currentSpec;
//        
//        /**
//         * Return the X.509 ASN.1 structure GOST3410Parameter.
//         * <p>
//         * <pre>
//         *  GOST3410Parameter ::= SEQUENCE {
//         *                   prime INTEGER, -- p
//         *                   subprime INTEGER, -- q
//         *                   base INTEGER, -- a}
//         * </pre>
//         */
//        protected byte[] engineGetEncoded()
//        {
//            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
//            DEROutputStream         dOut = new DEROutputStream(bOut);
//            GOST3410PublicKeyAlgParameters       gost3410P = new GOST3410PublicKeyAlgParameters(new DERObjectIdentifier(currentSpec.getPublicKeyParamSetOID()), new DERObjectIdentifier(currentSpec.getDigestParamSetOID()), new DERObjectIdentifier(currentSpec.getEncryptionParamSetOID()));
//            
//            try
//            {
//                dOut.writeObject(gost3410P);
//                dOut.close();
//            }
//            catch (IOException e)
//            {
//                throw new RuntimeException("Error encoding GOST3410Parameters");
//            }
//            
//            return bOut.toByteArray();
//        }
//        
//        protected byte[] engineGetEncoded(
//                String format)
//        {
//            if (format.equalsIgnoreCase("X.509")
//                    || format.equalsIgnoreCase("ASN.1"))
//            {
//                return engineGetEncoded();
//            }
//            
//            return null;
//        }
//        
//        protected AlgorithmParameterSpec engineGetParameterSpec(
//                Class paramSpec)
//        throws InvalidParameterSpecException
//        {
//            if (paramSpec == GOST3410PublicKeyParameterSetSpec.class)
//            {
//                return currentSpec;
//            }
//            
//            throw new InvalidParameterSpecException("unknown parameter spec passed to GOST3410 parameters object.");
//        }
//        
//        protected void engineInit(
//                AlgorithmParameterSpec paramSpec)
//        throws InvalidParameterSpecException
//        {
//            if (!(paramSpec instanceof GOST3410ParameterSpec))
//            {
//                throw new InvalidParameterSpecException("GOST3410ParameterSpec required to initialise a GOST3410 algorithm parameters object");
//            }
//            
//            this.currentSpec = (GOST3410ParameterSpec)paramSpec;
//        }
//        
//        protected void engineInit(
//                byte[] params)
//        throws IOException
//        {
//            ASN1InputStream        dIn = new ASN1InputStream(params);
//            
//            try
//            {
//                GOST3410PublicKeyAlgParameters gost3410P = new GOST3410PublicKeyAlgParameters((ASN1Sequence)dIn.readObject());
//                
//                currentSpec = new GOST3410ParameterSpec(gost3410P.getPublicKeyParamSet().getId(), gost3410P.getDigestParamSet().getId(), gost3410P.getEncryptionParamSet().getId());
//            }
//            catch (ClassCastException e)
//            {
//                throw new IOException("Not a valid GOST3410 Parameter encoding.");
//            }
//            catch (ArrayIndexOutOfBoundsException e)
//            {
//                throw new IOException("Not a valid GOST3410 Parameter encoding.");
//            }
//        }
//        
//        protected void engineInit(
//                byte[] params,
//                String format)
//        throws IOException
//        {
//            if (format.equalsIgnoreCase("X.509")
//                    || format.equalsIgnoreCase("ASN.1"))
//            {
//                engineInit(params);
//            }
//            else
//            {
//                throw new IOException("Unknown parameter format " + format);
//            }
//        }
//        
//        protected String engineToString()
//        {
//            return "GOST3410 Parameters";
//        }
//    }
//
//    public static class ElGamal
//        extends JDKAlgorithmParameters
//    {
//        ElGamalParameterSpec     currentSpec;
//
//        /**
//         * Return the X.509 ASN.1 structure ElGamalParameter.
//         * <p>
//         * <pre>
//         *  ElGamalParameter ::= SEQUENCE {
//         *                   prime INTEGER, -- p
//         *                   base INTEGER, -- g}
//         * </pre>
//         */
//        protected byte[] engineGetEncoded() 
//        {
//            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
//            DEROutputStream         dOut = new DEROutputStream(bOut);
//            ElGamalParameter        elP = new ElGamalParameter(currentSpec.getP(), currentSpec.getG());
//
//            try
//            {
//                dOut.writeObject(elP);
//                dOut.close();
//            }
//            catch (IOException e)
//            {
//                throw new RuntimeException("Error encoding ElGamalParameters");
//            }
//
//            return bOut.toByteArray();
//        }
//
//        protected byte[] engineGetEncoded(
//            String format) 
//        {
//            if (format.equalsIgnoreCase("X.509")
//                    || format.equalsIgnoreCase("ASN.1"))
//            {
//                return engineGetEncoded();
//            }
//
//            return null;
//        }
//
//        protected AlgorithmParameterSpec engineGetParameterSpec(
//            Class paramSpec) 
//            throws InvalidParameterSpecException
//        {
//            if (paramSpec == ElGamalParameterSpec.class)
//            {
//                return currentSpec;
//            }
//            else if (paramSpec == DHParameterSpec.class)
//            {
//                return new DHParameterSpec(currentSpec.getP(), currentSpec.getG());
//            }
//
//            throw new InvalidParameterSpecException("unknown parameter spec passed to ElGamal parameters object.");
//        }
//
//        protected void engineInit(
//            AlgorithmParameterSpec paramSpec) 
//            throws InvalidParameterSpecException
//        {
//            if (!(paramSpec instanceof ElGamalParameterSpec) && !(paramSpec instanceof DHParameterSpec))
//            {
//                throw new InvalidParameterSpecException("DHParameterSpec required to initialise a ElGamal algorithm parameters object");
//            }
//
//            if (paramSpec instanceof ElGamalParameterSpec)
//            {
//                this.currentSpec = (ElGamalParameterSpec)paramSpec;
//            }
//            else
//            {
//                DHParameterSpec s = (DHParameterSpec)paramSpec;
//                
//                this.currentSpec = new ElGamalParameterSpec(s.getP(), s.getG());
//            }
//        }
//
//        protected void engineInit(
//            byte[] params) 
//            throws IOException
//        {
//            ASN1InputStream        aIn = new ASN1InputStream(params);
//
//            try
//            {
//                ElGamalParameter elP = new ElGamalParameter((ASN1Sequence)aIn.readObject());
//
//                currentSpec = new ElGamalParameterSpec(elP.getP(), elP.getG());
//            }
//            catch (ClassCastException e)
//            {
//                throw new IOException("Not a valid ElGamal Parameter encoding.");
//            }
//            catch (ArrayIndexOutOfBoundsException e)
//            {
//                throw new IOException("Not a valid ElGamal Parameter encoding.");
//            }
//        }
//
//        protected void engineInit(
//            byte[] params,
//            String format) 
//            throws IOException
//        {
//            if (format.equalsIgnoreCase("X.509")
//                    || format.equalsIgnoreCase("ASN.1"))
//            {
//                engineInit(params);
//            }
//            else
//            {
//                throw new IOException("Unknown parameter format " + format);
//            }
//        }
//
//        protected String engineToString() 
//        {
//            return "ElGamal Parameters";
//        }
//    }
// END android-removed

    public static class IES
        extends JDKAlgorithmParameters
    {
        IESParameterSpec     currentSpec;

        /**
         * in the abscence of a standard way of doing it this will do for
         * now...
         */
        protected byte[] engineGetEncoded() 
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);

            try
            {
                ASN1EncodableVector    v = new ASN1EncodableVector();

                v.add(new DEROctetString(currentSpec.getDerivationV()));
                v.add(new DEROctetString(currentSpec.getEncodingV()));
                v.add(new DERInteger(currentSpec.getMacKeySize()));

                dOut.writeObject(new DERSequence(v));
                dOut.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error encoding IESParameters");
            }

            return bOut.toByteArray();
        }

        protected byte[] engineGetEncoded(
            String format) 
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                return engineGetEncoded();
            }

            return null;
        }

        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == IESParameterSpec.class)
            {
                return currentSpec;
            }

            throw new InvalidParameterSpecException("unknown parameter spec passed to ElGamal parameters object.");
        }

        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof IESParameterSpec))
            {
                throw new InvalidParameterSpecException("IESParameterSpec required to initialise a IES algorithm parameters object");
            }

            this.currentSpec = (IESParameterSpec)paramSpec;
        }

        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            ASN1InputStream        aIn = new ASN1InputStream(params);

            try
            {
                ASN1Sequence    s = (ASN1Sequence)aIn.readObject();

                this.currentSpec = new IESParameterSpec(
                                        ((ASN1OctetString)s.getObjectAt(0)).getOctets(),
                                        ((ASN1OctetString)s.getObjectAt(0)).getOctets(),
                                        ((DERInteger)s.getObjectAt(0)).getValue().intValue());
            }
            catch (ClassCastException e)
            {
                throw new IOException("Not a valid IES Parameter encoding.");
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                throw new IOException("Not a valid IES Parameter encoding.");
            }
        }

        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                engineInit(params);
            }
            else
            {
                throw new IOException("Unknown parameter format " + format);
            }
        }

        protected String engineToString() 
        {
            return "IES Parameters";
        }
    }
    
    public static class OAEP
        extends JDKAlgorithmParameters
    {
        OAEPParameterSpec     currentSpec;
    
        /**
         * Return the PKCS#1 ASN.1 structure RSA-ES-OAEP-params.
         */
        protected byte[] engineGetEncoded() 
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            // BEGIN android-changed
            AlgorithmIdentifier     hashAlgorithm = new AlgorithmIdentifier(
                                                            JCEDigestUtil.getOID(currentSpec.getDigestAlgorithm()),
                                                            DERNull.THE_ONE);
            MGF1ParameterSpec       mgfSpec = (MGF1ParameterSpec)currentSpec.getMGFParameters();
            AlgorithmIdentifier     maskGenAlgorithm = new AlgorithmIdentifier(
                                                            PKCSObjectIdentifiers.id_mgf1, 
                                                            new AlgorithmIdentifier(JCEDigestUtil.getOID(mgfSpec.getDigestAlgorithm()), DERNull.THE_ONE));
            // END android-changed
            PSource.PSpecified      pSource = (PSource.PSpecified)currentSpec.getPSource();
            AlgorithmIdentifier     pSourceAlgorithm = new AlgorithmIdentifier(
                                                            PKCSObjectIdentifiers.id_pSpecified, new DEROctetString(pSource.getValue()));
            RSAESOAEPparams         oaepP = new RSAESOAEPparams(hashAlgorithm, maskGenAlgorithm, pSourceAlgorithm);
    
            try
            {
                dOut.writeObject(oaepP);
                dOut.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error encoding OAEPParameters");
            }
    
            return bOut.toByteArray();
        }
    
        protected byte[] engineGetEncoded(
            String format) 
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                return engineGetEncoded();
            }
    
            return null;
        }
    
        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == OAEPParameterSpec.class && currentSpec instanceof OAEPParameterSpec)
            {
                return currentSpec;
            }
    
            throw new InvalidParameterSpecException("unknown parameter spec passed to OAEP parameters object.");
        }
    
        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof OAEPParameterSpec))
            {
                throw new InvalidParameterSpecException("OAEPParameterSpec required to initialise an OAEP algorithm parameters object");
            }
    
            this.currentSpec = (OAEPParameterSpec)paramSpec;
        }
    
        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            ASN1InputStream        aIn = new ASN1InputStream(params);
    
            try
            {
                RSAESOAEPparams oaepP = new RSAESOAEPparams((ASN1Sequence)aIn.readObject());
    
                currentSpec = new OAEPParameterSpec(
                                       oaepP.getHashAlgorithm().getObjectId().getId(), 
                                       oaepP.getMaskGenAlgorithm().getObjectId().getId(), 
                                       new MGF1ParameterSpec(AlgorithmIdentifier.getInstance(oaepP.getMaskGenAlgorithm().getParameters()).getObjectId().getId()),
                                       new PSource.PSpecified(ASN1OctetString.getInstance(oaepP.getPSourceAlgorithm().getParameters()).getOctets()));
            }
            catch (ClassCastException e)
            {
                throw new IOException("Not a valid OAEP Parameter encoding.");
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                throw new IOException("Not a valid OAEP Parameter encoding.");
            }
        }
    
        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                engineInit(params);
            }
            else
            {
                throw new IOException("Unknown parameter format " + format);
            }
        }
    
        protected String engineToString() 
        {
            return "OAEP Parameters";
        }
    }
    
    public static class PSS
        extends JDKAlgorithmParameters
    {  
        PSSParameterSpec     currentSpec;
    
        /**
         * Return the PKCS#1 ASN.1 structure RSA-ES-OAEP-params.
         */
        protected byte[] engineGetEncoded() 
            throws IOException
        {
            PSSParameterSpec    pssSpec = (PSSParameterSpec)currentSpec;
            // BEGIN android-changed
            AlgorithmIdentifier hashAlgorithm = new AlgorithmIdentifier(
                                                JCEDigestUtil.getOID(pssSpec.getDigestAlgorithm()),
                                                DERNull.THE_ONE);
            MGF1ParameterSpec   mgfSpec = (MGF1ParameterSpec)pssSpec.getMGFParameters();
            AlgorithmIdentifier maskGenAlgorithm = new AlgorithmIdentifier(
                                                PKCSObjectIdentifiers.id_mgf1, 
                                                new AlgorithmIdentifier(JCEDigestUtil.getOID(mgfSpec.getDigestAlgorithm()), DERNull.THE_ONE));
            // END android-changed
            RSASSAPSSparams     pssP = new RSASSAPSSparams(hashAlgorithm, maskGenAlgorithm, new DERInteger(pssSpec.getSaltLength()), new DERInteger(pssSpec.getTrailerField()));
            
            return pssP.getEncoded("DER");
        }
    
        protected byte[] engineGetEncoded(
            String format) 
            throws IOException
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                return engineGetEncoded();
            }
    
            return null;
        }
    
        protected AlgorithmParameterSpec engineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            if (paramSpec == PSSParameterSpec.class && currentSpec instanceof PSSParameterSpec)
            {
                return currentSpec;
            }
    
            throw new InvalidParameterSpecException("unknown parameter spec passed to PSS parameters object.");
        }
    
        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof PSSParameterSpec))
            {
                throw new InvalidParameterSpecException("PSSParameterSpec required to initialise an PSS algorithm parameters object");
            }
    
            this.currentSpec = (PSSParameterSpec)paramSpec;
        }
    
        protected void engineInit(
            byte[] params) 
            throws IOException
        {
            ASN1InputStream        aIn = new ASN1InputStream(params);
    
            try
            {
                RSASSAPSSparams pssP = new RSASSAPSSparams((ASN1Sequence)aIn.readObject());
    
                currentSpec = new PSSParameterSpec(
                                       pssP.getHashAlgorithm().getObjectId().getId(), 
                                       pssP.getMaskGenAlgorithm().getObjectId().getId(), 
                                       new MGF1ParameterSpec(AlgorithmIdentifier.getInstance(pssP.getMaskGenAlgorithm().getParameters()).getObjectId().getId()),
                                       pssP.getSaltLength().getValue().intValue(),
                                       pssP.getTrailerField().getValue().intValue());
            }
            catch (ClassCastException e)
            {
                throw new IOException("Not a valid PSS Parameter encoding.");
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                throw new IOException("Not a valid PSS Parameter encoding.");
            }
        }
    
        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            if (format.equalsIgnoreCase("X.509")
                    || format.equalsIgnoreCase("ASN.1"))
            {
                engineInit(params);
            }
            else
            {
                throw new IOException("Unknown parameter format " + format);
            }
        }
    
        protected String engineToString() 
        {
            return "PSS Parameters";
        }
    }
}
