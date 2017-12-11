package main;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import main.Immediate;
import main.Instruction;
import main.Memory;
import main.ParserException;
import main.Register;
import main.Token;
import main.VariableLocation;

import static main.Operator.*;

public class Parser {
	private static HashMap<String, VariableLocation> variables = new HashMap<String, VariableLocation>();
	private static ArrayList<Instruction> instructions;
	public final static int LINE_SIZE = 4;
	public final static int LINE_INIT_POS = 0;
	
	public static ArrayList<Instruction> parseAssignStatement(ArrayList<String> line, int index) {
		instructions = new ArrayList<Instruction>();
		
		// remove semicolon if assign statement
    	//String last = line.get(line.size() - 1);
    	//line.set(line.size() - 1, last.substring(0, last.length() - 1));
    	
    	String val = line.get(0);
    	String assignSymbol = line.get(1);
    	
    	if(!isNumeric(val) || !assignSymbol.equals("=")) {
    		// syntax: val = res
    		Object res = addAssignInstruction(new ArrayList<String>(line.subList(2, line.size())));
    		
    		boolean isRegister = (Register.isRegister(val) != -1);
    		Register r;
    		if(isRegister)
    			r = Register.getRegister(Register.isRegister(val));
    		else
    			r = Register.R15;
    			
    		if(res instanceof Register) {
    			// syntax: register1 = register2
    			int resIndex = ((Register) res).ordinal();
    			int valIndex = Register.isRegister(val);
    			if(resIndex != valIndex) // not same register
    				instructions.add(new Instruction(MOVR, r, (Register)res));
    		}
    		else if(res instanceof Integer) {
    			// syntax: register = integer
    			int numericVal = (Integer) res;
    			instructions.add(new Instruction(MOVI, r, new Immediate(numericVal)));
    		}
    		else {
    			VariableLocation varLocation = initVariable(res.toString());
    			instructions.add(new Instruction(MOVI, Register.R15, new Immediate(varLocation)));
    			Memory varMemory = new Memory(Register.R15, new Immediate(0));
    			instructions.add(new Instruction(MOVM, r, varMemory));
    		}
    		
    		if(!isRegister) {
    			VariableLocation varLocation = initVariable(val);
    			instructions.add(new Instruction(MOVI, Register.R13, new Immediate(varLocation)));
    			Memory varMemory = new Memory(Register.R13, new Immediate(0));
    			instructions.add(new Instruction(MOV, varMemory, r));
    		}
    	}
    	else {
    		throw new ParserException("Incorrect syntax at statement " + index);
    	}
    	
    	return instructions;
	}
	
	/**
	 * Add assign and arithmetic instructions
	 * @param tokens - ArrayList of string from splitted line
	 * @return object to be moved into first value
	 */
	private static Object addAssignInstruction(ArrayList<String> tokens) {
		Stack<Object> values = new Stack<Object>();
		Stack<Token> operands = new Stack<Token>();
		
		int size = tokens.size();
		for(int i = 0; i < size; ++i) {
			String s = tokens.get(i);
			System.out.println(tokens.get(i));
			if(s.equals("")) {
				continue;
			}
			else if(s.equals("(")) {
				operands.push(Token.LB);
			}
			else if(s.equals(")")) {
				while(!operands.peek().equals(Token.LB))
					values.push(applyOp(operands.pop(), values.pop(), values.pop()));
				operands.pop();
			}
			else if(s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/")) {
				while(!operands.empty() && Token.hasPrecedence(Token.getToken(s), operands.peek()))
					values.push(applyOp(operands.pop(), values.pop(), values.pop()));
				operands.push(Token.getToken(s));
			}
			else if(isNumeric(s)) {
				values.push(Integer.parseInt(s));
			}
			else if(Register.isRegister(s) != -1){
				int index = Register.isRegister(s);
				Register r = Register.getRegister(index);
				if(Register.isReserved(r))
					throw new ParserException("Cannot use reserved register");
				values.push(r);
			}
			else {
				values.push(s);
			}
		}
		
		// Apply operations to remaining values and operands in stack
		while(!operands.empty())
            values.push(applyOp(operands.pop(), values.pop(), values.pop()));
		
		return values.pop();
	}
	
