//package computer;// Modifikasi class Computer
//// Pendefinisian cpu dan mainMemory di class ini, bukan pada class ControlUnit
//
//import java.io.*;
//
//public class ComputerCopy implements OperationCode {
//  private boolean powerUp; // status awal false (down-off)
//  private boolean computerHealth; // default: OK
//  private byte endExecution; // opcode
//
//  //    Processor cpu = new Processor(Endian.littleEndian);
//  Processor_modif cpu = new Processor_modif();
//  Memory mainMemory = new Memory();
//  public ControlUnit cntrl_unit;
//  public int r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, PC;
//
//
//  ComputerCopy() {
//    powerUp = false; // Power-off
//    computerHealth = true; // OK, sehat
//  }
//
//  // Power suply diaktifkan
//  public boolean powerOn() {
//    computerHealth = true;
//    if (!powerUp) {
//      cpu.connectToMemory(mainMemory); // koneksikan cpu ke main memory
//      cntrl_unit = new ControlUnit(cpu, mainMemory); // kendali ada pada ControlUnit
////          cntrl_unit.cpu.hardwired(); // sample isi memori
////          codeInjector(); // sample code, sama dengan cpu.hardwired()
//      powerUp = true; // sekarang ON
//      System.out.println("Pengaktifan power komputer berhasil dilakukan");
//    } else {
//      System.out.println("Power ON idak dapat dilayani, komputer sudah ON");
//    }
//
//    return powerUp;
//  }
//
//  //power supply dinonaktifkan
//  public boolean powerOff() {
//    computerHealth = true;
//    if (powerUp) {
//      powerUp = false; // Power off
//      System.out.println("Power komputer telah OFF");
//    } else
//      System.out.println("Tidak bisa dilayani, komputer power sudah OFF");
//    return true; // Power sukses OFF
//  }
//
//  // dapatkan informasi nilai Program Counter (PC)
//  public int PC() {
//    return cpu.PC;
//  }
//
//  // menjalankan program hardwired (sample)
//  public boolean runProgram() {
//    cntrl_unit.setExecutionAddress(0);
//    boolean okLanjut = false;
//    if (powerUp) {
//      okLanjut = true; // power ON
//      // Verifikasi little endian
//      cntrl_unit.execute(); // tunda dulu
//      cntrl_unit.memoryContent();
//      cntrl_unit.registerContent(); // isi register
//      updateRegisters(); // untuk diakses level di atasnya
//      //         endExecution = cntrl_unit.executeInstruction();
//    } // true komputer ON
//    else {
//      okLanjut = false; // Power Off
//      System.out.println("Power komputer (OFF) belum diaktifkan, runProgram gagal");
//    } // endif, komputer OFF
//    return okLanjut;
//  }
//
//  // function overloading
//  public boolean runProgram(int startAddress) {
//    boolean okLanjut = false;
//    if ((startAddress % 4) == 0) {
//      cntrl_unit.setExecutionAddress(startAddress);
//      if (powerUp) {
//        okLanjut = true; // power ON
//        // Verifikasi little endian
//        cntrl_unit.execute(); // tunda dulu
//        cntrl_unit.memoryContent();
//        cntrl_unit.registerContent(); // isi register
//        updateRegisters(); // untuk diakses level di atasnya
//        //         endExecution = cntrl_unit.executeInstruction();
//      } // true komputer ON
//      else {
//        okLanjut = false; // Power Off
//        System.out.println("Power komputer (OFF) belum diaktifkan, runProgram gagal");
//      } // endif, komputer OFF
//    } // true: alamat yang valid
//    else {
//      System.out.println("ERROR, penetapan alamat yang tidak semestinya, mesti kelipatan 4");
//      okLanjut = false;
//    }
//    return okLanjut;
//  }
//
//  public byte runInstruction() {
//    if (!cntrl_unit.statusRun()) {
//      cntrl_unit.setExecutionAddress(0);
//      cntrl_unit.makeRun();
//    }
//    if (powerUp) {
//      endExecution = cntrl_unit.executeInstruction();
//      cntrl_unit.memoryContent();
//      cntrl_unit.registerContent(); // isi register
//      updateRegisters(); // untuk diakses level di atasnya
//    } // true komputer ON
//    else {
//      endExecution = HALT;
//      System.out.println("Power komputer (OFF) belum diaktifkan, runProgram gagal");
//    } // endif, komputer OFF
//    return endExecution;
//  }
//
//  // function overloading
//  public byte runInstruction(int startAddress) {
//    if (!cntrl_unit.statusRun()) {
//      cntrl_unit.setExecutionAddress(startAddress);
//      cntrl_unit.makeRun();
//    }
//    if (powerUp) {
//      endExecution = cntrl_unit.executeInstruction();
//      cntrl_unit.memoryContent();
//      cntrl_unit.registerContent(); // isi register
//      updateRegisters(); // untuk diakses level di atasnya
//    } // true komputer ON
//    else {
//      endExecution = HALT;
//      System.out.println("Power komputer (OFF) belum diaktifkan, runProgram gagal");
//    } // endif, komputer OFF
//    return endExecution;
//  }
//
//  // tools untuk Injeksi code ke komputer (memory)
//  //  memungkinkan user memasukkan machine (binary) code ke komputer (memori)
//  public void codeInjector() {
//    // Injector untuk mengisic machine code ke memori
//    System.out.println("Test Injector code");
//
//    int lokasi;
//    int tData;
//    byte[] aNumerik;
//    aNumerik = new byte[4];
//
//    lokasi = 0;
//    tData = 0x03200064; // movi r2,100(decimal)
////       aNumerik=cpu.makeLittleEndian(tData);
////    mainMemory.writeWordMemory(), ok, akses langsung ke memori, bisa untuk DMA
////      tetapi pastikan konsisten format endian: littleEndian/ bigEndian
////      mainMemory.writeWordMemory(lokasi,aNumerik);
//    cpu.writeWordMemory(lokasi, tData); // akses melalui cpu
//
//    lokasi = 4;
//    tData = 0x03100032; // movi r1,50(decimal)
////       aNumerik=cpu.makeLittleEndian(tData);
////    mainMemory.writeWordMemory(), ok, akses langsung ke memori, bisa untuk DMA
////      tetapi pastikan konsisten format endian: littleEndian/ bigEndian
//    //      mainMemory.writeWordMemory(lokasi,aNumerik);
//    cpu.writeWordMemory(lokasi, tData); // akses melalui cpu
//
//    // operasi penambahan
//    lokasi = 8;
//    tData = 0x00213000;
////       aNumerik=cpu.makeLittleEndian(tData);
////    mainMemory.writeWordMemory(), ok, akses langsung ke memori, bisa untuk DMA
////      tetapi pastikan konsisten format endian: littleEndian/ bigEndian
////       mainMemory.writeWordMemory(lokasi,aNumerik); // add r3,r2,r1 -> r3 = r1+r2
//    cpu.writeWordMemory(lokasi, tData); // akses melalui cpu
//
//    // cek HLT
//    lokasi = 12;
//    tData = 0x7F000000;
////       aNumerik=cpu.makeLittleEndian(tData);
//    cpu.writeWordMemory(lokasi, tData); // akses melalui cpu
//    lokasi = 16;
////    mainMemory.writeWordMemory(), ok, akses langsung ke memori, bisa untuk DMA
////      tetapi pastikan konsisten format endian: littleEndian/ bigEndian
////       mainMemory.writeWordMemory(lokasi,aNumerik);
//    cpu.writeWordMemory(lokasi, tData); // akses melalui cpu
//  }
//
//  // injector dengan asupan file dari pemakai (user)
//  // fMachineCode: file teks dalam bentuk kode bahasa mesin (*.mcd)
//  public void codeInjector(String fMachineCode) throws IOException {
//    //        System.out.println("Inserting/injecting code dari file: "+fMachineCode);
//    BufferedReader in = new BufferedReader(new FileReader(fMachineCode));
//    String s;
//    int lokasi;
//    int tData;
//    lokasi = 0;
//    while ((s = in.readLine()) != null) {
//      tData = Integer.parseInt(s);
//      cpu.writeWordMemory(lokasi, tData); // akses melalui cpu
//      lokasi = lokasi + 4;
//    } // endwhile readLine
//    //         System.out.println("after read codeInjector");
//    in.close();
//  }
//
//  // injector dengan asupan file dari pemakai (user), dan penempatan di memori
//  // fMachineCode: file teks dalam bentuk kode bahasa mesin (*.mcd)
//  public void codeInjector(String fMachineCode, int startLocation) throws IOException {
//    //        System.out.println("Inserting/injecting code dari file: "+fMachineCode);
//    BufferedReader in = new BufferedReader(new FileReader(fMachineCode));
//    String s;
//    int lokasi;
//    int tData;
//    lokasi = startLocation; // alamat penempatan ditetapkan oleh lapisan di atasnya
//    while ((s = in.readLine()) != null) {
//      tData = Integer.parseInt(s);
//      cpu.writeWordMemory(lokasi, tData); // akses melalui cpu
//      lokasi = lokasi + 4;
//    } // endwhile readLine
//    //         System.out.println("after read codeInjector");
//    in.close();
//  }
//
//  // siapkan register untuk diakses oleh level
//  //  yang lebih atas
//  public void updateRegisters() {
//    r0 = cpu.aRegister[0];
//    r1 = cpu.aRegister[1];
//    r2 = cpu.aRegister[2];
//    r3 = cpu.aRegister[3];
//    r4 = cpu.aRegister[4];
//    r5 = cpu.aRegister[5];
//    r6 = cpu.aRegister[6];
//    r7 = cpu.aRegister[7];
//    r8 = cpu.aRegister[8];
//    r9 = cpu.aRegister[9];
//    r10 = cpu.aRegister[10];
//    r11 = cpu.aRegister[11];
//    r12 = cpu.aRegister[12];
//    r13 = cpu.aRegister[13];
//    r14 = cpu.aRegister[14];
//    r15 = cpu.aRegister[15];
//    PC = cpu.PC;
//    // tambahkan register lainnya
//  }
//}
