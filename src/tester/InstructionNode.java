package tester;

import java.util.ArrayList;

public abstract class InstructionNode {
    InstructionNode parent;

    public InstructionNode getParent() {
        return parent;
    }
    
    public abstract ArrayList<InstructionNode> parse();
    
}
