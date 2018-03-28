package main;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.*;

import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;
import statement.*;

import static main.Operator.*;

public class Parser {
  private static HashMap<String, VariableLocation> variables = new HashMap<>();
  private static ArrayList<Instruction> instructions;
  public final static int LINE_SIZE = 4;
  public final static int LINE_INIT_POS = 0;
  private static MultipleStatementNode root = new MultipleStatementNode();

  //String patterns
  public static final String IDENTIFIER_PATTERN = "[A-Za-z_][A-Za-z0-9_]*";
  public static final String NUMBER_PATTERN = "\\d+";
  public static final String SUBSCRIPT_PATTERN = "\\[(?'NUMBER')\\]";
  public static final String ADDITIVE_OPERATOR = "[-+]";
  public static final String MULTIPLICATIVE_OPERATOR = "[*/]";
  public static final String COMPARISON_OPERATOR = "[<>]=?|==|!=";
  public static final String ADDITIVE_PATTERN = "(?'SUMMAND')(?:\\s(?'ADDITIVEOP')\\s(?'SUMMAND'))+";
  public static final String SUMMAND_PATTERN = "(?'VALUE') | (?'PRODUCT') | \\( (?: (?'SUM') | (?'PRODUCT') ) \\)";
  public static final String MULTIPLICATIVE_PATTERN = "(?'FACTOR')(?:(?'MULTIPLICATIVEOP')\\s(?'FACTOR'))+";
  public static final String FACTOR_PATTERN = "(?'VALUE')| \\( \\s (?: (?'SUM') | (?'PRODUCT') ) \\s \\)";
  public static final String VALUE_PATTERN = "(?'NUMBER')|(?'LVALUE')";
  public static final String RVALUE_PATTERN = "(?'VALUE')|(?'SUM')|(?'PRODUCT')";
  public static final String BOOLEAN_PATTERN = "true|false|(?'RVALUE')\\s(?'COMPARISONOP')\\s(?'RVALUE')";
  public static final String GENERAL_REGEX = "" +
      "(?x)(?(DEFINE)" +
      "(?<ADDITIVEOP>" + ADDITIVE_OPERATOR + ")" +
      "(?<MULTIPLICATIVEOP>" + MULTIPLICATIVE_OPERATOR + ")" +
      "(?<COMPARISONOP>" + COMPARISON_OPERATOR + ")" +
      "(?<RVALUE>" + RVALUE_PATTERN + ")" +
      "(?<BOOLEAN>" + BOOLEAN_PATTERN + ")" +
      "(?<SUM>" + ADDITIVE_PATTERN + ")" +
      "(?<SUMMAND>" + SUMMAND_PATTERN + ")" +
      "(?<PRODUCT>" + MULTIPLICATIVE_PATTERN + ")" +
      "(?<NUMBER>" + NUMBER_PATTERN + ")" +
      "(?<VALUE>" + VALUE_PATTERN + ")" +
      "(?<IDENTIFIER>" + IDENTIFIER_PATTERN + ")" +
      "(?<SUBSCRIPT>" + SUBSCRIPT_PATTERN + ")" +
      "(?<LVALUE>(?'IDENTIFIER')(?'SUBSCRIPT')?)" +
      "(?<FACTOR>" + FACTOR_PATTERN + ")" +
      ")";

  public static final Pattern INSTANTIATION_PATTERN = Pattern.compile(
      GENERAL_REGEX +
          "(var)\\s(?'LVALUE')"
  );
  public static final Pattern ASSIGNMENT_PATTERN = Pattern.compile(
      GENERAL_REGEX +
          "(?'LVALUE')\\s=\\s(?'RVALUE')"
  );
  public static final Pattern DIRECT_ASSIGNMENT_PATTERN = Pattern.compile(
      GENERAL_REGEX +
          "(var)\\s(?'IDENTIFIER')\\s=\\s(?'RVALUE')"
  );
  public static final Pattern IF_PATTERN = Pattern.compile(
      GENERAL_REGEX +
          "(if)\\s\\(\\s(?'BOOLEAN')\\s\\)"
  );
  public static final Pattern WHILE_PATTERN = Pattern.compile(
      GENERAL_REGEX +
          "(while)\\s\\(\\s(?'BOOLEAN')\\s\\)"
  );

