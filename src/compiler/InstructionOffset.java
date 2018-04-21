package compiler;

import java.util.ArrayList;

public class InstructionOffset {
    private Instruction instruction;
    private Integer offset;
    private String label = "";

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
        if(this.offset!=null) {
            int delta = offset - this.offset;
            Operator op = instruction.getOperator();
            switch (op) {
                case JE:
                case JNE:
                case JGT:
                case JLT:
                case JMP:
                    ArrayList<Operand> o = instruction.getOperands();
                    int k = ((Immediate) o.get(o.size() - 1)).getValue();
                    ((Immediate) o.get(o.size() - 1)).setValue(k + delta);
            }
        }
        this.offset = offset;
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
