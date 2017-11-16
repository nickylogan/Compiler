package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import static main.Operator.*;
import static main.Register.*;


public class Main {

    public static void main(String[] args) {
        ArrayList<Instruction> instructions = new ArrayList<>();
        instructions.add(new Instruction(MOVI, R1, new Immediate(56)));
        instructions.add(new Instruction(MOVI, R2, new Immediate(10)));
        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(0)), R2));
        instructions.add(new Instruction(MOVI, R2, new Immediate(7)));
        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(4)), R2));
        instructions.add(new Instruction(MOVI, R2, new Immediate(0)));
        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(8)), R2));
        instructions.add(new Instruction(MOVM, R8, new Memory(R1, new Immediate(0))));
        instructions.add(new Instruction(MOVM, R9, new Memory(R1, new Immediate(4))));
        instructions.add(new Instruction(JGT, R8, R9, new Immediate(48)));
        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(8)), R8));
        instructions.add(new Instruction(JMP, new Immediate(52)));
        instructions.add(new Instruction(MOV, new Memory(R1, new Immediate(8)), R9));
        instructions.add(new Instruction(HALT));
//        System.out.println(instructions.get(0).getOperator());
        Mapper.convertToMachineCode(instructions);
        
    }
}
