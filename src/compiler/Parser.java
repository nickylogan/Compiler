package compiler;

import java.util.*;

import com.florianingerl.util.regex.Pattern;
import com.sun.istack.internal.NotNull;
import main.Program;
import compiler.statement.*;

import static compiler.Operator.*;

public class Parser {
  private static HashMap<String, VariableLocation> variables = new HashMap<>();
  private static ArrayList<Instruction> instructions = new ArrayList<>();
  public final static int LINE_SIZE = 4;
  public final static int LINE_INIT_POS = 0;
  private static MultipleStatementNode root = new MultipleStatementNode();


  private static SymbolTable symbolTable = new SymbolTable();

  /**
   * String1: nodeID
   * String2: parentNodeID
   */
  private static HashMap<String, String> nodeTreeTable = new HashMap<>();

  /**
   * Instead of using exceptions, errors are stored here.
   */
  private static ArrayList<String> errors = new ArrayList<>();

  //String patterns
  public static final String KEYWORDS = "var|if|else|endif|while|endwhile|endprogram|break|continue|true|false";
  public static final String IDENTIFIER_PATTERN = "[A-Za-z_][A-Za-z0-9_]*";
  public static final String NUMBER_PATTERN = "\\d+";
  private static final String SUBSCRIPT_PATTERN = "\\[\\d+\\]";
  private static final String LVALUE_PATTERN = "[A-Za-z_][A-Za-z0-9_]*(?:\\[\\d+\\])?";
  private static final String ARITHMETIC_OPERATOR = "[-+*/]";
  private static final String COMPARISON_OPERATOR = "[<>]=?|==|!=";
  private static final String LOGICAL_OPERATOR = "&&|\\|\\|";
  private static final String BOOLEAN_LITERAL = "true|false";
  private static final String BINARY_OPERATOR = "(?'LOGICALOP')|(?'COMPARISONOP')|(?'ARITHMETICOP')";
  private static final String OPERAND_PATTERN = "(?'VALUE')|(?'EXPR')|(?:\\(\\s(?'EXPR')|(?'VALUE')|(?'OPERAND')\\s\\))";
  private static final String EXPR_PATTERN = "(?'OPERAND')|(?'OPERAND')(?:\\s(?'BINARYOP')\\s(?'OPERAND'))+|!\\s(?'OPERAND')";
  private static final String VALUE_PATTERN = "(?'NUMBER')|(?'LVALUE')|(?'BOOLEAN')";
  //  private static String RVALUE_PATTERN = "(?'VALUE')|(?'SUM')|(?'PRODUCT')|\\(\\s(?'RVALUE')\\s\\)";
  private static String GENERAL_REGEX = "" +
                                        "(?x)(?(DEFINE)" +
                                        "(?<LVALUE>" + LVALUE_PATTERN + ")" +
                                        "(?<NUMBER>" + NUMBER_PATTERN + ")" +
                                        "(?<IDENTIFIER>" + IDENTIFIER_PATTERN + ")" +
                                        "(?<SUBSCRIPT>" + SUBSCRIPT_PATTERN + ")" +
                                        "(?<ARITHMETICOP>" + ARITHMETIC_OPERATOR + ")" +
                                        "(?<COMPARISONOP>" + COMPARISON_OPERATOR + ")" +
                                        "(?<LOGICALOP>" + LOGICAL_OPERATOR + ")" +
                                        "(?<BINARYOP>" + BINARY_OPERATOR + ")" +
                                        "(?<BOOLEAN>" + BOOLEAN_LITERAL + ")" +
                                        "(?<VALUE>" + VALUE_PATTERN + ")" +
                                        "(?<OPERAND>" + OPERAND_PATTERN + ")" +
                                        "(?<EXPR>" + EXPR_PATTERN + ")" +
                                        ")";

