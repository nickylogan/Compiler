package compiler.statement;

import compiler.InstructionOffset;
import compiler.Parser;
import compiler.SymbolType;

import java.util.ArrayList;

public class InstantiationNode extends SingleStatementNode {
  public InstantiationNode(String line, int lineNumber) {
    setLine(line);
    setLineNumber(lineNumber);
    setNodeID("SINGLE" + Parser.getSingleCount());
  }
  @Override
  public ArrayList<InstructionOffset> parse(){
    String tokens[] = getLine().split(" ");
    String error = null;
    // syntax is "var identifier"
    String identifier = tokens[1];
    if(identifier.matches(Parser.KEYWORDS)){
      error = "Use of reserved keyword at line " + getLineNumber();
    } else if (identifier.matches(Parser.IDENTIFIER_PATTERN)) {
      if(!Parser.insertVariable(identifier, SymbolType.VAR, Parser.LINE_SIZE, getParent().getNodeID(), getLineNumber()))
        error = identifier + " already declared in this scope (at line " + getLineNumber() + ")";
    } else if (identifier.matches(Parser.IDENTIFIER_PATTERN + "\\[" + Parser.NUMBER_PATTERN + "]")) {
      String sArr[] = identifier.split("(?<=[\\[\\]])|(?=[\\[\\]])");
      String arrName = sArr[0];
      int len = Integer.parseInt(sArr[2]);
      if(!Parser.insertVariable(arrName, SymbolType.ARRAY, Parser.LINE_SIZE * len, getParent().getNodeID(), getLineNumber()))
        error = arrName + " already declared in this scope (at line " + getLineNumber() + ")";
    } else
      error = "Invalid identifier pattern at line " + getLineNumber();

    if (error != null){
      Parser.addError(error);
      return null;
    }
    return new ArrayList<>();
  }
}