  public static ArrayList<Instruction> compile(ArrayList<String> lines) throws ParserException {
    if (variables != null) variables.clear();
    if (instructions != null) instructions.clear();
    root = new MultipleStatementNode();
    MultipleStatementNode parent = root;
    ArrayList<InstructionOffset> instructionOffsets = new ArrayList<>();

    int size = lines.size();
    boolean stopped = false;
    for (int i = 0; i < lines.size(); ++i) {
      String line = lines.get(i);
      line = line.replaceAll("(?=[-+*/()<>!]|(?<![<>=!])=)|(?<=[-+*/()]|[<>=!](?!=))", " ");
      line = line.replaceAll("(?<=\\[)\\s+|\\s+(?=])", "");
      line = line.replaceAll("\\s+", " ").trim();
//            System.out.println(line);

      if (Pattern.matches(ASSIGNMENT_PATTERN.toString(), line)) {
        System.out.println("assignment");
        SimpleStatementNode statementNode = new AssignmentNode(line, i + 1, false);
        parent.addStatement(statementNode);
      } else if (Pattern.matches(INSTANTIATION_PATTERN.toString(), line)) {
        System.out.println("instantiation");
        SimpleStatementNode statementNode = new InstantiationNode(line, i + 1);
        parent.addStatement(statementNode);
      } else if (Pattern.matches(DIRECT_ASSIGNMENT_PATTERN.toString(), line)) {
        System.out.println("direct assignment");
        SimpleStatementNode statementNode = new AssignmentNode(line, i + 1, true);
        parent.addStatement(statementNode);
      } else if (Pattern.matches(IF_PATTERN.toString(), line)) {
//				System.out.println("if");
        IfNode ifNode = new IfNode(line, i + 1);
        parent.addStatement(ifNode);
        parent = ifNode.getTrueChild();
      } else if (line.matches("else")) {
//				System.out.println("else");
        parent = ((IfNode) parent.getParent()).createFalseChild();
      } else if (line.matches("endif")) {
//				System.out.println("endif");
        StatementNode st = parent;
        while (st != null && !(st instanceof IfNode)) {
          st = st.getParent();
        }
        if (st == null) {
          throw new ParserException("endif statement without if at line " + (i + 1));
        }
        parent = (MultipleStatementNode) st.getParent();
      } else if (Pattern.matches(WHILE_PATTERN.toString(), line)) {
//                System.out.println("while");
        WhileNode whileNode = new WhileNode(line, i + 1);
        parent.addStatement(whileNode);
        parent = whileNode.getChildren();
      } else if (line.matches("endwhile")) {
//                System.out.println("endwhile");
        StatementNode st = parent;
        while (st != null && !(st instanceof WhileNode)) {
          st = st.getParent();
        }
        if (st == null) {
          throw new ParserException("endwhile statement without while at line " + (i + 1));
        }
        parent = (MultipleStatementNode) st.getParent();
      } else if (line.matches("break")) {
//                System.out.println("break");
        StatementNode st = parent;
        while (st != null && !(st instanceof WhileNode)) {
          st = st.getParent();
        }
        if (st == null) {
          throw new ParserException("break statement without while at line " + (i + 1));
        }
        parent.addStatement(new KeywordStatement(Keyword.BREAK, i + 1));
      } else if (line.matches("continue")) {
//                System.out.println("continue");
        StatementNode st = parent;
        while (st != null && !(st instanceof WhileNode)) {
          st = st.getParent();
        }
        if (st == null) {
          throw new ParserException("continue statement without while at line " + (i + 1));
        }
        parent.addStatement(new KeywordStatement(Keyword.CONTINUE, i + 1));
      } else if (line.matches("endprogram")) {
        stopped = true;
        break;
      } else if (line.isEmpty()) {
      } else throw new ParserException("Invalid syntax at line " + (i + 1));
    }
    if (parent.getParent() != null) {
      throw new ParserException("Missing end of file");
    }
    ArrayList<InstructionOffset> temp = root.parse();
    Instruction init = new Instruction(Operator.MOVI, Register.R15, new Immediate((2 + temp.size()) * LINE_SIZE));
    instructionOffsets.add(new InstructionOffset(init, 0));
    for (InstructionOffset io : temp) {
      io.setOffset(io.getOffset() + 1);
      instructionOffsets.add(io);
    }
    instructionOffsets.add(new InstructionOffset(new Instruction(HALT), instructionOffsets.size()));

    ArrayList<Instruction> instructions = new ArrayList<>();
    for (InstructionOffset io : instructionOffsets) {
      Instruction ins = io.getInstruction();
      Operator op = ins.getOperator();
      if (op == JMP || op == JE || op == JGT || op == JLT || op == JNE) {
        ArrayList<Operand> ops = ins.getOperands();
        Immediate k = ((Immediate) ops.get(ops.size() - 1));
        k.setValue(k.getIntValue() * LINE_SIZE);
      }
      instructions.add(ins);
    }

    return instructions;
  }

