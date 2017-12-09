package statement;

import main.Instruction;

import java.util.ArrayList;

public class WhileNode extends StatementNode {
    MultipleStatementNode children;
    public WhileNode(){
        children = new MultipleStatementNode();
        children.setParent(this);
    }
    @Override
    public ArrayList<Instruction> parse() {
        ArrayList<Instruction> temp = new ArrayList<>();
//        temp.add("Loop start");
        temp.addAll(children.parse());
//        temp.add("Loop end");
        return temp;
    }
}
