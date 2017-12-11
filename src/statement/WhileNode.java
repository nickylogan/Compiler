package statement;

import main.Instruction;
import main.InstructionOffset;
import main.Token;

import java.util.ArrayList;

public class WhileNode extends StatementNode {
    private Token operator;
    private MultipleStatementNode children;
    public WhileNode(String line, int lineNumber){
        setLine(line);
        setLineNumber(lineNumber);
        children = new MultipleStatementNode();
        children.setParent(this);

    }
    @Override
    public ArrayList<InstructionOffset> parse() {
        ArrayList<InstructionOffset> res = new ArrayList<>();
//        temp.add("Loop start");
//        temp.addAll(children.parse());
//        temp.add("Loop end");
        return res;
    }

    public MultipleStatementNode getChildren() {
        return children;
    }
}
