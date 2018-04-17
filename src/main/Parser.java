package main;

import java.util.*;

import com.florianingerl.util.regex.Pattern;
import statement.*;

import static main.Operator.*;

public class Parser {
  private static HashMap<String, VariableLocation> variables = new HashMap<>();
  private static ArrayList<Instruction> instructions;
  public final static int LINE_SIZE = 4;
  public final static int LINE_INIT_POS = 0;
  private static MultipleStatementNode root = new MultipleStatementNode();


  private static SymbolTable symbolTable = new SymbolTable();

  /**
   * String1: nodeID
   * String2: parentNodeID
   */
  private static HashMap<String, String> nodeTreeTable;

  /**
   * Instead of using exceptions, errors are stored here.
   */
  private static ArrayList<String> errors;

  //String patterns
  public static final String KEYWORDS = "var|if|else|endif|while|endwhile|endprogram|break|continue";
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

  private static void reset(){
    mulCount = ifCount = whileCount = singleCount = 0;
    if (variables != null) variables.clear();
    if (instructions != null) instructions.clear();
    root = new MultipleStatementNode();
    root.setNodeID("MUL" + mulCount++);
  }

  public static ArrayList<Instruction> compile(ArrayList<String> lines) throws ParserException {
    reset();

    MultipleStatementNode parent = root;
    ArrayList<InstructionOffset> instructionOffsets = new ArrayList<>();

    // === FORMAT CLEANUP ===
    for (int i = 0; i < lines.size(); ++i) {
      String line = lines.get(i);

      // add space around operators and brackets
      line = line.replaceAll("(?=[-+*/()<>!]|(?<![<>=!])=)|(?<=[-+*/()]|[<>=!](?!=))", " ");

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
      LineType lineType = getLineType(line);
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
        if (!(parent.getParent() instanceof IfNode)) errors.add("Else statement without if at line " + (i + 1));
        else parent = ((IfNode) parent.getParent()).createFalseChild();

      } else if (lineType == LineType.ENDIF) {
//				System.out.println("endif");
        StatementNode st = parent;
        while (st != null && !(st instanceof IfNode)) {
          st = st.getParent();
        }
        if (st == null) {
          throw new ParserException("endif statement without if at line " + (i + 1));
        }
        parent = (MultipleStatementNode) st.getParent();

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
          throw new ParserException("endwhile statement without while at line " + (i + 1));
        }
        parent = (MultipleStatementNode) st.getParent();

      } else if (lineType == LineType.BREAK) {
//                System.out.println("break");
        StatementNode st = parent;
        while (st != null && !(st instanceof WhileNode)) {
          st = st.getParent();
        }
        if (st == null) {
          throw new ParserException("break statement without while at line " + (i + 1));
        }
        parent.addStatement(new KeywordStatement(Keyword.BREAK, i + 1));

      } else if (lineType == LineType.CONTINUE) {
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

  public static VariableLocation initVariable(String varName) {
    if (variables.containsKey(varName)) {
      return variables.get(varName);
    } else {
      VariableLocation i = new VariableLocation(variables.size() * LINE_SIZE);
      variables.put(varName, i);
      return i;
    }
  }

  public static void insertVariable(String varName, SymbolType type, int size, String nodeID, int lineNumber) {
    if (!symbolTable.insert(varName, type, size, nodeID, lineNumber))
      errors.add(varName + " already declared in this scope (at line " + lineNumber + ")");
  }

  public static Symbol lookupVariable(String varName, String nodeID) {
    Symbol symbol = null;
    String parentID = nodeID;
    while(parentID != null && (symbol = symbolTable.lookup(varName, parentID)) == null)
      parentID = nodeTreeTable.get(nodeID);
    return symbol;
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

  public static void createRelationship(String childID, String parentID){
    nodeTreeTable.put(childID, parentID);
  }

  public static void addError(String error){
    errors.add(error);
  }

  public static void addErrorList(ArrayList<String> errorList){
    errors.addAll(errorList);
  }

}
