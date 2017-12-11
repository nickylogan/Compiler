package main;

import statement.*;

import javax.swing.plaf.nimbus.State;
import java.io.*;
import java.util.ArrayList;

import static main.Operator.*;
import static main.Register.*;

public class Main {
    private static MultipleStatementNode root = new MultipleStatementNode();
    private static MultipleStatementNode parent = root;

    public static void main(String[] args) {

        ArrayList<String> lines = new ArrayList<>();
        String pseudocode = "";
        try {
            String path = "src/main/input.txt";
            File file = new File(path);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



//        for(InstructionOffset io : instructionOffsets){
//            System.out.println("["+io.getOffset()+"] "+io.getInstruction());
//        }
        ArrayList<Instruction> instructions = Parser.compile(lines);
        for (int i = 0; i < instructions.size(); ++i) {
            Instruction in = instructions.get(i);
            System.out.println("[" + ((i * 4) / 10) + ((i * 4) % 10) + "] " + in);
        }
        System.out.println();
        ArrayList<String> hex = Mapper.convertToHexString(instructions);
        for(String s : hex){
            System.out.println(s);
        }
        System.out.println();
        ArrayList<Long> longs = Mapper.convertHexStringToMachineCode(hex);
        for(Long  l : longs){
            System.out.println(l);
        }


//        instructions.size();

//        Parser.modifyVarLocations(instructions.size());

//        printAssemblyCode(instructions);
//        String s = Mapper.convertToMachineCode(instructions);
//        System.out.print(s);
        try {
			writeFile("src/main/output.mcd", longs);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }


    private static void printAssemblyCode(ArrayList<Instruction> instructions) {
        for (int i = 0; i < instructions.size(); ++i) {
            Instruction in = instructions.get(i);
            System.out.println("[" + ((i * Parser.LINE_SIZE) / 10) + ((i * Parser.LINE_SIZE) % 10) + "] " + in);
        }
    }

    public static void writeFile(String fileName, ArrayList<Long> machineCode) throws IOException {
        File file = new File(fileName);
        PrintWriter printWriter = new PrintWriter(file.getAbsolutePath());
        for(Long l : machineCode){
            printWriter.println(l);
        }
        printWriter.close();
    }


}
