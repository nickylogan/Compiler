package statement;

import main.Instruction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static main.Main.getString;

public class Test {
    public void test() {
        String path = "src/input.txt";
        File file = new File(path);
        String code = "";
        try {
            code = getString(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String lines[] = code.split("\\r?\\n");
        StatementNode root = new MultipleStatementNode();
        StatementNode parent = root;
        System.out.println(parent);
        for (String s : lines) {
            s = s.replaceAll(" +", "");
            if (s.matches("([A-Za-z][A-Za-z0-9]*+)(=)([0-9]+|[A-Za-z][A-Za-z0-9]*+)")) {
                SimpleStatementNode ins = new SimpleStatementNode(s,0);
                System.out.println(ins);
                System.out.println((MultipleStatementNode) parent);
                ((MultipleStatementNode) parent).addStatement(ins);
            } else if (s.matches("(if)[(]([0-9]+|[A-Za-z][A-Za-z0-9]*+)(<|<=|>=|>|==)([0-9]+|[A-Za-z][A-Za-z0-9]*+)[)](then)")) {
                IfNode ifNode = new IfNode(s,0);
                ((MultipleStatementNode) parent).addStatement(ifNode);
                parent = ifNode.getTrueChild();
            } else if (s.matches("else")) {
                parent = ((IfNode) parent.getParent()).createFalseChild();
            } else if (s.matches("endif")) {
                while (!(parent instanceof IfNode)) {
                    parent = parent.getParent();
                }
                parent = parent.getParent();
            }
        }
//        ArrayList<Instruction> arr = root.parse();
//        for (Instruction s : arr)
//            System.out.println(s);
    }
}