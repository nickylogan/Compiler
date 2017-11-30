package tester;

import java.util.ArrayList;

public class IfNode extends InstructionNode {
    private SimpleInstructionNode TrueChild;
    private SimpleInstructionNode FalseChild;
    @Override
    public ArrayList<InstructionNode> parse() {
        return null;
    }

    public SimpleInstructionNode getFalseChild() {
        return FalseChild;
    }

    public SimpleInstructionNode getTrueChild() {
        return TrueChild;
    }
}