  public static ArrayList<Instruction> parseAssignStatement(ArrayList<String> line, int lineNumber) throws ParserException {
    instructions = new ArrayList<>();

    // remove semicolon if assign statement
    //String last = line.get(line.size() - 1);
    //line.set(line.size() - 1, last.substring(0, last.length() - 1));

    String val = line.get(0);
    String assignSymbol = line.get(1);

    if (!isNumeric(val) || !assignSymbol.equals("=")) {
      // syntax: val = res
      Object res = addAssignInstruction(new ArrayList<>(line.subList(2, line.size())), lineNumber);

      boolean isRegister = (Register.isRegister(val) != -1);
      Register r;
      if (isRegister) {
        r = Register.getRegister(Register.isRegister(val));
        if (Register.isReserved(r)) throw new ParserException("Use of reserved register at line " + (lineNumber));
      } else
        r = Register.R14;

      if (res instanceof Register) {
        // syntax: register1 = register2
        int resIndex = ((Register) res).ordinal();
        int valIndex = r.ordinal();
        if (resIndex != valIndex) // not same register
          instructions.add(new Instruction(MOVR, r, (Register) res));
      } else if (res instanceof Integer) {
        // syntax: register = integer
        int numericVal = (Integer) res;
        instructions.add(new Instruction(MOVI, r, new Immediate(numericVal)));
      } else if (((String) res).matches("[A-Za-z][A-Za-z0-9]*")) {
        VariableLocation varLocation = initVariable(res.toString());
//                instructions.add(new Instruction(MOVI, Register.R14, new Immediate(varLocation)));
        Memory varMemory = new Memory(Register.R15, new Immediate(varLocation));
        instructions.add(new Instruction(MOVM, r, varMemory));
      }

      if (!isRegister) {
        VariableLocation varLocation = initVariable(val);
//                instructions.add(new Instruction(MOVI, Register.R12, new Immediate(varLocation)));
        Memory varMemory = new Memory(Register.R15, new Immediate(varLocation));
        instructions.add(new Instruction(MOV, varMemory, r));
      }
    } else {
      throw new ParserException("Incorrect syntax at statement " + lineNumber);
    }

    return instructions;
  }

  /**
   * Add assign and arithmetic instructions
   *
   * @param tokens - ArrayList of string from splitted line
   * @return object to be moved into first value
   */
  private static Object addAssignInstruction(ArrayList<String> tokens, int lineNumber) throws ParserException {
    Stack<Object> values = new Stack<>();
    Stack<Token> operands = new Stack<>();

    int size = tokens.size();
    for (int i = 0; i < size; ++i) {
      String s = tokens.get(i);
//            System.out.println(tokens.get(i));
      if (s.equals("")) {
        continue;
      } else if (s.equals("(")) {
        operands.push(Token.LB);
      } else if (s.equals(")")) {
        while (!operands.peek().equals(Token.LB))
          values.push(applyOp(operands.pop(), values.pop(), values.pop()));
        operands.pop();
      } else if (s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/")) {
        while (!operands.empty() && Token.hasPrecedence(Token.getToken(s), operands.peek()))
          values.push(applyOp(operands.pop(), values.pop(), values.pop()));
        operands.push(Token.getToken(s));
      } else if (isNumeric(s)) {
        values.push(Integer.parseInt(s));
      } else if (Register.isRegister(s) != -1) {
        int index = Register.isRegister(s);
        Register r = Register.getRegister(index);
        if (Register.isReserved(r))
          throw new ParserException("Cannot use reserved register");
        values.push(r);
      } else if (s.matches("[A-Za-z][A-Za-z0-9]*")) {
        values.push(s);
      } else {
        throw new ParserException("Incorrect syntax at statement " + lineNumber);
      }
    }

    // Apply operations to remaining values and operands in stack
    while (!operands.empty())
      values.push(applyOp(operands.pop(), values.pop(), values.pop()));

    return values.pop();
  }

