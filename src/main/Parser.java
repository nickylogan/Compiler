package main;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import static main.Operator.*;

public class Parser {
	private static ArrayList<Instruction> instructions = new ArrayList<Instruction>();
	private static HashMap<String, Integer> variables = new HashMap<String, Integer>(); // TODO Variable
	private static HashSet<Register> usedRegisters = new HashSet<Register>();
	private static HashSet<Register> tempUsedRegisters = new HashSet<Register>();
	
	/**
	 * Convert to assembly code per line
	 * @param s - 1 line in string form
	 * @param index - line number starting from 1
	 * @return instructions - ArrayList of instructions
	 */
	public static ArrayList<Instruction> convertToAssemblyCode(String s, int index) {
		ArrayList<String> line = splitToTokens(s);
		
        if(Keyword.contains(line.get(0))) {
        	
        	// TODO Conditional branches
        }
        else {
        	String val = line.get(0);
        	String assignSymbol = line.get(1);
        	if(!isNumeric(val) || !assignSymbol.equals("=")) {
        		// syntax: val = res
        		Object res = addAssignInstruction(new ArrayList<String>(line.subList(2, line.size())));
        			
        		if(Register.isRegister(val) != -1) {
        			int ind = Register.isRegister(val);
        			
        			if(res instanceof Register) {
        				// syntax: register1 = register2
        				int resIndex = ((Register) res).ordinal();
        				int valIndex = Register.isRegister(val);
        				if(resIndex != valIndex) // not same register
        					instructions.add(new Instruction(MOVR, Register.getRegister(ind), (Register)res));
        			}
        			else if(res instanceof Integer) {
        				// syntax: register = integer
        				int numericVal = ((Integer)res).intValue();
        				instructions.add(new Instruction(MOVI, Register.getRegister(ind), new Immediate(numericVal)));
        			}
        			else {
        				// TODO Variable
        			}
        		}
        		else {
        			// TODO Variable
        		}
        	}
        	else {
        		throw new ParserException("Incorrect syntax at statement " + index);
        	}
        }
        tempUsedRegisters.clear();
		return instructions;
	}
	
	/**
	 * Split line into separate tokens
	 * @param s - line to be splitted
	 * @return line - ArrayList of tokens in string forms
	 */
	private static ArrayList<String> splitToTokens(String s) {
		s = s.replaceAll(" +", "");
        s = s.substring(0, s.length()-1);
        String splitted[] = s.split("(?=[-+*/()<>!]|(?<![<>=!])=)|(?<=[-+*/()]|[<>=!](?!=))");
       
        ArrayList<String> line = new ArrayList<String>(Arrays.asList(splitted));
        
        /*for(String s1 : splitted) {
        	System.out.print(s1 + " ");
        }
        System.out.println("");*/
        
        return line;
	}
	
	/**
	 * Add assign and arithmetic instructions
	 * @param tokens - ArrayList of string from splitted line
	 * @return object to be moved into first value
	 */
	private static Object addAssignInstruction(ArrayList<String> tokens) {
		Stack<Object> values = new Stack<>();
		Stack<Token> operands = new Stack<>();
		
		int size = tokens.size();
		for(int i = 0; i < size; ++i) {
			String s = tokens.get(i);
			if(s.equals("(")) {
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
				values.push(r);
				usedRegisters.add(r);
			}
			else {
				// TODO Variable
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
			if(res < 0)
				throw new ParserException("Negative numbers not supported");
			else
				return res;
		}
		else if(val2 instanceof Register && val1 instanceof Integer) {
			Register r = getUnusedRegister();
			tempUsedRegisters.add(r);
			switch(token.getKeyword()) {
				case "+":	instructions.add(new Instruction(ADDI, r, (Register)val2, new Immediate((int)val1))); break;
				case "-":	instructions.add(new Instruction(SUBI, r, (Register)val2, new Immediate((int)val1))); break;
				case "*":	instructions.add(new Instruction(MULI, r, (Register)val2, new Immediate((int)val1))); break;
				case "/":	instructions.add(new Instruction(DIVI, r, (Register)val2, new Immediate((int)val1))); break;
			}
			return r;
		}
		else if(val1 instanceof Register && val2 instanceof Integer) {
			Register r1 = getUnusedRegister();
			tempUsedRegisters.add(r1);
			Register r2 = getUnusedRegister();
			switch(token.getKeyword()) {
				case "+":	instructions.add(new Instruction(ADDI, r1, (Register)val2, new Immediate((int)val1))); break;
				case "*":	instructions.add(new Instruction(MULI, r1, (Register)val2, new Immediate((int)val1))); break;
				case "-":	instructions.add(new Instruction(MOVI, r2, new Immediate((int)val2)));
							instructions.add(new Instruction(SUB, r1, r2, r1)); break;
				case "/":	instructions.add(new Instruction(MOVI, r2, new Immediate((int)val2)));
							instructions.add(new Instruction(DIV, r1, r2, r1)); break;
			}
			return r1;
		}
		else if(val1 instanceof Register && val2 instanceof Register) {
			Register r = getUnusedRegister();
			tempUsedRegisters.add(r);
			switch(token.getKeyword()) {
				case "+":	instructions.add(new Instruction(ADD, r, (Register)val2, (Register)val1)); break;
				case "-":	instructions.add(new Instruction(SUB, r, (Register)val2, (Register)val1)); break;
				case "*":	instructions.add(new Instruction(MUL, r, (Register)val2, (Register)val1)); break;
				case "/":	instructions.add(new Instruction(DIV, r, (Register)val2, (Register)val1)); break;
			}
			return r;
		}
		else if(val1 instanceof Variable && val2 instanceof Variable) {
			// TODO Variable
			return null;
		}
		else {
			if(val2 instanceof Variable) {
				Object temp = val1;
				val1 = val2;
				val2 = temp;
			}
			// TODO Variable
			return null;
		}
	}
	
	
	private static Register getUnusedRegister() {
		Register r = null;
		for(int i = 0; i < 16; ++i) {
			r = Register.getRegister(i);
		}
		if(r == null)
			throw new ParserException("Out of memory");
		return r;
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
	
	public static HashMap<String, Integer> getVariables() {
		return variables;
	}

	public static void setVariables(HashMap<String, Integer> variables) {
		Parser.variables = variables;
	}

	public static HashSet<Register> getUsedRegisters() {
		return usedRegisters;
	}

	public static void setUsedRegisters(HashSet<Register> usedRegisters) {
		Parser.usedRegisters = usedRegisters;
	}
}
