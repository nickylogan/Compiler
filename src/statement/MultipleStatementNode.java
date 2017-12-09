package statement;

import main.Instruction;

import java.util.ArrayList;

public class MultipleStatementNode extends StatementNode {
    ArrayList<StatementNode> children;

    public MultipleStatementNode(){
        children = new ArrayList<>();
    }
    public void addInstruction(StatementNode element) {
        element.setParent(this);
        children.add(element);
    }

    @Override
    public ArrayList<Instruction> parse() {
        ArrayList<Instruction> arr = new ArrayList<>();
        for (StatementNode i : children) {
            arr.addAll(i.parse());
        }
        return arr;
    }
}
