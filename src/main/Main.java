package main;

import GUI.mainWindow.MainWindowController;
import GUI.mainWindow.MainWindow;
import compiler.Instruction;
import compiler.Parser;
import compiler.ParserException;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

public class Main extends Application {
  public static MainWindow mainWindow;

  public static void main(String[] args) throws Exception {
    launch(args);
//    test();
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
    setUserAgentStylesheet(STYLESHEET_MODENA);
    MainWindowController controller = new MainWindowController();
    mainWindow = new MainWindow(controller);
    mainWindow.setMaximized(true);
    mainWindow.getIcons().add(new Image("GUI/assets/icon.png"));
    mainWindow.show();
  }

//  private static void test() throws IOException {
//    FileReader fileReader = new FileReader("C:\\Users\\Nicky\\IdeaProjects\\Compiler\\src\\main\\input.txt");
//    BufferedReader bufferedReader = new BufferedReader(fileReader);
//    String line;
//    ArrayList<String> text = new ArrayList<>();
//    while ((line = bufferedReader.readLine()) != null) text.add(line);
//    for (String s : text) System.out.println(s);
//    try {
//      ArrayList<Instruction> ins = Parser.compile(text);
//      System.out.println("Instructions: ");
//      int i = 0 ;
//      for (Instruction in : ins) {
//        System.out.println("[" + i*4 + "] " + in.toString());
//        ++i;
//      }
//    } catch (ParserException e) {
//      System.out.println(e.getMessage());
//    }
//  }
}
