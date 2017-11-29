package main;

public enum Keyword {
	IF("if", Keyword.BEGIN), WHILE("while", Keyword.BEGIN),
	ELSE("else", Keyword.END), BREAK("break", Keyword.END),
	ENDIF("endif", Keyword.END), ENDWHILE("endwhile", Keyword.END),
	ENDPROGRAM("endprogram", Keyword.EXIT);

	private String keyword;
	private int type;
	public final static int BEGIN = 0;
	public final static int END = 1;
	public final static int EXIT = 2;
	
	Keyword(String keyword, int type) {
		this.setKeyword(keyword);
		this.setType(type);
	}
	
	public static Keyword getToken(String s) {
		switch(s) {
			case "if":			return Keyword.IF;
			case "while":		return Keyword.WHILE;
			case "else":		return Keyword.ELSE;
			case "break":		return Keyword.BREAK;
			case "endif":		return Keyword.ENDIF; 
			case "endwhile":	return Keyword.ENDWHILE;
			case "endprogram":	return Keyword.ENDPROGRAM;
			default:			return null;
		}
	}
	
	public static boolean contains(String val) {
		for(Keyword c : Keyword.values()) {
			if(c.name().equalsIgnoreCase(val)) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static boolean isBegin(Keyword k) {
		return (k.getType() == Keyword.BEGIN);
	}
	
	public static boolean isEnd(Keyword k) {
		return (k.getType() == Keyword.END);
	}
	
	public static boolean isExit(Keyword k) {
		return (k.getType() == Keyword.EXIT);
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
