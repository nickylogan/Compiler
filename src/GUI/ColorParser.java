package GUI;

import javax.xml.soap.Text;
import java.util.ArrayList;

public class ColorParser {
    private String[] inputList;
    private String coloredText;

    public String color (String input) {
        for (int i=0; i<10; i++){ // TODO: 11-Dec-17 replace this with the correct input
            switch (input) {
                case "int":
                    break;
                case "=": case "!=": case "==": case ">": case "<": case ">=": case "<=": case "+": case "-":
                case "*": case "/":
                    break;
                case "r1": case "r2": case "r3": case "r4": case "r5": case "r6": case "r7": case "r8": case "r9":
                case "r10": case "r11": case "r12": case "r13": case "r14": case "r15": case "r16":
                    break;
                case "(": case ")": case "{": case "}":
                    break;
                case "0": case"1": case "2": case "3": case "4": case "5": case "6": case "7": case "8": case "9":
                    break;
                case "variable" :
                    break;
                    default:
                        break;


            }
        }
        return coloredText;
    }

}
