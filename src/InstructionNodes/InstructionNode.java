package InstructionNodes;

import java.util.ArrayList;

public abstract class InstructionNode {
    InstructionNode parent = null;

    public InstructionNode getParent() {
        return parent;
    }
    
    public void setParent(InstructionNode parent) {
        this.parent = parent;
    }

    public abstract ArrayList<String> parse();
}
