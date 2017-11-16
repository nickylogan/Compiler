package main;

public class Variable {
	private String name;
	private int decLocation;
	
	public Variable(String name, int value, int decLocation) {
		setName(name);
		setDecLocation(decLocation);
	}
	
	public Variable(String name, int decLocation) {
		this(name, 0, decLocation);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDecLocation() {
		return decLocation;
	}

	public void setDecLocation(int decLocation) {
		this.decLocation = decLocation;
	}
}