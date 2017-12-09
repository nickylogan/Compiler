package InstructionNodes;

import java.util.ArrayList;

public class SimpleInstructionNode extends InstructionNode {
    String representation;
    @Override
    public ArrayList<String> parse() {
        ArrayList<String> arr = new ArrayList<>();
        arr.add(representation);
        return arr;
    }
    public SimpleInstructionNode() {
        representation = "assignment thing";
    }


}
