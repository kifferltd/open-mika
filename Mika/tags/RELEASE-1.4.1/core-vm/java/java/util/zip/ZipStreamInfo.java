package java.util.zip;

final class ZipStreamInfo {

  int nrEntries;
  int entryPointer;
  int currentEntry;
  byte[] data;
  int have;

  public ZipStreamInfo(int nrE, int stP, byte[] bytes) {
    this.nrEntries = nrE;
    this.entryPointer = stP;
    this.data = bytes;
  }
}
