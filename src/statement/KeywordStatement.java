package statement;

import main.InstructionOffset;
import main.Keyword;

import java.util.ArrayList;

public class KeywordStatement extends StatementNode {
    private Keyword keyword;
    public KeywordStatement(Keyword keyword, int lineNumber){
        setKeyword(keyword);
        setLineNumber(lineNumber);
    }
    public void setKeyword(Keyword keyword) {
        this.keyword = keyword;
    }

    public Keyword getKeyword() {
        return keyword;
    }

    @Override
    public ArrayList<InstructionOffset> parse() {
        return null;
    }
}
