package statement;

import main.*;

import java.util.ArrayList;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
public class WhileNode extends StatementNode {
  private Token operator;
  private MultipleStatementNode children;

  public WhileNode(String line, int lineNumber) {
    setLine(line);
    setLineNumber(lineNumber);
    setNodeID("WHILE" + Parser.getWhileCount());
    children = new MultipleStatementNode();
    children.setParent(this);
  }

  @Override
  public ArrayList<InstructionOffset> parse() {
    // parsing result is stored here
    ArrayList<InstructionOffset> result = new ArrayList<>();
    // store errors for this while block
    ArrayList<String> tempErrors = new ArrayList<>();

    // extract loop condition
    String eval = getLine().split("(?<=while)")[1];

    // Immediate labels
    Immediate startLabel = new Immediate(0);
    Immediate endLabel = new Immediate(0);

    // parse loop condition
    ArrayList<String> RPN = Parser.parseArithmetic(eval, getLineNumber());
    if (RPN.get(0).equals("#ERROR")) {
      tempErrors.addAll(RPN.subList(1, RPN.size()));
    }

    // check if result of loop condition is always true/false
    Boolean aBoolean = null;
    if (RPN.size() == 1 && RPN.get(0).matches("-?\\d+")) {
      int res = Integer.parseInt(RPN.get(0));
      aBoolean = res != 0;
    }

    // parse loop condition into instructions
    ArrayList<Instruction> booleanInstructions = new ArrayList<>();
    if (aBoolean == null) {
      booleanInstructions = Parser.parseRPNtoInstruction(RPN, getLineNumber(), getNodeID(), tempErrors, 14);
      booleanInstructions.add(
          new Instruction(
              Operator.JE,
              Register.R14,
              Register.R0,
              endLabel
          )
      );
    } else if (!aBoolean) {
      // if condition is always false, why bother parsing the rest?
      return result;
    }

    // return nothing if errors exist in condition
    if (!tempErrors.isEmpty()) {
      Parser.addErrorList(tempErrors);
      return null;
    }

    // parse child statements
    ArrayList<InstructionOffset> childInstructions = children.parse();
    if (childInstructions == null)
      return null;

    // store references to labels
    ArrayList<Immediate> start = new ArrayList<>();
    ArrayList<Immediate> end = new ArrayList<>();

    // update children offset
    int offset = booleanInstructions.size();
    for (InstructionOffset io : childInstructions) {
      if (io.getLabel().equals("break")) {
        ArrayList<Operand> ops = io.getInstruction().getOperands();
        end.add((Immediate) ops.get(ops.size() - 1));
        io.setLabel("");
      } else if (io.getLabel().equals("continue")) {
        ArrayList<Operand> ops = io.getInstruction().getOperands();
        start.add((Immediate) ops.get(ops.size() - 1));
        io.setLabel("");
      }
      io.setOffset(io.getOffset() + offset);
    }
    childInstructions.add(new InstructionOffset(
        new Instruction(
            Operator.JMP,
            startLabel
        ), offset + childInstructions.size()
    ));

    // update labels
    endLabel.setValue(offset + childInstructions.size());
    // update references to label
    for(Immediate i : start) i.setValue(startLabel.getIntValue());
    for(Immediate i : end) i.setValue(endLabel.getIntValue());

    // add offset to instructions for loop condition
    for(int i = 0; i<booleanInstructions.size(); ++i){
      Instruction ins = booleanInstructions.get(i);
      result.add(new InstructionOffset(ins, i));
    }

    // add the whole while body to the result
    result.addAll(childInstructions);

    return result;
  }

  public MultipleStatementNode getChildren() {
    return children;
  }
}
