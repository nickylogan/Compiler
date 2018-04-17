package statement;

import main.*;

import java.util.ArrayList;

public class KeywordStatement extends StatementNode {
  private Keyword keyword;

  public KeywordStatement(Keyword keyword, int lineNumber) {
    setKeyword(keyword);
    setLineNumber(lineNumber);
    setNodeID("SINGLE" + Parser.getSingleCount());
  }

  public void setKeyword(Keyword keyword) {
    this.keyword = keyword;
  }

  public Keyword getKeyword() {
    return keyword;
  }

  @Override
  public ArrayList<InstructionOffset> parse() throws ParserException {
    ArrayList<InstructionOffset> ins = new ArrayList<>();
    InstructionOffset in = null;
    if (keyword == Keyword.BREAK) {
      in = new InstructionOffset(new Instruction(Operator.JMP, new Immediate(0)), 0);
      in.setLabel("break");
    } else if (keyword == Keyword.CONTINUE) {
      in = new InstructionOffset(new Instruction(Operator.JMP, new Immediate(0)), 0);
      in.setLabel("continue");
    }
    ins.add(in);
    return ins;
  }
}
