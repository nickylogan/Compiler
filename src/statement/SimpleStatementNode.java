package statement;

import main.Instruction;

import java.util.ArrayList;

public class SimpleStatementNode extends StatementNode {
    ArrayList<Instruction> representation;
    @Override
    public ArrayList<Instruction> parse() {
        return representation;
    }
    public SimpleStatementNode() {
        representation = new ArrayList<>();
    }


}
