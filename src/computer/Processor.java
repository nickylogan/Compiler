package computer;// public class Processor implements opcodeList

import java.io.*;
import java.util.Scanner;
import java.util.*;

public class Processor implements OperationCode {
  // Registers
  public int[] aRegister = new int[16]; // semula private
  public int PC; // register PC
  public int FLAG; // flag register
  public int BREG; //base Register untuk memfasilitasi memory abstraction
  public int LREG; // limitRegister untuk membatasi area(zone) per program di memori
  // stack memory
  //   size: 2048 bytes (2K), 512 slot (per-slot 4 bytes)
  //   lokasi 2 K dari alamat memori teratas (32K-2K), asumsi kapasitas memori 32K
  public int sizeStack = 2048;
  public int SP; // stack pointer register untuk menunjukkan Top of stack pada stack memori
  public int BP; // base pointer register untuk menunjukkan lokasi akhir yang dapat digunakan untuk stack memori

  public int PAGE; // page register untuk memfasilitasi virtual memory
  public int OFFSET; // offset register untuk memfasilitasi virtual memory
  public int THREAD; // thread register untuk memfasilitasi multithreading
  public int PFAULT; // page fault register
  public int PFAULTPC; // register PC untuk menangani page fault
  public boolean outStackMemory; // address memori stack tidak terjangkau

  // Data
  private int defStartLoc; // lokasi default untuk eksekusi code (100H)
  private byte[] IR = new byte[4]; // instruction register, coded
  private Endian endianUsed;
  private long addressLine; // maksimum kapasitas pengalamat prosesor (> 32 bits)
  private byte opcode; // hanya operation code dari struktur/word instruksi
  private boolean endExecution; // flag: true->masih proses eksekusi, false-akhir eksekusi

  // Connect ke memori
  public Memory RAM;// = new Memory();
  // Connect ke MMU
//    public cacheMMU MMU; // memory management unit, persiapan untuk Virtual memory    

  Processor(Memory pRAM) {
//      System.out.println("Register -> "+Register.r8.toString());
    RAM = pRAM;
    defStartLoc = 0; // start address to feth first instruction
    PC = defStartLoc;
    endianUsed = Endian.littleEndian;
    addressLine = 32; // default
    endExecution = true; // awal: belum mulai eksekusi
    opcode = UNKNOWN; // unknown operation (opcode)
    FLAG = 1; // init flag register, ZF=1
    BREG = 0; // base address untuk proses
    LREG = 0;
    //set up memory stack
    SP = 32 * 1024 - sizeStack;
    BP = 32 * 1024 - 1; // ujung yang dapat diakses untuk stack
    outStackMemory = false; // default: alamat masih dapat diakses
  }

  // constructor belum terkoneksi ke RAM (main memory)
  Processor() {
//      System.out.println("Register -> "+Register.r8.toString());
    defStartLoc = 0; // start address to feth first instruction
    PC = defStartLoc;
    endianUsed = Endian.littleEndian;
    addressLine = 32; // default
    endExecution = true; // awal: belum mulai eksekusi
    opcode = UNKNOWN; // unknown operation (opcode)
    FLAG = 1; // init flag register, ZF=1
    BREG = 0; // base address untuk proses
    LREG = 0;
    //set up memory stack
    SP = 32 * 1024 - sizeStack;
    BP = 32 * 1024 - 1; // ujung yang dapat diakses untuk stack
    outStackMemory = false; // default: alamat masih dapat diakses
  }
//

  Processor(Endian pEndian) {
//      System.out.println("Register -> "+Register.r8.toString());
    defStartLoc = 0; // start address to feth first instruction
    PC = defStartLoc;
    endianUsed = pEndian;//Endian.littleEndian;
    addressLine = 32; // default
    endExecution = true; // awal: belum mulai eksekusi
    opcode = UNKNOWN; // unknown operation (opcode)
    FLAG = 1; // init flag register, ZF = 1
    BREG = 0; // base address untuk proses
    LREG = 0;
    //set up memory stack
    SP = 32 * 1024 - sizeStack;
    BP = 32 * 1024 - 1; // ujung yang dapat diakses untuk stack
    outStackMemory = false; // default: alamat masih dapat diakses
  }

  public Endian whatEndian() {
    return endianUsed;
  }

  // sample isi memori, code program
  public void hardwired() {
    // default contentMemory (predefined) - sample, untuk TEST
    //  bahan untuk tool <injector.java>
    int lokasi;
    int tData;
    byte[] aNumerik;
    aNumerik = new byte[4];

    lokasi = 0;
    tData = 0x03200064; // movi r2,100(decimal)
    aNumerik = makeLittleEndian(tData);
    RAM.writeWordMemory(lokasi, aNumerik);

    lokasi = 4;
    tData = 0x03100032; // movi r1,50(decimal)
    aNumerik = makeLittleEndian(tData);
    RAM.writeWordMemory(lokasi, aNumerik);

    // operasi penambahan
    lokasi = 8;
    tData = 0x00213000;
    aNumerik = RAM.makeLittleEndian(tData);
    RAM.writeWordMemory(lokasi, aNumerik); // add r3,r2,r1 -> r3 = r1+r2
/*
       tData = 0x275B8A5F;
       aNumerik=RAM.makeLittleEndian(tData);
       lokasi=12;
       RAM.writeWordMemory(lokasi,aNumerik);
       tData = 0x017B5E9A;
       aNumerik=RAM.makeLittleEndian(tData);
      lokasi=12;
       RAM.writeWordMemory(lokasi,aNumerik);*/

    // cek HLT
    lokasi = 12;
    tData = 0x7F000000;
    aNumerik = RAM.makeLittleEndian(tData);
    lokasi = 16;
    RAM.writeWordMemory(lokasi, aNumerik);
    // end: default contentMemory
  }

