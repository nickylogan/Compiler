package compiler;

public class VariableLocation {
    private int value;

    public VariableLocation(int value) {
        this.setValue(value);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String toString() {
        return String.valueOf(value);
    }
}
