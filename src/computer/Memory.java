package computer;

public class Memory {
  private byte[] aCells; // memory cell (byte) data
  private int sizeCells; // ukuran (kapasitas) memory
  private String descIdMemory; // deskripsi identitas memori
  private int idMemory; // code (magic number) identitas memori

  // memory,status: digunakan untuk simulasi hardware memori
  private boolean memoryHealth; // status memori status diperbaharui saat POWER-ON
  private boolean readHealth; // true: sukses baca, false: gagal baca karena lokasi
  private boolean writeHealth; // true: sukses tulis, false: gagal tulis karena lokasi

  // constructor, default 32KB
  Memory() {
    sizeCells = 32768; // 32KB
    aCells = new byte[sizeCells];
    idMemory = 3; // default RAM
    descIdMemory = "RAM";
    memoryHealth = true; // OK, siap digunakan
    readHealth = true; // OK
    writeHealth = true; // OK
  }

  // constructor, set dari luar
  // szMem - dalam satuan KB
  Memory(int szMem) {
    sizeCells = szMem * 1024; //
    aCells = new byte[sizeCells];
    idMemory = 3; // default RAM
    descIdMemory = "RAM";
    memoryHealth = true; // OK, siap digunakan
    readHealth = true; // OK
    writeHealth = true; // OK
  }

  // memeriksa apakah lokasi yang diminta satu byte
  //   berada di luar area, data bus = 1 byte
  // mekanisme handshaking
  public boolean isLocationForByteExist(int lokasi) {
    if ((lokasi < sizeCells) && (lokasi >= 0))
      return true; // dalam rentang lokasi memori
    else
      return false; // di luar rentang lokasi memori
  }

  // memeriksa apakah lokasi yang diminta satu word (asumsi 32 bits)
  //   berada di luar area, data bus = word
  //  mekanisme handshaking
  public boolean isLocationForWordExist(int lokasi) {
    // konstanta 3 untuk ujung addres word
    if (((lokasi + 3) < sizeCells) && (lokasi >= 0))
      return true; // dalam rentang lokasi memori
    else
      return false; // di luar rentang lokasi memori
  }

  // membaca 1 byte di lokasi memori yang diminta
  // asumsi lokasi
  public byte readByteMemory(int lokasi) {
    if (isLocationForByteExist(lokasi)) {
      readHealth = true;
      return aCells[lokasi];
    } else {
      readHealth = false;
      return (byte) -1;
    }
  }


  // membaca 1 word data di lokasi memori yang diminta
  // asumsi lokasi
  public byte[] readWordMemory(int lokasi) {
    byte[] readWord;
    readWord = new byte[4];
    if (isLocationForWordExist(lokasi)) {
      readHealth = true;
      readWord[0] = aCells[lokasi];
      readWord[1] = aCells[lokasi + 1];
      readWord[2] = aCells[lokasi + 2];
      readWord[3] = aCells[lokasi + 3];
    } else readHealth = false;
    return readWord;
  }

  // menulis 1 byte di lokasi memori yang ditetapkan
  // asumsi lokasi
  public void writeByteMemory(int lokasi, byte dataByte) {
    if (isLocationForByteExist(lokasi)) {
      writeHealth = true;
      aCells[lokasi] = dataByte;
    } else writeHealth = false;
  }

  // menulis 1 word data di lokasi memori yang ditetapkan
  // asumsi lokasi
  public void writeWordMemory(int lokasi, byte[] wword) {
    if (isLocationForWordExist(lokasi)) {
      readHealth = true;
      aCells[lokasi] = wword[0];
      aCells[lokasi + 1] = wword[1];
      aCells[lokasi + 2] = wword[2];
      aCells[lokasi + 3] = wword[3];
    } else readHealth = false;
  }


  // Bisa ditambahkan beberapa methods untuk mensimulasikan
  //   koordinasi atau handshaking pada saat baca atau tulis
  // public bool readyWrite(): penanda siap untuk menerima (menulis) data ke memori
  // public bool readyRead(): penanda siap untuk mengirim (membaca) data dari memori
  // public bool acknowledgeRead()
  // public bool acknowledgeWrite()
  // public bool failRead()
  // public bool failWrite()

  public boolean successRead() {
    return readHealth;
  }

  public boolean successWrite() {
    return writeHealth;
  }

  // make numeric (32 bits) in big endian
  public byte[] makeBigEndian(int num32) {
    int perbyte;
    byte[] aBig;
    aBig = new byte[4]; // 32 bits
    perbyte = num32 >> 24;
    aBig[0] = (byte) perbyte;
    perbyte = num32 >> 16;
    aBig[1] = (byte) perbyte;
    perbyte = num32 >> 8;
    aBig[2] = (byte) perbyte;
    perbyte = num32 & 0x000000FF;
    aBig[3] = (byte) perbyte;
    return aBig;
  }

  // make numeric (32 bits) in little endian
  public byte[] makeLittleEndian(int num32) {
    int perbyte;
    byte[] aLittle;
    aLittle = new byte[4]; // 32 bits
    perbyte = num32 & 0x000000FF;
    aLittle[0] = (byte) perbyte;
    perbyte = (num32 >> 8) & 0x000000FF;
    aLittle[1] = (byte) perbyte;
    perbyte = (num32 >> 16) & 0x000000FF;
    aLittle[2] = (byte) perbyte;
    perbyte = (num32 >> 24) & 0x000000FF;
    aLittle[3] = (byte) perbyte;
    return aLittle;
  }

  // void dspDataInMemory(int startLoc); menampilkan satu blok data (16 bytes/perbaris)
  public void dspDataInMemory(int startLoc) {
    int i, k;
    int adrLine;

    adrLine = ((int) (startLoc / 16)) * 16;
    System.out.println("Display data in memory ");
    for (k = 0; k < 8; k++) { // perpage display : 8 baris
      System.out.printf("%08x -> ", adrLine);
      for (i = 0; i < 16; i++) {
        System.out.printf("%02x ", aCells[adrLine]);
        ++adrLine;
      } // endfor
      System.out.printf("\n");
    } // endfor 6 lines
  }

  // penetapan banyaknya baris perpage
  public void dspDataInMemory(int startLoc, int linePage) {
    int i, k;
    int adrLine;

    adrLine = ((int) (startLoc / 16)) * 16;
    System.out.println("Display data in memory ");
    for (k = 0; k < linePage; k++) { // perpage display : linePage
      System.out.printf("%08x -> ", adrLine);
      for (i = 0; i < 16; i++) {
        System.out.printf("%02x ", aCells[adrLine]);
        ++adrLine;
      } // endfor
      System.out.printf("\n");
    } // endfor 6 lines
  }

  // untuk verifikasi profil memori
  public void allInfoMemory() {
    System.out.println("Id memory : " + idMemory);
    System.out.println("Deskripsi memory : " + descIdMemory);
    System.out.println("Ukuran memory : " + sizeCells + " bytes");
  }

  public int memorySize() {
    return sizeCells;
  }

  public int memoryIdType() {
    return idMemory;
  }

  public String memoryType() {
    return descIdMemory;
  }

  public byte[] getaCells() {
    return aCells.clone();
  }
}