  // wrapping write a word to memory, berdasarkan endian yang digunakan
  public void writeWordMemory(int pLokasi, int pData) {
    byte[] aNumerik;
    aNumerik = new byte[4];
    if (endianUsed == Endian.littleEndian) {
      aNumerik = makeLittleEndian(pData);
      //         System.out.printf("processor, LOkasi %08x -> %08x\n",pLokasi,pData);
    } // little endian
    else {
      aNumerik = makeBigEndian(pData);
    } // endif, big endian
    RAM.writeWordMemory(pLokasi, aNumerik);
  }

  public void writeByteMemory(int pLokasi, byte pData) {
    RAM.writeByteMemory(pLokasi, pData);
  }

  // menata susunan data untuk persiapan basis pengolahan (littleEndian)
  public void tuningWordMemory(byte[] pData) {
    byte tmpByte;
    if (endianUsed != Endian.littleEndian) {
      // mengubah format big endian ke little endian
      tmpByte = pData[3];
      pData[3] = pData[0];
      pData[0] = tmpByte;
      tmpByte = pData[2];
      pData[2] = pData[1];
      pData[1] = tmpByte;
    } // tuning to little endian
  }

  // asumsi nilai unsigned integer
  public int wordMemoryToInteger32(byte[] pData) {
    byte tmpByte, signF;
    int int32 = 0, d = 0, k;
    signF = (byte) ((FLAG >> 1) & 0x0000000F); // dapatkan sign flag dari FLAG
    if (endianUsed == Endian.littleEndian) {
      // jadikan numerik dari litteEndian
      if (signF == 0) { // positif
        k = ((int) (pData[3] >> 4) & 0x0000000F) * 268435456;
        k = k + ((int) (pData[3] & 0x0F)) * 16777216;
        int32 = int32 + k; // d;
        k = ((int) (pData[2] >> 4) & 0x0000000F) * 1048576;
        k = k + ((int) (pData[2] & 0x0F)) * 65536;
        int32 = int32 + k; //d;
        k = ((int) (pData[1] >> 4) & 0x0000000F) * 4096;
        k = k + ((int) (pData[1] & 0x0F)) * 256;
        int32 = int32 + k;
        k = ((int) (pData[0] >> 4) & 0x0000000F) * 16;
        k = k + (int) (pData[0] & 0x0F);
        int32 = int32 + k;
      } // true
      else { // negatif
        // belum ditangani
      } // endif signF
    } // tuning to little endian
    else {
      // bigEndian
      if (signF == 0) { // positif
        // belum ditangani
      } // true
      else {
      } // endif signF
    }
    return int32;
  }

  // koneksi kememori
  public void connectToMemory(Memory pRAM) {
    RAM = pRAM;
  }
  // end: koneksi ke memori

/*
    // koneksi ke memory management unit
    public void connectToMMU(cacheMMU pMMU)
    {
       MMU=pMMU;
       }
    // end: koneksi ke memori
*/

  // kapasitas pengalamatan oleh prosesor
  public long addressing() {
    return addressLine;
  }

  // Instruction Set code(handler)
//  asumsi: semua array byte  merupakan representasi 
//    littleEndian sebagai basis eksekusi
  // menginterpretasi alamat register yang digunakan
  //   untuk persiapan operasi register, modul ini belum digunakan
  public void registerFile(byte[] p) {
    byte regSrc1, regSrc2, regDest;
    regSrc1 = (byte) ((p[2] >> 4) & 0x0F); // id register source-1
    regSrc2 = (byte) (p[2] & 0x0F); // id register source-2
    regDest = (byte) ((p[1] >> 4) & 0x0F); // id register destination
  }

  // set opcode berasal dari Control Unit (pcode)
  public void newOpCode(byte pcode) {
    opcode = pcode;
  }

  // operasi add register
  //  mungkin perlu dua parameter saja operand
  public void add(byte[] p) {
    System.out.printf("Operasi ADD : %x, PC(%x)\n", opcode, PC);
    byte regDest, regSrc1, regSrc2;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination, hasil operasi add
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1
    regSrc2 = (byte) ((p[1] >> 4) & 0x0F); // id register soure

    //      registerFile(p); // belum bagus
    aRegister[regDest] = aRegister[regSrc1] + aRegister[regSrc2];
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
  }

  // operasi add immediate data ke register
  public void addi(byte[] p) {
    System.out.printf("Operasi ADDI : %x, PC(%x)\n", opcode, PC);
    byte regSrc1, regDest;
    short dImmediate;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1
    dImmediate = (short) (((int) p[1] * 256) + (int) p[0]); // perlu men-set FLAG register (overflow)

    aRegister[regDest] = aRegister[regSrc1] + dImmediate;
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
  }

