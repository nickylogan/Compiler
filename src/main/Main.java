package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import static main.Operator.*;
import static main.Register.*;

public class Main {

    public static void main(String[] args) {
    	String path = "src/main/input.txt";
    	File file = new File(path);
    	String pseudocode = new String();
    	try {
			pseudocode = getString(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
        ArrayList<Instruction> instructions = new ArrayList<>();
        String lines[] = pseudocode.split("\\r?\\n");
        
        int size = lines.length;
        for(int i = 0; i < size; ++i) {
        	String l = lines[i];
            instructions = Parser.convertToAssemblyCode(l, i+1);
        }
        
        Parser.modifyVarLocations(instructions.size());
        
        printAssemblyCode(instructions);
        String s = Mapper.convertToMachineCode(instructions);
        System.out.print(s);
        try {
			writeFile("src/main/output.mcd", s);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static String getString(File file) throws IOException {
    	String s = "";
    	BufferedReader br = new BufferedReader(new FileReader(file));
    	String curLine = br.readLine();
    	while (curLine != null) {
    		// remove tabs
    		while(curLine.startsWith("\t")) {
    			curLine = curLine.substring(1, curLine.length());
    		}
    		// check if comment or not
    		if(!curLine.startsWith("//")) {
    			s += curLine;
    			//if(curLine.endsWith(";"))
    			s += "\n";
    		}
    		curLine = br.readLine();
    	}
    	br.close();
    	return s;
    }
    
    private static void printAssemblyCode(ArrayList<Instruction> instructions) {
        for(int i = 0; i < instructions.size(); ++i) {
        	Instruction ins = instructions.get(i);
        	System.out.println("[" + ((i * Parser.LINE_SIZE) + Parser.LINE_INIT_POS) + "] " + ins.toString());
        }
    }
    
    public static void writeFile(String fileName, String text) throws IOException {
    	File file = new File (fileName);
    	BufferedWriter out = new BufferedWriter(new FileWriter(file)); 
    	out.write(text);
    	out.close();
	}
    
    private static void testInstructions(ArrayList<Instruction> instructions) {
    	instructions.add(new Instruction(MOVI, R1, new Immediate(56)));
        instructions.add(new Instruction(MOVI, R2, new Immediate(10)));
        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(0)), R2));
        instructions.add(new Instruction(MOVI, R2, new Immediate(7)));
        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(4)), R2));
        instructions.add(new Instruction(MOVI, R2, new Immediate(0)));
        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(8)), R2));
        instructions.add(new Instruction(MOVM, R8, new Memory(R1, new Immediate(0))));
        instructions.add(new Instruction(MOVM, R9, new Memory(R1, new Immediate(4))));
        instructions.add(new Instruction(JGT, R8, R9, new Immediate(48)));
        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(8)), R8));
        instructions.add(new Instruction(JMP, new Immediate(52)));
        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(8)), R9));
        instructions.add(new Instruction(HALT));
    	//System.out.println(instructions.get(0).getOperator());
    }
}
