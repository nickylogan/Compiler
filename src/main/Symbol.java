package main;

public class Symbol {
  private String symbolName;
  private SymbolType type;
  private int size;
  private int lineNumber;
  private String nodeID;
  private Immediate location;

  public Symbol(String symbolName, SymbolType type, int size, int lineNumber, String nodeID) {
    this.symbolName = symbolName;
    this.type = type;
    this.size = size;
    this.lineNumber = lineNumber;
    this.nodeID = nodeID;
    this.location = new Immediate(0);
  }

  public void setLocation(Immediate location) {
    this.location = location;
  }

  public Immediate getLocation() {
    return location;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public int getSize() {
    return size;
  }

  public String getNodeID() {
    return nodeID;
  }

  public SymbolType getType() {
      return type;
  }

  public String getSymbolName() {
    return symbolName;
  }

  @Override
  public String toString() {
    return symbolName + " => " + getLocation().getIntValue();
  }
}
