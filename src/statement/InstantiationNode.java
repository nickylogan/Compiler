package statement;

import main.InstructionOffset;
import main.Parser;
import main.ParserException;
import main.SymbolType;

import java.util.ArrayList;

public class InstantiationNode extends SingleStatementNode {
  public InstantiationNode(String line, int lineNumber) {
    setLine(line);
    setLineNumber(lineNumber);
    setNodeID("SINGLE" + Parser.getSingleCount());
  }
  @Override
  public ArrayList<InstructionOffset> parse() throws ParserException {
    String tokens[] = getLine().split(" ");
    String error = null;
    // syntax is "var identifier"
    String identifier = tokens[1];
    if(identifier.matches(Parser.KEYWORDS)){
      error = "Use of reserved keyword at line " + getLineNumber();
    } else if (identifier.matches(Parser.IDENTIFIER_PATTERN)) {
      Parser.insertVariable(identifier, SymbolType.VAR, Parser.LINE_SIZE, getParent().getNodeID(), getLineNumber());
    } else if (identifier.matches(Parser.IDENTIFIER_PATTERN + "\\[" + Parser.NUMBER_PATTERN + "]")) {
      String sArr[] = identifier.split("(?<=[\\[\\]])|(?=[\\[\\]])");
      String arrName = sArr[0];
      int len = Integer.parseInt(sArr[2]);
      Parser.insertVariable(arrName, SymbolType.VAR, Parser.LINE_SIZE * len, getParent().getNodeID(), getLineNumber());
    } else
      error = "Invalid identifier pattern at line " + getLineNumber() + 1;

    if (error != null){
      Parser.addError(error);
      return null;
    }
    return new ArrayList<>();
  }
}
