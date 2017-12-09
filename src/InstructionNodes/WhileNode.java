package InstructionNodes;

import java.util.ArrayList;

public class WhileNode extends InstructionNode {
    MultipleInstructionNode children;
    public WhileNode(){
        children = new MultipleInstructionNode();
        children.setParent(this);
    }
    @Override
    public ArrayList<String> parse() {
        ArrayList<String> temp = new ArrayList<>();
        temp.add("Loop start");
        temp.addAll(children.parse());
        temp.add("Loop end");
        return temp;
    }
}
