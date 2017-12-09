package InstructionNodes;

import java.util.ArrayList;

public class IfNode extends InstructionNode {
    private MultipleInstructionNode trueChild;
    private MultipleInstructionNode falseChild;

    public IfNode(){
        trueChild = new MultipleInstructionNode();
        trueChild.setParent(this);
    }
    @Override
    public ArrayList<String> parse() {
        ArrayList<String> arr = new ArrayList<>();
        arr.add("If start");
        arr.addAll(trueChild.parse());
        if(falseChild != null){
            arr.add("else: ");
            arr.addAll(falseChild.parse());
        }
        arr.add("If end");
        return arr;
    }
    public MultipleInstructionNode createFalseChild(){
        falseChild = new MultipleInstructionNode();
        falseChild.setParent(this);
        return falseChild;
    }
    public MultipleInstructionNode getFalseChild() {
        return falseChild;
    }

    public MultipleInstructionNode getTrueChild() {
        return trueChild;
    }
}
