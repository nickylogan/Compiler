package GUI;

import javafx.scene.Scene;
import javafx.stage.Stage;
import main.Program;
import main.SymbolTable;

import java.util.function.Consumer;

public class DebuggerWindow extends Stage {
  private Scene scene;
  private DebuggerWindowController controller;
  private Program program;

  public DebuggerWindow(Program program) {
    controller = new DebuggerWindowController(program);
    this.program = program;
    scene = new Scene(controller);
    setScene(scene);
    setTitle("Debugger window [" + program.getFileName() + "]");
    initialize();
  }

  public void initialize(){
    setOnCloseRequest(e -> {
      e.consume();
      program.destroy();
      close();
    });
  }
}
