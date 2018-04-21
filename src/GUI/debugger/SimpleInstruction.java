package GUI.debugger;

import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;

public class SimpleInstruction {
  private SimpleStringProperty location;
  private SimpleStringProperty operator;
  private SimpleStringProperty operand1;
  private SimpleStringProperty operand2;
  private SimpleStringProperty operand3;
  private static int MAX_LOCATION_LEN = 2;

  SimpleInstruction(Integer location, String operator, ArrayList<String> operands) {
    this.location = new SimpleStringProperty("[" + (location < 10 ? "0" : "") + location + "]");
    this.operator = new SimpleStringProperty(operator);
    this.operand1 = new SimpleStringProperty(operands.size() > 0 ? operands.get(0) : "");
    this.operand2 = new SimpleStringProperty(operands.size() > 1 ? operands.get(1) : "");
    this.operand3 = new SimpleStringProperty(operands.size() > 2 ? operands.get(2) : "");
  }

  public String getLocation() {
    return location.get();
  }

  public SimpleStringProperty locationProperty() {
    return location;
  }

  public void setLocation(String location) {
    this.location.set(location);
  }

  public String getOperator() {
    return operator.get();
  }

  public SimpleStringProperty operatorProperty() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator.set(operator);
  }

  public String getOperand1() {
    return operand1.get();
  }

  public SimpleStringProperty operand1Property() {
    return operand1;
  }

  public void setOperand1(String operand1) {
    this.operand1.set(operand1);
  }

  public String getOperand2() {
    return operand2.get();
  }

  public SimpleStringProperty operand2Property() {
    return operand2;
  }

  public void setOperand2(String operand2) {
    this.operand2.set(operand2);
  }

  public String getOperand3() {
    return operand3.get();
  }

  public SimpleStringProperty operand3Property() {
    return operand3;
  }

  public void setOperand3(String operand3) {
    this.operand3.set(operand3);
  }


  public String shortString() {
    return getOperator() +
           (getOperand1().isEmpty() ? "" : " " + getOperand1()) +
           (getOperand2().isEmpty() ? "" : ", " + getOperand2()) +
           (getOperand3().isEmpty() ? "" : ", " + getOperand3());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    int locSize = getLocation().length();
    sb.append(getLocation());
    for (int i = 0; i <= MAX_LOCATION_LEN + 2 - locSize; ++i)
      sb.append(" ");
    sb.append(getOperator());
    sb.append(getOperator().length() == 2 ? "  " : getOperator().length() == 3 ? " " : "");
    sb.append(getOperand1().isEmpty() ? "" : " " + getOperand1());
    sb.append(getOperand2().isEmpty() ? "" : ", " + getOperand2());
    sb.append(getOperand3().isEmpty() ? "" : ", " + getOperand3());
    return sb.toString();
  }

  public static void setMaxLocation(int max) {
    MAX_LOCATION_LEN = (int) Math.floor(Math.log10(max)) + 1;
  }


}
