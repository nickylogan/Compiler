package computer;// simulasi akses komputer

import java.io.*;
import java.util.Scanner;

public class cekcpu implements OperationCode {
//  public static void main(String[] args) throws IOException {
//    Scanner konsol = new Scanner(System.in);
//    Computer comp = new Computer();
//    byte curExecution = 0;
//    int lokasi = 0; // start address program
//    int numProcesses = 0;
//    int[] startMemory; // bagian dari process table
//    String[] submittedFile;
//    String fn;
//    long tStart = 0, tEnd = 0;
//    //      String fn ="sample03.mcd";
//
//    comp.powerOn();
//    startMemory = new int[5];
//    submittedFile = new String[5];
//    comp.monitorStatus(); // display status memori dan register
//
//// komputer siap menerima perintah eksekusi
//    System.out.print("Masukan program yang dieksekusi -> ");
//    fn = konsol.nextLine();
////       comp.codeInjector(fn); // aktifkan modul tool injeksi code
//    startMemory[0] = lokasi;
//    comp.codeInjector(fn, lokasi); // aktifkan modul tool injeksi code, dengan alamat ditetapkan melalui parameter
//    submittedFile[numProcesses] = fn;
//    ++numProcesses;
//    System.out.printf("  program ==> %s, penempatan di lokasi memori [%08xH]\n", fn, startMemory[0]);
//    comp.monitorStatus(); // display status memori dan register
//
///* program ke-2
//// tambahan
//       System.out.print("Masukan program yang dieksekusi -> ");
//       fn=konsol.nextLine();
//       lokasi=80; // 0x50
//       startMemory[1]=lokasi; //
//       comp.codeInjector(fn,lokasi); // aktifkan modul tool injeksi code, dengan alamat ditetapkan melalui parameter
//       ++numProcesses;
//       lokasi=0; // kembalikan ke awal
//// end: tambahan
//
// //      comp.runProgram(); // default alamat awal eksekusi adalah 0
//      System.out.printf("  program ==> %s, penempatan di lokasi memori [%08xH]\n",fn,startMemory[1]);
//      comp.monitorStatus(); // display status memori dan register
//*/
//
//    System.out.print("Ketikkan sembarang angka, eksekusi program akan dilaksanakan -> ");
//    fn = konsol.nextLine();
//
///*
//      Calendar d = new GregorianCalendar();
//      for (int k=0;k<numProcesses;k++) {
//          d = new GregorianCalendar();
//          tStart=tEnd=d.getTimeInMillis();
//          comp.cpu.BREG=startMemory[k]; // base register
//          comp.runProgram(startMemory[k]); // user dapat menetapkan alamat awal program yang dieksekusi
//          d = new GregorianCalendar();
//          tEnd= d.getTimeInMillis();
////          System.out.print("Program ["+(k+1)+"] telah seleai dieksekusi, start["+tStart+"], end["+tEnd+"] ");
//          System.out.print("Program ["+(k+1)+"] telah seleai dieksekusi, time = "+(tEnd-tStart)+" ");
//          fn=konsol.nextLine();
//      } // endfor managing processes
//*/
//
//    // Eksekusi instruksi bertahap
////
//    int numImages = 0;
//    // loop eksekusi instruksi demi instruksi
//    for (int k = 0; k < numProcesses; k++) {
//      lokasi = startMemory[k];
//      comp.cpu.BREG = startMemory[k]; // base register
//      while ((curExecution = comp.runInstruction(lokasi)) != HALT) {
//        System.out.println("Eksekusi opcode ->  " + curExecution);
////         System.out.print("Ketikkan sembarang angka dan tekan <Enter>, untuk melanjutkan -> ");
////         numImages=konsol.nextInt();
////         konsol.nextLine(); // flush
//      } // endwhile
//      comp.monitorStatus(); // display status memori dan register
//      System.out.print("Eksekusi program [" + (k + 1) + ": " + submittedFile[k] + "] selesai, ketikkan sembarang angka ==> ");
//      fn = konsol.nextLine();
//    } // endfor
////
//    System.out.printf("Kapasitas memori -> %d\n", comp.cpu.RAM.memorySize());
//    comp.powerOff();
//
//  } // main
}
