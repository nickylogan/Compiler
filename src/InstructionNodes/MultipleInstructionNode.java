package InstructionNodes;

import java.util.ArrayList;

public class MultipleInstructionNode extends InstructionNode {
    ArrayList<InstructionNode> children;

    public MultipleInstructionNode(){
        children = new ArrayList<>();
    }
    public void addInstruction(InstructionNode element) {
        element.setParent(this);
        children.add(element);
    }

    @Override
    public ArrayList<String> parse() {
        ArrayList<String> arr = new ArrayList<>();
        for (InstructionNode i : children) {
            arr.addAll(i.parse());
        }
        return arr;
    }
}
