package statement;

import main.*;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class StatementNode {
  private StatementNode parent = null;
  private String line;
  private Integer lineNumber;
  private Integer offset;
  private String nodeID;

  public StatementNode getParent() {
    return parent;
  }

  public void setParent(StatementNode parent) {
    this.parent = parent;
    Parser.createRelationship(getNodeID(), parent.getNodeID());
  }

  public abstract ArrayList<InstructionOffset> parse();

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

  public void setNodeID(String nodeID) {
    this.nodeID = nodeID;
  }

  public String getNodeID() {
    return nodeID;
  }
}
