package statement;

import main.Instruction;

import java.util.ArrayList;

public abstract class StatementNode {
    StatementNode parent = null;

    public StatementNode getParent() {
        return parent;
    }
    
    public void setParent(StatementNode parent) {
        this.parent = parent;
    }

    public abstract ArrayList<Instruction> parse();
}