  // call procedure (address), jump to address (set new PC) and run
  //   save PC as return address to stack memory
  public void call(byte[] p) {
    System.out.printf("Operasi CALL : %x, PC(%x)\n", opcode, PC);
    int lokasi;
    byte regDest;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // alamat procedure
    // save PC
    if (SP < BP) {// ruang masih tersedia
      lokasi = SP;
      writeWordMemory(lokasi, PC); // save PC to stack
      outStackMemory = false; // masih aman
      PC = aRegister[regDest]; // set to address of procedure
    } else {
      lokasi = 32 * 1024 - 1; // lokasi ujung
      outStackMemory = true; // tidak ada tempat lagi di stack
    }
  }

  // DIV: multiply between registers and store into register
  public void div(byte[] p) {
    System.out.printf("Operasi DIV : %x, PC(%x)\n", opcode, PC);
    byte regDest, regSrc1, regSrc2;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination, hasil operasi add
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1
    regSrc2 = (byte) ((p[1] >> 4) & 0x0F); // id register soure

    //      registerFile(p); // belum bagus
    aRegister[regDest] = aRegister[regSrc1] / aRegister[regSrc2];
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
  }

  // operasi divide by immediate data ke register
  public void divi(byte[] p) {
    System.out.printf("Operasi DIVI : %x, PC(%x)\n", opcode, PC);
    byte regSrc1, regDest;
    short dImmediate;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1
    dImmediate = (short) (((int) p[1] * 256) + (int) p[0]); // perlu men-set FLAG register (overflow)

    aRegister[regDest] = aRegister[regSrc1] / dImmediate;
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
  }

  // halt program/system
  public void halt() {
    System.out.printf("Operasi HLT : %x, PC(%x)\n", opcode, PC);
    FLAG = FLAG & 0xFFFFF2FF; // set XF pada FLAG
  }

  // Read input device
  // delay dalam milisecond
  public boolean in(byte[] p) {
    boolean endInput = true;
    System.out.printf("Operasi IN: %x, PC(%x)\n", opcode, PC);
    byte regSrc1, regDest;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination, menyimpan data
    regSrc1 = (byte) (p[2] & 0x0F); // id register source, device input yang dipilih, 0-monitor
    if (regDest != 0) {
      long tStart = 0, tEnd = 0;
      Calendar d = new GregorianCalendar();
      tStart = tEnd = d.getTimeInMillis();
//          while ((tEnd-tStart) < aRegister[regSrc1]) {
      if ((tEnd - tStart) < aRegister[regSrc1]) endInput = true;
      else endInput = false;
      d = new GregorianCalendar();
      tEnd = d.getTimeInMillis();
//          }
    }
    return endInput;
  }

  // Invoke interrupt
  public void intr() {
    System.out.printf("Operasi INTR : %x, PC(%x)\n", opcode, PC);
  }

  // conditional jump on equal to immediate address, 16 bits
  public void je(byte[] p) {
    byte regSrc1, regSrc2;
    int int32, k;
    System.out.printf("Operasi JE: %x, lokasi awal PC(%x)\n", opcode, PC);
    regSrc1 = (byte) ((p[2] >> 4) & 0x0F); // id register destination, hasil operasi add
    regSrc2 = (byte) (p[2] & 0x0F); // i; d register source-1

    if (aRegister[regSrc1] == aRegister[regSrc2]) { // equal
      int32 = 0; // 16bit
      k = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
      k = k + ((int) (p[1] & 0x0F)) * 256;
      int32 = int32 + k;
      k = ((int) (p[0] >> 4) & 0x0000000F) * 16;
      k = k + (int) (p[0] & 0x0F);
      int32 = int32 + k;
      PC = int32 + BREG; // jump to dImmediate
    } // true, equal, maka PC harus diperbaharui
    else {
      System.out.println("Operasi JE, condition fail");
    }

//       System.out.printf("Operasi JE: %x, lokasi baru PC(%x)\n",opcode,PC);
/*     Scanner konsol=new Scanner(System.in);
    int numImages=0;
     numImages=konsol.nextInt();
     konsol.nextLine(); // flush
*/
  }

  // conditional jump on greater than to immediate address, 16 bits
  //  regSrc1 > regSrc2
  public void jgt(byte[] p) {
    byte regSrc1, regSrc2;
    int int32, k;
    System.out.printf("Operasi JGT: %x, lokasi awal PC(%x)\n", opcode, PC);
    regSrc1 = (byte) ((p[2] >> 4) & 0x0F); // id register destination, hasil operasi add
    regSrc2 = (byte) (p[2] & 0x0F); // id register source-1

    if (aRegister[regSrc1] > aRegister[regSrc2]) { // equal
      int32 = 0; // 16bit
      k = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
      k = k + ((int) (p[1] & 0x0F)) * 256;
      int32 = int32 + k;
      k = ((int) (p[0] >> 4) & 0x0000000F) * 16;
      k = k + (int) (p[0] & 0x0F);
      int32 = int32 + k;

      PC = int32 + BREG; // jump to dImmediate
    } // true, equal, maka PC harus diperbaharui
//       System.out.printf("Operasi JGT: %x, lokasi baru PC(%x)\n",opcode,PC);
/*     Scanner konsol=new Scanner(System.in);
    int numImages=0;
     numImages=konsol.nextInt();
     konsol.nextLine(); // flush
*/
  }

