package statement;

import main.*;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class StatementNode {
  private StatementNode parent = null;
  private String line;
  private Integer lineNumber;
  private Integer offset;
  private HashMap<String, Immediate> symbolTable;
  private int localTableSize;

  public StatementNode getParent() {
    return parent;
  }

  public void setParent(StatementNode parent) {
    this.parent = parent;
  }

  public abstract ArrayList<InstructionOffset> parse() throws ParserException;

  public void setLine(String line) {
    this.line = line;
  }

  public void setLineNumber(Integer lineNumber) {
    this.lineNumber = lineNumber;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public Integer getLineNumber() {
    return lineNumber;
  }

  public String getLine() {
    return line;
  }

  public Integer getOffset() {
    return offset;
  }

  public HashMap<String, Immediate> getSymbolTable() {
    return symbolTable;
  }

  /**
   * Adds a variable to the symbol table.
   *
   * @param variableName the name of the added variable
   */
  public void addVarToSymbolTable(String variableName) {
    symbolTable.put(variableName, new Immediate(getGlobalTableSize()));
    localTableSize += Parser.LINE_SIZE;
  }

  /**
   * Adds an array to the symbol table
   *
   * @param arrayName the name of the added array
   * @param size      size of the array
   */
  public void addArrToSymbolTable(String arrayName, int size) {
    symbolTable.put(arrayName, new Immediate(getGlobalTableSize()));
    localTableSize += size * Parser.LINE_SIZE;
  }


  public int getGlobalTableSize() {
    int size = 0;
    StatementNode current = this;
    while (current != null) {
      size += current.getLocalTableSize();
      current = current.getParent();
    }
    return size;
  }

  public int getLocalTableSize() {
    return localTableSize;
  }

  public Immediate getLocation(String identifier) {
    Immediate loc;
    StatementNode current = this;
    while ((loc = current.symbolTable.get(identifier)) == null && current.getParent() != null)
      current = current.getParent();
    return loc;
  }
}
