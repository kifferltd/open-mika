package java.util.jar;

import gnu.java.security.OID;
import gnu.java.security.pkcs.PKCS7SignedData;
import gnu.java.security.pkcs.SignerInfo;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;

class JarVerifier {

  /**
   * Taken from classpath JarFile
   * Signature OIDs. 
   */
  private static final OID MD2_OID = new OID("1.2.840.113549.2.2");
  private static final OID MD4_OID = new OID("1.2.840.113549.2.4");
  private static final OID MD5_OID = new OID("1.2.840.113549.2.5");
  private static final OID SHA1_OID = new OID("1.3.14.3.2.26");
  private static final OID DSA_ENCRYPTION_OID = new OID("1.2.840.10040.4.1");
  private static final OID RSA_ENCRYPTION_OID = new OID("1.2.840.113549.1.1.1");

  /** The META-INF directory entry. */
  private static final String META_INF = "META-INF/";

  /** The suffix for PKCS7 DSA signature entries. */
  private static final String PKCS7_DSA_SUFFIX = ".DSA";

  /** The suffix for PKCS7 RSA signature entries. */
  private static final String PKCS7_RSA_SUFFIX = ".RSA";

  /** The suffix for digest attributes. */
  private static final String DIGEST_KEY_SUFFIX = "-Digest";

  /** The suffix for signature files. */
  private static final String SF_SUFFIX = ".SF";
  /*Done import of classpath code ... */  
  /**
   * Base64 encoding algorithm ... Use char[] as lookup table !
   */
  private static final char[] base64_chars = { 'A', 'B', 'C', 'D', 'E', 'F',
      'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
      'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
      'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
      'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      '+', '/' };

  // TODO write some test for 'encode' function
  private static String encode(byte[] bytes) {
    int len = bytes.length;
    int loops = len / 3;
    int extra = len % 3;
    int off = 0;
    int idx = 0;
    char[] chars = new char[(loops + (extra == 0 ? 0 : 1)) * 4];
    while (loops-- > 0) {
      int b1 = bytes[idx++];
      chars[off++] = base64_chars[(b1 >> 2) & 0x03f];
      int b2 = bytes[idx++];
      chars[off++] = base64_chars[((b1 << 4) | ((b2 >> 4) & (0x00f))) & 0x03f];
      int b3 = bytes[idx++];
      chars[off++] = base64_chars[((b2 << 2) | ((b3 >> 6) & (0x003))) & 0x03f];
      chars[off++] = base64_chars[b3 & 0x03f];
    }
    if (extra > 0) {
      int b1 = bytes[idx++];
      chars[off++] = base64_chars[(b1 >> 2) & 0x03f];
      if (extra == 1) {
        chars[off++] = base64_chars[(b1 << 4) & 0x03f];
        chars[off] = '=';
      } else {
        int b2 = bytes[idx++];
        chars[off++] = base64_chars[((b1 << 4) | ((b2 >> 4) & (0x00f))) & 0x03f];
        chars[off++] = base64_chars[(b2 << 2) & 0x03f];
      }
      chars[off] = '=';
    }
    return new String(chars);
  }

  static void verifyBytes(byte[] bytes, String algorithm, String value) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance(algorithm);
    bytes =  md.digest(bytes);
    String encoded = encode(bytes);
    /* System.out.println("Verifyer.verifyBytes() encoded = '"+encoded+"" +
        "', value '"+value+"'"); */
    if (!encoded.equals(value)) {
      throw new SecurityException("verification failed");        
    }
  }

  static String[] getDigestKeyAlgortihm(Attributes attributes) {
    return JarVerifier.getDigestKeyAlgortihm(attributes, DIGEST_KEY_SUFFIX);
  }

  static String[] getDigestKeyAlgortihm(Attributes attributes, String pattern) {
    Iterator iterator = attributes.keySet().iterator();
    while (iterator.hasNext()) {
      Attributes.Name object =  (Attributes.Name) iterator.next();
      String element = object.toString();
      if (element.endsWith(pattern)) {
        return new String[]{element.substring(0, element.indexOf(pattern)), 
            attributes.getValue(element)};
      } else {
        //System.out.println("Verifyer.getDigestKeyAlgortihm() '"+element+"' is not a valid -Digest name");
      }
    }
    return null;
  }

  /**
   * Wrapper to use classpaths PKCS7 signed data parser. Large part of the following code where
   * taken from classpaths JarFile implentation.
   */
  
