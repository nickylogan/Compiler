package GUI;

import javafx.scene.text.Text;

import java.util.ArrayList;

public class CodeLine {
    Integer line;
    ArrayList<Text> code;

    public CodeLine(Integer line, ArrayList<Text> code) {
        this.line = line;
        this.code = code;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public ArrayList<Text> getCode() {
        return code;
    }

    public void setCode(ArrayList<Text> code) {
        this.code = code;
    }
}
