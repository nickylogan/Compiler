package main;

import java.util.ArrayList;

public class InstructionOffset {
    private Instruction instruction;
    private Integer offset;

    public InstructionOffset(Instruction instruction, Integer offset){
        setInstruction(instruction);
        setOffset(offset);
    }

    public Integer getOffset() {
        return offset;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public void setOffset(Integer offset) {
        int delta = offset - this.offset;
        Operator op = instruction.getOperator();
        switch (op){
            case JE:
            case JNE:
            case JGT:
            case JLT:
            case JMP:
                ArrayList<Operand> o = instruction.getOperands();
                int k = ((Immediate)o.get(o.size()-1)).getValue().getValue();
                ((Immediate)o.get(o.size()-1)).setValue(k+delta);
        }
        this.offset = offset;
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }
}