	/**
	 * Apply operation, add instruction and return result as object
	 * @param token - operand
	 * @param val1 - value 1
	 * @param val2 - value 2
	 * @return object to be pushed into equation
	 */
	public static Object applyOp(Token token, Object val1, Object val2) {
		if(val1 instanceof Integer && val2 instanceof Integer) {
			Integer res = 0;
			switch(token.getKeyword()) {
				case "+":	res = ((Integer)val2 + (Integer)val1); break;
				case "-":	res = ((Integer)val2 - (Integer)val1); break;
				case "*":	res = ((Integer)val2 * (Integer)val1); break;
				case "/":	res = ((Integer)val2 / (Integer)val1); break;
			}
			if(res < 0) {
				throw new ParserException("Negative numbers not supported");
			}
			else {
				return res;
			}
		}
		else if(val2 instanceof Register && val1 instanceof Integer) {
			// example: R1 (val2) - 5 (val1)
			Register r = Register.R15;
			switch(token.getKeyword()) {
				case "+":	instructions.add(new Instruction(ADDI, r, (Register)val2, new Immediate((int)val1))); break;
				case "-":	instructions.add(new Instruction(SUBI, r, (Register)val2, new Immediate((int)val1))); break;
				case "*":	instructions.add(new Instruction(MULI, r, (Register)val2, new Immediate((int)val1))); break;
				case "/":	instructions.add(new Instruction(DIVI, r, (Register)val2, new Immediate((int)val1))); break;
			}
			return r;
		}
		else if(val1 instanceof Register && val2 instanceof Integer) {
			// example: 5 (val2) - R1 (val1)
			Register r = Register.R15;
			switch(token.getKeyword()) {
				case "+":	instructions.add(new Instruction(ADDI, r, (Register)val1, new Immediate((int)val2))); break;
				case "*":	instructions.add(new Instruction(MULI, r, (Register)val1, new Immediate((int)val2))); break;
				case "-":	instructions.add(new Instruction(MOVI, r, new Immediate((int)val2)));
							instructions.add(new Instruction(SUB, r, r, (Register)val1)); break;
				case "/":	instructions.add(new Instruction(MOVI, r, new Immediate((int)val2)));
							instructions.add(new Instruction(DIV, r, r, (Register)val1)); break;
			}
			return r;
		}
		else if(val1 instanceof Register && val2 instanceof Register) {
			Register r = Register.R15;
			switch(token.getKeyword()) {
				case "+":	instructions.add(new Instruction(ADD, r, (Register)val2, (Register)val1)); break;
				case "-":	instructions.add(new Instruction(SUB, r, (Register)val2, (Register)val1)); break;
				case "*":	instructions.add(new Instruction(MUL, r, (Register)val2, (Register)val1)); break;
				case "/":	instructions.add(new Instruction(DIV, r, (Register)val2, (Register)val1)); break;
			}
			return r;
		}
		else {
			if(val1 instanceof String) {
				Register r = Register.R15;
				VariableLocation varLocation = initVariable(val1.toString());
    			instructions.add(new Instruction(MOVI, r, new Immediate(varLocation)));
    			Memory varMemory = new Memory(r, new Immediate(0));
    			instructions.add(new Instruction(MOVM, r, varMemory));
    			val1 = r;
			}
			if(val2 instanceof String) {
				Register r = Register.R14;
				VariableLocation varLocation = initVariable(val2.toString());
    			instructions.add(new Instruction(MOVI, r, new Immediate(varLocation)));
    			Memory varMemory = new Memory(r, new Immediate(0));
    			instructions.add(new Instruction(MOVM, r, varMemory));
    			val2 = r;
			}
			return applyOp(token, val1, val2);
		}
	}
	
	/**
	 * Check if string is a number or not
	 * @param s
	 * @return whether string is number or not
	 */
	public static boolean isNumeric(String s) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(s, pos);
		return s.length() == pos.getIndex();
	}
	
	public static VariableLocation initVariable(String varName) {
		if(variables.containsKey(varName)) {
			return variables.get(varName);
		}
		else {
			VariableLocation i = new VariableLocation(0);
			variables.put(varName, i);
			return i;
		}
	}
	
	public static void modifyVarLocations(int n) {
		for(String s : variables.keySet()) {
			VariableLocation temp = variables.get(s);
			temp.setValue(LINE_INIT_POS + (n * LINE_SIZE));
			n++;
		}
	}
	
	private static void modifyInstruction(Instruction ins, int pos) {
		Operator op = ins.getOperator();
		ArrayList<Operand> operands = ins.getOperands();
		if(op.equals(Operator.JMP)) {
			operands.remove(0);
		}
		else {
			operands.remove(2);
		}
		operands.add(new Immediate(LINE_INIT_POS + ((pos - 1) * LINE_SIZE)));
		ins.setOperands(operands);
	}
	
	public static HashMap<String, VariableLocation> getVariables() {
		return variables;
	}

	public static void setVariables(HashMap<String, VariableLocation> variables) {
		Parser.variables = variables;
	}
}
