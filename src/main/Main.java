package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ArrayList<Instruction> instructions = new ArrayList<>();
        instructions.add(new Instruction(Operator.MOVI, Register.R1, new Immediate(128)));
        instructions.add(new Instruction(Operator.MOVI, Register.R2, new Immediate(10)));
        instructions.add(new Instruction(Operator.MOV, new Memory(Register.R1, new Immediate(0)), Register.R2));
//        instructions.add(new Instruction(Operator.MOVMH));
        instructions.add(new Instruction(Operator.MOV, new Immediate(100)));
        Mapper.convertToMachineCode(instructions);
    }
}
