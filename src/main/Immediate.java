package main;

public class Immediate implements Operand{
    private int value;
    public Immediate(Immediate other){
        setValue(other.value);
    }
    public Immediate(Integer value){
        setValue(value);
    }
    public Immediate(int value){
        setValue(value);
    }

    public int getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
    public void setValue(int value){
        this.value = value;
    }
}
