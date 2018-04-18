package main;

import java.util.ArrayList;
import java.util.Arrays;

public class Instruction {
  private Operator operator;
  private ArrayList<Operand> operands;

  public Instruction(Operator operator, Operand... operands) {
    this.operands = new ArrayList<>();
    switch (operator) {
      case HALT:
      case RET:
        if (operands.length != 0)
          throw new InstructionException("Invalid operands for " + operator.name() + ". Required: (none)");
        break;
      case ADD:
      case SUB:
      case MUL:
      case DIV:
        if (operands.length != 3 || !(operands[0] instanceof Register) || !(operands[1] instanceof Register) || !(operands[2] instanceof Register))
          throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [register] [register]");
        break;
      case ADDI:
      case SUBI:
      case MULI:
      case DIVI:
      case SFLEFT:
      case SFRIGHT:
        if (operands.length != 3 || !(operands[0] instanceof Register) || !(operands[1] instanceof Register) || !(operands[2] instanceof Immediate))
          throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [register] [immediate]");
        break;
      case MOVR:
        if (operands.length != 2 || !(operands[0] instanceof Register) || !(operands[1] instanceof Register))
          throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [register]");
        break;
      case MOVI:
        if (operands.length != 2 || !(operands[0] instanceof Register) || !(operands[1] instanceof Immediate))
          throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [immediate]");
        break;
      case MOV:
      case MOVB:
      case MOVH:
      case MOVL:
        if (operands.length != 2 || !(operands[0] instanceof Memory) || !(operands[1] instanceof Register))
          throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [memory] [register]");
        break;
      case MOVM:
      case MOVMB:
      case MOVML:
      case MOVMHW:
        if (operands.length != 2 || !(operands[1] instanceof Memory) || !(operands[0] instanceof Register))
          throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [memory]");
        break;
      case JMP:
        if (operands.length != 1 || !(operands[0] instanceof Immediate))
          throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [memory]");
        break;
      case JMPR:
      case PUSH:
      case POP:
      case CALL:
        if (operands.length != 1 || !(operands[0] instanceof Register))
          throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [memory]");
        break;
      case JNE:
      case JE:
      case JLT:
      case JGT:
        if (operands.length != 3 || !(operands[0] instanceof Register) || !(operands[1] instanceof Register) || !(operands[2] instanceof Immediate))
          throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [register] [immediate]");
        break;
      default:
        throw new InstructionException("Operator " + operator.name() + " not implemented yet!");
    }
    setOperator(operator);
    setOperands(operands);
  }

  public void setOperator(Operator operator) {
    this.operator = operator;
  }

  public void setOperands(Operand... operands) {
    this.operands.clear();
    this.operands.addAll(Arrays.asList(operands));
  }

  public void setOperands(ArrayList<Operand> operands) {
    this.operands.clear();
    this.operands.addAll(operands);
  }

  public ArrayList<Operand> getOperands() {
    return new ArrayList<>(operands);
  }

  public Operator getOperator() {
    return operator;
  }

  public String toString() {
    StringBuilder s = new StringBuilder(operator.name() + " ");
    for(int i = 0; i<4-operator.name().length(); ++i) s.append(" ");
    int size = operands.size();
    for (int i = 0; i < operands.size(); ++i) {
      Operand o = operands.get(i);
      if (o instanceof Register) {
        if (i > 0) s.append(", ");
        s.append(((Register) o).name());
      } else if (o instanceof Immediate) {
        if (i > 0) s.append(", ");
        s.append(((Immediate) o).getValue());
      } else if (o instanceof Memory) {
        if (i > 0) s.append(", ");
        s.append("[").append(((Memory) o).getRegister().name()).append("+").append(((Memory) o).getImmediate().getValue()).append("]");
      }
    }
    return s.toString();
  }
}