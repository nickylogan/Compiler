package statement;

import main.Instruction;
import main.InstructionOffset;

import java.util.ArrayList;

public class MultipleStatementNode extends StatementNode {
    private ArrayList<StatementNode> children;

    public MultipleStatementNode(){
        children = new ArrayList<>();
    }

    public void addStatement(StatementNode element) {
        element.setParent(this);
        children.add(element);
    }

    @Override
    public ArrayList<InstructionOffset> parse() {
        ArrayList<InstructionOffset> arr = new ArrayList<>();
        int i = 0;
        for (StatementNode s : children) {
            ArrayList<InstructionOffset> ins = s.parse();
            for(InstructionOffset in : ins){
                in.setOffset(i + in.getOffset());
            }
            i += ins.size();
            arr.addAll(ins);
        }
        return arr;
    }
}
