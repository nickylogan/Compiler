package main;

import GUI.mainWindow.MainWindowController;
import GUI.mainWindow.MainWindow;
import compiler.Instruction;
import compiler.Parser;
import compiler.ParserException;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import utils.StringUTILS;

import java.io.*;
import java.util.ArrayList;

public class Main extends Application {
  private static MainWindow mainWindow;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    setUserAgentStylesheet(STYLESHEET_MODENA);
    MainWindowController controller = new MainWindowController();
    mainWindow = new MainWindow(controller);
    mainWindow.setMaximized(true);
    mainWindow.getIcons().add(new Image("GUI/assets/icon.png"));
    mainWindow.show();
    mainWindow.getController().getAboutMenu().setOnAction(e -> getHostServices().showDocument("https://github.com/Log-baseE/Compiler"));
  }
}
