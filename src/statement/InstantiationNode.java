package statement;

import main.InstructionOffset;
import main.Parser;
import main.ParserException;

import java.util.ArrayList;

public class InstantiationNode extends SimpleStatementNode {
  public InstantiationNode(String line, int lineNumber) {
    setLine(line);
    setLineNumber(lineNumber);
  }
  @Override
  public ArrayList<InstructionOffset> parse() throws ParserException {
    String tokens[] = getLine().split(" ");

    // syntax is "var identifier"
    String identifier = tokens[1];
    if (identifier.matches(Parser.IDENTIFIER_PATTERN)) {
      getParent().addVarToSymbolTable(identifier);
    } else if (identifier.matches(Parser.IDENTIFIER_PATTERN + "\\[" + Parser.NUMBER_PATTERN + "]")) {
      String sArr[] = identifier.split("(?<=[\\[\\]])|(?=[\\[\\]])");
      String arrName = sArr[0];
      int len = Integer.parseInt(sArr[2]);
      getParent().addArrToSymbolTable(arrName, len);
    } else
      throw new ParserException("Invalid identifier pattern");
    return new ArrayList<>();
  }
}