  // conditional jump on less than to immediate address, 16 bits
  //  regSrc1 < regSrc2
  public void jlt(byte[] p) {
    byte regSrc1, regSrc2;
    int int32, k;
    System.out.printf("Operasi JLT: %x, lokasi awal PC(%x)\n", opcode, PC);
    regSrc1 = (byte) ((p[2] >> 4) & 0x0F); // id register destination, hasil operasi add
    regSrc2 = (byte) (p[2] & 0x0F); // id register source-1

    if (aRegister[regSrc1] < aRegister[regSrc2]) { // equal
      int32 = 0; // 16bit
      k = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
      k = k + ((int) (p[1] & 0x0F)) * 256;
      int32 = int32 + k;
      k = ((int) (p[0] >> 4) & 0x0000000F) * 16;
      k = k + (int) (p[0] & 0x0F);
      int32 = int32 + k;

      PC = int32 + BREG; // jump to dImmediate
    } // true, equal, maka PC harus diperbaharui
//       System.out.printf("Operasi JLT: %x, lokasi baru PC(%x)\n",opcode,PC);
/*     Scanner konsol=new Scanner(System.in);
    int numImages=0;
     numImages=konsol.nextInt();
     konsol.nextLine(); // flush
*/
  }

  // unconditional jump immediate address, 24 bits
  public void jmp(byte[] p) {
    byte regDest;
    int int32, k;
    System.out.printf("Operasi JMP: %x, lokasi awal PC(%x)\n", opcode, PC);

    int32 = 0;
    k = ((int) (p[2] >> 4) & 0x0000000F) * 1048576;
    k = k + ((int) (p[2] & 0x0F)) * 65536;
    int32 = int32 + k; //d;
    k = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
    k = k + ((int) (p[1] & 0x0F)) * 256;
    int32 = int32 + k;
    k = ((int) (p[0] >> 4) & 0x0000000F) * 16;
    k = k + (int) (p[0] & 0x0F);
    int32 = int32 + k;

    PC = int32 + BREG; // jump to dImmediate
/*       System.out.printf("Operasi JMP: %x, lokasi baru PC(%x)\n",opcode,PC);
     Scanner konsol=new Scanner(System.in);
    int numImages=0;
     numImages=konsol.nextInt();
     konsol.nextLine(); // flush
*/
  }

  // unconditional jump register
  public void jmpr(byte[] p) {
    byte regDest;

    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination
    PC = aRegister[regDest] + BREG; // jump to pointed register
    System.out.printf("Operasi JMPR: %x, lokasi baru PC(%x)\n", opcode, PC);
  }

  // conditional jump on notequal to immediate address, 16 bits
  public void jne(byte[] p) {
    byte regSrc1, regSrc2;
    int int32, k;
    System.out.printf("Operasi JNE: %x, lokasi awal PC(%x)\n", opcode, PC);
    regSrc1 = (byte) ((p[2] >> 4) & 0x0F); // id register destination, hasil operasi add
    regSrc2 = (byte) (p[2] & 0x0F); // id register source-1

    if (aRegister[regSrc1] != aRegister[regSrc2]) { // equal
      int32 = 0; // 16bit
      k = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
      k = k + ((int) (p[1] & 0x0F)) * 256;
      int32 = int32 + k;
      k = ((int) (p[0] >> 4) & 0x0000000F) * 16;
      k = k + (int) (p[0] & 0x0F);
      int32 = int32 + k;

      PC = int32 + BREG; // jump to dImmediate
    } // true, equal, maka PC harus diperbaharui
    //      System.out.printf("Operasi JNE: %x, lokasi baru PC(%x)\n",opcode,PC);
/*     Scanner konsol=new Scanner(System.in);
    int numImages=0;
     numImages=konsol.nextInt();
     konsol.nextLine(); // flush
*/
  }

  // verifikasi simpan data ke memori melalui instruksi MOV
  //  1 word data dari register ke memori
  public void mov(byte[] p) {
    byte regDest, regSrc1;
    int dImmediate, dp = 0;
    int lokasi = 0;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination alamat memori
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1

    // setting alamat immediate
    // immediate data p[1] dan p[0], sebagai unsigned integer(byte/ short)
    dp = ((int) (p[0] >> 4) & 0x0000000F) * 16;
    dp = dp + (int) (p[0] & 0x0F);
    dImmediate = dp;
    dp = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
    dp = dp + ((int) (p[1] & 0x0F)) * 256;
    dImmediate = dImmediate + dp;
    lokasi = (int) aRegister[regDest] + dImmediate + BREG;

    writeWordMemory(lokasi, aRegister[regSrc1]);
    // belum ditambahkan pembaharuan FLAG register
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }

