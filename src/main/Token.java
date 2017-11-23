package main;

public enum Token {
	ADD("+", Token.ADD_SUB), SUB("-", Token.ADD_SUB),
	MUL("*", Token.MUL_DIV), DIV("/", Token.MUL_DIV),
	LB("(", Token.PRE), RB(")", Token.PRE);
	
	private String keyword;
	private int type;
	public final static int ADD_SUB = 0;
	public final static int MUL_DIV = 1;
	public final static int PRE = 2;
	
	Token(String keyword, int type) {
		setKeyword(keyword);
		setType(type);
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public static Token getToken(String s) {
		switch(s) {
			case "+":	return Token.ADD;
			case "-":	return Token.SUB;
			case "*":	return Token.MUL; 
			case "/":	return Token.DIV;
			case "(":	return Token.LB;
			case ")":	return Token.RB;
			default:	return null;
		}
	}
	
	public static Token getToken(char c) {
		return getToken(Character.toString(c));
	}
	
	public static boolean contains(char val) {
		for(Token c : Token.values()) {
			if(c.name().equals(val)) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static boolean hasPrecedence(Token op1, Token op2) {
        if (isPre(op2))
            return false;
        if (isMulDiv(op1) && isAddSub(op2))
            return false;
        else
            return true;
    }
	
	private static boolean isPre(Token t) {
		return (t.getType() == Token.PRE);
	}
	
	private static boolean isAddSub(Token t) {
		return (t.getType() == Token.ADD_SUB);
	}
	
	private static boolean isMulDiv(Token t) {
		return (t.getType() == Token.MUL_DIV);
	}
}
