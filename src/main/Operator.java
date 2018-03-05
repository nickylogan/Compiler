package main;

public enum Operator {
  ADD("00"), ADDI("01"),
  MOVR("02"), MOVI("03"), MOV("04"), MOVM("05"), MOVB("06"), MOVMB("07"),
  MOVH("08"), MOVMH("09"), MOVL("0A"), MOVML("0B"), MOVD("0C"), MOVMD("0D"),
  PUSH("0E"), POP("0F"), PUSHF("10"), POPF("11"),
  INTR("12"), IN("13"), OUT("14"),
  JMP("15"), JMPR("16"), JE("17"), JNE("18"), JLT("19"), JGT("1A"),
  SUB("1B"), SUBI("1C"), MOVMHW("1D"),
  MUL("1E"), MULI("1F"), DIV("20"), DIVI("21"),
  CALL("22"), RET("23"),
  SFLEFT("24"), SFRIGHT("25"),
  HALT("7F");
  private String opCode;

  Operator(String opCode) {
    this.opCode = opCode;
  }

  public String getOpCode() {
    return opCode;
  }
}
