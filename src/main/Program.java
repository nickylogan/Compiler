package main;

import com.sun.istack.internal.NotNull;
import computer.Computer;
import computer.Memory;
import computer.OperationCode;
import computer.Processor;

import java.io.IOException;
import java.util.ArrayList;

public class Program {
  private ArrayList<Instruction> instructions;
  private SymbolTable symbolTable;
  private String fileName;
  private int currentExecution = 0;
  private Computer comp;
  private int initLocation = 0;

  public Program(@NotNull ArrayList<Instruction> instructions, @NotNull SymbolTable symbolTable, String fileName) {
    this.instructions = instructions;
    this.symbolTable = symbolTable;
    this.fileName = fileName;


    comp = new Computer();
    comp.powerOn();
    try {
      comp.codeInjector(fileName, initLocation); // aktifkan modul tool injeksi code, dengan alamat ditetapkan melalui parameter
    } catch (IOException e) {
//      e.printStackTrace();
    }
    comp.cpu.BREG = initLocation; // base register
//    while ((curExecution = comp.runInstruction(initLocation)) != OperationCode.HALT) {
//      System.out.println("Eksekusi opcode ->  " + curExecution);
//    } // endwhile
  }

  public SymbolTable getSymbolTable() {
    return symbolTable;
  }

  public Instruction currentInstruction() {
    return currentExecution == -1 ? new Instruction(Operator.HALT) : instructions.get(currentExecution);
  }

  public int executeNext() {
    int temp = currentExecution;
    if (currentExecution == -1) return currentExecution;
    byte opcode = comp.runInstruction(initLocation);
    currentExecution = comp.PC()/4;
    if (opcode == OperationCode.HALT) {
      currentExecution = -1;
    }
    return temp;
  }

  public void destroy() {
    comp.powerOff();
  }

  public Memory getMemory() {
    return comp.mainMemory;
  }

  public byte[] accessMemory(int address) {
    return comp.mainMemory.readWordMemory(address);
  }

  public int[] getGeneralRegisters() {
    return comp.getRegisters();
  }

  public int getRegisterValue(int i) {
    return getGeneralRegisters()[i];
  }

  public int getProgramCounter() {
    return comp.PC();
  }

  public int getFlagRegister() {
    return comp.cpu.FLAG;
  }

  public int getBaseRegister() {
    return comp.cpu.BREG;
  }

  public int getStackPointer() {
    return comp.cpu.SP;
  }

  public int getBasePointer() {
    return comp.cpu.BP;
  }

  public int getLimitRegister() {
    return comp.cpu.LREG;
  }

  public ArrayList<Instruction> getInstructions() {
    return new ArrayList<>(instructions);
  }

  public int getCurrentExecution() {
    return currentExecution;
  }

  public String getFileName() {
    return fileName;
  }
}
