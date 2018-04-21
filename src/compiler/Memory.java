package compiler;

public class Memory implements Operand {
  private Register register;
  private Immediate immediate;

  public Memory(Memory other) {
    setImmediate(new Immediate(other.immediate));
    setRegister(other.register);
  }

  public Memory(Register register, Immediate immediate) {
    setRegister(register);
    setImmediate(new Immediate(immediate));
  }

  public void setImmediate(Immediate immediate) {
    this.immediate = immediate;
  }

  public void setRegister(Register register) {
    this.register = register;
  }

  public Register getRegister() {
    return register;
  }

  public Immediate getImmediate() {
    return new Immediate(immediate);
  }

  @Override
  public String toString() {
    return "[" + register.toString() + "+" + immediate.toString() + "]";
  }
}