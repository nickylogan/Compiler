package main;

import GUI.MainWindowController;
import GUI.Window;
import javafx.application.Application;
import javafx.stage.Stage;
import statement.*;

import javax.swing.plaf.nimbus.State;
import java.io.*;
import java.util.ArrayList;

import static main.Operator.*;
import static main.Register.*;

public class Main extends Application {
  public static Window window;

  public static void main(String[] args) {
    launch(args);
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
    for (Long l : machineCode) {
      printWriter.println(l);
    }
    printWriter.close();
  }


  @Override
  public void start(Stage primaryStage) throws Exception {
    MainWindowController controller = new MainWindowController();
    window = new Window(controller);
//    window.initialize();
    window.show();
  }
}