  public static Pattern INSTANTIATION_PATTERN = Pattern.compile(
      GENERAL_REGEX +
      "(var)\\s(?'LVALUE')"
  );
  public static Pattern ASSIGNMENT_PATTERN = Pattern.compile(
      GENERAL_REGEX +
      "(?'LVALUE')\\s=\\s(?'EXPR')"
  );
  public static Pattern DIRECT_ASSIGNMENT_PATTERN = Pattern.compile(
      GENERAL_REGEX +
      "(var)\\s(?'IDENTIFIER')\\s=\\s(?'EXPR')"
  );
  public static Pattern IF_PATTERN = Pattern.compile(
      GENERAL_REGEX +
      "(if)\\s\\(\\s(?'EXPR')\\s\\)"
  );
  public static Pattern WHILE_PATTERN = Pattern.compile(
      GENERAL_REGEX +
      "(while)\\s\\(\\s(?'EXPR')\\s\\)"
  );

  // For node ID purposes
  private static int mulCount = 0;
  private static int ifCount = 0;
  private static int whileCount = 0;
  private static int singleCount = 0;

  // count getters
  public static int getIfCount() {
    return ifCount++;
  }

  public static int getMulCount() {
    return mulCount++;
  }

  public static int getWhileCount() {
    return whileCount++;
  }

  public static int getSingleCount() {
    return singleCount++;
  }

  private enum LineType {
    ASSIGNMENT,
    INSTANTIATION,
    DIRECT_ASSIGNMENT,
    IF, ELSE, ENDIF,
    WHILE, ENDWHILE,
    BREAK, CONTINUE,
    NULL
  }

  private static LineType getLineType(String line) {
//    System.out.println("Ins: " + Pattern.matches(INSTANTIATION_PATTERN.toString(),line));
//    System.out.println("Ass: " + Pattern.matches(ASSIGNMENT_PATTERN.toString(),line));
//    System.out.println("Dir: " + Pattern.matches(DIRECT_ASSIGNMENT_PATTERN.toString(),line));
//    System.out.println("If: " + Pattern.matches(IF_PATTERN.toString(),line));
//    System.out.println("While: " + Pattern.matches(WHILE_PATTERN.toString(),line));
    if (Pattern.matches(ASSIGNMENT_PATTERN.toString(), line)) return LineType.ASSIGNMENT;
    else if (Pattern.matches(INSTANTIATION_PATTERN.toString(), line)) return LineType.INSTANTIATION;
    else if (Pattern.matches(DIRECT_ASSIGNMENT_PATTERN.toString(), line)) return LineType.DIRECT_ASSIGNMENT;
    else if (Pattern.matches(IF_PATTERN.toString(), line)) return LineType.IF;
    else if (Pattern.matches(WHILE_PATTERN.toString(), line)) return LineType.WHILE;
    else if (line.matches("else")) return LineType.ELSE;
    else if (line.matches("endif")) return LineType.ENDIF;
    else if (line.matches("endwhile")) return LineType.ENDWHILE;
    else if (line.matches("break")) return LineType.BREAK;
    else if (line.matches("continue")) return LineType.CONTINUE;
    else return LineType.NULL;
  }

  private static void reset() {
    mulCount = ifCount = whileCount = singleCount = 0;
    if (instructions != null) instructions.clear();
    root = new MultipleStatementNode();
    root.setNodeID("MUL" + mulCount++);
    symbolTable.clear();
    errors.clear();
    nodeTreeTable.clear();
  }

