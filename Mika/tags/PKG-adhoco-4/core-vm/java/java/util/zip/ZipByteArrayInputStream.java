package java.util.zip;

import java.io.ByteArrayInputStream;

class ZipByteArrayInputStream extends ByteArrayInputStream {

  public ZipByteArrayInputStream(byte[] b) {
    super(b);
  }
  
  byte[] getBytes() {
    return buf;
  }
}
