package main;

import java.util.ArrayList;
import java.util.Stack;

public class Parser {
	private static ArrayList<Instruction> instructions = new ArrayList<Instruction>();
	
	public final static int NUMERIC_VALUE = 0;
	public final static int REGISTER = 1;
	public final static int VARIABLE = 2;
	
	private static void addAssignInstruction(String line) {
		//ArrayList<Instruction> instTemp = new ArrayList<Instruction>();
		char[] tokens = line.toCharArray();
		String temp = "";
		Stack<Object> values = new Stack<Object>();
		Stack<Token> operands = new Stack<Token>();
		
		int size = tokens.length;
		for(int i = 0; i < size; ++i) {
			char c = tokens[i];
			if(!Character.isJavaIdentifierPart(c)) {
				temp += c;
			}
			else {
				// Add value to stack depending on type
				int type = -1;
				try {
					type = getType(temp);
				} catch (Exception e) {
					e.printStackTrace();
				}
				switch(type) {
					case NUMERIC_VALUE: values.push(Integer.parseInt(temp)); break;
					case REGISTER:		values.push(Register.getRegister(Register.isRegister(temp))); break;
					case VARIABLE:		values.push(VariableMemory.initVar(temp)); break;
					default:			break;
				}
				
				// Add operands and apply operations
				if(c == ' ') {
					continue;
				}
				else if(c == '(') {
					operands.push(Token.getToken(c));
				}
				else if(c == ')') {
					while(operands.peek() != Token.LB)
						values.push((applyOp(operands.pop(), values.pop(), values.pop())));
					operands.pop();
				}
				else if(c == '+' || c == '-' || c == '*' || c == '/' || c == '%') {
					while(!operands.empty() && Token.hasPrecedence(Token.getToken(c), operands.peek()))
						values.push(applyOp(operands.pop(), values.pop(), values.pop()));
					operands.push(Token.getToken(c));
				}
				
				temp = "";
			}
		}
		// Apply operations to remaining values and operands in stack
		while (operands.empty())
            values.push(applyOp(operands.pop(), values.pop(), values.pop()));
		Object b = values.pop();
		//Instruction.add(new Instruction());
	}
	
	public static ArrayList<Instruction> convertToAssemblyCode(ArrayList<String> lines) {
		int size = lines.size();
		
		for(int i = 0; i < size; ++i) {
			String curLine = lines.get(i);
			int length = curLine.length();
			String temp = "";
			for(int j = 0; j < length; ++j) {
				char c = curLine.charAt(j);
				if(!(c != '=' && c != ' ')) {
					temp += c;
				}
				else {
					addAssignInstruction(curLine);
				}
			}
		}
		
		return instructions;
	}
	
	public static Object applyOp(Token token, Object val1, Object val2) {
		if(val1 instanceof Number && val2 instanceof Number) {
			switch(token.getKeyword()) {
				case "+":	return ((int)val1 + (int)val2);
				case "-":	return ((int)val1 + (int)val2);
				case "*":	return ((int)val1 * (int)val2);
				case "/":	return ((int)val1 / (int)val2);
				case "%":	return ((int)val1 % (int)val2);
			}
		}
		else if((val1 instanceof Register && val2 instanceof Number) || (val2 instanceof Register && val1 instanceof Number)) {
			// val1 is register, val2 is number
			if(val2 instanceof Register) {
				Object temp = val1;
				val1 = val2;
				val2 = temp;
			}
			
			switch(token.getKeyword()) {
				case "+":	instructions.add(new Instruction(Operator.ADDI, (Register)val1, new Immediate((int)val2)));
				case "-":	instructions.add(new Instruction(Operator.SUBI, (Register)val1, new Immediate((int)val2)));
				//case "*":	instructions.add(new Instruction(Operator.MULI, (Register)val1, new Immediate((int)val2)));
				//case "/":	instructions.add(new Instruction(Operator.DIVI, (Register)val1, new Immediate((int)val2)));
				//case "%":	instructions.add(new Instruction(Operator.MOD, (Register)val1, new Immediate((int)val2)));
				return val1;
			}
		}
		else if((val1 instanceof Variable) || (val2 instanceof Variable)) {
			if(val2 instanceof Variable) {
				Object temp = val1;
				val1 = val2;
				val2 = temp;
			}
			// TODO Variable
		}
		else {
			// Register and register left
			switch(token.getKeyword()) {
				case "+":	instructions.add(new Instruction(Operator.ADD, (Register)val1, (Register)val2));
				case "-":	instructions.add(new Instruction(Operator.SUB, (Register)val1, (Register)val2));
			}
			return val1;
		}
		return null;
	}
	
	public static Instruction immediateIntruction(String opString, int a, int b) {
		char op = opString.charAt(0);
		
		switch(op) {
			case '+':	return new Instruction(Operator.ADD, new Immediate(a), new Immediate(b));
			case '-':	return new Instruction(Operator.SUB, new Immediate(a), new Immediate(b));
			//case '*':	return new Instruction(Operator.MUL, new Immediate(a), new Immediate(b));
			//case '/':	return new Instruction(Operator.DIV, new Immediate(a), new Immediate(b));
			default:	return null;
		}
    }
	
	private static int getType(String s) throws Exception {
		int length = s.length();
		if(Character.isDigit(s.charAt(0))) {
			for(int i = 0; i < length; ++i) {
				if(!Character.isDigit(s.charAt(i)))
					throw new Exception();
			}
			return NUMERIC_VALUE;
		}
		else {
			int rid = Register.isRegister(s);
			if(rid != -1)	return REGISTER;
				else		return VARIABLE;
		}
	}
}