package main;

import java.util.ArrayList;

public class VariableMemory {
	public final static int VARIABLE_SIZE = 4;
	public final static int INITIAL_LOCATION = 50;
	
	public static ArrayList<Variable> varList = new ArrayList<Variable>();
	
	private static int getLastLocation() {
		return (varList.size() * VARIABLE_SIZE) + INITIAL_LOCATION;
	}
	
	public static Variable initVar(String name) {
		for(Variable v : varList) {
			if(v.getName().equals(name)) {
				return v;
			}
		}
		Variable newVar = new Variable(name, getLastLocation());
		varList.add(newVar);
		return newVar;
	}
}