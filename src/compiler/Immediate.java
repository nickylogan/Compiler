package compiler;

public class Immediate implements Operand {
  private Integer value;

  public Immediate(Integer value) {
    setValue(value);
  }

  public Immediate(Immediate other) {
    setValue(other.getValue());
  }

  public Immediate(int value) {
    this.value = value;
  }

  public Integer getValue() {
    return value;
  }

  public int getIntValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }


  @Override
  public String toString() {
    return Integer.toString(getIntValue());
  }
}
