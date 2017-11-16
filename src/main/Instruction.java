package main;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.Arrays;

public class Instruction {
    private Operator operator;
    private ArrayList<Operand> operands;

    public Instruction(Operator operator, Operand... operands) {
        this.operands = new ArrayList<>();
        if (operator == Operator.HALT || operator == Operator.RET) {
            if (operands.length != 0)
                throw new InstructionException("Invalid operands for " + operator.name() + ". Required: (none)");
        } else if (operator == Operator.ADD || operator == Operator.SUB || operator == Operator.MUL || operator == Operator.DIV) {
            if (operands.length != 3 || !(operands[0] instanceof Register) || !(operands[1] instanceof Register) || !(operands[2] instanceof Register))
                throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [register] [register]");
        } else if (operator == Operator.ADDI || operator == Operator.SUBI || operator == Operator.MULI || operator == Operator.DIVI) {
            if (operands.length != 3 || !(operands[0] instanceof Register) || !(operands[1] instanceof Register) || !(operands[2] instanceof Immediate))
                throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [register] [immediate]");
        } else if (operator == Operator.MOVR) {
            if (operands.length != 2 || !(operands[0] instanceof Register) || !(operands[1] instanceof Register))
                throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [register]");
        } else if (operator == Operator.MOVI) {
            if (operands.length != 2 || !(operands[0] instanceof Register) || !(operands[1] instanceof Immediate))
                throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [immediate]");
        } else if (operator == Operator.MOV || operator == Operator.MOVB || operator == Operator.MOVH || operator == Operator.MOVL) {
            if (operands.length != 2 || !(operands[0] instanceof Memory) || !(operands[1] instanceof Register))
                throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [memory] [register]");
        } else if (operator == Operator.MOVM || operator == Operator.MOVMB || operator == Operator.MOVML || operator == Operator.MOVMHW) {
            if (operands.length != 2 || !(operands[1] instanceof Memory) || !(operands[0] instanceof Register))
                throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [memory]");
        } else if (operator == Operator.JMP) {
            if (operands.length != 1 || !(operands[0] instanceof Immediate))
                throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [memory]");
        } else if (operator == Operator.JMPR || operator == Operator.PUSH || operator == Operator.POP || operator == Operator.CALL) {
            if (operands.length != 1 || !(operands[0] instanceof Register))
                throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [memory]");
        } else if (operator == Operator.JE || operator == Operator.JLT || operator == Operator.JGT) {
            if (operands.length != 3 || !(operands[0] instanceof Register) || !(operands[1] instanceof Register) || !(operands[2] instanceof Immediate))
                throw new InstructionException("Invalid operands for " + operator.name() + ". Required: [register] [register] [immediate]");
        } else {
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

    public ArrayList<Operand> getOperands() {
        return new ArrayList<>(operands);
    }

    public Operator getOperator() {
        return operator;
    }
}
