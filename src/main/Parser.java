package main;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import static main.Operator.*;

public class Parser {
	private static ArrayList<Instruction> instructions = new ArrayList<Instruction>();
	private static HashMap<String, Integer> variables = new HashMap<String, Integer>(); // TODO Variable
	
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
        	if(!isNumeric(val)) {
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
        		throw new ParserException("Incorrect syntax at line " + index);
        	}
        }
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
        String splitted[] = s.split("(?=[-+*/()<>]|(?<![<>=])=)|(?<=[-+*/()]|[<>=](?!=))");
       
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
		Stack<Object> values = new Stack<Object>();
		Stack<Token> operands = new Stack<Token>();
		
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
				values.push(Register.getRegister(index));
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
			switch(token.getKeyword()) {
				case "+":	return ((Integer)val1 + (Integer)val2);
				case "-":	return ((Integer)val1 + (Integer)val2);
				case "*":	return ((Integer)val1 * (Integer)val2);
				case "/":	return ((Integer)val1 / (Integer)val2);
			}
		}
		else if((val1 instanceof Register && val2 instanceof Integer) || (val2 instanceof Register && val1 instanceof Number)) {
			boolean reverse = false;
			// val1 is register, val2 is number
			if(val1 instanceof Register) {
				Object temp = val1;
				val1 = val2;
				val2 = temp;
				reverse = true;
			}
			switch(token.getKeyword()) {
				case "+":	instructions.add(new Instruction(ADDI, (Register)val2, (Register)val2, new Immediate((int)val1))); break;
				case "-":	instructions.add(new Instruction(SUBI, (Register)val2, (Register)val2, new Immediate((int)val1))); break;
				case "*":	instructions.add(new Instruction(MULI, (Register)val2, (Register)val2, new Immediate((int)val1))); break;
				case "/":	instructions.add(new Instruction(DIVI, (Register)val2, (Register)val2, new Immediate((int)val1))); break;
			}
			if(reverse) {
				if(token.getKeyword().equals("-")) {
					// TODO fix subtract - example: 5 - R1
					//instructions.add(new Instruction(Operator.MULI, (Register)val2, (Register)val2, new Immediate((int)(-1))));
				}
				else if(token.getKeyword().equals("/")) {
					// TODO Divide - example: 5 / R1
				}
			}
			return val2;
		}
		else if(val1 instanceof Register && val2 instanceof Register) {
			switch(token.getKeyword()) {
				case "+":	instructions.add(new Instruction(ADD, (Register)val2, (Register)val2, (Register)val1)); break;
				case "-":	instructions.add(new Instruction(SUB, (Register)val2, (Register)val2, (Register)val1)); break;
				case "*":	instructions.add(new Instruction(MUL, (Register)val2, (Register)val2, (Register)val1)); break;
				case "/":	instructions.add(new Instruction(DIV, (Register)val2, (Register)val2, (Register)val1)); break;
			}
			return val2;
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
		return null;
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
}
