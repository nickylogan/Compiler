package computer;

public class cacheMMU {
  private int size_MMU;
  private byte[] memCache;

  cacheMMU() {
    size_MMU = 4096; // kapasitas dasar 4Kbyte
    memCache = new byte[size_MMU];
  }

  // capMMU, dalam satuan kilo bytes (K)
  cacheMMU(int capMMU) {
    size_MMU = capMMU * 1024; // kapasitas dasar 4Kbyte
    memCache = new byte[size_MMU];
  }
}
