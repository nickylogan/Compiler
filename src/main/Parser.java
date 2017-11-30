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
	private static Stack<MarkerInstruction> ifPos = new Stack<MarkerInstruction>();
	private static Stack<MarkerInstruction> whilePos = new Stack<MarkerInstruction>();
	private static int insIndex = 0;
	
	private static class MarkerInstruction {
		Instruction mainIns;
		Instruction elseIns;
		ArrayList<Instruction> breakIns = new ArrayList<Instruction>();
		Token token;
		boolean hasElse;
		
		MarkerInstruction(Instruction mainIns, Instruction elseIns, Token token, boolean hasElse) {
			this.mainIns = mainIns;
			this.elseIns = elseIns;
			this.token = token;
			this.hasElse = hasElse;
		}
		
		MarkerInstruction(Instruction ins, Token token) {
			this(ins, null, token, false);
		}
	}
	
	/**
	 * Convert to assembly code per line
	 * @param s - 1 line in string form
	 * @param index - line number starting from 1
	 * @return instructions - ArrayList of instructions
	 */
	public static ArrayList<Instruction> convertToAssemblyCode(String s, int index) {
		ArrayList<String> line = splitToTokens(s.trim());
		if(Keyword.contains(line.get(0))) {
			Keyword k = Keyword.getToken(line.get(0).toLowerCase());
	    	
			if(Keyword.isExit(k)) {
	    		instructions.add(new Instruction(HALT));
	    	}
	    	else if(Keyword.isBegin(k)) {
	    		int i = line.indexOf(")");
	        	if(i == -1) {
	        		throw new ParserException("Incorrect syntax at statement " + index);
	        	}
	        	else {
	        		ArrayList<String> condTemp = new ArrayList<String>(line.subList(0, ++i));
	        		begCondConvertToAssembly(condTemp, index, true);
	        		
	        		ArrayList<String> assignTemp = new ArrayList<String>(line.subList(i, line.size()));
	        		if(!assignTemp.isEmpty())
	        			assignConvertToAssembly(assignTemp, index);
	        	}
	    	}
	    	else if(Keyword.isEnd(k)) {
	    		endCondConvertToAssembly(line, index);
	    	}
        }
        else {
        	assignConvertToAssembly(line, index);
        }
		return instructions;
	}
	
	private static void begCondConvertToAssembly(ArrayList<String> line, int index, boolean isFirstTry) {
		Keyword k = Keyword.getToken(line.get(0).toLowerCase());
    	
		if(line.size() == 6) {
        	String lb, rb, val1, op, val2;
    		lb = line.get(1);
			val1 = line.get(2);
			op = line.get(3);
			val2 = line.get(4);
			rb = line.get(5);
			if(!lb.equals("(") && !rb.equals(")"))
				throw new ParserException("Incorrect syntax at statement " + index);

	    	if(isNumeric(val1) && isNumeric(val2)) {
	    		int x1 = Integer.parseInt(val1);
	    		int x2 = Integer.parseInt(val2);
	    		boolean cond = false;
	    		switch(op) {
					case "<":	cond = x1 < x2; break;
					case ">":	cond = x1 > x2; break;
					case "<=":	cond = x1 <= x2; break;
					case ">=":	cond = x1 >= x2; break;
					case "==":	cond = x1 == x2; break;
					case "!=":	cond = x1 != x2; break;
					default: 	throw new ParserException("Incorrect syntax at statement " + index);
	    		}
	    		ArrayList<String> newLine = new ArrayList<String>(Arrays.asList("if", "(", String.valueOf(cond), ")"));
	    		begCondConvertToAssembly(newLine, index, false);
	    	}
	    	else if (!(Register.isRegister(val1) != -1 && Register.isRegister(val2) != -1)) {
	    		String r1, r2;
	    		
	    		if(Register.isRegister(val1) == -1) {
	    			if(isNumeric(val1)) {
	    				Integer x1 = Integer.parseInt(val1);
	    				instructions.add(insIndex++, new Instruction(MOVI, Register.R15, new Immediate(x1)));
	    			}
	    			else {
	    				// TODO variable
	    			}
	    			r1 = "R15";
	    		}
	    		else {
	    			r1 = val1;
	    		}
	    		
	    		if(Register.isRegister(val2) == -1) {
	    			if(isNumeric(val2)) {
	    				Integer x2 = Integer.parseInt(val2);
	    				instructions.add(insIndex++, new Instruction(MOVI, Register.R14, new Immediate(x2)));
	    			}
	    			else {
	    				// TODO variable
	    			}
	    			r2 = "R14";
	    		}
	    		else {
	    			r2 = val2;
	    		}
	    		
	    		ArrayList<String> newLine = new ArrayList<String>(Arrays.asList("if", "(", r1, op, r2, ")"));
	    		begCondConvertToAssembly(newLine, index, false);
	    	}
	    	else {
	    		Register r1 = Register.getRegister(Register.isRegister(val1));
	    		Register r2 = Register.getRegister(Register.isRegister(val2));
	    		Token tempToken = null;
	    		if(isFirstTry && (r1.equals(Register.R15) || r2.equals(Register.R15) ||
	    								r1.equals(Register.R14) || r2.equals(Register.R14)))
	    			throw new ParserException("Cannot use reserved register");
	    		else {
	    			Instruction ins = null;
	    			Instruction elseIns = null;
	    			switch(op) {
	    				case "<":	ins = new Instruction(JLT, r1, r2, new Immediate(0));
	    							elseIns = new Instruction(JMP, new Immediate(0));
	    							tempToken = Token.getToken("<"); break;
	    				case ">":	ins = new Instruction(JGT, r1, r2, new Immediate(0));
									elseIns = new Instruction(JMP, new Immediate(0));
	    							tempToken = Token.getToken(">"); break;
	    				case "<=":	ins = new Instruction(JLT, r1, r2, new Immediate(0));
	    							tempToken = Token.getToken("<="); break;
	    				case ">=":	ins = new Instruction(JGT, r1, r2, new Immediate(0));
	    							tempToken = Token.getToken(">="); break;
	    				case "==":	ins = new Instruction(JNE, r1, r2, new Immediate(0));
	    							tempToken = Token.getToken("=="); break;
	    				case "!=":	ins = new Instruction(JE, r1, r2, new Immediate(0));
	    							tempToken = Token.getToken("!="); break;
	    				default: 	throw new ParserException("Incorrect syntax at statement " + index);
	    			}
					instructions.add(insIndex++, ins);
	    			if(k.equals(Keyword.IF)) {
	    				ifPos.push(new MarkerInstruction(ins, tempToken));
	    				if(Token.logType(tempToken) == 1) {
	    					ifPos.peek().elseIns = elseIns;
	    					instructions.add(insIndex++, elseIns);
	    				}
	    			}
	    			else {
	    				whilePos.push(new MarkerInstruction(ins, tempToken));
	    				if(Token.logType(tempToken) == 1) {
	    					whilePos.peek().elseIns = elseIns;
	    					instructions.add(insIndex++, elseIns);
	    				}
	    			}
	    		}
    		}
    	}
		else if(line.size() == 4) {
			String lb, rb, val;
    		lb = line.get(1);
    		val = line.get(2);
			rb = line.get(3);
			if(!lb.equals("(") && !rb.equals(")"))
				throw new ParserException("Incorrect syntax at statement " + index);
			
			if(val.equalsIgnoreCase("true")) {
				Instruction ins = new Instruction(JMP, new Immediate(instructions.size() + 2));
				instructions.add(insIndex++, ins);
				if(k.equals(Keyword.IF)) {
					ifPos.push(new MarkerInstruction(ins, Token.T));
				} else {
					whilePos.push(new MarkerInstruction(ins, Token.T));
				}
			}
			else if(val.equalsIgnoreCase("false")) {
				Instruction ins = new Instruction(JMP, new Immediate(instructions.size() + 2));
				instructions.add(insIndex++, ins);
				if(k.equals(Keyword.IF)) {
					ifPos.push(new MarkerInstruction(ins, Token.F));
				} else {
					whilePos.push(new MarkerInstruction(ins, Token.F));
				}
			}
			else {
				throw new ParserException("Incorrect syntax at statement " + index);
			}
		}
    }
    
	private static void endCondConvertToAssembly(ArrayList<String> line, int index) {
		Keyword k = Keyword.getToken(line.get(0).toLowerCase());
    	
		if(k.equals(Keyword.ELSE)) {
			ifPos.peek().hasElse = true;
			Token tempToken = ifPos.peek().token;
			int tokenType = Token.logType(tempToken);
			Instruction tempIns = ifPos.peek().mainIns;
			
    		if(tokenType == 1 || tokenType == 2) {
    			int tempPos = instructions.size() + 2;
				modifyInstruction(tempIns, tempPos);
			}
			if(tokenType != 1) {
				Instruction elseIns = new Instruction(JMP, new Immediate(0));
				ifPos.peek().elseIns = elseIns;
				if(tempToken.equals(Token.F)) {
					int tempPos = instructions.indexOf(tempIns) + 1;
					instructions.add(tempPos, elseIns);
					insIndex = tempPos;
				}
				else {
					instructions.add(insIndex++, elseIns);
				}
			}
			else {
				int tempPos = instructions.indexOf(tempIns) + 1;
				Instruction elseIns = ifPos.peek().elseIns;
				modifyInstruction(elseIns, tempPos);
				insIndex = tempPos;
			}
		}
		else if(k.equals(Keyword.BREAK)) {
			Instruction tempIns = new Instruction(JMP, new Immediate(0));
			instructions.add(insIndex++, tempIns);
			whilePos.peek().breakIns.add(tempIns);
		}
		else {
			boolean isIf = k.equals(Keyword.ENDIF);
    		Token tempToken = (isIf)? ifPos.peek().token : whilePos.peek().token;
    		if(Token.logType(tempToken) == 1) {
    			// TODO fix
    			Instruction tempIns;
    			Instruction tempMainIns;
				int mainPos;
    			if(isIf) {
        			tempMainIns = ifPos.peek().mainIns;
					tempIns = ifPos.peek().elseIns;
    				if(ifPos.peek().hasElse) {
    					mainPos = instructions.indexOf(tempIns) + 2;
    					int tempPos = instructions.size() + 1;
    					modifyInstruction(tempIns, tempPos);
    				}
    				else {
    					mainPos = instructions.indexOf(tempMainIns) + 3;
    					int tempPos = instructions.size() + 1;
    					modifyInstruction(tempIns, tempPos);
    				}
					modifyInstruction(tempMainIns, mainPos);
    				ifPos.pop();
    			}
    			else {
        			tempMainIns = whilePos.peek().mainIns;
    				tempIns = whilePos.peek().elseIns;
    				mainPos = instructions.indexOf(tempMainIns)+1;
    				instructions.add(insIndex++, new Instruction(JMP, new Immediate(mainPos)));
    				
    				mainPos = instructions.indexOf(tempMainIns) + 3;
					modifyInstruction(tempMainIns, mainPos);

					int tempPos = instructions.size() + 1;
					modifyInstruction(tempIns, tempPos);
    				
    				ArrayList<Instruction> tempBreak = whilePos.pop().breakIns;
    				int size = tempBreak.size();
    				for(int i = 0; i < size; ++i) {
    					modifyInstruction(tempBreak.get(i), instructions.size() + 1);
    				}
    			}
    		}
    		else if(Token.logType(tempToken) == 2){
    			Instruction tempIns;
    			if(isIf) {
    				tempIns = (ifPos.peek().hasElse)? ifPos.pop().elseIns : ifPos.pop().mainIns;
    			}
    			else {
    				tempIns = whilePos.peek().mainIns;
    				int mainPos = instructions.indexOf(tempIns)+1;
    				instructions.add(insIndex++, new Instruction(JMP, new Immediate(mainPos)));
    				
    				ArrayList<Instruction> tempBreak = whilePos.pop().breakIns;
    				int size = tempBreak.size();
    				for(int i = 0; i < size; ++i) {
    					modifyInstruction(tempBreak.get(i), instructions.size() + 1);
    				}
    			}
				int tempPos = instructions.size() + 1;
				modifyInstruction(tempIns, tempPos);
    		}
    		else {
    			if(isIf) {
    				Instruction tempIns = ifPos.peek().mainIns;
    				if(ifPos.peek().token.equals(Token.F)) {
    					tempIns = (ifPos.peek().hasElse)? ifPos.pop().elseIns : ifPos.pop().mainIns;
    					int tempPos = instructions.size() + 1;
    					modifyInstruction(tempIns, tempPos);
    				}
    				else {
    					if(ifPos.peek().hasElse) {
    						tempIns = ifPos.pop().elseIns;
    						int tempPos = instructions.size() + 1;
    						modifyInstruction(tempIns, tempPos);
    					}
    				}
    			}
    			else {
    				Instruction tempIns = whilePos.peek().mainIns;
    				if(whilePos.peek().token.equals(Token.F)) {
    					int tempPos = instructions.size() + 1;
    					modifyInstruction(tempIns, tempPos);
    				}
    				else {
    					int mainPos = instructions.indexOf(tempIns)+1;
    					instructions.add(insIndex++, new Instruction(JMP, new Immediate(mainPos)));
    				}
    				ArrayList<Instruction> tempBreak = whilePos.pop().breakIns;
    				int size = tempBreak.size();
    				for(int i = 0; i < size; ++i) {
    					modifyInstruction(tempBreak.get(i), instructions.size() + 1);
    				}
    			}
    		}
    		insIndex = instructions.size();
    	}
    }
	
	private static void assignConvertToAssembly(ArrayList<String> line, int index) {
		// remove semicolon if assign statement
    	String last = line.get(line.size() - 1);
    	line.set(line.size() - 1, last.substring(0, last.length() - 1));
    	
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
    					instructions.add(insIndex++, new Instruction(MOVR, Register.getRegister(ind), (Register)res));
    			}
    			else if(res instanceof Integer) {
    				// syntax: register = integer
    				int numericVal = ((Integer)res).intValue();
    				instructions.add(insIndex++, new Instruction(MOVI, Register.getRegister(ind), new Immediate(numericVal)));
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
	
	/**
	 * Split line into separate tokens
	 * @param s - line to be splitted
	 * @return line - ArrayList of tokens in string forms
	 */
	private static ArrayList<String> splitToTokens(String s) {
		s = s.replaceAll(" +", "");
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
				if(r.equals(Register.R15) || r.equals(Register.R14))
					throw new ParserException("Cannot use reserved register");
				values.push(r);
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
				case "+":	instructions.add(insIndex++, new Instruction(ADDI, r, (Register)val2, new Immediate((int)val1))); break;
				case "-":	instructions.add(insIndex++, new Instruction(SUBI, r, (Register)val2, new Immediate((int)val1))); break;
				case "*":	instructions.add(insIndex++, new Instruction(MULI, r, (Register)val2, new Immediate((int)val1))); break;
				case "/":	instructions.add(insIndex++, new Instruction(DIVI, r, (Register)val2, new Immediate((int)val1))); break;
			}
			return r;
		}
		else if(val1 instanceof Register && val2 instanceof Integer) {
			// example: 5 (val2) - R1 (val1)
			Register r = Register.R15;
			switch(token.getKeyword()) {
				case "+":	instructions.add(insIndex++, new Instruction(ADDI, r, (Register)val2, new Immediate((int)val1))); break;
				case "*":	instructions.add(insIndex++, new Instruction(MULI, r, (Register)val2, new Immediate((int)val1))); break;
				case "-":	instructions.add(insIndex++, new Instruction(MOVI, r, new Immediate((int)val2)));
							instructions.add(insIndex++, new Instruction(SUB, r, r, (Register)val1)); break;
				case "/":	instructions.add(insIndex++, new Instruction(MOVI, r, new Immediate((int)val2)));
							instructions.add(insIndex++, new Instruction(DIV, r, r, (Register)val1)); break;
			}
			return r;
		}
		else if(val1 instanceof Register && val2 instanceof Register) {
			Register r = Register.R15;
			switch(token.getKeyword()) {
				case "+":	instructions.add(insIndex++, new Instruction(ADD, r, (Register)val2, (Register)val1)); break;
				case "-":	instructions.add(insIndex++, new Instruction(SUB, r, (Register)val2, (Register)val1)); break;
				case "*":	instructions.add(insIndex++, new Instruction(MUL, r, (Register)val2, (Register)val1)); break;
				case "/":	instructions.add(insIndex++, new Instruction(DIV, r, (Register)val2, (Register)val1)); break;
			}
			return r;
		}
		else if(val1 instanceof Variable && val2 instanceof Variable) {
			// TODO Variable
			return null;
		}
		else {
			// TODO Variable
			return null;
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
		operands.add(new Immediate(pos));
		ins.setOperands(operands);
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
