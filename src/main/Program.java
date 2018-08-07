package main;

import com.sun.istack.internal.NotNull;
import compiler.Instruction;
import compiler.Operator;
import compiler.SymbolTable;
import computer.Computer;
import computer.OperationCode;

import java.io.IOException;
import java.util.ArrayList;

public class Program {
  private ArrayList<Instruction> instructions;
  private SymbolTable symbolTable;
  private String fileName;
  private int currentExecution = 0;
  private Computer comp;
  private int initLocation = 0;

  /**
   * The Program class will be used as an interface between the GUI and the computer.
   * When a program is ran on the main window, an instance of Program will be created.
   *
   * @param instructions a list of computer instructions to be executed
   * @param symbolTable the list of symbols used in the program, making it debuggable
   * @param fileName the program file name
   */
  public Program(@NotNull ArrayList<Instruction> instructions, @NotNull SymbolTable symbolTable, String fileName) {
    this.instructions = instructions;
    this.symbolTable = symbolTable;
    this.fileName = fileName;


    comp = new Computer();
    comp.powerOn();
    try {
      // activate the code injector module, with an initial location argument.
      comp.codeInjector(fileName, initLocation);
    } catch (IOException ignored) { }
    comp.cpu.BREG = initLocation;
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
    currentExecution = comp.PC() / 4;
    if (opcode == OperationCode.HALT) {
      currentExecution = -1;
    }
    return temp;
  }

  public void destroy() {
    comp.powerOff();
  }

  public byte[] getMemory() {
    return comp.mainMemory.getaCells();
  }

  public byte[] accessMemory(int address) {
    return comp.mainMemory.readWordMemory(address);
  }

  public byte accessByte(int address) {
    return comp.mainMemory.readByteMemory(address);
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

  public int getMemorySize() {
    return comp.mainMemory.memorySize();
  }
}