  public static ArrayList<Instruction> compile(ArrayList<String> lines) throws ParserException {
    reset();

    MultipleStatementNode parent = root;
    ArrayList<InstructionOffset> instructionOffsets = new ArrayList<>();

    // === FORMAT CLEANUP ===
    for (int i = 0; i < lines.size(); ++i) {
      String line = lines.get(i);

      // ignore comments
      if(line.matches("//[^\n]*|/\\*(.|\\R)*?\\*/")) {
        lines.set(i, "");
        continue;
      }

      // add space around operators and brackets
      line = line.replaceAll("(?=[-+*/()<>!]|(?<![<>=!])=)|(?<=[-+*/()]|[<>=!](?!=))|(?=&&|\\|\\|)|(?<=&&|\\|\\|)", " ");

      // remove space in subscripts
      line = line.replaceAll("(?<=\\[)\\s+|\\s+(?=])", "");

      // replace excess whitespace with single space character
      // remove leading and trailing whitespace
      line = line.replaceAll("\\s+", " ").trim();
      lines.set(i, line);
    }

    int codeSize = lines.size();
    boolean stopped = false;

    for (int i = 0; i < codeSize; ++i) {
      String line = lines.get(i);
//      System.out.println(line);
      LineType lineType = getLineType(line);
//      System.out.println(lineType);
      if (line.isEmpty()) continue;

      if (lineType == LineType.ASSIGNMENT) {
//        System.out.println("assignment");
        SingleStatementNode statementNode = new AssignmentNode(line, i + 1, false);
        parent.addStatement(statementNode);

      } else if (lineType == LineType.INSTANTIATION) {
//        System.out.println("instantiation");
        SingleStatementNode statementNode = new InstantiationNode(line, i + 1);
        parent.addStatement(statementNode);

      } else if (lineType == LineType.DIRECT_ASSIGNMENT) {
//        System.out.println("direct assignment");
        SingleStatementNode statementNode = new AssignmentNode(line, i + 1, true);
        parent.addStatement(statementNode);

      } else if (lineType == LineType.IF) {
//				System.out.println("if");
        IfNode ifNode = new IfNode(line, i + 1);
        parent.addStatement(ifNode);
        parent = ifNode.getTrueChild();

      } else if (lineType == LineType.ELSE) {
//				System.out.println("else");
        if (!(parent.getParent() instanceof IfNode)) errors.add("Else compiler.statement without if at line " + (i + 1));
        else parent = ((IfNode) parent.getParent()).createFalseChild();

      } else if (lineType == LineType.ENDIF) {
//				System.out.println("endif");
        StatementNode st = parent;
        while (st != null && !(st instanceof IfNode)) {
          st = st.getParent();
        }
        if (st == null) {
          errors.add("endif compiler.statement without if at line " + (i + 1));
        } else parent = (MultipleStatementNode) st.getParent();

      } else if (lineType == LineType.WHILE) {
//                System.out.println("while");
        WhileNode whileNode = new WhileNode(line, i + 1);
        parent.addStatement(whileNode);
        parent = whileNode.getChildren();

      } else if (lineType == LineType.ENDWHILE) {
//                System.out.println("endwhile");
        StatementNode st = parent;
        while (st != null && !(st instanceof WhileNode)) {
          st = st.getParent();
        }
        if (st == null) {
          errors.add("endwhile compiler.statement without while at line " + (i + 1));
        } else parent = (MultipleStatementNode) st.getParent();

      } else if (lineType == LineType.BREAK) {
//                System.out.println("break");
        StatementNode st = parent;
        while (st != null && !(st instanceof WhileNode)) {
          st = st.getParent();
        }
        if (st == null) {
          errors.add("break compiler.statement without while at line " + (i + 1));
        } else parent.addStatement(new KeywordStatement(Keyword.BREAK, i + 1));

      } else if (lineType == LineType.CONTINUE) {
//                System.out.println("continue");
        StatementNode st = parent;
        while (st != null && !(st instanceof WhileNode)) {
          st = st.getParent();
        }
        if (st == null) {
          errors.add("continue compiler.statement without while at line " + (i + 1));
        } else parent.addStatement(new KeywordStatement(Keyword.CONTINUE, i + 1));

      } else if (line.matches("endprogram")) {
        stopped = true;
        break;

      } else errors.add("Invalid syntax at line " + (i + 1));
    }


    if (parent.getParent() != null) {
      throw new ParserException("Missing end of file");
    }

    if (!errors.isEmpty()) {
      System.out.println("error 1");
      StringBuilder sb = new StringBuilder();
      for (String err : errors)
        sb.append(err).append("\n");
      throw new ParserException(sb.toString());
    }

    ArrayList<InstructionOffset> temp = root.parse();

//    System.out.println(temp);

    if (!errors.isEmpty()) {
      System.out.println("error 2");
      StringBuilder sb = new StringBuilder();
      for (String err : errors)
        sb.append(err).append("\n");
      throw new ParserException(sb.toString());
    }

    Instruction init = new Instruction(Operator.MOVI, Register.R0, new Immediate(0));
    instructionOffsets.add(new InstructionOffset(init, 0));
    for (InstructionOffset io : temp) {
      io.setOffset(io.getOffset() + 1);
      instructionOffsets.add(io);
    }
    instructionOffsets.add(new InstructionOffset(new Instruction(HALT), instructionOffsets.size()));

//    ArrayList<Instruction> instructions = new ArrayList<>();
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

    int i = instructions.size() * 4;
    for (String key : symbolTable.keySet()) {
      HashMap<String, Symbol> hs = symbolTable.get(key);
      for (String scope : hs.keySet()) {
        Symbol s = hs.get(scope);
        s.getLocation().setValue(i);
        i += s.getSize();
      }
    }
    System.out.println(symbolTable.toString());

    return instructions;
  }

