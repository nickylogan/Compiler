package computer;

public interface OperationCode {
  public enum Endian {littleEndian, bigEndian}

  ;

  public enum Register {r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15}

  ;
  // keyword dan kode, opcode (operation code), bisa jadikan Interface
  final byte ADD = (byte) 0x00; // add register ke register
  final byte ADDI = (byte) 0x01; // add immediate data ke register
  final byte MOVR = (byte) 0x02; // move register ke register, 32 bits
  final byte MOVI = (byte) 0x03; // move immediate data 16 bits ke register
  final byte MOV = (byte) 0x04; // move register ke memori 32 bits
  final byte MOVM = (byte) 0x05; // move 32 bits dari memori ke register
  final byte MOVB = (byte) 0x06; // move byte 8 bits dari register ke memori
  final byte MOVMB = (byte) 0x07; // move byte 8 bits dari memori ke register
  final byte MOVH = (byte) 0x08; // move half word high 16 bits dari register ke memori
  final byte MOVMH = (byte) 0x09; // move half word high 16 bits dari memori ke register
  final byte MOVL = (byte) 0x0A; // move half word low 16 bits dari register ke memori
  final byte MOVML = (byte) 0x0B; // move half word low 16 bits dari memori ke register
  final byte MOVD = (byte) 0x0C; // move double  word 32 bits dari register ke memori
  final byte MOVMD = (byte) 0x0D; // move double  word 32 bits dari memori ke register
  final byte PUSH = (byte) 0x0E; // masukan register ke stack
  final byte POP = (byte) 0x0F; // keluarkan data dari stack ke register
  final byte PUSHF = (byte) 0x10; // masukan flag register (FLAG) ke stack
  final byte POPF = (byte) 0x11; // keluarkan data dari stack simpan ke flag register (FLAG)
  final byte INTR = (byte) 0x12; // Invoke interrupt
  final byte IN = (byte) 0x13; // Read Input device
  final byte OUT = (byte) 0x14; // Write output device
  final byte JMP = (byte) 0x15; // uncoditional jump immediate address
  final byte JMPR = (byte) 0x16; // uncoditional jump immediate address
  final byte JE = (byte) 0x17; // coditional jump if equal to immediate address
  final byte JNE = (byte) 0x18; // coditional jump if not equal to immediate address
  final byte JLT = (byte) 0x19; // coditional jump if less than to immediate address
  final byte JGT = (byte) 0x1A; // coditional jump if greater than to immediate address
  final byte SUB = (byte) 0x1B; // arithmetic subtraction inter registers
  final byte SUBI = (byte) 0x1C; // arithmetic subtraction immediate data
  final byte MOVMHW = (byte) 0x1D; // move numeric data 16 bits dari memori ke register
  final byte MUL = (byte) 0x1E; // arithmetic multiplication inter registers
  final byte MULI = (byte) 0x1F; // arithmetic multiplication between register and immediate data
  final byte DIV = (byte) 0x20; // arithmetic divide inter registers
  final byte DIVI = (byte) 0x21; // arithmetic divide between register and immediate data
  final byte CALL = (byte) 0x22; // call procedure (jump to address and run)
  final byte RET = (byte) 0x23; // return from procedure (jump to return address and run)
  final byte SFLEFT = (byte) 0x24; // Shift bit left, sama dengan perkalian dengan 2
  final byte SFRIGHT = (byte) 0x25; // Shift bit right, sama dengan pembagian dengan 2
  final byte HALT = (byte) 0x7F; // halt program/system
  final byte UNKNOWN = (byte) 0xFF; // unknown (invalid) operation(code)
  // end: keyword opcode
}
