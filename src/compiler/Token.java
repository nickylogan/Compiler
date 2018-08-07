package compiler;

public enum Token {
	ADD("+", Token.ADD_SUB), SUB("-", Token.ADD_SUB),
	MUL("*", Token.MUL_DIV), DIV("/", Token.MUL_DIV),
	LB("(", Token.PRE), RB(")", Token.PRE),
	LT("<", Token.LOG1), GT(">", Token.LOG1),
	LE("<=", Token.LOG2), GE(">=", Token.LOG2),
	EQ("==", Token.LOG2), NEQ("!=", Token.LOG2),
	T("true", Token.LOG3), F("false", Token.LOG3);
	
	private String keyword;
	private int type;
	public final static int ADD_SUB = 0;
	public final static int MUL_DIV = 1;
	public final static int PRE = 2;
	public final static int LOG1 = 3;
	public final static int LOG2 = 4;
	public final static int LOG3 = 5;
	
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
}
