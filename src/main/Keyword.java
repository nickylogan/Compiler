package main;

public enum Keyword {
	IF("if", Keyword.BEGIN), WHILE("while", Keyword.BEGIN),
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

