package InstructionNodes;

import java.util.ArrayList;
import java.util.Arrays;
import main.Parser;

import main.Instruction;

public class SimpleInstructionNode extends InstructionNode {
	String line;
	Integer lineNumber;
    
    @Override
    public ArrayList<Instruction> parse() {
        ArrayList<Instruction> instructions = new ArrayList<Instruction>();
        
        String splitted[] = line.split("(?=[-+*/()<>!]|(?<![<>=!])=)|(?<=[-+*/()]|[<>=!](?!=))");
        ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(splitted));
        
        Parser.parseAssignStatement(tokens, lineNumber);
        
        return instructions;
    }
    
    public SimpleInstructionNode(String line) {
    	this.line = new String(line);
    }
}
