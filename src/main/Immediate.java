package main;

public class Immediate implements Operand{
    private VariableLocation value;
    
    public Immediate(VariableLocation value) {
    	setValue(value);
    }
    
    public Immediate(Immediate other){
        setValue(other.getValue());
    }
    
    public Immediate(Integer value){
        this.value = new VariableLocation(value);
    }
    
    public Immediate(int value){
    	this.value = new VariableLocation(value);
    }
    
    public VariableLocation getValue() {
    	return value;
    }
    
    public void setValue(VariableLocation value) {
    	this.value = value;
    }

    public void setValue(Integer value) {
        this.value.setValue(value);
    }
    
    public void setValue(int value){
    	this.value.setValue(value);
    }
}
