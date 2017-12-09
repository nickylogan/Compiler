package statement;

import main.Instruction;

import java.util.ArrayList;

public class IfNode extends StatementNode {
    private MultipleStatementNode trueChild;
    private MultipleStatementNode falseChild;
    private ArrayList<Instruction> representation;

    public IfNode(){
        trueChild = new MultipleStatementNode();
        trueChild.setParent(this);
    }
    @Override
    public ArrayList<Instruction> parse() {
        ArrayList<Instruction> arr = new ArrayList<>();
        arr.addAll(representation);
        arr.addAll(trueChild.parse());
        if(falseChild != null){
//            arr.add("else: ");
            arr.addAll(falseChild.parse());
        }
//        arr.add("If end");
        return arr;
    }
    public MultipleStatementNode createFalseChild(){
        falseChild = new MultipleStatementNode();
        falseChild.setParent(this);
        return falseChild;
    }
    public MultipleStatementNode getFalseChild() {
        return falseChild;
    }

    public MultipleStatementNode getTrueChild() {
        return trueChild;
    }
}