  static void verifyBlockSignatureFile(String name, JarEntry je, JarFile jf) {
    try {
      String alias = name.substring(META_INF.length(), name.lastIndexOf('.'));
      PKCS7SignedData sig = new PKCS7SignedData(jf.getInputStream(je));
      //sample code take from classpath Jarfile
      Set validCerts = new HashSet();
      
      Certificate[] certs = sig.getCertificates();
      Set signerInfos = sig.getSignerInfos();
      for (Iterator it2 = signerInfos.iterator(); it2.hasNext(); ) {
        verify(certs, (SignerInfo) it2.next(), alias, validCerts, jf);
      }
      if (validCerts.isEmpty()) {
        throw new SecurityException("No valid certificate");
      }
    } catch (CRLException e) {
      // TODO Auto-generated catch block
    } catch (CertificateException e) {
      // TODO Auto-generated catch block
    } catch (IOException e) {
      // TODO Auto-generated catch block
    }
  }
  /**
   * GRU: Sample from classpath JarFile implentation ... Tell if the given
   * signer info is over the given alias's signature file, given one of the
   * certificates specified.
   */
  private static void verify(Certificate[] certs, SignerInfo signerInfo,
      String alias, Set validCerts, JarFile jf) {
    Signature sig = null;
    try {
      OID alg = signerInfo.getDigestEncryptionAlgorithmId();
      if (alg.equals(DSA_ENCRYPTION_OID)) {
        if (!signerInfo.getDigestAlgorithmId().equals(SHA1_OID)) {
          return;
        }
        sig = Signature.getInstance("SHA1withDSA");
      } else if (alg.equals(RSA_ENCRYPTION_OID)) {
        OID hash = signerInfo.getDigestAlgorithmId();
        if (hash.equals(MD2_OID))
          sig = Signature.getInstance("md2WithRsaEncryption");
        else if (hash.equals(MD4_OID))
          sig = Signature.getInstance("md4WithRsaEncryption");
        else if (hash.equals(MD5_OID))
          sig = Signature.getInstance("md5WithRsaEncryption");
        else if (hash.equals(SHA1_OID))
          sig = Signature.getInstance("sha1WithRsaEncryption");
        else
          return;
      } else {
          System.out.println("Verifyer.verify()unsupported signature algorithm: " + alg);
        return;
      }
    } catch (NoSuchAlgorithmException nsae) {
      nsae.printStackTrace();     
      return;
    }
    ZipEntry sigFileEntry = jf.getEntry(META_INF + alias + SF_SUFFIX);
    if (sigFileEntry == null) {
      System.out.println("JarVerifier.verify(): No corresponding signature file found");
      return;
    }
    for (int i = 0; i < certs.length; i++) {
      if (!(certs[i] instanceof X509Certificate)) {
        continue;
      }
      X509Certificate cert = (X509Certificate) certs[i];
      if (!cert.getIssuerX500Principal().equals(
          signerInfo.getIssuer())
          || !cert.getSerialNumber().equals(signerInfo.getSerialNumber())) {
        continue;
      }
      try {
        sig.initVerify(cert.getPublicKey());
        InputStream in = jf.getInputStream(sigFileEntry);
        if (in == null)
          continue;
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = in.read(buf)) != -1)
          sig.update(buf, 0, len);
        if (sig.verify(signerInfo.getEncryptedDigest())) {          
          System.out.println("Verifyer.verify()signature for " + cert.getSubjectDN() + " is good");
          validCerts.add(cert);
        }
      } catch (IOException ioe) {
        continue;
      } catch (InvalidKeyException ike) {
        continue;
      } catch (SignatureException se) {
        continue;
      }
    }
  }

  static boolean verifyManifest(JarFile file) throws IOException {
    boolean do_verify = false;
    try {
      Enumeration en = file.entries();
      while (en.hasMoreElements()) {
        JarEntry je = (JarEntry) en.nextElement();
        String name = je.getName();
        if (name.startsWith("META-INF/")) {
          if (name.endsWith(".SF")) {
            // System.out.println("Verifyer.verifyManifest() found a valid .SF
            // file: "+name);
            do_verify = true;
            file.verifyManifest(je);
          } else if (!name.equals(JarFile.MANIFEST_NAME)
              && !name.equals("META-INF/") && (name.lastIndexOf('.') != -1)
              && (name.indexOf('/', 9) == -1)) {
            do_verify = true;
            JarVerifier.verifyBlockSignatureFile(name, je, file);
          }
        }
      }
    } catch (SecurityException se) {
      file.verificationFailed = true;
      return true;
    }
    return do_verify;
  }   
}