  /**
   * Apply operation, add instruction and return result as object
   *
   * @param token - operand
   * @param val1  - value 1
   * @param val2  - value 2
   * @return object to be pushed into equation
   */
  public static Object applyOp(Token token, Object val1, Object val2) throws ParserException {
    if (val1 instanceof Integer && val2 instanceof Integer) {
      Integer res = 0;
      switch (token.getKeyword()) {
        case "+":
          res = ((Integer) val2 + (Integer) val1);
          break;
        case "-":
          res = ((Integer) val2 - (Integer) val1);
          break;
        case "*":
          res = ((Integer) val2 * (Integer) val1);
          break;
        case "/":
          res = ((Integer) val2 / (Integer) val1);
          break;
      }
      if (res < 0) {
        throw new ParserException("Negative numbers not supported");
      } else {
        return res;
      }
    } else if (val2 instanceof Register && val1 instanceof Integer) {
      // example: R1 (val2) - 5 (val1)
      Register r = Register.R14;
      switch (token.getKeyword()) {
        case "+":
          instructions.add(new Instruction(ADDI, r, (Register) val2, new Immediate((int) val1)));
          break;
        case "-":
          instructions.add(new Instruction(SUBI, r, (Register) val2, new Immediate((int) val1)));
          break;
        case "*":
          instructions.add(new Instruction(MULI, r, (Register) val2, new Immediate((int) val1)));
          break;
        case "/":
          instructions.add(new Instruction(DIVI, r, (Register) val2, new Immediate((int) val1)));
          break;
      }
      return r;
    } else if (val1 instanceof Register && val2 instanceof Integer) {
      // example: 5 (val2) - R1 (val1)
      Register r = Register.R14;
      switch (token.getKeyword()) {
        case "+":
          instructions.add(new Instruction(ADDI, r, (Register) val1, new Immediate((int) val2)));
          break;
        case "*":
          instructions.add(new Instruction(MULI, r, (Register) val1, new Immediate((int) val2)));
          break;
        case "-":
          instructions.add(new Instruction(MOVI, r, new Immediate((int) val2)));
          instructions.add(new Instruction(SUB, r, r, (Register) val1));
          break;
        case "/":
          instructions.add(new Instruction(MOVI, r, new Immediate((int) val2)));
          instructions.add(new Instruction(DIV, r, r, (Register) val1));
          break;
      }
      return r;
    } else if (val1 instanceof Register && val2 instanceof Register) {
      Register r = Register.R14;
      switch (token.getKeyword()) {
        case "+":
          instructions.add(new Instruction(ADD, r, (Register) val2, (Register) val1));
          break;
        case "-":
          instructions.add(new Instruction(SUB, r, (Register) val2, (Register) val1));
          break;
        case "*":
          instructions.add(new Instruction(MUL, r, (Register) val2, (Register) val1));
          break;
        case "/":
          instructions.add(new Instruction(DIV, r, (Register) val2, (Register) val1));
          break;
      }
      return r;
    } else {
      if (val1 instanceof String && ((String) val1).matches("[A-Za-z][A-Za-z0-9]*")) {
        Register r = Register.R14;
        VariableLocation varLocation = initVariable(val1.toString());
//                instructions.add(new Instruction(MOVI, r, new Immediate(varLocation)));
        Memory varMemory = new Memory(Register.R15, new Immediate(varLocation));
        instructions.add(new Instruction(MOVM, r, varMemory));
        val1 = r;
      }
      if (val2 instanceof String && ((String) val2).matches("[A-Za-z][A-Za-z0-9]*")) {
        Register r = Register.R13;
        VariableLocation varLocation = initVariable(val2.toString());
//                instructions.add(new Instruction(MOVI, r, new Immediate(varLocation)));
        Memory varMemory = new Memory(Register.R15, new Immediate(varLocation));
        instructions.add(new Instruction(MOVM, r, varMemory));
        val2 = r;
      }
      return applyOp(token, val1, val2);
    }
  }