  public static boolean insertVariable(String varName, SymbolType type, int size, String nodeID, int lineNumber) {
    if (!symbolTable.insert(varName, type, size, nodeID, lineNumber)) {
      errors.add(varName + " already declared in this scope (at line " + lineNumber + ")");
      return false;
    }
    return true;
  }

  public static Symbol lookupVariable(String varName, String nodeID) {
    Symbol symbol = null;
    String parentID = nodeID;
    while (parentID != null && (symbol = symbolTable.lookup(varName, parentID)) == null)
      parentID = nodeTreeTable.get(parentID);
    return symbol;
  }

  public static ArrayList<String> convertInstructionsToString(ArrayList<Instruction> instructions) {
    ArrayList<String> strarr = new ArrayList<>();
    for (Instruction in : instructions) {
      strarr.add(in.toString());
    }
    return strarr;
  }

  private enum TokenOp {
    NOT("!", 3, 1),
    ADD("+", 6, 0), SUB("-", 6, 0), MUL("*", 5, 0), DIV("/", 5, 0),
    LT("<", 9, 0), LE("<=", 9, 0), GT(">", 9, 0), GE(">=", 9, 0),
    EQ("==", 10, 0), NEQ("!=", 10, 0),
    AND("&&", 14, 0), OR("||", 15, 0);
    private int precedence;
    private int associativity; // left: 0, right: 1
    private String representation;

    static TokenOp getToken(String op) {
      TokenOp[] tokens = values();
      for (TokenOp op2 : tokens) {
        if (op2.representation.equals(op)) return op2;
      }
      return null;
    }

    static boolean higherPrecedence(String op1, String op2) {
      TokenOp to1 = getToken(op1);
      TokenOp to2 = getToken(op2);
      return to1 != null && to2 != null && to1.precedence < to2.precedence;
    }

    static boolean isLeftAssociative(String op) {
      TokenOp op1 = getToken(op);
      return op1 != null && op1.associativity == 0;
    }

    static boolean equalPrecedence(String op1, String op2) {
      TokenOp to1 = getToken(op1);
      TokenOp to2 = getToken(op2);
      return to1 != null && to2 != null && to1.precedence == to2.precedence;
    }

    TokenOp(String representation, int precedence, int associativity) {
      this.precedence = precedence;
      this.associativity = associativity;
      this.representation = representation;
    }
  }

