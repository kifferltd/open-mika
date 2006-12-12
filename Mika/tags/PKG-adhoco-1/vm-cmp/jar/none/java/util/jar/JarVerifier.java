package java.util.jar;


import java.security.NoSuchAlgorithmException;
import java.io.IOException;

class JarVerifier {

  static void verifyBytes(byte[] bytes, String algorithm, String value) throws NoSuchAlgorithmException {
  }

  static String[] getDigestKeyAlgortihm(Attributes attributes) {
    return null;
  }

  static String[] getDigestKeyAlgortihm(Attributes attributes, String pattern) {
    return null;
  }

  static void verifyBlockSignatureFile(String name, JarEntry je, JarFile jf) {
  }
  
  static boolean verifyManifest(JarFile file) throws IOException {
    return false;
  }
}
