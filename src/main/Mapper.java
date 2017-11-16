package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Mapper {
    private static String toHexStringWithLength(int value, int length){
        StringBuilder sb = new StringBuilder();
        String hex = Integer.toHexString(value);
        for(int i = 0; i<length-hex.length(); ++i) sb.append("0");
        sb.append(hex);
        return sb.toString();
    }
    private static String convertToMachineCodeLine(Instruction instruction) throws MapperException {
        StringBuilder sb = new StringBuilder("0x");
        Operator op = instruction.getOperator();
        final ArrayList<Operand> operands = instruction.getOperands();
        sb.append(op.getOpCode());
        if (op == Operator.HALT || op == Operator.RET) {
            sb.append("000000");
        } else if (op == Operator.ADD || op == Operator.SUB || op == Operator.MUL || op == Operator.DIV) {
            sb.append(((Register) operands.get(0)).getCode());
            sb.append(((Register) operands.get(1)).getCode());
            sb.append(((Register) operands.get(2)).getCode());
            sb.append("000");
        } else if (op == Operator.ADDI || op == Operator.SUBI || op == Operator.MULI || op == Operator.DIVI) {
            sb.append(((Register) operands.get(0)).getCode());
            sb.append(((Register) operands.get(1)).getCode());
            sb.append(toHexStringWithLength(((Immediate) operands.get(2)).getValue(), 4));
        } else if (op == Operator.MOVR) {
            sb.append(((Register) operands.get(0)).getCode());
            sb.append(((Register) operands.get(1)).getCode());
            sb.append("0000");
        } else if (op == Operator.MOVI) {
            sb.append(((Register) operands.get(0)).getCode());
            sb.append(toHexStringWithLength(((Immediate) operands.get(1)).getValue(), 5));
        } else if (op == Operator.MOV || op == Operator.MOVB || op == Operator.MOVH || op == Operator.MOVL) {
            sb.append(((Memory) operands.get(0)).getRegister().getCode());
            sb.append(((Register) operands.get(1)).getCode());
            sb.append(toHexStringWithLength(((Memory) operands.get(0)).getImmediate().getValue(), 4));
        } else if (op == Operator.MOVM || op == Operator.MOVMB || op == Operator.MOVML || op == Operator.MOVMHW) {
            sb.append(((Register) operands.get(0)).getCode());
            sb.append(((Memory) operands.get(1)).getRegister().getCode());
            sb.append(toHexStringWithLength(((Memory) operands.get(1)).getImmediate().getValue(), 4));
        } else if (op == Operator.JMP) {
            sb.append(toHexStringWithLength(((Immediate) operands.get(0)).getValue(), 6));
        } else if (op == Operator.JMPR || op == Operator.PUSH || op == Operator.POP || op == Operator.CALL) {
            sb.append(((Register)operands.get(0)).getCode());
            sb.append("00000");
        } else if (op == Operator.JE || op == Operator.JLT || op == Operator.JGT){
            sb.append(((Register) operands.get(0)).getCode());
            sb.append(((Register) operands.get(1)).getCode());
            sb.append(toHexStringWithLength(((Immediate)operands.get(2)).getValue(), 4));
        } else {
            throw new MapperException("Instruction " + op.name() + " not implemented!");
        }

        return sb.toString();
    }
    public static void convertToMachineCode(ArrayList<Instruction> instructions) {
        for(Instruction instruction: instructions){
            System.out.println(Integer.decode(convertToMachineCodeLine(instruction)));
        }
    }
}