    System.out.printf("Operasi MOV register ke lokasi memori: %08x\n", lokasi);
  }

  // move byte 8 bits dari register ke memori
  public void movb(byte[] p) {
    byte regDest, regSrc1;
    int dImmediate, dp = 0;
    int lokasi = 0;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination alamat memori
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1

    // setting alamat immediate
    // immediate data p[1] dan p[0], sebagai unsigned integer(byte/ short)
    dp = ((int) (p[0] >> 4) & 0x0000000F) * 16;
    dp = dp + (int) (p[0] & 0x0F);
    dImmediate = dp;
    dp = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
    dp = dp + ((int) (p[1] & 0x0F)) * 256;
    dImmediate = dImmediate + dp;
    lokasi = (int) aRegister[regDest] + dImmediate + BREG;

    byte bData;
    bData = (byte) (aRegister[regSrc1] & 0x000000FF); // hanya 1 byte
    writeByteMemory(lokasi, bData);
    // belum ditambahkan pembaharuan FLAG register
    if (bData == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }

    System.out.printf("Operasi MOVB : %x, PC(%x)\n", opcode, PC);
  }

  // move double  word 64 bits dari register ke memori
  public void movd() {
    System.out.printf("Operasi MOVD : %x, PC(%x)\n", opcode, PC);

  }

  // move half word high 16 bits dari register ke memori R:[16-31]
  public void movh(byte[] p) {
    System.out.printf("Operasi MOVH : %x, PC(%x)\n", opcode, PC);
    byte regDest, regSrc1;
    int dImmediate, dp = 0;
    int lokasi = 0;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination alamat memori
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1

    // setting alamat immediate
    // immediate data p[1] dan p[0], sebagai unsigned integer(byte/ short)
    dp = ((int) (p[0] >> 4) & 0x0000000F) * 16;
    dp = dp + (int) (p[0] & 0x0F);
    dImmediate = dp;
    dp = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
    dp = dp + ((int) (p[1] & 0x0F)) * 256;
    dImmediate = dImmediate + dp;
    lokasi = (int) aRegister[regDest] + dImmediate + BREG;

//       writeWordMemory(lokasi,aRegister[regSrc1]);
    // write 8 bit low
    byte d8bits;
    d8bits = (byte) ((aRegister[regSrc1] & 0x00FF0000) >> 16);
    writeByteMemory(lokasi, d8bits); // write byte reg:16-23
    // write 8 bit high
    d8bits = (byte) (aRegister[regSrc1] >> 24);
    writeByteMemory(lokasi + 1, d8bits); // write byte reg:24-31
    // belum ditambahkan pembaharuan FLAG register
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }

