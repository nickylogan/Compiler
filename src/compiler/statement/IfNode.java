package compiler.statement;

import compiler.*;

import java.util.ArrayList;

@SuppressWarnings("Duplicates")
public class IfNode extends StatementNode {
  private MultipleStatementNode trueChild;
  private MultipleStatementNode falseChild;

  public IfNode(String line, int lineNumber) {
    setLine(line);
    setLineNumber(lineNumber);
    setNodeID("IF" + Parser.getIfCount());
    trueChild = new MultipleStatementNode();
    trueChild.setParent(this);
  }

  @Override
  public ArrayList<InstructionOffset> parse() {
    // parsing result is stored here
    ArrayList<InstructionOffset> result = new ArrayList<>();
    // store errors for this if block
    ArrayList<String> tempErrors = new ArrayList<>();

    // extract if condition
    String eval = getLine().split("(?<=if)")[1];

    // Immediate labels
    Immediate elseLabel = new Immediate(0);
    Immediate endLabel = new Immediate(0);

    // parse if condition
    ArrayList<String> RPN = Parser.parseArithmetic(eval, getLineNumber());
    if (RPN.get(0).equals("#ERROR")) {
      tempErrors.addAll(RPN.subList(1, RPN.size()));
    }

    // check if result of condition is always true/false
    Boolean aBoolean = null;
    if (RPN.size() == 1 && RPN.get(0).matches("-?\\d+")) {
      int res = Integer.parseInt(RPN.get(0));
      aBoolean = res != 0;
    }

    // parse condition into instructions
    ArrayList<Instruction> booleanInstructions = new ArrayList<>();
    if (aBoolean == null) {
      booleanInstructions = Parser.parseRPNtoInstruction(RPN, getLineNumber(), getNodeID(), tempErrors, 14);
      booleanInstructions.add(
          new Instruction(
              Operator.JE,
              Register.R14,
              Register.R0,
              elseLabel
          )
      );
    }

    // return nothing if errors exist in condition
    if (!tempErrors.isEmpty()) {
      Parser.addErrorList(tempErrors);
      return null;
    }

    // store children here
    ArrayList<InstructionOffset> trueInstructions = new ArrayList<>();
    ArrayList<InstructionOffset> falseInstructions = new ArrayList<>();

    int offset = booleanInstructions.size();

    // if condition is not deterministic
    if (aBoolean == null) {
      trueInstructions = trueChild.parse();

      // if errors exist in the child node, return null
      if (trueInstructions == null)
        return null;

      // update offset for each true child instructions
      for (InstructionOffset io : trueInstructions) {
        io.setOffset(offset + io.getOffset());
      }

      // update offset after true child is parsed
      offset += trueInstructions.size();

      // set else label to be after true instructions
      elseLabel.setValue(offset);

      // check if 'else' block exists
      if (falseChild != null) {
        // create exit instruction
        Instruction exit = new Instruction(
          Operator.JMP,
          endLabel
        );
        System.out.println(offset);
        trueInstructions.add(new InstructionOffset(exit, offset));

        // increment label for else because an exit instruction is added
        elseLabel.setValue(++offset);

        falseInstructions = falseChild.parse();
        // if errors exist in the child node, return null
        if(falseInstructions == null)
          return null;

        // update offset for each false child instructions
        for (InstructionOffset io : falseInstructions) {
          io.setOffset(offset + io.getOffset());
        }

        // last update to offset
        offset += falseInstructions.size();
        // update end label
        endLabel.setValue(offset);
      }
    } else if (aBoolean) { // if condition is always true, do not parse falseChild
      trueInstructions = trueChild.parse();
    } else if (falseChild != null){
      // if condition is always false, do not parse trueChild. If falseChild does not exist, return nothing
        falseInstructions = falseChild.parse();
    }

    // add offset to instructions for if condition
    for(int i = 0; i<booleanInstructions.size(); ++i){
      Instruction ins = booleanInstructions.get(i);
      result.add(new InstructionOffset(ins, i));
    }

    // combine parse result
    result.addAll(trueInstructions);
    result.addAll(falseInstructions);

    return result;
  }

  public MultipleStatementNode createFalseChild() {
    falseChild = new MultipleStatementNode();
    falseChild.setParent(this);
    return falseChild;
  }

  public MultipleStatementNode getFalseChild() {
    return falseChild;
  }

  public MultipleStatementNode getTrueChild() {
    return trueChild;
  }
}
