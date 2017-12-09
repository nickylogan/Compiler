package InstructionNodes;

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
        InstructionNode root = new MultipleInstructionNode();
        InstructionNode parent = root;
        System.out.println(parent);
        for (String s : lines) {
            s = s.replaceAll(" +", "");
            if (s.matches("([A-Za-z][A-Za-z0-9]*+)(=)([0-9]+|[A-Za-z][A-Za-z0-9]*+)")) {
                SimpleInstructionNode ins = new SimpleInstructionNode();
                System.out.println(ins);
                System.out.println((MultipleInstructionNode) parent);
                ((MultipleInstructionNode) parent).addInstruction(ins);
            } else if (s.matches("(if)[(]([0-9]+|[A-Za-z][A-Za-z0-9]*+)(<|<=|>=|>|==)([0-9]+|[A-Za-z][A-Za-z0-9]*+)[)](then)")) {
                IfNode ifNode = new IfNode();
                ((MultipleInstructionNode) parent).addInstruction(ifNode);
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
        ArrayList<String> arr = root.parse();
        for (String s : arr)
            System.out.println(s);
    }
}