//       System.out.printf("Operasi MOVH register ke lokasi memori: %08x\n",lokasi);       
  }

  // move immediate data 20 bits ke register
  public void movi(byte[] p) {
    System.out.printf("Operasi MOVI : %x, PC(%x), [0]%02x, [1]%02x, [2]%02x, [3]%02x\n", opcode, PC, p[0], p[1], p[2], p[3]);
    byte whichRegister;
    int vImmediate, dp = 0;
    whichRegister = (byte) ((p[2] >> 4) & 0x0F); // dapatkan register tujuan

    dp = ((int) (p[0] >> 4) & 0x0000000F) * 16;
    dp = dp + (int) (p[0] & 0x0F);
    vImmediate = dp;
    dp = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
    dp = dp + (int) (p[1] & 0x0F) * 256;
    vImmediate = vImmediate + dp;
    dp = (int) (p[2] & 0x0F) * 65536;
    vImmediate = vImmediate + dp;
    aRegister[whichRegister] = vImmediate;

    if (aRegister[whichRegister] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
  }

  // move half word low 16 bits dari register ke memori R:[0-16]
  public void movl(byte[] p) {
    System.out.printf("Operasi MOVL : %x, PC(%x)\n", opcode, PC);
    byte regDest, regSrc1;
    int dImmediate, dp = 0;
    int lokasi = 0;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination alamat memori
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1

    // setting alamat immediate
    // immediate data p[1] dan p[0], sebagai unsigned integer(byte/ short)
    dp = ((int) (p[0] >> 4) & 0x0000000F) * 16;
    dp = dp + (int) (p[0] & 0x0F);
    dImmediate = dp;
    dp = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
    dp = dp + ((int) (p[1] & 0x0F)) * 256;
    dImmediate = dImmediate + dp;
    lokasi = (int) aRegister[regDest] + dImmediate + BREG;

//       writeWordMemory(lokasi,aRegister[regSrc1]);
    // write 8 bit low
    byte d8bits;
    d8bits = (byte) (aRegister[regSrc1] & 0x000000FF);
    writeByteMemory(lokasi, d8bits); // write byte reg:16-23
    // write 8 bit high
    d8bits = (byte) ((aRegister[regSrc1] & 0x0000FF00) >> 8);
    writeByteMemory(lokasi + 1, d8bits); // write byte reg:24-31
    // belum ditambahkan pembaharuan FLAG register
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }

//       System.out.printf("Operasi MOVL register ke lokasi memori: %08x\n",lokasi);       
  }

  // move word (32 bits) dari memori ke register
  public void movm(byte[] p) {
    byte regSrc1, regDest;
    int dImmediate, dp = 0;
    int lokasi = 0;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination penerima
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1 sebagai alamat

    // setting alamat immediate
    // immediate data p[1] dan p[0], sebagai unsigned integer(byte/ short)
    dp = ((int) (p[0] >> 4) & 0x0000000F) * 16;
    dp = dp + (int) (p[0] & 0x0F);
    dImmediate = dp;
    dp = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
    dp = dp + ((int) (p[1] & 0x0F)) * 256;
    dImmediate = dImmediate + dp;
    System.out.printf("MOVM, lokasi memori -> %08x\n", dImmediate);
    lokasi = (int) aRegister[regSrc1] + dImmediate + BREG;

    byte[] dMem;
    dMem = new byte[4];
    dMem = RAM.readWordMemory(lokasi); //
    aRegister[regDest] = wordMemoryToInteger32(dMem); // mengubah menjadi integer untuk disimpan ke register
    // belum ditambahkan pembaharuan FLAG register
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
//       System.out.printf("Operasi MOVM a[regDest] : %x\n",aRegister[regDest]);
  }

  // move byte 8 bits dari memori ke register
  public void movmb(byte[] p) {
    byte regSrc1, regDest;
    int dImmediate, dp = 0;
    int lokasi = 0;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination penerima
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1 sebagai alamat

    // setting alamat immediate
    // immediate data p[1] dan p[0], sebagai unsigned integer(byte/ short)
    dp = ((int) (p[0] >> 4) & 0x0000000F) * 16;
    dp = dp + (int) (p[0] & 0x0F);
    dImmediate = dp;
    dp = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
    dp = dp + ((int) (p[1] & 0x0F)) * 256;
    dImmediate = dImmediate + dp;
    System.out.printf("MOVMB, lokasi memori -> %08x\n", dImmediate);
    lokasi = aRegister[regSrc1] + dImmediate + BREG;

    byte dMem;
    dMem = RAM.readByteMemory(lokasi); // belum selesai
//      System.out.printf("Operasi MOVMB regDst: %x\n",regDest);
    aRegister[regDest] = ((int) dMem) & 0x000000FF; // mengubah menjadi integer untuk disimpan ke register
    // belum ditambahkan pembaharuan FLAG register
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }

  }

  // move double  word 32 bits dari memori ke register
  public void movmd() {
    System.out.printf("Operasi MOVMD : %x, PC(%x)\n", opcode, PC);

  }

  // move half word high 16 bits dari memori data 32 bits ke register
  public void movmh(byte[] p) {
    System.out.printf("Operasi MOVMH : %x, PC(%x)\n", opcode, PC);
    byte regSrc1, regDest;
    int dImmediate, dp = 0;
    int lokasi = 0;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination penerima
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1 sebagai alamat

    // setting alamat immediate
    // immediate data p[1] dan p[0], sebagai unsigned integer(byte/ short)
    dp = ((int) (p[0] >> 4) & 0x0000000F) * 16;
    dp = dp + (int) (p[0] & 0x0F);
    dImmediate = dp;
    dp = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
    dp = dp + ((int) (p[1] & 0x0F)) * 256;
    dImmediate = dImmediate + dp;
//       System.out.printf("MOVML, lokasi memori -> %08x\n",dImmediate);
    lokasi = (int) aRegister[regSrc1] + dImmediate + BREG;

    byte[] dMem;
    dMem = new byte[4];
    dMem = RAM.readWordMemory(lokasi); //
    dMem[0] = dMem[2]; // get low
    dMem[1] = dMem[3]; // get low
    dMem[2] = 0x00; // high
    dMem[3] = 0x00; // high
    aRegister[regDest] = wordMemoryToInteger32(dMem); // mengubah menjadi integer untuk disimpan ke register
    // belum ditambahkan pembaharuan FLAG register
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
//       System.out.printf("Operasi MOVMH a[regDest] : %x\n",aRegister[regDest]);
  }

  ////
  // move numeric data 16 bits dari memori ke register
  public void movmhw(byte[] p) {
    System.out.printf("Operasi MOVMHW : %x, PC(%x)\n", opcode, PC);
    byte regSrc1, regDest;
    int dImmediate, dp = 0;
    int lokasi = 0;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination penerima
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1 sebagai alamat

    // setting alamat immediate
    // immediate data p[1] dan p[0], sebagai unsigned integer(byte/ short)
    dp = ((int) (p[0] >> 4) & 0x0000000F) * 16;
    dp = dp + (int) (p[0] & 0x0F);
    dImmediate = dp;
    dp = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
    dp = dp + ((int) (p[1] & 0x0F)) * 256;
    dImmediate = dImmediate + dp;
//       System.out.printf("MOVML, lokasi memori -> %08x\n",dImmediate);
    lokasi = (int) aRegister[regSrc1] + dImmediate + BREG;

    byte[] dMem;
//       byte[] dMem16;
    dMem = new byte[4];
//       dMem16 = new byte[2];
    dMem = RAM.readWordMemory(lokasi); //
    //      dMem16RAM.readByteMemory(lokasi);
    dMem[2] = 0x00; // set zero
    dMem[3] = 0x00; // set zero
    aRegister[regDest] = wordMemoryToInteger32(dMem); // mengubah menjadi integer untuk disimpan ke register
    // belum ditambahkan pembaharuan FLAG register
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
    //      System.out.printf("Operasi MOVMHW a[regDest] : %x\n",aRegister[regDest]);
  }

  // move half word low 16 bits dari memori data 32 bits ke register
  public void movml(byte[] p) {
    System.out.printf("Operasi MOVML : %x, PC(%x)\n", opcode, PC);
    byte regSrc1, regDest;
    int dImmediate, dp = 0;
    int lokasi = 0;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination penerima
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1 sebagai alamat

    // setting alamat immediate
    // immediate data p[1] dan p[0], sebagai unsigned integer(byte/ short)
    dp = ((int) (p[0] >> 4) & 0x0000000F) * 16;
    dp = dp + (int) (p[0] & 0x0F);
    dImmediate = dp;
    dp = ((int) (p[1] >> 4) & 0x0000000F) * 4096;
    dp = dp + ((int) (p[1] & 0x0F)) * 256;
    dImmediate = dImmediate + dp;
//       System.out.printf("MOVML, lokasi memori -> %08x\n",dImmediate);
    lokasi = (int) aRegister[regSrc1] + dImmediate + BREG;

    byte[] dMem;
    dMem = new byte[4];
    dMem = RAM.readWordMemory(lokasi); //
    dMem[3] = 0x00; // high
    dMem[2] = 0x00; // high
    aRegister[regDest] = wordMemoryToInteger32(dMem); // mengubah menjadi integer untuk disimpan ke register
    // belum ditambahkan pembaharuan FLAG register
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
//       System.out.printf("Operasi MOVML a[regDest] : %x\n",aRegister[regDest]);
  }

  // move register ke register, 32 bits
  public void movr(byte[] p) {
    System.out.printf("Operasi MOVR : %x, PC(%x)\n", opcode, PC);
    byte regDest, regSrc1;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register source-1
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-2
    aRegister[regDest] = aRegister[regSrc1]; //menyalin isi regSrc-1 ke regSrc-2
    // belum ditambahkan pembaharua FLAG register
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
  }

  // MUL: multiply between registers and store into register
  public void mul(byte[] p) {
    System.out.printf("Operasi MUL : %x, PC(%x)\n", opcode, PC);
    byte regDest, regSrc1, regSrc2;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination, hasil operasi add
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1
    regSrc2 = (byte) ((p[1] >> 4) & 0x0F); // id register soure

    //      registerFile(p); // belum bagus
    aRegister[regDest] = aRegister[regSrc1] * aRegister[regSrc2];
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
  }

  // MULI: multiply between registers and store into register
  public void muli(byte[] p) {
    System.out.printf("Operasi MULI : %x, PC(%x)\n", opcode, PC);
    byte regSrc1, regDest;
    short dImmediate;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1
    dImmediate = (short) (((int) p[1] * 256) + (int) p[0]); // perlu men-set FLAG register (overflow)

    aRegister[regDest] = aRegister[regSrc1] * dImmediate;
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
  }

  // Write output device
  // delay dalam milisecond
  public boolean out(byte[] p) {
    boolean endInput = true;
    System.out.printf("Operasi OUT: %x, PC(%x)\n", opcode, PC);
    byte regSrc1, regDest;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination, menyimpan data
    regSrc1 = (byte) (p[2] & 0x0F); // id register source, device input yang dipilih, 0-monitor
    if (regDest != 1) {
      long tStart = 0, tEnd = 0;
      Calendar d = new GregorianCalendar();
      tStart = tEnd = d.getTimeInMillis();
//          while ((tEnd-tStart) < aRegister[regSrc1]) {
      if ((tEnd - tStart) < aRegister[regSrc1]) endInput = true;
      else endInput = false;
      d = new GregorianCalendar();
      tEnd = d.getTimeInMillis();
//         }
    }
    return endInput;
  }

  // mengeluarkan data pada TOS dan simpan ke register
  public void pop(byte[] p) {
    System.out.printf("Operasi POP : %x, PC(%x)\n", opcode, PC);
    byte regDest;
    int dp = 0;
    int lokasi = 0;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination penerima

    System.out.printf("POP, lokasi top of stack memori -> %08x\n", SP);
//       lokasi = (int)aRegister[regSrc1]+dImmediate+BREG;
    lokasi = SP + BREG;


    byte[] dMem;
    dMem = new byte[4];
    dMem = RAM.readWordMemory(lokasi); //
    aRegister[regDest] = wordMemoryToInteger32(dMem); // mengubah menjadi integer untuk disimpan ke register
    SP = SP - 4; // Top of stack bergeser
    if (SP < (32 * 1024 - sizeStack)) { // cek apakah masih dalam rentang memory stack
      SP = 32 * 1024 - sizeStack;
    }
    // belum ditambahkan pembaharuan FLAG register
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
  }

  // masukan register ke stack
  public void push(byte[] p) {
    System.out.printf("Operasi PUSH : %x, PC(%x)\n", opcode, PC);

    byte regDest, regSrc1;
    int dImmediate, dp = 0;
    int lokasi = 0;
    regSrc1 = (byte) ((p[2] >> 4) & 0x0F); // gunakan SP, id register destination alamat memori

    if (SP < BP) {// ruang masih tersedia
      lokasi = SP;
      writeWordMemory(lokasi, aRegister[regSrc1]);
      outStackMemory = false; // masih aman
    } else {
      lokasi = 32 * 1024 - 1; // lokasi ujung
      outStackMemory = true; // tidak ada tempat lagi di stack
    }

  }

  // masukan flag register (FLAG) ke stack
  public void pushf() {
    System.out.printf("Operasi PUSHF : %x, PC(%x)\n", opcode, PC);
  }


  // keluarkan data dari stack simpan ke flag register (FLAG)
  public void popf() {
    System.out.printf("Operasi POPF : %x, PC(%x)\n", opcode, PC);
  }

  // return from procedure
  //   jump to return address from stack memory, by popped up
  public void ret(byte[] p) {
    System.out.printf("Operasi RET : %x, PC(%x)\n", opcode, PC);
    int lokasi;
    lokasi = SP + BREG;


    byte[] dMem;
    dMem = new byte[4];
    dMem = RAM.readWordMemory(lokasi); //
    PC = wordMemoryToInteger32(dMem); // mengubah menjadi integer untuk disimpan ke register
    SP = SP - 4; // Top of stack bergeser
    if (SP < (32 * 1024 - sizeStack)) { // cek apakah masih dalam rentang memory stack
      SP = 32 * 1024 - sizeStack;
    }
  }

  // shift left
  public void sfleft(byte[] p) {
    System.out.printf("Operasi SFLEFT : %x, PC(%x)\n", opcode, PC);
    byte regDest, regSrc1, numShift;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination, hasil operasi add
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1
    numShift = (byte) (p[0] & 0x1F); // number of shifting bit ti left
    aRegister[regDest] = aRegister[regSrc1] << numShift;

  }

  // shift right
  public void sfright(byte[] p) {
    System.out.printf("Operasi SFRIGHT : %x, PC(%x)\n", opcode, PC);
    byte regDest, regSrc1, numShift;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination, hasil operasi add
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1
    numShift = (byte) (p[0] & 0x1F); // number of shifting bit to right
    aRegister[regDest] = aRegister[regSrc1] >> numShift;
  }

  ///
  // operasi subtract register
  //  mungkin perlu dua parameter saja operand
  public void sub(byte[] p) {
    System.out.printf("Operasi SUB : %x, PC(%x)\n", opcode, PC);
    byte regDest, regSrc1, regSrc2;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination, hasil operasi add
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1
    regSrc2 = (byte) ((p[1] >> 4) & 0x0F); // id register soure

    //      registerFile(p); // belum bagus
    aRegister[regDest] = aRegister[regSrc1] - aRegister[regSrc2];
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
  }

  // operasi subtract immediate data ke register
  public void subi(byte[] p) {
    System.out.printf("Operasi SUBI : %x, PC(%x)\n", opcode, PC);
    byte regSrc1, regDest;
    short dImmediate;
    regDest = (byte) ((p[2] >> 4) & 0x0F); // id register destination
    regSrc1 = (byte) (p[2] & 0x0F); // id register source-1
    dImmediate = (short) (((int) p[1] * 256) + (int) p[0]); // perlu men-set FLAG register (overflow)
    aRegister[regDest] = aRegister[regSrc1] - dImmediate;
    if (aRegister[regDest] == 0) { // zero flag
      FLAG = FLAG | 0x1; // ZF = 1
    }
  }

  // unknown/invalid operation(code) program/system
  public void unknown() {
    System.out.printf("Operasi UNKNOWN : %x, PC(%x)\n", opcode, PC);
  }

