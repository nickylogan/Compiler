//package computer;// simulasi akses komputer
//import java.io.*;
//import java.util.Scanner;
//public class cekcpu_asal implements OperationCode {
//    public static void main(String []args) throws IOException {
//       Scanner konsol=new Scanner(System.in);
//       ComputerCopy comp = new ComputerCopy();
//       byte curExecution=0;
//       int lokasi=0; // start address program
//       String fn;
// //      String fn ="sample03.mcd";
//       comp.powerOn();
//       System.out.print("Masukan program yang dieksekusi -> ");
//       fn=konsol.nextLine();
////       comp.codeInjector(fn); // aktifkan modul tool injeksi code
//       comp.codeInjector(fn,lokasi); // aktifkan modul tool injeksi code, dengan alamat ditetapkan melalui parameter
//
//// tambahan
///*       System.out.print("Masukan program yang dieksekusi -> ");
//       fn=konsol.nextLine();
//       lokasi=80;
//       comp.codeInjector(fn,lokasi); // aktifkan modul tool injeksi code, dengan alamat ditetapkan melalui parameter
//       lokasi=0; // kembalikan ke awal*/
//// end: tambahan
//
// //      comp.runProgram(); // default alamat awal eksekusi adalah 0
//      comp.runProgram(lokasi); // user dapat menetapkan alamat awal program yang dieksekusi
//
//
//    // Eksekusi instruksi bertahap
///*TRAce
//         int numImages=0;
//      // loop eksekusi instruksi demi instruksi
//       while ((curExecution=comp.runInstruction()) != HALT) {
//         System.out.println("Akses register r0 = "+comp.r0+" r1 = "+comp.r1);
//         System.out.print("Ketikkan sembarang angka dan tekan <Enter>, untuk melanjutkan -> ");
//         numImages=konsol.nextInt();
//         konsol.nextLine(); // flush
//       }
//TRAce*/
//       System.out.printf("operation has been executed -> %02x\n",curExecution);
//       comp.powerOff();
//
//    } // main
//}
