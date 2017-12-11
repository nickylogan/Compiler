//package main;
//
//import statement.*;
//
//import javax.swing.plaf.nimbus.State;
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//
//import static main.Operator.*;
//import static main.Register.*;
//
//public class Test {
//    private static MultipleStatementNode root = new MultipleStatementNode();
//    private static MultipleStatementNode parent = root;
//
//    public static void mmain(String[] args) {
//
//        ArrayList<String> lines = new ArrayList<>();
//        String pseudocode = "";
//        try {
//            String path = "src/main/input.txt";
//            File file = new File(path);
//            FileReader fileReader = new FileReader(file);
//            BufferedReader bufferedReader = new BufferedReader(fileReader);
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                lines.add(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        ArrayList<InstructionOffset> instructions;
//
//        int size = lines.size();
//        for (int i = 0; i < lines.size(); ++i) {
//            String line = lines.get(i);
//            line = line.replaceAll(" +|[;]|\\t", "");
//            System.out.println(line);
//            if (line.matches("[A-Za-z][A-Za-z0-9]*=([A-Za-z][A-Za-z0-9]*|[0-9]+|[-+*/()])+")) {
//                System.out.println("assignment");
//                SimpleStatementNode statementNode = new SimpleStatementNode(line, i + 1);
//                parent.addStatement(statementNode);
//            } else if (line.matches("if[(](([0-9]+|[A-Za-z][A-Za-z0-9]*+)(<|<=|>=|>|==|!=)([0-9]+|[A-Za-z][A-Za-z0-9]*+)|true|false)[)]")) {
//                System.out.println("if");
//                IfNode ifNode = new IfNode(line, i + 1);
//                parent.addStatement(ifNode);
//                parent = ifNode.getTrueChild();
//            } else if (line.matches("else")) {
//                System.out.println("else");
//                parent = ((IfNode) parent.getParent()).createFalseChild();
//            } else if (line.matches("endif")) {
//                System.out.println("endif");
//                StatementNode st = parent;
//                while (st != null && !(st instanceof IfNode)) {
//                    st = st.getParent();
//                }
//                if (st == null) {
//                    throw new ParserException("endif statement without if at line " + (i + 1));
//                }
//                parent = (MultipleStatementNode) st.getParent();
//            } else if (line.matches("while[(](([0-9]+|[A-Za-z][A-Za-z0-9]*)(<|<=|>=|>|==|!=)([0-9]+|[A-Za-z][A-Za-z0-9]*)|true|false)[)]")) {
//                System.out.println("while");
//                WhileNode whileNode = new WhileNode(line, i + 1);
//                parent.addStatement(whileNode);
//                parent = whileNode.getChildren();
//            } else if (line.matches("endwhile")) {
//                System.out.println("endwhile");
//                StatementNode st = parent;
//                while (st != null && !(st instanceof WhileNode)) {
//                    st = st.getParent();
//                }
//                if (st == null) {
//                    throw new ParserException("endwhile statement without while at line " + (i + 1));
//                }
//                parent = (MultipleStatementNode) st.getParent();
//            } else if (line.matches("break")) {
//                System.out.println("break");
//                StatementNode st = parent;
//                while (st != null && !(st instanceof WhileNode)) {
//                    st = st.getParent();
//                }
//                if (st == null) {
//                    throw new ParserException("break statement without while at line " + (i + 1));
//                }
//                parent.addStatement(new KeywordStatement(Keyword.BREAK, i + 1));
//            } else if (line.matches("continue")) {
//                System.out.println("continue");
//                StatementNode st = parent;
//                while (st != null && !(st instanceof WhileNode)) {
//                    st = st.getParent();
//                }
//                if (st == null) {
//                    throw new ParserException("break statement without while at line " + (i + 1));
//                }
//                parent.addStatement(new KeywordStatement(Keyword.BREAK, i + 1));
//            }
//        }
//        instructions = root.parse();
//        for(InstructionOffset io : instructions){
//            System.out.println("[" + io.getOffset() + "] " + io.getInstruction());
//        }
//
////        instructions.size();
//
////        Parser.modifyVarLocations(instructions.size());
//
////        printAssemblyCode(instructions);
////        String s = Mapper.convertToMachineCode(instructions);
////        System.out.print(s);
////        try {
////			writeFile("src/main/output.mcd", s);
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
//    }
//
//    public static String getString(File file) throws IOException {
//        StringBuilder s = new StringBuilder();
//        BufferedReader br = new BufferedReader(new FileReader(file));
//        String curLine = br.readLine();
//        while (curLine != null) {
//            // remove tabs
//            while (curLine.startsWith("\t")) {
//                curLine = curLine.substring(1, curLine.length());
//            }
//            // check if comment or not
//            if (!curLine.startsWith("//")) {
//                s.append(curLine);
//                //if(curLine.endsWith(";"))
//                s.append("\n");
//            }
//            curLine = br.readLine();
//        }
//        br.close();
//        return s.toString();
//    }
//
//    private static void printAssemblyCode(ArrayList<Instruction> instructions) {
//        for (int i = 0; i < instructions.size(); ++i) {
//            Instruction ins = instructions.get(i);
////        	System.out.println("[" + ((i * Parser.LINE_SIZE) + Parser.LINE_INIT_POS) + "] " + ins.toString());
//        }
//    }
//
//    public static void writeFile(String fileName, String text) throws IOException {
//        File file = new File(fileName);
//        BufferedWriter out = new BufferedWriter(new FileWriter(file));
//        out.write(text);
//        out.close();
//    }
//
//    private static void testInstructions(ArrayList<Instruction> instructions) {
//        instructions.add(new Instruction(MOVI, R1, new Immediate(56)));
//        instructions.add(new Instruction(MOVI, R2, new Immediate(10)));
//        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(0)), R2));
//        instructions.add(new Instruction(MOVI, R2, new Immediate(7)));
//        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(4)), R2));
//        instructions.add(new Instruction(MOVI, R2, new Immediate(0)));
//        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(8)), R2));
//        instructions.add(new Instruction(MOVM, R8, new Memory(R1, new Immediate(0))));
//        instructions.add(new Instruction(MOVM, R9, new Memory(R1, new Immediate(4))));
//        instructions.add(new Instruction(JGT, R8, R9, new Immediate(48)));
//        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(8)), R8));
//        instructions.add(new Instruction(JMP, new Immediate(52)));
//        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(8)), R9));
//        instructions.add(new Instruction(HALT));
//        //System.out.println(instructions.get(0).getOperator());
//    }
//}