  /**
   * Converts infix arithmetic notation into RPN
   *
   * @return RPN of the passed infix notation
   */
  public static ArrayList<String> parseArithmetic(String infix, int lineNumber) {
    // Convert infix to postfix
    List<String> tokens = Arrays.asList(infix.split(" "));
    ArrayList<String> outputQueue = new ArrayList<>();
    Stack<String> opStack = new Stack<>();
    ArrayList<String> tempErrors = new ArrayList<>();
    tempErrors.add("#ERROR");

    for (String s : tokens) {
      if (s.matches(NUMBER_PATTERN + "|" + IDENTIFIER_PATTERN + "(?:\\[\\d+])?")) {
        outputQueue.add(s);
      } else if (s.matches("[-+*/]|[<>]=?|==|!=|&&|\\|\\||!")) {
        while (!opStack.empty()
               && (TokenOp.higherPrecedence(opStack.peek(), s)
                   || TokenOp.equalPrecedence(opStack.peek(), s)
                      && TokenOp.isLeftAssociative(opStack.peek())
               )
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
        else tempErrors.add("Unmatched ')' at line " + lineNumber);
      }
    }
    while (!opStack.empty()) {
      String s = opStack.pop();
      if (s.matches("[()]")) {
        tempErrors.add("Mismatched parentheses at line " + lineNumber);
      }
      outputQueue.add(s);
    }

    // Contract operation between numbers (e.g. a + 1 + 2 becomes a + 3)
    Stack<ArrayList<String>> contractedStack = new Stack<>();
    for (String token : outputQueue) {
      if (Pattern.matches(NUMBER_PATTERN + "|" + IDENTIFIER_PATTERN + "(?:\\[\\d+\\])?", token)) {
        ArrayList<String> a = new ArrayList<>();
        switch (token) {
          case "true": a.add("1");
            break;
          case "false": a.add("0");
            break;
          default: a.add(token);
            break;
        }
        contractedStack.push(a);
      } else if (token.equals("!")) {
        ArrayList<String> operand = contractedStack.pop();
        if (operand.size() == 1 && Pattern.matches("-?" + NUMBER_PATTERN, operand.get(0))) {
          int a = Integer.parseInt(operand.get(0));
          ArrayList<String> arr = new ArrayList<>();
          a = a == 0 ? 1 : 0;
          arr.add(Integer.toString(a));
          contractedStack.push(arr);
        } else {
          operand.add(token);
          contractedStack.push(operand);
        }
      } else {
        ArrayList<String> second = contractedStack.pop();
        ArrayList<String> first = contractedStack.pop();
        if (first.size() == 1 && Pattern.matches("-?" + NUMBER_PATTERN, first.get(0)) &&
            second.size() == 1 && Pattern.matches("-?" + NUMBER_PATTERN, second.get(0))) {
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
            case ">=":
              result = a >= b ? 1 : 0;
              break;
            case ">":
              result = a > b ? 1 : 0;
              break;
            case "<=":
              result = a <= b ? 1 : 0;
              break;
            case "<":
              result = a < b ? 1 : 0;
              break;
            case "==":
              result = a == b ? 1 : 0;
              break;
            case "!=":
              result = a != b ? 1 : 0;
              break;
            case "&&":
              result = a * b == 0 ? 0 : 1;
              break;
            case "||":
              result = a == 0 && b == 0 ? 0 : 1;
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
    if (tempErrors.size() > 1) {
      return tempErrors;
    } else {
//      System.out.println(contractedStack.peek());
      return contractedStack.pop();
    }
  }

  /**
   * Parses reverse polish notation into Instructions
   */
  public static ArrayList<Instruction> parseRPNtoInstruction(
      @NotNull ArrayList<String> RPN,
      int lineNumber,
      @NotNull String nodeID,
      @NotNull ArrayList<String> tempErrors,
      int currentRegister) {
    ArrayList<Instruction> instructions = new ArrayList<>();

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
        Symbol s = Parser.lookupVariable(token, nodeID);
        if (s == null)
          tempErrors.add("Cannot resolve symbol '" + token + "' at line " + lineNumber);
        else {
          instructions.add(
              new Instruction(
                  Operator.MOVI,
                  Register.R15,
                  s.getLocation()
              )
          );

          // Copy variable data into RX
          // MOVM RX [R15 + 0]
          instructions.add(
              new Instruction(
                  Operator.MOVM,
                  Register.getRegister(currentRegister--),
                  new Memory(Register.R15, new Immediate(0))
              )
          );
        }
      } else if (token.matches(Parser.IDENTIFIER_PATTERN + "\\[" + Parser.NUMBER_PATTERN + "]")) {
        String sArr[] = token.split("(?<=[\\[\\]])|(?=[\\[\\]])");
        String arrName = sArr[0];
        int index = Integer.parseInt(sArr[2]) * LINE_SIZE;
        Symbol s = Parser.lookupVariable(arrName, nodeID);
        if (s == null)
          tempErrors.add("Cannot resolve symbol '" + arrName + "' at line " + lineNumber);
        else {
          instructions.add(
              new Instruction(
                  Operator.MOVI,
                  Register.R15,
                  s.getLocation()
              )
          );

          // Copy array element data into RX
          // MOVM RX [R15 + arrayIndex]
          instructions.add(
              new Instruction(
                  Operator.MOVM,
                  Register.getRegister(currentRegister--),
                  new Memory(Register.R15, new Immediate(index))
              )
          );
        }
      } else if (token.matches("[-+*/]")) {
        ++currentRegister;
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
                Register.getRegister(currentRegister)
            )
        );
      } else if (token.matches("[<>]=?|==|!=")) {
        ++currentRegister;
//        System.out.println(token + ", reg: "+ currentRegister);
        Operator operator = null;
        int first = 1, second = 0;
        // set operator
        switch (token) {
          case "==": case "!=": operator = Operator.JE; break;
          case "<": case ">=": operator = Operator.JLT; break;
          case ">": case "<=": operator = Operator.JGT; break;
        }

        if (token.matches("[!<>]=")) {first = 0; second = 1;}

        //    J_ RX, RY, x
        //    MOVI RX, second
        //    JMP y
        // x: MOVI RX, first
        // y: ...
        instructions.add(
            new Instruction(
                operator,
                Register.getRegister(currentRegister + 1),
                Register.getRegister(currentRegister),
                new Immediate(instructions.size() + 3)
            )
        );
        instructions.add(
            new Instruction(
                Operator.MOVI,
                Register.getRegister(currentRegister + 1),
                new Immediate(second)
            )
        );
        instructions.add(
            new Instruction(
                Operator.JMP,
                new Immediate(instructions.size() + 2)
            )
        );
        instructions.add(
            new Instruction(
                Operator.MOVI,
                Register.getRegister(currentRegister + 1),
                new Immediate(first)
            )
        );
//        ++currentRegister;
      } else if (token.equals("!")) {
        ++currentRegister;
//        System.out.println(token + ", reg: "+ currentRegister);
        //    JE RX, R0, x
        //    MOVI RX, 0
        //    JMP y
        // x: MOVI RX, 1
        // y: ...
        instructions.add(
            new Instruction(
                Operator.JE,
                Register.getRegister(currentRegister),
                Register.R0,
                new Immediate(instructions.size() + 3)
            )
        );
        instructions.add(
            new Instruction(
                Operator.MOVI,
                Register.getRegister(currentRegister),
                new Immediate(0)
            )
        );
        instructions.add(
            new Instruction(
                Operator.JMP,
                new Immediate(instructions.size() + 2)
            )
        );
        instructions.add(
            new Instruction(
                Operator.MOVI,
                Register.getRegister(currentRegister),
                new Immediate(1)
            )
        );
      } else if (token.matches("&&|\\|\\|")) {
        ++currentRegister;
//        System.out.println(token + ", reg: "+ currentRegister);
        Operator op = null;
        switch (token) {
          case "&&": op = Operator.MUL; break;
          case "||": op = Operator.ADD; break;
        }
        // && =>
        //    MUL RX RX RY
        //    JNE R14 RO x
        //    MOVI RX 1
        // x: ...

        // || =>
        //    ADD RX RX RY
        //    JNE R14 RO x
        //    MOVI RX 1
        // x: ...
        instructions.add(
            new Instruction(
                op,
                Register.getRegister(currentRegister + 1),
                Register.getRegister(currentRegister + 1),
                Register.getRegister(currentRegister)
            )
        );
        instructions.add(
            new Instruction(
                Operator.JE,
                Register.getRegister(currentRegister + 1),
                Register.R0,
                new Immediate(instructions.size() + 2)
            )
        );
        instructions.add(
            new Instruction(
                Operator.MOVI,
                Register.getRegister(currentRegister + 1),
                new Immediate(1)
            )
        );
      }
//      System.out.println(token + ", reg: "+ currentRegister);
    }
    return instructions;
  }

  public static void createRelationship(String childID, String parentID) {
    nodeTreeTable.put(childID, parentID);
  }

  public static void addError(String error) {
    errors.add(error);
  }

  public static void addErrorList(ArrayList<String> errorList) {
    errors.addAll(errorList);
  }

  public static Program createProgram(String fileName){
    return new Program(instructions, symbolTable, fileName);
  }
}
