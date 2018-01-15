package GUI;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.Instruction;
import main.Parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class Window extends Stage {
    private Scene scene;
    private MainWindowController mainWindow;
    private boolean saved;
    private File file;

    public Window(MainWindowController controller) {
        mainWindow = controller;
        scene = new Scene(controller);
        setSaved(true);
        file = null;
        setTitle("Compiler - Untitled");
        setScene(scene);
        initialize();
    }

    public MainWindowController getMainWindow() {
        return mainWindow;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

//    public void save() {
////        System.out.println("save");
//        if (file != null) {
//            mainWindow.writeToFile(file);
//            setSaved(true);
//        } else saveAs();
//    }
//
//    public void saveAs() {
////        System.out.println("saveas");
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().add(
//                new FileChooser.ExtensionFilter("Pseudocode", "*.cpr"));
//        if (file != null) fileChooser.setInitialDirectory(file.getParentFile());
//        fileChooser.setTitle("Save As");
//        File file = fileChooser.showSaveDialog(this);
////        System.out.println(file == null ? "null" : file.getName());
//        if (file != null) {
//            setSaved(true);
//            mainWindow.writeToFile(file);
//            this.file = file;
//            setTitle("Compiler - [" + this.file.getParent() + "] - " + this.file.getName());
//        }
//    }
//
//    public void open() {
//        if (!saved) {
//            Alert alert = new Alert(
//                    Alert.AlertType.CONFIRMATION,
//                    "" +
//                            "Do you want to save " +
//                            (file == null ? "Untitled" : file.getName()) +
//                            " before opening another file?",
//                    ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
//            alert.setTitle("Save file?");
//            alert.setHeaderText(null);
//            Optional<ButtonType> result = alert.showAndWait();
//            if (result.get() == ButtonType.YES) {
//                save();
//            } else if (result.get() == ButtonType.NO) {
//            } else if (result.get() == ButtonType.CANCEL) {
//                alert.close();
//                return;
//            }
//        }
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().addAll(
//                new FileChooser.ExtensionFilter("Pseudocode", "*.cpr")
//        );
//        if (file != null)
//            fileChooser.setInitialDirectory(file.getParentFile());
//        fileChooser.setTitle("Open");
//        File file = fileChooser.showOpenDialog(this);
////        System.out.println("file chosen");
//        if (file != null) {
//            this.file = file;
//            mainWindow = new MainWindowController();
//            mainWindow.loadFile(file);
//            scene = new Scene(mainWindow);
//            setTitle("Compiler - [" + this.file.getParent() + "] - " + this.file.getName());
//            setScene(scene);
//            initialize();
//        }
//    }
//
//    public void newFile() {
//        if (!saved) {
//            Alert alert = new Alert(
//                    Alert.AlertType.CONFIRMATION,
//                    "" +
//                            "Do you want to save " +
//                            (file == null ? "Untitled" : file.getName()) +
//                            " before creating a new file?",
//                    ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
//            alert.setTitle("Save file?");
//            alert.setHeaderText(null);
//            Optional<ButtonType> result = alert.showAndWait();
//            if (result.get() == ButtonType.YES) {
//                save();
//            } else if (result.get() == ButtonType.NO) {
//                alert.close();
//            } else if (result.get() == ButtonType.CANCEL) {
//                alert.close();
//                return;
//            }
//        }
//        this.file = null;
//        mainWindow = new MainWindowController();
//        scene = new Scene(mainWindow);
//        setTitle("Compiler - Untitled");
//        setScene(scene);
//        initialize();
//    }
//
//    public boolean quit() {
//        if (!saved) {
//            Alert alert = new Alert(
//                    Alert.AlertType.CONFIRMATION,
//                    "" +
//                            "Do you want to save " +
//                            (file == null ? "Untitled" : file.getName()) +
//                            " before exiting?",
//                    ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
//            alert.setTitle("Save file?");
//            alert.setHeaderText(null);
//            Optional<ButtonType> result = alert.showAndWait();
//            if (result.get() == ButtonType.YES) {
//                save();
//                close();
//                return true;
//            } else if (result.get() == ButtonType.NO) {
//                close();
//                return true;
//            } else if (result.get() == ButtonType.CANCEL) {
//                alert.close();
//                return false;
//            } else {
//                return false;
//            }
//        } else {
//            close();
//            return true;
//        }
//    }
//
//    public void compile() {
////        System.out.println("compile");
//        if (file == null) saveAs();
//        if (file != null) {
//            if(!saved) save();
//            String s = file.getAbsolutePath();
//            int pos = s.lastIndexOf('.');
//            s = s.substring(0, pos);
////            System.out.println(s);
//            File file = new File(s + ".mcd");
//            mainWindow.compile(file);
//        }
//    }


    private void initialize() {
        setResizable(true);
        setMaximized(false);
        setMaximized(true);
//        mainWindow.getNewFile().setOnAction(e -> newFile());
//        mainWindow.getSave().setOnAction(e -> save());
//        mainWindow.getOpen().setOnAction(e -> open());
//        mainWindow.getClose().setOnAction(e -> close());
//        mainWindow.getCompile().setOnAction(e -> compile());
    }
}
