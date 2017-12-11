package GUI;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class ColorParser {
    private ArrayList<Text> coloredText;
    private Text t;
    private int tab = 0;

    public ColorParser(String input) {
        color(input);
    }

    public ArrayList<Text> getColoredText() {
        return coloredText;
    }

    public void setColoredText(ArrayList<Text> coloredText) {
        this.coloredText = coloredText;
    }

    private void color(String input) {
        coloredText = new ArrayList<>();

        input = input.replaceAll(" +|[;]|\\t", "");
        String arr[] = input.split("(?=(?<!end)(if|while)|else)|(?<=(if|while|else))|(?=[-+*/()<>!]|(?<![<>=!])=)|(?<=[-+*/()]|[<>=!](?!=))");
        for (String anArr : arr) {
            if (anArr.matches("[rR]([0-9]|1[0-5])")) regColor(anArr);
            else if (anArr.matches("[A-Za-z][A-Za-z0-9]*")) varColor(anArr);
            else if (anArr.matches("[0-9]+")) numColor(anArr);
            else if (anArr.matches("[-+*/]|[<>!=]?[=]|[<>]")) operatorColor(anArr);
            else if (anArr.matches("(end)?(while|if)|endprogram|break|continue")) {
                keywordColor(anArr);
                if (anArr.matches("(while|if)")) tab++;
                else tab--;
            }
            else defColor(anArr);
        }
        for (int i = 0; i<tab; i++) {
            coloredText.add(0, new Text("\t"));
        }
    }

    private void varColor (String var) {
        t = new Text(var);
        t.setFill(Color.rgb(253,165,255));
        t.setFont(Font.font("Monospaced Regular", 12));
        coloredText.add(t);
    }

    private void defColor (String def) {
        t = new Text(def);
        t.setFill(Color.rgb(171,178,191));
        t.setFont(Font.font("Monospaced Regular", 12));
        coloredText.add(t);
    }

    private void numColor (String num) {
        t = new Text(num);
        t.setFill(Color.rgb(210,148,93));
        t.setFont(Font.font("Monospaced Regular", 12));
        coloredText.add(t);
    }

    private void regColor (String reg) {
        t = new Text(reg);
        t.setFill(Color.rgb(255,198,109));
        t.setFont(Font.font("Monospaced Regular", 12));
        coloredText.add(t);
    }

    private void keywordColor (String keyword) {
        t = new Text(keyword);
        t.setFill(Color.rgb(198,121,221));
        t.setFont(Font.font("Monospaced Regular", 12));
        coloredText.add(t);
    }

    private void operatorColor (String operator) {
        t = new Text(operator);
        t.setFill(Color.rgb(255,255,255));
        t.setFont(Font.font("Monospaced Regular", 12));
        coloredText.add(t);
    }
}
