package statement;

import com.florianingerl.util.regex.Pattern;
import main.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class AssignmentNode extends SimpleStatementNode {
  private boolean directAssignment = false;
  public AssignmentNode(String line, int lineNumber, boolean direct) {
    setLine(line);
    setLineNumber(lineNumber);
    directAssignment = direct;
  }

  @Override
  public ArrayList<InstructionOffset> parse() throws ParserException {
    ArrayList<Instruction> instructions = new ArrayList<>();

    String tokens[] = getLine().split(" ");

    // For direct assignments, the syntax is "var lValue = ..."
    // For normal assignments, the syntax is "lValue = ..."
    String lValue = tokens[directAssignment ? 1 : 0];

    if(directAssignment){
      if (lValue.matches(Parser.IDENTIFIER_PATTERN)) {
        getParent().addVarToSymbolTable(lValue);
      } else if (lValue.matches(Parser.IDENTIFIER_PATTERN + "\\[" + Parser.NUMBER_PATTERN + "]")) {
        throw new ParserException("Direct assignments for arrays is not supported (at line " + getLineNumber() + ")");
      }
    }

    // Set offset for instruction memory access
    Immediate lValueOffset = null;
    if (lValue.matches(Parser.IDENTIFIER_PATTERN)) {
      if ((lValueOffset = getLocation(lValue)) == null)
        throw new ParserException("'" + lValue + "' is not declared in this scope (at line " + getLineNumber() + ")");
    } else if (lValue.matches(Parser.IDENTIFIER_PATTERN + "\\[" + Parser.NUMBER_PATTERN + "]")) {
      String sArr[] = lValue.split("(?<=[\\[\\]])|(?=[\\[\\]])");
      String arrName = sArr[0];
      int index = Integer.parseInt(sArr[2]);
      if ((lValueOffset = getLocation(arrName)) == null)
        throw new ParserException("'" + lValue + "' not declared in this scope (at line " + getLineNumber() + ")");
      lValueOffset = new Immediate(lValueOffset.getIntValue() + index * Parser.LINE_SIZE);
    }

    // Removes lValue and assignment operator from the arithmetic expression
    StringBuilder sb = new StringBuilder();
    for (int i = (directAssignment ? 3 : 2); i < tokens.length; i++) {
      if (i > (directAssignment ? 3 : 2)) sb.append(" ");
      sb.append(tokens[i]);
    }
    int currentRegister = 14;
    String infix = sb.toString();

    // Parses the arithmetic infix notation into RPN (Reverse Polish Notation)/postfix
    ArrayList<String> RPN = Parser.parseArithmetic(infix, getLineNumber());

    // Parses RPN into machine instructions
    for (String token : RPN) {
      if (token.matches(Parser.NUMBER_PATTERN)) {
        // Move constant into RX
        // MOVI RX constant
        instructions.add(
            new Instruction(
                Operator.MOVI,
                Register.getRegister(currentRegister--),
                new Immediate(Integer.parseInt(token))
            )
        );
      } else if (token.matches(Parser.IDENTIFIER_PATTERN)) {
        Immediate i = getLocation(token);
        if (i == null)
          throw new ParserException("'" + token + "' not declared in this scope (at line " + getLineNumber() + ")");
        // Copy variable data into RX
        // MOVM RX [R15 + locationOffset]
        instructions.add(
            new Instruction(
                Operator.MOVM,
                Register.getRegister(currentRegister--),
                new Memory(Register.R15, i)
            )
        );
      } else if (token.matches(Parser.IDENTIFIER_PATTERN + "\\[" + Parser.NUMBER_PATTERN + "]")) {
        String sArr[] = token.split("(?<=[\\[\\]])|(?=[\\[\\]])");
        String arrName = sArr[0];
        int index = Integer.parseInt(sArr[2]);
        Immediate i = getLocation(arrName);
        if (i == null)
          throw new ParserException("'" + token + "' not declared in this scope (at line " + getLineNumber() + ")");
        // Copy array element data into RX
        // MOVM RX [R15 + locationOffset + arrayIndex]
        instructions.add(
            new Instruction(
                Operator.MOVM,
                Register.getRegister(currentRegister--),
                new Memory(Register.R15, new Immediate(i.getIntValue() + index * Parser.LINE_SIZE))
            )
        );
      } else if (token.matches(Parser.ADDITIVE_OPERATOR + "|" + Parser.MULTIPLICATIVE_OPERATOR)) {
        Operator operator = null;
        switch (token) {
          case "+": operator = Operator.ADD; break;
          case "-": operator = Operator.SUB; break;
          case "*": operator = Operator.MUL; break;
          case "/": operator = Operator.DIV; break;
        }
        // Applies operation to the two top register "Stack"
        // OP RX RX RY, meaning RX = RX (OP) RY. OP can be either ADD, SUB, MUL, or DIV.
        instructions.add(
            new Instruction(
                operator,
                Register.getRegister(currentRegister + 1),
                Register.getRegister(currentRegister + 1),
                Register.getRegister(currentRegister++)
            )
        );
      }
    }

    // Assigns the value into the variable
    // MOV R14 [R15+lValueOffset]
    instructions.add(
        new Instruction(
            Operator.MOV,
            Register.R14,
            new Memory(Register.R15, lValueOffset)
        )
    );

    // Sets the offset for each instruction
    ArrayList<InstructionOffset> instructionOffset = new ArrayList<>();
    int i = 0;
    for (Instruction ins : instructions) {
      instructionOffset.add(new InstructionOffset(ins, i++));
    }
    return instructionOffset;
  }
}
