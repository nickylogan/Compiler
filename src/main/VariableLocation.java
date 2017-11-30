package main;

public class VariableLocation {
	private int value;

	VariableLocation(int value) {
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
