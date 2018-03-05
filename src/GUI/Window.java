package GUI;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class Window extends Stage{
  private Scene scene;
  private MainWindowController mainWindow;
  public static final String FILE_TYPE = "Pseudocode";
  public static final String FILE_EXTENSION = "*.cpr";

  //States
  BooleanProperty saved = new SimpleBooleanProperty();
  private File file;

  public Window(MainWindowController controller) {
    mainWindow = controller;
    scene = new Scene(controller);
    setScene(scene);
    mainWindow.observableAtMarkedPosition().addListener((e, o, n) -> {
      setSaved(n);
    });
    initialize();
  }

  public MainWindowController getController() {
    return mainWindow;
  }

  public void setSaved(boolean saved) {
    if(saved) mainWindow.markSaved();
    this.saved.set(saved);
  }

  public boolean getSaved() {
    return this.saved.get();
  }

  public BooleanProperty savedProperty() {
    return saved;
  }

  public void save() {
    System.out.println("Save");
    if (file != null) {
      mainWindow.writeToFile(this.file);
    } else saveAs();
    mainWindow.markSaved();
  }

  public void saveAs() {
    System.out.println("Save as");
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter(FILE_TYPE, FILE_EXTENSION));
    if (file != null) fileChooser.setInitialDirectory(file.getParentFile());
    fileChooser.setTitle("Save As");
    File file = fileChooser.showSaveDialog(this);
    System.out.println(file == null ? "null" : file.getName());
    if (file != null) {
      mainWindow.writeToFile(file);
      this.file = file;
      setTitle("Compiler - [" + this.file.getParent() + "] - " + this.file.getName());
      mainWindow.setTitle(this.file.getName());
      mainWindow.markSaved();
    }
  }

  public void open() {
    System.out.println("Open file");
    if (!getSaved()) {
      Alert alert = new Alert(
          Alert.AlertType.CONFIRMATION,
          "" +
              "Do you want to save " +
              (file == null ? "Untitled" : file.getName()) +
              " before opening another file?",
          ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
      alert.setTitle("Save file?");
      alert.setHeaderText(null);
      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.YES) {
        save();
      } else if (!result.isPresent() || result.get() == ButtonType.CANCEL) {
        alert.close();
        return;
      }
    }
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Pseudocode", "*.cpr")
    );
    if (file != null)
      fileChooser.setInitialDirectory(file.getParentFile());
    fileChooser.setTitle("Open");
    File file = fileChooser.showOpenDialog(this);
//        System.out.println("file chosen");
    if (file != null) {
      this.file = file;
      mainWindow.loadFile(file);
      setTitle("Compiler - [" + this.file.getParent() + "] - " + this.file.getName());
    }
  }

  public void newFile() {
    System.out.println("New file");
    if (!getSaved()) {
      Alert alert = new Alert(
          Alert.AlertType.CONFIRMATION,
          "" +
              "Do you want to save " +
              (file == null ? "Untitled" : file.getName()) +
              " before creating a new file?",
          ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
      alert.setTitle("Save file?");
      alert.setHeaderText(null);
      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.YES) save();
      else if (result.isPresent() && result.get() == ButtonType.NO) alert.close();
      else if (!result.isPresent() || result.get() == ButtonType.CANCEL) {
        alert.close();
        return;
      }
      reset();
    }
  }

  public boolean quit() {
    if (!getSaved()) {
      Alert alert = new Alert(
          Alert.AlertType.CONFIRMATION,
          "" +
              "Do you want to save " +
              (file == null ? "Untitled" : file.getName()) +
              " before exiting?",
          ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
      alert.setTitle("Save file?");
      alert.setHeaderText(null);
      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.YES) {
        save();
        close();
        return true;
      } else if (result.isPresent() && result.get() == ButtonType.NO) {
        close();
        return true;
      } else if (!result.isPresent() || result.get() == ButtonType.CANCEL) {
        alert.close();
        return false;
      } else {
        //unreachable
        return false;
      }
    } else {
      close();
      return true;
    }
  }

  public void compile() {
    System.out.println("Compile");
    if (file == null) saveAs();
    if (file != null) {
      if (!getSaved()) save();
      String s = file.getAbsolutePath();
      int pos = s.lastIndexOf('.');
      s = s.substring(0, pos);
//      System.out.println(s);
      File file = new File(s + ".mcd");
      mainWindow.compileToFile(file);
    }
  }

  public void setFile(File file) {
    this.file = file;
  }

  public File getFile() {
    return this.file;
  }

  private void reset() {
    setSaved(true);
    setFile(null);
    setTitle("Compiler - Untitled");
    mainWindow.reset();
  }

  public void initialize() {
    reset();
    mainWindow.getNewMenu().setOnAction(e -> newFile());
    mainWindow.getOpenMenu().setOnAction(e -> open());
    mainWindow.getSaveMenu().setOnAction(e -> save());
    mainWindow.getSaveAsMenu().setOnAction(e -> saveAs());
    mainWindow.getCompileMenu().setOnAction(e -> compile());
    mainWindow.getExitMenu().setOnAction(e -> quit());
  }
}