// END: Instruction set code (handler)

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
    perbyte = (num32 >> 24); // & 0x000000FF;
    aLittle[3] = (byte) perbyte;
    return aLittle;
  }

  // untuk keperluan verifikasi
  // display isi memory
  public void memoryContent() {
    RAM.dspDataInMemory(0, 16);
  }

  // display isi register
  public void registerContent() {
    int i, k;

    System.out.println("\nDisplay isi register ");
    for (k = 0; k < 4; k++) { // perpage display : 8 baris
      System.out.printf("r%d = %08x ", k, aRegister[k]);
    } // endfor 6 kolom
    System.out.printf("\n");

    for (k = 4; k < 8; k++) { // perpage display : 8 baris
      System.out.printf("r%d = %08x ", k, aRegister[k]);
    } // endfor 6 kolom
    System.out.printf("\n");

    for (k = 8; k < 12; k++) { // perpage display : 8 baris
      System.out.printf("r%d = %08x ", k, aRegister[k]);
    } // endfor 6 kolom
    System.out.printf("\n");

    for (k = 12; k < 16; k++) { // perpage display : 8 baris
      System.out.printf("r%d = %08x ", k, aRegister[k]);
    } // endfor 6 kolom
    System.out.println("");
    System.out.printf("PC = %08x SP = %08x BP = %08x\n", PC, SP, BP);
//         System.out.printf("\n\n");
  }
}