  /**
   * Check if string is a number or not
   *
   * @param s
   * @return whether string is number or not
   */
  public static boolean isNumeric(String s) {
    NumberFormat formatter = NumberFormat.getInstance();
    ParsePosition pos = new ParsePosition(0);
    formatter.parse(s, pos);
    return s.length() == pos.getIndex();
  }

  public static VariableLocation initVariable(String varName) {
    if (variables.containsKey(varName)) {
      return variables.get(varName);
    } else {
      VariableLocation i = new VariableLocation(variables.size() * LINE_SIZE);
      variables.put(varName, i);
      return i;
    }
  }

  public static HashMap<String, VariableLocation> getVariables() {
    return variables;
  }

  public static void setVariables(HashMap<String, VariableLocation> variables) {
    Parser.variables = variables;
  }

  public static ArrayList<String> convertInstructionsToString(ArrayList<Instruction> instructions) {
    ArrayList<String> strarr = new ArrayList<>();
    for (Instruction in : instructions) {
      strarr.add(in.toString());
    }
    return strarr;
  }

  public static ArrayList<String> parseArithmetic(String infix, int lineNumber) throws ParserException {
    // Convert infix to postfix
    List<String> tokens = Arrays.asList(infix.split(" "));
    ArrayList<String> outputQueue = new ArrayList<>();
    Stack<String> opStack = new Stack<>();
    for (String s : tokens) {
      if (s.matches(NUMBER_PATTERN + "|" + IDENTIFIER_PATTERN + "(?:\\[\\d+])?")) {
        outputQueue.add(s);
      } else if (s.matches(ADDITIVE_OPERATOR)) {
        while (!opStack.empty()
            && opStack.peek().matches(ADDITIVE_OPERATOR + "|" + MULTIPLICATIVE_OPERATOR)
            && !opStack.peek().matches("\\("))
          outputQueue.add(opStack.pop());
        opStack.push(s);
      } else if (s.matches(MULTIPLICATIVE_OPERATOR)) {
        while (!opStack.empty()
            && opStack.peek().matches(MULTIPLICATIVE_OPERATOR)
            && !opStack.peek().matches("\\("))
          outputQueue.add(opStack.pop());
        opStack.push(s);
      } else if (s.matches("\\(")) {
        opStack.push(s);
      } else if (s.matches("\\)")) {
        boolean match = false;
        while (!opStack.empty() && !(match = opStack.peek().matches("\\(")))
          outputQueue.add(opStack.pop());
        if (match) opStack.pop();
        else throw new ParserException("Mismatched parentheses at line " + lineNumber);
      }
    }
    while (!opStack.empty()) {
      String s = opStack.pop();
      if (s.matches("[()]")) throw new ParserException("Mismatched parentheses at line " + lineNumber);
      outputQueue.add(s);
    }

    // Contract operation between numbers (e.g. a + 1 + 2 becomes a + 3)
    Stack<ArrayList<String>> contractedStack = new Stack<>();
    for (String token : outputQueue) {
      if (Pattern.matches(NUMBER_PATTERN + "|" + IDENTIFIER_PATTERN + "(?:\\[\\d+\\])?", token)) {
        ArrayList<String> a = new ArrayList<>();
        a.add(token);
        contractedStack.push(a);
      } else {
        ArrayList<String> second = contractedStack.pop();
        ArrayList<String> first = contractedStack.pop();
        if (first.size() == 1 && Pattern.matches(NUMBER_PATTERN, first.get(0)) &&
            second.size() == 1 && Pattern.matches(NUMBER_PATTERN, second.get(0))) {
          int result = 0, a = Integer.parseInt(first.get(0)), b = Integer.parseInt(second.get(0));
          switch (token) {
            case "+":
              result = a + b;
              break;
            case "-":
              result = a - b;
              break;
            case "*":
              result = a * b;
              break;
            case "/":
              result = a / b;
              break;
          }
          ArrayList<String> arr = new ArrayList<>();
          arr.add(Integer.toString(result));
          contractedStack.push(arr);
        } else {
          first.addAll(second);
          first.add(token);
          contractedStack.push(first);
        }
      }
    }
    return contractedStack.pop();
  }
}
