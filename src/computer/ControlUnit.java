package computer;

// simulasi control unit
public class ControlUnit implements OperationCode {
  public Processor cpu;// = new Processor(new Memory());
  public Memory physicalMemory; // memory di controlunit

  public byte opcode;
  private byte[] IR = new byte[4];
  private boolean isRunProgram;
  public int durationInput = 0;
  public int durationOutput = 0;

  ControlUnit(Processor pCPU, Memory pMemory) {
    cpu = pCPU;
    cpu.PC = 0; // start address execution
    physicalMemory = pMemory;
    cpu.connectToMemory(physicalMemory);
    isRunProgram = false;
  }

  ControlUnit(Processor pCPU) {
    cpu = pCPU;
    cpu.PC = 0; // start address execution
  }

  // wrapping akses ke memori, display isi memori
  public void memoryContent() {
    cpu.memoryContent();
  }

  // informasi status in running program
  public boolean statusRun() {
    return isRunProgram;
  }

  // request  running program
  public boolean makeRun() {
    if (isRunProgram) return false; // sudah ada program yang sedang dieksekusi
    else {
      isRunProgram = true;
      return true;
    }
  }

  public void registerContent() {
    cpu.registerContent();
  }

  public void setExecutionAddress(int startAdr) {
    cpu.PC = startAdr;
  }

  public void setBaseAddress(int startAdr) {
    cpu.BREG = startAdr;
  }

  public void setNextInstruction() {
    cpu.PC = cpu.PC + 4;
  }

  public void execute() {
    isRunProgram = true;
    IR = fetchPhase(cpu.PC); // fetch
    opcode = decodePhase(IR); // byte mengenai operation code
    while (opcode != HALT) {
//          System.out.printf("execute(), PC = %x, opcode = %x\n",cpu.PC,opcode);
      cpu.PC = cpu.PC + 4; // normal next instruction address
      executePhase();
      // set next instruction address
//           cpu.PC = cpu.PC+4; // normal next instruction address      
      IR = fetchPhase(cpu.PC); // fetch
      opcode = decodePhase(IR); // asumsi operation code
    } // endwhile
    if (opcode == HALT) executePhase();
    System.out.println("HLT compiler.statement");
    isRunProgram = false; // completion
  }

  // eksekusi satu siklus instruksi
  //   return opcode yang barusan dieksekusi
  public byte executeInstruction() {
    if (!isRunProgram) isRunProgram = true;
    IR = fetchPhase(cpu.PC); // fetch
    opcode = decodePhase(IR); // byte mengenai operation code
    System.out.printf("execute(), PC = %x, opcode = %x\n", cpu.PC, opcode);
    cpu.PC = cpu.PC + 4; // normal next instruction address
    executePhase();
    // set next instruction address
    if (opcode == HALT) {
      isRunProgram = false;
    }
    return opcode; // member data, sehingga dapat diakses langsung
  }


  // fetch data/instruksi di memori pada lokasi lokMem
  // isi code ini masih sementara
  private byte[] fetchPhase(int lokMem) {
    byte[] aData;
    aData = new byte[4];
    aData = physicalMemory.readWordMemory(lokMem); // RAM
    return aData;
  }

  private byte decodePhase(byte[] aCode) {
    // peroleh opcode
    cpu.tuningWordMemory(aCode); // menata susunan data untuk persiapan basis pengolahan (little endian)
//      System.out.println("Fase decode dijalankan, opcode: "+aCode[3]);

    return aCode[3]; // opcode
  }

  private void executePhase() {
//      System.out.println("Fase eksekusi dijalankan");
    selectOperation(opcode, IR);
  }

  // decoded instruction
  // mengidentifikasi instruksi yang sekarang harus dikerjakan
  // opcode -> opc
  public void selectOperation(byte opc, byte[] pWord) {
    cpu.newOpCode(opc);
    switch (opc) {
      case ADD: { // ADD
        cpu.add(pWord);
        break;
      }
      case ADDI: { // ADDI
        cpu.addi(pWord);
        break;
      }
      case CALL: { // CALL
        cpu.call(pWord);
        break;
      }
      case DIV: { // DIV, divide
        cpu.div(pWord);
        break;
      }
      case DIVI: { // DIVI, divide
        cpu.divi(pWord);
        break;
      }
      case MOV: { // MOV
        cpu.mov(pWord);
        break;
      }
      case MOVR: { // MOVR
        cpu.movr(pWord);
        break;
      }
      case MOVI: { // MOVI
        cpu.movi(pWord);
        break;
      }
      case MOVM: { // MOVM
        cpu.movm(pWord);
        break;
      }
      case MOVB: { // MOVB
        cpu.movb(pWord);
        break;
      }
      case MOVMB: { // MOVMB
        cpu.movmb(pWord);
        break;
      }
      case MOVH: { // MOVH:[16-31]
        cpu.movh(pWord);
        break;
      }
      case MOVMH: { // MOVMH
        cpu.movmh(pWord);
        break;
      }
      case MOVL: { // MOVL:[0-15]
        cpu.movl(pWord);
        break;
      }
      case MOVML: { // MOVML
        cpu.movml(pWord);
        break;
      }
      case MOVD: { // MOVD
        cpu.movd();
        break;
      }
      case MOVMD: { // MOVMD
        cpu.movmd();
        break;
      }
      case MOVMHW: { // MOVMHW, memori 16 bits ke registe
        cpu.movmhw(pWord);
        break;
      }
      case MUL: { // MUL, multiplication
        cpu.mul(pWord);
        break;
      }
      case MULI: { // MULI, multiplication with immediate data
        cpu.muli(pWord);
        break;
      }
      case PUSH: { // PUSH
        cpu.push(pWord);
        break;
      }
      case POP: { // POP
        cpu.pop(pWord);
        break;
      }
      case PUSHF: { // PUSHF
        cpu.pushf();
        break;
      }
      case POPF: { // POPF
        cpu.popf();
        break;
      }
      case INTR: { // INTR
        cpu.intr();
        break;
      }
      case IN: { // IN
        byte regndx;
        regndx = (byte) (pWord[2] & 0x0F);
        cpu.in(pWord);
        durationInput = cpu.aRegister[regndx];
        break;
      }
      case OUT: { // OUT
        byte regndx;
        regndx = (byte) (pWord[2] & 0x0F);
        cpu.out(pWord);
        durationOutput = cpu.aRegister[regndx];
        break;
      }
      case JMP: { // JMP
        cpu.jmp(pWord);
        break;
      }
      case JMPR: { // JMPR
        cpu.jmpr(pWord);
        break;
      }
      case JE: { // JE
        cpu.je(pWord);
        break;
      }
      case JNE: { // JNE
        cpu.jne(pWord);
        break;
      }
      case JLT: { // JE
        cpu.jlt(pWord);
        break;
      }
      case JGT: { // JNE
        cpu.jgt(pWord);
        break;
      }
      case RET: { // RET
        cpu.ret(pWord);
        break;
      }
      case SFLEFT: { // SFLEFT
        cpu.sfleft(pWord);
        break;
      }
      case SFRIGHT: { // SFRIGHT
        cpu.sfright(pWord);
        break;
      }
      case SUB: { // SUB
        cpu.sub(pWord);
        break;
      }
      case SUBI: { // SUBI
        cpu.subi(pWord);
        break;
      }
      case HALT: { // HLT
        cpu.halt();
        break;
      }
      default: {
        cpu.unknown();
        break;
      }
    } // endcase-instruksi
  }
}